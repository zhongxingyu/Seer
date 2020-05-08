 package edu.gatech.cs4261.LAWN;
 
 import android.content.Context;
 import android.os.Bundle;
 import edu.gatech.cs4261.LAWN.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class Preferences extends CustomActivity {
 	/* constants*/
 	private static final String TAG = "Preferences";
 	private Context ctx = this;
 	
 	/*called when the class is first made*/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.prefs);
         
         /* set up the button listeners*/
         /* enable options*/
		Button btnScan = (Button)findViewById(R.id.btnScan);
		btnScan.setOnClickListener(OptionCheckListener);
 	}
 	
 	/* button listeners/actions */
 	/** what to do when an option is clicked */
     private OnClickListener OptionCheckListener = new OnClickListener() {
 		public void onClick(View v) {
 			Log.d(TAG, "an option clicked");
 			
 			/**TODO: do something with the option*/
 		}
     };
 }
