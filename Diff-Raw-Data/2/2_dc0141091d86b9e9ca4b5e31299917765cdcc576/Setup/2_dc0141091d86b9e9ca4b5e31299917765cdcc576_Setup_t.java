 package tc.net.expertmac2.UplinkBot.setup;
 
 import java.io.*;
 
 import org.yaml.snakeyaml.*;
 import tc.net.expertmac2.UplinkBot.UplinkRPG;
 import tc.net.expertmac2.UplinkBot.config.ConfigurationMaker;
 import tc.net.expertmac2.util.findOS;
 
 @SuppressWarnings("unused")
 public class Setup {
 	
 	private findOS a = new findOS();
 	private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	private final ConfigurationMaker cfgm = new ConfigurationMaker();
 	
 
 	public void SetupBot() {
 		System.out.println("");
 		System.out.println("-- UplinkRPG " + UplinkRPG.version + " Setup --");
 		System.out.println("UplinkRPG (C) 2011 expertmac2");
 		System.out.println("");
 		System.out.println("Initializing setup, please wait.");
 		System.out.print("Detecting OS..");
 		String os = a.getOS(); // Get the OS, then print it out
 		if (os.equalsIgnoreCase("windows")) { System.out.print(" OS: Windows"); }
 		if (os.equalsIgnoreCase("unix")) { System.out.print(" OS: Unix-based"); }
 		if (os.equalsIgnoreCase("linux")) { System.out.print(" OS: Linux-based"); }
 		if (os.equalsIgnoreCase("unknown")) { 
 			System.out.print(" OS: Unknown");
 			System.out.println("\n[ERROR] Your OS is unsupported by the UplinkRPG bot installation.");
 			System.out.println("[ERROR] If you are running Windows, Linux, or Unix, restart the setup.");
 			System.exit(1);
 		}
 		System.out.println("\nDeclaring variables..");
		String ntwk = null; // Declare and initialise the variables
 		String nick = null;
 		System.out.println("\n\nInitialization complete.");
 		System.out.print("\n\nEnter your desired network> ");
 		try { ntwk = br.readLine(); } // Read the network that was entered
 		catch (IOException ioe) {
 			System.out.println("\n[ERROR] An IOException was thrown, restart the setup.");
 			System.exit(1);
 		}
 		try { cfgm.makeConfig(ntwk, nick); }
 		catch (NullPointerException npe) {
 			System.out.println("\n[ERROR] Nice try, attempting to set something to null. Restart the setup.");
 			System.exit(1);
 		}
 		// TODO: database configuration
 	}
 }
