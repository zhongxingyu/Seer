 package com.sniper.core;
 
 //import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.util.Log;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 import com.sniper.utility.DbContract;
 
 public class Game {
 
         private ParseObject game;
         
         private ParseUser user;
         private String name, houseRules, debugInfo;
         private ArrayList<String> playerIds, targetIds, locationIds;
         private Date startTime, endTime;
         private boolean safeInside, isPublic;
         private String moderatorId;
         
         private int points;
         
         public Game() 
         {
                createGameParseObject();
         }
         
         public Game(ParseObject gameObject)
         {
                 pullDataFromParseObject(gameObject);
         }
                
         private void createGameParseObject()
         {
                 game = new ParseObject(Game.class.getSimpleName());
                 game.put(DbContract.Game.CREATOR, ParseUser.getCurrentUser());
                 game.saveEventually( new SaveCallback() {
                            public void done(ParseException e) {
                                      if (e != null) 
                                             createGameParseObject();
                            }
                 });
         }
         
         private void pullDataFromParseObject(ParseObject gameObject)
         {
             game = gameObject;
             name = game.getString(DbContract.Game.NAME);
             user = game.getParseUser(DbContract.Game.CREATOR);
             startTime = gameObject.getDate(DbContract.Game.START_TIME);
             endTime = gameObject.getDate(DbContract.Game.END_TIME);
             
             playerIds = convertJSONStringArrayToArrayList (gameObject.getJSONArray(DbContract.Game.PLAYERS));
             targetIds = convertJSONStringArrayToArrayList (gameObject.getJSONArray(DbContract.Game.TARGETS));
             houseRules = gameObject.getString(DbContract.Game.HOUSE_RULES);
                 safeInside = gameObject.getBoolean(DbContract.Game.SAFE_INSIDE);
                 moderatorId = gameObject.getString(DbContract.Game.MODERATOR);
                 locationIds = convertJSONStringArrayToArrayList (gameObject.getJSONArray(DbContract.Game.LOCATION_OBJECTS));
                 isPublic = gameObject.getBoolean(DbContract.Game.IS_PUBLIC);
                 
             debugInfo = game.getString(DbContract.Game.DEBUGINFO);
             if(!debugInfo.equals(""))
                     System.out.println("Error in " + Game.class.getSimpleName() + " pull method: " + debugInfo);
         }
         
         public void pullParseObject() {
                 
                 game.fetchInBackground(new GetCallback<ParseObject>() {
                           public void done(ParseObject gameObject, ParseException e) {
                             if (e == null) 
                                     pullDataFromParseObject(gameObject);
                             else 
                                     pullParseObject();
                           }
                         });
         }
         
         public void pushParseObject() {
                 
                 game.fetchInBackground(new GetCallback<ParseObject>() {
                           public void done(ParseObject gameObject, ParseException e) {
                             if (e == null) 
                             {
                                         game.put(DbContract.Game.NAME, name);
                                         game.put(DbContract.Game.START_TIME, startTime); //.getTime());
                                         game.put(DbContract.Game.END_TIME, endTime); //.getTime());
                                         
                                         game.put(DbContract.Game.PLAYERS, playerIds);
                                         game.put(DbContract.Game.TARGETS, targetIds);
                                         game.put(DbContract.Game.HOUSE_RULES, houseRules);
                                         game.put(DbContract.Game.SAFE_INSIDE, safeInside);
                                         game.put(DbContract.Game.MODERATOR, moderatorId);
                                         game.put(DbContract.Game.LOCATION_OBJECTS, locationIds);
                                         game.put(DbContract.Game.IS_PUBLIC, isPublic);
                                         
                                         game.saveEventually( new SaveCallback() {
                                                    public void done(ParseException e) {
                                                              if (e != null) 
                                                                     pushParseObject();
                                                    }
                                         });
                             } 
                             else 
                                     pushParseObject();
                           }
                         });
         }
         
         private ArrayList<String> convertJSONStringArrayToArrayList(JSONArray jsonArray)
         {
                 ArrayList<String> list = new ArrayList<String>();     
                 if (jsonArray != null) { 
                    int len = jsonArray.length();
                    for (int i=0;i<len;i++){ 
                     try
                         {
                                 list.add(jsonArray.get(i).toString());
                         } catch (JSONException e)
                         {
                                 e.printStackTrace();
                         }
                    } 
                 } 
                 
                 return list;
         }
         
         public void invitePlayer(String playerEmail)
         {
         	ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
         	query.whereEqualTo("email", playerEmail);
         	query.findInBackground(new FindCallback<ParseObject>() {
         	    public void done(List<ParseObject> userList, ParseException e) {
         	        if (e == null) {
         	            //send notification to
         	        	userList.get(0);
         	        } else {
         	            //send email
         	        }
         	    }
         	});
         }
 
         /*************************************
          * Accessor Methods
          *************************************/
         /**
          * @return the name
          */
         public String getName() {
                 return name;
         }
 
         /**
          * @param name the name to set
          */
         public void setName(String name) {
                 this.name = name;
         }
 
         /**
          * @return the players
          */
         public ArrayList<String> getPlayers() {
                 return playerIds;
         }
 
         /**
          * @param players the players to set
          */
         public void setPlayers(ArrayList<String> playerIds) {
                 this.playerIds = playerIds;
         }
 
         /**
          * @return the targets
          */
         public ArrayList<String> getTargetIds() {
                 return targetIds;
         }
 
         /**
          * @param targets the targets to set
          */
         public void setTargetIds(ArrayList<String> targetIds) {
                 this.targetIds = targetIds;
         }
 
         /**
          * @return the startTime
          */
         public Date getStartTime() {
                 return startTime;
         }
 
         /**
          * @param startTime the startTime to set
          */
         public void setStartTime(Date startTime) {
                 this.startTime = startTime;
         }
 
         /**
          * @return the endTime
          */
         public Date getEndTime() {
                 return endTime;
         }
 
         /**
          * @param endTime the endTime to set
          */
         public void setEndTime(Date endTime) {
                 this.endTime = endTime;
         }
 
         /**
          * @return the houseRules
          */
         public String getHouseRules() {
                 return houseRules;
         }
 
         /**
          * @param houseRules the houseRules to set
          */
         public void setHouseRules(String houseRules) {
                 this.houseRules = houseRules;
         }
 
         /**
          * @return the safeInside
          */
         public boolean isSafeInside() {
                 return safeInside;
         }
 
         /**
          * @param safeInside the safeInside to set
          */
         public void setSafeInside(boolean safeInside) {
                 this.safeInside = safeInside;
         }
 
         /**
          * @return the moderator
          */
         public String getModeratorId() {
                 return moderatorId;
         }
 
         /**
          * @param moderator the moderator to set
          */
         public void setModeratorId(String moderatorId) {
                 this.moderatorId = moderatorId;
         }
 
         /**
          * @return the points
          */
         public int getPoints() {
                 return points;
         }
 
         /**
          * @param points the points to set
          */
         public void setPoints(int points) {
                 this.points = points;
         }
 
         /**
          * @return the locationObjects
          */
         public ArrayList<String> getLocationObjects() {
                 return locationIds;
         }
 
         /**
          * @param locationObjects the locationObjects to set
          */
         public void setLocationObjects(ArrayList<String> locationIds) {
                 this.locationIds = locationIds;
         }
         
         /**
          * @return the isPublic
          */
         public boolean getIsPublic() {
                 return isPublic;
         }
 
         /**
          * @param isPublic the isPublic to set
          */
         public void setIsPublic(boolean isPublic) {
                 this.isPublic = isPublic;
         }
 }
