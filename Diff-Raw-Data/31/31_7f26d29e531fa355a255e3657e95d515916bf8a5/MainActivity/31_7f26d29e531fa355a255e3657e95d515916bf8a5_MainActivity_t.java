 package edu.ucsb.ece251.charlesmunger.roomdetector;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends RoboActivity {
 
 	@InjectView(R.id.test_button) Button b;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		b.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, InRoomService.class);
 				i.putExtra(InRoomService.PENDING_INTENT, PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("derp derp"), 0));
 				MainActivity.this.startService(i);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
