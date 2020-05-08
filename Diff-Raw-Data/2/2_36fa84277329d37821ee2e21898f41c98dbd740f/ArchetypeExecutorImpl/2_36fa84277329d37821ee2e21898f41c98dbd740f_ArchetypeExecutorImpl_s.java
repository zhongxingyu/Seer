 /*
  * ###
  * Phresco Service Implemenation
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
 /*******************************************************************************
  * Copyright (c) 2011 Photon.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Photon Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.photon.in/legal/ppl-v10.html
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Contributors:
  *     Photon - initial API and implementation
  ******************************************************************************/
 package com.photon.phresco.service.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.ArchetypeExecutor;
 import com.photon.phresco.service.api.DbManager;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.model.ServerConfiguration;
 import com.photon.phresco.service.util.ServerConstants;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class ArchetypeExecutorImpl implements ArchetypeExecutor,
         ServerConstants, Constants {
     private static final Logger S_LOGGER 			= Logger.getLogger(ArchetypeExecutorImpl.class);
     private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
     private static final String INTERACTIVE_MODE 	= "false";
     public static final String WINDOWS 			= "Windows";
     private static final String PHRESCO_FOLDER_NAME = "phresco";
     private static final String DOT_PHRESCO_FOLDER 	= "." + PHRESCO_FOLDER_NAME;
 //    private ServerConfiguration serverConfiguration; 
     private DbManager dbManager = null;
     
     public ArchetypeExecutorImpl(ServerConfiguration serverConfiguration) throws PhrescoException {
 //        this.serverConfiguration = serverConfiguration;
         PhrescoServerFactory.initialize();
         dbManager = PhrescoServerFactory.getDbManager();
     }
 
     public void execute(ProjectInfo projectInfo, String tempFolderPath) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ArchetypeExecutorImpl.execute(ProjectInfo info)");
 			S_LOGGER.debug("execute() ProjectCode=" + projectInfo.getProjectCode());
 		}
 		try {
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String customerId = projectInfo.getCustomerIds().get(0);
 			String commandString = buildCommandString(applicationInfo, projectInfo.getVersion(), customerId);
 			if (S_LOGGER.isDebugEnabled()) {
 				S_LOGGER.debug("command String " + commandString);
 			}
 			// the below implementation is required since a new command or shell is forked from the 
 			//existing running web server command or shell instance
 			BufferedReader bufferedReader = Utility.executeCommand(commandString, tempFolderPath);
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				if (S_LOGGER.isDebugEnabled()) {
 					S_LOGGER.debug(line);
 				}
 			}
 			createProjectFolders(projectInfo, applicationInfo.getAppDirName(), new File(tempFolderPath));
 			updateRepository(customerId, applicationInfo.getAppDirName(), new File(tempFolderPath));
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
     private void updateRepository(String customerId, String appDirName,	File tempFolderPath) throws PhrescoException {
 		RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
 		File pomFile = new File(tempFolderPath, appDirName + "/pom.xml");
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			processor.addRepositories(customerId, repoInfo.getGroupRepoURL());
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void createProjectFolders(ProjectInfo info, String appDirName, File file) throws PhrescoException {
     	if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ArchetypeExecutorImpl.createProjectFolders(ProjectInfo info, File file)");
 			S_LOGGER.debug("createProjectFolders()  path="+file.getPath());
 		}
         //create .phresco folder inside the project
     	if (isDebugEnabled) {
 			S_LOGGER.debug("createProjectFolders()  ProjectCode=" + info.getProjectCode());
 		}
     	File phrescoFolder = new File(file.getPath() + File.separator + appDirName + File.separator + DOT_PHRESCO_FOLDER);
         phrescoFolder.mkdirs();
         if (isDebugEnabled) {
 			S_LOGGER.info("create .phresco folder inside the project");
 		}
        ProjectUtils.writeProjectInfo(info, phrescoFolder);
     }
 
     private String buildCommandString(ApplicationInfo info, String projectVersion, String customerId) throws PhrescoException {
     	if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ArchetypeExecutorImpl.buildCommandString(ProjectInfo info)");
 			S_LOGGER.debug("buildCommandString() ProjectCode=" + info.getCode());
 		}
 
     	if(StringUtils.isEmpty(customerId)) {
     		throw new PhrescoException("Customer Id Should Not Be Null");
     	}
     	String techId = info.getTechInfo().getId();
     	ArtifactGroup archetypeInfo = dbManager.getArchetypeInfo(techId, customerId);
     	//TODO to sort version 
     	ArtifactInfo artifactInfo = archetypeInfo.getVersions().get(0);
     	String version = artifactInfo.getVersion();
     	RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
     	
         StringBuffer commandStr = new StringBuffer();
         commandStr.append(Constants.MVN_COMMAND).append(Constants.STR_BLANK_SPACE)
                 .append(Constants.MVN_ARCHETYPE).append(STR_COLON).append(Constants.MVN_GOAL_GENERATE)
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_ARCHETYPEGROUPID).append(Constants.STR_EQUALS).append(archetypeInfo.getGroupId())
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_ARCHETYPEARTIFACTID).append(Constants.STR_EQUALS).append(archetypeInfo.getArtifactId())
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_ARCHETYPEVERSION).append(Constants.STR_EQUALS).append(version)
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_GROUPID).append(Constants.STR_EQUALS).append("com.photon.phresco")
                 .append(Constants.STR_BLANK_SPACE)
                .append(ARCHETYPE_ARTIFACTID).append(Constants.STR_EQUALS).append(STR_DOUBLE_QUOTES).append(info.getAppDirName()).append(STR_DOUBLE_QUOTES) //artifactId --> project name could have space in between
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_VERSION).append(Constants.STR_EQUALS).append(projectVersion)
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_ARCHETYPEREPOSITORYURL).append(Constants.STR_EQUALS).append(repoInfo.getGroupRepoURL())
                 .append(Constants.STR_BLANK_SPACE)
                 .append(ARCHETYPE_INTERACTIVEMODE).append(Constants.STR_EQUALS).append(INTERACTIVE_MODE);
         return commandStr.toString();
     }
 
 }
