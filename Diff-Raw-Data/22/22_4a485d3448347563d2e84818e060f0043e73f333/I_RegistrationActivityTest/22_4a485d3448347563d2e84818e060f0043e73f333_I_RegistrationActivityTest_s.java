 /**
  * PHR_AndroidNative
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  */
 package com.photon.phresco.nativeapp.unit.test.testcases;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.photon.phresco.nativeapp.eshop.json.JSONHelper;
 import com.photon.phresco.nativeapp.eshop.logger.PhrescoLogger;
 import com.photon.phresco.nativeapp.unit.test.core.Constants;
 
 /**
  * @author chandankumar_r
  *
  */
 public class I_RegistrationActivityTest extends TestCase{
 	private static final String TAG = "I_RegistrationActivityTest  *********** ";
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() {
 	}
 
 	/**
 	 *  submit the registration form to web server
 	 *  and get response
 	 *
 	 */
 	@Test
	public final void testRegistraion() {
 		JSONObject jObjMain = new JSONObject();
 		JSONObject jObj = new JSONObject();
 
 		try {
 			PhrescoLogger.info(TAG + " testRegistraion -------------- START ");
 
 			jObj.put("firstName", "john");
 			jObj.put("lastName", "albert");
 			jObj.put("email", "john_albert@phresco.com");
 			jObj.put("phoneNumber", "");
 			jObj.put("password", "123");
 
 			jObjMain.put("register", jObj);
 
 			JSONObject responseJSON = null;
 			responseJSON=JSONHelper.postJSONObjectToURL(Constants.getWebContextURL() + Constants.getRestAPI() + Constants.REGISTER_POST_URL, jObjMain.toString());
 			assertNotNull("Login failed",responseJSON.length() > 0);
 
 			PhrescoLogger.info(TAG + " testRegistraion -------------- END ");
 
 		} catch (IOException ex) {
 			PhrescoLogger.info(TAG + " - testRegistraion  - IOException : " + ex.toString());
 			PhrescoLogger.warning(ex);
 		} catch (JSONException ex) {
 			PhrescoLogger.info(TAG + " - testRegistraion  - JSONException : " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 
 	}
 
 }
