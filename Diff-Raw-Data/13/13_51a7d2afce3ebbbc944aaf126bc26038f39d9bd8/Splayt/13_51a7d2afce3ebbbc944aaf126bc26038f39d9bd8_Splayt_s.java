 package controllers;
 
 import com.google.gson.JsonObject;
 import org.apache.commons.io.IOUtils;
 import play.*;
 import play.mvc.*;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.*;
 
 import models.*;
 
 import java.util.UUID;
 
 import org.lightcouch.*;
 
 
 
 public class Splayt extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void newUnit() {
         String newId = UUID.randomUUID().toString().substring(0, 8);
         redirect("/" + newId);
     }
 
     public static void showPage(String id) {
 
         JsonObject json = null;
         CouchDbClient dbClient = new CouchDbClient();
         try {
             json = dbClient.find(JsonObject.class, id);
         }
         catch (Exception ex) {
             // ignore and let method return a null json object.
         }
         renderTemplate("Application/page.html", id, json);
     }
 
     public static void save() {
         String debugmessage = "nothing happened";
         String id = params.get("id");
         String text = params.get("text");
 
         // Save to couchdb
         try {
             CouchDbClient dbClient = new CouchDbClient();
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("_id", id);
             map.put("text", text);
 
            JsonObject json = dbClient.find(JsonObject.class, id);

            if (json != null) {
                String rev = json.get("_rev").toString().replaceAll("\"", "");
                map.put("_rev", rev);
                dbClient.update(map);
             }
             else {
                 dbClient.save(map);
             }
 
             dbClient.shutdown();
             debugmessage = "it seemed to work";
         }
         catch (Exception ex) {
             debugmessage = ex.getMessage();
         }
 
         renderJSON("{\"debuginfo\": \"" + debugmessage + "\"}");
     }
 
 
     public static void upload(String qqfile) {
 
         if (request.isNew) {
 
             FileOutputStream moveTo = null;
 
             Logger.info("Name of the file %s", qqfile);
             // Another way I used to grab the name of the file
             String filename = request.headers.get("x-file-name").value();
 
             Logger.info("Absolute on where to send %s", Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator);
             try {
 
                 InputStream data = request.body;
                 moveTo = new FileOutputStream(new File(Play.getFile("").getAbsolutePath()) + File.separator + "uploads" + File.separator + filename);
                 IOUtils.copy(data, moveTo);
 
             } catch (Exception ex) {
 
                 Logger.error(ex.getMessage());
                 // catch file exception
                 // catch IO Exception later on
                 renderJSON("{success: false}");
             }
 
         }
 
 
         renderJSON("{success: true}");
     }
 
 }
