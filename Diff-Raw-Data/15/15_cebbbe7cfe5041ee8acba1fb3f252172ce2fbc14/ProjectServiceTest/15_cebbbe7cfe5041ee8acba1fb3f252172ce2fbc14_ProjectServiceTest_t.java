 package com.photon.phresco.framework.rest.api;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.commons.model.UserPermissions;
 
 public class ProjectServiceTest extends LoginServiceTest {
 	ProjectService projectService = new ProjectService();
 
 	@Test
 	public void createProjectTest() {
 		ProjectInfo projectInfo = createProjectInfo();
 		Response response = projectService.createProject(projectInfo, userId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200 , response.getStatus());
 		assertEquals("Project created Successfully", responseInfo.getMessage());
 	}
 	
 	@Test
 	public void createProjectForGitTest() {
 		ProjectInfo projectInfo = projectInfo();
 		Response response = projectService.createProject(projectInfo, userId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200 , response.getStatus());
 		assertEquals("Project created Successfully", responseInfo.getMessage());
 	}
 
 
 	@Test
 	public void listProjectsTest() {
 		Response response = projectService.list(customerId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200, response.getStatus());
 		assertEquals("Project List Successfully", responseInfo.getMessage());
 	}
 
 
 	@Test
 	public void editProjectTest() {
 		Response response = projectService.editProject("TestProject", customerId);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200 , response.getStatus());
 		assertEquals("Project edited Successfully", responseInfo.getMessage());
 	}
 
 
 	@Test
 	public void updateProjectTest() {
 		ProjectInfo projectInfo = createProjectInfo();
 		Response response = projectService.updateProject(projectInfo, userId);
 		ResponseInfo<ProjectInfo> resopnseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200 , response.getStatus());
 		assertEquals("Project updated Successfully", resopnseInfo.getMessage());
 	}
 
 	@Test
 	public void editApplicationWithoutUserId() {
 		Response response = projectService.editApplication(appDirName);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals("Application edited Successfully", responseInfo.getMessage());
 		assertEquals(200 , response.getStatus());
 	}
 
 
 	@Test
 	public void updateApplicationFeaturesTest() {
 		List<SelectedFeature> selectedFeatures = getSelectedFeatures();
 		Response response = projectService.updateApplicationFeatures(selectedFeatures, appDirName, userId, customerId);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals("Features updated successfully", responseInfo.getMessage());
 		assertEquals(200 , response.getStatus());
 	}
 
 	@Test
 	public void updateApplication() {
 		ApplicationInfo appInfo = getApplicationInfo();
 		Response response  = projectService.updateApplication(appDirName, appInfo, userId, customerId);
 		ResponseInfo<ApplicationInfo> responseInfo =  (ResponseInfo<ApplicationInfo>) response.getEntity();
 		int status = response.getStatus();
 		assertEquals("Application updated successfully", responseInfo.getMessage());
 		assertEquals(200 , response.getStatus());
 	}
 
 
 	@Test
 	public void createProjectTestsWithoutUserId() {
 		ProjectInfo projectInfo = createProjectInfo();
 		Response response = projectService.createProject(projectInfo, "");
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(400 , response.getStatus());
 		assertEquals("UnAuthorized User", responseInfo.getMessage());
 	}
 
 
 	@Test
 	public void listAppInfosTest() {
 		Response response = projectService.appinfoList(customerId, projectId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200, response.getStatus());
 		assertEquals("Application infos returned Successfully", responseInfo.getMessage());
 	}
 
 	@Test
 	public void listAppInfosWithoutCustomerIdTest() {
 		Response response = projectService.appinfoList("", projectId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200, response.getStatus());
 		assertEquals("No application to return", responseInfo.getMessage());
 	} 
 
 	@Test
 	public void updateProjectWithoutUserIdTest() {
 		ProjectInfo projectInfo = createProjectInfo();
 		Response response = projectService.updateProject(projectInfo, "");
 		ResponseInfo<ProjectInfo> resopnseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(400 , response.getStatus());
 		assertEquals("UnAuthorized User", resopnseInfo.getMessage());
 	} 
 
 	@Test
 	public void updateApplicationFeatureswithOutUserId() {
 		List<SelectedFeature> selectedFeatures = getSelectedFeatures();
 		Gson gson = new Gson();
 		Response response = projectService.updateApplicationFeatures(selectedFeatures, appDirName, "", customerId);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals("UnAuthorized User", responseInfo.getMessage());
 		assertEquals(400 , response.getStatus());
 	}
 
 	@Test
 	public void updateApplicationWithoutUserId() {
 		ApplicationInfo appInfo = getApplicationInfo();
 		Response response  = projectService.updateApplication(appDirName, appInfo, "", customerId);
 		ResponseInfo<ApplicationInfo> responseInfo =  (ResponseInfo<ApplicationInfo>) response.getEntity();
 		int status = response.getStatus();
 		assertEquals("UnAuthorized User", responseInfo.getMessage());
 		assertEquals(400 , response.getStatus());
 	}
 
 
 	@Test
 	public void getPermissionTest() {
 		Response response = projectService.getPermission(userId);
 		ResponseInfo<UserPermissions> responseInfo = (ResponseInfo<UserPermissions>) response.getEntity();
 		assertEquals("Permission for user returned Successfully", responseInfo.getMessage());
 		assertEquals(200 , response.getStatus());
 	} 
 
 	@Test
 	public void deleteprojectTest() {
 		ProjectService service = new ProjectService();
 		Response response = service.deleteproject(getCollections(appDirName));
 		ResponseInfo responseInfo = (ResponseInfo) response.getEntity();
 		assertEquals(200, response.getStatus());
 		assertEquals("Application deleted Successfully", responseInfo.getMessage());
 	}
 
 	@Test
 	public void createProjectTestDemo() {
 		ProjectInfo projectInfo = createProjectInfo();
 		projectInfo.getAppInfos().get(0).setSelectedWebservices(null);
 		Response response = projectService.createProject(projectInfo, userId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		assertEquals(200 , response.getStatus());
 		assertEquals("Project created Successfully", responseInfo.getMessage());
 	}
 
 
 	@Test
 	public void listProjectsWithoutProjectInfoTest() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		Response response = projectService.list(customerId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals(400, response.getStatus());
 		assertEquals("Project List failed", responseInfo.getMessage());
 	}
 
 	@Test
 	public void listAppInfosWithoutProjectInfoTest() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		Response response = projectService.appinfoList(customerId, projectId);
 		ResponseInfo<ProjectInfo> responseInfo =  (ResponseInfo<ProjectInfo>) response.getEntity();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals(400, response.getStatus());
 		assertEquals("Application info failed", responseInfo.getMessage());
 	}
 
 	@Test
 	public void editProjectWithoutProjectInfoTest() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		Response response = projectService.editProject("TestProject", customerId);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals(400 , response.getStatus());
 		assertEquals("Project edit failed", responseInfo.getMessage());
 	}
 
 	@Test
 	public void updateApplicationFeatureswithOutProjectInfo() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		List<SelectedFeature> selectedFeatures = getSelectedFeatures();
 		Gson gson = new Gson();
 		Response response = projectService.updateApplicationFeatures(selectedFeatures, appDirName, userId, customerId);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals("update Feature failed", responseInfo.getMessage());
 		assertEquals(400 , response.getStatus());
 	}
 
 	@Test
 	public void updateApplicationWithoutProjectInfo() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		ApplicationInfo appInfo = getApplicationInfo();
 		Response response  = projectService.updateApplication(appDirName, appInfo, userId, customerId);
 		ResponseInfo<ApplicationInfo> responseInfo =  (ResponseInfo<ApplicationInfo>) response.getEntity();
 		int status = response.getStatus();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals("Application update Failed", responseInfo.getMessage());
 		assertEquals(417 , response.getStatus());
 	}
 
 
 	@Test
 	public void editApplicationWithoutProjectInfoTest() {
 		File projectInfoPath = getProjectInfoPath();
 		File tempPath = getTempPath();
 		projectInfoPath.renameTo(tempPath);
 		Response response = projectService.editApplication(appDirName);
 		ResponseInfo<ProjectInfo> responseInfo = (ResponseInfo<ProjectInfo>) response.getEntity();
 		tempPath.renameTo(projectInfoPath);
 		assertEquals("Application edit Failed", responseInfo.getMessage());
 		assertEquals(404 , response.getStatus());
 	}
 
 	private ProjectInfo createProjectInfo() {
 		ProjectInfo projectInfo = new ProjectInfo();
 		projectInfo.setCustomerIds(getCustomer());
 		projectInfo.setId("TestProject");
 		projectInfo.setName("TestProject");
 		projectInfo.setNoOfApps(1);
 		projectInfo.setProjectCode("TestProject");
 		projectInfo.setVersion("1.0");
 		projectInfo.setProjectCode("TestProject");
 		projectInfo.setAppInfos(Collections.singletonList(getApplicationInfo()));
 		return projectInfo;
 	}
 	
 	private ProjectInfo projectInfo() {
 		ProjectInfo projectInfo = new ProjectInfo();
 		projectInfo.setCustomerIds(getCustomer());
 		projectInfo.setId("TestGitProject");
 		projectInfo.setName("TestGitProject");
 		projectInfo.setNoOfApps(1);
 		projectInfo.setProjectCode("TestGitProject");
 		projectInfo.setVersion("1.0");
 		projectInfo.setProjectCode("TestGitProject");
 		projectInfo.setAppInfos(Collections.singletonList(createAppInfo()));
 		return projectInfo;
 	}
 
 	private ApplicationInfo createAppInfo() {
 		ApplicationInfo appInfo = new ApplicationInfo();
 		appInfo.setAppDirName("TestGitProject");
 		appInfo.setCode("TestGitProject");
 		appInfo.setId("TestGitProject");
 		appInfo.setName("TestGitProject");
 		appInfo.setVersion("1.0");
 		TechnologyInfo info = new TechnologyInfo();
 		info.setAppTypeId("web-layer");
 		info.setId(techId);
 		info.setVersion("1.0");
 		appInfo.setTechInfo(info);
 		return appInfo;
 	}
 
 	private static List<SelectedFeature> getSelectedFeatures() {
 		List<SelectedFeature> selectedFeatures = new ArrayList<SelectedFeature>();
 		SelectedFeature selectedFeature = new SelectedFeature();
 		selectedFeature.setName("Spring-jms");
 		selectedFeature.setDispName("Spring-jms");
 		selectedFeature.setVersionID("a69c6875-0bb0-462c-86d5-e361d02157cc");
 		selectedFeature.setType("FEATURE");
 		selectedFeature.setModuleId("0e3cbd2a-4fe3-415f-8bc3-419c56aa6ca3");
 		selectedFeature.setArtifactGroupId("0e3cbd2a-4fe3-415f-8bc3-419c56aa6ca3");
 		selectedFeature.setPackaging("jar");
 		selectedFeatures.add(selectedFeature);
 		return selectedFeatures;
 	}
 
 
 	private static ApplicationInfo getApplicationInfo() {
 		ApplicationInfo info = new ApplicationInfo();
 		info.setAppDirName("TestProject");
 		info.setCode("TestProject");
 		info.setId("TestProject");
 		info.setCustomerIds(getCollections("photon"));
 		info.setEmailSupported(false);
 		info.setPhoneEnabled(false);
 		info.setTabletEnabled(false);
 		info.setDescription("Simple java web service Project");
 		info.setHelpText("Help");
 		info.setName("TestProject");
 		info.setPilot(false);
 		info.setUsed(false);
 		info.setDisplayName("TestProject");
 		info.setSelectedJSLibs(getCollections("99aa3901-a088-4142-8158-000f1e80f1bf"));
 		info.setVersion("1.0");
 
 		// TechnologyInfo
 
 		TechnologyInfo techInfo = new TechnologyInfo();
 		techInfo.setAppTypeId("web-layer");
 		techInfo.setVersion("1.6");
 		techInfo.setId("tech-java-webservice");
 		techInfo.setSystem(false);
 		info.setTechInfo(techInfo);
 
 		// selected Modules
 
 		List<String> selectedModules = new ArrayList<String>();
 		selectedModules.add("a69c6875-0bb0-462c-86d5-e361d02157cc");
 		info.setSelectedModules(selectedModules);
 
 
 		// server 
 
 		List<ArtifactGroupInfo> servers =  new ArrayList<ArtifactGroupInfo>();
 		ArtifactGroupInfo serverArtifactGroupInfo = new ArtifactGroupInfo();
 		serverArtifactGroupInfo.setArtifactGroupId("downloads_apache-tomcat");
 		serverArtifactGroupInfo.setDescription("Apache Tomcat");
 		serverArtifactGroupInfo.setDisplayName("Tomcat");
 		serverArtifactGroupInfo.setId("523c8806-86a8-4e61-937f-f27c8b32aa5c");
 		serverArtifactGroupInfo.setName("Eshop");
 		serverArtifactGroupInfo.setSystem(false);
 
 		List<String> serverArtifactInfoId = new ArrayList<String>();
 		serverArtifactInfoId.add("apachetomcat");
 		serverArtifactGroupInfo.setArtifactInfoIds(serverArtifactInfoId);
 
 		servers.add(serverArtifactGroupInfo);
 		info.setSelectedServers(servers);
 
 		// database
 
 		List<ArtifactGroupInfo> databases =  new ArrayList<ArtifactGroupInfo>();
 		ArtifactGroupInfo databaseArtifactGroupInfos = new ArtifactGroupInfo();
 		databaseArtifactGroupInfos.setArtifactGroupId("downloads_mysql");
 		databaseArtifactGroupInfos.setDescription("MYSQl");
 		databaseArtifactGroupInfos.setDisplayName("MySql");
 		databaseArtifactGroupInfos.setId("downloads_mysql");
 		databaseArtifactGroupInfos.setName("MySQL");
 		databaseArtifactGroupInfos.setSystem(false);
 		//		databaseArtifactGroupInfos.setArtifactGroupId("downloads.files");
 
 
 		List<String> databaseArtifactInfoId = new ArrayList<String>();
 		databaseArtifactInfoId.add("26bb9f28-e847-4099-b255-429706ceb7b9");
 		databaseArtifactGroupInfos.setArtifactInfoIds(databaseArtifactInfoId);
 
 		databases.add(databaseArtifactGroupInfos);
 		info.setSelectedDatabases(databases);
 
 		return info;
 	}
 
 	private static List<String> getCollections(String value) {
 		return Collections.singletonList(value);
 	} 
 }
