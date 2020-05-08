 import com.tinkerforge.BrickletIndustrialDual020mA;
 import com.tinkerforge.IPConnection;
 
 public class ExampleCallback {
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
 
 		// Set Period (sensor 1) for current callback to 1s (1000ms)
 		// Note: The current callback is only called every second if the 
 		//       current has changed since the last call!
 		dual020.setCurrentCallbackPeriod((short)1, 1000);
 
 		// Add and implement current listener (called if current changes)
 		dual020.addCurrentListener(new BrickletIndustrialDual020mA.CurrentListener() {
 			public void current(short sensor, int current) {
 				System.out.println("Current (sensor " + sensor + "): " + current/(1000.0*1000.0) + " mA");
 			}
 		});
 
 		System.console().readLine("Press key to exit\n");
		ipcon.disconnect();
 	}
 }
