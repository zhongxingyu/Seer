 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package it.polimi.dei.bosp;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
import android.os.Debug;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class BbqueActivity extends Activity implements Runnable,
 		View.OnClickListener {
 
 	private Handler handler;
 	private TextView output;
 	private static final String TAG = "BbqueActivity";
 	private static final String APP_NAME = "ABbque";
 	private static final String APP_RECIPE = "BbqRTLibTestApp";
 
 	/** Flag indicating whether we have called bind on the service. */
     boolean mBound;
 
     /** Messenger for communicating with the service. */
     Messenger mService;
 
     /** IntentFilter used to receive broadcast intents launched by service */
     IntentFilter receiverFilter = new IntentFilter ();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		super.setContentView(R.layout.main);
 		output = (TextView) findViewById(R.id.output);
 		handler = new Handler();
 		//TODO: Remove tracing when not needed anymore
		Debug.startMethodTracing();
 		receiverFilter.addAction("it.polimi.dei.bosp.BBQUE_INTENT");
 		registerReceiver(receiver, receiverFilter);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Binding to the service. This automatically starts the Service
         bindService(new Intent(this, CustomService.class), mConnection,
             Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		handler.post(this);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		unregisterReceiver(receiver);
 		// Unbinding from the service
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
		Debug.stopMethodTracing();
 	}
 
 	/** Buttons interacts with the Service through Messenger paradigm. */
 
 	public void btnRegistered(View v) {
 		Log.d(TAG, "isRegistered button pressed...");
 		if (!mBound) return;
 		Message msg = Message.obtain(null, CustomService.MSG_ISREGISTERED, 0, 0);
 		try {
 			msg.replyTo = mMessenger;
 			mService.send(msg);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void btnCreate(View v) {
 		Log.d(TAG, "Create button pressed...");
 		if (!mBound) return;
 		Message msg = Message.obtain(null, CustomService.MSG_CREATE, 0, 0);
 		msg.obj = APP_NAME+"#"+APP_RECIPE;
 		try {
 			msg.replyTo = mMessenger;
 			mService.send(msg);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void btnStart(View v) {
 		Log.d(TAG, "Start button pressed...");
 		if (!mBound) return;
 		//Prepare a message to set the number of onRun cycles to be performed
 		Message cycles = Message.obtain(null, CustomService.MSG_CYCLES, 0, 0);
 		//Get the number of iterations from the "cycle_inputs" EditText.
 		String input = (((EditText)findViewById(
 									R.id.cycles_input)).getText().toString());
 		//If empty, assume 0.
 		cycles.arg1 = (!input.equals("") ? Integer.parseInt(input) : 0);
 		try {
 			cycles.replyTo = mMessenger;
 			mService.send(cycles);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		//Prepare a message to call the "EXCStart" native function.
 		Message msg = Message.obtain(null, CustomService.MSG_START, 0, 0);
 		try {
 			msg.replyTo = mMessenger;
 			mService.send(msg);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void btnConsole(View v) {
 		Log.d(TAG, "Console button pressed");
 		startActivity(new Intent(this, BBC.class));
 	}
 
 	/**
 	 * Broadcast receiver: catches messages sent by the BbqueService, and its
 	 * derived Services
 	 */
 	BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String bbqDebugIntent = intent.getStringExtra("BBQ_DEBUG");
 			output.setText(bbqDebugIntent);
 			if (intent.getIntExtra("ON_RELEASE", 0) == 1) {
 				findViewById(R.id.btnStart).setEnabled(true);
 				findViewById(R.id.btnCreate).setEnabled(true);
 			}
 		}
 	};
 
 	/**
 	 * Instantiate the target - to be sent to clients - to communicate with
 	 * this instance of Service
 	 */
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	/**
 	 * Handler of incoming messages from Service (typically Service responses).
 	 * The same MSG_XXX are used to receive responses regarding the calls the
 	 * activity made before.
 	 */
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case CustomService.MSG_ISREGISTERED:
 				Log.d(TAG, "isRegistered?: "+msg.arg1);
 				output.setText("isRegistered?: "+msg.arg1);
 				break;
 			case CustomService.MSG_CREATE:
 				if(msg.arg1 == 0)
 					findViewById(R.id.btnCreate).setEnabled(false);
 				Log.d(TAG, "Create return: "+msg.arg1);
 				output.setText("Create return: "+msg.arg1);
 				break;
 			case CustomService.MSG_START:
 				if(msg.arg1 == 0)
 					findViewById(R.id.btnStart).setEnabled(false);
 				Log.d(TAG, "Start return: "+msg.arg1);
 				output.setText("Start return: "+msg.arg1);
 				break;
 			case CustomService.MSG_TERMINATE:
 				Log.d(TAG, "Terminate return: "+msg.arg1);
 				output.setText("Terminate return: "+msg.arg1);
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	/** Class for interacting with the main interface of the service. */
 	private final ServiceConnection mConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName className,
 				IBinder service) {
 			mService = new Messenger(service);
 			mBound = true;
 		}
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			mService = null;
 			mBound = false;
 		}
 	};
 }
