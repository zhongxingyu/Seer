 package de.franken.fermi.meterpal;
 
 import java.text.DateFormat;
 import java.util.HashMap;
 
 import de.franken.fermi.meterpal.DatabaseHelper.mySQLiteOpenHelper;
 import de.franken.fermi.meterpal.R.id;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDoneException;
 import android.database.sqlite.SQLiteStatement;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ShareActionProvider;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class RecordDeviceReadingActivity extends Activity implements OnItemSelectedListener {
 	/*
 	 * the activity can have two states: ENTER_READING, enters a reading on an
 	 * existing device. ENTER_DEVICE, enter a new device into the database.
 	 */
 
 	/*
 	private static final int STATE_ENTER_READING = 0;
 	private static final int STATE_ENTER_DEVICE = 1;
 	private int mState;
 */
 	
 	public final static String INTENT_ENTER_DEVICE_READING = "de.franken.meterpal.intent_enter_device_reading";
 	public final static String EXTRA_MESSAGE = "de.franken.meterpal.recordCounterWithName";
 	private static final String TAG = "RecordCounterActivity";
 
 	// Handle to a new DatabaseHelper.
 	private mySQLiteOpenHelper mOpenHelper;
 	private SQLiteDatabase db ;
 	private Long mDeviceID;
 	private String mDeviceName;
 	private SimpleCursorAdapter mEntryAdapter; // XXX needs version 11 or greater
 	private SimpleCursorAdapter mDeviceAdapter;
 	private Intent mShareIntent;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/*
 		 * The DatabaseHelper is an object that is a singleton within this Application.
 		 * We can thus refer to it from here.
 		 */
 	    DatabaseHelper dbh = ((DatabaseHelper)getApplicationContext());
 		mOpenHelper = (mySQLiteOpenHelper)dbh.getSQLiteOpenHelper();
 		db = mOpenHelper.getWritableDatabase();
 
 		// the only intent we have is to record a meter. See if we've been given
 		// a meter ID that is in our database
 		Intent intent = getIntent();
 		String extra = intent.getStringExtra(EXTRA_MESSAGE);
 
 		/*
 		 * The intent can get the device ID as extra. If there is no extra, or
 		 * the device id does not exist, pick the first available. if there is
 		 * none available, enter a new device.
 		 */
 
 		// XXX I think we should be catching and handling the exception here
 		if (extra != null) {
 			mDeviceID = Long.parseLong(extra);
 			mDeviceName = getCounterName(mDeviceID); // mis-use to see if we
 														// have this device
 			if (mDeviceName == null) {
 				extra = null; // discard invalid device ID
 			}
 		}
 
 		if (extra == null) {
 			mDeviceID = getFirstCounterID();
 			if (mDeviceID == null) {
 				intent = new Intent(this, NewDeviceActivity.class); startActivity(intent);
 				// never return
 			} else {
 				mDeviceName = getCounterName(mDeviceID);
 			}
 		}
 		setContentView(R.layout.activity_record_counter);
 
 		/*
 		 * to always fill the spinner with the availabel meters, set up a device adapter
 		 * and connect it to the spinner. The cursor will be set up at resume() time.
 		 */
 		mDeviceAdapter = new SimpleCursorAdapter(
 				getApplicationContext(),
 				R.layout.device_spinner,
 				null, // no cursor available yet
 				new String[] { DatabaseHelper.dbc.dev.COLUMN_NAME_METER_NAME }, // from
 				new int[] {R.id.actionBarItem},// to
 				0);
 
 		Spinner spinner = (Spinner) findViewById(R.id.deviceSpinner);
 		spinner.setAdapter(mDeviceAdapter);
 		spinner.setOnItemSelectedListener(this);
 
 		/*
 		 * to have the log update from the database, create an "entry adapter" and connect it to the
 		 * listview.
 		 */
 		mEntryAdapter = new SimpleCursorAdapter(
 				getApplicationContext(),
 				R.layout.reading_logview,
 				null, // no cursor available yet
 				new String[] {DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_READATTIME, DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_VALUE},// from
 				new int[] {R.id.logEntryDatetime,R.id.logEntryValue},// to
 				0);
 
 		ListView lv = (ListView)findViewById(R.id.logView);
 		lv.setAdapter(mEntryAdapter);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		db.close();
 		mDeviceAdapter = null;
 		mEntryAdapter = null;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		onDeviceIDChanged();
 
 		Cursor c = db.query(false, // not unique
 				DatabaseHelper.dbc.dev.TABLE_NAME, // table name
 				new String[] { DatabaseHelper.dbc.dev._ID, DatabaseHelper.dbc.dev.COLUMN_NAME_METER_NAME }, // column
 				null, // select
 				null, // no selection args
 				null, // no groupBy
 				null, // no having
 				null, // no specific order
 				null); // no limit
 
 		mDeviceAdapter.changeCursor(c);
 	}
 
 	private void onDeviceIDChanged() {
 		Cursor c = db.query(false, // not unique
 				DatabaseHelper.dbc.entries.TABLE_NAME, // table name
 				null, // all columns
 				DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_ID + "=" + mDeviceID, // select
 				null, // no selection args
 				null, // no groupBy
 				null, // no having
 				DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_READATTIME + " DESC" , // order by time, latest first
 				null); // no limit
 
 		mEntryAdapter.changeCursor(c);
 
 		/*
 		 * display selected device as title
 		 */
 		mDeviceName = getCounterName(mDeviceID);
 		setTitle(mDeviceName);
 
 		/*
 		 * set the last meter reading as default
 		 */
 		EditText et = (EditText)findViewById(id.meterTakenValue);
 		et.setText(getLastMeterValue(mDeviceID).toString());
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mEntryAdapter.changeCursor(null);
 		mDeviceAdapter.changeCursor(null);
 	}
 
 	private ShareActionProvider mShareActionProvider;
 
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.record_counter, menu);
 
 		// Locate MenuItem with ShareActionProvider
 		MenuItem item = menu.findItem(R.id.menu_item_share);
 
 		// Fetch and store ShareActionProvider
 		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
 
 		mShareIntent = new Intent();
 		mShareIntent.setAction(Intent.ACTION_SEND);
 		mShareIntent.setType("text/plain");
 		mShareIntent.putExtra(Intent.EXTRA_TEXT, mOpenHelper.getDatabaseDump());
 		mShareActionProvider.setShareIntent(mShareIntent); // without this, we could stick to API level 8
 
 		return true;
 	}
 
 	/*
 	 * OnItemSelectedListener methods
 	 */
     public void onItemSelected(AdapterView<?> parent, View view, 
             int pos, long id) {
         // An item was selected. You can retrieve the selected item using
         // parent.getItemAtPosition(pos)
     	mDeviceID = Long.valueOf(parent.getItemIdAtPosition(pos));
     	onDeviceIDChanged();
     }
 
     public void onNothingSelected(AdapterView<?> parent) { }
 
     public void onNextDeviceClicked(View v)
     {
     	mDeviceID = nextDeviceID(mDeviceID);
     	onDeviceIDChanged();
     }
 
     public void onPreviousDeviceClicked(View v)
     {
     	mDeviceID = previousDeviceID(mDeviceID);
     	onDeviceIDChanged();
     }
     
     public void newReadingDone(View view) {
 		TextView t;
 		t = (TextView) findViewById(R.id.meterTakenValue);
 		double value = Double.parseDouble(t.getText().toString());
 
 		ContentValues cv = new ContentValues();
 		cv.put(DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_ID, mDeviceID);
 		cv.put(DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_READATTIME,System.currentTimeMillis());
 		cv.put(DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_VALUE, value);
 
 		db.insertOrThrow(DatabaseHelper.dbc.entries.TABLE_NAME, // table
 				null, // nullColumnHack
 				cv);
 
 		onDeviceIDChanged();
 
 		/*
 		 * update the sharing intent.
 		 */
 		mShareIntent.removeExtra(Intent.EXTRA_TEXT);
 		mShareIntent.putExtra(Intent.EXTRA_TEXT, mOpenHelper.getDatabaseDump());
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_newmeter:
 			Intent intent = new Intent(this, NewDeviceActivity.class); startActivity(intent);
 			// never return
 
 			return true;
 		case R.id.menu_deldb:
 			mOpenHelper.deleteDB();
 			intent = new Intent(this, NewDeviceActivity.class); startActivity(intent);
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private Long nextDeviceID(long id)
 	{
 		return seekDeviceID(id,+1);
 	}
 
 	private Long previousDeviceID(long id)
 	{
 		return seekDeviceID(id,-1);
 	}
 
 	private Long seekDeviceID(long id, int dir)
 	{
 		SQLiteStatement stmta = db.compileStatement("SELECT "+DatabaseHelper.dbc.dev._ID+" FROM "+DatabaseHelper.dbc.dev.TABLE_NAME+" WHERE "+DatabaseHelper.dbc.dev._ID+" > ? ORDER BY "+DatabaseHelper.dbc.dev._ID+" ASC LIMIT 1");
 		SQLiteStatement stmtd = db.compileStatement("SELECT "+DatabaseHelper.dbc.dev._ID+" FROM "+DatabaseHelper.dbc.dev.TABLE_NAME+" WHERE "+DatabaseHelper.dbc.dev._ID+" < ? ORDER BY "+DatabaseHelper.dbc.dev._ID+" DESC LIMIT 1");
 		SQLiteStatement stmt ;
 		
 		switch(dir) {
 		case +1:
 			stmt = stmta;
 			break;
 		case -1:
 			stmt = stmtd;
 			break;
 		default:
 			stmt = null;
 		}
 		
 		try {
 			stmt.bindLong(1,id);
 			id = stmt.simpleQueryForLong();
 		}
 		catch (SQLiteDoneException e) {
			stmt.bindLong(1,-dir*10000); // XXX hack hack!!!
 			id = stmt.simpleQueryForLong();
 		}
 
 		return Long.valueOf(id);
 	}
 
 	private final Long getFirstCounterID() {
 		Cursor c = db.query(true, // unique
 				DatabaseHelper.dbc.dev.TABLE_NAME, // table name
 				new String[] {DatabaseHelper.dbc.dev._ID}, // only the id column
 				null, // select all
 				null, // no selection args
 				null, // no groupBy
 				null, // no having
 				DatabaseHelper.dbc.dev._ID, // order by _ID
 				null); // no limit
 
 		Long rv = null;
 		if (c.moveToFirst()) {
 			rv = c.getLong(0);
 		}
 
 		return rv ;
 	}
 
 	private final String getCounterName(long ID) {
 		Cursor c = db.query(true, DatabaseHelper.dbc.dev.TABLE_NAME, new String[] {DatabaseHelper.dbc.dev.COLUMN_NAME_METER_NAME}, DatabaseHelper.dbc.dev._ID + "="
 				+ ID, null, null, null, null, null);
 		// c = db.query (true, table, columns, selection, selectionArgs,
 		// groupBy, having, orderBy, limit)
 
 		String name = null ;
 		if (c.moveToFirst()) {
 			name = c.getString(0);
 		}
 
 		return name;
 	}
 
 	private Double getLastMeterValue(long ID) {		
 		Cursor c = db.query(true, // unique
 				DatabaseHelper.dbc.entries.TABLE_NAME, // table name
 				new String[] {DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_VALUE}, // only the value
 				DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_ID + "=" + ID, // select entries just for this device
 				null, // no selection args
 				null, // no groupBy
 				null, // no having
 				DatabaseHelper.dbc.entries.COLUMN_NAME_COUNTER_READATTIME + " DESC", // order by timestamp, last first
 				"1"); // and return the first
 	
 		Double rv = null ;
 		if (c.moveToFirst()) {
 			rv = c.getDouble(0);
 		}
 
 		return rv ;
 	}
 }
 
