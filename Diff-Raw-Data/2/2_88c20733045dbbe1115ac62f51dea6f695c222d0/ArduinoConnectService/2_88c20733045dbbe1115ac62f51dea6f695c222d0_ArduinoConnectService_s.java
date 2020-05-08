 package info.staticfree.android.taguid;
 
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Service;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 import edu.mit.mobile.android.greenwheel.BluetoothService;
 
 public class ArduinoConnectService extends Service {
 
 	private static final String TAG = ArduinoConnectService.class.getSimpleName();
 
 	public static final String PREF_BT_ADDR = "bt_addr";
 
 	private BluetoothService mBluetoothService;
 
 	private final IBinder mBinder = new LocalBinder();
 
 	private SharedPreferences mPrefs;
 
 	private BluetoothAdapter mBluetoothAdapter;
 
 	private static final int STATE_READY = 0, STATE_WAITING_FOR_RESPONSE = 1,
 			STATE_READING_RESPONSE = 2;
 	private int mState = STATE_READY;
 
 	private char mCmd = 0;
 
 	private static final char CMD_VER = 'v', CMD_LIST = 'l', CMD_SET_GROUP = 'a', CMD_DEL = 'd',
 			CMD_OPEN = 'r';
 
 	private final Queue<String> mSendQueue = new ConcurrentLinkedQueue<String>();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 
 		super.onCreate();
 
 		mBluetoothService = new BluetoothService(this, mBtHandler);
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	public boolean connect() {
 		if (mBluetoothService.getState() != BluetoothService.STATE_NONE) {
 			return false;
 		}
 		final String btAddr = mPrefs.getString(PREF_BT_ADDR, null);
 		if (btAddr == null) {
 			return false;
 		}
 
 		final BluetoothDevice btDev = mBluetoothAdapter.getRemoteDevice(btAddr);
 
 		mBluetoothService.connect(btDev);
 
 		return true;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		if (mBluetoothService != null) {
 			mBluetoothService.stop();
 		}
 	}
 
 	private final List<RfidRecord> mRfidRecords = new LinkedList<RfidRecord>();
 
	private static final Pattern REC_FORMAT = Pattern.compile("(\\d)\t([A-Fa-f0-9:]+)");
 
 
 	private RfidRecord parseRecord(String recLine) {
 		final Matcher m = REC_FORMAT.matcher(recLine);
 		if (!m.matches()) {
 			return null;
 		}
 		final RfidRecord r = new RfidRecord();
 		r.groups = Integer.valueOf(m.group(1));
 		final String hexString = m.group(2);
 
 		final String filteredHex = hexString.replaceAll("[^A-Fa-f0-9]", "");
 		r.id = new BigInteger(filteredHex, 16).toByteArray();
 
 		return r;
 	}
 
 	private final List<String> mCmdResults = new LinkedList<String>();
 
 	private void onRead(String msg) {
 		Log.d(TAG, "just read: " + msg);
 		if (mResultListener == null) {
 			return;
 		}
 
 		if ("".equals(msg)) {
 			onCommandFinished();
 			mCmd = 0;
 			mState = STATE_READY;
 			mCmdResults.clear();
 
 			final String cmd = mSendQueue.poll();
 			if (cmd != null) {
 				sendCommand(cmd);
 			}
 
 		} else {
 			mCmdResults.add(msg);
 			mState = STATE_READING_RESPONSE;
 		}
 	}
 
 	private void onCommandFinished() {
 		switch (mCmd){
 			case CMD_VER:
 				mResultListener.onReceiveVersion(mCmdResults.get(0));
 				break;
 
 			case CMD_LIST: {
 				mRfidRecords.clear();
 				for (final String msg : mCmdResults) {
 					final RfidRecord r = parseRecord(msg);
 					if (r != null) {
 
 						mRfidRecords.add(r);
 					} else {
 						mResultListener.onReceiveRecords(mRfidRecords);
 						break;
 					}
 				}
 			}
 				break;
 
 			case CMD_SET_GROUP:
 				mResultListener.onSetGroupResult(true);
 				break;
 
 			case CMD_DEL:
 				mResultListener.onDeleteResult(true);
 				break;
 
 			case CMD_OPEN:
 				mResultListener.onOpenResult();
 				break;
 		}
 	}
 
 	public void requestVersion() {
 		sendCommand("v");
 	}
 
 	public void requestOpen() {
 		sendCommand("r");
 	}
 
 	public void requestIdList() {
 		sendCommand("l");
 	}
 
 	public void requestDeleteId(RfidRecord record) {
 		sendCommand("d " + record.toIdString());
 	}
 
 	public void requestSetGroup(RfidRecord r){
 		sendCommand("a" + r.toIdString());
 	}
 
 	private void onCmdSent(char cmdId) {
 		switch (cmdId) {
 			case CMD_SET_GROUP:
 
 				break;
 		}
 	}
 
 	private void sendCommand(String command) {
 		final char cmdId = command.charAt(0);
 		if (mState == STATE_READY) {
 			mCmd = cmdId;
 			mBluetoothService.write((command + "\n").getBytes());
 			onCmdSent(cmdId);
 			mState = STATE_WAITING_FOR_RESPONSE;
 		} else {
 			mSendQueue.add(command);
 		}
 	}
 
 	public class LocalBinder extends Binder {
 		public ArduinoConnectService getService(){
 			return ArduinoConnectService.this;
 		}
 	}
 
 	private final Handler mBtHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case BluetoothService.MESSAGE_STATE_CHANGE: {
 					mResultListener.onStateChange(msg.arg1);
 					switch (msg.arg1) {
 						case BluetoothService.STATE_CONNECTED:
 							Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT)
 									.show();
 							break;
 
 						case BluetoothService.STATE_NONE:
 							break;
 					}
 
 				}
 					break;
 
 				case BluetoothService.MESSAGE_TOAST: {
 
 					Toast.makeText(getApplicationContext(),
 							msg.getData().getString(BluetoothService.TOAST), Toast.LENGTH_SHORT)
 							.show();
 				}
 					break;
 
 				case BluetoothService.MESSAGE_WRITE: {
 					final byte[] readBuf = (byte[]) msg.obj;
 					final String readMessage = new String(readBuf, 0, msg.arg1);
 					Log.d(TAG, "just wrote: " + readMessage);
 				}
 					break;
 
 				case BluetoothService.MESSAGE_READ: {
 					final byte[] readBuf = (byte[]) msg.obj;
 					final String readMessage = new String(readBuf, 0, msg.arg1);
 					onRead(readMessage);
 				}
 					break;
 
 			}
 		};
 	};
 
 	private OnDoorResultListener mResultListener;
 
 	public void setResultListener(OnDoorResultListener resultListener) {
 		mResultListener = resultListener;
 	};
 }
