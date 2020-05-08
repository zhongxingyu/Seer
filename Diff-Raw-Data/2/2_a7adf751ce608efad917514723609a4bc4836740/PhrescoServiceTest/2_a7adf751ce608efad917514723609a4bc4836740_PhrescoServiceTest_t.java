 /**
  * Service Web Archive
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
 package com.photon.phresco.service.rest.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.exception.PhrescoException;
 
 public class PhrescoServiceTest {
 
 	@Test
 	public void testCreateProject() throws PhrescoException, IOException {
 		ProjectService service = new ProjectService();
//		service.createProject(createApplicationInfo());
 	}
 	
 	private ProjectInfo createApplicationInfo() {
 		ProjectInfo projectInfo = new ProjectInfo();
 		projectInfo.setNoOfApps(3);
 		projectInfo.setVersion("1.0");
 		projectInfo.setProjectCode("PHR_sampleproject");
 		List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
 		appInfos.add(createAppInfo("Test1", "tech-php"));
 		appInfos.add(createAppInfo("Test2", "tech-php"));
 		appInfos.add(createAppInfo("Test3", "tech-php"));
 		projectInfo.setAppInfos(appInfos);
 		return projectInfo;
 	}
 	
 	private ApplicationInfo createAppInfo(String dirName, String techId) {
 		ApplicationInfo applicationInfo = new ApplicationInfo();
 		applicationInfo.setId("PHR_Test");
 		List<String> customerIds = new ArrayList<String>();
 		customerIds.add("photon");
 		applicationInfo.setCustomerIds(customerIds);
 		List<String> selectedModules = new ArrayList<String>();
 		selectedModules.add("mod_weather_tech_php1.0");
 		selectedModules.add("mod_commenting_system._tech_php1.0");
 		selectedModules.add("mod_reportgenerator_tech_php1.0");
 		applicationInfo.setSelectedModules(selectedModules);
 		List<String> selectedWebservices = new ArrayList<String>();
 		selectedWebservices.add("restjson");
 		applicationInfo.setSelectedWebservices(selectedWebservices);
 		TechnologyInfo techInfo = new TechnologyInfo();
 		techInfo.setVersion(techId);
 		applicationInfo.setTechInfo(techInfo);
 		applicationInfo.setAppDirName(dirName);
 		return applicationInfo;
 	}
 	
 }
