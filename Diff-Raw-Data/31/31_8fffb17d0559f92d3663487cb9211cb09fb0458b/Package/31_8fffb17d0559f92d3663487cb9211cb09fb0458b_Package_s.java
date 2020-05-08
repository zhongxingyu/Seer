 package com.photon.phresco.plugins.java;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Assembly.FileSets.FileSet;
 import com.photon.phresco.plugins.model.Assembly.FileSets.FileSet.Excludes;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.plugins.util.PluginPackageUtil;
 import com.photon.phresco.plugins.util.WarConfigProcessor;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class Package implements PluginConstants {
 	
 	private MavenProject project;
 	private File baseDir;
 	private String environmentName;
 	private String moduleName;
 	private String buildName;
 	private String buildNumber;
 	private int buildNo;
 	private String mainClassName;
 	private String jarName;
 	private File targetDir;
 	private File buildDir;
 	private File buildInfoFile;
 	private File tempDir;
 	private int nextBuildNo;
 	private String zipName;
 	private Date currentDate;
 	private String context;
 	private Log log;
 	private PluginPackageUtil util;
 	private PluginUtils pu;
 	private String sourceDir;
 	private StringBuilder builder;
 	
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo, Log log) throws PhrescoException {
 		this.log = log;
 		baseDir = mavenProjectInfo.getBaseDir();
         project = mavenProjectInfo.getProject();
         Map<String, String> configs = MojoUtil.getAllValues(configuration);
         environmentName = configs.get(ENVIRONMENT_NAME);
         buildName = configs.get(BUILD_NAME);
         buildNumber = configs.get(BUILD_NUMBER);
         jarName = configs.get(JAR_NAME);
         mainClassName = configs.get(MAIN_CLASS_NAME);
         util = new PluginPackageUtil();
         pu = new PluginUtils();
         builder = new StringBuilder();
         moduleName = configs.get(PROJECT_MODULE);
         String packMinifiedFilesValue = configs.get(PACK_MINIFIED_FILES);
         File warConfigFile = new File(baseDir.getPath() + File.separator + DOT_PHRESCO_FOLDER + File.separator + WAR_CONFIG_FILE);
 		try { 
 			init();
 			if (environmentName != null) {
 				updateFinalName();
 				configure();
 			}
 			if(StringUtils.isNotEmpty(packMinifiedFilesValue)) {
 				boolean packMinifiedFiles = Boolean.parseBoolean(packMinifiedFilesValue);
 				WarConfigProcessor configProcessor = new WarConfigProcessor(warConfigFile);
 				emptyFileSetExclude(configProcessor, EXCLUDE_FILE);
 				if(!packMinifiedFiles) {
 					List<String> excludes = new ArrayList<String>();
 					excludes.add("/js/**/*-min.js");
 					excludes.add("/js/**/*.min.js");
 					excludes.add("/css/**/*-min.css");
 					excludes.add("/css/**/*.min.css");
 					setFileSetExcludes(configProcessor, EXCLUDE_FILE, excludes);
 				}
 				configProcessor.save();
 			}
 			getMavenCommands(configuration);
 			executeMvnPackage();
 			boolean buildStatus = build();
 			writeBuildInfo(buildStatus);
 			cleanUp();
 		} catch (MojoExecutionException e) {
 			throw new PhrescoException(e);
 		} catch (JAXBException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}	
 	}
 	
 	private void setFileSetExcludes(WarConfigProcessor configProcessor, String FileSetId, List<String> exclues) throws PhrescoException {
 		try {
 			FileSet fileSet = configProcessor.getFileSet(FileSetId);
 			if(fileSet.getExcludes() == null) {
 				Excludes excludes = new Excludes();
 				fileSet.setExcludes(excludes);
 			}
 			for (String exclue : exclues) {
 				fileSet.getExcludes().getExclude().add(exclue);
 			}
 		} catch (JAXBException e) {
 			throw new PhrescoException(e);
 		} 
 	}
 	
 	private void emptyFileSetExclude(WarConfigProcessor configProcessor, String FileSetId) throws PhrescoException {
 		try {
 			FileSet fileSet = configProcessor.getFileSet(FileSetId);
 			fileSet.setExcludes(null);
 		} catch (JAXBException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void init() throws MojoExecutionException {
 		try {
 			buildDir = new File(baseDir.getPath() + PluginConstants.BUILD_DIRECTORY);
 			if(StringUtils.isNotEmpty(moduleName)) {
 				targetDir = new File(baseDir.getPath() + File.separator + moduleName + DO_NOT_CHECKIN_FOLDER + File.separator + TARGET);
 			} else {
 			targetDir = new File(project.getBuild().getDirectory());
 			}
 			baseDir = getProjectRoot(baseDir);
 			if (!buildDir.exists()) {
 				buildDir.mkdirs();
 				log.info("Build directory created..." + buildDir.getPath());
 			}
 			buildInfoFile = new File(baseDir.getPath() + PluginConstants.BUILD_DIRECTORY + BUILD_INFO_FILE);
 			File buildInfoDir = new File(baseDir.getPath() + PluginConstants.BUILD_DIRECTORY);
 			if (!buildInfoDir.exists()) {
 				buildInfoDir.mkdirs();
 				log.info("Build directory created..." + buildDir.getPath());
 			}
 			nextBuildNo = util.generateNextBuildNo(buildInfoFile);
 			currentDate = Calendar.getInstance().getTime();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private File getProjectRoot(File childDir) {
 		File[] listFiles = childDir.listFiles(new PhrescoDirFilter());
 		if (listFiles != null && listFiles.length > 0) {
 			return childDir;
 		}
 		if (childDir.getParentFile() != null) {
 			return getProjectRoot(childDir.getParentFile());
 		}
 		return null;
 	}
 
 	public class PhrescoDirFilter implements FilenameFilter {
 
 		public boolean accept(File dir, String name) {
 			return name.equals(DOT_PHRESCO_FOLDER);
 		}
 	}
 
 	private void updateFinalName() throws MojoExecutionException {
 		try {
 			File pom = project.getFile();
 			PomProcessor pomprocessor = new PomProcessor(pom);
 			if(pomprocessor.getModel().getPackaging() != null && pomprocessor.getModel().getPackaging().equals(PACKAGING_TYPE_JAR)) {
 				context = jarName;
 				updatemainClassName();
 			} else {
 				String envName = environmentName;
 				List<String> envList = pu.csvToList(environmentName);
 				
 				if (environmentName.indexOf(',') > -1) { // multi-value
 					 envName = readDefaultEnv(envList);
 					}
 				List<com.photon.phresco.configuration.Configuration> configurations = pu.getConfiguration(baseDir, envName, Constants.SETTINGS_TEMPLATE_SERVER);
 				for (com.photon.phresco.configuration.Configuration configuration : configurations) {
 					context = configuration.getProperties().getProperty(Constants.SERVER_CONTEXT);
 					break;
 				}
 			}
 			 sourceDir = pomprocessor.getProperty(POM_PROP_KEY_SOURCE_DIR);
 			if (StringUtils.isEmpty(context)) {
 				return;
 			}
 			pomprocessor.setFinalName(context);
 			pomprocessor.save();
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	public String readDefaultEnv(List<String> envList) throws MojoExecutionException {
 		boolean defaultEnv = false;
 		String defaultEnvName = "";
 		try {
 			String customerId = pu.readCustomerId(baseDir);
			ConfigManager configManager = new ConfigManagerImpl(new File(Utility.getProjectHome() + customerId + Constants.SETTINGS_XML)); 
			List<Environment> settingsEnvironments = configManager.getEnvironments(envList);
			for (Environment environment : settingsEnvironments) {
				 defaultEnv = environment.isDefaultEnv();
				 if(defaultEnv) {
 						defaultEnvName = environment.getName();
				 }
 			}
			if(!defaultEnv){
				configManager = new ConfigManagerImpl(new File(baseDir.getPath() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE));
 				List<Environment> configurationEnvironments = configManager.getEnvironments(envList);
 				for (Environment configEnvironment : configurationEnvironments) {
 					defaultEnv = configEnvironment.isDefaultEnv();
					if(defaultEnv) {
 						defaultEnvName = configEnvironment.getName();
				 }
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (ConfigurationException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return defaultEnvName;
 	}
 	
 	private void updatemainClassName() throws MojoExecutionException {
 		try {
 			if (StringUtils.isEmpty(mainClassName)) {
 				return;
 			}
 			File pom = project.getFile();
 			List<Element> configList = new ArrayList<Element>();
 			PomProcessor pomprocessor = new PomProcessor(pom);
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			Element archive = doc.createElement(JAVA_POM_ARCHIVE);
 			Element manifest = doc.createElement(JAVA_POM_MANIFEST);
 			Element addClasspath = doc.createElement(JAVA_POM_ADD_PATH);
 			addClasspath.setTextContent("true");
 			manifest.appendChild(addClasspath);
 			Element mainClass = doc.createElement(JAVA_POM_MAINCLASS);
 			mainClass.setTextContent(mainClassName);
 			manifest.appendChild(addClasspath);
 			manifest.appendChild(mainClass);
 			archive.appendChild(manifest);
 			configList.add(archive);
 
 			pomprocessor.addConfiguration(JAR_PLUGIN_GROUPID, JAR_PLUGIN_ARTIFACT_ID, configList, false);
 			pomprocessor.save();
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (ParserConfigurationException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 	
 	private void getMavenCommands(Configuration configuration) {
 		List<Parameter> parameters = configuration.getParameters().getParameter();
 		for (Parameter parameter : parameters) {
 			if(parameter.getPluginParameter() != null && parameter.getPluginParameter().equals(PLUGIN_PARAMETER)) {
 				List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
 				for (MavenCommand mavenCommand : mavenCommands) {
 					if(parameter.getValue().equals(mavenCommand.getKey())) {
 						builder.append(mavenCommand.getValue());
 						builder.append(STR_SPACE);
 					}
 				}
 			}
 		}
 	}
 	
 	private void executeMvnPackage() throws MojoExecutionException {
 		log.info("Packaging the project...");
 		StringBuilder sb = new StringBuilder();
 		sb.append(MVN_CMD);
 		sb.append(STR_SPACE);
 		sb.append(MVN_PHASE_CLEAN);
 		sb.append(STR_SPACE);
 		sb.append(MVN_PHASE_PACKAGE);
 		sb.append(STR_SPACE);
 		sb.append(builder.toString());
 		boolean status = Utility.executeStreamconsumer(sb.toString(), baseDir.getPath());
 		if(!status) {
 			throw new MojoExecutionException(Constants.MOJO_ERROR_MESSAGE);
 		}
 	}
 
 	private boolean build() throws MojoExecutionException {
 		boolean isBuildSuccess = true;
 		try {
 			log.info("Building the project...");
 			createPackage();
 		} catch (Exception e) {
 			isBuildSuccess = false;
 			log.error(e.getMessage());
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		return isBuildSuccess;
 	}
 
 	private void configure() throws MojoExecutionException {
 		log.info("Configuring the project....");
 		try {
 			adaptSourceConfig();
 			pu.writeDatabaseDriverToConfigXml(baseDir, sourceDir, environmentName);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void adaptSourceConfig() throws MojoExecutionException {
 		String modulePath = "";
 		if (moduleName != null) {
 			modulePath = File.separatorChar + moduleName;
 		}
 		PomProcessor processor  = null;
 		File sourceConfigXML = null;
 		try {
 			processor = new PomProcessor(project.getFile());
 			String configXml = processor.getProperty(POM_PROP_CONFIG_FILE);
 			if(StringUtils.isNotEmpty(configXml)) {
 				sourceConfigXML = new File(baseDir + modulePath + configXml);
 			} else {
 				sourceConfigXML = new File(baseDir + modulePath + sourceDir + FORWARD_SLASH +  CONFIG_FILE);
 			}
 			File parentFile = sourceConfigXML.getParentFile();
 			if (parentFile.exists()) {
 				pu.executeUtil(environmentName, baseDir.getPath(), sourceConfigXML);
 			}
 		} catch (PhrescoPomException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	private void createPackage() throws MojoExecutionException {
 		try {
 			zipName = util.createPackage(buildName, buildNumber, nextBuildNo, currentDate);
 			String zipFilePath = buildDir.getPath() + File.separator + zipName;
 			String zipNameWithoutExt = zipName.substring(0, zipName.lastIndexOf('.'));
 			ProjectUtils projectutils = new ProjectUtils();
 			ProjectInfo projectInfo = projectutils.getProjectInfo(baseDir);
 			TechnologyInfo applicationInfo = projectInfo.getAppInfos().get(0).getTechInfo();
 			String appTechId = applicationInfo.getId();
 			if (appTechId.equals(TechnologyTypes.JAVA_STANDALONE)) {
 				copyJarToPackage(zipNameWithoutExt);
 			} else {
 				copyWarToPackage(zipNameWithoutExt, context);
 			}
 			ArchiveUtil.createArchive(tempDir.getPath(), zipFilePath, ArchiveType.ZIP);
 		} catch (PhrescoException e) {
 			throw new MojoExecutionException(e.getErrorMessage(), e);
 		}
 	}
 
 	private void copyJarToPackage(String zipNameWithoutExt) throws MojoExecutionException {
 		try {
 			String[] list = targetDir.list(new JarFileNameFilter());
 			if (list.length > 0) {
 				File jarFile = new File(targetDir.getPath() + File.separator + list[0]);
 				tempDir = new File(buildDir.getPath() + File.separator + zipNameWithoutExt);
 				tempDir.mkdir();
 				FileUtils.copyFileToDirectory(jarFile, tempDir);
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void copyWarToPackage(String zipNameWithoutExt, String context) throws MojoExecutionException {
 		try {
 			String[] list = targetDir.list(new WarFileNameFilter());
 			if (list.length > 0) {
 				File warFile = new File(targetDir.getPath() + File.separator + list[0]);
 				tempDir = new File(buildDir.getPath() + File.separator + zipNameWithoutExt);
 				tempDir.mkdir();
 				File contextWarFile = new File(targetDir.getPath() + File.separator + context + ".war");
 				warFile.renameTo(contextWarFile);
 				FileUtils.copyFileToDirectory(contextWarFile, tempDir);
 			} else {
 				throw new MojoExecutionException(context + ".war not found in " + targetDir.getPath());
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private void writeBuildInfo(boolean isBuildSuccess) throws MojoExecutionException {
 		util.writeBuildInfo(isBuildSuccess, buildName, buildNumber, nextBuildNo, environmentName, buildNo, currentDate, buildInfoFile);
 	}
 
 	private void cleanUp() throws MojoExecutionException {
 		try {
 			FileUtils.deleteDirectory(tempDir);
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 }
 
 class WarFileNameFilter implements FilenameFilter {
 
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".war");
 	}
 }
 
 class JarFileNameFilter implements FilenameFilter {
 
 	public boolean accept(File dir, String name) {
 		return name.endsWith(".jar");
 	}
 
 }
