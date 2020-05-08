 package com.kmcginn.airquotes;
 
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import android.widget.ArrayAdapter;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseRelation;
 import com.parse.ParseUser;
 
 public class MessageHolder {
 
 	Set<ParseObject> messages = new LinkedHashSet<ParseObject>();
 	Set<ParseObject> map_messages = new LinkedHashSet<ParseObject>();
 	Set<ParseObject> friends = new LinkedHashSet<ParseObject>();
	Set<String> friendNames = new LinkedHashSet<String>();

 	private static String COUPON_URL = "http://www.hoosiertimescoupons.com/api/";
 
 	//constructor
 	public MessageHolder() {
 		refreshFriends(null);
 	}
 	
 	public void refreshFriends(final ArrayAdapter<String> flistAdapter) {
 		
 		if(flistAdapter != null) {
 			flistAdapter.clear();
 			flistAdapter.notifyDataSetChanged();
 		}
 		
 		//initialize friends here based on current parse user
 			ParseUser currUser= ParseUser.getCurrentUser();
 			// get relation and query
 		    ParseRelation friendsRel= currUser.getRelation("friends");
 		    ParseQuery friendsQuery= friendsRel.getQuery();
 		        
 		    friendsQuery.findInBackground(new FindCallback() {
 					public void done(List<ParseObject> users, ParseException e) {
 						if (e == null){
 							friends.clear();
 							for(ParseObject o: users){
 								try {
 								friends.add( o);
								friendNames.add(o.getString("username"));
 								}
 								catch (Exception e1){
 									Log.e("collection","Couldn't add to collection: "+o.getString("username")+e1);
 								}
 								
 								if(flistAdapter != null) {
 									flistAdapter.add(o.getString("username"));
 									flistAdapter.notifyDataSetChanged();
 								}
 							}
 						}
 						else {
 		    				Log.e("friends", "Unable to download friends: " + e);
 
 						}
 						
 					}
 		        	
 		        });
 		
 	}
 	
 	public void refreshMap(double nearbyRadius, LatLng loc, final GoogleMap mMap, final HashMap<Marker, ParseObject> idMap, final HashMap<Marker, JSONObject> coupMap) {
 		
 		mMap.clear();
 		
 		Boolean friendsOnly;
 		
 		friendsOnly= (Boolean) ParseUser.getCurrentUser().get("viewFriends");
 		
 		// setup parse query (for messages)
         ParseQuery query = new ParseQuery("Message");
         
         try {
 	        //set distance away
 	        query.whereWithinMiles("location", new ParseGeoPoint(loc.latitude,loc.longitude), nearbyRadius);
         } catch (Exception e1){
 			Log.e("loc", "Unable to get location: " + e1);
         }
 		if (friendsOnly){
        	query.whereContainedIn("user", friendNames);
         }
 		
     	// find them in the background
 		try {
         query.findInBackground(new FindCallback() {
         	public void done(List<ParseObject> objects, ParseException e) {
 
         		if(e == null) {
         			//success
         			map_messages.clear();
         			// add all objects from query to set
         			for(ParseObject o: objects){
         				
         				map_messages.add(o);
         			}
         			
         			
         			//TODO: move this into a function?
         			if(mMap != null) {
         				
         				mMap.clear();
         				Marker temp;
         				for(ParseObject o: map_messages) {
         					
         					// get the message text
             				String text = ((ParseObject) o).getString("text").toString();
             				//get the message's user
             				String user = ((ParseObject) o).getString("user").toString();
             				// get the location
             				ParseGeoPoint pt = ((ParseObject) o).getParseGeoPoint("location");
             				temp = mMap.addMarker(new MarkerOptions()
             					.position(new LatLng(pt.getLatitude(), pt.getLongitude()))
             					.title(text)
             					.snippet(user));
             				idMap.put(temp, o);
         				}
         				
         			}
         			
         		}
         		else {
         			//failure
         			Log.e("find","Unable to find posts: "+e);
        			
         		}
         		
         		refreshCoupons(coupMap, mMap);
         	}
         });
 		}
 		catch (Exception e1){
 			Log.e("find","Unable to enter findCallback: "+e1);
 			
 		}
 		
 		
 		
 	}
 	
 	private void refreshCoupons(HashMap<Marker, JSONObject> coupMap, GoogleMap mMap) {
 		FeedFetcher feedFetcher = new FeedFetcher();
         JSONArray array = feedFetcher.makeHTTPRequest(COUPON_URL);
         JSONObject myObj = new JSONObject();
         JSONArray coupons = new JSONArray();
         double lt = 0;
         double ln = 0;
         String locName = "";
         Marker temp;
         for(int i = 0; i < array.length(); i++) {
         	try {
 				myObj = array.getJSONObject(i);
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
         	
         	try {
 				lt = myObj.getDouble("lat");
 				locName = myObj.getString("name");
 				ln = myObj.getDouble("lon");
 				coupons = myObj.getJSONArray("coupons");
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 			}
         	
         	
         	temp = mMap.addMarker(new MarkerOptions()
         		.position(new LatLng(lt, ln))
         		.title(locName)
         		.snippet(coupons.length() + " coupon(s) available")
         		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
         	
         	try{
         		coupMap.put(temp, myObj);
         	} catch(Exception e) {
         		Log.e("message_h", "Unable to store pin: " + e.toString());
         	}
         	
         	
         	Log.d("map", "adding pin at " + locName);
         }
 	
 	}
 	
 	public Set<ParseObject> getAll() {
 		
 		return messages;
 	}
 	
 	public ParseObject get(int index) {
 		return (ParseObject) messages.toArray()[index];
 		
 	}
 	
 	public void refreshList(double nearbyRadius, LatLng loc, final ArrayAdapter<String> listAdapter) {
 		
 		listAdapter.clear();
 		listAdapter.notifyDataSetChanged();
 		
 		Boolean friendsOnly;
 		
 		friendsOnly= (Boolean) ParseUser.getCurrentUser().get("viewFriends");
 		
 		// setup parse query (for messages)
         ParseQuery query = new ParseQuery("Message");
         
         try {
 	        //set distance away
 	        query.whereWithinMiles("location", new ParseGeoPoint(loc.latitude,loc.longitude), nearbyRadius);
         } catch (Exception e1){
 			Log.e("loc", "Unable to get location: " + e1);
         }
 
         if (friendsOnly){
        	query.whereContainedIn("user", friendNames);
         }
 		
     	// find them in the background
 		try {
         query.findInBackground(new FindCallback() {
         	public void done(List<ParseObject> objects, ParseException e) {
         		
         		messages.clear();
         		if(e == null) {
         			//success
         			// add all objects from query to set
         			for(ParseObject o: objects){
         				
         				messages.add(o);
         			}
         			
         			
         			//TODO: move this into a function?
         			if(listAdapter != null) {
         				
         				for(ParseObject o: messages) {
         					// get the message
     	        			String text = o.getString("text").toString();
     	        			// get the author
     	        			String author = o.getString("user").toString();
     	        			// add a string combining the message and location
     	        			listAdapter.add(text + "\n" + author);      					
         				}
         				
         				//indicate that the list adapter has changed its data, so the listview will update
         				listAdapter.notifyDataSetChanged();
         			}
         			
         		}
         		else {
         			//failure
         			Log.e("find","Unable to find posts: "+e);
        			
         		}
         	}
         });
 		}
 		catch (Exception e1){
 			Log.e("find","Unable to enter findCallback: "+e1);
 			
 		}
 		
 	}
 }
