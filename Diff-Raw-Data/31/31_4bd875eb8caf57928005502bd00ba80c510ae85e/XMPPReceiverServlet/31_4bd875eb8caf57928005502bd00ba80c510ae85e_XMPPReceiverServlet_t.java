 package Servlets;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.*;
 
 import Entities.OfyService;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.xmpp.JID;
 import com.google.appengine.api.xmpp.Message;
 import com.google.appengine.api.xmpp.XMPPService;
 import com.google.appengine.api.xmpp.XMPPServiceFactory;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.googlecode.objectify.cmd.Query;
 
 import Entities.UserMessage;
 
 @SuppressWarnings("serial")
 public class XMPPReceiverServlet extends HttpServlet {
 	
     public void doPost(HttpServletRequest req, HttpServletResponse res)
           throws IOException {
     	
 		Gson gson = new Gson();
 
 		JsonParser jsonParser = new JsonParser();
 		JsonObject jo = (JsonObject)jsonParser.parse(req.getParameter("json"));
 		System.out.println(jo.toString());
 		UserMessage receivedMessage = new UserMessage();
 		
 		receivedMessage.setFrom(jo.get("sender").toString().replace("\"", ""));
 		receivedMessage.setTo(jo.get("recipient").toString().replace("\"", ""));
 		receivedMessage.setBody(jo.get("message").toString().replace("\"", ""));
 		receivedMessage.setEmail();
 		System.out.println(receivedMessage.getBody());
 		System.out.println(receivedMessage.getFrom());
		System.out.println(receivedMessage.getTo());       
         
         OfyService.ofy().save().entity(receivedMessage).now();
         System.out.println("Message is saved. From " + receivedMessage.getFrom() + " and to: " + receivedMessage.getTo() + " message is: " + receivedMessage.getBody());
        
         String ID;
         
         if(receivedMessage.getTo().compareTo(receivedMessage.getFrom()) > 0){
         	ID = receivedMessage.getTo()  + receivedMessage.getFrom();
         } else ID = receivedMessage.getFrom()  + receivedMessage.getTo();
         
 		Query<UserMessage> allMessages = OfyService.ofy().load().type(UserMessage.class).filter("email", ID);  
 		for (UserMessage um: allMessages) {
 		    System.out.println(um.toString());
 		}
     }
     
     
 }
