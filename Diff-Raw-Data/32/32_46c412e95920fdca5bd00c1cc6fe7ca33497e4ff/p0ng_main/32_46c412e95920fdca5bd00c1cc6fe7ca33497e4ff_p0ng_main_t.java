 package com.rampantmonk3y.p0ng;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 public class p0ng_main extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         initControls();
     }
      
     private void initControls(){
     	Button highScores = (Button) findViewById(R.id.viewHighScoreButton);
     	highScores.setOnClickListener(new View.OnClickListener(){
     		public void onClick(View view){
     			Intent myIntent = new Intent(view.getContext(), p0ng_hs.class);
     			startActivityForResult(myIntent, 0);
     		}
     	});
     	Button newGame = (Button) findViewById(R.id.newGameButton);
     	newGame.setOnClickListener(new View.OnClickListener(){
     		public void onClick(View view){
     			Intent myIntent = new Intent(view.getContext(), p0ng_game.class);
     			startActivityForResult(myIntent, 0);
     		}
     	});
    	Button settings = (Button) findViewById(R.id.settingsButton);
    	settings.setOnClickListener(new View.OnClickListener(){
    		public void onClick(View view){
    			Intent myIntent = new Intent(view.getContext(), p0ng_settings.class);
    			startActivityForResult(myIntent, 0);
    		}
    	});
     }
 
 }
