 package lazygames.trainyoureye;
 
 import lazygames.trainyoureye.R;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity implements OnClickListener{
 	
 	Button button_game1;
 	Button button_game2;
 	Button button_settings;
 	Button button_exit;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setButtons();
 		setDefaultSettings();
 		Log.e("Class initiation", "Type: MainActivity");
 	
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
     public void onClick(View v) {
         switch(v.getId()){
             case R.id.button1:
             	Intent game = new Intent("lazygames.trainyoureye.GameOneActivity");
             	startActivity(game);
                 break;
             case R.id.button2:
                 break;
             case R.id.button3:
             	Intent settings = new Intent("lazygames.trainyoureye.Settings");
             	startActivity(settings);
             	break;
             case R.id.button4:
             	break;
         }
     }
     
     public void setButtons() {
 
 		Button button_game1 = 		(Button) findViewById(R.id.button1);
 		Button button_game2 = 		(Button) findViewById(R.id.button2);
 		Button button_settings = 	(Button) findViewById(R.id.button3);
 		Button button_exit = 		(Button) findViewById(R.id.button4);
 			
 		button_game1.setOnClickListener(this);
 		button_game2.setOnClickListener(this);
 		button_settings.setOnClickListener(this);
 		button_exit.setOnClickListener(this);
     }
     
     public void setDefaultSettings() {
     	SharedPreferences preferences = PreferenceManager.
     			getDefaultSharedPreferences(getBaseContext());
     	if(preferences.contains("firstTime")) {
     		return;
     	} else {
     		SharedPreferences.Editor editor = preferences.edit();
     		editor.putInt("blueValue", 255);
     		editor.putInt("redValue", 255);
     		editor.putInt("Highscore", 0);
     		editor.putBoolean("firstTime", true);
     		editor.commit();
     	}
 
     }
 }
