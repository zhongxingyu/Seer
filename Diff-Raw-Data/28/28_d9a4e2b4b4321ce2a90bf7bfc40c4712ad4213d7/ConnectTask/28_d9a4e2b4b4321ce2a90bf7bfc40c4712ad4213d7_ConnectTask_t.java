 package di.kdd.smartmonitor.protocol;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class ConnectTask extends AsyncTask<Void, Void, Socket> {
 
 	DistributedSystem ds;
 	String ip;
 	
 	private static final String TAG = "connect task";
 	
 	public ConnectTask(DistributedSystem ds) {
 		this.ds = ds;
 	}
 	
 	public ConnectTask(DistributedSystem ds, String ip) {
 		this.ds = ds;
 		this.ip = ip;
 	}
 	
 	/***
 	 * Sends JOIN messages to the first 255 local IP addresses and
 	 * according to if it will get a response or not, the node becomes
 	 * a peer or a Master respectively 
 	 */
 
 	@Override
 	protected Socket doInBackground(Void... params) {
 		Socket socket;
 		String ipPrefix = "192.168.1."; //TODO FIXME
 		
 		android.os.Debug.waitForDebugger();
 
 		if(ip == null) {		
 			/* Look for the Master in the first 255 local IP addresses */
 			//TODO parallelize it
 			for(int i = 1; i < 256; ++i) {
 				String tempIP = ipPrefix + Integer.toString(i);
 
 				try{
 					Log.i(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));
 
 					socket = new Socket(tempIP, ISmartMonitor.JOIN_PORT);
 				}
 				catch(IOException e) {
 					Log.i(TAG, "Failed to connect at " + tempIP);
 					continue;
 				}
 	
 				/* Master found */
 				
 				return socket;
 			}
 			
 			/* No response, this is the first node of the distributed system and the Master */
 	
 			return null;
 		}
 		else {
 			/* Send JOIN request at specific IP address */
 
 			try {
 				socket = new Socket(ip, ISmartMonitor.JOIN_PORT);
 				
 				return socket;
 			}
 			catch(IOException e) {
 				Log.e(TAG, "Failed to connect at " + ip);
 				e.printStackTrace();
 
 				return null;
 			}
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(Socket result) {
 		if(result == null && ip == null) {
 			ds.connectedAsMaster();
 		}
 		else if (result == null && ip != null){
 			ds.failedToConnectAsPeer();
 		}
 		else {
			ds.connectedAsPeer(result);
 		}
 	}
 }
