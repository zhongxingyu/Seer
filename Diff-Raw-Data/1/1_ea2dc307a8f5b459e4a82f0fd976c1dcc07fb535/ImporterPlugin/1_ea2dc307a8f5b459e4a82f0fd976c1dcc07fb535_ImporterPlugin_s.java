 package de.deepamehta.plugins.importer;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Plugin;
 import de.deepamehta.core.util.UploadedFile;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class ImporterPlugin extends Plugin {
 
     // -------------------------------------------------------------------------------------------------- Static Methods
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
 
 
     // ************************
     // *** Overriding Hooks ***
     // ************************
 
 
 
     @Override
     public JSONObject executeCommandHook(String command, Map params, ClientContext clientContext) {
         if (command.equals("deepamehta3-importer.start")) {
             UploadedFile file = null;
             try {
                 file = (UploadedFile) params.get("file");
                 logger.info("### Importing " + file);
                 importFile(file);
                 //
                 JSONObject result = new JSONObject();
                 result.put("message", "OK");
                 return result;
             } catch (Throwable e) {
                 throw new RuntimeException("Error while importing " + file, e);
             }
         }
         return null;
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     private void importFile(UploadedFile file) throws Exception {
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getFile())));
         StringBuilder builder = new StringBuilder();
         builder.append(reader.readLine() + "\n");
         builder.append(reader.readLine() + "\n");
         builder.append(reader.readLine() + "\n");
         builder.append(reader.readLine() + "\n");
         builder.append(reader.readLine() + "\n");
         logger.info("### First 5 lines:\n" + builder);
     }
 }
