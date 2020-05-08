 package com.micronautics.aws;
 
 import akka.dispatch.Await;
 import akka.dispatch.Future;
 import akka.dispatch.Futures;
 import akka.dispatch.MessageDispatcher;
 import akka.util.Duration;
 import com.amazonaws.services.s3.model.PutObjectResult;
 import org.apache.commons.io.DirectoryWalker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.regex.Pattern;
 
 import static com.micronautics.aws.Model.*;
 import static com.micronautics.aws.Util.compareS3FileAge;
 import static com.micronautics.aws.Util.dtFmt;
 
 /** Uploads on multiple threads if Model.multithreadingEnabled is true */
 public class Uploader extends DirectoryWalker<File> {
     private Logger logger = LoggerFactory.getLogger(getClass());
     boolean overwrite;
     int treeRootStrLen;
     MessageDispatcher dispatcher = Main.system().dispatcher();
     private final ArrayList<Future<PutObjectResult>> futures = new ArrayList<Future<PutObjectResult>>();
 
     public Uploader(boolean overwrite) {
         super();
         this.overwrite = overwrite;
         s3 = new S3(credentials.accessKey(), credentials.secretKey());
     }
 
     public List<File> upload(File treeRoot) throws IOException {
         if (!Model.s3ObjectDataFetched) {
             Model.allNodes = s3.getAllObjectData(bucketName, ""); // get every object
             Model.s3ObjectDataFetched = true;
         }
         treeRootStrLen = treeRoot.getCanonicalPath().length();
        ArrayList<File> results = new ArrayList<File>();
         walk(treeRoot, results);
         if (multithreadingEnabled) {
             final Future<Iterable<PutObjectResult>> future = Futures.sequence(futures, dispatcher);
             try { // block until the Futures all complete
                 Await.result(future, Duration.Inf());
             } catch (Exception ex) {
                 System.err.println(ex.getMessage());
             }
         }
         return results;
     }
 
     protected boolean ignore(File file) {
         for (Pattern pattern : Model.ignoredPatterns) {
             //System.out.println("pattern=" + pattern.toString() + "; file name=" + file.getName());
             if (pattern.matcher(file.getName()).matches()) {
                 logger.debug("Uploader ignoring " + file.getName());
                 return true;
             }
         }
         return false;
     }
 
     @Override protected boolean handleDirectory(File directory, int depth, Collection results) {
         boolean ignore = ignore(directory);
         if (ignore)
             logger.debug("Uploader ignoring " + directory.getName());
         return !ignore;
     }
 
     @Override protected void handleFile(File file, int depth, Collection results) {
         try {
             String path = canonicalPath(file);
             int comparedAges = compareS3FileAge(file, path);
             //System.out.println("overwrite=" + overwrite + "; s3Older=" + s3Older + "; " + file.getAbsolutePath());
             if (ignore(file)) {
                 logger.debug("Uploader ignoring " + path);
                 return;
             }
             if (!overwrite)
               switch (comparedAges) {
                   case s3FileDoesNotExist:
                       logger.info("Uploading " + path + " to " + bucketName + " because it does not exist remotely.");
                       break;
 
                   case s3FileIsOlderThanLocal:
                       logger.info("Uploading '" + path + "' (" + dtFmt(file.lastModified()) + ") to '" + bucketName + "' because the remote copy is older.");
                       break;
 
                   case s3FileSameAgeAsLocal:
                       if (!overwrite) {
                           logger.debug("Uploader skipping " + path + " because it is the same age as the local copy and overwrite is disabled.");
                           return;
                       }
                       logger.debug("Uploading " + path + " even though it is the same age as the local copy because overwrite is enabled.");
                       break;
 
                   case s3FileNewerThanLocal:
                       logger.debug("Uploader skipping " + path + " because the local copy is older.");
                       return;
 
                   case s3FileDoesNotExistLocally:
                       logger.debug("Uploader cannot upload " + path + " because the local copy does not exist.");
                       return;
             }
             if (multithreadingEnabled) {
                 final Future<PutObjectResult> future = Futures.future(new UploadOne(path, file), dispatcher);
                 futures.add(future);
             } else
                 new UploadOne(path, file).call();
         } catch (IOException e) {
             System.err.println(e.getMessage());
         }
     }
 
     private String canonicalPath(File file) throws IOException {
         String path = file.getCanonicalPath().substring(treeRootStrLen).replace('\\', '/');
         if (path.startsWith("/") || path.startsWith("\\"))
             return path.substring(1);
         else
             return path;
     }
 }
 
 class UploadOne implements Callable<PutObjectResult> {
     private Logger logger = LoggerFactory.getLogger(getClass());
     private String bucketName;
     private String key;
     private File file;
 
     /** @param key backslashes are converted to forward slashes */
     public UploadOne(String key, File file) {
         this.bucketName = Model.bucketName;
         this.key = key.replace("\\", "/");
         this.file = file;
     }
 
     @Override
     public PutObjectResult call() {
         try {
             PutObjectResult result = s3.uploadFile(bucketName, key, file);
             logger.info(key + " uploaded.");
             return result;
         } catch (Exception e) {
             logger.warn(key + ": " + e.getMessage());
         }
         return new PutObjectResult();
     }
 }
