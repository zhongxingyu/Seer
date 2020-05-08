 package com.slauson.dasher.menu;
 
 import com.slauson.dasher.R;
 import com.slauson.dasher.other.GameBaseActivity;
 
 import android.os.Bundle;
 
 public class InstructionsMenu extends GameBaseActivity {
 
 	// TODO: add stuff
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.instructions_menu);
     	
     	// TODO: get bundle info to determine if from instructions menu vs tutorial
     	
     	// TODO: set up screens, automators
 	}
 	
 	@Override
 	public void update() {
 		super.update();
 		
 		// TODO: update automators
 		
 		// TODO: check required user interaction
 	}
 
 	public void gameOver() {
 		// TODO: reset ship
 	}
 }
