 package com.karlliu.combinephrase;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class GameActivity extends Activity {
	private String passedPhrase = null;
	
     @Override
    public void onCreate(Bundle savedInstanceState) {    	
     	
     	super.onCreate(savedInstanceState);
     	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		passedPhrase = this.getIntent().getStringExtra("onephrase");
 		setContentView(new MySurfaceView(this));
     }
    
    public String getPhrase() {
    	return passedPhrase;
    }
 }
