 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.sling.tests.sling_2998;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.inject.Inject;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.sling.auth.core.impl.LoginServlet;
 import org.apache.sling.auth.core.impl.LogoutServlet;
 import org.apache.sling.jcr.api.SlingRepository;
 import org.apache.sling.launchpad.karaf.testing.KarafTestSupport;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.OptionUtils;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
 import org.ops4j.pax.exam.util.Filter;
 
 import static org.junit.Assert.assertEquals;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 
 @RunWith(PaxExam.class)
 @ExamReactorStrategy(PerMethod.class)
 public class SLING_2998_IT extends KarafTestSupport {
 
     @Inject
     @Filter(timeout = 240000)
     protected SlingRepository slingRepository;
 
    protected String baseUri() throws Exception {
         return "http://localhost:" + httpPort();
     }
 
     @Configuration
     public Option[] configuration() {
         return OptionUtils.combine(baseConfiguration(),
             addBootFeature("sling-launchpad-jackrabbit"),
             addBootFeature("sling-launchpad-content"),
             mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").version("4.3.2"),
             mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").version("4.3.2")
         );
     }
 
     @Test
     public void testRoot() throws Exception {
         final HttpClient httpClient = HttpClients.createDefault();
         final HttpGet httpGet = new HttpGet(baseUri());
         final HttpResponse httpResponse = httpClient.execute(httpGet);
         assertEquals(200, httpResponse.getStatusLine().getStatusCode());
     }
 
     @Test
     public void testLoginServlet() throws Exception {
         final HttpClient httpClient = HttpClients.createDefault();
         final HttpGet httpGet = new HttpGet(baseUri() + LoginServlet.SERVLET_PATH);
         final HttpResponse httpResponse = httpClient.execute(httpGet);
         assertEquals(200, httpResponse.getStatusLine().getStatusCode());
     }
 
     @Test
     public void testLogoutServlet() throws Exception {
         final HttpClient httpClient = HttpClients.createDefault();
         final HttpGet httpGet = new HttpGet(baseUri() + LogoutServlet.SERVLET_PATH);
         final HttpResponse httpResponse = httpClient.execute(httpGet);
         assertEquals(200, httpResponse.getStatusLine().getStatusCode());
     }
 
     @Test
     public void testSlingTest() throws Exception {
         final HttpClient httpClient = HttpClients.createDefault();
         final HttpGet httpGet = new HttpGet(baseUri() + "/sling-test/sling/sling-test.html");
 
         final HttpResponse httpResponse = httpClient.execute(httpGet);
         assertEquals(200, httpResponse.getStatusLine().getStatusCode());
 
         final Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put("sling.auth.requirements", "+/sling-test");
         bundleContext.registerService(Object.class.getName(), new Object(), properties);
 
         final HttpResponse httpResponseUnauthorized = httpClient.execute(httpGet);
         assertEquals(401, httpResponseUnauthorized.getStatusLine().getStatusCode());
 
         final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
         credentialsProvider.setCredentials(
             AuthScope.ANY,
             new UsernamePasswordCredentials("admin", "admin")
         );
 
         final HttpClient authenticatingHttpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
         final HttpResponse httpResponseAuthorized = authenticatingHttpClient.execute(httpGet);
         assertEquals(200, httpResponseAuthorized.getStatusLine().getStatusCode());
     }
 
 }
