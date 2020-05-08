 import com.tinkerforge.BrickletSoundIntensity;
 import com.tinkerforge.IPConnection;
 
 public class ExampleThreshold {
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
 
 		// Get threshold callbacks with a debounce time of 1 seconds (1000ms)
 		si.setDebouncePeriod(1000);
 
 		// Configure threshold for "greater than 2000"
 		si.setIntensityCallbackThreshold('>', (short)(2000), (short)0);
 
 		// Add and implement intensity reached listener 
 		// (called if intensity is greater than 2000)
 		si.addIntensityReachedListener(new BrickletSoundIntensity.IntensityReachedListener() {
 			public void intensityReached(int intensity) {
 				System.out.println("Intensity: " + intensity);
 			}
 		});
 
		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
