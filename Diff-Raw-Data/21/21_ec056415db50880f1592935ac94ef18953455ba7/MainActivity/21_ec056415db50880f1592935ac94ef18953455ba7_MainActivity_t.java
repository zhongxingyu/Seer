 package com.example.insync;
 
 import android.media.MediaPlayer;
import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		final MediaPlayer buttonClick = MediaPlayer.create(this, R.raw.buttontest);
 		final ImageButton playbutton = (ImageButton) findViewById(R.id.imageButton1);
 		
 		playbutton.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View v){
 				buttonClick.start();
 			}
 			}
 		);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
	
	public void createSession(View view){
		Intent intent = new Intent(this, CreateSession.class);
		startActivity(intent);
	}
 
 }
