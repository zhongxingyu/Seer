 package cinnamon;
 
 import cinnamon.exceptions.CinnamonException;
 import cinnamon.global.Conf;
 import cinnamon.global.ConfThreadLocal;
 import cinnamon.utils.FileKeeper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.util.UUID;
 
 public class ContentStore {
 
     static final String sep = File.separator;
 
     /**
      * Create a 3-tier folder hierarchy named after the first 6 letters of the file's UUID
      * in the repository's data folder,
      * and copy the file into it. See upload method for more details.
      *
      * @param sourcePath path to the file to copy
      * @param repository the repository to which the file belongs
      * @return String
      * @throws java.io.IOException if something IO-related goes wrong (like disk full)
      */
     // TODO: find a better method name
     public static String copyToContentStore(String sourcePath, String repository) throws IOException {
         Logger log = LoggerFactory.getLogger(ContentStore.class);
         Conf conf = ConfThreadLocal.getConf();
 
         File source = new File(sourcePath);
         String targetName = UUID.randomUUID().toString(); // GUID
         String subfolderName = getSubFolderName(targetName);
 
         // TODO: refactor identical code between upload&copy-tocontentstore.
 
         String subfolderPath = conf.getDataRoot() + repository + sep
                 + subfolderName;
         File subfolder = new File(subfolderPath);
         boolean result = subfolder.mkdirs();
         log.debug("Result of mkdir: " + result);
         if (!result && !subfolder.isDirectory()) {
             throw new IOException("Could not create directory " + subfolderPath);
         }
 
         String targetPath = subfolderPath + sep + targetName;
         copyFile(source, new File(targetPath));
         return subfolderName + sep + targetName;
     }
 
     public static void copyFile(File src, File dst) throws IOException {
         InputStream inStream = new FileInputStream(src);
         copyStreamToFile(inStream, dst);
         inStream.close();
     }
 
     public static void copyStreamToFile(InputStream inStream, File target) throws IOException {
         OutputStream out = new FileOutputStream(target);
 
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = inStream.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         out.close();
     }
 
     private static String getSubFolderName(String f) {
         return f.substring(0, 2) + sep
                 + f.substring(2, 4) + sep + f.substring(4, 6);
     }
 
     /**
      * Put a file into the content store folder. Folder structure is:
      * $cinnamon-data-dir / $repository name / 3 folders / UUID-filename.
      * The 3 folders consist of 2 hexadecimal characters each, taken from
      * the beginning of the filename of the stored file.
      * Example:
      * /home/cinnamon/cinnamon-data/cmn_test/c8/72/4d/c8724d35-997f-4cbd-9643-1721a4443956
      *
      * @param file       the uploaded file
      * @param repository the repository to which the file belongs
      * @return String
      * @throws java.io.IOException if something unexpected and fatal happens (like missing write
      *                     permissions)
      */
     public static String upload(UploadedFile file,
                                 String repository) throws IOException {
         Logger log = LoggerFactory.getLogger(ContentStore.class);
         Conf conf = ConfThreadLocal.getConf();
 
         File f = new File(file.getFileBufferPath());
         String subfolderName = getSubFolderName(file.getName());
 
         String subfolderPath = conf.getDataRoot() + repository
                 + sep + subfolderName;
         File subfolder = new File(subfolderPath);
 
         boolean result = subfolder.mkdirs();    
         log.debug("Result of mkdir: " + result);
 
         String contentPath = subfolderPath + sep + file.getName();
 
         result = f.renameTo(new File(contentPath));               
         log.debug("Result of renameTo: " + contentPath + ": " + result);
         if(!result){
             log.debug("renameTo failed. Trying to copy.");
 //            if(new File(subfolderPath).canWrite()){
 //                log.warn("cannot write to "+subfolderPath);
 //                throw new IOException(subfolderPath+" is not writable.");
 //            }
             copyFile(f, new File(contentPath));
             log.debug("contentPath after copy: "+contentPath);
         }
                 
         if (!result && !subfolder.isDirectory()) {
             throw new IOException("Could not rename uploaded file " + contentPath);
         } else {
             return subfolderName + sep + file.getName();
         }
     }
 
     /**
      * Delete a file corresponding to an OSD object from the file system (and its
      * containing folder if it is empty after deletion)
      *
      * @param osd the OSD whose content is going to be deleted.
      */
     public static void deleteObjectFile(ObjectSystemData osd, String repository) {
         Logger log = LoggerFactory.getLogger(ContentStore.class);
         Conf conf = ConfThreadLocal.getConf();
         String contentPath = osd.getContentPath();
         if (contentPath != null && contentPath.length() > 0) {
             File contentFile = new File(conf.getDataRoot() + File.separator +
                     repository + File.separator + contentPath);
             log.debug("deleteContent: " + contentFile.getAbsolutePath());
 
             if (contentFile.exists()) {
                 log.debug("content exists, setting file up for later deletion.");
                 FileKeeper fileKeeper = FileKeeper.getInstance();
                 fileKeeper.addFileForDeletion(contentFile);
                 contentFile.deleteOnExit();
             }
             else {
                 log.warn("content file " + contentFile.getAbsolutePath() + "does not exist.");
             }
         }
     }
     
     /**
      * Delete a file from a repository. 
      *
      * @param contentPath the name of the file which is going to be deleted (as reported by osd.getContentPath)
      */
     public static void deleteFileInRepository(String contentPath, String repository) {
         Logger log = LoggerFactory.getLogger(ContentStore.class);
         Conf conf = ConfThreadLocal.getConf();
         if (contentPath != null && contentPath.length() > 0) {
             File contentFile = new File(conf.getDataRoot() + File.separator +
                     repository + File.separator + contentPath);
             log.debug("deleteContent: " + contentFile.getAbsolutePath());
 
             if (contentFile.exists()) {
                 log.debug("content exists, setting file up for later deletion.");
                 FileKeeper fileKeeper = FileKeeper.getInstance();
                 fileKeeper.addFileForDeletion(contentFile);
                 contentFile.deleteOnExit();
             }
             else {
                 log.warn("content file " + contentFile.getAbsolutePath() + "does not exist.");
             }
         }
     }
 
     /**
      * Replace the content of an OSD by importing a new file into the repository.
      * Note: this method will set the contentPath on the OSD and also the contentSize, but
      * will not touch the osd.format field.
      * @param osd the OSD into which the new content will be installed.
      * @param file the file containing the new content to copy into the repository.
      * @param repository the repository where the content will be stored.
      */
     public static void replaceContent(ObjectSystemData osd, File file, String repository, UserAccount user){
         Logger log = LoggerFactory.getLogger(ContentStore.class);
         Boolean removeLock = false;
         String contentPath = osd.getContentPath();
         try{
             if(osd.getLocker() == null){
                 osd.setLocker(user);
                 removeLock = true; 
             }
             if(osd.getLocker().equals(user)){
                 String newFilename = copyToContentStore(file.getAbsolutePath(), repository);
                 osd.setContentPath(newFilename);
             }
             else{
                 throw new CinnamonException("error.cannot.acquire.lock");
             }
             deleteFileInRepository(contentPath, repository);
         }
         catch (IOException e){
            log.error("Failed to replace content file on #"+osd.myId()+" with " +file.getAbsolutePath(),e);
             osd.setContentPath(contentPath);
             throw new CinnamonException(e);
         }
         finally {
             if(removeLock){
                 // only remove lock if the client has not already locked this object.
                 osd.setLocker(null);
             }
         }
         
     }
 
     /**
      * Create a byte array of the same size as the given OSD's content and set the
      * HttpServletResponse to binary/octed-stream for a binary attachment with name
      * = osd.getName().
      *
      * @param res HttpServletResponse
      * @param osd the object which is the source for the file content.
      * @return an empty byte-array of size=osd.content.size
      */
     public static byte[] getObjectFileContent(HttpServletResponse res,
                                               ObjectSystemData osd) {
         Long contentSize = osd.getContentSize();
         int fileSize = contentSize.intValue();
         // TODO: buffered streaming
         byte b[] = new byte[fileSize];
         res.setContentLength(fileSize);
         res.setContentType("binary/octet-stream"); // TODO: softcode with
         // detailed type info
         res.setHeader("Content-Disposition", "attachment; filename="
                 + osd.getName());
         return b;
     }
 
 }
