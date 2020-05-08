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
 
 @Every("5s")
 public class CheckPulses extends Job {
 	private static Long lastReceived = 0L;
 	
 	public void doJob() {
 		if (!Server.imAChatServer()) {
 			return;
 		}
 		
 		// this seems to be necessary to keep events popping off the queue in 
 		// a timely manner, though I hate it
         // UserEvent.userEvents.publish(new UserEvent.KeepItMoving());
 		
 		
 		// check user heartbeats
         // System.out.println(User.heartbeats);
         
 		for (Long user_id : User.heartbeats.keySet()) {
 			Date lastBeat = User.heartbeats.get(user_id);
 			Long diff = Utility.diffInSecs(new Date(), lastBeat);
 			if (diff > User.HEALTHY_HEARTBEAT) {
 				User.heartbeats.remove(user_id);
 				broadcastLogout(user_id);
 			}
 		}
 		
 		// check heartbeats in rooms
 		for (String key : User.roombeats.keySet()) {
 			Date lastBeat = User.roombeats.get(key);
 			Long diff = Utility.diffInSecs(new Date(), lastBeat);
 			if (diff > User.HEALTHY_HEARTBEAT) {
 				String parts[] = key.split("_");
 				User.roombeats.remove(key);
 				Long room_id = Long.parseLong(parts[0]);
 				Long user_id = Long.parseLong(parts[1]);
 				Logger.info("broadcast leave room, " + user_id + " from " + room_id);
 				broadcastLeaveRoom(room_id, user_id);
 				
 			}
 		}
     }
 
 	private static void broadcastLeaveRoom (Long room_id, Long user_id) {
 		if (Server.onMaster()) {
 			Room.removeUserFrom(room_id, user_id);
 		} else {	
 			String url = Server.getMasterServer().uri + "leaveroom";
 			HashMap<String, String> params = new HashMap<String, String>();
 			params.put("user_id", user_id.toString());
 			params.put("room_id", room_id.toString());
 			WS.HttpResponse resp = Utility.fetchUrl(url, params);
 			JsonObject json = resp.getJson().getAsJsonObject();
 		}
 	}
 	
 	private static void broadcastLogout (Long user_id) {
 		if (Server.onMaster()) {
 			User.logOutUser(user_id);
 		} else {	
 			String url = Server.getMasterServer().uri + "signout";
 			HashMap<String, String> params = new HashMap<String, String>();
			params.put("facebook_id", user_id.toString());
 			WS.HttpResponse resp = Utility.fetchUrl(url, params);
 			JsonObject json = resp.getJson().getAsJsonObject();
 		}
 	}
 
 }
