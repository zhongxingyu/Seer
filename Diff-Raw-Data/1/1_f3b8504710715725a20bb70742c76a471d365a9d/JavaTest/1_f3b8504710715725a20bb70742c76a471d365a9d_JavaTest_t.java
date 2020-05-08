 /**
  * java-phresco-plugin
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
 package com.photon.phresco.plugins.java;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Plugin;
 import com.phresco.pom.model.PluginExecution;
 import com.phresco.pom.util.PomProcessor;
 
 public class JavaTest implements PluginConstants {
     private File baseDir;
     private File testConfigPath;
     private MavenProject project;
     private File workingDirectory;
     private String subModule = "";
     private File pomFile;
     private String dotPhrescoDirName;
     private File dotPhrescoDir;
     private File srcDirectory;
 
     public void runTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException{
         try {
             baseDir = mavenProjectInfo.getBaseDir();
             if (StringUtils.isNotEmpty(mavenProjectInfo.getModuleName())) {
                 subModule = mavenProjectInfo.getModuleName();
             }
             project = mavenProjectInfo.getProject();
             workingDirectory = new File(baseDir.getPath() + File.separator + subModule);
             Map<String, String> configs = MojoUtil.getAllValues(configuration);
             String testAgainst = configs.get(TEST_AGAINST);
             String environment = configs.get(ENVIRONMENT_NAME);
             String webSeviceName = configs.get(WEBSERVICES);
             String goalPackBeforeTest = "";
             PluginUtils pluginUtils = new PluginUtils();
             dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
             dotPhrescoDir = baseDir;
             if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
                 dotPhrescoDir = new File(baseDir.getParent() + File.separator + dotPhrescoDirName);
             }
             dotPhrescoDir = new File(dotPhrescoDir.getPath() + File.separatorChar + subModule);
             pomFile = pluginUtils.getPomFile(dotPhrescoDir, workingDirectory);
             File splitProjectDirectory = pluginUtils.getSplitProjectSrcDir(pomFile, dotPhrescoDir, subModule);
             srcDirectory = workingDirectory;
             if (splitProjectDirectory != null) {
                 srcDirectory = splitProjectDirectory;
             }
             PomProcessor processor = new PomProcessor(new File(workingDirectory, pomFile.getName()));
             String reportDir = "";
             if (testAgainst.equals(JAVA)) {
                 reportDir = processor.getProperty("phresco.unitTest.java.report.dir");
             } else if (testAgainst.equals(JS)) {
                 reportDir = processor.getProperty("phresco.unitTest.js.report.dir");
                 ApplicationInfo appInfo = pluginUtils.getAppInfo(dotPhrescoDir);
                 String techId = appInfo.getTechInfo().getId();
                 if (environment != null && webSeviceName != null) {
                     copyUnitInfoFile(environment, webSeviceName, techId);
                 }
                 goalPackBeforeTest = getGoalPackBeforeTest(workingDirectory);
             }
             File reportLoc = new File(workingDirectory.getPath() + File.separator  + reportDir);
             if (reportLoc.exists()) {
                 pluginUtils.delete(reportLoc);
             }
 
             buildCommand(configuration, testAgainst, goalPackBeforeTest, subModule);
         } catch (Exception e) {
			e.printStackTrace();
             throw new PhrescoException(e); 
         }
     }
 
     private void copyUnitInfoFile(String environment, String webSeviceName, String techId) throws PhrescoException {
         try {
             PomProcessor processor = new PomProcessor( new File(workingDirectory.getPath() + File.separator + pomFile.getName()));
             String testSourcePath = processor.getProperty("phresco.env.test.config.xml");
             testConfigPath = new File(srcDirectory + File.separator + testSourcePath);
             if (!techId.equals(TechnologyTypes.JAVA_STANDALONE) && !techId.equals(TechnologyTypes.JAVA_WEBSERVICE) ) {
                 if(StringUtils.isNotEmpty(testSourcePath)){
 	            	PluginUtils utils = new PluginUtils();
 	                String fullPathNoEndSeparator = FilenameUtils.getFullPathNoEndSeparator(testConfigPath.getAbsolutePath());
 	                File fullPathNoEndSeparatorFile = new File(fullPathNoEndSeparator);
 	                fullPathNoEndSeparatorFile.mkdirs();
 	                utils.executeUtil(environment, webSeviceName, dotPhrescoDir.getPath(), testConfigPath);
                 } 
             }
         } catch (PhrescoPomException e) {
             throw new  PhrescoException(e);
         } catch (PhrescoException e) {
             throw new  PhrescoException(e);
         } 
     }
 
     private void buildCommand(Configuration configuration, String testAgainst, String goalPackBeforeTest, String subModule) throws PhrescoException {
         String mavenCommandValue = null;
         if (testAgainst != null) {
             List<Parameter> parameters = configuration.getParameters().getParameter();
             for (Parameter parameter : parameters) {
                 if (parameter.getPluginParameter() != null && parameter.getPluginParameter().equals(PLUGIN_PARAMETER)) {
                     List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
                     for (MavenCommand mavenCommand : mavenCommands) {
                         if (parameter.getValue().equals(testAgainst) && mavenCommand.getKey().equals(testAgainst)) {
                             mavenCommandValue = mavenCommand.getValue();
                         }
                     }
                 }
             }
         }
         executeTest(mavenCommandValue, testAgainst, goalPackBeforeTest, subModule);
     }
 
     private void executeTest(String mavenCommandValue, String testAgainst, String goalPackBeforeTest, String projectModule) throws PhrescoException {
         System.out.println("-----------------------------------------");
         System.out.println("T E S T S");
         System.out.println("-----------------------------------------");
         try {
             StringBuilder sb = new StringBuilder();
             if(StringUtils.isNotEmpty(goalPackBeforeTest)) {
                 sb.append("mvn jstest:pack-before-test");
             } else {
                 sb.append(UNITTEST_COMMAND);
             }
             sb.append(STR_SPACE).
             append(mavenCommandValue);
             //                        if (StringUtils.isNotEmpty(projectModule)) {
             //                                sb.append(STR_SPACE).append("-pl "+ projectModule);
             //                        }
             sb.append(STR_SPACE);
             sb.append(Constants.HYPHEN_F);
             sb.append(STR_SPACE); 
             sb.append(pomFile.getName());
             System.out.println("COMMAND IS  " + sb.toString());
             boolean status = Utility.executeStreamconsumer(sb.toString(), workingDirectory.getPath(), workingDirectory.getPath(), UNIT);
             if(!status) {
                 throw new MojoExecutionException(Constants.MOJO_ERROR_MESSAGE);
             }
         } catch (Exception e) {
             throw new  PhrescoException(e);
         }
     }
 
     private String getGoalPackBeforeTest(File baseDir) throws PhrescoException {
         try {
             PomProcessor processor = new PomProcessor(new File(baseDir.getPath() + File.separator + pomFile.getName()));
             Plugin plugin = processor.getPlugin("net.awired.jstest", "jstest-maven-plugin");
             if(plugin != null && plugin.getExecutions() != null && CollectionUtils.isNotEmpty(plugin.getExecutions().getExecution())) {
                 List<PluginExecution> execution = plugin.getExecutions().getExecution();
                 for (PluginExecution pluginExecution : execution) {
                     if(pluginExecution.getGoals() != null && CollectionUtils.isNotEmpty(pluginExecution.getGoals().getGoal())) {
                         List<String> goals = pluginExecution.getGoals().getGoal();
                         for (String goal : goals) {
                             if(PACK_BEFORE_TEST.equals(goal)) {
                                 return goal;
                             }
                         }
                     }
                 }
             }
         } catch (PhrescoPomException e) {
             throw new PhrescoException(e);
         }
         return "";
     }
 
 }
