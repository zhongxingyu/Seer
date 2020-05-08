 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.asyncutilities;
 
 import android.content.Context;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.entities.Course;
 import no.hials.muldvarp.entities.ListItem;
 import no.hials.muldvarp.entities.Programme;
 import no.hials.muldvarp.entities.Video;
 import no.hials.muldvarp.video.VideoActivity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class contains functionality to make and process requests, as well as caching the results.
  * 
  * @author johan
  */
 public class WebResourceUtilities {   
 
     
     /**
      * This function creates an ArrayList of ListItems from a JSONArray represented
      * by a String. Currently supports Video, Programmes, Course.
      * 
      * @param jsonString String value of JSONArray
      * @param type The type of JSONArray represented by it's cache name
      * @param context The application context to get the cache String name
      * @return ArrayList<ListItem>
      */
     public static ArrayList<ListItem> createListItemsFromJSONString(String jsonString, String type, Context context) {
         ArrayList itemList = new ArrayList();        
         
         try {
             System.out.println("WebResourceUtilities: Printing JSONString: " + jsonString);
             JSONArray jArray = new JSONArray(jsonString);            
                        
             if (type.equals(context.getString(R.string.cacheVideoCourseList))
                     || type.equals(context.getString(R.string.videoBookmarks))
                     || type.equals(context.getString(R.string.cacheVideoStudentList))) {
                 
                 //Video BS her
                 System.out.println("WebresourceUtilities: Array length: " + jArray.length());
                 for (int i = 0; i < jArray.length(); i++) {
                     JSONObject currentObject = jArray.getJSONObject(i);
                     itemList.add(new Video(currentObject.getString("id"),
                             currentObject.getString("videoName"),
                             currentObject.getString("videoDetail"),
                             currentObject.getString("videoDescription"),
                             currentObject.getString("videoType"),
                             null,
                             currentObject.getString("videoURI")));
                 }
 
             } else if (type.equals(context.getString(R.string.cacheCourseList))) {
 
                 //Course BS her
                 for (int i = 0; i < jArray.length(); i++) {
 
                     JSONObject currentObject = jArray.getJSONObject(i);
 
                     itemList.add(new Course(currentObject.getString("name"),
                             null,
                             null,
                             null,
                             null));
 
                 }
             } else if (type.equals(context.getString(R.string.cacheProgrammeList))) {
 
                 //Course BS her
                 for (int i = 0; i < jArray.length(); i++) {
 
                     JSONObject currentObject = jArray.getJSONObject(i);
 
                     itemList.add(new Programme(currentObject.getString("id"), 
                             currentObject.getString("name"),
                             null,  //no small description
                             currentObject.getString("detail"),
                             null, //no type
                             null)); //no bitmap (yet)
 
                 }
             } else {
 
                 System.out.println("It was null. or irrelevant :<");
             }
         } catch (Exception ex) {
             System.out.println("WebResourceUtilities: Failed to convert String of alleged type " + type );
             ex.printStackTrace();
         }
 
         return itemList;
     }
     
     public static String createJSONStringFromListItem(ArrayList<ListItem> listItem, String type, Context context){
         
         String retVal = "";
         JSONArray jsonArray = new JSONArray();
         
         
         if(type.equals(context.getString(R.string.videoBookmarks))
                 || type.equals(context.getString(R.string.cacheVideoStudentList))){
             
             
             for (int i = 0; i < listItem.size(); i++) {
                 
                 try {
                     JSONObject jSONObject = new JSONObject();
                     Video video = (Video) listItem.get(i);
                     
                     
                     jSONObject.put("id", video.getVideoID());
                     jSONObject.put("videoName", video.getItemName());
                     jSONObject.put("videoDetail", video.getSmallDetail());
                     jSONObject.put("videoDescription", video.getItemDescription());
                     jSONObject.put("videoType", video.getItemType());
                     jSONObject.put("videoIconURI", video.getVideoID());
                     jSONObject.put("videoThumbURI", video.getSmallDetail());
                     jSONObject.put("videoURI", video.getVideoURL());
                     
                     jsonArray.put(jSONObject);
                     
                 } catch (JSONException ex) {
                     Logger.getLogger(WebResourceUtilities.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
             }
             
             
             
             retVal = jsonArray.toString();
         } else if (type.equals(context.getString(R.string.cacheProgrammeList))){
             
             for (int i = 0; i < listItem.size(); i++) {
                 
                 try {
                     JSONObject jSONObject = new JSONObject();
                     Programme programme = (Programme) listItem.get(i);
                     
                     jSONObject.put("name", programme.getItemName());
                    jSONObject.put("id", programme.getProgrammeID());
                     jSONObject.put("detail", programme.getItemDescription());
                     
                     jsonArray.put(jSONObject);
                     
                 } catch (JSONException ex) {
                     Logger.getLogger(WebResourceUtilities.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
             }
            retVal = jsonArray.toString();
         }
         return retVal;
     }
     
     public static ArrayList<ListItem> getVideosFromYoutubeFeed(String JSONString){
 
         ArrayList<ListItem> videoList = new ArrayList<ListItem>();
         
         try {
             //Traverse through entries in the JSONObject
             JSONObject jsonObject = new JSONObject(JSONString);
 
             JSONObject feed = jsonObject.getJSONObject("feed");
             JSONArray entry = feed.getJSONArray("entry");
             
             for (int i = 0; i < entry.length(); i++) {
                 
                 JSONObject entryObject = entry.getJSONObject(i);
                 
                 Video video = new Video(Integer.toString(i),
                                         entryObject.getJSONObject("title").getString("$t"),
                                         entryObject.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t"),
                                         entryObject.getJSONObject("content").getString("$t"),
                                         "Youtube/ID",
                                         null,
                                         entryObject.getJSONArray("link").getJSONObject(0).getString("href"));
                 
                 videoList.add(video);
                 
             }
                         
         } catch (JSONException ex) {
             Logger.getLogger(VideoActivity.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         return videoList;
     }
 }
