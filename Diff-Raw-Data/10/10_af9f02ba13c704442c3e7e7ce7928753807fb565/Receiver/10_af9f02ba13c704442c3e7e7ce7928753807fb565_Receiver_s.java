 package fr.eurecom.messaging;
 
 import java.io.BufferedReader;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.io.OutputStream;
 import java.net.InetAddress;
import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class Receiver extends AsyncTask<String, Void, JSONObject> {
 	private static final String TAG = "ClientAsyncTask";
 	private boolean listening;
 	private Client client;
 
 	public Receiver(Client client) {
 		Log.d("ClientAsyncTask", "Constructor");
 		listening = true;
 		this.client = client;
 	}
 
 	@Override
 	protected JSONObject doInBackground(String... params) {
 		try {
 			ServerSocket socket = new ServerSocket(Config.PORT);
 			Log.d(Receiver.TAG, "Client: Socket opened!!");
 			while (listening) {
 				// Wait for incoming message
 				Socket sender = socket.accept();
 				// Read incoming message
 				BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));			
 				// Transform message to JSON Object
 				JSONObject json = new JSONObject(in.readLine());
 				// Send message to client
 				receiveMessage(json, sender.getInetAddress());
				Log.d("Tekst fra host", "Inputstreamen er: " + in.readLine());
 				
 			}
 			
 		} catch (IOException e) {
 			
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	private void receiveMessage(JSONObject json, InetAddress sender){
 		try {
 			Action action = Action.values()[json.getInt("action")];
 			String subject = json.getString("subject");
 			Message message = new Message(action, subject);
 			message.setSender(sender);
 			client.handleMessage(message);
 		} catch (JSONException e){
 			Log.e("ClientInterpreter:receiveMessage", e.getMessage());
 		}
 	}
 
 	
 }
