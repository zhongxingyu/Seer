 import models.Transaction;
 import org.junit.Before;
 import org.junit.Test;
 import play.mvc.Http.Response;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 import utils.FmtUtil;
 import utils.GsonUtil;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Test case for transaction retrieval routes
  */
 public class RouteRetrieveTest extends FunctionalTest {
 
     /**
      * Do a simple POST with the given values
      *
      * @param url    Internal URL
      * @param params Params
      * @return Response
      */
     private static Response POST(String url, Map<String, String> params) {
         return POST(url, "application/x-www-form-urlencoded",
                 FmtUtil.encode(params));
     }
 
     /**
      * Delete all fixtures and import test fixture before tests run
      */
     @Before
     public void setUp() {
         Fixtures.deleteAll();
         Fixtures.load("fixtures-test.yml");
     }
 
     /**
      * Test route /transactions/all
      */
     @Test
     public void testRouteTransactions() {
         Map<String, String> params = new HashMap<String, String>() {{
            put("username", "test");
         }};
         Response response = POST("/transactions/all", params);
         assertIsOk(response);
         assertContentType("application/json", response);
         assertCharset("utf-8", response);
         final String body = getContent(response);
         assertTrue(body.length() > 0);
         List<Transaction> transactions = GsonUtil.parseTransactions(body);
         assertTrue(!transactions.isEmpty());
     }
 
     /**
      * Test route /transactions/{timestamp}
      */
     @Test
     public void testRouteTransactionsAfter() {
         Map<String, String> params = new HashMap<String, String>() {{
            put("username", "test");
         }};
         Response response = POST("/transactions/0", params);
         assertIsOk(response);
         assertContentType("application/json", response);
         assertCharset("utf-8", response);
         final String body = getContent(response);
         assertTrue(body.length() > 0);
         List<Transaction> transactions = GsonUtil.parseTransactions(body);
         assertTrue(!transactions.isEmpty());
     }
 }
