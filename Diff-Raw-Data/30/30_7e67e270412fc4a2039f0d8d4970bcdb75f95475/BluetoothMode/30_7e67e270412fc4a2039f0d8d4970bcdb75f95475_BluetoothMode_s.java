 package com.uci.mobile.Project_9;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.StringTokenizer;
 import java.util.UUID;
 
 import com.uci.mobile.Game.Game;
 
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BluetoothMode extends Activity {
 
 	BluetoothAdapter mBluetoothAdapter;
 	Button acceptButton;
 	Button connectButton;
 	Button attackButton;
 	Button reloadButton;
 	Button defendButton;
 	ProgressBar progressBar;
 	TextView acceptingText;
 	private int mState;
 	private AcceptThread mAcceptThread;
 	private ConnectThread mConnectThread;
 	private ConnectedThread mConnectedThread;
 	private TextView enemyText;
 	private TextView ammoText;
 	private TextView lifeText;
 	private TextView playerWinsText;
 	public static String mDeviceAdd = null;
 	private String name;
 	private String character;
 	private ImageView imageCharacter;
 	private Game game;
 	private int userMove;
 	private int opponentMove;
 	private boolean youMoved;
 	private boolean opponentMoved;
 	private boolean gameWon;
 	BluetoothDevice mDevice = null;
 
 	// Constants that indicate the current connection state
 	public static final int STATE_NONE = 0;       // we're doing nothing
 	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
 	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
 	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
 
 	private static final String NAME = "Project 9";
 
 	private final static int REQUEST_ENABLE_BT = 22;
 	private static final int REQUEST_CONNECT_DEVICE = 21;
 
 	// Unique UUID for this application
 	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
 
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.bluetooth);
 		acceptButton = (Button) findViewById(R.id.button1);
 		connectButton = (Button) findViewById(R.id.button2);
 		attackButton = (Button) findViewById(R.id.attackButton);
 		defendButton = (Button) findViewById(R.id.defendButton);
 		reloadButton = (Button) findViewById(R.id.reloadButton);
 		lifeText = (TextView) findViewById(R.id.lifeText);
 		ammoText = (TextView) findViewById(R.id.ammoText);
 		enemyText = (TextView) findViewById(R.id.enemyText);
 		playerWinsText = (TextView) findViewById(R.id.playerWinsText);
 		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 		acceptingText = (TextView) findViewById(R.id.acceptingText);
 		imageCharacter = (ImageView) findViewById(R.id.imageView1);
 		gameWon = false;
 		Toast.makeText(getApplicationContext(), "To play, have your friend press accept and find his phone by pressing connect!", Toast.LENGTH_SHORT).show();
 		if(CheckFile(Project_9Activity.USERIDFILE))
 		{
 			try 
 			{
 				DataInputStream dis = new DataInputStream(openFileInput(Project_9Activity.USERIDFILE));
 				name = dis.readLine();
 			} catch (FileNotFoundException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		if(CheckFile(Project_9Activity.USERCHARACTERFILE))
 		{
 			try 
 			{
 				DataInputStream dis = new DataInputStream(openFileInput(Project_9Activity.USERCHARACTERFILE));
 				character = dis.readLine();
 			} catch (FileNotFoundException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		acceptButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0)
 			{
 				mAcceptThread = new AcceptThread();
 				progressBar.setVisibility(View.VISIBLE);
 				acceptingText.setVisibility(View.VISIBLE);
 				mState = STATE_LISTEN;
 				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 400);
 				startActivity(discoverableIntent);
 				mAcceptThread.start();
 			}
 		});
 		connectButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0)
 			{
 				Intent serverIntent = new Intent(getBaseContext(), DeviceListActivity.class);
 				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
 			}
 		});
 		attackButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0)
 			{
 				if(mConnectedThread != null)
 				{
 					if (!gameWon)
 					{
 						if(game.getUserAmo() > 0) {
 
 							userMove = Game.ATTACK;
 							mConnectedThread.write(new String("1 " + userMove).getBytes());
 							youMoved = true;
 							if (opponentMoved == true)
 								determineRound();
 						}
 					}
 				}
 			}
 		});
 
 		reloadButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0)
 			{
 				if(mConnectedThread != null)
 				{
 					if (!gameWon)
 					{
 						userMove = Game.RELOAD;
 						mConnectedThread.write(new String("1 " + userMove).getBytes());
 						youMoved = true;
 						if (opponentMoved == true)
 							determineRound();
 					}
 				}
 			}
 		});
 
 		defendButton.setOnClickListener(new OnClickListener(){
 			public void onClick(View arg0)
 			{
 				if(mConnectedThread != null)
 				{
 					if (!gameWon)
 					{
 						userMove = Game.DEFEND;
 						mConnectedThread.write(new String("1 " + userMove).getBytes());
 						youMoved = true;
 						if (opponentMoved == true)
 							determineRound();
 					}
 				}
 			}
 		});
 		if (mBluetoothAdapter == null) 
 		{
 			Toast.makeText(getApplicationContext(), "Application does not support Bluetooth", Toast.LENGTH_SHORT);
 		}
 		else
 		{
 			if (!mBluetoothAdapter.isEnabled()) {
 				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
 				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 			}
 		}
 
 	}
 
 	protected void onDestroy(){
 		if (mAcceptThread != null)
 			mAcceptThread.cancel();
 		if (mConnectThread != null)
 			mConnectThread.cancel();
 		if (mConnectedThread != null)
 			mConnectedThread.cancel();
 	}
 
 	private void determineRound()
 	{
 		game.determine(userMove, opponentMove);
 		lifeText.setText("Lives: " + game.getUserHealth());
 		ammoText.setText("Ammo: " + game.getUserAmo());
 		youMoved = false;
 		opponentMoved = false;
 		if (game.getBossHealth() <= 0) {
 			gameWon = true;
 			playerWinsText.setText("You win!");
 			playerWinsText.setVisibility(View.VISIBLE);
 		}
 		else if (game.getUserHealth() <= 0){
 			gameWon = true;
 			playerWinsText.setText("You lose!");
 			playerWinsText.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private final Handler mHandler = new Handler() {
 
 		Bundle b;
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case H_HIDE_NON_GAME:
 				acceptButton.setVisibility(View.INVISIBLE);
 				connectButton.setVisibility(View.INVISIBLE);
 				progressBar.setVisibility(View.INVISIBLE);
 				acceptingText.setVisibility(View.INVISIBLE);
 				game = new Game();
 				game.setBoss(0);
 				break;
 			case H_FIRST_TIME_CONNECTING:
 				b = msg.getData();
 				String character = b.getString("characterNumber");
 				String theId = b.getString("playerID");
 				switch(Integer.parseInt(character)){
 				case 0:
 					imageCharacter.setImageResource(R.drawable.a1);
 					break;
 				case 1:
 					imageCharacter.setImageResource(R.drawable.b1);
 					break;
 				case 2:
 					imageCharacter.setImageResource(R.drawable.c1);
 					break;
 				case 3:
 					imageCharacter.setImageResource(R.drawable.d1);
 					break;
 				case 4:
 					imageCharacter.setImageResource(R.drawable.e1);
 					break;
 				}
 				enemyText.setText("Enemy: " + theId);
 				break;
 			case H_OPPONENT_MOVED:
 				b = msg.getData();
 				opponentMove = Integer.parseInt(b.getString("opponentMove"));
 				opponentMoved = true;
 				if (youMoved == true)
 					determineRound();
 				break;
 				//			case 12:
 				//				b = msg.getData();
 				//				String key = b.getString("numberText");
 				//				numberText.setText(key);
 				//				break;
 			}
 		}
 	};
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case REQUEST_CONNECT_DEVICE:
 			// When DeviceListActivity returns with a device to connect
 			if (resultCode == Activity.RESULT_OK) {
 				// Get the device MAC address
 				mDeviceAdd = data.getExtras().getString(
 						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
 				// Get the BLuetoothDevice object
 				mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAdd);
 				// Attempt to connect to the device
 				connect(mDevice);
 			}
 			break;
 		}
 	}
 
 	public synchronized void start() {
 		//if (D) Log.d(TAG, "start");
 
 		// Cancel any thread attempting to make a connection
 		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Start the thread to listen on a BluetoothServerSocket
 		if (mAcceptThread == null) {
 			mAcceptThread = new AcceptThread();
 			mAcceptThread.start();
 		}
 		mState = STATE_LISTEN;
 	}
 
 	public synchronized void connect(BluetoothDevice device) {
 
 		// Cancel any thread attempting to make a connection
 		if (mState == STATE_CONNECTING) {
 			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 		}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Start the thread to connect with the given device
 		mConnectThread = new ConnectThread(device);
 		mConnectThread.start();
 		mState = STATE_CONNECTING;
 	}
 
 	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
 		//if (D) Log.d(TAG, "connected");
 		//progressBar.setVisibility(View.INVISIBLE);
 		//text.setVisibility(View.INVISIBLE);
 		// Cancel the thread that completed the connection
 		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
 
 		// Cancel any thread currently running a connection
 		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
 
 		// Cancel the accept thread because we only want to connect to one device
 		if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
 
 		// Start the thread to manage the connection and perform transmissions
 		mConnectedThread = new ConnectedThread(socket);
 		mConnectedThread.start();
 	}
 
 	private class AcceptThread extends Thread {
 		// The local server socket
 		private final BluetoothServerSocket mmServerSocket;
 
 		public AcceptThread() {
 			BluetoothServerSocket tmp = null;
 
 			// Create a new listening server socket
 			try {
 				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
 			} catch (IOException e) {
 				Log.e("SAD", "listen() failed", e);
 			}
 			mmServerSocket = tmp;
 		}
 
 		public void run() {
 
 			setName("AcceptThread");
 			BluetoothSocket socket = null;
 
 			// Listen to the server socket if we're not connected
 			while (true) {
 				try {
 					// This is a blocking call and will only return on a
 					// successful connection or an exception
 					socket = mmServerSocket.accept();
 				} catch (IOException e) {
 					Log.e("", "accept() failed", e);
 					break;
 				}
 
 				if (socket != null) {
 					synchronized (BluetoothMode.this) {
 						switch (mState) {
 						case STATE_LISTEN:
 						case STATE_CONNECTING:
 							// Situation normal. Start the connected thread.
 							connected(socket, socket.getRemoteDevice());
 							break;
 						case STATE_NONE:
 						case STATE_CONNECTED:
 							// Either not ready or already connected. Terminate new socket.
 							try {
 								socket.close();
 							} catch (IOException e) {
 								Log.e("TAG", "Could not close unwanted socket", e);
 							}
 							break;
 						}
 					}
 				}
 			}
 
 		}
 
 		public void cancel() {
 			//  if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
 			try {
 				mmServerSocket.close();
 			} catch (IOException e) {
 				//   Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
 			}
 		}
 
 	}
 
 	private class ConnectThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final BluetoothDevice mmDevice;
 		private String mSocketType;
 
 		public ConnectThread(BluetoothDevice device) {
 			mmDevice = device;
 			BluetoothSocket tmp = null;
 
 			// Get a BluetoothSocket for a connection with the
 			// given BluetoothDevice
 			try {
 				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
 
 			} catch (IOException e) {
 				Log.e("TAG", "Socket Type: create() failed", e);
 			}
 			mmSocket = tmp;
 		}
 
 		public void run() {
 			Log.i("TAG", "BEGIN mConnectThread SocketType:" + mSocketType);
 			setName("ConnectThread" + mSocketType);
 
 			// Always cancel discovery because it will slow down a connection
 			mBluetoothAdapter.cancelDiscovery();
 
 			// Make a connection to the BluetoothSocket
 			try {
 				// This is a blocking call and will only return on a
 				// successful connection or an exception
 				mmSocket.connect();
 			} catch (IOException e) {
 				// Close the socket
 				try {
 					mmSocket.close();
 				} catch (IOException e2) {
 					Log.e("TAG", "unable to close() socket during connection failure", e2);
 				}
 				Log.e("TAG", "CONNECTION FAIL");
 				return;
 			}
 
 			// Reset the ConnectThread because we're done
 			synchronized (BluetoothMode.this) {
 				mConnectThread = null;
 			}
 
 			// Start the connected thread
 			connected(mmSocket, mmDevice);
 		}
 
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) {
 				Log.e("TAG", "close() of connect socket failed", e);
 			}
 		}
 	}
 
 	private class ConnectedThread extends Thread {
 		private final BluetoothSocket mmSocket;
 		private final InputStream mmInStream;
 		private final OutputStream mmOutStream;
 		private String[] sa;
 		Message msg;
 
 		public ConnectedThread(BluetoothSocket socket) {
 			mmSocket = socket;
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			// Get the input and output streams, using temp objects because
 			// member streams are final
 			try {
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			} catch (IOException e) { }
 
 			mmInStream = tmpIn;
 			mmOutStream = tmpOut;
 		}
 
 		public void run() {
 			msg = mHandler.obtainMessage(H_HIDE_NON_GAME);
 			mHandler.sendMessage(msg);
 			byte[] buffer = new byte[1024];  // buffer store for the stream
 			int bytes; // bytes returned from read()
 			String sendMe = "0 " + name + " " + character;
 			write(sendMe.getBytes());
 
 			// Keep listening to the InputStream until an exception occurs
 			while (true) {
 				try {
 					// Read from the InputStream
 					bytes = mmInStream.read(buffer);
 					String readMessage = new String(buffer, 0, bytes);
 					sa = readMessage.split(" ");
 					if (sa[0].equals("0"))
 					{
 						msg = mHandler.obtainMessage(H_FIRST_TIME_CONNECTING);
 						Bundle bundle = new Bundle();
 						bundle.putString("characterNumber", sa[2]);
 						bundle.putString("playerID", sa[1]);
 						msg.setData(bundle);
 						mHandler.sendMessage(msg);
 					}
 					if (sa[0].equals("1"))
 					{
 						msg = mHandler.obtainMessage(H_OPPONENT_MOVED);
 						Bundle bundle = new Bundle();
 						bundle.putString("opponentMove", sa[1]);
 						msg.setData(bundle);
 						mHandler.sendMessage(msg);
 					}
 
 				} catch (IOException e) {
 					break;
 				}
 			}
 		}
 
 		/* Call this from the main activity to send data to the remote device */
 		public void write(byte[] bytes) {
 			try {
 				mmOutStream.write(bytes);
 			} catch (IOException e) { }
 		}
 
 		/* Call this from the main activity to shutdown the connection */
 		public void cancel() {
 			try {
 				mmSocket.close();
 			} catch (IOException e) { }
 		}
 	}
 
 	public boolean CheckFile(String s)
 	{
 		String[] filelist = fileList();
 		for(int i=0;i<filelist.length;i++)
 		{
 			if(filelist[i].equals(s))
 			{
 				return true;
 			}
 		}
 		//			System.out.println(filelist[0]);
 		return false;
 	}
 
 
 	public static final int H_HIDE_NON_GAME = 10; // hides non relevant game elements
 	public static final int H_FIRST_TIME_CONNECTING = 11; // two phones are loading each other's phone name and characters
 	public static final int H_OPPONENT_MOVED = 12;
 
 }
