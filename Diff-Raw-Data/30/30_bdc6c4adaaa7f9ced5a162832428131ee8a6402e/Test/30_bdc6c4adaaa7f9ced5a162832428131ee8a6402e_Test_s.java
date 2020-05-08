 package at.ac.dbisinformatik.snowprofile.web.svgcreator;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.io.FileUtils;
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
 	 */
	public static void main(String[] args) {
 		try {
 			boolean pdfFlag = true;
             Context cx = Context.enter();
             Scriptable scope = cx.initStandardObjects();  
             Reader script = new InputStreamReader(Test.class.getResourceAsStream("/at/ac/dbisinformatik/snowprofile/web/resources/includeFunctions.js"));
//            Reader script = new InputStreamReader(Test.class.getResourceAsStream("/at/ac/dbisinformatik/snowprofile/web/resources/data/svgcreator/test.js"));
             cx.evaluateReader(scope, script,"<cmd>", 1, null);
             Object func = scope.get("getJSON", scope);
             
            String jsonRawString = FileUtils.readFileToString(new File("c:\\json.json"));
             
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
