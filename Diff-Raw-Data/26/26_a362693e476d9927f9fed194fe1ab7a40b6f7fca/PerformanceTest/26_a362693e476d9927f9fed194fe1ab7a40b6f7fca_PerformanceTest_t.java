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
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.commons.lang.*;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.cli.*;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.*;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.*;
 import com.photon.phresco.util.Constants;
 
 public class PerformanceTest implements PluginConstants {
 	private File WorkingFile ;
 	public void performanceTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			System.out.println("@@@@@@@@@@@@@PerformanceTest @@@@@@@@@@@ ");
 			Map<String, String> configs = MojoUtil.getAllValues(configuration);
 			String baseDir = mavenProjectInfo.getBaseDir().getPath();
 			String deviceList= configs.get(DEVICES_LIST);
 			String signing = configs.get(SIGNING);
 			MavenProject project = mavenProjectInfo.getProject();
 			
 			if (StringUtils.isEmpty(deviceList)) {
 				System.out.println("Device list is empty . ");
 				throw new PhrescoException("Device list is empty . ");
 			}
 			
 			StringBuilder sb = new StringBuilder();
 			sb.append(MVN_CMD);
 			sb.append(STR_SPACE);
 			sb.append(MVN_PHASE_CLEAN);
 			sb.append(STR_SPACE);
 			sb.append(MVN_PHASE_INSTALL);
 			
 			Boolean isSigning = Boolean.valueOf(signing);
 			System.out.println("isSigning . " + isSigning);
 			//signing
 			if (isSigning) {
 				sb.append(STR_SPACE);
 				sb.append(PSIGN);
 			}
 			
 			sb.append(STR_SPACE);
 			sb.append(HYPHEN_D + DEVICES_LIST + EQUAL + deviceList);
 			
 			String workingDir = project.getProperties().getProperty(Constants.POM_PROP_KEY_PERFORMANCETEST_DIR);
			String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 			WorkingFile = new File(baseDir + workingDir + File.separator + project.getFile().getName());
 			File baseDirFile =new File(baseDir);
 			if(!WorkingFile.exists()){
 				WorkingFile = new File(baseDirFile.getParentFile().getParentFile() + workingDir + File.separator + project.getFile().getName());
 			}
 			if(WorkingFile.exists()) {
 				sb.append(STR_SPACE);
 				sb.append(Constants.HYPHEN_F);
 				sb.append(STR_SPACE);
 				sb.append(project.getFile().getName());
 			}
			
 			Commandline commandline = new Commandline(sb.toString());
			if (StringUtils.isNotEmpty(workingDir)&& StringUtils.isNotEmpty(dotPhrescoDirName)) {
 				commandline.setWorkingDirectory(baseDirFile.getParentFile().getParentFile() + workingDir);
			}else{
				commandline.setWorkingDirectory(baseDirFile.getAbsoluteFile()+File.separator+ workingDir);
 			}
 			
 			Process pb = commandline.execute();
 			InputStream is = new BufferedInputStream(pb.getInputStream());
 			int singleByte = 0;
 			while ((singleByte = is.read()) != -1) {
 				System.out.write(singleByte);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 }
