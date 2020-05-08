 package com.photon.phresco.plugins.java;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.MavenCommands.MavenCommand;
 import com.photon.phresco.plugins.util.MojoUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 
 public class JavaTest implements PluginConstants {
 	private File baseDir;
 	private File unitInfoPath;
 	private File configPath;
 	public void runTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException{
 		baseDir = mavenProjectInfo.getBaseDir();
 		Map<String, String> configs = MojoUtil.getAllValues(configuration);
 		String testAgainst = configs.get(TEST_AGAINST);
 		String environment = configs.get(ENVIRONMENT_NAME);
 		if (testAgainst.equals(JS)) {
 			PluginUtils pluginUtils = new PluginUtils();
 			ApplicationInfo appInfo = pluginUtils.getAppInfo(mavenProjectInfo.getBaseDir());
 			String techId = appInfo.getTechInfo().getId();
 			copyUnitInfoFile(environment, techId);
 		}
 		buildCommand(configuration, testAgainst);
 	}
 
 	private void copyUnitInfoFile(String environment, String techId) throws PhrescoException {
 		try {
 			if (!techId.equals(TechnologyTypes.JAVA_STANDALONE) && !techId.equals(TechnologyTypes.JAVA_WEBSERVICE) ) {
 				PluginUtils utils = new PluginUtils();
 				File destUnitFile = new File(baseDir + File.separator + JAVA_WEBAPP_CONFIG_FILE);
 				utils.executeUtil(environment, baseDir.getPath(), destUnitFile);
 				unitInfoPath = new File(baseDir + File.separator + DOT_PHRESCO_FOLDER  + File.separator + UNIT_INFO_FILE );
 				File destPath = new File(baseDir + File.separator + JAVA_WEBAPP_UNIT_INFO_FILE);
 				FileUtils.copyFile(unitInfoPath, destPath);
 				File codeInfoFile = new File(baseDir.getPath() + File.separator + JAVA_WEBAPP_CODE_INFO_FILE );
				File jsTestReportDir = new File(baseDir.getPath() + File.separator + DO_NOT_CHECKIN_FOLDER + File.separator + TARGET + File.separator + JSTEST);
 				if (codeInfoFile.exists()) {
 					codeInfoFile.delete();
 				}
				if (jsTestReportDir.exists()) {
					FileUtils.deleteDirectory(jsTestReportDir);
				}
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void buildCommand(Configuration configuration, String testAgainst) throws PhrescoException {
 		String mavenCommandValue = null;
 		if (testAgainst != null) {
 			List<Parameter> parameters = configuration.getParameters().getParameter();
 			for (Parameter parameter : parameters) {
 				if (parameter.getPluginParameter() != null && parameter.getPluginParameter().equals(PLUGIN_PARAMETER)) {
 					List<MavenCommand> mavenCommands = parameter.getMavenCommands().getMavenCommand();
 					for (MavenCommand mavenCommand : mavenCommands) {
 						if (parameter.getValue().equals(testAgainst) && mavenCommand.getKey().equals(testAgainst)) {
 							mavenCommandValue = mavenCommand.getValue();
 						}
 					}
 				}
 			}
 		}
 		executeTest(mavenCommandValue);
 	}
 
 	private void executeTest(String testAgainst) throws PhrescoException {
 		System.out.println("-----------------------------------------");
 		System.out.println("T E S T S");
 		System.out.println("-----------------------------------------");
 		try {
 			StringBuilder sb = new StringBuilder();
			sb.append(UNITTEST_COMMAND).append(STR_SPACE).
 			append(testAgainst);
 			boolean status = Utility.executeStreamconsumer(sb.toString(), baseDir.getPath());
 			if(!status) {
 				throw new MojoExecutionException(Constants.MOJO_ERROR_MESSAGE);
 			}
 		} catch (Exception e) {
 			throw new  PhrescoException(e);
 		}
 	}
 
 }
