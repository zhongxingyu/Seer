 /**
  * 
  */
 package com.ximad.apkpackager.ant;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.MagicNames;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 
 import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
 import com.ximad.apkpackager.EnumIconsApk;
 import com.ximad.apkpackager.apk.config.IConfigBuilder;
 import com.ximad.apkpackager.utils.RegExpUtils;
 
 /**
  * @author Vladimir Baraznovsky
  * 
  */
 public abstract class AntBuilder {
 
 	private IConfigBuilder config;
 
 	public AntBuilder(IConfigBuilder configBuilder) {
 		super();
 		this.config = configBuilder;
 	}
 
 	private Project createProjectAnt() {
 		Project projectAnt = new Project();
 
 		projectAnt.setUserProperty(MagicNames.ANT_FILE, config
 				.getBuildAntFile().getAbsolutePath());
 
 		initAntConsole(projectAnt);
 		return projectAnt;
 	}
 
 	private void initAntConsole(Project projectAnt) {
 		if (config.getOutputStream() == null) {
 			return;
 		}
 		OutputStream out = config.getOutputStream();
 		DefaultLogger consoleLogger = new DefaultLogger();
 		PrintStream printStream = new PrintStream(out);
 
 		consoleLogger.setErrorPrintStream(printStream);
		consoleLogger.setOutputPrintStream(printStream);
 
 		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
 
 		projectAnt.addBuildListener(consoleLogger);
 
 	}
 
 	private void initAntHelper(Project projectAnt) {
 		ProjectHelper helper = ProjectHelper.getProjectHelper();
 		projectAnt.addReference(ProjectHelper.PROJECTHELPER_REFERENCE, helper);
 		helper.parse(projectAnt, config.getBuildAntFile());
 
 	}
 
 	public void runAnt() {
 		Project projectAnt = createProjectAnt();
 		initAntProperties(projectAnt);
 		projectAnt.fireBuildStarted();
 		projectAnt.init();
 		if (!config.getTempBuildDirectory().exists()) {
 			config.getTempBuildDirectory().mkdirs();
 		}
 		projectAnt.setBaseDir(config.getTempBuildDirectory());
 		initAntHelper(projectAnt);
 		projectAnt.executeTarget(getExecuteTarget());
 		projectAnt.fireBuildFinished(null);
 	}
 
 	protected abstract String getExecuteTarget();
 
 	protected void initAntProperties(Project projectAnt) {
 		projectAnt.setProperty(AntConst.PROPERTY_SDK_DIR, config
 				.getAndroidSdkDirectory().getAbsolutePath());
 		projectAnt.setProperty(AntConst.PROPERTY_PROJECT_PATH, config
 				.getProjectDirectory().getAbsolutePath());
 		projectAnt.setProperty(AntConst.PROPERTY_BUILD_PATH, config
 				.getResultFile().getAbsolutePath());
 		projectAnt.setProperty(AntConst.PROPERTY_BUILD_PACKAGE,
 				config.getResultPackageName());
 		projectAnt.setProperty(AntConst.PROPERTY_BUILD_LABEL,
 				config.getResultLabel());
 		projectAnt.setProperty(AntConst.PROPERTY_APK_PATH, config.getApkFile()
 				.getAbsolutePath());
 		projectAnt.setProperty(AntConst.PROPERTY_USER_ID, config.getUserId());
 		
 		
 		projectAnt.setProperty(AntConst.PROPERTY_APK_LABEL,
 				config.getApkLabel());
 		for (EnumIconsApk enumIconsApk : EnumIconsApk.values()) {
 			File iconFile = config.getResultIcon(enumIconsApk);
 			if (iconFile != null) {
 				projectAnt.setProperty(enumIconsApk.getAntProperty(),
 						iconFile.getAbsolutePath());
 			}
 		}
 
 
 	}
 
 }
