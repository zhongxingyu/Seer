 package dk.aau.cs.giraf.cars;
 
import dk.aau.cs.giraf.cars.sound.RecorderThread;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class SettingsActivity extends Activity {
	RecorderThread recorderThread = new RecorderThread();
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
		//recorderThread.start();
		//System.out.println("average frequency = " + recorderThread.getFrequency());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.settings, menu);
 		return true;
 	}
	@Override
	public void onStop(){
		super.onStop();
	//	recorderThread.recording = false;
	}  
 
}
