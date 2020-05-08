 package com.ece.superkids;
 
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

 import java.io.File;
 import java.io.IOException;
 
 public class FileManagerImpl implements FileManager {
 
     private static String fileDirectory;
     private static final String customQuestionsFile = "customQuestions.txt";
 
    private FileManagerImpl() {
         String userHome = System.getProperty("user.home");
         fileDirectory = userHome + File.separator + ".superkidsdata";
     }
 
    private static class FileManagerFactory {
        private static final FileManagerImpl INSTANCE = new FileManagerImpl();
    }

    public static FileManagerImpl getInstance() {
        return FileManagerFactory.INSTANCE;
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
