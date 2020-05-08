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
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.model.RepoDetail;
import com.photon.phresco.util.Utility;
 
 public class RepositoryServiceTest extends RestBaseTest  {
 	
 	RepositoryService repositoryservice = new RepositoryService();
 	
 	@Test
 	public void  addProjectToRepo() {	
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("svn");
 		repodetail.setCommitMessage("[artf672433]testcommit");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 		Response addProjectToRepo = repositoryservice.addProjectToRepo(appDirName, repodetail, userId, "TestProject", "TestProject");
 		ResponseInfo entity = (ResponseInfo) addProjectToRepo.getEntity();
 		Assert.assertEquals(200,addProjectToRepo.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForCommit() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		ResponseInfo<RepoDetail> responseInfo = (ResponseInfo<RepoDetail>) fetchPopUpValues.getEntity();
 		RepoDetail repodetail = (RepoDetail) responseInfo.getData();
 		Assert.assertEquals("Type Of Project", "svn", repodetail.getType());
 	}
 	
 	@Test
 	public void  getpopupvaluesForCommitableFiles() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		ResponseInfo<RepoDetail> responseInfo = (ResponseInfo<RepoDetail>) fetchPopUpValues.getEntity();
 		RepoDetail repodetail = (RepoDetail) responseInfo.getData();
 		Assert.assertEquals("List of Files changed", 2, repodetail.getRepoInfoFile().size());
 	}
 	
 	@Test
 	public void  getpopupvaluesForUpdate() {
 		String action = "update";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues(appDirName, action, userId);
 		ResponseInfo<RepoDetail> responseInfo = (ResponseInfo<RepoDetail>) fetchPopUpValues.getEntity();
 		RepoDetail repodetail = (RepoDetail) responseInfo.getData();
 		Assert.assertEquals("Repository url of the project", "https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/" + appDirName, repodetail.getRepoUrl());
 	}
 	
 	@Test
 	public void  commitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("svn");
 		repodetail.setCommitMessage("[artf710705]testcommit");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/" + appDirName);
 		
 		repodetail.setCommitableFiles(Arrays.asList("C:\\Documents and Settings\\saravanan_va\\workspace\\projects\\" + appDirName +"\\pom.xml"));
 		Response commitImportedProject = repositoryservice.commitImportedProject(repodetail, appDirName);
 		Assert.assertEquals(200,commitImportedProject.getStatus());
 	}
 	
 	@Test
 	public void  updateProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/" + appDirName);
 		Response updateImportedApplicaion = repositoryservice.updateImportedApplicaion(appDirName, repodetail);
 		Assert.assertEquals(200,updateImportedApplicaion.getStatus());
 	}
 	
 	@Test
 	public void  importProjectFromRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/3.0.0/239/");
 		Response importApplication = repositoryservice.importApplication(repodetail);
 		Assert.assertEquals(200,importApplication.getStatus());
 	}
 	
 
 	@Test
 	public void  fetchLogMessages() throws PhrescoException {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh_ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setRevision("head");
 		repodetail.setType("svn");
 		repodetail.setRepoUrl("https://insight.photoninfotech.com/svn/repos/phresco-svn-projects/ci/2.0/TestProject/");
 		Response fetchLogMessages = repositoryservice.fetchLogMessages(repodetail);
 		ResponseInfo<List<String>> responseInfo = (ResponseInfo<List<String>>) fetchLogMessages.getEntity();
 		Assert.assertEquals(5,responseInfo.getData().size());
 	}
 	
 	@Test
 	public void addGitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setCommitMessage("commit for test");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/TestGit.git");
 		Response addProjectToRepo = repositoryservice.addProjectToRepo("TestGitProject", repodetail, userId, "TestGitProject", "TestGitProject");
 		Assert.assertEquals(200,  addProjectToRepo.getStatus());
 	}
 	
 	@Test
 	public void  getpopupvaluesForGitCommit() {
 		String action = "commit";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues("TestGitProject", action, userId);
 		ResponseInfo<RepoDetail> responseInfo = (ResponseInfo<RepoDetail>) fetchPopUpValues.getEntity();
 		RepoDetail repodetail = (RepoDetail) responseInfo.getData();
 		Assert.assertEquals("Type Of Project", "git", repodetail.getType());
 	}
 	
 	@Test
 	public void  getpopupvaluesForGitUpdate() {
 		String action = "update";
 		Response fetchPopUpValues = repositoryservice.fetchPopUpValues("TestGitProject", action, userId);
 		ResponseInfo<RepoDetail> responseInfo = (ResponseInfo<RepoDetail>) fetchPopUpValues.getEntity();
 		RepoDetail repodetail = (RepoDetail) responseInfo.getData();
 		Assert.assertEquals("Repository url of the project", "https://github.com/santhosh-ja/TestGit.git", repodetail.getRepoUrl());
 	}
 	
 	@Test
 	public void  commitGitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setCommitMessage("[artf710705]testcommit");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/TestGit.git");
 		
		repodetail.setCommitableFiles(Arrays.asList( Utility.getProjectHome() +"projects\\" + "TestGitProject" +"\\pom.xml"));
 		Response commitImportedProject = repositoryservice.commitImportedProject(repodetail, "TestGitProject");
 		Assert.assertEquals(200,commitImportedProject.getStatus());
 	}
 	
 	@Test
 	public void  updateGitProjectToRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/TestGit.git");
 		Response updateImportedApplicaion = repositoryservice.updateImportedApplicaion("TestGitProject", repodetail);
 		Assert.assertEquals(200,updateImportedApplicaion.getStatus());
 	}
 	
 	@Test
 	public void  importGitProjectFromRepo() {
 		RepoDetail repodetail = new RepoDetail();
 		repodetail.setUserName("santhosh-ja");
 		repodetail.setPassword("santJ!23");
 		repodetail.setType("git");
 		repodetail.setRepoUrl("https://github.com/santhosh-ja/GitAdd.git");
 		Response importApplication = repositoryservice.importApplication(repodetail);
 		Assert.assertEquals(200,importApplication.getStatus());
 	}
 }
