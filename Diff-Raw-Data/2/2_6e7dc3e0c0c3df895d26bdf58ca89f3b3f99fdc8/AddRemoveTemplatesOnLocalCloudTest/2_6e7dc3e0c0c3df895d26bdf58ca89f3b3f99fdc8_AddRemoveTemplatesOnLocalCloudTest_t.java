  /* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplatesCommands;
 import org.junit.Assert;
 import org.testng.annotations.Test;
 
 /**
  * All templates commands expected to failed on local cloud.
  * @author yael
  *
  */
 public class AddRemoveTemplatesOnLocalCloudTest extends AbstractLocalCloudTest{
 	
	private static final String TEMPLATES_FOLDER_PATH = CommandTestUtils.getPath("src/main/resources/templates/SMALL_LINUX");
 	
 	private static final String TEMPLATE_NAME = "template";
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void addTemplatesOnLocalcloudTest() {
 		String cliOutput = TemplatesCommands.addTemplatesCLI(restUrl, TEMPLATES_FOLDER_PATH, true);
 		assertLocalcloudErrorMessage(cliOutput);
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void removeTemplateOnLocalcloudTest() {
 		String cliOutput = TemplatesCommands.removeTemplateCLI(restUrl, TEMPLATE_NAME, true);
 		assertLocalcloudErrorMessage(cliOutput);
 	}
 	
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void getTemplateOnLocalcloudTest() {
 		String cliOutput = TemplatesCommands.getTemplateCLI(restUrl, TEMPLATE_NAME, true);
 		assertLocalcloudErrorMessage(cliOutput);
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void listTemplateOnLocalcloudTest() {
 		String cliOutput = TemplatesCommands.listTemplatesCLI(restUrl, true);
 		assertLocalcloudErrorMessage(cliOutput);
 	}
 
 	private void assertLocalcloudErrorMessage(String cliOutput) {
 		CharSequence expectedErrMsg = "Local cloud does not support";
 		Assert.assertTrue("output should contain \"" + expectedErrMsg + "\" but output was: " + cliOutput , 
 				cliOutput.contains(expectedErrMsg));
 		String falseErrMsg = "failed to read response";
 		Assert.assertFalse("output should not contain \"" + falseErrMsg + "\" but output was: " + cliOutput , 
 				cliOutput.contains(falseErrMsg));
 		
 	}
 	
 	
 }
