 package ca.ryerson.scs.rus.util;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ca.ryerson.scs.rus.messenger.objects.Friend;
 import ca.ryerson.scs.rus.messenger.objects.Message;
 import ca.ryerson.scs.rus.socialite.objects.User;
 
 public class ProcessList {
 	public static ArrayList<Message> processMessages(JSONArray response) {
 		
 		ArrayList<Message> alReturn = new ArrayList<Message>();
 		JSONObject jd;
 		Message tempMessage;
 
 		for (int i = 0; i < response.length(); i++) {
 			try {
 				jd = response.getJSONObject(i);
 				
 				tempMessage = new Message(jd.getString("sender"),jd.getString("message"),jd.getString("created_at"));
 				alReturn.add(tempMessage);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		return alReturn;
 	}
 
 	public static ArrayList<Friend> processFriends(JSONArray response) {
 
 		ArrayList<Friend> alReturn = new ArrayList<Friend>();
		JSONObject jd;
 		Friend tempUser;
 
		for (int i = 0; i < response.length(); i++) {
 			try {
 				jd = response.getJSONObject(i);
				tempUser = new Friend(jd.getString("friend"),
						jd.getString("state"), "a@b.com", "Hello world");
 				alReturn.add(tempUser);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		return alReturn;
 	}
 
 	public static ArrayList<User> processLocations(JSONArray response) {
 
 		ArrayList<User> alReturn = new ArrayList<User>();
 		JSONObject jd;
 		User tempUser;
 
 		for (int i = 0; i < response.length(); i++) {
 			try {
 				jd = response.getJSONObject(i);
 				tempUser = new User(jd.getString("username"), "",
 						jd.getString("status_message"), "", jd.getString("geoX"),
 						jd.getString("geoY"));
 				alReturn.add(tempUser);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		return alReturn;
 	}
 }
