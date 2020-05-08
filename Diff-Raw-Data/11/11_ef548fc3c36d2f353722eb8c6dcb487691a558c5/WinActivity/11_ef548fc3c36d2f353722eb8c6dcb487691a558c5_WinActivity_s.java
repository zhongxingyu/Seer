 package com.multimedia.slidepuzzel;
 
 import com.multimedia.slidepuzzel.application.SharedApplication;
 import com.multimedia.slidepuzzel.data.HighscoreEntry;
 import com.multimedia.slidepuzzel.data.Settings;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class WinActivity extends Activity {
 	private SharedApplication app;
 	private HighscoreEntry h;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		int totaltime, sec, min;
 		min = 0;
 		app = (SharedApplication) getApplication();
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.win);
 		
 		Intent sender=getIntent();
 		totaltime = sender.getExtras().getInt("time");
 		sec = totaltime % 60;
 		min = totaltime / 60;
 		
 		TextView wintext = (TextView) findViewById(R.id.wintext);
 		if(sec < 10)
 			wintext.setText("Time: " + min + ":0" + sec );
 		else
 			wintext.setText("Time: " + min + ":" + sec );
 		
 		Button back = (Button) findViewById(R.id.back5);
 		back.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				EditText et = (EditText) findViewById(R.id.entername);
 				String theText = et.getText().toString();
 				h.setName(theText);
 				app.dataManager.insertHighscore(h);
 				finish();
 			}
 		});
 		
 		Button newgame = (Button) findViewById(R.id.newgame);
 		newgame.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				EditText et = (EditText) findViewById(R.id.entername);
 				String theText = et.getText().toString();
 				h.setName(theText);
 				app.dataManager.insertHighscore(h);
				finish();
 				if(app.mode == Settings.MODE_IMAGE){
 					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 					photoPickerIntent.setType("image/*");
 					startActivityForResult(photoPickerIntent, 1);
 				}else{
 					Intent intent = new Intent(WinActivity.this, GameActivity.class);
 					startActivity(intent);
 				}
 			}
 		});
 		
 		h = new HighscoreEntry();
 		Settings s = new Settings();
 		s.setDifficulty(app.diff);
 		s.setSize(app.size);
 		h.setSettings(s);
 		h.setTime(totaltime);
 		
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
 		android.util.Log.d("Debug", "onActivityResult " + requestCode);
 		super.onActivityResult(requestCode, resultCode, intent);
 		if(resultCode == RESULT_OK){
 			Uri photoUri = intent.getData();
 			
 			if(photoUri != null){
 				Intent i = new Intent(this, GameActivity.class);
 				i.putExtra("uri", photoUri);
 				startActivity(i);
 			}
 		}
 	}
 	
 	public void newGame(View view) {
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 	}
 	
	@Override
 	protected void onStop() {
 		super.onStop();
 		finish();
 	}
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		finish();
	}
 }
