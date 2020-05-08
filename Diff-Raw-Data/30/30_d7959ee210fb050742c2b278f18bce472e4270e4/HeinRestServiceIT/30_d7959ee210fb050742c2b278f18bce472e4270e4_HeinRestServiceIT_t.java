 package net.binout.codestory2013;
 
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientFactory;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.ext.RuntimeDelegate;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 
 public class HeinRestServiceIT {
 
     private static HttpServer server;
     private static URI uri;
     private static Client client;
 
     @BeforeClass 
     public static void init() throws IOException {
         System.setProperty(HeinRestService.SCALASKEL_NO_CACHE_LOADING_PROPERTY, "true");
 
         uri = UriBuilder.fromUri("http://localhost/").port(8282).build();
 
         client = ClientFactory.newClient();
 
         server = HttpServer.create(new InetSocketAddress(uri.getPort()), 0);
         HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(new HeinRestApplication(), HttpHandler.class);
         server.createContext(uri.getPath(), handler);
 
         server.start();
     }
 
     @AfterClass
     public static void stop() {
         server.stop(0);
     }
 
     private String transformQuery(String query) {
         return query.replaceAll(" ", "+");
     }
 
    @Test
    public void get_unknown_request_should_return_response_with_bad_request_status() {
        Response response = client.target(uri).request().get(Response.class);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(HeinRestService.HEIN_BINOUT, response.readEntity(String.class));
    }

     @DataProvider
     public Object[][] query_response_provider() {
         return new Object[][] {
                 new String[] {HeinRestService.QUELLE_EST_TON_ADRESSE_EMAIL, HeinRestService.MAIL},
                 new String[] {HeinRestService.ES_TU_ABONNE_A_LA_MAILING_LIST_OUI_NON, HeinRestService.OUI},
                 new String[] {HeinRestService.ES_TU_HEUREUX_DE_PARTICIPER_OUI_NON, HeinRestService.OUI},
                 new String[] {HeinRestService.ES_TU_PRET_A_RECEVOIR_UNE_ENONCE_AU_FORMAT_MARKDOWN_PAR_HTTP_POST_OUI_NON, HeinRestService.OUI},
                 new String[] {HeinRestService.EST_CE_QUE_TU_REPONDS_TOUJOURS_OUI_OUI_NON, HeinRestService.NON},
                 new String[] {HeinRestService.AS_TU_BIEN_RECU_LE_PREMIER_ENONCE_OUI_NON, HeinRestService.OUI},
                 new String[] {"1 1", "2"},
                 new String[] {"(1 2)/2", "1,5"},
         };
     }
 
     @Test(dataProvider = "query_response_provider")
     public void get_should_return_response(String query, String expected) {
         String mailQuery = uri + "?" + HeinRestService.Q + "=" + transformQuery(query);
         String response = client.target(mailQuery).request().get(String.class);
         Assert.assertEquals(expected, response);
     }
 
     @Test(dataProvider = "query_response_provider")
     public void get_should_return_response_with_ok_status(String query, String expected) {
         String mailQuery = uri + "?" + HeinRestService.Q + "=" + transformQuery(query);
         Response response = client.target(mailQuery).request().get(Response.class);
         Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         Assert.assertEquals(expected, response.readEntity(String.class));
     }
 
     @Test
     public void get_should_return_bad_equest_for_unkwon_question() {
         String query = "quel est ton film prefere";
         String mailQuery = uri + "?" + HeinRestService.Q + "=" + transformQuery(query);
         Response response = client.target(mailQuery).request().get(Response.class);
         Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
     }
 
     @Test
     public void post_enonce_should_response_created_status() {
         String postEnonceQuery = uri + "enonce/1";
         Response response = client.target(postEnonceQuery).request().post(Entity.text("enonce"));
         Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
     }
 
     @DataProvider
     public Object[][] query_response_scalaskel() {
         return new Object[][] {
                 new String[] {"1", "[{\"foo\":1}]"},
                 new String[] {"7", "[{\"foo\":7},{\"bar\":1}]"},
         };
     }
 
     @Test(dataProvider = "query_response_scalaskel")
     public void get_scalaskel_should_return_json_result(String number, String expected) {
         String scalaskelQuery = uri + "scalaskel/change/" + number;
         Response response = client.target(scalaskelQuery).request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
         Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
         Assert.assertEquals(response.readEntity(String.class), expected);
     }
 
     @Test
     public void get_scalaskel_0_should_return_bad_request() {
         String scalaskelQuery = uri + "scalaskel/change/0";
         Response response = client.target(scalaskelQuery).request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
         Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
     }
 
     @Test
     public void get_scalaskel_101_should_return_bad_request() {
         String scalaskelQuery = uri + "scalaskel/change/101";
         Response response = client.target(scalaskelQuery).request(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
         Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
     }
 }
