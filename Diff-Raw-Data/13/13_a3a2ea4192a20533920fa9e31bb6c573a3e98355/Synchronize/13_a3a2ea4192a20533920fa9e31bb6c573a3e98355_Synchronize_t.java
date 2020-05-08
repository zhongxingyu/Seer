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
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.acl.AccessControlList;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.io.GZipDeflatingInputStream;
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
 import org.jets3t.service.utils.ServiceUtils;
 import org.jets3t.service.utils.TimeFormatter;
 
 /**
  * Console application to synchronize the local file system with Amazon S3.
  * For more information and help please see the 
  * <a href="http://jets3t.s3.amazonaws.com/applications/synchronize.html">Synchronize Guide</a>.
  * 
  * @author James Murty
  */
 public class Synchronize {
     public static final String APPLICATION_DESCRIPTION = "Synchronize/0.5.0";
     
     private S3Service s3Service = null;
     
     private boolean doAction = false; // Files will only be transferred if true. 
     private boolean isQuiet = false; // Report will only include summary of actions if true.
     private boolean isForce = false; // Files will be overwritten when unchanged if true.
     private boolean isKeepFiles = false; // Files will not be replaced/deleted if true.
     private boolean isGzipEnabled = false; // Files will be gzipped prior to upload if true.
     private boolean isEncryptionEnabled = false; // Files will be encrypted prior to upload if true.
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
      * @param isGzipEnabled 
      * Files will be gzipped prior to upload if true.
      * @param isEncryptionEnabled   
      * Files will be encrypted prior to upload if true.
      */
     public Synchronize(S3Service s3Service, boolean doAction, boolean isQuiet, boolean isForce, 
         boolean isKeepFiles, boolean isGzipEnabled, boolean isEncryptionEnabled) 
     {
         this.s3Service = s3Service;
         this.doAction = doAction;
         this.isQuiet = isQuiet;
         this.isForce = isForce;
         this.isKeepFiles = isKeepFiles;
         this.isGzipEnabled = isGzipEnabled;
         this.isEncryptionEnabled = isEncryptionEnabled;
     }
     
     /**
      * Prepares a file prior to upload by encrypting and/or gzipping it according to the
      * options specified by the user.
      * 
      * @param originalFile  the file to prepare for upload.
      * @param newObject     the object that will be created in S3 to store the file.
      * @return  the original file if no encryption/gzipping options are set, otherwise a 
      *          temporary file with encrypted and/or gzipped data from the original file. 
      * @throws Exception    exceptions could include IO failures, gzipping and encryption failures.
      */
     private File prepareUploadFile(final File originalFile, final S3Object newObject, 
         EncryptionUtil encryptionUtil) throws Exception 
     {
         if (!isGzipEnabled && !isEncryptionEnabled) {
             // No file pre-processing required.
             return originalFile;
         }
         
         // Create a temporary file to hold data transformed from the original file. 
         final File tempUploadFile = File.createTempFile("jets3t-Synchronize",".tmp");        
         tempUploadFile.deleteOnExit();
 
         // Transform data from original file, gzipping or encrypting as specified in user's options.
         OutputStream outputStream = null;
         InputStream inputStream = null;
         
         try {
             inputStream = new BufferedInputStream(new FileInputStream(originalFile));       
             outputStream = new BufferedOutputStream(new FileOutputStream(tempUploadFile));
             
             String contentEncoding = null;        
             if (isGzipEnabled) {
                 inputStream = new GZipDeflatingInputStream(inputStream);
                 contentEncoding = "gzip";
                 newObject.addMetadata(Constants.METADATA_JETS3T_COMPRESSED, "gzip"); 
             } 
             if (isEncryptionEnabled) {
                 inputStream = encryptionUtil.encrypt(inputStream);
                 contentEncoding = null;
                 newObject.setContentType(Mimetypes.MIMETYPE_OCTET_STREAM);
                 newObject.addMetadata(Constants.METADATA_JETS3T_CRYPTO_ALGORITHM, 
                     encryptionUtil.getAlgorithm()); 
                 newObject.addMetadata(Constants.METADATA_JETS3T_CRYPTO_VERSION, 
                     EncryptionUtil.DEFAULT_VERSION); 
             }
             if (contentEncoding != null) {
                 newObject.addMetadata("Content-Encoding", contentEncoding);
             }
     
             // Write transformed data to temporary file.
             byte[] buffer = new byte[8192];
             int c = -1;
             while ((c = inputStream.read(buffer)) >= 0) {
                 outputStream.write(buffer, 0, c);
             }
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
             if (outputStream != null) {
                 outputStream.close();
             }            
         }
         
         return tempUploadFile;
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
     private S3Object prepareUploadObject(String targetKey, File file, String aclString) 
         throws Exception 
     {
         S3Object newObject = new S3Object(targetKey);
         
         if ("PUBLIC_READ".equalsIgnoreCase(aclString)) {
             newObject.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);                        
         } else if ("PUBLIC_READ_WRITE".equalsIgnoreCase(aclString)) {
             newObject.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE);            
         } else if ("PRIVATE".equalsIgnoreCase(aclString)) {
             // Private is the default, no need to ad an ACL
         } else {
             throw new Exception("Invalid value for ACL string: " + aclString);
         }
         
         newObject.addMetadata(Constants.METADATA_JETS3T_LOCAL_FILE_DATE, 
             ServiceUtils.formatIso8601Date(new Date(file.lastModified())));
 
         if (file.isDirectory()) {
             newObject.setContentLength(0);
             newObject.setContentType(Mimetypes.MIMETYPE_JETS3T_DIRECTORY);
         } else {
             newObject.setContentType(Mimetypes.getInstance().getMimetype(file));
 
             EncryptionUtil encryptionUtil = null;
             if (isEncryptionEnabled) {
                 String algorithm = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
                     .getStringProperty("crypto.algorithm", "PBEWithMD5AndDES");
                 encryptionUtil = new EncryptionUtil(cryptoPassword, algorithm, EncryptionUtil.DEFAULT_VERSION);
             }
             File uploadFile = prepareUploadFile(file, newObject, encryptionUtil);
             
             newObject.setContentLength(uploadFile.length());
             newObject.setDataInputFile(uploadFile);
 
             // Compute the upload file's MD5 hash.
             newObject.setMd5Hash(ServiceUtils.computeMD5Hash(
                 new FileInputStream(uploadFile)));
             
             if (!uploadFile.equals(file)) {
                 // Compute the MD5 hash of the *original* file, if upload file has been altered
                 // through encryption or gzipping.
                 newObject.addMetadata(
                     S3Object.METADATA_HEADER_ORIGINAL_HASH_MD5,
                     ServiceUtils.toBase64(ServiceUtils.computeMD5Hash(new FileInputStream(file))));
             }
         }
         return newObject;
     }
     
     /**
      * Downloads a file from S3, transferring an object's data to the local file.
      * 
      * @param targetKey the key name for the object
      * @param file      the file to upload to S3
      * 
      * @param bucket    the bucket to create the object in 
      * @param object    the object to download
      * @param fileTarget    the file to save the S3 object to (which may also become a directory)
      *  
      * @throws Exception
      */
     private DownloadPackage prepareObjectForDownload(S3Object object, File fileTarget) 
         throws Exception 
     {
         if (!doAction) {
             return null;
         }
         
         if (Mimetypes.MIMETYPE_JETS3T_DIRECTORY.equals(object.getContentType())) {
             fileTarget.mkdir();
             return null;
         } else {
             if (fileTarget.getParentFile() != null) {
                 fileTarget.getParentFile().mkdirs();
             }            
             
             boolean isZipped = false;
             EncryptionUtil encryptionUtil = null;
                         
             if (isGzipEnabled && 
                 ("gzip".equalsIgnoreCase(object.getContentEncoding())
                 || object.containsMetadata(Constants.METADATA_JETS3T_COMPRESSED)))
             {
                 // Automatically inflate gzipped data.
                 isZipped = true;
             }
             if (isEncryptionEnabled 
                 && (object.containsMetadata(Constants.METADATA_JETS3T_ENCRYPTED_OBSOLETE)
                     || object.containsMetadata(Constants.METADATA_JETS3T_CRYPTO_ALGORITHM)))
             {
                 // Automatically decrypt encrypted files.
                 
                 if (object.containsMetadata(Constants.METADATA_JETS3T_ENCRYPTED_OBSOLETE)) {
                     // Item is encrypted with obsolete crypto.
                     encryptionUtil = EncryptionUtil.getObsoleteEncryptionUtil(
                         cryptoPassword);                                            
                 } else {
                     String algorithm = (String) object.getMetadata(
                         Constants.METADATA_JETS3T_CRYPTO_ALGORITHM);
                     String version = (String) object.getMetadata(
                         Constants.METADATA_JETS3T_CRYPTO_VERSION);
                     if (version == null) {
                         version = EncryptionUtil.DEFAULT_VERSION;
                     }
                     encryptionUtil = new EncryptionUtil(cryptoPassword, algorithm, version);                                            
                 }                    
             }
             
             return new DownloadPackage(object, fileTarget, isZipped, encryptionUtil);                        
         }        
     }
 
     private String formatTransferDetails(ThreadWatcher watcher) {
         String detailsText = "";
         if (watcher.isBytesPerSecondAvailable()) {
             long bytesPerSecond = watcher.getBytesPerSecond();
             detailsText = byteFormatter.formatByteSize(bytesPerSecond) + "/s";
         }
         if (watcher.isTimeRemainingAvailable()) {
             if (detailsText.trim().length() > 0) {
                 detailsText += " - ";
             }
             long secondsRemaining = watcher.getTimeRemaining();
             detailsText += "ETA: " + timeFormatter.formatTime(secondsRemaining, false);
         }
         return detailsText;
     }
     
     private void printLine(String line) {
         printLine(line, false);
     }
     
     /**
      * Prints text to StdOut provided the isQuiet flag is not set.
      * 
      * @param line the text to print
      * @param isTemporary
      * if true, the line is printed followed by a carriage return such
      * that the next line output to the console will overwrite it.
      */
     private void printLine(String line, boolean isTemporary) {
         if (!isQuiet) {
             if (isTemporary) {
                 String temporaryLine = "  " + line;                
                 if (temporaryLine.length() > maxTemporaryStringLength) {
                     maxTemporaryStringLength = temporaryLine.length();
                 }
                 String blanks = "";
                 for (int i = temporaryLine.length(); i < maxTemporaryStringLength; i++) {
                     blanks += " ";
                 }
                 System.out.print(temporaryLine + blanks + "\r");
             } else {
                 String blanks = "";
                 for (int i = line.length(); i < maxTemporaryStringLength; i++) {
                     blanks += " ";
                 }
                 System.out.println(line + blanks);
                 maxTemporaryStringLength = 0;
             }
         }
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
                 printLine("N " + keyPath);
                 objectsToUpload.add(prepareUploadObject(targetKey, file, aclString));
             } else if (disrepancyResults.updatedOnClientKeys.contains(keyPath)) {
                 printLine("U " + keyPath);
                 objectsToUpload.add(prepareUploadObject(targetKey, file, aclString));
             } else if (disrepancyResults.alreadySynchronisedKeys.contains(keyPath)) {
                 if (isForce) {
                     printLine("F " + keyPath);
                     objectsToUpload.add(prepareUploadObject(targetKey, file, aclString));
                 } else {
                     printLine("- " + keyPath);
                 }
             } else if (disrepancyResults.updatedOnServerKeys.contains(keyPath)) {
                 // This file has been updated on the server-side.
                 if (isKeepFiles) {
                     printLine("r " + keyPath);                    
                 } else {
                     printLine("R " + keyPath);
                     objectsToUpload.add(prepareUploadObject(targetKey, file, aclString));
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
 
             if (isKeepFiles) {
                 printLine("d " + keyPath);                
             } else {
                 printLine("D " + keyPath);
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
         
         System.out.println(
             "New files: " + disrepancyResults.onlyOnClientKeys.size() +
             ", Updated: " + disrepancyResults.updatedOnClientKeys.size() +
             (isKeepFiles?
                 ", Kept: " + 
                 (disrepancyResults.updatedOnServerKeys.size() + disrepancyResults.onlyOnServerKeys.size())                    
                 :                 
                 ", Reverted: " + disrepancyResults.updatedOnServerKeys.size() +
                 ", Deleted: " + disrepancyResults.onlyOnServerKeys.size()
                 ) +
             (isForce ?
                 ", Forced updates: " + disrepancyResults.alreadySynchronisedKeys.size() :
                 ", Unchanged: " + disrepancyResults.alreadySynchronisedKeys.size()
                 )        
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
         
         // Upload/update files.
         Iterator s3KeyIter = sortedS3ObjectKeys.iterator();
         while (s3KeyIter.hasNext()) {
             String keyPath = (String) s3KeyIter.next();
             S3Object s3Object = (S3Object) s3ObjectsMap.get(keyPath);
             
             if (disrepancyResults.onlyOnServerKeys.contains(keyPath)) {
                 printLine("N " + keyPath);
                 DownloadPackage downloadPackage = prepareObjectForDownload(
                     s3Object, new File(localDirectory, keyPath));
                 if (downloadPackage != null) {
                     downloadPackagesList.add(downloadPackage);
                 }
             } else if (disrepancyResults.updatedOnServerKeys.contains(keyPath)) {
                 printLine("U " + keyPath);
                 DownloadPackage downloadPackage = prepareObjectForDownload(
                     s3Object, new File(localDirectory, keyPath));
                 if (downloadPackage != null) {
                     downloadPackagesList.add(downloadPackage);
                 }
             } else if (disrepancyResults.alreadySynchronisedKeys.contains(keyPath)) {
                 if (isForce) {
                     printLine("F " + keyPath);
                     DownloadPackage downloadPackage = prepareObjectForDownload(
                         s3Object, new File(localDirectory, keyPath));
                     if (downloadPackage != null) {
                         downloadPackagesList.add(downloadPackage);
                     }
                 } else {
                     printLine("- " + keyPath);
                 }
             } else if (disrepancyResults.updatedOnClientKeys.contains(keyPath)) {
                 // This file has been updated on the client-side.
                 if (isKeepFiles) {
                     printLine("r " + keyPath);                    
                 } else {
                     printLine("R " + keyPath);
                     DownloadPackage downloadPackage = prepareObjectForDownload(
                         s3Object, new File(localDirectory, keyPath));
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
             
             if (isKeepFiles) {
                 printLine("d " + keyPath);                
             } else {
                 printLine("D " + keyPath);
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
         
         System.out.println(
             "New files: " + disrepancyResults.onlyOnServerKeys.size() +
             ", Updated: " + disrepancyResults.updatedOnServerKeys.size() +
             (isKeepFiles? 
                 ", Kept: " + 
                 (disrepancyResults.updatedOnClientKeys.size() + disrepancyResults.onlyOnClientKeys.size())                    
                 : 
                 ", Reverted: " + disrepancyResults.updatedOnClientKeys.size() +
                 ", Deleted: " + disrepancyResults.onlyOnClientKeys.size()
                 ) +
             (isForce ?
                 ", Forced updates: " + disrepancyResults.alreadySynchronisedKeys.size() :
                 ", Unchanged: " + disrepancyResults.alreadySynchronisedKeys.size()
                 )        
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
             System.out.println("UP "
                 + (doAction ? "" : "[No Action] ")
                 + "Local" + fileList + " => S3[" + s3Path + "]");              
         } else if ("DOWN".equals(actionCommand)) {
             if (fileList.size() != 1) {
                 throw new SynchronizeException("Only one target directory is allowed for downloads");
             }
             System.out.println("DOWN "
                 + (doAction ? "" : "[No Action] ")
                 + "S3[" + s3Path + "] => Local" + fileList);              
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
         
         if (objectPath.length() > 0) {
             // Create the S3Path.
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
                 
         // Compare contents of local directory with contents of S3 path and identify any disrepancies.
         Map filesMap = null;
         if ("UP".equals(actionCommand)) {
             filesMap = FileComparer.buildFileMap((File[]) fileList.toArray(new File[fileList.size()]));
         } else if ("DOWN".equals(actionCommand)) {
             filesMap = FileComparer.buildFileMap((File) fileList.get(0), null);
         }
 
         printLine("Listing objects in S3", true);
         Map s3ObjectsMap = FileComparer.buildS3ObjectMap(s3Service, bucket, objectPath, serviceEventAdaptor);
         if (serviceEventAdaptor.wasErrorThrown()) {
             throw new Exception("Unable to build map of S3 Objects", serviceEventAdaptor.getErrorThrown());
         }
         
         FileComparerResults discrepancyResults = FileComparer.buildDiscrepancyLists(filesMap, s3ObjectsMap);
 
         // Perform the requested action on the set of disrepancies.
         if ("UP".equals(actionCommand)) {
             uploadLocalDirectoryToS3(discrepancyResults, filesMap, s3ObjectsMap, bucket, objectPath, aclString);
         } else if ("DOWN".equals(actionCommand)) {
             restoreFromS3ToLocalDirectory(discrepancyResults, filesMap, s3ObjectsMap, objectPath, (File) fileList.get(0), bucket);
         }
     }
     
     S3ServiceEventAdaptor serviceEventAdaptor = new S3ServiceEventAdaptor() {
         private void displayProgressStatus(String prefix, ThreadWatcher watcher) {
             String statusText = prefix + watcher.getCompletedThreads() + "/" + watcher.getThreadCount();                    
             
             // Show percentage of bytes transferred, if this info is available.
             if (watcher.isBytesTransferredInfoAvailable()) {
                 String bytesTotalStr = byteFormatter.formatByteSize(watcher.getBytesTotal());
                 long percentage = (int) 
                     (((double)watcher.getBytesTransferred() / watcher.getBytesTotal()) * 100);
                 
                 String detailsText = formatTransferDetails(watcher);
                 
                 statusText += " - " + percentage + "% of " + bytesTotalStr 
                     + (detailsText.length() > 0 ? " (" + detailsText + ")" : "");
             } else {
                 long percentage = (int) 
                     (((double)watcher.getCompletedThreads() / watcher.getThreadCount()) * 100);
                 
                 statusText += " - " + percentage + "%";                
             }
             printLine(statusText, true);                
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
         System.out.println("   Runs quietly and does not report on each action performed. The action");
         System.out.println("   summary is still displayed.");
         System.out.println("");
         System.out.println("-f | --force");
         System.out.println("   Force tool to perform synchronization even when files are up-to-date.");
         System.out.println("   This may be useful if you need to update metadata or timestamps in S3.");
         System.out.println("");
         System.out.println("-k | --keepfiles");
         System.out.println("   Keep files on destination instead of reverting/removing them.");
         System.out.println("");
         System.out.println("-g | --gzip");
         System.out.println("   Compress (GZip) files when backing up and Decompress gzipped files");
         System.out.println("   when restoring.");
         System.out.println("");
         System.out.println("-c | --crypto");
         System.out.println("   Encrypt files when backing up and decrypt encrypted files when restoring. If");
         System.out.println("   this option is specified the properties must contain a password.");
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
         System.out.println("   because the --keepfiles option was set it was not deleted.");
         System.out.println("R: An existing file/object has changed more recently on the target than on the");
         System.out.println("   source. The target version will be reverted to the older source version");
         System.out.println("r: An existing file/object has changed more recently on the target than on the");
         System.out.println("   source but because the --keepfiles option was set it was not reverted.");
         System.out.println("-: The file identical locally and in S3, no action is necessary.");
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
         // Required arguments
         String actionCommand = null;
         String s3Path = null;
         int reqArgCount = 0;
         List fileList = new ArrayList();
         
         // Options
         boolean doAction = true;
         boolean isQuiet = false;
         boolean isForce = false;
         boolean isKeepFiles = false;
         boolean isGzipEnabled = false;
         boolean isEncryptionEnabled = false;
         
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
                 } else if (arg.equalsIgnoreCase("-f") || arg.equalsIgnoreCase("--force")) {
                     isForce = true; 
                 } else if (arg.equalsIgnoreCase("-k") || arg.equalsIgnoreCase("--keepfiles")) {
                     isKeepFiles = true; 
                 } else if (arg.equalsIgnoreCase("-g") || arg.equalsIgnoreCase("--gzip")) {
                     isGzipEnabled = true; 
                 } else if (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--crypto")) {
                     isEncryptionEnabled = true; 
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
                            System.err.println("WARN: Ignoring unreadable path: " + file);
                            continue;
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
         
         // Read the Properties file, and make sure it contains everything we need.
         String propertiesFileName = "synchronize.properties";
         Jets3tProperties properties = Jets3tProperties.getInstance(propertiesFileName);
         if (!properties.isLoaded()) {
             System.err.println("ERROR: The properties file " + propertiesFileName + " could not be found in the classpath");
             System.exit(2);                        
         }
         
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
         
         String aclString = properties.getStringProperty("acl", "PRIVATE");        
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
             doAction, isQuiet, isForce, isKeepFiles, isGzipEnabled, isEncryptionEnabled);
         client.run(s3Path, fileList, actionCommand, 
             properties.getStringProperty("password", null), aclString);
     }
         
 }
