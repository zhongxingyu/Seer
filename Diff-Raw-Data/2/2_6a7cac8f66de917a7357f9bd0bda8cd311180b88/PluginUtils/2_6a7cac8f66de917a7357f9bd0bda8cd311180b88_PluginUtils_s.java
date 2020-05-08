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
 package com.photon.phresco.plugin.commons;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.lang.reflect.Type;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.ConfigReader;
 import com.photon.phresco.configuration.ConfigWriter;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.model.ContextUrls;
 import com.photon.phresco.framework.model.DbContextUrls;
 import com.photon.phresco.framework.model.Headers;
 import com.photon.phresco.framework.model.Parameters;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugins.filter.FileListFilter;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.HubConfiguration;
 import com.photon.phresco.util.NodeConfiguration;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Dependency;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.util.PomProcessor;
 
 public class PluginUtils {
 	
 	private String host = null;
 	private String port = null;
 	private String protocol = null;
 	private String serverContext = null;
 
 	public void executeUtil(String environmentType, String basedir, File sourceConfigXML) throws PhrescoException {
 		try {
 			String customerId = readCustomerId(new File(basedir));
 			File currentDirectory = new File(basedir);
 			File configXML = new File(currentDirectory + File.separator + 
 			PluginConstants.DOT_PHRESCO_FOLDER + File.separator + PluginConstants.CONFIG_FILE);
 			File settingsXML = new File(Utility.getProjectHome() + customerId + Constants.SETTINGS_XML);
 			ConfigReader reader = new ConfigReader(configXML);
 			ConfigWriter writer = new ConfigWriter(reader, true);
 			writer.saveXml(sourceConfigXML, environmentType);
 			if (settingsXML.exists()) {
 				ConfigReader srcReaderToAppend = new ConfigReader(sourceConfigXML);
 				
 				ConfigReader globalReader = new ConfigReader(settingsXML);
 				ConfigWriter globalWriter = new ConfigWriter(globalReader, true);
 				globalWriter.saveXml(srcReaderToAppend, environmentType);
 			}
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public List<String> csvToList(String csvString) {
 		List<String> envs = new ArrayList<String>();
 		if (StringUtils.isNotEmpty(csvString)) {
 			String[] temp = csvString.split(",");
 			for (int i = 0; i < temp.length; i++) {
 				envs.add(temp[i]);
 			}
 		}
 		return envs;
 	}
 	
 	public void encode(File configFile) throws PhrescoException {
 		try {
 			String fileToString = org.codehaus.plexus.util.FileUtils.fileRead(configFile);
 			String content = Base64.encodeBase64String(fileToString.getBytes());
 //			FileUtils.fileWrite(configFile, content);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	public void encryptConfigFile(String fileName) throws PhrescoException {
 		InputStream inputStream = null;
 		try {
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			inputStream = new FileInputStream(new File(fileName));
 			Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
 			StringWriter stw = new StringWriter();
 			Transformer serializer = TransformerFactory.newInstance().newTransformer();
 			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
 			serializer.transform(new DOMSource(doc), new StreamResult(stw));
 			EncryptString encryptstring = new EncryptString();
 			encryptstring.Crypto("D4:6E:AC:3F:F0:BE");
 			String encryptXmlString = encryptstring.encrypt(stw.toString());
 			writeXml(encryptXmlString, fileName);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}  finally {
 			Utility.closeStream(inputStream);
 		}
 	}
 	
 	private void writeXml(String encrStr, String fileName) throws PhrescoException  {
 		DataOutputStream dos = null;
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(fileName);
 			dos = new DataOutputStream(fos);
 			dos.writeBytes(encrStr);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(dos);
 			Utility.closeStream(fos);
 		}
 	}
 	
 	public void setDefaultEnvironment(String environmentName, File sourceConfigXML) throws PhrescoException {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			docFactory.setNamespaceAware(false);
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.parse(sourceConfigXML);
 			NodeList environmentList = doc.getElementsByTagName("environment");
 			for (int i = 0; i < environmentList.getLength(); i++) {
 				Element environment = (Element) environmentList.item(i);
 				String envName = environment.getAttribute("name");
 				String[] envs = environmentName.split(",");
 				for (String envsName : envs) {
 					if (envsName.equals(envName)) {
 						environment.setAttribute("default", "true");
 						// write the content into xml file
 						TransformerFactory transformerFactory = TransformerFactory.newInstance();
 						Transformer transformer = transformerFactory.newTransformer();
 						DOMSource source = new DOMSource(doc);
 						StreamResult result = new StreamResult(sourceConfigXML.toURI().getPath());
 						transformer.transform(source, result);
 					}
 				}
 			}
 
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (TransformerException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (SAXException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public BuildInfo getBuildInfo(int buildNumber, String directory) throws MojoExecutionException {
 		File buildInfoFile = new File(directory + PluginConstants.BUILD_DIRECTORY + PluginConstants.BUILD_INFO_FILE);
 		if (!buildInfoFile.exists()) {
 			throw new MojoExecutionException("Build info is not available!");
 		}
 		try {
 			List<BuildInfo> buildInfos = getBuildInfo(buildInfoFile);
 			
 			 if (CollectionUtils.isEmpty(buildInfos)) {
 				 throw new MojoExecutionException("Build info is empty!");
 			 }
 
 			 for (BuildInfo buildInfo : buildInfos) {
 				 if (buildInfo.getBuildNo() == buildNumber) {
 					 return buildInfo;
 				 }
 			 }
 
 			 throw new MojoExecutionException("Build info is empty!");
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getLocalizedMessage());
 		}
 	}
 	
 	public BuildInfo getBuildInfo(int buildNumber) throws MojoExecutionException {
 		File currentDirectory = new File(".");
 		File buildInfoFile = new File(currentDirectory.getPath() + PluginConstants.BUILD_DIRECTORY + PluginConstants.BUILD_INFO_FILE);
 		if (!buildInfoFile.exists()) {
 			throw new MojoExecutionException("Build info is not available!");
 		}
 		try {
 			List<BuildInfo> buildInfos = getBuildInfo(buildInfoFile);
 			
 			 if (CollectionUtils.isEmpty(buildInfos)) {
 				 throw new MojoExecutionException("Build info is empty!");
 			 }
 
 			 for (BuildInfo buildInfo : buildInfos) {
 				 if (buildInfo.getBuildNo() == buildNumber) {
 					 return buildInfo;
 				 }
 			 }
 
 			 throw new MojoExecutionException("Build info is empty!");
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getLocalizedMessage());
 		}
 	}
 	
 	 public List<BuildInfo> getBuildInfo(File path) throws IOException {
 		 if (!path.exists()) {
 			 return new ArrayList<BuildInfo>(1);
 		 }
 
 		 BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
 		 Gson gson = new Gson();
 		 Type type = new TypeToken<List<BuildInfo>>(){}.getType();
 
 		 List<BuildInfo> buildInfos = gson.fromJson(bufferedReader, type);
 		 Collections.sort(buildInfos, new BuildInfoComparator());
 		 bufferedReader.close();
 
 		 return buildInfos;
 	 }
 	 
 	 public void changeTestName(String performancePath, String testName) throws Exception {
 			File buildPathXml = new File(performancePath + PluginConstants.BUILD_XML_FILE);
 			Document document = getDocument(buildPathXml);
 			String fileNameNode = "project/target[@name='init']/property[@name='jmeter.result.file']";
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, fileNameNode);
 			if (nodelist != null && nodelist.getLength() > 0) {
 				Node stringProp = nodelist.item(0);
 				NamedNodeMap attributes = stringProp.getAttributes();
 				Node valueAttr = attributes.getNamedItem("value");
 				String valueAttrTxt = valueAttr.getTextContent();
 				valueAttr.setTextContent(valueAttrTxt.substring(0, valueAttrTxt.indexOf("/") + 1).concat(testName + ".xml"));
 			}
 			saveDocument(buildPathXml, document);
 		}
 	 
 	 private Document getDocument(File file) throws PhrescoException {
 			try {
 				InputStream fis = null;
 				Reader reader = null;
 				InputSource source = null;
 				DocumentBuilder builder = null;
 				try {
 					fis = new FileInputStream(file);  
 				    reader = new InputStreamReader(fis, "UTF8");   
 				    source = new InputSource(reader);  
 				    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 				    domFactory.setNamespaceAware(false);
 				    builder = domFactory.newDocumentBuilder();
 				    Document doc = builder.parse(source);
 				    
 				    return doc;
 				    
 				} finally {
 					if (reader != null) {
 						reader.close();
 					}
 					
 					if (fis != null) {
 						fis.close();
 				    }
 				}
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e);
 			} catch (ParserConfigurationException e) {
 				throw new PhrescoException(e);
 			} catch (SAXException e) {
 				throw new PhrescoException(e);
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	 
 	 public void adaptTestConfig(String testDirPath, Configuration configuration) throws PhrescoException {
 			FileWriter out = null;
 			try {
 				File configFile = new File(testDirPath + PluginConstants.LOAD_TEST_CONFIG_FILE);
 				if (!configFile.exists()) {
 					return;
 				}
 				out = new FileWriter(configFile);
 
 				getConfiguration(configuration);
 
 				out.write(host + Constants.COMMA);
 				out.write(port + Constants.COMMA);
 				out.write(protocol + Constants.COMMA);
 				out.write(serverContext);
 				out.flush();
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			} finally {
 				try {
 					if (out != null) {
 						out.close();
 					}
 				} catch (IOException e) {
 					throw new PhrescoException(e);
 				}
 			}
 		}
 	 
 	 public void getConfiguration(Configuration configuration) {
 			String type = configuration.getType();
 			if (type.equalsIgnoreCase("Server")) {
 				host = configuration.getProperties().getProperty(Constants.SERVER_HOST);
 				port = configuration.getProperties().getProperty(Constants.SERVER_PORT);
 				protocol = configuration.getProperties().getProperty(Constants.SERVER_PROTOCOL);
 				serverContext = configuration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 			}
 
 			if (type.equalsIgnoreCase("WebService")) {
 				host = configuration.getProperties().getProperty(Constants.WEB_SERVICE_HOST);
 				port = configuration.getProperties().getProperty(Constants.WEB_SERVICE_PORT);
 				protocol = configuration.getProperties().getProperty(Constants.WEB_SERVICE_PROTOCOL);
 				serverContext = configuration.getProperties().getProperty(Constants.WEB_SERVICE_CONTEXT);
 			}
 
 			if (type.equalsIgnoreCase("Database")) {
 				host = configuration.getProperties().getProperty(Constants.DB_HOST);
 				port = configuration.getProperties().getProperty(Constants.DB_PORT);
 				protocol = configuration.getProperties().getProperty(Constants.DB_PROTOCOL);
 				serverContext = "/";
 			}
 		}
 	 
 	 public void adaptPerformanceJmx(String jmxFileLocation, List<ContextUrls> contextUrls, int noOfUsers,
 			 int rampUpPeriod, int loopCount, boolean authMngr, String authorizationUrl,
 			 String authorizationUserName, String authorizationPassword, String authorizationDomain, String authorizationRealm) throws Exception {
 		 File jmxFile = null;
 		 File jmxDir = new File(jmxFileLocation);
 		 if(jmxDir.isDirectory()) {
 			 FilenameFilter filter = new FileListFilter("", "jmx");
 			 File[] jmxFiles = jmxDir.listFiles(filter);
 			 for (File file : jmxFiles) {
 				if ("PhrescoFrameWork_TestPlan.jmx".equals(file.getName())) {
 					jmxFile = file;
 					break;
 				}
 			}
 		 }
 		 Document document = getDocument(jmxFile);
 		 appendThreadProperties(document, noOfUsers, rampUpPeriod, loopCount);
 		 NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/HTTPSamplerProxy");
 		 if (nodelist != null && nodelist.getLength() > 0) {
 			 Node hashTree = nodelist.item(0).getParentNode();
 			 hashTree = removeAllChilds(hashTree);
 			 hashTree.setTextContent(null);
 			 
 			 //Create AuthManager
 			 if (authMngr) {
 				 Node authManagerNode = createAuthManager(document, null, authorizationUrl, authorizationUserName, authorizationPassword, authorizationDomain, authorizationRealm);
 				 hashTree.appendChild(authManagerNode);
 				 hashTree.appendChild(document.createElement("hashTree"));
 			 }
 			 
 			 NodeList headerManagerNodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/HeaderManager");
 			 
 			 if (headerManagerNodelist != null && headerManagerNodelist.getLength() > 0) {
 				 for(int i = 0; i < headerManagerNodelist.getLength(); i++) {
 					 hashTree.appendChild(headerManagerNodelist.item(i));
 				 }
 				 hashTree.appendChild(document.createElement("hashTree"));
 			 }
 			 
 			 for (ContextUrls contextUrl : contextUrls) {
 				 Node appendHttpSamplerProxy = appendHttpSamplerProxy(document, hashTree, contextUrl);
 				 hashTree.appendChild(appendHttpSamplerProxy);
 				 
 				 List<Headers> headers = contextUrl.getHeaders();
 				 if (CollectionUtils.isNotEmpty(headers) || contextUrl.isRegexExtractor()) {
 					 Element hashTreeElement = document.createElement("hashTree");
 					 if (CollectionUtils.isNotEmpty(headers)) {
 						 NodeList headerMngrNodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/hashTree/HeaderManager/collectionProp");
 						 Node appendHeaderManager = appendUrlHeaderManager(document, headers, hashTreeElement, contextUrl.isRegexExtractor());
 						 hashTree.appendChild(appendHeaderManager);
 					 } 
 					 if (contextUrl.isRegexExtractor()) {
 						 Node regexExtractorNode = appendRegexExtractor(document, hashTree, hashTreeElement, contextUrl);
 						 hashTreeElement.appendChild(regexExtractorNode);
 						 hashTreeElement.appendChild(document.createElement("hashTree"));
 						 hashTree.appendChild(hashTreeElement);
 					 }
 				 } else {
 					 hashTree.appendChild(document.createElement("hashTree")); 
 				 }
 			 }
 		 }
 		 saveDocument(jmxFile, document);
 	 }
 	 
 	 private Node appendHttpSamplerProxy(Document document, Node hashTree, ContextUrls contextUrl) {
 			Node httpSamplerProxy = document.createElement("HTTPSamplerProxy");
 			String contentEncoding = null;
 			if(FrameworkConstants.POST.equals(contextUrl.getContextType())) {
 				contentEncoding = contextUrl.getEncodingType();
 			}
 			
 			String cntxt = "${context}";
 			if (StringUtils.isNotEmpty(contextUrl.getContext())) {
 				cntxt = cntxt + "/" + contextUrl.getContext();
 			}
 			 
 			NamedNodeMap attributes = httpSamplerProxy.getAttributes();
 			attributes.setNamedItem(createAttribute(document, "guiclass", "HttpTestSampleGui"));
 			attributes.setNamedItem(createAttribute(document, "testclass", "HTTPSamplerProxy"));
 			attributes.setNamedItem(createAttribute(document, "testname", contextUrl.getName())); //url name
 			attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 
 			appendElementProp(document, httpSamplerProxy, contextUrl.getContextType(), contextUrl.getContextPostData(), contextUrl);
 
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.domain", null);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.port", null);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.connect_timeout", null);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.response_timeout", null);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.protocol", null);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.contentEncoding", contentEncoding);
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.path", cntxt); // server url
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.method", contextUrl.getContextType());
 
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.follow_redirects", String.valueOf(contextUrl.isFollowRedirects()));
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.auto_redirects", String.valueOf(contextUrl.isRedirectAutomatically()));
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.use_keepalive", String.valueOf(contextUrl.isKeepAlive()));
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.DO_MULTIPART_POST", String.valueOf(contextUrl.isMultipartData()));
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.BROWSER_COMPATIBLE_MULTIPART", String.valueOf(contextUrl.isCompatibleHeaders()));
 			
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.implementation", "Java");
 			appendTypeProp(document, httpSamplerProxy, "boolProp", "HTTPSampler.monitor", "false");
 			appendTypeProp(document, httpSamplerProxy, "stringProp", "HTTPSampler.embedded_url_re", null);
 
 			return httpSamplerProxy;
 		}
 
 	 private Node appendRegexExtractor(Document document, Node hashTree, Element newHashTree, ContextUrls contextUrl) {
 		 Node regexExtractor = document.createElement("RegexExtractor");
 		 
 		 NamedNodeMap attributes = regexExtractor.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "guiclass", "RegexExtractorGui"));
 		 attributes.setNamedItem(createAttribute(document, "testclass", "RegexExtractor"));
 		 attributes.setNamedItem(createAttribute(document, "testname", "Regular Expression Extractor"));
 		 attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.useHeaders", contextUrl.getResponseField());
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.refname", contextUrl.getReferenceName());
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.regex", contextUrl.getRegex());
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.template", contextUrl.getTemplate());
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.default", contextUrl.getDefaultValue());
 		 appendTypeProp(document, regexExtractor, "stringProp", "RegexExtractor.match_number", contextUrl.getMatchNo());
 		 
 		 if (!"main".equals(contextUrl.getApplyTo())) {
 			 appendTypeProp(document, regexExtractor, "stringProp", "Sample.scope", contextUrl.getApplyTo());
 		 }
 
 		 return regexExtractor;
 	 }
 
 	 private Node createAuthManager(Document document, Node hashTree, String authorizationUrl, 
 			 String authorizationUserName, String authorizationPassword, String authorizationDomain, String authorizationRealm) {
 			Node authManager = document.createElement("AuthManager");
 			NamedNodeMap attributes = authManager.getAttributes();
 			attributes.setNamedItem(createAttribute(document, "guiclass", "AuthPanel"));
 			attributes.setNamedItem(createAttribute(document, "testclass", "AuthManager"));
 			attributes.setNamedItem(createAttribute(document, "testname", "HTTP Authorization Manager"));
 			attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 
 			appendAuthCollectionProp(document, authManager,  authorizationUrl, authorizationUserName, authorizationPassword, authorizationDomain, authorizationRealm);
 
 			return authManager;
 		}
 	 
 	 private void appendElementProp(Document document, Node parentNode, String contextType, String contextPostData, ContextUrls contextUrl) { // eleme prop
 			Node elementProp = document.createElement("elementProp");
 			NamedNodeMap attributes = elementProp.getAttributes();
 
 			attributes.setNamedItem(createAttribute(document, "name", "HTTPsampler.Arguments"));
 			attributes.setNamedItem(createAttribute(document, "elementType", "Arguments"));
 			attributes.setNamedItem(createAttribute(document, "guiclass", "HTTPArgumentsPanel"));
 			attributes.setNamedItem(createAttribute(document, "testclass", "Arguments"));
 			attributes.setNamedItem(createAttribute(document, "testname", "User Defined Variables"));
 			attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 			appendCollectionProp(document, elementProp, contextType, contextPostData, contextUrl);
 
 			//parentNode.setTextContent(null);
 			parentNode.appendChild(elementProp);
 		}
 	 
 	 private void appendCollectionProp(Document document, Node elementProp, String contextType, String contextPostData, ContextUrls contextUrl) { // collection append in prop
 		 String argumentValue = null;
 		 if(contextType.equals(FrameworkConstants.POST)) {
 			 argumentValue = contextPostData;
 		 }
 		 Node collectionProp = document.createElement("collectionProp");
 		 NamedNodeMap attributes = collectionProp.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "name", "Arguments.arguments"));
 		 List<Parameters> parameters = contextUrl.getParameters();
 		 if (CollectionUtils.isNotEmpty(parameters)) {
 			 for (Parameters parameter : parameters) {
 				 Node subElementProp = document.createElement("elementProp");
 				 NamedNodeMap subElementAttributes = subElementProp.getAttributes();
 				 subElementAttributes.setNamedItem(createAttribute(document, "name", parameter.getName()));
 				 subElementAttributes.setNamedItem(createAttribute(document, "elementType", "HTTPArgument"));
 				 collectionProp.appendChild(subElementProp);
 				 appendTypeProp(document, subElementProp, "boolProp", "HTTPArgument.always_encode", String.valueOf(parameter.isEncode()));
 				 appendTypeProp(document, subElementProp, "stringProp", "Argument.value", parameter.getValue());
 				 appendTypeProp(document, subElementProp, "stringProp", "Argument.metadata", "=");
 				 appendTypeProp(document, subElementProp, "boolProp", "HTTPArgument.use_equals", "true");
 				 appendTypeProp(document, subElementProp, "stringProp", "Argument.name", parameter.getName());
 			 }
 		 }
 		 
 		 elementProp.setTextContent(null);
 		 elementProp.appendChild(collectionProp);
 	 }
 	
 	 private void appendAuthCollectionProp(Document document, Node elementProp, String authorizationUrl, 
 			 String authorizationUserName, String authorizationPassword, String authorizationDomain, String authorizationRealm) { // collection append in prop
 		 byte[] decodedPswd = Base64.decodeBase64(authorizationPassword);
 		 String pswd = new String(decodedPswd);
 		 
 		 Node collectionProp = document.createElement("collectionProp");
 		 NamedNodeMap attributes = collectionProp.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "name", "AuthManager.auth_list"));
 		 
 		 Node subElementProp = document.createElement("elementProp");
 		 NamedNodeMap subElementAttributes = subElementProp.getAttributes();
 		 subElementAttributes.setNamedItem(createAttribute(document, "name", ""));
 		 subElementAttributes.setNamedItem(createAttribute(document, "elementType", "Authorization"));
 		 collectionProp.appendChild(subElementProp);
 		 
 		 appendTypeProp(document, subElementProp, "stringProp", "Authorization.url", authorizationUrl);
 		 appendTypeProp(document, subElementProp, "stringProp", "Authorization.username", authorizationUserName);
 		 appendTypeProp(document, subElementProp, "stringProp", "Authorization.password", pswd);
 		 appendTypeProp(document, subElementProp, "stringProp", "Authorization.domain", authorizationDomain);
 		 appendTypeProp(document, subElementProp, "stringProp", "Authorization.realm", authorizationRealm);
 		 
 		 elementProp.setTextContent(null);
 		 elementProp.appendChild(collectionProp);
 	 }
 	 
 	 public void adaptDBPerformanceJmx(String jmxFileLocation, List<DbContextUrls> dbContextUrls, String dataSource, int noOfUsers, int rampUpPeriod, int loopCount, String dbUrl, String driver, String userName, String passWord ) throws Exception {
 		 File jmxFile = null;
 		 File jmxDir = new File(jmxFileLocation);
 		 if(jmxDir.isDirectory()){
 			 FilenameFilter filter = new FileListFilter("", "jmx");
 			 File[] jmxFiles = jmxDir.listFiles(filter);
 			 for (File file : jmxFiles) {
 				 if ("PhrescoFrameWork_TestPlan.jmx".equals(file.getName())) {
 					 jmxFile = file;
 					 break;
 				 }
 			 }
 		 }
 
 		 Document document = getDocument(jmxFile);
 		 appendThreadProperties(document, noOfUsers, rampUpPeriod, loopCount);
 		 appendJdbcDataSrc(document, dataSource, dbUrl, driver, userName, passWord);
 		 NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/JDBCSampler");
 		 if (nodelist != null && nodelist.getLength() > 0) {
 			 Node hashTree = nodelist.item(0).getParentNode();
 			 hashTree = removeAllChilds(hashTree);
 			 hashTree.setTextContent(null);
 			 
 			 for (DbContextUrls dbContextUrl : dbContextUrls) {
 				 Node appendJdbcSampler = appendJdbcSampler(document, hashTree, dbContextUrl.getName(), dataSource, dbContextUrl.getQueryType(), dbContextUrl.getQuery());
 				 hashTree.appendChild(appendJdbcSampler);
 				 hashTree.appendChild(document.createElement("hashTree"));
 			 }
 		 }
 		 saveDocument(jmxFile, document);
 	 }
 	 
 	 private void appendJdbcDataSrc(Document document, String dataSrc, String dbUrl, String driver,String userName,String passWord) throws Exception {
 		 String dataSource = "jmeterTestPlan/hashTree/hashTree/JDBCDataSource/stringProp[@name='dataSource']";
 		 String url = "jmeterTestPlan/hashTree/hashTree/JDBCDataSource/stringProp[@name='dbUrl']";
 		 String driverName = "jmeterTestPlan/hashTree/hashTree/JDBCDataSource/stringProp[@name='driver']";
 		 String pwd = "jmeterTestPlan/hashTree/hashTree/JDBCDataSource/stringProp[@name='password']";
 		 String user = "jmeterTestPlan/hashTree/hashTree/JDBCDataSource/stringProp[@name='username']";
 		 appendTextContent(document, dataSource, ""+dataSrc);
 		 appendTextContent(document, url, ""+dbUrl);
 		 appendTextContent(document, driverName, ""+driver);
 		 appendTextContent(document, pwd, ""+passWord);
 		 appendTextContent(document, user, ""+userName);
 	 }
 	 
 	 private static Node appendJdbcSampler(Document document, Node hashTree, String name, String dataSource, String queryType, String query) {
 		 Node jdbcSampler = document.createElement("JDBCSampler");
 
 		 NamedNodeMap attributes = jdbcSampler.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "guiclass", "TestBeanGUI"));
 		 attributes.setNamedItem(createAttribute(document, "testclass", "JDBCSampler"));
 		 attributes.setNamedItem(createAttribute(document, "testname", name)); //url name
 		 attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 
 		 appendTypeProp(document, jdbcSampler, "stringProp", "dataSource", dataSource);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "queryType", queryType);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "query", query);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "queryArguments", null);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "queryArgumentsTypes", null);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "variableNames", null);
 		 appendTypeProp(document, jdbcSampler, "stringProp", "resultVariable", null); 
 
 		 return jdbcSampler;
 	 }
 	 
 	 private Node removeAllChilds(Node hashTree) {
 			NodeList childNodes = hashTree.getChildNodes();
 			for (int i = 0; i < childNodes.getLength(); i++) {
 				hashTree.removeChild(childNodes.item(i));
 			}
 			return hashTree;
 		}
 	 
 	 public void adaptLoadJmx(String jmxFileLocation, int noOfUsers, int rampUpPeriod, int loopCount, boolean authMngr, String authorizationUrl,
 			 String authorizationUserName, String authorizationPassword, String authorizationDomain, String authorizationRealm 
 			 , List<ContextUrls> contextUrls) throws Exception {
 	        File jmxFile = null;
 	        File jmxDir = new File(jmxFileLocation);
 	        if(jmxDir.isDirectory()){
 	            FilenameFilter filter = new FileListFilter("", "jmx");
 	            File[] jmxFiles = jmxDir.listFiles(filter);
 	            jmxFile = jmxFiles[0];
 	        }
 	        Document document = getDocument(jmxFile);
 	        appendThreadProperties(document, noOfUsers, rampUpPeriod, loopCount);
 	       NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/HTTPSamplerProxy");
 			 if (nodelist != null && nodelist.getLength() > 0) {
 				 Node hashTree = nodelist.item(0).getParentNode();
 				 hashTree = removeAllChilds(hashTree);
 				 hashTree.setTextContent(null);
 				 
 				//Create AuthManager
 				 if (authMngr) {
 					 Node authManagerNode = createAuthManager(document, null, authorizationUrl, authorizationUserName, authorizationPassword, authorizationDomain, authorizationRealm);
 					 hashTree.appendChild(authManagerNode);
 					 hashTree.appendChild(document.createElement("hashTree"));
 				 }
 				 
 				 NodeList headerManagerNodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/HeaderManager");
 				 if (headerManagerNodelist != null && headerManagerNodelist.getLength() > 0) {
 					 for(int i = 0; i < headerManagerNodelist.getLength(); i++) {
 						 hashTree.appendChild(headerManagerNodelist.item(i));
 					 }
 					 hashTree.appendChild(document.createElement("hashTree"));
 				 }
 
 				 for (ContextUrls contextUrl : contextUrls) {
 					 Node appendHttpSamplerProxy = appendHttpSamplerProxy(document, hashTree, contextUrl);
 					 hashTree.appendChild(appendHttpSamplerProxy);
 					 List<Headers> headers = contextUrl.getHeaders();
 					 if (CollectionUtils.isNotEmpty(headers) || contextUrl.isRegexExtractor()) {
 						 Element hashTreeElement = document.createElement("hashTree");
 						 if (CollectionUtils.isNotEmpty(headers)) {
 							 NodeList headerMngrNodelist = org.apache.xpath.XPathAPI.selectNodeList(document, "jmeterTestPlan/hashTree/hashTree/hashTree/hashTree/HeaderManager/collectionProp");
 							 Node appendHeaderManager = appendUrlHeaderManager(document, headers, hashTreeElement, contextUrl.isRegexExtractor());
 							 hashTree.appendChild(appendHeaderManager);
 						 } 
 						 if (contextUrl.isRegexExtractor()) {
 							 Node regexExtractorNode = appendRegexExtractor(document, hashTree, hashTreeElement, contextUrl);
 							 hashTreeElement.appendChild(regexExtractorNode);
 							 hashTreeElement.appendChild(document.createElement("hashTree"));
 							 hashTree.appendChild(hashTreeElement);
 						 }
 					 }
 				 }
 			 }
 			 
 	        saveDocument(jmxFile, document);
 	    }
 	 
 	 private static void appendThreadProperties(Document document, int noOfUsers, int rampUpPeriod, int loopCount) throws Exception {
 			String loopNode = "jmeterTestPlan/hashTree/hashTree/ThreadGroup/*/stringProp[@name='LoopController.loops']";
 			String threadNode = "jmeterTestPlan/hashTree/hashTree/ThreadGroup/stringProp[@name='ThreadGroup.num_threads']";
 			String rampNode = "jmeterTestPlan/hashTree/hashTree/ThreadGroup/stringProp[@name='ThreadGroup.ramp_time']";
 			appendTextContent(document, loopNode, ""+loopCount);
 			appendTextContent(document, threadNode, ""+noOfUsers);
 			appendTextContent(document, rampNode, ""+rampUpPeriod);
 		}
 	 
 	 private static void appendTextContent(Document document, String element, String textContent) throws Exception {
 			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, element);
 
 			for (int i = 0; i < nodelist.getLength(); i++) {
 				Node stringProp = nodelist.item(i);
 				stringProp.setTextContent(textContent);
 			}
 		}
 	 
 	 private static void createHeaderElementProp(Document document, List<Headers> headers, Node collectionProp) {
 		 //To remove already added header key,values
 		 NodeList childNodes = collectionProp.getChildNodes();
 		 for (int i = 0; i < childNodes.getLength(); i++) {
 			 Node item = childNodes.item(i);
 			 if(item.hasChildNodes()) {
 				 collectionProp.removeChild(item);
 			 }	 
 		 }
 		 
 		 //To append header key values newly
 		 for (Headers header : headers) {
 			 Node subElementProp = document.createElement("elementProp");
 			 NamedNodeMap subElementAttributes = subElementProp.getAttributes();
 			 subElementAttributes.setNamedItem(createAttribute(document, "name", ""));
 			 subElementAttributes.setNamedItem(createAttribute(document, "elementType", "Header"));
 			 collectionProp.appendChild(subElementProp);
 			 appendTypeProp(document, subElementProp, "stringProp", "Header.name", header.getKey());
 			 appendTypeProp(document, subElementProp, "stringProp", "Header.value", header.getValue());
 		 }
 	 }
 
 	 private static Attr createAttribute(Document document, String attrName, String attrValue) {
 		 Attr attr = document.createAttribute(attrName);
 		 attr.setValue(attrValue);
 		 return attr;
 	 }
 
 	 private static void appendTypeProp(Document document, Node parentProp, String tag, String nameAttr, String textContent) {
 		 Node typeProp = document.createElement(tag);
 		 NamedNodeMap attributes = typeProp.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "name", nameAttr));
 		 typeProp.setTextContent(textContent);
 		 parentProp.appendChild(typeProp);
 	 }
 	 
 	 private static Node appendUrlHeaderManager(Document document, List<Headers> headers, Element createElement, boolean isRegexExtractor) {
 		 Node headerManager = document.createElement("HeaderManager");
 		 NamedNodeMap attributes = headerManager.getAttributes();
 		 attributes.setNamedItem(createAttribute(document, "guiclass", "HeaderPanel"));
 		 attributes.setNamedItem(createAttribute(document, "testclass", "HeaderManager"));
 		 attributes.setNamedItem(createAttribute(document, "testname", "HTTP Header Manager"));
 		 attributes.setNamedItem(createAttribute(document, "enabled", "true"));
 		 appendHeaderManagerCollectionProp(document, headerManager, headers);
 		 createElement.appendChild(headerManager);
 		 createElement.appendChild(document.createElement("hashTree"));
 		 return createElement;
 	 }
 	 
 	 private static void appendHeaderManagerCollectionProp(Document document, Node elementProp, List<Headers> headers) {
 			Node collectionProp = document.createElement("collectionProp");
 			NamedNodeMap attributes = collectionProp.getAttributes();
 			attributes.setNamedItem(createAttribute(document, "name", "HeaderManager.headers"));
 			createHeaderElementProp(document, headers, collectionProp);
 			elementProp.setTextContent(null);
 			elementProp.appendChild(collectionProp);
 		}
 	 
 	 private static void saveDocument(File file, Document doc) throws PhrescoException {
 
 			try {
 				TransformerFactory factory = TransformerFactory.newInstance();
 				Transformer transformer = factory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 				StringWriter writer = new StringWriter();
 				StreamResult result = new StreamResult(writer);
 				DOMSource source = new DOMSource(doc);
 				transformer.transform(source, result);
 				String content = writer.toString();
 				FileWriter fileWriter = new FileWriter(file);
 				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
 				bufferedWriter.write(content);
 				bufferedWriter.flush();
 				bufferedWriter.close();
 			} catch (TransformerConfigurationException e) {
 				throw new PhrescoException(e);
 			} catch (IllegalArgumentException e) {
 				throw new PhrescoException(e);
 			} catch (TransformerFactoryConfigurationError e) {
 				throw new PhrescoException(e);
 			} catch (TransformerException e) {
 				throw new PhrescoException(e);
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	 
 	public void updateHubConfigInfo(File baseDir, String funcDir, HubConfiguration hubConfig)	throws PhrescoException {
 		BufferedWriter out = null;
 		FileWriter fileWriter = null;
 		try {
 			File hubConfigFile = new File(baseDir + funcDir + File.separator + Constants.HUB_CONFIG_JSON);
 			Gson gson = new Gson();
 			String infoJSON = gson.toJson(hubConfig);
 			fileWriter = new FileWriter(hubConfigFile);
 			out = new BufferedWriter(fileWriter);
 			out.write(infoJSON);
 		} catch (UnknownHostException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeWriter(out);
 			Utility.closeStream(fileWriter);
 		}
 	}
 
 	public void updateNodeConfigInfo(File baseDir, String funcDir, NodeConfiguration nodeConfiguration) throws PhrescoException {
 		BufferedWriter out = null;
 		FileWriter fileWriter = null;
 		try {
 			File nodeConfigFile = new File(baseDir + funcDir + File.separator + Constants.NODE_CONFIG_JSON);
 			Gson gson = new Gson();
 			String infoJSON = gson.toJson(nodeConfiguration);
 			fileWriter = new FileWriter(nodeConfigFile);
 			out = new BufferedWriter(fileWriter);
 			out.write(infoJSON);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeWriter(out);
 			Utility.closeStream(fileWriter);
 		}
 	}
 
 	public void startNode(File baseDir, String pomFile, String submodule) throws PhrescoException {
 		FileOutputStream fos = null;
 		try {
 			File LogDir = new File(baseDir + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 			if (!LogDir.exists()) {
 				LogDir.mkdirs();
 			}
 			File pomPath = new File(baseDir + File.separator + pomFile);
             PomProcessor processor = new PomProcessor(pomPath);
             String functionalTestDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
             StringBuilder builder = new StringBuilder()
             .append(baseDir)
             .append(functionalTestDir);
             executeValidatePhase(builder.toString());
             
 			File logFile  = new File(LogDir + Constants.SLASH + Constants.NODE_LOG);
 			StringBuilder sb = new StringBuilder()
 			.append(PluginConstants.JAVA_DWEBDRIVER_CHROME_DRIVER);
 			 if(StringUtils.isNotEmpty(submodule)) {
 	        	sb.append(submodule);
 	        	sb.append("/");
 	        }
 			sb.append(functionalTestDir.substring(1, functionalTestDir.length()));
 			if (findPlatform().equals(Constants.WINDOWS)) {
 				sb.append(PluginConstants.CHROMEDRIVER_CHROMEDRIVER_EXE_JAR);
 			} else {
 				sb.append(PluginConstants.CHROMEDRIVER_CHROMEDRIVER_JAR);
 			}
 			
 			 if(StringUtils.isNotEmpty(submodule)) {
 		        	sb.append(submodule);
 		        	sb.append("/");
 		        }
 			sb.append(functionalTestDir.substring(1, functionalTestDir.length()))
 			.append(PluginConstants.LIB_SELENIUM_SERVER_STANDALONE)
 			.append(PluginConstants.HYPEN)
 			.append(getVersion(baseDir, pomFile))
 			.append(PluginConstants.DOT_JAR)
 			.append(PluginConstants.ROLE_NODE_NODE_CONFIG);
 			 if(StringUtils.isNotEmpty(submodule)) {
 	        	sb.append(submodule);
 	        	sb.append("/");
 	        }
 			sb.append(functionalTestDir.substring(1, functionalTestDir.length()))
 	        .append(PluginConstants.NODECONFIG_JSON);
 			fos = new FileOutputStream(logFile, false);
 			Utility.executeStreamconsumer(sb.toString(), fos);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 		    throw new PhrescoException(e);
         }
 	}
 	
 	private String getVersion(File baseDir, String pom) throws PhrescoPomException {
 		File pomFile = new File(baseDir + File.separator + pom);
 		PomProcessor processor = new PomProcessor(pomFile);
 		String functionalDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		File funcPomFile = new File(baseDir + File.separator + functionalDir + File.separator + Constants.POM_NAME);
 		PomProcessor funPomProcessor = new PomProcessor(funcPomFile);
 		Dependency dependency = funPomProcessor.getDependency(PluginConstants.ORG_SELENIUMHQ_SELENIUM, PluginConstants.SELENIUM_SERVER_STANDALONE);
 		return dependency.getVersion();
 	}
 
 	public void startHub(File baseDir, String pomFile, String submodule) throws PhrescoException {
 	    FileOutputStream fos = null;
 	    try {
 	        File pomPath = new File(baseDir + File.separator + pomFile);
             PomProcessor processor = new PomProcessor(pomPath);
             String functionalTestDir = processor.getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
             StringBuilder builder = new StringBuilder()
             .append(baseDir)
             .append(functionalTestDir);
             
 	        executeValidatePhase(builder.toString());
 	        File LogDir = new File(baseDir + File.separator + Constants.DO_NOT_CHECKIN_DIRY + File.separator + Constants.LOG_DIRECTORY);
 	        if (!LogDir.exists()) {
 	            LogDir.mkdirs();
 	        }
 	        File logFile  = new File(LogDir + Constants.SLASH + Constants.HUB_LOG);
 	        StringBuilder sb = new StringBuilder()
 	        .append(PluginConstants.JAVA_JAR);
 	        if(StringUtils.isNotEmpty(submodule)) {
 	        	sb.append(submodule);
 	        	sb.append("/");
 	        }
 	       sb.append(functionalTestDir.substring(1, functionalTestDir.length()))
 	        .append(PluginConstants.LIB_SELENIUM_SERVER_STANDALONE)
 	        .append(PluginConstants.HYPEN)
 			.append(getVersion(baseDir, pomFile))
 			.append(PluginConstants.DOT_JAR)
 			.append(PluginConstants.ROLE_HUB_HUB_CONFIG);
 	         if(StringUtils.isNotEmpty(submodule)) {
 	        	sb.append(submodule);
 	        	sb.append("/");
 	        }
 	       sb.append(functionalTestDir.substring(1, functionalTestDir.length()))
 	        .append(PluginConstants.HUBCONFIG_JSON);
 	        fos = new FileOutputStream(logFile, false);
 	        Utility.executeStreamconsumer(sb.toString(), fos);
 	    } catch (FileNotFoundException e) {
 	        throw new PhrescoException(e);
 	    } catch (PhrescoPomException e) {
 	        throw new PhrescoException(e);
         }
 	}
 	
 	private void executeValidatePhase(String workingDir) throws PhrescoException {
 	    try {
 	    	StringBuilder sb = new StringBuilder();
 	    	sb.append(Constants.MVN_COMMAND);
 	    	sb.append(Constants.STR_BLANK_SPACE);
 	    	sb.append(Constants.PHASE);
 	        BufferedReader breader = Utility.executeCommand(sb.toString(), workingDir);
 	        String line = null;
 	        while ((line = breader.readLine()) != null) {
 	            if (line.startsWith("[ERROR]")) {
 	                System.out.println(line);
 	            }
 	        }
 	    } catch (IOException e) {
 	        throw new PhrescoException(e);
 	    }
 	}
 	
 	public void stopServer(String portNo, File baseDir) throws PhrescoException {
 		if (System.getProperty(Constants.OS_NAME).startsWith(Constants.WINDOWS_PLATFORM)) {
 			stopJavaServerInWindows("netstat -ao | findstr " + portNo + " | findstr LISTENING", baseDir);
 		} else if (System.getProperty(Constants.OS_NAME).startsWith("Mac")) {
 			stopJavaServerInMac("lsof -i tcp:" + portNo , baseDir);
 		} else {
 			stopJavaServer("fuser " + portNo + "/tcp " + "|" + "awk '{print $1}'", baseDir);
 		}
 	}
 
 	private void stopJavaServerInWindows(String command, File baseDir) throws PhrescoException {
 		BufferedReader bufferedReader = null;
 		try {
 			String pid = "";
 			bufferedReader = Utility.executeCommand(command, baseDir.getPath());
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				pid = line.substring(line.length() - 4, line.length());
 			}
 			Runtime.getRuntime().exec("cmd /X /C taskkill /F /PID " + pid);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(bufferedReader);
 		}
 	}
 
 	private void stopJavaServerInMac(String command, File baseDir) throws PhrescoException {
 		BufferedReader bufferedReader = null;
 		try {
 			bufferedReader = Utility.executeCommand(command, baseDir.getPath());
 			String line = null;
 			String pid = "";
 			while ((line = bufferedReader.readLine()) != null) {
 				if (line.startsWith(PluginConstants.JAVA)) {
 					line = line.substring(8, line.length());
 					pid = line.substring(0, line.indexOf(" "));
 				}
 			}
 			Runtime.getRuntime().exec(Constants.JAVA_UNIX_PROCESS_KILL_CMD + pid);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(bufferedReader);
 		}
 	}
 	
 	private void stopJavaServer(String command, File baseDir) throws PhrescoException {
 		BufferedReader bufferedReader = null;
 		try {
 			String pid = "";
 			bufferedReader = Utility.executeCommand(command, baseDir.getPath());
 			String line = null;
 			int count = 1;
 			while ((line = bufferedReader.readLine()) != null) {
 				if (count == 2) {
 					pid = line.trim();
 				}
 				count++;
 			}
 			Runtime.getRuntime().exec(Constants.JAVA_UNIX_PROCESS_KILL_CMD + pid);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeReader(bufferedReader);
 		}
 	}
 	
 	public static void checkForConfigurations(File baseDir, String environmentName) throws PhrescoException {
 		ConfigManager configManager = null;
 		PluginUtils pu = new PluginUtils();
 		try {
 			String customerId = pu.readCustomerId(baseDir);
 			File configFile = new File(baseDir.getPath() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE);
 			File settingsFile = new File(Utility.getProjectHome()+ customerId + PluginConstants.SETTINGS_FILE);
 			List<String> selectedEnvs = pu.csvToList(environmentName);
 			List<String> selectedConfigTypeList = pu.getSelectedConfigTypeList(baseDir);
 			List<String> nullConfig = new ArrayList<String>();
 			if (settingsFile.exists()) {
 				configManager = PhrescoFrameworkFactory.getConfigManager(settingsFile);
 				List<Environment> environments = configManager.getEnvironments();
 				for (Environment environment : environments) {
 					if (selectedEnvs.contains(environment.getName())) {
 						if (CollectionUtils.isNotEmpty(selectedConfigTypeList)) {
 							for (String selectedConfigType : selectedConfigTypeList) {
 								if(CollectionUtils.isEmpty(configManager.getConfigurations(environment.getName(), selectedConfigType))) {
 									nullConfig.add(selectedConfigType);
 								}
 							}
 						}
 					} if(CollectionUtils.isNotEmpty(nullConfig)) {
 						String errMsg = environment.getName() + " environment in global settings doesnot have "+ nullConfig + " configurations";
 						throw new PhrescoException(errMsg);
 					}
 				} 
 			} 
 			configManager = PhrescoFrameworkFactory.getConfigManager(configFile);
 			List<Environment> environments = configManager.getEnvironments();
 			for (Environment environment : environments) {
 				if (selectedEnvs.contains(environment.getName())) {
 					if (CollectionUtils.isNotEmpty(selectedConfigTypeList)) {
 						for (String selectedConfigType : selectedConfigTypeList) {
 							if(CollectionUtils.isEmpty(configManager.getConfigurations(environment.getName(), selectedConfigType))) {
 								nullConfig.add(selectedConfigType);
 							}
 						}
 					}
 				} if(CollectionUtils.isNotEmpty(nullConfig)) {
 					String errMsg = environment.getName() + " environment in " + baseDir.getName() + " doesnot have "+ nullConfig + " configurations";
 					throw new PhrescoException(errMsg);
 				}
 			} 
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private List<String> getSelectedConfigTypeList(File baseDir) throws PhrescoException {
 		try {
 			ApplicationInfo appInfo = getAppInfo(baseDir);
 			List<String> selectedList = new ArrayList<String>();
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedServers())) {
 				selectedList.add("Server");
 			}
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedDatabases())) {
 				selectedList.add("Database");
 			}
 			if(CollectionUtils.isNotEmpty(appInfo.getSelectedWebservices())) {
 				selectedList.add("WebService");
 			}
 			return selectedList;
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public ApplicationInfo getAppInfo(File baseDir) throws PhrescoException {
 		StringBuilder sb = new StringBuilder();
 		sb.append(baseDir.getPath())
 		.append(File.separator)
 		.append(Constants.DOT_PHRESCO_FOLDER)
 		.append(File.separator)
 		.append(Constants.PROJECT_INFO_FILE);
 		Gson gson = new Gson();
 		try {
 			FileReader filereader = new FileReader(new File(sb.toString()));
 			BufferedReader reader = new BufferedReader(filereader);
 			ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
 			return projectInfo.getAppInfos().get(0);
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String findPlatform() {
 		String osName = System.getProperty(Constants.OS_NAME);
 		if (osName.contains(Constants.WINDOWS)) {
 			osName = Constants.WINDOWS;
 		} else if (osName.contains(FrameworkConstants.LINUX)) {
 			osName = FrameworkConstants.LINUX;
 		} else if (osName.contains(FrameworkConstants.MAC)) {
 			osName = FrameworkConstants.MAC;
 		} else if (osName.contains(FrameworkConstants.SERVER)) {
 			osName = FrameworkConstants.SERVER;
 		} else if (osName.contains(FrameworkConstants.WINDOWS7)) {
 			osName = FrameworkConstants.WINDOWS7.replace(" ", "");
 		}
 		
 		return osName;
 	}
 
 	public List<com.photon.phresco.configuration.Configuration> getConfiguration(File baseDir, String environmentName,
 			String type) throws PhrescoException {
 		ConfigManager configManager = null;
 		try {
 			String customerId = readCustomerId(baseDir);
 			File settingsFile = new File(Utility.getProjectHome() + customerId + Constants.SETTINGS_XML);
 			if (settingsFile.exists()) {
 				configManager = new ConfigManagerImpl(settingsFile);
 				List<com.photon.phresco.configuration.Configuration> settingsconfig = configManager.getConfigurations(
 						environmentName, type);
 				if (CollectionUtils.isNotEmpty(settingsconfig)) {
 					return settingsconfig;
 				}
 			}
 			configManager = new ConfigManagerImpl(new File(baseDir.getPath() + File.separator
 					+ Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE));
 			List<com.photon.phresco.configuration.Configuration> configurations = configManager.getConfigurations(
 					environmentName, type);
 			if (CollectionUtils.isNotEmpty(configurations)) {
 				return configurations;
 			}
 
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 		return null;
 	}
 
 	public void writeDatabaseDriverToConfigXml(File baseDir, String sourceDir, String environmentName)
 			throws PhrescoException {
 		DatabaseUtil.initDriverMap();
 		try {
 			File configFile = new File(baseDir.getPath() + sourceDir + File.separator
 					+ Constants.CONFIGURATION_INFO_FILE);
 			if (!configFile.exists()) {
 				return;
 			}
 			DatabaseUtil dbutil = new DatabaseUtil();
 			List<String> envList = csvToList(environmentName);
 			for (String envName : envList) {
 				ConfigManager configManager = new ConfigManagerImpl(configFile);
 				List<com.photon.phresco.configuration.Configuration> configuration = configManager.getConfigurations(
 						envName, Constants.SETTINGS_TEMPLATE_DB);
 				if (CollectionUtils.isNotEmpty(configuration)) {
 					for (com.photon.phresco.configuration.Configuration config : configuration) {
 						Properties properties = config.getProperties();
 						String databaseType = config.getProperties().getProperty(Constants.DB_TYPE).toLowerCase();
 						String dbDriver = dbutil.getDbDriver(databaseType);
 						properties.setProperty(Constants.DB_DRIVER, dbDriver);
 						config.setProperties(properties);
 						configManager.createConfiguration(envName, config);
 						configManager.deleteConfiguration(envName, config);
 					}
 					configManager.writeXml(new FileOutputStream(configFile));
 				}
 			}
 		} catch (ConfigurationException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public String readCustomerId(File baseDir) throws PhrescoException {
 		ProjectInfo Projectinfo = null;
 		try {
 			File projectInfoPath = new File(baseDir.getPath() + File.separator + Constants.DOT_PHRESCO_FOLDER
 					+ File.separator + Constants.PROJECT_INFO_FILE);
 			if (projectInfoPath.exists()) {
 				BufferedReader bufferedReader = new BufferedReader(new FileReader(projectInfoPath));
 				Gson gson = new Gson();
 				Type type = new TypeToken<ProjectInfo>() {
 				}.getType();
 
 				Projectinfo = gson.fromJson(bufferedReader, type);
 			}
 			return Projectinfo.getCustomerIds().get(0);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public static void createBuildResources(File packageInfoFile, File baseDir, File tempDir) throws MojoExecutionException {
 		if(!packageInfoFile.exists()) {
 			return;
 		}
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder;
 		Document doc;
 		try {
 			dBuilder = dbFactory.newDocumentBuilder();
 			doc = dBuilder.parse(packageInfoFile);
 			doc.getDocumentElement().normalize();
 			NodeList nList = doc.getElementsByTagName(PluginConstants.ELEMENT_DIRECTORIES);
 			copyResources(nList, baseDir, tempDir);
 			nList = doc.getElementsByTagName(PluginConstants.ELEMENT_FILES);
 			copyResources(nList, baseDir, tempDir);
 		} catch (SAXException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} catch (ParserConfigurationException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	private static void copyResources(NodeList nList, File baseDir, File tempDir) throws MojoExecutionException {
 		for (int temp = 0; temp < nList.getLength(); temp++) {
 			Node nNode = nList.item(temp);
 			Element eElement = (Element) nNode;
 			NodeList attributes = eElement.getChildNodes();
 			for (int i = 0; i < attributes.getLength(); i++) {
 				Node node = attributes.item(i);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					Element element  = (Element) node;
 					if(StringUtils.isNotEmpty(element.getAttribute(PluginConstants.ATTR_TODIR))) {
 						copyToDirectory(baseDir, element.getAttribute(PluginConstants.ATTR_TODIR), node.getTextContent().trim(), tempDir);
 					} else {
 						copyFolders(baseDir, node.getTextContent().trim(), tempDir);
 					}
 				}
 			}
 		}
 	}
 	
 	private static void copyToDirectory(File projectBaseDir, String attribute, String source, File tempDir) throws MojoExecutionException {
 		if(StringUtils.isNotEmpty(attribute)) {
 			File cFile = new File(projectBaseDir, source);
 			File newFile = new File(tempDir, attribute);
 			if(!cFile.exists()) {
 				newFile.mkdirs();
 				return;
 			}
 			try {
 				if(cFile.exists()) {
 					if(cFile.isDirectory()) {
 						FileUtils.copyDirectory(cFile, newFile);
 					} else {
 						FileUtils.copyFileToDirectory(cFile, newFile);
 					}
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.getMessage());
 			}
 		}
 		
 	}
 
 	private static void copyFolders(File projectBaseDir, String folderToCopy, File tempDir) throws MojoExecutionException  {
 		if(StringUtils.isNotEmpty(folderToCopy)) {
 			File cFile = new File(projectBaseDir, folderToCopy);
 			if(!cFile.exists() && cFile.isDirectory()) {
 				File newFile = new File(tempDir, folderToCopy);
 				newFile.mkdirs();
 				return;
 			}
 			try {
 				if(cFile.exists()) {
 					if(cFile.isDirectory()) { 
 						FileUtils.copyDirectoryToDirectory(cFile, tempDir);
 					} else {
 						FileUtils.copyFileToDirectory(cFile, tempDir);
 					}
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.getMessage());
 			}
 		}
 	}
 	
 	public static List<String> getProjectModules(MavenProject project) throws PhrescoException {
     	try {
             StringBuilder builder = new StringBuilder();
             builder.append(project.getFile());
     		File pomPath = new File(builder.toString());
     		PomProcessor processor = new PomProcessor(pomPath);
     		Modules pomModule = processor.getPomModule();
     		List<String> moduleList = new ArrayList<String>();
     		if (pomModule != null) {
     			List<String> modules = pomModule.getModule();
     			for (String module : modules) {
 					String[] split = module.split("/");
 					if(split != null) {
 						module = split[0];
 					}
 					moduleList.add(module);
 				}
     			return moduleList;
     		}
     	} catch (PhrescoPomException e) {
     		 throw new PhrescoException(e);
     	}
     	return null;
     }
 	
 	public void delete(File file) {
 		// Check if file is directory/folder
 		if (file.isDirectory()) {
 			// Get all files in the folder
 			File[] files = file.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				// Delete each file in the folder
 				delete(files[i]);
 			}
 			// Delete the folder
 			file.delete();
 		} else {
 			// Delete the file if it is not a folder
 			file.delete();
 		}
 	}
 }
