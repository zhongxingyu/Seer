 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.archiver.xar;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.AbstractUnArchiver;
 
 import net.xp_forge.xar.XarEntry;
 import net.xp_forge.xar.XarArchive;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 
 /**
 * A plexus unarchiver implementation for XAR file format
  *
  */
 public class XarUnArchiver extends AbstractUnArchiver {
    private XarArchive archive;
 
     /**
      * Constructor
      *
      */
     public XarUnArchiver() {
     }
 
     /**
      * Constructor
      *
      */
     public XarUnArchiver(File srcFile) {
       super(srcFile);
     }
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   protected void execute() throws ArchiverException {
     File srcFile       = this.getSourceFile();
     File destDirectory = this.getDestDirectory();
     //getLogger().debug("Expanding [" + srcFile + "] into [" + destDirectory + "]");
 
     // Load archive
     try {
       this.archive= new XarArchive(srcFile);
     } catch (IOException ex) {
       throw new ArchiverException("Cannot load XAR archive [" + srcFile + "]", ex);
     }
 
     // Create destDirectory
     if (!destDirectory.exists()) {
       destDirectory.mkdirs();
     }
 
     // Extract archive entries
     for (XarEntry entry : archive.getEntries()) {
       File outFile= new File(destDirectory, entry.getName().replace('/', File.separatorChar));
       try {
         //getLogger().debug("Expanding [" + entry.getName() + "] into [" + outFile + "]");
         FileUtils.setFileContents(outFile, entry.getInputStream());
       } catch (IOException ex) {
         throw new ArchiverException("Error while expanding [" + entry.getName() + "]", ex);
       }
     }
 
     // Cleanup
     this.archive= null;
   }
 
   /**
    * {@inheritDoc}
    *
    */
   @Override
   protected void execute(String path, File outputDirectory) throws ArchiverException {
     File srcFile= this.getSourceFile();
     //getLogger().debug("Expanding [" + srcFile + "#" + path + "] into [" + outputDirectory + "]");
 
     // Remove starting slash from path; if the case
     if (path.startsWith("/")) {
       path= path.substring(1);
     }
 
     // Remove trailing slash from path; if the case
     if (path.endsWith("/")) {
       path= path.substring(0, path.length() - 1);
     }
 
     // Load archive
     try {
       this.archive= new XarArchive(srcFile);
     } catch (IOException ex) {
       throw new ArchiverException("Cannot load XAR archive [" + srcFile + "]", ex);
     }
 
     // Create outputDirectory
     if (!outputDirectory.exists()) {
       outputDirectory.mkdirs();
     }
 
     // Extract archive entries that start with path
     File outFile;
     for (XarEntry entry : archive.getEntries()) {
       String entryName= entry.getName();
 
       // Full entry match
       if (entryName.equals(path)) {
         int pos= entryName.lastIndexOf('/');
         if (-1 == pos) {
           outFile= new File(outputDirectory, entryName);
         } else {
           outFile= new File(outputDirectory, entryName.substring(pos + 1));
         }
 
       // Directory match
       } else if (entryName.startsWith(path + "/")) {
         outFile= new File(outputDirectory, entryName.substring(path.length() + 1));
 
       // No match; try next entry
       } else {
         continue;
       }
 
       try {
         //getLogger().debug("Expanding [" + entry.getName() + "] into [" + outputDirectory + "]");
         FileUtils.setFileContents(outFile, entry.getInputStream());
       } catch (IOException ex) {
         throw new ArchiverException("Error while expanding [" + entry.getName() + "]", ex);
       }
     }
 
     // Cleanup
     this.archive= null;
   }
 }
