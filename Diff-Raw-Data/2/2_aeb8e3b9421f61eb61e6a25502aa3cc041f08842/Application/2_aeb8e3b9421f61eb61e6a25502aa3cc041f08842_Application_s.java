 package org.robotics.nao;
 
 import org.robotics.nao.model.Accelerometer;
 import org.robotics.nao.model.SpeechRecognition;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.widget.Toast;
 
 public class Application extends Activity {
 	private static final int VOICE_RECOGNITION = 1;
 	private static final int ACCELEROMETER = 2;
 	private Accelerometer accelerometer;
 	private SpeechRecognition speechrecognition;
 /**
  * Activity
  */
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         setContentView(R.layout.main);
         speechrecognition=new SpeechRecognition(this);
         accelerometer=new Accelerometer(this);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch (requestCode) {
 		case VOICE_RECOGNITION:
 			if (resultCode == Activity.RESULT_OK)
 				speechrecognition.extractSpeechRecognitionResults(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
 			else
 				Toast.makeText(getApplicationContext(),"Erreur", Toast.LENGTH_LONG).show();
 			break;
 		default:
 			break;
 		}
     }
 /**
  * Option Menu
  */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	SubMenu m;
    	m= menu.addSubMenu(0, VOICE_RECOGNITION, 0, "Speak");
     	m.setIcon(android.R.drawable.ic_btn_speak_now);
     	m = menu.addSubMenu(0, ACCELEROMETER, 0, "Walk");
     	m.setIcon(android.R.drawable.ic_menu_directions);
     	return super.onCreateOptionsMenu(menu);
     }
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     		if(accelerometer.isbSensorRunning())
         		menu.findItem(ACCELEROMETER).setTitle("Stop");
         	else
         		menu.findItem(ACCELEROMETER).setTitle("Walk");    	
     	return super.onPrepareOptionsMenu(menu);
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
 		case VOICE_RECOGNITION:
 			speechrecognition.onSpeechRecognitionClick();;
 			break;
 		case ACCELEROMETER:
 			accelerometer.startAccelerometerActivity();
 			break;
 		default:
 			break;
 		}
        	return super.onContextItemSelected(item);
     }
     
     public void showMessageBox(String message){
     	Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
     }
 
  
 }
