 package gx.realtime;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.api.client.auth.oauth2.Credential;
 import gx.browserchannel.NormalizedJsonReader;
 import gx.browserchannel.util.URLWithQuery;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Top-level loader for the realtime API.
  */
 public class RealtimeLoader {
     
     // Callback interfaces
     public interface OnDocumentLoadedCallback {
         void onFileLoaded(Document doc);
     }
     public interface InitializeModelCallback {
         void initializeModel(Model model);
     }
     public interface HandleErrorsCallback {
         void handleErrors(Exception e);
     }
 
     // Properties for realtime loader
     private static String channelUrl = "https://drive.google.com/otservice";
     private static JsonFactory jfactory = new JsonFactory();
 
     private static class ModelResponse {
         @JsonProperty("modelId")
         private String modelId;
 
         public String getModelId() {
             return modelId;
         }
     }
     
     private RealtimeOptions options;
     
     public RealtimeLoader(RealtimeOptions options) {
         this.options = options;
     }
 
     /**
      * To avoid global states, the token will not be saved in this static class.
      * If we need the token anyway, it is advised to save it in the Document, and add a getToken() function to the Document class.
      * @return
      */
     @Deprecated
     public static String getToken(){
         return null;
     }
 
     private static String retrieveModelId(Credential cred, String docId) {
         // Set up parameters
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("id", docId);
         parameters.put("access_token", cred.getAccessToken());
 
         try {
             // Create connection
             URLWithQuery urlq = new URLWithQuery(new URL(channelUrl + "/modelid"), parameters);
             HttpURLConnection connection = (HttpURLConnection) urlq.getURL().openConnection();
             Reader in = new NormalizedJsonReader(connection.getInputStream());
 
             // Parse response
             JsonParser jParser = jfactory.createParser(in);
             ObjectMapper mapper = new ObjectMapper();
             jParser.setCodec(mapper);
             ModelResponse response = jParser.readValueAs(ModelResponse.class);
             in.close();
             return response.getModelId();
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private static Session createSession(Credential cred, String modelId) {
         // Set up parameters
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("id", modelId);
         parameters.put("access_token", cred.getAccessToken());
 
         try {
             // Create connection
             URLWithQuery urlq = new URLWithQuery(new URL(channelUrl + "/gs"), parameters);
             HttpURLConnection connection = (HttpURLConnection) urlq.getURL().openConnection();
             Reader in = new NormalizedJsonReader(connection.getInputStream());
 
             // Parse response
             JsonParser jParser = jfactory.createParser(in);
             ObjectMapper mapper = new ObjectMapper();
             jParser.setCodec(mapper);
             Session message = jParser.readValueAs(Session.class);
             in.close();
             return message;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private Document getDocument(Credential cred, String docId){
         String modelId = retrieveModelId(cred, docId);
         Session session = createSession(cred, modelId);
 
         Document doc = new Document(cred, session);
         return doc;
     }
     
     public void load(
         Credential cred,
         String docId,
         OnDocumentLoadedCallback onLoaded,
         InitializeModelCallback initializeFn,
         HandleErrorsCallback errorFn
     ) {
         Document doc = getDocument(cred, docId);

        if(options.getInitializeModel() != null)
            options.getInitializeModel().initializeModel(doc.getModel());
        if(options.getOnFileLoaded() != null)
         options.getOnFileLoaded().onFileLoaded(doc);
     }
     
     public void start() {
         Authorizer authorizer = new Authorizer();
         
         Credential cred;
         try {
             cred = authorizer.authorize();
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
         
         load(
                 cred,
                 options.getDocId(),
                 options.getOnFileLoaded(),
                 options.getInitializeModel(),
                 options.getHandleErrors()
         );
     }
     public static String getChannelUrl()
     {
         return channelUrl;
     }
 }
