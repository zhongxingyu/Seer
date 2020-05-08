 package com.vonhof.babelshark.language;
 
 import com.vonhof.babelshark.BabelShark;
 import com.vonhof.babelshark.Input;
 import com.vonhof.babelshark.exception.MappingException;
 import com.vonhof.babelshark.node.ArrayNode;
 import com.vonhof.babelshark.node.ObjectNode;
 import com.vonhof.babelshark.node.ValueNode;
 import java.io.IOException;
 import junit.framework.TestCase;
 
 /**
  *
 * @author Henrik Hofmeister <hh@cphse.com>
  */
 public class JsonLanguageTest extends TestCase {
     
     private static final String JSON_SIMPLE = "[true,false]";
     private static final String JSON_OBJECT = "{\"_id\":\"Some id\",\"name\":\"some name\",\"active\":true,\"visits\":123}";
     
     public JsonLanguageTest(String testName) {
         super(testName);
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         BabelShark.register(new JsonLanguage());
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testCanReadSimpleJson() throws MappingException, IOException {
         Input input = new Input(JSON_SIMPLE, "json");
         ArrayNode result = BabelShark.read(input,ArrayNode.class);
         assertEquals(new ValueNode(true),result.get(0));
         assertEquals(new ValueNode(false),result.get(1));
     }
     
     public void testCanReadJsonObject() throws MappingException, IOException {
         Input input = new Input(JSON_OBJECT, "json");
         ObjectNode result = BabelShark.read(input,ObjectNode.class);
         
         assertEquals(new ValueNode("Some id"),result.get("_id"));
         assertEquals(new ValueNode("some name"),result.get("name"));
         assertEquals(new ValueNode(true),result.get("active"));
         assertEquals(new ValueNode(123),result.get("visits"));
     }
     
     
     public void testCanWriteSimpleJson() throws MappingException, IOException {
         ArrayNode array = new ArrayNode();
         array.add(true,false);
         
         String json = BabelShark.writeToString(array,"json");
         assertEquals(JSON_SIMPLE,json);
     }
     
     public void testCanWriteJsonObject() throws MappingException, IOException {
         ObjectNode obj = new ObjectNode();
         obj.put("_id","Some id");
         obj.put("name","some name");
         obj.put("active",true);
         obj.put("visits",123);
         
         String json = BabelShark.writeToString(obj,"json");
         assertEquals(JSON_OBJECT,json);
     }
 }
