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
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.service.pom.POMConstants;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class RunAndroidTest implements PluginConstants {
 	
	public static void runAndroidTest(Configuration configuration, MavenProjectInfo mavenProjectInfo, String workingDir) throws PhrescoException {
 		try {			
 			Map<String, String> configs = MojoUtil.getAllValues(configuration);
 			MavenProject project = mavenProjectInfo.getProject();
 			String seleniumToolType = project.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL);
 			String baseDir = mavenProjectInfo.getBaseDir().getPath();
 			// calabash Execution
 			if(fromTest.equals("functional") && StringUtils.isNotEmpty((seleniumToolType)) && seleniumToolType.equals(CALABASH)) {
 				StringBuilder builder = new StringBuilder();
 				builder.append(CALABASH_ANDROID_COMMAND);
 				builder.append(STR_SPACE);
 				String directory = project.getBuild().getDirectory();
 				builder.append(directory + File.separator + project.getArtifactId() + DOT + POMConstants.APK);	
 				builder.append(STR_SPACE);
 				builder.append("-f junit --out test-reports");
 				Utility.executeStreamconsumer(builder.toString(), project.getBasedir() + File.separator + workingDir, project.getBasedir().getPath(), FUNCTIONAL);
 				return;
 			}
 			StringBuilder sb = new StringBuilder();
 			sb.append(MVN_CMD);
 			sb.append(STR_SPACE);
 			sb.append(MVN_PHASE_CLEAN);
 			sb.append(STR_SPACE);
 			sb.append(MVN_PHASE_INSTALL);
 			
 			String deviceValue = configs.get(DEVICES);
 			String signing = configs.get(SIGNING);
 			String device = configs.get(DEVICES_LIST);
 			System.out.println("Connected Devices . " +device);
 			
 			Boolean isSigning = Boolean.valueOf(signing);
 			System.out.println("isSigning . " + isSigning);
 			//signing
 			if (isSigning) {
 				sb.append(STR_SPACE);
 				sb.append(PSIGN);
 			}
 			
 			String otherDiviceValue = configs.get(deviceValue);
 			List<Parameter> parameters = configuration.getParameters().getParameter();
 			for (Parameter parameter : parameters) {
 				if (parameter.getPluginParameter() != null && parameter.getPluginParameter().equals(PluginConstants.PLUGIN_PARAMETER)) {
 					List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
 					for (MavenCommand mavenCommand : mavenCommands) {
 						if(mavenCommand.getKey().equalsIgnoreCase(deviceValue)) {
 							sb.append(STR_SPACE);
 							sb.append(mavenCommand.getValue());
 						} 
 					}
 				}
 				if(parameter.getKey().equalsIgnoreCase(deviceValue)) {
 					sb.append(STR_SPACE);
 					sb.append(HYPHEN_D + ANDROID_DEVICE + EQUAL + otherDiviceValue);
 				}
 			}
 			sb.append(STR_SPACE);
 			sb.append(HYPHEN_D + ANDROID_EMULATOR + EQUAL + DEFAULT);
 			File workingFile = new File(baseDir + workingDir + File.separator + project.getFile().getName());
 			if(workingFile.exists()) {
 				sb.append(STR_SPACE);
 				sb.append(Constants.HYPHEN_F);
 				sb.append(STR_SPACE);
 				sb.append(project.getFile().getName());
 			}
 			System.out.println("Command " + sb.toString());
 			Commandline commandline = new Commandline(sb.toString());
 			if(StringUtils.isNotEmpty(device)) {
 				PomProcessor pomProcessor = new PomProcessor(new File(baseDir + File.separator + PluginConstants.TEST_FOLDER + File.separator + PluginConstants.FUNCTIONAL_TEST_FOLDER + File.separator + Constants.POM_NAME));						
 				com.phresco.pom.model.PluginExecution.Configuration pluginExecutionConfiguration = pomProcessor.getPluginExecutionConfiguration(PluginConstants.MVN_ANT_PLUGIN_GRP_ID,PluginConstants.MVN_ANT_PLUGIN_ARTF_ID);
 				Element run = getTagname(PluginConstants.RUN, pluginExecutionConfiguration);			
 				run.setAttribute(PluginConstants.ADB_SERIAL, device);
 				pomProcessor.save();
 			}
 			if (StringUtils.isNotEmpty(workingDir)) {
 				commandline.setWorkingDirectory(baseDir + workingDir);
 			}
 			Process pb = commandline.execute();
 			InputStream is = new BufferedInputStream(pb.getInputStream());
 			int singleByte = 0;
 			while ((singleByte = is.read()) != -1) {
 				System.out.write(singleByte);
 			}
 		} catch (CommandLineException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			// TODO Auto-generated catch block
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static Element getTagname(String tagName,
 			com.phresco.pom.model.PluginExecution.Configuration configuration) {
 		for (Element config : configuration.getAny()) {
 			NodeList childNodes = config.getChildNodes();
 			for(int i=0 ; i< childNodes.getLength(); i ++) {
 				Node node = childNodes.item(i);
 				if(node instanceof Element) {
 					Element element = (Element) node;
 					if(tagName.equals(element.getTagName())){
 						return element;
 					}
 				}
 			}
 		}
 		return null;
 	}
 }
