 package net.cpbgroup.Saucier;
 
 import java.net.URL;
 import java.util.concurrent.CountDownLatch;
 
 public class Saucier {
 	
 	//=========================================================================
 	// PROPERTIES
 	//=========================================================================
 
 	private SauceConnectThread sauceConnect;
 	private CountDownLatch latch;
 	private static Saucier instance = null;
 	public RequestBuffer requestBuffer;
 	
 	//=========================================================================
 	// SINGLETON INSTANCE
 	//=========================================================================
 	
 	public static void main(String[] args) {
 		
 		if(args.length != 2) {
 			System.err.println("Usage: $ java -jar saucier [USERNAME] [API KEY]");
 		}
 		
		Saucier sc = Saucier.getInstance(args[0], args[1]);
 		
 	}
 	
 	//=========================================================================
 	// SINGLETON INSTANCE
 	//=========================================================================
 	
 	public static Saucier getInstance(String username, String secretKey) {
 		if(instance == null) {
 			instance = new Saucier(username, secretKey);
 		}
 		return instance;
 	}
 	
 	//=========================================================================
 	// CONSTRUCTOR - PROTECTED TO ENFORCE SINGLETON
 	//=========================================================================
 	
 	/**
 	 * Created a new singleton instance of the SauceConnect client.
 	 * 
 	 * @param username - The username for the SauceConnect client to use.
 	 * @param secretKey - The secretKey for the user of the SauceConnect client;
 	 */
 	protected Saucier(String username, String secretKey) {
 		latch = new CountDownLatch(1);
 		sauceConnect = new SauceConnectThread(username, secretKey, latch);
 		requestBuffer = sauceConnect.requestBuffer;
 		
 		new Thread(sauceConnect).start();
 		
 		try {
 			latch.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Gets the SauceConnect connection URL to be used for WebDrivers.
 	 * @return URL
 	 */
 	public URL getConnectionURL() {
 		return sauceConnect.getConnectionURL();
 	}
 	
 	/**
 	 * Shutdown Sauceconnect
 	 */
 	public void close() {
 		sauceConnect.close();
 	}
 
 }
