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
 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.quality.iTests.framework.tools.SGTestHelper;
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.framework.utils.SSHUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.AbstractCloudService;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 public class ByonCloudService extends AbstractCloudService {
 
 	public static final String BYON_CLOUD_USER= "tgrid";
 	public static final String BYON_CLOUD_PASSWORD = "tgrid";
 	
 	public static final String IP_LIST_PROPERTY = "ipList";
 	
 	protected static final String NEW_URL_PREFIX = "http://tarzan/builds/GigaSpacesBuilds/cloudify";
 	protected static final String NEW_XAP_URL_PREFIX = "http://tarzan/builds/GigaSpacesBuilds";
 	
 	public static final String ENV_VARIABLE_NAME = "GIGASPACES_TEST_ENV";
 	public static final String ENV_VARIABLE_VALUE = "DEFAULT_ENV_VARIABLE";
 	
 	private boolean sudo;
 	
 	/**
 	 * this folder is where Cloudify will be downloaded to and extracted from. NOTE - this is not the WORKING_HOME_DIRECTORY.
 	 * if is also defined in the custom bootstrap-management.sh script we use in our tests. 
 	 */
 	public static final String BYON_HOME_FOLDER = "/tmp/byon";
 	
 	private String ipList;
 	private String[] machines;
 
 	public ByonCloudService() {
 		this("byon");
 	}
 
 	public ByonCloudService(final String name) {
 		super(name);
 		if (SGTestHelper.isDevMode()) {
 			this.sudo = false;
 			this.ipList = getIpListFromPropsFile();
 		} else {
 			this.ipList = System.getProperty(IP_LIST_PROPERTY);
 			this.sudo = true;
 		}
 		if (this.ipList == null || this.ipList.isEmpty()) {
 			throw new IllegalStateException("ipList is empty! , please populate ipList property in the byon.properties file with the machines you wish to use");
 		}
 		this.machines = ipList.split(",");
 	}
 	
 	public boolean isSudo() {
 		return sudo;
 	}
 
 	public void setSudo(boolean sudo) {
 		this.sudo = sudo;
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
 	public void injectCloudAuthenticationDetails() throws IOException {	
 		getProperties().put("username", BYON_CLOUD_USER);
 		getProperties().put("password", BYON_CLOUD_PASSWORD);
 		
 		Map<String, String> propsToReplace = new HashMap<String,String>();
 		propsToReplace.put("cloudify_agent_", getMachinePrefix() + "cloudify-agent");
 		propsToReplace.put("cloudify_manager", getMachinePrefix() + "cloudify-manager");
 		propsToReplace.put("// cloudifyUrl", "cloudifyUrl");
 		
 		if (!sudo) {
 			propsToReplace.put("privileged true", "privileged false");
 		}
 		
 		if (StringUtils.isNotBlank(ipList)) {
 			propsToReplace.put("0.0.0.0", ipList);
 		}
 		
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "  + getNumberOfManagementMachines());
		propsToReplace.put("\"org.cloudifysource.clearRemoteDirectoryOnStart\":\"false\"", "\"org.cloudifysource.clearRemoteDirectoryOnStart\":\"true\"");
 		propsToReplace.put("/tmp/gs-files", "/tmp/byon/gs-files");
 		this.getAdditionalPropsToReplace().putAll(propsToReplace);
 
 		replaceCloudifyURL();
 		replaceBootstrapManagementScript();
 		
 		// byon-cloud.groovy in SGTest has this variable defined for cloud overrides tests.
 		// that why we need to add a properties file with a default value so that bootstrap will work 
 		// on other tests.
 		getProperties().put("myEnvVariable", ENV_VARIABLE_VALUE);
 	}
 	
 	private void replaceBootstrapManagementScript() throws IOException {
 		// use a script that does not install java
 		File standardBootstrapManagement = new File(this.getPathToCloudFolder() + "/upload", "bootstrap-management.sh");
 		File customBootstrapManagement = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/cloud/byon/bootstrap-management.sh");
 		IOUtils.replaceFile(standardBootstrapManagement, customBootstrapManagement);
 	}
 
 	public void replaceCloudifyURL() throws IOException {
 		String buildNumber = PlatformVersion.getBuildNumber();
 		String version = PlatformVersion.getVersion();
 		String milestone = PlatformVersion.getMilestone();
 	
 		// TODO : replace hard coded 'cloudify' string with method to determine weather or no we are running xap or cloudify
 		String newCloudifyURL;
 		if(getBootstrapper().isNoWebServices()){
 			newCloudifyURL =NEW_XAP_URL_PREFIX+ "/" +version +"/build_" + buildNumber + "/xap-bigdata/1.5/gigaspaces-xap-premium-" + version + "-" + milestone + "-b" + buildNumber;
 		}
 		else {
 			newCloudifyURL = NEW_URL_PREFIX + "/" + version + "/build_" + buildNumber + "/cloudify/1.5/gigaspaces-cloudify-" + version + "-" + milestone + "-b" + buildNumber;
 		}
 		Map<String, String> propsToReplace = new HashMap<String, String>();
 		LogUtils.log("replacing cloudify url with : " + newCloudifyURL);
 		propsToReplace.put("cloudifyUrl \".+\"", "cloudifyUrl \"" + newCloudifyURL + '"');
 		this.getAdditionalPropsToReplace().putAll(propsToReplace);
 	}
 	
 	@Override
 	public String getUser() {
 		return BYON_CLOUD_USER;
 	}
 
 	@Override
 	public String getApiKey() {
 		return BYON_CLOUD_PASSWORD;
 	}
 	
 	@Override
 	public void beforeBootstrap() {
 		cleanMachines();
 	}
 	
 	private void cleanMachines() {
 		killAllJavaOnAllHosts();
 		cleanGSFilesOnAllHosts();
 		cleanCloudifyTempDir();
 	}
 	
 	private void cleanCloudifyTempDir() {
 		
 		String command = "rm -rf /export/tgrid/.cloudify/";
 		if (sudo) {
 			command = "sudo " + command;
 		}
 		
 		try {
 			LogUtils.log(SSHUtils.runCommand(this.getMachines()[0], AbstractTestSupport.OPERATION_TIMEOUT, command, "tgrid", "tgrid"));
 		} catch (AssertionError e) {
 			LogUtils.log("Failed to clean files .cloudify folder Reason --> " + e.getMessage());
 		}
 		
 	}
 
 	private void cleanGSFilesOnAllHosts() {
 		
 		String command = "rm -rf /tmp/byon/gs-files";
 		if (sudo) {
 			command = "sudo " + command;
 		}
 		
 		String[] hosts = this.getMachines();			
 		for (String host : hosts) {
 			try {
 				LogUtils.log(SSHUtils.runCommand(host, AbstractTestSupport.OPERATION_TIMEOUT, command, "tgrid", "tgrid"));
 			} catch (AssertionError e) {
 				LogUtils.log("Failed to clean files on host " + host + " .Reason --> " + e.getMessage());
 			}
 		}				
 	}
 	
 	private void killAllJavaOnAllHosts() {
 		
 		String command = "killall -9 java";
 		if (sudo) {
 			command = "sudo " + command;
 		}
 		
 		String[] hosts = this.getMachines();
 		for (String host : hosts) {
 			try {
 				LogUtils.log(SSHUtils.runCommand(host, AbstractTestSupport.OPERATION_TIMEOUT, command, "tgrid", "tgrid"));
 			} catch (AssertionError e) {
 				LogUtils.log("Failed to kill java processes on host " + host + " .Reason --> " + e.getMessage());
 			}
 		}
 	}
 
 	private String getIpListFromPropsFile() {
 		
 		File propsFile = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/cloud/byon/byon.properties");
 		Properties props;
 		try {
 			props = IOUtils.readPropertiesFromFile(propsFile);
 		} catch (final Exception e) {
 			throw new IllegalStateException("Failed reading properties file : " + e.getMessage());
 		}
 		return props.getProperty(IP_LIST_PROPERTY);
 	}
 }
