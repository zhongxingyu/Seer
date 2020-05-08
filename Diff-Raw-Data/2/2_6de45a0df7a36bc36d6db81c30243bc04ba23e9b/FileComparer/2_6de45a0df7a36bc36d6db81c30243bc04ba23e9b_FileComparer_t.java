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
 package org.jets3t.service.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.io.BytesProgressWatcher;
 import org.jets3t.service.io.ProgressMonitoredInputStream;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.multithread.GetObjectHeadsEvent;
 import org.jets3t.service.multithread.S3ServiceEventAdaptor;
 import org.jets3t.service.multithread.S3ServiceEventListener;
 import org.jets3t.service.multithread.S3ServiceMulti;
 
 /**
  * File comparison utility to compare files on the local computer with objects present in an S3
  * account and determine whether there are any differences. This utility contains methods to
  * build maps of the contents of the local file system or S3 account for comparison, and 
  * <tt>buildDiscrepancyLists</tt> methods to find differences in these maps.
  * <p>
  * File comparisons are based primarily on MD5 hashes of the files' contents. If a local file does
  * not match an object in S3 with the same name, this utility determine which of the items is
  * newer by comparing the last modified dates.
  * 
  * @author James Murty
  */
 public class FileComparer {
     private static final Log log = LogFactory.getLog(FileComparer.class);
 
     /**
      * If a <code>.jets3t-ignore</code> file is present in the given directory, the file is read
      * and all the paths contained in it are coverted to regular expression Pattern objects.
      * 
      * @param directory
      * a directory that may contain a <code>.jets3t-ignore</code> file. If this parameter is null
      * or is actually a file and not a directory, an empty list will be returned.
      * 
      * @return
      * a list of Pattern objects representing the paths in the ignore file. If there is no ignore
      * file, or if it has no contents, the list returned will be empty. 
      */
     protected static List buildIgnoreRegexpList(File directory) {
         ArrayList ignorePatternList = new ArrayList();
 
         if (directory == null || !directory.isDirectory()) {
             return ignorePatternList;
         }
         
         File jets3tIgnoreFile = new File(directory, Constants.JETS3T_IGNORE_FILENAME);
         if (jets3tIgnoreFile.exists() && jets3tIgnoreFile.canRead()) {
             log.debug("Found ignore file: " + jets3tIgnoreFile.getPath());
             try {
                 String ignorePaths = ServiceUtils.readInputStreamToString(
                     new FileInputStream(jets3tIgnoreFile));
                 StringTokenizer st = new StringTokenizer(ignorePaths.trim(), "\n");
                 while (st.hasMoreTokens()) {
                     String ignorePath = st.nextToken();
                     
                     // Convert path to RegExp.
                     String ignoreRegexp = ignorePath;
                     ignoreRegexp = ignoreRegexp.replaceAll("\\.", "\\\\.");
                     ignoreRegexp = ignoreRegexp.replaceAll("\\*", ".*");
                     ignoreRegexp = ignoreRegexp.replaceAll("\\?", ".");
                     
                     Pattern pattern = Pattern.compile(ignoreRegexp);
                     log.debug("Ignore path '" + ignorePath + "' has become the regexp: " 
                         + pattern.pattern());                    
                     ignorePatternList.add(pattern);                    
                 }
             } catch (IOException e) {
                 log.error("Failed to read contents of ignore file '" + jets3tIgnoreFile.getPath()
                     + "'", e);
             }
         }
         
         if (Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
             .getBoolProperty("filecomparer.skip-upload-of-md5-files", false))
         {
             Pattern pattern = Pattern.compile(".*\\.md5");
             log.debug("Skipping upload of pre-computed MD5 files with path '*.md5' using the regexp: " 
                 + pattern.pattern());
             ignorePatternList.add(pattern);                                
         }        
         
         return ignorePatternList;
     }
     
     /**
      * Determines whether a file should be ignored, based on whether it matches a regular expression
      * Pattern in the provided ignore list.
      * 
      * @param ignorePatternList
      * a list of Pattern objects representing the file names to ignore.
      * @param file
      * a file that will either be ignored or not, depending on whether it matches an ignore Pattern.
      * 
      * @return
      * true if the file should be ignored, false otherwise.
      */
     protected static boolean isIgnored(List ignorePatternList, File file) {
         Iterator patternIter = ignorePatternList.iterator();
         while (patternIter.hasNext()) {
             Pattern pattern = (Pattern) patternIter.next();
 
             if (pattern.matcher(file.getName()).matches()) {
                 log.debug("Ignoring " + (file.isDirectory() ? "directory" : "file") 
                     + " matching pattern '" + pattern.pattern() + "': " + file.getName());                
                 return true;
             }
         }
         
         return false;
     }
     
     /**
      * Builds a File Map containing the given files. If any of the given files are actually
      * directories, the contents of the directory are included.
      * <p>
      * File keys are delimited with '/' characters.
      * <p>
      * Any file or directory matching a path in a <code>.jets3t-ignore</code> file will be ignored.
      * 
      * @param files
      * the set of files/directories to include in the file map.
      * @param includeDirectories
      * If true all directories, including empty ones, will be included in the Map. These directories
      * will be mere place-holder objects with the content type {@link Mimetypes#MIMETYPE_JETS3T_DIRECTORY}.
      * If this variable is false directory objects will not be included in the Map, and it will not
      * be possible to store empty directories in S3.
      * 
      * @return
      * a Map of file path keys to File objects.
      */
     public static Map buildFileMap(File[] files, boolean includeDirectories) {
         // Build map of files proposed for upload.
         HashMap fileMap = new HashMap();
         for (int i = 0; i < files.length; i++) {
             List ignorePatternList = buildIgnoreRegexpList(files[i].getParentFile());
             
             if (!isIgnored(ignorePatternList, files[i])) {
                 if (!files[i].isDirectory() || includeDirectories) {
                     fileMap.put(files[i].getName(), files[i]);
                 }
                 if (files[i].isDirectory()) {
                     buildFileMapImpl(files[i], files[i].getName() + Constants.FILE_PATH_DELIM, 
                         fileMap, includeDirectories);
                 }
             }
         }
         return fileMap;
     }
     
     /**
      * Builds a File Map containing all the files and directories inside the given root directory,
      * where the map's key for each file is the relative path to the file.
      * <p> 
      * File keys are delimited with '/' characters.
      * <p>
      * Any file or directory matching a path in a <code>.jets3t-ignore</code> file will be ignored.
      * 
      * @see #buildDiscrepancyLists(Map, Map)
      * @see #buildS3ObjectMap(S3Service, S3Bucket, String, S3Object[], S3ServiceEventListener)
      * 
      * @param rootDirectory
      * The root directory containing the files/directories of interest. The root directory is <b>not</b>
      * included in the result map.
      * @param fileKeyPrefix
      * A prefix added to each file path key in the map, e.g. the name of the root directory the
      * files belong to. If provided, a '/' suffix is always added to the end of the prefix. If null
      * or empty, no prefix is used.
      * @param includeDirectories
      * If true all directories, including empty ones, will be included in the Map. These directories
      * will be mere place-holder objects with the content type {@link Mimetypes#MIMETYPE_JETS3T_DIRECTORY}.
      * If this variable is false directory objects will not be included in the Map, and it will not
      * be possible to store empty directories in S3.
      * 
      * @return A Map of file path keys to File objects.
      */
     public static Map buildFileMap(File rootDirectory, String fileKeyPrefix, boolean includeDirectories) {
         HashMap fileMap = new HashMap();
         List ignorePatternList = buildIgnoreRegexpList(rootDirectory);
         
         if (!isIgnored(ignorePatternList, rootDirectory)) {        
             if (fileKeyPrefix == null || fileKeyPrefix.length() == 0) {
                 fileKeyPrefix = "";
             } else {
                 if (!fileKeyPrefix.endsWith(Constants.FILE_PATH_DELIM)) {
                     fileKeyPrefix += Constants.FILE_PATH_DELIM;
                 }
             }
             buildFileMapImpl(rootDirectory, fileKeyPrefix, fileMap, includeDirectories);
         }
         return fileMap;
     }
     
     /**
      * Recursively builds a File Map containing all the files and directories inside the given directory,
      * where the map's key for each file is the relative path to the file.
      * <p> 
      * File keys are delimited with '/' characters.
      * <p>
      * Any file or directory matching a path in a <code>.jets3t-ignore</code> file will be ignored.
      * 
      * @param directory
      * The directory containing the files/directories of interest. The directory is <b>not</b>
      * included in the result map.
      * @param fileKeyPrefix
      * A prefix added to each file path key in the map, e.g. the name of the root directory the
      * files belong to. This prefix <b>must</b> end with a '/' character.
      * @param fileMap
      * a map of path keys to File objects, that this method adds items to.
      * @param includeDirectories
      * If true all directories, including empty ones, will be included in the Map. These directories
      * will be mere place-holder objects with the content type {@link Mimetypes#MIMETYPE_JETS3T_DIRECTORY}.
      * If this variable is false directory objects will not be included in the Map, and it will not
      * be possible to store empty directories in S3.
      */
     protected static void buildFileMapImpl(File directory, String fileKeyPrefix, Map fileMap, boolean includeDirectories) {
         List ignorePatternList = buildIgnoreRegexpList(directory);
 
         File children[] = directory.listFiles();
         for (int i = 0; children != null && i < children.length; i++) {                        
             if (!isIgnored(ignorePatternList, children[i])) {
                 if (!children[i].isDirectory() || includeDirectories) {
                     fileMap.put(fileKeyPrefix + children[i].getName(), children[i]);
                 }
                 if (children[i].isDirectory()) {
                     buildFileMapImpl(children[i], fileKeyPrefix + children[i].getName() + "/", 
                         fileMap, includeDirectories);
                 } 
             }
         }
     }
 
     /**
      * Builds an S3 Object Map containing all the objects within the given target path,
      * where the map's key for each object is the relative path to the object.
      * 
      * @see #buildDiscrepancyLists(Map, Map)
      * @see #buildFileMap(File, String, boolean)
      * 
      * @param s3Service
      * @param bucket
      * @param targetPath
      * @return
      * maping of keys/S3Objects
      * @throws S3ServiceException
      */
     public static Map buildS3ObjectMap(S3Service s3Service, S3Bucket bucket, String targetPath,
         S3ServiceEventListener s3ServiceEventListener) throws S3ServiceException
     {
         String prefix = (targetPath.length() > 0 ? targetPath : null);
         S3Object[] s3ObjectsIncomplete = s3Service.listObjects(bucket, prefix, null);
         return buildS3ObjectMap(s3Service, bucket, targetPath, s3ObjectsIncomplete, s3ServiceEventListener);
     }
 
 
     /**
      * Builds an S3 Object Map containing all the given objects, by retrieving HEAD details about
      * all the objects and using {@link #populateS3ObjectMap(String, S3Object[])} to product an object/key 
      * map.
      * 
      * @see #buildDiscrepancyLists(Map, Map)
      * @see #buildFileMap(File, String, boolean)
      * 
      * @param s3Service
      * @param bucket
      * @param targetPath
      * @param s3ObjectsIncomplete
      * @return
      * mapping of keys/S3Objects
      * @throws S3ServiceException
      */
     public static Map buildS3ObjectMap(S3Service s3Service, S3Bucket bucket,  String targetPath, 
         S3Object[] s3ObjectsIncomplete, S3ServiceEventListener s3ServiceEventListener) 
         throws S3ServiceException
     {
         // Retrieve the complete information about all objects listed via GetObjectsHeads.
         final ArrayList s3ObjectsCompleteList = new ArrayList(s3ObjectsIncomplete.length);
         final S3ServiceException s3ServiceExceptions[] = new S3ServiceException[1];
         S3ServiceMulti s3ServiceMulti = new S3ServiceMulti(s3Service, new S3ServiceEventAdaptor() {
             public void s3ServiceEventPerformed(GetObjectHeadsEvent event) {
                 if (GetObjectHeadsEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                     S3Object[] finishedObjects = event.getCompletedObjects();
                     if (finishedObjects.length > 0) {
                         s3ObjectsCompleteList.addAll(Arrays.asList(finishedObjects));
                     }
                 } else if (GetObjectHeadsEvent.EVENT_ERROR == event.getEventCode()) {
                     s3ServiceExceptions[0] = new S3ServiceException(
                         "Failed to retrieve detailed information about all S3 objects", 
                         event.getErrorCause());                    
                 }
             }
         });
         if (s3ServiceEventListener != null) {
             s3ServiceMulti.addServiceEventListener(s3ServiceEventListener);
         }
         s3ServiceMulti.getObjectsHeads(bucket, s3ObjectsIncomplete);
         if (s3ServiceExceptions[0] != null) {
             throw s3ServiceExceptions[0];
         }        
         S3Object[] s3Objects = (S3Object[]) s3ObjectsCompleteList
             .toArray(new S3Object[s3ObjectsCompleteList.size()]);
         
         return populateS3ObjectMap(targetPath, s3Objects);
     }
 
     /**
      * Builds a map of key/object pairs each object is associated with a key based on its location
      * in the S3 target path. 
      * 
      * @param targetPath
      * @param s3Objects
      * @return
      * a map of key/S3Object pairs.
      */
     public static Map populateS3ObjectMap(String targetPath, S3Object[] s3Objects) {
         HashMap map = new HashMap();
         for (int i = 0; i < s3Objects.length; i++) {
             String relativeKey = s3Objects[i].getKey();
             if (targetPath.length() > 0) {
                 relativeKey = relativeKey.substring(targetPath.length());
                 int slashIndex = relativeKey.indexOf(Constants.FILE_PATH_DELIM);
                 if (slashIndex >= 0) {
                     relativeKey = relativeKey.substring(slashIndex + 1, relativeKey.length());
                 } else {
                     // This relative key is part of a prefix search, the key does not point to a
                     // real S3 object.
                     relativeKey = "";
                 }
             }
             if (relativeKey.length() > 0) {
                 map.put(relativeKey, s3Objects[i]);
             }
         }
         return map;
     }
 
     /**
      * Compares the contents of a directory on the local file system with the contents of an
      * S3 resource. This comparison is performed on a map of files and a map of S3 objects previously
      * generated using other methods in this class.
      * 
      * @param filesMap
      *        a map of keys/Files built using the method {@link #buildFileMap(File, String, boolean)}
      * @param s3ObjectsMap
      *        a map of keys/S3Objects built using the method 
      *        {@link #buildS3ObjectMap(S3Service, S3Bucket, String, S3ServiceEventListener)}
      * @return
      * an object containing the results of the file comparison.
      * 
      * @throws NoSuchAlgorithmException
      * @throws FileNotFoundException
      * @throws IOException
      * @throws ParseException
      */
     public static FileComparerResults buildDiscrepancyLists(Map filesMap, Map s3ObjectsMap) 
         throws NoSuchAlgorithmException, FileNotFoundException, IOException, ParseException
     {
         return buildDiscrepancyLists(filesMap, s3ObjectsMap, null);
     }
     
     /**
      * Compares the contents of a directory on the local file system with the contents of an
      * S3 resource. This comparison is performed on a map of files and a map of S3 objects previously
      * generated using other methods in this class.
      * 
      * @param filesMap
      *        a map of keys/Files built using the method {@link #buildFileMap(File, String, boolean)}
      * @param s3ObjectsMap
      *        a map of keys/S3Objects built using the method 
      *        {@link #buildS3ObjectMap(S3Service, S3Bucket, String, S3ServiceEventListener)}
      * @param progressWatcher
      *        watches the progress of file hash generation.
      * @return
      * an object containing the results of the file comparison.
      * 
      * @throws NoSuchAlgorithmException
      * @throws FileNotFoundException
      * @throws IOException
      * @throws ParseException
      */
     public static FileComparerResults buildDiscrepancyLists(Map filesMap, Map s3ObjectsMap, 
         BytesProgressWatcher progressWatcher)
         throws NoSuchAlgorithmException, FileNotFoundException, IOException, ParseException
     {
         List onlyOnServerKeys = new ArrayList();
         List updatedOnServerKeys = new ArrayList();
         List updatedOnClientKeys = new ArrayList();
         List alreadySynchronisedKeys = new ArrayList();
         List onlyOnClientKeys = new ArrayList();
 
         // Check files on server against local client files.
         Iterator s3ObjectsMapIter = s3ObjectsMap.entrySet().iterator();
         while (s3ObjectsMapIter.hasNext()) {
             Map.Entry entry = (Map.Entry) s3ObjectsMapIter.next();
             String keyPath = (String) entry.getKey();
             S3Object s3Object = (S3Object) entry.getValue();
 
             // Check whether local file is already on server
             if (filesMap.containsKey(keyPath)) {
                 // File has been backed up in the past, is it still up-to-date?
                 File file = (File) filesMap.get(keyPath);
 
                 if (file.isDirectory()) {
                     // We don't care about directory date changes, as long as it's present.
                     alreadySynchronisedKeys.add(keyPath);
                 } else {
                     // Compare file hashes.
                     boolean useMd5Files = 
                         Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
                         .getBoolProperty("filecomparer.use-md5-files", false);
 
                     boolean generateMd5Files = 
                         Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
                         .getBoolProperty("filecomparer.generate-md5-files", false);                                        
                     
                     byte[] computedHash = null;
                     
                     // Check whether a pre-computed MD5 hash file is available
                     File computedHashFile = new File(file.getPath() + ".md5");
                     if (useMd5Files
                         && computedHashFile.canRead()
                         && computedHashFile.lastModified() > file.lastModified())
                     {
                         try {
                             // A pre-computed MD5 hash file is available, try to read this hash value
                             BufferedReader br = new BufferedReader(new FileReader(computedHashFile));
                            computedHash = ServiceUtils.fromHex(br.readLine().split("\\s")[0]);
                             br.close();
                         } catch (Exception e) {
                             log.warn("Unable to read hash from computed MD5 file", e);
                         }
                     }
                     
                     if (computedHash == null) {
                         // A pre-computed hash file was not available, or could not be read. 
                         // Calculate the hash value anew.
                         InputStream hashInputStream = null;
                         if (progressWatcher != null) {
                             hashInputStream = new ProgressMonitoredInputStream( // Report on MD5 hash progress.
                                 new FileInputStream(file), progressWatcher);
                         } else {
                             hashInputStream = new FileInputStream(file);
                         }
                         computedHash = ServiceUtils.computeMD5Hash(hashInputStream);
                     }
                                                                                 
                     String fileHashAsBase64 = ServiceUtils.toBase64(computedHash);
                     
                     if (generateMd5Files && !file.getName().endsWith(".md5") &&
                         (!computedHashFile.exists() 
                         || computedHashFile.lastModified() < file.lastModified()))
                     {
                         // Create or update a pre-computed MD5 hash file.
                         try {
                             FileWriter fw = new FileWriter(computedHashFile);                            
                             fw.write(ServiceUtils.toHex(computedHash));
                             fw.close();
                         } catch (Exception e) {
                             log.warn("Unable to write computed MD5 hash to a file", e);
                         }
                     }
                     
                     // Get the S3 object's Base64 hash.
                     String objectHash = null;
                     if (s3Object.containsMetadata(S3Object.METADATA_HEADER_ORIGINAL_HASH_MD5)) {
                         // Use the object's *original* hash, as it is an encoded version of a local file.
                         objectHash = (String) s3Object.getMetadata(
                             S3Object.METADATA_HEADER_ORIGINAL_HASH_MD5);
                         log.debug("Object in S3 is encoded, using the object's original hash value for: "
                             + s3Object.getKey());
                     } else {
                         // The object wasn't altered when uploaded, so use its current hash.
                         objectHash = s3Object.getMd5HashAsBase64();
                     } 
                     
                     if (fileHashAsBase64.equals(objectHash)) {
                         // Hashes match so file is already synchronised.
                         alreadySynchronisedKeys.add(keyPath);
                     } else {
                         // File is out-of-synch. Check which version has the latest date.
                         Date s3ObjectLastModified = null;
                         String metadataLocalFileDate = (String) s3Object.getMetadata(
                             Constants.METADATA_JETS3T_LOCAL_FILE_DATE);
                         if (metadataLocalFileDate == null) {
                             // This is risky as local file times and S3 times don't match!
                             log.warn("Using S3 last modified date as file date. This is not reliable " 
                                 + "as the time according to S3 can differ from your local system time. "
                                 + "Please use the metadata item " 
                                 + Constants.METADATA_JETS3T_LOCAL_FILE_DATE);
                             s3ObjectLastModified = s3Object.getLastModifiedDate();
                         } else {
                             s3ObjectLastModified = ServiceUtils
                                 .parseIso8601Date(metadataLocalFileDate);
                         }
                         if (s3ObjectLastModified.getTime() > file.lastModified()) {
                             updatedOnServerKeys.add(keyPath);
                         } else if (s3ObjectLastModified.getTime() < file.lastModified()) {
                             updatedOnClientKeys.add(keyPath);
                         } else {
                             // Dates match exactly but the hash doesn't. Shouldn't ever happen!
                             throw new IOException("Backed-up S3Object " + s3Object.getKey()
                                 + " and local file " + file.getName()
                                 + " have the same date but different hash values. "
                                 + "This shouldn't happen!");
                         }
                     }
                 }
             } else {
                 // File is not in local file system, so it's only on the S3
                 // server.
                 onlyOnServerKeys.add(keyPath);
             }
         }
 
         // Any local files not already put into another list only exist locally.
         onlyOnClientKeys.addAll(filesMap.keySet());
         onlyOnClientKeys.removeAll(updatedOnClientKeys);
         onlyOnClientKeys.removeAll(alreadySynchronisedKeys);
         onlyOnClientKeys.removeAll(updatedOnServerKeys);
 
         return new FileComparerResults(onlyOnServerKeys, updatedOnServerKeys, updatedOnClientKeys,
             onlyOnClientKeys, alreadySynchronisedKeys);
     }
 
 }
