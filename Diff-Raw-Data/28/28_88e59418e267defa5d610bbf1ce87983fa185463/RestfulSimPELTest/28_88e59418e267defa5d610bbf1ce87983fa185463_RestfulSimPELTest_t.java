 package org.apache.ode.simpel;
 
 import org.apache.ode.EmbeddedServer;
 import org.apache.ode.Descriptor;
 import junit.framework.TestCase;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.ClientResponse;
 
 import javax.ws.rs.core.Response;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class RestfulSimPELTest extends TestCase {
 
     EmbeddedServer server;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         server = new EmbeddedServer();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
         server.stop();
     }
 
     private static final String HELLO_WORLD =
             "process HelloWorld { \n" +
             "   receive(self) { |name| \n" +
             "       helloXml = <hello>{\"Hello \" + name}</hello>; \n" +
             "       reply(helloXml); \n" +
             "   }\n" +
             "}";
 
     public void testRestfulHelloWorld() throws Exception {
         server.start();
         Descriptor desc = new Descriptor();
         desc.setAddress("/hello");
         server.deploy(HELLO_WORLD, desc);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/hello");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<wrapper>foo</wrapper>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertTrue(response.indexOf("Hello foo") > 0);
         assertTrue(resp.getMetadata().get("Location").get(0), resp.getMetadata().get("Location").get(0).matches(".*/hello/[0-9]*"));
         assertTrue(resp.getStatus() == 201);
     }
 
     private static final String COUNTER =
             "process Counter {\n" +
             "   counter = receive(self); \n" +
             "   reply(counter, self); \n" +
 
             "   value = resource(\"/value\"); \n" +
             "   inc = resource(\"/inc\"); \n" +
             "   dec = resource(\"/dec\"); \n" +
            "   counter = parseInt(counter); \n" +
             "   scope { \n" +
             "       while(counter>0) { \n" +
             "           wait(\"PT1S\"); \n" + // TODO support time as well as duration
             "       } \n" +
             "   } onQuery(self) {\n" +
             "       links = <counter></counter>; \n" +
             "       links.increment = inc; \n" +
             "       links.decrement = dec; \n" +
             "       links.value = value; \n" +
             "       reply(links); \n" +
            "   } onQuery(value) { \n" +
             "       reply(counter); \n" +
            "   } onReceive(dec) { \n" +
             "       counter = counter - 1; \n" +
             "       reply(counter); \n" +
            "   } onReceive(inc) { \n" +
            "       counter = counter + 1; \n" + // TODO fix the - - hack
             "       reply(counter); \n" +
             "   } \n" +
             "}";
 
     public void testCounter() throws Exception {
         server.start();
        Descriptor desc = new Descriptor(); // TODO remove the descriptor to use environment-based configuration
         desc.setAddress("/counter");
         server.deploy(COUNTER, desc);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         // Starting the counter process
         WebResource wr = c.resource("http://localhost:3434/counter"); // TODO default on process name
         ClientResponse createResponse = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<counter>3</counter>");
         String response = createResponse.getEntity(String.class);
         String location = createResponse.getMetadata().get("Location").get(0);
         assertTrue(createResponse.getStatus() == 201);
         assertTrue(location.matches(".*/counter/[0-9]*$"));
         assertEquals(response, "3");
 
         // Requesting links
         WebResource instance = c.resource(location);
         ClientResponse queryResponse = instance.path("/").type("application/xml").get(ClientResponse.class);
         response = queryResponse.getEntity(String.class);
 
         Matcher m = Pattern.compile("/counter/[0-9]*/value").matcher(response);
         assertTrue(m.find());
         m = Pattern.compile("/counter/[0-9]*/dec").matcher(response);
         assertTrue(m.find());
         assertTrue(queryResponse.getStatus() == 200);
 
         // Requesting counter value to check the initial is correct
         ClientResponse valueResponse = instance.path("/value").type("application/xml").get(ClientResponse.class);
         response = valueResponse.getEntity(String.class);
         assertTrue(valueResponse.getStatus() == 200);
         assertTrue(response.indexOf("3") >= 0);
 
         // Incrementing twice
         ClientResponse incResponse;
         for (int n = 0; n < 2; n++) {
             incResponse = instance.path("/inc").type("application/xml").post(ClientResponse.class);
             response = incResponse.getEntity(String.class);
             assertTrue(incResponse.getStatus() == 200);
             System.out.println("=> " + response);
             assertTrue(response.indexOf(""+(4+n)) >= 0);
         }
 
         // Checking value again, should be 5 now
         valueResponse = instance.path("/value").type("application/xml").get(ClientResponse.class);
         response = valueResponse.getEntity(String.class);
         assertTrue(valueResponse.getStatus() == 200);
         assertTrue(response.indexOf("5") >= 0);
 
         // Decrementing counter to 0 to let process complete
         ClientResponse decResponse;
         for (int n = 0; n < 5; n++) {
             decResponse = instance.path("/dec").type("application/xml").post(ClientResponse.class);
             response = decResponse.getEntity(String.class);
             assertTrue(valueResponse.getStatus() == 200);
             assertTrue(response.indexOf(""+(4-n)) >= 0);
         }
 
         // The process shouldn't be here anymore
         Thread.sleep(1500);
         queryResponse = instance.path("/").type("application/xml").get(ClientResponse.class);
         assertTrue(queryResponse.getStatus() == 410);
     }
     
     public static final String CALLING_GET =
             "var feedBUrl = \"http://feeds.feedburner.com/\"; " +
             "process CallingGet {\n" +
             "   receive(self) { |query|\n" +
             "       feed = request(feedBUrl + query);\n" +
             "       title = feed.channel.title;\n" +
             "       reply(title);\n" +
             "   }\n" +
             "}";
 
     public void testCallingGet() throws Exception {
         server.start();
         Descriptor desc = new Descriptor();
         desc.setAddress("/feedget");
         server.deploy(CALLING_GET, desc);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/feedget");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<name>OffTheLip</name>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertEquals(response, "Off The Lip");
     }
 
     public static final String GET_PUT_POST_DELETE =
             "var testRoot = \"http://localhost:3434/gppd\"; " +
             "process AllMethods {\n" +
             "   receive(self) { |query|\n" +
             "       getRes = request(testRoot);\n" +
             "       res = getRes.text();\n" +
 
             "       postMsg = <foo>foo</foo>;\n" +
             "       postRes = request(testRoot, \"post\", postMsg);\n" +
             "       res = res + postRes.text();\n" +
 
             "       putMsg = <bar>bar</bar>;\n" +
             "       putRes = request(testRoot, \"put\", putMsg);\n" +
             "       res = res + putRes.text();\n" +
 
             "       request(testRoot, \"delete\");\n" +
             "       reply(res);\n" +
             "   }\n" +
             "}";
 
     public void testAllMethods() throws Exception {
         server.start();
         Descriptor desc = new Descriptor();
         desc.setAddress("/gppdproc");
         server.deploy(GET_PUT_POST_DELETE, desc);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/gppdproc");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<simpelWrapper xmlns=\"http://ode.apache.org/simpel/1.0/definition/AllMethods\">foo</simpelWrapper>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertEquals("GETPOSTfooPUTbar", response);
     }
 
     public static final String POST_WITH_201 =
             "var testRoot = \"http://localhost:3434/post201\"; " +
             "process PostRedirect {\n" +
             "   receive(self) { |query|\n" +
             "       postMsg = <foo>foo</foo>;\n" +
             "       postRes = request(testRoot, \"post\", postMsg);\n" +
             "       if(postRes.headers.Status == \"201\") { \n" +
             "           msg = postRes.headers.Location;\n" +
             "           reply(msg);\n" +
             "       } else {\n" +
             "           msg = <fail>fail</fail>;\n" +
             "           reply(msg);\n" +
             "       }\n" +
             "   }\n" +
             "}";
 
     public void testPostWith201() throws Exception {
         server.start();
         Descriptor desc = new Descriptor();
         desc.setAddress("/post201proc");
         server.deploy(POST_WITH_201, desc);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/post201proc");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertEquals(response, "http://foo/bar");
     }
 
 }
