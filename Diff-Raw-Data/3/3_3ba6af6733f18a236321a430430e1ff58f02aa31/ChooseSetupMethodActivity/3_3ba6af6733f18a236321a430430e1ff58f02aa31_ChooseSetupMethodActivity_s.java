 package org.fourdnest.androidclient.ui;
 
 import org.fourdnest.androidclient.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * Not used in 1.0
  *
  */
 public class ChooseSetupMethodActivity extends Activity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Button QRButton = (Button) findViewById(R.id.setup_qr);
 		Button manualButton = (Button) findViewById(R.id.setup_manual);
 		
 		QRButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		manualButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 
 }
