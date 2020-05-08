 package com.dualitysoftware.magiccompanion;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MagicCompanionActivity extends Activity {
 	
 	int health = 20;
 	TextView healthText;
 	TextView deathMsg;
 	Button subHealthBtn;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.magic_companion_activity);
 		
 		deathMsg = (TextView) findViewById(R.id.death_msg);
 		deathMsg.setVisibility(4);
 		
 		healthText = (TextView) findViewById(R.id.health);
 		healthText.setText(Integer.toString(health));
 		
 		subHealthBtn = (Button) findViewById(R.id.subHealth);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.magic_companion_activity, menu);
 		return true;
 	}
 	
 	public void addHealth(View view) {
 		health += 1;
 		healthText.setText(Integer.toString(health));
 	}
 	
 	public void subHealth(View view) {
 		health -= 1;
 		healthText.setText(Integer.toString(health));
 		
 		if (health == 0) {
 			subHealthBtn.setEnabled(false);
 			deathMsg.setVisibility(0);
 		}
 	}
 	
 	public void resetHealth(View view) {
 		health = 20;
 		healthText.setText(Integer.toString(health));
 		deathMsg.setVisibility(4);
 	}
 }
