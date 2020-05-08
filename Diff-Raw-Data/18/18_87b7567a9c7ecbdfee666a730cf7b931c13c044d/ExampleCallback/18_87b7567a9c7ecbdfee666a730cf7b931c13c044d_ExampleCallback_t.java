 import com.tinkerforge.BrickletRotaryEncoder;
 import com.tinkerforge.IPConnection;
 
 public class ExampleCallback {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "XYZ"; // Change to your UID
 	
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
		BrickletRotaryEncoder re = new BrickletRotaryEncoder(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
 		// Set Period for count callback to 0.05s (50ms)
 		// Note: The count callback is only called every 50ms if the
 		//       count has changed since the last call!
		re.setCountCallbackPeriod(50);
 
 		// Add and implement count listener
		re.addCountListener(new BrickletRotaryEncoder.CountListener() {
 			public void count(int count) {
 				System.out.println("Count: " + count);
 			}
 		});
 
 		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
