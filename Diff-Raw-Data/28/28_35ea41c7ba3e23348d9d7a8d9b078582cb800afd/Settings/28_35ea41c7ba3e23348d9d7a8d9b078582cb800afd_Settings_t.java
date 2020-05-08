 package no.ntnu.stud.fallprevention;
 
 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;

 import android.view.View;
 import android.widget.Button;

 import android.widget.ArrayAdapter;
 import android.widget.Spinner;

 
 public class Settings extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 	}

	public void openNameScreen(View v){
		/*Intent intent = new Intent(this, WriteName.class);
		startActivity(intent);*/
		//Does not close down the earlier activities
		Intent intent = new Intent(getApplicationContext(), WriteName.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
		this.finish();
 	}
 
 
 	/*@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_settings, menu);
 		return true;
 	}
 */
 }
