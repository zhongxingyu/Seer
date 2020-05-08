 package nl.rug.snoozzz;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import objects.Movement;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.SensorManager;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MeasurementService extends Service implements AccelerometerListener {
 
 	private String ip;
 	
 	private static final String TAG = "MeasurementService";
 	
 	private ArrayList<Movement> movementList = new ArrayList<Movement>();
 	
 	private long startTime;
 	
 	private Connection connection;
 
 	private boolean connected = false;
 	
 	Timer timer = new Timer();
 	
 	private long interval = 1000; // Time in milliseconds
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		
 		super.onCreate();
 		
 		Log.v(TAG, "Created");
 		
 		String sensorService = Context.SENSOR_SERVICE;
 
 		SensorManager sensorManager = (SensorManager) getSystemService(sensorService);
 		
 		Accelerometer accelerometer = new Accelerometer(sensorManager);
 
 		accelerometer.addListener(this);
 		
 		
 		
 		
 	}
 
 	@Override
 	public void onAccelerationChanged(float x, float y, float z) {
 
 		if (connected) {
 
 			Movement movement = new Movement(x, y, z);
 			
 			System.out.println("Movement created.");
 			
 			movement.setTime(System.currentTimeMillis() - startTime);
 			
 			movementList.add(movement);
 
 		}
 		
 	}
 	
 	
 	
 	@Override
 	public void onDestroy() { 
 		
 		if(!connected) {
 			
 			connected = !connection.disconnect();
 			
 		}
 		
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startid) { 
 		
 		Log.v(TAG, "Started");
 		
		ip = intent.getExtras().getString("ip");
		
		int port = getResources().getInteger(R.integer.port);
 		
 		if (!connected) {
 
			connection = new SocketConnection(ip, port);
 
 			connected = connection.connect();
 			
 			if (connected) {
 
 				startTime = System.currentTimeMillis();
 
 				timer = new Timer();
 
 				timer.scheduleAtFixedRate(writeMovementTask(), 0, interval);
 
 			} else {
 
 				Toast.makeText(this, "Connection failed..", 1000).show();
 
 			}
 			
 		}
 		
 	}
 	
 	private TimerTask writeMovementTask() {
 		return new TimerTask() {
 			@Override
 			public void run() {
 
 				List<Movement> movements = new ArrayList<Movement>();
 
 				movements.addAll(movementList);
 
 				movementList.clear();
 
 				connection.sendToServer(movements);
 
 			}
 			
 		};
 		
 	}
 
 	@Override
 	public void onShake(float force) {
 		// TODO Auto-generated method stub
 		
 	}
 }
