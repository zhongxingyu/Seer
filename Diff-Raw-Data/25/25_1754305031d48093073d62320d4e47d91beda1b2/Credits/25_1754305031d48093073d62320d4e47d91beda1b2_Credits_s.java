 package com.informagics.gehirnjogging.menue;
 
 import com.informagics.gehirnjogging.R;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.widget.MediaController;
 import android.widget.VideoView;
 import android.app.Activity;
 
 public class Credits extends Activity 
 {
 	VideoView Videoview;
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.credits);
         
         Videoview = (VideoView)findViewById(R.id.videoview1);
         Uri videopfad = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.credits); //Quelle ist eigenes Projekt; Name des Projekts; Dateipfad (ohne dateiendung)
         MediaController mc = new MediaController(this); // Erstellt ein Videoplayer mit Pause-Button usw. DRINGEND NOTWENDIG!!! ohne gehts nicht
         mc.setVisibility(4);
         Videoview.setOnCompletionListener(new OnCompletionListener()
         {	
         	public void onCompletion(MediaPlayer arg0)
         	{
         		arg0.seekTo(0);
         		arg0.start();
         	}
         });
         Videoview.setMediaController(mc);
         Videoview.setVideoURI(videopfad);
         Videoview.start();
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
     
     @Override
     public void onPause() 
 	{
     	super.onStop();
    	
    	Videoview.pause();
 	}
     
     @Override
 	public void onResume() 
 	{
 		super.onResume();
		
 		Videoview.resume();
 	}
 }
