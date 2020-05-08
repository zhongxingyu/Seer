 package com.idthk.wristband.api;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.idthk.wristband.database.DatabaseHandler;
 import com.idthk.wristband.database.Record;
 import com.idthk.wristband.database.SleepPattern;
 import com.idthk.wristband.database.SleepRecord;
 
 public class BLEBaseFragmentActivity extends FragmentActivity {
 	static final String TAG = "BLEBaseFragmentActivity";
 	protected static final int REQUEST_SELECT_DEVICE = 1;
 	protected static final int REQUEST_ENABLE_BT = 2;
 	protected static final int STATE_READY = 10;
 	protected static final int BLE_PROFILE_CONNECTED = 20;
 	protected static final int BLE_PROFILE_DISCONNECTED = 21;
 	protected static final int BLE_SERVICE_DISCOVERED = 22;
 	protected static final int BLE_HISTORY_MODE = 24;
 
 	protected static final int STATE_OFF = 10;
 	private int mState = BLE_PROFILE_DISCONNECTED;
 
 	protected WristbandBLEService mService = null;
 	protected BluetoothAdapter mBtAdapter = null;
 	protected ServiceConnection onService = null;
 
 	public static final String TABLE_CONTENT = "activity_table";
 
 	class BLEErrorType {
 		static final public int READ_HISTORY_ERROR = 0x401;
 		static final public int CONNTECT_ERROR = 0x402;
 		static final public int DISCONNECT_ERROR = 0x403;
 		static final public int SEND_ERROR = 0x404;
 		static final public int READ_ERROR = 0x405;
 	}
 
 	private CountDownTimer mCountDownTimer;
 	public boolean bTrace = true;
 	byte[] rawDataBuffer;
 	String savedAddress = "";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mCountDownTimer = new CountDownTimer(1000 * 2, 1000) {
 
 			public void onFinish() {
 
 				// onError(BLEErrorType.READ_HISTORY_ERROR);
 
 				parseHistoryData(rawDataBuffer);
 
 				onHistoryReadFinish();
 				mState = BLE_PROFILE_CONNECTED;
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// TODO Auto-generated method stub
 				Log.v(TAG, "connect count down trick");
 
 			}
 		};
 		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		if (mBtAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not available",
 					Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 		Intent bindIntent = new Intent(this, WristbandBLEService.class);
 		startService(bindIntent);
 		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
 		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
 		this.registerReceiver(bleStatusChangeReceiver, filter);
 
 	}
 
 	protected void onHistoryReadFinish() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void parseHistoryData(byte[] _rawDataBuffer) {
 
 		int p = 0;
 
 		List<byte[]> pedometerValues = new ArrayList<byte[]>();
 		List<byte[]> sleepValues = new ArrayList<byte[]>();
 		List<Byte> inData = null;
 		String dataType = "";
 		boolean isPedometerStart = false, isPedometerEnd = false;
 		boolean isSleepDataStart = false, isSleepDataEnd = false;
 		if (_rawDataBuffer == null)
 			return;
 
 		if (_rawDataBuffer != null) {
 			boolean bPrint = false;
 			if (bPrint) {
 				printByteArray(_rawDataBuffer);
 			}
 		}
 		while (p < _rawDataBuffer.length) {
 			if (inData == null) {
 				if (matchData(_rawDataBuffer, p,
 						WristbandBLEService.ACTIVITY_HISTORY_RETURN_HEADER)) {
 					// header of pedometer
 					//Log.v(TAG, "ACTIVITY_HISTORY_RETURN_HEADER " + p);
 					dataType = "PEDOMETER";
 					inData = new ArrayList<Byte>();
 					isPedometerStart = true;
 					p += 6;
 					continue;
 				} else if (matchData(_rawDataBuffer, p,
 						WristbandBLEService.SLEEP_HISTORY_RETURN_HEADER)) {
 					//Log.v(TAG, "SLEEP_HISTORY_RETURN_HEADER " + p);
 					// header of sleep
 					inData = new ArrayList<Byte>();
 					dataType = "SLEEP";
 					isSleepDataStart = true;
 					p += 6;
 					continue;
 				}
 				p += 1;
 
 			} else {
 				if (matchData(_rawDataBuffer, p,
 						WristbandBLEService.HISTORY_RETURN_FOOTER)) {
 					// end of data footer
 					//Log.v(TAG, "Match Fotter " + p);
 
 					if (dataType.equals("PEDOMETER")) {
 						byte newData[] = new byte[inData.size()];
 						for (int i = 0; i < inData.size(); i++) {
 							newData[i] = inData.get(i);
 						}
 						pedometerValues.add(newData);
 						isPedometerEnd = true;
 					} else if (dataType.equals("SLEEP")) {
 
 						byte newData[] = new byte[inData.size()];
 						for (int i = 0; i < inData.size(); i++) {
 							newData[i] = inData.get(i);
 						}
 						sleepValues.add(newData);
 						isSleepDataEnd = true;
 					}
 					dataType = "";
 					inData = null;
 					p += 6;
 				} else {
 					// data
 					inData.add(_rawDataBuffer[p]);
 
 					p += 1;
 
 				}
 			}
 
 		}
 		if (isSleepDataStart && isSleepDataEnd && isPedometerStart
 				&& isPedometerEnd) {
 
 			List<Record> pedometerData = new ArrayList<Record>();
 			for (byte[] data : pedometerValues) {
 				//Log.v(TAG, "found pedometerValues ");
 				if (data.length == 9) {
 
 					p = 0;
 					// Current hour pedometer data
 					Record record = new Record();
 					Calendar calendar = Calendar.getInstance();
 
					calendar.set(Calendar.HOUR, (int) (data[p]));
 					calendar.set(Calendar.MINUTE, 0);
 					calendar.set(Calendar.SECOND, 0);
 					calendar.set(Calendar.MILLISECOND, 0);
 					//calendar.setTimeZone(TimeZone.getDefault());
 					record.setDate(calendar);
 
 					record.setSteps((data[p + 1] * 100) + data[p + 2]);
 					record.setDistance((float) (data[p + 3] + (data[p + 4] * 0.01)));
 					record.setCalories((data[p + 5] * 100) + data[p + 6]);
 					record.setActivityTime((data[p + 7] * 60) + data[p + 8]);
 
 					pedometerData.add(record);
 					onReadCurrentRecord(record);
 
 				} else {
 					p = 0;
 					Calendar calendar = Calendar.getInstance();
 					calendar.set(2000 + data[p], data[p + 1]-1, data[p + 2]);
 					calendar.set(Calendar.MINUTE, 0);
 					calendar.set(Calendar.SECOND, 0);
 					calendar.set(Calendar.MILLISECOND, 0);
 					p = 3;
 					while ((p + 8) <= (data.length)) {

						calendar.set(Calendar.HOUR, (int) (data[p]));
 
 						int steps = (data[p + 1] * 100) + data[p + 2];
 						float distance = (float) (data[p + 3] + (data[p + 4] * 0.01));
 						int calories = (data[p + 5] * 100) + data[p + 6];
 						int activityTime = (data[p + 7] * 60) + data[p + 8];
 
 						Record record = new Record(calendar.getTimeInMillis(),
 								activityTime);
 
						record.setSteps(record.getSteps() + steps);
						record.setDistance(record.getDistance() + distance);
						record.setCalories(record.getCalories() + calories);
						record.setActivityTime(record.getActivityTime()
								+ activityTime);
 
 						p += 9;
 						pedometerData.add(record);
 					}
 
 				}
 			}
 			
 			onReadActivityHistoryData(pedometerData);
 
 			// Decode sleep data
 			for (byte[] data : sleepValues) {
 
 				SleepRecord sleepRecord = new SleepRecord();
 
 				p = 0;
 
 				sleepRecord.setGoToBedTime(Calendar.getInstance());
 				sleepRecord.getGoToBedTime().set(2000 + data[p], data[p + 1]-1,
 						data[p + 2]);
 				sleepRecord.getGoToBedTime().set(Calendar.HOUR_OF_DAY,
 						data[p + 3]);
 				sleepRecord.getGoToBedTime().set(Calendar.MINUTE, data[p + 4]);
 				sleepRecord.getGoToBedTime().set(Calendar.SECOND, 0);
 				sleepRecord.getGoToBedTime().set(Calendar.MILLISECOND, 0);
 				p += 5;
 				sleepRecord
 						.setFallingAsleepDuration(((data[p] * 60) + data[p + 1]));
 
 				p += 2;
 				sleepRecord.setNumberOfTimesWaken((data[p]));
 
 				p += 1;
 				sleepRecord.setInBedTime(((data[p] * 60) + data[p + 1]));
 
 				p += 2;
 				sleepRecord.setActualSleepTime(((data[p] * 60) + data[p + 1]));
 
 				p += 2;
 				sleepRecord.setActualWakeupTime(Calendar.getInstance());
 				sleepRecord.getActualWakeupTime().set(2013, 1, 1);
 				sleepRecord.getActualWakeupTime().set(Calendar.HOUR_OF_DAY,
 						data[p]);
 				sleepRecord.getActualWakeupTime().set(Calendar.MINUTE,
 						data[p + 1]);
 				sleepRecord.getActualWakeupTime().set(Calendar.SECOND, 0);
 				sleepRecord.getGoToBedTime().set(Calendar.MILLISECOND, 0);
 
 
 				p += 2;
 				sleepRecord.setPresetWakeupTime(Calendar.getInstance());
 				sleepRecord.getPresetWakeupTime().set(2013, 1, 1);
 				sleepRecord.getPresetWakeupTime().set(Calendar.HOUR_OF_DAY,
 						data[p]);
 				sleepRecord.getPresetWakeupTime().set(Calendar.MINUTE,
 						data[p + 1]);
 				sleepRecord.getPresetWakeupTime().set(Calendar.SECOND, 0);
 				sleepRecord.getGoToBedTime().set(Calendar.MILLISECOND, 0);
 
 
 				p += 2;
 				sleepRecord.setSleepEfficiency((data[p]));
 
 				p += 1;
 
 				while ((p + 5) <= data.length) {
 					SleepPattern slppePattern = new SleepPattern();
 
 					slppePattern.setTime(((data[p + 3] * 60) + data[p + 4]));
 					slppePattern
 							.setDuration(((data[p + 1] * 60) + data[p + 2]));
 					slppePattern.setAmplitude((data[p]));
 					sleepRecord.getPatterns().add(slppePattern);
 					p += 5;
 				}
 				onReadSleepHistoryData(sleepRecord);
 			}
 			_rawDataBuffer = null;
 			onReadHistoryDataFinished();
 		} else if (!isPedometerStart || !isPedometerEnd || !isSleepDataStart
 				|| !isSleepDataEnd) {
 			onReadDataFailed(isPedometerStart, isPedometerEnd,
 					isSleepDataStart, isSleepDataEnd);
 		}
 
 	}
 
 	protected void onReadDataFailed(boolean isPedometerStart,
 			boolean isPedometerEnd, boolean isSleepDataStart,
 			boolean isSleepDataEnd) {
 		// TODO Auto-generated method stub
 
 	}
 
 	protected void onReadHistoryDataFinished() {
 		// TODO Auto-generated method stub
 		Log.v(TAG, "onReadHistoryDataFinished");
 	}
 
 	protected void onReadCurrentRecord(Record record) {
 		// TODO Auto-generated method stub
 		Log.v(TAG,"Current Record = "+record.toString());
 	}
 
 	protected static void printByteArray(byte value[], int start, int end) {
 		String _msg = "";
 		if (start >= 0 && end <= value.length) {
 			for (int i = start; i < end; i++) {
 				_msg += Integer.toHexString(value[i]) + " , ";
 			}
 			Log.v(TAG, "printByteArray : " + _msg);
 		}
 	}
 
 	protected static void printByteArray(byte value[]) {
 		String _msg = "";
 
 		for (int i = 0; i < value.length; i++) {
 
 			_msg += Integer.toHexString(value[i]) + ",";
 			if (i % 10 == 0) {
 				Log.v(TAG, "printByteArray : " + _msg);
 				_msg = "";
 			}
 		}
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
 		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
 		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
 		this.registerReceiver(mReceiver, filter);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.v(TAG, "onDestroy");
 		disconnect();
 		if (mService != null) {
 			mService.scan(false);
 			mService.disconnect();
 		}
 
 		try {
 			unregisterReceiver(bleStatusChangeReceiver);
 		} catch (Exception ignore) {
 			Log.e(TAG, ignore.toString());
 		}
 		unbindService(mServiceConnection);
 		unregisterReceiver(mReceiver);
 		stopService(new Intent(this, WristbandBLEService.class));
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStop() {
 
 		super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 
 		super.onPause();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 
 	}
 
 	@Override
 	public void onResume() {
 
 		super.onResume();
 
 		if (!mBtAdapter.isEnabled()) {
 
 			Intent enableIntent = new Intent(
 					BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		} else {
 			if (mState == BLE_PROFILE_DISCONNECTED) {
 				connect();
 			}
 		}
 
 	}
 
 	private ServiceConnection mServiceConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className,
 				IBinder rawBinder) {
 			mService = ((WristbandBLEService.LocalBinder) rawBinder)
 					.getService();
 
 			// set call back method
 			mService.setActivityHandler(mHandler);
 			// set call back method
 			mService.setDeviceListHandler(mHandler);
 			prepareToConnect();
 		}
 
 		public void onServiceDisconnected(ComponentName classname) {
 			mService.disconnect();
 			mService = null;
 		}
 	};
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			final Bundle data = msg.getData();
 			final int rssi = data.getInt(WristbandBLEService.EXTRA_RSSI);
 			switch (msg.what) {
 
 			// Gatt device found message.
 			case WristbandBLEService.BLE_CONNECT_MSG:
 
 				mState = BLE_PROFILE_CONNECTED;
 				
 				savedAddress = data.getString(BluetoothDevice.EXTRA_DEVICE);
 				Log.v(TAG,"ConnectTo Device address "+savedAddress);
 				onConnected();
 				break;
 			case WristbandBLEService.GATT_DEVICE_FOUND_MSG:
 
 //				final BluetoothDevice device = data
 //						.getParcelable(BluetoothDevice.EXTRA_DEVICE);
 				
 				onDeviceFound();
 				break;
 
 			case WristbandBLEService.BLE_DISCONNECT_MSG:
 
 				mService.disconnect();
 				mState = BLE_PROFILE_DISCONNECTED;
 				onDisconnected();
 				break;
 
 			case WristbandBLEService.BLE_READY_MSG:
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 
 						onReady();
 					}
 				});
 				break;
 			case WristbandBLEService.BLE_CHARACTERISTIC_WROTE:
 
 				break;
 			case WristbandBLEService.BLE_STREAM_MSG:
 				// mState = BLE_STREAM_MODE_ON;
 				final byte[] value_ = data
 						.getByteArray(WristbandBLEService.EXTRA_VALUE);
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						try {
 							if (value_.length == 19) {
 								int steps = (value_[1] * 10000)
 										+ (value_[2] * 100) + value_[3];
 								int calories = (value_[7] * 10000)
 										+ (value_[8] * 100) + value_[9];
 								float distance = (float) ((value_[10] * 100)
 										+ value_[11] + (value_[12] / 100.0));
 
 								int activityTime = (value_[13] * 60)
 										+ value_[14];
 								// int activityLevel = value_[15];
 
 								int batteryLevel = value_[16];
 
 								onStreamMessage(steps, calories, distance,
 										activityTime, batteryLevel);
 							}
 						} catch (Exception e) {
 							Log.e(TAG, e.toString());
 						}
 					}
 
 				});
 				break;
 			case WristbandBLEService.BLE_VALUE_MSG:
 
 				final byte[] value = data
 						.getByteArray(WristbandBLEService.EXTRA_VALUE);
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						try {
 
 							int ret = mService.checkPrefix(value);
 							final int dataStart = 9;
 							mService.printPrefix(ret);
 							if (ret == WristbandBLEService.DEVICE_RETURN_TIME) {
 
 								onReadTime((2000 + value[dataStart]),
 										value[dataStart + 1],
 										value[dataStart + 2],
 										value[dataStart + 3],
 										value[dataStart + 4],
 										value[dataStart + 5],
 										value[dataStart + 6]);
 							} else if (ret == WristbandBLEService.DEVICE_RETURN_PROFILE) {
 								onReadProfile(value[dataStart],
 										(value[dataStart + 1] + 1914),
 										(value[dataStart + 2]),
 										(value[dataStart + 3] + 20),
 										(value[dataStart + 4] + 69));
 							} else if (ret == WristbandBLEService.DEVICE_RETURN_TARGET) {
 								int steps = (value[dataStart + 2] * 10000)
 										+ (value[dataStart + 3] * 100)
 										+ value[dataStart + 4];
 								int calories = (value[dataStart + 6] * 10000)
 										+ (value[dataStart + 7] * 100)
 										+ value[dataStart + 8];
 
 								onReadTarget(value[dataStart],
 										value[dataStart + 1], steps,
 										value[dataStart + 5], calories);
 							} else if (ret == WristbandBLEService.DEVICE_RETURN_SLEEP) {
 								onReadSleep(value[dataStart],
 										value[dataStart + 1],
 										value[dataStart + 2],
 										value[dataStart + 3],
 										value[dataStart + 4]);
 
 							} else if (ret == WristbandBLEService.DEVICE_RETURN_VERSION) {
 								int xx = Integer.valueOf(value[17] - 30);
 								int yy = Integer.valueOf(value[18] - 30);
 
 								onReadVersion(xx, yy);
 
 							} else if (ret == WristbandBLEService.DEVICE_RETURN_SERIAL) {
 								int xx = Integer.valueOf(value[17] - 30);
 								int yy = Integer.valueOf(value[18] - 30);
 
 								byte serial[] = new byte[10];
 								serial[0] = value[8];
 								serial[1] = value[9];
 								serial[2] = value[10];
 								serial[3] = value[11];
 								serial[4] = value[12];
 								serial[5] = value[13];
 								serial[6] = value[14];
 								serial[7] = value[15];
 								serial[8] = value[16];
 								serial[9] = value[17];
 
 								onReadSerial(serial);
 
 							} else if (mState == BLE_HISTORY_MODE) {
 								mCountDownTimer.cancel();
 								if (rawDataBuffer == null) {
 
 									rawDataBuffer = new byte[value.length];
 									System.arraycopy(value, 0, rawDataBuffer,
 											0, value.length);
 
 								} else {
 									rawDataBuffer = concat(rawDataBuffer, value);
 								}
 								mCountDownTimer.start();
 							} else {
 
 								onReadUnknownProtocol(value);
 
 							}
 						} catch (Exception e) {
 							Log.v(TAG, e.getMessage());
 						}
 					}
 
 				});
 				break;
 			case WristbandBLEService.BLE_ERROR_MSG:
 				int errorType = data.getInt(WristbandBLEService.EXTRA_VALUE);
 				onError(errorType);
 				break;
 			case WristbandBLEService.GATT_CHARACTERISTIC_RSSI_MSG:
 
 				final BluetoothDevice RemoteRssidevice = data
 						.getParcelable(WristbandBLEService.EXTRA_DEVICE);
 
 				break;
 			case WristbandBLEService.BLE_WRITE_REQUEST_MSG: {
 				final byte[] value1 = data
 						.getByteArray(WristbandBLEService.EXTRA_VALUE);
 
 				String __msg = "";
 				for (int i = 0; i < value1.length; i++) {
 					__msg += "BLE_WRITE_REQUEST_MSG " + i + " : " + value1[i]
 							+ "\n";
 				}
 
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 
 					}
 				});
 			}
 				break;
 			case WristbandBLEService.BLE_SERVICE_DISCOVER_MSG: {
 
 				Bundle _data = msg.getData();
 				final BluetoothDevice _device = _data
 						.getParcelable(BluetoothDevice.EXTRA_DEVICE);
 
 				mService.scan(false);
 
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 
 						onServiceDiscovered();
 					}
 				});
 
 			}
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	};
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
 
 				setProgressBarIndeterminateVisibility(false);
 
 			}
 			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
 
 				if (!mBtAdapter.isEnabled()) {
 
 				}
 
 			}
 		}
 	};
 
 	static boolean matchData(byte[] srcData, byte[] dataToFind) {
 		int iDataLen = srcData.length;
 		int iDataToFindLen = dataToFind.length;
 		boolean bGotData = false;
 		int iMatchDataCntr = 0;
 		for (int i = 0; i < iDataLen; i++) {
 			if (srcData[i] == dataToFind[iMatchDataCntr]) {
 				iMatchDataCntr++;
 				bGotData = true;
 			} else {
 				if (srcData[i] == dataToFind[0]) {
 					iMatchDataCntr = 1;
 				} else {
 					iMatchDataCntr = 0;
 					bGotData = false;
 				}
 
 			}
 
 			if (iMatchDataCntr == iDataToFindLen) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	static boolean matchData(byte[] srcData, int startIndex, byte[] dataToFind) {
 		int iDataLen = srcData.length;
 		int iDataToFindLen = dataToFind.length;
 		boolean bGotData = false;
 		int iMatchDataCntr = 0;
 		if (startIndex + iDataToFindLen > iDataLen)
 			return false;
 		for (int i = startIndex; i < startIndex + iDataToFindLen; i++) {
 
 			if (srcData[i] == dataToFind[iMatchDataCntr]) {
 
 				bGotData = true;
 			} else {
 				bGotData = false;
 				break;
 			}
 			iMatchDataCntr++;
 			// else {
 			// if (srcData[i] == dataToFind[0]) {
 			// iMatchDataCntr = 1;
 			// } else {
 			// iMatchDataCntr = 0;
 			// bGotData = false;
 			// }
 			//
 			// }
 
 			// if (iMatchDataCntr == iDataToFindLen) {
 			// return true;
 			// }
 		}
 
 		return bGotData;
 	}
 
 	private final BroadcastReceiver bleStatusChangeReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			final Intent mIntent = intent;
 			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
 				BluetoothDevice device = mIntent
 						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 				// showMessage( "BluetoothDevice.ACTION_BOND_STATE_CHANGED");
 			}
 			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
 				final int state = mIntent.getIntExtra(
 						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
 				// showMessage( "BluetoothAdapter.ACTION_STATE_CHANGED" +
 				// "state is"+ state);
 				runOnUiThread(new Runnable() {
 					public void run() {
 						if (state == STATE_OFF) {
 
 						}
 					}
 				});
 			}
 		}
 	};
 
 	@Override
 	public void onBackPressed() {
 		if (mState == STATE_READY) {
 			Intent startMain = new Intent(Intent.ACTION_MAIN);
 			startMain.addCategory(Intent.CATEGORY_HOME);
 			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(startMain);
 			disconnect();
 			finish();
 		} else {
 			new AlertDialog.Builder(this)
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle(R.string.popup_title)
 					.setMessage(R.string.popup_message)
 					.setPositiveButton(R.string.popup_yes,
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									disconnect();
 									finish();
 								}
 							}).setNegativeButton(R.string.popup_no, null)
 					.show();
 		}
 	}
 
 	public void prepareToConnect() {
 		// TODO Auto-generated method stub
 		if (mState == BLE_PROFILE_DISCONNECTED || mState == STATE_READY) {
 
 			connect();
 		}
 
 	}
 
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu) {
 	// // Inflate the menu; this adds items to the action bar if it is present.
 	// getMenuInflater().inflate(R.menu.main, menu);
 	// return true;
 	// }
 
 	public static String bytesToHex(byte[] bytes) {
 		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
 				'9', 'A', 'B', 'C', 'D', 'E', 'F' };
 		char[] hexChars = new char[bytes.length * 3];
 		int v;
 		for (int j = 0; j < bytes.length; j++) {
 			v = bytes[j] & 0xFF;
 			hexChars[j * 3] = hexArray[v >>> 4];
 			hexChars[j * 3 + 1] = hexArray[v & 0x0F];
 			hexChars[j * 3 + 2] = '-';
 		}
 		return new String(hexChars);
 	}
 
 	// public set function
 	// wristband specify function
 	public void enableNotification() {
 		mService.enableWristbandNotification();
 	}
 
 	public void startStream() {
 		mService.EnableDeviceNoti(WristbandBLEService.PE128_NOTI_SERVICE,
 				WristbandBLEService.PE128_CHAR_STREAMING);
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER,
 				WristbandBLEService.STREAM_MODE_START);
 
 	}
 
 	public void stopStream() {
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER,
 				WristbandBLEService.STREAM_MODE_STOP);
 		// mState = BLE_STREAM_MODE_OFF;
 	}
 
 	public void setTime(int year, int month, int day, int hour, int minute,
 			int second, int week) {
 		byte data[] = WristbandBLEService.SET_TIME_PREFIX;
 		int dataStart = 9;
 		data[dataStart] = (byte) year;
 		data[dataStart + 1] = (byte) month;
 		data[dataStart + 2] = (byte) day;
 		data[dataStart + 3] = (byte) hour;
 		data[dataStart + 4] = (byte) minute;
 		data[dataStart + 5] = (byte) second;
 		data[dataStart + 6] = (byte) week;
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER, data);
 	}
 
 	public void setProfile(int gender, int birthyear, int birthmonth,
 			int weight, int height) {
 		byte data[] = WristbandBLEService.SET_PROFILE_PREFIX;
 		int dataStart = 9;
 		data[dataStart] = (byte) gender;
 		data[dataStart + 1] = (byte) (birthyear - 1914);
 		data[dataStart + 2] = (byte) birthmonth;
 		data[dataStart + 3] = (byte) (weight - 20);
 		data[dataStart + 4] = (byte) (height - 69);
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER, data);
 	}
 
 	public void setSleep(int weekday_weak_hour, int weekday_weak_minute,
 			int weekend_weak_hour, int weekend_weak_minute, int toggle) {
 		byte data[] = WristbandBLEService.SET_SLEEP_PREFIX;
 		int dataStart = 9;
 		data[dataStart] = (byte) weekday_weak_hour;
 		data[dataStart + 1] = (byte) weekday_weak_minute;
 		data[dataStart + 2] = (byte) weekend_weak_hour;
 		data[dataStart + 3] = (byte) weekend_weak_minute;
 		data[dataStart + 4] = (byte) toggle;
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER, data);
 	}
 
 	public void setTarget(int duration, int toggle, int step, int distance,
 			int calories) {
 
 		String s = "";
 		s += "Wristband Set Target :\n";
 
 		s += "Activity duration " + duration + "minutes\n";
 		s += "Toggle Gauge" + toggle + "\n";
 		s += "Step " + step + "\n";
 		s += "Distance " + distance + "km \n";
 		s += "Calories " + calories + "kcal \n";
 		Log.v(TAG, s);
 
 		byte data[] = WristbandBLEService.SET_TARGET_PREFIX;
 		int dataStart = 9;
 		data[dataStart] = (byte) duration;
 		data[dataStart + 1] = (byte) toggle;
 
 		int steps_high = step / 10000;
 		int steps_medium = (step - (steps_high) * 10000) / 100;
 		int steps_low = step % 100;
 
 		int calories_high = calories / 10000;
 		int calories_medium = (calories - (calories_high) * 10000) / 100;
 		int calories_low = calories % 100;
 
 		data[dataStart + 2] = (byte) steps_high;
 		data[dataStart + 3] = (byte) steps_medium;
 		data[dataStart + 4] = (byte) steps_low;
 		data[dataStart + 5] = (byte) distance;
 		data[dataStart + 6] = (byte) calories_high;
 		data[dataStart + 7] = (byte) calories_medium;
 		data[dataStart + 8] = (byte) calories_low;
 
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER, data);
 	}
 
 	public void getVersion() {
 
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER,
 				WristbandBLEService.TEST_VERSION_PREFIX);
 
 	}
 
 	public void getSerial() {
 
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER,
 				WristbandBLEService.TEST_SERIAL_PREFIX);
 
 	}
 
 	public void getHistory() {
 		rawDataBuffer = null;
 		mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 				WristbandBLEService.PE128_CHAR_XFER,
 				WristbandBLEService.HISTORY_PREFIX);
 		mState = BLE_HISTORY_MODE;
 		mCountDownTimer.start();
 	}
 
 	// public callback function
 	public void connect() {
 
 		if (mService != null) {
 			
 			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
 			if(pairedDevices.size()>0)
 			{
 				boolean result = false;
 		        for (BluetoothDevice pairedDevice : pairedDevices) {
 		        	result = mService.isBLEDevice(pairedDevice);
 		        	if(result && pairedDevice.getName().startsWith("A"))
 		        	{
 		        		mService.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
 		        				savedAddress);
 		        		
 		        		mService.connect( false);
 		        	}
 		        }
 			}
 			else
 			{
 				mService.scan(true);
 			}
 		}
 	}
 
 	public int getState() {
 		return mState;
 	}
 
 	public void disconnect() {
 		Log.v(TAG, "disconnect()");
 		if (mState == BLE_PROFILE_CONNECTED) {
 			mService.WriteDevice(WristbandBLEService.PE128_SERVICE,
 					WristbandBLEService.PE128_CHAR_XFER,
 					WristbandBLEService.SET_DISCONNECT_PREFIX);
 		}
 		if (mService != null) {
 			mService.scan(false);
 			mService.disconnect();
 		}
 
 	}
 
 	public void discover(boolean scan) {
 
 	}
 
 	// public void onDiscover()
 	// {
 	//
 	// }
 	public void onError(int errorType) {
 		switch (errorType) {
 		case WristbandBLEService.BLE_CONNECTTION_ERROR:
 			Log.v(TAG, "WristbandBLEService.BLE_CONNECTTION_ERROR");
 			break;
 		case BLEErrorType.READ_HISTORY_ERROR:
 			Log.v(TAG, "BLEErrorType.READ_HISTORY_ERROR");
 			break;
 		}
 	}
 
 	public void onConnected() {
 		mState = BLE_PROFILE_CONNECTED;
 
 	}
 
 	public void onDisconnected() {
 		mState = BLE_PROFILE_DISCONNECTED;
 		onError(0);
 	}
 
 	public void onReady() {
 		mState = STATE_READY;
 
 	}
 
 	public void onServiceDiscovered() {
 		mState = BLE_SERVICE_DISCOVERED;
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		enableNotification();
 	}
 
 	public void onDeviceFound() {
 
 	}
 
 	public void onStreamMessage(int steps, int calories, float distance,
 			int activityTime, int batteryLevel) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onReadVersion(int xx, int yy) {
 		String s = xx + "." + yy;
 
 	}
 
 	public void onReadSerial(byte serial[]) {
 		Charset charset = Charset.forName("UTF-8");
 		CharSequence seq2 = new String(serial, charset);
 		Log.v(TAG, "Serial : " + seq2);
 
 	}
 
 	public void onReadTime(int year, int month, int day, int hour, int minute,
 			int second, int weekday) {
 		String s = "";
 		s += "Wristband Set Time :\n";
 		s += "YEAR : " + year + "\n";
 		s += "MONTH : " + month + "\n";
 		s += "DAY : " + day + "\n";
 		s += "HOUR : " + hour + "\n";
 		s += "MINUTE : " + minute + "\n";
 		s += "SECOND : " + second + "\n";
 		s += "WEEKDAY : " + weekday + "\n";
 		Log.v(TAG, s);
 	}
 
 	public void onReadProfile(int gender, int year, byte month, int weight,
 			int height) {
 		// TODO Auto-generated method stub
 		String s = "";
 		s += "Wristband Read Profile :\n";
 		s += "GENDER : " + ((gender == 0) ? "Male" : "Female") + "\n";
 		s += "YEAR : " + (year) + "\n";
 		s += "MONTH : " + (month) + "\n";
 		s += "WEIGHT : " + (weight) + "\n";
 		s += "HEIGHT : " + (height);
 		Log.v(TAG, s);
 	}
 
 	public void onReadSleep(int weekday_hour, int weekday_minute,
 			int weekend_hour, int weekend_minute, int toggle) {
 		// TODO Auto-generated method stub
 		String s = "";
 		s += "Wristband Set Sleep :\n";
 
 		s += "WEEKDAY HOUR : " + weekday_hour + "\n";
 		s += "WEEKDAY MINUTE : " + weekday_minute + "\n";
 		s += "WEEKEND HOUR : " + weekend_hour + "\n";
 		s += "WEEKEND MINUTE : " + weekend_minute + "\n";
 		switch (toggle) {
 		case 0:
 			s += "TOGGLE : ALL OFF\n";
 			break;
 		case 1:
 			s += "TOGGLE : WEEKDAY ON\n";
 			break;
 		case 2:
 			s += "TOGGLE : WEEKEND ON\n";
 			break;
 		case 3:
 			s += "TOGGLE : ALL ON\n";
 			break;
 		}
 		Log.v(TAG, s);
 	}
 
 	public void onReadTarget(int duration, int toggle, long step, int distance,
 			int calories) {
 		// TODO Auto-generated method stub
 		String s = "";
 		s += "Wristband Read Target :\n";
 
 		s += "Activity duration " + duration + "minutes\n";
 		s += "Toggle Gauge" + toggle + "\n";
 		s += "Step " + step + "\n";
 		s += "Distance " + distance + "km \n";
 		s += "Calories " + calories + "kcal \n";
 		Log.v(TAG, s);
 	}
 
 	public void onReadUnknownProtocol(byte[] value) {
 		// TODO Auto-generated method stub
 		String s = "";
 		try {
 			s = new String(value, "UTF-8");
 
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		String _msg = "";
 		for (int i = 0; i < value.length; i++) {
 			_msg += Integer.toHexString(value[i]) + " , ";
 		}
 		Log.v(TAG, s);
 		Log.v(TAG, _msg);
 
 	}
 
 	protected void onReadActivityHistoryData(List<Record> pedometerData) {
 		DatabaseHandler db = new DatabaseHandler(this, TABLE_CONTENT, null, 1);
 		Log.v(TAG,
 				"Incoming Activity Data Size : "+ pedometerData.size());
 		if (bTrace) {
 			Log.v(TAG,
 					"----------------------------Incoming Activity Data----------------------------");
 			for (Record record : pedometerData) {
 				Log.v(TAG, "Synced Activity > " + record.toString());
 			}
 			Log.v(TAG,
 					"----------------------------Incoming Activity Data----------------------------");
 
 			Log.v(TAG,
 					"~~~~~~~~~~~~~~~~~~~~~~~~~~~ OLD Activity ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 			List<Record> records = db.getAllRecords();
 			for (Record record : records) {
 
 				Log.v(TAG, record.toString());
 			}
 			Log.v(TAG,
 					"~~~~~~~~~~~~~~~~~~~~~~~~~~~ OLD Activity ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		}
 		Log.v(TAG,
 				"=====================Sync Activity Start=====================");
 		
 		db.updateRecord(pedometerData);
 //		for (Record record : pedometerData) {
 //			if (bTrace)
 //				Log.v(TAG, "Incoming Record " + record.toString());
 //			if (db.updateRecord(record) == 0) {
 //				db.addRecord(record);
 //				Log.v(TAG, "INSERT Activity =>=>=> " + record.toString());
 //			} else {
 //
 //				if (bTrace)
 //					Log.v(TAG, "UPDATE Activity ^^^ " + record.toString()
 //							+ "!!!!!!!!!!!!!!");
 //			}
 //
 //		}
 
 		Log.v(TAG,
 				"=====================Sync Activity End=====================");
 	}
 
 	protected void onReadSleepHistoryData(SleepRecord sleepRecord) {
 		if (bTrace) {
 			Log.v(TAG,
 					"----------------------------Incoming Sleep Data----------------------------");
 
 			Log.v(TAG, "Synced Sleep > " + sleepRecord.toString());
 
 			Log.v(TAG,
 					"----------------------------Incoming Sleep Data----------------------------");
 		}
 		DatabaseHandler db = new DatabaseHandler(this, TABLE_CONTENT, null, 1);
 		 if (bTrace)
 		{
 			Log.v(TAG,
 					"~~~~~~~~~~~~~~~~~~~~~~~~~~~ OLD Sleep Data ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 			List<SleepRecord> sleepRecords = db.getAllSleepRecords();
 			for (SleepRecord sleepRrecord : sleepRecords) {
 				Log.v(TAG, sleepRrecord.toString());
 			}
 			Log.v(TAG,
 					"~~~~~~~~~~~~~~~~~~~~~~~~~~~ OLD Sleep Data ~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		}
 
 		if (bTrace)
 			Log.v(TAG,
 					"=====================Sync Sleep Start=====================");
 //		db.updateSleepRecord(sleepRecord);
 		if (db.updateSleepRecord(sleepRecord) == 0) {
 			db.addSleepRecord(sleepRecord);
 			Log.v(TAG, "INSERT Sleep Record =>=>=> " + sleepRecord.toString());
 		} else {
 			if (bTrace)
 				Log.v(TAG, "UPDATE Sleep Recod ^^^ " + sleepRecord.toString()
 						+ "!!!!!!!!!!!!!!");
 		}
 		if (bTrace)
 			Log.v(TAG,
 					"=====================Sync Sleep Start=====================");
 
 	}
 
 	public void onConnectionTimeout() {
 
 	}
 
 	private byte[] concat(byte[]... arrays) {
 		// Determine the length of the result array
 		int totalLength = 0;
 		for (int i = 0; i < arrays.length; i++) {
 			totalLength += arrays[i].length;
 		}
 
 		// create the result array
 		byte[] result = new byte[totalLength];
 
 		// copy the source arrays into the result array
 		int currentIndex = 0;
 		for (int i = 0; i < arrays.length; i++) {
 			System.arraycopy(arrays[i], 0, result, currentIndex,
 					arrays[i].length);
 			currentIndex += arrays[i].length;
 		}
 
 		return result;
 	}
 }
