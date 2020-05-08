 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.runners;
 
 import java.io.File;
 import java.util.List;
 import java.util.ArrayList;
 
 import net.xp_forge.maven.plugins.xp.runners.input.UnittestRunnerInput;
 
 /**
  * Wrapper over XP-Framework "unittest" runner
  *
  */
 public class UnittestRunner extends AbstractRunner {
  private UnittestRunnerInput input;
 
   /**
    * Constructor
    *
    * @param  net.xp_forge.maven.plugins.xp.runners.input.UnittestRunnerInput input
    */
   public UnittestRunner(File executable, UnittestRunnerInput input) {
     super(executable);
     this.input= input;
   }
 
   /**
    * {@inheritDoc}
    *
    */
   public void execute() throws RunnerException {
 
     // Build arguments
     List<String> arguments= new ArrayList<String>();
 
     // Configure classpath (via project.pth)
     File pthFile= new File(this.getWorkingDirectory(), "project.pth");
     this.setClasspath(this.input.classpaths, pthFile);
 
     // Add verbose (-v)
     if (this.input.verbose) arguments.add("-v");
 
     // Add arguments (-a)
     for (String arg : this.input.arguments) {
       arguments.add("-a");
       arguments.add(arg);
     }
 
     // Add inifiles
     for (File ini : this.input.inifiles) {
       arguments.add(ini.getAbsolutePath());
     }
 
     // Execute command
     this.executeCommand(arguments);
   }
 }
