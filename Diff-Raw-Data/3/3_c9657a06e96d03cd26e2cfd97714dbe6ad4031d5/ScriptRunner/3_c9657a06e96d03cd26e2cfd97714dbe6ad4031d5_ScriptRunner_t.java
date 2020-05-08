 package org.jahia.server.tools.scriptrunner;
 
 import org.apache.commons.cli.*;
 import org.apache.commons.io.FileUtils;
 import org.jahia.commons.Version;
 import org.jahia.server.tools.scriptrunner.common.InContextRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 /**
  * Main bootstrap class
  */
 public class ScriptRunner {
 
     private static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
     private static String jahiaInstallLocation;
     private static Version scriptRunnerVersion;
     private static String scriptRunnerBuildNumber;
     private static File userHomeDir;
     private static File tempDirectory;
 
     public static Options buildOptions() {
 
         Option threads = OptionBuilder.withArgName("dir")
                 .hasArg()
                 .withDescription("Jahia installation directory")
                 .withLongOpt("installationDirectory")
                 .create("d");
 
         Option scriptOptions = OptionBuilder.withArgName("scriptOptions")
                 .hasArg()
                 .withDescription("A comma separated list of key=value options to pass to the script")
                 .withLongOpt("scriptOptions")
                 .create("x");
 
         Option jahiaVersion = OptionBuilder.withArgName("version")
                 .hasArg()
                 .withDescription("Overrides the automatic Jahia version detection and specify a version using this command line option")
                 .withLongOpt("jahiaVersion")
                 .create("v");
 
         Option listAvailableScripts = OptionBuilder
                 .withDescription("Outputs the list of built-in available scripts for this Jahia version")
                 .withLongOpt("listScripts")
                 .create("l");
 
         Option help =
                 OptionBuilder.withDescription("Prints this help screen")
                         .withLongOpt("help")
                         .create("h");
 
         Options options = new Options();
         options.addOption(threads);
         options.addOption(scriptOptions);
         options.addOption(jahiaVersion);
         options.addOption(listAvailableScripts);
         options.addOption(help);
         return options;
     }
 
     public static void main(String[] args) {
         // create the parser
         CommandLineParser parser = new PosixParser();
         try {
             displayStartupBanner();
             displayWarning();
 
             userHomeDir = new File(System.getProperty("user.home"), ".jahia-scriptrunner");
 
             tempDirectory = new File(userHomeDir, "temp");
             tempDirectory.mkdirs();
 
 
             // parse the command line arguments
             Options options = buildOptions();
             CommandLine line = parser.parse(options, args);
             String[] lineArgs = line.getArgs();
             if (line.hasOption("h")) {
                 // automatically generate the help statement
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("jahia-scriptrunner [options] script_to_launch", options);
                 return;
             }
 
             if (line.hasOption("d")) {
                 jahiaInstallLocation = line.getOptionValue("d");
             } else {
                 jahiaInstallLocation = System.getProperty("user.dir");
             }
 
             File jahiaInstallLocationFile = new File(jahiaInstallLocation);
             if (!jahiaInstallLocationFile.exists() && !jahiaInstallLocationFile.isDirectory()) {
                 logger.error("Invalid jahia installation directory " + jahiaInstallLocationFile.getAbsolutePath());
                 return;
             }
 
             String command = null;
             File scriptFile = null;
             if (lineArgs.length >= 1) {
                 StringBuffer commandBuffer = new StringBuffer();
                 for (int i = 0; i < lineArgs.length; i++) {
                     commandBuffer.append(lineArgs[i]);
                 }
                 command = commandBuffer.toString();
                 if ("".equals(command)) {
                     command = null;
                 }
                 if (command != null) {
                     scriptFile = new File(command);
                 }
             }
 
             List<URL> jahiaClassLoaderURLs = new ArrayList<URL>();
 
             // first we look for the Script Runner's jars
             // here because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735639 that is still not fixed (!)
             // we have to resort to extract the JARs to a temporary directory
             String projectVersion = getScriptRunnerVersion().toString();
             ClassLoader appClassLoader = ScriptRunner.class.getClassLoader();
             URL scriptRunnerCommonEngineJar = appClassLoader.getResource("libs/jahia-scriptrunner-engines-common-" + projectVersion + ".jar");
             URL extractedScriptRunnerCommonEngineJar = extractToTemp(scriptRunnerCommonEngineJar).toURI().toURL();
             jahiaClassLoaderURLs.add(extractedScriptRunnerCommonEngineJar);
 
             // resolve the Jahia engine JAR, possibly resolving using intelligent resolving 6.6.1.1 -> 6.6.1 -> 6.6
             File libDirectory = new File(jahiaInstallLocationFile, "WEB-INF" + File.separator + "lib");
             File[] jarFiles = libDirectory.listFiles(new FilenameFilter() {
                 public boolean accept(File file, String name) {
                     if (name.toLowerCase().endsWith(".jar")) {
                         return true;
                     }
                     return false;
                 }
             });
             String jahiaVersion = "6.6";
             if (line.hasOption("v")) {
                 jahiaVersion = line.getOptionValue("v");
             } else {
                 Version jahiaImplementationVersion = null;
                 if (jarFiles != null) {
                     for (File file : jarFiles) {
                         if (file.getName().toLowerCase().startsWith("jahia-impl-")) {
                             JarFile jarFile = new JarFile(file);
                             Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
                             String implementationVersion = mainAttributes.getValue("Implementation-Version");
                             jahiaImplementationVersion = new Version(implementationVersion);
                             jahiaVersion = implementationVersion;
                             String implementationBuild = mainAttributes.getValue("Implementation-Build");
                             logger.info("Detected Jahia v" + jahiaImplementationVersion + " build number " + implementationBuild);
                         }
                     }
                 }
             }
             URL scriptRunnerJahiaEngineJar = appClassLoader.getResource("libs/jahia-scriptrunner-engines-jahia-" + jahiaVersion + "-" + projectVersion + ".jar");
             while (scriptRunnerJahiaEngineJar == null && jahiaVersion.length() > 0) {
                 int lastDotPos = jahiaVersion.lastIndexOf(".");
                 if (lastDotPos > -1) {
                     jahiaVersion = jahiaVersion.substring(0, lastDotPos);
                     scriptRunnerJahiaEngineJar = appClassLoader.getResource("libs/jahia-scriptrunner-engines-jahia-" + jahiaVersion + "-" + projectVersion + ".jar");
                 } else {
                     jahiaVersion = "";
                 }
             }
             if (jahiaVersion.length() > 0) {
                 logger.info("Using script engine v" + jahiaVersion);
            } else {
                logger.error("Couldn't find any engine for the specified Jahia version, aborting !");
                return;
             }
             URL extractedScriptRunnerJahiaEngineJar = extractToTemp(scriptRunnerJahiaEngineJar).toURI().toURL();
             jahiaClassLoaderURLs.add(extractedScriptRunnerJahiaEngineJar);
 
             File classesDirectory = new File(jahiaInstallLocationFile, "WEB-INF" + File.separator + "classes");
             jahiaClassLoaderURLs.add(classesDirectory.toURI().toURL());
 
             if (jarFiles != null) {
                 for (File jarFile : jarFiles) {
                     jahiaClassLoaderURLs.add(jarFile.toURI().toURL());
                 }
             }
 
             Properties scriptOptions = new Properties();
             if (line.hasOption("x")) {
                 String scriptOptionList = line.getOptionValue("x");
                 String[] scriptOptionArray = scriptOptionList.split(",");
                 for (String scriptOption : scriptOptionArray) {
                     int equalsPos = scriptOption.indexOf("=");
                     if (equalsPos > -1) {
                         String key = scriptOption.substring(0, equalsPos);
                         String value = scriptOption.substring(equalsPos + 1);
                         scriptOptions.put(key, value);
                     } else {
                         logger.error("Found invalid key-pair value: " + scriptOption + ", will ignore it!");
                     }
                 }
             }
 
 
             URLClassLoader urlClassLoader = new URLClassLoader(jahiaClassLoaderURLs.toArray(new URL[jahiaClassLoaderURLs.size()]), ScriptRunner.class.getClassLoader());
             if (line.hasOption("l")) {
                 InputStream scriptClassLoaderStream = urlClassLoader.getResourceAsStream("scripts/availableScripts.properties");
                 if (scriptClassLoaderStream == null) {
                     logger.error("Couldn't find a built-in script list !");
                 }
                 Properties availableScripts = new Properties();
                 availableScripts.load(scriptClassLoaderStream);
                 logger.info("Available built-in scripts:");
                 for (String availableScriptName : availableScripts.stringPropertyNames()) {
                     logger.info("    " + availableScriptName + " : " + availableScripts.getProperty(availableScriptName));
                 }
                 return;
             }
             String scriptName = null;
             InputStream scriptStream = null;
             if (scriptFile != null && !scriptFile.exists()) {
                 logger.info("Script file not found on FileSystem, searching for built-in scripts...");
                 InputStream scriptClassLoaderStream = urlClassLoader.getResourceAsStream("scripts/" + command);
                 if (scriptClassLoaderStream == null) {
                     logger.error("Couldn't find a built-in script named" + command + ", aborting !");
                     return;
                 }
                 scriptName = command;
                 scriptStream = scriptClassLoaderStream;
             } else {
                 if (scriptFile != null) {
                     scriptName = scriptFile.getName();
                     scriptStream = new FileInputStream(scriptFile);
                 }
             }
             if (scriptStream != null) {
                 Class inContextRunnerClass = urlClassLoader.loadClass("org.jahia.server.tools.scriptrunner.engines.common.InContextRunnerImpl");
                 InContextRunner inContextRunner = (InContextRunner) inContextRunnerClass.newInstance();
                 inContextRunner.run(jahiaInstallLocationFile, scriptName, scriptStream, scriptOptions, urlClassLoader);
             } else {
                 logger.error("Couldn't resolve any script to run, aborting !");
             }
 
         } catch (ParseException exp) {
             // oops, something went wrong
             logger.error("Parsing failed.  Reason: ", exp);
         } catch (MalformedURLException e) {
             logger.error("Malformed URL ", e);
         } catch (ClassNotFoundException e) {
             logger.error("Class not found ", e);
         } catch (InstantiationException e) {
             logger.error("Error instantiating class", e);
         } catch (IllegalAccessException e) {
             logger.error("Illegal access", e);
         } catch (Exception e) {
             logger.error("Error", e);
         }
 
     }
 
     public static void displayStartupBanner() throws Exception {
         String message =
                 "==========================================================================================\n" +
                         "Jahia Script Runner v" + getScriptRunnerVersion() + " build " + getScriptRunnerBuildNumber() + " (c) 2013 All Rights Reserved.     \n" +
                         "==========================================================================================\n";
         System.out.println(message);
     }
 
     public static void displayWarning() {
         String warningMessage =
                 "IMPORTANT WARNINGS:\n" +
                         "--------------------------------------------------------------------------\n" +
                         "Please:\n" +
                         "- Backup your jahia installation before running this fix applier\n" +
                         "- Stop jahia before backing up and running the fix applier\n" +
                         "- Run a pre-production test first\n" +
                         "--------------------------------------------------------------------------\n" +
                         "Disclaimer : this tool is designed and tested with default installs. If \n" +
                         "the install has been heavily modified, you might need to apply some \n" +
                         "modifications manually (the tool will inform you which files it couldn't\n" +
                         "merge automatically.\n" +
                         "--------------------------------------------------------------------------\n";
         System.out.println(warningMessage);
     }
 
 
     public static Version getScriptRunnerVersion() throws Exception {
         if (scriptRunnerVersion != null) {
             return scriptRunnerVersion;
         }
         Package scriptRunnerPackage = ScriptRunner.class.getPackage();
         if (scriptRunnerPackage != null) {
             scriptRunnerVersion = new Version(scriptRunnerPackage.getImplementationVersion());
             return scriptRunnerVersion;
         }
         throw new Exception("Couldn't resolve ScriptRunner version !");
     }
 
     public static String getScriptRunnerBuildNumber() throws Exception {
         if (scriptRunnerBuildNumber != null) {
             return scriptRunnerBuildNumber;
         }
         Package scriptRunnerPackage = ScriptRunner.class.getPackage();
         if (scriptRunnerPackage != null) {
             Enumeration<URL> manifestEnum = ScriptRunner.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
             while (manifestEnum.hasMoreElements() && scriptRunnerBuildNumber == null) {
                 URL manifestURL = manifestEnum.nextElement();
                 InputStream manifestStream = manifestURL.openStream();
                 Manifest manifest = new Manifest(manifestStream);
                 Attributes mainAttributes = manifest.getMainAttributes();
                 String implementationVendorId = mainAttributes.getValue("Implementation-Vendor-Id");
                 if ("org.jahia.server.tools.scriptrunner".equals(implementationVendorId)) {
                     scriptRunnerBuildNumber = mainAttributes.getValue("Implementation-Timestamp");
                 }
                 manifestStream.close();
             }
             return scriptRunnerBuildNumber;
         }
         throw new Exception("Couldn't resolve ScriptRunner build number !");
     }
 
     public static File extractToTemp(URL resourceURL) throws IOException {
         String fileName = resourceURL.getFile();
         int lastSlashPos = fileName.lastIndexOf("/");
         if (lastSlashPos > -1) {
             fileName = fileName.substring(lastSlashPos + 1);
         }
         File destFile = new File(tempDirectory + File.separator + fileName);
         logger.info("Extracting resource " + resourceURL + " to " + destFile);
         FileUtils.copyInputStreamToFile(resourceURL.openStream(), destFile);
         return destFile;
     }
 
 }
