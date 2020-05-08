 package com.example.testapplication;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.MediaController;
 import android.widget.VideoView;
import java.io.File;
 
 public class VideoplayerActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.videoplayer);
         VideoView videoView = (VideoView) findViewById(R.id.videoView);
         MediaController mediaController = new MediaController(this);
         mediaController.setAnchorView(videoView);
        File file = new File("/sdcard/Videos/one.mp4");
        Uri uri = Uri.fromFile(file);
         videoView.setMediaController(mediaController);
         videoView.setVideoURI(uri);
         videoView.requestFocus();
 
         videoView.start();
 
 
     }
 }
