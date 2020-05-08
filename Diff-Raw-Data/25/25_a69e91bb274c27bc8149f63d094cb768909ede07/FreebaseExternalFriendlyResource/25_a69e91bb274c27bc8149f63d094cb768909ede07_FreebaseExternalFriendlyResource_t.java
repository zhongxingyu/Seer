 package org.triple_brain.module.model;
 
 import com.freebase.api.Freebase;
 import com.freebase.json.JSON;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
import org.triple_brain.module.common_utils.DataFetcher;
 import org.triple_brain.module.common_utils.Urls;
 
import java.net.MalformedURLException;
import java.net.URL;
 import java.util.*;
 
 import static com.freebase.json.JSON.a;
 import static com.freebase.json.JSON.o;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class FreebaseExternalFriendlyResource extends Observable {
     private ExternalFriendlyResource externalFriendlyResource;
 
     public static String DESCRIPTION_BASE_URI = "https://www.googleapis.com/freebase/v1/text/";
 
     public static Boolean isFromFreebase(ExternalFriendlyResource externalFriendlyResource) {
         return externalFriendlyResource
                 .uri()
                 .getHost()
                 .toLowerCase().
                         contains("freebase.com");
     }
 
     public static FreebaseExternalFriendlyResource fromExternalResource(ExternalFriendlyResource externalFriendlyResource) {
         return new FreebaseExternalFriendlyResource(
                 externalFriendlyResource
         );
     }
 
     protected FreebaseExternalFriendlyResource(ExternalFriendlyResource externalFriendlyResource) {
         this.externalFriendlyResource = externalFriendlyResource;
     }
 
     public void getImages(Observer observer) {
         new Thread(new GetImageThread(
                 this,
                 observer
         )).start();
     }
 
     public void getDescription(Observer observer) {
         new Thread(new GetDescriptionThread(
                 this,
                 observer
         )).start();
     }
 
     public String freebaseId() {
         return externalFriendlyResource.uri().toString()
                 .replace("http://rdf.freebase.com/rdf", "");
     }
 
     public ExternalFriendlyResource get() {
         return externalFriendlyResource;
     }
 
     private class GetImageThread implements Runnable{
 
         private FreebaseExternalFriendlyResource freebaseExternalFriendlyResource;
         private Observer observer;
 
         public GetImageThread(FreebaseExternalFriendlyResource freebaseExternalFriendlyResource, Observer observer){
             this.freebaseExternalFriendlyResource = freebaseExternalFriendlyResource;
             this.observer = observer;
         }
 
         @Override
         public void run() {
             observer.update(
                     freebaseExternalFriendlyResource,
                     getImages()
             );
         }
 
         private Set<Image> getImages(){
             String imagesKey = "/common/topic/image";
             String id = "id";
             JSON query = o(
                     id, freebaseId(),
                     imagesKey, a(o(
                     id, null
             ))
             );
             JSON queryEnvelope = o("query", query);
             JSON params = o();
             JSON queryResult= Freebase.getFreebase().mqlread(
                     query, queryEnvelope, params
             ).get("result");
             Set<Image> images = new HashSet<>();
             if(queryResult == null || !queryResult.has(imagesKey)){
                 return images;
             }
             JSON imagesResult = queryResult.get(imagesKey);
             for (JSON imageResult : (List<JSON>) imagesResult.array()) {
                 String imageId = imageResult.get(id).string();
                 images.add(
                         Image.withUrlForSmallAndBigger(
                                 Urls.get(
                                         "http://img.freebase.com/api/trans/image_thumb" + imageId
                                 ),
                                 Urls.get(
                                         "http://img.freebase.com/api/trans/raw" + imageId
                                 )
                         )
                 );
             }
             return images;
         }
     }
 
     private class GetDescriptionThread implements Runnable{
 
         private FreebaseExternalFriendlyResource freebaseExternalFriendlyResource;
         private Observer observer;
 
         public GetDescriptionThread(FreebaseExternalFriendlyResource freebaseExternalFriendlyResource, Observer observer){
             this.freebaseExternalFriendlyResource = freebaseExternalFriendlyResource;
             this.observer = observer;
         }
 
         @Override
         public void run() {
             observer.update(
                     freebaseExternalFriendlyResource,
                     getDescription()
             );
         }
 
         private String getDescription(){
             try{
                JSONObject resultEnveloppe = DataFetcher.getJsonFromUrl(
                        new URL(DESCRIPTION_BASE_URI + freebaseExternalFriendlyResource.freebaseId())
                );
                 return resultEnveloppe.getString("result");
            }catch(JSONException | MalformedURLException e){
                 throw new RuntimeException(e);
             }
         }
     }
 }
