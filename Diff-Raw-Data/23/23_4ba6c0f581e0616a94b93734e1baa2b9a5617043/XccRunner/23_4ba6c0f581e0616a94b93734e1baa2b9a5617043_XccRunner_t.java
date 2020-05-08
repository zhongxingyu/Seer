 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.runners;
 
 import java.io.File;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import net.xp_forge.maven.plugins.xp.runners.input.XccRunnerInput;
 
 /**
  * Wrapper over XP-Framework "xcc" runner
  *
  */
 public class XccRunner extends AbstractRunner {
  private XccRunnerInput input;
 
   /**
    * Constructor
    *
    * @param  net.xp_forge.maven.plugins.xp.runners.input.XccRunnerInput input
    */
   public XccRunner(File executable, XccRunnerInput input) {
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
 
     // Add sourcepath (-sp)
     for (File sp : this.input.sourcepaths) {
       arguments.add("-sp");
       arguments.add(sp.getAbsolutePath());
     }
 
     // Add emitter (-e)
     if (null != this.input.emitter && 0 != this.input.emitter.trim().length()) {
       arguments.add("-e");
       arguments.add(this.input.emitter);
     }
 
     // Add profile (-p)
     if (!this.input.profiles.isEmpty()) {
       String profilesString= "";
       Iterator it= this.input.profiles.iterator();
       while (it.hasNext()) {
         profilesString+= (String)it.next();
         if (it.hasNext()) profilesString+= ",";
       }
 
       arguments.add("-p");
       arguments.add(profilesString);
     }
 
     // Add output (-o)
     if (null == this.input.outputdir) {
       throw new RunnerException("xcc outputdir not set");
     }
     arguments.add("-o");
     arguments.add(this.input.outputdir.getAbsolutePath());
 
     // Add sources
     for (File src : this.input.sources) {
       arguments.add(src.getAbsolutePath());
     }
 
     // Execute command
     this.executeCommand(arguments);
   }
 }
