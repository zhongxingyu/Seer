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
 
 import java.io.File;
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
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.android.AndroidProfile;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.model.PluginExecution;
 import com.phresco.pom.model.PluginExecution.Goals;
 import com.phresco.pom.util.AndroidPomProcessor;
 
 public class Package implements PluginConstants {
 	private Log log;
 	private String baseDir;
 	private MavenProject project;
 	private String pomFile;
 	
 	public void pack(com.photon.phresco.plugins.model.Mojos.Mojo.Configuration config, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir().getPath();
 		project = mavenProjectInfo.getProject();
 		pomFile = project.getFile().getName();
 		
 		Map<String, String> configs = MojoUtil.getAllValues(config);
 		String environmentName = configs.get(ENVIRONMENT_NAME);
 		String buildName = configs.get(BUILD_NAME);
 		String buildNumber = configs.get(BUILD_NUMBER);
 		String sdkVersion = configs.get(SDK_VERSION);
 		String proguard = configs.get(PROGUARD);
 		String skipTest = configs.get(SKIP_TEST);
 		
 		String signing = configs.get(SIGNING);
 		String keystore = configs.get(KEYSTORE);
 		String storepass = configs.get(STOREPASS);
 		String keypass = configs.get(KEYPASS);
 		String alias = configs.get(ALIAS);
 		String zipAlign = configs.get(ZIP_ALIGN);
 				
 		if (StringUtils.isEmpty(environmentName)) {
 			System.out.println("Environment Name is empty . ");
 			throw new PhrescoException("Environment Name is empty . ");
 		}
 		
 		if (StringUtils.isEmpty(sdkVersion)) {
 			System.out.println("sdkVersion is empty . ");
 			throw new PhrescoException("sdkVersion is empty . ");
 		}
 		
 		PluginUtils.checkForConfigurations(new File(baseDir), environmentName);
 		
 		Boolean isZipAlign = Boolean.valueOf(zipAlign);
 		log.info("isZipAlign . " +isZipAlign);
 		Boolean isSigning = Boolean.valueOf(signing);
 		log.info("isSigning . " + isSigning);
 		
 		if(isZipAlign) {						
 			isSigning = true;
 		}
 		
 		if (isSigning) {			
 			updateAllPOMWithProfile(keystore, storepass, keypass, alias);
 		}
 		
 		updateDotPhrescoInfoFiles(isSigning, mavenProjectInfo);
 		
 		
 		log.info("Project is Building...");
 		StringBuilder sb = new StringBuilder();
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
 		}
 		//zip Align 
 		if(StringUtils.isNotEmpty(zipAlign)) {			
 			sb.append(STR_SPACE);
 			boolean zipAlignVal = Boolean.valueOf(zipAlign);
 			sb.append(HYPHEN_D + ZIP_ALIGN_SKIP + EQUAL + !zipAlignVal);
 		}
 		//signing
 		if (isSigning) {
 			sb.append(STR_SPACE);
 			sb.append(PSIGN);
 		}
 				
 		List<Parameter> parameters = config.getParameters().getParameter();
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
 		}		
 		if(!Constants.POM_NAME.equals(pomFile)) {
 			sb.append(STR_SPACE);
 			sb.append(Constants.HYPHEN_F);
 			sb.append(STR_SPACE);
 			sb.append(pomFile);
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
 	
 	/**
 	 * This function is used to change the Unit,functional and performance info files
 	 * for displaying the signing check box on Unit,functional and performance build 
 	 * pop-up's based on signing value from main build pop-up
 	 * @param isSigning
 	 * @param mavenProjectInfo
 	 */
 	private void updateDotPhrescoInfoFiles(Boolean isSigning, MavenProjectInfo mavenProjectInfo){
 		
 		
 			
 			String baseDir = mavenProjectInfo.getBaseDir().toString();
 			
 			
 			String unitXmlFile = baseDir + File.separator + DOT_PHRESCO_FOLDER + File.separator + UNIT_INFO_FILE;
 			String functionalXmlFile = baseDir + File.separator +DOT_PHRESCO_FOLDER + File.separator + FUNCTIONAL_INFO_FILE;
 			String performanceXmlFile = baseDir + File.separator +DOT_PHRESCO_FOLDER + File.separator + PERFORMANCE_INFO_FILE;
 			MojoProcessor mojoObj;
 			try {
 				mojoObj = new MojoProcessor(new File(unitXmlFile));
 				Parameter unitSigningParameter = mojoObj.getParameter("unit-test", "signing");
 				unitSigningParameter.setShow(isSigning);
 				mojoObj.save();
 				
 				mojoObj = new MojoProcessor(new File(functionalXmlFile));
				Parameter functionalSigningParameter = mojoObj.getParameter("functional-test-robotium", "signing");
				functionalSigningParameter.setShow(isSigning);
				mojoObj.save();
 				
				mojoObj = new MojoProcessor(new File(performanceXmlFile));
 				Parameter performanceSigningParameter = mojoObj.getParameter("performance-test", "signing");
 				performanceSigningParameter.setShow(isSigning);
 				mojoObj.save();
 				
 			} catch (PhrescoException e) {
 				
 				e.printStackTrace();
 			}
 	}
 	
 	private void updateAllPOMWithProfile(String keystore, String storepass, String keypass, String alias) throws PhrescoException {
 		log.info("baseDir " + baseDir);
 		List<String> pomsTobeUpdated = new ArrayList<String>();
 		pomsTobeUpdated.add("");
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_UNITTEST_DIR);
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_FUNCTEST_DIR);
 		pomsTobeUpdated.add(Constants.POM_PROP_KEY_PERFORMANCETEST_DIR);
 		
 		List<String> projPoms = new ArrayList<String>(pomsTobeUpdated.size());
 		for (String pomTobeUpdated : pomsTobeUpdated) {
 			// get root dir
 			StringBuilder sb = new StringBuilder(baseDir);
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
 			log.info("pom updating for " + projPom);
 			createAndroidProfile(projPom, keystore, storepass, keypass, alias);
 		}
 	}
 	
 	private String createAndroidProfile(String filePath, String keystore, String storepass, String keypass, String alias) throws PhrescoException {
 		log.info("Entering Method Build.createAndroidProfile() " + filePath);
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
