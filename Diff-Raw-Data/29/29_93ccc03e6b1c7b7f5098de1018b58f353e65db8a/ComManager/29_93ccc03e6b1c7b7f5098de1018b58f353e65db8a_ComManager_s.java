 package com.kaist.crescendo.manager;
 
import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ComManager {
 	private CommunicationInterface handler;
	private int msgId;
	private JSONObject jsonObject;
	
 	public ComManager() {
 		// choose FileEmulator or RealSocket
 		handler = new FileEmulator();
 	}
 	
 	public String ProcessMsg(JSONObject msg) {
 		int result = MsgInfo.STATUS_OK;
		String userId = null;
		String jsonString = null;
		
		try {
			msgId = msg.getInt(MsgInfo.MSGID_LABEL);
			jsonObject = msg;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(msgId == MsgInfo.REGISTER_ID) {
			JSONObject uData = null;
			
			try {
				uData = (JSONObject) jsonObject.get(MsgInfo.MSGBODY_LABEL);
				userId = uData.getString(MsgInfo.USERID_LABEL);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
 		
 		result = handler.write(msg);
 		
 		if(result == MsgInfo.STATUS_OK) {
 			jsonString = handler.read();
 		}
 		
 		return jsonString;
 	}
 
 }
