 import com.tinkerforge.BrickletRotaryPoti;
 import com.tinkerforge.IPConnection;
 
 public class ExampleCallback {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "ABC"; // Change to your UID
 	
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletRotaryPoti rp = new BrickletRotaryPoti(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
 		// Set Period for position callback to 0.05s (50ms)
		// Note: The position callback is only called every 50ms if the
 		//       position has changed since the last call!
 		rp.setPositionCallbackPeriod(50);
 
 		// Add and implement position listener (called if position changes)
 		rp.addPositionListener(new BrickletRotaryPoti.PositionListener() {
 			public void position(short position) {
 				System.out.println("Position: " + position);
 			}
 		});
 
 		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
