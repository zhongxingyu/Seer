 package com.photon.phresco.util;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 
 public class ProjectUtilsTest {
 	
 	ProjectUtils projectUtils = new ProjectUtils();
 
 	@BeforeClass
     public static void before() throws IOException {
 		File projectFile = new File("src/test/resources/wp1-wordpress3.4.2");
 		File destDirectory = new File(Utility.getProjectHome());
 		FileUtils.copyDirectoryToDirectory(projectFile, destDirectory);
     }
 	
 	@Test
 	public void writeProjectInfoTest() throws PhrescoException {
 		File dotPhresco = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+".phresco");
 		projectUtils.writeProjectInfo(getProjectInfo("wp1-wordpress3.4.2"), dotPhresco);
 	}
 	
 	@Test
 	public void updateTestPomTest() throws PhrescoException {
 		File path = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2");
 		projectUtils.updateTestPom(path);
 	}
 	
 	@Test
 	public void updateProjectInfoTest() throws PhrescoException {
 		File projectInfoFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+".phresco"+File.separator+"project.info");
 		projectUtils.updateProjectInfo(getProjectInfo("wp1-wordpress3.4.2"), projectInfoFile);
 	}
 	
 	@Test
 	public void getProjectInfoTest() throws PhrescoException {
 		File path = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2");
 		assertNotNull(projectUtils.getProjectInfo(path));
 	}
 	
 	@Test
 	public void updatePOMWithEmptyPluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		projectUtils.updatePOMWithPluginArtifact(pomFile, null);
 	}
 	
 	@Test
 	public void updatePOMWithZipIsCorePluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId", "testArtifactId");
 		group.setPackaging("zip");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(true);
 		coreOption.setTechId("testTechId");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		artifactGroup.add(group);
 		projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
 	}
 	
 	@Test
 	public void updatePOMWithZipIsNotCoreFeaturePluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId1", "testArtifactId1");
 		group.setPackaging("zip");
 		group.setName("testName1");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(false);
 		coreOption.setTechId("testTechId1");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		Type type = Type.FEATURE;
 		group.setType(type);
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0a");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		artifactGroup.add(group);
 		projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
 	}
 	
 	@Test
 	public void removeExtractedFeaturesTest() throws PhrescoException {
 		List<ArtifactGroup> removedArtifacts = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId1", "testArtifactId1");
 		group.setPackaging("zip");
 		group.setName("testName1");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(false);
 		coreOption.setTechId("testTechId1");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		Type type = Type.FEATURE;
 		group.setType(type);
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0a");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		removedArtifacts.add(group);
		projectUtils.removeExtractedFeatures(getProjectInfo("wp1-wordpress3.4.2").getAppInfos().get(0), removedArtifacts);
 	}
 	
 	@Test
 	public void updatePOMWithZipIsNotCoreJavaScriptPluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId2", "testArtifactId2");
 		group.setPackaging("zip");
 		group.setName("testName2");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(false);
 		coreOption.setTechId("testTechId2");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		Type type = Type.JAVASCRIPT;
 		group.setType(type);
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0b");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		artifactGroup.add(group);
 		projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
 	}
 	
 	@Test
 	public void updatePOMWithZipIsNotCoreComponentPluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId3", "testArtifactId3");
 		group.setPackaging("zip");
 		group.setName("testName3");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(false);
 		coreOption.setTechId("testTechId3");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		Type type = Type.COMPONENT;
 		group.setType(type);
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0c");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		artifactGroup.add(group);
 		projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
 	}
 	
 	@Test
 	public void updatePOMWithJarPluginArtifactTest() throws PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId", "testArtifactId");
 		group.setPackaging("jar");
 		group.setGroupId("testGroupId");
 		group.setArtifactId("testArtifactId");
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		artifactGroup.add(group);
 		projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
 	}
 	
 	@Test
 	public void deleteFeatureDependenciesTest() throws PhrescoException {
 		List<ArtifactGroup> artifactGroup = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId", "testArtifactId");
 		group.setPackaging("jar");
 		group.setGroupId("testGroupId");
 		group.setArtifactId("testArtifactId");
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		artifactGroup.add(group);
 		projectUtils.deleteFeatureDependencies(getProjectInfo("wp1-wordpress3.4.2").getAppInfos().get(0), artifactGroup);
 	}
 	
 	@Test
 	public void deleteTest() throws IOException {
 		File file = new File("testFile");
 		file.createNewFile();
 		projectUtils.delete(file);
 		File file2 = new File("testDir");
 		file2.mkdir();
 		projectUtils.delete(file2);
 		File file3 = new File("testDir2");
 		file3.mkdir();
 		File file4 = new File("testDir2//testFile");
 		file4.createNewFile();
 		projectUtils.delete(file3);		
 	}
 	
 	@Test
 	public void deletePluginExecutionFromPomTest() throws IOException, PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		projectUtils.deletePluginExecutionFromPom(pomFile);
 	}
 	
 	@Test
 	public void addServerPluginTest() throws IOException, PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		projectUtils.addServerPlugin(getProjectInfo("wp1-wordpress3.4.2").getAppInfos().get(0), pomFile);
 	}
 	
 	@Test
 	public void deletePluginFromPomTest() throws IOException, PhrescoException {
 		File pomFile = new File(Utility.getProjectHome()+"wp1-wordpress3.4.2"+File.separator+"pom.xml");
 		projectUtils.deletePluginFromPom(pomFile);
 	}
 	
 	@Test
 	public void extractFeatureTest() throws IOException, PhrescoException {
 		projectUtils.ExtractFeature(getProjectInfo("wp1-wordpress3.4.2").getAppInfos().get(0));
 	}
 	
 	@Test(expected=PhrescoException.class)
 	public void testMockErrorWriteProjectInfo() throws PhrescoException {
 		File mock = org.mockito.Mockito.mock(File.class);
 		Mockito.when(mock.getPath()).thenThrow(IOException.class);
 		ProjectUtils.writeProjectInfo(getProjectInfo("wp1-wordpress3.4.2"), mock);
 	}
 	
 	
 	@Test(expected=PhrescoException.class)
 	public void testMockErrorUpdateProjectInfo() throws PhrescoException {
 		File mock = org.mockito.Mockito.mock(File.class);
 		Mockito.when(mock.getPath()).thenThrow(IOException.class);
 		ProjectUtils.updateProjectInfo(getProjectInfo("wp1-wordpress3.4.2"), mock);
 	}
 		
 	@Test
 	public void removeMarkerFilesTest() throws Exception {
 		List<ArtifactGroup> removedArtifacts = new ArrayList<ArtifactGroup>();
 		ArtifactGroup group = new ArtifactGroup("testGroupId1", "testArtifactId1");
 		group.setPackaging("zip");
 		group.setName("testName1");
 		List<CoreOption> appliesTo = new ArrayList<CoreOption>();
 		CoreOption coreOption = new CoreOption();
 		coreOption.setCore(false);
 		coreOption.setTechId("testTechId1");
 		appliesTo.add(coreOption);
 		group.setAppliesTo(appliesTo);
 		Type type = Type.FEATURE;
 		group.setType(type);
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artInfo = new ArtifactInfo();
 		artInfo.setVersion("1.0a");
 		artInfo.setScope("testScope");
 		artifactInfos.add(artInfo);
 		group.setVersions(artifactInfos);
 		removedArtifacts.add(group);
 		projectUtils.removeMarkerFiles(getProjectInfo("wp1-wordpress3.4.2").getAppInfos().get(0), removedArtifacts);
 	}
 			
 	private static ProjectInfo getProjectInfo(String appDirName) throws PhrescoException {
 		StringBuilder builder  = new StringBuilder();
 		builder.append(Utility.getProjectHome())
 		.append(appDirName)
 		.append(File.separatorChar)
 		.append(".phresco")
 		.append(File.separatorChar)
 		.append("project.info");
 		try {
 			BufferedReader bufferedReader = new BufferedReader(new FileReader(builder.toString()));
 			Gson gson = new Gson();
 			ProjectInfo projectInfo = gson.fromJson(bufferedReader, ProjectInfo.class);
 			return projectInfo;
 		} catch (JsonSyntaxException e) {
 			throw new PhrescoException(e);
 		} catch (JsonIOException e) {
 			throw new PhrescoException(e);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		}
 	}
 }
