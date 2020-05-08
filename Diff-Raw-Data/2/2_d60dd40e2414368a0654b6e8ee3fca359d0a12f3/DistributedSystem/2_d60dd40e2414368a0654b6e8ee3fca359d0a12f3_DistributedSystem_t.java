 package di.kdd.buildmon.protocol;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Date;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import di.kdd.buildmon.MainActivity;
 import di.kdd.buildmon.protocol.exceptions.NotCaptainException;
 
 public class DistributedSystem extends AsyncTask<Void, Void, Boolean> implements IProtocol {
 	private MainActivity view;
 	private DistributedSystemNode node;
 	
 	private static final String TAG = "distributed system";
 		
 	public DistributedSystem(MainActivity view) {
 		this.view = view;
 	}
 	
 	public void connect() {
 		this.execute();
 	}
 	
 	/***
 	 * Sends Knock-Knock messages to the first 255 local IP addresses and
 	 * according to if it will get a response or not, the node becomes
 	 * a peer or a Captain respectively.
 	 */
 
 	@Override
 	protected Boolean doInBackground(Void... arg0) {
 		Socket socket;
 		String ipPrefix = "192.168.1."; //TODO FIXME
 		
 		android.os.Debug.waitForDebugger();
 
 		/* Look for the Captain in the first 255 local IP addresses */
 		//TODO parallelize it
		for(int i = 1; i < 256; ++i) {
 			try{
 				Log.d(TAG, "Trying to connect to :" + ipPrefix + Integer.toString(i));
 				socket = new Socket(ipPrefix + Integer.toString(i), IProtocol.KNOCK_KNOCK_PORT);
 			}
 			catch(IOException e) {
 				continue;
 			}
 
 			/* Captain found */
 			
 			node = new PeerNode(socket);
 			
 			return true;
 		}
 		
 		/* No response, I am the first node of the distributed system and the Captain */
 		
 		node = new CaptainNode(); 
 
 		return false;
 	}
 	
 	@Override
 	protected void onPostExecute(Boolean becamePeer) {
 		if(becamePeer) {
 			view.showMessage("Connected as Peer");			
 		}
 		else {
 			view.showMessage("Connected as Captain");			
 		}
 	}
 	
 	@Override
 	public void computeBuildingSignature(Date from, Date to) throws NotCaptainException, IOException {
 		if(node.isCaptain() == false) {
 			throw new NotCaptainException();
 		}
 		
 		((CaptainNode) node).computeBuildingSignature(from, to);
 	}
 
 	@Override
 	public boolean isCaptain() {
 		return node.isCaptain();
 	}
 
 	@Override
 	public String getCaptainIP() {
 		return node.getCaptainIP();
 	}
 
 	@Override
 	public void end() {
 		node.end();
 	}
 }
