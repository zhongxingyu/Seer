 /**
  * This file is part of the XP-Framework
  *
  * XP-Framework Maven plugin
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.maven.artifact.Artifact;
 
 import org.codehaus.plexus.archiver.AbstractArchiver;
 import org.codehaus.plexus.archiver.AbstractUnArchiver;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 
 import org.codehaus.plexus.logging.Logger;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.archiver.xar.XarArchiver;
 import net.xp_forge.maven.plugins.xp.archiver.xar.XarUnArchiver;
 
 /**
  * Utility class
  *
  */
 public final class ArchiveUtils {
   private static Logger logger;
 
   /**
    * Utility classes should not have a public or default constructor
    *
    */
   private ArchiveUtils() {
   }
 
   /**
    * Set STATIC logger
    *
    * @param  org.codehaus.plexus.logging.Logger log
    * @return void
    */
   public static void enableLogging(Logger log) {
     ArchiveUtils.logger= log;
   }
 
   /**
    * Get archiver based on specified file
    *
    * @param  java.io.File file
    * @return org.codehaus.plexus.archiver.AbstractArchiver
    */
   public static AbstractArchiver getArchiver(File file) throws IllegalArgumentException {
     String extension= file.getName().substring(file.getName().lastIndexOf('.') + 1);
 
     if (extension.equals("xar")) {
       AbstractArchiver retVal= new XarArchiver();
       retVal.setDestFile(file);
       retVal.enableLogging(ArchiveUtils.logger);
       return retVal;
     }
 
     if (extension.equals("zip")) {
       AbstractArchiver retVal= new ZipArchiver();
       retVal.setDestFile(file);
       retVal.enableLogging(ArchiveUtils.logger);
       return retVal;
     }
 
     // Invalid package type
     throw new IllegalArgumentException("Cannot get Archiver for [" + file + "]");
   }
 
   /**
    * Get unarchiver for the specified file
    *
    * @param  java.io.File file
    * @return org.codehaus.plexus.archiver.AbstractUnArchiver
    */
   public static AbstractUnArchiver getUnArchiver(File file) throws IllegalArgumentException {
     String extension= file.getName().substring(file.getName().lastIndexOf('.') + 1);
 
     if (extension.equals("xar")) {
       AbstractUnArchiver retVal= new XarUnArchiver(file);
       retVal.enableLogging(ArchiveUtils.logger);
       return retVal;
     }
 
     if (extension.equals("zip")) {
       AbstractUnArchiver retVal= new ZipUnArchiver(file);
       retVal.enableLogging(ArchiveUtils.logger);
       return retVal;
     }
 
     // Invalid extension
     throw new IllegalArgumentException("Cannot get UnArchiver for [" + file + "]");
   }
 
   /**
    * Get unarchiver for the specified Artifact
    *
    * @param  org.apache.maven.artifact.Artifact artifact
    * @return org.codehaus.plexus.archiver.AbstractUnArchiver
    */
   public static AbstractUnArchiver getUnArchiver(Artifact artifact) throws IllegalArgumentException {
     return ArchiveUtils.getUnArchiver(artifact.getFile());
   }
 
   /**
    * Dump artifact contents in the specified directory
    *
    * @param  org.apache.maven.artifact.Artifact artifact
    * @param  java.io.File destDirectory
    * @param  boolean overwrite
    * @throws org.codehaus.plexus.archiver.ArchiverException
    */
   public static void dumpArtifact(Artifact artifact, File destDirectory, boolean overwrite) throws ArchiverException {
     ArchiveUtils.dumpArtifact(artifact.getFile(), destDirectory, overwrite);
   }
 
   /**
    * Dump artifact contents in the specified directory
    *
    * @param  java.io.File artifact
    * @param  java.io.File destDirectory
    * @param  boolean overwrite
    * @throws org.codehaus.plexus.archiver.ArchiverException
    */
   public static void dumpArtifact(File artifact, File destDirectory, boolean overwrite) throws ArchiverException {
 
     // Create destination directory if not exists
     if (!destDirectory.exists()) {
       destDirectory.mkdirs();
     }
 
     // Dump artifact contents
     AbstractUnArchiver unarchiver= ArchiveUtils.getUnArchiver(artifact);
     unarchiver.setDestDirectory(destDirectory);
     unarchiver.setOverwrite(overwrite);
     unarchiver.extract();
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  org.codehaus.plexus.archiver.UnArchiver src
    * @param  org.codehaus.plexus.archiver.Archiver dest
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(AbstractUnArchiver unArchiver, AbstractArchiver archiver, Map<String, String> entries) throws IOException {
 
     // Copy entries
     File tmpDirectory= FileUtils.getTempDirectory();
     for (Map.Entry<String, String> entry: entries.entrySet()) {
       String srcEntry  = entry.getKey();
       String destEntry = entry.getValue();
 
       // Get entry name
       String entryName= srcEntry;
       if (srcEntry.contains("/")) {
         entryName= srcEntry.substring(srcEntry.lastIndexOf('/') + 1);
       }
 
       // Unpack & pack entry
       unArchiver.extract(srcEntry, tmpDirectory);
       archiver.addFile(new File(tmpDirectory, entryName), destEntry);
     }
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  java.io.File srcFile
    * @param  org.codehaus.plexus.archiver.AbstractArchiver archiver
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(File srcFile, AbstractArchiver archiver, Map<String, String> entries) throws IOException {
     ArchiveUtils.copyArchiveEntries(ArchiveUtils.getUnArchiver(srcFile), archiver, entries);
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  org.apache.maven.artifact.Artifact srcArtifact
    * @param  org.codehaus.plexus.archiver.AbstractArchiver archiver
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(Artifact srcArtifact, AbstractArchiver archiver, Map<String, String> entries) throws IOException {
     ArchiveUtils.copyArchiveEntries(srcArtifact.getFile(), archiver, entries);
   }
 }
