package startactivityforresult.shawnerlsala.packagecom.midtermexam;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ShawnErl on 13/02/2017.
 */

public class HomeFragment extends Fragment {
    private List<Album> albums;

    private EditText mSearch;
    private AlbumAdapter albumAdapter;
    private RecyclerView recyclerView;
    private View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        albums = new ArrayList<Album>();
        mSearch = (EditText) v.findViewById(R.id.searchTxt);
        new SearchTask(getContext());
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        albumAdapter = new AlbumAdapter(getContext(), albums);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(albumAdapter);

        setHasOptionsMenu(true);
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                SearchTask searchTask = new SearchTask(getContext());
                searchTask.execute(mSearch.getText().toString());
                return false;
            }
        });
        this.view=v;
        return v;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("Inside", "oncreate");
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Inside", "Onoptionsselected");
        if(item.getItemId()==R.id.clear){
            Log.d("Clear", "Inside");
            clear();
        }
        return super.onOptionsItemSelected(item);
    }

    public void clear(){
        view.findViewById(R.id.tvNo).setVisibility(View.VISIBLE);
        mSearch.setText("");
        int size = this.albums.size();
        albums.clear();
        albumAdapter.notifyItemRangeRemoved(0, size);
    }


    public class SearchTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog dialog;
        private Context context;
        private int added;
        boolean error = false;

        public SearchTask(Context context) {
            this.context = context;
            dialog = new ProgressDialog(context);
        }


        protected void onPreExecute() {
            albumAdapter.notifyDataSetChanged();
            dialog.setMessage("Searching");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            albums.clear();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (error) {
                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
            }
            albumAdapter.notifyDataSetChanged();
            view.findViewById(R.id.tvNo).setVisibility(View.INVISIBLE);
//            recyclerView.setVisibility(View.VISIBLE);
//            findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);

//            else{

//            }
        }

        protected Boolean doInBackground(final String... args) {

            boolean flag = true;
            try {
                String queryValue = args[0].replace(" ", "%20");
                String urlForSearch ="http://ws.audioscrobbler.com/2.0/?method=album.search&album=" + queryValue + "&api_key=220b24b1490e374d3845b966fa7f0076&format=json";
                try {
                    HttpClient client = new DefaultHttpClient();
                    ResponseHandler<String> handler = new BasicResponseHandler();
                    HttpPost request = new HttpPost(urlForSearch);


                    String httpResponse = client.execute(request, handler);


                    JSONObject finalResult = new JSONObject(httpResponse);
                    JSONObject temp = finalResult.getJSONObject("results");
                    JSONObject temp2 = temp.getJSONObject("albummatches");
                    JSONArray lists = temp2.getJSONArray("album");

                    for (int init = 0; init < lists.length(); init++) {
                        Album album = new Album();
                        temp = lists.getJSONObject(init);
                        album.setAlbumName(temp.getString("name"));
                        album.setArtist(temp.getString("artist"));
                        album.setUrl(temp.getString("url"));
                        JSONArray images = temp.getJSONArray("image");
                        album.setImageUrl(images.getJSONObject(1).getString("#text"));
                        albums.add(album);
                        Log.d("name", albums.get(init).getAlbumName());
                        Log.d("artist", albums.get(init).getArtist());
                        Log.d("image url", albums.get(init).getImageUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    error = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("List size", Integer.toString(albums.size()));
            return true;
        }
    }
}