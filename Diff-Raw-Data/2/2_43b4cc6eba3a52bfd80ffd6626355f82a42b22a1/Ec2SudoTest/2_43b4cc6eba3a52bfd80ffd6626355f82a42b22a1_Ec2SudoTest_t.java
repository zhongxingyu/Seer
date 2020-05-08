 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
 
 package test.cli.cloudify.cloud.ec2;
 
 import java.io.IOException;
 
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.AbstractExamplesTest;
 
 
 public class Ec2SudoTest extends AbstractExamplesTest {
 
 	final private String serviceName = "groovy";
 	final private String RECIPE_DIR_PATH = CommandTestUtils
 			.getPath("apps/USM/usm/groovySudo");
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Test
 	public void testSudo() throws IOException, InterruptedException {
 		installServiceAndWait(RECIPE_DIR_PATH, "groovy");
 		String invokeResult = CommandTestUtils.runCommandAndWait("connect " + getRestUrl()
 				+ "; invoke groovy sudo");
 		assertTrue("Could not find expected output ('OK') in custom command response", invokeResult.contains("OK"));
		assertTrue("Could not find expected output ('marker.txt') in custom command response", invokeResult.contains("marker.txt"));
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 	
 	@AfterMethod
 	public void cleanUp() {
 		try {
 			super.uninstallServiceAndWait(serviceName);
 		} catch (Exception e) {
 			AssertFail("Failed to uninstall application " + serviceName + " in the aftertest method", e);
 		}
 		super.scanNodesLeak();
 	}
 }
