 package nuig.ece.third;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.concurrent.*;
 
 public class NetworkXMLOperationsQueue implements Runnable {
 
 	public static final int		maxRetries = 6;
 	public static final Long	timeOutTime = 5000L;
 	
 	private static boolean 		shouldExit = false;
 	private static int			operationsID = 0;
 	private static ConcurrentLinkedQueue<NetworkXMLOperation> operationsQueue;
 	private static HashMap<NetworkXMLOperation, HashMap<String, Object>> operationsMetadata;
 	private static String[] APIEndpoints;
 	
 	public void run() {
 		operationsQueue = new ConcurrentLinkedQueue<NetworkXMLOperation>();
 		operationsMetadata = new HashMap<NetworkXMLOperation, HashMap<String, Object>>();
 		loadAPIEndpoints();
 		
 		while ( shouldExit == false ) {
 			
 			if ( !operationsQueue.isEmpty() ) {
 				NetworkXMLOperation currentOp = operationsQueue.remove();
 				HashMap<String, Object> currentOpData = operationsMetadata.get(currentOp);
 				
 				if ( currentOp.isCancelled() ) {
 					operationsMetadata.remove( currentOp );
 					currentOp.getCallback().call( null ); // indicate operation cancelled
 					continue;
 				}
 				
 				Thread currentOpThread = new Thread( currentOp );
 				
 				currentOpThread.start();
 				
 				boolean timedOut = false;
 				Long startTime = (Long) currentOpData.get("startTime");
 				
 				while ( !currentOp.isCompleted() && !timedOut ) {
 					if ( System.currentTimeMillis() - startTime > timeOutTime ) {
 						timedOut = true;
 						continue;
 					}
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {}
 				}
 				
 				Response currentOpResponse = currentOp.getResponse();
 				
 				if ( currentOpResponse.getError() != null || currentOp.isCompleted() == false ) {
 					Integer numRetries = (Integer) currentOpData.get("retries");
 					
 					currentOp.log("Retries: " + numRetries);
 					
 					currentOp.log("Error detected: " + currentOpResponse.getError().getMessage() );
 					
 					if ( numRetries.intValue() >= maxRetries ) {
 						TooManyRetriesException tmr = new TooManyRetriesException("NW Op " + currentOp.getID() + " - " + currentOp.getRequestURL() + " failed too many times!");
 					
 						currentOpResponse.setError( tmr );
 						
 						currentOp.getCallback().call( currentOpResponse );
 						operationsMetadata.remove(currentOp);
 						
 						currentOpThread.interrupt();
 						currentOpThread = null;
 					
 					} else {
 						currentOp.setCompleted(false);
 						numRetries++;
 						
 						currentOpData.put("retries",numRetries);
 						currentOpData.put("startTime", System.currentTimeMillis());
 						operationsMetadata.put(currentOp, currentOpData);
 						
 						operationsQueue.add(currentOp);
 					}
 				
 				} else { // no error, successful response
 					operationsMetadata.remove(currentOp);
 					currentOpThread.interrupt();
 					currentOpThread = null;
 					
 					currentOp.getCallback().call( currentOp.getResponse() );
 				}
 				
 			} else {
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {}
 			}
 				
 		}
 		
 		operationsQueue = null;
 		operationsMetadata = null;
 			
 	}
 
 	// Queue methods
 	
 	public synchronized NetworkXMLOperation add( String requestURL, Callback callback ) throws MalformedURLException, InvalidDreamboxAPICallException {
 		NetworkXMLOperation operation = new NetworkXMLOperation( nextID(), new URL( requestURL ), callback );
 		
 		String endpoint = parseEndpoint(requestURL);
 		
 		if ( isSupportedAPIEndpoint(endpoint) == false )
 			throw new InvalidDreamboxAPICallException(endpoint);
 		
 		HashMap<String, Object> opData = new HashMap<String, Object>();
 		
 		opData.put("startTime", System.currentTimeMillis());
 		opData.put("retries", new Integer(0));
 		
 		operationsMetadata.put(operation, opData);
 		
 		operationsQueue.add(operation);
 		
 		return operation;
 	}
 	
 	private static int nextID() {
 		return operationsID = (operationsID + 1) % 10000;
 	}
 	
 	private String parseEndpoint( String apiURL ) {
 		String[] 	paths = apiURL.split("/");
 		String resource;
 		
 		if ( paths.length > 0 )
 			resource = paths[ paths.length - 1 ];
 		else
 			return null;
 		
 		return resource.split("\\?")[0];
 	}
 	
 	public static boolean isSupportedAPIEndpoint( String endpoint ) {
 		String[] apiEndpoints = getAPIEndpoints();
 		
 		for ( String supportedEndpoint : apiEndpoints )
 			if ( endpoint.equals(supportedEndpoint) )
 				return true;
 		
 		return false;
 	}
 	
 	// Accessors
 	public static void setShouldExit(boolean shouldExit) {
 		NetworkXMLOperationsQueue.shouldExit = shouldExit;
 	}
 	
 	
 	public static String[] getAPIEndpoints() {
 		return APIEndpoints;
 	}
 
 	public static void setAPIEndpoints(String[] aPIEndpoints) {
 		return; // behave like a final variable
 	}
 
 	// Initialisation of constant data
 	
 	private static void loadAPIEndpoints() {
 		String[] endpoints = {
 			"about",
 			"addlocation",
 			"autotimerlist",
 			"backup",
 			"currenttime",
 			"deviceinfo",
 			"downmix",
 			"epgbouquet",
 			"epgmulti",
 			"epgnext",
 			"epgnow",
 			"epgnownext",
 			"epgsearch.rss",
 			"epgsearch",
 			"epgservice",
 			"epgservicenext",
 			"epgservicenow",
 			"epgsimilar",
 			"external",
 			"getallservices",
 			"getaudiotracks",
 			"getcurrent",
 			"getcurrlocation",
 			"getlocations",
 			"getpid",
 			"getservices",
 			"gettags",
 			"mediaplayeradd",
 			"mediaplayercmd",
 			"mediaplayercurrent",
 			"mediaplayerlist",
 			"mediaplayerload",
 			"mediaplayerplay",
 			"mediaplayerremove",
 			"mediaplayerwrite",
 			"message",
 			"messageanswer",
 			"moviedelete",
 			"movielist.html",
 			"movielist.m3u",
 			"movielist.rss",
 			"movielist",
 			"moviemove",
 			"movietags",
 			"parentcontrollist",
 			"pluginlistread",
 			"powerstate",
 			"recordnow",
 			"remotecontrol",
 			"removelocation",
 			"restarttwisted",
 			"restore",
 			"selectaudiotrack",
 			"servicelistplayable",
 			"servicelistreload",
 			"serviceplayable",
 			"services.m3u",
 			"session",
 			"settings",
 			"signal",
 			"sleeptimer",
 			"stream.m3u",
 			"stream",
 			"streamcurrent.m3u",
 			"streamsubservices",
 			"strings.js",
 			"subservices",
 			"timeradd",
 			"timeraddbyeventid",
 			"timerchange",
 			"timercleanup",
 			"timerdelete",
 			"timerlist",
 			"timerlistwrite",
 			"tpm",
 			"ts.m3u",
 			"tvbrowser",
 			"updates.html",
 			"vol",
 			"zap"
 		};
 		
 		APIEndpoints = endpoints;
 	}
 }
