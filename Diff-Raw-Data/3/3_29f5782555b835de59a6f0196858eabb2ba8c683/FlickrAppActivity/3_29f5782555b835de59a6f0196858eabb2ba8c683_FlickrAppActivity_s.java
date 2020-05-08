 package com.hrupin.flickrapp.ui.activities;
 
 import java.util.Collection;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.gmail.yuyang226.flickr.oauth.OAuth;
 import com.gmail.yuyang226.flickr.oauth.OAuthToken;
 import com.gmail.yuyang226.flickr.people.User;
 import com.gmail.yuyang226.flickr.photos.GeoData;
 import com.gmail.yuyang226.flickr.photos.Note;
 import com.gmail.yuyang226.flickr.photos.Photo;
 import com.gmail.yuyang226.flickr.photos.PhotoList;
 import com.hrupin.flickrapp.R;
 import com.hrupin.flickrapp.UserPreferences;
 import com.hrupin.flickrapp.adapters.GridThumbsAdapter;
 import com.hrupin.flickrapp.development.Logger;
 import com.hrupin.flickrapp.images.ImageUtils.DownloadedDrawable;
 import com.hrupin.flickrapp.task.ImageDownloadTask;
 import com.hrupin.flickrapp.task.LoadPhotostreamTask;
 import com.hrupin.flickrapp.task.LoadPhotostreamTask.LoadListener;
 
 public class FlickrAppActivity extends Activity implements OnItemClickListener, OnClickListener {
 
     private static final String TAG = FlickrAppActivity.class.getSimpleName();
     private GridView gridView;
     private FrameLayout frameLayoutFullScreenImageWrapper;
     private ImageView imageViewFullScreen;
     private ImageButton imageButtonShowOnMap;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         gridView = (GridView) findViewById(R.id.gridViewThumbs);
         frameLayoutFullScreenImageWrapper = (FrameLayout) findViewById(R.id.frameLayoutFullScreenImageWrapper);
         imageViewFullScreen = (ImageView) findViewById(R.id.imageViewFullScreen);
         imageViewFullScreen.setOnClickListener(this);
         imageButtonShowOnMap = (ImageButton) findViewById(R.id.imageButtonShowOnMap);
         enableFullscreenMode(false);
     }
 
     private void openPhotoInFullScreen(Photo photo) {
         if (photo != null) {
             enableFullscreenMode(true);
             ImageDownloadTask task = new ImageDownloadTask(imageViewFullScreen);
             Drawable drawable = new DownloadedDrawable(task);
             imageViewFullScreen.setImageDrawable(drawable);
             task.execute(photo.getLargeUrl());
 
             GeoData geoData = photo.getGeoData();
             if (geoData != null) {
                 imageButtonShowOnMap.setVisibility(View.VISIBLE);
                 imageButtonShowOnMap.setTag(geoData);
             } else {
                 imageButtonShowOnMap.setVisibility(View.GONE);
             }
         } else {
             enableFullscreenMode(false);
             imageViewFullScreen.setImageDrawable(null);
         }
 
     }
 
     private void enableFullscreenMode(boolean isEnable) {
         if (isEnable) {
             frameLayoutFullScreenImageWrapper.setVisibility(View.VISIBLE);
             gridView.setEnabled(false);
             imageViewFullScreen.setClickable(true);
             imageButtonShowOnMap.setVisibility(View.VISIBLE);
         } else {
             frameLayoutFullScreenImageWrapper.setVisibility(View.GONE);
             gridView.setEnabled(true);
             imageViewFullScreen.setClickable(false);
             imageButtonShowOnMap.setVisibility(View.GONE);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         successLoginFlow();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         gridView.setAdapter(null);
     }
 
     private void successLoginFlow() {
         OAuth oAuth = UserPreferences.getOAuthToken();
         if (oAuth != null) {
             User user = oAuth.getUser();
             OAuthToken token = oAuth.getToken();
             if (user == null || user.getId() == null || token == null || token.getOauthToken() == null || token.getOauthTokenSecret() == null) {
                 pleaseLoginFirst();
                 return;
             }
             load(oAuth);
         } else {
             pleaseLoginFirst();
         }
     }
 
     private void pleaseLoginFirst() {
         Toast.makeText(this, "Please, login first", Toast.LENGTH_LONG).show();
         Intent intent = new Intent(this, LoginActivity.class);
         startActivity(intent);
         finish();
     }
 
     private void load(OAuth oauth) {
         if (oauth != null) {
             enableFullscreenMode(false);
             new LoadPhotostreamTask(this, gridView, new LoadListenerImpl()).execute(oauth);
         }
     }
 
     class LoadListenerImpl implements LoadListener {
 
         @Override
         public void onComplete(PhotoList result) {
             GridThumbsAdapter adapter = new GridThumbsAdapter(FlickrAppActivity.this, result);
             gridView.setAdapter(adapter);
             gridView.setOnItemClickListener(FlickrAppActivity.this);
         }
 
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         Photo photo = (Photo) arg1.getTag();
         Logger.i(TAG, "PHOTO:::" + photo.getLargeUrl());
         openPhotoInFullScreen(photo);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             if (frameLayoutFullScreenImageWrapper.getVisibility() == View.VISIBLE) {
                 enableFullscreenMode(false);
                 return true;
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public void onClick(View v) {
         if (v.getId() == imageViewFullScreen.getId()) {
             if (frameLayoutFullScreenImageWrapper.getVisibility() == View.VISIBLE) {
                 enableFullscreenMode(false);
             }
         }
         if (v.getId() == imageButtonShowOnMap.getId()) {
             GeoData geoData = (GeoData) imageButtonShowOnMap.getTag();
             if (geoData != null) {
                 Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:" + geoData.getLatitude() + "," + geoData.getLatitude()));
                 startActivity(intent);
             }
         }
 
     }
 }
