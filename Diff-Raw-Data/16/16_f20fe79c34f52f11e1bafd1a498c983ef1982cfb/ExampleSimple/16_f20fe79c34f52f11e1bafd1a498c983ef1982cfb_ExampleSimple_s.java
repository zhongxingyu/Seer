 import com.tinkerforge.BrickletSoundIntensity;
 import com.tinkerforge.IPConnection;
 
 public class ExampleSimple {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "XYZ"; // Change to your UID
 	
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletSoundIntensity si = new BrickletSoundIntensity(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
 		// Get current intensity
 		int intensity = si.getIntensity(); // Can throw com.tinkerforge.TimeoutException
 
 		System.out.println("Intensity: " + intensity);
 
		System.console().readSoundIntensity("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
