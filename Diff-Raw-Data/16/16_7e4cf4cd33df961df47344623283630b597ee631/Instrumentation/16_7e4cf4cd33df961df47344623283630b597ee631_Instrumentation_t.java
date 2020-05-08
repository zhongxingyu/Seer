 /**
  * Xcodebuild Command-Line Wrapper
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
 package com.photon.phresco.plugins.xcode;
 
 
 import java.io.*;
 import java.text.*;
 import java.text.ParseException;
 import java.util.*;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 
 import org.apache.commons.collections.*;
 import org.apache.commons.configuration.HierarchicalConfiguration.Node;
 import org.apache.commons.configuration.plist.*;
 import org.apache.commons.configuration.tree.*;
 import org.apache.commons.io.*;
 import org.apache.commons.lang.*;
 import org.apache.maven.plugin.*;
 import org.apache.maven.project.*;
 import org.w3c.dom.*;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.commons.model.*;
 import com.photon.phresco.plugin.commons.*;
 import com.photon.phresco.plugins.xcode.utils.*;
 import com.photon.phresco.util.Constants;
 
 /**
  * APP instrumentation
  * @goal instruments
  */
 public class Instrumentation extends AbstractXcodeMojo implements PluginConstants {
 	private static final String YES = "yes";
 
 	private static final String EEE_MMM_DD_HH_MM_SS_Z_YYYY = "EEE MMM dd hh:mm:ss z yyyy";
 
 	private static final String LOG_TYPE = "LogType";
 
 	private static final String TIMESTAMP = "Timestamp";
 	private static final String DEVICE_DEPLOY = "deviceDeploy";
 	private static final String CAN_CREATE_IPA = "canCreateIpa";
 	
 	/**
 	 * @parameter experssion="${command}" default-value="instruments"
 	 */
 	private String command;
 	
 	/**
 	 * The maven project.
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 	
 	/**
 	 * @parameter expression="${template}" default-value="/Developer/Platforms/iPhoneOS.platform/Developer/Library/Instruments/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate"
 	 */
 	private String template;
 	
 	/**
 	 * This should be either device or template
 	 * @parameter expression="${deviceId}"
 	 */
 	private String deviceid;
 	
 	/**
 	 * @parameter expression="${pid}"
 	 */
 	private String pid;
 	
 	/**
 	 * @parameter expression="${verbose}" default-value="false"
 	 */
 	private boolean verbose;
 	
 	private String appPath;
 	
 	/**
 	 * @parameter expression="${script.name}" default-value="test/functional/src/AllTests.js"
 	 */
 	private String script;
 	
 	/**
 	 * @parameter expression="${script.name}" default-value="Run 1/Automation Results.plist"
 	 */
 	private String plistResult;
 	
 	/**
 	 * @parameter expression="${script.name}" default-value="do_not_checkin/functional.xml"
 	 */
 	public String xmlResult;
 	
 	/**
 	 * @parameter expression="${project.basedir}" required="true"
 	 * @readonly
 	 */
 	protected File baseDir;
 	
 	/**
 	 * @parameter expression="${buildNumber}" required="true"
 	 */
 	protected String buildNumber;
 	
 	/**
 	 * @parameter 
 	 */
 	private String outputFolder;
 	
 	/**
 	 * @parameter 
 	 */
 	private static XMLPropertyListConfiguration config;
 	private File buildInfoFile;
 	
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		getLog().info("Instrumentation command" + command);
 		try {
 			List<String> traceTemplates = new ArrayList<String>(4);
 			traceTemplates.add("/Developer/Platforms/iPhoneOS.platform/Developer/Library/Instruments/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate");
 			traceTemplates.add("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/Library/Instruments/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate");
 			traceTemplates.add("/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate");
 			
 			for (String traceTemplate : traceTemplates) {
 				File traceTemplateFile = new File(traceTemplate);
 				if (traceTemplateFile.isFile()) {
 					getLog().info("Template found at " + traceTemplate);
 					template = traceTemplate;
 				}
 			}
 			
 			if (StringUtils.isEmpty(template)) {
 				throw new MojoExecutionException("Template not found");
 			}
 //			if (SdkVerifier.isAvailable("iphonesimulator5.1")) {
 //				template = "/Applications/Xcode.app/Contents" + template;
 //			} else if (SdkVerifier.isAvailable("iphonesimulator6.0")) {
 //				template = "/Applications/Xcode.app/Contents/Applications/Instruments.app/Contents/PlugIns/AutomationInstrument.bundle/Contents/Resources/Automation.tracetemplate";	
 //			}
         } catch (Exception e2) {
         	throw new MojoExecutionException("Template interrupted!");
         }
 		
 		if (StringUtils.isEmpty(buildNumber)) {
 			throw new MojoExecutionException("Selected build is not available!");
 		}
 		getLog().info("Build id is " + buildNumber);
 		getLog().info("Project Code " + baseDir.getName());
 		PluginUtils pu = new PluginUtils();
 		BuildInfo buildInfo = pu.getBuildInfo(Integer.parseInt(buildNumber));
 //		appPath = buildInfo.getBuildName();
 		appPath = buildInfo.getDeployLocation();
 		getLog().info("Application.path = " + appPath);
 		
 		// if the build is build for device, pass as param
         Map<String, Boolean> options = buildInfo.getOptions();
         if (options != null) {
         	boolean createIpa = MapUtils.getBooleanValue(buildInfo.getOptions(), CAN_CREATE_IPA);
         	boolean deviceDeploy = MapUtils.getBooleanValue(buildInfo.getOptions(), DEVICE_DEPLOY);
         	if (!createIpa && !deviceDeploy) { // if it is simulator build, do not pass -w
         		deviceid = "";
         	}
         }
 		
 		try {
 			outputFolder = project.getBasedir().getAbsolutePath();
 			File f = new File(outputFolder);
 			File files[] = f.listFiles();
 			for(File file : files) {
 				if(file.getName().startsWith("Run 1" ) || file.getName().endsWith(".trace")) {
 					FileUtils.deleteDirectory(file);		
 				}
 			}
 		} catch (IOException e) {
 			getLog().error(e);
 		}
 			
 			Runnable runnable = new Runnable() {
 				public void run() {
 					ProcessBuilder pb = new ProcessBuilder(command);
 					//device takes the highest priority
 					if(StringUtils.isNotEmpty(deviceid)) {
 						pb.command().add("-w");
 						pb.command().add(deviceid);
 					}
 					pb.command().add("-t");
 					pb.command().add(template);
 
 					if(StringUtils.isNotBlank(appPath)) {
 						pb.command().add(appPath);
 					} else {
 						getLog().error("Application should not be empty");
 					}
 					if(StringUtils.isNotBlank(script)) {
						System.out.println("script is not empty");
 						pb.command().add("-e");
 						pb.command().add("UIASCRIPT");
						
 						String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
						String scriptPath=null;
						if(dotPhrescoDirName!=null){
					      scriptPath = project.getBasedir().getParentFile()+File.separator+dotPhrescoDirName+File.separator+script;
						}else{
							 scriptPath = project.getBasedir()+File.separator+script;
						}
 						pb.command().add(scriptPath);
						
 					} else {
 						getLog().error("script is empty");
 					}
 					
 					pb.command().add("-e");
					pb.command().add("UIARESULTSPATH");		
 					pb.command().add(outputFolder);
 					
 					// Include errors in output
 					pb.redirectErrorStream(true);
 
 					getLog().info("List of commands"+pb.command());
 					Process child;
 					try {
 						child = pb.start();
 						// Consume subprocess output and write to stdout for debugging
 						InputStream is = new BufferedInputStream(child.getInputStream());
 						int singleByte = 0;
 						while ((singleByte = is.read()) != -1) {
 							System.out.write(singleByte);
 						}
 					} catch (IOException e) {
 						getLog().error(e);
 					}
 										
 				}
 		
 			};
 			
 		   
 		   
 			Thread t = new Thread(runnable, "iPhoneSimulator");
 			t.start();
 			getLog().info("Thread started");
 			try {
 				//Thread.sleep(5000);
 				t.join();
 			} catch (InterruptedException e) {
 				getLog().error(e);
 			}
 
 			preparePlistResult();
 			getLog().info("Plist path ... " + project.getBasedir().getAbsolutePath()+File.separator+plistResult);
 			generateXMLReport(project.getBasedir().getAbsolutePath()+File.separator+plistResult);
 	}
 
 			
 
 	private void preparePlistResult() throws MojoExecutionException {
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			Document xmldoc = dbf.newDocumentBuilder().parse(
 					new File(project.getBasedir().getAbsolutePath()+File.separator+plistResult));
 
 			StreamResult out = new StreamResult(project.getBasedir().getAbsolutePath()+File.separator+plistResult);
 			
 			DOMSource domSource = new DOMSource(xmldoc);
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "file://localhost/System/Library/DTDs/PropertyList.dtd");
 			transformer.transform(domSource , out);
 		} catch (Exception e) { 
 //			getLog().error(e.getLocalizedMessage());
 			throw new MojoExecutionException(e.getLocalizedMessage());
 		}
 	}
 
 //    public static void main(String[] args) {
 //        try {
 //            new Instrumentation().generateXMLReport("/Users/kal/Notes/XCOde-Plist/Issue.plist");
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //        }
 //    }
 	
 	private void generateXMLReport(String location) throws MojoExecutionException {
 		getLog().info("xml generation started");
 		try {
 			String startTime = "";
 			int total,pass,fail;
 			total=pass=fail=0;
 			config = new XMLPropertyListConfiguration(location);
 			// getting all <dict> tags
 			ArrayList list =  (ArrayList) config.getRoot().getChild(0).getValue();
 			String key;
 
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 
 			Element root = doc.createElement(XMLConstants.TESTSUITES_NAME);
 			doc.appendChild(root);
 			Element testSuite = doc.createElement(XMLConstants.TESTSUITE_NAME);
 			testSuite.setAttribute(XMLConstants.NAME, "FunctionalTestSuite");
 			root.appendChild(testSuite);
 
 
 			for (Object object : list) {
 				// getting start time from initial <dict tag>
 				XMLPropertyListConfiguration config = (XMLPropertyListConfiguration) object;
 				startTime = getValueOfNode(config.getRoot(), TIMESTAMP);
 				break;
 			}
 
 			for (Object object : list) {
 				// getChild(0) - <key>LogType</key>
 				// getChild(1) - <key>Message</key>
 				// getChild(2) - <key>Timestamp</key>
 				XMLPropertyListConfiguration config = (XMLPropertyListConfiguration) object;
 
 				ConfigurationNode con = getConfigurationOfNode(config.getRoot(), LOG_TYPE);
 				
 				if (con != null) {
 				
 					key = con.getName();
 					if(key.equals(XMLConstants.LOGTYPE) && con.getValue().equals(XMLConstants.PASS)){
 						pass++;total++;
 						Element child1 = doc.createElement(XMLConstants.TESTCASE_NAME);
 						child1.setAttribute(XMLConstants.NAME,(String) config.getRoot().getChild(1).getValue());
 						child1.setAttribute(XMLConstants.RESULT,(String) con.getValue());
 	
 						String endTime = getValueOfNode(config.getRoot(), TIMESTAMP);
 						
 						long differ = getTimeDiff(startTime, endTime);
 						startTime = endTime;
 						child1.setAttribute(XMLConstants.TIME, differ+"");
 						testSuite.appendChild(child1);
 					} else if (key.equals(XMLConstants.LOGTYPE) && (con.getValue().equals(XMLConstants.ERROR) || con.getValue().equals(XMLConstants.FAIL))) {
 						fail++;total++;
 						Element child1 = doc.createElement(XMLConstants.TESTCASE_NAME);
 						child1.setAttribute(XMLConstants.NAME,(String) config.getRoot().getChild(1).getValue());
 						child1.setAttribute(XMLConstants.RESULT,(String) con.getValue());
 						String endTime = getValueOfNode(config.getRoot(), TIMESTAMP);
 						
 						List<ConfigurationNode> children = (List<ConfigurationNode>) config.getRoot().getChildren();
 						String errorTextNodes = XMLConstants.DICT_START;
 						for (ConfigurationNode child : children) {
 							errorTextNodes = errorTextNodes + XMLConstants.KEY_START + child.getName() + XMLConstants.KEY_END;
 							errorTextNodes = errorTextNodes + XMLConstants.STRING_START + child.getValue() + XMLConstants.STRING_END;
 						}
 						errorTextNodes = errorTextNodes + XMLConstants.DICT_END;
 						
 						getLog().info("error node " + errorTextNodes);
 						long differ = getTimeDiff(startTime, endTime);
 						startTime = endTime;
 						child1.setAttribute(XMLConstants.TIME, differ+"");
 						//adding error element
 						
 						if (con.getValue().equals(XMLConstants.ERROR)) {
 							Element errorElem = doc.createElement(XMLConstants.ELEM_ERROR);
 							errorElem.setAttribute(XMLConstants.TYPE, "Exception");
 							errorElem.setTextContent(errorTextNodes);
 							child1.appendChild(errorElem);
 						} else {
 							Element failureElem = doc.createElement(XMLConstants.ELEM_FAILURE);
 							failureElem.setAttribute(XMLConstants.TYPE, "Failure");
 							failureElem.setTextContent(errorTextNodes);
 							child1.appendChild(failureElem);
 						}
 						
 						testSuite.appendChild(child1);
 					}
 				}
 
 			}
 			
 			testSuite.setAttribute(XMLConstants.TESTS, String.valueOf(total));
 			testSuite.setAttribute(XMLConstants.SUCCESS, String.valueOf(pass));
 			testSuite.setAttribute(XMLConstants.FAILURES, String.valueOf(fail));
 
 			getLog().info("Total " + total);
 			getLog().info("Success " + pass);
 			getLog().info("Failure " + fail);
 
 			TransformerFactory transfac = TransformerFactory.newInstance();
 			Transformer trans = transfac.newTransformer();
 			trans.setOutputProperty(OutputKeys.INDENT, YES);
 
 //			File file = new File("/Users/kaleeswaran/Notes/XCOde-Plist/DRIssue.xml");
 			getLog().info("functional xml file generated ... ");
 			File file = new File(project.getBasedir().getAbsolutePath()+File.separator+xmlResult);
 			Writer bw = new BufferedWriter(new FileWriter(file));
 			StreamResult result = new StreamResult(bw);
 			DOMSource source = new DOMSource(doc);
 			trans.transform(source, result);
 
 		} catch (Exception e) {
 //			getLog().error(e.getLocalizedMessage());
 			throw new MojoExecutionException(e.getLocalizedMessage());
 		}
 	}
 
     private String getValueOfNode(Node rootNode, String nodeName) {
         List<ConfigurationNode> children = (List<ConfigurationNode>) rootNode.getChildren();
         String value = "";
         for (ConfigurationNode child : children) {
             if (nodeName.equals(child.getName())) {
                 value = child.getValue().toString();
                 break;
             }
         }
         return value;
     }
     
     private ConfigurationNode getConfigurationOfNode(Node rootNode, String nodeName) {
         List<ConfigurationNode> children = (List<ConfigurationNode>) rootNode.getChildren();
         for (ConfigurationNode child : children) {
             if (nodeName.equals(child.getName())) {
                 return child;
             }
         }
 		return null;
     }
     
 	private long getTimeDiff(String dateStart, String dateStop) {
 		//TODO try to get the system time format.
 		SimpleDateFormat format = new SimpleDateFormat(EEE_MMM_DD_HH_MM_SS_Z_YYYY);  
 		Date d1 = null;
 		Date d2 = null;
 		try {
 			d1 = format.parse(dateStart);
 			d2 = format.parse(dateStop);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}    
 
 		long diff = d2.getTime() - d1.getTime();
 		return diff;
 	}
 	
 }
