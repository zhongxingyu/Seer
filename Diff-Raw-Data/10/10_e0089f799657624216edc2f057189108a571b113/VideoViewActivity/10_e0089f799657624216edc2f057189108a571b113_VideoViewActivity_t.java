 package com.dozuki.ifixit.ui.guide.view;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.widget.MediaController;
 import android.widget.VideoView;
 import com.actionbarsherlock.view.Window;
 import com.dozuki.ifixit.R;
 
 public class VideoViewActivity extends Activity {
 
    public static final String VIDEO_URL = "VIDEO_URL";
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
 
       mVideoView = (VideoView) findViewById(R.id.video_view);
 
       Bundle extras = getIntent().getExtras();
      String videoUrl = (String) extras.get(VIDEO_URL);
 
       MediaController mc = new MediaController(this);
       mVideoView.setMediaController(mc);
 
      mVideoView.setVideoURI(Uri.parse(videoUrl));
 
      mProgressDialog = ProgressDialog.show(mContext,
          getString(R.string.video_activity_progress_title),
          getString(R.string.video_activity_progress_body), true);
 
       mVideoView.setOnPreparedListener(new OnPreparedListener() {
          public void onPrepared(MediaPlayer mp) {
       
             if (mProgressDialog != null)
                mProgressDialog.dismiss();
             
             mVideoView.requestFocus();            
             mp.start();
             
          }
       });                  
 
       mVideoView.setOnCompletionListener(new OnCompletionListener() {
          MediaPlayer mMediaPlayer;
          
          @Override
          public void onCompletion(MediaPlayer mp) {
             mMediaPlayer = mp;
             AlertDialog.Builder restartDialog = new AlertDialog.Builder(mContext);
             restartDialog.setTitle(getString(R.string.restart_video));
             restartDialog
                .setMessage(getString(R.string.restart_video_message))
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      // Reset the video player and restart the clip
                      mMediaPlayer.seekTo(0);
                      mMediaPlayer.start();
                   }})
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog,int id) {
                      // close the dialog box and go back to the guide step
                      dialog.cancel();
                      finish();
                   }
                });
             
             restartDialog.create().show();
           }
       });
    }
 
 }
