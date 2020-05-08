 package libs.lastfm;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import play.libs.ws.WSUrlFetch;
 
 import java.net.URL;
 
 /**
  * User: derf4002
  * Date: 4/23/11
  * Time: 8:56 PM
  */
 public class LastFMApi {
 
     private static final String API_URL = "http://ws.audioscrobbler.com/2.0/?format=json";
 

     public enum PictureSize {
         LARGE("large"), LARGE_SQUARE("largesquare"), MEDIUM("medium"), SMALL("small"), EXTRA_LARGE("extralarge");
 
         private String name;

         PictureSize(String name) {
             this.name = name;
         }
     }
 
     private String apiKey;
 
 
     public LastFMApi(String apiKey) {
         this.apiKey = apiKey;
     }
 
     public URL retrieveArtistImage(String name, PictureSize size) {
         String serviceURL = API_URL + "&api_key=" + apiKey + "&method=artist.getimages"
                 + "&artist=" + name + "&limit=1";
 
         WSUrlFetch ws = new WSUrlFetch();
         JsonObject response = ws.newRequest(serviceURL).get().getJson().getAsJsonObject();
 
         try {
            JsonArray images = response.get("images").getAsJsonObject().get("image").getAsJsonObject().get("sizes").getAsJsonObject().get("size").getAsJsonArray();

             for (JsonElement image : images) {
                 String imageSize = image.getAsJsonObject().get("name").getAsString();
                if (imageSize.equals(size.name)) {
                     String imagePath = image.getAsJsonObject().get("#text").getAsString();
                     return new URL(imagePath);
                 }
             }
        } catch (Exception e) {

         }

         return null;
     }
 
     public static void main(String... args) {
         LastFMApi fmApi = new LastFMApi("1cae0d3a28fc36a955ea9241610d113a");
         System.out.println(fmApi.retrieveArtistImage("TTC", PictureSize.LARGE_SQUARE));
     }
 
 }
