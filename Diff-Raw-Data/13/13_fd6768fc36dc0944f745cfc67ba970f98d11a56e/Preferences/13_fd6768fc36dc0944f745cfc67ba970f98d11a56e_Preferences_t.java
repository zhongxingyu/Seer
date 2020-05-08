 package edu.gatech.cs4261.LAWN;
 
 import android.content.Context;
 import android.os.Bundle;
 import edu.gatech.cs4261.LAWN.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.CheckBox;
 
 public class Preferences extends CustomActivity {
 	/* constants*/
 	private static final String TAG = "Preferences";
 	private Context ctx = this;
 	
 	/*called when the class is first made*/
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		Log.d(TAG, "before set prefs layout");
         setContentView(R.layout.prefs);
         
        Log.d(TAG, "before set button listeners");
        
         /* set up the button listeners*/
         /* enable options*/
		CheckBox OptionCheck = (CheckBox)findViewById(R.id.OptionCheck);
		OptionCheck.setOnClickListener(OptionCheckListener);
		
		Log.d(TAG, "end of onCreate");
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
