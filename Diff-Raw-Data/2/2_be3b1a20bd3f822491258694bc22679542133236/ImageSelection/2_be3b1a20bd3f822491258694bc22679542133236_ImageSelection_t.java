 /* author: Christopher Farm*/
 package com.appordinance.ImagePuzzle;
 
 import com.appordinance.ImagePuzzle.R;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.AdapterView;
 
 
 public class ImageSelection extends ListActivity implements
 		AdapterView.OnItemClickListener {
 	/** Called when the activity is first created. */
 	// create shared preferences string
 	final String GAME_SETTINGS = "mySettings";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//Set up shared preferences and check if game has been played before
 		SharedPreferences settings = getSharedPreferences(GAME_SETTINGS, 0);
 		if (settings.getBoolean("hasPlayed", false) == true) {
			//if game has been played, then go straight to game play with the picture that was stored
 			Intent i = new Intent(this, GamePlay.class);
 			int res = settings.getInt("imageUsed", R.drawable.puzzle_0);
 			i.putExtra("pictureRef", res);
 			startActivity(i);
 		}
 		
 		//if the game wasn't played then go to the home screen by setting the list adapter
 		setListAdapter(new ImageAdapter(this));
 
 		ListView lv = this.getListView();
 		lv.setOnItemClickListener(this);
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 		
 		//concatenate name so that you can dynamically look up any pictures
 		int res = 0;
 		String name;
 		for (int i = 0; i < parent.getCount(); i++){
 			if(position == i){
 				name = "drawable/puzzle_" + Integer.toString(i);
 				res = getResources().getIdentifier(name, "id", "com.appordinance.ImagePuzzle");
 			}
 		}
 		
 		//fire an intent and send the resource integer so that picture can be accessed in new activity
 		Intent i = new Intent(this, GamePlay.class);
 		i.putExtra("pictureRef", res);
 		ImageSelection.this.finish();
 		startActivity(i);
 	}
 }
