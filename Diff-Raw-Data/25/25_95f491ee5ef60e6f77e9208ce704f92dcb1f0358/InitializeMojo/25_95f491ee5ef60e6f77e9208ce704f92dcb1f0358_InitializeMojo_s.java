 /**
  * This file is part of the XP-Framework
  *
  * XP-Framework Maven plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.ArrayList;
 
 import org.apache.commons.exec.OS;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.archiver.UnArchiver;
 
 import net.xp_forge.maven.plugins.xp.io.PthFile;
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
 import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
 import net.xp_forge.maven.plugins.xp.io.IniFile;
 import net.xp_forge.maven.plugins.xp.logging.LogLogger;
 import net.xp_forge.maven.plugins.xp.exec.RunnerOutput;
 import net.xp_forge.maven.plugins.xp.exec.RunnerException;
 import net.xp_forge.maven.plugins.xp.exec.runners.php.PhpRunner;
 import net.xp_forge.maven.plugins.xp.exec.input.php.PhpRunnerInput;
 
 import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;
 
 /**
  * Initialize phase that bootstraps XP-Framework
  *
  * @goal initialize
  * @requiresDependencyResolution compile
  */
 public class InitializeMojo extends AbstractXpMojo {
   public static final String[] WINDOWS_EXTENSIONS= {"com_dotnet"};
   public static final String[] UNIX_EXTENSIONS= {"pcntl", "posix"};
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   public void execute() throws MojoExecutionException {
     ArchiveUtils.enableLogging(new LogLogger(getLog()));
 
     // User clearly specified to use already installed XP-Runtime via ${xp.runtime.local}
     if (this.local) {
       this.setupRuntimeFromLocalInstall();
 
     // Special case: self-bootstrap for compiling XP-Framework itself
     // Setup XP-Runtime using project resources
     } else if (
         this.project.getGroupId().equals(XP_FRAMEWORK_GROUP_ID) &&
         (
           this.project.getArtifactId().equals(CORE_ARTIFACT_ID) ||
           this.project.getArtifactId().equals(TOOLS_ARTIFACT_ID)
         )
       ) {
       this.setupRuntimeFromThisProject(new File(this.outputDirectory, ".runtime"));
 
     // Setup XP-Runtime using project dependencies
     } else {
       this.setupRuntimeFromDependencies(new File(this.outputDirectory, ".runtime"));
     }
 
     getLog().info("XP Runners [" + this.runnersDirectory + "]");
     this.project.getProperties().setProperty("xp.runtime.runners", this.runnersDirectory.getAbsolutePath());
 
     getLog().info("USE_XP     [" + (null == this.use_xp ? "N/A" : this.use_xp) + "]");
     if (null != this.use_xp) {
       this.project.getProperties().setProperty("xp.runtime.use_xp", this.use_xp);
     }
 
     getLog().info("PHP path   [" + this.php + "]");
     getLog().info("Timezone   [" + this.timezone + "]");
     getLog().info("Extensions [" + (null == this.extensions ? "N/A" : this.extensions.toString()) + "]");
 
     // Alter default Maven settings
     this.alterSourceDirectories();
   }
 
   /**
    * Use XP-Runtime already installed on local machine
    * by searching for XP-Runners in PATH
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromLocalInstall() throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from local install");
     try {
       this.runnersDirectory= ExecuteUtils.getExecutable("xp").getParentFile();
     } catch (FileNotFoundException ex) {
       throw new MojoExecutionException("Cannot find XP-Framework. Please install it from http://xp-framework.net/", ex);
     }
 
 
   }
 
   /**
    * Prepare our own XP-Runtime into specified directory using project dependencies
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromDependencies(File targetDirectory) throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from project dependencies into [" + targetDirectory + "]");
 
     // Configure directories
     File bootstrapDirectory = new File(targetDirectory, "bootstrap");
     this.runnersDirectory   = new File(targetDirectory, "runners");
 
     // Setup bootstrap from dependencies
     this.setupBootstrapFromDependencies(bootstrapDirectory);
 
     // Setup XP-Runners
     this.use_xp= bootstrapDirectory.getAbsolutePath();
     this.setupRunners(this.runnersDirectory, this.use_xp);
   }
 
   /**
    * Prepare our own XP-Runtime into specified directory using *this* project sources
    *
    * Note: only used if this project is net.xp-framework:core or net.xp-framework:tools
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromThisProject(File targetDirectory) throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from this project sources into [" + targetDirectory + "]");
 
     // Configure directories
     File bootstrapDirectory = new File(targetDirectory, "bootstrap");
     File toolsDirectory     = new File(bootstrapDirectory, TOOLS_ARTIFACT_ID);
     this.runnersDirectory   = new File(targetDirectory, "runners");
 
     // Copy [lang.base.php]
     try {
       FileUtils.copyFile(
         new File(this.basedir.getParentFile(), "core/src/main/php/lang.base.php"),
         new File(bootstrapDirectory, "lang.base.php")
       );
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot copy bootstrap files to [" + bootstrapDirectory + "]", ex);
     }
 
     // Locate parent project
     File parentProjectDirectory= this.basedir.getParentFile();
 
     // Setup use_xp
     this.use_xp = bootstrapDirectory.getAbsolutePath();
     this.use_xp+= File.pathSeparator;
     this.use_xp+= new File(parentProjectDirectory, "tools").getAbsolutePath();
 
     // Setup XP-Runners
     this.setupRunners(this.runnersDirectory, this.use_xp);
   }
 
   /**
    * Prepare XP-Bootstrap using project dependencies into specified directory
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupBootstrapFromDependencies(File targetDirectory) throws MojoExecutionException {
     getLog().debug("- Preparing XP-Bootstrap from project dependencies into [" + targetDirectory + "]");
 
     // Init [boot.pth] entries
     PthFile pth= new PthFile();
     pth.addFileEntry(targetDirectory);
 
     // Locate required XP-artifacts: core & tools
     Artifact coreArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, CORE_ARTIFACT_ID);
     if (null == coreArtifact) {
       throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
     }
 
     Artifact toolsArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, TOOLS_ARTIFACT_ID);
     if (null == toolsArtifact) {
       throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
     }
 
     pth.addArtifactEntry(coreArtifact);
     pth.addArtifactEntry(toolsArtifact);
 
     // Locate optional XP-artifacts: compiler
     Artifact compilerArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, COMPILER_ARTIFACT_ID);
     if (null != compilerArtifact) {
       pth.addArtifactEntry(compilerArtifact);
     }
 
     // Unpack bootstrap
     UnArchiver unArchiver= ArchiveUtils.getUnArchiver(coreArtifact);
     unArchiver.extract("lang.base.php", targetDirectory);
 
     File toolsDirectory= new File(targetDirectory, TOOLS_ARTIFACT_ID);
     unArchiver= ArchiveUtils.getUnArchiver(toolsArtifact);
     unArchiver.extract("tools/class.php", toolsDirectory);
     unArchiver.extract("tools/web.php", toolsDirectory);
     unArchiver.extract("tools/xar.php", toolsDirectory);
 
     // Create [target/bootstrap/boot.pth]
     File pthFile= new File(targetDirectory, "boot.pth");
     try {
       pth.setComment(CREATED_BY_NOTICE);
       pth.dump(pthFile);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot write [" + pthFile + "]", ex);
     }
   }
 
   /**
    * Setup XP-Runners into specified directory
    *
    * @param  java.io.File targetDirectory
    * @param  java.lang.String useXp value of USE_XP
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRunners(File targetDirectory, String useXp) throws MojoExecutionException {
     getLog().debug("- Preparing XP-Runners into [" + targetDirectory + "]");
 
     // Extract XP-runners
     try {
       getLog().debug(" - Extracting runners from resources");
 
       ExecuteUtils.saveRunner("cgen", targetDirectory);
       ExecuteUtils.saveRunner("doclet", targetDirectory);
       ExecuteUtils.saveRunner("unittest", targetDirectory);
       ExecuteUtils.saveRunner("xar", targetDirectory);
       ExecuteUtils.saveRunner("xcc", targetDirectory);
       ExecuteUtils.saveRunner("xp", targetDirectory);
       ExecuteUtils.saveRunner("xpcli", targetDirectory);
       ExecuteUtils.saveRunner("xpi", targetDirectory);
       ExecuteUtils.saveRunner("xpws", targetDirectory);
 
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot extract XP-Runners to [" + targetDirectory + "]", ex);
     }
 
     // Set USE_XP
     IniFile ini= new IniFile();
     ini.setProperty("use", useXp);
 
     // Set PHP executable and timezone
     this.setupPhp();
     ini.setProperty("runtime", "default", this.php.getAbsolutePath());
 
     this.setupTimezone();
     ini.setProperty("runtime", "date.timezone", this.timezone);
 
     this.setupExtensions();
     if (null != this.extensions) {
       ini.setProperty("runtime", "extension", this.extensions);
     }
 
     // Dump ini file
     File iniFile= new File(targetDirectory, "xp.ini");
     try {
       ini.setComment(CREATED_BY_NOTICE);
       ini.dump(iniFile);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot write [" + iniFile + "]", ex);
     }
   }
 
   /**
    * Locate PHP executable. If ${xp.runtime.php} is not set, look for 'php' executable in PATH
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupPhp() throws MojoExecutionException {
     getLog().debug("Identifying PHP executable");
 
     // ${xp.runtime.php.executable} is already set
     if (null != this.php) {
       getLog().debug(" - Provided by caller");
 
     // Check for PHP executable in PATH
     } else {
       try {
         getLog().debug(" - Inspecting PATH");
         this.php= ExecuteUtils.getExecutable("php");
 
       // Extract runners from resources
       } catch (FileNotFoundException ex) {
         throw new MojoExecutionException("Cannot find PHP executable. You can use -Dxp.runtime.php=...", ex);
       }
     }
 
     // Update ${xp.runtime.php} property
     getLog().debug(" - Using PHP from [" + this.php.getAbsolutePath() + "]");
     this.project.getProperties().setProperty("xp.runtime.php", this.php.getAbsolutePath());
   }
 
   /**
    * Determine timezone. If ${xp.runtime.timezone} is not set, use system timezone
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupTimezone() throws MojoExecutionException {
     getLog().debug("Identifying timezone");
 
     // ${xp.runtime.timezone} is already set
     if (null != this.timezone) {
       getLog().debug(" - Provided by caller");
 
     // Use system default
     } else {
       getLog().debug(" - Using system timezone");
       this.timezone= Calendar.getInstance().getTimeZone().getID();
     }
 
     // Update ${xp.runtime.timezone} property
     getLog().debug(" - Using timezone [" + this.timezone + "]");
     this.project.getProperties().setProperty("xp.runtime.timezone", this.timezone);
   }
 
   /**
    * Setup PHP extensions to load
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupExtensions() throws MojoExecutionException {
     getLog().debug("Identifying PHP extensions");
 
     // No extenions needed
     if (null == this.extensions || this.extensions.isEmpty()) {
       getLog().debug(" - No required extensions");
       this.extensions= null;
       return;
     }
 
     // Get list of loaded extensions
     List<String> loadedExtensions= this.getLoadedPhpExtensions();
     getLog().debug(" - Loaded extensions:" + loadedExtensions.toString());
 
     // Get list of available extensions
     List<String> availableExtensions= this.getAvailablePhpExtensions();
     getLog().debug(" - Available extensions:" + availableExtensions.toString());
 
     // Identify extensions to load
     List<String> extensionsToLoad= new ArrayList<String>();
     for (String extension: this.extensions) {
 
       // Check already loaded; nothing to do
       if (loadedExtensions.contains(extension)) continue;
 
       // Ignore Windows-only extension on Un*x environments
       if (!OS.isFamilyWindows() && Arrays.asList(InitializeMojo.WINDOWS_EXTENSIONS).contains(extension)) continue;
 
       // Ignore Un*x-only extension on Windows environments
       if (OS.isFamilyWindows() && Arrays.asList(InitializeMojo.UNIX_EXTENSIONS).contains(extension)) continue;
 
       // Hack: gd extension on Windows is contained inside php_gd2.dll
       if (OS.isFamilyWindows() && extension.equals("gd")) extension= "gd2";
 
       // Check extension is not available
       if (!availableExtensions.contains(extension)) {
         getLog().warn("Required PHP extension [" + extension + "] could not be loaded; ignored");
         continue;
       }
 
       // Add to list
       if (OS.isFamilyWindows()) {
         extensionsToLoad.add("php_" + extension + ".dll");
       } else {
         extensionsToLoad.add(extension + ".so");
       }
     }
 
     // Overwrite value
     this.extensions= extensionsToLoad.isEmpty() ? null : extensionsToLoad;
   }
 
   /**
    * Get list of already loaded PHP extensions
    *
    * @return java.util.List<java.lang.String>
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private List<String> getLoadedPhpExtensions() throws MojoExecutionException {
 
     // Setup runner
     PhpRunnerInput input= new PhpRunnerInput();
     input.code= "foreach (get_loaded_extensions() as $ext) echo strtolower($ext).'•'";
 
     PhpRunner runner= new PhpRunner(this.php, input);
     runner.setLog(getLog());
 
     // Execute runner
     try {
       runner.execute();
     } catch (RunnerException ex) {
       throw new MojoExecutionException("Execution of [php] runner failed: " + runner.getOutput().asString(), ex);
     }
 
     // Return runner output
     return Arrays.asList(runner.getOutput().asString().trim().split("•"));
   }
 
   /**
    * Get list of available PHP extensions
    *
    * @return java.util.List<java.lang.String>
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private List<String> getAvailablePhpExtensions() throws MojoExecutionException {
 
     // Setup runner
     PhpRunnerInput input= new PhpRunnerInput();
     input.code= "echo ini_get('extension_dir')";
 
     PhpRunner runner= new PhpRunner(this.php, input);
     runner.setLog(getLog());
 
     // Execute runner
     try {
       runner.execute();
     } catch (RunnerException ex) {
       throw new MojoExecutionException("Execution of [php] runner failed: " + runner.getOutput().asString(), ex);
     }
 
    // Get extensions directory
     File extensionsDirectory= new File(runner.getOutput().asString().trim());
    getLog().debug(" - PHP extensions directory [" + extensionsDirectory + "]");
     if (!extensionsDirectory.exists() || !extensionsDirectory.isDirectory()) {
      throw new MojoExecutionException("Invalid PHP extensions directory [" + extensionsDirectory +"]");
     }
 
     // List directory contents
     File[] entries= extensionsDirectory.listFiles();
     if (null == entries) {
       throw new MojoExecutionException("Failed to list contents of directory [" + extensionsDirectory + "]");
     }
 
     List<String> retVal= new ArrayList<String>();
     for (File entry : entries) {
       String entryName= entry.getName();
 
       // Remove starting and trailing bits
       if (entryName.startsWith("php_")) entryName= entryName.substring(4);
       if (entryName.endsWith(".dll"))   entryName= entryName.substring(0, entryName.length() - 4);
       if (entryName.endsWith(".so"))    entryName= entryName.substring(0, entryName.length() - 3);
 
       // Add to list
       retVal.add(entryName.toLowerCase());
     }
 
     // Return list of found extensions
     return retVal;
   }
 
   /**
    * This will alter ${project.sourceDirectory} and ${project.testSourceDirectory} only if they have
    * the default values set up in the Maven Super POM:
    * - src/main/java
    * - src/test/java
    *
    * to the following values:
    * - src/main/xp
    * - src/test/xp
    *
    * @return void
    */
   private void alterSourceDirectories() {
 
     // Check ${project.sourceDirectory} ends with "src/main/java"
     String oldDirectory = this.project.getBuild().getSourceDirectory();
     String xpDirectory  = this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "xp";
     if (oldDirectory.endsWith("src" + File.separator + "main" + File.separator + "java")) {
 
       // Alter ${project.sourceDirectory}
       this.project.getBuild().setSourceDirectory(xpDirectory);
       getLog().debug("Set ${project.sourceDirectory} to [" + xpDirectory + "]");
 
       // Maven2 limitation: changing ${project.sourceDirectory} doesn't change ${project.compileSourceRoots}
       List<String> newRoots= new ArrayList<String>();
       for (String oldRoot : (List<String>)this.project.getCompileSourceRoots()) {
         if (oldRoot.equals(oldDirectory)) {
           newRoots.add(xpDirectory);
         } else {
           newRoots.add(oldRoot);
         }
       }
 
       // Replace ${project.compileSourceRoots} with new list
       this.project.getCompileSourceRoots().clear();
       for (String newRoot : newRoots) {
         this.project.addCompileSourceRoot(newRoot);
       }
     }
 
     // Check ${project.testSourceDirectory} ends with "src/test/java"
     oldDirectory= this.project.getBuild().getTestSourceDirectory();
     xpDirectory= this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "xp";
     if (oldDirectory.endsWith("src" + File.separator + "test" + File.separator + "java")) {
 
       // Alter ${project.testSourceDirectory}
       this.project.getBuild().setTestSourceDirectory(xpDirectory);
       getLog().debug("Set ${project.testSourceDirectory} to [" + xpDirectory + "]");
 
       // Maven2 limitation: changing ${project.testSourceDirectory} doesn't change ${project.testCompileSourceRoots}
       List<String> newRoots= new ArrayList<String>();
       for (String oldRoot : (List<String>)this.project.getTestCompileSourceRoots()) {
         if (oldRoot.equals(oldDirectory)) {
           newRoots.add(xpDirectory);
         } else {
           newRoots.add(oldRoot);
         }
       }
 
       // Replace ${project.testCompileSourceRoots} with new list
       this.project.getTestCompileSourceRoots().clear();
       for (String newRoot : newRoots) {
         this.project.addTestCompileSourceRoot(newRoot);
       }
     }
 
     // Set ${project.itSourceDirectory}
     xpDirectory= this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "it" + File.separator + "xp";
     getLog().debug("Set ${project.itSourceDirectory} to [" + xpDirectory + "]");
     this.project.getProperties().setProperty("project.itSourceDirectory", xpDirectory);
   }
 }
