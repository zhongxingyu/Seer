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
 
 import org.apache.maven.project.MavenProject;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.plugin.commons.PluginConstants;
 
 public class FunctionalTest {
 
 	public void functionalTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		MavenProject project = mavenProjectInfo.getProject();
 		String functionalTest= PluginConstants.FUNCTIONAL;
 		String workingDir = project.getProperties().getProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR);
		RunAndroidTest.runAndroidTest(configuration, mavenProjectInfo, workingDir,functionalTest);
 	}
 }
