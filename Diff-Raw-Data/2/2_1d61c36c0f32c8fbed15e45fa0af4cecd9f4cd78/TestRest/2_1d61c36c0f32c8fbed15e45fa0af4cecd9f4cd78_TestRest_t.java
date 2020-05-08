 package be.kdg.groeph.service;
 
 
 
 import com.sun.jersey.api.client.Client;
 
 import com.sun.jersey.api.client.WebResource;
 
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 import org.junit.Test;
 
 
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import java.net.URI;
 
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Frederik
  * Date: 6/02/13
  * Time: 11:09
  * To change this template use File | Settings | File Templates.
  */
 public class TestRest {
 
     private final String password = "def";
     private final String username = "test@test.com";
     private final String passwordFalse = "ddaeraeef";
     private final String usernameFalse = "fdqsfdsqfd@test.com";
     private String baseURI;
 
 
     @Test
     public void loginTrue() {
         RestService restService = new RestService();
         String isValidUser = restService.login(username, password);
         assertEquals("Login moet test zijn", username, isValidUser);
 
     }
 
     @Test
     public void loginFalse() {
         RestService restService = new RestService();
 
         String isValidUser = restService.login(usernameFalse, passwordFalse);
         assertEquals("Login moet test zijn", usernameFalse, isValidUser);
     }
 
     @Test
     public void loginRest() {
 
 
         Client client = Client.create(new DefaultClientConfig());
         WebResource service = client.resource(getBaseURI());
 
         String response = service.path("rest").path("login").queryParam("Username","test").queryParam("Password","test").accept(MediaType.APPLICATION_JSON).get(String.class);
 
         assertEquals("result van RestCall moet test zijn", "test", response.toString());
 
 
     }
 
     public static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/groepH_war_exploded/api").build();
     }
 }
