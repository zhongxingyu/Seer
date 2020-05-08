 /*
  * ###
  * Framework Web Archive
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.framework.actions.applications;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.model.DependantParameters;
 import com.photon.phresco.framework.param.impl.IosTargetParameterImpl;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class Code extends DynamicParameterAction implements Constants {
     
 	private static final long serialVersionUID = 8217209827121703596L;
 	
     private static final Logger S_LOGGER = Logger.getLogger(Code.class);
     private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
     
     private String selectedModule = "";
     
 	/**
 	 * populate drop down with targets or list of code validation(js, web)
 	 * @return
 	 */
 	public String code() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Code.code()");
 		}
 		
 		try {
 		    removeSessionAttribute(getAppId() + SESSION_APPINFO);//To remove the appInfo from the session
         	ApplicationInfo appInfo = getApplicationInfo();
         	setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         	setReqAttribute(REQ_APP_INFO, appInfo);
         	
 			File pomPath = getPOMFile();
 			PomProcessor pomProcessor = new PomProcessor(pomPath);
 			String validateReportUrl = pomProcessor.getProperty(POM_PROP_KEY_VALIDATE_REPORT);
 			// when the report url is not available, it is for sonar
 			// if the report url is available, it is for clang report(iphone)
 			if (StringUtils.isNotEmpty(validateReportUrl)) {
 				setReqAttribute(CLANG_REPORT, validateReportUrl);
 				List<Value> values = getClangReports(appInfo);
 				setReqAttribute(REQ_VALIDATE_AGAINST_VALUES, values);
 			} else {
 	        	MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_VALIDATE_CODE)));
 				Parameter valdAgnstParam = mojo.getParameter(PHASE_VALIDATE_CODE, SONAR);
 				PossibleValues valdAgnstPsbleValues = valdAgnstParam.getPossibleValues();
 				List<Value> valdAgnstValues = valdAgnstPsbleValues.getValue();
 				setReqAttribute(REQ_VALIDATE_AGAINST_VALUES, valdAgnstValues);
 				Parameter sourceParam = mojo.getParameter(PHASE_VALIDATE_CODE, SOURCE);
 				if (sourceParam != null) {
                     PossibleValues sourcePsbleValues = sourceParam.getPossibleValues();
                     List<Value> sourceValues = sourcePsbleValues.getValue();
                     setReqAttribute(REQ_SOURCE_VALUES, sourceValues);
 				}
 				setProjModulesInReq();
 				setSonarServerStatus();
 			}
     	} catch (PhrescoException e) {
     		if (s_debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
     		return showErrorPopup(e, getText("excep.hdr.code.load"));
         } catch (PhrescoPomException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(new PhrescoException(e), getText("excep.hdr.code.load"));
         }
         
 		return APP_CODE;
 	}
 
 	private File getPOMFile() throws PhrescoException {
 		try {
             StringBuilder builder = new StringBuilder(getApplicationHome());
             builder.append(File.separator);
             builder.append(POM_XML);
             File pomPath = new File(builder.toString());
             return pomPath;
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
 	}
 
 	private void setSonarServerStatus() throws PhrescoException {		
 		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();		
 		if (s_debugEnabled) {
 			S_LOGGER.debug("sonar home url to check server status " + frameworkUtil.getSonarURL());
 		}
 		
 		try {
 			URL sonarURL = new URL(frameworkUtil.getSonarURL());
 			String protocol = sonarURL.getProtocol();
 			HttpURLConnection connection = null;
 			HttpsURLConnection connectionHttps = null; 
 			int responseCode;			
 			if(protocol.equals("http")) {				
 				connection = (HttpURLConnection) sonarURL.openConnection();
 				responseCode = connection.getResponseCode();				
 			} else {
 				try {
 					connectionHttps = (HttpsURLConnection) sonarURL.openConnection();
 					responseCode = connectionHttps.getResponseCode();
 				} catch (IOException e) {					
 					responseCode = 200;
 				}
 			}			
 			if (responseCode != 200) {
 				setReqAttribute(REQ_ERROR, getText(SONAR_NOT_STARTED));
 		    }
 			
 		} catch(Exception e) {
 			setReqAttribute(REQ_ERROR, getText(SONAR_NOT_STARTED));
 		}
 	}
 
 	private List<Value> getClangReports(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			IosTargetParameterImpl targetImpl = new IosTargetParameterImpl();
 			Map<String, Object> paramMap = new HashMap<String, Object>();
 			paramMap.put(KEY_APP_INFO, appInfo);
 			PossibleValues possibleValues = targetImpl.getValues(paramMap);
 			List<Value> values = possibleValues.getValue();
 			return values;
 		} catch (IOException e) {
            throw new PhrescoException(e);
         } catch (ParserConfigurationException e) {
             throw new PhrescoException(e);
         } catch (SAXException e) {
             throw new PhrescoException(e);
         } catch (ConfigurationException e) {
             throw new PhrescoException(e);
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
 	}
 	
 	/**
 	 * show code validation report
 	 */
 	public String check() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Code.check()");
 		}
 		
 		StringBuilder sb = new StringBuilder();
     	try {
 	        Properties sysProps = System.getProperties();
 	        if (s_debugEnabled) {
 	        	S_LOGGER.debug( "Phresco FileServer Value of " + PHRESCO_FILE_SERVER_PORT_NO + " is " + sysProps.getProperty(PHRESCO_FILE_SERVER_PORT_NO) );
 			}
 	        String phrescoFileServerNumber = sysProps.getProperty(PHRESCO_FILE_SERVER_PORT_NO);
             FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
             ApplicationInfo applicationInfo = getApplicationInfo();
             
         	File pomPath = getPOMFile();
             PomProcessor processor = new PomProcessor(pomPath);
             String validateAgainst = getReqParameter(REQ_VALIDATE_AGAINST); //getHttpRequest().getParameter("validateAgainst");
             String validateReportUrl = processor.getProperty(POM_PROP_KEY_VALIDATE_REPORT);
             
             //Check whether iphone Technology or not
 			if (StringUtils.isNotEmpty(validateReportUrl)) {
             	StringBuilder codeValidatePath = new StringBuilder(getApplicationHome());
             	codeValidatePath.append(validateReportUrl);
             	codeValidatePath.append(validateAgainst);
             	codeValidatePath.append(File.separatorChar);
             	codeValidatePath.append(INDEX_HTML);
                 File indexPath = new File(codeValidatePath.toString());
                 S_LOGGER.debug("indexPath ..... " + indexPath);
              	if (indexPath.isFile() && StringUtils.isNotEmpty(phrescoFileServerNumber)) {
                 	sb.append(HTTP_PROTOCOL);
                 	sb.append(PROTOCOL_POSTFIX);
                 	InetAddress thisIp =InetAddress.getLocalHost();
                 	sb.append(thisIp.getHostAddress());
                 	sb.append(COLON);
                 	sb.append(phrescoFileServerNumber);
                 	sb.append(FORWARD_SLASH);
                 	sb.append(getApplicationInfo().getAppDirName());
                 	sb.append(validateReportUrl);
                 	sb.append(validateAgainst);
                 	sb.append(FORWARD_SLASH);
                 	sb.append(INDEX_HTML);
                 	S_LOGGER.debug("File server path " + sb.toString());
              	} else {
              		setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
              	}
              	setReqAttribute(CLANG_REPORT, validateReportUrl);
         	} else {
 				Customer customer = getServiceManager().getCustomer(getCustomerId());
 				Map<String, String> theme = customer.getFrameworkTheme();
 				if (MapUtils.isNotEmpty(theme)) {
 					setReqAttribute(CUST_BODY_BACK_GROUND_COLOR, theme.get("bodyBackGroundColor"));
 					setReqAttribute(CUST_BRANDING_COLOR, theme.get("brandingColor"));
 					setReqAttribute(CUST_MENU_BACK_GROUND, theme.get("MenuBackGround"));
 					setReqAttribute(CUST_MENUFONT_COLOR, theme.get("MenufontColor"));
 					setReqAttribute(CUST_LABEL_COLOR, theme.get("LabelColor"));
 					setReqAttribute(CUST_DISABLED_LABEL_COLOR, theme.get("DisabledLabelColor"));
 					S_LOGGER.debug("Framework theme for customer==> " + getCustomerId());
 				} else {
 					setReqAttribute(REQ_CUSTOMER_ID, PHOTON);
 				}
 				
 	        	String serverUrl = "";
 	    		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
	    		serverUrl = frameworkUtil.getSonarURL();	    		
 				StringBuilder reportPath = new StringBuilder(getApplicationHome());
 				if (StringUtils.isNotEmpty(getSelectedModule())) {
 				    reportPath.append(File.separatorChar);
                     reportPath.append(getSelectedModule());
                 }
 				if (StringUtils.isNotEmpty(validateAgainst) && FUNCTIONALTEST.equals(validateAgainst)) {
 					reportPath.append(frameworkUtil.getFunctionalTestDir(applicationInfo));
                 }
 				reportPath.append(File.separatorChar);
 				reportPath.append(POM_XML);
 				File file = new File(reportPath.toString());
 				processor = new PomProcessor(file);
 				String groupId = processor.getModel().getGroupId();
 	        	String artifactId = processor.getModel().getArtifactId();
 	        	
 	        	sb.append(serverUrl);
 	        	sb.append(frameworkConfig.getSonarReportPath());
 	        	sb.append(groupId);
 	        	sb.append(COLON);
 	        	sb.append(artifactId);
 	        	
 	        	if (StringUtils.isNotEmpty(validateAgainst) && !REQ_SRC.equals(validateAgainst)) {
 	        		sb.append(COLON);
 	        		sb.append(validateAgainst);
 	        	}
 	    		try {
 	    			if (s_debugEnabled) {
 	    				S_LOGGER.debug("Url to access API " + sb.toString());
 	    			}
 					URL sonarURL = new URL(sb.toString());
 					HttpURLConnection connection = (HttpURLConnection) sonarURL.openConnection();
 					int responseCode = connection.getResponseCode();
 					if (s_debugEnabled) {
 	    				S_LOGGER.debug("Response code value " + responseCode);
 		    		}
 					if (responseCode != 200) {
 						setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
 					    return APP_CODE;
 		            }
 				} catch (Exception e) {
 					if (s_debugEnabled) {
 						S_LOGGER.error("Entered into catch block of Code.check()"+ FrameworkUtil.getStackTraceAsString(e));
 		    		}
 					new LogErrorReport(e, "Code review");
 					setReqAttribute(REQ_ERROR, getText(FAILURE_CODE_REVIEW));
 					return APP_CODE;
 				}
         	}
     	} catch (Exception e) {
     		if (s_debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.check()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
     		return showErrorPopup(new PhrescoException(e), getText("excep.hdr.code.load.report"));
     	}
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Sonar final report path " + sb.toString());
     	}
     	setReqAttribute(REQ_SONAR_PATH, sb.toString());
     	
         return APP_CODE;
     }
 	
 	/**
 	 * To show code validation popup
 	 * @return
 	 */
 	public String showCodeValidatePopup() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Code.showCodeValidatePopup()");
 		}
 		
 		try {
 		    ApplicationInfo appInfo = getApplicationInfo();
             removeSessionAttribute(appInfo.getId() + PHASE_VALIDATE_CODE + SESSION_WATCHER_MAP);
             setProjModulesInReq();
             Map<String, DependantParameters> watcherMap = new HashMap<String, DependantParameters>(8);
             
             MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_VALIDATE_CODE)));
             List<Parameter> parameters = getMojoParameters(mojo, PHASE_VALIDATE_CODE);
             
             setPossibleValuesInReq(mojo, appInfo, parameters, watcherMap, PHASE_VALIDATE_CODE);
             setSessionAttribute(appInfo.getId() + PHASE_VALIDATE_CODE + SESSION_WATCHER_MAP, watcherMap);
             setReqAttribute(REQ_DYNAMIC_PARAMETERS, parameters);
             setReqAttribute(REQ_GOAL, PHASE_VALIDATE_CODE);
             setReqAttribute(REQ_PHASE, PHASE_VALIDATE_CODE);
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Code.showCodeValidatePopup()" + FrameworkUtil.getStackTraceAsString(e));
 			}
 			return showErrorPopup(new PhrescoException(e), getText("excep.hdr.code.load.validate.popup"));
 		}
 		
 		return SHOW_CODE_VALIDATE_POPUP;
 	}
 	
 	private void setProjModulesInReq() throws PhrescoException {
         try {
             List<String> projectModules = getProjectModules(getApplicationInfo().getAppDirName());
             setReqAttribute(REQ_PROJECT_MODULES, projectModules);
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
     }
 	
 	/**
 	 * To run the code validation
 	 * @return
 	 */
 	public String codeValidate() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Code.codeValidate()");
 		}
 		
 		try {
 			ProjectInfo projectInfo = getProjectInfo();
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			MojoProcessor mojo = new MojoProcessor(new File(getPhrescoPluginInfoFilePath(PHASE_VALIDATE_CODE)));
 			persistValuesToXml(mojo, PHASE_VALIDATE_CODE);
 			
 			List<Parameter> parameters = getMojoParameters(mojo, PHASE_VALIDATE_CODE);
 			List<String> buildArgCmds = getMavenArgCommands(parameters);
 			buildArgCmds.add(HYPHEN_N);
 			String workingDirectory = getAppDirectoryPath(applicationInfo);
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			
 			BufferedReader reader = applicationManager.performAction(projectInfo, ActionType.CODE_VALIDATE, buildArgCmds, workingDirectory);
 			setSessionAttribute(getAppId() + REQ_CODE, reader);
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_ACTION_TYPE, REQ_CODE);
 		} catch (PhrescoException e) {
     		if (s_debugEnabled) {
     			S_LOGGER.error("Entered into catch block of Code.code()"+ FrameworkUtil.getStackTraceAsString(e));
     		}
     		return showErrorPopup(new PhrescoException(e), getText("excep.hdr.code.trigger.validate"));
 		}
 		
 		return APP_ENVIRONMENT_READER;
 	}
 
     public void setSelectedModule(String selectedModule) {
         this.selectedModule = selectedModule;
     }
 
     public String getSelectedModule() {
         return selectedModule;
     }
 }
