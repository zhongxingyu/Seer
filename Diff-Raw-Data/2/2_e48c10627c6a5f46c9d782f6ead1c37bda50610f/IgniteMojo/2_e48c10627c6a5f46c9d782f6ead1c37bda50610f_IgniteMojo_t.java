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
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.codehaus.plexus.archiver.ArchiverException;
 
 import net.xp_forge.maven.plugins.xp.io.PthFile;
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
 import net.xp_forge.maven.plugins.xp.logging.LogLogger;
 
 import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;
 
 /**
 * Download project dependencies locally to ${basedir}/lib and create ${basedir}/ignite.pth
  *
  * @goal ignite
  * @requiresDependencyResolution test
  */
 public class IgniteMojo extends AbstractXpMojo {
 
   /**
    * Where to store dependencies artifacts
    *
    * @parameter expression="${xp.ignite.libDirectory}" default-value="${basedir}/lib"
    */
   protected File libDirectory;
 
   /**
    * Unpack dependencies artifacts to ${libDirectory} or keep them packed as .xar files
    *
    * @parameter expression="${xp.ignite.unpackArtifacts}" default-value="false"
    */
   protected boolean unpackArtifacts;
 
   /**
    * Name of the .pth file to be created
    *
    * @parameter expression="${xp.ignite.pthFile}" default-value="${basedir}/ignite.pth"
    */
   protected File pthFile;
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   @SuppressWarnings("unchecked")
   public void execute() throws MojoExecutionException {
     ArchiveUtils.enableLogging(new LogLogger(getLog()));
 
     // Debug info
     getLog().info("Lib directory [" + this.libDirectory + "]");
     getLog().info("Pth file      [" + this.pthFile + "]");
 
     // Init .pth file
     PthFile pth= new PthFile();
 
     // Prepare lib directory
     getLog().debug("Deleting directory [" + this.libDirectory + "]");
     try {
       FileUtils.deleteDirectory(this.libDirectory);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot remove lib directory [" + this.libDirectory + "]", ex);
     }
 
     // Dump artifacts to lib directory
     for (Artifact artifact : this.getArtifacts(true)) {
       File artifactFile= artifact.getFile();
 
       // Skip non-xar artifacts
       if (!artifact.getType().equals("xar")) continue;
 
       // No unpack needed
       if (!this.unpackArtifacts) {
 
         // Copy artifact to libDirectory
         File dstFile= new File(this.libDirectory, artifactFile.getName());
 
         try {
           getLog().info("- Copy [" + artifactFile.getName() + "] to [" + dstFile + "]");
           FileUtils.copyFile(artifactFile, dstFile);
         } catch (IOException ex) {
           throw new MojoExecutionException("Cannot copy [" + artifactFile + "] to [" + dstFile + "]", ex);
         }
 
         // Add to pth
         pth.addFileEntry(dstFile);
 
       // Use unpacked artifacts
       } else {
 
         // Dump artifact to libDirectory
         File dstDirectory= new File(this.libDirectory, artifactFile.getName().replaceFirst("[.][^.]+$", ""));
 
         try {
           getLog().info("- Unpack [" + artifactFile.getName() + "] to [" + dstDirectory + "]");
           ArchiveUtils.dumpArtifact(artifactFile, dstDirectory, true);
         } catch (ArchiverException ex) {
           throw new MojoExecutionException("Cannot unpack [" + artifactFile + "] to [" + dstDirectory + "]", ex);
         }
 
         // Add to pth
         boolean isPatch= null != artifact.getClassifier() && artifact.getClassifier().equals("patch");
         pth.addFileEntry(dstDirectory, isPatch);
       }
     }
 
     // Dump pth file
     try {
       pth.setComment(CREATED_BY_NOTICE);
       pth.dump(pthFile);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot write [" + pthFile + "]", ex);
     }
   }
 }
