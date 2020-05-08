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
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 import org.jets3t.service.S3ObjectsChunk;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.io.BytesProgressWatcher;
 import org.jets3t.service.io.ProgressMonitoredInputStream;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.multithread.GetObjectHeadsEvent;
 import org.jets3t.service.multithread.ListObjectsEvent;
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
     
     private Jets3tProperties jets3tProperties = null;
     
     /**
      * Constructs the class.
      * 
      * @param jets3tProperties
      * the object containing the properties that will be applied in this class.
      */
     public FileComparer(Jets3tProperties jets3tProperties) {
         this.jets3tProperties = jets3tProperties;        
     }
     
     /**
      * @param jets3tProperties
      * the object containing the properties that will be applied in the instance.
      * @return
      * a FileComparer instance. 
      */
     public static FileComparer getInstance(Jets3tProperties jets3tProperties) {
         return new FileComparer(jets3tProperties);
     }
 
     /**
      * @return
      * a FileComparer instance initialized with the default JetS3tProperties
      * object. 
      */
     public static FileComparer getInstance() {
         return new FileComparer(
             Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME));
     }
 
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
     protected List buildIgnoreRegexpList(File directory) {
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
         
         if (jets3tProperties.getBoolProperty("filecomparer.skip-upload-of-md5-files", false))
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
     protected boolean isIgnored(List ignorePatternList, File file) {
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
     public Map buildFileMap(File[] files, boolean includeDirectories) {
         // Build map of files proposed for upload or download.
         HashMap fileMap = new HashMap();
         List ignorePatternList = null;
         List ignorePatternListForCurrentDir = null;
         
         for (int i = 0; i < files.length; i++) {
             if (files[i].getParentFile() == null) {
                 // For direct references to a file or dir, look for a .jets3t-ignore file
                 // in the current directory - only do this once for the current dir.
                 if (ignorePatternListForCurrentDir == null) {
                     ignorePatternListForCurrentDir = buildIgnoreRegexpList(new File("."));
                 }
                 ignorePatternList = ignorePatternListForCurrentDir;
             } else {
                 ignorePatternList = buildIgnoreRegexpList(files[i].getParentFile());
             }
             
             if (!isIgnored(ignorePatternList, files[i])) {
                 if (!files[i].exists()) {
                     continue;
                 }
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
     public Map buildFileMap(File rootDirectory, String fileKeyPrefix, boolean includeDirectories) {
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
     protected void buildFileMapImpl(File directory, String fileKeyPrefix, Map fileMap, boolean includeDirectories) {
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
      * Lists the objects in a bucket using a partitioning technique to divide
      * the object namespace into separate partitions that can be listed by
      * multiple simultaneous threads. This method divides the object namespace
      * using the given delimiter, traverses this space up to the specified
      * depth to identify prefix names for multiple "partitions", and 
      * then lists the objects in each partition. It returns the complete list 
      * of objects in the bucket path.
      * <p>
      * This partitioning technique will work best for buckets with many objects
      * that are divided into a number of virtual subdirectories of roughly equal
      * size.
      *  
      * @param s3Service
      * the service object that will be used to perform listing requests.
      * @param bucketName
      * the name of the bucket whose contents will be listed.
      * @param targetPath
      * a root path within the bucket to be listed. If this parameter is null, all
      * the bucket's objects will be listed. Otherwise, only the objects below the
      * virtual path specified will be listed.
      * @param delimiter
      * the delimiter string used to identify virtual subdirectory partitions 
      * in a bucket. If this parameter is null, or it has a value that is not
      * present in your object names, no partitioning will take place.
      * @param toDepth
      * the number of delimiter levels this method will traverse to identify
      * subdirectory partions. If this value is zero, no partitioning will take
      * place.  
      * 
      * @return
      * the list of objects under the target path in the bucket.
      * 
      * @throws S3ServiceException
      */
     public S3Object[] listObjectsThreaded(S3Service s3Service, 
         final String bucketName, String targetPath, final String delimiter, int toDepth) 
         throws S3ServiceException
     {
         final List allObjects = Collections.synchronizedList(new ArrayList());
         final List lastCommonPrefixes = Collections.synchronizedList(new ArrayList());
         final S3ServiceException s3ServiceExceptions[] = new S3ServiceException[1]; 
         
         /*
          * Create a S3ServiceMulti object with an event listener that responds to
          * ListObjectsEvent notifications and populates a complete object listing. 
          */
         final S3ServiceMulti s3Multi = new S3ServiceMulti(s3Service, new S3ServiceEventAdaptor() {
             public void s3ServiceEventPerformed(ListObjectsEvent event) {
                 if (ListObjectsEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                     Iterator chunkIter = event.getChunkList().iterator();
                     while (chunkIter.hasNext()) {
                         S3ObjectsChunk chunk = (S3ObjectsChunk) chunkIter.next();
                         
                         log.debug("Listed " + chunk.getObjects().length
                             + " objects and " + chunk.getCommonPrefixes().length 
                             + " common prefixes in bucket '" + bucketName 
                             + "' using prefix=" + chunk.getPrefix() 
                             + ", delimiter=" + chunk.getDelimiter());
 
                         allObjects.addAll(Arrays.asList(chunk.getObjects()));
                         lastCommonPrefixes.addAll(Arrays.asList(chunk.getCommonPrefixes()));
                     }
                 } else if (ListObjectsEvent.EVENT_ERROR == event.getEventCode()) {
                     s3ServiceExceptions[0] = new S3ServiceException(
                         "Failed to list all objects in S3 bucket", 
                         event.getErrorCause());                    
                 }
             }
         });  
 
         // The first listing partition we use as a starting point is the target path.
         String[] prefixesToList = new String[] {targetPath};
         int currentDepth = 0;        
         
         while (currentDepth <= toDepth && prefixesToList.length > 0) {
             log.debug("Listing objects in '" + bucketName + "' using " 
                 + prefixesToList.length + " prefixes: " 
                 + Arrays.asList(prefixesToList));
 
             // Initialize the variables that will be used, or populated, by the
             // multi-threaded listing.
             lastCommonPrefixes.clear();
             final String[] finalPrefixes = prefixesToList; 
             final String finalDelimiter = (currentDepth < toDepth ? delimiter : null);
 
             /*
              * Perform a multi-threaded listing, where each prefix string
              * will be used as a unique partition to be listed in a separate thread.
              */ 
             (new Thread() {
                 public void run() {
                     s3Multi.listObjects(bucketName, finalPrefixes, 
                         finalDelimiter, Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE);
                 };
             }).run();
             // Throw any exceptions that occur inside the threads.
             if (s3ServiceExceptions[0] != null) {
                 throw s3ServiceExceptions[0];
             }
             
             // We use the common prefix paths identified in the last listing 
             // iteration, if any, to identify partitions for follow-up listings.
             prefixesToList = (String[]) lastCommonPrefixes
                 .toArray(new String[lastCommonPrefixes.size()]);                        
 
             currentDepth++;
         }
         
         return (S3Object[]) allObjects.toArray(new S3Object[allObjects.size()]);
     }
     
     
     /**
      * Lists the objects in a bucket using a partitioning technique to divide
      * the object namespace into separate partitions that can be listed by
      * multiple simultaneous threads. This method divides the object namespace
      * using the given delimiter, traverses this space up to the specified
      * depth to identify prefix names for multiple "partitions", and 
      * then lists the objects in each partition. It returns the complete list 
      * of objects in the bucket path.
      * <p>
      * This partitioning technique will work best for buckets with many objects
      * that are divided into a number of virtual subdirectories of roughly equal
      * size.
      * <p>
      * The delimiter and depth properties that define how this method will 
      * partition the bucket's namespace are set in the jets3t.properties file
      * with the setting: 
      * filecomparer.bucket-listing.&lt;bucketname>=&lt;delim>,&lt;depth><br>
      * For example: <code>filecomparer.bucket-listing.my-bucket=/,2</code>
      *  
      * @param s3Service
      * the service object that will be used to perform listing requests.
      * @param bucketName
      * the name of the bucket whose contents will be listed.
      * @param targetPath
      * a root path within the bucket to be listed. If this parameter is null, all
      * the bucket's objects will be listed. Otherwise, only the objects below the
      * virtual path specified will be listed.
      * 
      * @return
      * the list of objects under the target path in the bucket.
      * 
      * @throws S3ServiceException
      */
     public S3Object[] listObjectsThreaded(S3Service s3Service, 
         final String bucketName, String targetPath) throws S3ServiceException    
     {
         String delimiter = null;
         int toDepth = 0;
         
         // Find bucket-specific listing properties, if any.
         String bucketListingProperties = jets3tProperties.getStringProperty(
             "filecomparer.bucket-listing." + bucketName, null);
         if (bucketListingProperties != null) {
             String splits[] = bucketListingProperties.split(",");
             if (splits.length != 2) {
                 throw new S3ServiceException(
                     "Invalid setting for bucket listing property "
                     + "filecomparer.bucket-listing." + bucketName + ": '" +
                     bucketListingProperties + "'");
             }            
             delimiter = splits[0].trim();
             toDepth = Integer.parseInt(splits[1]);
         }
         
         return listObjectsThreaded(s3Service, bucketName, targetPath, 
             delimiter, toDepth);
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
      * @param skipMetadata
      * @param s3ServiceEventListener
      * @return
      * mapping of keys/S3Objects
      * @throws S3ServiceException
      */
     public Map buildS3ObjectMap(S3Service s3Service, S3Bucket bucket, String targetPath,
         boolean skipMetadata, S3ServiceEventListener s3ServiceEventListener) 
         throws S3ServiceException
     {
         String prefix = (targetPath.length() > 0 ? targetPath : null);        
         S3Object[] s3ObjectsIncomplete = this.listObjectsThreaded(
             s3Service, bucket.getName(), prefix);
         return buildS3ObjectMap(s3Service, bucket, targetPath, s3ObjectsIncomplete, 
             skipMetadata, s3ServiceEventListener);
     }
 
 
     /**
      * Builds an S3 Object Map containing a partial set of objects within the given target path,
      * where the map's key for each object is the relative path to the object.
      * <p>
      * If the method is asked to perform a complete listing, it will use the
      * {@link #listObjectsThreaded(S3Service, String, String)} method to list the objects
      * in the bucket, potentially taking advantage of any bucket name partitioning
      * settings you have applied.
      * <p>
      * If the method is asked to perform only a partial listing, no bucket name
      * partitioning will be applied. 
      * 
      * @see #buildDiscrepancyLists(Map, Map)
      * @see #buildFileMap(File, String, boolean)
      * 
      * @param s3Service
      * @param bucket
      * @param targetPath
      * @param priorLastKey 
      * the prior last key value returned by a prior invocation of this method, if any.
      * @param completeListing
      * if true, this method will perform a complete listing of an S3 target. 
      * If false, the method will list a partial set of objects commencing from the
      * given prior last key.
      * 
      * @return
      * an object containing a mapping of key names to S3Objects, and the prior last
      * key (if any) that should be used to perform follow-up method calls.
      * @throws S3ServiceException
      */
     public PartialObjectListing buildS3ObjectMapPartial(S3Service s3Service, S3Bucket bucket, 
         String targetPath, String priorLastKey, boolean completeListing, 
         boolean skipMetadata, S3ServiceEventListener s3ServiceEventListener) 
         throws S3ServiceException
     {
         String prefix = (targetPath.length() > 0 ? targetPath : null);
         S3Object[] objects = null;
         String resultPriorLastKey = null;
         if (completeListing) {
             objects = listObjectsThreaded(s3Service, bucket.getName(), prefix);
         } else {
             S3ObjectsChunk chunk = s3Service.listObjectsChunked(bucket.getName(), 
                 prefix, null, Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE, 
                 priorLastKey, completeListing);
             objects = chunk.getObjects();
             resultPriorLastKey = chunk.getPriorLastKey();
         }
 
         Map objectsMap = buildS3ObjectMap(s3Service, bucket, targetPath, 
             objects, skipMetadata, s3ServiceEventListener);
         return new PartialObjectListing(objectsMap, resultPriorLastKey);
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
      * @param skipMetadata
      * @param s3ObjectsIncomplete
      * @return
      * mapping of keys/S3Objects
      * @throws S3ServiceException
      */
     public Map buildS3ObjectMap(S3Service s3Service, S3Bucket bucket,  String targetPath, 
         S3Object[] s3ObjectsIncomplete, boolean skipMetadata,
         S3ServiceEventListener s3ServiceEventListener) 
         throws S3ServiceException
     {
         S3Object[] s3Objects = null;
         
         if (skipMetadata) {
             s3Objects = s3ObjectsIncomplete;            
         } else {
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
             s3Objects = (S3Object[]) s3ObjectsCompleteList
                 .toArray(new S3Object[s3ObjectsCompleteList.size()]);
         }
 
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
     public Map populateS3ObjectMap(String targetPath, S3Object[] s3Objects) {
         HashMap map = new HashMap();
         for (int i = 0; i < s3Objects.length; i++) {
             String relativeKey = s3Objects[i].getKey();
             if (targetPath.length() > 0) {
                 relativeKey = relativeKey.substring(targetPath.length());
                 int slashIndex = relativeKey.indexOf(Constants.FILE_PATH_DELIM);
                 if (slashIndex == 0) {
                     relativeKey = relativeKey.substring(slashIndex + 1, relativeKey.length());
                 } else {
                     // This object is the result of a prefix search, not an explicit directory. 
                     // Base the relative key on the last full subdirectory in the 
                     // target path if available...
                     slashIndex = targetPath.lastIndexOf(Constants.FILE_PATH_DELIM);
                     if (slashIndex >= 0) {
                         relativeKey = s3Objects[i].getKey().substring(slashIndex + 1);
                     }
                     // ...otherwise, use the full object key name.
                     else {
                         relativeKey = s3Objects[i].getKey();
                     }
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
     public FileComparerResults buildDiscrepancyLists(Map filesMap, Map s3ObjectsMap) 
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
     public FileComparerResults buildDiscrepancyLists(Map filesMap, Map s3ObjectsMap, 
         BytesProgressWatcher progressWatcher)
         throws NoSuchAlgorithmException, FileNotFoundException, IOException, ParseException
     {
         Set onlyOnServerKeys = new HashSet();
         Set updatedOnServerKeys = new HashSet();
         Set updatedOnClientKeys = new HashSet();
         Set alreadySynchronisedKeys = new HashSet();
         Set onlyOnClientKeys = new HashSet();
 
         // Check files on server against local client files.
         Iterator s3ObjectsMapIter = s3ObjectsMap.entrySet().iterator();
         while (s3ObjectsMapIter.hasNext()) {
             Map.Entry entry = (Map.Entry) s3ObjectsMapIter.next();
             String keyPath = (String) entry.getKey();
             S3Object s3Object = (S3Object) entry.getValue();
 
             // A special-case check to identify objects created by Panic's
             // Transmit application that serve as directory placehoders -
             // a similar concept to the placeholders JetS3t uses but sadly
             // these look different.
             if (keyPath.endsWith("/")
                 && s3Object.getContentLength() == 0
                 && "binary/octet-stream".equals(s3Object.getContentType()))
             {
                 boolean ignorePanicDirPlaceholders = 
                     jets3tProperties.getBoolProperty(
                         "filecomparer.ignore-panic-dir-placeholders", false);
                 
                 if (ignorePanicDirPlaceholders) {                                
                     log.debug("Ignoring object that looks like a directory " +
                         "placeholder created by Panic's Transmit application: " + keyPath);
                     alreadySynchronisedKeys.add(keyPath);                
                     continue;
                 } else {
                     log.warn("Identified an object that looks like a directory " +
                         "placeholder created by Panic's Transmit application. " +
                         "If this object was indeed created by Transmit, it will not " +
                         "be handled properly unless the JetS3t property " +
                         "\"filecomparer.ignore-panic-dir-placeholders\" is set to " +
                         "true. " + s3Object);                    
                 }
             }
 
             // Check whether local file is already on server
             if (filesMap.containsKey(keyPath)) {                                
                 // File has been backed up in the past, is it still up-to-date?
                 File file = (File) filesMap.get(keyPath);
 
                 if (file.isDirectory()) {
                     // We don't care about directory date changes, as long as it's present.
                     alreadySynchronisedKeys.add(keyPath);
                 } else {
                     // Compare file hashes.
                     boolean useMd5Files = jets3tProperties
                         .getBoolProperty("filecomparer.use-md5-files", false);
 
                     boolean generateMd5Files = jets3tProperties
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
                         
                         // Try to retrieve the date using the deprecated metadata name
                         if (metadataLocalFileDate == null) {
                             metadataLocalFileDate = (String) s3Object.getMetadata(
                                 Constants.METADATA_JETS3T_LOCAL_FILE_DATE_DEPRECATED);
                         }
                         
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
     
     public class PartialObjectListing {
         private Map objectsMap = null;
         private String priorLastKey = null;
         
         public PartialObjectListing(Map objectsMap, String priorLastKey) {
             this.objectsMap = objectsMap;
             this.priorLastKey = priorLastKey;
         }
 
         public Map getObjectsMap() {
             return objectsMap;
         }
 
         public String getPriorLastKey() {
             return priorLastKey;
         }        
     }
     
 }
