 package at.yawk.fimficiton.operation;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import lombok.AccessLevel;
 import lombok.Cleanup;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NonNull;
 import lombok.RequiredArgsConstructor;
 import lombok.Setter;
 import lombok.experimental.FieldDefaults;
 
 import org.xml.sax.SAXException;
 
 import at.yawk.fimficiton.FimFiction;
 import at.yawk.fimficiton.Story;
 import at.yawk.fimficiton.html.FullSearchParser;
 import at.yawk.fimficiton.json.StoryParser;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  * Request story metadata.
  * 
  * @author Yawkat
  */
 @Getter
 @FieldDefaults(level = AccessLevel.PRIVATE)
 @EqualsAndHashCode(callSuper = false)
 @RequiredArgsConstructor
 public class GetStoryMetaOperation extends AbstractRequest<Story> {
     /**
      * Story for which information should be requested. Right now, only the
      * {@link Story#id} field is required but try to provide as accurate
      * information as possible nonetheless.
      */
     @NonNull final Story storyFor;
     /**
      * {@link RequestMethod} to use to get story metadata.
      */
     @NonNull @Setter RequestMethod requestMethod = RequestMethod.JSON;
     
     @Override
     protected Story request(final FimFiction session) throws Exception {
         switch (this.requestMethod) {
         case JSON:
             return this.requestJson();
         case WEB:
             return this.requestFull(session);
         default:
             throw new IllegalStateException();
         }
     }
     
     /**
      * Load story data using JSON request.
      */
     private Story requestJson() throws IOException {
         // prepare URL
         final URL targeting = new URL("http://fimfiction.net/api/story.php?story=" + this.storyFor.getId());
         
         // download and convert to JsonObject using Gson
         final JsonObject returned;
         final InputStream request = targeting.openStream();
         try {
             returned = new JsonParser().parse(new InputStreamReader(request)).getAsJsonObject();
         } finally {
             request.close();
         }
         
         // parse JSON data
         final StoryParser parser = new StoryParser();
        return parser.parse(returned.getAsJsonObject("story"));
     }
     
     private Story requestFull(final FimFiction session) throws IOException, SAXException {
         // prepare URL
         final URL targeting = new URL("http://fimfiction.net/story/" + this.storyFor.getId());
         
         final URLConnection connection = targeting.openConnection();
         connection.setRequestProperty("Cookie", Util.getCookies(session));
         connection.connect();
         
         @Cleanup final Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
         return new FullSearchParser().parse(reader).get(0);
     }
     
     /**
      * Request method to be used to download story metadata. Different methods
      * may differ in speed, failure frequency and accuracy.
      */
     public static enum RequestMethod {
         /**
          * Fast, accurate and pretty low exception rate. Uses
          * {@link StoryParser}. Does not return sex/gore flags and characters or
          * any user-related metadata such as favorite status.
          */
         JSON,
         /**
          * Slow compared to {@link #JSON}, very likely to break with FimFiction
          * updates. Try to avoid this if you don't need sex/gore flags,
          * characters or account-specific information. Parses the story page and
          * also returns data such as favorite status, read / unread chapters,
          * like token etc.
          */
         WEB;
     }
 }
