 package socket;
 
 import java.net.MalformedURLException;
 
 import io.socket.IOAcknowledge;
 import io.socket.IOCallback;
 import io.socket.SocketIO;
 import io.socket.SocketIOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import topology.TwitterTopology;
 
 
 public class Socket implements IOCallback {
 	
 	private SocketIO socket;
 	private TwitterTopology twtp;
 
 	public Socket()  {
 		socket = new SocketIO();
 		
 		try {
 			socket.connect("http://127.0.0.1:3000/", this);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 		
 	}
 	public Socket(TwitterTopology t)  {
 		socket = new SocketIO();
 		this.twtp = t;
 		try {
 			socket.connect("http://127.0.0.1:3000/", this);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 		
 	}
 
 	public SocketIO getSocket() {
 		return socket;
 	}
 
 	public void setSocket(SocketIO socket) {
 		this.socket = socket;
 	}
 
 	@Override
 	public void onMessage(JSONObject json, IOAcknowledge ack) {
 		try {
 			System.out.println("Server said:" + json.toString(2));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onMessage(String data, IOAcknowledge ack) {
 		System.out.println("Server said: " + data);
 		
 	}
 
 	@Override
 	public void onError(SocketIOException socketIOException) {
 		System.out.println("an Error occured");
 		socketIOException.printStackTrace();
 	}
 
 	@Override
 	public void onDisconnect() {
 		System.out.println("Connection terminated.");
 	}
 
 	@Override
 	public void onConnect() {
 		System.out.println("Connection established");
 	}
 
 	@Override
 	public void on(String event, IOAcknowledge ack, Object... args) {
 		
 		if (event.equals("startStorm")){
 			try {
 				JSONObject json = ((JSONObject) args[0]);
 				System.out.println(event + " " + json.getString("cat"));
 				try {
					
					String category = json.getString("cat");
					this.twtp.executeStorm(category);
				
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
 	}
 	
 	
 }
