 package ixcode.platform.json;
 
 import org.junit.*;
 
 import static ixcode.platform.json.JsonBoolean.jsonBoolean;
 import static ixcode.platform.json.JsonNull.jsonNull;
 import static ixcode.platform.json.JsonNumber.jsonNumber;
import static ixcode.platform.json.JsonObject.emptyJsonObject;
 import static ixcode.platform.json.JsonString.jsonString;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.nullValue;
 
 public class JsonParserTest {
 
     private final JsonParser jsonParser = new JsonParser();
 
     @Test
     public void object_containing_an_array() {
         String json = "{ \"anArray\" : [ { \"child\" : \"foo\" } ] }";
 
         JsonObject rootObject = jsonParser.parse(json);
 
         JsonArray jsonArray = rootObject.valueOf("anArray");
 
         assertThat(jsonArray.size(), is(1));
 
         JsonObject childObject = jsonArray.valueAt(0);
 
         assertThat(childObject.<JsonString>valueOf("child"), is(jsonString("foo")));
     }
 
     @Test
     public void array_of_objects() {
         String json = "[ { \"child\" : \"foo\" } ]";
 
         JsonArray jsonArray = jsonParser.parse(json);
 
         assertThat(jsonArray.size(), is(1));
 
         JsonObject childObject = jsonArray.valueAt(0);
 
         assertThat(childObject.<JsonString>valueOf("child"), is(jsonString("foo")));
     }
 
     @Test
     public void number_value() {
         String json = "{ \"someObject\" : 345 }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<JsonNumber>valueOf("someObject"), is(jsonNumber(345)));
     }
 
     @Test
     public void true_value() {
         String json = "{ \"someObject\" : true }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<JsonBoolean>valueOf("someObject"), is(jsonBoolean(true)));
     }
 
     @Test
     public void false_value() {
         String json = "{ \"someObject\" : false }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<JsonBoolean>valueOf("someObject"), is(jsonBoolean(false)));
     }
 
     @Test
     public void null_value() {
         String json = "{ \"someObject\" : null }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<JsonNull>valueOf("someObject"), is(jsonNull()));
     }
 
     @Test
     public void empty_object_value() {
         String json = "{ \"someObject\" : {} }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
        assertThat(jsonObject.<JsonObject>valueOf("someObject"), is(emptyJsonObject()));
     }
 }
