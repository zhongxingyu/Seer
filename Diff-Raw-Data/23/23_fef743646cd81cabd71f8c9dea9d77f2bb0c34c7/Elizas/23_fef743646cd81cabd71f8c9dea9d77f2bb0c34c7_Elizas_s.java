 package controllers;
 
 import java.util.*;
import java.lang.reflect .*;
 
 import play.*;
 import play.mvc.*;
 import play.libs.*;
 import play.libs.F.*;
 import play.cache.Cache;
 import play.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import models.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 import play.db.jpa.GenericModel.JPAQuery;
 import play.db.jpa.JPA;
 import javax.persistence.Query;
 import models.eliza.*;
 
 import com.google.gson.*;
 import com.google.gson.reflect.*;
 
 /**
  * This controller is responsible for keeping track of users, updating their
  * status in the system, assigning them rooms, passing chat requests, etc..
  */
 public class Elizas extends Index {
     private static HashMap<String, String> convo_ids = new HashMap<String, String>();
     
     public static void reply (String qry, long from_user, long user_id, long room_id, String callback) {
         if (qry == null) qry = "";
         String response = Eliza.respondTo(qry);
         User u = User.findById(user_id);
         User u2 = User.findById(from_user);
         if (u == null || u2 == null) {
             returnFailed("both from_user and user_id must map to existing users", callback);
         }
         u.sendMessage(from_user, room_id, "@Azile " + qry);
         u.sendMessage(Eliza.user_id, room_id, response);
         u2.sendMessage(Eliza.user_id, room_id, response);
         returnOkay(callback);
     }
     
     /**
 	 * Get a room with the requested bot; will immediately generate a join
 	 * message
 	 * @param user_id your user_id
 	 * @param botid the string identifying the bot to request
 	 * @param callback optional JSONP callback*/
 	public static void requestBotRoom (long user_id, String bot_id, String callback) {
 	    User bot = User.find("byBotid", bot_id).first();
         if (bot == null) {
             returnFailed(bot_id + " does not map to a valid bot", callback);
         }   
         Room.createBotRoomFor(user_id, bot);
 		returnOkay(callback);
 	}
 
     
     public static void talkTo (String bot_id, long bot_user_id, String qry, long user_id, long room_id, String callback) {
         String url = "http://www.pandorabots.com/pandora/talk-xml";
         String key = user_id + "_" + bot_id;
         String custid = convo_ids.get(key);
         
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("botid", bot_id);
         params.put("input", qry);
         if (custid != null) {
             params.put("custid", custid);
         }
         
         String xmlStr = Utility.fetchUrl(url, params).getString();
         String reply = xmlStr.substring(
             xmlStr.indexOf("<that>") + 6,
             xmlStr.indexOf("</that>")
         );
         
         if (custid == null) {
             custid = xmlStr.substring(
                 xmlStr.indexOf("custid=\"") + 8,
                 xmlStr.indexOf("\"><input")
             );
             convo_ids.put(key, custid);
         }
         
         new UserEvent.RoomMessage(user_id, bot_user_id, room_id, reply);
         returnOkay(callback);
     }
         
 }
