 package net.sourcewalker.garanbot.api;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class ItemService {
 
     private final GaranboClient client;
 
     ItemService(GaranboClient client) {
         this.client = client;
     }
 
     public List<Integer> list() throws ClientException {
         List<Integer> result = new ArrayList<Integer>();
         try {
             HttpResponse response = client.get("/item");
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 String content = client.readEntity(response);
                 JSONObject jsonContent = (JSONObject) new JSONTokener(content)
                         .nextValue();
                 JSONArray refArray = jsonContent.getJSONArray("ref");
                 for (int i = 0; i < refArray.length(); i++) {
                     String refString = refArray.getString(i);
                     int refId = Integer.parseInt(refString.split("/")[1]);
                     result.add(refId);
                 }
             } else {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         } catch (JSONException e) {
             throw new ClientException("JSON Parser error: " + e.getMessage(), e);
         } catch (NumberFormatException e) {
             throw new ClientException("Error parsing id: " + e.getMessage(), e);
         }
         return result;
     }
 
     public Item get(int id) throws ClientException {
         try {
             HttpResponse response = client.get("/item/" + id);
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 String content = client.readEntity(response);
                 return Item.fromJSON(content);
             } else {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         } catch (NumberFormatException e) {
             throw new ClientException("Error parsing id: " + e.getMessage(), e);
         }
     }
 
     public Bitmap getPicture(int id) throws ClientException {
         try {
             HttpResponse response = client.get("/item/" + id + "/picture");
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream content = response.getEntity().getContent();
                Bitmap result = BitmapFactory.decodeStream(content);
                 if (result != null) {
                     return result;
                 } else {
                     throw new ClientException("Picture could not be decoded!");
                 }
             } else {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         }
     }
 
     public void delete(int id) throws ClientException {
         try {
             HttpResponse response = client.delete("/item/" + id);
             if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT)) {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         }
     }
 
     public int create(Item itemData) throws ClientException {
         try {
             if (itemData.getId() != Item.UNKNOWN_ID) {
                 throw new ClientException(
                         "Can't create Item which already has an ID!");
             }
             String jsonData = itemData.json().toString();
             HttpResponse response = client.put("/item", jsonData);
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 String responseContent = client.readEntity(response);
                 return Integer.parseInt(responseContent);
             } else {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         } catch (NumberFormatException e) {
             throw new ClientException("Error parsing new item id: "
                     + e.getMessage(), e);
         }
     }
 
     public void update(Item itemData) throws ClientException {
         try {
             if (itemData.getId() == Item.UNKNOWN_ID) {
                 throw new ClientException("Can't update Item with no ID!");
             }
             String jsonData = itemData.json().toString();
             HttpResponse response = client.put("/item", jsonData);
             if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 throw new ClientException("Got HTTP error: "
                         + response.getStatusLine().toString());
             }
         } catch (IOException e) {
             throw new ClientException("IO error: " + e.getMessage(), e);
         }
     }
 
 }
