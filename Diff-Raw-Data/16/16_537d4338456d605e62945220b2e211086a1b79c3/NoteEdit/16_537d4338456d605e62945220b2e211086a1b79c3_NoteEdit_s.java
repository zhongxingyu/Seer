 package noteBlock.hig.noteedit;
 
 import java.text.SimpleDateFormat;
 
 import noteBlock.hig.R;
 import noteBlock.hig.alarm.AlarmManagerService;
 import noteBlock.hig.location.GoogleMapsActivity;
 import noteBlock.hig.notepad.NotesDbAdapter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 
 /**
  * This is a activity thats called from the {@link Notepad}
  * It Extends FragmentActivity so we can have orientation on the date input
  * @author Kristoffer Benum , and Solveig Srheim
  *
  */
 public class NoteEdit extends FragmentActivity {
 	private static final int MAPSINTENT_ID = 1;
 	private NotesDbAdapter mDbHelper;
 	private Long mRowId;
 	private InitiateAlarmButtons initiateAlarmButtons;
 	private NoteEditLayoutManager layoutManager;
 	private NoteEditSavePopulate savePopulateManager;
 	private IntentFilter intentFilter;
 	private ToggleButton showAlarmInfo;
 	private boolean gpsEnabled = false, networkEnabled = false;
 	public boolean DialogCheck;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.note_edit);
 		setTitle(R.string.edit_note);
 
 		// Log.i("NoteEdit", "created");
 
 		mDbHelper = new NotesDbAdapter(this);
 		mDbHelper.open();
 
 		initiateLayout(savedInstanceState);
 
 		savePopulateManager = new NoteEditSavePopulate(this, mDbHelper, mRowId);
 		savePopulateManager.populateFields();
 		if(savePopulateManager.getRowId() == null){
 			savePopulateManager.saveState();
 		}
 		
 		layoutManager = new NoteEditLayoutManager(this, mRowId,
 				savePopulateManager);
 		initiateAlarmButtons = new InitiateAlarmButtons(this, layoutManager);
 
 		// Set onClickListeners on buttons.
 		initiateButtons();
 
 		intentFilter = new IntentFilter(
 				"com.example.ass2note.notepad.NoteEdit.connectionReceiver");
 
 		cancelNotificationOnPanel();
 
 		layoutManager.displayAlarmInfo();
 		// initiateAlarmButtons.DateCheck = false;
 
 	}
 
 	private void cancelNotificationOnPanel() {
 		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		try {
 			mNotificationManager.cancel((Integer.parseInt(String
 					.valueOf(mRowId))));
 		} catch (Exception e) {
 			Log.i("NoteEdit", e.getMessage());
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 
 		Intent i = new Intent();
 		i.putExtra("rowId", savePopulateManager.getRowId());
 		
 		Log.i("NoteEdit", "onBackPressed. Savepop rowid is: " + savePopulateManager.getRowId());
 		Log.i("NoteEdit", "onBackPressed. noty rowid is: " + mRowId);
 		
 		if(savePopulateManager.emptyNote()) 
 			{ i.putExtra("deleteNote", true); }
 		
 		if(savePopulateManager.noTitle()){
 			savePopulateManager.setTitle(getString(R.string.NoTitleSet));
 		}
 		
 		setResult(Activity.RESULT_CANCELED, i);
 		
 		super.onBackPressed();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (initiateAlarmButtons.isDialogShowing())
 			DialogCheck = true;
 		else
 			DialogCheck = false;
 
 		initiateAlarmButtons.dimiss();
 
 		
 Log.i("noteedit", "trying to destroy");
 
 		
 		
 		savePopulateManager.closeDB();
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		RelativeLayout focuslayout = (RelativeLayout) findViewById(R.id.RequestFocusLayout);
 		focuslayout.requestFocus();
 	}
 
 	private void initiateLayout(Bundle savedInstanceState) {
 		mRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(NotesDbAdapter.KEY_ROWID);
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
 					: null;
 		}
 		
 	}
 
 	private void initiateButtons() {
 		Button confirmButton = (Button) findViewById(R.id.confirm);
 		Button alarmButton = (Button) findViewById(R.id.newNoteAlarm);
 
 		showAlarmInfo = (ToggleButton) findViewById(R.id.showAlarmNoteInfo);
 		showAlarmInfo.setSaveEnabled(false);
 
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			// ON click CONFIRM
 			public void onClick(View view) {
 				
 				if(!savePopulateManager.getTitle().trim().matches("")){
 					Log.i("NoteEdit ", "title was not null");
 					setResult(RESULT_OK);
 					finish();
 				}else if(savePopulateManager.noTitle()){
 					Log.i("NoteEdit ", "title is null but info is written");
 					alertToast("Please enter a title");
 					EditText mTitleText = (EditText) findViewById(R.id.title);
 					mTitleText.requestFocus();
 				}else{
 					Intent i = new Intent();
 					Log.i("NoteEdit ", "title is null and everything else too");
 					i.putExtra("rowId", mRowId);
 					i.putExtra("deleteNote", true);
 					setResult(Activity.RESULT_CANCELED, i);
 					finish();
 				}
 			}
 		});
 
 		alarmButton.setOnClickListener(new View.OnClickListener() {
 			// ON click ALARM
 			public void onClick(View v) {
 
 				initiateAlarmButtons.initiateAlarmButtonDialog(
 						getSupportFragmentManager(), initiateAlarmButtons);
 			}
 		});
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		savePopulateManager.saveState();
 
 		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
 		outState.putParcelable("alarmToggle",
 				showAlarmInfo.onSaveInstanceState());
 
 		if (initiateAlarmButtons.isDialogShowing())
 			DialogCheck = true;
 		else
 			DialogCheck = false;
 
 		outState.putBoolean("dialog", DialogCheck);
 
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		showAlarmInfo.onRestoreInstanceState(savedInstanceState
 				.getParcelable("alarmToggle"));
 		layoutManager.showAlarmLayout();
 		DialogCheck = savedInstanceState.getBoolean("dialog");
 		if (DialogCheck) {
 
 			initiateAlarmButtons.initiateAlarmButtonDialog(
 					getSupportFragmentManager(), initiateAlarmButtons);
 		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(connectionReceiver);
 		savePopulateManager.saveState();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(connectionReceiver, intentFilter);
 		savePopulateManager.populateFields();
 	}
 
 	/**
 	 * Function. Receives information from GoogleMapsActivity when the activity
 	 * finishes. Both latitude and longitude from the preferred position from
 	 * the user is available here.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		// The result came from GoogleMapsActivity:
 		if (requestCode == MAPSINTENT_ID)
 			switch (resultCode) {
 			// A new location was selected:
 			case Activity.RESULT_OK: {
				// Fetch the new data:
 				savePopulateManager.savePosition(
 						data.getStringExtra("latitude"),
 						data.getStringExtra("longitude"),
						data.getStringExtra("snippet"), "true");
 
 				layoutManager.displayAlarmInfo();
 				break;
 			}
 			// Unexpected occurrence happened or no new location was selected:
 			case Activity.RESULT_CANCELED: {
 				break;
 			}
 			} // end switch
 	} // end onActivityResult
 
 	private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String fromCaller = intent.getStringExtra("fromCaller");
 
 			if (fromCaller.contains("ConnectionService")) {
 				checkConnection(intent);
 
 			} else if (fromCaller.contains("InitiateAlarmButtons")) {
 				Log.i("NoteEdit", "receiver called from InitiateAlarmButtons.");
 
 				String command = intent.getStringExtra("command");
 				if (command.contains("updateTime")) {
 					SimpleDateFormat dateFormat = new SimpleDateFormat(
 							"dd-MM-yyyy HH:mm");
 					long time = intent.getLongExtra("time", 0);
 					Log.i("NoteEdit", "time is: " + dateFormat.format(time));
 
 					savePopulateManager.saveTime(time, "true");
 					layoutManager.displayAlarmInfo();
 
 					alertToast(getString(R.string.toast_set_alarm) + " \n"
 							+ dateFormat.format(time));
 
 					// TODO: Start this at an other place..
 					startTimeAlarm();
 				} else if (command.contains("stopTimeAlarm")) {
 					stopTimeAlarm(intent.getLongExtra("time", 0));
 				}
 			}
 		}
 	};
 
 	private void checkConnection(Intent intent) {
 		gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
 		networkEnabled = intent.getBooleanExtra("networkEnabled", false);
 
 		if (networkEnabled && gpsEnabled)
 			startGoogleMaps();
 		else if (networkEnabled && !gpsEnabled)
 			alertGPSConnection();
 		else if (!networkEnabled)
 			alertToast(getString(R.string.network_alert));
 		else
 			Log.e("NoteEdit checkConnection",
 					"Something really weird is going on now...");
 	}
 
 	private void startGoogleMaps() {
 		Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
 		i.putExtra("LATITUDE", savePopulateManager.getLatitude());
 		i.putExtra("LONGITUDE", savePopulateManager.getLongitude());
 		i.putExtra("gpsEnabled", gpsEnabled);
 		i.putExtra("networkEnabled", networkEnabled);
 		startActivityForResult(i, MAPSINTENT_ID);
 	}
 
 	private void startTimeAlarm() {
 		long closestTime[] = mDbHelper.getClosestTime();
 		if (closestTime[0] > 0) {
 			Intent i = new Intent(this, AlarmManagerService.class);
 			i.putExtra("alarmType", "time");
 			i.putExtra("time", closestTime[0]);
 			i.putExtra("rowId", closestTime[1]);
 			i.putExtra("COMMAND", "Start Alarm");
 			startService(i);
 		}
 	}
 
 	private void stopTimeAlarm(long time) {
 		if (time > 0) {
 			Intent i = new Intent(this, AlarmManagerService.class);
 			i.putExtra("alarmType", "time");
 			i.putExtra("time", time);
 			i.putExtra("rowId", mRowId);
 			i.putExtra("COMMAND", "Stop Alarm");
 			startService(i);
 		}
 	}
 
 	private void alertToast(String message) {
 		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.CENTER, 0, 0);
 		toast.show();
 	}
 
 	private void alertGPSConnection() {
 		AlertDialog.Builder altDialog = new AlertDialog.Builder(this);
 		altDialog.setMessage("Please start your GPS and try again. The GPS "
 				+ "needs to be ON all the time for this function to work.");
 
 		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				enableLocationSettings();
 			}
 		});
 
 		altDialog.setNegativeButton(R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// User cancelled the dialog
 					}
 				});
 		altDialog.show();
 	}
 
 	private void enableLocationSettings() {
 		Intent settingsIntent = new Intent(
 				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 		startActivity(settingsIntent);
 	}
 }
