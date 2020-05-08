 package biz.robotopen.robotopencontrol;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.MobileAnarchy.Android.Widgets.Joystick.DualJoystickView;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 
 public class RobotOpenControl extends Activity implements Observer {
     
     //private SharedPreferences preferences;
     private RORobotInstance robotInstance;
 	
 	private DualJoystickView joystick;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         
         // Initialize preferences
 		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
         
         joystick = (DualJoystickView)findViewById(R.id.dualjoystickView);
         
 		robotInstance = new RORobotInstance(joystick);
 		robotInstance.getDashboardData().addObserver(this);
 		robotInstance.getJoystickHandler().addObserver(this);
     }
     
     /* Call this whenever the network settings need to be reloaded */
     /*public void updateNetworking() {
     	try {
 			//ipAddress = InetAddress.getByName(preferences.getString("ipaddress", "192.168.1.22"));
 			//packetRate = Integer.parseInt(preferences.getString("txinterval", "20"));
     	} catch (Exception e) {
     		// Networking exception
     	}
     }*/
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//MenuInflater inflater = getMenuInflater();
 		//inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
     
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		//startActivity(new Intent(this, Preferences.class));
 		return true;
 	}
     
     @Override
     protected void onPause() {
     	// End Ethernet communications
     	robotInstance.getPacketTransmitter().setDisabled();
 		robotInstance.getPacketTransmitter().terminate();
     	
     	super.onPause();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	// Update networking settings
     	//updateNetworking();
     }
 
 	@Override
 	public void update(Observable obs, Object obj) {
 		if (obs == robotInstance.getJoystickHandler()) {
 			if (robotInstance.getJoystickHandler().controllersActive()) {
 				// Controller is active - begin xmit and enable
 				robotInstance.getPacketTransmitter().beginXmit();
 				robotInstance.getPacketTransmitter().setEnabled();
 			}
 			else {
 				// Controller is inactive - disable and terminate
 				robotInstance.getPacketTransmitter().setDisabled();
 				robotInstance.getPacketTransmitter().terminate();
 			}
 		}
 		else if (obs == robotInstance.getDashboardData()) {
 			// New Packet!
 			ArrayList<String> bundles = robotInstance.getDashboardData().getBundles();
 			
 			String[] dashboardData = new String[bundles.size()];
 			
 			for (int i = 0; i < bundles.size(); i++)
 				dashboardData[i] = bundles.get(i);
 		}
 	}
 
 }
