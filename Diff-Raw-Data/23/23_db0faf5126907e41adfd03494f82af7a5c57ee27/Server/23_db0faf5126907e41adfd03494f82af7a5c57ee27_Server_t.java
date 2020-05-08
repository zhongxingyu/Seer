 package edu.rosehulman.server;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.List;
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
 
 
 import android.R;
 import android.annotation.SuppressLint;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 @SuppressLint("UseSparseArrays")
 public class Server {
 	private static Server instance = null;
 	
 	private static String connect_to = "bad address";
 	private final static int connect_port = 5047;
 
 	public static double[] GPS;
 	public static boolean GPSsent = false;
 	
 	public static Map<Integer,POI> POIelements;
   	public static Handler updatePOIHandler;
 
 	private Server()
 	{
		Server.POIelements = new HashMap<Integer,POI>();
 		GPS = new double[2];
 	}
 
 	public void setServerAddress(String address)
 	{
 		Server.connect_to = address;
 	}
 	
 	public static Server getInstance()
 	{
 		if (Server.instance == null)
 		{
 			Server.instance = new Server();
 		}
 		return Server.instance;
 	}
 	
 	public boolean updateGPSFromString(String data)
 	{
 		try {
 			JSONObject text = new JSONObject(data);
 			JSONObject JSONGPSelements = text.getJSONObject("GPS");
 	
 			JSONArray names = JSONGPSelements.names();
 //			Log.d("GPSelements", "size: " + JSONGPSelements.length() + " | names: " + names);
 			Log.d("GPS data", data);
 
 			Server.GPS[0] = JSONGPSelements.getDouble("latitude");
 			Server.GPS[1] = JSONGPSelements.getDouble("longitude");
 			
 			return true;
 		} catch (JSONException e) {
 			Log.d("Server updateGPS", "malformed or bad data:" + data);
 			return false;
 		}
 	}
 	
 	public boolean updatePOIFromString(String data)
 	{
		try {		
			Log.d("POI data", data);
 			
 			JSONObject text = new JSONObject(data);
 			JSONObject JSONPOIelements = text.getJSONObject("POI");
 	
 			JSONArray names = JSONPOIelements.names();
 			Log.d("POIelements", "size: " + JSONPOIelements.length() + " | names: " + names);
 			
 			for (int i = 0; i < JSONPOIelements.length(); i++)
 			{
 				String UIDstring = names.getString(i);
 	
 //				Log.d("POIelement", "name: " + UIDstring);
 				int UIDint = Integer.parseInt(UIDstring);
 	
 				JSONObject JSONPOIelement = JSONPOIelements.getJSONObject(UIDstring);
 //				Log.d("POIelement", "element: " + POIelement);
 			
 				POI point = new POI(JSONPOIelement);
 				Server.POIelements.remove(UIDint);
 				Server.POIelements.put(UIDint, point);
 			}
 			return true;
 		} catch (JSONException e) {
 			Log.d("Server updatePOI", "malformed or bad data:" + data);
 			return false;
 		}
 	}
 	
   	public boolean sendMessage(String message) throws Exception
   	{
   		// TODO this is bad as we fail to send the message
   		if (ListenPOISocket.getInstance().acked == true)
   		{
   	  		ListenPOISocket.getInstance().sendMessage(message);
   	  		return true;
 		} else {
 			return false;
 		}
   	}
 
   	public void startServer()
   	{
   		ListenPOISocket.getInstance();
   	}
 
   	public void stopServer()
   	{
 	  	if (ListenPOISocket.instance != null)
 		{
 	  		try {
 				this.sendMessage("close");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	  		ListenPOISocket.instance.stopThread = true;
 	  		ListenPOISocket.instance = null;
 		}
   	}
 
   	public boolean serverRunning()
   	{
   		return ListenPOISocket.instance != null;
   	}
 
   	public void startPOISync()
   	{
   		try {
 			if (this.sendMessage("sendPOI")) {
 				ListenPOISocket.getInstance().pullFrom = true;
 			}	
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
   	}
   	
   	public void stopPOISync()
   	{
   		try {
   			if (this.serverRunning())
   				this.sendMessage("stopPOI");
   				ListenPOISocket.getInstance().pullFrom = false;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
   	}
 
   	public void addOverlay(String overlay)
   	{
   		try {
 			sendMessage("addOverlay:" + overlay);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	}
 
   	public void removeOverlay(String overlay)
   	{
   		try {
 			sendMessage("removeOverlay:" + overlay);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	}
 
   	
   	public boolean startedPOISync()
   	{
   		return (ListenPOISocket.getInstance().pullFrom == true);
   	}
 
   	protected static class ListenPOISocket extends Thread {
 		private static ListenPOISocket instance = null;
 		private Socket socket;
 	   	private DataInputStream in;
         private DataOutputStream out;
 
         public volatile boolean acked = false;
 
         public volatile boolean stopThread = false;
 	  	public volatile boolean pullFrom = false;
 	  	public volatile boolean pushTo = false;
 	  	
 	   	private ListenPOISocket()
 	  	{
 	  	}
 
 	  	public static ListenPOISocket getInstance()
 	  	{
 	  		if (ListenPOISocket.instance == null)
 	  		{
 		  		ListenPOISocket.instance = new ListenPOISocket();
 		  		ListenPOISocket.instance.start();
 	  		}
 	  		return ListenPOISocket.instance;
 	  	}
 
 	  	public void sendMessage(String msg) throws IOException
 	  	{  		
 	  		Log.d("POI", "sending message: '" + msg + "'");
   			out.writeBytes(msg);
   			out.flush();
   			Log.d("POI", "sent");
 	  	}
 
 	  	private String getMessage() throws IOException
 	  	{
 			String msg = in.readLine();
 			return msg;
 	  	}
 
 	  	public void run() {
 	  		try{
 	  			//open a socket connecting us to the server
 	  			Log.d("POI", "Opening Socket to: '" + connect_to + "'");	
 	  			socket = new Socket(connect_to, connect_port);
 	  			Log.d("POI", "Socket Connected!");
 
 	  			//create in and out streams
 	            out = new DataOutputStream(socket.getOutputStream());
 	            in = new DataInputStream(socket.getInputStream());
 
 	  			sendMessage("hello");
 				String message = getMessage();
 				if (message.equals("ACKhello"))
 				{
 					Log.d("POI", "socket server replied ackHello");
 				}
 				else
 				{
 					Log.d("POI", "socket server did not ackHello instead received |" + message + "|");
 					return;
 				}
 		  		this.acked = true;
 
 		  		while ( this.stopThread == false )
 		  		{	
 		  			Thread.sleep(50);
 	
 					Log.d("POI", "waiting on message");
 					message = getMessage();
 					if (message == null)
 					{
						Log.d("SERVER", "got null message");
 						break;
 					}
					Log.d("SERVER",  "socket server got message : '" + message.substring(0, Math.min(20, message.length())) + "...'");
 
 					if (message.contains("\"GPS\":") && Server.getInstance().updateGPSFromString(message))
 					{
 						Log.d("POI", "socket got GPS data");
 					}
 
 					if (this.pullFrom && message.contains("\"POI\":") && Server.getInstance().updatePOIFromString(message))
 					{
 						Log.d("POI", "socket dispatching updateDisplay handler");
 						Server.updatePOIHandler.sendMessage(Server.updatePOIHandler.obtainMessage());
 					}
 
 					// TODO Seth or Sam do this in a different way
					// this was an attempt to flush data we don't have time to parse because it arrives to quickly
					//byte dst[] = new byte[10000];
					//in.read(dst);						
 		  		}
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 	  		finally
 			{
 				try {
 					Log.d("POI", "in finally");
 					ListenPOISocket.instance = null;
 					
 					if (out != null)
 					{
 						out.close();
 						sendMessage("close");
 					}
 
 					if (in != null)
 						in.close();
 
 					if (socket != null)
 						socket.close();
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	};
 }
