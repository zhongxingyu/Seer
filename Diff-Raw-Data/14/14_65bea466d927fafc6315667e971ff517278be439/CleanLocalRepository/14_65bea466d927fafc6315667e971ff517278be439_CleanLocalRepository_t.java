 package org.hackystat.utilities.ivy;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Removes all but most recently published files from the Hackystat local repository.
  * Can be invoked locally from the hackystat-utilities build system using 
  * "ant cleanlocalrepository". 
  * The repository is structured as follows:
  * <pre>
  * ~/.ivy2/local-repository/org.hackystat/
  *                                        hackystat-utilities/
  *                                                            2009.07.13.12.49.00/
  *                                                                                f1
  *                                                                                f2
  *                                                            2009.07.08.16.26.27/
  *                                                                                f1
  *                                                                                f2
  *                                                            :
  *                                        hackystat-sensorbase-uh/
  *                                                                2009.07.13.12.49.00/
  *                                                                :
  *                                        :
  * </pre>  
  * @author Philip Johnson
  *
  */
 public class CleanLocalRepository {
   
   /** The location of the Hackystat Ivy local repository. */
   private static File repo =
     new File(System.getProperty("user.home") + "/.ivy2/local-repository/org.hackystat");
 
   /**
    * Processes the entire local Hackystat repository.
    * @param repo The local repo.
    */
   private static void processRepository(File repo) {
     System.out.println("Processing repository: " + repo);
     for (File fileOrDir : repo.listFiles()) {
       if (fileOrDir.isDirectory()) {
         processModule(fileOrDir);
       }
     }
   }
   
   /**
    * Processes an individual Hackystat module publication directory.
    * @param moduleDir The module directory.
    */
   private static void processModule(File moduleDir) {
     System.out.println("Processing module: " + moduleDir);
     // Create sorted list of publication date directory names.
     List<String> pubDirNames = new ArrayList<String>();
     for (String pubDirName : moduleDir.list()) {
       if ((new File(moduleDir, pubDirName)).isDirectory()) {
         pubDirNames.add(pubDirName);
       }
     }
     Collections.sort(pubDirNames);
     // Determine whether there are any directories to delete.
     int size = pubDirNames.size();
     if (size <= 1) {
       System.out.println("  No old publications to delete.");
     }
     else {
       // Delete all old publications.
       for (String pubDirName : pubDirNames.subList(0, (size - 1))) {
         deletePublicationDir(new File(moduleDir, pubDirName));
       }
     }
   }
   
   /**
    * Deletes the passed publication directory.
    * @param pubDir The publication directory to delete.
    */
   private static void deletePublicationDir(File pubDir) {
     System.out.println("  Deleting contents of directory: " + pubDir);
     for (File pubFile : pubDir.listFiles()) {
       if (pubFile.isFile()) {
        String deleteSuccess = pubFile.delete() ? "OK" : "not OK";
        System.out.println("    Deleting file: " + pubFile + "was " + deleteSuccess);
       }
     }
    String deleteSuccess = pubDir.delete() ? "OK" : "not OK";
    System.out.println("   Deleting directory: " + pubDir + "was " + deleteSuccess);
   }
   
  /**
   * Runs the program.
   * @param args Ignored. 
   */
   public static void main(String[] args) {
     if (repo.exists()) {
       processRepository(repo);
       System.out.println("Finished processing the local hackystat repository.");
     }
     else {
       System.out.println(repo + " does not exist. Doing nothing.");
     }
   }
 
 }
