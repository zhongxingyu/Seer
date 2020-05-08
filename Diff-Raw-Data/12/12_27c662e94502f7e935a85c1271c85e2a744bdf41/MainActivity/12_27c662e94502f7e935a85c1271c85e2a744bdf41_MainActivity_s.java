 package com.cheesymountain.woe;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity {
 	
 	private Everbie everbie;
 	private Use use;
 	//private ActionMode.Callback actionMode;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         //((ImageButton)findViewById(R.id.mainImage)).setImageResource(getResources().getIdentifier(everbie.getImageName(), "drawable", getPackageName()));
         updateLog();
         //menu_feed
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
     
     @Override
     public void onBackPressed(){
     	
     }
     
     public void onSaveInstanceState(Bundle savedInstanceState){
     	super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putString("Name", everbie.getName());
     	savedInstanceState.putInt("maxHealth", everbie.getMaxHealth());
     	savedInstanceState.putInt("health", everbie.getHealth());
     	savedInstanceState.putInt("Strength", everbie.getStrength());
     	savedInstanceState.putInt("Intelligence", everbie.getIntelligence());
     	savedInstanceState.putInt("stamina", everbie.getStamina());
     	savedInstanceState.putInt("charm", everbie.getCharm());
     	savedInstanceState.putInt("fullness", everbie.getFullness());
     	savedInstanceState.putInt("happiness", everbie.getHappiness());
     	savedInstanceState.putInt("toxicity", everbie.getToxicity());
     	savedInstanceState.putInt("cuteness", everbie.getCuteness());
     	savedInstanceState.putInt("money", everbie.getMoney());
     	savedInstanceState.putBoolean("Alive", everbie.isAlive());
     	savedInstanceState.putString("imagePath", everbie.getImageName());
     }
     
     public void onRestoreInstanceState(Bundle savedInstanceState){
     	super.onRestoreInstanceState(savedInstanceState);
     	int[] values = {savedInstanceState.getInt("maxHealth"),
     	savedInstanceState.getInt("health"),
     	savedInstanceState.getInt("Strength"),
     	savedInstanceState.getInt("intelligence"),
     	savedInstanceState.getInt("stamina"),
     	savedInstanceState.getInt("charm"),
     	savedInstanceState.getInt("fullness"),
     	savedInstanceState.getInt("happiness"),
     	savedInstanceState.getInt("toxicity"),
     	savedInstanceState.getInt("cuteness"),
     	savedInstanceState.getInt("money")};
     	everbie.restoreEverbie(savedInstanceState.getString("Name"), values,
     			savedInstanceState.getBoolean("alive"), savedInstanceState.getString("imagePath"));
     	
     }
     
     public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 			case R.id.menu_feed:
 				use.activate(new BreadAndWater());
 				//inflate feedmenu shall be implemented
 				return true;
 		}
 		return false;
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
     	super.onCreateContextMenu(menu, view, menuInfo);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.feed_menu, menu);
     }
     
     public boolean onContestItemSelected(MenuItem item){
		Use use = new Use();
     	switch(item.getItemId()){
     	case R.id.feedPet:
     		use.activate(new BreadAndWater());
     		return true;
     	}
     	return false;
     }
    
     public boolean onCreateActionMode(ActionMode mode, Menu menu){
     	MenuInflater menuInflater = mode.getMenuInflater();
     	menuInflater.inflate(R.menu.feed_menu, menu);
     	return true;
     }
     
     public boolean onActionItemClicked(ActionMode actionMode, MenuItem item){
     	use = new Use();
     	switch(item.getItemId()){
     	case R.id.feedPet:
     		use.activate(new BreadAndWater());
     		return true;
     	}
     	return false;
     }
 
     public void change(View view){
     	everbie = Everbie.getEverbie();
     	this.setContentView(R.layout.activity_stats);
     	
     	//((ImageButton)findViewById(R.id.statsImage)).setImageResource(getResources().getIdentifier(everbie.getImageName(), "drawable", getPackageName()));
     	
     	((TextView)findViewById(R.id.nameText)).setText(everbie.getName() + "");
     	((TextView)findViewById(R.id.charmText)).setText(everbie.getCharm() + "");
     	((TextView)findViewById(R.id.cuteText)).setText(everbie.getCuteness() + "");
     	((TextView)findViewById(R.id.levelText)).setText(everbie.getLevel() + "");
     	((TextView)findViewById(R.id.strengthText)).setText(everbie.getStrength() + "");
     	((TextView)findViewById(R.id.staminaText)).setText(everbie.getStamina() + "");
     	((TextView)findViewById(R.id.intelligenceText)).setText(everbie.getIntelligence() + "");
     	
     	((ProgressBar)findViewById(R.id.fullnessBar)).setProgress(everbie.getFullness());
     	((ProgressBar)findViewById(R.id.happinessBar)).setProgress(everbie.getHappiness());
     	((ProgressBar)findViewById(R.id.toxicityBar)).setProgress(everbie.getToxicity());
     	((ProgressBar)findViewById(R.id.healthBar)).setMax(everbie.getMaxHealth());
     	((ProgressBar)findViewById(R.id.healthBar)).setProgress(everbie.getHealth());
     	
    
     	
     }
     
     public void back(View view){
     	this.setContentView(R.layout.activity_main);
     }
     
     public void updateLog(){
     	((EditText)findViewById(R.id.log)).setText(Log.getLog().getLogList());
     }    
 }
