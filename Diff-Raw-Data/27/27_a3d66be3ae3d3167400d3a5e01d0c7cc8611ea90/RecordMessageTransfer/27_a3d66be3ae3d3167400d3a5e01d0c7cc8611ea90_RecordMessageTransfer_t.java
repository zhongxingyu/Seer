 package server.transfer;
 
 import org.jwebsocket.api.WebSocketConnector;
 import org.jwebsocket.api.WebSocketPacket;
 import org.jwebsocket.kit.RawPacket;
 import org.jwebsocket.listener.WebSocketServerTokenEvent;
 import org.jwebsocket.token.Token;
 import server.shared.RecordMessage;
 import server.usermanager.UserManager;
 
 public class RecordMessageTransfer extends ListenerTransfer{
 	private UserManager userManager;
 	
 	public RecordMessageTransfer(UserManager userManager){
 		this.userManager=userManager;
 	}
 	
 	@Override
     public void processToken(WebSocketServerTokenEvent event, Token token) {
    		String type= token.getString("type");
    		WebSocketPacket wspacket=null;	
    		if(type.equals("getMessages")){
    			wspacket = new RawPacket(converter.convertMessages(userManager.getMessages(token.getString("username")),"\"type\":\"getMessages\""));
    	  		sendPacket(wspacket, event.getConnector());
    		}else if (type.equals("saveMessage")){
    			java.util.Date dt = new java.util.Date();
    			java.text.SimpleDateFormat sdf =new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			RecordMessage recMex= userManager.createMessage(token.getString("sender"), token.getString("addressee"), token.getString("path"), sdf.format(dt));
    			if(recMex==null){
    				wspacket = new RawPacket("{\"type\":\"saveMessage\",\"answer\":\"false\"}");
    	   	  		sendPacket(wspacket, event.getConnector());
    			}else{
    				wspacket = new RawPacket("{\"type\":\"saveMessage\",\"answer\":\"true\"}");
    	   	  		sendPacket(wspacket, event.getConnector());
   				WebSocketConnector connector = getConnector(userManager.getUser(token.getString("addressee")).getIP());
    				if(connector!= null){
    					java.util.Vector<RecordMessage> vecRecMex=new java.util.Vector<RecordMessage>();
    					vecRecMex.add(recMex);
    					wspacket=new RawPacket(converter.convertMessages(vecRecMex, "\"type\":\"getMessages\""));
    					sendPacket(wspacket,connector);
    				}   
    			}
    		}else if (type.equals("removeMessage")){
    			boolean answer =userManager.removeMessage(token.getString("sender"), token.getString("addressee"), token.getString("path"),token.getString("date"));
    			if(answer)
    				wspacket = new RawPacket("{\"type\":\"removeMessage\",\"answer\":\"true\"}");
    			else
    				wspacket = new RawPacket("{\"type\":\"removeMessage\",\"answer\":\"false\"}");
    			sendPacket(wspacket, event.getConnector());
    		}
 	}
 
 }
