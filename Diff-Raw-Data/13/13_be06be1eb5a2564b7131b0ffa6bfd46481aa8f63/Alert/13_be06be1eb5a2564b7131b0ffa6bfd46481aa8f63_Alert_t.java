 package ss12.usc.audioalert;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.Menu;
 import android.widget.TextView;
 
 
 public class Alert extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_alert);
 		
<<<<<<< HEAD
 		Intent intent = getIntent();
 		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
=======
		//Intent intent = getIntent();
		//String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
>>>>>>> a3e643a246c62c5a6c772d7de41c5d1f5abebf39
 		
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 
 		long[] _pattern = new long[]{0,300,0,300,0,300};
 		v.vibrate(_pattern, -1);		
 		
 	    // Create the text view
 	    TextView textView = new TextView(this);
 	    textView.setTextSize(40);
 	    textView.setText(message);
 
 	    // Set the text view as the activity layout
 	    setContentView(textView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_alert, menu);
 		return true;
 	}
 
 }
