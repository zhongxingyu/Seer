 package org.game.advwars;
 
 import dataconnectors.DBAndroidConnector;
 import dataconnectors.SaveData;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class SelectPlayerName extends Activity implements OnItemSelectedListener, OnClickListener
 {
 	private String playerName = "";
 	private GUIGameValues selectPlayerGGV = new GUIGameValues();
 	private SaveData sd = new SaveData();
 	
 	protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_player_name);
         
         Spinner spinner = (Spinner) findViewById(R.id.player_name_selection);
         
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         
         DBAndroidConnector db = new DBAndroidConnector();
         SQLiteDatabase myDataBase = db.getDB();
        
         try
         {
         	Cursor cursor = myDataBase.rawQuery("select Player_Name from PlayerSettings", null);
         	int playerNameIndex = cursor.getColumnIndexOrThrow("Player_Name");
 
             if(cursor.moveToFirst())
             {
                 do
                 {
                     adapter.add(cursor.getString(playerNameIndex));
                 }
                 while(cursor.moveToNext());
             }
            
            cursor.close();
         }
         finally
         {
             myDataBase.close();
         }
         
         spinner.setOnItemSelectedListener(new SelectPlayerName());
         
         Button selectPlayerName = (Button) findViewById(R.id.confirm_player_name);
         selectPlayerName.setOnClickListener(this);
     }
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 	{
 		playerName = parent.getItemAtPosition(pos).toString();
 		this.selectPlayerGGV.setPlayerName(playerName);
 		sd.saveGGVData(selectPlayerGGV);
 	    Toast.makeText(parent.getContext(), "You selected " + playerName, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0)
 	{
 		// Do nothing
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		selectPlayerGGV = sd.loadGGVData();
 		playerName = selectPlayerGGV.getPlayerName();
 		
 		if (!playerName.equals(""))
 		{
 			Intent i = new Intent(this, MainMenu.class);
 			i.putExtra("ggv", selectPlayerGGV);
 			startActivity(i);
 		}
 		else
 		{
 			Intent i2 = new Intent(this, InvalidPlayerNameSelected.class);
 			startActivity(i2);
 		}
 	}
 }
