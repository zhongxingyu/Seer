 package edu.rosehulman.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.HttpURLConnection;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.rosehulman.server.POI;
 
 
 import android.annotation.SuppressLint;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.os.Handler;
 import android.util.Log;
 
 @SuppressLint("UseSparseArrays")
 public class Server {
 	private static String connect_to = "queen.wlan.rose-hulman.edu";
 	private static int connect_port = 5047;
 
 	private static String message;
 	public static Map<Integer,POI> POIelements;
 	
 	public Server()
 	{
 		if (Server.POIelements == null)
 		{
 			Server.POIelements = new HashMap<Integer,POI>();
 		}
 	}
 
 	public static boolean updatePOIFromString(String data)
 	{
 		try {
 			JSONObject text = new JSONObject(data);
 			JSONObject POIelements = text.getJSONObject("POI");
 	
 			JSONArray names = POIelements.names();
			Log.d("POIelements", "size: " + POIelements.length() + " | names: " + names);
 			
 			for (int i = 0; i < POIelements.length(); i++)
 			{
 				String UIDstring = names.getString(i);
 	
//				Log.d("POIelement", "name: " + UIDstring);
 				int UIDint = Integer.parseInt(UIDstring);
 	
 				JSONObject POIelement = POIelements.getJSONObject(UIDstring);
//				Log.d("POIelement", "element: " + POIelement);
 			
 				POI point = new POI(POIelement);
 				Server.POIelements.remove(UIDint);
 				Server.POIelements.put(UIDint, point);
 			}
 			return true;
 		} catch (JSONException e) {
 			Log.d("Server updatePOI", "malformed or bad data:" + data);
 			return false;
 		}
 	}
 	
 	public class ListenPOISocket extends Thread {
 		private Socket socket;
 	   	private ObjectInputStream in;
 	  	private ObjectOutputStream out;
 	  	public Handler updatePOIHandler;
 	  	public volatile boolean stopThread = false;
 	  	
 	  	void sendMessage(String msg)
 	  	{
 	  		try{
 	  			out.writeObject(msg);
 	  			Log.d("POI", "socket sending: |" + msg + "|");
 	  		}
 	  		catch(Exception e){
 	  			e.printStackTrace();
 	  		}
 	  	}
 
 	  	public void run() {
 	  		try{
 	  			//open a socket connecting us to the server
 	  			socket = new Socket(connect_to, connect_port);
 	  			Log.d("POI", "Socket Connected!");
 
 	  			//create in and out streams
 	  			out = new ObjectOutputStream(socket.getOutputStream());
 	  			in = new ObjectInputStream(socket.getInputStream());
 	
 	  			//send (and receive from server)
   				try{
   					sendMessage("hello\n");
   					message = (String)in.readObject();
   					if (!message.equals("ACKhello\n"))
   						Log.d("POI", "socket server did not ackHello instead recieved|" + message + "|");
   					else
   					{
   						Log.d("POI", "socket server replied ackHello");
 
 	  					while (!this.stopThread)
 	  					{
 	  	  					message = (String)in.readObject();
 	  						Log.d("POI",  "socket server says: omitted"); //" + message);
 	  						if (Server.updatePOIFromString(message))
 	  						{
 	  							Log.d("POI", "socket dispatching updateDisplay handler");
 	  							updatePOIHandler.sendMessage(updatePOIHandler.obtainMessage());
 	  						}
 	  						Thread.sleep(50);
 	  					}
   					}
 				}
   				catch(ClassNotFoundException classNot){
   				}
 	  		}
 	  		catch(Exception e){
 	  			e.printStackTrace();
 	  		}
 	  		finally{
 	  			try{
 	  				in.close();
 	  				out.close();
 	  				socket.close();
 	  			}
 	  			catch(Exception e){
 	  				e.printStackTrace();
 	  			}
 	  		}
 		}
 	};
 }
