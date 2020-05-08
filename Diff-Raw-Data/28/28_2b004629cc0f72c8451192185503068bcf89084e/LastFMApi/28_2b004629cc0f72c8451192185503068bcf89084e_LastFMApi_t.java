 package libs.lastfm;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import models.Artist;
 import play.libs.ws.WSUrlFetch;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * User: derf4002
  * Date: 4/23/11
  * Time: 8:56 PM
  */
 public class LastFMApi {
 
     private static final String API_URL = "http://ws.audioscrobbler.com/2.0/?format=json";
 
    
     public enum PictureSize {
         LARGE("large"), MEDIUM("medium"), SMALL("small"), EXTRA_LARGE("extralarge");
 
         private String name;
        
         PictureSize(String name) {
             this.name = name;
         }
     }
 
     private String apiKey;
 
 
     public LastFMApi(String apiKey) {
         this.apiKey = apiKey;
     }
 
     public Artist retrieveArtist(String name, PictureSize size) {
         Artist artist = null;
 
         String serviceURL = API_URL + "&api_key=" + apiKey + "&method=artist.search"
                 + "&artist=" + name + "&limit=1";
         try {
             WSUrlFetch ws = new WSUrlFetch();
             JsonObject response = ws.newRequest(serviceURL).get().getJson().getAsJsonObject();
             JsonObject artistObject = response.get("results").getAsJsonObject().get("artistmatches").getAsJsonObject().get("artist").getAsJsonObject();
 
             String artistName = artistObject.get("name").getAsString();
 
             if (artistName != null) {
                 artist = new Artist();
 
                 artist.name = artistName;
 
                 JsonArray images = artistObject.get("image").getAsJsonArray();
                 for (JsonElement image : images) {
                     try {
                         String imageSize = image.getAsJsonObject().get("size").getAsString();
                         if (imageSize.equals(size.name)) {
                             String imagePath = image.getAsJsonObject().get("#text").getAsString();
                             artist.imagePath = new URL(imagePath).toString();
                             break;
                         }
                     } catch (MalformedURLException e) {
                         e.printStackTrace();
                     }
                 }
             }
         } catch (Exception e) {}
 
         return artist;
     }
 
     public static void main(String... args) {
         LastFMApi fmApi = new LastFMApi("1cae0d3a28fc36a955ea9241610d113a");
 
         Artist artist = fmApi.retrieveArtist("* IAM *", PictureSize.LARGE);
         System.out.println(artist.name);
         System.out.println(artist.imagePath);
 
         artist = fmApi.retrieveArtist("* LinkinPark *", PictureSize.LARGE);
         System.out.println(artist.name);
         System.out.println(artist.imagePath);
 
         artist = fmApi.retrieveArtist("Bjorc", PictureSize.LARGE);
         System.out.println(artist.name);
         System.out.println(artist.imagePath);
 
         artist = fmApi.retrieveArtist("2 be 3", PictureSize.LARGE);
         System.out.println(artist.name);
         System.out.println(artist.imagePath);
 
         artist = fmApi.retrieveArtist("admiralty", PictureSize.LARGE);
         System.out.println(artist.name);
         System.out.println(artist.imagePath);
     }
 
 }
