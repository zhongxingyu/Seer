 package info.bowkett.joxy;
 
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

 /**
  * Created by IntelliJ IDEA.
  * User: jbowkett
  * Date: Jul 24, 2013
  * Time: 9:48:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RequestParser {
   public Request parseRequest(String request) {
    final StringTokenizer tokenizer = new StringTokenizer(request, "\n");
    final Map<String, String> values = new HashMap<String, String>();
    while(tokenizer.hasMoreElements()){
      final String line = (String) tokenizer.nextElement();
      final int index = line.indexOf(' ');
      if(index > -1){
        final String key = line.substring(0, index);
        final String value = line.substring(index +1);
        values.put(key.replaceAll(" |:", "").toUpperCase(), value);
      }
    }
    return new Request(values.get("HOST"), values.get("PROXY-CONNECTION"),
      values.get("CACHE-CONTROL"), values.get("ACCEPT"), values.get("USER-AGENT"),
      values.get("ACCEPT-ENCODING"), values.get("ACCEPT-LANGUAGE"),
      values.get("COOKIE"));
   }
 }
