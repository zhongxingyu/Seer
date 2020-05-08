 package uk.co.gidley.clockRadio;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 
 import uk.co.gidley.clockRadio.RadioStationsList.OnSelectStationListener;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.flurry.android.FlurryAgent;
 
 public class ClockRadioActivity extends Activity implements
 		OnSelectStationListener {
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		FlurryAgent.onStartSession(this, "AWYQTHVF81ERH8HVUP9V");
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		FlurryAgent.onEndSession(this);
 	}
 
 	private static final String TAG = "ClockRadioActivity";
 
 	private RadioPlayerService mRadioPlayerService;
 	boolean mBound;
 
 	private OnClickListener mOnClickSleepListener = new OnClickListener() {
 		public void onClick(View v) {
 			new Thread(new Runnable() {
 				public void run() {
 
 					Log.d(TAG, "Started creating sleep event");
 					// Read the stop time NOTE current doesn't handle going
 					// passed midnight.
 					Calendar stopTime = Calendar.getInstance();
 					TimePicker timePicker = (TimePicker) findViewById(R.id.sleepTime);
 					stopTime.set(Calendar.HOUR, timePicker.getCurrentHour());
 					stopTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());

 					Intent sleepTime = new Intent(getBaseContext(),
 							SleepTimerReciever.class);
 
 					PendingIntent stopTimePending = PendingIntent.getBroadcast(
 							getBaseContext(), 0, sleepTime,
 							PendingIntent.FLAG_CANCEL_CURRENT);
 					AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 					alarms.set(AlarmManager.RTC, stopTime.getTimeInMillis(),
 							stopTimePending);
					Log.d(TAG, "Alarm created: " + stopTime);
 				}
 			}).start();
 
 		}
 	};
 
 	private OnClickListener mOnPlayListener = new OnClickListener() {
 		public void onClick(View v) {
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						mRadioPlayerService.play(stationUri);						
 					} catch (UnableToPlayException e) {
 						runOnUiThread(new Runnable() {
 							public void run() {
 								Toast.makeText(getBaseContext(), "Unable to play",
 										Toast.LENGTH_SHORT);	
 							}
 						});
 					}
 				}
 			}).start();
 		}
 	};
 
 	private OnClickListener mRefreshVideoListener = new OnClickListener() {
 		public void onClick(View v) {
 			startService(new Intent(getBaseContext(), StationsListService.class));
 			Log.d(TAG, "Requested Stations list");
 		}
 	};
 
 	private OnClickListener mOnStopListener = new OnClickListener() {
 		public void onClick(final View v) {
 			new Thread(new Runnable() {
 				public void run() {
 					mRadioPlayerService.stop();
 				}
 			}).start();
 		}
 	};
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service. Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mRadioPlayerService = ((RadioPlayerService.LocalBinder) service)
 					.getService();
 
 			mRadioPlayerService.addPropertyChangeListener(new PropertyChangeListener() {
 				public void propertyChange(PropertyChangeEvent event) {
 					if (event.getPropertyName().equals("State")){
 						final Button button = (Button) findViewById(R.id.start);
 						if (event.getNewValue().equals(RadioPlayerService.State.PLAYING)){
 							runOnUiThread(new Runnable() {
 								public void run() {
 									button.setText(R.string.stop);
 								}
 							});
 						} else if (event.getNewValue().equals(RadioPlayerService.State.LOADING)){
 							runOnUiThread(new Runnable() {
 								public void run() {
 									button.setText(R.string.loading);
 								}
 							});
 						} else if (event.getNewValue().equals(RadioPlayerService.State.STOPPED)){
 							runOnUiThread(new Runnable() {
 								public void run() {
 									button.setText(R.string.start);
 								}
 							});
 						}
 					}
 				}
 			});
 			
 			Log.d(TAG, "Bound Service");
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mRadioPlayerService = null;
 				
 		}
 	};
 
 	private String stationUri;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		doBindService();
 
 		Button playButton = (Button) findViewById(R.id.start);
 		playButton.setOnClickListener(mOnPlayListener);
 		Button stopButton = (Button) findViewById(R.id.stop);
 		stopButton.setOnClickListener(mOnStopListener);
 		Button refreshStations = (Button) findViewById(R.id.refreshStations);
 		refreshStations.setOnClickListener(mRefreshVideoListener);
 		Button sleepButton = (Button) findViewById(R.id.sleepTimer);
 		sleepButton.setOnClickListener(mOnClickSleepListener);
 	}
 
 	void doBindService() {
 		// Establish a connection with the service. We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
 		bindService(new Intent(ClockRadioActivity.this,
 				RadioPlayerService.class), mConnection,
 				Context.BIND_AUTO_CREATE);
 		mBound = true;
 		
 	}
 
 	void doUnbindService() {
 		if (mBound) {
 			// Detach our existing connection.
 			unbindService(mConnection);
 			mBound = false;
 		}
 	}
 
 	public void onSelectStationListener(String stationUri) {
 		this.stationUri = stationUri;
 	}
 
 }
