 package android.game.guessmynumber;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.support.v4.app.NavUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ScoreActivity extends Activity {
 	
 	private ArrayAdapter<ScoreHelper> score;
 	private MaintainDatabase maintainDB;
 	private Settings setting ;
 	Cursor cursor;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_score);
 		
 		setting = new Settings(getApplicationContext());
 		score = new ArrayAdapter<ScoreHelper>(this, android.R.layout.simple_list_item_1);
 		ListView listview = (ListView)findViewById(R.id.listViewScore);
 		listview.setAdapter(score);
 		
 		maintainDB = new MaintainDatabase(getApplicationContext());
 											
 		cursor = maintainDB.selectFromDatabase();
 		if(cursor.getColumnCount()  == 0){
 			score.add(new ScoreHelper("No Records Found", "", ""));//populate listview
 		}
 		else{
 			while(cursor.moveToNext()){
 				String name = cursor.getString(0);
 				String Score = cursor.getString(1);
 				String when = cursor.getString(2);
 				score.add(new ScoreHelper(name, Score, when));//populate listview
 			}
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		return true;
 	}
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub		
 		switch(item.getItemId()){
 		case android.R.id.home:
 			//Intent upIntent = new Intent(this, MainActivity.class);
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		if(setting.getBackGroundMusic()){
 			BackgroundMusic.startMusic();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		if(setting.getBackGroundMusic()){
 			BackgroundMusic.onActivityPause();
 		}
 	}
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
 		if(setting.getBackGroundMusic()){
 			BackgroundMusic.setFalse();
 		}
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if(setting.getBackGroundMusic()){
 			if(keyCode == KeyEvent.KEYCODE_BACK){
 				BackgroundMusic.setFalse();
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 }
