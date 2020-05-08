 /*
  * Simplex, lightweight SimPEL server
  * Copyright (C) 2008-2009  Intalio, Inc.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.intalio.simplex.http;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.intalio.simplex.EmbeddedServer;
 import junit.framework.TestCase;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/hello\";\n" +
 
             "process HelloWorld { \n" +
             "   receive(self) { |name| \n" +
             "       helloXml = <hello>{\"Hello \" + name}</hello>; \n" +
             "       reply(helloXml); \n" +
             "   }\n" +
             "}";
 
     public void testRestfulHelloWorld() throws Exception {
         server.start();
         server.deploy(HELLO_WORLD);
 
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
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/counter\";\n" +
 
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
         server.deploy(COUNTER);
 
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
         assertTrue(response.indexOf("<counter>3</counter>") > 0);
 
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
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/feedget\";\n" +
 
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
         server.deploy(CALLING_GET);
 
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
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/gppdproc\";\n" +
 
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
         server.deploy(GET_PUT_POST_DELETE);
 
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
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/post201proc\";\n" +
 
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
         server.deploy(POST_WITH_201);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/post201proc");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertEquals(response, "http://foo/bar");
     }
 
     private static final String HELLO_FORM_WORLD =
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/hello-form\";\n" +
 
             "process HelloFormWorld { \n" +
             "   receive(self) { |form| \n" +
             "       helloXml = <hello>{\"Hello \" + form.firstname + \" \" + form.lastname}</hello>; \n" +
             "       reply(helloXml); \n" +
             "   }\n" +
             "}";
 
     public void testFormHelloWorld() throws Exception {
         server.start();
         server.deploy(HELLO_FORM_WORLD);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/hello-form");
         ClientResponse resp = wr.path("/").type("application/x-www-form-urlencoded")
                 .post(ClientResponse.class, "firstname=foo&lastname=bar");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertTrue(response.indexOf("Hello foo bar") > 0);
         assertTrue(resp.getMetadata().get("Location").get(0), resp.getMetadata().get("Location").get(0).matches(".*/hello-form/[0-9]*"));
         assertTrue(resp.getStatus() == 201);
     }
 
     private static final String SUB_URL_PROCESS =
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/sub/address/hello-form\";\n" +
 
             "process HelloFormWorld { \n" +
             "   receive(self) { |form| \n" +
             "       helloXml = <hello>{\"Hello \" + form.firstname + \" \" + form.lastname}</hello>; \n" +
             "       reply(helloXml); \n" +
             "   }\n" +
             "}";
 
     public void testSubUrlProcess() throws Exception {
         server.start();
         server.deploy(SUB_URL_PROCESS);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/sub/address/hello-form");
         ClientResponse resp = wr.path("/").type("application/x-www-form-urlencoded")
                 .post(ClientResponse.class, "firstname=foo&lastname=bar");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertTrue(response.indexOf("Hello foo bar") > 0);
         assertTrue(resp.getMetadata().get("Location").get(0), resp.getMetadata().get("Location").get(0).matches(".*/hello-form/[0-9]*"));
         assertTrue(resp.getStatus() == 201);
     }
 
     public static final String REQUEST_NO_OUTPUT =
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/noout\";\n" +
 
             "var testRoot = \"http://localhost:3434/post201\"; " +
             "process PostRedirect {\n" +
             "   receive(self) { |query|\n" +
             "       postMsg = <foo>foo</foo>;\n" +
             "       request(testRoot, \"post\", postMsg);\n" +
             "       msg = <ok>ok</ok>;\n" +
             "       reply(msg);\n" +
             "   }\n" +
             "}";
 
     public void testRequestNoOutput() throws Exception {
         server.start();
         server.deploy(REQUEST_NO_OUTPUT);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/noout");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String response = resp.getEntity(String.class);
         System.out.println("=> " + response);
         assertTrue(response.indexOf("ok") > 0);
     }
 
     public static final String PARAMETERIZED_POST =
             "processConfig.inMem = true;\n" +
             "processConfig.address = \"/parampost\";\n" +
 
             "var testRoot = \"http://localhost:3434/post201\"; " +
             "process ParameterPost {\n" +
             "   receive(self) { |begin|\n" +
             "       rep = <ok>ok</ok>;\n" +
             "       reply(rep);\n" +
             "   }\n" +
 
             "   resp = <resp></resp>; \n" +
             "   sub = resource(\"/sub/{name}\"); \n" +
             "   done = resource(\"/done\"); \n" +
             "   scope { \n" +
             "       receive(done) { |msg| \n " +
             "           reply(resp); \n" +
             "       } \n " +
             "   } onReceive(sub) { |cnt,name| \n" +
             "       resp.name = name; \n" +
             "       reply(resp); \n" +
             "   } \n" +
             "}";
 
     public void testParameterizedPost() throws Exception {
         server.start();
         server.deploy(PARAMETERIZED_POST);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/parampost");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String response = resp.getEntity(String.class);
         assertTrue(response.indexOf("ok") > 0);
         assertTrue(resp.getStatus() == 201);
 
         String location = resp.getMetadata().get("Location").get(0);
         WebResource pwr = c.resource(location + "/sub/johndoe");
         ClientResponse presp = pwr.accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String response2 = presp.getEntity(String.class);
         assertTrue(response2.indexOf("<name>johndoe</name>") > 0);
 
         WebResource donewr = c.resource(location + "/done");
         ClientResponse dresp = donewr.accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<foo>foo</foo>");
         String doneResponse = dresp.getEntity(String.class);
         assertTrue(doneResponse.indexOf("<name>johndoe</name>") > 0);
     }
 
     public static final String REQ_ERROR =
             "processConfig.inMem = false;\n" +
             "processConfig.address = \"/reqerror\";\n" +
 
             "process RequestError {\n" +
             "   receive(self) { |query|\n" +
             "       resp = request(\"http://localhost:9999/nowhere\");\n" +
             "       reply(resp);\n" +
             "   }\n" +
             "}";
 
     public void testRequestError() throws Exception {
         server.start();
         server.deploy(REQ_ERROR);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/reqerror");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<empty/>");
         String response = resp.getEntity(String.class);
         // Getting here shows we're fine already
         System.out.println("=> " + response);
     }
 
     public static final String EMPTY_REPLY =
             "processConfig.inMem = false;\n" +
             "processConfig.address = \"/emptyreply\";\n" +
 
             "process RequestError {\n" +
             "   receive(self) { |query|\n" +
             "       reply();\n" +
             "   }\n" +
             "}";
 
     public void testEmptyReply() throws Exception {
         server.start();
         server.deploy(EMPTY_REPLY);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/emptyreply");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<empty/>");
         assertTrue(resp.getStatus() == 201);
     }
 
     public static final String IMPLICIT_REPLY =
             "processConfig.inMem = false;\n" +
             "processConfig.address = \"/implicitreply\";\n" +
 
             "process RequestError {\n" +
             "   receive(self) { |query|\n" +
            "       foo = 1;\n" +
             "   }\n" +
             "}";
 
     public void testImplicitReply() throws Exception {
         server.start();
         server.deploy(IMPLICIT_REPLY);
 
         ClientConfig cc = new DefaultClientConfig();
         Client c = Client.create(cc);
 
         WebResource wr = c.resource("http://localhost:3434/implicitreply");
         ClientResponse resp = wr.path("/").accept("application/xml").type("application/xml")
                 .post(ClientResponse.class, "<empty/>");
         assertTrue(resp.getStatus() == 201);
     }
 
 }
