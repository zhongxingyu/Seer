 package net.sourcewalker.smugview.gui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import net.sourcewalker.smugview.ApiConstants;
 import net.sourcewalker.smugview.R;
 import net.sourcewalker.smugview.data.Cache;
 import net.sourcewalker.smugview.parcel.AlbumInfo;
 import net.sourcewalker.smugview.parcel.Extras;
 import net.sourcewalker.smugview.parcel.ImageInfo;
 import net.sourcewalker.smugview.parcel.LoginResult;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.kallasoft.smugmug.api.json.entity.Album;
 import com.kallasoft.smugmug.api.json.v1_2_0.APIVersionConstants;
 import com.kallasoft.smugmug.api.json.v1_2_0.albums.Get;
 import com.kallasoft.smugmug.api.json.v1_2_0.albums.Get.GetResponse;
 
 public class AlbumListActivity extends ListActivity {
 
     private AlbumListAdapter listAdapter;
     private LoginResult login;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.albumlist);
 
         login = (LoginResult) getIntent().getExtras().get(Extras.EXTRA_LOGIN);
 
         listAdapter = new AlbumListAdapter();
         setListAdapter(listAdapter);
 
         startGetAlbums();
     }
 
     private void startGetAlbums() {
         new GetAlbumsTask().execute(login);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         AlbumInfo album = (AlbumInfo) listAdapter.getItem(position);
         Intent intent = new Intent(this, AlbumActivity.class);
         intent.putExtra(Extras.EXTRA_LOGIN, login);
         intent.putExtra(Extras.EXTRA_ALBUM, album);
         startActivity(intent);
     }
 
     private class GetAlbumsTask extends
             AsyncTask<LoginResult, Void, List<AlbumInfo>> {
 
         @Override
         protected void onPreExecute() {
             setProgressBarIndeterminateVisibility(true);
         }
 
         @Override
         protected List<AlbumInfo> doInBackground(LoginResult... params) {
             LoginResult login = params[0];
             List<AlbumInfo> result = new ArrayList<AlbumInfo>();
             GetResponse response = new Get().execute(
                     APIVersionConstants.SECURE_SERVER_URL, ApiConstants.APIKEY,
                     login.getSession(), false);
             if (!response.isError()) {
                 for (Album a : response.getAlbumList()) {
                     result.add(new AlbumInfo(a));
                 }
             }
             return result;
         }
 
         @Override
         protected void onPostExecute(List<AlbumInfo> result) {
             setProgressBarIndeterminateVisibility(false);
 
             listAdapter.clear();
             listAdapter.addAll(result);
         }
     }
 
     private class AlbumListAdapter implements ListAdapter {
 
         private List<AlbumInfo> albums = new ArrayList<AlbumInfo>();
         private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();
         private Random rnd = new Random();
 
         public void addAll(Collection<? extends AlbumInfo> items) {
             albums.addAll(items);
             notifyObservers();
         }
 
         public void clear() {
             albums.clear();
             notifyObservers();
         }
 
         private void notifyObservers() {
             for (DataSetObserver observer : observers) {
                 observer.onChanged();
             }
         }
 
         @Override
         public boolean areAllItemsEnabled() {
             return true;
         }
 
         @Override
         public boolean isEnabled(int position) {
             return true;
         }
 
         @Override
         public int getCount() {
             return albums.size();
         }
 
         @Override
         public Object getItem(int position) {
             return albums.get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return albums.get(position).getId();
         }
 
         @Override
         public int getItemViewType(int position) {
             return 0;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             AlbumListHolder holder;
             if (convertView == null) {
                 convertView = View.inflate(AlbumListActivity.this,
                         R.layout.albumlist_row, null);
                 holder = new AlbumListHolder();
                 holder.title = (TextView) convertView
                         .findViewById(R.id.albumrow_title);
                 holder.desc = (TextView) convertView
                         .findViewById(R.id.albumrow_desc);
                 holder.example = (ImageView) convertView
                         .findViewById(R.id.albumrow_image);
                 convertView.setTag(holder);
             } else {
                 holder = (AlbumListHolder) convertView.getTag();
             }
             AlbumInfo item = albums.get(position);
             holder.title.setText(item.getTitle());
             holder.desc.setText(item.getDescription());
             Drawable thumbnail = null;
             List<ImageInfo> cache = Cache.getAlbumImages(item);
            if (cache != null) {
                 ImageInfo random = cache.get(rnd.nextInt(cache.size()));
                 thumbnail = random.getThumbnail();
             }
             holder.example.setImageDrawable(thumbnail);
             return convertView;
         }
 
         @Override
         public int getViewTypeCount() {
             return 1;
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         @Override
         public boolean isEmpty() {
             return albums.size() == 0;
         }
 
         @Override
         public void registerDataSetObserver(DataSetObserver observer) {
             observers.add(observer);
         }
 
         @Override
         public void unregisterDataSetObserver(DataSetObserver observer) {
             observers.remove(observer);
         }
     }
 
     private class AlbumListHolder {
 
         public TextView title;
         public TextView desc;
         public ImageView example;
     }
 }
