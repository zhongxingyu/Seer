 package com.medievaltech;
 
import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.medievaltech.utils.*;
 
public class ShipDesignerActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ship_designer);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		Log.i("SpaceRace", "Action: "+event.getAction());
 		
 		if(event.getAction() == 1) {	// up action
 			finish();
 			return true;
 		}
 		else {
 			Log.i("SpaceRace", "Ignoring this action");
 			return false;
 		}
 	}
 
 	@Override
 	public void finish() {
 		setResult(RESULT_OK);
 		super.finish();
 	}
 }
