 import org.junit.Test;
 import play.mvc.Http.Response;
 import play.test.FunctionalTest;
 
 public class RouteTest extends FunctionalTest {
 
     @Test
     public void testRouteAvg() {
         Response response = GET("/t/avg");
         assertIsOk(response);
         assertContentType("application/json", response);
         assertCharset("utf-8", response);
         assertTrue(getContent(response).length() > 0);
     }
 
     @Test
     public void testRouteTags() {
        Response response = GET("/t/tags/1");
         assertIsOk(response);
         assertContentType("application/json", response);
         assertCharset("utf-8", response);
         assertTrue(getContent(response).length() > 0);
     }
 
     @Test
     public void testRouteTransactions() {
         Response response = GET("/t/transactions/1");
         assertIsOk(response);
         assertContentType("application/json", response);
         assertCharset("utf-8", response);
         assertTrue(getContent(response).length() > 0);
     }
 }
