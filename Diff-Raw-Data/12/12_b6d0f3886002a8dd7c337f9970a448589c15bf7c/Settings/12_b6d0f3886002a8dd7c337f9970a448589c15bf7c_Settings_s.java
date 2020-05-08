 package org.game.advwars;
 
 import dataconnectors.DBAndroidConnector;
 import dataconnectors.SaveData;
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class Settings extends Activity implements OnClickListener, OnCheckedChangeListener
 {
 	private int soundOn;
 	private String difficulty;
 	private String playerName;
 	private SaveData sd = new SaveData();
 	private GUIGameValues ggv = new GUIGameValues();
 	
 	protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
         
         View changePlayerName = findViewById(R.id.change_player_name);
         changePlayerName.setOnClickListener(this);
         
         RadioGroup soundRadioGroup = (RadioGroup) findViewById(R.id.sound_radio_group);
         soundRadioGroup.setOnCheckedChangeListener(this);
         
         RadioGroup difficultyRadioGroup = (RadioGroup) findViewById(R.id.difficulty_radio_group);
         difficultyRadioGroup.setOnCheckedChangeListener(this);
         
         DBAndroidConnector db = new DBAndroidConnector();
         SQLiteDatabase myDataBase = db.getDB();
         ggv = sd.loadGGVData();
         playerName = ggv.getPlayerName();
         
         // Get sound preference from database and set it on/off
         try
         {
         	Cursor soundCursor = myDataBase.rawQuery("select Sound_On from PlayerSettings where Player_Name = '" + playerName + "'", null);
         	int soundOnIndex = soundCursor.getColumnIndexOrThrow("Sound_On");
         	soundCursor.moveToFirst();
         	soundOn = soundCursor.getInt(soundOnIndex);
         	
         	RadioButton soundOnRB = (RadioButton)findViewById(R.id.sound_on);
     		RadioButton soundOffRB = (RadioButton)findViewById(R.id.sound_off);
         	
         	if (soundOn == 0)
         	{
         		soundOnRB.setChecked(false);
         		soundOffRB.setChecked(true);
         	}
         	else if (soundOn == 1)
         	{
         		soundOnRB.setChecked(true);
         		soundOffRB.setChecked(false);
         	}
         	else
         	{
         		soundOnRB.setChecked(false);
         		soundOffRB.setChecked(false);
         	}
         }
         catch (Exception ex)
         {
         	Log.e("AdvWars", ex.toString());
         }
         
         // Get difficulty preference from database and set it to easy, medium, or hard
         try
         {
         	Cursor difficultyCursor = myDataBase.rawQuery("select Difficulty from PlayerSettings where Player_Name = '" + playerName + "'", null);
         	int difficultyIndex = difficultyCursor.getColumnIndexOrThrow("Difficulty");
         	difficultyCursor.moveToFirst();
         	difficulty = difficultyCursor.getString(difficultyIndex);
         	
         	RadioButton easyRB = (RadioButton)findViewById(R.id.easy);
     		RadioButton mediumRB = (RadioButton)findViewById(R.id.medium);
     		RadioButton hardRB = (RadioButton)findViewById(R.id.hard);
         	
         	if (difficulty.equals("easy"))
         	{
         		easyRB.setChecked(true);
         		mediumRB.setChecked(false);
         		hardRB.setChecked(false);
         	}
         	else if (difficulty.equals("medium"))
         	{
         		easyRB.setChecked(false);
         		mediumRB.setChecked(true);
         		hardRB.setChecked(false);
         	}
         	else if (difficulty.equals("hard"))
         	{
         		easyRB.setChecked(false);
         		mediumRB.setChecked(false);
         		hardRB.setChecked(true);
         	}
         	else
         	{
         		easyRB.setChecked(false);
         		mediumRB.setChecked(false);
         		hardRB.setChecked(false);
         	}
         }
         catch (Exception ex)
         {
         	Log.e("AdvWars", ex.toString());
         }
         finally
         {
             myDataBase.close();
         }
     }
 
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId())
     	{
     	case R.id.change_player_name:
     		Intent i = new Intent(this, EnterPlayerName.class);
     		startActivity(i);
     		break;
     	}
 	}
 
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId)
 	{
 		DBAndroidConnector db = new DBAndroidConnector();
         SQLiteDatabase myDB = db.getDB();
         GUIGameValues tempGGV = new GUIGameValues();
         SaveData tempSD = new SaveData();
         tempGGV = sd.loadGGVData();
         playerName = tempGGV.getPlayerName();
 		
         switch (group.getCheckedRadioButtonId())
     	{
     	case R.id.sound_on:
     		myDB.execSQL("update PlayerSettings set Sound_On = 1 where Player_Name = '" + playerName + "';");
     		myDB.close();
     		break;
     	case R.id.sound_off:
     		myDB.execSQL("update PlayerSettings set Sound_On = 0 where Player_Name = '" + playerName + "';");
     		myDB.close();
     		break;
     	case R.id.easy:
     		myDB.execSQL("update PlayerSettings set Difficulty = 'easy' where Player_Name = '" + playerName + "';");
     		myDB.close();
     		break;
     	case R.id.medium:
     		myDB.execSQL("update PlayerSettings set Difficulty = 'medium' where Player_Name = '" + playerName + "';");
     		myDB.close();
     		break;
     	case R.id.hard:
     		myDB.execSQL("update PlayerSettings set Difficulty = 'hard' where Player_Name = '" + playerName + "';");
     		myDB.close();
     		break;
     	}
 	}
 }
