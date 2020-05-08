 /*******************************************************************************
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.SystemDefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.CloudifyErrorMessages;
 import org.cloudifysource.dsl.rest.response.Response;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.restclient.RestClientExecutor;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.Assert;
 import org.testng.annotations.Test;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 public class UnsupportedOperationErrorMsgTest extends AbstractLocalCloudTest {
 	
 	private static final String OPERATION_NAME = "getServiceStatus";
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void UnsupportedGetServiceStatusOperationTest() throws MalformedURLException {
 		
 		
 		DefaultHttpClient httpClient = new SystemDefaultHttpClient();
 		final HttpParams httpParams = httpClient.getParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, CloudifyConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(httpParams, CloudifyConstants.DEFAULT_HTTP_READ_TIMEOUT);
 		
 		RestClientExecutor restClientExecutor = new RestClientExecutor(httpClient, new URL(restUrl));
 		
 		try {
 			restClientExecutor.get(PlatformVersion.getVersion() + "/deployments/default/services/simple", new TypeReference<Response<Void>>() {});
 		} catch (RestClientException e) {
 			String messageCode = e.getMessageCode();
 			String expectedMessageCode = CloudifyErrorMessages.UNSUPPORTED_OPERATION.getName();
 			Assert.assertTrue("message code [" + messageCode + "] expected to be " + expectedMessageCode, 
 					expectedMessageCode.equals(messageCode));
 			String message = e.getMessageFormattedText();
 			Assert.assertTrue("Formatted message [" + message + "] does not to contain " + OPERATION_NAME, 
 					message.contains(OPERATION_NAME));
 		}
 
 	}
 }
