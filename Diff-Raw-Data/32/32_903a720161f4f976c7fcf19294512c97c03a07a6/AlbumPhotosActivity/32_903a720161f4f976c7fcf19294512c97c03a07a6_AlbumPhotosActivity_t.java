 package com.lopefied.pepemon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.lopefied.pepemon.adapter.PhotoListAdapter;
 import com.lopefied.pepemon.adapter.PhotoListAdapter.IPhotoListAdapter;
 import com.lopefied.pepemon.model.Photo;
 import com.lopefied.pepemon.task.GetAlbumPhotosTask;
 import com.lopefied.pepemon.task.GetAlbumPhotosTask.IAlbumPhotosDownloader;
 
 /**
  * 
  * @author Lope Chupijay Emano
  * 
  */
 public class AlbumPhotosActivity extends Activity {
     public static final String TAG = AlbumPhotosActivity.class.getSimpleName();
     public static final String ALBUM_ID = "album_id";
 
     private SharedPreferences mPrefs;
     private ListView listView;
     private Integer currentPage = 0;
     private String albumID = null;
     private String accessToken = null;
     private ProgressDialog progressDialog;
     private Boolean isDownloadingStuff = false;
     private PhotoListAdapter adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         init();
     }
 
     private void initExtras() {
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             albumID = extras.getString(ALBUM_ID);
             Log.i(TAG, "Got albumID : " + albumID);
             /*
              * Get existing access_token if any
              */
             mPrefs = getSharedPreferences("com.lopefied.pepemon",
                     MODE_WORLD_READABLE);
             accessToken = mPrefs.getString("access_token", null);
             Log.i(TAG, "Got access token : " + accessToken);
         }
     }
 
     private void init() {
         initExtras();
         progressDialog = new ProgressDialog(this);
         listView = (ListView) findViewById(R.id.listView);
         final String accessToken = mPrefs.getString("access_token", null);
         IPhotoListAdapter albumListAdapterListener = new IPhotoListAdapter() {
             @Override
             public String getFBToken() {
                 return accessToken;
             }
 
             @Override
             public void selectPhoto(Photo photo) {
                 Intent intent = new Intent();
                 intent.setClass(getApplicationContext(),
                         ViewPhotoActivity.class);
                 intent.putExtra(ViewPhotoActivity.PHOTO_URL,
                         photo.getPhotoURL());
                 startActivity(intent);
             }
         };
         adapter = new PhotoListAdapter(this, R.layout.item_album,
                 new ArrayList<Photo>(), albumListAdapterListener);
         listView.setAdapter(adapter);
 
         if (albumID != null) {
             downloadAlbumPhotos(accessToken, albumID, currentPage);
         } else {
             Log.e(TAG, "Null album ID received");
         }
 
         listView.setOnScrollListener(new OnScrollListener() {
 
             @Override
             public void onScrollStateChanged(AbsListView arg0, int arg1) {
             }
 
             @Override
             public void onScroll(AbsListView listView, int firstVisibleItem,
                     int visibleItemCount, int totalItemCount) {
                 switch (listView.getId()) {
                 case R.id.listView:
                     final int lastItem = firstVisibleItem + visibleItemCount;
                     if ((lastItem >= totalItemCount - 2)
                             && (totalItemCount != 0)) {
                         Photo photo = (Photo) listView.getAdapter().getItem(
                                 totalItemCount - 1);
                         if (photo != null) {
                             if (!isDownloadingStuff) {
                                 downloadAlbumPhotos(accessToken, albumID,
                                        currentPage += 11);
                                 Toast.makeText(getApplicationContext(),
                                        "Loading more items..",
                                         Toast.LENGTH_LONG).show();
                             }
                         }
                     }
                 }
             }
         });
     }
 
     private void downloadAlbumPhotos(final String accessToken,
             final String albumID, Integer page) {
         Log.i(TAG, "Downloading new photos starting page : " + page);
         isDownloadingStuff = true;
         IAlbumPhotosDownloader albumPhotosDownloaderListener = new IAlbumPhotosDownloader() {
 
             @Override
             public void noMoreAlbumPhotos() {
                 progressDialog.dismiss();
             }
 
             @Override
             public String getFBAccessToken() {
                 return accessToken;
             }
 
             @Override
             public void foundAlbumPhotos(List<Photo> photoList) {
                 Log.i(TAG, "Received photos : " + photoList.size());
                 if (photoList.size() > 0) {
                     downloadAndDisplayPictures(photoList);
                     isDownloadingStuff = false;
                 } else {
                     Toast.makeText(getApplicationContext(),
                             "No more photos to load", Toast.LENGTH_LONG).show();
                 }
             }
         };
         progressDialog.setCancelable(false);
         progressDialog.setMessage("Downloading photos..");
         progressDialog
                 .setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
         progressDialog.setProgress(0);
         progressDialog.setMax(100);
         GetAlbumPhotosTask task = new GetAlbumPhotosTask(
                 albumPhotosDownloaderListener, progressDialog, accessToken,
                 page);
         task.execute(albumID);
     }
 
     private void downloadAndDisplayPictures(List<Photo> photoList) {
        adapter.getList();
         adapter.addAll(photoList);
         adapter.notifyDataSetChanged();
     }
 
 }
