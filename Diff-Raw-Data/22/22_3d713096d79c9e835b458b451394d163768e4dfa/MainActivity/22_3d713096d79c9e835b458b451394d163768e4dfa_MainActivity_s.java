 /**
  * MainActivity.java (bttagwriter)
  *
  * Copyright 2011 Sami Viitanen <sami.viitanen@gmail.com>
  * All rights reserved.
  */
 
 package fi.siika.bttagwriter;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import fi.siika.bttagwriter.TagWriter.TagInformation;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothClass;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.MifareUltralight;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 /**
  * Main activity of BtTagWriter application
  * @author Sami Viitanen <sami.viitanen@gmail.com>
  */
 public class MainActivity extends Activity implements
 	BluetoothManager.DiscoveryListener {
 
 	private PendingIntent mNfcPendingIntent = null;
 	private TagWriter mTagWriter = null;
 	private Handler mTagWriterHandler = null; 
 	private TagWriter.TagInformation mTagInfo = new TagWriter.TagInformation();
 	private BluetoothManager mBtMgr = null;
 	private NfcManager mNfcMgr = null;
 	
 	public enum Pages {
 		START(0), ABOUT(1), BT_SELECT(2), EXTRA_OPTIONS(3), TAG(4), SUCCESS(5);
 		
 		private int mValue;
 		
 		private Pages(int value) {
 			mValue = value;
 		}
 		
 		public int toInt() {
 			return mValue;
 		}
 		
 		public boolean equal(int value) {
 			return toInt() == value;
 		}
 	};
 	
 	private void changeToPage (Pages page) {
 		if (showFlipChild (page.toInt())) {
 			if (page == Pages.BT_SELECT) {
 				startBluetoothDiscovery();
 			} else if (page == Pages.TAG) {
 				mNfcMgr.enableTechDiscovered();
 			}
 		}
 	}
 	
 	private int currentPage() {
 		ViewFlipper flip = (ViewFlipper)findViewById (R.id.mainFlipper);
 		return flip.getDisplayedChild();
 	}
 	
 	/*
 	 * Start Bluetooth discovery (if not active)
 	 */
 	private void startBluetoothDiscovery() {
 		
 		if (mBtMgr.startDiscovery(this) == false) {
 			showActionDialog(R.string.action_dialog_bluetooth_failed_str,
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						changeToPage(Pages.START);
 					}
 				
 				}, false, null);
 		}
 	}
 	
 	/**
 	 * Function that will handle things that should be done after user moves
 	 * away from different child views of flip. Called by showFlipChild.
 	 * @param index Index of child that was replaced with something else
 	 */
 	private void flipChildHidden (int index) {
 		if (Pages.BT_SELECT.equal(index)) {
 			mBtMgr.stopDiscovery();
 		} else if (Pages.TAG.equal(index)) {
 			mNfcMgr.disableForegroundDispatch();
 		}
 	}
 	
 	/***
 	 * Call this when changing the shown child of flip page. Will selected
 	 * the correct animation depending on current and selected child.
 	 * @param index Index of child shown next
 	 * @return true if shown child was changed
 	 */
 	private boolean showFlipChild (int index) {
 		ViewFlipper flip = (ViewFlipper)findViewById (R.id.mainFlipper);
 		int current = flip.getDisplayedChild();
 		
 		if (current == index) {
 			return false;
 		} else if (current < index) {
 			flip.setInAnimation (this, R.animator.in_left_anim);
 			flip.setOutAnimation (this, R.animator.out_right_anim);
 		} else {
 			flip.setInAnimation (this, R.animator.in_right_anim);
 			flip.setOutAnimation (this, R.animator.out_left_anim);
 		}
 		
 		flipChildHidden (current);
 		flip.setDisplayedChild (index);
 		return true;
 	}
 	
 	private OnClickListener mStartButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			
 			mBtListAdapter.clear();
 			changeToPage (Pages.BT_SELECT);
 		}
 	};
 	
 	private OnClickListener mRescanButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			
 			mBtMgr.startDiscovery(MainActivity.this);
 		}
 	};
 	
 	private OnClickListener mAboutButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			
 			changeToPage (Pages.ABOUT);
 		}
 	};
 	
 	private OnClickListener mExtraoptsReadyButtonListener =
 		new OnClickListener() {
 		
 		public void onClick(View v) {
 			
 			changeToPage (Pages.TAG);
 		}
 	};
 	
 	private OnClickListener mExitButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			
 			finish();
 		}
 	};
 	
 	//row adapter for bluetooth list
 	public class BluetoothRowAdapter extends ArrayAdapter<Object> {
 		
 		public class Row {
 			public String name;
 			public String address;
 		};
 		
 		private Vector<Row> mList;
 
 		public BluetoothRowAdapter(Context context) {
 			super(context, R.layout.bt_device_layout, R.id.btDeviceNameTextView);
 			
 			mList = new Vector<Row> ();
 		}
 		
 		@Override
 		public int getCount() {
 			return mList.size();
 		}
 		
 		@Override
 		public View getView (int position, View convertView, ViewGroup parent) {
 			View row = convertView;
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.bt_device_layout, null);
 			}
 			
 			Row rowData = mList.elementAt(position);
 			TextView line =(TextView)row.findViewById (R.id.btDeviceNameTextView);
 			line.setText (rowData.name);
 			line =(TextView)row.findViewById (R.id.btDeviceAddressTextView);
 			line.setText (rowData.address);
 			return(row);                         
 		}
 		
 		public void addDevice(String name, String address) {
 			Row row = new Row();
 			row.name = name;
 			row.address = address;
 			mList.add(row);
 			notifyDataSetChanged();
 		}
 		
 		public void addDevice(BluetoothDevice device) {
 			addDevice (device.getName(), device.getAddress());
 		}
 		
 		public Row getRow (int index) {
 			return mList.elementAt(index);
 		}
 		
 		public void clear() {
 			mList.clear();
 			notifyDataSetChanged();
 		}
 		
 		public void addDeviceIfNotPresent (BluetoothDevice device) {
 			Iterator<Row> iter = mList.iterator();
 			while (iter.hasNext()) {
 				Row row = iter.next();
 				if (row.address.equals(device.getAddress())) {
 					return;
 				}
 			}
 			addDevice(device);
 		}
 	}
 	private BluetoothRowAdapter mBtListAdapter = null;
    
 	protected void connectSignals() {
 		Button button = (Button)findViewById (R.id.startButton);
 		button.setOnClickListener (mStartButtonListener);
 		
 		button = (Button)findViewById (R.id.restartButton);
 		button.setOnClickListener (mStartButtonListener);
 		
 		button = (Button)findViewById (R.id.extraoptsReadyButton);
 		button.setOnClickListener (mExtraoptsReadyButtonListener);
 		
 		button = (Button)findViewById (R.id.exitButton);
 		button.setOnClickListener(mExitButtonListener);
 		
 		button = (Button)findViewById (R.id.aboutButton);
 		button.setOnClickListener(mAboutButtonListener);
 		
 		ImageButton ib = (ImageButton)findViewById (R.id.btRescanButton);
 		ib.setOnClickListener(mRescanButtonListener);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		mTagWriterHandler = new Handler() {
 			@Override
 			public void handleMessage (Message msg) {
 				switch (msg.what) {
 				case TagWriter.HANDLER_MSG_SUCCESS:
 					changeToPage(Pages.SUCCESS);
 					break;
 				default:
 					showActionDialog(R.string.tag_no_accepted_str, null,
 						false, null);
 				}
 			}
 		};
 		
 		mTagWriter = new TagWriter (mTagWriterHandler);
 	}
 	
 	@Override
 	public void onPause() {
 		mBtMgr.releaseAdapter();	
 		super.onPause();
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mBtMgr = new BluetoothManager(this);
         mNfcMgr = new NfcManager (this);
         
         if (mBtListAdapter == null) {
         	mBtListAdapter = new BluetoothRowAdapter(this);
         }
         ListView list = (ListView)findViewById (R.id.btDevicesList);
         list.setAdapter (mBtListAdapter);
         
         list.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view,
         		int position, long id) {
         		
         		mTagInfo.name = mBtListAdapter.getRow(position).name;
         		mTagInfo.address = mBtListAdapter.getRow(position).address;
         		
         		StringBuilder sbuilder = new StringBuilder();
         		sbuilder.append(mBtListAdapter.getRow(position).name);
         		sbuilder.append(" (");
         		sbuilder.append(mBtListAdapter.getRow(position).address);
         		sbuilder.append(")");
         		
         		TextView tview = (TextView)findViewById (
         			R.id.extraoptsSelectedDeviceValue);
         		tview.setText(sbuilder.toString());
         		
         		changeToPage (Pages.EXTRA_OPTIONS);
         	}
         });
         
         connectSignals();
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	
     	boolean ret = false;
     	
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
         	int curPage = currentPage();
         	
         	if (Pages.ABOUT.equal(curPage)) {
         		changeToPage(Pages.START);
         	} else if (Pages.BT_SELECT.equal(curPage)) {
         		changeToPage(Pages.START);
         	} else if (Pages.EXTRA_OPTIONS.equal(curPage)) {
         		changeToPage(Pages.BT_SELECT);
         	} else if (Pages.TAG.equal(curPage)) {
         		changeToPage(Pages.EXTRA_OPTIONS);
         	} else {
         		ret = super.onKeyDown(keyCode, event);
         	}
         } else {
         	ret = super.onKeyDown(keyCode, event);
         }
         return ret;
     }
     
     @Override
     public void onNewIntent(Intent intent) {
     	setIntent(intent);
     	String action = intent.getAction();
     	
     	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
     		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);    		
     		if (mTagWriter.writeToTag(tag, mTagInfo) == false) {
     			showActionDialog(R.string.tag_no_accepted_str, null, false,
     				null);
     		}
 
     	} else {
         	Toast toast = Toast.makeText(this, action, 
         		Toast.LENGTH_SHORT);
         	toast.show();
     	}
     }
     
     private void showActionDialog (int textResId, 
     	DialogInterface.OnClickListener clickListener,
     	boolean cancelable,
     	DialogInterface.OnCancelListener cancelListener) {
     	
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setMessage(textResId);
     	
     	AlertDialog dialog = builder.create();
     	
     	if (clickListener != null) {
     		dialog.setCancelable(cancelable);
     		dialog.setButton(getResources().getText(R.string.action_dialog_ok),
     			clickListener);
     		
     	} else {
     		dialog.setCancelable(true);
     	}
     	
     	if (cancelListener != null) {
     		dialog.setOnCancelListener(cancelListener);
     	}
     	
     	dialog.show();
     	
     }
 
 	/* (non-Javadoc)
 	 * @see fi.siika.bttagwriter.BluetoothManager.Listener#bluetoothDeviceFound(android.bluetooth.BluetoothDevice)
 	 */
 	public void bluetoothDeviceFound(BluetoothDevice device) {
 		
 		//For now filter out everything but audio
 		if (device.getBluetoothClass().hasService(
 			BluetoothClass.Service.AUDIO)) {
 			
 			mBtListAdapter.addDeviceIfNotPresent (device);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see fi.siika.bttagwriter.BluetoothManager.Listener#bluetoothDiscoveryStateChanged(boolean)
 	 */
 	public void bluetoothDiscoveryStateChanged(boolean active) {
 		ProgressBar pb = (ProgressBar)findViewById (R.id.btScanProgressBar);
 		ImageButton ib = (ImageButton)findViewById (R.id.btRescanButton);
 		pb.setIndeterminate (active);
 		
 		ViewFlipper flip = (ViewFlipper)findViewById (
 			R.id.btScanActionsFlipper);
 
 		if (active) {
 			flip.setDisplayedChild(0);
 			pb.setVisibility(View.VISIBLE);
 		} else {
 			flip.setDisplayedChild(1);
 			pb.setVisibility(View.INVISIBLE);
 		}
 		
 	}
 
 
 }
