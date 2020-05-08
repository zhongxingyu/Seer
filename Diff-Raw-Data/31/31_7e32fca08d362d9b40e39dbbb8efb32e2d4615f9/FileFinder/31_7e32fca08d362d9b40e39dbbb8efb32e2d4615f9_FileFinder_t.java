 package com.javastorm.common.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 /**
  * The FileFinder class is used for finding files
  * 
  * @author Hemant Kumar
  * @version 1.0 Dated: 13/01/2013 
  */
 public class FileFinder 
 {
   /**
    * This field contains the repository path where all the configuration files are lying.
   * It gets its value from System Properties 	
    */
   private static final String fileRepoPath_;
 	
   static {
    fileRepoPath_ = System.getProperty("FILE_REPO");
   }
 
   /**
   * This method is responsible for finding the specified file. If FILE_REPO is defined 
   * then file will be searched in FILE_REPO and if it is not defined then file will be
    * searched in class path.
    * @param fileName
    * @return File[]
    */
   public static File[] findFiles(String fileName) {
     List<File> fileList_ = new ArrayList<File>();
     if(fileRepoPath_ != null)
      listFiles(new File(fileRepoPath_),fileName, fileList_);
     if(fileList_.size()==0) {
       try {
 	    Enumeration<URL> en_ = FileFinder.class.getClassLoader().getResources(fileName);
 	    while(en_.hasMoreElements()) {
 	      fileList_.add(new File(en_.nextElement().getFile().replaceAll("%20"," ")));
 	    }
 	  } 
 	  catch(IOException e) { }
     }
 	return (File[])fileList_.toArray(new File[fileList_.size()]);
   }
   
   /**
    * This function recursively scans the folders and lists the files
    * @param rootFile
    * @param fileList
    */
  private static void listFiles(File rootFile, String filename, List<File> fileList) {
     File[] files_ = rootFile.listFiles();
    if(files_ != null) {
      for(File file : files_) {
        if(file.isFile() && file.getName().equalsIgnoreCase(filename))
    	  fileList.add(file);
        else
          listFiles(file, filename, fileList);
      }
    }
   }
 }
