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
 
 
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.backplane2.server.config.BusConfig2;
 import com.janrain.backplane2.server.config.Client;
 import com.janrain.backplane2.server.config.User;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.crypto.ChannelUtil;
 import com.janrain.crypto.HmacHashUtils;
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
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.HandlerAdapter;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.inject.Inject;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_CLIENTS;
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_MESSAGES;
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
     private SuperSimpleDB superSimpleDB;
 
     @Inject
     private Backplane2Config bpConfig;
 
     @Inject
     private DaoFactory daoFactory;
 
     private static final Logger logger = Logger.getLogger(Backplane2ControllerTest.class);
 
     ArrayList<String> createdMessageKeys = new ArrayList<String>();
     ArrayList<String> createdTokenKeys = new ArrayList<String>();
     ArrayList<String> createdGrantsKeys = new ArrayList<String>();
 
     static final String OK_RESPONSE = "{\"stat\":\"ok\"}";
     static final String ERR_RESPONSE = "\"error\":";
 
     static final String TEST_MSG =
             "    {\n" +
             "        \"bus\": \"mybus.com\",\n" +
             "        \"channel\": \"testchannel\",\n" +
             "        \"source\": \"ftp://bla_source/\",\n" +
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
 	public void init() throws SimpleDBException {
         assertNotNull(applicationContext);
         handlerAdapter = applicationContext.getBean("handlerAdapter", HandlerAdapter.class);
         this.testClient = this.createTestClient();
 		refreshRequestAndResponse();
 	}
 
     @After
     public void cleanup() {
         logger.info("Tearing down test writes to db");
         try {
             for (String key:this.createdMessageKeys) {
                 logger.info("deleting Message " + key);
                 superSimpleDB.delete(bpConfig.getTableName(BP_MESSAGES), key);
             }
 
             try {
                 List<BackplaneMessage> testMsgs = superSimpleDB.
                         retrieveWhere(bpConfig.getTableName(BP_MESSAGES), BackplaneMessage.class, "channel='testchannel'", true);
                 for (BackplaneMessage msg : testMsgs) {
                     logger.info("deleting Message " + msg.getIdValue());
                     superSimpleDB.delete(bpConfig.getTableName(BP_MESSAGES), msg.getIdValue());
                 }
             } catch (SimpleDBException sdbe) {
                 // ignore - the domain may not exist
             }
             for (String key:this.createdTokenKeys) {
                 logger.info("deleting Token " + key);
                 daoFactory.getTokenDao().deleteTokenById(key);
             }
 
             for (String key:this.createdGrantsKeys) {
                 logger.info("deleting Grant " + key);
                 Grant grant = daoFactory.getGrantDao().retrieveGrant(key);
                 daoFactory.getTokenDao().revokeTokenByGrant(grant);
                daoFactory.getGrantDao().deleteGrantById(key);
             }
 
             daoFactory.getClientDAO().delete(testClient.getIdValue());
         } catch (SimpleDBException e) {
             logger.error(e);
         }
     }
 
 
 
 
     private Client createTestClient() throws SimpleDBException {
         Client client = new Client(ChannelUtil.randomString(15), HmacHashUtils.hmacHash("secret"), "source_url", "http://redirect.com");
         daoFactory.getClientDAO().persist(client);
         return client;
     }
 
     private void refreshRequestAndResponse() {
 		request = new MockHttpServletRequest();
 		response = new MockHttpServletResponse();
 	}
 
     private void saveMessage(BackplaneMessage message) throws SimpleDBException {
         daoFactory.getBackplaneMessageDAO().persist(message);
         this.createdMessageKeys.add(message.getIdValue());
         logger.info("created Message " + message.getIdValue());
     }
 
     private void saveGrant(Grant grant) throws SimpleDBException {
         daoFactory.getGrantDao().persist(grant);
         logger.info("saved grant: " + grant.getIdValue());
         this.createdGrantsKeys.add(grant.getIdValue());
     }
 
     private void saveToken(Token token) throws SimpleDBException {
         daoFactory.getTokenDao().persist(token);
         logger.info("saved token: " + token.getIdValue());
         this.createdTokenKeys.add(token.getIdValue());
     }
 
 
 
     @Test
     public void testChannelGeneration() {
         String channel = ChannelUtil.randomString(1000);
         logger.info(channel);
         assertTrue(Base64.isBase64(channel));
     }
 
 
     @Test
     public void testTokenEndPointAnonymousWithClientSecret() throws Exception {
         //satisfy 13.1.1
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", "anonymous");
         request.setParameter("grant_type", "client_credentials");
         //shouldn't contain the client_secret below
         request.setParameter("client_secret","meh");
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousWithClientSecret() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointAnonymousTokenRequest() throws Exception {
         //satisfy 13.1.1
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.call_back";
 
         //  should return the form:
         //  callback({
         //      "access_token": "l5feG0KjdXTpgDAfOvN6pU6YWxNb7qyn",
         //      "expires_in":3600,
         //      "token_type": "Bearer",
         //      "backplane_channel": "Tm5FUzstWmUOdp0xU5UW83r2q9OXrrxt"
         // })
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("client_id", "anonymous");
         request.setParameter("grant_type", "client_credentials");
         request.setParameter("client_secret","");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequest() => " + response.getContentAsString());
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches(callback + "[(][{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"expires_in\":\\s*3600,\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"backplane_channel\":\\s*\".{32}+\"\\s*[}][)]"));
 
         // cleanup test token
         String result = response.getContentAsString().substring(response.getContentAsString().indexOf("{"), response.getContentAsString().indexOf(")"));
         Map<String,Object> returnedBody = new ObjectMapper().readValue(result, new TypeReference<Map<String,Object>>() {});
         daoFactory.getTokenDao().delete((String)returnedBody.get("access_token"));
 
     }
 
     @Test
     public void testTokenEndPointAnonymousTokenRequestWithInvalidScope() throws Exception {
         //satisfy 13.1.1
 
         //TODO: the spec doesn't allow '.' in the callback name but this likely needs to change
         String callback = "Backplane.callback";
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("client_id", "anonymous");
         request.setParameter("grant_type", "client_credentials");
         request.setParameter("client_secret","");
         request.setParameter("scope","channel:notmychannel");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequestWithInvalidScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("GET");
         request.setParameter("client_id", "anonymous");
         request.setParameter("grant_type", "client_credentials");
         request.setParameter("client_secret","");
         request.setParameter("scope","bus:notmybus");
         request.setParameter("callback", callback);
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointAnonymousTokenRequestWithInvalidScope() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
     }
 
 
 
     @Test
     public void testTokenEndPointClientTokenRequestInvalidCode() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
 
         //will fail because the code below is not valid
         request.setParameter("code", "meh");
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientTokenRequestInvalidCode() => " + request.toString() + " => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
     }
 
     @Test
     public void testTokenEndPointClientTokenRequest() throws Exception {
 
         //  should return the form:
         //  {
         //      "access_token":"l5feG0KjdXTpgDAfOvN6pU6YWxNb7qyn",
         //      "token_type":"Bearer",
         //      "scope":"bus:???"
         //  }
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
 
         //create grant for test
 
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),"test");
         // set code to expire?
         grant.setCodeExpirationDefault();
         this.saveGrant(grant);
 
         // because we didn't specify the "scope" parameter, the server will
         // return the scope it determined from the grant
 
         request.setParameter("code", grant.getIdValue());
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientTokenRequest() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"scope\":\\s*\".*\"\\s*" +
                         "[}]"));
     }
 
     @Test
     public void testTokenGrantByCodeScopeIsolation() throws Exception {
 
         refreshRequestAndResponse();
 
         //create grant for test
         Grant grant1 = new Grant("fakeOwnerId", testClient.getClientId(),"foo");
         grant1.setCodeExpirationDefault();
         this.saveGrant(grant1);
 
         Grant grant2 = new Grant("fakeOwnerId", testClient.getClientId(), "bar");
         grant2.setCodeExpirationDefault();
         // next step required to make it eligible for "client credentials" access
         grant2.setCodeUsedNow();
         this.saveGrant(grant2);
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
 
         // because we didn't specify the "scope" parameter, the server will
         // return the scope it determined from grant1 but not grant2
 
         request.setParameter("code", grant1.getIdValue());
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeIsolation() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"scope\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
 
         assertFalse("Invalid scope: " + msg.get("scope").toString(), msg.get("scope").toString().contains("bar"));
 
         //
         // make the call again with "client_credentials" and verify that scope covers both grants
         //
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "client_credentials");
 
         // because we didn't use a "code", the server will
         // return the scope it determined from grant1 and grant2
 
         request.setParameter("code", "");
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeIsolation() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"scope\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         msg = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         String scope = msg.get("scope").toString();
 
         assertTrue("Invalid scope: " + scope, scope.contains("bar") && scope.contains("foo") );
 
 
     }
 
 
     @Test
     public void testTokenGrantByCodeScopeComplexity() throws Exception {
 
         refreshRequestAndResponse();
 
         //create grant for test
         ArrayList<String> randomBuses = new ArrayList<String>();
         for (int i=0; i < 60; i++) {
             randomBuses.add(ChannelUtil.randomString(10));
         }
         String buses = StringUtils.collectionToDelimitedString(randomBuses, " ");
 
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),buses);
         grant.setCodeExpirationDefault();
         this.saveGrant(grant);
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
         request.setParameter("code", grant.getIdValue());
         request.setParameter("scope", "sticky:false sticky:true source:ftp://bla_source/");
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeComplexity() get token => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"scope\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         // attempt to read a message on one of the buses
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> reply = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         String returnedToken = reply.get("access_token").toString();
 
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message1 = new BackplaneMessage("123456", randomBuses.get(randomBuses.size()-1), "randomchannel", msg);
         this.saveMessage(message1);
 
          // Make the call
         refreshRequestAndResponse();
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         request.setParameter("access_token", returnedToken);
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenGrantByCodeScopeComplexity()   => " + response.getContentAsString());
 
         assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         // should just receive one message on the last bus
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue(returnedMsgs.size() == 1);
 
     }
 
     @Test
     public void testTokenEndPointClientUsedCode() throws Exception {
         refreshRequestAndResponse();
 
         //create grant for test
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),"test");
         grant.setCodeExpirationDefault();
         this.saveGrant(grant);
         logger.info("issued AuthCode " + grant.getIdValue());
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
         request.setParameter("code", grant.getIdValue());
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientUsedCode() => " + response.getContentAsString());
         //assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         assertTrue("Invalid response: " + response.getContentAsString(), response.getContentAsString().
                 matches("[{]\\s*\"access_token\":\\s*\".{22}+\",\\s*" +
                         "\"token_type\":\\s*\"Bearer\",\\s*" +
                         "\"scope\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         // now, try to use the same code again
         refreshRequestAndResponse();
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
         request.setParameter("code", grant.getIdValue());
         request.setParameter("client_secret", testClient.get(User.Field.PWDHASH));
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         handlerAdapter.handle(request, response, controller);
         logger.info("testTokenEndPointClientUsedCode() ====> " + response.getContentAsString());
 
         assertTrue(daoFactory.getGrantDao().retrieveGrant(grant.getIdValue()).isCodeUsed() == true);
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
 
     }
 
     @Test
     public void TryToUseMalformedScopeTest() throws Exception {
 
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
 
         //create grant for test
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),"test");
         grant.setCodeIssuedNow();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.getIdValue());
         request.setParameter("client_secret",  "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         request.setParameter("scope", "bus;mybus.com bus:yourbus.com");
         handlerAdapter.handle(request, response, controller);
         logger.info("TryToUseMalformedScopeTest() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
 
         // try again with anonymous access with privileged use of payload
         request.setParameter("client_id", Token.ANONYMOUS);
         request.setParameter("client_secret", "");
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
         request.setParameter("client_id", testClient.get(User.Field.USER));
         request.setParameter("grant_type", "code");
 
         //create grant for test
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),"mybus.com");
         grant.setCodeIssuedNow();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.getIdValue());
         request.setParameter("client_secret", "secret");
         request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
         request.setParameter("scope", "bus:mybus.com bus:yourbus.com");
         handlerAdapter.handle(request, response, controller);
         logger.info("TryToUseInvalidScopeTest() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
     }
 
     @Test
     public void testTokenEndPointNoURI() throws Exception {
         refreshRequestAndResponse();
 
         request.setRequestURI("/v2/token");
         request.setMethod("POST");
         request.setParameter("client_id", "meh");
         request.setParameter("grant_type", "code");
 
         //create grant for test
         Grant grant = new Grant("fakeOwnerId", testClient.getClientId(),"test");
         grant.setCodeIssuedNow();
         this.saveGrant(grant);
 
         request.setParameter("code", grant.getIdValue());
 
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
         request.setParameter("client_id", "meh");
         request.setParameter("grant_type", "client_credentials");
         //will fail because no client_secret is included
         request.setParameter("client_secret","");
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
         request.setParameter("grant_type", "code");
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
         assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
     }
 
     @Test
     public void testMessageEndPoint() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         TokenAnonymous token = new TokenAnonymous("mybus.com", null, new Date(new Date().getTime() + Token.EXPIRES_SECONDS * 1000));
         this.saveToken(token);
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message = new BackplaneMessage("123456", "mybus.com", token.getChannelName(), msg);
         this.saveMessage(message);
 
         // Make the call
         request.setRequestURI("/v2/message/123456");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
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
                         "\"messageURL\":\\s*\".*\",\\s*" +
                         "\"source\":\\s*\".*\",\\s*" +
                         "\"type\":\\s*\".*\",\\s*" +
                         "\"bus\":\\s*\".*\",\\s*" +
                         "\"channel\":\\s*\".*\"\\s*" +
                         "[}]"));
 
         assertTrue("Expected " + HttpServletResponse.SC_OK + " but received: " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_OK);
         assertTrue(response.getContentType().equals("application/json"));
     }
 
     @Test
     public void testMessageEndPointWithCallBack() throws Exception {
 
         String callbackName = "meh";
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         TokenAnonymous token = new TokenAnonymous("mybus.com", null, new Date(new Date().getTime() + Token.EXPIRES_SECONDS * 1000));
         this.saveToken(token);
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message = new BackplaneMessage("123456", "mybus.com", token.getChannelName(), msg);
         this.saveMessage(message);
 
         // now, try it via callback
         refreshRequestAndResponse();
         request.setRequestURI("/v2/message/123456");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
         request.setParameter("callback", callbackName);
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessageEndPointWithCallBack()  => " + response.getContentAsString());
 
         // callback({
         //  "messageURL": "https://bp.example.com/v2/message/097a5cc401001f95b45d37aca32a3bd2",
         //  "source": "http://aboutecho.com",
         //  "type": "identity/ack"
         //  "bus": "customer.com",
         //  "channel": "67dc880cc265b0dbc755ea959b257118"
         // })
 
         assertTrue("Invalid response: '" + response.getContentAsString() + "'", response.getContentAsString().
                 matches(callbackName + "[(][{]\\s*" +
                         "\"messageURL\":\\s*\".*\",\\s*" +
                         "\"source\":\\s*\".*\",\\s*" +
                         "\"type\":\\s*\".*\",\\s*" +
                         "\"bus\":\\s*\".*\",\\s*" +
                         "\"channel\":\\s*\".*\"\\s*" +
                         "[}][)]"));
 
         assertTrue("Expected " + HttpServletResponse.SC_OK + " but received: " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_OK);
         assertTrue(response.getContentType().equals("application/x-javascript"));
 
 
     }
 
     @Test
     public void testMessageEndPointPAL() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         TokenPrivileged token = new TokenPrivileged("fooClient", "mybus.com", null, null);
         this.saveToken(token);
 
         // Seed message
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message = new BackplaneMessage("123456", "mybus.com", "randomchannel", msg);
         this.saveMessage(message);
 
         // Make the call
         request.setRequestURI("/v2/message/123456");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
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
                         "\"messageURL\":\\s*\".*\",\\s*" +
                         "\"source\":\\s*\".*\",\\s*" +
                         "\"type\":\\s*\".*\",\\s*" +
                         "\"bus\":\\s*\".*\",\\s*" +
                         "\"channel\":\\s*\".*\",\\s*" +
                         "\"payload\":\\s*.*" +
                         "[}]"));
 
         assertTrue("Expected " + HttpServletResponse.SC_OK + " but received: " + response.getStatus(), response.getStatus() == HttpServletResponse.SC_OK);
         assertTrue(response.getContentType().equals("application/json"));
 
 
     }
 
     @Test
     public void testMessagesEndPointPAL() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         TokenPrivileged token = new TokenPrivileged("fooClient", "this.com that.com", "bus:this.com bus:that.com", null);
         this.saveToken(token);
 
         // Seed 2 messages
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message1 = new BackplaneMessage("123456", "this.com", "qCDsQm3JTnhZ91RiPpri8R31ehJQ9lhp", msg);
         this.saveMessage(message1);
 
         BackplaneMessage message2 = new BackplaneMessage("1234567", "that.com", "randomchannel", msg);
         this.saveMessage(message2);
 
          // Make the call
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessagesEndPointPAL()   => " + response.getContentAsString());
 
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue(returnedMsgs.size() == 2);
     }
 
     @Test
     public void testMessagesEndPointRegular() throws Exception {
 
         logger.info("TEST: testMessagesEndPointRegular() =================");
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         TokenAnonymous token = new TokenAnonymous(null, null, new Date(new Date().getTime() + Token.EXPIRES_SECONDS * 1000));
         this.saveToken(token);
 
         // Seed 2 messages
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         BackplaneMessage message1 = new BackplaneMessage(BackplaneMessage.generateMessageId(), "a.com", token.getChannelName(), msg);
         this.saveMessage(message1);
 
         BackplaneMessage message2 = new BackplaneMessage(BackplaneMessage.generateMessageId(), "b.com", token.getChannelName(), msg);
         this.saveMessage(message2);
 
          // Make the call
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
         request.setParameter("since", message1.getIdValue());
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessagesEndPointRegular() => " + response.getContentAsString());
 
         assertFalse(response.getContentAsString().contains(ERR_RESPONSE));
 
         // should just receive one of the two messages
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue(returnedMsgs.size() == 1);
 
         logger.info("========================================================");
 
     }
 
     @Test
     public void testMessagesEndPointPALInvalidScope() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create inappropriate token
         try {
             TokenPrivileged token = new TokenPrivileged("fooClient", "mybus.com yourbus.com", "bus:invalidbus.com", null);
         } catch (BackplaneServerException bpe) {
             //expected
             return;
         }
 
         fail("Token requested with invalid scope should have failed");
 
     }
 
     @Test
     public void testMessagesPostEndPointPAL() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create source token for the channel
         TokenAnonymous token1 = new TokenAnonymous("", "", null);
         // override the random channel name for our test channel
         token1.put(TokenAnonymous.Field.CHANNEL.getFieldName(), "testchannel");
         this.saveToken(token1);
 
         // Create appropriate token
         TokenPrivileged token2 = new TokenPrivileged("clientFoo", "mybus.com yourbus.com", "bus:yourbus.com", null);
         this.saveToken(token2);
 
         // Make the call
         request.setRequestURI("/v2/messages");
         request.setMethod("POST");
         request.setParameter("access_token", token2.getIdValue());
         request.addHeader("Content-type", "application/json");
         //request.setContentType("application/json");
         //request.setParameter("messages", TEST_MSG);
         HashMap<String, Object> msgs = new HashMap<String, Object>();
         ArrayList msgsList = new ArrayList();
         msgsList.add(new ObjectMapper().readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {}));
         msgsList.add(new ObjectMapper().readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {}));
 
         msgs.put("messages", msgsList);
         String msgsString = new ObjectMapper().writeValueAsString(msgs);
         logger.info(msgsString);
         request.setContent(msgsString.getBytes());
 
         handlerAdapter.handle(request, response, controller);
 
         assertTrue(response.getStatus() == HttpServletResponse.SC_CREATED);
 
     }
 
     @Test
     public void testGrantAndRevoke() throws Exception {
 
         refreshRequestAndResponse();
 
         logger.info("TEST: testGrantAndRevoke() =================");
 
         // Create auth
         ArrayList<Grant> grants = new ArrayList<Grant>();
         Grant grant1 = new Grant("fakeOwnerId", testClient.getClientId(), "mybus.com");
         Grant grant2 = new Grant("fakeOwnerId", testClient.getClientId(), "thisbus.com");
         this.saveGrant(grant1);
         this.saveGrant(grant2);
         grants.add(grant1);
         grants.add(grant2);
 
         // Create appropriate token
         TokenPrivileged token = new TokenPrivileged(testClient.getClientId(), grants, "");
         grant1.addIssuedTokenId(token.getIdValue());
         daoFactory.getGrantDao().persist(grant1);
         grant2.addIssuedTokenId(token.getIdValue());
         daoFactory.getGrantDao().persist(grant2);
         saveToken(token);
 
         // quick validation
         Grant temp = daoFactory.getGrantDao().retrieveGrant(grant1.getIdValue());
         assertTrue(temp.isIssuedToken(token.getIdValue()));
 
         temp = daoFactory.getGrantDao().retrieveGrant(grant2.getIdValue());
         assertTrue(temp.isIssuedToken(token.getIdValue()));
 
         // Revoke token based on one code
         daoFactory.getTokenDao().revokeTokenByGrant(daoFactory.getGrantDao().retrieveGrant(grant1.getIdValue()));
 
         try {
             // Now the token should fail
             // Make the call
             request.setRequestURI("/v2/messages");
             request.setMethod("GET");
             request.setParameter("access_token", token.getIdValue());
             handlerAdapter.handle(request, response, controller);
             logger.info("testGrantAndRevoke() => " + response.getContentAsString());
 
             assertTrue(response.getContentAsString().contains(ERR_RESPONSE));
         } finally {
             daoFactory.getTokenDao().delete(token.getIdValue());
         }
 
     }
 
     @Test
     public void testAuthenticate() throws Exception {
 
         User user = new User();
         user.put(User.Field.USER.getFieldName(), ChannelUtil.randomString(20));
         user.put(User.Field.PWDHASH.getFieldName(), HmacHashUtils.hmacHash("foo"));
 
         BusConfig2 bus1 = new BusConfig2(ChannelUtil.randomString(30), user.getIdValue(), "100", "50000");
         BusConfig2 bus2 = new BusConfig2(ChannelUtil.randomString(30), user.getIdValue(), "100", "50000");
 
         try {
             daoFactory.getBusOwnerDAO().persist(user);
 
             // create a few buses
             daoFactory.getBusDao().persist(bus1);
             daoFactory.getBusDao().persist(bus2);
 
             refreshRequestAndResponse();
 
             // encode un:pw
             String credentials = testClient.getIdValue() + ":" + "secret";
             String encodedCredentials = new String(Base64.encode(credentials.getBytes()));
 
             logger.info("hit /authorize endpoint to get ball rolling");
             request.setRequestURI("/v2/authorize");
             request.setMethod("GET");
             request.setAuthType("BASIC");
             request.addParameter("redirect_uri", testClient.getRedirectUri());
             request.addParameter("response_type", "authorization_code");
             request.addParameter("client_id", testClient.getClientId());
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
             request.addParameter("busOwner", user.getIdValue());
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
             request.addParameter("redirect_uri", testClient.getRedirectUri());
             request.addParameter("response_type", "authorization_code");
             request.addParameter("client_id", testClient.getClientId());
             request.setCookies(new Cookie[]{authNCookie, authZCookie});
 
             request.addHeader("Authorization", "Basic " + encodedCredentials);
             mv = handlerAdapter.handle(request, response, controller);
             Map<String, Object> model = mv.getModel();
             String authKey = (String) model.get("auth_key");
 
             assertNotNull(authKey);
             logger.info("auth_key=" + authKey);
             logger.info("client_id=" + (String) model.get("client_id"));
             logger.info("redirect_uri=" + (String) model.get("redirect_uri"));
             logger.info("scope=" + (String) model.get("scope"));
 
             logger.info("should be redirect to authorize view => " + mv.getViewName());
 
             refreshRequestAndResponse();
             logger.info("post bus owner grant to /authorize endpoint");
             request.setRequestURI("/v2/authorize");
             request.setMethod("POST");
             request.setAuthType("BASIC");
             request.addParameter("redirect_uri", (String) model.get("redirect_uri"));
             request.addParameter("response_type", "authorization_code");
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
             request.setParameter("client_id", testClient.getClientId());
             request.setParameter("grant_type", "code");
             request.setParameter("code", code);
             request.setParameter("client_secret", "secret");
             request.setParameter("redirect_uri", testClient.get(Client.ClientField.REDIRECT_URI));
 
             handlerAdapter.handle(request, response, controller);
 
             logger.info("should be a token response => " + response.getContentAsString());
 
             Map<String,Object> returnedBody = new ObjectMapper().readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
             String tokenId = (String) returnedBody.get("access_token");
             assertNotNull(tokenId);
 
             Grant grant = daoFactory.getGrantDao().retrieveGrant(code);
             TokenPrivileged token = (TokenPrivileged) daoFactory.getTokenDao().retrieveToken(tokenId);
 
             assertTrue(grant.getGrantClientId().equals(token.getClientId()));
             assertTrue(grant.getBusOwnerId().equals(user.getIdValue()));
 
 
         } finally {
             daoFactory.getBusOwnerDAO().delete(user.getIdValue());
             daoFactory.getBusDao().delete(bus1.getIdValue());
             daoFactory.getBusDao().delete(bus2.getIdValue());
         }
 
     }
 
     @Test
     public void testMessageOrder() throws Exception {
 
         refreshRequestAndResponse();
 
         // Create appropriate token
         String bus = ChannelUtil.randomString(10) + ".com";
 
         TokenPrivileged token = new TokenPrivileged("fooClient", bus + " yourbus.com", "bus:" + bus, null);
         this.saveToken(token);
 
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> msg = mapper.readValue(TEST_MSG, new TypeReference<Map<String,Object>>() {});
 
         // seed messages
         int numMessages = 10;
         ArrayList<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
         String channel = ChannelUtil.randomString(TokenAnonymous.CHANNEL_NAME_LENGTH);
 
         for (int i=0;i < numMessages; i++) {
             BackplaneMessage message = new BackplaneMessage(BackplaneMessage.generateMessageId(), bus, channel, msg);
             messages.add(message);
         }
 
         // reverse the list
         Collections.reverse(messages);
 
         for (BackplaneMessage message : messages) {
             this.saveMessage(message);
         }
 
          // Make the call
         request.setRequestURI("/v2/messages");
         request.setMethod("GET");
         request.setParameter("access_token", token.getIdValue());
         handlerAdapter.handle(request, response, controller);
         logger.info("testMessageOrder()  => " + response.getContentAsString());
 
         Map<String,Object> returnedBody = mapper.readValue(response.getContentAsString(), new TypeReference<Map<String,Object>>() {});
         List<Map<String,Object>> returnedMsgs = (List<Map<String, Object>>) returnedBody.get("messages");
         assertTrue(returnedMsgs.size() == numMessages);
 
         // they should be returned in lexicographic order by ID
         String prev = "";
         for (Map<String,Object> m : returnedMsgs) {
             assertTrue(m.get("messageURL").toString().compareTo(prev) > 0);
             prev = (String)m.get("messageURL");
         }
 
     }
 
 
 }
