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
 import controllers.Users;
 
 @Every("5s")
 public class CheckPulses extends Job {
 	
 	public void doJob() {
         if (!Server.imAChatServer()) {
          return;
         }
         
         AbstractMap<Long, Date> heartbeats = HeartBeat.getHeartBeats();
         for (Long user_id : heartbeats.keySet()) {
             Date lastBeat = heartbeats.get(user_id);
             long diff = Utility.diffInSecs(new Date(), lastBeat);
             if (diff > HeartBeat.HEALTHY_HEARTBEAT) {
                 HeartBeat.removeBeatFor(user_id);
                 broadcastLogout(user_id);
             }
         }
         
 		// check heartbeats in rooms
         for (String key : HeartBeat.roombeats.keySet()) {
          Date lastBeat = HeartBeat.roombeats.get(key);
          long diff = Utility.diffInSecs(new Date(), lastBeat);
          if (diff > HeartBeat.HEALTHY_HEARTBEAT) {
              String parts[] = key.split("_");
              HeartBeat.roombeats.remove(key);
              long room_id = Long.parseLong(parts[0]);
              long user_id = Long.parseLong(parts[1]);
              broadcastLeaveRoom(room_id, user_id);
          }
         }
 
         // this prevents memory from bloating up and bloating up, but I have no clue why
         // it's necessary.  Sounds like its not usually necessary
         // http://stackoverflow.com/questions/66540/system-gc-in-java
         // http://stackoverflow.com/questions/2414105/why-is-it-a-bad-practice-to-call-system-gc
         // http://stackoverflow.com/questions/4784987/calling-system-gc-explicitly
         // if (counter++ % 900 == 0) {  // every 15 minutes
            // Logger.info("request Garbage Collection");
           // System.gc(); 
         // }
     }
 
 	private static void broadcastLeaveRoom (Long room_id, Long user_id) {
 		if (Server.onMaster()) {
 			Room.removeUserFrom(room_id, user_id);
 		} else {	
 			String url = Server.getMasterServer().uri + "leaveroom";
 			HashMap<String, String> params = new HashMap<String, String>();
 			params.put("user_id", user_id.toString());
 			params.put("room_id", room_id.toString());
 			Utility.fetchUrl(url, params);
 		}
 	}
 	
 	private static void broadcastLogout (Long user_id) {
 		if (Server.onMaster()) {
 		    User u = User.findById(user_id);
 		    u.logout();
 		    removeFromWaitingRoom(user_id, true);
 		} else {	
 			String url = Server.getMasterServer().uri + "signout";
 			HashMap<String, String> params = new HashMap<String, String>();
 			params.put("user_id", user_id.toString());
 			Utility.fetchUrl(url, params);
 		}
 	}
 	
 	/**
      * Removes all occurences of the given user from the waiting room
      * @param user_id the id to remove from the room 
      * @param removeAll if true, remove all of the occurences of this user,
      *                  else just one */
     private static void removeFromWaitingRoom (long user_id, boolean removeAll) {
         while (Users.waitingRoom.contains(user_id)) {
             Users.waitingRoom.remove(user_id);
             if (!removeAll) {
                 return;
             }
         }
     }
     
 }
