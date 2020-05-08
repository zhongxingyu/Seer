 package com.askcs.ADK;
 
 import com.askcs.ADK.lib.SessionHandler;
 import com.askcs.webservices.AskPortType;
 import com.askcs.webservices.ResourceDataResponse;
 
 public class AskatarMessage {
 
 	protected String messageUUID=null;
 	protected String message="";
 	protected String type="askMessage";
 	protected String sender="";
 	protected Boolean beenRead=false;
 	protected Integer prio=0;
 	
 	public AskatarMessage(String uuid, SessionHandler sh){
 		ResourceDataResponse res;
 		Boolean error=false;
 		AskPortType askport = sh.getAskPort();
 		
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "message", "TXT");
 		if(res.getError()==0) this.message = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "type", "TXT");
 		if(res.getError()==0) this.type = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "beenRead", "TXT");
		if(res.getError()==0 && res.getResult().getValue() != null) this.beenRead=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "sender", "TXT");
 		if(res.getError()==0) this.sender = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "prio", "TXT");
 		if(res.getError()==0) this.prio = Integer.getInteger(res.getResult().getValue()); else error=true;
 		
 		if (!error) this.messageUUID = uuid;
 	}
 	
 	public AskatarMessage(String messageUUID,String message, String type, String sender, Integer prio){
 		this.messageUUID = messageUUID;
 		this.message = message;
 		this.type = type;
 		this.beenRead = false;
 		this.sender = sender;
 		this.prio = prio;
 	}
 
 	public String getMessageUUID() {
 		return messageUUID;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public String getSender() {
 		return sender;
 	}
 
 	public Boolean getBeenRead() {
 		return beenRead;
 	}
 
 	public Integer getPrio() {
 		return prio;
 	}
 
 
 }
