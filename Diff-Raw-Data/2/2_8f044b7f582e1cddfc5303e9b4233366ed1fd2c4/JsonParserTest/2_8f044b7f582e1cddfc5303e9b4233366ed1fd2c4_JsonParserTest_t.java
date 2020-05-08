 package ixcode.platform.json;
 
 import org.junit.*;
 
 import static ixcode.platform.json.JsonObject.*;
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 public class JsonParserTest {
 
     private final JsonParser jsonParser = new JsonParser();
 
     @Test
     public void object_containing_an_array() {
         String json = "{ \"anArray\" : [ { \"child\" : \"foo\" } ] }";
 
         JsonObject rootObject = jsonParser.parse(json);
 
         JsonArray jsonArray = rootObject.valueOf("anArray");
 
         assertThat(jsonArray.size(), is(1));
 
         JsonObject childObject = jsonArray.get(0);
 
         assertThat(childObject.<String>valueOf("child"), is("foo"));
     }
 
     @Test
     public void array_of_objects() {
         String json = "[ { \"child\" : \"foo\" } ]";
 
         JsonArray jsonArray = jsonParser.parse(json);
 
         assertThat(jsonArray.size(), is(1));
 
         JsonObject childObject = jsonArray.get(0);
 
         assertThat(childObject.<String>valueOf("child"), is("foo"));
     }
 
     @Test
     public void integer_value() {
         String json = "{ \"someObject\" : 345 }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<Integer>valueOf("someObject"), is(345));
     }
 
     @Test
     public void double_value() {
         String json = "{ \"someObject\" : 4356.455 }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<Double>valueOf("someObject"), is(4356.455));
     }
 
     @Test
     public void true_value() {
         String json = "{ \"someObject\" : true }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<Boolean>valueOf("someObject"), is(true));
     }
 
     @Test
     public void false_value() {
         String json = "{ \"someObject\" : false }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<Boolean>valueOf("someObject"), is(false));
     }
 
     @Test
     public void null_value() {
         String json = "{ \"someObject\" : null }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.valueOf("someObject"), is(nullValue()));
     }
 
     @Test
     public void empty_object_value() {
         String json = "{ \"someObject\" : {} }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<JsonObject>valueOf("someObject"), is(emptyJsonObject()));
     }
 
     @Test
     public void value_with_uri() {
        String json = "{ \"someUri\" : \"http://foo.bar.com\" }";
 
         JsonObject jsonObject = jsonParser.parse(json);
 
         assertThat(jsonObject.<String>valueOf("someUri"), is("http://foo.bar.com"));
     }
 }
