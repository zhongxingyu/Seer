 package jp.dqneo.amazons3;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 /**
 * main class of Uploader
  *
  */
 public class Uploader {
 
     private static final int NUM_THREADS = 100;
 
     private static String endpoint = "https://s3-ap-northeast-1.amazonaws.com";
 
     private static AmazonS3 s3;
 
     private static int count_success = 0;
     private static int count_failure = 0;
 
     private static Timer timer;
 
 
     /**
      * @param args basedir , bucketName
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
 
         File baseDir = checkBaseDir(args[0]);
 
         if (args.length < 2 ||   args[1] == "") {
             throw new IllegalArgumentException("bucketName is not given");
         }
 
         String bucketName = args[1];
 
         s3 = new AmazonS3Client(new PropertiesCredentials(
                 Uploader.class
                         .getResourceAsStream("AwsCredentials.properties")));
 
         s3.setEndpoint(endpoint);
 
 
         ArrayList<File> files = FileFinder.find(baseDir);
         System.out.printf("===== [%d] files foud ========\n", files.size());
 
         uploadFiles(bucketName, baseDir, files);
         end(files.size());
     }
 
     private static File checkBaseDir(String path) throws Exception {
         File dir  =new File(path);
         if (!  dir.isDirectory()) {
             throw new Exception("directory not found:" + path);
         }
         return dir;
     }
 
     public static void uploadFiles(String bucketName, File baseDir, ArrayList<File> files) throws Exception {
 
         Collection<Task> tasks = new ArrayList<Task>();
 
 
         int index = 1;
         for(File file :files) {
             tasks.add(new Task(new UploadableFile(s3, bucketName, baseDir, file), index++));
         }
 
         System.out.println("===== Start uploading ========");
         timer = new Timer();
 
         ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
 
         //maybe we'd better try-catch below?
         List<Future<Boolean>> list = threadPool.invokeAll(tasks);
 
         for (Future<Boolean> fut : list) {
             if(fut.get()) {
                 count_success++;
             } else {
                 count_failure++;
             }
         }
         threadPool.shutdown();
 
     }
 
     /**
     * @param countFiles
     */
     public static void end(int countFiles) {
         long elapsedTime = timer.getElapsedTime();
 
         System.out.println("Number of threas - "    + NUM_THREADS);
         System.out.println("TOTAL SUCCESS - " + count_success);
         System.out.println("TOTAL FAILURE - " + count_failure);
         System.out.println("Total time - "    + elapsedTime + " ms");
         System.out.println("Millsec per file - " + ( elapsedTime / countFiles));
     }
 
 }
 
