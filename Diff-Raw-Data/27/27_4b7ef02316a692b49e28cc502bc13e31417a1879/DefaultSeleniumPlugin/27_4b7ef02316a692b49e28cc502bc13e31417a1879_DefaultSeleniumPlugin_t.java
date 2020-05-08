 /**
  * Phresco Plugin Commons
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
 package com.photon.phresco.plugins;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.api.ExecutionStatus;
 import com.photon.phresco.plugins.api.SeleniumPlugin;
 import com.photon.phresco.plugins.impl.DefaultExecutionStatus;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.HubConfiguration;
 import com.photon.phresco.util.NodeCapability;
 import com.photon.phresco.util.NodeConfig;
 import com.photon.phresco.util.NodeConfiguration;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class DefaultSeleniumPlugin implements SeleniumPlugin {
 	
 	public Log log;
 
 	public DefaultSeleniumPlugin(Log log) {
 		this.log = log;
 	}
 
 	protected final Log getLog() {
 		return log;
 	}
 
 	public ExecutionStatus startHub(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		File baseDir = mavenProjectInfo.getBaseDir();
 		MavenProject project = mavenProjectInfo.getProject();
 		Map<String, String> configs = MojoUtil.getAllValues(configuration);
		String subModule = "";
 		File workingDir = baseDir;
		if (StringUtils.isNotEmpty(mavenProjectInfo.getModuleName())) {
			subModule = mavenProjectInfo.getModuleName();
 			workingDir = new File(baseDir + File.separator + subModule);
 		}
 		Integer port = Integer.parseInt(configs.get("port"));
 		int newSessionTimeout = 0;
 		if (StringUtils.isNotEmpty(configs.get("newSessionWaitTimeout"))) {
 		    newSessionTimeout = Integer.parseInt(configs.get("newSessionWaitTimeout"));
 		}
 		String servlets = configs.get("servlets");
 		String prioritizer = configs.get("prioritizer");
 		String capabilityMatcher = configs.get("capabilityMatcher");
 		boolean throwOnCapabilityNotPresent = Boolean.valueOf(configs.get("throwOnCapabilityNotPresent"));
 		int nodePolling = 0;
 		if (StringUtils.isNotEmpty(configs.get("nodePolling"))) {
 		     nodePolling = Integer.parseInt(configs.get("nodePolling"));
 		}
 		int cleanUpCycle = 0;
 		if (StringUtils.isNotEmpty(configs.get("cleanUpCycle"))) {
 		    cleanUpCycle = Integer.parseInt(configs.get("cleanUpCycle"));
 		}
 		int timeout = 0; 
 		if (StringUtils.isNotEmpty(configs.get("timeout"))) {
 		    timeout = Integer.parseInt(configs.get("timeout"));
 	    }
 		int browserTimeout = 0;
 		if (StringUtils.isNotEmpty(configs.get("browserTimeout"))) {
 		    browserTimeout = Integer.parseInt(configs.get("browserTimeout"));
 	    }
 		int maxSession = 0;
 		if (StringUtils.isNotEmpty(configs.get("maxSession"))) {
 		    maxSession = Integer.parseInt(configs.get("maxSession"));
 	    }
 		
 		try {
 			HubConfiguration hubConfig = new HubConfiguration();
 			InetAddress thisIp = InetAddress.getLocalHost();
 			hubConfig.setHost(thisIp.getHostAddress());
 			hubConfig.setPort(port);
 			hubConfig.setNewSessionWaitTimeout(newSessionTimeout);
 			PluginUtils pluginUtils = new PluginUtils();
 			hubConfig.setServlets(pluginUtils.csvToList(servlets));
 			if (StringUtils.isNotEmpty(prioritizer)) {
 				hubConfig.setPrioritizer(prioritizer);
 			}
 			hubConfig.setCapabilityMatcher(capabilityMatcher);
 			hubConfig.setThrowOnCapabilityNotPresent(throwOnCapabilityNotPresent);
 			hubConfig.setNodePolling(nodePolling);
 			hubConfig.setCleanUpCycle(cleanUpCycle);
 			hubConfig.setTimeout(timeout);
 			hubConfig.setBrowserTimeout(browserTimeout);
 			hubConfig.setMaxSession(maxSession);
 			
 			String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			File dotPhrescoDir = baseDir;
 			if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				dotPhrescoDir = new File(baseDir.getParent() +  File.separatorChar + dotPhrescoDirName);
 			}
 			dotPhrescoDir = new File(dotPhrescoDir.getPath() + File.separatorChar + subModule);
 			File pomFile = pluginUtils.getPomFile(dotPhrescoDir, workingDir);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String testDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			String srcDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR);
 			ApplicationInfo appInfo = pluginUtils.getAppInfo(dotPhrescoDir);
 			String appDirName = appInfo.getAppDirName();
 			File testDir = workingDir;
 			if (StringUtils.isNotEmpty(testDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + testDirName + File.separatorChar + subModule);
 			} else if (StringUtils.isNotEmpty(srcDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + srcDirName + File.separatorChar + subModule);
 			}
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			pluginUtils.updateHubConfigInfo(testDir, funcDir, hubConfig);
 			log.info("Starting the Hub...");
 			pluginUtils.startHub(workingDir, pomFile.getName(), subModule);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (UnknownHostException e) {
 			throw new PhrescoException(e);
 		}
 		return new DefaultExecutionStatus();
 	}
 
 	public ExecutionStatus stopHub(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			File baseDir = mavenProjectInfo.getBaseDir();
 			MavenProject project = mavenProjectInfo.getProject();
			String subModule = "";
 			PluginUtils pluginutil = new PluginUtils();
 			File workingDir = baseDir;
			if (StringUtils.isNotEmpty(mavenProjectInfo.getModuleName())) {
				subModule = mavenProjectInfo.getModuleName();
 				workingDir = new File(baseDir + File.separator + subModule);
 			}
 			String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			File dotPhrescoDir = baseDir;
 			if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				dotPhrescoDir = new File(baseDir.getParent() +  File.separatorChar + dotPhrescoDirName);
 			}
 			dotPhrescoDir = new File(dotPhrescoDir.getPath() + File.separatorChar + subModule);
 			File pomFile = pluginutil.getPomFile(dotPhrescoDir, workingDir);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			String testDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			String srcDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR);
 			ApplicationInfo appInfo = pluginutil.getAppInfo(dotPhrescoDir);
 			String appDirName = appInfo.getAppDirName();
 			File testDir = workingDir;
 			if (StringUtils.isNotEmpty(testDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + testDirName + File.separatorChar + subModule);
 			} else if (StringUtils.isNotEmpty(srcDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + srcDirName + File.separatorChar + subModule);
 			}
 			File configFile = new File(testDir + funcDir + File.separator + Constants.HUB_CONFIG_JSON);
 			Gson gson = new Gson();
 	        BufferedReader reader = new BufferedReader(new FileReader(configFile));
 	        HubConfiguration hubConfiguration = gson.fromJson(reader, HubConfiguration.class);
 	        int portNumber = hubConfiguration.getPort();
 			pluginutil.stopServer("" + portNumber, workingDir);
 			log.info("Hub Stopped Successfully...");
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 		    throw new PhrescoException(e);
 	    }
 		return new DefaultExecutionStatus();
 	}
 
 	public ExecutionStatus startNode(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		File baseDir = mavenProjectInfo.getBaseDir();
		String subModule = "";
 		MavenProject project = mavenProjectInfo.getProject();
 		PluginUtils pluginutil = new PluginUtils();
 		File workingDir = baseDir;
		if (StringUtils.isNotEmpty(mavenProjectInfo.getModuleName())) {
			subModule = mavenProjectInfo.getModuleName();
 			workingDir = new File(baseDir + File.separator + subModule);
 		}
 		Map<String, String> configs = MojoUtil.getAllValues(configuration);
 		String hubHost = configs.get("hubHost");
 		Integer maxSession = 0;
 		if (StringUtils.isNotEmpty(configs.get("maxSession"))) {
 		    maxSession = Integer.parseInt(configs.get("maxSession"));
 		}
 		String seleniumProtocol = configs.get("seleniumProtocol");
 		int nodeport = 0;
 		if (StringUtils.isNotEmpty(configs.get("nodeport"))) {
 		    nodeport = Integer.parseInt(configs.get("nodeport"));
 		}
 		boolean register = Boolean.valueOf(configs.get("register"));
 		int registerCycle = 0;
 		if (StringUtils.isNotEmpty(configs.get("registerCycle"))) {
 		    registerCycle = Integer.parseInt(configs.get("registerCycle"));
 		}
 		int hubPort = 0;
 		if (StringUtils.isNotEmpty(configs.get("hubPort"))) {
 		    hubPort = Integer.parseInt(configs.get("hubPort"));
 		}
 		String proxy = configs.get("proxy");
 		String browserInfo = configs.get("browserInfo");
 		
 		boolean connectionAlive = Utility.isConnectionAlive(Constants.DB_PROTOCOL, hubHost, hubPort);
 		if (!connectionAlive) {
 		    log.error("Hub not yet started...");
 		    throw new PhrescoException("Hub not yet started...");
 		}
 		try {
 			NodeConfiguration nodeConfiguration = new NodeConfiguration();
 			List<NodeCapability> nodeCapabilities = new ArrayList<NodeCapability>();
 			StringReader reader = new StringReader(browserInfo);
 			Properties props = new Properties();
 			props.load(reader); // properties read from the reader 
 			Set<String> propertyNames = props.stringPropertyNames();
 			for (String key : propertyNames) {
 			    NodeCapability nodeCapability = new NodeCapability();
 				nodeCapability.setBrowserName(key);
 				if (StringUtils.isNotEmpty(props.getProperty(key))) {
 				    nodeCapability.setMaxInstances(Integer.parseInt(props.getProperty(key)));
 				}
 				nodeCapabilities.add(nodeCapability);
 				nodeCapability.setSeleniumProtocol(seleniumProtocol);
 			}
 			nodeConfiguration.setCapabilities(nodeCapabilities);
 	
 			NodeConfig nodeConfig = new NodeConfig();
 			nodeConfig.setProxy(proxy);
 			nodeConfig.setMaxSession(maxSession);
 			nodeConfig.setPort(nodeport);
 			InetAddress thisIp = InetAddress.getLocalHost();
 			nodeConfig.setHost(thisIp.getHostAddress());
 			nodeConfig.setRegister(register);
 			nodeConfig.setRegisterCycle(registerCycle);
 			nodeConfig.setHubPort(hubPort);
 			nodeConfig.setHubHost(hubHost);
 			nodeConfiguration.setConfiguration(nodeConfig);
 			String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			File dotPhrescoDir = baseDir;
 			if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				dotPhrescoDir = new File(baseDir.getParent() +  File.separatorChar + dotPhrescoDirName);
 			}
 			dotPhrescoDir = new File(dotPhrescoDir.getPath() + File.separatorChar + subModule);
 			File pomFile = pluginutil.getPomFile(dotPhrescoDir, workingDir);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			String testDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			String srcDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR);
 			ApplicationInfo appInfo = pluginutil.getAppInfo(dotPhrescoDir);
 			String appDirName = appInfo.getAppDirName();
 			File testDir = workingDir;
 			if (StringUtils.isNotEmpty(testDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + testDirName + File.separatorChar + subModule);
 			} else if (StringUtils.isNotEmpty(srcDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + srcDirName + File.separatorChar + subModule);
 			}
 			pluginutil.updateNodeConfigInfo(testDir, funcDir, nodeConfiguration);
 			log.info("Starting the Node...");
 			pluginutil.startNode(workingDir, pomFile.getName(),subModule);
 		}  catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 		return new DefaultExecutionStatus();
 	}
 
 	public ExecutionStatus stopNode(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			File baseDir = mavenProjectInfo.getBaseDir();
			String subModule = "";
 			MavenProject project = mavenProjectInfo.getProject();
 			PluginUtils pluginutil = new PluginUtils();
 			File workingDir = baseDir;
			if (StringUtils.isNotEmpty(mavenProjectInfo.getModuleName())) {
				subModule = mavenProjectInfo.getModuleName();
 				workingDir = new File(baseDir + File.separator + subModule);
 			}
 			String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			File dotPhrescoDir = baseDir;
 			if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				dotPhrescoDir = new File(baseDir.getParent() +  File.separatorChar + dotPhrescoDirName);
 			}
 			dotPhrescoDir = new File(dotPhrescoDir.getPath() + File.separatorChar + subModule);
 			File pomFile = pluginutil.getPomFile(dotPhrescoDir, workingDir);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			String testDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			String srcDirName = processor.getProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR);
 			ApplicationInfo appInfo = pluginutil.getAppInfo(dotPhrescoDir);
 			String appDirName = appInfo.getAppDirName();
 			File testDir = workingDir;
 			if (StringUtils.isNotEmpty(testDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + testDirName + File.separatorChar + subModule);
 			} else if (StringUtils.isNotEmpty(srcDirName)) {
 				testDir = new File(Utility.getProjectHome() + File.separatorChar + appDirName + File.separatorChar + srcDirName + File.separatorChar + subModule);
 			}
 			File configFile = new File(testDir + funcDir + File.separator + Constants.NODE_CONFIG_JSON);
 			Gson gson = new Gson();
 			BufferedReader reader = new BufferedReader(new FileReader(configFile));
 	        NodeConfiguration nodeConfiguration = gson.fromJson(reader, NodeConfiguration.class);
 	        int portNumber = nodeConfiguration.getConfiguration().getPort();
 			pluginutil.stopServer("" + portNumber, workingDir);
 			log.info("Node Stopped Successfully...");
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 		    throw new PhrescoException(e);
 	    }
 		return new DefaultExecutionStatus();
 	}
 }
