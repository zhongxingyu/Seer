 import com.tinkerforge.BrickletGPS;
 import com.tinkerforge.IPConnection;
 
 public class ExampleSimple {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "ABC"; // Change to your UID
 	
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the commnents below
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletGPS gps = new BrickletGPS(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
		// Get GPS coordinates
 		BrickletGPS.Coordinates coords = gps.getCoordinates(); // Can throw com.tinkerforge.TimeoutException
 
 		System.out.println("Latitude: " + coords.latitude/1000000.0 + "° " + coords.ns);
 		System.out.println("Longitude: " + coords.longitude/1000000.0 + "° " + coords.ew);
 
 		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
