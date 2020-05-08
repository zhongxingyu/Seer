 package net.cpbgroup.Saucier;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.CountDownLatch;
 
 public class SauceConnectThread implements Runnable {
 
 
 	//=========================================================================
 	// PROPERTIES
 	//=========================================================================
 	
 	public String username, secretKey;
 	public RequestBuffer requestBuffer = new RequestBuffer();
 	
 	private URL connectionURL;
 	private Process connect;
 	private boolean isConnected = false;
 	private CountDownLatch latch;
 	private boolean isRunning = true;
 	
 	//=========================================================================
 	// CONSTRUCTOR
 	//=========================================================================
 	
 	/**
 	 * Created a new singleton instance of the SauceConnect client.
 	 * 
 	 * @param username - The username for the SauceConnect client to use.
 	 * @param secretKey - The secretKey for the user of the SauceConnect client;
 	 */
 	public SauceConnectThread(String username, String secretKey, CountDownLatch doneSignal) {
 		setConnectionURL(username, secretKey);
 		this.username = username;
 		this.secretKey = secretKey;
 		this.latch = doneSignal;
 	}
 	
 	//=========================================================================
 	// PUBLIC METHODS
 	//=========================================================================
 	
 	@Override
 	public void run() {
 		BufferedReader sauceOutput;
		BufferedReader sauceError;
 
 		// start sauce connect
 		try {
 			connect = Runtime.getRuntime().exec("java -jar libs/Sauce-Connect.jar " + username + " " + secretKey);
 			sauceOutput = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			sauceError = new BufferedReader(new InputStreamReader(connect.getErrorStream()));
 
 			// Capture all output from the sauceconnect executable
 			String line;
 			boolean captureRequests = false;
 			while (isRunning &&
 				   (line = sauceOutput.readLine()) != null) {
 				
 				System.out.println(line);
 				
 				if(captureRequests) {
 					requestBuffer.add(new Request(line));
 				}
 				
 				if (line.contains("Connected! You may start your tests")) {
 					latch.countDown();
 					captureRequests = true;
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return URL object with the connection to the sauce connect instance
 	 */
 	public URL getConnectionURL() {
 		return connectionURL;
 	}
 	
 	/**
 	 * Returns boolean for if the connection is open or not.
 	 */
 	public boolean isConnected() {
 		return isConnected;
 	}
 	
 	/**
 	 * Checks the RequestBuffer for a request;
 	 * @return The string that matched the request
 	 */
 	public String checkRequest() {
 		// TODO: need to implement SauceConnect::checkRequest();
 		return "NOT IMPLEMENTED";
 	}
 	/**
 	 * Kill the SauceConnect instance 
 	 */
 	public void close() {
 		isRunning = false;
 		connect.destroy();
 	}
 	
 	//=========================================================================
 	// PRIVATE METHODS
 	//=========================================================================
 	
 	/**
 	 * Set the SauceConnect connection url.
 	 * @param username - The username to use for the connection
 	 * @param secretKey - The secret key to use for the connection
 	 */
 	private void setConnectionURL(String username, String secretKey) {
 		try {
 			connectionURL = new URL("http://" + username + ":" + secretKey + "@ondemand.saucelabs.com:80/wd/hub");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 }
