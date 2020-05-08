 package fr.eurecom.cardify;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class HostGame extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.host_game, menu);
 		return true;
 	}
 	
 	public void startGame(View view) {
 		Intent intent = new Intent(this,Game.class);
 		startActivity(intent);
 	}
 
 }
