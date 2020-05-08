 package lastfm;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.json.JSONObject;
 
 public class LastfmMain {
 	//public static String outpath = "/home/neera/lastfm-data/";
 	public static String outpath = "";
 	//user -> list of friends
 	public static HashMap<String, HashSet<String>> hmFriends = new HashMap<String, HashSet<String>>();
 	//user -> userobject;
 	public static HashMap<String, User> hmUser = new HashMap<String, User>();
 	public static HashMap<String, String> hmTrackTags = new HashMap<String, String>();
 
 
 	public static void main(String[] args){
 		String key = "e77094ac5bf414726b355017e631d048";
 		Authorization auth = new Authorization();
 		LastfmObjects lastfmObj = new LastfmObjects();
 
 		String token = auth.getAuthorization(key);
 		//geo get event
 		int eventCount = 0;
 		int userCnt = 0;
 		ArrayList<String> events = new ArrayList<String>(); 
 		ArrayList<String> spainEvents = lastfmObj.getEventsByLocation(key, "UK");
 		//ArrayList<String> franceEvents = lastfmObj.getEventsByLocation(key, "france");
 		//ArrayList<String> usEvents = lastfmObj.getEventsByLocation(key, "US");
 		//ArrayList<String> ukEvents = lastfmObj.getEventsByLocation(key, "UK");
 		System.out.println("number of spain events "+spainEvents);
 		events.addAll(spainEvents);
 		//events.addAll(franceEvents);
 		//events.addAll(usEvents);
 		//events.addAll(ukEvents);
 		if(events != null && events.size() > 0){
 			int eventNo = events.size();
 			String[] eventsArr = events.toArray(new String[0]);
 			//for(int x = eventNo-10 ; x > 0; x--){
 			for(int x = 0 ; x < eventNo-1; x++){
 				//get attendees for each events
 				String e = eventsArr[x];
 				System.out.println("processing for event id:: "+e);
 				ArrayList<String> attendees = lastfmObj.getAttendeesByEvents(key, e);
 				System.out.println("got attendees"+attendees.size());
 				for(String u : attendees.toArray(new String[0])){
 					HashSet<String> friends = lastfmObj.getUserFriends(key, u);
 					//System.out.println("got friends"+friends.size());
 					hmFriends.put(u, friends);
 					User userInfo = lastfmObj.getUserInfo(key, u);
 					System.out.println("got user info");
 					hmUser.put(u, userInfo);
 					userCnt ++;
 					if(userCnt % 3 == 0){
 						dumpMaps(userCnt);
 					}
 					for (String f : friends){
 						User info = lastfmObj.getUserInfo(key, f);
 						hmUser.put(f, info);
 
 						HashSet<String> fr = lastfmObj.getUserFriends(key, f);
 						hmFriends.put(f, fr);
 
 					}
 
 				}
 				eventCount ++;
 
 			}
 		}
 
 	}
 
 	public static void dumpMaps(int count){
 		System.out.println("size of friends map "+hmFriends.size());
 		System.out.println("size of user map "+hmUser.size());
 
 		HashMap<String, JSONObject> hmUserForJson = new HashMap<String, JSONObject>();
 		Iterator<String> it = hmUser.keySet().iterator();
 		while(it.hasNext()){
 			String user = it.next();
 			User userInfo = hmUser.get(user);
			if(user != null){
 				HashSet<Track> tracks = userInfo.getHsTracks();
 				ArrayList<JSONObject> tracksJson = new ArrayList<JSONObject>();
 				if(tracks != null && tracks.size() > 0){
 					for(Track t : tracks){
 						TrackJson trk = new TrackJson();
 						trk.setAlbum(new JSONObject(t.getAlbum()));
 						trk.setArtist(new JSONObject(t.getArtist()));
 						trk.setName(t.getName());
 						trk.setID(t.getID());
 						trk.setDuration(t.getDuration());
 						trk.setListeners(t.getListeners());
 						trk.setPlayCount(t.getPlayCount());
 						trk.setTimeofPlay(t.getTimeofPlay());
 						trk.setTagName(t.getTagName());
 						JSONObject trackJson = new JSONObject(trk);
 						tracksJson.add(trackJson);
 					}
 				}
 				UserJson userJson = new UserJson();
 				userJson.setHsTracks(tracksJson);
 
 				JSONObject userInfoJson = new JSONObject(userJson);
 				hmUserForJson.put(user, userInfoJson);
 			}
 		}
 
 		JSONObject friendsJsonObject = new JSONObject( hmFriends );
 		JSONObject userJsonObject = new JSONObject( hmUserForJson );
 		try {	
 			FileWriter friendsFile = new FileWriter(outpath+"dumps/hmfriends_uk_"+count);
 			friendsFile.write(friendsJsonObject.toString());
 			friendsFile.flush();
 			friendsFile.close();
 
 			FileWriter userFile = new FileWriter(outpath+"dumps/hmUser_uk_"+count);
 			userFile.write(userJsonObject.toString());
 			userFile.flush();
 			userFile.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
 
