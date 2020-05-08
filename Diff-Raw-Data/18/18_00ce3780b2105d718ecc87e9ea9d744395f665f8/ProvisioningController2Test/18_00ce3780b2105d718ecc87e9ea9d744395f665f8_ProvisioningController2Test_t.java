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
 
 package com.janrain.backplane2.server.provision;
 
 import com.janrain.backplane.server.config.User;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.backplane2.server.config.Client;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.crypto.ChannelUtil;
 import com.janrain.crypto.HmacHashUtils;
 import org.apache.log4j.Logger;
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
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Tom Raney
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring/app-config.xml", "classpath:/spring/mvc-config.xml" })
 public class ProvisioningController2Test {
 
     @Inject
 	private ApplicationContext applicationContext;
 
     @Inject
 	private ProvisioningController2 controller;
 
     @Inject
     private SuperSimpleDB superSimpleDB;
 
     @Inject
     private Backplane2Config bpConfig;
 
     @Inject
     private DaoFactory daoFactory;
 
     private MockHttpServletRequest request;
 	private MockHttpServletResponse response;
     private HandlerAdapter handlerAdapter;
 
     private static final Logger logger = Logger.getLogger(ProvisioningController2Test.class);
     private User user;
     private Client client;
     private String pw;
 
     /**
 	 * Initialize before every individual test method
 	 */
 	@Before
 	public void init() throws SimpleDBException {
 
         handlerAdapter = applicationContext.getBean("handlerAdapter", HandlerAdapter.class);
 		refreshRequestAndResponse();
 
         // create temporary admin user account to enable the tests to work
         user = new User();
         pw = ChannelUtil.randomString(10);
         user.put(User.Field.USER.getFieldName(), ChannelUtil.randomString(20));
         user.put(User.Field.PWDHASH.getFieldName(), HmacHashUtils.hmacHash(pw));
 
         superSimpleDB.store(bpConfig.getTableName(Backplane2Config.SimpleDBTables.BP_ADMIN_AUTH), User.class, user);
 
         client = new Client( ChannelUtil.randomString(20), pw, "http://source.com", "http://redirect.com" );
         daoFactory.getClientDAO().persist(client);
         logger.info("Created test client: " + client.getClientId());
 
 
 	}
 
     @After
     public void cleanup() throws SimpleDBException {
         superSimpleDB.delete(bpConfig.getTableName(Backplane2Config.SimpleDBTables.BP_ADMIN_AUTH), user.getIdValue());
         superSimpleDB.delete(bpConfig.getTableName(Backplane2Config.SimpleDBTables.BP_CLIENTS), client.getIdValue());
         daoFactory.getClientDAO().delete(client.getClientId());
     }
 
     @Test
     public void testClientList() throws Exception {
 
         refreshRequestAndResponse();
 
         // create client
         String jsonUpdateClient = "{ \"admin\": \"" + user.get(User.Field.USER) + "\", \"secret\": \"" + pw + "\"," +
                 " \"configs\": [ { \"USER\":\"" + client.getClientId() + "\", \"PWDHASH\":\"" + pw + "\"} ] }";
         logger.info("passing in json " + jsonUpdateClient);
         request.setContent(jsonUpdateClient.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/client/update");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testClientUpdate -> " + response.getContentAsString());
         assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
 
         refreshRequestAndResponse();
 
         String queryJson = "{ \"admin\": \"" + user.get(User.Field.USER) + "\", \"secret\": \"" + pw + "\", \"entities\": [] }";
         logger.info("passing in json " + queryJson);
         request.setContent(queryJson.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/client/list");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testClientList() => " + response.getContentAsString());
         assertTrue(response.getContentAsString().contains(client.getClientId()));
 
     }
 
     @Test
     public void testProvisioningDelete() throws Exception {
 
         refreshRequestAndResponse();
 
         String delete = "{ \"entities\":[\"does\", \"not\", \"exist\"], \"admin\":\"" + user.get(User.Field.USER) + "\", \"secret\":\"" + pw + "\"}";
         request.setContent(delete.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/user/delete");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testProvisioningDelete() -> " + response.getContentAsString());
         assertTrue(response.getContentAsString().equals("{\"does\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"not\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"exist\":\"BACKPLANE_ENTRY_NOT_FOUND\"}"));
 
         refreshRequestAndResponse();
         request.setContent(delete.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/bus/delete");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testProvisioningDelete() -> " + response.getContentAsString());
         assertTrue(response.getContentAsString().equals("{\"does\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"not\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"exist\":\"BACKPLANE_ENTRY_NOT_FOUND\"}"));
 
         refreshRequestAndResponse();
         request.setContent(delete.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/client/delete");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testProvisioningDelete() -> " + response.getContentAsString());
         assertTrue(response.getContentAsString().equals("{\"does\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"not\":\"BACKPLANE_ENTRY_NOT_FOUND\",\"exist\":\"BACKPLANE_ENTRY_NOT_FOUND\"}"));
 
     }
 
     @Test
     public void testProvisioningGrant() throws Exception {
 
         refreshRequestAndResponse();
 
         String addGrant = "{\"grants\":{\"" + client.getClientId() + "\":\"qa-test-bus\"},\"admin\":\"" + user.get(User.Field.USER) + "\", \"secret\":\"" + pw + "\"}";
         request.setContent(addGrant.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/grant/add");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testProvisioningGrant() -> " + response.getContentAsString());
        assertTrue("Invalid response", response.getContentAsString().equals("{\"" + client.getClientId() + "\":\"GRANT_UPDATE_SUCCESS\"}"));
 
         refreshRequestAndResponse();
         request.setContent(addGrant.getBytes());
         request.addHeader("Content-type", "application/json");
         request.setRequestURI("/v2/provision/grant/revoke");
         request.setMethod("POST");
 
         handlerAdapter.handle(request, response, controller);
         logger.info("testProvisioningGrant() -> " + response.getContentAsString());
        assertTrue(response.getContentAsString().contains("GRANT_UPDATE_SUCCESS"));
 
 
     }
 
     private void refreshRequestAndResponse() {
 		request = new MockHttpServletRequest();
 		response = new MockHttpServletResponse();
 	}
 
 }
