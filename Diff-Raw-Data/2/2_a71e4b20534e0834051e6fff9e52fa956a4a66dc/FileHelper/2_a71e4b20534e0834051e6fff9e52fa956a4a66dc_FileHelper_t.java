 package gov.usgs.cida.utilities.file;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility class that helps with FileIO operations
  *
  * @author isuftin
  *
  */
 public class FileHelper {
 
     private static org.slf4j.Logger log = LoggerFactory.getLogger(FileHelper.class);
 
     /**
      * @see FileHelper#base64Encode(byte[])
      * @param input
      * @return
      * @throws IOException
      */
     public static byte[] base64Encode(final File input) throws IOException {
         byte[] result = null;
        
         result = FileHelper.base64Encode(FileHelper.getByteArrayFromFile(input));
 
         return result;
     }
 
     /**
      * Provides Base64 encoding and decoding as defined by <a
      * href="http://tools.ietf.org/html/rfc2045">RFC 2045</a>.
      *
      * @param input
      * @return Byte array representing the base 64 encoding of the incoming File
      * or byte array
      */
     public static byte[] base64Encode(final byte[] input) {
         if (input == null) {
             return (byte[]) Array.newInstance(byte.class, 0);
         }
 
         log.trace(new StringBuilder("Attempting to base64 encode a byte array of ").append(input.length).append(" bytes.").toString());
 
         byte[] result = null;
 
         Base64 encoder = new Base64();
 
         result = encoder.encode(input);
 
         return result;
     }
 
     /**
      * Reads a file into a byte array
      *
      * @param file
      * @return a byte array representation of the incoming file
      * @throws IOException
      */
     public static byte[] getByteArrayFromFile(File file) throws IOException {
         if (file == null) {
             return (byte[]) Array.newInstance(byte.class, 0);
         }
 
         log.debug(new StringBuilder("Attempting to get a byte array from file: ").append(file.getPath()).toString());
 
         // Get the size of the file
         long length = file.length();
 
         // Maximum size of file cannot be larger than the Integer.MAX_VALUE
         if (length > Integer.MAX_VALUE) {
             throw new IOException("File is too large: File length: " + file.length() + " bytes. Maximum length: " + Integer.MAX_VALUE + " bytes.");
         }
 
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int) length];
 
         // Read in the bytes
         int offset = 0, numRead = 0;
 
         InputStream is = null;
         try {
             is = new FileInputStream(file);
             while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                 offset += numRead;
             }
         } finally {
             if (is != null) {
                 is.close();
             }
         }
 
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file " + file.getName());
         }
         log.debug(new StringBuilder("Successfully attained a byte array from file: ").append(file.getPath()).toString());
         return bytes;
     }
 
     /**
      * Performs a safe renaming of a file. First copies old file to new file,
      * then if new file exists, removes old file.
      *
      * @param fromFile
      * @param toFileName
      * @return true if succeeded, false if not
      * @throws IOException
      */
     public static boolean renameFile(final File fromFile, final String toFileName) throws IOException {
         File toFile = new File(fromFile.getParent() + File.separator + toFileName);
 
         FileUtils.copyFile(fromFile, toFile);
 
         if (!toFile.exists()) {
             return false;
         }
 
         return fromFile.delete();
     }
 
     /**
      * Performs a filecopy without deleting the original file
      *
      * @see FileHelper#copyFileToPath(java.io.File, java.lang.String, boolean)
      * @param inFile
      * @param outFilePath
      * @return
      * @throws IOException
      */
     public static boolean copyFileToPath(final File inFile, final String outFilePath) throws IOException {
         return FileHelper.copyFileToPath(inFile, outFilePath, false);
     }
 
     /**
      * Copies a File object (directory or file) to a given location Is able to
      * handle
      *
      * @param inFile File to be copied
      * @param outPath Destination where to copy to
      * @param deleteOriginalFile - effectively makes this function as a MOVE
      * command instead of a COPY command
      * @return true if file properly copied, otherwise false
      * @throws IOException
      */
     public static boolean copyFileToPath(final File inFile, final String outPath, boolean deleteOriginalFile) throws IOException {
         if (inFile.isDirectory()) {
             FileUtils.copyDirectory(inFile, (new File(outPath + File.separator + inFile.getName())));
         } else {
             FileUtils.copyFile(inFile, (new File(outPath + File.separator + inFile.getName())));
         }
 
         if (deleteOriginalFile) {
             FileUtils.deleteQuietly(inFile);
         }
 
         return true;
     }
 
     /**
      * Delete files older than a given Long instance
      *
      * @param directory Directory within which to search.
      * @param cutoffTime
      * @param deleteDirectory Also delete the directory given in the directory
      * param
      * @return files that were deleted
      */
     public static Collection<File> wipeOldFiles(File directory, Long cutoffTime, boolean deleteDirectory) {
         if (directory == null || !directory.exists()) {
             return new ArrayList<File>();
         }
 
         Collection<File> result = new ArrayList<File>();
         Collection<File> oldFiles = FileHelper.getFilesOlderThan(directory, cutoffTime, Boolean.TRUE);
         for (File file : oldFiles) {
             String logString = "Deleting File: \"" + file.toString() + "\" ... ";
 
             if (file.canWrite() && file.delete()) {
                 logString += "done. ";
                 result.add(file);
                 if (file.getParentFile().isDirectory()) {
                     if (file.getParentFile() != directory && file.getParentFile().delete()) {
                         log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
                     } else if (file.getParentFile() == directory && deleteDirectory) {
                         log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
                     }
                 }
             } else {
                 logString += "FAILED!";
             }
             log.info(logString);
         }
 
         return result;
     }
 
     /**
      * Creates a directory according to the passed in File object
      *
      * @see FileHelper#createDir(java.lang.String)
      * @param directory
      * @return true if directory has been created, false if not
      */
     public static boolean createDir(File directory) {
         return FileHelper.createDir(directory.toString());
     }
 
     /**
      * Creates a directory in the filesystem according to the passed in String
      * object
      *
      * @param directory
      * @param removeAtSysExit
      * @return boolean true if already exists or created, false if directory
      * could not be created
      */
     public static boolean createDir(String directory) {
         boolean result = false;
         if (FileHelper.doesDirectoryOrFileExist(directory)) {
             return true;
         }
         result = new File(directory).mkdirs();
         return result;
     }
 
     /**
      * Recursively deletes a directory from the filesystem.
      *
      * @param directory
      * @return
      */
     public static boolean deleteDirRecursively(File directory) throws IOException {
         if (!directory.exists()) {
             return false;
         }
         FileUtils.deleteDirectory(directory);
         return true;
     }
 
     /**
      * Recursively deletes a directory from the filesystem.
      *
      * @param directory
      * @return
      */
     public static boolean deleteDirRecursively(String directory) throws IOException {
         boolean result = false;
         File dir = new File(directory);
         if (!dir.exists()) {
             return false;
         }
         result = FileHelper.deleteDirRecursively(dir);
         return result;
     }
 
     /**
      * Deletes a file at the location of the passed in String object.
      *
      * @param filePath
      * @return true if file has been deleted, false otherwise
      */
     public static boolean deleteFileQuietly(String filePath) {
         return FileUtils.deleteQuietly(new File(filePath));
     }
 
     /**
      * Deletes a file at the location of the passed in File object.
      *
      * @see FileHelper#deleteFileQuietly(java.lang.String)
      * @param filePath
      * @return true if file has been deleted, false otherwise
      */
     public static boolean deleteFileQuietly(File file) {
         return FileUtils.deleteQuietly(file);
     }
 
     /**
      * @see FileHelper.deleteFile
      *
      * @param filePath
      * @return
      * @throws SecurityException
      */
     public static boolean deleteFile(String filePath) throws SecurityException {
         if ("".equals(filePath)) {
             return false;
         }
         return FileHelper.deleteFile(new File(filePath));
     }
 
     /**
      * Deletes a file from the file system
      *
      * @param file - method returns false if File object passed in was null
      * @return true if file was deleted, false if not
      * @throws SecurityException
      */
     public static boolean deleteFile(File file) throws SecurityException {
         if (file == null) {
             return false;
         }
         return file.delete();
     }
 
     /**
      * Tests whether or not a directory or file exists given the passed String
      * representing a file/directory location
      *
      * @param filePath
      * @return
      */
     public static boolean doesDirectoryOrFileExist(String filePath) {
         return new File(filePath).exists();
     }
 
     /**
      * Attempts to find a file by recursively going through a given directory
      *
      * @param file The file that is being searched for
      * @param rootPath The path to begin looking from
      * @return the first file that was found
      */
     public static File findFile(String file, String rootPath) {
         if (rootPath == null || "".equals(rootPath)) {
             return null;
         }
         File result = null;
         Collection<File> fileCollection = FileUtils.listFiles(new File(rootPath), new String[]{file.substring(file.lastIndexOf('.') + 1)}, true);
         if (fileCollection.isEmpty()) {
             return result;
         }
         Iterator<File> fileCollectionIterator = fileCollection.iterator();
         while (fileCollectionIterator.hasNext()) {
             File testFile = fileCollectionIterator.next();
             if (file.equals(testFile.getName())) {
                 result = testFile;
             }
         }
         return result;
     }
 
     /**
      * Get recursive directory listing
      *
      * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
      * boolean)
      * @param filePath the path to begin looking through
      * @param recursive whether or not the function should look only at base
      * level or recursively
      * @return a list of strings that represent the path to the files found
      * @throws IllegalArgumentException
      */
     public static List<String> getFileList(String filePath, boolean recursive) throws IllegalArgumentException {
         List<String> result = null;
 
         result = FileHelper.getFileList(filePath, null, recursive);
 
         return result;
     }
 
     /**
      * Get recursive directory listing
      *
      * @param filePath the path to begin looking through
      * @param extensions a list of extensions to match on
      * @param recursive whether or not the function should look only at base
      * level or recursively
      * @return a list of strings that represent the path to the files found
      * @throws IllegalArgumentException
      */
     public static List<String> getFileList(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
         if (filePath == null) {
             return null;
         }
         List<String> result = null;
         Collection<File> fileList = null;
         fileList = FileUtils.listFiles((new File(filePath)), extensions, recursive);
         result = new ArrayList<String>();
 
         for (File file : fileList) {
             result.add(file.getName());
         }
 
         return result;
     }
 
     /**
      * Returns a Collection of type File
      *
      * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
      * boolean)
      * @param filePath the path to begin looking through
      * @param recursive whether or not the function should look only at base
      * level or recursively
      * @return a collection of type File of files found at the directory point
      * given
      */
     public static Collection<File> getFileCollection(String filePath, boolean recursive) throws IllegalArgumentException {
         return (Collection<File>) FileHelper.getFileCollection(filePath, null, recursive);
     }
 
     /**
      * Returns a Collection of type File
      *
      * @see FileHelper#getFileCollection(java.lang.String, java.lang.String[],
      * boolean)
      * @param filePath the path to begin looking through
      * @param extensions a list of extensions to match on
      * @param recursive whether or not the function should look only at base
      * level or recursively
      * @return a collection of type File of files found at the directory point
      * given
      */
     public static Collection<?> getFileCollection(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
         if (filePath == null) {
             return null;
         }
 
         Collection<File> result = null;
         Object interimResult = FileUtils.listFiles((new File(filePath)), extensions, recursive);
         if (interimResult instanceof Collection<?>) {
             result = (Collection<File>) interimResult;
         }
         return result;
     }
 
     /**
      * Returns the temp directory specific to the operating system
      *
      * @see System.getProperty("java.io.tmpdir")
      * @return
      */
     public static String getSystemTemp() {
         String result = "";
 
         result = System.getProperty("java.io.tmpdir");
 
         return result;
     }
 
     /**
      * Takes a zip file and unzips it to a outputDirectory
      *
      * @param outputDirectory
      * @param zipFile
      * @return
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static boolean unzipFile(String outputDirectory, File zipFile) throws FileNotFoundException, IOException {
         FileInputStream fis = new FileInputStream(zipFile);
         ZipInputStream zis = null;
         BufferedOutputStream dest = null;
         try {
             zis = new ZipInputStream(new BufferedInputStream(fis));
             ZipEntry entry = null;
 
 
             final int BUFFER = 2048;
             while ((entry = zis.getNextEntry()) != null) {
                 String fileName = entry.getName();
                 int count = 0;
                 byte data[] = new byte[BUFFER];
                 // Get the final filename (even if it's within directories in the ZIP file)
                 String destinationFileName = entry.getName().contains(File.pathSeparator) ? entry.getName().substring(entry.getName().lastIndexOf(File.pathSeparator)) : entry.getName();
                 String destinationPath = outputDirectory + java.io.File.separator + destinationFileName;
                 FileOutputStream fos = new FileOutputStream(destinationPath);
                 dest = new BufferedOutputStream(fos, BUFFER);
                 log.debug(new StringBuilder("Unzipping: ").append(fileName).append(" to ").append(destinationPath).toString());
                 while ((count = zis.read(data, 0, BUFFER)) != -1) {
                     dest.write(data, 0, count);
                 }
                 dest.flush();
                 dest.close();
                 log.trace(new StringBuilder("Unzipped: ").append(fileName).append(" to ").append(destinationPath).toString());
             }
         } finally {
             if (zis != null) {
                 IOUtils.closeQuietly(zis);
             }
             if (dest != null) {
                 IOUtils.closeQuietly(dest);
             }
         }
         return true;
     }
 
     /**
      * Creates a unique user directory
      *
      * @param applicationUserSpaceDir User directory created
      * @return
      */
     public static String createUserDirectory(String applicationUserSpaceDir) {
         String userSubDir = Long.toString(new Date().getTime());
 
         //String applicationUserSpaceDir = System.getProperty("applicationUserSpaceDir");
         String seperator = File.separator;
         String userTempDir = applicationUserSpaceDir + seperator + userSubDir;
         if (FileHelper.createDir(userTempDir)) {
             log.debug("User subdirectory created at: " + userTempDir);
             return userSubDir;
         }
         log.warn(new StringBuilder("User subdirectory could not be created at: " + userSubDir).toString());
         log.debug("User will be unable to upload files for this session.");
         return "";
     }
 
     /**
      * Updates the time stamp on a file or a list of files within a given
      * directory
      *
      * @param path Path to file or directory
      * @param recursive If path parameter is a directory and this param is true,
      * will attempt to update the timestamp on all files within the directory to
      * current time
      * @return true if updating succeeded, false if not
      * @throws IOException
      */
     public static boolean updateTimestamp(final String path, final boolean recursive) throws IOException {
         if (path == null || "".equals(path)) {
             return false;
         }
         if (!FileHelper.doesDirectoryOrFileExist(path)) {
             return false;
         }
 
         if (recursive) {
             Iterator<File> files = FileUtils.iterateFiles(new File(path), null, true);
             while (files.hasNext()) {
                 File file = files.next();
                 FileUtils.touch(file); // update date on file
                 log.debug(new StringBuilder("Updated timestamp on file: ").append(file.getPath()).toString());
             }
         } else {
             FileUtils.touch(new File(path));
             log.debug(new StringBuilder("Updated timestamp on file: ").append(new File(path).getPath()).toString());
         }
         return true;
     }
 
     /**
      * Returns files and directories older that a specified date
      *
      * @param filePath System path to the directory
      * @param age
      * @param msPerDay
      * @param recursive
      * @return
      */
     static Collection<File> getFilesOlderThan(File filePath, Long age, Boolean recursive) {
         if (filePath == null || !filePath.exists()) {
             return new ArrayList<File>();
         }
         Iterator<File> files = null;
 
         if (recursive.booleanValue()) {
             files = FileUtils.iterateFiles(filePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
         } else {
             files = FileUtils.iterateFiles(filePath, TrueFileFilter.INSTANCE, null);
         }
 
         Collection<File> result = new ArrayList<File>();
         Date date = new Date();
         while (files.hasNext()) {
             File file = files.next();
 
             if (file.lastModified() < date.getTime() - age.longValue()) {
                 result.add(file);
                 log.trace(new StringBuilder("Added ").append(file.getPath()).append(" to \"old files list\".").toString());
             }
         }
 
         return result;
     }
 
     public static void saveFileFromInputStream(InputStream is, File destinationFile) throws IOException {
         FileOutputStream os = null;
         try {
             os = new FileOutputStream(destinationFile);
             IOUtils.copy(is, os);
         } finally {
             IOUtils.closeQuietly(os);
         }
     }
 
     public static File zipFile(File file, String newName, FileFilter filter) throws FileNotFoundException, IOException {
         String zipFileName = StringUtils.isBlank(newName) ? file.getName() : newName;
 
         FileOutputStream fos;
         ZipOutputStream zos = null;
         FileInputStream fis;
 
         File[] files;
         File zipFile;
         if (file.isDirectory()) {
             files = file.listFiles(filter != null ? filter : new WildcardFileFilter("*"));
             zipFile = new File(file.getPath() + File.separator + zipFileName + ".zip");
         } else {
             files = new File[] {file};
             zipFile = new File(file.getParentFile().getPath() + File.separator + zipFileName + ".zip");
         }
 
         try {
             fos = new FileOutputStream(zipFile);
             zos = new ZipOutputStream(fos);
             
             for (File fileItem : files) {
                 fis = new FileInputStream(fileItem);
                 ZipEntry ze = new ZipEntry(fileItem.getName());
                 zos.putNextEntry(ze);
                 IOUtils.copy(fis, zos);
                 IOUtils.closeQuietly(fis);
             }
             IOUtils.closeQuietly(zos);
             return zipFile;
         } finally {
             IOUtils.closeQuietly(zos);
         }
     }
 }
