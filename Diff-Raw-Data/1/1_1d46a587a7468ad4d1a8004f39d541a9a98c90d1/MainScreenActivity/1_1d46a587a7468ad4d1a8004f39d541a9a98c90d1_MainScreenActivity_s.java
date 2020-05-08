 package com.jand.bombercommander.screens;
 
import com.jand.bombercommander.PlayingFieldActivity;
 import com.jand.bombercommander.R;
 import com.jand.bombercommander.R.layout;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.app.Activity;
 import android.content.Intent;
 
 public class MainScreenActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_screen);
 		
 		Button startButton = (Button)findViewById(R.id.btnStart);
 		
 		startButton.setOnClickListener( new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent startGame = new Intent( MainScreenActivity.this, PlayingFieldActivity.class );
 				startActivity( startGame );
 			}
 		});
 	}
 	
 }
