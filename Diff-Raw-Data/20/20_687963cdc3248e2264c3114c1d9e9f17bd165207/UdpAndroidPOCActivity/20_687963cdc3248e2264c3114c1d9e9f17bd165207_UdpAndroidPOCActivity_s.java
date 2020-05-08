 package udppoc.lenamespace;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.SocketException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.provider.Settings.Secure;
 
 public class UdpAndroidPOCActivity extends Activity  implements SensorEventListener {
 	private DatagramSocket s;
 	private int messageNumber = 0;
 	
 	static int server_port = 12345;
 	static String ip_address;
 	
 	// accelerometer stuff
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	
 	// unique ID used to identify a mobile device on the server
 	private String uniqueID;
 	
 	// a unique browser ID that is used on the server side to match the 
 	// mobile device with a browser
 	private String browserID;
 	
 	// widgets
 	Button send_udp_button;
 	Button set_ip_button;
 	Button set_browserID_button;
 	EditText browserID_editText;
 	EditText ip_editText;
 	TextView ip_textView;
 	TextView uid_textView;
 	TextView browserID_textView;
    
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         initSensors();
         // get the screen widgets
         setViewItems();
         generateUniqueID();
         
         // set the default ip
        ip_address = "10.86.234.216";
         ip_textView.setText(ip_address);
         
         // event listener for the send test udp package button click
         send_udp_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Send a test message to the sever on button click
             	messageNumber++;
             	
                 String messageStr="Message number " + String.valueOf(messageNumber);
         		try {
         			s = new DatagramSocket();
        			InetAddress local = InetAddress.getByName("10.86.234.216");
         	        int msg_length=messageStr.length();
         	        byte[] message = messageStr.getBytes();
         	        DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
         	        s.send(p);
         		} catch (SocketException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		} catch (IOException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		}
             }
         });
         
         set_ip_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	// get the IP
                 ip_address = ip_editText.getText().toString();
                 if (!ip_address.equals(""))
                 	ip_textView.setText(ip_address);
             }
         });
         
         set_browserID_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	browserID = browserID_editText.getText().toString();
             	browserID_textView.setText(browserID);
             }
         });
     }
     
     private void initSensors()
     {
         // get the sensors
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
     }
     
     private void generateUniqueID()
     {
     	uniqueID = Secure.getString(getBaseContext().getContentResolver(),
                 Secure.ANDROID_ID);
     	uid_textView.setText(uniqueID);
     }
     
     private void setViewItems()
     {
     	send_udp_button = (Button) findViewById(R.id.send_udp_button);
     	set_ip_button = (Button) findViewById(R.id.set_ip_button);
     	ip_editText = (EditText) findViewById(R.id.ip_editText);
     	ip_textView = (TextView) findViewById(R.id.ip_textView);
     	uid_textView = (TextView) findViewById(R.id.uid_textView);
     	set_browserID_button = (Button) findViewById(R.id.set_browserID_button);
     	browserID_editText = (EditText) findViewById(R.id.browserID_editText);
     	browserID_textView = (TextView) findViewById(R.id.browserID_textView);
     }
     
     protected void onResume() 
     {
     	super.onResume();
     	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 	}
     
 	protected void onPause() 
 	{
 		super.onPause();
 		mSensorManager.unregisterListener(this);
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) 
 	{
 		// can be safely ignored for now
 	}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) 
 	{
 		float x = event.values[0];
 		float y = event.values[1];
 		float z = event.values[2];
 		
 		messageNumber++;
 		// we only take into consideration the y axis - the phone will handle
 		// the landscape horizontal tilt
 		// the message has the format:
 		// message_type|browser_id|player_id|accelero_data|additional_message
 		// where message_type = 0 if there was an error, 1 if the message contains
 		// valid accelerometer data
         String messageStr;
 		try {
 			// build the message string
 			messageStr = String.valueOf(1) + "|" + browserID + "|" + uniqueID + "|" + String.valueOf(y) + "|";
 			
 			s = new DatagramSocket();
			InetAddress local = InetAddress.getByName("10.86.234.216");
 	        int msg_length=messageStr.length();
 	        byte[] message = messageStr.getBytes();
 	        DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
 	        s.send(p);
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
     	
 }
