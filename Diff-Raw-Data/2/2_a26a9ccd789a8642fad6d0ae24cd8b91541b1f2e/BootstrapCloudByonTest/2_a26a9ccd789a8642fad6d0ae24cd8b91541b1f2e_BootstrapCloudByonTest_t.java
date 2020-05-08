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
 ******************************************************************************/
 package test.cli.cloudify;
 
 import java.io.File;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 
 public class BootstrapCloudByonTest extends AbstractCloudByonTest {
 
 	/**
 	 * CLOUDIFY-128
 	 * cloud bootstrapping test 
 	 * @throws Exception 
 	 * @see AbstractCloudByonTest
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = false)
 	public void bootstrapByonCloudTest() throws Exception {
 	    
 		for (int i = 0; i < NUM_OF_MANAGEMENT_MACHINES; i++) {
 			// The rest home page is a JSP page, which will fail to compile if there is no JDK installed. So use testrest instead
 			assertWebServiceAvailable(new URL( restAdminUrl[i].toString() + "/service/testrest"));
 			assertWebServiceAvailable(webUIUrl[i]);
 		}
 	    
 		String connectCommand = "connect " + restAdminUrl[0].toString() + ";";
 	    
 	    URL machinesURL = getMachinesUrl(restAdminUrl[0].toString());
 	    assertEquals("Expecting " + NUM_OF_MANAGEMENT_MACHINES + " machines", 
 	    		NUM_OF_MANAGEMENT_MACHINES, getNumberOfMachines(machinesURL));
 	    
 	    //running install application on simple
 	    String installCommand = new StringBuilder()
 	        .append("install-application ")
 	        .append("--verbose ")
 	        .append("-timeout ")
 	        .append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
	        .append((new File(ScriptUtils.getBuildPath(), "recipes/apps/travel").toString()).replace('\\', '/'))
 	        .toString();
 	    
 	    String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 	    
 	    Assert.assertTrue(output.contains(INSTALL_TRAVEL_EXPECTED_OUTPUT));
 
 	    // Travel is started with 2 instances (1 tomcat, 1 cassandra) so we are expecting two more machine
         assertEquals("Expecting " + (NUM_OF_MANAGEMENT_MACHINES+2) + " machines", 
                 NUM_OF_MANAGEMENT_MACHINES+2, getNumberOfMachines(machinesURL));
 	    
 	    
 	    //uninstall simple application
 	    String uninstallCommand = "uninstall-application --verbose travel";
 	    output = CommandTestUtils.runCommandAndWait(connectCommand + uninstallCommand);
 	    
 	    Assert.assertTrue(output.contains(UNINSTALL_TRAVEL_EXPECTED_OUTPUT));
 	    
 	}
 	
 	private static void assertWebServiceAvailable(final URL url) {
         repetitiveAssertTrue(url + " is not up", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 try {
                     return WebUtils.isURLAvailable(url);
                 } catch (Exception e) {
                     return false;
                 }
             }
         }, OPERATION_TIMEOUT);	    
 	}
 	
     private static int getNumberOfMachines(URL machinesRestAdminUrl) throws Exception {
         String json = WebUtils.getURLContent(machinesRestAdminUrl);
         Matcher matcher = Pattern.compile("\"Size\":\"([0-9]+)\"").matcher(json);
         if (matcher.find()) {
             String rawSize = matcher.group(1);
             int size = Integer.parseInt(rawSize);
             return size;
         } else {
             return 0;
         }
     }
 	
     private static String stripSlash(String str) {
         if (str == null || !str.endsWith("/")) {
             return str;
         }
         return str.substring(0, str.length()-1);
     }
     
     private static URL getMachinesUrl(String url) throws Exception {
         return new URL(stripSlash(url) + "/admin/machines");
     }
     
 }
