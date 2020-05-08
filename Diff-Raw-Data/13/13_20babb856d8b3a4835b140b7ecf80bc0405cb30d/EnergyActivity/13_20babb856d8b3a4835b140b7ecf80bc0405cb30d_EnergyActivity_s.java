 package com.freescale.iastate.hvac;
 
 import com.freescale.iastate.hvac.energy.GraphFragment;
 import com.freescale.iastate.util.ButtonColumnFragment.OnButtonSelectedListener;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 public class EnergyActivity extends Activity implements MenuInterface, DisplayInterface, OnButtonSelectedListener {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.energy);
 
 		//Finds view, then uses DisplayInterface to change background color
 		View view = findViewById(R.id.energy);
 		ColorDisplay background_color = new ColorDisplay();
 		background_color.setBackgroundColor(view, getBaseContext());
 				
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);

 	}
 
 	public void onButtonSelected(int buttonID){
 		GraphFragment graphFrag = (GraphFragment)getFragmentManager().findFragmentById(R.id.energy_graph_fragment);
 		
 		graphFrag.updateGraph(buttonID);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return rootIntent.onOptionsItemSelected(this, item);
 	}
 	
 }
