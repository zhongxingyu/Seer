 package edu.pugetsound.vichar;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.protocol.HTTP;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 
 /**
  * A service that syncronizes a local JSONObject with it's remote counterpart
  * 
  * IMPORTANT NOTE: This service uses org.json.simple.JSONObject, 
  * NOT org.json.JSONObject! Don't be fooled. The simple library has better
  * merging support.
  * 
  * @author Kirah Taylor & Michael DuBois
  * @version 10/24/12
  */
 public class NetworkingService extends Service {
 
 	public static final String DEFAULT_SERVER_IP = "10.150.2.55";
 	public static final String DEFAULT_PORT = "1730";
 	public static final String DEFAULT_DOMAIN 
 		= "http://" + DEFAULT_SERVER_IP + ":" + DEFAULT_PORT;
 	
 	private Timer timer = new Timer();
 	private boolean polling = false;
 	private JSONParser jsonParser = new JSONParser();
 	public static final long POLL_INTERVAL = 100L; //in milliseconds
 
 	// Messenger to handle inbound messages
     final Messenger mMessenger = new Messenger(new IncomingHandler());
     // Roster of all registered clients.
  	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
  	int mValue = 0; // Holds last value set by a client.
 	
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_SET_JSON_STRING_VALUE = 3;
 	public static final int MSG_RET_JSON_STRING_FROM_SERVER = 4;
 	public static final int MSG_QUEUE_OUTBOUND_J_STRING = 5;
     
     /**
      * IncomingHandler performs internal functions in response to the received 
      * messages of each predefined type denoted by NetworkingServices MSG_ 
      * constants.
      * @author Michael DuBois
      */
     class IncomingHandler extends Handler { // Handler of incoming messages from clients.
         @Override
         public void handleMessage(Message msg) {
         	mValue = msg.what; // Remember this msg type
         	switch (mValue) {
 	            
         		case MSG_RET_JSON_STRING_FROM_SERVER: {
 	            	serveBundle(msg.getData());
 	            	break;
 	            }
 	            
 	            case MSG_QUEUE_OUTBOUND_J_STRING: {
 	            	String jStr = msg.getData()
 	            			.getString("" + MSG_QUEUE_OUTBOUND_J_STRING);
 	            	queueOutboundJString(jStr);
 	            	break;
 	            }
 	            
 	            case MSG_REGISTER_CLIENT: {
 	                addClient(msg.replyTo);
 	                startPolling(); // Starts polling if not already
 	                break;
 	            }
 	            
 	            case MSG_UNREGISTER_CLIENT: {
 	            	removeClient(msg.replyTo);
 	                break;
 	            }
 	            
 	            default: {
 	                super.handleMessage(msg);
 	            }
 	        }
         }
     }
     
     /**
      * Add a client to the client array
      * @param m
      */
     private void addClient(Messenger m) {
     	mClients.add(m);
     }
     
     /**
      * Remove a client from the client array and check the validity of service's 
      * existence
      * @param m reference to the client to be removed
      */
 	private void removeClient(Messenger m) {
 		mClients.remove(m);
 		checkVitals();
 	}
 	
 	/**
 	 * Remove a client from the client array by index and check the validity of 
 	 * service's existence
 	 * @param i the index (in the client array) for the client to be removed
 	 */
 	private void removeClient(int i) {
 		mClients.remove(i);
 		checkVitals();
 	}
 	
 	/**
 	 * Checks if we have clients, kills self if we don't.
 	 */
 	private void checkVitals() {
 		if(mClients.size() <= 0) {
 			// We have no clients, time to die.
 			this.stopSelf();
 		}
 	}
     
     /**
      * Serves a Bundle to all mClients
      * @param b an Android Bundle containing a JSON_STRING
      */
     private void serveBundle(Bundle b) {
     	Message msg = Message.obtain(null, MSG_SET_JSON_STRING_VALUE);
     	msg.setData(b);
     	
     	//Send msg to subscribers
         for (int i=mClients.size()-1; i>=0; i--) {
             try {
                 mClients.get(i).send(msg);
 
             } catch (RemoteException e) {
                 // The client is dead. Remove it from the list\
             	Log.i(this.toString(), "Activity client is dead.");
                 removeClient(i);
             }
         }
     }
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 	
 //------------------------------------------------------------------------------
 // POLLING
 //------------------------------------------------------------------------------
 
    /**
     * Starts the polling clock. Subsequent calls have no effect.
     */
 	public void startPolling() {
 		if(!polling) {
 			// TODO change to ScheduledThreadPoolExecutor and Future
 			// run pollingTask every 100ms
 			timer.scheduleAtFixedRate(pollingTask, 0, POLL_INTERVAL);
 			polling = true;
 		}
 	}
 	
 	/**
 	 * Stops the polling clock. Subsequent calls have no effect.
 	 */
 	public void stopPolling() {
 		pollingTask.cancel();
 		polling = false;
 	}
 	
 	public void queueOutboundJString(String jStr) {
 		try {
 			JSONObject json = (JSONObject) jsonParser.parse(jStr);
 			pollingTask.outboundQueue.offer(json);
 		} catch(ParseException e) {
 			// do something
 		}
 		
 		// TODO Should we merge here? Might cause the queue to be empty when we
 		// try POSTing... but doesn't delay the PollingTask. Might slow down the
 		// UI thread if this Service is on the UI thread? How long does it take?
 //		try {
 //			JSONObject outboundJson = new JSONObject();
 //			// merge all things in the queue into one object to reduce POSTs
 //			while(pollingTask.outboundQueue.peek() != null) {
 //				outboundJson.putAll(pollingTask.outboundQueue.poll());
 //			}
 //			// Merge the new json into that object
 //			outboundJson.putAll((JSONObject) jsonParser.parse(jStr));
 //			// Offer it back to the queue
 //			pollingTask.outboundQueue.offer(outboundJson);
 //		} catch(ParseException e) {
 //			// do something
 //		}
 	}
 	
 	/**
      * An extension of TimerTask that allows us to queue some things.
      * @author DuBious
      */
     private abstract class PollingTask extends TimerTask {
 		public ConcurrentLinkedQueue<JSONObject> outboundQueue 
 			= new ConcurrentLinkedQueue<JSONObject>();
 		// Track consecutive failures
 		protected int consecutiveFailures = 0;
 	}
 	
 	/**
 	 * A Runnable task that is run every time the polling clock ticks.
 	 * This task pulls JSON from the server and passes it back to the
 	 * caller as a JSON string. When outboundJSON objects are queued
 	 * for processing, the task merges and POSTs them first, then performs 
 	 * the GET request.
 	 * 
 	 * @author Michael DuBois
 	 */
 	private PollingTask pollingTask = new PollingTask(){
 		
 		public void run() {
 			String returnStr = "{}"; // empty JSON string by default
 			String urlString = DEFAULT_DOMAIN;
 
 			try { // POST/GET the latest states
 				URL url = new URL(urlString);
 				HttpURLConnection conn = 
 						(HttpURLConnection) url.openConnection();
 				
 				// Process the outbound queue
 				JSONObject outboundJson = outboundQueue.poll(); // null if empty
 				// While something remains in the queue, 
 				while(outboundQueue.peek() != null) {
 					// merge it into outboundJson 
 					// this way we don't have to do multiple POSTs
 					outboundJson.putAll(outboundQueue.poll());
 				}
 				
 				// If we have something to send
 				if(outboundJson != null) {
 					String outboundJStr = outboundJson.toString();
 					//Log.d(this.toString(), "SENDING JSON: " + outboundJStr);
 					conn.setDoOutput(true);	
 					//conn.setDoInput(true);
 					conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
 					conn.setRequestProperty("Accept", "application/json");
 					conn.setRequestMethod("POST");
 					conn.connect();
 					OutputStream out = conn.getOutputStream();
 					StringEntity se = new StringEntity(outboundJStr);
 					se.setContentEncoding(
 							new BasicHeader(HTTP.CONTENT_TYPE, 
 									"application/json;charset=UTF-8"));
 					se.writeTo(out);
 					out.flush();
 					out.close();
 					outboundJStr = null;
 					
 					// It seems like getting the response code ensures that the 
 					// data is transferred. It's also just a good idea to check 
 					// it.
 					int status = conn.getResponseCode();
 					if(status == 200) {
 						consecutiveFailures = 0;
 					} else {
 						// Hope to get it on the next try
 						consecutiveFailures++;
 						//TODO do something if it fails over and over
 					}
					System.out.println("ResponseCode: " + status);
 					
 					// Close connection - so it doesn't mess with the next one
 					conn.disconnect();
 				}
 
 				//TODO right now server check for POST/GET, should return 
 				//something on POST too In the meantime, we do two requests.
 				URL url2 = new URL(urlString);
 				HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
 				
 				// Parse the response into a JSON string:
 				// First, get input stream
 				InputStream in = conn2.getInputStream();
 				
 				// Use the encoding listed in the HTTP header
 				String encoding = conn2.getContentEncoding();
 				// Or, if missing, default to UTF-8 instead of system default
 				encoding = encoding == null ? "UTF-8" : encoding;
 				
 				// Read out the body as a string
 				String body = IOUtils.toString(in, encoding);
 				in.close(); // close the stream
 				//Log.d(this.toString(),"body: " + body.toString());
 				returnStr = body;
 
 				// It seems like getting the response code ensures that the 
 				// data is transferred. It's also just a good idea to check it.
 				int status = conn2.getResponseCode();
 				if(status == 200) {
 					consecutiveFailures = 0;
 				} else {
 					consecutiveFailures++;
 				}
				System.out.println("ResponseCode: " + status);
 				
 				// Close connection
 				conn2.disconnect();
 			}
 			catch (ClientProtocolException e) {
 				//TODO: connection problem exception
 				Log.i(this.toString(), "ClientProtocolException");
 			}
 			catch (UnsupportedEncodingException e) {
 				//TODO: connection problem exception
 				Log.i(this.toString(), "UnsupportedEncodingException");
 			} 
 			catch (IOException e) {
 				//TODO: connection problem exception
 				Log.i(this.toString(), "IOException");
 			}
 	 		
 	 		//TODO check for failure
 	 		
 	 		// return the resulting String via Messenger
 	 		// first, Bundle JSON string as a string
 	    	Bundle b = new Bundle();
 	    	b.putString("" + MSG_SET_JSON_STRING_VALUE, returnStr);
 	    	Message msg = Message.obtain(null, MSG_RET_JSON_STRING_FROM_SERVER);
 	    	msg.setData(b);
 	    	try {
 				mMessenger.send(msg);
 			} catch(RemoteException e) {
 				// The caller thread is dead?
 				// There's really nothing we can do if this happens...
 				Log.i(this.toString(), "RemoteException from timer thread.");
 			}
 		}
 	};
 }
 
