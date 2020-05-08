 package ee.stacc.transformer.client;
 
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.junit.client.GWTTestCase;
 import ee.stacc.transformer.client.data.json.JsonDataFrame;
 import ee.stacc.transformer.client.mapping.Mapping;
 import ee.stacc.transformer.client.mapping.MappingElement;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 public class JsonDataFrameOptionalityInitializerImplTest extends GWTTestCase {
   private static final String GLOBAL_REF_LEAF = "global_ref#leaf";
 
   public void testInitDataFrameOptionality() throws Exception {
     JsonDataFrame jsonDataFrame = new JsonDataFrame();
     jsonDataFrame.setJsonSchema(getJsonSchema());
    jsonDataFrame.addMapping(new MappingElement("parent/child/leaf", GLOBAL_REF_LEAF), GLOBAL_REF_LEAF);
     JsonDataFrameOptionalityInitializer optInit = new JsonDataFrameOptionalityInitializerImpl();
     optInit.initDataFrameOptionality(jsonDataFrame);
     assertFalse(jsonDataFrame.getMapping(GLOBAL_REF_LEAF).isOptional());
   }
 
   public JSONObject getJsonSchema() {
     String schemaDefinition = "" +
             "{\"type\":\"object\"," +
               "\"properties\":{" +
                 "\"parent\":{" +
                   "\"type\":\"array\"," +
                   "\"items\":{" +
                     "\"type\":\"object\"," +
                     "\"properties\":{" +
                       "\"child\":{" +
                         "\"type\":\"object\"," +
                         "\"properties\":{" +
                           "\"leaf\":{\"type\":\"string\", \"required\":true}" +
                         "}" +
                       "}" +
                     "}" +
                   "}" +
                 "}" +
               "}" +
             "}";
     JSONObject jsonSchema = JSONParser.parse(schemaDefinition).isObject();
     return jsonSchema;
   }
 
   @Override
   public String getModuleName() {
     return "ee.stacc.transformer.TransformerWidget";
   }
 
 
 }
