 package di.kdd.smartmonitor.protocol;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.List;
 
 public class BroadcastAsyncTask extends AsyncTask<Void, Void, Void> {
 
 	private List<Socket> sockets;
 	private Message message;
 	
	private static final String TAG = "broadcast task";
 	
 	public BroadcastAsyncTask(List<Socket> sockets, Message message) {
 		this.sockets = sockets;
 		this.message = message;
 	}
 	
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		for(Socket peer : sockets) {
 			try {
 				DistributedSystemNode.send(peer, message);
 			}
 			catch(IOException e) {
 				Log.i(TAG, "Failed to broadcast at node " + e.getMessage());
 			}
 		}		
 
 		return null;
 	}
 
 		
 	
 }
