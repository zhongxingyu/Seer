 import com.tinkerforge.BrickletSoundIntensity;
 import com.tinkerforge.IPConnection;
 
 public class ExampleCallback {
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
 
 		// Set Period for intensity callback to 1s (1000ms)
 		// Note: The intensity callback is only called every second if the 
 		//       intensity has changed since the last call!
 		si.setIntensityCallbackPeriod(1000);
 
 		// Add and implement intensity listener (called if intensity changes)
 		si.addIntensityListener(new BrickletSoundIntensity.IntensityListener() {
 			public void intensity(int intensity) {
 				System.out.println("Intensity: " + intensity);
 			}
 		});
 
		System.console().readLine("Press key to exit\n");
 		ipcon.disconnect();
 	}
 }
