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
 package test.cli.cloudify.cloud.services.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.dsl.cloud.CloudTemplate;
 
 import test.AbstractTest;
 import test.cli.cloudify.cloud.services.AbstractCloudService;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 import framework.utils.SSHUtils;
 
 public class ByonCloudService extends AbstractCloudService {
 
 	private static final String BYON_CLOUD_NAME = "byon";
 	public static final String BYON_CLOUD_USER= "tgrid";
 	public static final String BYON_CLOUD_PASSWORD = "tgrid";
 	
 	public static final String IP_LIST_PROPERTY = "ipList";
 	
 	protected static final String NEW_URL_PREFIX = "http://tarzan/builds/GigaSpacesBuilds/cloudify";
 	
	private static final String DEFAULT_MACHINES = "192.168.9.115,192.168.9.116,192.168.9.118,192.168.9.120,192.168.9.135,192.168.9.136,192.168.9.137";
 	
 	public static final String ENV_VARIABLE_NAME = "GIGASPACES_TEST_ENV";
 	public static final String ENV_VARIABLE_VALUE = "DEFAULT_ENV_VARIABLE";
 	
 	/**
 	 * this folder is where Cloudify will be downloaded to and extracted from. NOTE - this is not the WORKING_HOME_DIRECTORY.
 	 * if is also defined in the custom bootstrap-management.sh script we use in our tests. 
 	 */
 	public static final String BYON_HOME_FOLDER = "/tmp/byon";
 	
 	private String ipList;
 	private String[] machines;
 
 	public ByonCloudService(String uniqueName) {
 		super(uniqueName, BYON_CLOUD_NAME);
 		this.ipList = System.getProperty(IP_LIST_PROPERTY, DEFAULT_MACHINES);
 		this.machines = ipList.split(",");
 	}
 	
 	public void setIpList(String ipList) {
 		this.ipList = ipList;
 	}
 	
 	public String getIpList() {
 		return ipList;
 	}
 	
 	public String[] getMachines() {
 		return machines;
 	}
 	
 	public void setMachines(final String[] machines) {
 		this.machines = machines;
 	}
 
 	@Override
 	public void injectServiceAuthenticationDetails() throws IOException {	
 		getProperties().put("username", '"' + BYON_CLOUD_USER+  '"');
 		getProperties().put("password", '"' + BYON_CLOUD_PASSWORD +  '"');
 		
 		Map<String, String> propsToReplace = new HashMap<String,String>();
 		propsToReplace.put("cloudify_agent_", this.machinePrefix + "cloudify-agent");
 		propsToReplace.put("cloudify_manager", this.machinePrefix + "cloudify-manager");
 		propsToReplace.put("// cloudifyUrl", "cloudifyUrl");
 		if (StringUtils.isNotBlank(ipList)) {
 			propsToReplace.put("0.0.0.0", ipList);
 		}
 		
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "  + numberOfManagementMachines);
 		this.getAdditionalPropsToReplace().putAll(propsToReplace);
 
 		replaceCloudifyURL();
 		replaceBootstrapManagementScript();
 		
 		// byon-cloud.groovy in SGTest has this variable defined for cloud overrides tests.
 		// that why we need to add a properties file with a default value so that bootstrap will work 
 		// on other tests.
 		getProperties().setProperty("myEnvVariable", '"' + ENV_VARIABLE_VALUE + '"');
 	}
 
 	@Override
 	public void beforeBootstrap() throws Exception {
 		cleanMachines();
 	}
 	
 	private void replaceBootstrapManagementScript() {
 		// use a script that does not install java
 		File standardBootstrapManagement = new File(this.getPathToCloudFolder() + "/upload", "bootstrap-management.sh");
 		File customBootstrapManagement = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/byon/bootstrap-management.sh");
 		Map<File, File> filesToReplace = new HashMap<File, File>();
 		filesToReplace.put(standardBootstrapManagement, customBootstrapManagement);
 		this.addFilesToReplace(filesToReplace);
 	}
 
 	public void replaceCloudifyURL() throws IOException {
 		String buildNumber = PlatformVersion.getBuildNumber();
 		String version = PlatformVersion.getVersion();
 		String milestone = PlatformVersion.getMilestone();
 		
 		// TODO : replace hard coded 'cloudify' string with method to determine weather or no we are running xap or cloudify 
 
 		String newCloudifyURL = NEW_URL_PREFIX + "/" + version + "/build_" + buildNumber + "/cloudify/1.5/gigaspaces-cloudify-" + version + "-" + milestone + "-b" + buildNumber;
 		Map<String, String> propsToReplace = new HashMap<String, String>();
 		LogUtils.log("replacing cloudify url with : " + newCloudifyURL);
 		propsToReplace.put("cloudifyUrl \".+\"", "cloudifyUrl \"" + newCloudifyURL + '"');
 		this.getAdditionalPropsToReplace().putAll(propsToReplace);
 	}
 	
 	private void cleanMachines() {
 		killAllJavaOnAllHosts();
 		cleanGSFilesOnAllHosts();
 		cleanCloudifyTempDir();
 	}
 
 	private void cleanCloudifyTempDir() {
 		LogUtils.log(SSHUtils.runCommand(getMachines()[0], AbstractTest.OPERATION_TIMEOUT, "rm -rf /export/tgrid/.cloudify/", "tgrid", "tgrid"));
 		
 	}
 
 	private void cleanGSFilesOnAllHosts() {
 		for (CloudTemplate template : cloudConfiguration.getTemplates().values()) {
 			String command = "rm -rf " + template.getRemoteDirectory();;
 			String[] hosts = this.getMachines();			
 			for (String host : hosts) {
 				try {
 					LogUtils.log(SSHUtils.runCommand(host, AbstractTest.OPERATION_TIMEOUT, command, "tgrid", "tgrid"));
 				} catch (AssertionError e) {
 					LogUtils.log("Failed to clean files on host " + host + " .Reason --> " + e.getMessage());
 				}
 			}	
 		}			
 	}
 	
 	private void killAllJavaOnAllHosts() {
 		String command = "killall -9 java";
 		String[] hosts = this.getMachines();
 		for (String host : hosts) {
 			try {
 				LogUtils.log(SSHUtils.runCommand(host, AbstractTest.OPERATION_TIMEOUT, command, "tgrid", "tgrid"));
 			} catch (AssertionError e) {
 				LogUtils.log("Failed to kill java processes on host " + host + " .Reason --> " + e.getMessage());
 			}
 		}
 	}
 	
 
 	@Override
 	public String getUser() {
 		return BYON_CLOUD_USER;
 	}
 
 	@Override
 	public String getApiKey() {
 		return BYON_CLOUD_PASSWORD;
 	}
 
 }
