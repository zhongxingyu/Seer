 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server;
 
 
 import com.janrain.backplane.common.BackplaneServerException;
 import com.janrain.backplane.config.BackplaneConfig;
 import com.janrain.backplane.dao.DaoException;
 import com.janrain.backplane.server2.dao.BP2DAOs;
import com.janrain.backplane.server2.model.Backplane2Message;
import com.janrain.backplane.server2.model.Backplane2MessageFields;
import com.janrain.backplane.server2.model.BusConfig2;
import com.janrain.backplane.server2.model.BusConfig2Fields;
 import com.janrain.backplane.server2.oauth2.model.*;
 import com.janrain.oauth2.*;
 import com.janrain.servlet.InvalidRequestException;
 import com.janrain.util.RandomUtils;
 import org.apache.catalina.util.Base64;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.servlet.HandlerAdapter;
 import org.springframework.web.servlet.ModelAndView;
import scala.Option;
 import scala.collection.JavaConversions;
 
 import javax.inject.Inject;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.janrain.oauth2.OAuth2.*;
 import static org.junit.Assert.*;
 
 /**
  * @author Tom Raney
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/app-config.xml", "classpath:/spring/mvc-config.xml" })
 public class Backplane2ControllerTest {
 
     @Inject
 	private ApplicationContext applicationContext;
 
     @Inject
 	private Backplane2Controller controller;
 
     @Inject
     private BackplaneConfig bpConfig;
 
     private static final Logger logger = Logger.getLogger(Backplane2ControllerTest.class);
 
     private static final int TOKEN_EXPIRES_SECONDS = 600;
     private static final int DEFAULT_MESSAGE_RETENTION_SECONDS = 60;
     private static final int MAX_MESSAGE_RETENTION_SECONDS = 300;
 
     ArrayList<String> createdMessageKeys = new ArrayList<String>();
     ArrayList<String> createdTokenKeys = new ArrayList<String>();
     ArrayList<String> createdGrantsKeys = new ArrayList<String>();
 
     static final String OK_RESPONSE = "{\"stat\":\"ok\"}";
     static final String ERR_RESPONSE = "\"error\":";
 
     static final String TEST_MSG_1 =
             "    {\n" +
             "        \"bus\": \"mybus.com\",\n" +
             "        \"channel\": \"testchannel\",\n" +
             "        \"type\": \"bla_type\",\n" +
             "        \"sticky\": \"false\",\n" +
             "        \"payload\":{\n" +
             "            \"identities\":{\n" +
             "               \"startIndex\":0,\n" +
             "               \"itemsPerPage\":1,\n" +
             "               \"totalResults\":1,\n" +
             "               \"entry\":{\n" +
             "                  \"displayName\":\"inewton\",\n" +
             "                  \"accounts\":[\n" +
             "                     {\n" +
             "                        \"username\":\"inewton\",\n" +
             "                        \"openid\":\"https://www.google.com/profiles/105119525695492353427\"\n" +
             "                     }\n" +
             "                  ],\n" +
             "                  \"id\":\"1\"\n" +
             "               }\n" +
             "            },\n" +
             "            \"context\":\"http://backplane1-2.janraindemo.com/token.html\"\n" +
             "         }" +
             "    }";
 
         static final String TEST_MSG_2 =
             "    {\n" +
             "        \"bus\": \"yourbus.com\",\n" +
             "        \"channel\": \"testchannel\",\n" +
             "        \"type\": \"bla_type\",\n" +
             "        \"sticky\": \"false\",\n" +
             "        \"payload\":{\n" +
             "            \"identities\":{\n" +
             "               \"startIndex\":0,\n" +
             "               \"itemsPerPage\":1,\n" +
             "               \"totalResults\":1,\n" +
             "               \"entry\":{\n" +
             "                  \"displayName\":\"inewton\",\n" +
             "                  \"accounts\":[\n" +
             "                     {\n" +
             "                        \"username\":\"inewton\",\n" +
             "                        \"openid\":\"https://www.google.com/profiles/105119525695492353427\"\n" +
             "                     }\n" +
             "                  ],\n" +
             "                  \"id\":\"1\"\n" +
             "               }\n" +
             "            },\n" +
             "            \"context\":\"http://backplane1-2.janraindemo.com/token.html\"\n" +
             "         }" +
             "    }";
 
     private MockHttpServletRequest request;
 	private MockHttpServletResponse response;
     private HandlerAdapter handlerAdapter;
     private Client testClient;
 
     /**
 	 * Initialize before every individual test method
 	 */
 	@Before
 	public void init() throws BackplaneServerException, DaoException {
         assertNotNull(applicationContext);
         handlerAdapter = applicationContext.getBean("handlerAdapter", HandlerAdapter.class);
         this.testClient = this.createTestBusAndClient();
 		refreshRequestAndResponse();
 	}
 
     @After
     public void cleanup() throws TokenException, DaoException {
 
         logger.info("Tearing down test writes to db");
 
         try {
 
             for (String key:this.createdMessageKeys) {
                 logger.info("deleting Message " + key);
                 BP2DAOs.messageDao().delete(key);
                 //superSimpleDB.delete(bpConfig.getTableName(BP_MESSAGES), key);
             }
 
             try {
                 List<Backplane2Message> testMsgs = JavaConversions.seqAsJavaList(
                         BP2DAOs.messageDao().retrieveMessagesPerScope(new Scope("channel:testchannel"), null)._1());
 
                 for (Backplane2Message msg : testMsgs) {
                     logger.info("deleting Message " + msg.id());
                     BP2DAOs.messageDao().delete(msg.id());
                 }
             } catch (Exception e) {
                 // ignore - the domain may not exist
             }
 
             logger.info("checking for tokens to delete...");
             for (String key:this.createdTokenKeys) {
                 logger.info("deleting Token " + key);
                 BP2DAOs.tokenDao().delete(key);
             }
 
             for (String key:this.createdGrantsKeys) {
                 logger.info("deleting Grant " + key);
                Option<Grant2> grantOption = BP2DAOs.grantDao().get(key);
                Grant2 grant = grantOption.isDefined() ? grantOption.get() : null;
                 if (grant != null) {
                     com.janrain.backplane2.server.dao.BP2DAOs.getTokenDao().revokeTokenByGrant(grant.id());
                 }
                 BP2DAOs.grantDao().delete(key);
             }
 
             deleteTestBusAndClient();
         } catch (BackplaneServerException e) {
             logger.error(e);
         }
     }
 
 
 
 
     private Client createTestBusAndClient() throws BackplaneServerException, DaoException {
         BP2DAOs.busOwnerDao().store(new BusOwner("testBusOwner", "busOwnerSecret"));
         
         BP2DAOs.busDao().store(new BusConfig2(new HashMap<String,String>() {{
             put(BusConfig2Fields.BUS_NAME().name(), "testbus");
             put(BusConfig2Fields.OWNER().name(), "testBusOwner");
             put(BusConfig2Fields.RETENTION_TIME_SECONDS().name(), "600");
             put(BusConfig2Fields.RETENTION_STICKY_TIME_SECONDS().name(), "28800");
         }}));
 
         Client client = new Client(new HashMap<String,String>() {{
             put(ClientFields.USER().name(), RandomUtils.randomString(15));
             put(ClientFields.PWDHASH().name(), "secret");
             put(ClientFields.SOURCE_URL().name(), "http://source_url.com");
             put(ClientFields.REDIRECT_URI().name(), "http://redirect.com");
         }});
         BP2DAOs.clientDao().store(client);
         return client;
     }
 
     private void deleteTestBusAndClient() throws BackplaneServerException, TokenException, DaoException {
         BP2DAOs.busOwnerDao().delete("testBusOwner");
         BP2DAOs.busDao().delete("testbus");
         BP2DAOs.clientDao().delete(this.testClient.id());
     }
 
     private void refreshRequestAndResponse() {
 		request = new MockHttpServletRequest();
         // simulate https for tests to pass
         request.addHeader("x-forwarded-proto", "https");
 		response = new MockHttpServletResponse();
 	}
 
     private void saveMessage(Backplane2Message message) throws BackplaneServerException, DaoException {
         BP2DAOs.messageDao().store(message);
         this.createdMessageKeys.add(message.id());
         logger.info("created Message " + message.id());
     }
 
     private void saveGrant(Grant2 grant) throws BackplaneServerException, DaoException {
         BP2DAOs.grantDao().store(grant);
         logger.info("saved grant: " + grant.id());
         this.createdGrantsKeys.add(grant.id());
     }
 
     private void saveToken(com.janrain.backplane.server2.oauth2.model.Token token) throws BackplaneServerException, DaoException {
         BP2DAOs.tokenDao().store(token);
         logger.info("saved token: " + token.id());
         this.createdTokenKeys.add(token.id());
     }
 
 
 
     @Test
     public void testChannelGeneration() {
         String channel = RandomUtils.randomString(1000);
         logger.info(channel);
         assertTrue(Base64.isBase64(channel));
     }
 
 
     @Test
     public void testTokenEndPointAnonymousWithClientSecret() throws Exception {
         //satisfy 13.1.1
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         // this could go to either the POST or GET enabled endpoint
         request.setMethod("POST");
         //request.setParameter("grant_type", com.janrain.oauth2.OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         //shouldn't contain the client_secret below
         setOAuthBasicAuthentication(request, "anonymous", "meh");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousWithClientSecret() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
         assertTrue(HttpServletResponse.SC_UNAUTHORIZED == response.getStatus());
     }
 
     @Test
     public void testTokenEndPointAuthenticationFailure() throws Exception {
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         setOAuthBasicAuthentication(request, testClient.id(), "wrong_secret");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAuthenticationFailure() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
         assertTrue(HttpServletResponse.SC_UNAUTHORIZED == response.getStatus());
     }
 
     @Test
     public void testTokenEndPointAnonymousTokenRequest() throws Exception {
         //satisfy 13.1.1
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.call_back";
 
         //  should return the form:
         //  callback({
         //      "access_token": "l5feG0KjdXTpgDAfOvN6pU6YWxNb7qyn",
         //      "expires_in":604800,
         //      "token_type": "Bearer",
         //      "scope": "channel:Tm5FUzstWmUOdp0xU5UW83r2q9OXrrxt"
         // })
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("callback", callback);
         request.setParameter("bus", "testbus");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequest() => " + response.getContentAsString());
 
         // our tests won't include the callback string in this test so don't expect it
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\\s*\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\"bus:\\s*testbus\\s*channel:.{34}\",\\s*" +
                         "\"expires_in\":\\s*\"604[0-9]{3}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
         // cleanup test tokens
         String result = response.getContentAsString();
         Map<String,Object> returnedBody = new ObjectMapper().readValue(result, new TypeReference<Map<String,Object>>() {});
         BP2DAOs.tokenDao().delete((String)returnedBody.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME));
         BP2DAOs.tokenDao().delete((String)returnedBody.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME));
 
 
     }
 
     @Test
     public void testTokenEndPointAnonymousTokenRequestWithInvalidScope() throws Exception {
         //satisfy 13.1.1
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         setOAuthBasicAuthentication(request, "anonymous", "");
         request.setParameter("scope","channel:notmychannel");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequestWithInvalidScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         setOAuthBasicAuthentication(request, "anonymous", "");
         request.setParameter("scope","bus:notmybus");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequestWithInvalidScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testScope() throws Exception {
         refreshRequestAndResponse();
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("scope","type: sticky:");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testScope2() throws Exception {
         refreshRequestAndResponse();
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("scope","sticky:meh");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testScope2() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testScope3() throws Exception {
         refreshRequestAndResponse();
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("scope","source:httpgoogle.com");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testScope3() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testScope4() throws Exception {
         refreshRequestAndResponse();
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("scope","sticky:false source:http://test.com");
         request.setParameter("callback", callback);
         request.setParameter("bus", "testbus");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testScope4() => " + response.getContentAsString());
 
 
         String result = response.getContentAsString();
         Map<String,Object> msg = new ObjectMapper().readValue(result, new TypeReference<Map<String,Object>>() {});
         String scope = msg.get("scope").toString();
 
         assertTrue(scope.contains("sticky:false"));
         assertTrue(scope.contains("source:http://test.com"));
         assertTrue(scope.contains("channel:"));
 
         // remove test tokens
         BP2DAOs.tokenDao().delete((String)msg.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME));
         BP2DAOs.tokenDao().delete((String)msg.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME));
     }
 
 
     @Test
     public void testTokenEndPointClientTokenRequestInvalidCode() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         //will fail because the code below is not valid
         request.setParameter("code", "meh");
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientTokenRequestInvalidCode() => " + request.toString() + " => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEnflushadPointClientTokenRequest() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         //create grant for test
 
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:test").buildGrant();
         this.saveGrant(grant);
 
         // because we didn't specify a bus in the "scope" parameter, the server will
         // return the scope it determined from the grant
 
         request.setParameter("scope", "sticky:true");
         request.setParameter("code", grant.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientTokenRequest() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\"bus:test sticky:\\s*true\\s*\",\\s*" +
                         "\"expires_in\":\\s*\"3153[0-9]{4}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
 
         Map<String,Object> msg = new ObjectMapper().readValue(response.getContentAsString(),
                 new TypeReference<Map<String,Object>>() {});
         String scope = msg.get("scope").toString();
 
         assertTrue(scope.contains("sticky:true"));
         assertTrue(scope.contains("bus:test"));
 
     }
 
     @Test
     public void testTokenGrantByCodeScopeIsolation() throws Exception {
 
         refreshRequestAndResponse();
 
         //create grant for test
         Grant2 grant1 = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:foo").buildGrant();
         this.saveGrant(grant1);
 
         Grant2 grant2 = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.ACTIVE, "fakeOwnerId", testClient.id(), "bus:bar").buildGrant();
         this.saveGrant(grant2);
 
         // add grant with duplicate bus
         Grant2 grant3 = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.ACTIVE, "fakeOwernId", testClient.id(), "bus:foo").buildGrant();
         this.saveGrant(grant3);
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         // because we didn't specify the "scope" parameter, the server will
         // return the scope it determined from grant1 but not grant2
 
         request.setParameter("code", grant1.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeIsolation() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\\s*\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\"bus:foo\",\\s*" +
                         "\"expires_in\":\\s*\"3153[0-9]{4}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
 
         assertFalse("Invalid scope: " + msg.get("scope"), msg.get("scope").toString().contains("bar"));
 
         //
         // make the call again with "client_credentials" and verify that scope covers all grants
         //
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
 
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         // because we didn't use a "code", the server will
         // return the scope it determined from grant1 and grant2
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeIsolation() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\\s*\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\\s*\"[\\s:a-z]*\",\\s*" +
                         "\"expires_in\":\\s*\"3153[0-9]{4}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
         msg = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         String scope = msg.get("scope").toString();
 
         assertTrue("Invalid scope: " + scope, scope.contains("bar") && scope.contains("foo") );
         assertTrue(new Scope(scope).getScopeFieldValues(Backplane2MessageFields.BUS()).size() == 2);
 
 
     }
 
 
     @Test
     public void testTokenGrantByCodeScopeComplexity() throws Exception {
 
         refreshRequestAndResponse();
 
         //create grant for test
         ArrayList<String> randomBuses = new ArrayList<String>();
         int numBuses = 60;
         for (int i=0; i < numBuses; i++) {
             randomBuses.add(RandomUtils.randomString(10));
         }
 
         // add a duplicate bus in the grant - it should be ignored in the issued token
         randomBuses.add(randomBuses.get(0));
 
         String buses = org.springframework.util.StringUtils.collectionToDelimitedString(randomBuses, " ");
 
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), buses)).buildGrant();
         this.saveGrant(grant);
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
         request.setParameter("code", grant.id());
         request.setParameter("scope", "sticky:false sticky:true source:" + testClient.get(ClientFields.SOURCE_URL()).get());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeComplexity() get token => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\\s*\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\\s*\".*\",\\s*" +
                         "\"expires_in\":\\s*\"3153[0-9]{4}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
         // attempt to read a message on one of the buses
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> reply = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         String returnedToken = (String)reply.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME);
         Scope scope = new Scope((String)reply.get("scope"));
         assertTrue(scope.getScopeFieldValues(Backplane2MessageFields.BUS()).size() == numBuses);
 
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), randomBuses.get(randomBuses.size()-1));
         msg.put(Backplane2MessageFields.CHANNEL().name(), "randomchannel");
         Backplane2Message message1 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message1);
 
         // make sure the processor runs
         Thread.sleep(4000);
 
          // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         setOauthBearerTokenAuthorization(request, returnedToken);
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeComplexity()   => " + response.getContentAsString());
 
         assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         // should just receive one message on the last bus
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue("messages returned " + returnedMsgs.size() + " but should have been 1", returnedMsgs.size() == 1);
 
     }
 
     @Test
     public void testTokenEndPointClientUsedCode() throws Exception {
         refreshRequestAndResponse();
 
         //create grant for test
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:test").buildGrant();
         this.saveGrant(grant);
         logger.info("issued AuthCode " + grant.id());
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
         request.setParameter("code", grant.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientUsedCode() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\\s*\"access_token\":\\s*\".{27}\",\\s*" +
                         "\"scope\":\"bus:test\",\\s*" +
                         "\"expires_in\":\\s*\"3153[0-9]{4}\",\\s*" +
                         "\"refresh_token\":\".{27}\"\\s*[}]"));
 
         // now, try to use the same code again
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
         request.setParameter("code", grant.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), testClient.get(ClientFields.PWDHASH()).get());
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientUsedCode() ====> " + response.getContentAsString());
 
         assertTrue(BP2DAOs.grantDao().get(grant.id()).get().getState() == GrantState.REVOKED);
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
 
     }
 
     @Test
     public void TryToUseMalformedScopeTest() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         //create grant for test
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:test").buildGrant();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         request.setParameter("scope", "bus;mybus.com bus:yourbus.com");
         handlerAdapter.handle(request, response, controller);
         logger.info("TryToUseMalformedScopeTest() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
         // try again with anonymous access with privileged use of payload
 
         request.setParameter("scope", "payload.blah.blah");
         handlerAdapter.handle(request, response, controller);
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
         logger.info("TryToUseMalformedScopetest() => " + response.getContentAsString());
 
     }
 
     @Test
     public void TryToUseInvalidScopeTest() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         //create grant for test
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:mybus.com").buildGrant();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.id());
         request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
 
         request.setParameter("scope", "bus:mybus.com bus:yourbus.com");
         handlerAdapter.handle(request, response, controller);
         logger.info("TryToUseInvalidScopeTest() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testClientCredentialsUnauthorizedScope() throws Exception {
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         request.setParameter("scope", "bus:someunauthorized.bus");
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         handlerAdapter.handle(request, response, controller);
         logger.info("testClientCredentialsUnauthorizedScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointNoURI() throws Exception {
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", "meh");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
 
         //create grant for test
         Grant2 grant = new GrantBuilder(GrantType.AUTHORIZATION_CODE, GrantState.INACTIVE, "fakeOwnerId", testClient.id(),"bus:test").buildGrant();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.id());
 
         //will fail because no redirect_uri value is included
         request.setParameter("redirect_uri","");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointNoURI() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointNoClientSecret() throws Exception {
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS);
         //will fail because no client_secret is included
         setOAuthBasicAuthentication(request, "meh", "");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointNoClientSecret() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointEmptyCode() throws Exception {
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", "meh");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
         //will fail because no code value is included
         request.setParameter("code","");
         request.setParameter("redirect_uri","meh");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointEmptyCode() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointBadGrantType() throws Exception {
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", "meh");
         //will fail because bad grant type included
         request.setParameter("grant_type", "unexpected_value");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointBadGrantType() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointNoParams() throws Exception {
         // test empty parameters submitted to the token endpoint
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointNoParams() => " + response.getContentAsString());
 
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
         assertTrue( response.getStatus() == HttpServletResponse.SC_BAD_REQUEST ||
                     response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED );
     }
 
     @Test
     public void testMessageEndPoint() throws Exception {
 
         // Create appropriate token
         String tokenBus = "testbus";
         TokensAndChannel tokensAndChannel = anonTokenRequest(tokenBus);
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), tokenBus);
         msg.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannel.channel);
         Backplane2Message message = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message);
 
         Thread.sleep(1000);
 
         // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message/" + message.id());
         request.setMethod("GET");
         request.setParameter(OAUTH2_ACCESS_TOKEN_PARAM_NAME, tokensAndChannel.bearerToken);
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessageEndPoint()  => " + response.getContentAsString());
        // assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         // {
         //  "messageURL": "https://bp.example.com/v2/message/097a5cc401001f95b45d37aca32a3bd2",
         //  "source": "http://aboutecho.com",
         //  "type": "identity/ack"
         //  "bus": "customer.com",
         //  "channel": "67dc880cc265b0dbc755ea959b257118"
         //}
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*" +
                         "\"bus\":\\s*\".*\",\\s*" +
                         "\"channel\":\\s*\".*\",\\s*" +
                         "\"source\":\\s*\".*\",\\s*" +
                         "\"messageURL\":\\s*\".*\",\\s*" +
                         "\"sticky\":\\s*\".*\",\\s*" +
                         "\"expire\":\\s*\".*\",\\s*" +
                         "\"type\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         assertTrue("Expected " + HttpServletResponse.SC_OK + " but received: " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_OK);
 
         BP2DAOs.tokenDao().delete(tokensAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannel.refreshToken);
     }
 
     @Test
     public void testMessageEndPointPAL() throws Exception {
 
         // Create appropriate token
         String testBus = "testbus";
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),"bus:" + testBus).buildGrant());
         String token = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), testBus));
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), testBus);
         msg.put(Backplane2MessageFields.CHANNEL().name(), "randomchannel");
         Backplane2Message message = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message);
 
         Thread.sleep(1000);
 
         // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message/" + message.id());
         request.setMethod("GET");
         setOauthBearerTokenAuthorization(request, token);
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessageEndPointPAL()   => " + response.getContentAsString());
        // assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
 
         // {
         //  "messageURL": "https://bp.example.com/v2/message/097a5cc401001f95b45d37aca32a3bd2",
         //  "source": "http://aboutecho.com",
         //  "type": "identity/ack"
         //  "bus": "customer.com",
         //  "channel": "67dc880cc265b0dbc755ea959b257118",
         //  "payload": {
         //      "role": "administrator"
         //  },
         //}
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*" +
                         "\"bus\":\\s*\".*\",\\s*" +
                         "\"channel\":\\s*\".*\",\\s*" +
                         "\"source\":\\s*\".*\",\\s*" +
                         "\"payload\":\\s*.*" +
                         "\"messageURL\":\\s*\".*\",\\s*" +
                         "\"sticky\":\\s*\".*\",\\s*" +
                         "\"expire\":\\s*\".*\",\\s*" +
                         "\"type\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         assertTrue("Expected " + HttpServletResponse.SC_OK + " but received: " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_OK);
     }
 
     @Test
     public void testMessagesEndPointPAL() throws Exception {
 
         // Create appropriate token
         String testBuses = "this.com that.com";
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), testBuses)).buildGrant());
         String token = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), testBuses));
 
         // Seed 2 messages
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
 
         msg.put(Backplane2MessageFields.BUS().name(), "this.com");
         msg.put(Backplane2MessageFields.CHANNEL().name(), "qCDsQm3JTnhZ91RiPpri8R31ehJQ9lhp");
         Backplane2Message message1 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message1);
 
         msg.put(Backplane2MessageFields.BUS().name(), "that.com");
         msg.put(Backplane2MessageFields.CHANNEL().name(), "randomchannel");
         Backplane2Message message2 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message2);
 
         Thread.sleep(1000);
 
          // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         setOauthBearerTokenAuthorization(request, token);
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessagesEndPointPAL()   => " + response.getContentAsString());
 
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue("Expected 2 messages, received "  + returnedMsgs.size() + " :\n " + response.getContentAsString(), returnedMsgs.size() == 2);
     }
 
     @Test
     public void testMessagesEndPointRegular() throws Exception {
 
         logger.info("TEST: testMessagesEndPointRegular() =================");
 
         // Create appropriate token
         String testBus = "testbus";
         TokensAndChannel tokensAndchannel = anonTokenRequest(testBus);
 
         // Seed 2 messages
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
 
         msg.put(Backplane2MessageFields.BUS().name(), "otherbus");
         msg.put(Backplane2MessageFields.CHANNEL().name(), tokensAndchannel.channel);
         Backplane2Message message1 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message1);
 
         msg.put(Backplane2MessageFields.BUS().name(), "testbus");
         // same channel / different bus should never happen in production with true random, server-generated channel name
         msg.put(Backplane2MessageFields.CHANNEL().name(), tokensAndchannel.channel);
         Backplane2Message message2 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message2);
 
         Thread.sleep(1000);
 
          // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         request.setParameter("block", "15");
         request.setParameter(OAUTH2_ACCESS_TOKEN_PARAM_NAME, tokensAndchannel.bearerToken);
         //request.setParameter("since", message1.id());
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessagesEndPointRegular() => " + response.getContentAsString());
 
         assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         // should just receive one of the two messages
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue("Expected 1 message, received "  + returnedMsgs.size(), returnedMsgs.size() == 1);
 
         BP2DAOs.tokenDao().delete(tokensAndchannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndchannel.refreshToken);
 
         logger.info("========================================================");
 
     }
 
     @Test
     public void testMessagesEndPointPALInvalidScope() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create inappropriate token
         try {
             privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "mybus.com yourbus.com invalidbus.com"));
         } catch (TokenException bpe) {
             //expected
             return;
         }
 
         fail("Token requested with invalid scope should have failed");
     }
 
     @Test
     public void testMessagesPostEndPointPAL() throws Exception {
 
         // Create source token for the channel
         TokensAndChannel tokensAndChannel = anonTokenRequest("testbus");
 
         logger.info("created one anon token");
 
         // Create appropriate token
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus")).buildGrant());
         String token2 = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus"));
 
         // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message");
         request.setMethod("POST");
         setOauthBearerTokenAuthorization(request, token2);
         request.addHeader("Content-type", "application/json");
         HashMap<String, Object> msg = new HashMap<String, Object>();
         Map<String,Object> postMessage = new ObjectMapper().readValue(TEST_MSG_1, new TypeReference<Map<String, Object>>(){});
         postMessage.put(Backplane2MessageFields.BUS().name(), "testbus");
         postMessage.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannel.channel);
         msg.put("message", postMessage);
         String msgsString = new ObjectMapper().writeValueAsString(msg);
         logger.info(msgsString);
         request.setContent(msgsString.getBytes());
 
         handlerAdapter.handle(request, response, controller);
         logger.info(response.getContentAsString());
 
         assertTrue(response.getStatus() == HttpServletResponse.SC_CREATED);
 
         Thread.sleep(1000);
 
         List<Backplane2Message> messages = JavaConversions.seqAsJavaList(
                 BP2DAOs.messageDao().retrieveMessagesPerScope(new Scope("channel:" + tokensAndChannel.channel), null)._1());
         for (Backplane2Message message: messages) {
             BP2DAOs.messageDao().delete(message.id());
         }
 
         BP2DAOs.tokenDao().delete(tokensAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannel.refreshToken);
 
     }
 
     /**
      * Test to determine if two messages posted to the same channel but on different buses fail, as they should
      * @throws Exception
      */
 
     @Test
     public void testMessagePost() throws Exception {
 
         // Create source token for the channel
         TokensAndChannel tokensAndChannelAndChannel = anonTokenRequest("testbus");
 
         // Create appropriate token
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus")).buildGrant());
         String token2 = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus"));
 
         // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message");
         request.setMethod("POST");
         setOauthBearerTokenAuthorization(request, token2);
         request.addHeader("Content-type", "application/json");
         //request.setContentType("application/json");
         //request.setParameter("messages", TEST_MSG_1);
         HashMap<String, Object> msg = new HashMap<String, Object>();
         Map<String,Object> postMessage1 = new ObjectMapper().readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>(){});
         postMessage1.put(Backplane2MessageFields.BUS().name(), "testbus");
         postMessage1.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannelAndChannel.channel);
         msg.put("message", postMessage1);
         String msgString = new ObjectMapper().writeValueAsString(msg);
         logger.info(msgString);
         request.setContent(msgString.getBytes());
 
         try {
             handlerAdapter.handle(request, response, controller);
             logger.info("testMessagePost() => " + response.getContentAsString());
         } catch (InvalidRequestException notExpected) {
             // should not fail
             fail("should not fail " + notExpected.getMessage());
         }
 
 
         refreshRequestAndResponse();
         // Make the call
         request.setRequestURI("/v2/message");
         request.setMethod("POST");
         setOauthBearerTokenAuthorization(request, token2);
         request.addHeader("Content-type", "application/json");
         //request.setContentType("application/json");
         //request.setParameter("messages", TEST_MSG_1);
         msg = new HashMap<String, Object>();
         Map<String,Object> postMessage2 = new ObjectMapper().readValue(TEST_MSG_2, new TypeReference<Map<String,Object>>(){});
         postMessage2.put(Backplane2MessageFields.BUS().name(), "otherbus");
         postMessage2.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannelAndChannel.channel);
         msg.put("message", postMessage2);
         msgString = new ObjectMapper().writeValueAsString(msg);
         logger.info(msgString);
         request.setContent(msgString.getBytes());
 
         try {
             handlerAdapter.handle(request, response, controller);
             logger.info("testMessagePost() => " + response.getContentAsString());
             assertTrue("This test should have failed due to attempt to bind a channel to two buses", false);
         } catch (InvalidRequestException expected) {
             // should fail
             assertTrue(expected.getMessage().contains("Invalid bus - channel binding"));
         }
 
         BP2DAOs.tokenDao().delete(tokensAndChannelAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannelAndChannel.refreshToken);
     }
 
     /**
      * Test single message retrieval
      * @throws Exception
      */
 
     @Test
     public void testMessagePost2() throws Exception {
 
         // Create source token for the channel
         String testBus = "testbus";
         TokensAndChannel tokensAndChannel = anonTokenRequest(testBus);
 
         // Create appropriate token
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus")).buildGrant());
         String token2 = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus"));
 
          // Seed 1 message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), testBus);
         msg.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannel.channel);
         Backplane2Message message1 = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message1);
 
         Thread.sleep(1000);
 
         // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message/" + message1.id());
         request.setMethod("GET");
         setOauthBearerTokenAuthorization(request, token2);
 
         handlerAdapter.handle(request, response, controller);
         logger.info(response.getContentAsString());
         assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         BP2DAOs.tokenDao().delete(tokensAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannel.refreshToken);
 
     }
 
     @Test
     public void testMessagePost3() throws Exception {
 
         // Create source token for the channel
         String testBus = "testbus";
         TokensAndChannel tokensAndChannel = anonTokenRequest(testBus);
 
         // Create appropriate token
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus")).buildGrant());
         String token2 = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus"));
 
         boolean success = false;
         int numberOfPostedMessages = 0;
 
         refreshRequestAndResponse();
         for (int i=0; i < bpConfig.getDefaultMaxMessageLimit()+1; i++) {
             // Make the call
             request.setRequestURI("/v2/message");
             request.setMethod("POST");
             setOauthBearerTokenAuthorization(request, token2);
             request.addHeader("Content-type", "application/json");
             //request.setContentType("application/json");
             //request.setParameter("messages", TEST_MSG_1);
             HashMap<String, Object> msg = new HashMap<String, Object>();
             Map<String,Object>postMesssage = new ObjectMapper().readValue(TEST_MSG_1, new TypeReference<Map<String, Object>>(){});
             postMesssage.put(Backplane2MessageFields.BUS().name(), testBus);
             postMesssage.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannel.channel);
             msg.put("message", postMesssage);
             String msgsString = new ObjectMapper().writeValueAsString(msg);
             logger.info(msgsString);
             request.setContent(msgsString.getBytes());
 
             try {
                 handlerAdapter.handle(request, response, controller);
                 logger.info("testMessagePost3 => " + response.getContentAsString());
                 assertFalse("Unexpected error: " + response.getContentAsString(), response.getContentAsString().contains("invalid_request"));
                 assertTrue(response.getStatus() == HttpServletResponse.SC_CREATED);
                 logger.info("Messages posted: " + ++numberOfPostedMessages);
             } catch (InvalidRequestException expected) {
                 // should fail if we're over quota
                 if (i >= bpConfig.getDefaultMaxMessageLimit() && expected.getMessage().contains("Message limit of")) {
                     success=true;
                 } else {
                     fail("Unexpected error: " + expected.getMessage());
                 }
             } catch (Exception e) {
                 fail("Error: " + e.getMessage());
             }
 
             Thread.sleep(500);
 
             refreshRequestAndResponse();
         }
 
         assertTrue("Limit should have been reached, but " + numberOfPostedMessages + "<=" + bpConfig.getDefaultMaxMessageLimit(), success);
 
         BP2DAOs.tokenDao().delete(tokensAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannel.refreshToken);
 
     }
 
 
     @Test
     public void testGrantAndRevoke() throws Exception {
 
         refreshRequestAndResponse();
 
         logger.info("TEST: testGrantAndRevoke() =================");
 
         // Create auth
         ArrayList<Grant2> grants = new ArrayList<Grant2>();
         Grant2 grant1 = new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(), "bus:mybus.com").buildGrant();
         Grant2 grant2 = new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(), "bus:thisbus.com").buildGrant();
         this.saveGrant(grant1);
         this.saveGrant(grant2);
         grants.add(grant1);
         grants.add(grant2);
 
         // Create appropriate token
         String  token = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), ""));
 
        // Revoke grant and associated tokens based on one code
        BP2DAOs.grantDao().delete(grant1.id());
 
         try {
             // Now the token should fail
             // Make the call
             request.setRequestURI("/v2/messages");
             request.setMethod("GET");
             setOauthBearerTokenAuthorization(request, token);
             handlerAdapter.handle(request, response, controller);
             logger.info("testGrantAndRevoke() => " + response.getContentAsString());
 
             assertTrue(HttpServletResponse.SC_FORBIDDEN == response.getStatus());
             assertTrue(response.getContentAsString().contains("invalid token"));
         } finally {
             BP2DAOs.tokenDao().delete(token);
         }
     }
 
     @Test
     public void testGrantAndRevokeByBus() throws Exception {
 
         refreshRequestAndResponse();
 
         logger.info("TEST: testGrantAndRevokeByBus() =================");
 
         // Create auth
         ArrayList<Grant2> grants = new ArrayList<Grant2>();
         Grant2 grant1 = new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(), "bus:mybus.com").buildGrant();
         Grant2 grant2 = new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(), "bus:thisbus.com").buildGrant();
         this.saveGrant(grant1);
         this.saveGrant(grant2);
         grants.add(grant1);
         grants.add(grant2);
 
         // Create appropriate token
         String  token = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), ""));
 
         logger.info("revoken grants by bus: mybus.com");
         BP2DAOs.busDao().delete("mybus.com");
 
         try {
             // Now the token should fail
             // Make the call
             request.setRequestURI("/v2/messages");
             request.setMethod("GET");
             setOauthBearerTokenAuthorization(request, token);
             handlerAdapter.handle(request, response, controller);
             logger.info("testGrantAndRevokeByBus() => " + response.getContentAsString());
 
             assertTrue(HttpServletResponse.SC_FORBIDDEN == response.getStatus());
             assertTrue(response.getContentAsString().contains("invalid token"));
         } finally {
             BP2DAOs.tokenDao().delete(token);
         }
 
     }
 
 
     @Test
     public void testAuthenticate() throws Exception {
 
         final BusOwner busOwner = new BusOwner(RandomUtils.randomString(20), "foo");
 
         BusConfig2 bus1 = new BusConfig2(new HashMap<String,String>() {{
             put(BusConfig2Fields.BUS_NAME().name(), RandomUtils.randomString(30));
             put(BusConfig2Fields.OWNER().name(), busOwner.id());
             put(BusConfig2Fields.RETENTION_TIME_SECONDS().name(), "100");
             put(BusConfig2Fields.RETENTION_STICKY_TIME_SECONDS().name(), "50000");
         }});
         BusConfig2 bus2 = new BusConfig2(new HashMap<String,String>() {{
             put(BusConfig2Fields.BUS_NAME().name(), RandomUtils.randomString(30));
             put(BusConfig2Fields.OWNER().name(), busOwner.id());
             put(BusConfig2Fields.RETENTION_TIME_SECONDS().name(), "100");
             put(BusConfig2Fields.RETENTION_STICKY_TIME_SECONDS().name(), "50000");
         }});
 
         try {
             BP2DAOs.busOwnerDao().store(busOwner);
 
             // create a few buses
             BP2DAOs.busDao().store(bus1);
             BP2DAOs.busDao().store(bus2);
 
             refreshRequestAndResponse();
 
             // encode un:pw
             String credentials = testClient.id() + ":" + "secret";
             String encodedCredentials = new String(Base64.encode(credentials.getBytes()));
 
             logger.info("hit /authorize endpoint to get ball rolling");
             request.setRequestURI("/v2/authorize");
             request.setMethod("GET");
             request.setAuthType("BASIC");
             request.addParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
             request.addParameter("response_type", OAUTH2_TOKEN_RESPONSE_TYPE_CODE);
             request.addParameter("client_id", testClient.id());
             request.addHeader("Authorization", "Basic " + encodedCredentials);
             ModelAndView mv = handlerAdapter.handle(request, response, controller);
             logger.info("should be redirect view to authenticate => " + mv.getViewName());
             Cookie authZCookie = response.getCookie("bp2.authorization.request");
             assertNotNull(authZCookie);
             logger.info("authZ cookie = " + authZCookie.getValue());
 
             refreshRequestAndResponse();
 
             logger.info("redirect to /authenticate endpoint");
             request.setRequestURI("/v2/authenticate");
             request.setMethod("GET");
             mv = handlerAdapter.handle(request, response, controller);
             logger.info("should be authentication view => " + mv.getViewName());
 
             refreshRequestAndResponse();
 
             request.setRequestURI("/v2/authenticate");
             request.addParameter("busOwner", busOwner.id());
             request.addParameter("password", "foo");
             request.setMethod("POST");
             mv = handlerAdapter.handle(request, response, controller);
             logger.info("should be redirect to authorize view => " + mv.getViewName());
             Cookie authNCookie = response.getCookie("bp2.bus.owner.auth");
             assertNotNull(authNCookie);
             logger.info("authN cookie = " + authNCookie.getValue());
 
             refreshRequestAndResponse();
 
             logger.info("redirect back to /authorize endpoint");
             request.setRequestURI("/v2/authorize");
             request.setMethod("POST");
             request.setAuthType("BASIC");
             request.addParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
             request.addParameter("response_type", OAUTH2_TOKEN_RESPONSE_TYPE_CODE);
             request.addParameter("client_id", testClient.id());
             request.setCookies(new Cookie[]{authNCookie, authZCookie});
 
             request.addHeader("Authorization", "Basic " + encodedCredentials);
             mv = handlerAdapter.handle(request, response, controller);
             Map<String, Object> model = mv.getModel();
             String authKey = (String) model.get("auth_key");
             model.put("scope", bus1.id());
 
             assertNotNull(authKey);
             logger.info("auth_key=" + authKey);
             logger.info("client_id=" + model.get("client_id"));
             logger.info("redirect_uri=" + model.get("redirect_uri"));
             logger.info("scope=" + model.get("scope"));
 
             logger.info("should be redirect to authorize view => " + mv.getViewName());
 
             refreshRequestAndResponse();
             logger.info("post bus owner grant to /authorize endpoint");
             request.setRequestURI("/v2/authorize");
             request.setMethod("POST");
             request.setAuthType("BASIC");
             request.addParameter("redirect_uri", (String) model.get("redirect_uri"));
             request.addParameter("response_type", OAUTH2_TOKEN_RESPONSE_TYPE_CODE);
             request.addParameter("client_id", (String) model.get("client_id"));
             request.addParameter("auth_key", authKey);
             request.addParameter("scope", (String) model.get("scope"));
             // simulate button press
             request.addParameter("authorize", "Authorize");
 
             request.setCookies(new Cookie[]{authNCookie, authZCookie});
 
             request.addHeader("Authorization", "Basic " + encodedCredentials);
             mv = handlerAdapter.handle(request, response, controller);
             logger.info("should be redirect back to client => " + mv.getViewName());
             assertTrue(mv.getViewName().contains("?code="));
 
             String code = mv.getViewName().substring(mv.getViewName().indexOf("code=")+5);
             logger.info("using code: '" + code + "' to retrieve token");
 
             // redeem the code for a token
             refreshRequestAndResponse();
 
             request.setRequestURI("/v2/token");
             request.setMethod("POST");
             request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_AUTH_CODE);
             request.setParameter("code", code);
             request.setParameter("redirect_uri", testClient.get(ClientFields.REDIRECT_URI()).get());
             setOAuthBasicAuthentication(request, testClient.id(), "secret");
 
             handlerAdapter.handle(request, response, controller);
 
             logger.info("should be a token response => " + response.getContentAsString());
 
             Map<String,Object> returnedBody = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
             String tokenId = (String) returnedBody.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME);
             assertNotNull(tokenId);
 
             Grant2 grant = BP2DAOs.grantDao().get(code).getOrElse(null);
             com.janrain.backplane.server2.oauth2.model.Token token = BP2DAOs.tokenDao().get(tokenId).getOrElse(null);
 
             assertTrue(grant.get(GrantFields.ISSUED_TO_CLIENT()).getOrElse(null).equals(token.get(TokenFields.ISSUED_TO_CLIENT()).getOrElse(null)));
             assertTrue(grant.get(GrantFields.ISSUED_BY_USER()).getOrElse(null).equals(busOwner.id()));
 
 
         } finally {
             BP2DAOs.busOwnerDao().delete(busOwner.id());
             BP2DAOs.busDao().delete(bus1.id());
             BP2DAOs.busDao().delete(bus2.id());
         }
 
     }
 
     @Test
     public void testMessageOrder() throws Exception {
 
         // Create source token for the channel
         String testBus = "testbus";
         TokensAndChannel tokensAndChannel = anonTokenRequest(testBus);
 
         // Create appropriate token
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),
                 Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus otherbus")).buildGrant());
         String token2 = privTokenRequest(Scope.getEncodedScopesAsString(Backplane2MessageFields.CHANNEL(), tokensAndChannel.channel));
 
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), testBus);
         msg.put(Backplane2MessageFields.CHANNEL().name(), tokensAndChannel.channel);
 
         // seed messages
         long numMessages = bpConfig.getDefaultMaxMessageLimit();
         ArrayList<Backplane2Message> messages = new ArrayList<Backplane2Message>();
 
         for (int i=0;i <= numMessages; i++) {
             messages.add(new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg));
         }
 
         // use #0 to set the 'since' from server time, don't count #0
         String since = messages.iterator().next().get(Backplane2MessageFields.ID()).getOrElse(null);
 
         // reverse the list
         //Collections.reverse(messages);
 
         for (Backplane2Message message : messages) {
             this.saveMessage(message);
         }
 
         // we assume the message processor is running in another thread...
        Thread.sleep(15000);
 
         // Make the call
         List<Map<String,Object>> allMsgs = new ArrayList<Map<String, Object>>();
         boolean moreMessages = false;
         do {
             refreshRequestAndResponse();
             request.setRequestURI("/v2/messages");
             request.setMethod("GET");
             request.setParameter("scope", "channel:" + tokensAndChannel.channel);
             if (org.apache.commons.lang.StringUtils.isNotBlank(since)) {
                 request.setParameter("since", since);
             }
             setOauthBearerTokenAuthorization(request, token2);
             handlerAdapter.handle(request, response, controller);
             logger.info("testMessageOrder()  => " + response.getContentAsString());
 
             Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
             List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
             allMsgs.addAll(returnedMsgs);
 
             moreMessages = (Boolean)returnedBody.get("moreMessages");
 
             //"messageURL": "https://bp.example.com/v2/message/097a5cc401001f95b45d37aca32a3bd2",
             String nextURL = returnedBody.get("nextURL").toString();
             since = nextURL.substring(nextURL.indexOf("since=") + 6);
             //Thread.sleep(1000);
 
         } while (moreMessages);
 
         assertTrue("Expected " + numMessages + " messages, received "  + allMsgs.size(), allMsgs.size() == numMessages);
         // they should be returned in lexicographic order by ID
         String prev = "";
         for (Map<String,Object> m : allMsgs) {
             assertTrue(m.get("messageURL").toString() + " <= " + prev, m.get("messageURL").toString().compareTo(prev) > 0);
             prev = (String)m.get("messageURL");
         }
 
         BP2DAOs.tokenDao().delete(tokensAndChannel.bearerToken);
         BP2DAOs.tokenDao().delete(tokensAndChannel.refreshToken);
 
     }
 
     @Test
     public void testAnonymousRefreshToken() throws Exception {
 
         Map<String, Object> tokenResponse = new AnonymousTokenRequest("bla", "testbus", null, request).tokenResponse();
         String refreshToken = tokenResponse.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME).toString();
         Scope scope1 = new Scope(tokenResponse.get(OAUTH2_SCOPE_PARAM_NAME).toString());
 
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("callback", "bla");
         request.setParameter(OAUTH2_REFRESH_TOKEN_PARAM_NAME, refreshToken);
         handlerAdapter.handle(request, response, controller);
         logger.info(response.getContentAsString());
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> responseBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         assertNotNull("expected access_token, got null", responseBody.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME));
         assertNotNull("expected refresh_token, got null", responseBody.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME));
         Scope scope2 = new Scope(responseBody.get(OAUTH2_SCOPE_PARAM_NAME).toString());
         assertEquals("initial and refresh token response scopes are not equal", scope1, scope2);
     }
 
     @Test
     public void testPrivilegedRefreshToken() throws Exception {
         refreshRequestAndResponse();
         saveGrant(new GrantBuilder(GrantType.CLIENT_CREDENTIALS, GrantState.ACTIVE, "fakeOwnerId", testClient.id(),"bus:testbus").buildGrant());
         Scope scope1 = new Scope(Scope.getEncodedScopesAsString(Backplane2MessageFields.BUS(), "testbus"));
         setOAuthBasicAuthentication(request, testClient.id(), testClient.get(ClientFields.PWDHASH()).get());
         Map<String, Object> tokenResponse = new AuthenticatedTokenRequest(OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS, testClient,
                 null, null, scope1.toString(), request).tokenResponse();
         String accessToken = tokenResponse.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME).toString();
         String refreshToken = tokenResponse.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME).toString();
 
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         setOAuthBasicAuthentication(request, testClient.id(), "secret");
         request.setParameter("grant_type", OAuth2.OAUTH2_TOKEN_GRANT_TYPE_REFRESH_TOKEN);
         request.setParameter(OAUTH2_REFRESH_TOKEN_PARAM_NAME, refreshToken);
         handlerAdapter.handle(request, response, controller);
         logger.info(response.getContentAsString());
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> responseBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
 
         String responseAccessToken = (String) responseBody.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME);
         assertNotNull("expected access_token, got null", responseAccessToken);
         assertFalse("expected access token, got: " + responseAccessToken, GrantType.fromTokenString(responseAccessToken).isRefresh());
         assertTrue("expected privileged access token, got: " + responseAccessToken, GrantType.fromTokenString(responseAccessToken).isPrivileged());
 
         String responseRefreshToken = (String) responseBody.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME);
         assertNotNull("expected refresh_token, got null", responseRefreshToken);
         assertTrue("expected refresh token, got: " + responseRefreshToken, GrantType.fromTokenString(responseRefreshToken).isRefresh());
         assertTrue("expected privileged refresh token, got: " + responseRefreshToken, GrantType.fromTokenString(responseRefreshToken).isPrivileged());
 
         Scope scope2 = new Scope(responseBody.get(OAUTH2_SCOPE_PARAM_NAME).toString());
         assertEquals("initial and refresh token response scopes are not equal", scope1, scope2);
     }
 
     @Test
     public void testLatestMessageRetrieval() throws Exception {
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG_1, new TypeReference<Map<String,Object>>() {});
         msg.put(Backplane2MessageFields.BUS().name(), "foo");
         msg.put(Backplane2MessageFields.CHANNEL().name(), "bar");
         Backplane2Message message = new Backplane2Message(testClient.get(ClientFields.SOURCE_URL()).get(), DEFAULT_MESSAGE_RETENTION_SECONDS, MAX_MESSAGE_RETENTION_SECONDS, msg);
         this.saveMessage(message);
 
         Thread.sleep(1000);
 
         Backplane2Message lastMessage = BP2DAOs.messageDao().retrieveMessagesPerScope(new Scope("channel:bar"), null)._1().reverse().head();
 
         assertTrue("messages not equal", lastMessage.id().equals(message.id()));
     }
 
     // - PRIVATE
 
     private void setOAuthBasicAuthentication(MockHttpServletRequest request, String client_id, String client_password) throws UnsupportedEncodingException {
         String userPass = client_id + ":" + client_password;
         request.addHeader("Authorization", "Basic " + new String(Base64.encode(userPass.getBytes("utf-8")), "utf-8"));
     }
 
     private void setOauthBearerTokenAuthorization(MockHttpServletRequest request, String accessToken) throws Exception {
         request.addHeader("Authorization", "Bearer " + accessToken);
     }
 
     private TokensAndChannel anonTokenRequest(String tokenBus) throws TokenException, DaoException {
         refreshRequestAndResponse();
         TokenRequest req = new AnonymousTokenRequest("bla", tokenBus, null, request);
         Map<String, Object> tokenResponse = req.tokenResponse();
         Scope scope = new Scope(tokenResponse.get(OAUTH2_SCOPE_PARAM_NAME).toString());
 
         //return new Pair<String, String>(tokenResponse.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME).toString(), scope.getScopeFieldValues(Backplane2MessageFields.CHANNEL()).iterator().next());
         TokensAndChannel tokensAndChannel = new TokensAndChannel();
         tokensAndChannel.bearerToken = tokenResponse.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME).toString();
         tokensAndChannel.refreshToken = tokenResponse.get(OAUTH2_REFRESH_TOKEN_PARAM_NAME).toString();
         tokensAndChannel.channel = scope.getScopeFieldValues(Backplane2MessageFields.CHANNEL()).iterator().next();
         return tokensAndChannel;
     }
 
     private String privTokenRequest(String scopeString) throws UnsupportedEncodingException, TokenException, DaoException {
         refreshRequestAndResponse();
         Scope scope = new Scope(scopeString);
         setOAuthBasicAuthentication(request, testClient.id(), testClient.get(ClientFields.PWDHASH()).get());
         TokenRequest req = new AuthenticatedTokenRequest(OAUTH2_TOKEN_GRANT_TYPE_CLIENT_CREDENTIALS, testClient,
                 null, null, scope.toString(), request);
         return req.tokenResponse().get(OAUTH2_ACCESS_TOKEN_PARAM_NAME).toString();
     }
 
     private class TokensAndChannel {
         String bearerToken;
         String refreshToken;
         String channel;
     }
 
 }
