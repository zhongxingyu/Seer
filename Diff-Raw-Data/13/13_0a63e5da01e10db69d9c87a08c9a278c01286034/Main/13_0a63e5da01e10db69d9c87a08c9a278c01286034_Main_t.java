 package se.somath.publisher;
 
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 public class Main {
     public void copyToTarget() {
        // todo remove hardcoded file paths
        String targetDirectoryName = "/Users/tsu/projects/tsu/publisher/acceptance-test/target";
        File targetDirectory = new File(targetDirectoryName);
 
        if (!targetDirectory.mkdirs()) {
            String failedDirectoryName = targetDirectory.getAbsolutePath();
            // todo add custom exception
            throw new RuntimeException("Unable to create " + failedDirectoryName);
        }

        // todo remove hardcoded file paths
         File sourceFile = new File("/Users/tsu/projects/tsu/publisher/acceptance-test/src/main/resources/index.html");
 
         try {
             FileUtils.copyFileToDirectory(sourceFile, targetDirectory);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
