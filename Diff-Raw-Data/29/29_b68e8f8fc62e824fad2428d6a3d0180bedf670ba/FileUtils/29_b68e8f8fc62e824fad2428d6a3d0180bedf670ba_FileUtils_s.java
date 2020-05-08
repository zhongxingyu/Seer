 /**
  * Licensed under the GNU LGPL v.2.1 or later.
  */
 
 package info.freelibrary.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.FileChannel;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Utilities for working with files.
  * 
  * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
  */
 public class FileUtils {
 
     private static final String DIR_TYPE = "dir";
 
     private static final String FILE_TYPE = "file";
 
     private static final String FILE_PATH = "path";
 
     private static final String MODIFIED = "modified";
 
     private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(FileUtils.class);
 
     /**
      * Constructor for the contained file utilities.
      */
     private FileUtils() {
     }
 
     /**
      * Creates a string of XML that describes the supplied file or directory
      * (and, possibly, all its subdirectories). Includes absolute path, last
      * modified time, read/write permissions, etc. By default, it only descends
      * one directory.
      * 
      * @param aFilePath A file system path to be turned into as XML
      * @return Am XML string representation of the file structure
      * @throws FileNotFoundException If the supplied file or directory can not
      *         be found
     * @throws ParserConfigurationException If the default XML parser for the
     *         JRE isn't configured correctly
      */
     public static String toXML(String aFilePath) throws FileNotFoundException,
             TransformerException {
         return toXML(aFilePath, ".*");
     }
 
     /**
      * Creates an <code>Element</code> that describes the supplied file or
      * directory (and, possibly, all its subdirectories). Includes absolute
      * path, last modified time, read/write permissions, etc. By default, it
      * only descends one directory.
      * 
      * @param aFilePath A file system path to turn into XML
      * @return An element representation of the file structure
      * @throws FileNotFoundException If the supplied file does not exist
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Element toElement(String aFilePath)
             throws FileNotFoundException, ParserConfigurationException {
         return toElement(aFilePath, ".*");
     }
 
     /**
      * Creates an <code>Element</code> that describes the supplied file or
      * directory (and, possibly, all its subdirectories). Includes absolute
      * path, last modified time, read/write permissions, etc.
      * 
      * @param aFilePath A file system path to turn into XML
      * @param aBool A boolean indicating whether the XML should contain more
      *        than one level
      * @return An element representation of the file structure
      * @throws FileNotFoundException If the supplied file does not exist
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Element toElement(String aFilePath, boolean aBool)
             throws FileNotFoundException, ParserConfigurationException {
         return toElement(aFilePath, ".*", aBool);
     }
 
     /**
      * Creates an <code>Document</code> that describes the supplied file or
      * directory (and, possibly, all its subdirectories). Includes absolute
      * path, last modified time, read/write permissions, etc. By default, it
      * only descends one directory.
      * 
      * @param aFilePath A file system path to turn into XML
      * @return A document representation of the file structure
      * @throws FileNotFoundException If the supplied file does not exist
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Document toDocument(String aFilePath)
             throws FileNotFoundException, ParserConfigurationException {
         return toDocument(aFilePath, ".*");
     }
 
     /**
      * Creates a string of XML that describes the supplied file or directory
      * (and all its subdirectories). Includes absolute path, last modified time,
      * read/write permissions, etc.
      * 
      * @param aFilePath The file or directory to be returned as XML
      * @param aPattern A regular expression that file names must match
      * @return A string of XML describing the supplied file system path's
      *         structure
      * @throws FileNotFoundException If the supplied file or directory can not
      *         be found
     * @throws ParserConfigurationException If the default XML parser for the
     *         JRE isn't configured correctly
      */
     public static String toXML(String aFilePath, String aPattern)
             throws FileNotFoundException, TransformerException {
         return toXML(aFilePath, aPattern, false);
     }
 
     /**
      * Creates a string of XML that describes the supplied file or directory
      * (and, optionally, all its subdirectories). Includes absolute path, last
      * modified time, read/write permissions, etc.
      * 
      * @param aFilePath The file or directory to be returned as XML
      * @param aDeepConversion Whether the subdirectories are included
      * @return A string of XML describing the supplied file system path's
      *         structure
      * @throws FileNotFoundException If the supplied file or directory can not
      *         be found
     * @throws ParserConfigurationException If the default XML parser for the
     *         JRE isn't configured correctly
      */
     public static String toXML(String aFilePath, boolean aDeepConversion)
             throws FileNotFoundException, TransformerException {
         return toXML(aFilePath, ".*", aDeepConversion);
     }
 
     /**
      * Creates a string of XML that describes the supplied file or directory
      * (and, optionally, all its subdirectories). Includes absolute path, last
      * modified time, read/write permissions, etc.
      * 
      * @param aFilePath The file or directory to be returned as XML
      * @param aPattern A regular expression pattern to evaluate file matches
      *        against
      * @param aDeepTransformation Whether the subdirectories are included
      * @return A string of XML describing the supplied file system path's
      *         structure
      * @throws FileNotFoundException If the supplied file or directory can not
      *         be found
      */
     public static String toXML(String aFilePath, String aPattern,
             boolean aDeepTransformation) throws FileNotFoundException,
             TransformerException {
         Element element = toElement(aFilePath, aPattern, aDeepTransformation);
         return DOMUtils.toXML(element);
     }
 
     /**
      * Returns a Map representation of the supplied directory's structure. The
      * map contains the file name as the key and its path as the value. If a
      * file with a name occurs more than once, multiple path values are returned
      * for that file name key. The map that is returned is unmodifiable.
      * 
      * @param aFilePath The directory of which you'd like a file listing
      * @return An unmodifiable map representing the files in the file structure
      * @throws FileNotFoundException If the directory for the supplied file path
      *         does not exist
      */
     public static Map<String, List<String>> toHashMap(String aFilePath)
             throws FileNotFoundException {
         return toHashMap(aFilePath, null, (String[]) null);
     }
 
     /**
      * Returns a Map representation of the supplied directory's structure. The
      * map contains the file name as the key and its path as the value. If a
      * file with a name occurs more than once, multiple path values are returned
      * for that file name key. The map that is returned is unmodifiable.
      * 
      * @param aFilePath The directory of which you'd like a file listing
      * @param aPattern A regular expression pattern which the files must match
      *        to be returned
      * @return An unmodifiable map representing the files in the file structure
      * @throws FileNotFoundException If the directory for the supplied file path
      *         does not exist
      */
     public static Map<String, List<String>> toHashMap(String aFilePath,
             String aPattern) throws FileNotFoundException {
         return toHashMap(aFilePath, aPattern, (String[]) null);
     }
 
     /**
      * Returns a Map representation of the supplied directory's structure. The
      * map contains the file name as the key and its path as the value. If a
      * file with a name occurs more than once, multiple path values are returned
      * for that file name key. The map that is returned is unmodifiable.
      * 
      * @param aFilePath The directory of which you'd like a file listing
      * @param aPattern A regular expression pattern which the files must match
      *        to be returned
      * @param aIgnoreList A list of directories into which we shouldn't descend
      * @return An unmodifiable map representing the files in the file structure
      * @throws FileNotFoundException If the directory for the supplied file path
      *         does not exist
      * @throws RuntimeException If a duplicate file path name is discovered
      */
     public static Map<String, List<String>> toHashMap(String aFilePath,
             String aPattern, String... aIgnoreList) throws RuntimeException,
             FileNotFoundException {
         String filePattern = aPattern != null ? aPattern : ".*";
         RegexFileFilter filter = new RegexFileFilter(filePattern);
         Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
         File source = new File(aFilePath);
 
         for (File file : listFiles(source, filter, true, aIgnoreList)) {
             String fileName = file.getName();
             String filePath = file.getAbsolutePath();
 
             if (fileMap.containsKey(fileName)) {
                 List<String> paths = fileMap.get(fileName);
 
                 if (!paths.contains(filePath)) {
                     paths.add(filePath);
                 } else {
                     throw new RuntimeException("Duplicate file path name");
                 }
             } else {
                 ArrayList<String> pathList = new ArrayList<String>();
                 pathList.add(filePath);
                 fileMap.put(fileName, pathList);
             }
         }
 
         return Collections.unmodifiableMap(fileMap);
     }
 
     /**
      * Returns an XML Element representing the file structure found at the
      * supplied file system path. Files included in the representation will
      * match the supplied regular expression pattern. This method doesn't
      * descend through the directory structure.
      * 
      * @param aFilePath The directory from which the structural representation
      *        should be built
      * @param aPattern A regular expression pattern which files included in the
      *        Element should match
      * @return An XML Element representation of the directory structure
      * @throws FileNotFoundException If the supplied directory isn't found
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Element toElement(String aFilePath, String aPattern)
             throws FileNotFoundException, ParserConfigurationException {
         return toElement(aFilePath, aPattern, false);
     }
 
     /**
      * Returns an XML Element representing the file structure found at the
      * supplied file system path. Files included in the representation will
      * match the supplied regular expression pattern. This method doesn't
      * descend through the directory structure.
      * 
      * @param aFilePath The directory from which the structural representation
      *        should be built
      * @param aPattern A regular expression pattern which files included in the
      *        Element should match
     * @param aDeepConversion Whether the conversion should descend through
      *        subdirectories
      * @return An XML Element representation of the directory structure
      * @throws FileNotFoundException If the supplied directory isn't found
     * @throws ParserConfigurationException If the default XML parser for the
     *         JRE isn't configured correctly
      */
     public static Element toElement(String aFilePath, String aPattern,
             boolean aDeepTransformation) throws FileNotFoundException {
         RegexFileFilter filter = new RegexFileFilter(aPattern);
         File file = new File(aFilePath);
 
         if (file.exists() && file.canRead()) {
             return add(file, null, filter, aDeepTransformation);
         }
 
         throw new FileNotFoundException(aFilePath);
     }
 
     /**
      * Returns an XML Document representing the file structure found at the
      * supplied file system path. Files included in the representation will
      * match the supplied regular expression pattern. This method doesn't
      * descend through the directory structure.
      * 
      * @param aFilePath The directory from which the structural representation
      *        should be built
      * @param aPattern A regular expression pattern which files included in the
      *        Element should match
      * @return An XML Document representation of the directory structure
      * @throws FileNotFoundException If the supplied directory isn't found
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Document toDocument(String aFilePath, String aPattern)
             throws FileNotFoundException, ParserConfigurationException {
         return toDocument(aFilePath, aPattern, false);
     }
 
     /**
      * Returns an XML Document representing the file structure found at the
      * supplied file system path. Files included in the representation will
      * match the supplied regular expression pattern. This method doesn't
      * descend through the directory structure.
      * 
      * @param aFilePath The directory from which the structural representation
      *        should be built
      * @param aPattern A regular expression pattern which files included in the
      *        Document should match
      * @param aDeepConversion Whether the conversion should descend through
      *        subdirectories
      * @return An XML Document representation of the directory structure
      * @throws FileNotFoundException If the supplied directory isn't found
      * @throws ParserConfigurationException If the default XML parser for the
      *         JRE isn't configured correctly
      */
     public static Document toDocument(String aFilePath, String aPattern,
             boolean aDeepConversion) throws FileNotFoundException,
             ParserConfigurationException {
         Element element = toElement(aFilePath, aPattern, aDeepConversion);
         return element.getOwnerDocument();
     }
 
     /**
      * Returns a Java <code>File</code> for the supplied file-based URL.
      * 
      * @param aURL A URL that has a file protocol
      * @return A Java <code>File</code> for the supplied URL
      * @throws MalformedURLException If the supplied URL doesn't have a file
      *         protocol
      */
     public static File toFile(URL aURL) throws MalformedURLException {
         if (aURL.getProtocol().equals("file")) {
             return new File(aURL.toString().replace("file:", ""));
         }
 
         throw new MalformedURLException("Not a file URL");
     }
 
     /**
      * An array of all the files in the supplied directory that match the
      * supplied <code>FilenameFilter</code>.
      * 
      * @param aDir A directory from which a file listing should be returned
      * @param aFilter A file name filter which returned files should match
      * @return An array of matching files
      * @throws FileNotFoundException If the supplied directory doesn't exist
      */
     public static File[] listFiles(File aDir, FilenameFilter aFilter)
             throws FileNotFoundException {
         return listFiles(aDir, aFilter, false, (String[]) null);
     }
 
     /**
      * An array of all the files in the supplied directory that match the
      * supplied <code>FilenameFilter</code>.
      * 
      * @param aDir A directory from which a file listing should be returned
      * @param aFilter A file name filter which returned files should match
      * @param aDeepListing Whether we should descend through subdirectories
      * @return An array of matching files
      * @throws FileNotFoundException If the supplied directory doesn't exist
      */
     public static File[] listFiles(File aDir, FilenameFilter aFilter,
             boolean aDeepListing) throws FileNotFoundException {
         return listFiles(aDir, aFilter, aDeepListing, (String[]) null);
     }
 
     /**
      * An array of all the files in the supplied directory that match the
      * supplied <code>FilenameFilter</code>. Directories that match the supplied
      * ignore list will not be included in the result.
      * 
      * @param aDir A directory from which a file listing should be returned
      * @param aFilter A file name filter which returned files should match
      * @param aDeepListing Whether we should descend through subdirectories
      * @param aIgnoreList Directory names that should be ignored in the list.
      * @return An array of matching files
      * @throws FileNotFoundException If the supplied directory doesn't exist
      */
     public static File[] listFiles(File aDir, FilenameFilter aFilter,
             boolean aDeepListing, String... aIgnoreList)
             throws FileNotFoundException {
         if (!aDir.exists()) {
             throw new FileNotFoundException(aDir.getAbsolutePath());
         }
 
         if (aDir.isFile()) {
             File parent = aDir.getParentFile();
             boolean accept = aFilter.accept(parent, aDir.getName());
 
             return accept ? new File[] {
                 aDir
             } : new File[0];
         }
 
         if (!aDeepListing) {
             return aDir.listFiles(aFilter);
         } else {
             ArrayList<File> fileList = new ArrayList<File>();
             String[] ignoreList;
 
             if (aIgnoreList == null) {
                 ignoreList = new String[0];
             } else {
                 ignoreList = aIgnoreList;
             }
 
             for (File file : aDir.listFiles()) {
                 String fileName = file.getName();
 
                 if (aFilter.accept(aDir, fileName)) {
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Match file: {}", file);
                     }
 
                     fileList.add(file);
                 }
 
                 if (file.isDirectory() &&
                         Arrays.binarySearch(ignoreList, fileName) < 0) {
                     if (LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Descending into: {}", file);
                     }
 
                     File[] files = listFiles(file, aFilter, aDeepListing);
                     fileList.addAll(Arrays.asList(files));
                 }
 
             }
 
             return fileList.toArray(new File[fileList.size()]);
         }
     }
 
     /**
      * Return a file name without the dot extension.
      * 
      * @param aFile The file name
      * @return The file name without the extension
      */
     public static String stripExt(File aFile) {
         return stripExt(aFile.getName());
     }
 
     /**
      * Return a file name without the dot extension.
      * 
      * @param aFileName The file name from which we want to strip the extension
      * @return The file name without the extension
      */
     public static String stripExt(String aFileName) {
         int index = aFileName.lastIndexOf('.');
 
         if (index != -1) {
             return aFileName.substring(0, index);
         }
 
         return aFileName;
     }
 
     /**
      * Gets the calculated size of a directory of files.
      * 
      * @param aFile A file or directory from which to calculate size
      * @return The calculated size of the supplied directory or file
      */
     public static long getSize(File aFile) {
         long size = 0;
 
         if (aFile != null && aFile.exists()) {
             size += aFile.length();
 
             if (aFile.isDirectory()) {
                 for (File file : aFile.listFiles()) {
                     size += getSize(file);
                 }
             }
         }
 
         return size;
     }
 
     /**
      * Deletes a directory and all its children.
      * 
      * @param aDir A directory to delete
      */
     public static boolean delete(File aDir) {
         if (aDir.exists()) {
             File[] files = aDir.listFiles();
 
             if (files != null) {
                 for (int index = 0; index < files.length; index++) {
                     if (files[index].isDirectory()) {
                         delete(files[index]);
                     } else {
                         if (!files[index].delete() && LOGGER.isWarnEnabled()) {
                             LOGGER.warn("Unable to delete: " +
                                     files[index].getAbsolutePath());
                         }
                     }
                 }
             }
         }
 
         return aDir.delete();
     }
 
     /**
      * Copies a file or directory from one place to another. This copies a file
      * to a file or a directory to a directory. It does not copy a file to a
      * directory.
      * 
      * @param aFromFile A file or directory source
      * @param aToFile A file or directory destination
      * @throws IOException If there is an exception copying the files or
      *         directories
      */
     public static void copy(File aFromFile, File aToFile) throws IOException {
         if (aFromFile.isDirectory() && aToFile.isFile() || aFromFile.isFile() &&
                 aToFile.isDirectory()) {
             throw new IOException(
                     "Can't copy file to directory or directory to file");
         }
 
         if (aFromFile.isDirectory()) {
             if (!aToFile.exists() && !aToFile.mkdirs()) {
                 throw new RuntimeException("Unable to create new directory: " +
                         aToFile.getAbsolutePath());
             }
 
             for (File file : aFromFile.listFiles()) {
                 copy(file, new File(aToFile, file.getName()));
             }
         } else {
             copyFile(aFromFile, aToFile);
         }
     }
 
     /**
      * Returns a human readable size from a large number of bytes.
      * 
      * @param aByteCount A large number of bytes
      * @return A human readable size
      */
     public static String sizeFromBytes(long aByteCount) {
         return sizeFromBytes(aByteCount, false);
     }
 
     /**
      * Returns a human readable size from a large number of bytes. You can
      * specify that the human readable size use an abbreviated label (e.g., GB
      * or MB).
      * 
      * @param aByteCount A large number of bytes
      * @param aAbbreviatedLabel Whether the label should be abbreviated
      * @return A human readable size
      */
     public static String sizeFromBytes(long aByteCount,
             boolean aAbbreviatedLabel) {
         long count;
 
         if ((count = aByteCount / 1073741824) > 0) {
             return count + (aAbbreviatedLabel ? " GB" : " gigabytes");
         } else if ((count = aByteCount / 1048576) > 0) {
             return count + (aAbbreviatedLabel ? " MB" : " megabytes");
         } else if ((count = aByteCount / 1024) > 0) {
             return count + (aAbbreviatedLabel ? " KB" : " kilobytes");
         }
 
         return count + (aAbbreviatedLabel ? " B" : " bytes");
     }
 
     /**
      * Get a hash for a supplied file.
      * 
      * @param aFile A file to get a hash for
      * @param aAlgorithm A hash algorithm supported by
      *        <code>MessageDigest</code>
      * @return A hash string
      * @throws NoSuchAlgorithmException If the supplied algorithm isn't
      *         supported
      * @throws IOException If there is trouble reading the file
      */
     public static String hash(File aFile, String aAlgorithm)
             throws NoSuchAlgorithmException, IOException {
         MessageDigest md = MessageDigest.getInstance(aAlgorithm);
         FileInputStream inStream = new FileInputStream(aFile);
         DigestInputStream mdStream = new DigestInputStream(inStream, md);
         byte[] bytes = new byte[8192];
         int bytesRead = 0;
 
         while (bytesRead != -1) {
             bytesRead = mdStream.read(bytes);
         }
 
         Formatter formatter = new Formatter();
         String hash;
 
         for (byte bite : md.digest()) {
             formatter.format("%02x", bite);
         }
 
         IOUtils.closeQuietly(mdStream);
         hash = formatter.toString();
         formatter.close();
 
         return hash;
     }
 
     /**
      * Gets the MIME type of the supplied file URL. It gets MIME type from
      * {@link URLConnection}'s file name map.
      * 
      * @param aFileUrl A file-based URL
      * @return The MIME-type name for the supplied file
      * @throws IOException
      */
     public static String getMimeType(String aFileUrl) throws IOException {
         return URLConnection.getFileNameMap().getContentTypeFor(aFileUrl);
     }
 
     /**
      * Copies a non-directory file from one location to another.
      * 
      * @param aSourceFile A file to copy
      * @param aDestFile A destination location for the copied file
      * @return True if the copy was successful; else, false
      * @throws IOException If there is a problem copying the file
      */
     private static boolean copyFile(File aSourceFile, File aDestFile)
             throws IOException {
         FileOutputStream outputStream = null;
         FileInputStream inputStream = null;
         boolean success = true;
 
         // destructive copy
         if (aDestFile.exists() && !aDestFile.delete()) {
             success = false;
         }
 
         if (success && !aDestFile.createNewFile()) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Failed to create: " + aDestFile.getAbsolutePath());
             }
 
             success = false;
         }
 
         if (success) {
             try {
                 outputStream = new FileOutputStream(aDestFile);
                 inputStream = new FileInputStream(aSourceFile);
 
                 FileChannel source = inputStream.getChannel();
                 outputStream.getChannel()
                         .transferFrom(source, 0, source.size());
             } finally {
                 IOUtils.closeQuietly(outputStream);
                 IOUtils.closeQuietly(inputStream);
             }
         }
 
         if (success && aDestFile.exists() && aSourceFile.canRead()) {
             success = aDestFile.setReadable(true, true);
         }
 
         if (success && aDestFile.exists() && aSourceFile.canWrite()) {
             success = aDestFile.setWritable(true, true);
         }
 
         if (success && aDestFile.exists() && aSourceFile.canExecute()) {
             success = aDestFile.setExecutable(true, true);
         }
 
         if (!success && !aDestFile.delete() && LOGGER.isWarnEnabled()) {
             LOGGER.warn("Unable to delete: " + aDestFile.getAbsolutePath());
         }
 
         return success;
     }
 
     private static Element add(File aFile, Element aParent,
             RegexFileFilter aFilter, boolean aDeepAdd)
             throws FileNotFoundException {
         Element element;
         String tagName;
 
         if (aFile.isDirectory()) {
             tagName = DIR_TYPE;
         } else {
             tagName = FILE_TYPE;
         }
 
         if (aParent == null) {
             try {
                 DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                 Document doc = f.newDocumentBuilder().newDocument();
 
                 element = doc.createElement(tagName);
             } catch (ParserConfigurationException details) {
                 throw new RuntimeException(details);
             }
         } else {
             element = aParent.getOwnerDocument().createElement(tagName);
             aParent.appendChild(element);
         }
 
         copyMetadata(aFile, element);
 
         if (aFile.isDirectory()) {
             if (aDeepAdd) {
                 for (File dir : listFiles(aFile, new RegexDirFilter(".*"))) {
                     element.appendChild(add(dir, element, aFilter, aDeepAdd));
                 }
             } else {
                 Document doc = element.getOwnerDocument();
 
                 for (File dir : listFiles(aFile, new RegexDirFilter(".*"))) {
                     Element dirElem = doc.createElement(DIR_TYPE);
 
                     element.appendChild(dirElem);
                     copyMetadata(dir, dirElem);
                 }
             }
 
             for (File file : listFiles(aFile, aFilter)) {
                 element.appendChild(add(file, element, aFilter, aDeepAdd));
             }
         }
 
         return element;
     }
 
     /**
      * Copy file metadata into the supplied file element.
      * 
      * @param aFile A file to extract metadata from
      * @param aElement A destination element for the file metadata
      */
     private static void copyMetadata(File aFile, Element aElement) {
         SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
         StringBuilder permissions = new StringBuilder();
         Date date = new Date(aFile.lastModified());
 
         aElement.setAttribute(FILE_PATH, aFile.getAbsolutePath());
         aElement.setAttribute(MODIFIED, formatter.format(date));
 
         if (aFile.canRead()) {
             permissions.append('r');
         }
 
         if (aFile.canWrite()) {
             permissions.append('w');
         }
 
         aElement.setAttribute("permissions", permissions.toString());
     }
 }
