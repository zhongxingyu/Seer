 package com.photon.phresco.plugins.wordpress;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.plugin.commons.DatabaseUtil;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 
 public class Deploy implements PluginConstants {
 
 	private File baseDir;
 	private String buildNumber;
 	private String environmentName;
 	private boolean importSql;
 
 	private File buildFile;
 	private File buildDir;
 	private File tempDir;
 	private File binariesDir;
 	private String context;
 	private Log log;
 	private String sqlPath;
 	
 	public void deploy(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         importSql = Boolean.parseBoolean(configs.get(EXECUTE_SQL));
 	    sqlPath = configs.get(FETCH_SQL);
         
         try {
 			init();
 			createDb();
 			packDrupal();
 			extractBuild();
 			deploy();
 			cleanUp();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	private void init() throws MojoExecutionException {
 		try {
 			if (StringUtils.isEmpty(buildNumber) || StringUtils.isEmpty(environmentName)) {
 				callUsage();
 			}
 			
 			PluginUtils pu = new PluginUtils();
 			BuildInfo buildInfo = pu.getBuildInfo(Integer.parseInt(buildNumber));
 			log.info("Build Name " + buildInfo);
 			
 			buildDir = new File(baseDir.getPath() + BUILD_DIRECTORY);
 			binariesDir = new File(baseDir.getPath() + BINARIES_DIR);
 			buildFile = new File(buildDir.getPath() + File.separator + buildInfo.getBuildName());
 			List<com.photon.phresco.configuration.Configuration> configurations = getConfiguration(Constants.SETTINGS_TEMPLATE_SERVER);
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				context = configuration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 				break;
 			}
 			tempDir = new File(buildDir.getPath() + TEMP_DIR + File.separator
 					+ context);
 			tempDir.mkdirs();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void packDrupal() throws MojoExecutionException {
 		BufferedReader bufferedReader = null;
 		boolean errorParam = false;
 		try {
 			//fetching drupal binary from repo
 			StringBuilder sb = new StringBuilder();
 			sb.append(MVN_CMD);
 			sb.append(STR_SPACE);
 			sb.append(MVN_PHASE_INITIALIZE);
 
 			bufferedReader = Utility.executeCommand(sb.toString(), baseDir.getPath());
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				if (line.startsWith("[ERROR]")) {
 					errorParam = true;
 				}
 			}
 			if (errorParam) {
 				throw new MojoExecutionException("Drupal Binary Download Failed ");
 			}
 			File drupalBinary = null;
 			File[] listFiles = binariesDir.listFiles();
 			for (File file : listFiles) {
 				if(file.isDirectory()){
 					drupalBinary = new File(binariesDir + "/wp");
 					file.renameTo(drupalBinary);
 				}
 			}
 			if (!drupalBinary.exists()) {
 				throw new MojoExecutionException("wp binary not found");
 			}
 			if(drupalBinary != null) {
 				FileUtils.copyDirectoryStructure(drupalBinary, tempDir);
 			}
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}  finally {
 			Utility.closeStream(bufferedReader);
 		}
 	}
 
 	private void callUsage() throws MojoExecutionException {
 		log.error("Invalid usage.");
 		log.info("Usage of Deploy Goal");
 		log.info(
 				"mvn drupal:deploy -DbuildName=\"Name of the build\""
 				+ " -DenvironmentName=\"Multivalued evnironment names\""
 				+ " -DimportSql=\"Does the deployment needs to import sql(TRUE/FALSE?)\"");
 		throw new MojoExecutionException(
 				"Invalid Usage. Please see the Usage of Deploy Goal");
 	}
 	
 	private void extractBuild() throws MojoExecutionException {
 		try {
 			ArchiveUtil.extractArchive(buildFile.getPath(), tempDir.getPath(),
 					ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 
 	private void createDb() throws MojoExecutionException, PhrescoException {
 		DatabaseUtil util = new DatabaseUtil();
 		try {
 			util.fetchSqlConfiguration(sqlPath, importSql, baseDir, environmentName);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void deploy() throws MojoExecutionException {
 		String deployLocation = "";
 		try {
 			List<com.photon.phresco.configuration.Configuration> configurations = getConfiguration(Constants.SETTINGS_TEMPLATE_SERVER);
 			for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 				deployLocation = configuration.getProperties().getProperty(Constants.SERVER_DEPLOY_DIR);
 				break;
 			}
 		File deployDir = new File(deployLocation);
 		if (!deployDir.exists()) {
 				throw new MojoExecutionException(" Deploy Directory" + deployLocation + " Does Not Exists ");
 			}
 			log.info("Project is deploying into " + deployLocation);
 			FileUtils.copyDirectoryStructure(tempDir.getParentFile(), deployDir);
 			log.info("Project is deployed successfully");
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private List<com.photon.phresco.configuration.Configuration> getConfiguration(String type) throws PhrescoException, ConfigurationException {
 		ConfigManager configManager = PhrescoFrameworkFactory.getConfigManager(new File(baseDir.getPath() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE));
 		return configManager.getConfigurations(environmentName, type);		
 	}
 
 	private void cleanUp() throws MojoExecutionException {
 		try {
 			FileUtils.deleteDirectory(tempDir.getParentFile());
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 }
