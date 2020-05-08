 package com.example.simplevrcontroller.gamepad;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.simplevrcontroller.MainActivity;
 import com.example.simplevrcontroller.R;
 import com.example.simplevrcontroller.cave.Cave;
 import com.example.simplevrcontroller.cave.CaveManager;
 
 // 
 public class Gamepad extends Activity implements OnTouchListener, SensorEventListener{
 	
 	// Sockets 
 	private InetAddress serverAddr;
 	private DatagramSocket socket;
 	private boolean socketOpen;
 	private Cave cave;
 	private int port;
 	private String nodeName;
 	private String axis;
 	private Map<String, Boolean> nodeOn;
 	
 	private static final int MAX_SPEED = 50;
 
     // Type for queueing in CalVR
     static final int COMMAND = 7;
     static final int NAVI = 8;
     static final int NODE = 9;
     
     // What data type is being sent to socket
         // Movement type
     static final int ROT = 10;
     static final int TRANS = 11;
     static final int ZTRANS = 12;
     static final int VELOCITY = 13;
     static final int MOVE_NODE = 14;
         // Mode
     static final int MANUAL = 20;
     static final int DRIVE = 21;
     static final int OLD_FLY = 23; 
     static final int AIRPLANE = 22;
         //Command
     static final int CONNECT = 30;
     static final int FLIP = 32;
     static final int FETCH = 34;
     static final int SELECTNODE = 35;
     static final int HIDE = 31;
     static final int SHOW = 33;
     
     // Booleans for checks
     boolean invert_pitch = false;
     boolean invert_roll = false;
     boolean toggle_pitch = false;
     boolean toggle_roll = false;
     
     boolean onNavigation = false;
     boolean onIp = false;
     boolean motionOn = true;
     boolean onNode = false;
     boolean onNodeMove = false;
     boolean onNodeType = false;
     
     // For Touch data 
 	Double[] coords = {0d, 0d};      //Only x and y 
 	Double[] z_coord = {0d};         //Handles z coord
 	double x = 0.0;
 	double y = 0.0;
 	boolean move = false;
 	boolean zoom = false;
 	double magnitude = 1d;
 	double new_mag = 1d;
 	View touchControll, speed;
     
     // For Sensor Values
     private SensorManager sense = null;
     float[] accelData = new float[3];
     float[] magnetData = new float[3];
     float[] rotationMatrix = new float[16];
     Double[] resultingAngles = {0.0, 0.0, 0.0};
     Double[] previousAngles = {0d, 0d, 0d};
     Double[] recalibration = {0.0, 0.0, 0.0};
     Double[] prepare = {0.0, 0.0, 0.0};
     double MIN_DIFF = 0.05d;
     float[] gravity = {0f, 0f, 0f};
     
     // Velocity
     Double[] velocity = {0d};
 	long timeStart;
 	TextView velText;
     
     // For Node Movement
     double height_adjust = 0d;
     double mag = 1d;
     double x_node = 0d;
     double y_node = 0d;
     Double[] adjustment = {0d, 0d, 0d};
     
     // Data inputs
       // Main Navigation Screen
     TextView sensorText; 
     TextView ipText; 
     TextView accelText;
     SharedPreferences textValues;
     SharedPreferences.Editor text_editor;
     int height;
     int width;
     float xdpi;
     float ydpi;
       // Ip Screen 
       // Main Node Screen -- Find Nodes
     Spinner nodeOptions;
     SharedPreferences nodesFound;
     ArrayAdapter<CharSequence> nodeAdapter;
     Map<String, String> nodeCollection;
     SharedPreferences.Editor node_editor;
     
     // For SharedPreferences
     public static final String PREF_IP = "IpPrefs";
     public static final String PREF_DATA = "DataPref";
     public static final String PREF_NODES = "NodesPref";
     final String IPVALUE = "IPVALUE";
     final String MODEVALUE = "MODEVALUE";
     final String VELVALUE = "VELVALUE";
     
     // For Log
     static String LOG_TAG = "Gamepad";
 
     
     /* 
      * Called on program create
      *   Establishes Landscape Orientation
      *   Calculates screen dimensions for later use
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
 	@Override 
     public void onCreate(Bundle savedInstanceState){
 		
 		socketOpen = false;
 		port = 8888;
 		nodeOn = new HashMap<String, Boolean>();
     	
     	super.onCreate(savedInstanceState); 
     	sense = (SensorManager)getSystemService(SENSOR_SERVICE);
     	
     	cave = CaveManager.getCaveManager().getCave(getIntent().getExtras().getString("CAVE"));
     	getWindow().setBackgroundDrawableResource(R.drawable.techback);
     	
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
         setContentView(R.layout.main); 
         
 		this.getActionBar().setHomeButtonEnabled(true);
         
         textValues = getSharedPreferences(PREF_DATA, 0);
         text_editor = textValues.edit();
         motionOn = true;
         
         // Calculates screen dimensions
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int statusHeight;   
         switch(metrics.densityDpi){
 	        case DisplayMetrics.DENSITY_HIGH:
 	        	statusHeight = 38; // HIGH DPI STATUS HEIGHT
 	        	break;
 	        case DisplayMetrics.DENSITY_LOW:
 	        	statusHeight = 19; // LOW DPI STATUS HEIGHT
 	   			break;
 	        case DisplayMetrics.DENSITY_MEDIUM:
 	       	default:
 	       		statusHeight = 25; // MEDIUM DPI STATUS HEIGHT
 	       		break;
         }
         ydpi = metrics.ydpi - statusHeight;
         height = (int) ((ydpi * getResources().getDisplayMetrics().density + 0.5f)/3f);
         xdpi = metrics.xdpi * getResources().getDisplayMetrics().density + 0.5f;
         width = (int) (xdpi/3f);
 
         
         
         AsyncTask.execute(new Runnable(){
 
 			@Override
 			public void run() {
 				openSocket();
 			}
         	
         });
         
         try {
         	Thread.sleep(500);
         	int failed = 0;
 			Log.d("Start", socketOpen + "");
         	
         	while(!socketOpen){
         		failed++;
         		if(failed < 5){
         			Log.d("Start", "Waiting for connection..." + socketOpen);
         			Toast.makeText(this, "Failed to connect, trying again...", Toast.LENGTH_SHORT).show();
         			Thread.sleep(500);
         		} else {
         			Toast.makeText(this, "Could not connect to host!", Toast.LENGTH_LONG).show();
         			return;
         		}
         	}
         	
         } catch(InterruptedException e){
         	e.printStackTrace();
         }
         
         onMainStart();
         
     }
 	
 	/*
 	 * Removes sensor listeners and clears text fields in SharedPreferences
 	 * @see android.app.Activity#onPause()
 	 */
     @Override
     protected void onPause(){
     	super.onPause();
     	sense.unregisterListener(this); 
     	closeSocket();
     	text_editor.clear();
     	text_editor.commit();
     }
     
     /*
      * Registers listener for acceleration and magnetic_field data
      * @see android.app.Activity#onResume()
      */
     @Override
     protected void onResume(){
     	super.onResume();
     	sense.registerListener(this, sense.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
     	sense.registerListener(this, sense.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
     	sense.registerListener(this, sense.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
     	onMainStart();
     }
     
     /*
      * From here you either select Navigation or Node
      */
     protected void onMainStart(){
     	Button navigation = (Button) findViewById(R.id.navigationButton);
     	Button nodeControl= (Button) findViewById(R.id.nodeButton);
     	 
     	navigation.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             	
             	onNode = false;
             	setContentView(R.layout.main_navigation);
             	onNavigationStart();
             	onNavigation = true;
             	invalidateOptionsMenu();
             }
         });
 	        
 	    nodeControl.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 onNavigation = false;
                 setContentView(R.layout.find_node);
                 onNodeMainStart();
                 node_editor.clear();
         		node_editor.commit();
         		nodeAdapter.clear();
         		onNode = true;
         		invalidateOptionsMenu();
             }
         });
     }
      
     /*
      * Sets up main navigation screen:
      *   Faster button -- increases velocity on touch
      *   Slower button -- decreases velocity on touch
      *   Stop button -- pauses all rotation movement and resets velocity to 0.
      *     Also recalibrates orientation such that current orientation is the standard orientation.
      */
     protected void onNavigationStart(){
     	try{
     		Log.d("Navigation", "Initialization");
     		LinearLayout layout = (LinearLayout) findViewById(R.id.navigationLayout);
 	        layout.setOnTouchListener((OnTouchListener) this);
 	        layout.setKeepScreenOn(true);
 	        
 	    	sensorText = (TextView) findViewById(R.id.sensorText);
 	    	ipText = (TextView) findViewById(R.id.ipText);
 	    	accelText = (TextView) findViewById(R.id.accelData);
 	    	velText = (TextView) findViewById(R.id.velocity);
 	    	touchControll = findViewById(R.id.touchControl);
 	    	speed = findViewById(R.id.speed);
 	    	speed.setBackgroundColor(Color.GRAY);
 			speed.setAlpha(.7f);
             
 	    	velText.setWidth(width);
             ipText.setWidth(width);
             sensorText.setWidth(width);
             accelText.setWidth(width);
             
 	        ipText.setText(socket.getInetAddress()
 	        		.getHostAddress());
 	        sensorText.setText(textValues.getString(MODEVALUE, "Mode: Select Mode"));
 	        velText.setText(textValues.getString(VELVALUE, "Velocity: 0"));
 	        
 	        Button flip = (Button) findViewById(R.id.flip);
 			flip.setOnClickListener(new OnClickListener(){
 				
 				@Override
 				public void onClick(View arg0) {
 					sendSocketCommand(FLIP, "Flip");
 				}
 	        	
 	        });
 	        
 	        touchControll.setOnTouchListener(this);
 	        
 	        speed.setOnTouchListener(new OnTouchListener(){
 	        	
 	        	private float old;
 
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					
 					float dy = 0;
 					
 					if(event.getAction() == MotionEvent.ACTION_UP){
 						dy = 0;
 						speed.setBackgroundColor(Color.GRAY);
 	        			speed.setAlpha(.7f);
 					}
 					else if(event.getAction() == MotionEvent.ACTION_DOWN){
 						speed.setAlpha(.5f);
 						old = event.getY();
 					}
 					else if(event.getAction() == MotionEvent.ACTION_MOVE)
 						dy = old - event.getY();
 	        		
 					dy = (dy / (speed.getHeight() / 2) * MAX_SPEED);
 					
 					if(dy > MAX_SPEED)
 						dy = MAX_SPEED;
 					
 					if(dy < -MAX_SPEED)
 						dy = -MAX_SPEED;
 	        		
 	        		velocity[0] = roundDecimal(dy);
 	        		
 	        		if(dy != 0)
 	        			speed.setAlpha((Math.abs(dy)/ MAX_SPEED));
 	        		
 	        		if(dy > 0)
 	        			speed.setBackgroundColor(Color.GREEN);
 	        		else if(dy < 0)
 	        			speed.setBackgroundColor(Color.RED);
 	        		
	        		Log.d("asd", "" + velocity[0]);
	        		
 	        		sendSocketDoubles(VELOCITY, velocity, 1, NAVI);
 	        		velText.setText("Velocity: " + velocity[0]);
 	        		
 					return true;
 				}
 	        	
 	        });
 	        
     	}
     	catch(Exception e){
     		Log.e(LOG_TAG, "Exception in NavigationStart: " + e.getMessage());
     		e.printStackTrace();
     	}
     }
    
     /* 
      * Main Node loaded
      *   find -- sends a request to find all AndroidTransform nodes. Calls getNodes() to return them.
      *   selectNode -- whichever node is selected in the nodeOptions spinner becomes the selected node to act upon
      *   hideNodes -- hides the selected node
      */
     @SuppressWarnings("unchecked")
 	protected void onNodeMainStart(){
     	try{
 	    	nodesFound = getSharedPreferences(PREF_NODES, 0);
 	        node_editor = nodesFound.edit();
 	       
 	        nodeOptions = (Spinner) findViewById(R.id.nodeOptions);
 	        Button find = (Button) findViewById(R.id.findNodesButton);
 	        Button selectNode = (Button) findViewById(R.id.selectNodeButton);
 	        final CheckBox hideNodes = (CheckBox) findViewById(R.id.hideNodeBox);
 	        hideNodes.setClickable(false);
 	        
 	        // Allows spinner to dynamically update ip addresses.
 	        CharSequence[] nodeArray = 
 	                getResources().getTextArray(R.array.nodes_array);
 	        List<CharSequence> nodeNames = new ArrayList<CharSequence>(Arrays.asList(nodeArray));
 	        nodeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, nodeNames);
 	        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	        nodeOptions.setAdapter(nodeAdapter);
 	        nodeOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 	        	
 	            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
 	            {
 	            	nodeName = parent.getItemAtPosition(pos).toString(); 
 	            }
 	            public void onNothingSelected(AdapterView<?> parent) {
 	              // Do nothing.
 	            }
 	        });
 
 	        find.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					node_editor.clear();
 	        		node_editor.commit();
 	        		nodeAdapter.clear();
 	        		nodeOptions.setAdapter(nodeAdapter);
 	        		if(!nodeOn.isEmpty()) nodeOn.clear();
 	        		hideNodes.setClickable(false);
 					getNodes();
 					
 				}
 			});
 	        
 	        selectNode.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if(nodeName != null){
 						sendSocketString(SELECTNODE, nodeName);
 						if(socketOpen) Toast.makeText(Gamepad.this, "Selecting " + nodeName, Toast.LENGTH_SHORT).show();
 						hideNodes.setClickable(true);
 						Boolean on = nodeOn.get(nodeName);
 						if(on){
 							hideNodes.setChecked(false);
 						}
 						else{
 							hideNodes.setChecked(true);
 						}
 					}
 					else{
 						Toast.makeText(Gamepad.this, "No node selected", Toast.LENGTH_SHORT).show();
 					}
 				}
 			});
 	       
 	        nodeCollection = (Map<String, String>) nodesFound.getAll();
 	        if(!nodeCollection.isEmpty()){
 	        	Iterator<String> over = nodeCollection.values().iterator();
 	        	while(over.hasNext()){
 	        		String temp = over.next();
 	        		nodeAdapter.add(temp);
 	        	}
 	        }
 	        
 	        hideNodes.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if(nodeName != null){
 						if(hideNodes.isChecked()){ 
 							sendSocketCommand(HIDE, "HideNode");
 							Toast.makeText(Gamepad.this, "Hiding " + nodeName, Toast.LENGTH_SHORT).show();
 							nodeOn.remove(nodeName);
 							nodeOn.put(nodeName, false);
 						}
 						else{
 							sendSocketCommand(SHOW, "ShowNode"); 
 							Toast.makeText(Gamepad.this, "Showing " + nodeName, Toast.LENGTH_SHORT).show();
 							nodeOn.remove(nodeName);
 							nodeOn.put(nodeName, true);
 						}
 					}
 					else{
 						Toast.makeText(Gamepad.this, "No node selected", Toast.LENGTH_SHORT).show();
 						if(hideNodes.isChecked()){ 
 							hideNodes.setChecked(false);
 						}
 						else{
 							hideNodes.setChecked(true);
 						}
 					}
 					
 				}
 			});
 	        
     	}
     	catch(Exception e){
     		Log.w(LOG_TAG, "Exception in IP Loader: " + e.getMessage());	
     		onIp = false;
     	}
     }
     
     /*
      * Moves the actual node
      *   selectAxisButton -- picks an axis to work on X, Y, Z Trans, X, Y, Z Rotation
      *   When you move, the x-axis is the magnitude (increases to the right) and the y-axis is the axis you're moving on.
      */
    	protected void onNodeMove(){
         LinearLayout layout = (LinearLayout) findViewById(R.id.moveNodeLayout); // main layout  
         
         Button selectAxisButton = (Button) findViewById(R.id.selectAxisButton);
         
         selectAxisButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final String[] axisOptions = {"X Trans", "Y Trans", "Z Trans", "X Rot", "Y Rot", "Z Rot"};
 				AlertDialog.Builder builder = new AlertDialog.Builder(Gamepad.this);
 				builder.setItems(axisOptions, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						axis = String.valueOf(which);
 				        Toast.makeText(Gamepad.this, axisOptions[which] + " selected.", Toast.LENGTH_SHORT).show();
 					}
 				});
 				builder.show();
 			}
 		});
         
         layout.setOnTouchListener(new OnTouchListener(){
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch(event.getAction() & MotionEvent.ACTION_MASK){
 				case MotionEvent.ACTION_DOWN:
 					height_adjust = event.getY();
 					break; 			
 				case MotionEvent.ACTION_MOVE:
 					mag = (event.getX())/xdpi;
 					adjustment[0] = roundDecimal(event.getY() - height_adjust);
 					adjustment[1] = roundDecimal(mag);
 					height_adjust = event.getY();
 					sendSocketDoubles(MOVE_NODE, adjustment, 2, NODE);
 					break;
 				}
 				
 			return true;
 			}
         	
         });
         
         layout.setKeepScreenOn(true);
    	}
    	
    	/*
    	 * Gets node names from CalVR (after find is selected in onNodeMainStart())
    	 */
    	public boolean getNodes(){
    		if (!socketOpen) {
 			if (onNode) Toast.makeText(Gamepad.this, "Not connected...", Toast.LENGTH_SHORT).show();
 			return false;
    		}	
     	try{
     		sendSocketCommand(FETCH, "Fetch Nodes");
     		
     		// First -- receive how many
     		// Second -- receive all strings
     		  
     		byte[] data = new byte[1024];
     		DatagramPacket get = new DatagramPacket(data, 1024);
     		receiveSocket(get);
     		int num = byteToInt(data);
     		
     		for(int i = 0; i< num; i++){
     			byte[] dataSize = new byte[Integer.SIZE];
     			DatagramPacket getSize = new DatagramPacket(dataSize, Integer.SIZE);
     			receiveSocket(getSize);
         		
         		byte[] dataName = new byte[byteToInt(dataSize)];
     			DatagramPacket getName = new DatagramPacket(dataName, byteToInt(dataSize));
     			receiveSocket(getName);
         		
         		String temp = new String(dataName);
 
         		node_editor.putString(temp, temp);
         		nodeAdapter.add(temp);
         		nodeOn.put(temp, true);
     		}
     		node_editor.commit();
     	}
     	catch (Exception ie){
         		Toast.makeText(Gamepad.this, "Exception in getting Nodes! " + ie.getMessage(), Toast.LENGTH_SHORT).show();   
         		Log.w(LOG_TAG, "Exception getNodes: " + ie.getMessage());
         		ie.printStackTrace();
         }
 		return true;
     	
     }
    	
    	public void receiveSocket(DatagramPacket pack) throws InterruptedException, ExecutionException, IOException{
    		IOException e = new AsyncTask<DatagramPacket, Void, IOException>(){
 
 			@Override
 			protected IOException doInBackground(DatagramPacket... params) {
 				for(DatagramPacket pack : params)
 					try {
 						socket.receive(pack);
 					} catch (IOException e) {
 						return e;
 					}
 				return null;
 			}
 			
 		}.execute(pack).get();
 		
 		if(e != null)
 			throw e;
    	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     } 
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu){
     	 MenuInflater inflater = getMenuInflater();
     	 menu.clear();
     	if(onNavigation) {
         	inflater.inflate(R.menu.navi_menu, menu);
         }
         else if(onNode) {
         	inflater.inflate(R.menu.node_menu, menu);
         }
         else{
         	inflater.inflate(R.menu.main_menu, menu);
         }
     	return true;
     }
     
     /*
      * Handles when menu and submenu options are selected:
      *   subFly -- enters fly mode -- NAVI
      *   subNewFly -- enters new fly mode -- NAVI
      *   subDrive -- enters Drive mode -- NAVI
      *   subMove -- enters Rotate world mode -- NAVI
      *   address -- toggles on/off ip selection screen -- NAVI/NODE
      *   invertPitch -- inverts pitch -- NAVI
      *   invertRoll -- inverts roll -- NAVI
      *   togglePitch -- toggles pitch -- NAVI
      *   toggleRoll -- toggles roll -- NAVI
      *   moveNodesMenu -- goes to move node menu screen -- NODE
      *   findNodesMenu -- goes to find node menu screen -- NODE
      *   returnMain -- returns to Main screen -- NAVI/NODE
      *        This is where you pick either Navigation page or Node page
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override   
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
        // NAVIGATION 
          //MOVEMENT TYPE
         	case android.R.id.home:
         		// app icon in action bar clicked; go home
         		Intent intent = new Intent(this, MainActivity.class);
         		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         		startActivity(intent);
         		finish();
         		return true;
 	        case R.id.subManual:
 	        	sendSocketCommand(MANUAL, "Manual");
 	        	break; 
 	        case R.id.subAirplane:
 	        	sendSocketCommand(AIRPLANE, "Aiplane");
 	        	break; 
 	        case R.id.subDrive:
 	        	sendSocketCommand(DRIVE, "Drive");
 	        	break;
 	        case R.id.subOldFly:
 	        	sendSocketCommand(OLD_FLY, "Old Fly");
 	        	break;
 	     // OPTIONS FOR PITCH/ROLL
 	        case R.id.invertPitch:
 	        	if(invert_pitch){
 	        		invert_pitch = false;
 	        		Toast.makeText(Gamepad.this, "Reverting Pitch", Toast.LENGTH_SHORT).show();
 	        	}
 	        	else{
 	        		invert_pitch = true;
 	        		Toast.makeText(Gamepad.this, "Inverting Pitch", Toast.LENGTH_SHORT).show();
 	        	}
 	        	break;
 	        case R.id.invertRoll:
 	        	if(invert_roll){
 	        		invert_roll = false;
 	        		Toast.makeText(Gamepad.this, "Reverting Roll", Toast.LENGTH_SHORT).show();
 	        	}
 	        	else{
 	        		invert_roll = true;
 	        		Toast.makeText(Gamepad.this, "Inverting Roll", Toast.LENGTH_SHORT).show();
 	        	}
 	        	break;
 	        case R.id.togglePitch:
 	        	if(toggle_pitch){
 	        		toggle_pitch = false;
 	        		Toast.makeText(Gamepad.this, "Pitch On", Toast.LENGTH_SHORT).show();
 	        	}
 	        	else{
 	        		toggle_pitch = true;
 	        		Toast.makeText(Gamepad.this, "Pitch Off", Toast.LENGTH_SHORT).show();
 	        	}
 	        	break;
 	        case R.id.toggleRoll:
 	        	if(toggle_roll){
 	        		toggle_roll = false;
 	        		Toast.makeText(Gamepad.this, "Roll On", Toast.LENGTH_SHORT).show();
 	        	}
 	        	else{
 	        		toggle_roll = true;
 	        		Toast.makeText(Gamepad.this, "Roll Off", Toast.LENGTH_SHORT).show();
 	        	}
 	        	break;
 	   // NODE CONTROL
 	        // MOVE NODE
 	        case R.id.moveNodesMenu:
 	        	if (!onNodeMove){
 	        		setContentView(R.layout.move_node); 
 	        		onNodeMove = true;
 	        		onNodeMove();	
 	        	}
 	        	else{
 	        		onNodeMove = false;
 	        		setContentView(R.layout.find_node); 
 	        		onNodeMainStart();
 	        	}
 	            break;
 	      // FIND NODES (MAIN PAGE)
 	        case R.id.findNodesMenu:
 	        	onNodeMove = false;
         		setContentView(R.layout.find_node); 
         		onNodeMainStart();
         		break;
 	  
 	   // RETURNS TO MAIN SCREEN
 	        case R.id.returnMain:
 	        	setContentView(R.layout.main); 
         		onMainStart();
         		onIp = false;
         		onNavigation = false;
         		onNode = false;
         		onNodeMove = false;
         		break;
         } 
         return true;
     }
    
     /*
      * Sets up socket using _ip given by spinner (MyOnItemSelectedListener and ipValues)
      */
     public void openSocket(){
     	Log.d("SocketOpen", "Connecting...");
     	if (socketOpen) return;
 	    try{
 	    	serverAddr = InetAddress.getByName(cave.getAddress());   	
 	    	socket = new DatagramSocket();
 	    	socket.connect(new InetSocketAddress(serverAddr, port));
 	    	if(!socket.isConnected())
 	    		throw new Exception("Couldn't open socket!");
 	    	socketOpen = true;
 	    	socket.setSoTimeout(1000);
 	    	Log.d("SocketOpen", "Socket Opened!");
 	    	return;
 	    }catch (IOException ie){ 
 	    	Log.w(LOG_TAG, "IOException Opening: " + ie.getMessage());
 	    	Toast.makeText(Gamepad.this, "IOException Opening!: " + ie.getMessage() , Toast.LENGTH_SHORT).show();   
 	    }catch (Exception e){
 	    	Log.w(LOG_TAG, "Exception: " + e.getMessage());
 	    	e.printStackTrace();
 	    }
     	Log.d("SocketOpen", "Failed to open!");
     }
      
     // Says socket is closed. 
     public void closeSocket(){
     	socketOpen = false;
     } 
     
     /**
      * Sends menu command to socket. 
      * Receives confirmation messages and then updates layout accordingly (updateLayout(int, int))
      */
     public void sendSocketCommand(int tag, String textStr){
     	if (!socketOpen) {
     		if (onNode) Toast.makeText(Gamepad.this, "Not connected...", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	try{	
     		byte[] bytes = (String.valueOf(COMMAND) + String.valueOf(tag) + " ").getBytes();
 	    	sendPacket(new DatagramPacket(bytes, bytes.length, serverAddr, port)); 
     		
     		// Gets tag back confirming message sent
     		byte[] data = new byte[Integer.SIZE];
     		DatagramPacket get = new DatagramPacket(data, Integer.SIZE);
     		
     		receiveSocket(get);
     		 
     		int value = byteToInt(data);
     		updateLayout(value);
     	}
     	catch (Exception ie){
     		if (tag == CONNECT) Toast.makeText(Gamepad.this, "Cannot Connect. Please reconnect to proper IP.", Toast.LENGTH_SHORT).show();   
     		else{
         		Toast.makeText(Gamepad.this, "Exception in Sending! " + ie.getMessage(), Toast.LENGTH_SHORT).show();   
         		Log.w(LOG_TAG, "Exception Sending: " + ie.getMessage());
     		}
         } 
     }
     
     /**
      * Sends a double[] as a byte[] to server
      * Used to send touch and rotation data
      *
      *   Double[tag, size of value, value, size of value 2, value 2, ...]
      */
     public void sendSocketDoubles(int tag, Double[] value, int arrayLength, int type){  	
     	if (!socketOpen) {
     		Toast.makeText(Gamepad.this, "Not connected...", Toast.LENGTH_SHORT).show();
     		return;
     	}
     	
     	String send = String.valueOf(type) + String.valueOf(tag);
 	    for(int i = 0; i< arrayLength; i++){
 	    	send += (" " + String.valueOf(value[i]));
 	    }
 	    send += " ";
 	    if(tag == MOVE_NODE){
 	    	send += axis + " ";
 	    }
 	    byte[] bytes = send.getBytes();
 		sendPacket(new DatagramPacket(bytes, bytes.length, serverAddr, port));
     }
     
     /*
      * Sends a double[] as a byte[] to server
      * Used to send touch and rotation data
      *
      *   Double[tag, size of value, value, size of value 2, value 2, ...]
      */
     public void sendSocketString(int tag, String str){  	
     	if (!socketOpen) {
     		if (onNode) Toast.makeText(Gamepad.this, "Not connected...", Toast.LENGTH_SHORT).show();
     		return;
     	} 
     	
     	String send = String.valueOf(NODE) + String.valueOf(tag) + " " + str + " ";
 	    byte[] bytes = new byte[send.getBytes().length];
 	    bytes = send.getBytes();
 		sendPacket(new DatagramPacket(bytes, bytes.length, serverAddr, port));
 		
     }
     
     protected void sendPacket(final DatagramPacket p){
     	AsyncTask.execute(new Runnable(){
 
 			@Override
 			public void run() {
 				try {
 					socket.send(p);
 				} catch (IOException e) {
 					
 					Toast.makeText(Gamepad.this, "IOException in Sending! " + e.getMessage(), Toast.LENGTH_SHORT).show();   
 	        		Log.w(LOG_TAG, "IOException Sending: " + e.getMessage());
 					e.printStackTrace();
 				}
 			}
     		
     	});
     }
     
 
 	@Override 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Not used...
 	}
  
 	/**
 	 * Processes sensor changes
 	 *    Accelerometer  -- passes through low-pass filter to reduce error
 	 *    Magnetic Field
 	 * Uses data to calculate orientation (rotationMatrix)
 	 * Compares new orientation data with previous data to check threshold (MIN_DIFF)
 	 * Also checks to see if data has been toggle/inverted and performs appropriately
 	 * Finally, sends data to sendSocketDouble to pass to server
 	 * 
 	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent s) {
 		if(!onNavigation) return;
 		if(!motionOn) return;
 		final float alpha = .5f;
 		System.arraycopy(resultingAngles, 0, previousAngles, 0, 3);
 		
 		synchronized(this){
     		int type = s.sensor.getType();
     		
     		if (type == Sensor.TYPE_ACCELEROMETER){			
     			// Low-pass filter
     			accelData[0] = gravity[0] + alpha*(s.values[0] - gravity[0]);
     			accelData[1] = gravity[1] + alpha*(s.values[1] - gravity[1]);
     			accelData[2] = gravity[2] + alpha*(s.values[2] - gravity[2]);
     			gravity[0] = accelData[0];
     			gravity[1] = accelData[1];
     			gravity[2] = accelData[2];
     		}
     		if(type == Sensor.TYPE_MAGNETIC_FIELD){
     			magnetData = s.values.clone();
     		}
     	}
 
 		
 		// Gets a rotation matrix to calculate angles
 		SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
 		float[] anglesInRadians = new float[3];
 		// Uses orientation to get angles
 		SensorManager.getOrientation(rotationMatrix, anglesInRadians);
 		
		//TODO: Temporary fix for inversion
		float tmp = -anglesInRadians[1];
		anglesInRadians[1] = -anglesInRadians[2];
		anglesInRadians[2] = tmp;
		
 		// Checks if angles have changed enough
 		boolean run = false;
 		
 		// Gets difference between result and previous angles for limiting buffer
 		for (int i = 1; i < 3; i++){
			resultingAngles[i] = roundDecimal((double)anglesInRadians[i]);
 			prepare[i] = resultingAngles[i]; 
 			resultingAngles[i] = roundDecimal(resultingAngles[i] - recalibration[i]);
 			if(i == 1){
 				if(invert_roll) resultingAngles[i] *= -1;
 				if(toggle_roll) resultingAngles[i] *= 0;
 			} 
 			else if(i == 2){
 				if(invert_pitch) resultingAngles[i] *= -1;
 				if(toggle_pitch) resultingAngles[i] *= 0;
 			}
 			
 			if(Math.abs(resultingAngles[i] - previousAngles[i]) < MIN_DIFF ){
 					resultingAngles[i] = 0.0;  
 			}	
 			else{
 				run = true;
 			}
 		}
 		
 		//anglesInRadians is +/-PI while orient is 0-2PI so this fixes that
 		if(anglesInRadians[0] < 0)
 			anglesInRadians[0] = (float) (2*Math.PI + anglesInRadians[0]);
 		
 		
 		// Gets the orientation angle (Y-axis rotation) and compares it to the previous one
 		resultingAngles[0] = roundDecimal(anglesInRadians[0]);
 		if (Math.abs(resultingAngles[0] - previousAngles[0])< MIN_DIFF){
 			resultingAngles[0] = previousAngles[0];
 		}
 		else{
 			run = true;
 		}
 		
 		
 		if (run){
 			accelText.setText("Accel: " + resultingAngles[0] + ", " + resultingAngles[1] + ", " + resultingAngles[2]);
 			sendSocketDoubles(ROT, resultingAngles, 3, NAVI);
 		}
 	}
 	 
 	/**
 	 * Processes touch data:
 	 *   down -- starts move procedure
 	 *   pointer down (for 2+ fingers) -- starts zoom procedure
 	 *   pointer up 
 	 *   up
 	 *   move -- if move procedure, calculates distance moved
 	 *        -- if zoom, calculates distance zoomed
 	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onTouch(View v, MotionEvent e){
 		if (!onNavigation) return false;
 		if (!motionOn) return false;
 		
 		switch(e.getAction() & MotionEvent.ACTION_MASK){
 			case MotionEvent.ACTION_DOWN:
 				x = e.getX();
 				y = e.getY();
 				move = true; 
 				break; 			
 			case MotionEvent.ACTION_POINTER_DOWN:
 				// If magnitude too small (fingers resting) won't run
 				if ((magnitude = distance(e)) > 10f){
 					zoom = true; 
 					move = false; 
 				}
 				break;
 			case MotionEvent.ACTION_POINTER_UP:
 				zoom = false;
 				break;
 	 		case MotionEvent.ACTION_UP:
 				move = false;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				if (move){
 					coords[0] = roundDecimal(e.getX() - x);
 					coords[1] = roundDecimal(e.getY() - y);
 					
 					x = e.getX();
 					y = e.getY();
 					
 					sendSocketDoubles(TRANS, coords, 2, NAVI);
 					break;
 				}
 				if (zoom){
 					new_mag = distance(e);
 					// If new_mag too small, prevents zoom. 
 					if(new_mag > 10f && (Math.abs(new_mag - magnitude) > 15f)){
 						// Calculates the distance moved by one finger (assumes a pinching movement is used)
 						// If z < 0, then the object moves farther away
 						z_coord[0] = roundDecimal((new_mag- magnitude)/2);
 						//Trying to make it less drastic
 						z_coord[0] /= 2;
 						sendSocketDoubles(ZTRANS, z_coord, 1, NAVI);
 						break;
 					}
 				}
 		}
 		return true;
 	} 
 	
 	/*
 	 * Updates layout according to tag
 	 * Tags are listed at top of page 
 	 * TYPE is tens digit, TAG is ones digit
 	 */
     public void updateLayout(int tag){
     	switch(tag){
         case MANUAL:
 			sensorText.setText("Mode: Manual");
 			text_editor.putString(MODEVALUE, "Mode: Manual");
 			text_editor.commit();
 			break;
         case DRIVE:
 			sensorText.setText("Mode: Drive");
 			text_editor.putString(MODEVALUE, "Mode: Drive");
 			text_editor.commit();
 			 break;
         case OLD_FLY:
 			sensorText.setText("Mode: Old Fly");
 			text_editor.putString(MODEVALUE, "Mode: Old Fly");
 			text_editor.commit();
 			break;
         case AIRPLANE:
 			sensorText.setText("Mode: Airplane");
 			text_editor.putString(MODEVALUE, "Mode: Airplane");
 			text_editor.commit();
 			break;
         case CONNECT:
 	        Toast.makeText(Gamepad.this, "Connected!!", Toast.LENGTH_SHORT).show();
 	        onIp = false;
 		    text_editor.putString(IPVALUE, "ip: " + cave.getAddress());
 		    text_editor.commit();
 	        if(onNavigation){
 	        	setContentView(R.layout.main_navigation); 
 	        	ipText.setText("ip: " + cave.getAddress());
 	        	onNavigationStart();
 	        }
 	        else if (onNode){
 	        	setContentView(R.layout.find_node);
 	        	nodeAdapter.clear();
 	        	node_editor.clear();
         		node_editor.commit();
 	        	onNodeMainStart();
 	        }
 		    break;
         case FLIP:  		
         	Toast.makeText(Gamepad.this, "Rotation Command received", Toast.LENGTH_SHORT).show();
 	        break; 
     	}
  }
 	
     /*
      * Calculates distance between two fingers (for zoom)
      */
 	private double distance(MotionEvent e) {
 		double x = e.getX(0) - e.getX(1);
 		double y = e.getY(0) - e.getY(1);
 		return Math.sqrt(x*x + y*y);
 	}
 	
 	/*
 	 * Converts int to byte[]
 	 */
 	public final byte[] intToByte(int value) {
 	    return new byte[] {
 	    		(byte)value,
 	            (byte)(value >>> 8),
 	            (byte)(value >>> 16), 
 	            (byte)(value >>> 24),};
 	}
 	
 	/*
 	 * Converts byte[] to int
 	 */
 	public static int byteToInt( byte[] bytes ) {
 	    int result = 0;
 	    for (int i=0; i<4; i++) {
 	        //result = (result << 8) + (bytes[i] & 0xff);
 	        result += (bytes[i] & 0xff) << (8 * i);
 	    }
 	    return result;
 	  }
 
 	/*
 	 * Rounds decimal to 4 places.
 	 */
 	double roundDecimal(double d) {
     	DecimalFormat dForm = new DecimalFormat("#.####");
     	return Double.valueOf(dForm.format(d));
 	}
 	
 	void recalibrate(){
 		System.arraycopy(prepare, 0, recalibration, 0, 3);
 	}
 }
 
