 package com.networksinmocean.timecard;
 
import com.networksinmocean.timecard.Add.TimePickerFragmentOut;

 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 import android.app.Activity;
import android.app.DialogFragment;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PunchClock extends Activity {
 
 	private SharedPreferences currentProject;
 	
 	Button buttonInOut;
 	Button buttonChangeProject;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_punch_clock);
         
         // This hides the top status bar since it is not necessary if the app is running.
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         		WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         TextView clockColon = (TextView) findViewById(R.id.textClockColon );
 
         Animation anim = new AlphaAnimation(0.0f, 1.0f);
         anim.setDuration(1000); //You can manage the time of the blink with this parameter
         anim.setStartOffset(20);
         anim.setRepeatMode(Animation.REVERSE);
         anim.setRepeatCount(Animation.INFINITE);
         clockColon.startAnimation(anim);
         
         currentProject = getSharedPreferences("currentProject", MODE_PRIVATE);
         
         buttonInOut = (Button) findViewById(R.id.buttonInOut);
     	buttonChangeProject = (Button) findViewById(R.id.buttonChangeActivity);
     	
     	buttonInOut.setOnClickListener(buttonInOutListener);
     	buttonChangeProject.setOnClickListener(buttonChangeProjectListener);
     	
     	updateCurrentProject();
     	
     }
     
     private void updateCurrentProject() {
     	
     	String savedCurrentProjectText = currentProject.getAll().keySet().toString();
     	
     	TextView currentProjectTextView = (TextView) findViewById(R.id.textViewCurrentProject); 
     	
     	currentProjectTextView.setText(savedCurrentProjectText);
     }
     
     public OnClickListener buttonInOutListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			
 		}
     	
     };
     
     public OnClickListener buttonChangeProjectListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			
 		}
     	
     };
     
     public void showActivityEntries(){
     	
     	Intent showActivityEntries = new Intent(getApplication(), Entries.class);
     	
     	startActivity(showActivityEntries);
     	
     }
     
     public void showActivityAdd(){
     	
     	Intent showActivityAdd = new Intent(getApplication(), Add.class);
     	
     	startActivity(showActivityAdd);
     	
     }
     
     public static class SettingsFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             // Load the preferences from an XML resource
             addPreferencesFromResource(R.xml.preferences);
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.punch_clock, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             case R.id.buttonListView:
                 showActivityEntries();
                 return true;
             case R.id.buttonManualAdd:
                 showActivityAdd();
                 return true;
             case R.id.action_settings:
             	getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new SettingsFragment())
                 .addToBackStack(null)
                 .commit();
             	return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
 }
