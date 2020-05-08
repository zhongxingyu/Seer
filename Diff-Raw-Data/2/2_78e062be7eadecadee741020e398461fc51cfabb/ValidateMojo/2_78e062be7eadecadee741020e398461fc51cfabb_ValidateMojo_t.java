 /**
  * This file is part of the XP-Framework
  *
  * Maven XP-Framework plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package org.apache.maven.plugins.xpframework;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.apache.maven.plugins.xpframework.util.ExecuteUtils;
 import org.apache.maven.plugins.xpframework.AbstractXpFrameworkMojo;
 
 /**
  * Check for XP Framework runners
  *
  * @goal validate
  * @execute lifecycle="xar" phase="validate"
  */
 public class ValidateMojo extends AbstractXpFrameworkMojo {
 
   /**
    * Check for XP Framework runners in PATH
    *
    */
   public void execute() throws MojoExecutionException {
     getLog().info(LINE_SEPARATOR);
     getLog().info("VALIDATE XP-FRAMEWORK INSTALL");
     getLog().info(LINE_SEPARATOR);
 
     // Check for XP runners
     try {
       getLog().info("Found xcc      at [" + ExecuteUtils.getExecutable("xcc") + "]");
       getLog().info("Found xar      at [" + ExecuteUtils.getExecutable("xar") + "]");
       getLog().info("Found unittest at [" + ExecuteUtils.getExecutable("unittest") + "]");
       getLog().info(LINE_SEPARATOR);
     } catch (FileNotFoundException ex) {
       throw new MojoExecutionException("Cannot find XP Framework runners. Install them from http://xp-framework.net/", ex);
     }
 
     // Alter default Maven settings
     this.alterSourceDirectories();
     getLog().info(LINE_SEPARATOR);
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
   }
 }
