 package com.photon.phresco.plugins;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.google.gson.Gson;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.api.PhrescoPlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.HubConfiguration;
 import com.photon.phresco.util.NodeCapability;
 import com.photon.phresco.util.NodeConfig;
 import com.photon.phresco.util.NodeConfiguration;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class PhrescoBasePlugin implements PhrescoPlugin, PluginConstants {
 
 	public Log log;
 
 	public PhrescoBasePlugin(Log log) {
 		this.log = log;
 	}
 
 	protected final Log getLog() {
 		return log;
 	}
 
 	public void runUnitTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		generateMavenCommand(mavenProjectInfo, Constants.POM_PROP_KEY_UNITTEST_DIR);
 	}
 
 	public void runFunctionalTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		MavenProject project = mavenProjectInfo.getProject();
 		String basedir = project.getBasedir().getPath();
 		Map<String, String> configValues = MojoUtil.getAllValues(configuration);
 		String environmentName = configValues.get(ENVIRONMENT_NAME);
 		String testAgainst = configValues.get(TEST_AGAINST);
 		if(testAgainst.equals(BUILD)) {
                 environmentName = configValues.get(BUILD_ENVIRONMENT_NAME);
         } else if (testAgainst.equals(SERVER)) {
                 environmentName = configValues.get(ENVIRONMENT_NAME);
         }
 		String browserValue = configValues.get(BROWSER);
 		String resolutionValue = configValues.get(RESOLUTION);
 		File selectedEnvFile = new File(basedir + File.separator + DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE);
 		String resultConfigFileDir = project.getProperties().getProperty(Constants.PHRESCO_FUNCTIONAL_TEST_ADAPT_DIR);
 		if(StringUtils.isNotEmpty(resultConfigFileDir)) {
 		File resultConfigXml = new File(basedir + resultConfigFileDir);
 		adaptTestConfig(selectedEnvFile, resultConfigXml, environmentName, browserValue, resolutionValue);
 		}
 		generateMavenCommand(mavenProjectInfo, Constants.POM_PROP_KEY_FUNCTEST_DIR);
 	}
 
 	public void runPerformanceTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void runLoadTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			PluginUtils pluginUtils = new PluginUtils();
 			MavenProject project = mavenProjectInfo.getProject();
 			String basedir = project.getBasedir().getPath();
 			Map<String, String> configs = MojoUtil.getAllValues(configuration);
			String testAgainstType = configs.get("testAgainst");
 			String testName = configs.get(KEY_TEST_NAME);
 			String environmentName = configs.get(ENVIRONMENT_NAME);
 			String type = configs.get(testAgainstType);
 			String noOfUsers = configs.get(KEY_NO_OF_USERS);
 			String rampUpPeriod = configs.get(KEY_RAMP_UP_PERIOD);
 			String loopCount = configs.get(KEY_LOOP_COUNT);
 			String str = configs.get(ADD_HEADER);
 			StringReader reader = new StringReader(str);
 			Properties props = new Properties();
 			props.load(reader);
 			Set<String> propertyNames = props.stringPropertyNames();
 			Map<String, String> headersMap = new HashMap<String, String>(2);
 			for (String key : propertyNames) {
 				headersMap.put(key, props.getProperty(key));
 			}
 			ConfigManager configManager = new ConfigManagerImpl(new File(basedir + File.separator + DOT_PHRESCO_FOLDER + File.separator + CONFIG_FILE));
 			com.photon.phresco.configuration.Configuration config = configManager.getConfiguration(environmentName, testAgainstType, type);
 			String performancePath = project.getProperties().getProperty(Constants.POM_PROP_KEY_LOADTEST_DIR);
 			if(StringUtils.isNotEmpty(performancePath)) {
 				pluginUtils.changeTestName(basedir + File.separator + performancePath + File.separator, testName);
 			}
 			String testConfigFilePath = basedir + File.separator + performancePath + File.separator + "tests";
 			pluginUtils.adaptTestConfig(testConfigFilePath + File.separator , config);
 			pluginUtils.adaptLoadJmx(testConfigFilePath + File.separator, Integer.parseInt(noOfUsers), Integer.parseInt(rampUpPeriod), Integer.parseInt(loopCount), headersMap);
 			generateMavenCommand(mavenProjectInfo, Constants.POM_PROP_KEY_LOADTEST_DIR);
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public void validate(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append(SONAR_COMMAND);
 			Map<String, String> config = MojoUtil.getAllValues(configuration);
 			MavenProject project = mavenProjectInfo.getProject();
 			String baseDir = project.getBasedir().getPath();
 			Commandline commandline = new Commandline(sb.toString());
 			String value = config.get(SONAR);
 			String string = config.get(value);
 			if(string != null) {
 				List<Parameter> parameters = configuration.getParameters().getParameter();
 				for (Parameter parameter : parameters) {
 					if (parameter.getPluginParameter() != null && parameter.getPluginParameter().equals(PLUGIN_PARAMETER)) {
 						List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
 						for (MavenCommand mavenCommand : mavenCommands) {
 							if (parameter.getValue().equals(value) || mavenCommand.getKey().equals(string)) {
 								String mavenCommandValue = mavenCommand.getValue();
 								sb.append(STR_SPACE);
 								sb.append(mavenCommandValue);
 								commandline = new Commandline(sb.toString());
 							}
 						}
 					}
 				}
 			}
 
 			if(value.equals("functional")) {
 				String workingDir = project.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 				sb.append(STR_SPACE);
 				sb.append("-Dsonar.branch=functional");
 				commandline = new Commandline(sb.toString());
 				if (StringUtils.isNotEmpty(workingDir)) {
 					commandline.setWorkingDirectory(baseDir + workingDir);
 				}
 			}
 			Process pb = commandline.execute();
 			// Consume subprocess output and write to stdout for debugging
 			InputStream is = new BufferedInputStream(pb.getInputStream());
 			int singleByte = 0;
 			while ((singleByte = is.read()) != -1) {
 				//output.write(buffer, 0, bytesRead);
 				System.out.write(singleByte);
 			}
 		} catch (CommandLineException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void deploy(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 	}
 
 	public void startServer(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 	}
 
 	public void stopServer(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 	}
 	
 	public void performCIPreBuildStep(String jobName, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 	}
 	
 	public void generateReport(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		// TODO Auto-generated method stub
 	}
 	
 	private void generateMavenCommand(MavenProjectInfo mavenProjectInfo, String propertyTagName) throws PhrescoException {
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append(TEST_COMMAND);
 			MavenProject project = mavenProjectInfo.getProject();
 			String baseDir = project.getBasedir().getPath();
 			String workingDirectory = project.getProperties().getProperty(propertyTagName);
 			Commandline cl = new Commandline(sb.toString());
 
 			if (StringUtils.isNotEmpty(workingDirectory)) {
 				cl.setWorkingDirectory(baseDir + File.separator + workingDirectory);
 			}
 			Process pb = cl.execute();
 			// Consume subprocess output and write to stdout for debugging
 			InputStream is = new BufferedInputStream(pb.getInputStream());
 			int singleByte = 0;
 			while ((singleByte = is.read()) != -1) {
 				//output.write(buffer, 0, bytesRead);
 				System.out.write(singleByte);
 			}
 		} catch (CommandLineException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public void adaptTestConfig(File SelectedEnvFile, File resultConfigXml, String envName, String browser, String resolution) throws PhrescoException {
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(SelectedEnvFile);
 			List<com.photon.phresco.configuration.Configuration> configurations = configManager.getConfigurations(envName, "Server");
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				updateTestConfiguration(envName, configuration, browser, resultConfigXml, resolution);
 			}
         } catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void updateTestConfiguration(String envName, com.photon.phresco.configuration.Configuration configuration, String browser, File resultConfigXml, String resolution) throws PhrescoException {
 	    try {
 	    	ConfigManager configManager = new ConfigManagerImpl(resultConfigXml);
 	    	Element createConfigElement = configManager.createConfigElement(configuration);
 	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	    	DocumentBuilder builder = factory.newDocumentBuilder();
 	    	Document document = builder.parse(resultConfigXml);
 	    	
 	        Node configNode = getNode("environment", document);
 	        Node node = getNode("environment/" + configuration.getType(), document);
 	        Node browserNode = getNode("environment/Browser", document);
 	        Node resolutionNode = getNode("environment/Resolution", document);
 	        
 	        if (node != null) {
 	            configNode.removeChild(node);
 	        }
 	        
 	        if (browserNode != null) {
 	        	browserNode.setTextContent(browser);
 	        } else {
 	        	Element browserEle = document.createElement("Browser");
 	        	browserEle.setTextContent(browser);
 	        	configNode.appendChild(browserEle);
 	        }
 	        if (resolution != null) {
 		        if (resolutionNode !=  null ) {
 		        	resolutionNode.setTextContent(resolution);
 		        } else {
 		        	Element resolutiontag = document.createElement("Resolution");
 		        	resolutiontag.setTextContent(resolution);
 		        	configNode.appendChild(resolutiontag);
 		        }
 	        }
 	        Node importNode = document.importNode(createConfigElement, Boolean.TRUE);
 	        configNode.appendChild(importNode);
 	        writeXml(new FileOutputStream(resultConfigXml), document);
         } catch (Exception e) {
             throw new PhrescoException("Configuration not found to delete");
         }
     }
 	
 	private Node getNode(String xpath, Document document) throws XPathExpressionException {
 		XPathExpression xPathExpression = getXPath().compile(xpath);
 		return (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
 	}
 	
 	private XPath getXPath() {
 	    XPathFactory xPathFactory = XPathFactory.newInstance();
 	    return xPathFactory.newXPath();	
 	}
 	
 	protected void writeXml(OutputStream fos, Document document) throws PhrescoException, TransformerException ,IOException{
 		try {
 			TransformerFactory tFactory = TransformerFactory.newInstance();
 			Transformer transformer = tFactory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 
 			Source src = new DOMSource(document);
 			Result res = new StreamResult(fos);
 			transformer.transform(src, res);
 		} finally {
 			if (fos != null) {
 				fos.close();
 			}
 		}
 	}
 
 	@Override
 	public void startHub(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		File baseDir = mavenProjectInfo.getBaseDir();
 		MavenProject project = mavenProjectInfo.getProject();
 		Map<String, String> configs = MojoUtil.getAllValues(configuration);
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
 			hubConfig.setServlets(servlets);
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
 			File pomFile = project.getFile();
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			PluginUtils plugniutil = new PluginUtils();
 			plugniutil.updateHubConfigInfo(baseDir, funcDir, hubConfig);
 			log.info("Starting the Hub...");
 			plugniutil.startHub(baseDir);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (UnknownHostException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 
 	@Override
 	public void stopHub(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			File baseDir = mavenProjectInfo.getBaseDir();
 			File pomFile = new File(baseDir  + File.separator + Constants.POM_NAME);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			File configFile = new File(baseDir + funcDir + File.separator + Constants.HUB_CONFIG_JSON);
 			Gson gson = new Gson();
             BufferedReader reader = new BufferedReader(new FileReader(configFile));
             HubConfiguration hubConfiguration = gson.fromJson(reader, HubConfiguration.class);
             int portNumber = hubConfiguration.getPort();
 			PluginUtils pluginutil = new PluginUtils();
 			pluginutil.stopServer("" + portNumber, baseDir);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 		    throw new PhrescoException(e);
         }
 		
 	}
 
 	@Override
 	public void startNode(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		File baseDir = mavenProjectInfo.getBaseDir();
 		MavenProject project = mavenProjectInfo.getProject();
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
 			File pomFile = project.getFile();
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			PluginUtils plugniutil = new PluginUtils();
 			plugniutil.updateNodeConfigInfo(baseDir, funcDir, nodeConfiguration);
 			log.info("Starting the Node...");
 			plugniutil.startNode(baseDir);
 		}  catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 		
 	}
 
 	@Override
 	public void stopNode(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			File baseDir = mavenProjectInfo.getBaseDir();
 			File pomFile = new File(baseDir  + File.separator + Constants.POM_NAME);
 			PomProcessor processor = new PomProcessor(pomFile);
 			String funcDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 			File configFile = new File(baseDir + funcDir + File.separator + Constants.NODE_CONFIG_JSON);
 			Gson gson = new Gson();
 			BufferedReader reader = new BufferedReader(new FileReader(configFile));
             NodeConfiguration nodeConfiguration = gson.fromJson(reader, NodeConfiguration.class);
             int portNumber = nodeConfiguration.getConfiguration().getPort();
 			PluginUtils pluginutil = new PluginUtils();
 			pluginutil.stopServer("" + portNumber, baseDir);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 		    throw new PhrescoException(e);
         }
 	}
 }
