 package jobs;
  
 import play.jobs.*;
 
 import play.*;
 import play.mvc.*;
 import play.libs.*;
 import play.libs.F.*;
 import java.util.*;
 import com.google.gson.*;
 import com.google.gson.reflect.*;
 import models.*;
 import controllers.Index;
 import java.lang.reflect .*;
 
 
 @Every("10s")
 public class CheckPowers extends Job {
     private static HashMap<String, Long> lastReceived = new HashMap<String, Long>();
     private static Set<Long> myUsers = new HashSet<Long>();
     
     public static final String REVEAL_CODE = "!@#$%^&$#@";
     public static final String DATA_CODE = "*&^%$!";
     public static final int HEARTBEAT_INTERVAL = 5; 
     
     public void doJob () {
             
        if (!Server.onMaster()) {
             return;
         }
         
         List<Server> chatServers = Server.getChatServers();
         List<String> processedURIs = new LinkedList<String>();
         for (Server s : chatServers) {
             if (!processedURIs.contains(s.uri)) {
                 processUpdates(getEvents(s));
                 processedURIs.add(s.uri);
             }
         }
 
         for (Long id : myUsers) {
            User u = User.findById(id);
            if (u != null) {
               u.checkForNewPowers();
            }
         }
         myUsers.clear();        
     }
     
     private static void processUpdates (List<UserEvent.Event> events) {
         for (UserEvent.Event event : events) {
            System.out.println("admin process " + event.toString());
             if (event instanceof UserEvent.RoomMessage) {
                 UserEvent.RoomMessage rm = (UserEvent.RoomMessage)event;
                 if (rm.user_id == -1 || rm.from == -1) {
                     continue;
                 }
                 User to = getUser(rm.user_id);
                 User from = getUser(rm.from); 
                 if (to == null || from == null) {
                     continue;
                 } 
                 String text = rm.text;
                 
                 if (text.equals(REVEAL_CODE)) {
                     to.offersReceivedCount += 1;
                     from.offersMadeCount += 1;
                 } else if (text.length() >= DATA_CODE.length()
                            && text.substring(0, DATA_CODE.length()).equals(DATA_CODE)
                            && text.indexOf("facebook_id") > -1) {   
                     // only count this for one user; there will be a corresponding event
                     // for the other one
                     to.revealCount += 1;                       
                 } else {
                     to.gotMessageCount += 1;
                     from.messageCount += 1;             
                 }                           
                 to.save();
                 from.save();
             } else if (event instanceof UserEvent.Join) {
                 UserEvent.Join j = (UserEvent.Join)event;
                 User user = getUser(j.user_id);     
                 if (user != null) {
                     user.joinCount += 1;
                     user.save();                    
                 }           
             } else if (event instanceof UserEvent.HeartBeat) {
                 UserEvent.HeartBeat hb = (UserEvent.HeartBeat)event;
                 User user = getUser(hb.for_user_id);                
                 if (user != null) {
                     user.chatTime += HEARTBEAT_INTERVAL;    
                     user.save();                    
                 }
             } else if (!(event instanceof UserEvent.Event)) {
                 Logger.error("Processing super power check and encountered a bad event");
             } 
         }
     }
     
     private static User getUser (long id) {
         User u = User.findById(id);
         if (u == null) {
             Logger.warn("attempted to process event for non-existant user (%s)", id);
             return null;
         }
         myUsers.add(id);
         return u;
     }
     
     private static List<UserEvent.Event> getEvents (Server s) {
         long last = getLastReceived(s.name);
         List<UserEvent.Event> events;
         
         if (s.iAmMaster()) {
             List<IndexedEvent> indexedEvents = UserEvent.get().availableEvents(last);    
             last = indexedEvents.size() > 0 
                    ? indexedEvents.get(indexedEvents.size() - 1).id
                   : last;
             events = new LinkedList<UserEvent.Event>();
             for (IndexedEvent e : indexedEvents) {
                 events.add((UserEvent.Event)e.data);
             }
         } else {
             String url = s.uri + "notify/adminlisten";
             HashMap<String, String> params = new HashMap<String, String>();
             params.put("lastReceived", last + "");
             WS.HttpResponse resp = Utility.fetchUrl(url, params);
             events = UserEvent.deserializeEvents(resp.getString());
             UserEvent.DirectMessage lastMsg = (UserEvent.DirectMessage)events.get(events.size() - 1);
             last = Long.parseLong(lastMsg.text);                
         }
         lastReceived.put(s.name, last);
         return events;
     }
     
     private static long getLastReceived (String url) {
         if (lastReceived.containsKey(url)) {
             return lastReceived.get(url);
         } else {
             return 0;
         }
     }
     
 }
