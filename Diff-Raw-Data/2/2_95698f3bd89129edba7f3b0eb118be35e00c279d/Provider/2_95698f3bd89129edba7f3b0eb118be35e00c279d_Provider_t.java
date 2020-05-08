 package models;
 
 import org.codehaus.jackson.JsonNode;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jérôme
  * Date: 01/06/13
  * Time: 01:06
  * To change this template use File | Settings | File Templates.
  */
 public enum Provider {
     DEEZER("https://api.deezer.com/2.0/", "https://api.deezer.com/2.0/search") {
         @Override
         public Query buildSearchQuery(Track track) {
 
             Query searchParameters = new Query();
             searchParameters.url = searchBaseUrl;
             searchParameters.queryParameters.put("q", track.artist + " " + track.title);
 
             return searchParameters;
 
         }
 
         @Override
         public Query buildLookupQuery(String type, String id) {
 
             Query query = new Query();
             query.url = lookupBaseUrl + type + "/" + id;
             return query;
         }
 
         @Override
         public Track getTrack(JsonNode root) {
             String title = root.findPath("title").getTextValue();
             String artist = root.findPath("artist").findPath("name").getTextValue();
             return new Track(title, artist);
         }
 
         @Override
         public String getTrackUrl(JsonNode root) {
 
             return root.get("data").get(0).findPath("link").getTextValue();
         }
     },
     SPOTIFY("http://ws.spotify.com/lookup/1/", "http://ws.spotify.com/search/1/") {
         @Override
         public Track getTrack(JsonNode root) {
 
 
 
 
 
             String title = root.get("track").get("name").getTextValue();
             String artist = root.get("track").get("artists").get(0).findValue("name").getTextValue();
             return new Track(title, artist);
         }
 
         @Override
         public Query buildLookupQuery(String type, String id) {
 
             Query query = new Query();
             query.url = lookupBaseUrl + ".json";
             query.queryParameters.put("uri", id);
 
             return query;
         }
 
         @Override
         public Query buildSearchQuery(Track track) {
             Query searchParameters = new Query();
             searchParameters.url = searchBaseUrl + "track.json";
             searchParameters.queryParameters.put("q", track.artist + " " + track.title);
 
             return searchParameters;
         }
 
         @Override
         public String getTrackUrl(JsonNode root) {
 
             String spotifyUri = root.findPath("tracks").get(0).findValuesAsText("href").get(1);
             String[] uriParts = spotifyUri.split(":");
            return  "https://play.spotify.com/track/" + uriParts[2];
         }
     };
     protected String lookupBaseUrl;
     protected String searchBaseUrl;
     Provider(String lookupBaseUrl, String searchBaseUrl) {
 
         this.searchBaseUrl = searchBaseUrl;
         this.lookupBaseUrl = lookupBaseUrl;
     }
 
     public abstract Track getTrack(JsonNode root);
 
     public abstract Query buildLookupQuery(String type, String id);
 
     public abstract Query buildSearchQuery(Track track);
 
     public abstract String getTrackUrl(JsonNode root);
 }
