 package com.ece.superkids;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FileManagerImpl implements FileManager {
 
     private static String fileDirectory;
     private static final String customQuestionsFile = "customQuestions.txt";
 
    public FileManagerImpl() {
         String userHome = System.getProperty("user.home");
         fileDirectory = userHome + File.separator + ".superkidsdata";
     }
 
     @Override
     public File getCustomQuestionsFile() {
         File customFile = new File(fileDirectory + File.separator + customQuestionsFile);
         if (!customFile.exists())
             try {
                 customFile.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         return customFile;
     }
 
     @Override
     public File getUserFile(final String userName) {
         return null;
     }
 
     @Override
     public File getImagePathsFile() {
         return null;
     }
 
     @Override
     public File getDirectory() {
         return new File(fileDirectory);
     }
 }
