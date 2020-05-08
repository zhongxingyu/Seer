 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 public class ParameterServiceTest extends RestBaseTest {
 	
 	ParameterService parameterService = new ParameterService();
 	
 	public ParameterServiceTest() {
 		super();
 	}
 	
 	@Test
 	public void getDynamicParameter() {
 		Response responsePackage = parameterService.getParameter(appDirName, "", "package", "package", userId, customerId, "", "", "");
 		Assert.assertEquals(200, responsePackage.getStatus());
 		Response responseFunctional = parameterService.getParameter(appDirName,"", "functional-test", "", userId, customerId, "", "", "");
 		Assert.assertEquals(200, responseFunctional.getStatus());
 	}
 	
 	@Test
 	public void getDynamicParamsFail() {
 		Response responseFailure = parameterService.getParameter(appDirName, "","deploy", "", "", customerId, "", "", "");
 		Assert.assertEquals(200, responseFailure.getStatus());
 	}
 	
 	
 	@Test
 	public void getParamsFile() {
 		Response response = parameterService.getFileAsString(appDirName, "deploy", "");
 		Assert.assertEquals(200, response.getStatus());
 		Response responseFailure = parameterService.getFileAsString(appDirName, "pdf-", "");
 		Assert.assertEquals(400, responseFailure.getStatus());
 	}
 
 	 @Test
 	public void updateWatcher() {
 		Response updateWatcher = parameterService.updateWatcher(appDirName, "package", "showSettings", "true", "");
 		Assert.assertEquals(200, updateWatcher.getStatus());
 //		Response updateWatcherFail = parameterService.updateWatcher(appDirName, "", "showSettings", "");
 //		Assert.assertEquals(400, updateWatcherFail.getStatus());
 		
 	}
 
 	 @Test
 	public void dependency() {
 		Response dependency = parameterService.getDependencyPossibleValue(appDirName, customerId, userId, "package", "environmentName", "package", "");
 		Assert.assertEquals(200, dependency.getStatus());
 	}
 	 
 	 @Test
 	 public void getSonarUrl() {
 			MockHttpServletRequest request = new MockHttpServletRequest();
			Response sonarUrl = parameterService.getSonarUrl(request,"");
 			Assert.assertEquals(200, sonarUrl.getStatus());
 	 }
 }
