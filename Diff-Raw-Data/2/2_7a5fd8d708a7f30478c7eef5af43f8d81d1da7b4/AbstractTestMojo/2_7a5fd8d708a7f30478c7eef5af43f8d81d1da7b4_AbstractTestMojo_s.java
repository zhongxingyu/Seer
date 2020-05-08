 /**
  * This file is part of the XP-Framework
  *
  * XP-Framework Maven plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 import net.xp_forge.maven.plugins.xp.exec.RunnerException;
 import net.xp_forge.maven.plugins.xp.exec.runners.xp.UnittestRunner;
 import net.xp_forge.maven.plugins.xp.exec.input.xp.UnittestRunnerInput;
 
 /**
  * Wrapper around the XP-Framework "UnittestRunner" runner
  *
  */
 public abstract class AbstractTestMojo extends AbstractXpMojo {
 
   /**
    * Whether to skip running tests
    *
    * @return boolean
    */
   protected abstract boolean isSkip();
 
   /**
    * Location of test [*.ini] files directory
    *
    * @return java.io.File
    */
   protected abstract File getIniDirectory();
 
   /**
   * Location of additional test [*.ini] files directory
    *
    * @return java.util.List<java.io.File>
    */
   protected abstract List<File> getAdditionalIniDirectories();
 
   /**
    * Location of classes directory
    *
    * @return java.io.File
    */
   protected abstract File getClassesDirectory();
 
   /**
    * Location of test classes directory
    *
    * @return java.io.File
    */
   protected abstract File getTestClassesDirectory();
 
   /**
    * Whether to run all tests using a single unittest runner instance
    *
    * @return boolean
    */
   protected abstract boolean isSingleInstance();
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   @SuppressWarnings("unchecked")
   public void execute() throws MojoExecutionException {
 
     // Skip tests alltogether?
     if (this.isSkip()) {
       getLog().info("Not running tests");
       return;
     }
 
     File iniDirectory                   = this.getIniDirectory();
     List<File> additionalIniDirectories = this.getAdditionalIniDirectories();
     File classesDirectory               = this.getClassesDirectory();
     File testClassesDirectory           = this.getTestClassesDirectory();
     boolean singleInstance              = this.isSingleInstance();
 
     // Debug info
     getLog().info("Unittest ini directory [" + iniDirectory + "]");
     getLog().debug("Additional directories [" + (null == additionalIniDirectories ? "NULL" : additionalIniDirectories) + "]");
     getLog().debug("Classes directory      [" + classesDirectory + "]");
     getLog().debug("Test classes directory [" + testClassesDirectory + "]");
     getLog().debug("Single runner instance [" + (true == singleInstance ? "Yes" : "No") + "]");
 
     // Prepare [unittest] input
     UnittestRunnerInput input= new UnittestRunnerInput();
 
     // Add dependency classpaths
     input.addClasspath(this.getArtifacts(false));
 
     // Add vendor libs to classpath
     List<File> vendorLibs= this.getVendorLibs();
     if (null == vendorLibs) {
       getLog().debug("No vendor libraries found");
 
     } else {
       getLog().debug("Found vendor libraries:");
       for (File vendorLib : vendorLibs) {
         getLog().debug("- " + vendorLib);
         input.addClasspath(vendorLib);
       }
     }
 
     // Add classesDirectory to classpath
     if (null != classesDirectory) {
       if (null != this.classifier && this.classifier.equals("patch")) {
         input.addClasspath("!" + classesDirectory);
       } else {
         input.addClasspath(classesDirectory);
       }
     }
 
     // Add testClassesDirectory to classpath
     input.addClasspath(testClassesDirectory);
 
     // Add xsl directory to classpath; if present
     File xslDirectory= new File(this.outputDirectory, "xsl");
     if (xslDirectory.exists()) {
       input.addClasspath(xslDirectory);
     }
 
     // Inifiles
     input.addInifileDirectory(iniDirectory);
     if (null != additionalIniDirectories) {
       for (File additionalIniDirectory : additionalIniDirectories) {
         input.addInifileDirectory(additionalIniDirectory);
       }
     }
 
     // Check no tests to run
     if (0 == input.inifiles.size()) {
       getLog().info("There are no tests to run");
       getLog().info(LINE_SEPARATOR);
       return;
     }
 
     // Execute all *.ini files together
     if (true == singleInstance || 1 == input.inifiles.size()) {
       this.executeUnittestRunner(input);
 
     // Spawn a unittest runner for each *.ini file
     } else {
       for (File iniFile : input.inifiles) {
         getLog().info("Running tests from [" + iniFile + "]");
 
         // Prepare new input
         UnittestRunnerInput singleIniInput= input.clone();
         singleIniInput.addInifile(iniFile);
 
         // Execute runner
         this.executeUnittestRunner(singleIniInput);
       }
     }
   }
 
   /**
    * Execute unittests
    *
    * @param 
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   protected void executeUnittestRunner(UnittestRunnerInput input) throws MojoExecutionException {
 
     // Configure [unittest] runner
     File executable= new File(this.runnersDirectory, "unittest");
     UnittestRunner runner= new UnittestRunner(executable, input);
     runner.setLog(getLog());
 
     // Set runner working directory to [/target]
     runner.setWorkingDirectory(this.outputDirectory);
 
     // Set USE_XP environment variable
     if (null != this.use_xp) {
       runner.setEnvironmentVariable("USE_XP", this.use_xp);
     }
 
     // Execute runner
     try {
       runner.execute();
     } catch (RunnerException ex) {
       throw new MojoExecutionException("Execution of [unittest] runner failed", ex);
     }
   }
 }
