 package nl.rug.snoozzz;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import nl.rug.sleepassistant.R;
 import objects.Movement;
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements AccelerometerListener {
 	private Connection connection;
 
 	private SensorManager sensorManager;
 
 	private boolean connected = false;
 
 	private Context CONTEXT;
 
 	// private float previousX = 0, previousY = 0, previousZ = 0;
 
 	private long startTime;
 
 	private ArrayList<Movement> movementList = new ArrayList<Movement>();
 
 	private Timer timer = new Timer();
 	// private float movement = 0;
 	// private boolean local = false;
 	private long interval = 1000; // Time in milliseconds
 
 	// private Movements movements = new Movements();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		CONTEXT = this;
 
 		sensorManager = (SensorManager) getContext().getSystemService(
 				Context.SENSOR_SERVICE);
 	}
 
 	public void onClickDisconnectButton(View view) {
 
 		if (connection != null) {
 			
 			timer.cancel();
 			
 			connected = connection.disconnect();
 
 			((ProgressBar) findViewById(R.id.progressBar))
 					.setVisibility(View.INVISIBLE);
 		}
 
 	}
 
 	public void onClickConnectButton(View view) {
 		// startTime = System.currentTimeMillis();
 		// if(local)startLocal(view);
 		// else startOnline(view);
 		startOnline(view);
 
 	}
 
 	private void startLocal(View view) {
 		connected = true;
 		Accelerometer accelerometer = new Accelerometer(sensorManager);
 		accelerometer.addListener(this);
 		view.setEnabled(false);
 		findViewById(R.id.disconnect_button).setEnabled(true);
 		((ProgressBar) findViewById(R.id.progressBar))
 				.setVisibility(View.VISIBLE);
 	}
 
 	private void startOnline(View view) {
 
 		String ip = ((EditText) findViewById(R.id.ip)).getText().toString();
 
 		connection = new SocketConnection(ip, 5554);
 
 		connected = connection.connect();
 
 		if (connected) {
 
 			Accelerometer accelerometer = new Accelerometer(sensorManager);
 
 			accelerometer.addListener(this);
 
 			((ProgressBar) findViewById(R.id.progressBar))
 					.setVisibility(View.VISIBLE);
 
 			startTime = System.currentTimeMillis();
 
 			timer.scheduleAtFixedRate(writeMovementTask(), 0, interval);
 
 		} else {
 
 			Toast.makeText(this, "Connection failed..", 1000).show();
 
 			((ProgressBar) findViewById(R.id.progressBar))
 					.setVisibility(View.INVISIBLE);
 
 		}
 	}
 
 	public Context getContext() {
 
 		return CONTEXT;
 
 	}
 
 	@Override
 	public void onAccelerationChanged(float x, float y, float z) {
 
 		// Doe iets met die datei
 		if (connected) {
 			// movement += getMovement(x, y, z);
 
 			Movement movement = new Movement(x, y, z);
 			System.out.println("Movement created.");
 			movement.setTime(System.currentTimeMillis() - startTime);
 			movementList.add(movement);
 
 		}
 	}
 
 	private TimerTask writeMovementTask() {
 		return new TimerTask() {
 			@Override
 			public void run() {
 				// double time = System.currentTimeMillis() - startTime;
 				// String message = time + "," + movement + ";";
 				// writeMovement(message);
 				// movement = 0;
 				List<Movement> movements = new ArrayList<Movement>();
 
 				movements.addAll(movementList);
 				movementList.clear();
 
 				connection.sendToServer(movements);
 				// movements.clearList();
 			}
 		};
 	}
 
 	// private void writeMovement(String movement){
 	// System.out.println("Message: " + movement);
 	//
 	// }
 	//
 	// private void writeToFile(String filename, String message){
 	// try {
 	// FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
 	// fos.write(message.getBytes());
 	// fos.close();
 	// } catch (FileNotFoundException e) {
 	// e.printStackTrace();
 	// } catch (IOException e) {
 	// e.printStackTrace();
 	// }
 	// }
 
 	// public float getMovement(float x, float y, float z){
 	// float difference = Math.abs(previousX - x) + Math.abs(previousY - y) +
 	// Math.abs(previousZ - z);
 	//
 	// previousX = x;
 	// previousY = y;
 	// previousZ = z;
 	//
 	// return difference;
 	// }
 
 	@Override
 	public void onShake(float force) {
 		// TODO Auto-generated method stub
 
 	}
 }
