 import com.tinkerforge.BrickletIndustrialDual020mA;
 import com.tinkerforge.IPConnection;
 
 public class ExampleThreshold {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "XYZ"; // Change to your UID
 	
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletIndustrialDual020mA dual020 = new BrickletIndustrialDual020mA(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
 		// Get threshold callbacks with a debounce time of 10 seconds (10000ms)
 		dual020.setDebouncePeriod(10000);
 
 		// Configure threshold (sensor 1) for "greater than 10mA" (unit is mA)
 		dual020.setCurrentCallbackThreshold((short)1, '>', 10*1000*1000, 0);
 
 		// Add and implement current reached listener (called if current is greater than 10mA)
 		dual020.addCurrentReachedListener(new BrickletIndustrialDual020mA.CurrentReachedListener() {
 			public void currentReached(short sensor, int current) {
 				System.out.println("Current (sensor " + sensor + ") is greater than 10mA: " + current/(1000.0*1000.0));
 			}
 		});
 
 		System.console().readLine("Press key to exit\n");
 	}
 }
