 package com.askcs.ADK;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.askcs.ADK.lib.Logger;
 import com.askcs.ADK.lib.SessionHandler;
 import com.askcs.ADK.lib.WSLib;
 
import com.askcs.webservices.AskPortType;
import com.askcs.webservices.BoolResponse;
import com.askcs.webservices.ResourceDataResponse;
import com.askcs.webservices.StringArray;
import com.askcs.webservices.StringArrayResponse;
import com.askcs.webservices.StringResponse;
import com.askcs.webservices.Tuple;
import com.askcs.webservices.TupleArray;
 
 public class PersonalAgent {
 	
 	protected String uuid="";
 	protected String name="";
 	
 	protected SessionHandler sh;
 	protected AskPortType askport;
 	protected Logger logger;
 	
 	protected static String askatarMessagesUUID;
 
 	public PersonalAgent(String uuid, String name, SessionHandler sh) {
 		this.uuid=uuid;
 		this.name=name;
 		
 		this.sh=sh;
 		
 		askport = sh.getAskPort();
 		logger = new Logger();
 		
 		checkAskatarMessages();
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public ArrayList<AskatarMessage> getMessages(){
 		logger.log("Start collection messages");
 		
 		if(askatarMessagesUUID!=null) {
 			
 			StringArrayResponse resMes = askport.getNodeMembers(sh.getSessionId(), askatarMessagesUUID);
 			StringArray messages = resMes.getResult();
 			
 			ArrayList<AskatarMessage> messageList = new ArrayList<AskatarMessage>();
 			for(String messageUUID : messages.getString()){
 				messageList.add(new AskatarMessage(messageUUID, sh));
 			}
 			logger.log("Returning "+messageList.size()+" messages");
 			return messageList;
 		} else
 			System.out.println("askatarMessagesUUID is not set!!");
 		
 		return null;
 	}
 	
 	public boolean setTextResource(String tag, String value){
 		
 		ResourceDataResponse res = askport.getResourceDataByTag(sh.getSessionId(), uuid, tag, "TXT");
 		if(res.getError()!=0){
 			StringResponse resRes = askport.createResource(sh.getSessionId(), new String(), "TXT", uuid, tag, "Automatically added resource tagged "+tag, "", tag, value);
 			if(resRes.getError()==0)
 				return true;				
 		} else {
 			BoolResponse boolRes=askport.setResourceData(sh.getSessionId(), res.getResult().getResUUID(), uuid, tag, "Automatically added resource tagged "+tag, "", tag, value);
 			return boolRes.isResult();				
 		}
 		return false;
 	}
 	
 	public boolean addMessage(AskatarMessage message){
 		if(askatarMessagesUUID==null){
 			Map<String, String> map = new HashMap<String, String>();
 			map.put("name", "Message");
 
 			StringResponse res=askport.createNode(sh.getSessionId(), askatarMessagesUUID, WSLib.convertMapToTupleArray(map));
 			if(res.getError()==0){
 				// TODO: add resources to the message
 				
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected void checkAskatarMessages(){
 		if(askatarMessagesUUID==null) {
 			StringArrayResponse res = askport.getNodeMembersByTag(sh.getSessionId(), uuid, "askatarMessages");
 			if(res.getError()!=0){
 				Map<String, String> map = new HashMap<String, String>();
 				map.put("name", uuid+"_askatarMessages");
 				StringResponse res1=askport.createNode(sh.getSessionId(), "", WSLib.convertMapToTupleArray(map));
 				askatarMessagesUUID = res1.getResult();
 				System.out.println("Set askatarMessagesUUID is set to: "+askatarMessagesUUID);				
 			} else {
 				askatarMessagesUUID = res.getResult().getString().get(0);
 				System.out.println("Set askatarMessagesUUID is set to:"+askatarMessagesUUID);
 			}
 		}
 	}
 
 }
