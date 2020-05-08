 package com.example.benchmarkactivity;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.TextView;
 
 import com.example.benchmarkservice.IActivityListener;
 import com.example.benchmarkservice.IBenchMarkService;
 
 public class MainActivity extends Activity {
 
 	/**
 	 * Statistics
 	 */
 	private float	rate;
 	private long 	counter, begin, stop, gap, loss;
 	
 	/**
 	 * CPU Statistics
 	 */
 	private long work, workAM, total;
 	private long workBefore, totalBefore, workAMBefore;
 	private RandomAccessFile reader;
 	
 	/**
 	 * Graphic widget
 	 */
 	static int SET = 100, DIVISOR = 10;
 	
 	//scaling graph values
 	//OS one shot
 	//LI listener intent
 	static int OSSCALE = 3000, LISCALE = DIVISOR;
 
 	//Service Variables
 	private static AIDLConnection	BoundAIDLConnection;
 	private IBenchMarkService		IBenchService;
 	private boolean					state = false;
 	
 	private byte[] buffer;
 	private String opts, s, unit;
 	private static int N_PACKETS = 5000;
 	
 	private TextView	textResultService;
 	private TextView	textResultStats;
 	private TextView	textViewPayload;
 	private Widget		widget;
 	private Button		bStartAIDLService;
 	private Button		bStopAIDLService;
 	private RadioButton	rbPriority0;
 	private RadioButton	rbPriority1;
 	private RadioButton	rbPriority2;
 	private AnGraphic	graphWidget;
 
 	private Intent		intent;
 	private BReceiver	bcRec;
 	private Handler		handler;
 
 	//contain which mode used to evaluate benchmark
 	private int mode = 0;
 	//priority select for the thread
 	private int prio = 0;
 	private int tid;
 	
 	protected static String[] sourcesItems = new String[]{"Shot", "Listener", "Intent"};
 	
 	private IActivityListener.Stub iListener = new IActivityListener.Stub(){
 		@Override
 		public void onReadyValue(byte[] load) throws RemoteException {
 			buffer = load.clone();
 			counter++;
 			if(counter % N_PACKETS == 0)
 				UpdateTextView();
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mapObjectAndListener();
 		
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		bcRec = new BReceiver();
 		registerReceiver(bcRec, new IntentFilter("com.example.benchmarkactivity.MainActivity"));
		BoundAIDLConnection = new AIDLConnection();
		intent = new Intent("com.example.benchmarkservice.IBenchMarkService");
		bindService(intent, BoundAIDLConnection, Context.BIND_AUTO_CREATE);
		
		tid = android.os.Process.myTid();
 	}
 
 	@Override
 	protected void onPause() {
		try {
			IBenchService.stopRunning();
		} catch (RemoteException e) {
			Log.e("Activity", "Impossible to stop remote service");
		}
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onStop() {
 		unregisterReceiver(bcRec);
 		unbindService(BoundAIDLConnection);
 		graphWidget.saveInfoToStorage();
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	/**
 	 * Class definition to connect on AIDL Service
 	 * @author F31999A
 	 *
 	 */
 	class AIDLConnection implements ServiceConnection{
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			IBenchService = IBenchMarkService.Stub.asInterface(service);
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			IBenchService = null;
 		}
 	}
 
 	public class BReceiver extends BroadcastReceiver{
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			counter++;
 			writeByteArray(intent.getByteArrayExtra("LOAD"));
 		}
 	}
 
 	/**
 	 * Initialize all the object from the ContentView and associate 
 	 * their relative listeners
 	 */
 	private void mapObjectAndListener(){
 		LinearLayout l = (LinearLayout)findViewById(R.id.layerwidget);
 		widget				= (Widget)		l.findViewById(R.id.Widget);
 		rbPriority0			= (RadioButton)	l.findViewById(R.id.priority0);
 		rbPriority1			= (RadioButton)	l.findViewById(R.id.priority1);
 		rbPriority2			= (RadioButton)	l.findViewById(R.id.priority2);
 		l					= (LinearLayout)findViewById(R.id.layerButtons);
 		bStartAIDLService	= (Button)		l.findViewById(R.id.buttonStartAIDL);
 		bStopAIDLService	= (Button)		l.findViewById(R.id.buttonStopAIDL);
 		l					= (LinearLayout)findViewById(R.id.layerTexts);
 		textResultService	= (TextView)	l.findViewById(R.id.textView1);
 		textResultStats		= (TextView)	l.findViewById(R.id.textView2);
 		graphWidget			= (AnGraphic)	l.findViewById(R.id.graphWidget);
 		l					= (LinearLayout)findViewById(R.id.layerPayload);
 //		textViewPayload		= (TextView)	l.findViewById(R.id.textViewPayload);
 
 		bStartAIDLService.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(!state){
 					rate = gap = stop = counter = 0;
 					loss = 0;
 					showChoices();
 				}
 			}
 		});
 		
 		bStopAIDLService.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(state){
 					try{
 						IBenchService.stopRunning();
 						gap		= System.currentTimeMillis() - begin;
 						loss	= (IBenchService.getNPackets() - counter);
 						loss	= (loss>0)? loss : 0;
 						rate	= ((float)counter)/((float)gap);
 						
 						textResultService.setText("ELAPSED TIME ["+unit+"]:" +
 												"\nactivity: " + gap + 
 												"\n\nPACKETS COUNTED:" +
 												"\nactivity: " + counter +
 												"\nservice: " + IBenchService.getNPackets() +
 												"\n\nTotal sended: " + ((counter * widget.getIntValue())/1024) + " KByte" +
 												"\n\n PACKET'S RATE [pks/"+unit+"]:" +
 												"\nActivity rate: " + rate + 
 												"\nPackets loss [n]: " + loss +
 												((loss > 0)? "\nPackets loss rate [n/"+unit+"]: "+ ((float)loss)/((float)gap) : "" ));
 						saveStatElement();
 
 						state	= false;
 						handler	= null;
 					} catch(RemoteException e){
 						Log.e("AIDL", "STOP SEND ERROR");
 					}
 				}
 			}
 		});
 
 		widget.addLeftClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					IBenchService.setBurstSize(widget.getIntValue());
 				} catch (RemoteException e) {
 					Log.e("AIDL", "SETBURST SIZE -");
 				}
 			}
 		});
 
 		widget.addRightClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					IBenchService.setBurstSize(widget.getIntValue());
 				} catch (RemoteException e) {
 					Log.e("AIDL", "SETBURST SIZE +");
 				}
 			}
 		});
 
 		setRadioButtonListener();
 	}
 
 	/**
 	 * Set default checked value and add all the complementary listener to the radio button
 	 */
 	private void setRadioButtonListener() {
 		rbPriority0.setChecked(true);
 		
 		rbPriority0.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				android.os.Process.setThreadPriority(tid,android.os.Process.THREAD_PRIORITY_DEFAULT);
 				prio = 0;
 			}
 		});
 		
 		rbPriority1.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				android.os.Process.setThreadPriority(tid,android.os.Process.THREAD_PRIORITY_FOREGROUND);
 				prio = -2;
 			}
 		});
 		
 		rbPriority2.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				android.os.Process.setThreadPriority(tid,android.os.Process.THREAD_PRIORITY_DISPLAY);
 				prio = -4;
 			}
 		});
 	}
 
 	private void saveStatElement(){
 		String report = graphWidget.addDataContainer(widget.getIntValue(), mode, new DataContainer(gap, counter, rate, loss, getCpuUsage(), prio));
 		graphWidget.invalidate();
 		textResultStats.setText(report + "\npriority: " + android.os.Process.getThreadPriority(android.os.Process.myTid()) + "\nMode: " + sourcesItems[mode] );
 	}
 
 	private long getCpuUsage(){
 		String[] tok;
 		long workT	 = 0;
 		long totalT	 = 0;
 		long workAMT = 0;
 		try {
 			reader = new RandomAccessFile("/proc/stat","r");
 			tok = reader.readLine().split("[ ]+", 9);
 			
 			work	= Long.parseLong(tok[1]) +
 					  Long.parseLong(tok[2]) +
 					  Long.parseLong(tok[3]);
 			total	= work +
 					  Long.parseLong(tok[4]) +
 					  Long.parseLong(tok[5]) +
 					  Long.parseLong(tok[6]) +
 					  Long.parseLong(tok[7]);
 			
 			reader = new RandomAccessFile("/proc/"+android.os.Process.myPid()+"/stat","r");
 			tok = reader.readLine().split("[ ]+", 18);
 			
 			workAM	= Long.parseLong(tok[13]) +
 					  Long.parseLong(tok[14]) +
 					  Long.parseLong(tok[15]) +
 					  Long.parseLong(tok[16]);
 			if(totalBefore != 0){
 				workT	= work - workBefore;
 				totalT	= total - totalBefore;
 				workAMT	= workAM - workAMBefore;
 			}
 			workBefore = work;
 			totalBefore = total;
 			workAMBefore = workAM;
 
 			return workAMT;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * show up dialog to choose which method to trigger
 	 */
 	protected void showChoices() {
 		new AlertDialog.Builder(MainActivity.this)
 		.setTitle(R.string.selectSource)
 		.setSingleChoiceItems(sourcesItems, mode, new DialogInterface.OnClickListener() {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {}})
 		.setPositiveButton("go", yesListener)
 		.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
 										@Override
 										public void onClick(DialogInterface dialog, int which) {}})
 		.show();
 	}
 
 	/**
 	 * define and schedule partial data 
 	 */
 	private void show(){
 		if(handler != null){
 			handler.postDelayed(new Runnable() {
 				
 				@Override
 				public void run() {
 					long gap = System.currentTimeMillis() - begin;
 					long n = counter;
 					print(gap, n, 3);
 				}
 			}, 1000);
 		}
 	}
 
 	/**
 	 * print partial data and reschedule show process
 	 * modes:
 	 * 0 - singleShot
 	 * 1 - listener
 	 * 2 - intent
 	 * 3 - listener & intent (same for both)
 	 * @param gap
 	 * @param counter
 	 */
 	private void print(long gap, long counter, int mode) {
 		if(state){
 			opts	= (mode != 0)? ("\n\nTotal sended: "
 					+ ((counter * widget.getIntValue())/1024)
 					+ " KByte\n\nPACKET'S RATE [pks/"+unit+"]:"
 					+ (float)((float)counter)/((float)gap))
 					: "";
 
 			textResultService.setText("ELAPSED TIME ["+unit+"]:" + gap
 								+ "\n\nPACKETS COUNTED: " + counter
 								+ opts);
 	
 //			if(buffer.length > 0){
 //				s = "";
 //				for(int i=0; i<buffer.length; i++){
 //					s += (buffer[i] + " | ");
 //				}
 //				textViewPayload.setText("\nsize: " + buffer.length + "\nPayload: " + s);
 //			}
 			if(mode != 0)
 				show();
 		}
 	}
 
 	private synchronized void writeByteArray(byte[] bufferIn){
 		buffer = bufferIn.clone();
 	}
 
 	final DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener(){
 
 		@Override
 		public void onClick(DialogInterface dialog, int arg1) {
 			state	= true;
 			counter	= 0;
 			handler	= new Handler();
 			mode = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
 			unit = (mode != 0)? "ms" : "ns";
 			switch(mode){
 			case 0:	//ONE SHOT
 				try {
 					IBenchService.setOneShotPacketSize(widget.getIntValue());
 					begin	= System.nanoTime();
 					buffer	= IBenchService.getOneShotPacket();
 					stop	= System.nanoTime();
 					gap		= stop - begin;
 					print(gap, 1, 0);
 					state = false;
 					saveStatElement();
 				} catch (RemoteException e) {
 					Log.e("AIDL", "START SEND ERROR");
 				}
 				break;
 			case 1:	//LISTENER
 				try {
 					IBenchService.setOneShotPacketSize(widget.getIntValue());
 					IBenchService.bindClientListener(iListener);
 					begin = System.currentTimeMillis();
 					IBenchService.startListenerRunning();
 				} catch (RemoteException e) {
 					Log.e("AIDL", "START SEND ERROR");
 				}
 				break;
 			case 2: //INTENT
 				try {
 					begin = System.currentTimeMillis();
 					IBenchService.startRunning();
 					show();
 				} catch (RemoteException e) {
 					Log.e("AIDL", "START SEND ERROR");
 				}
 				break;
 			}
 		};
 	};
 
 	/**
 	 * set up thread to show partial result, associated to IActivityListener
 	 */
 	private void UpdateTextView(){
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				gap = System.currentTimeMillis() - begin;
 				rate = ((float)counter)/((float)gap);
 //				s = "";
 //				for(int i=0; i<buffer.length; i++){
 //					s += (buffer[i] + " | ");
 //				}
 				textResultService.setText("ELAPSED TIME ["+unit+"]:" + gap + 
 										"\n\nPACKETS COUNTED: " + counter +
 										"\n\nTotal sended: " + ((counter * widget.getIntValue())/1024) + " KByte" +
 										"\n\nPACKET'S RATE [pks/"+unit+"]:" + rate);
 //				textViewPayload.setText("\nsize: " + buffer.length + "\nPayload: " + s);
 			}
 		});
 	}
 }
