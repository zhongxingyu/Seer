 package com.tabbie.android.radar;
 
 /**
  * ServerThread.java
  * 
  * Created on: June 9, 2012
  * Author: vkarpov
  * 
  * A thread for handling server requests and responses. Consumes incoming ServerRequests,
  * and calls back to its handler when it receives a response.
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class ServerThread extends Thread {
  public static final String TABBIE_SERVER = "http://tonight-life.com";
   public static final int    NO_INTERNET   = -2;
   
 	private Handler upstreamHandler;
 	private HttpURLConnection conn;
 	private boolean active = true;
 	private boolean waiting = false;
 	private final Queue<ServerResponse> responses = new LinkedList<ServerResponse>();
 	private final Queue<ServerRequest> requests = new LinkedList<ServerRequest>();
 	
 	private Object handlerLock = new Object();
 	
 	// Methods exposed to clients.
 	public ServerThread(String name, Handler upstreamHandler) {
 		super(name);
 		this.upstreamHandler = upstreamHandler;
 		// Java occasionally includes HTTP headers in response. This prevents that from happening. Don't ask me why.
 		System.setProperty("http.keepAlive", "false");
 	}
 	
 	public void setUpstreamHandler(Handler handler) {
 	  synchronized(handlerLock) {
 	    this.upstreamHandler = handler;
 	  }
 	}
 	
 	public void sendRequest(ServerRequest req) {
 		requests.add(req);
 	}
 	
 	public void setActive() {
 		this.active = true;
 		this.setPriority(NORM_PRIORITY);
 	}
 	
 	public void setInactive() {
 		this.active = false;
 		this.setPriority(MIN_PRIORITY);
 	}
 	
 	public void parley() {
 	  while (!waiting) {
 	  }
 	  this.stop();
 	}
 	// end methods exposed to clients
 	
 	@Override
 	public void run() {
 		this.setPriority(NORM_PRIORITY);
 		while (true) {
 		  if (!this.active) {
 		    try {
           Thread.sleep(100);
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
 		  } else {
   			while (requests.peek() != null) {
   				handleRequest(requests.poll());
   			}
   			while (upstreamHandler != null && responses.peek() != null) {
       		/* No lock should be required here - fireHandler only gets called
       		 * as a helper method in the ServerHandler thread.
       		 */
       		Message msg = new Message();
       		msg.obj = responses.poll();
       		Log.v(this.getClass().getName(), "run() found a queued response - '" + ((ServerResponse) msg.obj).content + "'");
       		upstreamHandler.dispatchMessage(msg);
   			}
   			// Make sure you yield after running once - helps keep app responsive on old phones.
   			Thread.yield();
 		  }
 		}
 	}
 	
 	private void fireHandler(ServerResponse response) {
 	  synchronized(handlerLock) {
   		if (upstreamHandler == null) {
   			Log.v("ServerThread", "No upstream handler!");
   			responses.add(response);
   		} else {
   			Message msg;
   			while (responses.peek() != null) {
   				msg = new Message();
   				msg.obj = responses.poll();
   				Log.v(this.getClass().getName(), "Dispatching message in loop '" + ((ServerResponse) msg.obj).content + "'");
   				upstreamHandler.dispatchMessage(msg);
   			}
   			
   			msg = new Message();
   			msg.obj = response;
   			Log.v(this.getClass().getName(), "Dispatching message '" + response.content + "'");
   			upstreamHandler.dispatchMessage(msg);
   		}
   		waiting = false;
 	  }
 	}
 
 	private boolean handleRequest(ServerRequest req) {
 	  waiting = true;
 	  Log.v(this.getClass().getName(), "Got request for URL " + req.url);
 		try {
 			conn = (HttpURLConnection) new URL(req.url).openConnection();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			fireHandler(new ServerResponse(NO_INTERNET, "Malformed URL", req.type));
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			fireHandler(new ServerResponse(NO_INTERNET, "Could not open connection", req.type));
 			return true;
 		}
 		
 		try {
 		  // POST, GET, etc
 			conn.setRequestMethod(req.reqMethod);
 			for (String key : req.httpParams.keySet()) {
 			  conn.setRequestProperty(key, req.httpParams.get(key));
 			}
 			
 			if (req.hasOutput()) {
 			  Log.v(this.getClass().getName(), "Writing '" + req.getOutput() + "'");
 			  conn.setDoOutput(true);
         OutputStream stream = conn.getOutputStream();
         stream.write(req.getOutput().getBytes());
         stream.flush();
       } else {
         conn.connect();
       }
 		} catch (ProtocolException e) {
 			// Should never happen
 			fireHandler(new ServerResponse(-1, "ProtocolException", req.type));
 			e.printStackTrace();
 			return true;
 		} catch (IOException e) {
 		  fireHandler(new ServerResponse(NO_INTERNET, "ProtocolException", req.type));
       e.printStackTrace();
       return true;
     }
 
 		try {
       Log.v(this.getClass().getName(), "Got HTTP response code " + conn.getResponseCode() + " " + conn.getResponseMessage());
       if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
         fireHandler(new ServerResponse(conn.getResponseCode(), conn.getResponseMessage(), req.type));
         return true;
       }
     } catch (IOException e1) {
       // TODO Auto-generated catch block
       e1.printStackTrace();
     }
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			fireHandler(new ServerResponse(-1, "Reader IOException", req.type));
 			return true;
 		}
 		StringBuilder sb = new StringBuilder();
 		while (true) {
 			String y = "";
 			try {
 				y = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if (y == null) {
 				break;
 			}
 			sb.append(y);
 		}
 		String s = sb.toString();
 		s = s.replace("\t", "").replace("\n", "");
 		Log.v(this.getClass().getName(), "Read from url : '" + sb.toString() + "'");
 		fireHandler(new ServerResponse(0, sb.toString(), req.type));
 		
 		return true;
 	}
 	
 	
 
 }
