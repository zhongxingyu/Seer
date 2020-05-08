 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.agent.webapp;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
 
 import junit.framework.TestCase;
 
 import org.fusesource.cloudmix.agent.common.EndpointRefBuilder;
 import org.fusesource.cloudmix.common.jetty.WebServer;
 
 /**
  * Test the webapp agent in a deployed state. 
  */
 public class WebappTest extends TestCase {
     
     private static final String BASE_URI = "http://localhost:9091/service-grid/agent";
     private static final String BASE_URN = "urn:%7Bhttp:%2F%2Fcxf.apache.org%7D";
 
     private static final String[] EXPECTED_STATUS_REGEXP = 
     {"^<html><head>$",
         "^<link href=\"css/main.css\" rel=\"stylesheet\" type=\"text/css\">$",
         "^<title>Grid Agent</title>$",
         "^</head>$",
         "^<body><img src=\"images/logo.gif\"/>$",
         "^<h1>Grid Agent</h1>$",
         "^<h2>Properties</h2>$",
         "^<table>$",
         "^<tr><td><b>Agent Profile</b></td><td><i>$",
         "^default</i></td></tr>$",
         "^<tr><td><b>Agent Host</b></td><td><i>$",
         "^.*</i></td></tr>$",
         "^<tr><td><b>Agent OS</b></td><td><i>$",
         "^(Linux|Windows.*|Mac OS.*|Solaris|SunOS|HP-UX|AIX)</i></td></tr>$",
         "^<tr><td><b>Agent PID</b></td><td><i>$",
         "^[0-9]+</i></td></tr>$",
         "^<tr><td><b>Agent Link</b></td><td><i>$",
         "^null</i></td></tr>$",
         "^<tr><td><b>Agent Container</b></td><td><i>$",
         "^tomcat6</i></td></tr>$",
         "^<tr><td><b>Package types</b></td><td><i>$",
         "^war </i></td></tr>$",
         "^<tr><td><b>Install Directory</b></td><td><i>./webapps</i></td></tr>$",
         "^<tr><td><b>Temp Suffix</b></td><td><i>.tmp</i></td></tr>$",
         "^<tr><td><b>Max Features</b></td><td><i>25</i></td></tr>$",
         "^<tr><td><b>Repository URI</b></td><td><i>http://localhost:9091/controller/</i></td></tr>$",
         "^<tr><td><b>Polling Period</b></td><td><i>1000</i></td></tr>$",
         "^<tr><td><b>Initial Polling Delay</b></td><td><i>1000</i></td></tr>$",
         "^</table>$",
         "^<h2>Features</h2><i>No features installed</i>$",
         "^<hr noshade><i>jetty-6.[0-9].[0-9]</i></hr>$",
         "^</body>$",
         "^</html>$"};
 
     protected WebServer webServer = new WebServer();
 
     @Override
     protected void setUp() throws Exception {
        System.setProperty("catalina.home", ".");
         super.setUp();
         webServer.setWebAppContext("/service-grid");
         webServer.start();
     }
 
     @Override
     protected void tearDown() throws Exception {
         webServer.stop();
     }
 
     public void testGetStatus() throws Exception {
         String uri = BASE_URI + "/status";
         HttpURLConnection httpConnection = getHttpConnection(uri);
         httpConnection.connect();
 
         verifyResponseCode(200, httpConnection);
         assertEquals("text/html", httpConnection.getContentType());
         assertEquals("OK", httpConnection.getResponseMessage());
 
         InputStream in = httpConnection.getInputStream();
         assertNotNull(in);
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         String line = null;
         int i = 0;
         while ((line = reader.readLine()) != null) {
             Pattern pattern = Pattern.compile(EXPECTED_STATUS_REGEXP[i]);
             Matcher matcher = pattern.matcher(line);
             assertTrue(line + " doesn't match: " + EXPECTED_STATUS_REGEXP[i],
                        matcher.matches());
             i++;
         }
     }
 
     public void testAddEndpoint() throws Exception {
         doTestAddEndpoint(BASE_URN + "SoapPort", "http://tempuri.org/foo/bar");
         doTestAddEndpoint(BASE_URN + "SoapPort", "http://tempuri.org/sna/fu");
         doTestAddEndpoint("urn:Bank::Account%2F12345",
                           "corbaname:rir/NameService#account_12345");
         doTestAddEndpoint(BASE_URN + "SamePort", "http://tempuri.org/sna/fu");
         doTestAddEndpoint(BASE_URN + "SamePort", "http://tempuri.org/sna/fu");
     }
 
     public void doTestAddEndpoint(String encodedId, String address)
         throws Exception {
         String uri = BASE_URI + "/endpoint/" + encodedId;
         HttpURLConnection httpConnection = getHttpConnection(uri, true);
         httpConnection.setRequestMethod("PUT");
         httpConnection.connect();
 
         OutputStream os = httpConnection.getOutputStream();
         W3CEndpointReference ref = 
             EndpointRefBuilder.create(address);
         EndpointRefBuilder.marshal(ref, os);
         os.close();
 
         verifyResponseCode(201, httpConnection);
         assertEquals("Created", httpConnection.getResponseMessage());
     }
 
     public void testRemoveEndpoint() throws Exception {
         String encodedId = BASE_URN + "SoapPort";
         // add endpoint first before removing it
         doTestAddEndpoint(encodedId + "A", "http://tempuri.org/foo/bar");
         doTestAddEndpoint(encodedId + "B", "http://tempuri.org/sna/fu");
         // TODO is this right?
         //doTestRemoveEndpoint(encodedId + "A", 204, "No Content");
         doTestRemoveEndpoint(encodedId + "A", 200, "OK");
         doTestRemoveEndpoint(encodedId + "A", 404, "Not Found");
         // TODO is this right?
         //doTestRemoveEndpoint(encodedId + "B", 204, "No Content");
         doTestRemoveEndpoint(encodedId + "B", 200, "OK");
     }
 
     public void doTestRemoveEndpoint(String encodedId,
                                      int expectedCode,
                                      String expectedMessage) throws Exception {
         String uri = BASE_URI + "/endpoint/" + encodedId;
 
         HttpURLConnection httpConnection = getHttpConnection(uri);
         httpConnection.setRequestMethod("DELETE");
         httpConnection.connect();
 
         verifyResponseCode(expectedCode, httpConnection);
         assertEquals(expectedMessage, httpConnection.getResponseMessage());
     }
     
     protected HttpURLConnection getHttpConnection(String target)
         throws Exception {
         return getHttpConnection(target, false);
     }
 
     protected HttpURLConnection getHttpConnection(String target,
                                                   boolean doOutput)
         throws Exception {
         URL url = new URL(target);
         URLConnection connection = url.openConnection();
         connection.setDoOutput(doOutput);
         assertTrue(connection instanceof HttpURLConnection);
         return (HttpURLConnection)connection;
     }
 
     protected void verifyResponseCode(int expectedCode, 
                                       HttpURLConnection httpConnection) 
         throws Exception {
         int responseCode = httpConnection.getResponseCode();
         if (responseCode != expectedCode) {
             try {
                 if (responseCode == 500) {
                     System.out.println("\nError response:");
                     InputStream err = httpConnection.getErrorStream();
                     if (err != null) {
                         int c = 0;
                         while ((c = err.read()) != -1) {
                             System.out.print((char)c);
                         } 
                         System.out.println();
                     }
                 }
             } catch (Throwable t) {
                 t.printStackTrace();
             } finally {
                 assertEquals(expectedCode, responseCode);
             }
         }
     }
 }
