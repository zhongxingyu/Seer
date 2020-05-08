 package ixcode.platform.http.protocol.request;
 
 import org.apache.log4j.*;
 import org.junit.*;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 public class RequestParameterSerialiserTest {
     private static final Logger log = Logger.getLogger(RequestParameterSerialiserTest.class);
 
     @Test
     public void formats_nice_json() {
        RequestParameters parameters = new RequestParameters().withQueryParameter("foobar", "Jim", "John", "bar")
                                                              .withQueryParameter("blob", "mcblag");
 
         String json = new RequestParameterSerialiser(parameters).toJson();
 
         log.debug("\n" + json);
 
         assertThat(json, containsString("\"foobar\""));
         assertThat(json, containsString("\"Jim\""));
         assertThat(json, containsString("\"John\""));
         assertThat(json, containsString("\"bar\""));
         assertThat(json, containsString("\"blob\""));
         assertThat(json, containsString("\"mcblag\""));
 
     }
 }
