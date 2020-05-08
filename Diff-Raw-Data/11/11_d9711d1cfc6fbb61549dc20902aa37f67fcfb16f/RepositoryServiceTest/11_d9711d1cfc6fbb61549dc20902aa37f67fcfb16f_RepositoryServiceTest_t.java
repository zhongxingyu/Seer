 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import java.io.File;
 import java.util.Arrays;
 
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.codehaus.plexus.util.FileUtils;
 import org.junit.Test;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.model.RepoDetail;
 import com.photon.phresco.framework.model.RepoInfo;
 import com.photon.phresco.util.Utility;
 
 public class RepositoryServiceTest extends RestBaseTest  {
 	
 	RepositoryService repositoryservice = new RepositoryService();
 	
 	@Test
 	public void  addProjectToRepo() {	
 		Response addProjectToRepo = repositoryservice.addProjectToRepo(appDirName, getSvnRepoInfo(), userId, "TestProject", "TestProject", "admin_user");
 		Assert.assertEquals(200,addProjectToRepo.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForCommit() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		Assert.assertEquals(200, fetchPopUpValues.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForCommitableFiles() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		Assert.assertEquals(200, fetchPopUpValues.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForUpdate() {
 		String action = "update";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		Assert.assertEquals(200, fetchPopUpValues.getStatus());
 	}
 	
 @Test
 	public void  commitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("itkillsme!23");
 		repodetail.setType("svn");
 		repodetail.setCommitMessage("[artf710705]testcommit");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/" + appDirName);
 		
 		repodetail.setCommitableFiles(Arrays.asList(Utility.getProjectHome() + appDirName +"\\pom.xml"));
 		RepoInfo repoInfo = new RepoInfo();
 		repoInfo.setSrcRepoDetail(repodetail);
 		Response commitImportedProject = repositoryservice.commitImportedProject(repoInfo, appDirName, "admin_user");
 		Assert.assertEquals(200,commitImportedProject.getStatus());
 	}
 	
 	@Test
 	public void  updateProjectToRepo() {
 		RepoInfo repoInfo = new RepoInfo();
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("itkillsme!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/" + appDirName);
 		repoInfo.setSrcRepoDetail(repodetail);
 		Response updateImportedApplicaion = repositoryservice.updateImportedApplicaion(appDirName,"admin_user", repoInfo );
 		Assert.assertEquals(200,updateImportedApplicaion.getStatus());
 	}
 	
 	@Test
 	public void  importProjectFromRepo() throws Exception {
 		RepoInfo repoInfo = new RepoInfo();
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("itkillsme!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/3.0.0/239/");
 		repoInfo.setSrcRepoDetail(repodetail);
		Response importApplication = repositoryservice.importApplication(repoInfo, "Admin", "santhosh_ja");
 		Assert.assertEquals(200,importApplication.getStatus());
 		
 		repodetail.setUserName("sample");
 		repoInfo.setSrcRepoDetail(repodetail);
		Response authenticationException = repositoryservice.importApplication(repoInfo, "Admin", "santhosh_ja");
 		Assert.assertEquals(200, authenticationException.getStatus());
 	}
 
 	@Test
 	public void  fetchLogMessages() throws PhrescoException {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("itkillsme!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 		Response fetchLogMessages = repositoryservice.fetchLogMessages(repodetail);
 		Assert.assertEquals(200,fetchLogMessages.getStatus());
 		
 		repodetail.setRepoUrl("http://wrong.url.com");
 		Response wrongUrlResponse = repositoryservice.fetchLogMessages(repodetail);
 		Assert.assertEquals(200, wrongUrlResponse.getStatus());
 		
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/Dharani/iphone/iPhone-Native-None/");
 		Response lessLogs = repositoryservice.fetchLogMessages(repodetail);
 		Assert.assertEquals(200, lessLogs.getStatus());
 		
 		repodetail.setUserName("sample");
 		repodetail.setPassword("sample");
 		Response unauthorisedResponse = repositoryservice.fetchLogMessages(repodetail);
 		Assert.assertEquals(200, unauthorisedResponse.getStatus());
 	}
 	
 	@Test
 	public void addGitProjectToRepo() {
 		Response addProjectToRepo = repositoryservice.addProjectToRepo("TestGitProject", getGitRepoInfo(), userId, "TestGitProject", "TestGitProject", "admin_user");
 		Assert.assertEquals(200,  addProjectToRepo.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForGitCommit() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues("TestGitProject", action, userId);
 		Assert.assertEquals(200, fetchPopUpValues.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForGitUpdate() {
 		String action = "update";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues("TestGitProject", action, userId);
 		Assert.assertEquals(200, fetchPopUpValues.getStatus());
 	}
 	
 	@Test
 	public void  commitGitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setCommitMessage("[artf710705]testcommit");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/TestGit.git");
 		
 		repodetail.setCommitableFiles(Arrays.asList(Utility.getProjectHome() + "TestGitProject" +"\\pom.xml"));
 		RepoInfo info = new RepoInfo();
 		info.setSrcRepoDetail(repodetail);
 		Response commitImportedProject = repositoryservice.commitImportedProject(info, "TestGitProject" ,"admin_user");
 		Assert.assertEquals(200,commitImportedProject.getStatus());
 	}
 	
 	@Test
 	public void  updateGitProjectToRepo() {
 		RepoInfo repoInfo = new RepoInfo();
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/TestGit.git");
 		repoInfo.setSrcRepoDetail(repodetail);
 		Response updateImportedApplicaion = repositoryservice.updateImportedApplicaion("TestGitProject","admin_user", repoInfo);
 		Assert.assertEquals(200,updateImportedApplicaion.getStatus());
 	}
 	
 	@Test
 	public void  importGitProjectFromRepo() throws Exception {
 		RepoInfo repoInfo = new RepoInfo();
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/GitAdd.git");
 		repoInfo.setSrcRepoDetail(repodetail);
		Response importApplication = repositoryservice.importApplication(repoInfo, "Admin", "santhosh_ja");
 		Assert.assertEquals(200,importApplication.getStatus());
 	}
 	
 	@Test
 	public void importTfsProjectFromRepo() throws Exception {
 		RepoInfo repoInfo = new RepoInfo();
 		RepoDetail repoDetail = new RepoDetail();
 		repoDetail.setUserName("vivekraja.vasudevan@photoninfotech.com");
 		repoDetail.setPassword("phresco123");
 		repoDetail.setType("tfs");
 		repoDetail.setProName("raj");
 		repoDetail.setServerPath("raj/jsa");
 		repoDetail.setRepoUrl("https://vivekraja.visualstudio.com/DefaultCollection");
 		repoInfo.setSrcRepoDetail(repoDetail);
		Response importApplication = repositoryservice.importApplication(repoInfo, "Admin", "santhosh_ja");
 		Assert.assertEquals(200,importApplication.getStatus());
 		Assert.assertEquals(true, true);
 	}
 	
 	@Test
 	public void  buildRepoTest() throws Exception {
 		Response response = repositoryservice.getBuildRepoStructure(customerId, userId, projectId);
 		Assert.assertEquals(200, response.getStatus());
 	} 
 	
 	@Test
 	public void  getSourceRepoTest() throws Exception {
 		Response response = repositoryservice.getSourceRepo(customerId, "TestGitProject", "santhosh_ja", "farewellJ123");
 		Assert.assertEquals(200, response.getStatus());
 	} 
 
 	@Test
 	public void  getArtifactInfo() throws Exception {
 		Response response = repositoryservice.getArtifactInfo(customerId, appDirName, userId, "release", "1.0", "");
 		Assert.assertEquals(200, response.getStatus());
 	}
 
 	@Test
 	public void  downloadTest() throws Exception {
 		Response response = repositoryservice.downloadService(customerId, appDirName, userId, "release", "1.0", "");
 		Assert.assertEquals(200, response.getStatus());
 	}
 	
 	private RepoInfo getSvnRepoInfo() {
 		RepoInfo repoInfo = new RepoInfo();
 
 		RepoDetail srcRepoDetail = new RepoDetail();
 		srcRepoDetail.setType("svn");
 		srcRepoDetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 		srcRepoDetail.setUserName("santhosh_ja");
 		srcRepoDetail.setPassword("itkillsme!23");
 		srcRepoDetail.setCommitMessage("[artf778042] Add test");
 		srcRepoDetail.setPassPhrase("");
 		repoInfo.setSrcRepoDetail(srcRepoDetail);
 
 //		RepoDetail phrescoRepoDetail = new RepoDetail();
 //		phrescoRepoDetail.setType("svn");
 //		phrescoRepoDetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 //		phrescoRepoDetail.setUserName("santhosh_ja");
 //		phrescoRepoDetail.setPassword("farewellJ123");
 //		phrescoRepoDetail.setCommitMessage("[artf778042] Add test");
 //		phrescoRepoDetail.setPassPhrase("");
 //		repoInfo.setPhrescoRepoDetail(phrescoRepoDetail);
 //
 //		RepoDetail testRepoDetail = new RepoDetail();
 //		testRepoDetail.setType("svn");
 //		testRepoDetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 //		testRepoDetail.setUserName("santhosh_ja");
 //		testRepoDetail.setPassword("farewellJ123");
 //		testRepoDetail.setCommitMessage("[artf778042] Add test");
 //		testRepoDetail.setPassPhrase("");
 //		repoInfo.setTestRepoDetail(testRepoDetail);
 
 		repoInfo.setSplitPhresco(false);
 		repoInfo.setSplitTest(false);
 
 		return repoInfo;
 	}
 	
 	private RepoInfo getGitRepoInfo() {
 		RepoInfo repoInfo = new RepoInfo();
 
 		RepoDetail srcRepoDetail = new RepoDetail();
 		srcRepoDetail.setType("git");
 		srcRepoDetail.setRepoUrl("https://github.com/santhosh-ja/TestProject_src.git");
 		srcRepoDetail.setUserName("santhosh-ja");
 		srcRepoDetail.setPassword("santJ!23");
 		srcRepoDetail.setCommitMessage("[artf778042] Add test");
 		srcRepoDetail.setPassPhrase("");
 		repoInfo.setSrcRepoDetail(srcRepoDetail);
 
 //		RepoDetail phrescoRepoDetail = new RepoDetail();
 //		phrescoRepoDetail.setType("git");
 //		phrescoRepoDetail.setRepoUrl("https://github.com/santhosh-ja/TestProject_phresco.git");
 //		phrescoRepoDetail.setUserName("santhosh-ja");
 //		phrescoRepoDetail.setPassword("santJ!23");
 //		phrescoRepoDetail.setCommitMessage("[artf778042] Add test");
 //		phrescoRepoDetail.setPassPhrase("");
 //		repoInfo.setPhrescoRepoDetail(phrescoRepoDetail);
 //
 //		RepoDetail testRepoDetail = new RepoDetail();
 //		testRepoDetail.setType("git");
 //		testRepoDetail.setRepoUrl("https://github.com/santhosh-ja/TestProject_test.git");
 //		testRepoDetail.setUserName("santhosh-ja");
 //		testRepoDetail.setPassword("santJ!23");
 //		testRepoDetail.setCommitMessage("[artf778042] Add test");
 //		testRepoDetail.setPassPhrase("");
 //		repoInfo.setTestRepoDetail(testRepoDetail);
 
 		repoInfo.setSplitPhresco(false);
 		repoInfo.setSplitTest(false);
 
 		return repoInfo;
 	}
 }
