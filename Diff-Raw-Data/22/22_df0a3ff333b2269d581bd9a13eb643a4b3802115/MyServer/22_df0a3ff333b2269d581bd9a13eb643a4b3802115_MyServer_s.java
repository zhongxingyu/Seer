 package com.smart.smartkitchen;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import jexxus.common.Connection;
 import jexxus.common.ConnectionListener;
 import jexxus.server.ServerConnection;
 
 public class MyServer implements ConnectionListener {
 	public Context ctx;
 	private static int orderNo = 0;
 	public MyServer(Context ctx) {
 		this.ctx = ctx;
 	}
 
 	String TAG = "Table Client";
 
 	@Override
 	public void connectionBroken(Connection broken, boolean forced) {
 		Log.d(TAG, "Connection lost:");
 		String message = "Connection lost: " + broken;
 		System.out.println(message);
 	}
 
 	@Override
 	public void receive(byte[] data, Connection from) {
 		String receivedMessage = new String(data);
 		String message = "Received message: " + receivedMessage;
 		JSONObject deserializedMessage = new JSONObject();
 		JSONArray l1j = new JSONArray();
		JSONArray l2j = new JSONArray();
 		ArrayList<String> l1 = new ArrayList<String>();
		ArrayList<String> l2 = new ArrayList<String>();
 
 		try {
 			deserializedMessage = new JSONObject(receivedMessage);
 			l1j = new JSONArray(deserializedMessage.get("l1").toString());
			l2j = new JSONArray(deserializedMessage.get("l2").toString());
 			Log.d(TAG, "Received message: " + message);
 			System.out.println(message);
			if (l1j != null && l2j != null) {
 				int len = l1j.length();
 				for (int i = 0; i < len; i++) {
 					l1.add(l1j.get(i).toString());
					l2.add(l2j.get(i).toString());
 				}
 			}
 			Log.d(TAG, "l1j length  " + l1j.length());
			Log.d(TAG, "l2j length  " + l2j.length());
 			Log.d(TAG, "l1 length  " + l1.size());
			Log.d(TAG, "l2 length  " + l2.size());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		// iterator gets items
 		Iterator<String> ir1 = l1.iterator();
		Iterator<String> ir2 = l2.iterator();
 		String order = "New Order\n\nTable number: #\n\nOrder number: " + ++orderNo + "\n\n";
 		while (ir1.hasNext()) {
 			String msg1 = ir1.next();
			String msg2 = ir2.next();
 			Log.d(TAG, "l1 Message  " + msg1);
			Log.d(TAG, "l2 Message  " + msg2);
			order = order + "Item: " + msg1 + "----------" + "Count: " + msg2 + "\n\n";
 		}
 		Log.d(TAG, "Final Message" + order);
 		new ShowTheOrder().execute(order);
 	}
 
 	@Override
 	public void clientConnected(ServerConnection conn) {
 		Log.d(TAG, "Client Connected:");
 		String message = "Client Connected: " + conn.getIP();
 		System.out.println(message);
 	}
 
 	private class ShowTheOrder extends AsyncTask<String, Void, String> {
 
 		@Override
 		protected String doInBackground(String... messages) {
 			Log.d(TAG, "Showing The order - BG");
 			return messages[0];
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			Log.d(TAG, "Showing The order - POST");
 			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ctx);
 			dlgAlert.setMessage(result);
 			dlgAlert.setTitle("Smart Kitchen");
 			dlgAlert.setPositiveButton("OK", null);
 			dlgAlert.setCancelable(true);
 			dlgAlert.create().show();
 		}
 
 	}
 
 }
