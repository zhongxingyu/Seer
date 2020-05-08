 package edu.depaul.madl.wizards;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.IConsoleView;
 import org.eclipse.ui.console.MessageConsole;
 
 import edu.depaul.cdm.madl.eclipse.ui.PreferenceConstants;
 
 public class AppBuilderConfiguration {
 
 	private static IProject project;
 	private static AppBuilderConfiguration config;
 	
 	private AppBuilderConfiguration(){}
 	
 	public static AppBuilderConfiguration getInstance() {
 		if (config == null) {
 			config = new AppBuilderConfiguration();
 		}
 		return config;
 	}
 
 	public IProject getProject() {
 		return project;
 	}
 	
 	public void setProject(IProject project) {
 		AppBuilderConfiguration.project = project;
 	}
 	
 	public void copyAppBuilderFiles() {
 		System.out.println("Copying AppBuilder files from: " + PreferenceConstants.APP_BUILDER_HOME);
 		
 		// Files and directories in the project
 		File projectConfDir = new File(getProject().getLocation() + File.separator + "conf");
 		File projectLibApiDir = new File(getProject().getLocation() + File.separator + "lib/api");
 		File projectTemplatesDir = new File(getProject().getLocation() + File.separator + "templates");
 //		File projectOrgPropertiesFile = new File(getProject().getLocation() + File.separator + "org.properties");
 		
 		try {
 			// Local AppBuilder files and directories
			File localConfDir = new File(PreferenceConstants.getAppBuilderHome() + "/conf");
			File localLibApiDir = new File(PreferenceConstants.getAppBuilderHome() + "/lib/api");
			File localTemplateDir = new File(PreferenceConstants.getAppBuilderHome() + "/templates");
 //			File localOrgPropertiesFile = new File(PreferenceConstants.APP_BUILDER_HOME + "/test/org.properties");
 			
 			// Copy AppBuilder from local location to the Eclipse project
 			FileUtils.copyDirectory(localConfDir, projectConfDir);
 			FileUtils.copyDirectory(localLibApiDir, projectLibApiDir);
 			FileUtils.copyDirectory(localTemplateDir, projectTemplatesDir);
 //			FileUtils.copyFile(localOrgPropertiesFile, projectOrgPropertiesFile);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 }
