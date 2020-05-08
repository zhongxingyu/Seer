 package net.djmacgyver.bgt.activity;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.alarm.AlarmReceiver;
 import net.djmacgyver.bgt.event.Event;
 import net.djmacgyver.bgt.event.EventList;
import net.djmacgyver.bgt.gps.GPSTrackingListener;
 import net.djmacgyver.bgt.gps.GPSTrackingService;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 	private GPSTrackingService service;
 	private boolean bound = false;
     ServiceConnection conn = new ServiceConnection() {
    	private GPSTrackingListener l = new GPSTrackingListener() {
			@Override
			public void trackingEnabled() {
		        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
				b.setChecked(true);
			}
			
			@Override
			public void trackingDisabled() {
		        ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
				b.setChecked(false);
			}
		};
    	
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
			service.removeListener(l);
 			service = null;
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder binder) {
 			service = ((GPSTrackingService.LocalBinder) binder).getService();
	        final ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
 	        b.setChecked(service.isEnabled());
	        
	        service.addListener(l);
 		}
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
         TextView t = (TextView) findViewById(R.id.title);
         t.setText(R.string.app_name);
         
         startService(new Intent(this, GPSTrackingService.class));
         bindService(new Intent(this, GPSTrackingService.class), conn, Context.BIND_AUTO_CREATE);
         bound = true;
         
         ToggleButton b = (ToggleButton) findViewById(R.id.toggleButton1);
         b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((ToggleButton) v).isChecked()) {
 					service.enable();
 				} else {
 					service.disable();
 				}
 			}
 		});
         
         Button mapButton = (Button) findViewById(R.id.mapButton);
         mapButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(getApplicationContext(), Map.class);
 				startActivity(i);
 			}
 		});
         
         final EventList events = new EventList(this);
         ListView eventList = (ListView) findViewById(R.id.upcomingEvents);
         eventList.setAdapter(events);
         eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				// get the event the user clicked on
 				Event event = new Event((JSONObject) events.getItem(position));
 				
 				// set up a system alarm that will wake us up when the time has come
 				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 				Intent i = new Intent(MainActivity.this, AlarmReceiver.class);
 				i.putExtra("eventId", event.getId());
 				PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 113124, i, PendingIntent.FLAG_UPDATE_CURRENT);
 				am.set(AlarmManager.RTC_WAKEUP, event.getControlConnectionStartTime().getTime(), sender);
 			}
 		});
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.config:
 				Intent i = new Intent(getApplicationContext(), Settings.class);
 				startActivity(i);
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (bound) {
 			if (!service.isEnabled()) stopService(new Intent(this, GPSTrackingService.class));
 			unbindService(conn);
 			bound = false;
 		}
 	}
 }
