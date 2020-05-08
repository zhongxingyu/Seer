 package com.game.slapkai;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.Window;
 import com.quantcast.measurement.service.QuantcastClient;
 
 
 public class Main extends Activity {
 	
 	GameView gv = null;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		QuantcastClient.beginSessionWithApiKey(this, "0lnkxnci0mlmsquo-h9hu77wy738hds3j");
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		gv = new GameView(this);
 		setContentView(gv);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	@Override
     protected void onResume() {
 		QuantcastClient.resumeSession();
 		super.onResume();
     }
     @Override
     protected void onPause() {
     	QuantcastClient.pauseSession();
     	super.onPause();
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	super.onSaveInstanceState(savedInstanceState);
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     }
 
     
     @Override
     protected void onDestroy() {
     	QuantcastClient.endSession(this);
     	super.onDestroy();
     }
 
 }
