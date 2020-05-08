 package se.somath.publisher;
 
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 public class Main {
     public void copyToTarget() {
        File targetDirectory = new File("/Users/tsu/projects/tsu/publisher/acceptance-test/target");
        targetDirectory.mkdirs();
 
         File sourceFile = new File("/Users/tsu/projects/tsu/publisher/acceptance-test/src/main/resources/index.html");
 
         try {
             FileUtils.copyFileToDirectory(sourceFile, targetDirectory);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
