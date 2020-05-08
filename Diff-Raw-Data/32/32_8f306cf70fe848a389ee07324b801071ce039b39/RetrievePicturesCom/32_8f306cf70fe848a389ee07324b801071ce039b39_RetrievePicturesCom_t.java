 package repository;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import model.Picture;
 
 /**
  *
  * @author Emil
  */
 public class RetrievePicturesCom {
 
     private String request = GlobalVariables.baseUrl + "picture/getpictures";
 
     /**
      * Retrieves a list of Pictures from the Server
      *
      * @return An Arraylist of Picture objects.
      * @throws IOException
      */
     public ArrayList<Picture> getImageList() throws IOException {
         ArrayList<Picture> imageList = new ArrayList();
 
         URL url = new URL(request);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Content-Type", "application/v2+json");
         connection.connect();
         InputStreamReader reader = new InputStreamReader(connection.getInputStream());
 
         int respons = connection.getResponseCode();
 
         if (respons == 200) {
             imageList = parsePictures(reader);
         }
         return imageList;
     }
 
     public ArrayList<Picture> getImageListByTag(String tag) throws IOException {
         ArrayList<Picture> imageList = new ArrayList();
 
         URL url = new URL(request + "/" + tag);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Content-Type", "application/v2+json");
         connection.connect();
         InputStreamReader reader = new InputStreamReader(connection.getInputStream());
 
         int respons = connection.getResponseCode();
 
         if (respons == 200) {
             imageList = parsePictures(reader);
         }
         return imageList;
     }
 
     private ArrayList<Picture> parsePictures(InputStreamReader reader) throws MalformedURLException, IOException {
         ExpandUrl expandUrl = new ExpandUrl();
         DeletePicturesCom deletePictures = new DeletePicturesCom();
         StorePicturesCom storePictures = new StorePicturesCom();
 
         ArrayList<Picture> imageList = new ArrayList();
         ArrayList<Picture> forDeletion = new ArrayList();
         ArrayList<Picture> forStorage = new ArrayList();
 
         JsonParser parser = new JsonParser();
         JsonArray imageUrlArray = parser.parse(reader).getAsJsonArray();
 
         for (JsonElement j : imageUrlArray) {
             String largeUrl = j.getAsJsonObject().get("url").getAsString();
             String thumbUrl = j.getAsJsonObject().get("thumburl").getAsString();
             Picture picture = new Picture(
                     largeUrl,
                     thumbUrl);
             picture.setTag(j.getAsJsonObject().get("tag").getAsString());
            picture.setUnixDate(j.getAsJsonObject().get("date").getAsString());
             if (picture.getLargeUrl().contains("twitpic")) {
                 forDeletion.add(picture);
                 picture.setLargeUrl(expandUrl.expand(largeUrl));
                 picture.setThumbUrl(expandUrl.expand(thumbUrl));
                 forStorage.add(picture);
             }
             imageList.add(picture);
         }
         if (!forDeletion.isEmpty() && !forStorage.isEmpty()) {
             deletePictures.deletePictures(forDeletion);
             storePictures.storePictures(forStorage);
         }
         return imageList;
     }
 }
