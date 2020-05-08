 /**
  * Xcodebuild Command-Line Wrapper
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
 package com.photon.phresco.plugins.xcode;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.XCodeConstants;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.plugin.commons.PluginConstants;
 import com.photon.phresco.plugin.commons.PluginUtils;
 import com.photon.phresco.plugins.xcode.utils.SdkVerifier;
 import com.photon.phresco.plugins.xcode.utils.XcodeUtil;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.IosSdkUtil;
 import com.photon.phresco.util.IosSdkUtil.MacSdkType;
 
 /**
  * Run the xcodebuild command line program
  * 
  * @goal xcodebuild
  * @phase compile
  */
 public class XcodeBuild extends AbstractMojo implements PluginConstants {
 
 	private static final String RUN_UNIT_TEST_WITH_IOS_SIM_YES = "RUN_UNIT_TEST_WITH_IOS_SIM=YES";
 
 	private static final String TEST_AFTER_BUILD_YES = "TEST_AFTER_BUILD=YES";
 
 	private static final String XCODEBUILD = "xcodebuild";
 
 	private static final String C = "-c";
 
 	private static final String BIN_SH = "/bin/sh";
 
 	private static final String OCUNIT2JUNITCMD = " 2>&1 | ";
 
 	private static final String APP_PROJECT = "appProject";
 
 	private static final String LIB_PROJECT = "libProject";
 
 	private static final String OUTPUT = "-output";
 
 	private static final String CREATE = "-create";
 
 	private static final String LIPO = "lipo";
 
 	private static final String BUILD_FAILURE = "FAILURE";
 
 	private static final String BUILD_SUCCESS = "SUCCESS";
 
 	private static final String DSYM = "dSYM";
 
 	private static final String APP = "app";
 
 	private static final String IPHONE_SIMULATOR = "iphonesimulator";
 
 	private static final String IPHONEOS = "iphoneos";
 
 	private static final String HYPHEN = "-";
 
 	private static final String CMD_BUILD = "build";
 	
 	private static final String CMD_TEST = "test";
 
 	private static final String CMD_CLEAN = "clean";
 
 	private static final String LIB = "lib";
 
 	private static final String FAT_FILE = "FatFile";
 
 	private static final String SPACE = " ";
 
 	private static final String DOT_A_EXT = ".a";
 
 	private static final String STATIC_LIB_EXT = "a";
 
 	private static final String SDK = "-sdk";
 
 	private static final String CONFIGURATION = "-configuration";
 
 	private static final String TARGET = "-target";
 
 	private static final String SCHEME = "-scheme";
 
 	private static final String PROJECT = "-project";
 
 	private static final String WORKSPACE = "-workspace";
 
 	private static final String DD_MMM_YYYY_HH_MM_SS = "dd/MMM/yyyy HH:mm:ss";
 
 	private static final String POM_XML = "/pom.xml";
 
 	private static final String PACKAGING_XCODE_STATIC_LIBRARY = "xcode-static-library";
 
 	private static final String PACKAGING_XCODE = "xcode";
 	
 	private static final String PACKAGING_XCODE_WORLSAPCE = "xcode-workspace";
 
 	private static final String PATH_TXT = "path.txt";
 
 	private static final String DO_NOT_CHECKIN_BUILD = "/do_not_checkin/build";
 
 	/**
 	 * Location of the xcodebuild executable.
 	 * 
 	 * @parameter expression="${xcodebuild}" default-value="/usr/bin/xcodebuild"
 	 */
 	private File xcodeCommandLine;
 
 	/**
 	 * Project Name
 	 * 
 	 * @parameter
 	 */
 	private String xcodeProject;
 
 	/**
 	 * Target to be built
 	 * 
 	 * @parameter expression="${targetName}"
 	 */
 	private String xcodeTarget;
 	
 	/**
 	 * @parameter expression="${encrypt}"
 	 */
 	private boolean encrypt;
 
 	/**
 	 * The maven project.
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter expression="${basedir}"
 	 */
 	private String basedir;
 	/**
 	 * @parameter expression="${unittest}"
 	 */
 	private boolean unittest;
 	/**
 	 * Build directory.
 	 * 
 	 * @parameter expression="${project.build.directory}"
 	 * @required
 	 */
 	private File buildDirectory;
 
 	/**
 	 * @parameter expression="${configuration}" default-value="Debug"
 	 */
 	private String configuration;
 
 	/**
 	 * @parameter expression="${sdk}" default-value="iphonesimulator5.0"
 	 */
 	private String sdk;
 	
 	/**
 	 * @parameter 
 	 */
 	protected String gccpreprocessor;
 
 	/**
 	 * The java sources directory.
 	 * 
 	 * @parameter default-value="${project.basedir}"
 	 * 
 	 * @readonly
 	 */
 	protected File baseDir;
 
 	/**
 	 * @parameter expression="${environmentName}" required="true"
 	 */
 	protected String environmentName;
 
 	/**
 	 * XML property list file. In this file the webserverName
 	 * 
 	 * @parameter expression="${plistfile}"
 	 *            default-value="phresco-env-config.xml"
 	 */
 	protected String plistFile;
 	
 	/**
 	 * @parameter expression="${buildNumber}" required="true"
 	 */
 	protected String buildNumber;
 	
 	/**
 	 * @parameter expression="${buildName}" default-value=""
 	 */
 	protected String buildName;
 	
 	/**
 	 * @parameter expression="${applicationTest}"
 	 */
 	private boolean applicationTest;
 	
 	/**
 	 * @parameter expression="${projectType}" default-value="xcode"
 	 */
 	private String projectType;
 	
 	protected int buildNo;
 	private File buildDirFile;
 	private File buildInfoFile;
 	private List<BuildInfo> buildInfoList;
 	private int nextBuildNo;
 	private Date currentDate;
 	private String appFileName;
 	private String dSYMFileName;
 	private String deliverable;
 	private Map<String, Object> sdkOptions;
 	
 	/**
 	 * Execute the xcode command line utility.
 	 */
 	public void execute() throws MojoExecutionException {
 		if (!xcodeCommandLine.exists()) {
 			throw new MojoExecutionException("Invalid path, invalid xcodebuild file: "
 					+ xcodeCommandLine.getAbsolutePath());
 		}
 		getLog().info("basedir xcode " + basedir);
 		getLog().info("baseDir Name Xcode" + baseDir.getName());
 
 		try {
 			if(!SdkVerifier.isAvailable(sdk) && !projectType.equals(MAC)) {
 					throw new MojoExecutionException("Selected version " +sdk +" is not available!");
 			}
 		} catch (IOException e2) {
 			throw new MojoExecutionException("SDK verification failed!");
 		} catch (InterruptedException e2) {
 			throw new MojoExecutionException("SDK verification interrupted!");
         }
 			
 		try {
 			init();
 			configure();
 			
 			executeAppCreateCommads();
 			// below statement is used to get the project type. if it produces .a file, it static lib or app project
 			String projectTypeIdentified = getProjectType();
 			if (StringUtils.isEmpty(projectTypeIdentified)) {
 				throw new MojoExecutionException("Project type can not be predicted...whether it is library project or app project ");
 			}
 
 			// In case of unit testcases run, the APP files will not be generated.
 			if (LIB_PROJECT.equals(projectTypeIdentified)  && !unittest) {
 				getLog().info("Static lib file generation started ");
 				// generate .a file for device and sim and generate fat file and provide deliverables
 				createFatFileDeliverables();
 			} else if (APP_PROJECT.equals(projectTypeIdentified) && !unittest) {
 				getLog().info("Creating app file deliverables ");
 				getLog().info("xcode archiving started ");
 				createdSYM();
 				createApp();
 				// packaging is not found exception should not be thrown for unit test. Unit test just executes commands , it should not expect file like packaging cmd
 			} else if (!unittest) {
 				throw new MojoExecutionException("Packaging is not defined!");
 			}
 		} catch (IOException e) {
 			getLog().error("An IOException occured.");
 			throw new MojoExecutionException("An IOException occured", e);
 		} catch (InterruptedException e) {
 			getLog().error("The clean process was been interrupted.");
 			throw new MojoExecutionException("The clean process was been interrupted", e);
 		} catch (MojoFailureException e) {
 			throw new MojoExecutionException("An MojoFailure Exception occured", e);
 		}
 		File directory = new File(this.basedir + POM_XML);
 		this.project.getArtifact().setFile(directory);
 	}
 
 	private void createFatFileDeliverables() throws IOException,
 			InterruptedException, MojoExecutionException {
 		// getting machine sim and device sdks
 		List<String> iphoneSimSdks = IosSdkUtil.getMacSdks(MacSdkType.iphonesimulator);
 		List<String> iphoneOSSdks = IosSdkUtil.getMacSdks(MacSdkType.iphoneos);
 		boolean isSimSdk = iphoneSimSdks.contains(sdk);
 		boolean isOSSdk = iphoneOSSdks.contains(sdk);
 		
 		// generate .a file for simulator
 		File simDotAFile = null;
 		File simDotAFileBaseFolder = null;
 		
 		// generate .a file for device
 		File deviceDotAFile = null;
 		File deviceDotAFileBaseFolder = null;
 		
 		if (isSimSdk) {
 			getLog().info("Iphone sim static lib created .");
 			simDotAFile = getDotAFile();
 			simDotAFileBaseFolder = getDotAFileBaseFolder();
 			getLog().info("Simulator dot a file ..... " + simDotAFile);
 			
 			if (CollectionUtils.isNotEmpty(iphoneOSSdks)) {
 				getLog().info("Iphone device lib generation started ");
 				sdk = iphoneOSSdks.get(0);
 				executeAppCreateCommads();  // executing command to generate .a file
 				deviceDotAFile = getDotAFile();
 				deviceDotAFileBaseFolder = getDotAFileBaseFolder();
 				getLog().info("device dot a file ..... " + deviceDotAFile);
 			}
 		} else if (isOSSdk) {
 			getLog().info("Iphone device static lib created .");
 			deviceDotAFile = getDotAFile();
 			deviceDotAFileBaseFolder = getDotAFileBaseFolder();
 			getLog().info("device dot a file .  " + deviceDotAFile);
 			
 			if (CollectionUtils.isNotEmpty(iphoneSimSdks)) {
 				getLog().info("Iphone static lib creation started .");
 				sdk = iphoneSimSdks.get(0);
 				executeAppCreateCommads(); // executing command to generate .a file
 				simDotAFile = getDotAFile();
 				simDotAFileBaseFolder = getDotAFileBaseFolder();
 				getLog().info("Simulator dot a file . " + simDotAFile);
 			}
 		}
 		
 		// if both the files are generated, genearte fat file and pzck it
 		if (simDotAFile.exists() && deviceDotAFile.exists()) {
 			getLog().info("sim and device library file created.. Copying to Build directory....." + project.getBuild().getFinalName());
 			String archiveBuildName = "";
 			if (StringUtils.isNotEmpty(buildName)) {
 				archiveBuildName = buildName;
 			} else {
 				archiveBuildName = project.getBuild().getFinalName() + '_' + getTimeStampForBuildName(currentDate);
 			}
 			File baseFolder = new File(baseDir + DO_NOT_CHECKIN_BUILD, archiveBuildName);
 			
 			if (!baseFolder.exists()) {
 				baseFolder.mkdirs();
 				getLog().info("build output direcory created at " + baseFolder.getAbsolutePath());
 			}
 			
 			// copy include folder and .a files to build directory
 			getLog().info("simDotAFileBaseFolder of static library " + simDotAFileBaseFolder.getAbsolutePath());
 			getLog().info("baseFolder of static library " + baseFolder.getAbsolutePath());
 			XcodeUtil.copyFolder(simDotAFileBaseFolder, baseFolder); // sim copied to base folder
 			
 			// include dir is same for both simulator and device. so can copy either whole folder and we need to delete .a file. bcz we will create a fat file and we will plca here
 			File aFile = getFile(STATIC_LIB_EXT, baseFolder);
 			String fatFileName = "";
 			if (aFile.exists()) {
 				fatFileName = aFile.getName();
 				FileUtils.deleteQuietly(aFile);
 			}
 			
 			// pack and place generated .a file and delete old .a file
 			generateFatFile(simDotAFile, deviceDotAFile, baseFolder, fatFileName);
 			
 			// generating zip file
 			createDeliverables(archiveBuildName, baseFolder);
 			
 			// writing build info
 			File outputlibFile = getDotAFile();
 			File destFile = new File(baseFolder, outputlibFile.getName());
 			appFileName = destFile.getAbsolutePath();
 			getLog().info("static library app name ... " + destFile.getName());
 			boolean isDeviceBuild = Boolean.FALSE;
 			writeBuildInfo(true, appFileName, isDeviceBuild);
 			
 		// if both the files are not available, pack with 
 		} else if (simDotAFile.exists()) {
 			getLog().info("Simulator library file created.. Copying to Build directory. ");
 			createStaticLibrary(simDotAFileBaseFolder);
 		} else if (deviceDotAFile.exists()) {
 			getLog().info("Device library file created.. Copying to Build directory. ");
 			createStaticLibrary(deviceDotAFileBaseFolder);
 		} else {
 			throw new MojoExecutionException("Static library is not generated! ");
 		}
 	}
 
 	private void generateFatFile (File simFile, File deviceFile, File outputLocation, String fatFileName) throws MojoExecutionException {
 		getLog().info("Fat file generation started.");
 		try {
 			if (!simFile.exists()) {
 				throw new MojoExecutionException("simulator static library not found ");
 			}
 			
 			if (!deviceFile.exists()) {
 				throw new MojoExecutionException("device static library not found ");
 			}
 			
 			if (!outputLocation.exists()) {
 				outputLocation.mkdirs();
 				getLog().info("Fat file output folder create " + outputLocation.getAbsolutePath());
 			}
 			
 			// output file name
 			String fatFileLibName = "";
 			if (StringUtils.isEmpty(fatFileName)) {
 				fatFileLibName = LIB + project.getBuild().getFinalName();
 			} else {
 				fatFileLibName = FilenameUtils.removeExtension(fatFileName);
 			}
 			fatFileLibName = fatFileLibName + FAT_FILE + DOT_A_EXT;
 			
 			// excecuting lipo command to merge sim and device static library .a files
 			// lipo -create libPhresco.a libPhresco.a -output libPhrescoFatFile.a 
 			List<String> cmdargs = new ArrayList<String>();
 			cmdargs.add(LIPO);
 			cmdargs.add(CREATE);
 			cmdargs.add(simFile.getAbsolutePath());
 			cmdargs.add(deviceFile.getAbsolutePath());
 			cmdargs.add(OUTPUT);
 			cmdargs.add(outputLocation.getAbsolutePath() + File.separator + fatFileLibName);
 			getLog().info("static library falt file generation command . " + cmdargs);
 			ProcessBuilder pb = new ProcessBuilder(cmdargs);
 			pb.redirectErrorStream(true);
 			Process proc = pb.start();
 			proc.waitFor();
 			
 			// Consume subprocess output and write to stdout for debugging
 			InputStream is = new BufferedInputStream(proc.getInputStream());
 			int singleByte = 0;
 			while ((singleByte = is.read()) != -1) {
 				System.out.write(singleByte);
 			}
 			getLog().info("Fat file generation completed .");
 		} catch (Exception e) {
 			throw new MojoExecutionException("Failed to generate falt library file. ", e);
 		}
 	}
 	private String executeCreateCmdForXcode() throws IOException  
 	{
 		String version = "";
 		ProcessBuilder pb = new ProcessBuilder(BIN_SH, C,"xcodebuild -version");
 		pb.redirectErrorStream(true);
 		pb.directory(new File(basedir));
 		Process process = pb.start();
 		InputStream is = process.getInputStream();
 	    InputStreamReader isr = new InputStreamReader(is);
 	    BufferedReader br = new BufferedReader(isr);
 	    String line;
 	    while ((line = br.readLine()) != null) {
 	      String[] splited = line.split("\\s");
 	    	  version = splited[1];
 	      break;
 	    }
 		return version;
 	}
 
     private boolean isXcodeLatestVersion(String str1, String str2) {
 		String[] vals1 = str1.split("\\.");
 		String[] vals2 = str2.split("\\.");
 		int i=0;
 		while(i<vals1.length&&i<vals2.length&&vals1[i].equals(vals2[i])) {
 		  i++;
 		}
 
 		if (i<vals1.length&&i<vals2.length) {
 		    int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
 		    return diff<0?false:diff==0?true:true;
 		}
 
 		return vals1.length<vals2.length?false:vals1.length==vals2.length?true:true;
 	}
 	
 	private void executeAppCreateCommads() throws IOException, InterruptedException, MojoExecutionException {
 		Boolean isLogicalTest = unittest && !applicationTest ? true : false;
		Boolean isApplicationTest = applicationTest ? true : false;
			
 		String executeCreateCmdForXcode = executeCreateCmdForXcode();
 		boolean isXcodeLatestVersion = isXcodeLatestVersion(executeCreateCmdForXcode, "5.0");
 		String sdkVersion = getSDKVersion(sdk);
 
 		ProcessBuilder pb = new ProcessBuilder(BIN_SH, C);
 		// Include errors in output
 		pb.redirectErrorStream(true);
 
 		List<String> commands = new ArrayList<String>();
 		commands.add(XCODEBUILD);
 		
 		if (xcodeProject != null) {
 			// based on project type , it should be changed to -workspace
 			if (PACKAGING_XCODE_WORLSAPCE.equals(projectType)) {
 				commands.add(WORKSPACE);
 			} else {
 				commands.add(PROJECT);
 			}
 			commands.add(xcodeProject.replace(STR_SPACE, SHELL_SPACE));
 		}
 		
 		if (StringUtils.isNotBlank(xcodeTarget)) {
 			// latest version xcode logical test command changes
			if (isXcodeLatestVersion && (isLogicalTest || isApplicationTest)) {
 				commands.add(SCHEME);
 			} else {
 				// same for other operations
 				// based on project type , it should be changed
 				if (PACKAGING_XCODE_WORLSAPCE.equals(projectType)) {
 					commands.add(SCHEME);
 				} else {
 					commands.add(TARGET);
 				}
 			}
 			commands.add(xcodeTarget.replace(STR_SPACE, SHELL_SPACE));
 		}
 		
 		
		if (StringUtils.isNotBlank(sdkVersion.trim()) && isXcodeLatestVersion && (isLogicalTest || isApplicationTest)) {
 			commands.add("-destination OS=" + sdkVersion.trim() + ",name=" + "\"" + "iPhone Retina (4-inch)" + "\"");
 		} else if (StringUtils.isNotBlank(configuration)) {
 			commands.add(CONFIGURATION);
 			commands.add(configuration);
 		}
 		
 		// if it is unit test, we have to set TEST_AFTER_BUILD=YES in build settings
		// if (unittest) {
		//	commands.add(TEST_AFTER_BUILD_YES);
		// }
 		
 		// if it is unit test and application test, we need to pass this command
		// if (unittest && applicationTest) {
		// 	commands.add(RUN_UNIT_TEST_WITH_IOS_SIM_YES);
		// }
 		
		if (StringUtils.isNotBlank(sdk) && !projectType.equals(MAC) && ((isXcodeLatestVersion && !isLogicalTest && !isApplicationTest) || !isXcodeLatestVersion)) {
 			commands.add(SDK);
 			commands.add(sdk);
 		}
 		
 		commands.add("OBJROOT=" + buildDirectory.toString().replace(STR_SPACE, SHELL_SPACE));
 		commands.add("SYMROOT=" + buildDirectory.toString().replace(STR_SPACE, SHELL_SPACE));
 		commands.add("DSTROOT=" + buildDirectory.toString().replace(STR_SPACE, SHELL_SPACE));
 		
 		if(StringUtils.isNotBlank(gccpreprocessor)) {
 			commands.add("GCC_PREPROCESSOR_DEFINITIONS="+gccpreprocessor);
 		}
 
 		
 		getLog().info("Unit test triggered from UI " + applicationTest);
 		// if the user selects , logical test, we need to add clean test... for other test nothing will be added except target.
 		if ((isLogicalTest && !isXcodeLatestVersion)) {
 			getLog().info("Unit test for logical test triggered ");
 			commands.add(CMD_CLEAN);
 		}
 		
		if (isXcodeLatestVersion && (isLogicalTest || isApplicationTest)) {
 			commands.add(CMD_TEST);
 		} else {
 			commands.add(CMD_BUILD);
 		}
 		
 		// All the reports are generated using the ruby file
 		if (unittest) {
 			getLog().info("OCunit2Junit Home... " + System.getenv(OCUNIT2JUNIT_HOME));
 			String OCunit2Junit_home = System.getenv(OCUNIT2JUNIT_HOME);
 			if(StringUtils.isEmpty(OCunit2Junit_home)) {
 				throw new MojoExecutionException("OCunit2Junit Home is not found!");
 			}
 			commands.add(OCUNIT2JUNITCMD);
 			commands.add(OCunit2Junit_home.replace(STR_SPACE, SHELL_SPACE));
 		}
 		
 		getLog().info("List of commands " + pb.command() + " " + commands);
 		
 		StringBuilder sb = new StringBuilder();
 		for(String command : commands){
 		    sb.append(command + " ");
 		}
 		pb.command().add(sb.toString());
 		
 		// pb.command().add("install");
 		pb.directory(new File(basedir));
 		Process child = pb.start();
 
 		// Consume subprocess output and write to stdout for debugging
 		InputStream is = new BufferedInputStream(child.getInputStream());
 		int singleByte = 0;
 		while ((singleByte = is.read()) != -1) {
 			// output.write(buffer, 0, bytesRead);
 			System.out.write(singleByte);
 		}
 
 		child.waitFor();
 		int exitValue = child.exitValue();
 		getLog().info("Exit Value: " + exitValue);
 		if (exitValue != 0) {
 			throw new MojoExecutionException("Compilation error occured. Resolve the error(s) and try again!");
 		}
 	}
 
 	private String getSDKVersion(String sdk) {
 		String sdkVersion = "";
 		Pattern pattern = Pattern.compile("[0-9]+.[0-9]*|[0-9]+");
 		Matcher m = pattern.matcher(sdk);
 		while (m.find()) {
 		 sdkVersion = m.group();
 		}
 		return sdkVersion;
 	}
 
 	private void init() throws MojoExecutionException, MojoFailureException {
 
 		try {
 			//if it is application test, no need to delete target directory
 			// To Delete the buildDirectory if already exists
 			if (buildDirectory.exists() && !applicationTest) {
 				getLog().info("removing exisiting target folder ... ");
 				FileUtils.deleteQuietly(buildDirectory);
 				buildDirectory.mkdirs();
 			}
 
 			buildInfoList = new ArrayList<BuildInfo>(); // initialization
 			buildDirFile = new File(baseDir, DO_NOT_CHECKIN_BUILD);
 			if (!buildDirFile.exists()) {
 				buildDirFile.mkdirs();
 				getLog().info("Build directory created..." + buildDirFile.getPath());
 			}
 			buildInfoFile = new File(buildDirFile.getPath() + "/build.info");
 			nextBuildNo = generateNextBuildNo();
 			currentDate = Calendar.getInstance().getTime();
 		}
 		catch (IOException e) {
 			throw new MojoFailureException("APP could not initialize " + e.getLocalizedMessage());
 		 }
 	}
 
 	private int generateNextBuildNo() throws IOException {
 		int nextBuildNo = 1;
 		if (!buildInfoFile.exists()) {	
    			return nextBuildNo;
 			}
 		
 		BufferedReader read = new BufferedReader(new FileReader(buildInfoFile));
 		String content = read.readLine();
 		Gson gson = new Gson();
 		java.lang.reflect.Type listType = new TypeToken<List<BuildInfo>>() {
 		}.getType();
 		buildInfoList = (List<BuildInfo>) gson.fromJson(content, listType);
 		if (buildInfoList == null || buildInfoList.size() == 0) {
 			return nextBuildNo;
 		}
 		int buildArray[] = new int[buildInfoList.size()];
 		int count = 0;
 		for (BuildInfo buildInfo : buildInfoList) {
 			buildArray[count] = buildInfo.getBuildNo();
 			count++;
 		}
 
 		Arrays.sort(buildArray); // sort to the array to find the max build no
 
 		nextBuildNo = buildArray[buildArray.length - 1] + 1; // increment 1 to the max in the build list
 		return nextBuildNo;
 	}
 	
 	private void createStaticLibrary(File dotAFileBaseFolder) throws MojoExecutionException {
 		File outputFile = dotAFileBaseFolder;
 		if (outputFile == null) {
 			getLog().error("xcodebuild failed. resultant library is not generated!");
 			throw new MojoExecutionException("xcodebuild has been failed");
 		}
 		
 		if (outputFile.exists()) {
 			try {
 				getLog().info("Completed " + outputFile.getAbsolutePath());
 				getLog().info("Folder name ....." + baseDir.getName());
 				getLog().info("library file created.. Copying to Build directory....." + project.getBuild().getFinalName());
 				String archiveBuildName = "";
 				if (StringUtils.isNotEmpty(buildName)) {
 					archiveBuildName = buildName;
 				} else {
 					archiveBuildName = project.getBuild().getFinalName() + '_' + getTimeStampForBuildName(currentDate);
 				}
 				File baseFolder = new File(baseDir + DO_NOT_CHECKIN_BUILD, archiveBuildName);
 				if (!baseFolder.exists()) {
 					baseFolder.mkdirs();
 					getLog().info("build output direcory created at " + baseFolder.getAbsolutePath());
 				}
 				
 				// copy include folder and .a files to build directory
 				getLog().info("outputFile of static library " + outputFile.getAbsolutePath());
 				getLog().info("baseFolder of static library " + baseFolder.getAbsolutePath());
 				XcodeUtil.copyFolder(outputFile, baseFolder);
 				
 				// generating zip file
 				createDeliverables(archiveBuildName, baseFolder);
 				
 				// writing build info
 				File outputlibFile = getDotAFile();
 				File destFile = new File(baseFolder, outputlibFile.getName());
 				appFileName = destFile.getAbsolutePath();
 				getLog().info("static library app name ... " + destFile.getName());
 				
 				boolean isDeviceBuild = Boolean.FALSE;
 				writeBuildInfo(true, appFileName, isDeviceBuild);
 			} catch (IOException e) {
 				throw new MojoExecutionException("Error in writing output..." + e.getLocalizedMessage());
 			}
 		} else {
 			getLog().info("output directory not found");
 		}
 	}
 	
 	private void createApp() throws MojoExecutionException {
 		File outputFile = getAppName();
 		if (outputFile == null) {
 			getLog().error("xcodebuild failed. resultant APP not generated!");
 			throw new MojoExecutionException("xcodebuild has been failed");
 		}
 		
 		if (outputFile.exists()) {
 			try {
 				getLog().info("Completed " + outputFile.getAbsolutePath());
 				getLog().info("Folder name ....." + baseDir.getName());
 				getLog().info("APP created.. Copying to Build directory....." + project.getBuild().getFinalName());
 				String archiveBuildName = "";
 				if (StringUtils.isNotEmpty(buildName)) {
 					archiveBuildName = buildName;
 				} else {
 					archiveBuildName = project.getBuild().getFinalName() + '_' + getTimeStampForBuildName(currentDate);
 				}
 				File baseFolder = new File(baseDir + DO_NOT_CHECKIN_BUILD, archiveBuildName);
 				if (!baseFolder.exists()) {
 					baseFolder.mkdirs();
 					getLog().info("build output direcory created at " + baseFolder.getAbsolutePath());
 				}
 				File destFile = new File(baseFolder, outputFile.getName());
 				getLog().info("Destination file " + destFile.getAbsolutePath());
 				XcodeUtil.copyFolder(outputFile, destFile);
 				getLog().info("copied to..." + destFile.getName());
 				appFileName = destFile.getAbsolutePath();
 
 				createDeliverables(archiveBuildName, baseFolder);
 
 				// writing build info
 				boolean isDeviceBuild = Boolean.FALSE;
 				if (sdk.startsWith(IPHONEOS)) {
 					isDeviceBuild = Boolean.TRUE;
 				}
 				writeBuildInfo(true, appFileName, isDeviceBuild);
 			} catch (IOException e) {
 				throw new MojoExecutionException("Error in writing output..." + e.getLocalizedMessage());
 			}
 
 		} else {
 			getLog().info("output directory not found");
 		}
 	}
 
 	private void createdSYM() throws MojoExecutionException {
 		File outputFile = getdSYMName();
 		if (outputFile == null) {
 			return;
 		}
 		if (outputFile.exists()) {
 
 			try {
 				getLog().info("dSYM created.. Copying to Build directory.....");
 				String archiveBuildName = "";
 				if (StringUtils.isNotEmpty(buildName)) {
 					archiveBuildName = buildName;
 				} else {
 					archiveBuildName = project.getBuild().getFinalName() + '_' + getTimeStampForBuildName(currentDate);
 				}
 				File baseFolder = new File(baseDir + DO_NOT_CHECKIN_BUILD, archiveBuildName);
 				if (!baseFolder.exists()) {
 					baseFolder.mkdirs();
 					getLog().info("build output direcory created at " + baseFolder.getAbsolutePath());
 				}
 				File destFile = new File(baseFolder, outputFile.getName());
 				getLog().info("Destination file " + destFile.getAbsolutePath());
 				XcodeUtil.copyFolder(outputFile, destFile);
 				getLog().info("copied to..." + destFile.getName());
 				dSYMFileName = destFile.getAbsolutePath();
 
 				// create deliverables
 				createDeliverables(archiveBuildName, baseFolder);
 
 			} catch (IOException e) {
 				throw new MojoExecutionException("Error in writing output..." + e.getLocalizedMessage());
 			}
 
 		} else {
 			getLog().info("output directory not found");
 		}
 	}
 
 
 	private void createDeliverables(String archiveBuildName, File baseFolder) throws MojoExecutionException {
 		File tmpFile = null;
 		try {
 			getLog().info("Creating deliverables.....");
 			File packageInfoFile = new File(baseDir.getPath() + File.separator + DOT_PHRESCO_FOLDER + File.separator + PHRESCO_PACKAGE_FILE);
 			tmpFile = new File(baseDir + DO_NOT_CHECKIN_BUILD, "temp");
 			FileUtils.copyDirectory(baseFolder, tmpFile);
 			if(packageInfoFile.exists()) {
 				PluginUtils.createBuildResources(packageInfoFile, baseDir, tmpFile);
 			}
 			ZipArchiver zipArchiver = new ZipArchiver();
 			zipArchiver.addDirectory(tmpFile);
 			File deliverableZip = new File(baseDir + DO_NOT_CHECKIN_BUILD, archiveBuildName + ".zip");
 			zipArchiver.setDestFile(deliverableZip);
 			zipArchiver.createArchive();
 			deliverable = deliverableZip.getAbsolutePath();
 			getLog().info("Deliverables available at " + deliverableZip.getName());
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error in writing output..." + e.getLocalizedMessage());
 		} finally {
 			if(tmpFile.exists()) {
 				FileUtil.delete(tmpFile);
 			}
 		}
 	}
 	
 	private File getDotAFileBaseFolder() {
 		String path = getTargetResultPath();
 		File baseFolder = new File(buildDirectory, path);
 		File[] files = baseFolder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(STATIC_LIB_EXT)) {
 				return baseFolder;
 			}
 		}
 		return null;
 	}
 	
 	private File getDotAFile() {
 		String path = getTargetResultPath();
 		File baseFolder = new File(buildDirectory, path);
 		File[] files = baseFolder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(STATIC_LIB_EXT)) {
 				return file;
 			}
 		}
 		return null;
 	}
 	
 	private File getFile(String extension, File Folder) {
 		File[] files = Folder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(STATIC_LIB_EXT)) {
 				return file;
 			}
 		}
 		return null;
 	}
 	
 	private File getAppName() {
 		String path = getTargetResultPath();
 		File baseFolder = new File(buildDirectory, path);
 		File[] files = baseFolder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(APP)) {
 				return file;
 			}
 		}
 		return null;
 	}
 
 	private File getdSYMName() {
 		String path = getTargetResultPath();
 		File baseFolder = new File(buildDirectory, path);
 		File[] files = baseFolder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(DSYM)) {
 				return file;
 			}
 		}
 		return null;
 	}
 
 	private String getProjectType() throws MojoExecutionException{
 		boolean libProjet = false;
 		boolean xcodeProjet = false;
 		String targetResultPath = getTargetResultPath();
 		File baseFolder = new File(buildDirectory, targetResultPath);
 		getLog().info("getProject Type target path " + baseFolder);
 		File[] files = baseFolder.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			if (file.getName().endsWith(STATIC_LIB_EXT)) {
 				libProjet = true;
 			}
 			if (file.getName().endsWith(APP)) {
 				xcodeProjet = true;
 			}
 			if (file.getName().endsWith(DSYM)) {
 				xcodeProjet = true;
 			}
 		}
 		
 		if (libProjet && xcodeProjet) {
 			getLog().info("App project is having library dependency . ");
 			return APP_PROJECT;
 		}
 		
 		if (libProjet) {
 			getLog().info("Library project found . ");
 			return LIB_PROJECT;
 		}
 		
 		if (xcodeProjet) {
 			return APP_PROJECT;
 		}
 		return null;
 	}
 	
 	private String getTargetResultPath() {
 		String path = configuration + HYPEN;
 		if(projectType.equals(MAC)) {
 			return configuration;
 		}
 		if (sdk.startsWith(IPHONEOS)) {
 			path = path + IPHONEOS;
 		} else {
 			path = path + IPHONE_SIMULATOR;
 		} 
 		return path;
 	}
 	
 	private void writeBuildInfo(boolean isBuildSuccess, String appFileName1, boolean isDeviceBuild1) throws MojoExecutionException {
 		try {
 			PluginUtils pu = new PluginUtils();
 			BuildInfo buildInfo = new BuildInfo();
 			List<String> envList = pu.csvToList(environmentName);
 			
 			//Set build no in build.info
 			if (StringUtils.isNotEmpty(buildNumber) && Integer.parseInt(buildNumber) > 0) {
 				buildInfo.setBuildNo(Integer.parseInt(buildNumber));
 			} else {
 				buildInfo.setBuildNo(nextBuildNo);
 			}
 			
 			// Set build name in build.info (set zip name as build name)
 //			String appName = FilenameUtils.getName(appFileName1);
 			String appName = FilenameUtils.getName(deliverable);
 			buildInfo.setBuildName(appName);
 			
 			// Set deploy location specifies deployable files
 			buildInfo.setDeployLocation(appFileName1);
 			
 			buildInfo.setTimeStamp(getTimeStampForDisplay(currentDate));
 			if (isBuildSuccess) {
 				buildInfo.setBuildStatus(BUILD_SUCCESS);
 			} else {
 				buildInfo.setBuildStatus(BUILD_FAILURE);
 			}
 			
 			buildInfo.setDeliverables(deliverable);
 			buildInfo.setEnvironments(envList);
 			
 			if(StringUtils.isNotEmpty(projectType) && !projectType.equals(MAC)) {
 				Map<String, Boolean> sdkOptions = new HashMap<String, Boolean>(2);
 				sdkOptions.put(XCodeConstants.CAN_CREATE_IPA, isDeviceBuild1);
 				sdkOptions.put(XCodeConstants.DEVICE_DEPLOY, isDeviceBuild1);
 		        buildInfo.setOptions(sdkOptions);
 			}
 	        
 			buildInfoList.add(buildInfo);
 			Gson gson2 = new Gson();
 			FileWriter writer = new FileWriter(buildInfoFile);
 			//gson.toJson(buildInfoList, writer);
 			String json = gson2.toJson(buildInfoList);
 			System.out.println("json = " + json);
 			writer.write(json);
 			writer.close();
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 	}
 
 	private String getTimeStampForDisplay(Date currentDate) {
 		SimpleDateFormat formatter = new SimpleDateFormat(DD_MMM_YYYY_HH_MM_SS);
 		String timeStamp = formatter.format(currentDate.getTime());
 		return timeStamp;
 	}
 
 	private String getTimeStampForBuildName(Date currentDate) {
 		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy-HH-mm-ss");
 		String timeStamp = formatter.format(currentDate.getTime());
 		return timeStamp;
 	}
 
 	private void configure() throws MojoExecutionException {
 		if (StringUtils.isEmpty(environmentName)) {
 			return;
 		}
 		FileWriter writer = null;
 		try {
 			getLog().info("Configuring the project....");
 			getLog().info("environment name :" + environmentName);
 			getLog().info("base dir name :" + baseDir.getName());
 			
 			File srcConfigFile = null;
 			String currentProjectPath = "";
 			// pom.xml file have "/source" as as source directory , in that case we are getting only "/source" as string .
 			// if pom.xml file has source directory as "source", we will get whole path of the file
 			if (project.getBuild().getSourceDirectory().startsWith("/source")) {
 				srcConfigFile = new File(baseDir, project.getBuild().getSourceDirectory() + File.separator + plistFile);
 				currentProjectPath = baseDir.getAbsolutePath() + project.getBuild().getSourceDirectory();
 			} else {
 				srcConfigFile = new File(project.getBuild().getSourceDirectory() + File.separator + plistFile);
 				currentProjectPath = project.getBuild().getSourceDirectory();
 			}
 			getLog().info("baseDir ... " + baseDir.getAbsolutePath());
 			getLog().info("SourceDirectory ... " + project.getBuild().getSourceDirectory());
 			getLog().info("Config file : " + srcConfigFile.getAbsolutePath() );
 			PluginUtils pu = new PluginUtils();
 			
 			File[] files = baseDir.listFiles();
 			File dotPhrescoDir = baseDir;
 			Boolean fileFlag = false;
 			
 			for (File f: files) {
 				if (f.isDirectory() && f.getName().equals(Constants.DOT_PHRESCO_FOLDER)){
 					fileFlag = true;
 					break;
 				}
 			}			
 			
 			if (fileFlag == false) {
 				String dotPhrescoDirName = project.getProperties().getProperty(Constants.POM_PROP_KEY_SPLIT_PHRESCO_DIR);
 	        	if (StringUtils.isNotEmpty(dotPhrescoDirName)) {
 	        		dotPhrescoDir = new File(baseDir.getParent() + File.separatorChar + dotPhrescoDirName);
 	        	}
 			}
 			pu.executeUtil(environmentName, dotPhrescoDir.getPath(), srcConfigFile);
 			pu.setDefaultEnvironment(environmentName, srcConfigFile);
 			
 			// write project source path inside source folder
 			getLog().info("Project source path identification file... " + currentProjectPath);
 			File projectSourceDir = new File(currentProjectPath, PATH_TXT);
 			writer = new FileWriter(projectSourceDir, false);
 			writer.write(currentProjectPath + File.separator);
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (Exception e) {
 					getLog().error(e);
 				}
 			}
 		}
 	}
 }
