 package com.lopefied.pepemon;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.lopefied.pepemon.adapter.AlbumListAdapter;
 import com.lopefied.pepemon.adapter.AlbumListAdapter.IAlbumListAdapter;
 import com.lopefied.pepemon.db.DBHelper;
 import com.lopefied.pepemon.db.model.Album;
 import com.lopefied.pepemon.service.AlbumService;
 import com.lopefied.pepemon.service.impl.AlbumServiceImpl;
 import com.lopefied.pepemon.task.GetAlbumsFQLTask;
 import com.lopefied.pepemon.task.GetAlbumsTask.IAlbumDownloader;
 
 /**
  * 
  * @author Lope Chupijay Emano
  * 
  */
 public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private Facebook facebook = new Facebook("195085947295131");
     private static final String FACEBOOK_ID = "pepemon3";
 
     private SharedPreferences mPrefs;
     private ListView listView;
     private DBHelper dbHelper;
     private AlbumService albumService;
     private AlbumListAdapter adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         dbHelper = (DBHelper) OpenHelperManager.getHelper(this, DBHelper.class);
         try {
             albumService = new AlbumServiceImpl(dbHelper.getAlbumDao());
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         /*
          * Get existing access_token if any
          */
         mPrefs = this.getSharedPreferences("com.lopefied.pepemon",
                 MODE_WORLD_READABLE);
         String accessToken = mPrefs.getString("access_token", null);
         long expires = mPrefs.getLong("access_expires", 0);
         if (accessToken != null) {
             facebook.setAccessToken(accessToken);
         }
         if (expires != 0) {
             facebook.setAccessExpires(expires);
         }
 
         /*
          * Only call authorize if the access_token has expired.
          */
         if (!facebook.isSessionValid()) {
 
             facebook.authorize(this, new String[] {}, new DialogListener() {
                 @Override
                 public void onComplete(Bundle values) {
                     SharedPreferences.Editor editor = mPrefs.edit();
                     editor.putString("access_token", facebook.getAccessToken());
                     editor.putLong("access_expires",
                             facebook.getAccessExpires());
                     editor.commit();
                    initAdapter(facebook.getAccessToken());
                    loadAlbums(facebook.getAccessToken());
                 }
 
                 @Override
                 public void onFacebookError(FacebookError error) {
                    Log.e(TAG, "onFBError");
                    Toast.makeText(getApplicationContext(),
                            "A facebook-related error occured",
                            Toast.LENGTH_LONG).show();
                 }
 
                 @Override
                 public void onError(DialogError e) {
                     e.printStackTrace();
                 }
 
                 @Override
                 public void onCancel() {
                    Log.e(TAG, "onCancel");
                    Toast.makeText(
                            getApplicationContext(),
                            "You must login to facebook to be able to download photos",
                            Toast.LENGTH_LONG).show();
                 }
             });
         }
         if (accessToken != null) {
             initAdapter(accessToken);
             loadAlbums(accessToken);
         }
     }
 
     public void initAdapter(final String accessToken) {
         listView = (ListView) findViewById(R.id.listView);
         IAlbumListAdapter albumListListener = new IAlbumListAdapter() {
             @Override
             public String getFBToken() {
                 return accessToken;
             }
 
             @Override
             public void selected(Album album) {
                 launchAlbumPhotoList(album.getAlbumID());
             }
         };
         adapter = new AlbumListAdapter(this, R.layout.item_album,
                 new ArrayList<Album>(), albumListListener, accessToken);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1,
                     int position, long arg3) {
                 launchAlbumPhotoList(adapter.getItem(position).getAlbumID());
             }
         });
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (adapter != null)
             adapter.clearCache();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (dbHelper != null) {
             OpenHelperManager.releaseHelper();
             dbHelper = null;
         }
     }
 
     private void loadAlbums(String accessToken) {
         if (albumService.isCached()) {
             downloadAndDisplayPictures(albumService.getAlbums(), accessToken);
         } else {
             downloadAlbum(accessToken);
         }
     }
 
     private void downloadAlbum(final String accessToken) {
         final ProgressDialog progressDialog = new ProgressDialog(this);
         IAlbumDownloader albumDownloaderListener = new IAlbumDownloader() {
 
             @Override
             public void noMoreAlbums() {
                 progressDialog.dismiss();
             }
 
             @Override
             public String getFBAccessToken() {
                 return accessToken;
             }
 
             @Override
             public void foundAlbums(List<Album> albumList) {
                 downloadAndDisplayPictures(albumList, accessToken);
             }
         };
         progressDialog.setCancelable(false);
         progressDialog.setMessage("Downloading albums..");
         progressDialog
                 .setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
         progressDialog.setProgress(0);
         progressDialog.setMax(100);
         GetAlbumsFQLTask task = new GetAlbumsFQLTask(albumService,
                 albumDownloaderListener, progressDialog, accessToken);
         task.execute(FACEBOOK_ID);
     }
 
     private void downloadAndDisplayPictures(List<Album> albumList,
             final String accessToken) {
         adapter.clear();
         adapter.addAll(albumList);
         adapter.notifyDataSetChanged();
     }
 
     public void launchAlbumPhotoList(String albumID) {
         Intent intent = new Intent();
         intent.setClass(getApplicationContext(), AlbumPhotosActivity.class);
         intent.putExtra(AlbumPhotosActivity.ALBUM_ID, albumID);
         startActivity(intent);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
 
 }
