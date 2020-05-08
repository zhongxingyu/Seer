 package net.sf.taverna.portal.wireit.module;
 
 import net.sf.taverna.portal.wireit.exception.WireItRunException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Christian
  */
 public class InputURIModule extends InputModule{
         
     /**
      * Constructor for passing the json to the super class.
      * 
      * @param json JSON representation of the modules.
      * @throws JSONException Thrown if the json is not in the expected format.
      */
     public InputURIModule (JSONObject json) throws JSONException{
         super(json);
     }
     
     /**
      * Converts the value to a URI and passes it to any Listeners.
      * <p>
      * It is important to pass the Value as a URI otherwise the TavernaModule has no way of knowing
      *    if the object is a String value that looks like a URI or a URI to a location that holds the actual input.
      * @param outputBuilder Logging buffer.
      * @throws WireItRunException Any Exception caught will be wrapped in a single Exception type.
      */
     @Override
     public void run(StringBuilder outputBuilder) throws WireItRunException {
        Object value = values.get("uri");
        //Just in case a StringField is used which used the output tag.
        if (value == null){
            value = values.get("output");
        }
         try {
             URI uri = new URI(value.toString());
             output.fireOutputReady(uri, outputBuilder);
         } catch (URISyntaxException ex) {
             throw new WireItRunException("Ilegal URI: " + value, ex);
         }
     }
 
 }
