 package com.photon.phresco.plugins;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.api.PhrescoPlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 
 /**
  * Goal which validate the code
  * 
  * @goal validate-code
  * 
  */
 public class SonarCodeValidator extends PhrescoAbstractMojo implements PluginConstants {
 
 	private static final String VALIDATE_CODE = Constants.PHASE_VALIDATE_CODE;
 
 	/**
 	 * The Maven project.
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter expression="${project.basedir}" required="true"
 	 * @readonly
 	 */
 	protected File baseDir;
 	private File configPath;
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		getLog().info("Executing Code Validation");
 		File targetDir = null;
 		Gson gson = new Gson();
 		try {
 			String projectInfoPath = baseDir + File.separator + DOT_PHRESCO_FOLDER + File.separatorChar + Constants.PROJECT_INFO_FILE;
 			targetDir = new File(baseDir + File.separator + DO_NOT_CHECKIN_FOLDER + File.separatorChar + TARGET);
 			String infoFile = baseDir + File.separator + Constants.VALIDATE_CODE_INFO_FILE;
 			FileReader projectInfoJson = new FileReader(new File(projectInfoPath));
 			Type projectInfoType = new TypeToken<ProjectInfo>(){}.getType();
 			ProjectInfo projectInfo = gson.fromJson(projectInfoJson , projectInfoType);
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String techId = applicationInfo.getTechInfo().getId();
 			Configuration config = getConfiguration(infoFile, VALIDATE_CODE);
 			Map<String, String> parameters = MojoUtil.getAllValues(config);
 			String testAgainst = parameters.get("sonar");
 			String environmentName = parameters.get(ENVIRONMENT_NAME);
 			if (((techId.equals(TechnologyTypes.ANDROID_NATIVE) && testAgainst.equals("functional")) || (techId.equals(TechnologyTypes.ANDROID_HYBRID) && testAgainst.equals("functional"))) ||  (techId.equals(TechnologyTypes.JAVA_STANDALONE) && testAgainst.equals("functional"))) {
 				String[] list = targetDir.list(new JarFileNameFilter());
 				if (list == null || list.length == 0) {
 					throw new MojoExecutionException("Code Validation for functional test scripts requires a build. Generate a build and try again.");
 				}
 			} 
 			if (techId.equals(TechnologyTypes.HTML5_JQUERY_MOBILE_WIDGET) || techId.equals(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET) ||
 					techId.equals(TechnologyTypes.HTML5_MOBILE_WIDGET) || techId.equals(TechnologyTypes.HTML5_WIDGET) || techId.equals(TechnologyTypes.HTML5)  ) {
 				PluginUtils utils = new PluginUtils();
 				File envConfFile = new File(baseDir + File.separator + JAVA_WEBAPP_CONFIG_FILE);
 				utils.executeUtil(environmentName, baseDir.getPath(), envConfFile);
 				File codeInfoFile = new File(baseDir.getPath() + File.separator + DOT_PHRESCO_FOLDER  + File.separator + VALIDATE_INFO_INFO_FILE );
 				File destCodeInfoFile = new File(baseDir.getPath() + File.separator + JAVA_WEBAPP_CODE_INFO_FILE);
 				FileUtils.copyFile(codeInfoFile, destCodeInfoFile);
				File unitInfoPath = new File(baseDir.getPath() + File.separator + JAVA_WEBAPP_UNIT_INFO_FILE);
 				if (unitInfoPath.exists()) {
 					unitInfoPath.delete();
 				}
 			}
 			pluginValidate(infoFile);
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void pluginValidate(String infoFile) throws PhrescoException {
 		if (isGoalAvailable(infoFile, VALIDATE_CODE) && getDependency(infoFile, VALIDATE_CODE) != null) {
 			PhrescoPlugin plugin = getPlugin(getDependency(infoFile, VALIDATE_CODE));
 			plugin.validate(getConfiguration(infoFile, VALIDATE_CODE), getMavenProjectInfo(project));
 		} else {
 			PhrescoPlugin plugin = new PhrescoBasePlugin(getLog());
 			plugin.validate(getConfiguration(infoFile, VALIDATE_CODE), getMavenProjectInfo(project));
 		}
 	}
 }
 
 class JarFileNameFilter implements FilenameFilter {
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".jar");
 	}
 }
