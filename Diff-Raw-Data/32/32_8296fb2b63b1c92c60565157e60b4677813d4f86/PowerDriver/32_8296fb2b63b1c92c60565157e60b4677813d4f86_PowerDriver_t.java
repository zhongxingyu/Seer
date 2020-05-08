 package silas;
 import java.util.Collection;
 import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

 import org.grailrtls.libworldmodel.client.ClientWorldConnection;
 import org.grailrtls.libworldmodel.client.StepResponse;
 import org.grailrtls.libworldmodel.client.Response;
 import org.grailrtls.libworldmodel.client.WorldState;
 import org.grailrtls.libworldmodel.client.protocol.messages.Attribute;
 import org.grailrtls.libworldmodel.types.StringConverter;
 import org.grailrtls.libworldmodel.types.IntegerConverter;
 import org.grailrtls.libworldmodel.types.BooleanConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import org.apache.http.Header;
 import org.apache.http.HeaderIterator;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class PowerDriver {
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger log = LoggerFactory.getLogger(PowerDriver.class);
 
 	/**
 	 * Controls any number of Digital Loggers Web Power Switch III outlets.
 	 * Watches the world model for changes in objects with URIs matching
 	 * ".*powerswitch.*". Each object should have:
 	 * <ul><li>username (string) for the
 	 * network power switch</li>
 	 * <li>password (string) for the network power switch</li>
 	 * <li>target (string) for the HTTP request to access the power switch, in this
 	 * format: http://192.168.200.34:7005/</li>
 	 * <li>on (boolean)</li>
 	 * <li>outlet (integer) from 1 to 8</li>
 	 * </ul>
 	 * @param args World Model Host, World Model Client Port 
 	 */
 	public static void main(String[] args) {
 		if (args.length==1 && "-?".equals(args[0])){
 			System.out.println("name: Power Driver");
 			System.out.println("arguments: worldmodel wm_client");
 			System.out.println("description: Controls any number of Digital Loggers");
 			System.out.println("description: Web Power Switch outlets. World model URIs");
 			System.out.println("description: must match \".*powerswitch.*\".");
 			System.out.println("requires: username");
 			System.out.println("requires: password");
 			System.out.println("requires: target");
 			System.out.println("requires: outlet");
 		}
 		if (args.length < 2) {
 			printUsageInfo();
 			return;
 		}
		HashMap<String, Integer> outletmap = new HashMap<String, Integer>();
		HashMap<String, String> targetmap = new HashMap<String, String>();
		HashMap<String, String> usernamemap = new HashMap<String, String>();
		HashMap<String, String> passwordmap = new HashMap<String, String>();
		
 		// Create a connection to the World Model as a client
 		final ClientWorldConnection wmc = new ClientWorldConnection();
 		log.info("Connecting to {}", wmc);
 		wmc.setHost(args[0]);
 		wmc.setPort(Integer.parseInt(args[1]));
 		log.debug("Connected.");
 		// Attempt to connect and exit if it fails
 		if (!wmc.connect()) {
 			log.error("Couldn't connect to the world model!  Check your connection parameters.");
 			return;
 		}
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				log.info("Disconnecting from {}", wmc);
 				wmc.disconnect();
 			}
 		});
 		// Set up streaming request
 		long now = System.currentTimeMillis();
 		long streamInterval = 1000;
 		log.info("Requesting from {} every {} ms.", new Date(now), streamInterval);
 		StepResponse response = wmc.getStreamRequest(".*powerswitch.*", now,
 				streamInterval, ".*");
 		WorldState state = null;
 		// Streaming request loop
 		while (!response.isComplete()) {
 			try {
 				state = response.next();
 				log.debug("Recieved response");
 			} catch (Exception e) {
 				log.error("Error occured during request.", e);
 
 				// Restart request if there is an error
 				now = System.currentTimeMillis();
 				response = wmc.getStreamRequest(".*powerswitch.*", now,
 						streamInterval, ".*");
 				log.info("Restarting request. Requesting from " + new Date(now)
 						+ " every " + streamInterval + " ms.");
 				state = null;
 				continue;
 			}
 			Collection<String> uris = state.getURIs();
 			for (String uri : uris) {
 				log.debug("URI: {}", uri);
 				Collection<Attribute> attribs = state.getState(uri);
 				// TS represents time stamp
 				long onTS = 0, outletTS = 0, targetTS = 0, usernameTS = 0, passwordTS = 0;
 				boolean newOnStatus = true;
 				int outlet = -1;
 				String target = null, username = null, password = null;
 				for (Attribute att : attribs) {
 					try {
 						if ("on".equals(att.getAttributeName())) {
 							if (att.getCreationDate() > onTS) {
 								onTS = att.getCreationDate();
 								newOnStatus = BooleanConverter.CONVERTER.decode(att.getData());
 								log.debug("Decoded on: {}",newOnStatus);
 							}
 						} else if ("outlet".equals(att.getAttributeName())) {
 							if (att.getCreationDate() > outletTS) {
 								outletTS = att.getCreationDate();
 								outlet = IntegerConverter.CONVERTER.decode(att.getData());
 								log.debug("Decoded outlet: {}",outlet);
								outletmap.put(uri, outlet);
 							}
 						} else if ("target".equals(att.getAttributeName())) {
 							if (att.getCreationDate() > targetTS) {
 								targetTS = att.getCreationDate();
 								target = StringConverter.CONVERTER.decode(att.getData());
 								log.debug("Decoded target: {}",target);
								targetmap.put(uri,  target);
 							}
 						} else if ("username".equals(att.getAttributeName())) {
 							if (att.getCreationDate() > usernameTS) {
 								usernameTS = att.getCreationDate();
 								username = StringConverter.CONVERTER.decode(att.getData());
 								log.debug("Decoded username: {}",username);
								usernamemap.put(uri, username);
 							}
 						} else if ("password".equals(att.getAttributeName())) {
 							if (att.getCreationDate() > passwordTS) {
 								passwordTS = att.getCreationDate();
 								password = StringConverter.CONVERTER.decode(att.getData());
 								log.debug("Decoded password: {}",password);
								passwordmap.put(uri, password);
 							}
 						}
 					} catch (Exception e) {
 						log.error("Exception thrown while retrieving {}",
 								att.getAttributeName());
 					}
 				}
 				log.info("{} is {}", uri, newOnStatus ? "on" : "off");
				if(outlet==-1){
					outlet = outletmap.get(uri);
				}
				if(null==target){
					target = targetmap.get(uri);
				}
				if(null==username){
					username = usernamemap.get(uri);
				}
				if(null==password){
					password = passwordmap.get(uri);
				}
				
 				WebPowerSwitchIII(target, outlet, newOnStatus, username, password);
 			}
 		}
 	}
 	/**
 	 * Prints usage info to the console.
 	 */
 	public static void printUsageInfo() {
 		StringBuffer sb = new StringBuffer("Usage: <World Model Host> <World Model Port>" + "\n");
 		System.err.println(sb.toString());
 	}
 
 	/** Method that makes an HTTP request to the power switch, logging the
 	 * response.
 	 * 
 	 * 
 	*/ 
 	private static void WebPowerSwitchIII(String target, int outletnum,
 			boolean on, String username, String password) {
 		{
 			int numOutlets = 8;
 			if (target == null || username == null || password == null
 					|| outletnum <= 0 || outletnum > numOutlets) {
 				log.error("Attribute missing required information.");
 				return;
 			}
 			log.debug("Calling method with args: " + target + " "
 					+ outletnum + " " + on);
 			log.debug("Outlet number {}/{}", outletnum, numOutlets - 1);
 			// Establish an HTTP client for connections
 			HttpClient httpclient = new DefaultHttpClient();
 			// Define the GET request
 			// Format: "http://192.168.200.34:7005/outlet?8=ON"
 			if (!target.startsWith("http://"))
 				target = "http://" + target;
 			if (!target.endsWith("/"))
 				target = target + "/";
 			target = target + "outlet?" + outletnum;
 			if (on)
 				target = target + "=ON";
 			else
 				target = target + "=OFF";
 			log.info("Target URL: {}", target);
 			HttpGet httpget = new HttpGet(target);
 			// Add authentication to the GET request
 			try {
 				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
 						username, password);
 				BasicScheme scheme = new BasicScheme();
 				Header authorizationHeader = scheme.authenticate(credentials,httpget);
 				httpget.addHeader(authorizationHeader);
 			} catch (AuthenticationException e) {
 				e.printStackTrace();
 				return;
 			}
 
 			try {
 				// Send the request to the WebPowerSwitch, store the response
 				HttpResponse response = httpclient.execute(httpget);
 				// Examine the response status
 				log.info("Status line: {}", response.getStatusLine());
 				// Print out all the headers in the response
 				HeaderIterator it = response.headerIterator();
 				while (it.hasNext()) {
 					log.debug("Response header: " + it.next());
 				}
 				// Get hold of the response entity
 				HttpEntity entity = response.getEntity();
 				// If the response does not enclose an entity, there is no need
 				// to worry about connection release
 				if (entity != null) {
 					InputStream instream = entity.getContent();
 					try {
 						BufferedReader reader = new BufferedReader(
 								new InputStreamReader(instream));
 						// do something useful with the response content
 						String str = null;
 						while ((str = reader.readLine()) != null) {
 							log.info("Response line: " + str);
 						}
 					} catch (IOException ex) {
 						// In case of an IOException the connection will be
 						// released
 						// back to the connection manager automatically
 						throw ex;
 					} catch (RuntimeException ex) {
 						// In case of an unexpected exception you may want to
 						// abort
 						// the HTTP request in order to shut down the underlying
 						// connection and release it back to the connection
 						// manager.
 						httpget.abort();
 						throw ex;
 					} finally {
 						// Closing the input stream will trigger connection
 						// release
 						instream.close();
 					}
 					// When HttpClient instance is no longer needed,
 					// shut down the connection manager to ensure
 					// immediate deallocation of all system resources
 					httpclient.getConnectionManager().shutdown();
 				}
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
