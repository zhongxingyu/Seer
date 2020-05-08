 package fr.eurecom.messaging;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Sender implements Runnable {
 	
 	private GameMessage message;
 	private InetAddress receiver;
 
 	public Sender(GameMessage message, InetAddress receiver) {
 		this.message = message;
 		this.receiver = receiver;
 	}
 	
 	private void send(GameMessage message, InetAddress receiver) {

 
 	    String messageToSend = serialize(message);
 	    
 	    if (messageToSend.length() == 0) return;
 	    if (Thread.currentThread().isInterrupted()) return;
 		Socket socket = new Socket();
 		try {
 			socket.bind(null);
 			socket.connect((new InetSocketAddress(receiver, Config.PORT)), Config.TIMEOUT_INTERVAL);
 
 			OutputStream outputStream = socket.getOutputStream();
 			outputStream.write(messageToSend.getBytes());
 			outputStream.close();
 			
 		} catch (IOException e) {
 		} 
 		
 		finally {
 		    if (socket != null) {
 		        if (socket.isConnected()) {
 		            try {
 		                socket.close();
 		            } catch (IOException e) {
 		            }
 		        }
 		    }
 		}
 	}
 	
 	private String serialize(GameMessage message) {
 		JSONObject json = new JSONObject();
 		try {
 			json.put("what", message.what.ordinal());
 			json.put("about", message.about);
 			String name;
 			if (message.getOriginatorName() == null || message.getOriginatorName().length() == 0) name = "0";
 			else name = message.getOriginatorName();
 			json.put("name", name);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return json.toString();
 	}
 
 	@Override
 	public void run() {
 		if (!Thread.currentThread().isInterrupted()) send(message, receiver);
 	}
 	
 }
