 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.apps.synchronize;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.io.BytesProgressWatcher;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.multithread.CreateObjectsEvent;
 import org.jets3t.service.multithread.DeleteObjectsEvent;
 import org.jets3t.service.multithread.DownloadObjectsEvent;
 import org.jets3t.service.multithread.DownloadPackage;
 import org.jets3t.service.multithread.GetObjectHeadsEvent;
 import org.jets3t.service.multithread.S3ServiceEventAdaptor;
 import org.jets3t.service.multithread.S3ServiceMulti;
 import org.jets3t.service.multithread.ServiceEvent;
 import org.jets3t.service.multithread.ThreadWatcher;
 import org.jets3t.service.security.AWSCredentials;
 import org.jets3t.service.security.EncryptionUtil;
 import org.jets3t.service.utils.ByteFormatter;
 import org.jets3t.service.utils.FileComparer;
 import org.jets3t.service.utils.FileComparerResults;
 import org.jets3t.service.utils.Mimetypes;
 import org.jets3t.service.utils.ObjectUtils;
 import org.jets3t.service.utils.TimeFormatter;
 
 /**
  * Console application to synchronize the local file system with Amazon S3.
  * For more information and help please see the 
  * <a href="http://jets3t.s3.amazonaws.com/applications/synchronize.html">Synchronize Guide</a>.
  * 
  * @author James Murty
  */
 public class Synchronize {
     public static final String APPLICATION_DESCRIPTION = "Synchronize/0.6.1";
     
     protected static final int REPORT_LEVEL_NONE = 0;
     protected static final int REPORT_LEVEL_ACTIONS = 1;
     protected static final int REPORT_LEVEL_DIFFERENCES = 2;
     protected static final int REPORT_LEVEL_ALL = 3;
     
     private S3Service s3Service = null;
     
     private boolean doAction = false; // Files will only be transferred if true. 
     private boolean isQuiet = false; // Report will only include summary of actions if true.
     private boolean isNoProgress = false; // Progress messages are not displayed if true.
     private boolean isForce = false; // Files will be overwritten when unchanged if true.
     private boolean isKeepFiles = false; // Files will not be replaced/deleted if true.
     private boolean isNoDelete = false; // Files will not be deleted if true, but may be replaced.
     private boolean isGzipEnabled = false; // Files will be gzipped prior to upload if true.
     private boolean isEncryptionEnabled = false; // Files will be encrypted prior to upload if true.
     private int reportLevel = REPORT_LEVEL_ALL;
     private String cryptoPassword = null;
 
     private final ByteFormatter byteFormatter = new ByteFormatter();
     private final TimeFormatter timeFormatter = new TimeFormatter();
     private int maxTemporaryStringLength = 0;
 
     
     /**
      * Constructs the application with a pre-initialised S3Service and the user-specified options.
      * 
      * @param s3Service     
      * a pre-initialised S3Service (including AWS Authorization credentials)
      * @param doAction
      * Files will only be transferred if true.
      * @param isQuiet       
      * Report will only include summary of actions if true.
      * @param isForce       
      * Files will be overwritten when unchanged if true.
      * @param isKeepFiles     
      * Files will not be replaced/deleted if true.
      * @param isNoDelete     
      * Files will not be deleted if true, but may be replaced.
      * @param isGzipEnabled 
      * Files will be gzipped prior to upload if true.
      * @param isEncryptionEnabled   
      * Files will be encrypted prior to upload if true.
      * @param reportLevel
      * The level or amount of reporting to perform. The default value is 
      * {@link #REPORT_LEVEL_ALL}.
      */
     public Synchronize(S3Service s3Service, boolean doAction, boolean isQuiet, boolean isNoProgress, boolean isForce, 
         boolean isKeepFiles, boolean isNoDelete, boolean isGzipEnabled, boolean isEncryptionEnabled, int reportLevel) 
     {
         this.s3Service = s3Service;
         this.doAction = doAction;
         this.isQuiet = isQuiet;
         this.isNoProgress = isNoProgress;
         this.isForce = isForce;
         this.isKeepFiles = isKeepFiles;
         this.isNoDelete = isNoDelete;
         this.isGzipEnabled = isGzipEnabled;
         this.isEncryptionEnabled = isEncryptionEnabled;
         this.reportLevel = reportLevel;
     }
     
 
     /**
      * Prepares a file to be uploaded to S3, creating an S3Object with the 
      * appropriate key and with some jets3t-specific metadata items set.
      * 
      * @param bucket    the bucket to create the object in 
      * @param targetKey the key name for the object
      * @param file      the file to upload to S3
      * @param aclString the ACL to apply to the uploaded object
      * 
      * @throws Exception
      */
     private S3Object prepareUploadObject(String targetKey, File file, String aclString, EncryptionUtil encryptionUtil) 
         throws Exception 
     {        
         S3Object newObject = ObjectUtils
             .createObjectForUpload(targetKey, file, encryptionUtil, isGzipEnabled, null);
         
         if ("PUBLIC_READ".equalsIgnoreCase(aclString)) {
             newObject.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);                        
         } else if ("PUBLIC_READ_WRITE".equalsIgnoreCase(aclString)) {
             newObject.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE);            
         } else if ("PRIVATE".equalsIgnoreCase(aclString)) {
             // Private is the default, no need to add an ACL
         } else {
             throw new Exception("Invalid value for ACL string: " + aclString);
         }
         
         return newObject;
     }
     
 
     private String formatTransferDetails(ThreadWatcher watcher) {
         String detailsText = "";
         long bytesPerSecond = watcher.getBytesPerSecond();
         detailsText = byteFormatter.formatByteSize(bytesPerSecond) + "/s";
 
         if (watcher.isTimeRemainingAvailable()) {
             if (detailsText.trim().length() > 0) {
                 detailsText += " - ";
             }
             long secondsRemaining = watcher.getTimeRemaining();
             detailsText += "ETA: " + timeFormatter.formatTime(secondsRemaining, false);
         }
         return detailsText;
     }
     
     private void printOutputLine(String line, int level) {
        if (isQuiet || reportLevel < level) {
             return;
         }
         
         String blanks = "";
         for (int i = line.length(); i < maxTemporaryStringLength; i++) {
             blanks += " ";
         }
         System.out.println(line + blanks);
         maxTemporaryStringLength = 0;
     }
     
     /**
      * Prints text to StdOut provided the isQuiet flag is not set.
      * 
      * @param line the text to print
      * @param isProgressMessage
      * if true, the line is printed followed by a carriage return such
      * that the next line output to the console will overwrite it.
      */
     private void printProgressLine(String line) {
         if (isQuiet || isNoProgress) {
             return;
         }
         
         String temporaryLine = "  " + line;                
         if (temporaryLine.length() > maxTemporaryStringLength) {
             maxTemporaryStringLength = temporaryLine.length();
         }
         String blanks = "";
         for (int i = temporaryLine.length(); i < maxTemporaryStringLength; i++) {
             blanks += " ";
         }
         System.out.print(temporaryLine + blanks + "\r");
     }
     
     /**
      * Copies the contents of a local directory to S3, storing them in the given root path.
      * <p>
      * A set of comparisons is used to determine exactly how the local files differ from the
      * contents of the S3 location, and files are transferred based on these comparisons and
      * options set by the user.
      * <p>
      * The following S3 Object properties are set when a file is uploaded:
      * <ul>
      * <li>The object's key name</li>
      * <li>Content-Length: The size of the uploaded file. This will be 0 for directories, and will
      *     differ from the original file if gzip or encryption options are set.</li>
      * <li>Content-Type: {@link Mimetypes#MIMETYPE_JETS3T_DIRECTORY} for directories, otherwise a
      *     mimetype determined by {@link Mimetypes#getMimetype} <b>unless</b> the gzip option is
      *     set, in which case the Content-Type is set to application/x-gzip.
      * </ul>
      * <p>
      * The following jets3t-specific metadata items are also set:
      * <ul>
      * <li>The local file's last-modified date, as {@link Constants#METADATA_JETS3T_LOCAL_FILE_DATE}</li>
      * <li>An MD5 hash of file data, as {@link S3Object#METADATA_HEADER_HASH_MD5}</li>
      * </ul>
      * 
      * @param disrepancyResults a set of comparisons of the local file system compared with S3
      * @param filesMap      a map of the local <code>File</code>s with '/'-delimited file paths as keys 
      * @param s3ObjectsMap  a map of <code>S3Object</code>s corresponding to local files with 
      *                      '/'-delimited file paths as keys
      * @param bucket        the bucket to put the objects in (will be created if necessary)
      * @param rootObjectPath    the root path where objects are put (will be created if necessary)
      * @param aclString     the ACL to apply to the uploaded object
      * 
      * @throws Exception
      */
     public void uploadLocalDirectoryToS3(FileComparerResults disrepancyResults, Map filesMap, 
         Map s3ObjectsMap, S3Bucket bucket, String rootObjectPath, String aclString) throws Exception 
     {        
         List objectsToUpload = new ArrayList();
         
         // Sort upload file candidates by path.
         ArrayList sortedFilesKeys = new ArrayList(filesMap.keySet());
         Collections.sort(sortedFilesKeys);
         
         EncryptionUtil encryptionUtil = null;
         if (isEncryptionEnabled) {
             String algorithm = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
                 .getStringProperty("crypto.algorithm", "PBEWithMD5AndDES");
             encryptionUtil = new EncryptionUtil(cryptoPassword, algorithm, EncryptionUtil.DEFAULT_VERSION);
         }
         
         // Iterate through local files and perform the necessary action to synchronise them with S3.
         Iterator fileKeyIter = sortedFilesKeys.iterator();
         while (fileKeyIter.hasNext()) {
             String keyPath = (String) fileKeyIter.next();
             File file = (File) filesMap.get(keyPath);
             
             String targetKey = keyPath;
             if (rootObjectPath.length() > 0) {
                 targetKey = rootObjectPath + Constants.FILE_PATH_DELIM + targetKey; 
             }
 
             if (disrepancyResults.onlyOnClientKeys.contains(keyPath)) {
                 printOutputLine("N " + keyPath, REPORT_LEVEL_ACTIONS);
                 objectsToUpload.add(prepareUploadObject(targetKey, file, aclString, encryptionUtil));
             } else if (disrepancyResults.updatedOnClientKeys.contains(keyPath)) {
                 printOutputLine("U " + keyPath, REPORT_LEVEL_ACTIONS);
                 objectsToUpload.add(prepareUploadObject(targetKey, file, aclString, encryptionUtil));
             } else if (disrepancyResults.alreadySynchronisedKeys.contains(keyPath)) {
                 if (isForce) {
                     printOutputLine("F " + keyPath, REPORT_LEVEL_ACTIONS);
                     objectsToUpload.add(prepareUploadObject(targetKey, file, aclString, encryptionUtil));
                 } else {
                     printOutputLine("- " + keyPath, REPORT_LEVEL_ALL);
                 }
             } else if (disrepancyResults.updatedOnServerKeys.contains(keyPath)) {
                 // This file has been updated on the server-side.
                 if (isKeepFiles) {
                     printOutputLine("r " + keyPath, REPORT_LEVEL_DIFFERENCES);                    
                 } else {
                     printOutputLine("R " + keyPath, REPORT_LEVEL_ACTIONS);
                     objectsToUpload.add(prepareUploadObject(targetKey, file, aclString, encryptionUtil));
                 }
             } else {
                 // Uh oh, program error here. The safest thing to do is abort!
                 throw new SynchronizeException("Invalid discrepancy comparison details for file " 
                     + file.getPath() 
                     + ". Sorry, this is a program error - aborting to keep your data safe");
             }
         }
                 
         // Upload New/Updated/Forced/Replaced objects to S3.
         if (doAction && objectsToUpload.size() > 0) {
             S3Object[] objects = (S3Object[]) objectsToUpload.toArray(new S3Object[objectsToUpload.size()]);
             (new S3ServiceMulti(s3Service, serviceEventAdaptor)).putObjects(bucket, objects);
             if (serviceEventAdaptor.wasErrorThrown()) {
                 Throwable thrown = serviceEventAdaptor.getErrorThrown();
                 if (thrown instanceof Exception) {
                     throw (Exception) thrown;
                 } else {
                     throw new Exception(thrown);
                 }
             }
         }
         
         // Delete objects on S3 that don't correspond with local files.
         List objectsToDelete = new ArrayList();
         Iterator serverOnlyIter = disrepancyResults.onlyOnServerKeys.iterator();
         while (serverOnlyIter.hasNext()) {
             String keyPath = (String) serverOnlyIter.next();
             S3Object s3Object = (S3Object) s3ObjectsMap.get(keyPath);
 
             if (isKeepFiles || isNoDelete) {
                 printOutputLine("d " + keyPath, REPORT_LEVEL_DIFFERENCES);                
             } else {
                 printOutputLine("D " + keyPath, REPORT_LEVEL_ACTIONS);
                 if (doAction) {
                     objectsToDelete.add(s3Object);
                 }
             }
         }
         if (objectsToDelete.size() > 0) {
             S3Object[] objects = (S3Object[]) objectsToDelete.toArray(new S3Object[objectsToDelete.size()]);
             (new S3ServiceMulti(s3Service, serviceEventAdaptor)).deleteObjects(bucket, objects);
             if (serviceEventAdaptor.wasErrorThrown()) {
                 Throwable thrown = serviceEventAdaptor.getErrorThrown();
                 if (thrown instanceof Exception) {
                     throw (Exception) thrown;
                 } else {
                     throw new Exception(thrown);
                 }
             }
         }
         
         printOutputLine( 
             "New files: " + disrepancyResults.onlyOnClientKeys.size() +
             ", Updated: " + disrepancyResults.updatedOnClientKeys.size() +
             (isKeepFiles?
                 ", Kept: " + 
                 (disrepancyResults.updatedOnServerKeys.size())                    
                 :                 
                 ", Reverted: " + disrepancyResults.updatedOnServerKeys.size()
                 ) +
             (isNoDelete || isKeepFiles?
                 ", Not Deleted: " + disrepancyResults.onlyOnServerKeys.size()
                 :
                 ", Deleted: " + disrepancyResults.onlyOnServerKeys.size()
                 ) +
             (isForce ?
                 ", Forced updates: " + disrepancyResults.alreadySynchronisedKeys.size() :
                 ", Unchanged: " + disrepancyResults.alreadySynchronisedKeys.size()
                 ), REPORT_LEVEL_NONE
             );
     }
         
     /**
      * Copies the contents of a root path in S3 to the local file system.
      * <p>
      * A set of comparisons is used to determine exactly how the S3 objects/files differ from the
      * local target, and files are transferred based on these comparisons and options set by the user.
      * <p>
      * If an object is gzipped (according to its Content-Type) and the gzip option is set, the object
      * is inflated. If an object is encrypted (according to the metadata item 
      * {@link Constants#METADATA_JETS3T_CRYPTO_ALGORITHM}) and the crypt option is set, the object 
      * is decrypted. If encrypted and/or gzipped objects are restored without the corresponding option 
      * being set, the user will be responsible for inflating or decrypting the data.
      * <p>
      * <b>Note</b>: If a file was backed-up with both encryption and gzip options it cannot be 
      * restored with only the gzip option set, as files are gzipped prior to being encrypted and cannot
      * be inflated without first being decrypted.
      * 
      * @param disrepancyResults a set of comparisons of the local file system compared with S3
      * @param filesMap      a map of the local <code>File</code>s with '/'-delimited file paths as keys 
      * @param s3ObjectsMap  a map of <code>S3Object</code>s corresponding to local files with 
      *                      '/'-delimited file paths as keys
      * @param rootObjectPath    the root path in S3 where backed-up objects were stored
      * @param bucket        the bucket into which files were backed up
      * @throws Exception
      */
     public void restoreFromS3ToLocalDirectory(FileComparerResults disrepancyResults, Map filesMap, 
         Map s3ObjectsMap, String rootObjectPath, File localDirectory, S3Bucket bucket) throws Exception 
     {
         List downloadPackagesList = new ArrayList();
         
         ArrayList sortedS3ObjectKeys = new ArrayList(s3ObjectsMap.keySet());
         Collections.sort(sortedS3ObjectKeys);
         
         // Download objects to local files/directories.
         Iterator s3KeyIter = sortedS3ObjectKeys.iterator();
         while (s3KeyIter.hasNext()) {
             String keyPath = (String) s3KeyIter.next();
             S3Object s3Object = (S3Object) s3ObjectsMap.get(keyPath);
             File fileTarget = new File(localDirectory, keyPath);
             
             // Create local directories corresponding to objects flagged as dirs.
             if (Mimetypes.MIMETYPE_JETS3T_DIRECTORY.equals(s3Object.getContentType())) {
                 if (doAction) {
                     fileTarget.mkdirs();                    
                 }
             }            
             
             if (disrepancyResults.onlyOnServerKeys.contains(keyPath)) {
                 printOutputLine("N " + keyPath, REPORT_LEVEL_ACTIONS);
                 DownloadPackage downloadPackage = ObjectUtils.createPackageForDownload(
                     s3Object, fileTarget, isGzipEnabled, isEncryptionEnabled, cryptoPassword);
                 if (downloadPackage != null) {
                     downloadPackagesList.add(downloadPackage);
                 }
             } else if (disrepancyResults.updatedOnServerKeys.contains(keyPath)) {
                 printOutputLine("U " + keyPath, REPORT_LEVEL_ACTIONS);
                 DownloadPackage downloadPackage = ObjectUtils.createPackageForDownload(
                     s3Object, fileTarget, isGzipEnabled, isEncryptionEnabled, cryptoPassword);
                 if (downloadPackage != null) {
                     downloadPackagesList.add(downloadPackage);
                 }
             } else if (disrepancyResults.alreadySynchronisedKeys.contains(keyPath)) {
                 if (isForce) {
                     printOutputLine("F " + keyPath, REPORT_LEVEL_ACTIONS);
                     DownloadPackage downloadPackage = ObjectUtils.createPackageForDownload(
                         s3Object, fileTarget, isGzipEnabled, isEncryptionEnabled, cryptoPassword);
                     if (downloadPackage != null) {
                         downloadPackagesList.add(downloadPackage);
                     }
                 } else {
                     printOutputLine("- " + keyPath, REPORT_LEVEL_ALL);
                 }
             } else if (disrepancyResults.updatedOnClientKeys.contains(keyPath)) {
                 // This file has been updated on the client-side.
                 if (isKeepFiles) {
                     printOutputLine("r " + keyPath, REPORT_LEVEL_DIFFERENCES);                    
                 } else {
                     printOutputLine("R " + keyPath, REPORT_LEVEL_ACTIONS);
                     DownloadPackage downloadPackage = ObjectUtils.createPackageForDownload(
                         s3Object, fileTarget, isGzipEnabled, isEncryptionEnabled, cryptoPassword);
                     if (downloadPackage != null) {
                         downloadPackagesList.add(downloadPackage);
                     }
                 }
             } else {
                 // Uh oh, program error here. The safest thing to do is abort!
                 throw new SynchronizeException("Invalid discrepancy comparison details for S3 object " 
                     + keyPath
                     + ". Sorry, this is a program error - aborting to keep your data safe");
             }
         }
         
         // Download New/Updated/Forced/Replaced objects from S3.
         if (doAction && downloadPackagesList.size() > 0) {
             DownloadPackage[] downloadPackages = (DownloadPackage[]) 
                 downloadPackagesList.toArray(new DownloadPackage[downloadPackagesList.size()]);
             (new S3ServiceMulti(s3Service, serviceEventAdaptor)).downloadObjects(bucket, downloadPackages);
             if (serviceEventAdaptor.wasErrorThrown()) {
                 Throwable thrown = serviceEventAdaptor.getErrorThrown();
                 if (thrown instanceof Exception) {
                     throw (Exception) thrown;
                 } else {
                     throw new Exception(thrown);
                 }
             }
         }
 
         // Delete local files that don't correspond with S3 objects.
         ArrayList dirsToDelete = new ArrayList();
         Iterator clientOnlyIter = disrepancyResults.onlyOnClientKeys.iterator();
         while (clientOnlyIter.hasNext()) {
             String keyPath = (String) clientOnlyIter.next();
             File file = (File) filesMap.get(keyPath);
             
             if (isKeepFiles || isNoDelete) {
                 printOutputLine("d " + keyPath, REPORT_LEVEL_DIFFERENCES);                
             } else {
                 printOutputLine("D " + keyPath, REPORT_LEVEL_ACTIONS);
                 if (doAction) {
                     if (file.isDirectory()) {
                         // Delete directories later, as they may still have files 
                         // inside until this loop completes.
                         dirsToDelete.add(file);
                     } else {
                         file.delete();
                     }
                 }
             }
         }
         Iterator dirIter = dirsToDelete.iterator();
         while (dirIter.hasNext()) {
             File dir = (File) dirIter.next();
             dir.delete();
         }
         
         printOutputLine( 
             "New files: " + disrepancyResults.onlyOnServerKeys.size() +
             ", Updated: " + disrepancyResults.updatedOnServerKeys.size() +
             (isKeepFiles? 
                 ", Kept: " + 
                 (disrepancyResults.updatedOnClientKeys.size())                    
                 : 
                 ", Reverted: " + disrepancyResults.updatedOnClientKeys.size()
                 ) +
             (isNoDelete || isKeepFiles?
                 ", Not Deleted: " + disrepancyResults.onlyOnClientKeys.size()
                 :
                 ", Deleted: " + disrepancyResults.onlyOnClientKeys.size()
                 ) +
             (isForce ?
                 ", Forced updates: " + disrepancyResults.alreadySynchronisedKeys.size() :
                 ", Unchanged: " + disrepancyResults.alreadySynchronisedKeys.size()
                 ), REPORT_LEVEL_NONE
             );
     }
     
     /**
      * Runs the application, performing the action specified on the given S3 and local directory paths.
      * 
      * @param s3Path    
      * the path in S3 (including the bucket name) to which files are backed-up, or from which files are restored.
      * @param fileList
      * a list one or more of File objects for Uploads, or a single target directory for Downloads.
      * @param actionCommand     
      * the action to perform, UP(load) or DOWN(load)
      * @param cryptoPassword      
      * if non-null, an {@link EncryptionUtil} object is created with the provided password to encrypt or decrypt files.
      * @param aclString 
      * the ACL to apply to the uploaded object
      * 
      * @throws Exception
      */
     public void run(String s3Path, List fileList, String actionCommand, String cryptoPassword, 
         String aclString) throws Exception 
     {
         String bucketName = null;
         String objectPath = "";        
         int slashIndex = s3Path.indexOf(Constants.FILE_PATH_DELIM);
         if (slashIndex >= 0) {
             // We have a bucket name and an object path. 
             bucketName = s3Path.substring(0, slashIndex);
             objectPath = s3Path.substring(slashIndex + 1, s3Path.length());
         } else {
             // We only have a bucket name.
             bucketName = s3Path;
         }
         
         // Describe the action that will be performed.
         if ("UP".equals(actionCommand)) {
             String uploadPathSummary = null;
             
             if (fileList.size() > 3) {            
                 int dirsCount = 0;
                 int filesCount = 0;
                 
                 Iterator pathIter = fileList.iterator();
                 while (pathIter.hasNext()) {
                     File path = (File) pathIter.next();
                     if (path.isDirectory()) {
                         dirsCount++;
                     } else {
                         filesCount++;
                     }
                 }
                 uploadPathSummary =  
                     "[" 
                     + dirsCount + (dirsCount == 1 ? " directory" : " directories")
                     + ", " + filesCount + (filesCount == 1 ? " file" : " files") + "]";
             } else {
                 uploadPathSummary = fileList.toString();
             }
             
             printOutputLine("UP "
                 + (doAction ? "" : "[No Action] ")
                 + "Local " + uploadPathSummary + " => S3[" + s3Path + "]", REPORT_LEVEL_NONE);              
         } else if ("DOWN".equals(actionCommand)) {
             if (fileList.size() != 1) {
                 throw new SynchronizeException("Only one target directory is allowed for downloads");
             }
             printOutputLine("DOWN "
                 + (doAction ? "" : "[No Action] ")
                 + "S3[" + s3Path + "] => Local" + fileList, REPORT_LEVEL_NONE);
         } else {
             throw new SynchronizeException("Action string must be 'UP' or 'DOWN'");
         }        
         
         this.cryptoPassword = cryptoPassword;
                 
         S3Bucket bucket = null;
         try {
             if (!s3Service.isBucketAccessible(bucketName)) {
                 // Create/connect to the S3 bucket.
                 bucket = s3Service.createBucket(bucketName);
             } else {
                 bucket = new S3Bucket(bucketName);
             }
         } catch (Exception e) {
             throw new SynchronizeException("Unable to create/connect to S3 bucket: " + bucketName, e);
         }
                         
         boolean storeEmptyDirectories = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
             .getBoolProperty("uploads.storeEmptyDirectories", true);
 
         // Compare contents of local directory with contents of S3 path and identify any disrepancies.
         printProgressLine("Listing files in local file system");
         Map filesMap = null;        
         if ("UP".equals(actionCommand)) {            
             filesMap = FileComparer.buildFileMap((File[]) fileList.toArray(new File[fileList.size()]), storeEmptyDirectories);
         } else if ("DOWN".equals(actionCommand)) {
             filesMap = FileComparer.buildFileMap((File) fileList.get(0), null, true);
         }
 
         printProgressLine("Listing objects in S3");
         Map s3ObjectsMap = FileComparer.buildS3ObjectMap(s3Service, bucket, objectPath, serviceEventAdaptor);
         if (serviceEventAdaptor.wasErrorThrown()) {
             throw new Exception("Unable to build map of S3 Objects", serviceEventAdaptor.getErrorThrown());
         }
         
         // Calculate total files size.
         final long filesSizeTotal[] = new long[] { 0 }; 
         File[] files = (File[]) filesMap.values().toArray(new File[filesMap.size()]);
         for (int i = 0; i < files.length; i++) {
             filesSizeTotal[0] += files[i].length();
         }
         
         // Monitor generation of MD5 hashes, and provide feedback via progress messages.
         BytesProgressWatcher progressWatcher = new BytesProgressWatcher(filesSizeTotal[0]) {
             public void updateBytesTransferred(long byteCount) {
                 super.updateBytesTransferred(byteCount);
                 
                 int percentage = (int)((double)getBytesTransferred() * 100 / getBytesToTransfer());
                 printProgressLine("Comparing files: " + percentage + "% of " +
                     byteFormatter.formatByteSize(filesSizeTotal[0]));
             }
         };
 
         printProgressLine("Comparing S3 contents with local system");        
         FileComparerResults discrepancyResults = 
             FileComparer.buildDiscrepancyLists(filesMap, s3ObjectsMap, progressWatcher);
 
         // Perform the requested action on the set of disrepancies.
         if ("UP".equals(actionCommand)) {  
             if (objectPath.length() > 0 && doAction && storeEmptyDirectories) {
                 // Create the target path in S3.
                 try {
                     String targetDirs[] = objectPath.split(Constants.FILE_PATH_DELIM);
                     StringBuffer currentDirPathBuf = new StringBuffer();
                     for (int i = 0; i < targetDirs.length; i++) {
                         currentDirPathBuf.append(targetDirs[i]);
                         
                         S3Object dirObject = new S3Object(currentDirPathBuf.toString());
                         dirObject.setContentType(Mimetypes.MIMETYPE_JETS3T_DIRECTORY);
                         s3Service.putObject(bucket, dirObject);
                         currentDirPathBuf.append(Constants.FILE_PATH_DELIM);
                     }
                 } catch (Exception e) {
                     throw new SynchronizeException("Unable to create S3 path: " + objectPath, e);
                 }
             }            
             
             uploadLocalDirectoryToS3(discrepancyResults, filesMap, s3ObjectsMap, bucket, objectPath, aclString);
         } else if ("DOWN".equals(actionCommand)) {
             restoreFromS3ToLocalDirectory(discrepancyResults, filesMap, s3ObjectsMap, objectPath, (File) fileList.get(0), bucket);
         }
     }
     
     S3ServiceEventAdaptor serviceEventAdaptor = new S3ServiceEventAdaptor() {
         private void displayProgressStatus(String prefix, ThreadWatcher watcher) {
             String progressMessage = prefix + watcher.getCompletedThreads() + "/" + watcher.getThreadCount();                    
             
             // Show percentage of bytes transferred, if this info is available.
             if (watcher.isBytesTransferredInfoAvailable()) {
                 String bytesTotalStr = byteFormatter.formatByteSize(watcher.getBytesTotal());
                 long percentage = (int) 
                     (((double)watcher.getBytesTransferred() / watcher.getBytesTotal()) * 100);
                 
                 String detailsText = formatTransferDetails(watcher);
                 
                 progressMessage += " - " + percentage + "% of " + bytesTotalStr 
                     + (detailsText.length() > 0 ? " (" + detailsText + ")" : "");
             } else {
                 long percentage = (int) 
                     (((double)watcher.getCompletedThreads() / watcher.getThreadCount()) * 100);
                 
                 progressMessage += " - " + percentage + "%";                
             }
             printProgressLine(progressMessage);                
         }
         
         public void s3ServiceEventPerformed(CreateObjectsEvent event) {
             super.s3ServiceEventPerformed(event);
             if (ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                 displayProgressStatus("Upload: ", event.getThreadWatcher());                    
             }
         }
         
         public void s3ServiceEventPerformed(DownloadObjectsEvent event) {
             super.s3ServiceEventPerformed(event);
             if (ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                 displayProgressStatus("Download: ", event.getThreadWatcher());                    
             }
         }
         
         public void s3ServiceEventPerformed(GetObjectHeadsEvent event) {
             super.s3ServiceEventPerformed(event);
             if (ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {                
                 displayProgressStatus("Retrieving object details from S3: ", event.getThreadWatcher());
             }
         }
         
         public void s3ServiceEventPerformed(DeleteObjectsEvent event) {
             super.s3ServiceEventPerformed(event);
             if (ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {                
                 displayProgressStatus("Deleting objects in S3: ", event.getThreadWatcher());
             }
         }
     };
 
     /**
      * Prints usage/help information and forces the application to exit with errorcode 1. 
      */
     private static void printHelpAndExit(boolean fullHelp) {
         System.out.println();
         System.out.println("Usage: Synchronize [options] UP <S3Path> <File/Directory> (<File/Directory>...)");
         System.out.println("   or: Synchronize [options] DOWN <S3Path> <DownloadDirectory>");
         System.out.println("");
         System.out.println("UP      : Synchronize the contents of the Local Directory with S3.");
         System.out.println("DOWN    : Synchronize the contents of S3 with the Local Directory");
         System.out.println("S3Path  : A path to the resource in S3. This must include at least the");
         System.out.println("          bucket name, but may also specify a path inside the bucket.");
         System.out.println("          E.g. <bucketName>/Backups/Documents/20060623");
         System.out.println("File/Directory : A file or directory on your computer to upload");
         System.out.println("DownloadDirectory : A directory on your computer where downloaded files");
         System.out.println("          will be stored");
         System.out.println();
         System.out.println("A property file with the name 'synchronize.properties' must be available in the");
         System.out.println("classpath and contains the following properties:");
         System.out.println("          accesskey : Your AWS Access Key (Required)");
         System.out.println("          secretkey : Your AWS Secret Key (Required)");
         System.out.println("          password  : Encryption password (only required when using crypto)");
         System.out.println("          acl       : ACL permissions for uploads (optional)");
         System.out.println("");
         System.out.println("For more help : Synchronize --help");
         if (!fullHelp)
             System.exit(1);        
 
         System.out.println("");
         System.out.println("Options");
         System.out.println("-------");
         System.out.println("-h | --help");
         System.out.println("   Displays this help message.");
         System.out.println("");
         System.out.println("-n | --noaction");
         System.out.println("   No action taken. No files will be changed locally or on S3, instead");
         System.out.println("   a report will be generating showing what will happen if the command");
         System.out.println("   is run without the -n option.");
         System.out.println("");
         System.out.println("-q | --quiet");
         System.out.println("   Runs quietly, without reporting on each action performed or displaying");
         System.out.println("   progress messages. The summary is still displayed.");
         System.out.println("");
         System.out.println("-p | --noprogress");
         System.out.println("   Runs somewhat quietly, without displaying progress messages.");
         System.out.println("   The action report and overall summary are still displayed.");
         System.out.println("");
         System.out.println("-f | --force");
         System.out.println("   Force tool to perform synchronization even when files are up-to-date.");
         System.out.println("   This may be useful if you need to update metadata or timestamps in S3.");
         System.out.println("");
         System.out.println("-k | --keepfiles");
         System.out.println("   Keep outdated files on destination instead of reverting/removing them.");
         System.out.println("   This option cannot be used with --nodelete.");
         System.out.println("");
         System.out.println("-d | --nodelete");
         System.out.println("   Keep files on destination that have been removed from the source. This");
         System.out.println("   option is similar to --keepfiles except that files may be reverted.");
         System.out.println("   This option cannot be used with --keepfiles.");
         System.out.println("");
         System.out.println("-g | --gzip");
         System.out.println("   Compress (GZip) files when backing up and Decompress gzipped files");
         System.out.println("   when restoring.");
         System.out.println("");
         System.out.println("-c | --crypto");
         System.out.println("   Encrypt files when backing up and decrypt encrypted files when restoring. If");
         System.out.println("   this option is specified the properties must contain a password.");
         System.out.println("");
         System.out.println("--properties <filename>");
         System.out.println("   Load the synchronizer app properties from the given file instead of from");
         System.out.println("   a synchronizer.properties file in the classpath.");
         System.out.println("");
         System.out.println("--acl <ACL string>");
         System.out.println("   Specifies the Access Control List setting to apply. This value must be one");
         System.out.println("   of: PRIVATE, PUBLIC_READ, PUBLIC_READ_WRITE. This setting will override any");
         System.out.println("   acl property specified in the synchronize.properties file");
         System.out.println("");
         System.out.println("--reportlevel <Level>");
         System.out.println("   A number that specifies how much report information will be printed:");
         System.out.println("   0 - no report items will be printed (the summary will still be printed)");
         System.out.println("   1 - only actions are reported             [Prefixes N, U, D, R, F]");
         System.out.println("   2 - differences and actions are reported  [Prefixes N, U, D, R, F, d, r]");
         System.out.println("   3 - DEFAULT: all items are reported       [Prefixes N, U, D, R, F, d, r, -]");
         System.out.println("");
         System.out.println("Report");
         System.out.println("------");
         System.out.println("Report items are printed on a single line with an action flag followed by");
         System.out.println("the relative path of the file or S3 object. The flag meanings are...");
         System.out.println("N: A new file/object will be created");
         System.out.println("U: An existing file/object has changed and will be updated");
         System.out.println("D: A file/object existing on the target does not exist on the source and");
         System.out.println("   will be deleted.");
         System.out.println("d: A file/object existing on the target does not exist on the source but");
         System.out.println("   because the --keepfiles or --nodelete option was set it was not deleted.");
         System.out.println("R: An existing file/object has changed more recently on the target than on the");
         System.out.println("   source. The target version will be reverted to the older source version");
         System.out.println("r: An existing file/object has changed more recently on the target than on the");
         System.out.println("   source but because the --keepfiles option was set it was not reverted.");
         System.out.println("-: A file is identical between the local system and S3, no action is necessary.");
         System.out.println("F: A file identical locally and in S3 was updated due to the Force option.");
         System.out.println();
         System.exit(1);        
     }
     
     /**
      * Runs this application from the console, accepts and checks command-line parameters and runs an
      * upload or download operation when all the necessary parameters are provided.
      * @throws Exception
      */
     public static void main(String args[]) throws Exception {
         String propertiesFileName = "synchronize.properties";
         Jets3tProperties properties = null;
 
         // Required arguments
         String actionCommand = null;
         String s3Path = null;
         int reqArgCount = 0;
         List fileList = new ArrayList();
         
         // Options
         boolean doAction = true;
         boolean isQuiet = false;
         boolean isNoProgress = false;
         boolean isForce = false;
         boolean isKeepFiles = false;
         boolean isNoDelete = false;
         boolean isGzipEnabled = false;
         boolean isEncryptionEnabled = false;
         String aclString = null;
         int reportLevel = REPORT_LEVEL_ALL;
                 
         // Parse arguments.
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
             if (arg.startsWith("-")) {
                 // Argument is an option.
                 if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
                     printHelpAndExit(true);
                 } else if (arg.equalsIgnoreCase("-n") || arg.equalsIgnoreCase("--noaction")) {
                     doAction = false; 
                 } else if (arg.equalsIgnoreCase("-q") || arg.equalsIgnoreCase("--quiet")) {
                     isQuiet = true; 
                 } else if (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("--noprogress")) {
                     isNoProgress = true; 
                 } else if (arg.equalsIgnoreCase("-f") || arg.equalsIgnoreCase("--force")) {
                     isForce = true; 
                 } else if (arg.equalsIgnoreCase("-k") || arg.equalsIgnoreCase("--keepfiles")) {
                     isKeepFiles = true; 
                 } else if (arg.equalsIgnoreCase("-d") || arg.equalsIgnoreCase("--nodelete")) {
                     isNoDelete = true; 
                 } else if (arg.equalsIgnoreCase("-g") || arg.equalsIgnoreCase("--gzip")) {
                     isGzipEnabled = true; 
                 } else if (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--crypto")) {
                     isEncryptionEnabled = true; 
                 } else if (arg.equalsIgnoreCase("--properties")) {
                     if (i + 1 < args.length) {
                         // Read the Synchronize properties file from the specified file            
                         i++;
                         propertiesFileName = args[i];
                         File propertiesFile = new File(propertiesFileName);
                         if (!propertiesFile.canRead()) {
                             System.err.println("ERROR: The properties file " + propertiesFileName + " could not be found");
                             System.exit(2);                        
                         }
                         properties = Jets3tProperties.getInstance(
                             new FileInputStream(propertiesFileName), propertiesFile.getName());                        
                     } else {
                         System.err.println("ERROR: --properties option must be followed by a file path");
                         printHelpAndExit(false);                        
                     }
                 } else if (arg.equalsIgnoreCase("--acl")) {
                     if (i + 1 < args.length) {
                         // Read the acl setting string             
                         i++;
                         aclString = args[i];
                         
                         if (!"PUBLIC_READ".equalsIgnoreCase(aclString) 
                             && !"PUBLIC_READ_WRITE".equalsIgnoreCase(aclString) 
                             && !"PRIVATE".equalsIgnoreCase(aclString)) 
                         {
                             System.err.println("ERROR: Acess Control List setting \"acl\" must have one of the values "
                                 + "PRIVATE, PUBLIC_READ, PUBLIC_READ_WRITE");
                             printHelpAndExit(false);                        
                         }                        
                     } else {
                         System.err.println("ERROR: --acl option must be followed by an ACL string");
                         printHelpAndExit(false);                        
                     }
                 } else if (arg.equalsIgnoreCase("--reportlevel")) {
                     if (i + 1 < args.length) {
                         // Read the report level integer
                         i++;
                         try {
                             reportLevel = Integer.parseInt(args[i]);
                             
                             if (reportLevel < 0 || reportLevel > 3) { 
                                 System.err.println("ERROR: Report Level setting \"reportlevel\" must have one of the values "
                                     + "0 (no reporting), 1 (actions only), 2 (differences only), 3 (DEFAULT - all reporting)");
                                 printHelpAndExit(false);                        
                             }                                                        
                         } catch (NumberFormatException e) {
                             System.err.println("ERROR: --reportlevel option must be followed by 0, 1, 2 or 3");
                             printHelpAndExit(false);                                                        
                         }
                     } else {
                         System.err.println("ERROR: --reportlevel option must be followed by 0, 1, 2 or 3");
                         printHelpAndExit(false);
                     }
                 } else {
                     System.err.println("ERROR: Invalid option: " + arg);
                     printHelpAndExit(false);
                 }
             } else {
                 // Argument is one of the required parameters.
                 if (reqArgCount == 0) {
                     actionCommand = arg.toUpperCase(Locale.getDefault());
                     if (!"UP".equals(actionCommand) && !"DOWN".equals(actionCommand)) {
                         System.err.println("ERROR: Invalid action command " + actionCommand 
                             + ". Valid values are 'UP' or 'DOWN'");
                         printHelpAndExit(false);
                     }                    
                 } else if (reqArgCount == 1) {
                     s3Path = arg;
                 } else if (reqArgCount > 1) {
                     File file = new File(arg);
                     
                     if ("DOWN".equals(actionCommand)) {
                         if (reqArgCount > 2) {
                             System.err.println("ERROR: Only one target directory may be specified"
                                 + " for " + actionCommand); 
                             printHelpAndExit(false);
                         }
                        if (!file.canRead() || !file.isDirectory()) {
                            if (!file.mkdirs()) {
                                System.err.println("ERROR: Cannot create target download directory : " 
                                    + file);                                
                            }
                         }         
                     } else {
                         if (!file.canRead()) {
                             if (properties != null && properties.getBoolProperty("upload.ignoreMissingPaths", false)) {
                                 System.err.println("WARN: Ignoring missing upload path: " + file);
                                 continue;
                             } else {
                                 System.err.println("ERROR: Cannot read upload file/directory: " 
                                     + file
                                     + "\n       To ignore missing paths set the property upload.ignoreMissingPaths");
                                 printHelpAndExit(false);                            
                             }
                         }
                     }
                     fileList.add(file);
                 }
                 reqArgCount++;
             }
         }
         
         if (fileList.size() < 1) {
             // Missing one or more required parameters.
             System.err.println("ERROR: Missing required file path(s)");
             printHelpAndExit(false);
         }
         
         if (isKeepFiles && isNoDelete) {
             // Incompatible options.
             System.err.println("ERROR: Options --keepfiles and --nodelete cannot be used at the same time");
             printHelpAndExit(false);            
         }
         
         if (properties == null) {        
             // Read the Synchronize properties file from the classpath            
             properties = Jets3tProperties.getInstance(propertiesFileName);
             if (!properties.isLoaded()) {
                 System.err.println("ERROR: The properties file " + propertiesFileName + " could not be found in the classpath");
                 System.exit(2);                        
             }
         }
                 
         // Ensure the Synchronize properties file contains everything we need.
         if (!properties.containsKey("accesskey")) {
             System.err.println("ERROR: The properties file " + propertiesFileName + " must contain the property: accesskey");
             System.exit(2);            
         } else if (!properties.containsKey("secretkey")) {
             System.err.println("ERROR: The properties file " + propertiesFileName + " must contain the property: secretkey");
             System.exit(2);                        
         } else if (isEncryptionEnabled && !properties.containsKey("password")) {
             System.err.println("ERROR: You are using encryption, so the properties file " + propertiesFileName + " must contain the property: password");
             System.exit(2);                        
         }
         
         // Load the AWS credentials from encrypted file.
         AWSCredentials awsCredentials = new AWSCredentials(
             properties.getStringProperty("accesskey", null), 
             properties.getStringProperty("secretkey", null));       
         
         if (aclString == null) {
             aclString = properties.getStringProperty("acl", "PRIVATE");
         }
         if (!"PUBLIC_READ".equalsIgnoreCase(aclString) 
             && !"PUBLIC_READ_WRITE".equalsIgnoreCase(aclString) 
             && !"PRIVATE".equalsIgnoreCase(aclString)) 
         {
             System.err.println("ERROR: Acess Control List setting \"acl\" must have one of the values "
                 + "PRIVATE, PUBLIC_READ, PUBLIC_READ_WRITE");
             System.exit(2);                                    
         }
          
         // Perform the UPload/DOWNload.
         Synchronize client = new Synchronize(
             new RestS3Service(awsCredentials, APPLICATION_DESCRIPTION, null),
             doAction, isQuiet, isNoProgress, isForce, isKeepFiles, isNoDelete, 
             isGzipEnabled, isEncryptionEnabled, reportLevel);
         client.run(s3Path, fileList, actionCommand, 
             properties.getStringProperty("password", null), aclString);
     }
         
 }
