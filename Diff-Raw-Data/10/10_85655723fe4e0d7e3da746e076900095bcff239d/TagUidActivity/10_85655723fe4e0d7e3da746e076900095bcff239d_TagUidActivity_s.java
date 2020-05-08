 package info.staticfree.android.taguid;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.List;
 
 import org.json.JSONException;
 
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.content.OperationApplicationException;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.NfcA;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract.Contacts;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.android.BluetoothChat.DeviceListActivity;
 
 /**
  * RFID door interface
  *
  */
 /**
  * @author steve
  *
  */
 public class TagUidActivity extends FragmentActivity implements OnClickListener, ServiceConnection,
         LoaderCallbacks<Cursor> {
     private static final String TAG = TagUidActivity.class.getSimpleName();
 
     PendingIntent pendingIntent;
 
     private IntentFilter[] intentFiltersArray;
 
     private NfcAdapter mAdapter;
 
     private final String[][] techListsArray = new String[][] { new String[] { NfcA.class.getName() } };
 
     private TextView mUidTextView;
     private TextView mUidIntTextView;
     private ViewGroup mControls;
 
     private BluetoothAdapter mBluetoothAdapter;
 
     private SharedPreferences mPrefs;
 
     private ArduinoConnectService mArduinoService;
 
     private ListView mList;
     private RfidAdapter mRfidListAdapter;
 
     private ProgressBar mLoadingView;
 
     private RfidRecord mScannedRecord;
 
     private Spinner mGroupView;
     private int mLastSelectedGroup;
 
     private RfidRecord mSelectedId;
 
     private Cursor mContacts;
 
     private static final int REQUEST_PAIR = 100, REQUEST_BT_ENABLE = 101, REQUEST_BT_DEVICE = 102;
 
     private static final int REQUEST_PICK_CONTACT = 103;
 
     private static final String INSTANCE_STATE_SELECTED_ID = "info.staticfree.android.SELECTED_ID";
     private static final String INSTANCE_STATE_SCANNED_ID = "info.staticfree.android.SCANNED_ID";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mUidTextView = (TextView) findViewById(R.id.uid);
         mUidIntTextView = (TextView) findViewById(R.id.uid_int);
         mLoadingView = (ProgressBar) findViewById(R.id.loading);
         mControls = (ViewGroup) findViewById(R.id.controls);
         findViewById(R.id.connect).setOnClickListener(this);
         findViewById(R.id.add).setOnClickListener(this);
         findViewById(R.id.open).setOnClickListener(this);
 
         mGroupView = (Spinner) findViewById(R.id.cur_group);
 
         mGroupView.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                 if (mLastSelectedGroup != position) {
                     if (mArduinoService != null) {
                         mArduinoService.requestSetCurGroup(position + 1);
                     }
                     mLastSelectedGroup = position;
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapter) {
                 // that's fine. Nothing to do...
             }
         });
 
         mUidTextView.setOnClickListener(this);
         mUidIntTextView.setOnClickListener(this);
 
         mList = (ListView) findViewById(android.R.id.list);
 
         mRfidListAdapter = new RfidAdapter(this, R.layout.rfid_list_item, null);
         mList.setAdapter(mRfidListAdapter);
 
         getSupportLoaderManager().initLoader(0, null, this);
 
         parseIntent(getIntent());
 
         pendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
         final IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
         try {
             ndef.addDataType("*/*"); /*
                                       * Handles all MIME based dispatches. You should specify only
                                       * the ones that you need.
                                       */
         } catch (final MalformedMimeTypeException e) {
             throw new RuntimeException("fail", e);
         }
         intentFiltersArray = new IntentFilter[] { ndef, };
 
         mAdapter = NfcAdapter.getDefaultAdapter(this);
 
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
         mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         registerForContextMenu(mList);
 
         if (savedInstanceState != null) {
             mScannedRecord = savedInstanceState.getParcelable(INSTANCE_STATE_SCANNED_ID);
             mSelectedId = savedInstanceState.getParcelable(INSTANCE_STATE_SELECTED_ID);
             if (mScannedRecord != null) {
                 showUid(mScannedRecord);
             }
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
        mAdapter.disableForegroundDispatch(this);
         if (mArduinoService != null) {
 
             unbindService(this);
             mArduinoService = null;
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (mArduinoService == null) {
             final Intent arduinoService = new Intent(this, ArduinoConnectService.class);
             startService(arduinoService);
             bindService(arduinoService, this, 0);
         }
 
         maybeAutoconnect();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putParcelable(INSTANCE_STATE_SELECTED_ID, mSelectedId);
         outState.putParcelable(INSTANCE_STATE_SCANNED_ID, mScannedRecord);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.export:
                 export();
                 return true;
 
             case R.id.importContacts:
                 importContacts();
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
 
     }
 
     private void export() {
         try {
             ContactUtils.export(this, new File(Environment.getExternalStorageDirectory(),
                     "rfid_export.json"));
         } catch (final IOException e) {
             Log.e(TAG, "export error", e);
         } catch (final JSONException e) {
             Log.e(TAG, "export error", e);
         }
     }
 
     private void importContacts() {
         try {
             ContactUtils.importContacts(this, new File(Environment.getExternalStorageDirectory(),
                     "rfid_export.json"));
         } catch (final RemoteException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (final IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (final JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (final OperationApplicationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.uid:
             case R.id.uid_int:
                 copyTextToClipboard(v.getId());
                 break;
 
             case R.id.connect:
                 connectOrPair();
                 break;
 
             case R.id.add:
                 if (mArduinoService != null) {
                     mArduinoService.requestAdd(mScannedRecord);
                 }
                 break;
 
             case R.id.open:
                 if (mArduinoService != null) {
                     mArduinoService.requestOpen();
                 }
                 break;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_PAIR: {
                 if (RESULT_OK == resultCode) {
 
                 }
             }
                 break;
 
             case REQUEST_BT_ENABLE: {
                 if (RESULT_OK == resultCode) {
                     connectOrPair();
                 }
             }
                 break;
 
             case REQUEST_BT_DEVICE: {
                 if (RESULT_OK == resultCode) {
                     final String btAddr = data.getExtras().getString(
                             DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                     mPrefs.edit().putString(ArduinoConnectService.PREF_BT_ADDR, btAddr).commit();
                 }
             }
                 break;
 
             case REQUEST_PICK_CONTACT: {
                 if (RESULT_OK == resultCode) {
                     associateIdWithContact(mSelectedId, data);
                 } else {
                     mSelectedId = null;
                 }
             }
                 break;
         }
     }
 
     private void associateIdWithContact(RfidRecord selectedId, Intent data) {
 
         final ContentValues cv = new ContentValues();
 
         cv.put(ContactId.CONTACT_URI, data.getDataString());
         cv.put(ContactId.RFID, selectedId.getIdString());
 
         final Uri rec = getContentResolver().insert(ContactId.CONTENT_URI, cv);
         Toast.makeText(this, "Added: " + rec, Toast.LENGTH_LONG).show();
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
 
         parseIntent(intent);
     }
 
     private void parseIntent(Intent intent) {
 
         final String action = intent.getAction();
         if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                 || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                 || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
 
             final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 
             mScannedRecord = new RfidRecord(tag.getId(), 1);
             showUid(mScannedRecord);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         } catch (final ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return;
         }
 
         final RfidRecord item = mRfidListAdapter.getItem(info.position);
 
         getMenuInflater().inflate(R.menu.rfid_context, menu);
         menu.setHeaderTitle(item.getIdString());
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (final ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return false;
         }
 
         final RfidRecord rfid = mRfidListAdapter.getItem(info.position);
 
         switch (item.getItemId()) {
             case R.id.delete:
                 mArduinoService.requestDeleteId(rfid);
                 return true;
 
             case R.id.associate_contact:
                 mSelectedId = rfid;
                 startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI),
                         REQUEST_PICK_CONTACT);
                 return true;
 
             default:
                 return super.onContextItemSelected(item);
         }
 
     }
 
     /**
      * Displays the ID on the screen so that it can be added.
      *
      * @param uid
      */
     private void showUid(RfidRecord uid) {
 
         mUidTextView.setText(uid.getIdString());
         final byte[] unsigned = new byte[uid.id.length + 1];
 
         System.arraycopy(uid.id, 0, unsigned, 1, uid.id.length);
         mUidIntTextView.setText(new BigInteger(unsigned).toString());
 
         findViewById(R.id.add).setEnabled(true);
     }
 
     private void copyTextToClipboard(int textViewId) {
         final TextView tv = (TextView) findViewById(textViewId);
 
         final CharSequence t = tv.getText();
         final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 
         cm.setText(t);
 
         Toast.makeText(this, t + " copied to clipboard.", Toast.LENGTH_SHORT).show();
     }
 
     // ////////////////////////////////////////
     // Bluetooth service
     // ////////////////////////////////////////
 
     /**
      * Only autoconnects if Bluetooth is already enabled and the device has been paired before.
      */
     private void maybeAutoconnect() {
         if (!mBluetoothAdapter.isEnabled()) {
             return;
         }
 
         final String btAddr = mPrefs.getString(ArduinoConnectService.PREF_BT_ADDR, null);
         if (btAddr == null) {
             return;
         }
 
         connectOrPair();
     }
 
     /**
      * Connects or pairs a device. Will request Bluetooth to be enabled if it's not.
      */
     private void connectOrPair() {
         if (!mBluetoothAdapter.isEnabled()) {
             startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                     REQUEST_BT_ENABLE);
             return;
         }
 
         final String btAddr = mPrefs.getString(ArduinoConnectService.PREF_BT_ADDR, null);
         if (btAddr == null) {
             startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_BT_DEVICE);
             return;
         }
 
         if (mArduinoService != null) {
             mArduinoService.connect();
         }
     }
 
     @Override
     public void onServiceConnected(ComponentName name, IBinder service) {
         mArduinoService = ((ArduinoConnectService.LocalBinder) service).getService();
 
         mArduinoService.setResultListener(mResultListener);
 
         mArduinoService.connect();
 
     }
 
     @Override
     public void onServiceDisconnected(ComponentName name) {
         mArduinoService.setResultListener(null);
         mArduinoService = null;
     }
 
     private final OnDoorResultListener mResultListener = new OnDoorResultListener() {
         private boolean mFirstLoad = true;
 
         @Override
         public void onVersionResult(String version) {
             Toast.makeText(TagUidActivity.this, version, Toast.LENGTH_LONG).show();
 
         }
 
         @Override
         public void onListResult(List<RfidRecord> rfidRecords) {
 
             mRfidListAdapter.setRecords(rfidRecords);
             getSupportLoaderManager().restartLoader(0, null, TagUidActivity.this);
 
         }
 
         @Override
         public void onStateChange(int state) {
             switch (state) {
                 case ArduinoConnectService.STATE_READY:
                     mLoadingView.setVisibility(View.GONE);
                     mControls.setEnabled(true);
                     // handle the first load
                     if (mFirstLoad) {
                         mLoadingView.setVisibility(View.GONE);
                         findViewById(R.id.connect).setVisibility(View.GONE);
                         mArduinoService.requestIdList();
                         mArduinoService.requestGetCurGroup();
                         mFirstLoad = false;
                     }
 
                     break;
 
                 case ArduinoConnectService.STATE_CONNECTING:
                     mLoadingView.setVisibility(View.VISIBLE);
                     findViewById(R.id.connect).setVisibility(View.GONE);
                     mControls.setEnabled(false);
                     mFirstLoad = true;
                     break;
 
                 case ArduinoConnectService.STATE_DISCONNECTED:
                     mControls.setEnabled(false);
                     mLoadingView.setVisibility(View.GONE);
                     findViewById(R.id.connect).setVisibility(View.VISIBLE);
                     mRfidListAdapter.setRecords(null);
 
                     break;
 
                 case ArduinoConnectService.STATE_WAITING_FOR_RESPONSE:
                 case ArduinoConnectService.STATE_READING_RESPONSE:
                     mLoadingView.setVisibility(View.VISIBLE);
                     mControls.setEnabled(false);
                     break;
 
             }
         }
 
         @Override
         public void onAddResult(boolean result) {
             mArduinoService.requestIdList();
 
         }
 
         @Override
         public void onDeleteResult(boolean result) {
             mArduinoService.requestIdList();
 
         }
 
         @Override
         public void onOpenResult() {
             Toast.makeText(TagUidActivity.this, R.string.door_open_result, Toast.LENGTH_SHORT)
                     .show();
         }
 
         @Override
         public void onCurGroupResult(int group) {
             mLastSelectedGroup = group - 1;
             mGroupView.setSelection(group - 1);
 
         }
     };
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(this, ContactId.CONTENT_URI, null, null, null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
         mRfidListAdapter.swapCursor(c);
 
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         mRfidListAdapter.swapCursor(null);
 
     }
 
 }
