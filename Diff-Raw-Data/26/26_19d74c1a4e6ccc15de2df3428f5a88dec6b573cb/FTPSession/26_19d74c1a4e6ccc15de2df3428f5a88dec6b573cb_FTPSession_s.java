 package info.mikaelsvensson.ftpbackup.util;
 
 import info.mikaelsvensson.ftpbackup.model.FtpConnectionSettings;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPFileFilter;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class FTPSession {
 // ------------------------------ FIELDS ------------------------------
 
     public static final char FTP_PATH_SEPARATOR_CHAR = '/';
    public static final String FTP_PATH_SEPARATOR_REGEXP = "\\" + FTP_PATH_SEPARATOR_STRING;
     private static final String FTP_PATH_SEPARATOR_STRING = String.valueOf(FTP_PATH_SEPARATOR_CHAR);
     private static final Logger logger = Logger.getLogger(FTPSession.class);
 
     private static FileTransferClientFactory clientFactory = new ApacheCommonsFTPClientFactory();
 
     private String workingDirectoryCached;
     private Integer serverTimeOffset;
 
     // TODO: rename homePath to something better to avoid confusion with initialPath
     private final String homePath;
     private final String initialPath;
 
     private final FileTransferClient client;
 
 // -------------------------- STATIC METHODS --------------------------
 
     public static void setClientFactory(FileTransferClientFactory clientFactory) {
         FTPSession.clientFactory = clientFactory;
     }
 
     public static FTPSession connect(FtpConnectionSettings settings, String initialPath) throws FTPSessionException {
         try {
             FileTransferClient ftpClient = clientFactory.create(settings, initialPath);
 
             ftpClient.connect(settings.getHost(), settings.getPort());
 
             if (settings.isPassive()) {
                 ftpClient.enterLocalPassiveMode();
             }
 
             boolean isAuthenticated = ftpClient.login(settings.getUserName(), settings.getPassWord());
             if (isAuthenticated) {
                 String homePath = ftpClient.printWorkingDirectory();
 
                 ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                 ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                 if (initialPath != null && initialPath.length() > 0) {
                     if (!ftpClient.changeWorkingDirectory(initialPath)) {
                         try {
                             ftpClient.logout();
                             ftpClient.quit();
                         } catch (IOException e) {
                             logger.warn("Could not close connection after failed attempt at changing initial working directory.", e);
                         }
                         throw new IOException("Could connect to host but could not change working directory to '" + initialPath + "'.");
                     }
                 }
 
                 return new FTPSession(ftpClient, ftpClient.printWorkingDirectory(), homePath);
             } else {
                 throw new FTPSessionException("Invalid user credentials");
             }
         } catch (IOException e) {
             throw new FTPSessionException(e);
         } catch (IllegalArgumentException e) {
             throw new FTPSessionException(e);
         }
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     private FTPSession(FileTransferClient client, String initialPath, String homePath) {
         this.client = client;
         this.initialPath = initialPath;
         this.homePath = homePath;
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public String getHomePath() {
         return homePath;
     }
 
     public String getInitialPath() {
         return initialPath;
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     public boolean changeToParentDirectory() {
         workingDirectoryCached = null;
         try {
             return client.changeToParentDirectory();
         } catch (IOException e) {
             return false;
         }
     }
 
     public boolean changeWorkingDirectory(String pathname) {
         workingDirectoryCached = null;
         try {
             return client.changeWorkingDirectory(pathname);
         } catch (IOException e) {
             return false;
         }
     }
 
     public boolean close() {
         try {
             if (!client.logout()) {
                 logger.warn("Could not log out before closing FTP connection.");
             }
             client.quit();
             return true;
         } catch (IOException e) {
             logger.warn("Could not close FTP connection because of this error: " +
                     e.getMessage());
             return false;
         }
     }
 
     public boolean deleteDirectory(String pathname) throws IOException {
         for (FTPFile dir : client.listDirectories()) {
             deleteDirectory(pathname + "/" + dir.getName());
         }
         for (FTPFile file : client.listFiles()) {
             client.deleteFile(pathname + "/" + file.getName());
         }
         return client.removeDirectory(pathname);
     }
 
     public boolean deleteFile(String pathname) throws IOException {
         return client.deleteFile(pathname);
     }
 
     public String getModificationTime(String pathname) throws IOException {
         return client.getModificationTime(pathname);
     }
 
     /**
      * The number of seconds that the server's clock is ahead of the local computer's clock. In
      * other words the method returns the number of seconds that one needs to add to the local time
      * in order to get the corresponding server time.
      */
     public int getServerTimeOffset() throws IOException {
         return client.getServerTimeOffset();
     }
 
     public String getWorkingDirectory() throws IOException {
         if (null == workingDirectoryCached) {
             workingDirectoryCached = client.printWorkingDirectory();
         }
         return workingDirectoryCached;
     }
 
     public FTPFile[] listDirectories() throws IOException {
         return client.listDirectories();
     }
 
     public FTPFile[] listDirectories(String parent) throws IOException {
         return client.listDirectories(parent);
     }
 
     public FTPFile[] listFiles() throws IOException {
         return client.listFiles();
     }
 
     public FTPFile[] listFiles(String pathname) throws IOException {
         return client.listFiles(pathname);
     }
 
     public FTPFile[] listFiles(String pathname, FTPFileFilter filter) throws IOException {
         return client.listFiles(pathname, filter);
     }
 
     public String[] listNames() throws IOException {
         return client.listNames();
     }
 
     public String[] listNames(String pathname) throws IOException {
         return client.listNames(pathname);
     }
 
     public boolean makeDirectory(String pathname) throws IOException {
         return client.makeDirectory(pathname);
     }
 
     @SuppressWarnings("UnusedReturnValue")
     public int quit() throws IOException {
         return client.quit();
     }
 
     public boolean rename(String oldName, String newName) throws IOException {
         return client.rename(oldName, newName);
     }
 
     private boolean storeFile(String remote, InputStream local) throws IOException {
         return client.storeFile(remote, local);
     }
 
     public boolean uploadFile(String localAbsolutePath) throws IOException {
         return client.storeFile(new File(localAbsolutePath).getName(), localAbsolutePath);
     }
 }
