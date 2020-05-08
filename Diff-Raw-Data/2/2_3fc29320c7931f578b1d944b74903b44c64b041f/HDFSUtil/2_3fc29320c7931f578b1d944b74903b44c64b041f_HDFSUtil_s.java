 package LASER.Utils;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import sun.tools.tree.NewArrayExpression;

 import java.io.IOException;
 
 public class HDFSUtil {
 
     private static final String LASER_PATH = "Laser";
 
     private static Path getLaserPath(){
         return new Path(LASER_PATH);
     }
 
     public static Path getInputPath(){
         return new Path(getLaserPath(), "input");
     }
 
     public static Path getOutputPath(){
         return new Path(getLaserPath(), "output");
     }
 
     public static Path getTemporaryPath(){
         return new Path(getLaserPath(),"temp");
     }
 
     public static Path getDebugPath() {
         return new Path(getLaserPath(), "debug");
     }
 
     public static boolean cleanupTemporaryPath(Configuration config) throws IOException{
         FileSystem hdfs = FileSystem.get(config);
 
         Logger logger = LoggerFactory.getLogger(HDFSUtil.class);
 
         boolean deleteSucceeded = hdfs.delete(getTemporaryPath(), true);
 
         if(deleteSucceeded) {
             logger.info("Remaining temporary files successfully removed.");
         } else {
             logger.warn("Could not remove temporary files. They might have already been removed in a previous run. " + getTemporaryPath().toString());
         }
 
         return deleteSucceeded;
     }
 
     public static boolean cleanupOutputPath(Configuration config) throws IOException{
         FileSystem hdfs = FileSystem.get(config);
 
         Logger logger = LoggerFactory.getLogger(HDFSUtil.class);
 
         boolean deleteSucceeded = hdfs.delete(getOutputPath(), true);
 
         if(deleteSucceeded) {
             logger.info("Remaining output files successfully removed.");
         } else {
             logger.warn("Could not remove output files. They might have already been removed in a previous run. " + getTemporaryPath().toString());
         }
 
         return deleteSucceeded;
     }
 
     public static boolean cleanupDebugPath(Configuration config) throws IOException{
         FileSystem hdfs = FileSystem.get(config);
 
         Logger logger = LoggerFactory.getLogger(HDFSUtil.class);
 
         boolean deleteSucceeded = hdfs.delete(getDebugPath(), true);
 
         if(deleteSucceeded) {
             logger.info("Remaining debug files successfully removed.");
         } else {
             logger.warn("Could not remove output files. They might have already been removed in a previous run. " + getDebugPath().toString());
         }
 
         return deleteSucceeded;
     }
 }
