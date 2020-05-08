 package org.krall.security.timer;
 
 import org.krall.security.commandline.AppOptions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileStore;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.TimerTask;
 
 public class FreeSpaceTimer extends TimerTask {
 
     private static final Logger logger = LoggerFactory.getLogger(FreeSpaceTimer.class);
 
     @Override
     public void run() {
         try {
             Path outputDirectory = AppOptions.getInstance().getOutputDirectory().toPath();
             FileStore fileStore = Files.getFileStore(outputDirectory);
 
             double bytesFree = fileStore.getUnallocatedSpace();
             double totalSpace = fileStore.getTotalSpace();
             double percentFree = bytesFree / totalSpace * 100;
             double percentUsed = 100 - percentFree;
 
             if (percentUsed > AppOptions.getInstance().getMaxPercentFree()) {
                double bytesNeedToRemove = totalSpace * AppOptions.getInstance().getKeepPercentFree();
                 List<Path> files = sortDirectoryByLastModified(outputDirectory);
                 int x = 0;
                 while (bytesNeedToRemove > 0) {
                     Path p = files.get(x++);
                    bytesNeedToRemove = -Files.size(p);
                     Files.delete(p);
                 }
             }
         } catch (Exception e) {
             logger.error("Error while checking for free space.", e);
         }
     }
 
     private List<Path> sortDirectoryByLastModified(Path outputDirectory) throws IOException {
         List<Path> files = new ArrayList<>();
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDirectory)) {
             for (Path p : stream) {
                 files.add(p);
             }
         }
 
         Collections.sort(files, new Comparator<Path>() {
             public int compare(Path o1, Path o2) {
                 try {
                     return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
                 } catch (IOException e) {
                     logger.error("error while sorting paths.", e);
                     throw new RuntimeException(e);
                 }
             }
         });
 
         return files;
     }
 
 }
