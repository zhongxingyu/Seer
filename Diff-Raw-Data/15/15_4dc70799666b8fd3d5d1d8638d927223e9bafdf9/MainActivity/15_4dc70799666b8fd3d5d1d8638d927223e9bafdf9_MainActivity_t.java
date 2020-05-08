 package ca.polymtl.inf8405.sevenwonders;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.view.Window;
 
 public class MainActivity extends Activity {
 
 	public static final boolean DEBUG_MODE = false;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 	}
 
 	public void play(View view){
 		Intent intent = new Intent(this, GameScreenActivity.class);
 		startActivity(intent);
 	}
 	
 	public void test(View view){
        Intent intent = new Intent(this, LogInActivity.class);
         startActivity(intent);
 	}
 	
 	public void joinGameRoom(View view){
 		Intent intent = new Intent(this, LogInActivity.class);
 		startActivity(intent);
 	}
 
 }
