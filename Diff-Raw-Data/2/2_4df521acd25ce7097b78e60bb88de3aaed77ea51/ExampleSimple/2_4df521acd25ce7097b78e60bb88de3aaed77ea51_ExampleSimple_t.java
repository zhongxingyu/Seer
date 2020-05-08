 import com.tinkerforge.BrickletBarometer;
 import com.tinkerforge.IPConnection;
 
 public class ExampleSimple {
 	private static final String HOST = "localhost";
 	private static final int PORT = 4223;
 	private static final String UID = "bAc"; // Change to your UID
 
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletBarometer b = new BrickletBarometer(UID, ipcon); // Create device object
 
 		ipcon.connect(HOST, PORT); // Connect to brickd
 		// Don't use device before ipcon is connected
 
 		// Get current air pressure (unit is mbar/1000)
 		int airPressure = b.getAirPressure(); // Can throw com.tinkerforge.TimeoutException
 
		System.out.println("Air Pressure: " + airPressure/1000.0 + " mbar");
 
 		// Get current altitude (unit is cm)
 		int altitude = b.getAltitude(); // Can throw com.tinkerforge.TimeoutException
 
 		System.out.println("Altitude: " + altitude/100.0 + " m");
 
 		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
