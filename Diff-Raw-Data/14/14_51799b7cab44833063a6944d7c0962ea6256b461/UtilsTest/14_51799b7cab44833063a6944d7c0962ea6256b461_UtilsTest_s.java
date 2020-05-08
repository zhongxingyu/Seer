 package org.wings.plaf.css;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.wings.io.StringBuilderDevice;
 
 import junit.framework.TestCase;
 
 public class UtilsTest extends TestCase {
     private String encodeJSToString(Object o) {
         StringBuilderDevice sb = new StringBuilderDevice(10);
         try {
             Utils.encodeJS(sb, o);
         } catch (IOException e) {
         }
         return sb.toString();
     }
 
     public void test_encodeJS_anytype() {
         assertEquals("null", encodeJSToString(null));
         assertEquals("42", encodeJSToString(new Integer(42)));
         assertEquals("\"foo\"", encodeJSToString("foo"));
     }
 
     public void test_encodeJS_Stringquoting() {
         // Empty.
         assertEquals("null", encodeJSToString(null));
         assertEquals("\"\"", encodeJSToString(""));
 
         // Generic escapes.
         assertEquals("\"\\\\\"", encodeJSToString("\\"));
         assertEquals("\"\\b\"", encodeJSToString("\b"));
         assertEquals("\"\\f\"", encodeJSToString("\f"));
         assertEquals("\"\\n\"", encodeJSToString("\n"));
         assertEquals("\"\\r\"", encodeJSToString("\r"));
         assertEquals("\"\\t\"", encodeJSToString("\t"));
 
         // Special characters are encoded as utf-8 escape
         assertEquals("\"\\u0000\"", encodeJSToString("\u0000"));
         assertEquals("\"\\u001f\"", encodeJSToString("\u001F"));
         assertEquals("\" \"", encodeJSToString("\u0020")); // first non-special

         // And now: all together ;-)
         assertEquals("\"foo\\\\\\\"bar\"", encodeJSToString("foo\\\"bar"));
         assertEquals("\"\\nfoo\\\\\\\"bar\"", encodeJSToString("\nfoo\\\"bar"));
         assertEquals("\"\\nfoo\\\\\\\"bar\\t\"",
                 encodeJSToString("\nfoo\\\"bar\t"));
     }
 
     public void test_JsonArray_rendering() {
         List<Object> list = new ArrayList<Object>();
         list.add("foo");
         list.add(new Integer(42));        
         assertEquals("[\"foo\",42]", 
                 encodeJSToString(Utils.listToJsArray(list)));
     }
     
     public void test_JsonObject_rendering() {
         // Use TreeMap to have a certain sequence
         final Map<String, Object> map = new TreeMap<String,Object>();
         map.put("bar", new Integer(42));
         map.put("baz", "s");
         
         final Map<String, Object> nestedMap = new TreeMap<String,Object>();
         nestedMap.put("success", new Boolean(true));
         map.put("foo", Utils.mapToJsObject(nestedMap));
         
         final Object json = Utils.mapToJsObject(map);
         assertEquals("{\"bar\":42,"
                      + "\"baz\":\"s\","
                      + "\"foo\":{\"success\":true}}",
                      encodeJSToString(json));                
     }
 }
