 /**
  * This file is part of the XP-Framework
  *
  * Maven XP-Framework plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package org.apache.maven.plugins.xpframework;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Iterator;
import java.util.Set;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.xpframework.runners.RunnerException;
 import org.apache.maven.plugins.xpframework.runners.XpRunner;
import org.apache.maven.plugins.xpframework.runners.input.XpRunnerInput;
 
 /**
  * Run unittests
  *
  * @goal xp
  * @requiresDependencyResolution    runtime
  */
 public class XpMojo extends AbstractXpFrameworkMojo {
 
   // ----------------------------------------------------------------------
   // Configurables
   // ----------------------------------------------------------------------
 
   /**
    * Display verbose diagnostics
    *
    * The -v argument for the unittest runner
    *
    * @parameter expression="${xpframework.xp.verbose}" default-value="false"
    */
   protected boolean verbose;
 
   /**
    * Add path to classpath
    *
    * The -cp argument for the unittest runner
    *
    * @parameter expression="${xpframework.xp.classpaths}"
    */
   protected ArrayList<String> classpaths;
 
   /**
    * The directory containing generated classes of the project being tested
    * This will be included after the test classes in the test classpath
    *
    * @parameter default-value="${project.build.outputDirectory}"
    */
   protected File classesDirectory;
 
   /**
    * Define name of class to run
    *
    * @parameter expression="${xpframework.xp.classname}"
    */
   protected String className;
 
   /**
    * Define inline code to run
    *
    * @parameter expression="${xpframework.xp.code}"
    */
   protected String code;
 
   /**
    * Assemble test XAR archive
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException When unittest runner execution failed
    */
   public void execute() throws MojoExecutionException {
     Iterator i;
 
     // Run tests
     getLog().info(LINE_SEPARATOR);
     getLog().info("RUN - XP CLASS");
     getLog().info(LINE_SEPARATOR);
 
     // Debug info
     getLog().debug("Classes directory      [" + this.classesDirectory + "]");
     getLog().debug("Classpaths             [" + (null == this.classpaths ? "NULL" : this.classpaths.toString()) + "]");
     getLog().debug("ClassName              [" + this.className + "]");
     getLog().debug("Code                   [" + this.code + "]");
 
     // Prepare unittest input
    XpRunnerInput input= new XpRunnerInput();
     input.verbose= this.verbose;
 
     // Add testClassesDirectory and classesDirectory to classpaths
     input.addClasspath(classesDirectory);
 
     // Add xar dependencies to classpath
     Set projectArtifacts= this.project.getArtifacts();
     if (projectArtifacts.isEmpty()) {
       getLog().debug("No dependencies found");
     } else {
       getLog().info("Dependencies:");
       for (Iterator it= projectArtifacts.iterator(); it.hasNext(); ) {
         Artifact projectArtifact= (Artifact) it.next();
         getLog().info(" * " + projectArtifact.getType() + " [" + projectArtifact.getFile().getAbsolutePath() + "]");
 
         // Add xar file to classpath
         if (!projectArtifact.getType().equalsIgnoreCase("xar")) continue;
         input.addClasspath(projectArtifact.getFile());
       }
     }
 
     // Add pom-defined classpaths
     if (null != this.classpaths) {
       i= this.classpaths.iterator();
       while (i.hasNext()) {
         input.addClasspath(new File((String)i.next()));
       }
     }
 
     input.className= this.className;
     input.code= this.code;
 
     // Prepare runner
     XpRunner runner= new XpRunner(input);
     runner.setTrace(getLog());
 
     // Set runner working directory
     try {
       runner.setWorkingDirectory(this.basedir);
     } catch (FileNotFoundException ex) {
       throw new MojoExecutionException("Cannot set xp runner working directory", ex);
     }
 
     // Execute runner
     try {
       runner.execute();
     } catch (RunnerException ex) {
       throw new MojoExecutionException("Execution of xp runner failed", ex);
     }
 
     getLog().info(LINE_SEPARATOR);
   }
 }
