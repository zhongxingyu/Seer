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
 package com.photon.phresco.framework.commons;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues.Value;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.model.Model.Profiles;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 
 public class FrameworkUtil extends FrameworkBaseAction implements Constants {
 
 	private static final long serialVersionUID = 1L;
 	private static FrameworkUtil frameworkUtil = null;
     private static final Logger S_LOGGER = Logger.getLogger(FrameworkUtil.class);
     
 	public static FrameworkUtil getInstance() throws PhrescoException {
         if (frameworkUtil == null) {
             frameworkUtil = new FrameworkUtil();
         }
         return frameworkUtil;
     }
 	
 	public String getSqlFilePath(String oldAppDirName) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(oldAppDirName).getProperty("phresco.sql.path");
 	}
 	
 	public String getUnitTestReportOptions(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
 		return getPomProcessor(appinfo.getAppDirName()).getProperty("phresco.unitTest");
 	}
 	
     public String getUnitTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_DIR);
     }
     
     public String getUnitTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR);
     }
     
     public String getUnitTestReportDir(ApplicationInfo appInfo, String option) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_RPT_DIR_START + option + POM_PROP_KEY_UNITTEST_RPT_DIR_END);
     }
 
 	public String getUnitTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH);
     }
 	
 	public String getUnitTestSuitePath(ApplicationInfo appInfo, String option) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_START + option + POM_PROP_KEY_UNITTEST_TESTSUITE_XPATH_END);
     }
     
     public  String getUnitTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTCASE_PATH);
     }
     
     public  String getUnitTestCasePath(ApplicationInfo appInfo, String option) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_UNITTEST_TESTCASE_PATH_START + option + POM_PROP_KEY_UNITTEST_TESTCASE_PATH_END);
     }
     
     public String getSeleniumToolType(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
     }
     
     public String getFunctionalTestDir(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_DIR);
     }
     
     public String getFunctionalTestReportDir(ApplicationInfo appInfo) throws PhrescoPomException, PhrescoException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_RPT_DIR);
     }
 
     public String getFunctionalTestSuitePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH);
     }
     
     public  String getFunctionalTestCasePath(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_FUNCTEST_TESTCASE_PATH);
     }
     
     public String getLoadTestDir(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
     	return getPomProcessor(appInfo.getAppDirName()).getProperty(POM_PROP_KEY_LOADTEST_DIR);
     }
     
     public String getLoadTestReportDir(ApplicationInfo appinfo) throws PhrescoPomException, PhrescoException {
     	return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_LOADTEST_RPT_DIR);
     }
     
     public String getPerformanceTestDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERFORMANCETEST_DIR);
     }
     
     public String getPerformanceTestShowDevice(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERF_SHOW_DEVICE);
     }
     
     public String getPerformanceTestReportDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_PERFORMANCETEST_RPT_DIR);
     }
     
     public String getEmbedAppTargetDir(ApplicationInfo appinfo) throws PhrescoException, PhrescoPomException {
         return getPomProcessor(appinfo.getAppDirName()).getProperty(POM_PROP_KEY_EMBED_APP_TARGET_DIR);
     }
 
 	public String getHubConfigFile(ApplicationInfo appInfo) throws PhrescoException, PhrescoPomException {
         StringBuilder sb = new StringBuilder(Utility.getProjectHome());
         sb.append(appInfo.getAppDirName());
         sb.append(File.separator);
         sb.append(getFunctionalTestDir(appInfo));
         sb.append(File.separator);
         sb.append("hubconfig.json");
         return sb.toString();
     }
 
     public static String getStackTraceAsString(Exception exception) {
 	    StringWriter sw = new StringWriter();
 	    PrintWriter pw = new PrintWriter(sw);
 	    pw.print(" " + SQUARE_OPEN + " ");
 	    pw.print(exception.getClass().getName());
 	    pw.print(" " + SQUARE_CLOSE + " ");
 	    pw.print(exception.getMessage());
 	    pw.print(" ");
 	    exception.printStackTrace(pw);
 	    return sw.toString();
     }
     
     public static String removeFileExtension(String fileName) {
     	fileName = fileName.substring(0, fileName.lastIndexOf('.'));
     	return fileName;
     }
     
     public static float roundFloat(int decimal, double value) {
 		BigDecimal roundThroughPut = new BigDecimal(value);
 		return roundThroughPut.setScale(decimal, BigDecimal.ROUND_HALF_EVEN).floatValue();
 	}
     
     public static String convertToCommaDelimited(String[] list) {
         StringBuffer ret = new StringBuffer("");
         for (int i = 0; list != null && i < list.length; i++) {
             ret.append(list[i]);
             if (i < list.length - 1) {
                 ret.append(',');
             }
         }
         return ret.toString();
     }
     
     public static void copyFile(File srcFile, File dstFile) throws PhrescoException {
     	try {
     		if (!dstFile.exists()) {
     			dstFile.getParentFile().mkdirs();
     			dstFile.createNewFile();
     		}
 			FileUtils.copyFile(srcFile, dstFile);
 		} catch (Exception e) {
 			throw new PhrescoException();
 		}
     }
     
     public PomProcessor getPomProcessor(String appDirName) throws PhrescoException {
     	try {
     		StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     		builder.append(appDirName);
     		builder.append(File.separatorChar);
     		builder.append(POM_XML);
     		S_LOGGER.debug("builder.toString() " + builder.toString());
     		File pomPath = new File(builder.toString());
     		S_LOGGER.debug("file exists " + pomPath.exists());
     		return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
     }
     
     //get server Url for sonar
     public String getSonarHomeURL() throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = "";
     	
 	    if (StringUtils.isNotEmpty(frameworkConfig.getSonarUrl())) {
 	    	serverUrl = frameworkConfig.getSonarUrl();
 	    	S_LOGGER.debug("if condition serverUrl  " + serverUrl);
 	    } else {
 	    	serverUrl = getHttpRequest().getRequestURL().toString();
 	    	StringBuilder tobeRemoved = new StringBuilder();
 	    	tobeRemoved.append(getHttpRequest().getContextPath());
 	    	tobeRemoved.append(getHttpRequest().getServletPath());
 
 	    	Pattern pattern = Pattern.compile(tobeRemoved.toString());
 	    	Matcher matcher = pattern.matcher(serverUrl);
 	    	serverUrl = matcher.replaceAll("");
 	    	S_LOGGER.debug("else condition serverUrl  " + serverUrl);
 	    }
 	    return serverUrl;
     }
     
     //get server Url for sonar
     public String getSonarURL() throws PhrescoException {
     	FrameworkConfiguration frameworkConfig = PhrescoFrameworkFactory.getFrameworkConfig();
     	String serverUrl = getSonarHomeURL();
     	S_LOGGER.debug("serverUrl ... " + serverUrl);
 	    String sonarReportPath = frameworkConfig.getSonarReportPath();
 	    S_LOGGER.debug("sonarReportPath ... " + sonarReportPath);
 	    String[] sonar = sonarReportPath.split("/");
 	    S_LOGGER.debug("sonar[1] " + sonar[1]);
 	    serverUrl = serverUrl.concat(FORWARD_SLASH + sonar[1]);
 	    S_LOGGER.debug("Final url => " + serverUrl);
 	    return serverUrl;
     }
     
 	public List<String> getSonarProfiles() throws PhrescoException {
 		List<String> sonarTechReports = new ArrayList<String>(6);
 		try {
 			StringBuilder pomBuilder = new StringBuilder(getApplicationHome());
 			pomBuilder.append(File.separator);
 			pomBuilder.append(POM_XML);
 			File pomPath = new File(pomBuilder.toString());
 			PomProcessor pomProcessor = new PomProcessor(pomPath);
 			Model model = pomProcessor.getModel();
 			S_LOGGER.debug("model... " + model);
 			Profiles modelProfiles = model.getProfiles();
 			if (modelProfiles == null) {
 				return sonarTechReports;
 			}
 			S_LOGGER.debug("model Profiles... " + modelProfiles);
 			List<Profile> profiles = modelProfiles.getProfile();
 			if (profiles == null) {
 				return sonarTechReports;
 			}
 			S_LOGGER.debug("profiles... " + profiles);
 			for (Profile profile : profiles) {
 				S_LOGGER.debug("profile...  " + profile);
 				if (profile.getProperties() != null) {
 					List<Element> any = profile.getProperties().getAny();
 					int size = any.size();
 					
 					for (int i = 0; i < size; ++i) {
 						boolean tagExist = 	any.get(i).getTagName().equals(SONAR_LANGUAGE);
 						if (tagExist){
 							S_LOGGER.debug("profile.getId()... " + profile.getId());
 							sonarTechReports.add(profile.getId());
 						}
 					}
 				}
 			}
 			S_LOGGER.debug("return from getSonarProfiles");
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return sonarTechReports;
 	}
 	
 	/**
 	 * To encrypt the given string
 	 * @param inputString
 	 * @return
 	 */
 	public static String encryptString(String inputString) {
         byte[] encodeBase64 = Base64.encodeBase64(inputString.getBytes());
         String encodedString = new String(encodeBase64);
 
         return encodedString;
     }
 	
 	/**
 	 * To decrypt the given string
 	 * @param inputString
 	 * @return
 	 */
 	public static String decryptString(String inputString) {
         byte[] decodedBytes = Base64.decodeBase64(inputString);
         String decodedString = new String(decodedBytes);
 
         return decodedString;
     }
 
     public static StringTemplate constructInputElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	String type = getInputType(pm.getInputType());
     	StringTemplate inputElement = null;
     	if(TYPE_SCHEDULER.equals(type)) {
     		inputElement = new StringTemplate(getSchedulerTemplate());
     	} else if(!TYPE_SCHEDULER.equals(type)) {
     		inputElement = new StringTemplate(getInputTemplate());
     	}
     	inputElement.setAttribute("type", type);
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("placeholder", pm.getPlaceHolder());
     	inputElement.setAttribute("value", pm.getValue());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
 		return controlGroupElement;
     }
 
 	/**
 	 * @param inputType
 	 * @return
 	 */
 	private static String getInputType(String inputType) {
 		String type = "";
 		if (TYPE_PASSWORD.equalsIgnoreCase(inputType)) {
     		type = TYPE_PASSWORD;
 		}  else if (TYPE_HIDDEN.equalsIgnoreCase(inputType)) {
 			type = TYPE_HIDDEN;
 		} else if (TYPE_SCHEDULER.equalsIgnoreCase(inputType)) {
 			type = TYPE_SCHEDULER;
 		} else {
 			type = TEXT_BOX;
 		}
 		
 		return type;
 	}
     
 	public static StringTemplate constructMapElement(ParameterModel pm) throws IOException {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	Properties properties = new Properties();
 		StringReader reader = new StringReader(pm.getValue());
 		properties.load(reader);
 		Set<Object> keySet = properties.keySet();
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	if (pm.getValue().isEmpty()) {
 	    	StringTemplate mapElement = new StringTemplate(getMapTemplate());
 	    	if (pm.isMandatory()) {
 	    		mapElement.setAttribute("mandatory", getMandatoryTemplate());
 	    	}
 	    	
 	    	mapElement.setAttribute("legendHeader", pm.getLableText());
 	    	List<BasicParameterModel> childs = pm.getChilds();
 	    	for (BasicParameterModel child : childs) {
 	    	    StringTemplate childElement = new StringTemplate();
 	            if (child.getInputType().equalsIgnoreCase(TYPE_LIST)) {
 	                childElement = new StringTemplate(getMapSelectElement());
 	                StringBuilder options = constructOptions(child.getObjectValue(), null, null, "");
 	                childElement.setAttribute("options", options);
 	            } else if (child.getInputType().equalsIgnoreCase(TYPE_STRING)) {
 	                childElement = new StringTemplate(getMapInputElement());
 	            }
 	            childElement.setAttribute("name", child.getName());
 	            updateChildLabels(mapElement, child);
 	            mapElement.setAttribute("mapControls", childElement);
 	        }
 	    	controlGroupElement.setAttribute("lable", mapElement);
     	} else {
     		StringTemplate mapElmnt = new StringTemplate(getMapTemplatesForValues());
 	    	if (pm.isMandatory()) {
 	    		mapElmnt.setAttribute("mandatory", getMandatoryTemplate());
 	    	}
 	    	
 	    	mapElmnt.setAttribute("legendHeader", pm.getLableText());
 	    	List<BasicParameterModel> childs = pm.getChilds();
 			
     	    StringTemplate childElement = new StringTemplate();
     	    
 	    	for (Object object : keySet) {
     			String key =(String)object;
     			String value = properties.getProperty(key);
     			StringTemplate parentTr = new StringTemplate(getMapTableRowElement());
     			//For left side controls
 	            if (childs.get(0).getInputType().equalsIgnoreCase(TYPE_LIST)) {
 	                childElement = new StringTemplate(getMapSelectElement());
 	                List<String> selectedKeys = new ArrayList<String>();
 	                selectedKeys.add(key);
 	                StringBuilder options = constructOptions(childs.get(0).getObjectValue(), selectedKeys, null, "");
 	                childElement.setAttribute("options", options);
 	                childElement.setAttribute("value", key);
 	                childElement.setAttribute("name", childs.get(0).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            } else if (childs.get(0).getInputType().equalsIgnoreCase(TYPE_STRING)) {
 	                childElement = new StringTemplate(getMapInputElement());
 	                childElement.setAttribute("value", key);
 	                childElement.setAttribute("name", childs.get(0).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            }
 	          //For right side controls  with plus, minus icons
 	            if (childs.get(1).getInputType().equalsIgnoreCase(TYPE_LIST)) {
 	                childElement = new StringTemplate(getMapSelectElement());
 	                List<String> selectedValues = new ArrayList<String>();
 	                selectedValues.add(value);
 	                StringBuilder options = constructOptions(childs.get(1).getObjectValue(), selectedValues, null, "");
 	                childElement.setAttribute("options", options);
 	                childElement.setAttribute("name", childs.get(1).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	                childElement = new StringTemplate(getMapPlusMinusIconElement(keySet.size()));
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	                childElement.setAttribute("value", value);
 	            } else if (childs.get(1).getInputType().equalsIgnoreCase(TYPE_STRING)) {
 	                childElement = new StringTemplate(getMapInputElement());
 	                childElement.setAttribute("value", value);
 	                childElement.setAttribute("name", childs.get(1).getName());
 	                parentTr.setAttribute("mapTdContrls", childElement);
                 	childElement = new StringTemplate(getMapPlusMinusIconElement(keySet.size()));
 	                parentTr.setAttribute("mapTdContrls", childElement);
 	            }
 	            
 	            mapElmnt.setAttribute("mapTrContrls", parentTr);
 	        }
             updateChildLabels(mapElmnt, childs.get(0));
             
             
             updateChildLabels(mapElmnt, childs.get(1));
             
 	    	controlGroupElement.setAttribute("lable", mapElmnt);
          }
 		return controlGroupElement;
 	}
 	
 	public static StringTemplate constructCustomParameters(ParameterModel pm) {
 	    StringTemplate controlGroupElement = new StringTemplate(getCustomParamTableTemplate(pm.getValue(), pm.getObjectValue(), pm.isShowMinusIcon()));
 	    return controlGroupElement;
 	}
 	
 	private static String getCustomParamTableTemplate(String key, List<? extends Object> value, boolean showMinus) {
         StringBuilder sb = new StringBuilder();
         sb.append("<table class='custParamTable'>")
         .append("<tbody id='propTempTbodyForHeader'>")
         .append("<tr class='borderForLoad'>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"key\" placeholder=\"Key\" value="+key+">")
         .append("</td>")
         .append("<td class=\"noBorder\">")
         .append("<input type=\"text\" class=\"input-medium\" ")
         .append("name=\"value\" placeholder=\"Value\" value="+value.get(0)+">")
         .append("</td>")
         .append("<td class='borderForLoad noBorder'>")
         .append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>");
         if (showMinus) {
         	sb.append("<td class='borderForLoad noBorder'><a><img class='add imagealign' src='images/icons/minus_icon.png' onclick='removeRow(this)'></a></td>");
         }
         sb.append("</tr></tbody></table>");
         
         return sb.toString();
     }
 	
     private static void updateChildLabels(StringTemplate mapElement, BasicParameterModel child) {
         String keyLabel = (String) mapElement.getAttribute("keyLabel");
         if (StringUtils.isEmpty(keyLabel)) {
             mapElement.setAttribute("keyLabel", child.getLableText());
         } else {
             mapElement.setAttribute("valueLabel", child.getLableText());
         }
         mapElement.setAttribute("childLabel", child.getLableText());
     }
 
     public static StringTemplate constructCheckBoxElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate checkboxElement = new StringTemplate(getCheckBoxTemplate());
     	checkboxElement.setAttribute("class", pm.getCssClass());
     	checkboxElement.setAttribute("id", pm.getId());
     	checkboxElement.setAttribute("name", pm.getName());
     	checkboxElement.setAttribute("onClickFunction", pm.getOnClickFunction());
     	checkboxElement.setAttribute("onChangeFunction", pm.getOnChangeFunction());
     	
     	if (StringUtils.isNotEmpty(pm.getValue())) {
     		checkboxElement.setAttribute("value", pm.getValue());	
     	} else {
     		checkboxElement.setAttribute("value", false);
     	}
     	
     	if (Boolean.parseBoolean(pm.getValue())) {
     		checkboxElement.setAttribute("checked", "checked");
     	} else {
     		checkboxElement.setAttribute("checked", "");
     	}
     	checkboxElement.setAttribute("ctrlsId", pm.getControlId());
     	String additionalParam = getAdditionalParam(pm.getValue(), pm.getDependency());
         if (StringUtils.isNotEmpty(additionalParam)) {
             StringBuilder builder = new StringBuilder("additionalParam='dependency=");
             builder.append(additionalParam);
             builder.append("' ");
             checkboxElement.setAttribute("additionalParam", builder);
         }
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", checkboxElement);
     	
     	return controlGroupElement;
     }
     
     public static StringTemplate constructSelectElement(ParameterModel pm) {
     	if (pm.isMultiple()) {
     		return constructMultiSelectElement(pm);
     	} else {
     		return constructSingleSelectElement(pm);
     	}
     }
     
     public static StringTemplate constructConfigMultiSelectBox(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate multiSelectElement = new StringTemplate(getConfigMultiSelectTemplate());
     	multiSelectElement.setAttribute("cssClass", pm.getCssClass());
     	multiSelectElement.setAttribute("id", pm.getId());
     	multiSelectElement.setAttribute("name", pm.getId());
     	
     	StringBuilder multiSelectOptions = constructConfigMultiSelectOptions(pm.getName(), pm.getObjectValue(), pm.getSelectedValues(), pm.getId());
     	multiSelectElement.setAttribute("multiSelectOptions", multiSelectOptions);
     	multiSelectElement.setAttribute("ctrlsId", pm.getControlId());
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", multiSelectElement);
     	
     	return controlGroupElement;
     }
     
     public static StringTemplate constructActionsElement(ParameterModel pm) {
         StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
         controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
         StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), "");
         
         StringTemplate actionsElement = new StringTemplate(getActionsTemplate());
         actionsElement.setAttribute("value", pm.getLableText());
         actionsElement.setAttribute("id", pm.getId());
         actionsElement.setAttribute("onClickFunction", pm.getOnClickFunction());
         
         controlGroupElement.setAttribute("lable", lableElmnt);
         controlGroupElement.setAttribute("controls", actionsElement);
         return controlGroupElement;
     }
 
     public static StringTemplate constructSingleSelectElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate selectElement = new StringTemplate(getSelectTemplate());
     	StringBuilder options = constructOptions(pm.getObjectValue(), pm.getSelectedValues(), pm.getDependency(), pm.getOptionOnclickFunction());
     	selectElement.setAttribute("name", pm.getName());
     	selectElement.setAttribute("cssClass", pm.getCssClass());
     	selectElement.setAttribute("options", options);
     	selectElement.setAttribute("id", pm.getId());
     	selectElement.setAttribute("isMultiple", pm.isMultiple());
     	selectElement.setAttribute("ctrlsId", pm.getControlId());
     	selectElement.setAttribute("onChangeFunction", pm.getOnChangeFunction());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", selectElement);
     	
     	return controlGroupElement;
     }
     
     private static StringTemplate constructMultiSelectElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	if (!pm.isShow()) {
             controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
         }
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	
     	StringTemplate multiSelectElement = new StringTemplate(getMultiSelectTemplate());
     	multiSelectElement.setAttribute("cssClass", pm.getCssClass());
     	multiSelectElement.setAttribute("id", pm.getId());
     	multiSelectElement.setAttribute("additionalParam", pm.getDependency());
     	
     	StringBuilder multiSelectOptions = constructMultiSelectOptions(pm.getName(), pm.getObjectValue(), pm.getSelectedValues());
     	multiSelectElement.setAttribute("multiSelectOptions", multiSelectOptions);
     	multiSelectElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", multiSelectElement);
     	
     	return controlGroupElement;
     }
     
     private static StringBuilder constructOptions(List<? extends Object> values, List<String> selectedValues, String dependency, String optionsOnclickFunctioin) {
     	StringBuilder builder = new StringBuilder();
     	String selectedStr = "";
     	if (CollectionUtils.isNotEmpty(values)) {
         	for (Object value : values) {
         		String optionValue = getValue(value);
         		String optionKey = "";
         		if (value instanceof Value) {
         		    optionKey = ((Value) value).getKey();
         		} else if (value instanceof com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) {
         		    optionKey = ((com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) value).getKey();
         		} else if (value instanceof com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) {
         			optionKey = ((com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) value).getKey();	
         		}
 
         		if (CollectionUtils.isNotEmpty(selectedValues) && (selectedValues.contains(optionKey) || selectedValues.contains(optionValue))) {
         			selectedStr = "selected";
         		} else {
         			selectedStr = "";
         		}
         		builder.append("<option value='");
         		if (StringUtils.isNotEmpty(optionKey)) {
         		    builder.append(optionKey);
     		    } else {
         		    builder.append(optionValue);
     		    }
                 builder.append("' ");
                 String additionalParam = getAdditionalParam(value, dependency);
                 if (StringUtils.isNotEmpty(additionalParam)) {
                     builder.append("additionalParam='dependency=");
                     builder.append(additionalParam);
                     builder.append("' ");
                 }
                 builder.append(selectedStr);
                 builder.append(" ");
                 builder.append("onclick='");
                 builder.append(optionsOnclickFunctioin);
                 builder.append("'>");
                 builder.append(optionValue);
                 builder.append("</option>");
         	}
     	}
 
     	return builder;
     }
     
     private static String getAdditionalParam(Object value, String dependency) {
         StringBuilder builder = new StringBuilder();
         boolean appendComma = false;
         if (StringUtils.isNotEmpty(dependency)) {
             appendComma = true;
             builder.append(dependency);
         }
         if (value != null && value instanceof Value && StringUtils.isNotEmpty(((Value) value).getDependency())) {
             if (appendComma) {
                 builder.append(",");
             }
             builder.append(((Value) value).getDependency());
         }
         return builder.toString();
     }
 
     private static StringBuilder constructMultiSelectOptions(String name, List<? extends Object> values, List<String> selectedValues) {
     	StringBuilder builder = new StringBuilder();
 
     	String checkedStr = "";
     	for (Object value : values) {
     		String optionValue = getValue(value);
     		if (selectedValues!= null && selectedValues.contains(optionValue)) {
     			checkedStr = "checked";
     		} else {
     			checkedStr = "";
     		}
     		String additionalParam = getAdditionalParam(value, "");
     		String onClickFunction = "";
     		if (StringUtils.isNotEmpty(additionalParam)) {
     		    onClickFunction = "updateDepdForMultSelect(this)";
     		}
     		
     		builder.append("<li><input type='checkbox' additionalParam=\"dependency="+ additionalParam + "\" onclick=\""+ onClickFunction + "\" class='popUpChckBox' value=\"");
     		builder.append(optionValue + "\" name=\""+ name + "\" " + checkedStr + ">" + optionValue + "</li>");
     	}
 
     	return builder;
     }
     
     private static StringBuilder constructConfigMultiSelectOptions(String name, List<? extends Object> values, List<String> selectedValues, String key) {
     	StringBuilder builder = new StringBuilder();
     	
     	String className = key;
     	String checkedStr = "";
     	for (Object value : values) {
     		String optionValue = getValue(value);
     		if (selectedValues != null && selectedValues.contains(optionValue)) {
     			checkedStr = "checked";
     		} else {
     			checkedStr = "";
     		}
     		String additionalParam = getAdditionalParam(value, "");
     		
     		builder.append("<li><input type='checkbox' additionalParam=\"dependency="+ additionalParam + "\" onclick='updateHdnFieldForMultType(this)' class=\""+ className + "\" value=\"");
     		builder.append(optionValue + "\" " + checkedStr + ">" + optionValue + "</li>");
     	}
 
     	return builder;
     }
     
     
 	/**
 	 * @param value
 	 * @return
 	 */
 	private static String getValue(Object value) {
 		String optionValue = "";
 		if (value instanceof Value) {
 			optionValue = ((Value) value).getValue();
 		} else if (value instanceof com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) {
 		    optionValue = ((com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.Childs.Child.PossibleValues.Value) value).getValue();
 		} else if (value instanceof com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) {
 			optionValue = ((com.photon.phresco.plugins.model.Module.Configurations.Configuration.Parameter.PossibleValues.Value) value).getValue();	
 		} else {
 			optionValue = (String) value;
 		}
 		return optionValue;
 	}
 
     public static StringTemplate constructLabelElement(Boolean isMandatory, String cssClass, String Label) {
     	StringTemplate labelElement = new StringTemplate(getLableTemplate());
     	if (isMandatory) {
     		labelElement.setAttribute("mandatory", getMandatoryTemplate());
     	} else {
     		labelElement.setAttribute("mandatory", "");
     	}
     	labelElement.setAttribute("txt", Label);
     	labelElement.setAttribute("class", cssClass);
     	return labelElement;
     }
     
     public static StringTemplate constructFieldSetElement(ParameterModel pm) {
     	StringTemplate st = new StringTemplate(getFieldsetTemplate());
     	
     	if (!pm.isShow()) {
     		st.setAttribute("hideClass", "hideContent");
     	}
     	
     	List<? extends Object> objectValues = pm.getObjectValue();
     	StringBuilder builder = new StringBuilder();
     	if (CollectionUtils.isNotEmpty(objectValues)) {
         	for (Object objectValue : objectValues) {
         		String filePath = getValue(objectValue);
         		filePath = filePath.replace("#SEP#", "/");
         		int index = filePath.lastIndexOf("/");
         		String fileName = filePath.substring(index + 1);
         		builder.append("<option value=\"");
         		builder.append(filePath + "\" >" + fileName + "</option>");
         	}
     	}	
     	
     	st.setAttribute("fileList", builder);
     	return st;
     }
 
     public static StringTemplate constructBrowseFileTreeElement(ParameterModel pm) {
     	StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
     	controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
     	
     	if (!pm.isShow()) {
     	    controlGroupElement.setAttribute("ctrlGrpClass", "hideContent");
     	}
     	
     	StringTemplate lableElmnt = constructLabelElement(pm.isMandatory(), pm.getLableClass(), pm.getLableText());
     	StringTemplate inputElement = new StringTemplate(getBrowseFileTreeTemplate(pm.getFileType()));
     	inputElement.setAttribute("class", pm.getCssClass());
     	inputElement.setAttribute("id", pm.getId());
     	inputElement.setAttribute("name", pm.getName());
     	inputElement.setAttribute("ctrlsId", pm.getControlId());
     	
     	controlGroupElement.setAttribute("lable", lableElmnt);
     	controlGroupElement.setAttribute("controls", inputElement);
     	
 		return controlGroupElement;
     }
     
     public static StringTemplate constructFileBrowseForPackage(ParameterModel pm) {
         StringTemplate controlGroupElement = new StringTemplate(getControlGroupTemplate());
         controlGroupElement.setAttribute("ctrlGrpId", pm.getControlGroupId());
         StringTemplate tableElement = new StringTemplate(constructTable());
         StringTemplate targetFolder = new StringTemplate(constructInputElement());
         targetFolder.setAttribute("name", "targetFolder");
         targetFolder.setAttribute("class", "input-small");
         tableElement.setAttribute("td1", targetFolder);
         StringTemplate fileOrFolder = new StringTemplate(constructInputElement());
         fileOrFolder.setAttribute("name", "selectedFileOrFolder");
         fileOrFolder.setAttribute("disabled", "disabled");
         fileOrFolder.setAttribute("class", "input-medium");
         tableElement.setAttribute("td2", fileOrFolder);
         tableElement.setAttribute("td3", constructButtonElement());
         controlGroupElement.setAttribute("controls", tableElement);
         
         return controlGroupElement;
     }
     
     private static String constructTable() {
         StringBuilder sb = new StringBuilder()
        .append("<div class='headerDiv'>")
         .append("<table align='center' class='package-file-browse-tbl'>")
         .append("<thead class='header-background'>")
         .append("<tr class='borderForLoad'>")
         .append("<th class='borderForLoad mapHeading'>Target Folder</th>")
         .append("<th class='borderForLoad mapHeading'>File/Folder</th>")
        .append("</tr></thead></table>")
        .append("</div>")
        .append("<div class='bldBrowseFilePrntDiv'>")
        .append("<div class='bldBrowseFileDiv'>")
        .append("<div class='bldBrowseFileLeftDiv'>")
        .append("<table align='center' class='package-file-browse-tbl'>")
         .append("<tbody id='propTempTbodyForHeader'>")
         .append("<tr>")
         .append("<td class='popuptable'>$td1$</td>")
         .append("<td class='popuptable'>$td2$</td>")
         .append("<td class='popuptable'>$td3$</td>")
        .append("</tr></tbody></table></div>")
        .append("<div class='bldBrowseFileRightDiv'>")
        .append("<img class='imagealign add_icon' src='images/icons/add_icon.png' onclick='addRowInPackageBrowse(this);'>")
        .append("<img class='imagealign hideContent minus_icon' src='images/icons/minus_icon.png' onclick='removeRowInPackageBrowse(this);'>")
        .append("</div><div style='clear:both;'></div></div></div>");
 
         return sb.toString();
     }
     
	private static String constructInputElement() {
         StringBuilder sb = new StringBuilder("<input type='text' name='$name$' class='$class$' $disabled$ />");
         return sb.toString();
     }
     
     private static String constructButtonElement() {
         StringBuilder sb = new StringBuilder("<input type='button' value='Browse' class='btn btn-primary' fromPage='package' onclick='browseFiles(this)'/>");
         return sb.toString();
     }
     
     public StringTemplate constructDynamicTemplate(String CustomerId, Parameter parameter,ParameterModel parameterModel, List<? extends Object> obj, String className) throws IOException {
     	try {
     		StringBuilder sb = new StringBuilder();
     		String line;
     		Customer customer = getServiceManager().getCustomer(CustomerId);
     		RepoInfo repoInfo = customer.getRepoInfo();
     		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
     		ArtifactGroup artifactGroup = new ArtifactGroup();
     		artifactGroup.setGroupId(parameter.getDynamicParameter().getDependencies().getDependency().getGroupId());
     		artifactGroup.setArtifactId(parameter.getDynamicParameter().getDependencies().getDependency().getArtifactId());
     		artifactGroup.setPackaging(parameter.getDynamicParameter().getDependencies().getDependency().getType());
     		//to set version
     		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
     		ArtifactInfo artifactInfo = new ArtifactInfo();
     		artifactInfo.setVersion(parameter.getDynamicParameter().getDependencies().getDependency().getVersion());
     		artifactInfos.add(artifactInfo);
     		artifactGroup.setVersions(artifactInfos);
     		artifactGroups.add(artifactGroup);
     		//dynamically loads Template Stream 
     		PhrescoDynamicLoader phrescoDynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
     		InputStream fileStream = phrescoDynamicLoader.getResourceAsStream(parameterModel.getName()+".st");
     		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
     		while ((line = br.readLine()) != null) {
     			sb.append(line);
     		} 
     		
     		StringTemplate dynamicTemplateDiv = new StringTemplate(getDynamicTemplateWholeDiv());
     		if (parameterModel.isShow()) {
     			dynamicTemplateDiv.setAttribute("templateClass", parameterModel.getName() + "PerformanceDivClass");
     		} else {
     			dynamicTemplateDiv.setAttribute("templateClass", parameterModel.getName() + "PerformanceDivClass  hideContent");
     		}
     		
     		//dynamicTemplateDiv.setAttribute("templateId", parameterModel.getName() + "DivId");
     		StringTemplate stringTemplate = new StringTemplate(sb.toString());
     		dynamicTemplateDiv.setAttribute("className", className);
     		if (CollectionUtils.isNotEmpty(obj)) {
     			stringTemplate.setAttribute("myObject", obj);
     		} else {
     			stringTemplate.setAttribute("myObject", "");
     		}
     		dynamicTemplateDiv.setAttribute("templateDesign", stringTemplate);
     		
     		return dynamicTemplateDiv;
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
 
     	return null;
     }
    
     private static String getControlGroupTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='control-group $ctrlGrpClass$' id=\"$ctrlGrpId$\">")
     	.append("$lable$ $controls$")
     	.append("</div>");
     	
     	return sb.toString();
     }
     
     private static String getLableTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<label for='xlInput' class='control-label labelbold $class$'>")
     	.append("$mandatory$ $txt$")
     	.append("</label>");
     	
     	return sb.toString();
     }
     
     private static String getMandatoryTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<span class='red'>*</span>&nbsp");
     	
     	return sb.toString();
     }
     
     private static String getSelectTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<select class=\"input-xlarge $cssClass$\" ")
     	.append("id=\"$id$\" name=\"$name$\" isMultiple=\"$isMultiple$\" ")
     	.append("additionalParam=\"\" ")
     	.append("onfocus=\"setPreviousDependent(this);\" ")
     	.append("onchange=\"$onChangeFunction$\">")
     	.append("$options$</select>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getInputTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type=\"$type$\" class=\"input-xlarge $class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" placeholder=\"$placeholder$\" value=\"$value$\">")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getSchedulerTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type=\"$type$\" class=\"input-xlarge $class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" placeholder=\"$placeholder$\" value=\"$value$\">")
     	.append("<span class='help-inline'><img class='add imagealign' src='images/icons/gear.png' style='cursor:pointer' connector=\"$id$\" onclick='callCron(this);'></span>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	
     	return sb.toString();
     }
     
     private static String getBrowseFileTreeTemplate(String fileTypes) {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type='text' class=\"$class$\" id='fileLocation' style='margin-right:5px;'")
     	.append("name=\"$name$\" >")
     	.append("<input id='browseButton' class='btn-primary btn_browse browseFileLocation'")
     	.append("value='Browse' type='button' fileTypes="+fileTypes+" onclick='browseFiles(this);'></div>");
     	
     	return sb.toString();
     }
     
     private static String getMapTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<fieldset class='mfbox siteinnertooltiptxt popup-fieldset fieldSetClassForHeader'>")
     	.append("<legend class='fieldSetLegend'>$mandatory$ $legendHeader$</legend>")
     	.append("<table align='center'>")
     	.append("<thead class='header-background'>")
     	.append("<tr class='borderForLoad'>")
     	.append("<th class='borderForLoad mapHeading'>$keyMandatory$$keyLabel$</th>")
     	.append("<th class='borderForLoad mapHeading'>$valueMandatory$$valueLabel$</th><th></th><th></th></tr></thead>")
     	.append("<tbody id='propTempTbodyForHeader'>")
     	.append("<tr class='borderForLoad'>")
     	.append("$mapControls$")
     	.append("<td class='borderForLoad'>")
     	.append("<a><img class='add imagealign' src='images/icons/add_icon.png' onclick='addRow(this);'></a></td>")
     	.append("</tr></tbody></table></fieldset>");
 
     	return sb.toString();
     }
     
     private static String getMapTemplatesForValues() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<fieldset class='mfbox siteinnertooltiptxt popup-fieldset fieldSetClassForHeader'>")
     	.append("<legend class='fieldSetLegend'>$mandatory$ $legendHeader$</legend>")
     	.append("<table align='center'>")
     	.append("<thead class='header-background'>")
     	.append("<tr class='borderForLoad'>")
     	.append("<th class='borderForLoad mapHeading'>$keyMandatory$$keyLabel$</th>")
     	.append("<th class='borderForLoad mapHeading'>$valueMandatory$$valueLabel$</th><th></th><th></th></tr></thead>")
     	.append("<tbody id='propTempTbodyForHeader'>")
     	.append("$mapTrContrls$")
     	.append("</tbody></table></fieldset>");
 
     	return sb.toString();
     }
 	
     private static String getMapSelectElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad'>")
         .append("<select class=\"input-medium $cssClass$\" ")
         .append("id=\"$id$\" name=\"$name$\" isMultiple=\"$isMultiple$\">")
         .append("$options$</select>")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span>")
         .append("</td>");
         
         return sb.toString();
     }
     
     private static String getMapInputElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad'>")
         .append("<input type='text' class='input-mini' id=\"$id$\" ")
         .append("name=\"$name$\" value=\"$value$\" placeholder=\"$valuePlaceholder$\" />")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span>")
         .append("</td>");
         
         return sb.toString();
     }
     
     private static String getMapPlusMinusIconElement(int size) {
         StringBuilder sb = new StringBuilder();
         sb.append("<td class='borderForLoad addImage' name='addImage'>")
     	.append("<a><img class='add imagealign' src='images/icons/add_icon.png' style='cursor:pointer' onclick='addRow(this);'></a></td>");
         if (size == 1) {
         	sb.append("<td class='borderForLoad removeImage hideContent' >");
         	sb.append("<a><img class='add imagealign' src='images/icons/minus_icon.png' style='cursor:pointer' onclick='removeRow(this);'></a></td>");
         } else {
         	sb.append("<td class='borderForLoad removeImage'>");
         	sb.append("<a><img class='add imagealign' src='images/icons/minus_icon.png' style='cursor:pointer' onclick='removeRow(this);'></a></td>");
         }
         
         return sb.toString();
     }
     
     private static String getMapTableRowElement() {
         StringBuilder sb = new StringBuilder();
         sb.append("<tr class='borderForLoad'>")
     	.append("$mapTdContrls$")
         .append("</tr>");
         
         return sb.toString();
     }
    
    
     private static String getCheckBoxTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'>")
     	.append("<input type='checkbox' class=\"$class$\" id=\"$id$\" ")
     	.append("name=\"$name$\" value=\"$value$\" $checked$ onchange=\"$onChangeFunction$\" onclick=\"$onClickFunction$\" $additionalParam$/>")
     	.append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
     	return sb.toString();
     }
     
     private static String getActionsTemplate() {
         StringBuilder sb = new StringBuilder();
         sb.append("<div class='controls'>")
         .append("<input type='button' class=\"btn btn-primary $class$\" id=\"$id$\" ")
         .append("name=\"$name$\" value=\"$value$\" $checked$ onclick=\"$onClickFunction$\" $additionalParam$/>")
         .append("<span class='help-inline' id=\"$ctrlsId$\"></span></div>");
         return sb.toString();
     }
     
     private static String getMultiSelectTemplate() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='controls'><div class='multiSelectBorder'>")
     	.append("<div class='multilist-scroller multiselect multiSelHeight $class$' id=\"$id$\"")
     	.append(" additionalParam=\"dependency=$additionalParam$\"><ul>$multiSelectOptions$</ul>")
     	.append("</div><span class='help-inline' id=\"$ctrlsId$\"></span></div></div>");
     	
     	return sb.toString();
     }
     
     private static String getConfigMultiSelectTemplate() {
     	StringBuilder sb = new StringBuilder()
     	.append("<div class='controls'><div class='multiSelectBorder'>")
     	.append("<div class='multilist-scroller multiselect multiSelHeight $class$' id=\"$id$\"")
     	.append(" additionalParam=\"dependency=$additionalParam$\"><ul>$multiSelectOptions$</ul>")
     	.append("</div><span class='help-inline' id=\"$ctrlsId$\"></span></div>")
     	.append("<input type='hidden' value='' name=\"$name$\"></div>");
     	
     	return sb.toString();
     }
     
     private static String getFieldsetTemplate() {
     	StringBuilder sb = new StringBuilder();
     	
     	sb.append("<fieldset id='fetchSqlControl' class='popup-fieldset fieldset_center_align fieldSetClass $hideClass$'>")
     	.append("<legend class='fieldSetLegend'>DB Script Execution</legend>")
     	.append("<table class='fieldSetTbl'><tbody><tr class='fieldSetTr'><td  class='fieldSetTrTd'>")
     	.append("<select class='fieldSetSelect' multiple='multiple' id='avaliableSourceScript'>$fileList$</select></td>")
     	.append("<td class='fldSetSelectTd'><ul class='fldSetUl'>")
     	.append("<li class='fldSetLi'><input type='button' value='&gt;&gt;' id='btnAddAll' class='btn btn-primary arrowBtn' onclick='buttonAddAll();'></li>")
     	.append("<li class='fieldsetLi'><input type='button' value='&gt;' id='btnAdd' class='btn btn-primary arrowBtn' onclick='buttonAdd();'></li>") 
     	.append("<li class='fieldsetLi'><input type='button' value='&lt;' id='btnRemove' class='btn btn-primary arrowBtn' onclick='buttonRemove();'></li>")
     	.append("<li class='fieldsetLi'><input type='button' value='&lt;&lt;' id='btnRemoveAll' class='btn btn-primary arrowBtn' onclick='buttonRemoveAll();'></li>")
     	.append("</ul></td><td class='fieldSetTrTd'>")
     	.append("<select class='fieldSetSelect' multiple='multiple' name='selectedSourceScript' id='selectedSourceScript'></select>")
     	.append("</td><td class='fldSetRightTd'><img  class='moveUp'  id='up' title='Move up' src='images/icons/top_arrow.png' onclick='moveUp();'><br>")
     	.append("<img class='moveDown' id='down' title='Move down' src='images/icons/btm_arrow.png' onclick='moveDown();'></td></tr></tbody></table>")
     	.append("<input type='hidden' value='' name='fetchSql' id='fetchSql'></fieldset>");	
 
     	return sb.toString();
     }
     
     private static String getDynamicTemplateWholeDiv() {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<div class='$templateClass$' id='$templateId$'> $templateDesign$")
     	.append("<input type='hidden' name='objectClass' value='$className$'/></div>");
     	
     	return sb.toString();
     }
     
     public static List<String> getCsvAsList(String csv) {
         Pattern csvPattern = Pattern.compile(CSV_PATTERN);
         Matcher match = csvPattern.matcher(csv);
 
         List<String> list = new ArrayList<String>(match.groupCount());
         // For each field
         while (match.find()) {
             String value = match.group();
             if (value == null) {
                 break;
             }
             if (value.endsWith(",")) {  // trim trailing ,
                 value = value.substring(0, value.length() - 1);
             }
             if (value.startsWith("\"")) { // assume also ends with
                 value = value.substring(1, value.length() - 1);
             }
             if (value.length() == 0) {
                 value = null;
             }
             list.add(value.trim());
         }
         if (CollectionUtils.isEmpty(list)) {
             list.add(csv.trim());
         }
         
         return list;
     }
     
     public static String listToCsv(List<?> list) {
         Iterator<?> iter = list.iterator();
         String csvString = "";
         String sep = "";
         while (iter.hasNext()) {
             csvString += sep + iter.next();
             sep = ",";
         }
         
         return csvString;
     }
     
 	public static String findPlatform() {
 		String osName = System.getProperty(OS_NAME);
 		String osBit = System.getProperty(OS_ARCH);
 		if (osName.contains(WINDOWS)) {
 			osName = WINDOWS;
 		} else if (osName.contains(LINUX)) {
 			osName = LINUX;
 		} else if (osName.contains(MAC)) {
 			osName = MAC;
 		} else if (osName.contains(SERVER)) {
 			osName = SERVER;
 		} else if (osName.contains(WINDOWS7)) {
 			osName = WINDOWS7.replace(" ", "");
 		}
 		if (osBit.contains(OS_BIT64)) {
 			osBit = OS_BIT64;
 		} else {
 			osBit = OS_BIT86;
 		}
 		return osName.concat(osBit);
 	}
 }
