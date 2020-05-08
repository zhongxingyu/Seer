 /*
  *  Copyright 2012 Brendan McCarthy (brendan@oddsoftware.net)
  *
  *  This file is part of Feedscribe.
  *
  *  Feedscribe is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 
  *  as published by the Free Software Foundation.
  *
  *  Feedscribe is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Feedscribe.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.oddsoftware.android.feedscribe.ui;
 
 import java.io.File;
 
 import net.oddsoftware.android.feedscribe.AudioPlayer;
 import net.oddsoftware.android.feedscribe.Globals;
 import net.oddsoftware.android.feedscribe.R;
 import net.oddsoftware.android.feedscribe.data.Feed;
 import net.oddsoftware.android.feedscribe.data.FeedItem;
 import net.oddsoftware.android.feedscribe.data.FeedManager;
 import net.oddsoftware.android.utils.TagParser;
 import net.oddsoftware.android.utils.Utilities;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class PlayAudioActivity extends Activity
 {
     public static final String EXTRA_PATH = "path";
     public static final String EXTRA_URL = "url";
     public static final String EXTRA_ENCLOSURE_ID = "enclosure_id";
     public static final String EXTRA_ITEM_ID = "item_id";
     public static final String EXTRA_SEEK_TO = "seek_to";
     public static final String EXTRA_FROM_NOTIFICATION = "from_notification";
 
     private Handler mHandler = null;
     private Runnable mUpdateTask;
     private SeekBar mSeekBar;
     
     private TextView mPositionText;
     private TextView mDurationText;
     
     private String mTitleText;
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.play_audio_activity);
         
         mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
         mPositionText = (TextView) findViewById(R.id.position_text);
         mDurationText = (TextView) findViewById(R.id.duration_text);
         
         ((ImageButton)findViewById(R.id.btn_pause)).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 onAudioPause();
             }
         });
         
         ((ImageButton)findViewById(R.id.btn_ff)).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 onFastForward();
             }
         });
         
         ((ImageButton)findViewById(R.id.btn_rew)).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 onRewind();
             }
         });
         
         
         mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             
             @Override
             public void onStopTrackingTouch(SeekBar seekBar)
             {
                 onSeek( seekBar.getProgress() );
             }
             
             @Override
             public void onStartTrackingTouch(SeekBar seekBar)
             {
             }
             
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
             {
             }
         });
         
         if( mHandler == null )
         {
             mHandler = new Handler();
         }
         
         if( mUpdateTask == null )
         {
             mUpdateTask = new Runnable() {
                 
                 @Override
                 public void run() {
                     onTimer();
                 }
             };
         }
         
         
         /*
         ContentResolver crThumb = getContentResolver();
         BitmapFactory.Options options=new BitmapFactory.Options();
         options.inSampleSize = 1;
         MediaStore.Audio.
         Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
         iv.setImageBitmap(curThumb);
         */
         
         AudioPlayer player = AudioPlayer.getInstance(this);
         boolean started = false;
         
         boolean fromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
         long itemId = -1;
         
         if( fromNotification )
         {
             // try and get stuff from audio player
             itemId = player.getItemId();
             started = true;
             
             loadAlbumArt( itemId, player.getCurrentPath() );
         }
         else
         {
             // stop any currently playing dude
             if(player.isPlaying())
             {
                 player.savePosition();
             }
             
             String path = getIntent().getStringExtra(EXTRA_PATH);
             String url = getIntent().getStringExtra(EXTRA_URL);
             
             itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, 0);
             long enclosureId = getIntent().getLongExtra(EXTRA_ENCLOSURE_ID, 0);
             
             player.setItem(itemId, enclosureId);
 
             int seekTo = (int) getIntent().getLongExtra(EXTRA_SEEK_TO, 0);
             
             if( path != null )
             {
                 started = player.playPath( path, seekTo );
                 
                 loadAlbumArt( itemId, path );
             }
             else if( url != null )
             {
                 started = player.playUrl( url, seekTo );
                 
                 loadAlbumArt( itemId, null );
             }
         }
         
         updateData(itemId);
         
         if( started )
         {
             int duration = player.getDuration();
             mSeekBar.setMax( duration );
             mDurationText.setText( Utilities.formatDuration((duration + 500)/1000) );
             
             ((ImageButton) findViewById(R.id.btn_pause)).setImageResource(android.R.drawable.ic_media_pause);
         }
     }
     
     private void loadAlbumArt(long itemId, String path)
     {
         // load image
         ImageView iv = (ImageView ) findViewById(R.id.imageView1);
                 
         Cursor cursorAudio = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA}, MediaStore.Audio.Media.DATA+ " LIKE \"" + path+ "\"", null, null);
         if(cursorAudio != null  && cursorAudio.moveToFirst())
         {
             Long albumId = Long.valueOf(cursorAudio.getString(cursorAudio.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
             Cursor cursorAlbum = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID+ "=" + albumId, null, null);
             if(cursorAlbum != null  && cursorAlbum.moveToFirst())
             {
                 String uriString = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                 if(uriString != null)
                 {
                     Uri imageUri = Uri.parse(uriString);
                     Globals.LOG.d("image uri is 1 " + imageUri);
                     iv.setImageURI(imageUri);
                 }
             }
             
             if(cursorAlbum != null) {
                 cursorAlbum.close();
             }
         }
         
         if(cursorAudio != null) {
             cursorAudio.close();
         }
         
         byte[] data = null;
         
         if( path != null )
         {
             TagParser parser = new TagParser(new File(path));
             if( parser.findImage())
             {
                 String mimeType = parser.getImageMimeType();
                 data = parser.getImageData();
                 
                 Globals.LOG.d("trying image mime type " + mimeType + " size " + data.length);
             }
         }
         
         if(data == null && itemId > 0)
         {
             FeedManager feedManager = FeedManager.getInstance(this);
             Feed feed = feedManager.getFeedByItemId(itemId);
             data = feedManager.getImage(feed.mImageURL); 
         }
         
         if( data != null )
         {
             Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
             if( bitmap != null )
             {
                 iv.setImageBitmap( bitmap );
             }
         }
     }
 
     private void onAudioPause()
     {
         AudioPlayer player = AudioPlayer.getInstance(this);
         
         if( ! player.isPaused() )
         {
             player.pause();
             player.savePosition();
             updateDisplay();
         }
         else
         {
             player.resume();
             updateDisplay();
         }
     }
     
     private void onFastForward()
     {
         AudioPlayer.getInstance(this).seekBy( 60 * 1000 );
         onTimer();
     }
     
     private void onRewind()
     {
         AudioPlayer.getInstance(this).seekBy( -60 * 1000 );
         onTimer();
     }
     
     private void onTimer()
     {
         int timeout = updateDisplay();
         
         Globals.LOG.v("updateDisplay: timeout: " + timeout);
         
         mHandler.removeCallbacks(mUpdateTask);
         
         if(timeout > 0)
         {
             mHandler.postDelayed(mUpdateTask, timeout);
         }
     }
     
     private void onSeek(int position)
     {
         AudioPlayer.getInstance(this).seekTo(position);
         onTimer();
     }
     
     private int updateDisplay()
     {
         AudioPlayer player = AudioPlayer.getInstance(this);
         
         if( !player.hasStarted() )
         {
             mPositionText.setText( Utilities.formatDuration(0) );
             mSeekBar.setProgress( 0 );
             ((ImageButton) findViewById(R.id.btn_pause)).setImageResource(android.R.drawable.ic_media_play);
             
             return -1;
         }
         else if( ! player.isPaused() )
         {
             int position = player.getCurrentPosition();
             
             //int seconds = (position + 500) / 1000;
             int seconds = position / 1000;
             
             mSeekBar.setProgress( position );
             mPositionText.setText( Utilities.formatDuration(seconds) );
             
             int remaining = 1000 - (position % 1000);
             
             mPositionText.setVisibility(View.VISIBLE);
             
             
             ((ImageButton) findViewById(R.id.btn_pause)).setImageResource(android.R.drawable.ic_media_pause);
             
             return remaining;
         }
        else  // Player paused
         {
            int position = player.getCurrentPosition();
            mSeekBar.setProgress( position );
            
             if( mPositionText.getVisibility() == View.VISIBLE )
             {
                 mPositionText.setVisibility(View.INVISIBLE);
             }
             else
             {
                int seconds = position / 1000;
                
                mPositionText.setText( Utilities.formatDuration(seconds) );
                 mPositionText.setVisibility(View.VISIBLE);
             }
             
             ((ImageButton) findViewById(R.id.btn_pause)).setImageResource(android.R.drawable.ic_media_play);
             
             return 500;
         }
     }
     
     private void updateData(long itemId)
     {
         FeedManager feedManager = FeedManager.getInstance(this);
         FeedItem feedItem = feedManager.getItemById(itemId);
         if( feedItem != null)
         {
             Feed feed = feedManager.getFeed( feedItem.mFeedId );
             
             WebView webView = (WebView) findViewById(R.id.web_view);
             webView.loadDataWithBaseURL(null, feedItem.mDescription, "text/html", "utf-8", null);
             
             TextView textView = (TextView) findViewById(R.id.titleTextView);
             
             StringBuffer titleText = new StringBuffer();
             
             titleText.append( feedItem.mTitle );
             
             if( feedItem.mAuthor.length() > 0 )
             {
                 titleText.append(" - ");
                 titleText.append( feedItem.mAuthor );
             }
             else if( feed != null && feed.mName.length() > 0 )
             {
                 titleText.append(" - ");
                 titleText.append( feed.mName );
             }
             
             mTitleText = titleText.toString();
             
             textView.setText( mTitleText );
         }
     }
 
     @Override
     protected void onPause()
     {
         mHandler.removeCallbacks(mUpdateTask);
         
         AudioPlayer player = AudioPlayer.getInstance(this);
        if( player.hasStarted() )
         {
             player.savePosition();
         }
         
         super.onPause();
     }
     
 
     @Override
     protected void onResume()
     {
         onTimer(); // update seek bar and kick-off timer
         
         // hide notification
         NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
         notificationManager.cancel(Globals.NOTIFICATION_PLAYING);
         
         super.onResume();
     }
 
     @Override
     protected void onStart()
     {
         // hide notification
         NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
         notificationManager.cancel(Globals.NOTIFICATION_PLAYING);
         
         super.onStart();
     }
 
     @Override
     protected void onStop()
     {
         // if playing, create notification
         
         AudioPlayer player = AudioPlayer.getInstance(this);
         if(player.isPlaying())
         {
             Intent notificationIntent = new Intent(this, PlayAudioActivity.class);
             notificationIntent.putExtra(EXTRA_FROM_NOTIFICATION, true);
             PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
             
             Notification notification = new Notification(R.drawable.feedscribe_status, null, 0);
             notification.setLatestEventInfo(
                     this,
                     getResources().getString(R.string.notification_now_playing),
                     mTitleText,
                     pendingIntent
                     );
             notification.flags |= Notification.FLAG_ONGOING_EVENT;
             NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
             notificationManager.notify(Globals.NOTIFICATION_PLAYING, notification);
         }
         super.onStop();
     }
 }
