 package org.melati.poem.prepro;
 
 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2006 Tim Pizey
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Pizey <timp At paneris.org>
  *     http://paneris.org/~timp
  */
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 
 /**
  * Process a DSD file.
  * 
  * @goal generate
  * @description Process a DSD file to generate java sources.
  * @phase process-sources
  */
 public class MelatiDsdProcessorMojo extends AbstractMojo {
   /**
    * Location of the dsd.
    * 
    * @parameter expression=
    */
   private String dsdPackage;
 
   /**
    * DSD file name.
    * 
    * @parameter expression=
    */
   private String dsdFile;
 
   /**
    * Location of the source.
    * 
    * @parameter expression="${project.build.sourceDirectory}"
    * @required
    */
   private File sourceDirectory;
 
   /**
    * @parameter expression="${project.groupId}"
    * @required
    */
   private String groupId;
 
   /**
    * @parameter expression="${project.artifactId}"
    * @required
    */
   private String artifactId;
   
   /**
    * @parameter expression="${checkUptodate}" default-value=true
    */
   private boolean checkUptodate;
   
 
   private String searchedLocations = "";
 
   public void execute()
       throws MojoExecutionException {
 
     File f = sourceDirectory;
     if (f == null || !f.exists()) {
       throw new MojoExecutionException("Source directory could not be found");
     }
 
     String dsdPath = null;
     if (dsdPackage != null) {
       String lookupDir = sourceDirectory.getPath() + "/"
           + dsdPackage.replace('.', '/') + "/";
       dsdPath = dsdFileName(lookupDir, dsdFile);
       if (dsdPath == null) {
         if (dsdFile != null) {
           throw new MojoExecutionException("Configured DSD file " + lookupDir
               + dsdFile + " could not be found.");
         } else {
           throw new MojoExecutionException(
               "DSD file could not be found on configured path "
               + dsdPackage
               + " in any of: \n"
               + searchedLocations
               + "Add an explicit dsdPackage and/or dsdFile parameter to your configuration.");
 
         }
       }
     } else {
       String groupDir = groupId.replace('.', '/');
       String sourceDir = sourceDirectory.getPath() + "/" + groupDir + "/";
       String foundDsdName = existingDsdFileName(sourceDir, dsdFile);
       if (foundDsdName == null)
         foundDsdName = existingDsdFileName(sourceDir + artifactId + "/",
             dsdFile);
       if (foundDsdName == null)
         throw new MojoExecutionException(
              "DSD file could not be found in any of: \n"
              + searchedLocations
              + "Add an explicit dsdPackage and/or dsdFile parameter to your configuration.");
       getLog().info("Found DSD at " + foundDsdName + ":");
       dsdPath = foundDsdName;
     }
     String modelDir = dsdPath.substring(0, dsdPath.lastIndexOf('/') + 1);
     String modelName = capitalised(dsdPath.substring(dsdPath.lastIndexOf('/') + 1, dsdPath
         .lastIndexOf('.')));
     String databaseTablesFileName = modelDir + "generated/" + modelName
         + "DatabaseBase.java";
 
     File databaseTablesFile = new File(databaseTablesFileName);
     long dsdTimestamp = new File(dsdPath).lastModified();
     long databaseTablesTimestamp = 1;
    if (databaseTablesFile != null && databaseTablesFile.exists()) {
       databaseTablesTimestamp = databaseTablesFile.lastModified();
     }
     boolean doIt = true;
     if (checkUptodate) { 
       getLog().info(" Checking " + databaseTablesFileName);
       if (dsdTimestamp < databaseTablesTimestamp) {
         getLog().info("Generated files are uptodate. No action required.");
         doIt = false;
       } 
     } else 
       getLog().info(" Not checking - doing regardless");
     if (doIt) {
       DSD dsd;
       try {
         dsd = new DSD(dsdPath);
         dsd.generateJava();
       } catch (Exception e) {
         throw new MojoExecutionException("Error processing DSD", e);
       }
     }
   }
 
  private String existingDsdFileName(String dir, String dsdFileName)
      throws MojoExecutionException {
     String modelDirName = dir + "poem/";
     File modelDir = new File(modelDirName);
    if (modelDir == null || !modelDir.exists()) {
       searchedLocations += " " + modelDirName + "\n";
       modelDirName = dir + "model/";
       modelDir = new File(modelDirName);
     }
    if (modelDir == null || !modelDir.exists()) {
       searchedLocations += " " + modelDirName + "\n";
       modelDirName = dir;
       modelDir = new File(modelDirName);
     }
    if (modelDir == null || !modelDir.exists()) {
       searchedLocations += " " + modelDirName + "\n";
       return null;
     }
     return dsdFileName(modelDirName, dsdFileName);
   }
 
  private String dsdFileName(String dir, String dsdFileName)
      throws MojoExecutionException {
     if (dsdFileName != null) {
       dsdFileName = dir + dsdFileName;
       File foundDsdFile = new File(dsdFileName);
      if (foundDsdFile == null || !foundDsdFile.exists()) {
         searchedLocations += " " + dsdFileName + "\n";
         return null;
       }
     } else {
       dsdFileName = dir + artifactId + ".dsd";
       File foundDsdFile = new File(dsdFileName);
      if (foundDsdFile == null || !foundDsdFile.exists()) {
         searchedLocations += " " + dsdFileName + "\n";
         dsdFileName = dir + capitalised(artifactId) + ".dsd";
         foundDsdFile = new File(dsdFileName);
       }
      if (foundDsdFile == null || !foundDsdFile.exists()) {
         searchedLocations += " " + dsdFileName + "\n";
         return null;
       }
     }
     return dsdFileName;
   }
 
   /**
    * Captialise the first character of the input string.
    * 
    * @param name
    * @return the capitalised string
    */
   public static String capitalised(String name) {
     char suffix[] = name.toCharArray();
     suffix[0] = Character.toUpperCase(suffix[0]);
     return new String(suffix);
   }
 }
