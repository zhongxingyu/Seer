 /**
  * android-phresco-plugin
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
 package com.photon.phresco.plugins.android;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.gson.Gson;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.android.AndroidProfile;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.model.PluginExecution;
 import com.phresco.pom.model.PluginExecution.Goals;
 import com.phresco.pom.util.AndroidPomProcessor;
 import com.phresco.pom.util.PomProcessor;
 
 public class Package implements PluginConstants {
 	private Log log;
 	private String baseDir;
 	private String dotPhrescoDirName;
 	private MavenProject project;
 	private String pomFile;
 	private String TRUE = "true";
 	private List<String> projPoms ;
 	private List<String> pomsTobeUpdated ;
 	private String sourceDirName;
 	private String testDirName ;
 	private String currentDir ;
 	private File base;
 	private String moduleName = "";
 	public void pack(com.photon.phresco.plugins.model.Mojos.Mojo.Configuration config, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir().getPath();
 		project = mavenProjectInfo.getProject();
 		moduleName = mavenProjectInfo.getModuleName();
 		pomFile = project.getFile().getName();
 		
 		Map<String, String> configs = MojoUtil.getAllValues(config);
 		String environmentName = configs.get(ENVIRONMENT_NAME);
 		String buildName = configs.get(BUILD_NAME);
 		String buildNumber = configs.get(BUILD_NUMBER);
 		String sdkVersion = configs.get(SDK_VERSION);
 		String proguard = configs.get(PROGUARD);
 		String skipTest = configs.get(SKIP_TEST);
 		String unitCodeCoverage = configs.get(COVERAGE);
 		log.info("unitCodeCoverage :" +unitCodeCoverage);
 		
 		String signing = configs.get(SIGNING);
 		String keystore = configs.get(KEYSTORE);
 		String storepass = configs.get(STOREPASS);
 		String keypass = configs.get(KEYPASS);
 		String alias = configs.get(ALIAS);
 		String zipAlign = configs.get(ZIP_ALIGN);
 				
 		StringBuilder sb;
 		try {
 			ProjectInfo projectInfo = Utility.getProjectInfo(baseDir, moduleName);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String techId = applicationInfo.getTechInfo().getId();
 			System.out.println("tech id..."+techId);
 			if (StringUtils.isEmpty(environmentName)) {
 				System.out.println("Environment Name is empty . ");
 				throw new PhrescoException("Environment Name is empty . ");
 			}
 			
 			
 			if (StringUtils.isEmpty(sdkVersion)) {
 				System.out.println("sdkVersion is empty . ");
 				throw new PhrescoException("sdkVersion is empty . ");
 			}
 			dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
			sourceDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_SOURCE_DIR);
 			testDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR);
 			
 			if(dotPhrescoDirName!=null){
 				baseDir = mavenProjectInfo.getBaseDir().getParentFile()+ File.separator + dotPhrescoDirName;
 			}
 			
 			PluginUtils.checkForConfigurations(new File(baseDir), environmentName);
 			
 			if (TechnologyTypes.ANDROID_HYBRID.equals(techId)) {
 				writeConfigJson(mavenProjectInfo, new File(baseDir), environmentName);
 			}	
 			
 			Boolean isZipAlign = Boolean.valueOf(zipAlign);
 			log.info("isZipAlign . " +isZipAlign);
 			Boolean isSigning = Boolean.valueOf(signing);
 			log.info("isSigning . " + isSigning);
 			Boolean isSkipTest = Boolean.valueOf(skipTest);
 			log.info("isSkipTest . " +isSkipTest);
 			Boolean isUnitCodeCoverage = Boolean.valueOf(unitCodeCoverage);
 			log.info("isUntCodeCoverage . " +isUnitCodeCoverage);
 			
 			if(isZipAlign) {						
 				isSigning = true;
 			}
 			
 			if (isSigning) {			
 				updateAllPOMWithProfile(keystore, storepass, keypass, alias);
 			}
 			
 			updateDotPhrescoInfoFiles(baseDir ,isSigning, mavenProjectInfo);
 			
 			
 			log.info("Project is Building...");
 			sb = new StringBuilder();
 			sb.append(ANDROID_BUILD_COMMAND);
 			
 			sb.append(STR_SPACE);
 			sb.append(HYPHEN_D + ANDROID_VERSION + EQUAL + sdkVersion);
 			
 			if (StringUtils.isNotEmpty(buildName)) {
 				sb.append(STR_SPACE);
 				sb.append(HYPHEN_D + BUILD_NAME + EQUAL + buildName);
 			}
 			
 			if (StringUtils.isNotEmpty(buildNumber)) {
 				sb.append(STR_SPACE);
 				sb.append(HYPHEN_D + BUILD_NUMBER + EQUAL + buildNumber);
 			}
 			
 			sb.append(STR_SPACE);
 			sb.append(HYPHEN_D + ENVIRONMENT_NAME + EQUAL + environmentName);
 			
 			if (StringUtils.isNotEmpty(proguard)) {
 				sb.append(STR_SPACE);
 				boolean proguradVal = Boolean.valueOf(proguard);
 				sb.append(HYPHEN_D + PROGUARD_SKIP + EQUAL + !proguradVal);
 			}else{
 				sb.append(STR_SPACE);
 				sb.append(HYPHEN_D + PROGUARD_SKIP + EQUAL + TRUE);
 			}
 			//zip Align 
 			if(StringUtils.isNotEmpty(zipAlign)) {			
 				sb.append(STR_SPACE);
 				boolean zipAlignVal = Boolean.valueOf(zipAlign);
 				sb.append(HYPHEN_D + ZIP_ALIGN_SKIP + EQUAL + !zipAlignVal);
 			}else{
 				sb.append(STR_SPACE);
 				sb.append(HYPHEN_D + ZIP_ALIGN_SKIP + EQUAL + TRUE);
 			}
 			//run unit test 
 			if ((isSkipTest==false && isUnitCodeCoverage==false)||(isSkipTest==false && unitCodeCoverage.isEmpty())){
 						sb.append(STR_SPACE);
 						sb.append(PRUNUNIT);
 			 }
 			//run code coverage
 			 if (isUnitCodeCoverage){
 					sb.append(STR_SPACE);
 					sb.append(PCOVERAGE);
 			}
 			//signing
 			if (isSigning) {
 				sb.append(STR_SPACE);
 				sb.append(PSIGN);
 			}
 					
 			/*List<Parameter> parameters = config.getParameters().getParameter();
 			for (Parameter parameter : parameters) {
 				if(parameter.getPluginParameter() != null && parameter.getMavenCommands() != null) {
 					List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
 					for (MavenCommand mavenCommand : mavenCommands) {
 						if(parameter.getValue().equals(mavenCommand.getKey())) {						
 							sb.append(STR_SPACE);
 							sb.append(mavenCommand.getValue());
 						}
 					}
 				}			
 			}	*/	
 			
 			if(!Constants.POM_NAME.equals(pomFile)) {
 				sb.append(STR_SPACE);
 				sb.append(Constants.HYPHEN_F);
 				sb.append(STR_SPACE);
 				sb.append(pomFile);
 			}
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			throw new PhrescoException(e1);
 		}
 		log.info("Command " + sb.toString());
 		boolean status = Utility.executeStreamconsumer(sb.toString(), baseDir, baseDir, FrameworkConstants.PACKAGE);
 		if(!status) {
 			try {
 				throw new MojoExecutionException(Constants.MOJO_ERROR_MESSAGE);
 			} catch (MojoExecutionException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 
 	private void writeConfigJson(MavenProjectInfo mavenProjectInfo, File dotPhrecoDir,  String environmentName) throws PhrescoException {
 		PluginUtils pu = new PluginUtils();
 		String customerId = pu.readCustomerId(dotPhrecoDir);
 		File configFile = new File(new File(baseDir).getPath() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE);
 		File settingsFile = new File(Utility.getProjectHome()+ customerId + PluginConstants.SETTINGS_FILE);
 		FileWriter fstream = null;
 		BufferedWriter out = null;
 		try {
 			Environment obtainedEnv = null;
 			ConfigManager configManager = null;
 
 			if (settingsFile.exists()) {
 				configManager = PhrescoFrameworkFactory.getConfigManager(settingsFile);
 				obtainedEnv = configManager.getEnvironment(environmentName);
 			}
 			configManager = PhrescoFrameworkFactory.getConfigManager(configFile);
 			if (configManager != null && obtainedEnv == null) {
 				obtainedEnv = configManager.getEnvironment(environmentName);
 			}
 
 			if (obtainedEnv != null) {
 				Gson gson = new Gson();
 				String envJson = gson.toJson(obtainedEnv);
 				PomProcessor pomProcessor = Utility.getPomProcessor(mavenProjectInfo.getBaseDir().getPath(), moduleName);
 				String configJsonPath = pomProcessor.getProperty(Constants.POM_PROP_KEY_CONFIG_JSON_PATH);
 				if (StringUtils.isNotEmpty(configJsonPath)) {
 					StringBuilder path = new StringBuilder(mavenProjectInfo.getBaseDir().getPath());
 					if (StringUtils.isNotEmpty(moduleName)) {
 						path.append(File.separator).append(moduleName);
 					}
 					path.append(configJsonPath).append(File.separator).append(Constants.CONFIG_JSON);
 					File configJsonFile = new File(path.toString());
 					if (!configJsonFile.exists()) {
 						configJsonFile.createNewFile();
 					}
 					fstream = new FileWriter(configJsonFile.getPath());
 					out = new BufferedWriter(fstream);
 					out.write(envJson);
 				}
 			}
 		} catch (ConfigurationException e1) {
 			throw new PhrescoException(e1);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 				}
 				if (fstream != null) {
 					fstream.close();
 				}
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 	
 	/**
 	 * This function is used to change the Unit,functional and performance info files
 	 * for displaying the signing check box on Unit,functional and performance build 
 	 * pop-up's based on signing value from main build pop-up
 	 * @param isSigning
 	 * @param mavenProjectInfo
 	 */
 	private void updateDotPhrescoInfoFiles(String dotPhrescoLocation, Boolean isSigning, MavenProjectInfo mavenProjectInfo){
 		
 		
 			
 			String baseDir = dotPhrescoLocation;
 			
 //			log.info("updateDotPhrescoInfoFiles - baseDir = " + baseDir);
 //			log.info("updateDotPhrescoInfoFiles - isSigning = " + isSigning.toString());
 			
 			String unitXmlFile = baseDir + File.separator + DOT_PHRESCO_FOLDER + File.separator + UNIT_INFO_FILE;
 			String functionalXmlFile = baseDir + File.separator +DOT_PHRESCO_FOLDER + File.separator + FUNCTIONAL_INFO_FILE;
 			String performanceXmlFile = baseDir + File.separator +DOT_PHRESCO_FOLDER + File.separator + PERFORMANCE_INFO_FILE;
 			
 //			log.info("updateDotPhrescoInfoFiles - unitXmlFile = " + unitXmlFile);
 //			log.info("updateDotPhrescoInfoFiles - functionalXmlFile = " + functionalXmlFile);
 //			log.info("updateDotPhrescoInfoFiles - performanceXmlFile = " + performanceXmlFile);
 			
 			
 			
 			MojoProcessor mojoObj;
 			try {
 				
 				mojoObj = new MojoProcessor(new File(unitXmlFile));
 				Parameter unitSigningParameter = mojoObj.getParameter("unit-test", "signing");
 				if (unitSigningParameter != null) {
 				unitSigningParameter.setShow(isSigning);
 				unitSigningParameter.setValue(isSigning.toString());
 				Boolean isEditable = !isSigning;
 				unitSigningParameter.setEditable(isEditable.toString());
 				mojoObj.save();
 				}
 				
 				mojoObj = new MojoProcessor(new File(functionalXmlFile));
 				Parameter functionalRobotiumSigningParameter = mojoObj.getParameter("functional-test-robotium", "signing");
 				if (functionalRobotiumSigningParameter != null) {
 					functionalRobotiumSigningParameter.setShow(isSigning);
 					functionalRobotiumSigningParameter.setValue(isSigning.toString());
 					Boolean isEditable = !isSigning;
 					functionalRobotiumSigningParameter.setEditable(isEditable.toString());
 					mojoObj.save();
 				}
 				
 				Parameter functionalCalabashSigningParameter = mojoObj.getParameter("functional-test-calabash", "signing");
 				if (functionalCalabashSigningParameter != null) {
 					functionalCalabashSigningParameter.setShow(isSigning);
 					functionalCalabashSigningParameter.setValue(isSigning.toString());
 					Boolean isEditable = !isSigning;
 					functionalCalabashSigningParameter.setEditable(isEditable.toString());
 					mojoObj.save();
 				}
 				
 				Parameter functionalMonkeyTalkSigningParameter = mojoObj.getParameter("functional-test-monkey-talk", "signing");
 				if (functionalMonkeyTalkSigningParameter != null) {
 					functionalMonkeyTalkSigningParameter.setShow(isSigning);
 					functionalMonkeyTalkSigningParameter.setValue(isSigning.toString());
 					Boolean isEditable = !isSigning;
 					functionalMonkeyTalkSigningParameter.setEditable(isEditable.toString());
 					mojoObj.save();
 				}
 				
 				mojoObj = new MojoProcessor(new File(performanceXmlFile));
 				Parameter performanceSigningParameter = mojoObj.getParameter("performance-test", "signing");
 				if (performanceSigningParameter != null) {
 				performanceSigningParameter.setShow(isSigning);
 				performanceSigningParameter.setValue(isSigning.toString());
 				Boolean isEditable = !isSigning;
 				performanceSigningParameter.setEditable(isEditable.toString());
 				mojoObj.save();
 				}
 				
 			} catch (PhrescoException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	private void updateAllPOMWithProfile(String keystore, String storepass, String keypass, String alias) throws PhrescoException {
 		
 		base = new File(baseDir);
 		
 		if(sourceDirName!=null && testDirName!=null  ){
 		   
 			currentDir = base.getParentFile().getPath() + File.separator;
 			
 	    }
 		
 		pomsTobeUpdated = new ArrayList<String>();
 		pomsTobeUpdated.add(Constants.SOURCE);
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_UNITTEST_DIR);
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_PERFORMANCETEST_DIR);
 		
 	    projPoms = new ArrayList<String>(pomsTobeUpdated.size());
 
 		for (String pomTobeUpdated : pomsTobeUpdated) {
 			// get root dir
 			StringBuilder sb;
 			if(currentDir!=null){
 				 sb = new StringBuilder(currentDir);
 			}else{
 				sb = new StringBuilder(baseDir);
 			}
 			
 		    if(sourceDirName!=null && pomTobeUpdated.equals(pomsTobeUpdated.get(0))){
 		    	sb.append(sourceDirName);
 		    	
 		    }
 		    if(testDirName!=null  && !pomTobeUpdated.equals(pomsTobeUpdated.get(0))){
 		    	sb.append(testDirName);
 		    }
 			// get pom path
 			String property = project.getProperties().getProperty(pomTobeUpdated);
 			if (StringUtils.isNotEmpty(property)) {
 				sb.append(property);
 			}
 			// pom file
 			sb.append(File.separatorChar);
 			sb.append(pomFile);
 			projPoms.add(sb.toString());
 			
 
 		}
 		
 		for (String projPom : projPoms) {
 			createAndroidProfile(projPom, keystore, storepass, keypass, alias);
 		}
 	}
 	
 	private String createAndroidProfile(String filePath, String keystore, String storepass, String keypass, String alias) throws PhrescoException {
 		
 		try {
 			if (StringUtils.isEmpty(keystore)) {
 				throw new PhrescoException("keystore value is empty "); 
 			}
 			
 			if (StringUtils.isEmpty(storepass)) {
 				throw new PhrescoException("storepass value is empty "); 
 			}
 			
 			if (StringUtils.isEmpty(keypass)) {
 				throw new PhrescoException("keypass value is empty "); 
 			}
 			
 			if (StringUtils.isEmpty(alias)) {
 				throw new PhrescoException("alias value is empty "); 
 			}
 			
 			boolean hasSigning = false;
 
 			File pomPath = new File(filePath);
 
 			AndroidPomProcessor processor = new AndroidPomProcessor(pomPath);
 			hasSigning = processor.hasSigning();
 			String profileId = PROFILE_ID;
 			String defaultGoal = GOAL_INSTALL;
 			Plugin plugin = new Plugin();
 			plugin.setGroupId(ANDROID_PROFILE_GROUP_ID);
 			plugin.setArtifactId(ANDROID_PROFILE_ARTIFACT_ID);
 			plugin.setVersion(ANDROID_PROFILE_VERSION);
 
 			PluginExecution execution = new PluginExecution();
 			execution.setId(ANDROID_EXECUTION_ID);
 			Goals goal = new Goals();
 			goal.getGoal().add(GOAL_SIGN);
 			execution.setGoals(goal);
 			execution.setPhase(PHASE_PACKAGE);
 			execution.setInherited(TRUE);
 
 			AndroidProfile androidProfile = new AndroidProfile();
 			androidProfile.setKeystore(keystore);
 			androidProfile.setStorepass(storepass);
 			androidProfile.setKeypass(keypass);
 			androidProfile.setAlias(alias);
 			androidProfile.setVerbose(true);
 			androidProfile.setVerify(true);
 
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 
 			List<Element> executionConfig = new ArrayList<Element>();
 			executionConfig.add(doc.createElement(ELEMENT_ARCHIVE_DIR));
 			Element removeExistSignature = doc.createElement(ELEMENT_REMOVE_EXIST_SIGN);
 			Element includeElement = doc.createElement(ELEMENT_INCLUDES);
 			Element doNotCheckInBuildInclude = doc.createElement(ELEMENT_INCLUDE);
 			doNotCheckInBuildInclude.setTextContent(ELEMENT_BUILD);
 			Element doNotCheckinTargetInclude = doc.createElement(ELEMENT_INCLUDE);
 			doNotCheckinTargetInclude.setTextContent(ELEMENT_TARGET);
 			includeElement.appendChild(doNotCheckInBuildInclude);
 			includeElement.appendChild(doNotCheckinTargetInclude);
 			executionConfig.add(includeElement);
 			removeExistSignature.setTextContent(TRUE);
 			executionConfig.add(removeExistSignature);
 
 			// verboss
 			Element verbos = doc.createElement(ELEMENT_VERBOS);
 			verbos.setTextContent(TRUE);
 			executionConfig.add(verbos);
 			// verify
 			Element verify = doc.createElement(ELEMENT_VERIFY);
 			verbos.setTextContent(TRUE);
 			executionConfig.add(verify);
 			
 			//arguments
 			List<String> argumentContents = new ArrayList<String>();
 			argumentContents.add(HYPHEN_SIGALG);
 			argumentContents.add(MD5_WITH_RSA);
 			argumentContents.add(HYPHEN_DIGESTALG);
 			argumentContents.add("SHA1");
 			Element arguments = doc.createElement(ARGUMENTS);
 			for (String string : argumentContents) {
 				Element argument = doc.createElement(ARGUMENT);
 				argument.setTextContent(string);
 				arguments.appendChild(argument);
 			}
 			executionConfig.add(arguments);
 			
 			com.phresco.pom.model.PluginExecution.Configuration configValues = new com.phresco.pom.model.PluginExecution.Configuration();
 			configValues.getAny().addAll(executionConfig);
 			execution.setConfiguration(configValues);
 			List<Element> additionalConfigs = new ArrayList<Element>();
 			processor.setProfile(profileId, defaultGoal, plugin, androidProfile, execution, null, additionalConfigs);
 			processor.save();
 			if (hasSigning) {
 				log.info("Signing info updated successfully ");
 			} else {
 				log.info("Signing info created successfully ");
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e); 
 		}
 		return SUCCESS;
 	}
 }
