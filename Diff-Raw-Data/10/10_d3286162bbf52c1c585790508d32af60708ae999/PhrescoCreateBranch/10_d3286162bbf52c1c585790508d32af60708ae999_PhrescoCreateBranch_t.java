 package com.photon.phresco.plugins;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.impl.util.FrameworkUtil;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 /**
  * Goal which create branch
  * 
  * @goal createBranch
  * 
  */
 public class PhrescoCreateBranch extends AbstractMojo {
 
 	/**
      * The Maven project.
      * 
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
     
     /**
      * @parameter expression="${branchName}"
      * @readonly
      */
     protected String branchName;
     
     /**
      * @parameter expression="${comment}"
      * @readonly
      */
     protected String comment;
     
     /**
      * @parameter expression="${appDirName}"
      * @readonly
      */
     protected String appDirName;
     
     /**
      * @parameter expression="${releaseVersion}"
      * @readonly
      */
     protected String version;
     
     /**
      * @parameter expression="${currentBranch}"
      * @readonly
      */
     protected String currentBranch;
     
     /**
      * @parameter expression="${downloadOption}"
      * @readonly
      */
     protected boolean downloadOption;
     
     
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		createBranch(appDirName, version, comment, currentBranch, branchName, downloadOption);
 	}
 	
 	public void createBranch(String appDirName, String version,  
 			String comment, String currentBranch, String branchName, boolean downloadOption) throws MojoExecutionException {
 		String phrescoTemp = Utility.getPhrescoTemp();
 		List<String> workingDirList = new ArrayList<String>();
 		PluginUtils pluginUtils = new PluginUtils();
 		String username = "";
 		String password = "";
 		try {
 			File pomFile = project.getFile();
 			PomProcessor processor = new PomProcessor(pomFile);
 			List<String> repoUrls = new ArrayList<String>();
 			String dotPhrescoRepoUrl = processor.getProperty(Constants.POM_PROP_KEY_PHRESCO_REPO_URL);
 			String srcRepoUrl = processor.getProperty(Constants.POM_PROP_KEY_SRC_REPO_URL);
 			String testRepoUrl = processor.getProperty(Constants.POM_PROP_KEY_TEST_REPO_URL);
 			boolean hasSplit = false;
 			if (StringUtils.isNotEmpty(dotPhrescoRepoUrl)) {
 				hasSplit = true;
 				repoUrls.add(dotPhrescoRepoUrl);
 			} 
 			if (StringUtils.isNotEmpty(srcRepoUrl)) {
 				repoUrls.add(srcRepoUrl);
 			} 
 			if (StringUtils.isNotEmpty(testRepoUrl)) {
 				hasSplit = true;
 				repoUrls.add(testRepoUrl);
 			}
 			for (String repoUrl : repoUrls) {
 				Map<String, String> credential = FrameworkUtil.getCredential(repoUrl);
 				if (MapUtils.isNotEmpty(credential)) {
 					username = credential.get(FrameworkConstants.REQ_USER_NAME);
 					String encryptedPassword = credential.get(FrameworkConstants.REQ_PASSWORD);
 					password = FrameworkUtil.getdecryptedPassword(encryptedPassword);
 				}
 				String uuid = UUID.randomUUID().toString();
 				String workingDir = phrescoTemp + uuid;
 				workingDirList.add(workingDir);
 				pluginUtils.checkout(repoUrl, currentBranch, phrescoTemp, uuid, false, username, password);
 				
 				// Construct command for branch
 				StringBuilder builder = new StringBuilder();
 				builder.append(Constants.MVN_COMMAND)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.RELEASE_PLUGIN).append(Constants.STR_COLON).append(Constants.SCM_BRANCH)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D).append(Constants.SCM_BRANCH_NAME)
 				.append(Constants.STR_EQUALS).append(branchName)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D).append(Constants.SCM_USERNAME)
 				.append(Constants.STR_EQUALS).append(username)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D).append(Constants.SCM_PASSWORD)
 				.append(Constants.STR_EQUALS).append(password)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D)
 				.append(Constants.SCM_UPDATE_BRANCH_VERSIONS + Constants.STR_EQUALS + true)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D)
 				.append(Constants.SCM_UPDATE_WORKING_COPY_VERSIONS + Constants.STR_EQUALS + false)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D).append(Constants.SCM_RELEASE_VERSION).append(Constants.STR_EQUALS)
 				.append(version)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.SCM_HYPHEN_D).append(Constants.SCM_COMMENT_PREFIX).append(Constants.STR_EQUALS)
 				.append("\"" + comment + "\"");
				File pom = new File(workingDir + File.separatorChar + FrameworkConstants.POM_XML);
				if (!pom.exists()) {
					pom = new File(workingDir + File.separatorChar + FrameworkConstants.PHR_POM_XML);
 				}
				builder.append(Constants.STR_BLANK_SPACE)
				.append(Constants.HYPHEN_F).append(Constants.STR_BLANK_SPACE).append(pom.getName());
 				Utility.executeStreamconsumer(builder.toString(), workingDir, "", "");
 				// copy into workspace
 				if (downloadOption) {
 					String destDir =  appDirName + FrameworkConstants.HYPHEN + branchName;
 					if (hasSplit) {
 						if (StringUtils.isNotEmpty(srcRepoUrl) && srcRepoUrl.equals(repoUrl)) {
 							destDir = destDir + File.separator + destDir;
 						}
 						if (StringUtils.isNotEmpty(dotPhrescoRepoUrl) && dotPhrescoRepoUrl.equals(repoUrl)) {
 							destDir = destDir + File.separator + destDir + Constants.SUFFIX_PHRESCO;
 						}
 						if (StringUtils.isNotEmpty(testRepoUrl) && testRepoUrl.equals(repoUrl)) {
 							destDir = destDir + File.separator + destDir + Constants.SUFFIX_TEST;
 						}
 					}
 					pluginUtils.checkout(repoUrl, branchName, Utility.getProjectHome(), destDir, false, username, password);
 				}
 			}
 			if (downloadOption) {
 				String branchAppDir = appDirName + FrameworkConstants.HYPHEN + branchName;
 				String branchAppDirPath = Utility.getProjectHome() + branchAppDir;
 				ProjectInfo projectInfo = Utility.getProjectInfo(branchAppDirPath, "");
 				ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 				applicationInfo.setAppDirName(branchAppDir);
 				applicationInfo.setName(branchAppDir);
 				applicationInfo.setVersion(version);
 				File branchDotPhrDir = new File(Utility.getDotPhrescoFolderPath(branchAppDirPath, ""), Constants.PROJECT_INFO_FILE);
 				ProjectUtils.updateProjectInfo(projectInfo, branchDotPhrDir);
 				
 				File pomFileLocation = Utility.getPomFileLocation(branchAppDirPath, "");
 				PomProcessor pomProcessor = new PomProcessor(pomFileLocation);
 				if (StringUtils.isNotEmpty(pomProcessor.getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR))) {
 					pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR, branchAppDir + Constants.SUFFIX_PHRESCO);
 				}
 				if (StringUtils.isNotEmpty(pomProcessor.getProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR))) {
 					pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_TEST_DIR, branchAppDir + Constants.SUFFIX_TEST);
 				}
 				if (StringUtils.isNotEmpty(pomProcessor.getProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR))) {
 					pomProcessor.setProperty(Constants.POM_PROP_KEY_SPLIT_SRC_DIR, branchAppDir);
 				}
 				pomProcessor.save();
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		finally {
 			try {
 				if (CollectionUtils.isNotEmpty(workingDirList)) {
 					for (String workingDir : workingDirList) {
 						FileUtils.deleteDirectory(new File(workingDir));
 					}
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(e.getMessage(), e);
 			}
 		}
 	}
 }
