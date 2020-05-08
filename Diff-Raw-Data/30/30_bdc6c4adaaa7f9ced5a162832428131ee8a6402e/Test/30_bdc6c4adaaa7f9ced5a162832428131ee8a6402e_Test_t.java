 package at.ac.dbisinformatik.snowprofile.web.svgcreator;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.JavaScriptException;
 import org.mozilla.javascript.Scriptable;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 
 /**
  * 
  */
 
 /**
  * @author Robert Binna
  *
  */
 public class Test {
 
 	/**
 	 * @param args
	 * @throws JSONException 
 	 */
	public static void main(String[] args) throws JSONException {
 		try {
 			boolean pdfFlag = true;
             Context cx = Context.enter();
             Scriptable scope = cx.initStandardObjects();  
             Reader script = new InputStreamReader(Test.class.getResourceAsStream("/at/ac/dbisinformatik/snowprofile/web/resources/includeFunctions.js"));
             cx.evaluateReader(scope, script,"<cmd>", 1, null);
             Object func = scope.get("getJSON", scope);
             
            JSONObject jsObject = new JSONObject(FileUtils.readFileToString(new File("c:\\json.json")));
            String jsonRawString = jsObject.get("SnowProfile").toString();
             
             Object stringify = ((Scriptable) scope.get("JSON", scope)).get("stringify", scope);
             Object jsonParse = ((Scriptable) scope.get("JSON", scope)).get("parse", scope);
             Object jsonRawObject = ((Function)jsonParse).call(cx, scope, scope, new Object[] { jsonRawString });
             if (func instanceof Function) {
                 Object funcArgs[] = new Object[] { jsonRawObject, pdfFlag };
                 Object result = ((Function)func).call(cx, scope, scope, funcArgs);
                 String jsonString = (String) ((Function)stringify).call(cx, scope, scope, new Object[] { result });
                 JsonArray jsonObject = (JsonArray) new JsonParser().parse(jsonString);
                 SVGCreator.svgDocument(jsonObject);
             }
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (JavaScriptException jse) {
             jse.printStackTrace();
         } catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
             Context.exit();
         }
 
 	}
 
 }
