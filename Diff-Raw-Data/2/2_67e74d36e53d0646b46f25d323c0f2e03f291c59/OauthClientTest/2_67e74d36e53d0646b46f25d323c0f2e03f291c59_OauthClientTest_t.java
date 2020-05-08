 /*
  * Copyright 2013 SURFnet bv, The Netherlands
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.surfnet.coin.shared.oauth;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.localserver.LocalTestServer;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.HttpServerErrorException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class OauthClientTest {
 
   public static final String ACCCESS_TOKEN_VALUE = "some-accesstoken";
 
   private final OauthClient oauthClient = new OauthClient() {
     @Override
     protected String getAccessToken() {
       return ACCCESS_TOKEN_VALUE;
     }
   };
   private LocalTestServer server;
 
   @Before
   public void setup() throws Exception {
     server = new LocalTestServer(null, null);
     server.start();
   }
 
   @Test
   public void requestContainsToken() {
     server.register("/foobar", new HttpRequestHandler() {
       @Override
       public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
         assertTrue(request.getHeaders("Authorization")[0].getValue().equals("bearer " + ACCCESS_TOKEN_VALUE));
         response.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));
         response.setStatusCode(200);
       }
 
     });
 
     oauthClient.exchange(urlBase() + "/foobar", null, null, String.class);
   }
 
   @Test
   public void retry() {
 
     final int[] timesCalled = {0};
     OauthClient oauthClient1 = new OauthClient() {
       @Override
       protected String getAccessToken() {
         timesCalled[0]++;
         return ACCCESS_TOKEN_VALUE;
       }
     };
 
     server.register("/foobar2", new HttpRequestHandler() {
       @Override
       public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
         if (timesCalled[0] <= 1) {
           response.setEntity(new StringEntity("Invalid access token ", ContentType.APPLICATION_JSON));
           response.setStatusCode(403);
         } else {
           response.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));
           response.setStatusCode(200);
         }
       }
 
     });
 
     oauthClient1.doExchange(urlBase() + "/foobar2", null, null, String.class, true);
     assertEquals("getAccessToken() should be called again after getting a 'Forbidden' response", 2, timesCalled[0]);
   }
 
   @Test(expected = HttpClientErrorException.class)
   public void clientErrorsArePassedThrough() {
     server.register("/foobar3", new HttpRequestHandler() {
       @Override
       public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
         response.setEntity(new StringEntity("Some bad request", ContentType.APPLICATION_JSON));
         response.setStatusCode(400);
       }
 
     });
     oauthClient.doExchange(urlBase() + "/foobar3", null, null, String.class, true);
   }
 
   @Test(expected = HttpServerErrorException.class)
   public void serverErrorsArePassedThrough() {
     server.register("/foobar4", new HttpRequestHandler() {
       @Override
       public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
         response.setEntity(new StringEntity("Some internal server error", ContentType.APPLICATION_JSON));
         response.setStatusCode(500);
       }
 
     });
     oauthClient.doExchange(urlBase() + "/foobar4", null, null, String.class, true);
   }
 
   private String urlBase() {
    return "http://" + server.getServiceAddress().getHostName() + ":" + server.getServiceAddress().getPort();
   }
 }
