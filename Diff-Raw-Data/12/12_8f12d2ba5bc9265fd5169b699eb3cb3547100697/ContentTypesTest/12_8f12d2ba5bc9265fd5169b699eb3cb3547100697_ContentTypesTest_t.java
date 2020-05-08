 package jewas.http;
 
 import com.jayway.restassured.RestAssured;
 import jewas.routes.StaticResourcesRoute;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import static com.jayway.restassured.RestAssured.expect;
 
 /**
  * @author fcamblor
  */
 public class ContentTypesTest {
 
     private static final int SERVER_PORT = 28086;
 
     private RestServer restServer = null;
     @Rule
     public TemporaryFolder testFolder = new TemporaryFolder();
 
     public ContentTypesTest() {
     }
 
     @Before
     public void startServer() {
        System.setProperty("deploy.target.env", "test");

         // Restserver without any route
         restServer = RestServerFactory.createRestServer(SERVER_PORT);
         restServer.addRoutes(
                 new StaticResourcesRoute("/public/", "jewas/http/staticResources/", testFolder.newFolder("resources"))
         );
         restServer.start();
         RestAssured.port = SERVER_PORT;
     }
 
     @After
     public void stopServer() {
         restServer.stop();
         restServer = null;
     }
 
     @Test
    public void shouldJsUriBeOfJavascriptContentType() {
         expect().
                contentType("application/javascript").
                when().
                get("/public/test.js");
     }
 }
