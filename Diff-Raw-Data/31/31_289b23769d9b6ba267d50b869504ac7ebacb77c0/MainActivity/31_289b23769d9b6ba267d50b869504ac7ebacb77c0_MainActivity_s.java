 package hu.medtech.tictactoe;
 
 import hu.medtech.tictactoe.ConnectionService.State;
 import hu.medtech.tictactoe.datastorage.ScoreDbLoader;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	// Debugging
 	private static final String TAG = "MainActivity";
 	private static final boolean D = true;
 
 	private static final int REQUEST_CONNECT = 1;
 	private static final int REQUEST_ENABLE_BT = 2;
 
 	public static final int MESSAGE_TOAST = 1;
 	public static final int MESSAGE_DEVICE_NAME = 2;
 	public static final int MESSAGE_READ = 3;
 
 	public static String TOAST;
 
 	private static BluetoothAdapter mBluetoothAdapter;
 	private static BluetoothDevice mBluetoothDevice;
 
 	private static final UUID UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
 	public static String mConnectedDeviceName;
 	public static String DEVICE_NAME;
 
 	ImageView multiplayer;
 	ImageView options;
 	ImageView exit;
 	ImageView highscore;
 
 	AlertDialog alert;
 	ProgressDialog progress;
 
 	public byte[] messageContainer2ByteArray(MessageContainer m) throws IOException {
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(baos);
 		oos.writeObject(m);
 		return baos.toByteArray();
 
 	}
 
 	OnClickListener onClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			switch (v.getId()) {
 
 			case R.id.main_multiplayer:
 
				if (((GlobalVariables) getApplication()).getConnectionService().getState() == State.connected) {
 
					MessageContainer startGameMessage = new MessageContainer(MessageContainer.MESSAGE_NEW_GAME, -1, "player1");
 
					try {
 
						((GlobalVariables) getApplication()).getConnectionService().write(messageContainer2ByteArray(startGameMessage));
 
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
 					}
 
 				} else {
 
					Intent startConnection = new Intent(getApplicationContext(), DeviceConnect.class);
					startActivityForResult(startConnection, REQUEST_CONNECT);
 
 				}
 
 				break;
 
 			case R.id.main_highscore:
 
 				Intent highscore = new Intent(MainActivity.this, HighScoreActivity.class);
 				startActivity(highscore);
 
 				break;
 
 			case R.id.main_options:
 
 				Intent options = new Intent(MainActivity.this, OptionsActivity.class);
 				startActivity(options);
 
 				break;
 
 			case R.id.main_exit:
 				// TODO: alert.show();
 				// //////EZ CSAK DEBUG CELRA KELL////////
 				Intent game = new Intent(MainActivity.this, GameActivity.class);
 				startActivity(game);
 				// /////////////////////////////////////
 				break;
 
 			default:
 				break;
 			}
 
 		}
 	};
 
 	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (D) {
 				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
 					Log.v(TAG, "RECEIVED BLUETOOTH STATE CHANGE: STATE_TURNING_ON");
 				}
 
 				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
 					Log.v(TAG, "RECEIVED BLUETOOTH STATE CHANGE: STATE_ON");
 				}
 			}
 
 			if (mBluetoothAdapter.isEnabled()) {
 				((GlobalVariables) getApplication()).setConnectionService(new ConnectionService(mHandler));
 				((GlobalVariables) getApplication()).getConnectionService().start();
 			}
 		}
 
 	};
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.v(TAG, "ONSTART");
 		if (!mBluetoothAdapter.isEnabled()) {
 			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
 		}
 
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Log.v(TAG, "ONCREATE");
 		// Get local Bluetooth adapter
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
 		// If the adapter is null, then Bluetooth is not supported
 		if (mBluetoothAdapter == null) {
 			Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 
 		registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
 
 		if (mBluetoothAdapter.isEnabled()) {
 			((GlobalVariables) getApplication()).setConnectionService(new ConnectionService(mHandler));
 			((GlobalVariables) getApplication()).getConnectionService().start();
 		}
 
 		// are you sure dialog
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					finish();
 					break;
 
 				case DialogInterface.BUTTON_NEGATIVE:
 					dialog.cancel();
 					break;
 				}
 			}
 		};
 
 		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
 		alt_bld.setMessage("Are you sure to want to quit?").setCancelable(false).setPositiveButton("Yes", dialogClickListener)
 				.setNegativeButton("No", dialogClickListener);
 		alert = alt_bld.create();
 		alert.setTitle("Are you sure?");
 
 		// proba ertek beirasa a tablaba
 		ScoreDbLoader dbLoader = new ScoreDbLoader(getApplicationContext());
 		dbLoader.open();
 		dbLoader.createScore(new Score("Sanyi", "00:50", "15"));
 		dbLoader.createScore(new Score("S", "0:0", "1"));
 		dbLoader.close();
 
 		multiplayer = (ImageView) findViewById(R.id.main_multiplayer);
 		highscore = (ImageView) findViewById(R.id.main_highscore);
 		options = (ImageView) findViewById(R.id.main_options);
 		exit = (ImageView) findViewById(R.id.main_exit);
 
 		multiplayer.setOnClickListener(onClickListener);
 		highscore.setOnClickListener(onClickListener);
 		options.setOnClickListener(onClickListener);
 		exit.setOnClickListener(onClickListener);
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (((GlobalVariables) getApplication()).getConnectionService() != null) {
 			((GlobalVariables) getApplication()).getConnectionService().stop();
 			unregisterReceiver(bluetoothReceiver);
 		}
 	}
 
 	private final Handler mHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			switch (msg.what) {
 
 			case MESSAGE_TOAST:
 
 				String message = (String) msg.obj;
 
 				if (message.contains("Connected"))
 					Toast.makeText(getApplicationContext(), "Connected start a new multiplayer game", Toast.LENGTH_LONG).show();
 
 				if (progress != null) {
 					if (progress.isShowing()) {
 						progress.dismiss();
 					}
 				}
 
 				break;
 
 			case MESSAGE_READ:
 
 				try {
 
 					byte[] readBuf = (byte[]) msg.obj;
 					int paramInt = msg.arg1;
 
 					ByteArrayInputStream bais = new ByteArrayInputStream(readBuf);
 					ObjectInputStream ois;
 					ois = new ObjectInputStream(bais);
 
 					MessageContainer readedMessage = (MessageContainer) ois.readObject();
 					Log.i("MainActivity", "Message NUM: " + readedMessage.getMessage());
 
 					switch (readedMessage.getMessage()) {
 
 					case MessageContainer.MESSAGE_NEW_GAME:
 
 						Log.i("MainActivity", "MESSAGE_NEW_GAME received from: " + readedMessage.getName() + " sending ACK");
 
 						MessageContainer ackNewGame = new MessageContainer(MessageContainer.MESSAGE_ACK, -1, "player2");
 						((GlobalVariables) getApplication()).getConnectionService().write(messageContainer2ByteArray(ackNewGame));
 
 						Intent gameActivity = new Intent(getApplicationContext(), GameActivity.class);
 						startActivity(gameActivity);
 
 						break;
 
 					case MessageContainer.MESSAGE_ACK:
 
 						Log.i("MainActivity", "MESSAGE_ACK received");
 
 						if (readedMessage.getCoords() == -1) {
 							Log.i("MainActivity", "ACK for starting new Game with: " + readedMessage.getName());
 
 							Intent ackGameActivity = new Intent(getApplicationContext(), GameActivity.class);
 							startActivity(ackGameActivity);
 						}
 
 						break;
 
 					default:
 
 						break;
 					}
 
 				} catch (StreamCorruptedException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 
 				break;
 
 			}
 		}
 	};
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		switch (requestCode) {
 		case REQUEST_CONNECT:
 
 			if (resultCode == RESULT_CANCELED) {
 				Toast.makeText(getApplicationContext(), "No device selected", Toast.LENGTH_LONG).show();
 				break;
 			}
 
 			progress = ProgressDialog.show(this, "Please wait", "Setting up connection...", true, false);
 
 			String address = data.getStringExtra(DeviceConnect.EXTRA_DEVICE_ADDRESS);
 			mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
 			Log.i(TAG, mBluetoothDevice.getName() + mBluetoothDevice.getAddress());
 
 			((GlobalVariables) getApplication()).getConnectionService().connect(mBluetoothDevice);
 
 			break;
 
 		case REQUEST_ENABLE_BT:
 
 			if (resultCode == RESULT_CANCELED) {
 				Toast.makeText(getApplicationContext(), "Can't enable bluetooth", Toast.LENGTH_LONG).show();
 				break;
 			}
 
 			if (resultCode == RESULT_OK) {
 				Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
 				break;
 			}
 
 			break;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_connect:
 
 			Intent startConnection = new Intent(getApplicationContext(), DeviceConnect.class);
 			startActivityForResult(startConnection, REQUEST_CONNECT);
 
 			break;
 
 		case R.id.menu_discoverable:
 			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 			startActivity(discoverableIntent);
 			break;
 
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		// vissza gomb eseten kerdes
 		alert.show();
 	}
 
 }
