 package com.kaist.crescendo.manager;
 
 import org.json.JSONObject;
 
 public class ComManager {
 	private CommunicationInterface handler;

 	public ComManager() {
 		// choose FileEmulator or RealSocket
 		handler = new FileEmulator();
 	}
 	
 	public String ProcessMsg(JSONObject msg) {
 		int result = MsgInfo.STATUS_OK;
		String jsonString = null;	
 		
 		result = handler.write(msg);
 		
 		if(result == MsgInfo.STATUS_OK) {
 			jsonString = handler.read();
 		}
 		
 		return jsonString;
 	}
 
 }
