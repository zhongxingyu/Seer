 package org.pticlic;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 public class Main extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		startActivityForResult(new Intent(this, FrontPage.class), 0x0);
 		return super.onTouchEvent(event);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
		
 	}
 
 }
