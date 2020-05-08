 package utils;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.example.mobilemeasuringapp.PureWebSocket;
 
 import de.tavendo.autobahn.WebSocketConnection;
 import de.tavendo.autobahn.WebSocketConnectionHandler;
 import de.tavendo.autobahn.WebSocketException;
 
 public class WSConnectionHandler extends WebSocketConnectionHandler{
 
 	SmartphoneData smartphoneData;
 	JSONObject reveivedPayload = new JSONObject();
 	PureWebSocket pWebSocket;
 	WebSocketConnection webSocketConnection;
 	
 	public WSConnectionHandler(PureWebSocket pWebSocket, WebSocketConnection webSocketConnection, SmartphoneData smartphoneData){
 		this.pWebSocket = pWebSocket;
 		this.webSocketConnection = webSocketConnection;
 		this.smartphoneData = smartphoneData;
 	}
 
 	@Override
 	public void onOpen() {
 		pWebSocket.notificationArrayAdapter.add("Sender trying to connect to Server ...");
 
 		if(webSocketConnection.isConnected()){
 			pWebSocket.notificationArrayAdapter.add("Sender connected to Server " + pWebSocket.serverAddress);
 			pWebSocket.connectedToServer = true;
 			//webSocketConnection.sendTextMessage("bulbber".toString());
 		}
 		else
 			pWebSocket.notificationArrayAdapter.add("Sender NOT connected to Server " + pWebSocket.serverAddress);
 	}
 
 	@Override
 	public void onBinaryMessage(byte[] payload) {
 		super.onBinaryMessage(payload);
 		pWebSocket.notificationArrayAdapter.add("onBinaryMessage");
 		pWebSocket.notificationArrayAdapter.add(payload.toString());
 	}
 	
 
 	
 	
 	@Override
 	public void onTextMessage(String payload) {
 		super.onTextMessage(payload);
 
 //		pWebSocket.notificationArrayAdapter.add("onTextMessage");
 //		pWebSocket.notificationArrayAdapter.add(payload.toString());
 			
 		reveivedPayload = null;
 		double latency = 0;
 		long transmissionTime = 0;
 
 		int payloadSize = (payload.length() + 48 + 34)* 8;
 		
 		try {reveivedPayload = new JSONObject(payload.toString());} 
 		catch (JSONException e1) {e1.printStackTrace();}
 				
 		if(reveivedPayload.has("ack")){
 			try {pWebSocket.notificationArrayAdapter.add(reveivedPayload.get("ack").toString());} 
 			catch (JSONException e) {e.printStackTrace();}
 		}
 		if(reveivedPayload.has("timestamp")){
 			try {transmissionTime = Long.parseLong(reveivedPayload.get("timestamp").toString());}
 			catch (JSONException e) {e.printStackTrace();}
 
 			/*
 			 * latency calculation 
 			 */
 			
 			long receiveTime = smartphoneData.getDate().getTime();
 			latency = (int)(receiveTime - transmissionTime);
 
 			pWebSocket.notificationArrayAdapter.add(transmissionTime+" (transmissionTime)");
 			pWebSocket.notificationArrayAdapter.add(receiveTime+" (receiveTime)");
 			pWebSocket.notificationArrayAdapter.add("latency: "+latency);
 			pWebSocket.notificationArrayAdapter.add("payloadSize: "+payloadSize);
 			pWebSocket.notificationArrayAdapter.add("CdmaDbm: "+smartphoneData.getCdmaDbm());
 			pWebSocket.notificationArrayAdapter.add("GsmRssi: "+smartphoneData.getGsmRssi());
			pWebSocket.notificationArrayAdapter.add("GsmBitErrorRate: "+smartphoneData.getGsmBitErrorRate());
 		}
 		
 		JSONObject logObject = new JSONObject();
 		try {
 			// Log latency package received
 			if (pWebSocket.loggerOn == true) {
 				logObject.put("mode", "receiver");
 				logObject.put("timestamp_sender", transmissionTime);
 				logObject.put("timestamp_receiver", smartphoneData.getDate().getTime());
 				logObject.put("latency", latency);
 				logObject.put("networkType", smartphoneData.getNetworkType());
 				logObject.put("payloadSize", payloadSize);
 				Logger.log(logObject);
 			}	
 		}catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public void onClose(int code, String reason) {
 
 		//mConnectionMeasurements.notificationArrayAdapter.add("code: " + code + " reason: " + reason);
 		if(!webSocketConnection.isConnected())
 			pWebSocket.notificationArrayAdapter.add("Sender connection closed");
 
 		//Auto reconnect to Server
 		if (pWebSocket.wsReconnect && pWebSocket.connectButtonPressed && !webSocketConnection.isConnected()) {
 			final String wsuri = "ws://" + pWebSocket.serverAddress;
 
 			pWebSocket.notificationArrayAdapter.add("Sender trying to reconnect to Server ...");
 			try {
 				webSocketConnection.connect(wsuri, pWebSocket.wsConnectionHandler);
 			} catch (WebSocketException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
