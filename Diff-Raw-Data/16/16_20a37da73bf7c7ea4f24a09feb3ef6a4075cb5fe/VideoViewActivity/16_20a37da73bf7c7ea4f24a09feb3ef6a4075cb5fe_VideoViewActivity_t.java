 package com.dozuki.ifixit.guide_view.ui;
 
 import org.holoeverywhere.app.ProgressDialog;
 
 import com.actionbarsherlock.view.Window;
 import com.dozuki.ifixit.R;
 
 import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.widget.MediaController;
 import android.widget.VideoView;
 
 public class VideoViewActivity extends Activity {
 
    protected static final String VIDEO_URL = "VIDEO_URL";
    private String mVideoUrl;
    private VideoView mVideoView;
    private ProgressDialog mProgressDialog;
   private Context mContext;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
      mContext = this;
       requestWindowFeature((int) Window.FEATURE_NO_TITLE);
 
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
       setContentView(R.layout.video_view);
       Bundle extras = getIntent().getExtras();
       mVideoUrl = (String) extras.get(VIDEO_URL);
 
       mVideoView = (VideoView) findViewById(R.id.video_view);
 
       MediaController mc = new MediaController(this);
       mVideoView.setMediaController(mc);
 
       Uri uri = Uri.parse(mVideoUrl);
 
       mVideoView.setVideoURI(uri);
 
      mProgressDialog = ProgressDialog.show(mContext, 
          getString(R.string.video_activity_progress_title),
          getString(R.string.video_activity_progress_body), true);
 
       mVideoView.setOnPreparedListener(new OnPreparedListener() {
          public void onPrepared(MediaPlayer mp) {
             mProgressDialog.dismiss();

            mVideoView.requestFocus();
            mVideoView.start();
          }
       });
    }
 }
