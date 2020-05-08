 package derricp1.apps.navi;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.math.*;
 import android.hardware.*;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.graphics.*;
 import android.graphics.Bitmap.Config;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.view.*;
 import android.media.*;
 import android.hardware.*;
 
 public class LoadActivity extends Activity implements SensorEventListener {
 
 	public final int LEVELS = 100; //Scaling factor for strength
 	public final int SIGNALS = 10; //number of signals used for the comparison
 	
 	public final int FLOORWIDTH = 190; //Floor height/width in feet
 	public final int FLOORHEIGHT = 124;
 	
 	Context now = this;
 	
	boolean debug = false;
 	
 	String estring = "";
 	int enumb = 0;
 	public int target = 0;
 	
 	//(0,0) is in top left corner
 	
 	public String[] ids; //ids of access points (rssids) we are using
 	
 	public int mapsize = 5; //How many nodes, read in from file (defaults now);
 	public double[] nodex;
 	public double[] nodey;
 
 	public int[][] nodess;
 			
 	public String[] rssids; //A copy of ids right now - when we cover multiple floor we'll need to expand this into every possible one we'd need to compare
 	
 	public double[] calcs; //calculated strength difference
 	
     private ImageView imageView;
     private Paint myPaint = new Paint();
     private Paint BLUE = new Paint();
     private Paint GREEN = new Paint();
     
     private boolean vare = true;
     private boolean cancelled = false;
     private boolean butcan = false;
     private boolean notav = false;
 
     //accelerometer stuff
     public int holdcycles = 0;
 	public float NOISE = (float) 0.3;
 	public float alpha = (float) 0.8;
 
 	private boolean mInitialized = false;
 	
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	
 	private float mLastX = 0, mLastY = 0, mLastZ = 0;
 	private float gravity[];
 	private float rx = 0, ry = 0, rz = 0;
 	private float lockedx = 0;
 	
 	private boolean dleft = false;
 	private boolean dright = false;
 	private boolean dturn = false;
 	private boolean willturn = false;
 	
 	//all in below:
 	//may need to move to async
 	private int lastnode = -1;
 	private int currnode = -1;
 	private int nextnode = -1;
 	
 	private boolean reset = false;
 	private int[] resetarr = new int[4];
 	
 	//private int oldo = 0;
 	
 	int[] currsig = new int[SIGNALS];
 	int tests = 333;
 	
 	int stickiness = 2;
 	int sticknode = -1; //node that's trying to be moved to
 	int stucknode = -1; //node we are "stuck" at
 	
     Bitmap bitmap, newbitmap;
     //Determine scaling factor
     int imageViewHeight;
     float factor;
     
     int nh;
     int nw;
 
     //Create another bitmap to draw on
     Bitmap tempBitmap;
     Canvas tempCanvas;
     
     //Turn the canvas back to a new bitmap
     Bitmap linemap;	
     
     //detect direction of turn
     public int curror;
     public Configuration config;
 	public String LASTORIENT;
 	public String LASTREADING;
 	public boolean orleft;
 	public boolean orright;
 	public int rotation = 0;
 	
 	//start loc
 	int starget = -1;
 	
     //----------------------------------------------------------
     
     @SuppressLint("NewApi")
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);  	
         
         //More sensor stuff
 		mInitialized = false;
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 		
 		gravity = new float[3];
 		for (int i=0; i<3; i++)
 			gravity[i] = (float) 9.8;
 		//Return to normal
         
 		//check out orientation
 		Configuration config = getResources().getConfiguration();
 		curror = config.orientation; //1 for port, 2 for landscape
 		rotation = getWindowManager().getDefaultDisplay().getRotation();
 
         setContentView(R.layout.activity_load);    
         
         this.imageView = (ImageView)findViewById(R.id.scroller); //Read in the view that lets us scroll the map
 
         //sound
         int S1 = 1;
         int S2 = 2;
         int S3 = 3;
         int S4 = 4;
         int S5 = 5;
         final SoundPool pool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
         final HashMap<Integer, Integer> soundsMap = new HashMap<Integer, Integer>();
         soundsMap.put(S1, pool.load(this, R.raw.left, 1));
         soundsMap.put(S2, pool.load(this, R.raw.right, 1));
         soundsMap.put(S3, pool.load(this, R.raw.s3, 1));
         soundsMap.put(S4, pool.load(this, R.raw.s4, 1));
         soundsMap.put(S5, pool.load(this, R.raw.s5, 1));
 
         // Get the message from the intent
         Intent intent = getIntent();
         target = intent.getIntExtra(Main.EXTRA_MESSAGE, 0); //Will never need the default or it would not even get here. 0 is a choice though
         starget = intent.getIntExtra(Main.START, -1);
         final boolean ztog = intent.getBooleanExtra(Main.ZOOM_LEVEL, false);
         final boolean voice = intent.getBooleanExtra(Main.VOICES, false);
         final boolean words = intent.getBooleanExtra(Main.WORDS, false);
         
 		WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		boolean wasEnabled = myWifiManager.isWifiEnabled();
 		
 		if (wasEnabled == false) {
 			Toast.makeText(this, "Wifi Disabled. Please enable Wifi and try again.", Toast.LENGTH_SHORT).show();
 			finish();
 			vare = false;
 		}
 		
 		ids = new String[SIGNALS]; //The RSSIDs we want
 		rssids = new String[SIGNALS]; //Need to expand for more floors
 		
 		String[] s1 = new String[SIGNALS];
 		String[] s2 = new String[SIGNALS];
 		String[] s3 = new String[SIGNALS];
 		int[] ticker = new int[3];
 		
 		s1[0] = "d8:c7:c8:6e:81:a9";
 		s1[1] = "d8:c7:c8:ab:2e:e8";
 		s1[2] = "d8:c7:c8:6e:81:a8";
 		s1[3] = "d8:c7:c8:ab:21:50";
 		s1[4] = "d8:c7:c8:6e:81:a8";
 		s1[5] = "d8:c7:c8:ab:2e:e9";
 		s1[6] = "d8:c7:c8:6e:7f:a9";
 		s1[7] = "d8:c7:c8:ab:21:52";
 		s1[8] = "d8:c7:c8:ab:2e:e0";
 		s1[9] = "d8:c7:c8:ab:21:58";
 		
 		s2[0] = "d8:c7:c8:ab:29:82";
 		s2[1] = "d8:c7:c8:ab:29:81";
 		s2[2] = "d8:c7:c8:ab:29:f1";
 		s2[3] = "d8:c7:c8:ab:2c:18";
 		s2[4] = "d8:c7:c8:ab:29:b8";
 		s2[5] = "d8:c7:c8:ab:2c:19";
 		s2[6] = "d8:c7:c8:ab:2f:80";
 		s2[7] = "d8:c7:c8:ab:26:d8";
 		s2[8] = "d8:c7:c8:ab:2f:81";
 		s2[9] = "d8:c7:c8:ab:26:d1";
 		
 		s3[0] = "d8:c7:c8:ab:29:ea";
 		s3[1] = "d8:c7:c8:ab:23:0a";
 		s3[2] = "d8:c7:c8:ab:2e:99";
 		s3[3] = "d8:c7:c8:ab:2e:d9";
 		s3[4] = "d8:c7:c8:ab:2e:91";
 		s3[5] = "d8:c7:c8:ab:29:e8";
 		s3[6] = "d8:c7:c8:ab:2a:00";
 		s3[7] = "d8:c7:c8:ab:2c:2a";
 		s3[8] = "d8:c7:c8:ab:2a:08";
 		s3[9] = "d8:c7:c8:ab:2c:50";
 		
 		//0-233: floor 2
 		//234-383: floor 3
 		//384+: floor 4
 		
 		int targetfloor = 3;
 		if (target < 234) {
 			targetfloor = 2;
 		}
 		if (target > 383) {
 			targetfloor = 4;
 		}
 		int bestmatch = 4;
 		
 		try {
 			//check to determine floor
 			if (myWifiManager.isWifiEnabled()){ //Recheck WiFi availability, safety measure
 				if(myWifiManager.startScan()){ //Can we start?
 					// List available APs
 					List<ScanResult> scans = myWifiManager.getScanResults();
 					if (scans != null && !scans.isEmpty()){
 						for (ScanResult scan : scans) {
 							
 							for(int i=0; i<SIGNALS; i++) {
 								if (s1[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
 									ticker[0] = ticker[0] + 1;
 								}
 								if (s2[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
 									ticker[1] = ticker[1] + 1;
 								}
 								if (s3[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
 									ticker[2] = ticker[2] + 1;
 								}
 							}
 							
 						}
 					}
 				}				
 			}
 			 
 			if (ticker[targetfloor-2] >= 5) {
 				bestmatch = targetfloor;
 			}
 			else {
 				if ((ticker[0] > ticker[1] && ticker[0] > ticker[2]) || (ticker[0] >= ticker[1] && ticker[0] >= ticker[2] && targetfloor == 2)) {
 					bestmatch = 2;
 				}
 				if ((ticker[1] > ticker[0] && ticker[1] > ticker[2]) || (ticker[1] >= ticker[0] && ticker[1] >= ticker[2] && targetfloor == 3)) {
 					bestmatch = 3;
 				}
 			}
 			
 			switch (bestmatch) {
 			
 			case 2:
 				for (int k=0; k<SIGNALS; k++) {
 					ids[k] = s1[k];
 				}
 				break;
 			case 3:
 				for (int k=0; k<SIGNALS; k++) {
 					ids[k] = s2[k];
 				}
 				break;			
 			default:
 				for (int k=0; k<SIGNALS; k++) {
 					ids[k] = s3[k];
 				}
 				break;
 			
 			}
 		}
 		catch (Exception e) {
 			vare = false;
 			estring = e.toString();
 		}
 		
 		final int floor = bestmatch;
 		
 		final int thisscan[] = new int[SIGNALS];
 		
 		if (debug == false) {
 			if ((vare == true && (ticker[0] == 0 && ticker[1] == 0 && ticker[2] == 0)) || (bestmatch != targetfloor)) {
 				String failstring = "Book at floor " + targetfloor + ".  Please retry there.";
 				Toast.makeText(this, failstring, Toast.LENGTH_LONG).show();
 				vare = false;
 			}
 		}
 		
 		if (vare == false) {
 			finish();
 			if (estring != "")
 				Toast.makeText(this, "Sorry, an error has occurred.", Toast.LENGTH_LONG).show();
 		}
 		
 		final int finalmatch = bestmatch;
 		final int targetnode = target;
 		final boolean isvalid = vare;
 			
 		//Begin loop here
 		
 		AsyncTask<Void, Bitmap, Void> math = new AsyncTask<Void, Bitmap, Void>(){
 			
 			int messagetype = 0;
 			boolean reminder = false;
 			boolean success = false;
 			int messageticker = 0;
 
 			@Override
 			protected Void doInBackground(Void... arg0) {
 				
 				WifiManager myWifiManager2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 				boolean wasEnabled2 = myWifiManager2.isWifiEnabled();
 				
 				boolean canspeak = true;
 				
 				currsig = new int[SIGNALS];
 				tests = 333;
 				
 				stickiness = 2;
 				sticknode = -1; //node that's trying to be moved to
 				stucknode = -1; //node we are "stuck" at
 				
 				//obstacles
 				int obs = 5;
 				double[] ox;
 				double[] oy;
 				double[] or;
 				int[] oends = new int[3]; //where the points at each end
 				
 				int[] fstarts = new int[3]; //where the floor node ranges lie.
 				int[] fends = new int[3]; //where the floor node ranges lie.
 				
 				//0-233: floor 2
 				//234-383: floor 3
 				//384+: floor 4
 				fstarts[0] = 0;
 				fends[0] = 233;
 				fstarts[1] = 234;
 				fends[1] = 383;
 				fstarts[2] = 384;
 				fends[2] = 454;
 				
 				int countdown = 240*15;
 				
 		        //Read in absolute coordinates of nodes
 				InputStream is = getResources().openRawResource(R.raw.nodelocs);
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				
 				try {
 					String str = br.readLine(); //Reads the number of nodes - should be the same as before
 					mapsize = Integer.parseInt(str);
 					nodex = new double[mapsize];
 					nodey = new double[mapsize];
 					nodess = new int[SIGNALS][mapsize];
 					
 					//Sets up arrays
 					
 					str = br.readLine(); //Discards hash
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here
 				}
 				
 				//read in obstacles
 				InputStream isb = getResources().openRawResource(R.raw.obs);
 				InputStreamReader isrb = new InputStreamReader(isb);
 				BufferedReader brb = new BufferedReader(isrb);					
 				
 				try {
 					String str = brb.readLine(); //Reads the number of obstacles and the end index per floor
 					obs = Integer.parseInt(str);
 					str = brb.readLine();
 					oends[0] = Integer.parseInt(str);
 					str = brb.readLine();
 					oends[1] = Integer.parseInt(str);
 					oends[2] = obs-1;
 					str = brb.readLine(); //Discards hash
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here
 				}
 				
 				ox = new double[obs];
 				oy = new double[obs];
 				or = new double[obs];
 				
 				try { //Read in strength and location data
 					for (int i=0; i< mapsize; i++) {
 						String str = br.readLine(); //Reads in data
 						nodex[i] = Double.parseDouble(str);
 						str = br.readLine(); //Reads in data
 						nodey[i] = Double.parseDouble(str);
 						
 						for (int j=0; j<SIGNALS; j++) {
 							str = br.readLine();
 							nodess[j][i] = Integer.parseInt(str);							
 						}
 						
 						str = br.readLine(); //Discards hash
 		
 					}
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here either
 				}
 				
 				try { //Read in location data for obstacles
 					for (int i=0; i< obs; i++) {
 						String str = brb.readLine(); //Reads in data
 						ox[i] = Double.parseDouble(str);
 						str = brb.readLine(); //Reads in data
 						oy[i] = Double.parseDouble(str);
 						str = brb.readLine(); //Reads in data
 						or[i] = Double.parseDouble(str);
 						
 						str = brb.readLine(); //Discards hash
 		
 					}
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here either
 				}		        
 				
 				int startnode = 0; //need to generate
 		        double calcs[] = new double[mapsize];
 		        
 		        boolean newstep = false; //did we change positions?
 				
 				//Let's try to get the info for where we are!
 				for(int i=0; i<SIGNALS; i++)
 					currsig[i] = 0;	
 				
 				boolean firstrun = true;
 				
 				while (success == false && isvalid == true && countdown > 0 && cancelled == false) {
 
 					try {
 						
 						rotation = getWindowManager().getDefaultDisplay().getRotation();
 						
 						//only matters for messages, and will be deleted
 						dleft = false;
 						dright = false;
 						dturn = false;
 						
 						if (holdcycles > 0)
 							holdcycles--;
 						
 						boolean foundpoint = false;
 						int maxmistakes = 2; //may change this back
 						int trials = 0;
 						messageticker--;
 						
 						int[] numneighbors = new int[mapsize]; //used later, but needs to be kept in scope
 						
 						while (foundpoint == false && maxmistakes < 5) {
 							
 							for(int i=0; i<SIGNALS; i++)
 								thisscan[i] = -1;
 					
 							if (wasEnabled2 == true){ //Recheck WiFi availability, safety measure
 								for(int z=0; z<tests; z++) {
 									
 									for(int i=0; i<SIGNALS; i++)
 										thisscan[i] = -1;
 									
 									if(myWifiManager2.startScan()){ //Can we start?
 										// List available APs
 										List<ScanResult> scans = myWifiManager2.getScanResults();
 										if (scans != null && !scans.isEmpty()){
 											for (ScanResult scan : scans) {
 												int found = -1;
 												
 												for(int i=0; i<SIGNALS; i++) {
 													if (ids[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
 														found = i;
 													}
 												}
 												
 												if (found > -1) {
 													thisscan[found] = WifiManager.calculateSignalLevel(scan.level, LEVELS);
 												}
 												
 											}
 										}
 									}
 									
 									for(int i=0; i<SIGNALS; i++)
 										currsig[i] = currsig[i] + thisscan[i];
 									
 								}
 							}
 		
 							for(int q=0; q<SIGNALS; q++) {
 								currsig[q] = (int) Math.ceil(currsig[q]/tests);
 							}					
 							
 							//Location determination starts here...
 							//Let's derive a start node, now that all of the nodes have been read in.
 					
 					        startnode = 0; //need to generate
 					        calcs = new double[mapsize];
 					        
 					        newstep = false; //did we change positions?
 					        
 					        boolean isthere[][] = new boolean[SIGNALS][mapsize];
 					        boolean isus[] = new boolean[SIGNALS];
 					        
 					        for (int i=0; i<SIGNALS; i++) {
 					        	if (currsig[i] > -1)
 					        		isus[i] = true;
 					        	else
 					        		isus[i] = false;
 					        }
 					        
 					        for (int i=0; i<SIGNALS; i++) {
 						        for (int j=0; j<mapsize; j++) {
 						        	if (nodess[i][j] > -1)
 						        		isthere[i][j] = true;
 						        	else
 						        		isthere[i][j] = false;
 						        }			        	
 					        }
 					        
 					        //
 					        //
 					        //get neighbors ready
 					        int[][] neighbors = new int[mapsize][4];
 					        for (int p=0; p<mapsize; p++) {
 					        	numneighbors[p] = 0;
 					        	for (int q=0; q<4; q++)
 					        		neighbors[p][q] = -1;
 					        }
 					        	
 							InputStream is3 = getResources().openRawResource(R.raw.neighbors);
 							InputStreamReader isr3 = new InputStreamReader(is3);
 							BufferedReader br3 = new BufferedReader(isr3);	
 
 							try {
 								String str = br3.readLine(); //Reads the number of nodes - should be the same as before
 								mapsize = Integer.parseInt(str);
 								
 								str = br3.readLine(); //Discards hash
 							} 
 							catch (IOException e) {
 								finish(); //should not reach here
 							}
 
 							try { //Gets every set of neighbors for each node as in the neighbors file
 								for (int ii=0; ii < mapsize; ii++) {
 									int ncount = 0;
 									String stri = br3.readLine(); //Reads in data
 									while (stri.compareTo("#########") != 0) {
 										int j = Integer.parseInt(stri);
 										if (ii != j) { //Double checking neighbor with self case - should not occur on well-made file
 											neighbors[ii][ncount] = j;
 										}
 										stri = br3.readLine();
 										ncount++;
 										numneighbors[ii]++;
 									}
 								}
 							} 
 							catch (IOException e) {
 								finish(); //should not reach here either
 							}								
 							//
 							//
 							//Done checking and doing if needed
 					        
 							//Move on to trying to find a proper signal
 					        //Currsig[0-2] is our signal, nodess1-3 is the signal strength of each node 0 to n-1 
 							for(int i=0; i<mapsize; i++) {
 								if (i >= fstarts[floor-2] && i <= fends[floor-2]) {
 									
 									int mistakes = 0;
 									int pow = 2; //never changed
 									
 									for (int j=0; j<SIGNALS; j++) {
 										if (isthere[j][i] != isus[j] || Math.abs(currsig[j]-nodess[j][i]) > 50)
 											mistakes++;
 									}
 									
 									for (int j=0; j<SIGNALS; j++) {
 										calcs[i] = calcs[i] + Math.pow(Math.abs(currsig[j]-nodess[j][i]),pow);
 									}
 									//new version, above is old
 									/*for (int j=0; j<SIGNALS; j++) { //experimental
 										if (isthere[j][i] == isus[j] && Math.abs(currsig[j]-nodess[j][i]) <= 50)
 											calcs[i] = calcs[i] + Math.pow(Math.abs(currsig[j]-nodess[j][i]),pow);
 										else
 											calcs[i] = calcs[i] + Math.pow(50,pow);
 									}*/								
 									if (mistakes > maxmistakes) {
 										calcs[i] = -1;
 									}
 									else {
 										foundpoint = true;
 										double heur = 1;
 										if (lastnode != -1)
 											//heur = Math.max(1,Math.pow(Math.log10(Math.max(1,realDistance(nodex[lastnode], nodey[lastnode], nodex[i], nodey[i]))),5)); //upped due to feedback
 											heur = Math.max(1,(Math.log10(Math.max(1,realDistance(nodex[lastnode], nodey[lastnode], nodex[i], nodey[i]))))); //additive heuristic 
 											//old version, backed up to this point.
 										
 										calcs[i] = heur * Math.sqrt(calcs[i]);
 									}
 									//
 									//take the rotation into account
 									//
 									
 									float rot = lockedx;
 									if (currnode > -1 && Math.abs(rot) > NOISE && lastnode > -1) { //not first run (needs to reset)
 										
 										for (int f=0; f<mapsize; f++) { //if turning, you're probably at an intersection
 											if (numneighbors[f] > 2 && calcs[f] != -1) {
 												calcs[f] = calcs[f] * (1.2-(0.1*numneighbors[f]));
 											}
 										}
 										
 										boolean neigh = false;
 										//check to see if this node is a neighbor
 										for (int qq=0; qq < 4; qq++) {
 											if (neighbors[currnode][qq] == i)
 												neigh = true;
 										}
 										
 										if (neigh == true) {
 											//No link to neighbor checking (may need to be removed)
 											boolean left = false;
 											boolean right = false;
 											
 											if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] > nodex[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < nodex[i]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[i])) {
 												right = true;
 											}
 											if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] < nodex[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] > nodex[i]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[i])) {
 												left = true;
 											}
 											
 											boolean correct = false;
 											if (curror == 1 && ((lockedx > NOISE && left == true) || (lockedx < -NOISE && right == true))) {
 												correct = true;
 											}
 											//1, screen is counterclockwise, - is left, 3, screen is clockwise, + is left
 											if (curror == 2 && ( (rotation == 3 &&(lockedx > NOISE && left == true) || (lockedx < -NOISE && right == true)) || (rotation == 1 &&(lockedx < NOISE && left == true) || (lockedx > -NOISE && right == true)) )) {
 												correct = true;
 											}
 											
 											//scale calcs[i] if turning - turning now accounts for anything down the line
 											if (correct == true) {
 												//calcs[i] = calcs[i]*(1-(1/(Math.max(1,realDistance(nodex[lastnode], nodey[lastnode], nodex[i], nodey[i])))));
 												calcs[i] = calcs[i] * 0.1;
 												willturn = true; //actually holds the turn
 												if (lockedx > NOISE && left == true) //printing
 													dleft = true;
 												else
 													dright = true;
 													
 											}
 										}
 									}
 		
 								}
 								else
 									calcs[i] = -1; //don't consider if not on the floor.
 								
 							}        
 					        //Calculate closest distance (above)
 							
 							trials++;
 							if ((trials % 10) % 2 == 0) {
 								maxmistakes++;
 							}
 							if (foundpoint == false) {
 						        try {
 						            Thread.sleep(100*maxmistakes);
 						        } 
 						        catch (InterruptedException e) {
 						        	e.printStackTrace();
 						        }
 							}
 						}
 						//signal finding ends here
 						
						if (maxmistakes >= 5) { //should be 5
 							cancelled = true;
 							notav = true;
 							Drawable drawable = getResources().getDrawable(R.drawable.floorplan);
 							Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
 							publishProgress(bitmap);
 						}
 						else {
 							
 							//get the last position, for voice
 							//lastnode = currnode;
 							startnode = currnode;
 							
 							int closestnode = -1; //the candidate for switching
 							
 							//this code is correct
 							for(int i=0; i<mapsize; i++) {
 								if (closestnode == -1) {
 									if (calcs[i] > -1)
 										closestnode = i;
 								}
 								else {
 									if (calcs[i] < calcs[closestnode] && calcs[i] > -1) //Pick nearest neighbor
 										closestnode = i;
 								}
 							}
 							
 							if ((currnode > -1 && Math.abs(lockedx) > NOISE) && numneighbors[closestnode] > 2) {
 								willturn = true;
 							}
 							
 							//reset on new state
 							if (firstrun == true && reset == true) {
 					    		target = resetarr[0];
 					    		currnode = resetarr[1];
 					    		lastnode = resetarr[2];
 					    		nextnode = resetarr[3];
 							}
 							
 							//Hardcode first location by node in other input
 							if (firstrun == true && starget >= 0) {
 								closestnode = starget;
 							}
 							
 							//Reset position here so that max time to actually use them
 							lockedx = 0;
 							
 							if (debug == true) {
 								//obvious test stuff
 								if ((currnode == 52 || currnode == 53) && willturn == false)
 									closestnode = 53;
 							}
 							
 							final int maxstick = 1; //change back to 2, experimental
 							
 							//check closestnode
 							if (stucknode == -1) { //if first run, set sticking point, where one is
 								stickiness = maxstick;
 								startnode = closestnode;
 								stucknode = closestnode;
 								sticknode = -1;
 								newstep = true;
 							}
 							else {
 								if (willturn == true) { //turning via gyro overrides sticking
 									if (closestnode != currnode) {
 										lastnode = currnode;
 									}
 									startnode = closestnode;
 									stucknode = closestnode;
 									sticknode = -1;
 									stickiness = maxstick;
 									newstep = true;
 									canspeak = true;
 								}
 								else {
 									if (closestnode == sticknode) { //If tests match, iterate towards updating location
 										stickiness--;
 										
 										if (stickiness == 0) {
 											if (closestnode != currnode) {
 												lastnode = currnode;
 											}
 											startnode = closestnode;
 											stucknode = closestnode;
 											sticknode = -1;
 											stickiness = maxstick;
 											newstep = true;
 											canspeak = true;
 										}
 										
 									}
 									else { //reset
 										sticknode = closestnode;
 										stickiness = maxstick;
 									}
 								}
 							}
 							//doesn't matter anymore
 							willturn = false;
 							
 							//set the current location
 							startnode = stucknode;
 							
 							//set for voice
 							currnode = stucknode;
 							
 							//All nodes have been read
 							//Lets generate neighbors from the next file
 							InputStream is2 = getResources().openRawResource(R.raw.neighbors);
 							InputStreamReader isr2 = new InputStreamReader(is2);
 							BufferedReader br2 = new BufferedReader(isr2);	
 							
 							try {
 								String str = br2.readLine(); //Reads the number of nodes - should be the same as before
 								mapsize = Integer.parseInt(str);
 								
 								str = br2.readLine(); //Discards hash
 							} 
 							catch (IOException e) {
 								finish(); //should not reach here
 							}
 							
 							//update path generation - create a map of the right size (
 							
 							EdgeWeightedDigraph dg = new EdgeWeightedDigraph(mapsize);
 							
 							try { //Gets every set of neighbors for each node as in the neighbors file
 								for (int i=0; i< mapsize; i++) {
 									String str = br2.readLine(); //Reads in data
 									while (str.compareTo("#########") != 0) {
 										int j = Integer.parseInt(str);
 										if (i != j) { //Double checking neighbor with self case - should not occur on well-made file
 											double dist = Math.sqrt(((nodex[i]-nodex[j])*(nodex[i]-nodex[j]))+((nodey[i]-nodey[j])*(nodey[i]-nodey[j])));
 											dist = Math.sqrt(dist);
 											
 											DirectedEdge e = new DirectedEdge(i, j, dist);
 											dg.addEdge(e);
 											
 										}
 										str = br2.readLine();
 									}
 								}
 							} 
 							catch (IOException e) {
 								finish(); //should not reach here either
 							}		
 							
 							if (startnode == targetnode) {
 								success = true;
 							}
 							else {
 								//Create Map with this info (9)
 								DijkstraSP distances = new DijkstraSP(dg,startnode);
 						        
 								//Work with it
 								int[] pathstarts;
 								int[] pathends;
 								int counting = 0;
 								
 						        //Unless the map is poorly constructed, this won't fail
 							
 						        for (DirectedEdge e : distances.pathTo(targetnode)) {
 						            counting++;
 						        }
 						        
 						        pathstarts = new int[counting];
 						        pathends = new int[counting];
 						        counting--;
 						        int linecount = 0;
 						    	
 						        for (DirectedEdge e : distances.pathTo(targetnode)) { //Read in the start and end of each node in the shortest path to draw it
 						            pathstarts[counting] = e.from();
 						            pathends[counting] = e.to();               
 						            linecount++;
 						            counting--;
 						        }
 						
 						        // The image is coming from resource folder but it could also 
 						        // load from the Internet or whatever
 						        
 						        Drawable drawable;
 						        
 						        switch (floor) {
 						        	case 2:
 						        		drawable = getResources().getDrawable(R.drawable.f2);
 						        		break;
 						        	case 3:
 						        		drawable = getResources().getDrawable(R.drawable.f3);
 						        		break;
 						        	default:
 						        		drawable = getResources().getDrawable(R.drawable.f4);
 						        		break; //this will be for 4, the only other choice
 						        }
 
 						        double mult = 1;
 						        //we could toggle but there's the leak
 						        
 						        /* bitmap = ((BitmapDrawable)drawable).getBitmap();
 						
 						        //Determine scaling factor
 						        imageViewHeight = getWindowManager().getDefaultDisplay().getHeight();
 						        factor = ( (float) imageViewHeight / (float) bitmap.getHeight() );
 						        
 						        nh = (int)(bitmap.getHeight()*factor*mult);
 						        nw = (int)(bitmap.getWidth()*factor*mult);
 						        
 						        // Create a new bitmap with the scaling factor
 						        //zoom the drawable if zoom option is on
 						        newbitmap = Bitmap.createScaledBitmap(bitmap, nw, nh, false);
 						        
 						        if (newbitmap.getHeight() == -1 || newbitmap.getWidth() == -1) {
 						        	newbitmap = bitmap;
 						        }
 						
 						        //Create another bitmap to draw on
 						        tempBitmap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
 						        tempCanvas = new Canvas(tempBitmap);
 						        
 						        //Turn the canvas back to a new bitmap
 						        linemap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
 						        tempCanvas.setBitmap(linemap);
 						        //Works as we go
 						        
 						        myPaint.setColor(Color.BLACK);
 						        BLUE.setColor(Color.BLUE);
 						        GREEN.setColor(Color.GREEN);
 						        
 						        myPaint.setStrokeWidth((float) (10*mult)); //make the lines bigger, will only affect the directional lines
 						        
 						        //do again for crown
 						        
 						        Drawable drawable2 = getResources().getDrawable(R.drawable.crown);
 						        Bitmap imgmap = ((BitmapDrawable)drawable2).getBitmap();
 						        int imgh = (int)(imgmap.getHeight()*factor*mult);
 						        int imgw = (int)(imgmap.getWidth()*factor*mult);
 						        
 						        float firstx = scale((float)nodex[pathstarts[linecount-1]], FLOORWIDTH, nw);
 						        
 						        if (firstrun == true && firstx > getWindowManager().getDefaultDisplay().getWidth())
 						        	reminder = true;
 						        else
 						        	reminder = false;
 						        	
 						        firstrun = false;
 						        
 						        //Draw the image bitmap into the canvas
 						        
 						        tempCanvas.drawBitmap(newbitmap, 0, 0, null); */
 						        
 						       
 						        
 						        bitmap = ((BitmapDrawable)drawable).getBitmap();
 								
 						        //Determine scaling factor
 						        imageViewHeight = getWindowManager().getDefaultDisplay().getHeight();
 						        factor = ( (float) imageViewHeight / (float) bitmap.getHeight() );
 						        
 						        nh = (int)(bitmap.getHeight()*factor*mult);
 						        nw = (int)(bitmap.getWidth()*factor*mult);
 						        
 						        // Create a new bitmap with the scaling factor
 						        //zoom the drawable if zoom option is on
 						        bitmap = Bitmap.createScaledBitmap(bitmap, nw, nh, false);
 						
 						        //Create another bitmap to draw on
 						        tempBitmap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
 						        tempCanvas = new Canvas(tempBitmap);
 						        
 						        //Turn the canvas back to a new bitmap
 						        linemap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
 						        tempCanvas.setBitmap(linemap);
 						        //Works as we go
 						        
 						        myPaint.setColor(Color.BLACK);
 						        BLUE.setColor(Color.BLUE);
 						        GREEN.setColor(Color.GREEN);
 						        
 						        myPaint.setStrokeWidth((float) (10*mult)); //make the lines bigger, will only affect the directional lines
 						        
 						        //do again for crown
 						        
 						        Drawable drawable2 = getResources().getDrawable(R.drawable.crown);
 						        Bitmap imgmap = ((BitmapDrawable)drawable2).getBitmap();
 						        int imgh = (int)(imgmap.getHeight()*factor*mult);
 						        int imgw = (int)(imgmap.getWidth()*factor*mult);
 						        
 						        float firstx = scale((float)nodex[pathstarts[linecount-1]], FLOORWIDTH, nw);
 						        
 						        if (firstrun == true && firstx > getWindowManager().getDefaultDisplay().getWidth())
 						        	reminder = true;
 						        else
 						        	reminder = false;
 						        	
 						        firstrun = false;
 						        
 						        //Draw the image bitmap into the canvas
 						        
 						        tempCanvas.drawBitmap(bitmap, 0, 0, null);
 						        
 
 						        
 						        //Draw Lines
 						        for (int i=0; i<linecount; i++) {
 						        	float sx = scale((float)nodex[pathstarts[i]], FLOORWIDTH, nw);
 						        	float sy = scale((float)nodey[pathstarts[i]], FLOORHEIGHT, nh);
 						        	float ex = scale((float)nodex[pathends[i]], FLOORWIDTH, nw);
 						        	float ey = scale((float)nodey[pathends[i]], FLOORHEIGHT, nh);   	
 						        	tempCanvas.drawLine(sx, sy, ex, ey, myPaint);
 						        	
 						        	nextnode = pathends[0];
 						        }
 						        
 						        Bitmap newimgmap = Bitmap.createScaledBitmap(imgmap, imgw, imgh, false);
 						        
 						        //Draw full map
 						        tempCanvas.drawCircle(firstx, scale((float)nodey[pathstarts[linecount-1]], FLOORHEIGHT, nh), 10, BLUE); //Draw Circle at start spot
 						        tempCanvas.drawBitmap(newimgmap, (scale((float)nodex[pathends[0]], FLOORWIDTH, nw) - (imgw/2)), (scale((float)nodey[pathends[0]], FLOORHEIGHT, nh) - (imgh/2)), GREEN);
 						        
 						        //try to play sound
 						        boolean left = false;
 						        boolean right = false;
 						        
 						        messagetype = 0;
 	
 						        if (!left && !right && canspeak && messageticker <= 0 && currnode > -1 && lastnode > -1) { //if no turns, check for obstacles
 						        	
 						        	int ostart = 0;
 						        	//oends[bestmatch-2]
 						        	if (finalmatch > 2) {
 						        		ostart = oends[finalmatch-3] + 1;
 						        	}
 						        	
 						        	for (int i=0; i<obs; i++) {
 						        		if (realDistance(nodex[currnode], nodey[currnode], ox[i], oy[i]) < or[i]*1.5 && i >= ostart && i <= oends[finalmatch-2]) {
 						        			//obstacle!
 						        			//check left/right and stuff
 									        if ((nodex[currnode] >= nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] <= oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] >= ox[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] < ox[i])) {
 									            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 									            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 									            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 									            float volume = streamVolumeCurrent / streamVolumeMax;
 									            canspeak = false;
 									            messageticker = 10;
 			
 									            if (voice) {
 									            	pool.play(soundsMap.get(3), volume, volume, 1, 0, 1); 
 									            }
 									            if (words) {
 									            	messagetype = 11;
 									            }
 									        	break;
 									        }
 									        if ((nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < ox[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] <= oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] >= ox[i])) {
 									            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 									            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 									            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 									            float volume = streamVolumeCurrent / streamVolumeMax;
 									            canspeak = false;
 									            messageticker = 10;
 									            
 									            if (voice) {
 									            	pool.play(soundsMap.get(4), volume, volume, 1, 0, 1); 
 									            }
 									            if (words) {
 									            	messagetype = 12;
 									            }
 									        	break;
 									        }	
 						        		}
 						        	}
 						        }					        
 						        
 						        //nodex, nodey
 						        if (currnode > -1 && nextnode > -1 && lastnode > -1 && newstep == true) {
 							        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode])) {
 							        	left = true;
 							        }
 							        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode])) {
 							        	right = true;
 							        }
 						        }
 						        if (left && canspeak && messageticker <= 0) {
 						            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 						            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 						            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 						            float volume = streamVolumeCurrent / streamVolumeMax; 
 						            canspeak = false;
 						            messageticker = 10;
 			
 						            if (voice) {
 						            	pool.play(soundsMap.get(1), volume, volume, 1, 0, 1); 
 						            }
 						            if (words) {
 						            	messagetype = 1;
 						            }
 						            
 						        }
 						        if (right && canspeak  && messageticker <= 0) {
 						            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 						            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 						            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 						            float volume = streamVolumeCurrent / streamVolumeMax;
 						            canspeak = false;
 						            messageticker = 10;
 			
 						            if (voice) {
 						            	pool.play(soundsMap.get(2), volume, volume, 1, 0, 1); 
 						            } 
 						            if (words) {
 						            	messagetype = 2;
 						            }
 						        }
 						        			        
 						        try {
 						            Thread.sleep(50);
 						        } 
 						        catch (InterruptedException e) {
 						        	e.printStackTrace();
 						        }
 						        
 						        if (currnode == targetnode || startnode == targetnode) {
 						        	success = true;
 						        }
 						        //cancelled = true;
 						        if (cancelled == true)
 						        	this.cancel(true);
 						        
 						        countdown--;
 						        publishProgress(linemap);
 						        
 							}
 						}
 					}
 					
 					catch (Exception e) {
 						//String estring = e.toString();
 						e.printStackTrace(System.out);
 						cancelled = true;
 						Drawable drawable = getResources().getDrawable(R.drawable.floorplan);
 						Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
 						publishProgress(bitmap);
 					}
 			
 				}
 				
 				//Each of these give a sort of last step, so that the message can be displayed
 				if (success == true) {
 		            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
 		            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		            float volume = streamVolumeCurrent / streamVolumeMax;
 		            if (voice) {
 		            	pool.play(soundsMap.get(5), volume, volume, 1, 0, 1); //needs to be 5
 		            }
 		            
 					Drawable drawable = getResources().getDrawable(R.drawable.floorplan);
 					Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
 					publishProgress(bitmap);
 		            
 				}
 				if (cancelled == true) {
 					Drawable drawable = getResources().getDrawable(R.drawable.floorplan);
 					Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
 					publishProgress(bitmap);
 			        this.cancel(true);
 				}
 				finish();
 				return null;
 				
 				
 				
 				
 			}
 
 			@Override
 			protected void onProgressUpdate(Bitmap... result) {
 				imageView.setImageBitmap(result[0]);
 				
 				if (messagetype == 1) {
 					Toast.makeText(now, "Turn Left.", Toast.LENGTH_LONG).show();
 				}
 				if (messagetype == 2) {
 					Toast.makeText(now, "Turn Right.", Toast.LENGTH_LONG).show();
 				}	
 				
 				if (messagetype == 11) {
 					Toast.makeText(now, "Obstacle in area. Move Left.", Toast.LENGTH_LONG).show();
 				}
 				if (messagetype == 12) {
 					Toast.makeText(now, "Obstacle in area. Move Right.", Toast.LENGTH_LONG).show();
 				}				
 				
 				if (reminder == true)
 					Toast.makeText(now, "Scroll right to see your location", Toast.LENGTH_LONG).show();
 				
 				if (notav == true || (cancelled == true && butcan == false))
 					Toast.makeText(now, "Wifi not availible. Try again in a few moments.", Toast.LENGTH_LONG).show();
 				
 				if (success == true)
 					Toast.makeText(now, "You have arrived at your destination.", Toast.LENGTH_LONG).show();
 				
 				if (estring != "")
 					Toast.makeText(now, estring, Toast.LENGTH_LONG).show();
 				
 				
 				if (dleft == true)
 					Toast.makeText(now, "Detected left turn", Toast.LENGTH_SHORT).show();
 				if (dright == true)
 					Toast.makeText(now, "Detected right turn", Toast.LENGTH_SHORT).show();	
 				//if (dturn == true)
 				//	Toast.makeText(now, "Detected a turn", Toast.LENGTH_SHORT).show();					
 				
 			}
 			
 			
 		};
 		
 		Void v = null;
 		math.execute(v);
         
     }   
     
     public float scale(float inloc, int inmax, int indim) {
     	
     	float scaleloc = (float)((inloc/inmax)*indim);
     	
     	return scaleloc;
     	
     }
     
     public double realDistance(double cx, double cy, double ox, double oy) {
     	
     	return Math.sqrt((Math.abs(cx-ox)*Math.abs(cx-ox)) + (Math.abs(cy-oy)*Math.abs(cy-oy)));
     	
     }
     
     @Override
     public void onBackPressed() {
         //don't go back immediately, it'll mess everything up due to asyncs
     	cancelled = true;
     	butcan = true;
     }
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		//not changing anyway
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		
 		//Left turns seem to have a positive x briefly, right turns negative
 		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
 
 			if (holdcycles /*== 0*/ > -999) { //will detect no matter what
 				
 				if (curror == 1) { //x axis of portrait
 		        	gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
 		        	rx = -(event.values[0] - gravity[0]);
 				}
 				if (curror == 2) { //y axis if landscape
 		        	gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
 		        	rx = -(event.values[1] - gravity[1]);
 				}				
 		        
 				if (!mInitialized) {
 					mLastX = rx;
 					mInitialized = true;
 				}
 				else {
 					if (Math.abs(rx) < NOISE) 
 						rx = (float)0.0;
 					else
 						if ((rx > NOISE && rx > mLastX) || (rx < -1*NOISE && rx < mLastX)) { //filters the end of a turn off, we want the beginning
 							dturn = true;
 							holdcycles = 3;
 							lockedx = rx;
 						}
 					mLastX = rx;
 				}
 			}
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mSensorManager.unregisterListener(this);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             NavUtils.navigateUpFromSameTask(this);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
 
