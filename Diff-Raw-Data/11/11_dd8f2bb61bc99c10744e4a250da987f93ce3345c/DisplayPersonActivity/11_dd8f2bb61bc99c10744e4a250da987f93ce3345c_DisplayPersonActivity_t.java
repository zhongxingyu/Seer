 package com.example.nearfieldnetworking;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 
 import com.example.nearfieldnetworking.FileSelectDialog.NoticeDialogListener;
 
 import android.app.ActionBar;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.provider.Settings;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /********************************************************
  * DisplayPersonActivity This Activity is responsible for displaying a person It
  * should be passed the directory of the person and a boolean indicating whether
  * or the profile can be edited
  * 
  *******************************************************/
 public class DisplayPersonActivity extends FragmentActivity implements
 		CreateNdefMessageCallback, OnNdefPushCompleteCallback,
 		NoticeDialogListener {
 
 	static final String PROFILE_PIC_FILE_NAME = ".profile_pic.jpg";
 	static final String PERSON_FILE_NAME = ".person";
 
 	// /////NFC DENNIS///////
 	private NfcAdapter mNfcAdapter;
 	private BluetoothAdapter mBluetoothAdapter;
 	private NFCService mNFCService = null;
 	private BluetoothDevice bt;
 
 	private Uri[] filesToSend;
 	// private DialogFragment newFragment;
 	TextView mInfoText;
 
 	public static final int REQUEST_ENABLE_BT = 1;
 	public static final int REQUEST_FILE = 2;
 
 	// Message types sent from the BluetoothChatService Handler
 	public static final int MESSAGE_STATE_CHANGE = 1;
 	public static final int MESSAGE_READ = 2;
 	public static final int MESSAGE_WRITE = 3;
 	public static final int MESSAGE_DEVICE_NAME = 4;
 	public static final int MESSAGE_TOAST = 5;
 	public static final int MESSAGE_SENT = 6;
 	public static final int MESSAGE_UPDATE = 7;
 
 	// Key names received from the BluetoothChatService Handler
 	public static final String DEVICE_NAME = "device_name";
 	public static final String PROGRESS = "progress";
 	public static final String TOTAL = "total";
 	public static final String NUM_LEFT = "num_left";
 	public static final String FNAME = "fname";
 	public static final String TOAST = "toast";
 
 	private String recievedFilepath;
 	private String recievedFilename;
 	private FileOutputStream fos = null;
 	private int totalSize;
 	private int numFilesLeft;
 	private boolean flag = false;
 	private ProgressDialog progressBar;
 	private ProgressDialog progressSBar;
 	private ProgressDialog waitBar;
 	// ///////////////////
 
 	// private variables
 	private String person_path = "";
 	private boolean editable;
 	private Person person = new Person("");
 	private DialogFragment newFragment;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.display_person);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 
 		// fetch data from intent
 		Bundle extras = getIntent().getExtras();
 		if (!extras.equals(null)) {
 			person_path = extras.getString("person_directory");
 			editable = extras.getBoolean("editable", false);
 			// Log.d("debug",person_path);
 
 			if (person_path == null) {
 				Log.d("debug", "there are no extras");
 				String MAIN_DIR = Environment.getExternalStorageDirectory()
 						+ File.separator + "near_field_networking";
 				String MY_PROFILE_PATH = MAIN_DIR + File.separator
 						+ "my_profile";
 				person_path = MY_PROFILE_PATH;
 				editable = true;
 			}
 		}
 
 		// make sure person_directory exists
 		File person_dir = new File(person_path);
 		if (!person_dir.exists() || !person_dir.isDirectory()) {
 			Toast.makeText(getApplicationContext(), "No person directory",
 					Toast.LENGTH_SHORT).show();
 			finish();
 		}
 
 		// if can edit, set on click listener
 		Button edit_button = (Button) findViewById(R.id.button1);
 		if (editable) {
 			edit_button.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					Intent intent = new Intent(getBaseContext(),
 							EditPersonActivity.class);
 					intent.putExtra("person_directory", person_path);
 					startActivityForResult(intent, 0);
 				}
 			});
 			;
 		} else {
 			// else hide the button
 			edit_button.setVisibility(View.GONE);
 		}
 
 		// FAIZAN ADD
 		// Button dialog_button = (Button) findViewById(R.id.button2);
 		//
 		// dialog_button.setOnClickListener(new View.OnClickListener() {
 		// public void onClick(View v) {
 		//
 		// Bundle args = new Bundle();
 		// args.putString("passPath", person_path);
 		//
 		// newFragment = new FileSelectDialog();
 		// newFragment.setArguments(args);
 		// newFragment.show(getSupportFragmentManager(), "chooseFiles");
 		// }
 		// });
 		// ;
 		// FINISH FAIZAN ADD
 
 		// load image profile image from file
 		loadImage();
 
 		// load person from file
 		loadPerson();
 
 		// load list of files associated with the person
 		loadList();
 
 		// /NFC///
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 
 		if (mNfcAdapter == null) {
 			mInfoText = (TextView) findViewById(R.id.textView);
 			mInfoText.setText("NFC is not available on this device.");
 		} else {
 			// Register callback to set NDEF message
 			mNfcAdapter.setNdefPushMessageCallback(this, this);
 			// Register callback to listen for message-sent success
 			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
 		}
 
 		File directory = new File(Environment.getExternalStorageDirectory()
 				+ File.separator + "NFN");
 		directory.mkdirs();
 
 		// /
 	}
 
 	// @Override
 	// public boolean onOptionsItemSelected(MenuItem item) {
 	// switch (item.getItemId()) {
 	// case android.R.id.home:
 	// onBackPressed();
 	// return true;
 	// }
 	// return super.onOptionsItemSelected(item);
 	// }
 	//
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu) {
 	// // Inflate the menu; this adds items to the action bar if it is present.
 	// getMenuInflater().inflate(R.menu.activity_main, menu);
 	// return true;
 	// }
 	//
 	// //upon return from activity with file
 	// protected void onActivityResult (int requestCode, int resultCode, Intent
 	// data){
 	// //act according to request code
 	//
 	// //refresh the person and files (they may have changed after editing)
 	// loadPerson();
 	// loadList();
 	// loadImage();
 	// }
 
 	// load image from file
 	private void loadImage() {
 
 		// get image view
 		ImageView image_view = (ImageView) findViewById(R.id.image1);
 
 		// file where picture is stored
 		File image_file = new File(person_path + File.separator
 				+ PROFILE_PIC_FILE_NAME);
 		// if such a file exists, try to load person
 		if (image_file.exists()) {
 			try {
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inSampleSize = 8;
 				Bitmap image_bitmap = BitmapFactory.decodeFile(
 						image_file.getAbsolutePath(), options);
 				image_view.setImageBitmap(image_bitmap);
 
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(), "Could not map image",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		// set values layout
 		TextView name_text = (TextView) findViewById(R.id.textView2);
 		name_text.setText(person.getName());
 
 	}
 
 	// load person from file
 	private void loadPerson() {
 
 		// open person's object file
 		File person_file = new File(person_path + File.separator
 				+ PERSON_FILE_NAME);
 		if (!person_file.exists()) {
 			Toast.makeText(getApplicationContext(), "No person file",
 					Toast.LENGTH_SHORT).show();
 			finish();
 		} else {
 			try {
 				FileInputStream fin = new FileInputStream(person_file);
 				ObjectInputStream oin = new ObjectInputStream(fin);
 				person = (Person) oin.readObject();
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(),
 						"Error reading person object", Toast.LENGTH_SHORT)
 						.show();
 				finish();
 			}
 		}
 
 		// set values layout
 		TextView name_text = (TextView) findViewById(R.id.textView2);
 		name_text.setText(person.getName());
 
 		TextView email_text = (TextView) findViewById(R.id.TextView1);
 		email_text.setText(person.getEmailAddress());
 
 		TextView phone_text = (TextView) findViewById(R.id.TextView03);
 		phone_text.setText(person.getPhoneNumber());
 	}
 
 	// load list
 	private void loadList() {
 
 		// expandable list view
 		ExpandableListView list_view = (ExpandableListView) findViewById(R.id.expandableListView1);
 
 		// add all directories to categories
 		ArrayList<File> categories = new ArrayList<File>();
 		File person_dir = new File(person_path);
 		File[] files = person_dir.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				categories.add(files[i]);
 			}
 		}
 
 		// add all subfiles to subcategories
 		ArrayList<ArrayList<File>> subcategories = new ArrayList<ArrayList<File>>();
 		for (int i = 0; i < categories.size(); i++) {
 			ArrayList<File> sub_list = new ArrayList<File>();
 			subcategories.add(sub_list);
 			File[] sub_files = categories.get(i).listFiles();
 			for (int j = 0; j < sub_files.length; j++) {
 				if (!sub_files[j].isDirectory()) {
 					sub_list.add(sub_files[j]);
 				}
 			}
 		}
 
 		FileExpandableListAdapter adapter = new FileExpandableListAdapter(this,
 				categories, subcategories, false);
 
 		// Set this blank adapter to the list view
 		list_view.setAdapter(adapter);
 
 		// expand groups to start
 		for (int i = 0; i < adapter.getGroupCount(); i++) {
 			list_view.expandGroup(i);
 		}
 
 	}
 
 	// //////DENNIS NFC///////////////
 	@Override
 	public void onStart() {
 		super.onStart();
 		// if(D) Log.e(TAG, "++ ON START ++");
 		// If BT is not on, request that it be enabled.
 		// setupChat() will then be called during onActivityResult
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 			// Otherwise, setup the chat session
 		} else {
 			if (mNFCService == null)
 				mNFCService = new NFCService(this, mFileHandler);
 		}
 	}
 
 	/**
 	 * Implementation for the CreateNdefMessageCallback interface
 	 */
 	@Override
 	public NdefMessage createNdefMessage(NfcEvent event) {
 		String macAddr = mBluetoothAdapter.getAddress();
 		NdefMessage msg = new NdefMessage(NdefRecord.createMime(
 				"application/com.example.nearfieldnetworking",
 				macAddr.getBytes())
 		/**
 		 * The Android Application Record (AAR) is commented out. When a device
 		 * receives a push with an AAR in it, the application specified in the
 		 * AAR is guaranteed to run. The AAR overrides the tag dispatch system.
 		 * You can add it back in to guarantee that this activity starts when
 		 * receiving a beamed message. For now, this code uses the tag dispatch
 		 * system.
 		 */
 		// ,NdefRecord.createApplicationRecord("com.example.android.beam")
 		);
 		return msg;
 	}
 
 	/**
 	 * Implementation for the OnNdefPushCompleteCallback interface
 	 */
 	@Override
 	public void onNdefPushComplete(NfcEvent arg0) {
 		// A handler is needed to send messages to the activity when this
 		// callback occurs, because it happens from a binder thread
 		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
 	}
 
 	@Override
 	public synchronized void onPause() {
 		super.onPause();
 		Log.e("debug", "- ON PAUSE -");
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Log.e("debug", "-- ON STOP --");
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		Log.e("debug", "+ ON RESUME +");
 
 		if (mNFCService != null) {
 			// Only if the state is STATE_NONE, do we know that we haven't
 			// started already
 			if (mNFCService.getState() == NFCService.STATE_NONE) {
 				// Start the Bluetooth chat services
 				mNFCService.start();
 			}
 		}
 
 		// Check to see that the Activity started due to an Android Beam
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			processIntent(getIntent());
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Stop the Bluetooth chat services
 		if (mNFCService != null)
 			mNFCService.stop();
 		Log.e("debug", "--- ON DESTROY ---");
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		// onResume gets called after this to handle the intent
 
 		setIntent(intent);
 	}
 
 	/**
 	 * Parses the NDEF Message from the intent and prints to the TextView
 	 */
 	void processIntent(Intent intent) {
 		Parcelable[] rawMsgs = intent
 				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		// only one message sent during the beam
 		NdefMessage msg = (NdefMessage) rawMsgs[0];
 		// record 0 contains the MIME type, record 1 is the AAR, if present
 		String macAddr = new String(msg.getRecords()[0].getPayload());
 
 		// Pair BT Devices
 
 		bt = mBluetoothAdapter.getRemoteDevice(macAddr);
 
 		mNFCService.connect(bt);
 
 		waitBar = new ProgressDialog(DisplayPersonActivity.this);
 		waitBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		waitBar.setTitle("Initiating Transfer");
 		waitBar.setMessage("Waiting For Data");
 		waitBar.show();
 
 		waitBar.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				mNFCService.stop();
 				Toast.makeText(getApplicationContext(),
 						"Connection Terminated", Toast.LENGTH_LONG).show();
 				finish();
 
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_nfc, menu);
 		return true;
 	}
 
 	// @Override
 	// protected void onSaveInstanceState(Bundle outState) {
 	// //No call for super(). Bug on API Level > 11.
 	// }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.phone_nfc_settings:
 			Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
 			startActivity(intent);
 			return true;
 		case R.id.phone_bt_settings:
 			intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(intent, REQUEST_ENABLE_BT);
 			return true;
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		loadPerson();
 		loadList();
 		loadImage();
 	}
 
 	/** This handler receives a message from onNdefPushComplete */
 	private final Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 
 			case MESSAGE_SENT:
 				Bundle args = new Bundle();
 				args.putString("passPath", person_path);
 
 				newFragment = new FileSelectDialog();
 				newFragment.setArguments(args);
 
 				newFragment.show(getSupportFragmentManager(), "chooseFiles");
 
 				break;
 			}
 		}
 	};
 
 	private final Handler mFileHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 
 			case MESSAGE_STATE_CHANGE:
 				switch (msg.arg1) {
 				case NFCService.STATE_CONNECTED:
 					// setStatus(getString(R.string.title_connected_to,
 					// mConnectedDeviceName));
 					Log.d("debug", "connected");
 					break;
 				case NFCService.STATE_CONNECTING:
 					// setStatus(R.string.title_connecting);
 					Log.d("debug", "connecting");
 					break;
 				case NFCService.STATE_LISTEN:
 				case NFCService.STATE_NONE:
 					// setStatus(R.string.title_not_connected);
 					Log.d("debug", "closed");
 					break;
 				}
 				break;
 
 			case MESSAGE_READ:
 				byte[] readBuf = (byte[]) msg.obj;
 				int pos = msg.arg1;
 				// construct a string from the valid bytes in the buffer
 
 				byte[] buffer = new byte[990];
 
 				byte[] totalBytes = new byte[4];
 				byte[] filesLeft = new byte[4];
 
 				// Log.d("totalBytes",Integer.toString(wrapped.getInt()));
 
 				// Toast.makeText(getApplicationContext(),
 				// new String(headerBuf), Toast.LENGTH_SHORT)
 				// .show();
 
 				Log.d("pos", Integer.toString(pos));
 
 				// Log.d("string", new String(readBuf));
 
 				// Array.Resize(readBuf, 5);
 				if (pos <= 1024 && !flag) {
 
 					// get filename
 					System.arraycopy(readBuf, 0, buffer, 0, 512);
 					recievedFilepath = new String(buffer).trim();
 					recievedFilename = new File(recievedFilepath).getName();
 
 					// get totalsize
 					System.arraycopy(readBuf, 511, totalBytes, 0, 4);
 
 					// get files left count
 					System.arraycopy(readBuf, 600, filesLeft, 0, 4);
 
 					ByteBuffer wrapped = ByteBuffer.wrap(totalBytes);
 					IntBuffer ib = wrapped.asIntBuffer();
 					totalSize = ib.get(0);
 
 					wrapped = ByteBuffer.wrap(filesLeft);
 					ib = wrapped.asIntBuffer();
 					numFilesLeft = ib.get(0);
 
 					mNFCService.setSize(totalSize);
 
 					Log.d("totalBytes", Integer.toString(totalSize));
 
 					File path = Environment.getExternalStorageDirectory();
 					File file = new File(path + "/NFN", recievedFilename);
 
 					try {
 						fos = new FileOutputStream(file);
 					} catch (FileNotFoundException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 
 					// Hide original dialog and show progressbar
 					if (waitBar != null) {
 						if (waitBar.isShowing()) {
 							waitBar.dismiss();
 						}
 
 						// Log.d("debug","hey this is here");
 
 						if (progressBar == null || !progressBar.isShowing()) {
 							progressBar = new ProgressDialog(
 									DisplayPersonActivity.this);
 							progressBar
 									.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 							progressBar.setTitle("Downloading");
 
 							progressBar
 									.setOnCancelListener(new OnCancelListener() {
 										@Override
 										public void onCancel(
 												DialogInterface dialog) {
 											mNFCService.stop();
 											Toast.makeText(
 													getApplicationContext(),
 													"Connection Terminated",
 													Toast.LENGTH_LONG).show();
 											finish();
 
 										}
 									});
 						}
 						progressBar.setMessage("File: " + recievedFilename);
 						progressBar.setMax(totalSize);
 
 						if (!progressBar.isShowing()) {
 							progressBar.show();
 						}
 
 						// prevSize = pos;
 						flag = true;
 
 					}
 				}
 
 				else if (pos > 909) {
 
 					if (pos - totalSize > 0) {
 						buffer = new byte[pos - totalSize];
 						System.arraycopy(readBuf, 0, buffer, 0, pos - totalSize);
 					}
 
 					else
 					// Log.d("size", Integer.toString(msg.arg2));
 					{
 						buffer = new byte[msg.arg2];
 						System.arraycopy(readBuf, 0, buffer, 0, msg.arg2);
 					}
 
 					progressBar.setProgress(pos);
 					// prevSize = pos;
 
 					// Copy entire contents of file
 
 					try {
 						fos.write(buffer);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				}
 				// String readMessage = new String(readBuf, 0, msg.arg1);
 				// Toast.makeText(getApplicationContext(), readMessage,
 				// Toast.LENGTH_LONG).show();
 
 				if (pos >= totalSize) {
 
 					try {
 						fos.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 					Toast.makeText(getApplicationContext(),
 							recievedFilename + " succesfully downloaded",
 							Toast.LENGTH_SHORT).show();
 
 					// Reset for next iteration
 					// pos = 0;
 					flag = false;
 
 					if (numFilesLeft == 0) {
 						try {
 							synchronized (this) {
 								wait(1000);
 							}
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						if (progressBar.isShowing())
 							progressBar.dismiss();
 						mNFCService.stop();
 						finish();
 					}
 				}
 
 				break;
 			// case MESSAGE_DONE:
 			// Toast.makeText(getApplicationContext(),
 			// msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
 			// .show();
 			// mNFCService.stop();
 			// finish();
 			// break;
 			case MESSAGE_UPDATE:
 				int progress = msg.getData().getInt(PROGRESS);
 				int total = msg.getData().getInt(TOTAL);
 				int numLeft = msg.getData().getInt(NUM_LEFT);
 				String fname = msg.getData().getString(FNAME);
 
 				// if (sendNextFile)
 				// {
 				// progressSBar.setMessage("File: " + fname);
 				// }
 
 				progressSBar.setMessage("File: " + fname);
 				progressSBar.setMax(total);
 
 				if (!progressSBar.isShowing())
 					progressSBar.show();
 
 				if (progress < total) {
 					progressSBar.setProgress(progress);
 					// sendNextFile = false;
 				} else {
 					progressSBar.setProgress(progress);
 
 					Toast.makeText(getApplicationContext(),
 							fname + " sent succesfully", Toast.LENGTH_SHORT)
 							.show();
 
 					// sendNextFile = true;
 
 					if (numLeft == 0) {
 						try {
 							synchronized (this) {
 								wait(1000);
 							}
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						progressSBar.dismiss();
 						mNFCService.stop();
 						finish();
 					}
 				}
 				break;
 			}
 		}
 	};
 
 	// converts file to bytes
 	public byte[] readBytes(Uri uri, int filesLeft) throws IOException {
 		// this dynamically extends to take the bytes you read
 
 		// for ()
 
 		InputStream inputStream = getContentResolver().openInputStream(uri);
 		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
 
 		// String fname = (new File(uri.getPath())).getName();
 		Log.d("debug", uri.getPath());
 
 		int headerSize = 990;
 		byte[] headerBuffer = new byte[headerSize];
 
 		System.arraycopy(uri.getPath().getBytes(), 0, headerBuffer, 0, uri
 				.getPath().getBytes().length);
 		byteBuffer.write(headerBuffer);
 
 		// this is storage overwritten on each iteration with bytes
 		int bufferSize = 990;
 		byte[] buffer = new byte[bufferSize];
 
 		// we need to know how may bytes were read to write them to the
 		// byteBuffer
 
 		int len = 0;
 		totalSize = 990;
 		while ((len = inputStream.read(buffer)) != -1) {
 			totalSize += len;
 			byteBuffer.write(buffer, 0, len);
 			byteBuffer.flush();
 		}
 
 		totalSize += (990 - (totalSize % 990));
 
 		ByteBuffer b = ByteBuffer.allocate(4);
 		b.putInt(totalSize);
 
 		// numFilesLeft = filesLeft;
 		ByteBuffer f = ByteBuffer.allocate(4);
 		f.putInt(filesLeft);
 
 		// and then we can return your byte array.
 		byte[] temp = byteBuffer.toByteArray();
 
 		int extra = 990 - (temp.length % 990);
 
 		byte[] bb = new byte[temp.length + extra];
 
 		System.arraycopy(temp, 0, bb, 0, temp.length);
 
 		// Log.d("sender", totalBytes.toString());
 
 		// Add total number of bytes to header
 		System.arraycopy(b.array(), 0, bb, 511, b.array().length);
 
 		// Add files left to header
 		System.arraycopy(f.array(), 0, bb, 600, f.array().length);
 
 		return bb;
 	}
 
 	public void onDialogPositiveClick(DialogFragment dialog) {
 		// User touched the dialog's positive button
 
 		FileSelectDialog fsd = (FileSelectDialog) (dialog);
 		// fsd.mSelectedItems;
 
 		File file = new File(person_path + File.separator + ".profile_pic.jpg");
 		if (file.exists())
			fsd.uris.add(Uri.fromFile(file));
		
		 fsd.uris.add(Uri.fromFile(new File(person_path + File.separator +
		 ".person")));


 
 		filesToSend = (Uri[]) fsd.uris.toArray(new Uri[fsd.uris.size()]);
 		// new Uri[] { intent.getData() };
 
 		try {
 
 			progressSBar = new ProgressDialog(DisplayPersonActivity.this);
 			progressSBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressSBar.setTitle("Sending");
 
 			progressSBar.setOnCancelListener(new OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					mNFCService.stop();
 					Toast.makeText(getApplicationContext(),
 							"Connection Terminated", Toast.LENGTH_LONG).show();
 					finish();
 
 				}
 			});
 
 			for (int i = 0; i != filesToSend.length; i++) {
 
 				int num = filesToSend.length - i - 1;
 
 				mNFCService.writeToFile(readBytes(filesToSend[i], num),
 						new File(filesToSend[i].getPath()).getName(), num);
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		// User touched the dialog's negative button
 		mNFCService.stop();
 		Toast.makeText(getApplicationContext(), "Connection Terminated",
 				Toast.LENGTH_LONG).show();
 		finish();
 
 	}
 
 	// //////////////////
 }
