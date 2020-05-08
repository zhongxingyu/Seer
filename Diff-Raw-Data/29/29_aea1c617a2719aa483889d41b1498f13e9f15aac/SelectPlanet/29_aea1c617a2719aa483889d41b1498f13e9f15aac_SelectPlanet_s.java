 package com.cs2340.spacetrader;
 
import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
 
 public class SelectPlanet extends Activity {
 	
 	private ListView pListView;
 	private Ship ship;
 	private Planet currentPlanet;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
     	//View settings
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_select_planet);
         
         //Current ship and planet
         ship = GameSetup.thePlayer.getship();
         currentPlanet = GameSetup.theMap.getPlanet(ship.getPlanetName());
         
         //Effective 'cancel' button
         Button stayButton = (Button) findViewById(R.id.button_stay_here);
         stayButton.setText("Stay on " + currentPlanet.getName());
         
         //get planet array to pass into list adapter
         Planet[] planetData = GameSetup.theMap.getPlanetArray();
         
         //set up list adapter
         PlanetListAdapter adapter = new PlanetListAdapter(this, R.layout.planet_row, planetData);
         pListView = (ListView)findViewById(R.id.selectplanet_list);
         pListView.setAdapter(adapter);
        
        //something is wrong here!
        pListView.setOnItemClickListener(new ListView.OnItemClickListener()
        {
        	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) 
        	{
            	startSpaceView(view);
			}
		});
     }
     
     /**
      * Return to Space from SelectPlanet
      * @param view
      */
     public void startSpaceView(View view)
     {
     	Intent intent = new Intent(this, Space.class);
     	startActivity(intent);
     }
 }
