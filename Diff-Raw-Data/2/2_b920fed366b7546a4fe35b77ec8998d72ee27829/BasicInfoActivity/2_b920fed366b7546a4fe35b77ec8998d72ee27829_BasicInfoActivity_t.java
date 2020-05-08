 package com.example.myfirstapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 
 public class BasicInfoActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_basic_info);
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.basic_info, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void basicInfo(View view){
 		Character createdChar = new Character();
 		CharacterDescription baseInfo = new CharacterDescription();
 		//TODO: Handle empty cases
 		
 		EditText char_name = (EditText) findViewById(R.id.char_name_enter);
 		String cName = char_name.getText().toString().trim();
 		
 		//TODO: Make alignment a dropdown menu
 		//Alignment
 		//EditText align_enter = (EditText) findViewById(R.id.alignment_enter);
 		//String align = align_enter.getText().toString().trim();
 		
 		//Player
 		EditText player_enter = (EditText) findViewById(R.id.player_enter);
 		String player = player_enter.getText().toString().trim();
 		
 		//Level
 		EditText level_enter = (EditText) findViewById(R.id.char_level_enter);
 		String level = level_enter.getText().toString().trim();
 		Integer lvl = Integer.getInteger(level);
 		if (lvl == null) {
 			//TODO:ERROR HANDLING
 		}
 		
		//Deity
 		EditText deity_enter = (EditText) findViewById(R.id.deity_enter);
 		String deity = deity_enter.getText().toString().trim();
 		
 		//Homeland
 		EditText homeland_enter = (EditText) findViewById(R.id.homeland_enter);
 		String homeland = homeland_enter.getText().toString().trim();
 		
 		//Race
 		EditText race_enter = (EditText) findViewById(R.id.race_enter);
 		String race = race_enter.getText().toString().trim();
 		
 		//TODO: Make size dropdown
 		//Size
 		//EditText size_enter = (EditText) findViewById(R.id.size_enter);
 		//String size = align_enter.getText().toString().trim();
 		
 		//Gender
 		EditText gender_enter = (EditText) findViewById(R.id.gender_enter);
 		String gender = gender_enter.getText().toString().trim();
 		
 		//TODO: Figure out how to get out numbers
 
 		//Age
 		EditText age_enter = (EditText) findViewById(R.id.age_enter);
 		String age = age_enter.getText().toString().trim();
 		Integer ageNum = Integer.getInteger(age);
 		if (ageNum == null) {
 			//TODO:ERROR HANDLING
 		}
 		
 		//Height
 		EditText height_enter = (EditText) findViewById(R.id.height_enter);
 		String height = height_enter.getText().toString().trim();
 		Integer heightNum = Integer.getInteger(height);
 		if (heightNum == null) {
 			//TODO:ERROR HANDLING
 		}
 		
 		//Weight
 		EditText weight_enter = (EditText) findViewById(R.id.weight_enter);
 		String weight = weight_enter.getText().toString().trim();
 		Integer weightNum = Integer.getInteger(weight);
 		if (weightNum == null) {
 			//TODO:ERROR HANDLING
 		}
 		
 		//Hair
 		EditText hair_enter = (EditText) findViewById(R.id.hair_enter);
 		String hair = hair_enter.getText().toString().trim();
 		
 		//Eyes
 		EditText eyes_enter = (EditText) findViewById(R.id.eyes_enter);
 		String eyes = eyes_enter.getText().toString().trim();
 		
 		baseInfo.name = cName;
 		baseInfo.player = player;
 		createdChar.setLevel(lvl);
 		baseInfo.diety = deity;
 		baseInfo.homeLand = homeland;
 		baseInfo.race = race;
 		baseInfo.gender = gender;
 		baseInfo.age = ageNum;
 		baseInfo.height = heightNum;
 		baseInfo.weight = weightNum;
 		baseInfo.hair = hair;
 		baseInfo.eyes = eyes;
 		
 		createdChar.setDescriptions(baseInfo);
 		
 	}
 
 }
