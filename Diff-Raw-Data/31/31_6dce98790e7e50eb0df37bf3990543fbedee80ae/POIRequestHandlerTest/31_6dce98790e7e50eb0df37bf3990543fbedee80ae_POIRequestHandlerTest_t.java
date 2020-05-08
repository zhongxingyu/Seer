 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.tests.unit.handler;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import com.google.gson.JsonObject;
import com.mymed.controller.core.exception.IOBackEndException;
import com.mymed.controller.core.exception.InternalBackEndException;
import com.mymed.controller.core.manager.profile.ProfileManager;
import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.requesthandler.POIRequestHandler;
 import com.mymed.tests.unit.handler.utils.BackendAssert;
 import com.mymed.tests.unit.handler.utils.TestUtils;
 
 /**
  * Test for the {@link POIRequestHandler} handler.
  * <p>
  * This test is parameterized.
  * 
  * @author Milo Casagrande
  * 
  */
 @RunWith(Parameterized.class)
 public class POIRequestHandlerTest extends GeneralHandlerTest {
 
   private static final String HANDLER_NAME = "POIRequestHandler";
   // Maximum number of tests
   private static final int TESTS_NUMBER = 200;
   private String accessToken = null; // For null testing
 
   // Parameters used for the tests
   private final String app;
   private final String latitude;
   private final String longitude;
   private final String value;
   private final String radius;
 
   /**
    * Constructor for the parameterized test.
    * 
    * @param app
    *          the application name
    * @param latitude
    *          the latitude of the POI
    * @param longitude
    *          the longitude of the POI
    * @param value
    *          a fictional value
    * @param radius
    *          the radius of the search
    */
   public POIRequestHandlerTest(final String app, final String latitude, final String longitude, final String value,
       final String radius) {
     super();
 
     this.app = app;
     this.latitude = latitude;
     this.longitude = longitude;
     this.value = value;
     this.radius = radius;
   }
 
   @Parameters
   public static Collection<Object[]> config() {
     // Number of tests, and number of parameters in the constructor
     final Object[][] data = new Object[TESTS_NUMBER][5];
 
     for (int i = 0; i < TESTS_NUMBER; i++) {
       final String app = "application" + i;
       final String latitude = "12.34.5" + i;
       final String longitude = "12.30.4" + i;
       final String value = "value" + i;
       final int radius = 250 * (i % 100 + 2);
 
       data[i][0] = app;
       data[i][1] = latitude;
       data[i][2] = longitude;
       data[i][3] = value;
       data[i][4] = String.valueOf(radius);
     }
 
     return Arrays.asList(data);
   }
 
   @Before
  public void setUpBefore() throws InternalBackEndException, IOBackEndException {
     accessToken = TestUtils.createAccessToken();

    accessToken = TestUtils.createAccessToken();

    final ProfileManager manager = new ProfileManager();
    manager.create(TestUtils.createUserBean(""));

    final SessionManager session = new SessionManager();
    session.create(TestUtils.createSessionBean(accessToken));
  }

  @After
  public void cleanAfter() throws InternalBackEndException {
    final ProfileManager manager = new ProfileManager();
    final SessionManager session = new SessionManager();

    try {
      manager.delete(TestUtils.MYMED_ID);
      session.delete(accessToken);
    } catch (final IOBackEndException ex) {
      // Do nothing, even if we have an error deleting something
    }
   }
 
   @BeforeClass
   public static void setUpBeforeClass() {
     path = TestUtils.createPath(HANDLER_NAME);
   }
 
   @Test
   public void multipleCreateTest() throws URISyntaxException, ClientProtocolException, IOException {
     final JsonObject user = TestUtils.createUserJson();
 
     TestUtils.addParameter(params, PARAM_CODE, CREATE);
     TestUtils.addParameter(params, PARAM_ACCESS_TOKEN, accessToken);
     TestUtils.addParameter(params, PARAM_APPLICATION, app);
     TestUtils.addParameter(params, PARAM_TYPE, "type");
     TestUtils.addParameter(params, PARAM_USER, user.toString());
     TestUtils.addParameter(params, PARAM_LONGITUDE, longitude);
     TestUtils.addParameter(params, PARAM_LATITUDE, latitude);
     TestUtils.addParameter(params, PARAM_VALUE, value);
 
     final String query = TestUtils.createQueryParams(params);
     final URI uri = TestUtils.createUri(path, query);
 
     final HttpPost postRequest = new HttpPost(uri);
     final HttpResponse response = client.execute(postRequest);
 
     BackendAssert.assertResponseCodeIs(response, 200);
     BackendAssert.assertIsValidJson(response);
   }
 
   @Test
   public void multipleReadTest() throws URISyntaxException, ClientProtocolException, IOException {
     TestUtils.addParameter(params, PARAM_CODE, READ);
     TestUtils.addParameter(params, PARAM_ACCESS_TOKEN, accessToken);
     TestUtils.addParameter(params, PARAM_APPLICATION, app);
     TestUtils.addParameter(params, PARAM_TYPE, "type");
     TestUtils.addParameter(params, PARAM_LONGITUDE, longitude);
     TestUtils.addParameter(params, PARAM_LATITUDE, latitude);
     TestUtils.addParameter(params, PARAM_RADIUS, radius);
 
     final String getQuery = TestUtils.createQueryParams(params);
     final URI getUri = TestUtils.createUri(path, getQuery);
 
     final HttpGet getRequest = new HttpGet(getUri);
     final HttpResponse getResponse = client.execute(getRequest);
 
     BackendAssert.assertResponseCodeIs(getResponse, 200);
     BackendAssert.assertIsValidPOIJson(getResponse);
   }
 }
