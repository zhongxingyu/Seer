 /**
  * This file is part of the XP-Framework
  *
  * XP-Framework Maven plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.archiver.AbstractArchiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.util.DefaultFileSet;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
 import net.xp_forge.maven.plugins.xp.logging.LogLogger;
 
 import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;
 
 /**
  * Pack project sources
  *
  * @goal source
  * @since 3.1.9
  */
 public class SourceMojo extends AbstractXpMojo {
 
   /**
    * Specify what archiver to use. There are 2 options:
    * - zip
    * - xar
    *
   * If not set, ${project.packaging} will be used
   *
    * @parameter expression="${xp.source.format}" default-value="xar"
    * @required
    */
   protected String format;
 
   /**
    * List of files to include. Specified as fileset patterns which are relative to ${basedir}
    *
    * @parameter
    */
   protected List<String> includes;
 
   /**
    * List of files to exclude. Specified as fileset patterns which are relative to ${basedir}
    *
    * @parameter
    */
   protected List<String> excludes;
 
   /**
    * Specifies whether or not to attach the artifact to the project
    *
    * @parameter expression="${xp.source.attach}" default-value="true"
    * @required
    */
   protected boolean attach;
 
   /**
    * Name of the generated XAR
    *
    * @parameter expression="${project.build.finalName}"
    * @required
    */
   protected String finalName;
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   @SuppressWarnings("unchecked")
   public void execute() throws MojoExecutionException {
     ArchiveUtils.enableLogging(new LogLogger(getLog()));
 
     // Debug info
     getLog().info("Format   [" + this.format + "]");
     getLog().info("Includes [" + (null == this.includes ? "n/a" : this.includes) + "]");
     getLog().info("Excludes [" + (null == this.excludes ? "n/a" : this.excludes) + "]");
     getLog().info("Attach   [" + (this.attach ? "yes" : "no") + "]");
 
     // Get output file
     File outputFile= this.getOutputFile();
     getLog().debug("Output file [" + outputFile + "]");
 
     // Load archiver
     AbstractArchiver archiver= ArchiveUtils.getArchiver(outputFile);
 
     // Init archive contents
     DefaultFileSet fileSet= new DefaultFileSet();
     fileSet.setDirectory(this.basedir);
     fileSet.setPrefix(this.finalName + "/");
 
     // Init excludes
     if (null == this.excludes) {
       this.excludes= new ArrayList<String>();
     }
 
     // Exclude ${basedir}/target
     this.excludes.add("target/**");
 
     // Exclude directories created by xp:ignite
     this.excludes.add("lib/**");
     this.excludes.add("ignite.pth");
 
     // Exclude SCM resources ("CSV", ".svn", etc.)
     this.excludes.addAll(Arrays.asList(DirectoryScanner.DEFAULTEXCLUDES));
 
     // Set excludes & includes
     fileSet.setExcludes(this.excludes.toArray(new String[this.excludes.size()]));
     if (null != this.includes) {
       fileSet.setIncludes(this.includes.toArray(new String[this.includes.size()]));
     }
 
     // Add filtered resources to archive
     archiver.addFileSet(fileSet);
 
     // Save archive to output file
     try {
       getLog().debug(" - Creating sources archive [" + outputFile + "]");
       outputFile.delete();
       archiver.createArchive();
     } catch (Exception ex) {
       throw new MojoExecutionException(
         "Cannot create [" + this.format + "] to [" + outputFile + "]", ex
       );
     }
 
     // Attach generated archive as project artifact
     if (this.attach) {
       this.projectHelper.attachArtifact(this.project, this.format, "sources", outputFile);
     }
   }
 
   /**
    * Returns the output file, based on finalName, classifier and format
    *
    * @return java.io.File Location where to generate the output XAR file
    */
   private File getOutputFile() {
     return new File(
       this.outputDirectory,
       this.finalName + "-sources." + this.format
     );
   }
 }
