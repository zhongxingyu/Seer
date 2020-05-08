 package com.deepmine.by;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 
 import java.net.MalformedURLException;
 import java.util.Timer;
 
 import com.deepmine.by.components.TimerTaskPlus;
 import com.deepmine.by.helpers.Constants;
 import com.deepmine.by.adapters.ItemImageBinder;
 import com.deepmine.by.helpers.GSONTransformer;
 import com.deepmine.by.helpers.ImageThreadLoader;
 import com.deepmine.by.helpers.ResourceHelper;
 import com.deepmine.by.models.Blocks;
 import com.deepmine.by.services.DataService;
 import com.deepmine.by.services.MediaService;
 import com.deepmine.by.services.RadioService;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class MainActivity extends Activity implements Constants {
 
     public static String TAG = MAIN_TAG+":MainActivity";
 
     private ImageView mPlayBtn;
     private ListView mListView;
     public static TextView mTrackArtist;
     public static TextView mTrack;
     public static ImageView mCover;
     private ProgressDialog loadingDialog = null;
     private Intent _radioService;
     private Intent _dataService;
     private String _lastCover = "";
     private AQuery _aQuery = new AQuery(this);
     private ImageThreadLoader imageThreadLoader = new ImageThreadLoader();
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         ResourceHelper.getInstance().init(this);
         EasyTracker.getInstance().activityStart(this); // Add this method.
 
         startDataService();
 
         mTrackArtist =  (TextView) findViewById(R.id.artist);
         mTrack =  (TextView) findViewById(R.id.track);
         mPlayBtn = (ImageView) findViewById(R.id.playBtn);
         mCover = (ImageView) findViewById(R.id.trackCover);
         mListView = (ListView) findViewById(R.id.listEvents);
 
         _radioService = new Intent(this, RadioService.class);
         updateTitle();
         getEvents();
 
     }
 
     @Override
     public void onStart() {
         EasyTracker.getInstance().activityStart(this);
         super.onStart();
     }
 
     @Override
     public void onStop() {
         EasyTracker.getInstance().activityStop(this);
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         RadioService.stop();
         stopService(_radioService);
         stopService(_dataService);
         super.onDestroy();
     }
 
     protected void startDataService()
     {
        if(!DataService.status())
        {
            _dataService = new Intent(getApplicationContext(),DataService.class);
            startService(_dataService);
        }
     }
 
     protected void updateTitle()
     {
          new Timer().scheduleAtFixedRate(new TimerTaskPlus() {
              @Override
              public void run() {
                  handler.post(new Runnable() {
                      public void run() {
                          if (!DataService.getDataTitle().title.equals("") && !MediaService.isPlaying()) {
 
                              mTrackArtist.setText(DataService.getDataTitle().artist);
                              mTrack.setText(DataService.getDataTitle().track);
                              if (!_lastCover.equals(DataService.getDataTitle().cover)) {
                                  try {
                                      imageThreadLoader.loadImage(DataService.getDataTitle().cover, new ImageThreadLoader.ImageLoadedListener() {
                                          @Override
                                          public void imageLoaded(Bitmap imageBitmap) {
                                              mCover.setImageBitmap(imageBitmap);
                                          }
                                      });
                                  } catch (MalformedURLException e) {
                                      Log.d(TAG, "Error image load:" + e.getMessage());
                                  }
                              }
                          }
                         else
                          {
                              mTrackArtist.setText(MediaService.getDataTitle().artist);
                              mTrack.setText(MediaService.getDataTitle().track);
                              if (!_lastCover.equals(MediaService.getDataTitle().cover)) {
                                  try {
                                      _lastCover= MediaService.getDataTitle().cover;
                                      imageThreadLoader.loadImage(MediaService.getDataTitle().cover, new ImageThreadLoader.ImageLoadedListener() {
                                          @Override
                                          public void imageLoaded(Bitmap imageBitmap) {
                                              mCover.setImageBitmap(imageBitmap);
                                          }
                                      });
                                  } catch (MalformedURLException e) {
                                      Log.d(TAG, "Error image load:" + e.getMessage());
                                  }
                              }
                          }
                          updatePlayerStatus();
                      }
                  });
              }
          }, 1000, 1000);
     }
 
     public void getEvents()
     {
                _aQuery.transformer(new GSONTransformer())
                                                         .ajax(
                                                                 EVENT_URL,
                                                                 Blocks.class,
                                                                 new AjaxCallback<Blocks>() {
                                                                     public void callback(String url, Blocks blocks, AjaxStatus status) {
 
                                                                         SimpleAdapter simpleAdapter = new SimpleAdapter
                                                                                 (getApplicationContext(),
                                                                                 blocks.getList(),
                                                                                 R.layout.menu_row,
                                                                                 getResources().getStringArray(R.array.menu_row_element_names),
                                                                                 ResourceHelper.getInstance().getIntArray(R.array.menu_row_element_ids)
                                                                         );
 
                                                                         simpleAdapter.setViewBinder(new ItemImageBinder());
 
                                                                         mListView.setAdapter(simpleAdapter);
                                                                         mListView.setDividerHeight(0);
                                                                         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                         @Override
                                                                         public void onItemClick(AdapterView<?> adapterView, View view,
                                                                                                 int i, long l) {
                                                                                                 Intent browserIntent = new Intent(
                                                                                                         Intent.ACTION_VIEW, Uri.parse(((TextView) view
                                                                                                         .findViewById(R.id.link)).getText().toString()));
                                                                                                 startActivity(browserIntent);
 
 
                                                                         }
                                                                 });
                                                                 simpleAdapter.notifyDataSetChanged();
 
 
                                                         }
                                                     }
                                                 );
 
                                             }
 
                                             public void onPlay(View view) {
                                                 if (RadioService.isPlaying())
                                                     stopMedia();
                                                 else
                                                     playMedia();
                                             }
 
                                             public void onClickTitle(View view) {
                                                 startActivity(new Intent(this, NextActivity.class));
                                             }
 
                                             private void playMedia() {
                                                 showLoading();
                                                 startService(_radioService);
                                             }
 
                                             private void showLoading() {
                                                 loadingDialog = new ProgressDialog(this);
                                                 loadingDialog.setMessage(getText(R.string.connection));
                                                 loadingDialog.setCancelable(false);
                                                 loadingDialog.setCanceledOnTouchOutside(false);
                                                 loadingDialog.show();
                                             }
 
                                             private void stopMedia() {
                                                 RadioService.stop();
                                                 stopService(_radioService);
                                                 updatePlayerStatus();
                                             }
 
                                             private void updatePlayerStatus() {
                                                 if (RadioService.isPlaying()) {
                                                     if (loadingDialog != null && loadingDialog.isShowing())
                                                         loadingDialog.dismiss();
 
                                                     mPlayBtn.setImageResource(R.drawable.ic_media_pause);
                                                 } else {
                                                     mPlayBtn.setImageResource(R.drawable.ic_media_play);
                                                 }
 
                                                 if (RadioService.isErrors()) {
                                                     if (loadingDialog != null && loadingDialog.isShowing())
                                                         loadingDialog.dismiss();
 
                                                     Toast.makeText(this, R.string.error_connection, Toast.LENGTH_SHORT).show();
                                                     RadioService.cleanErrors();
                                                     stopMedia();
                                                 }
 
                                             }
 
                                         }
