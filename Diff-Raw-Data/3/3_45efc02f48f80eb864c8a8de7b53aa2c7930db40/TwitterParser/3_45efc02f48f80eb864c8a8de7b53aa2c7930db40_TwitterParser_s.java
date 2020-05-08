 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package imageGetters;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.Picture;
 import repository.ExpandUrl;
 
 /**
  *
  * @author T
  */
 public class TwitterParser {
 
     private JsonArray jsonPictures;
     JsonObject obj;
 
     /**
      * Finds the location of the pictures in the InputStreamReader, returns them
      * as a JsonArray.
      *
      * @param reader (InputStreamReader)
      * @return jsonPictures (JsonArray)
      */
     public JsonArray parse(InputStreamReader reader) {
         JsonParser parser = new JsonParser();
         obj = parser.parse(reader).getAsJsonObject();
         jsonPictures = obj.get("statuses").getAsJsonArray();
         jsonPictures = findMedia(jsonPictures);
         return jsonPictures;
     }
 
     private JsonArray findMedia(JsonArray jsonPictures) {
         JsonArray tempArray = new JsonArray();
         for (int i = 0; i < jsonPictures.size(); i++) {
             JsonObject entities = (JsonObject) jsonPictures.get(i).getAsJsonObject().get("entities");
             if (entities != null && hasPictureUrl(entities)) {
                 tempArray.add(jsonPictures.get(i).getAsJsonObject());
             }
         }
         return tempArray;
     }
 
     /**
      * Finds the next url in the InputStreamReader and returns it as a string.
      *
      * @return next_url (String)
      */
     public String getNextUrl() {
         JsonElement next_url = obj.get("search_metadata");
         if (next_url != null) {
             try {
                 String url = next_url.getAsJsonObject().get("next_results").getAsString();
                 if (url != null) {
                     return "https://api.twitter.com/1.1/search/tweets.json" + url;
                 } else {
                     return null;
                 }
             } catch (NullPointerException e) {
                 return null;
             }
         } else {
             return null;
         }
 
     }
 
     /**
      * Finds the URLs in the JsonElement and makes a type Picture out of them.
      *
      * @param j (JsonElement)
      * @return picture (model.Picture)
      */
     public Picture addToList(JsonElement j) {
         Picture picture = null;
         JsonObject jsonPicture = j.getAsJsonObject();
         JsonObject entities = (JsonObject) jsonPicture.getAsJsonObject().get("entities");
 
         JsonElement picTwitMedia = entities.getAsJsonObject().get("media");
         JsonElement otherMedia = entities.getAsJsonObject().get("urls");
         if (picTwitMedia != null) {
             picTwitMedia = ((JsonArray) picTwitMedia).get(0);
             String pictureUrl = picTwitMedia.getAsJsonObject().get("media_url").getAsString();
             picture = new Picture(
                     pictureUrl + ":large",
                     pictureUrl + ":thumb");
             String id = jsonPicture.get("id_str").getAsString();
             picture.setId(id);
             String date = jsonPicture.get("created_at").getAsString();
             long unix_timestamp = strDateToUnixTimestamp(date);
             picture.setUnixDate(unix_timestamp+"");
         } else if (otherMedia != null) {
             otherMedia = ((JsonArray) otherMedia).get(0);
             String pictureUrl = otherMedia.getAsJsonObject().get("expanded_url").getAsString();
             if (pictureUrl.contains("twitpic")) {
                 ExpandUrl expand = new ExpandUrl();
                 String[] Array = pictureUrl.split("/");
                 String thumbUrl = "http://twitpic.com/show/thumb/" + Array[3];
                 String largeUrl = "http://twitpic.com/show/large/" + Array[3];
                 try {
                     thumbUrl = expand.expand(thumbUrl);
                     largeUrl = expand.expand(largeUrl);
                     picture = new Picture(largeUrl, thumbUrl);
                     String id = jsonPicture.get("id_str").getAsString();
                     picture.setId(id);
                     String date = jsonPicture.get("created_at").getAsString();
                     long unix_timestamp = strDateToUnixTimestamp(date);
                     picture.setUnixDate(unix_timestamp+"");
                 } catch (MalformedURLException ex) {
                     Logger.getLogger(TwitterParser.class
                             .getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex) {
                     Logger.getLogger(TwitterParser.class
                             .getName()).log(Level.SEVERE, null, ex);
                 }
 
             } else {
             }
         }
         return picture;
     }
 
     private boolean hasPictureUrl(JsonObject entities) {
         JsonElement media = entities.getAsJsonObject().get("media");
         JsonElement url = entities.getAsJsonObject().get("urls");
         if (media != null) {
             return true;
         }
         if (!"[]".equals(url.toString())) {
             url = ((JsonArray) url).get(0);
             if (url != null) {
                 String pictureUrl = url.getAsJsonObject().get("expanded_url").getAsString();
                 if (pictureUrl.contains("twitpic")) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } else {
             return false;
         }
 
     }
 
     private static long strDateToUnixTimestamp(String dt) {
         DateFormat formatter;
         Date date = null;
         long unixtime;
         final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
         formatter = new SimpleDateFormat(TWITTER, Locale.US);
         try {
             date = formatter.parse(dt);
         } catch (ParseException ex) {
         }
        unixtime = date.getTime() / 1000L;
         return unixtime;
     }
     
     
 }
