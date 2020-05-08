 /*******************************************************************************
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import iTests.framework.utils.LogUtils;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * byon kitchen-sink test
  * 
  * @author adaml
  *
  */
 public class ByonKitchenSinkTest extends AbstractByonCloudTest {
 	
 	
 
 	private static final String KEY_FILE_NAME = "testkey.pem";
 	private static final String SERVICE_NAME = "simpleByonKitchensink";
 
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void kitchenSinkTest() throws Exception {
 		
 		installServiceAndWait(getServicePath(SERVICE_NAME), SERVICE_NAME);
 		
 		LogUtils.log("asserting key file not copied to agent machines.");
 		assertMachineKeyFileNotCopied();
 		
 		checkIsLocalcloudServiceContext();
 		
 	}
 	
     private void assertMachineKeyFileNotCopied() throws IOException, InterruptedException {
     	String filesList = invokeListRemoteFilesCommand();
     	Assert.assertTrue("list of remote files contains the key file name.", 
     			!filesList.contains(KEY_FILE_NAME));
 	}
     
 	private String invokeListRemoteFilesCommand() throws IOException, InterruptedException {
 		final String invokeCommand = "connect " + this.getRestUrl() + ";" 
     			+ " invoke " + SERVICE_NAME + " listRemoteFiles";
 		LogUtils.log("invoking command: " + invokeCommand);
     	final String output = CommandTestUtils.runCommandAndWait(invokeCommand);
     	LogUtils.log("command output was: " + output);
     	assertTrue("invocation failed", output.contains("invocation completed successfully."));
     	return output;
 	}
 	
 	private void checkIsLocalcloudServiceContext() throws IOException, InterruptedException {
 		final String invokeResult = CommandTestUtils.runCommandAndWait("connect "
 				+ this.getRestUrl()
				+ "; invoke " + SERVICE_NAME + " isLocalcloud");
 		assertTrue("invocation output did not contain expected string 'isLocalcloud=true'. output:" + invokeResult , invokeResult.contains("isLocalcloud=false"));
 	}
 
 	protected String getServicePath(final String serviceName) {
     	return CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + serviceName);
     }
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		getService().setSudo(false);
 		getService().getProperties().put("keyFile", KEY_FILE_NAME);
 	}
 	
 	@Override
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		uninstallServiceIfFound(SERVICE_NAME);
 		super.teardown();
 	}
 }
