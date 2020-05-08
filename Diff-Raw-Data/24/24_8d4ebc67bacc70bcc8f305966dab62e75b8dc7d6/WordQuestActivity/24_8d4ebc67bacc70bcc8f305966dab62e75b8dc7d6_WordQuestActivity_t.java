 package edu.cis350.mosstalkwords;
 
 import java.util.logging.Level;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class WordQuestActivity extends UserActivity {
 
 	private int unlockedLevels = 1;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_word_quest);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.word_quest, menu);
 		return true;
 	}
 	
	@Override
	public void onResume(){
		new LoadUnlockedLevels().execute();
	}
	
     public void btn_tap_mode_1(View v) { btn_tap_mode(v, 1); }
     public void btn_tap_mode_2(View v) { btn_tap_mode(v, 2); }
     public void btn_tap_mode_3(View v) { btn_tap_mode(v, 3); }
     public void btn_tap_mode_4(View v) { btn_tap_mode(v, 4); }
     public void btn_tap_mode(View v, int level) { 
     	if(level <= unlockedLevels){
 			Intent activityMain= new Intent(this, MainActivity.class);
 			activityMain.putExtra("startWordQuest", level);
 			startActivity(activityMain);
     	}
     	else{
 			new AlertDialog.Builder(this)
 			.setTitle("Unlocked Level")
 			.setMessage("You cannot play this level yet. It is locked.")
 			.setPositiveButton("OK", null)
 			.show();
     	}
     }
 
 	private void unlockLevels(int levels) {
 		unlockedLevels = levels;
 		int[] colors = new int[] { android.R.color.holo_green_light, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_red_dark };
 		int[] btnIds = new int[] { R.id.mode1, R.id.mode2, R.id.mode3, R.id.mode4};
 		String[] levelNames = new String[] { "Easy", "Medium", "Hard",
 				"Harder", "Hardest" };
 		for (int i = 0; i < unlockedLevels; i++) {
 			Button btn = (Button) findViewById(btnIds[i]);
 			btn.setBackgroundColor(getResources().getColor(colors[i]));
 			btn.setText(levelNames[i]);
 		}
 	}
 	
 	private class LoadUnlockedLevels extends AsyncTask<Void, Void, Integer> {
 	     protected Integer doInBackground(Void... params) {
 	    	 ImageManager im = new ImageManager(getUserName(), getApplicationContext());
 	    	 int levels = im.getLevelsForWordQuest();
 	    	 return levels;
 	     }
 
 	     protected void onPostExecute(Integer result) {
 	    	 unlockLevels(result);
 	     }
 	 }
 	
 }
