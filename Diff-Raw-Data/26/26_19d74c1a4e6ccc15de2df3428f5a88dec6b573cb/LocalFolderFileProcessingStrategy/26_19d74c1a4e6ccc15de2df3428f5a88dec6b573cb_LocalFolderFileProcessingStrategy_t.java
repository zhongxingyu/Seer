 package info.mikaelsvensson.ftpbackup.command.fileprocessing;
 
 import info.mikaelsvensson.ftpbackup.command.CancellationRequestedException;
 import info.mikaelsvensson.ftpbackup.command.CommandCancellationStatus;
 import info.mikaelsvensson.ftpbackup.model.LocalFolderDestination;
 import info.mikaelsvensson.ftpbackup.model.filesystem.LightFileSystemObject;
 import info.mikaelsvensson.ftpbackup.util.DefaultLocalFileSystemFolderReader;
 import info.mikaelsvensson.ftpbackup.util.FTPSession;
 import info.mikaelsvensson.ftpbackup.util.LocalFileSystemFolderReader;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.commons.net.ftp.FTPFile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 @SuppressWarnings("UnusedDeclaration")
 public class LocalFolderFileProcessingStrategy extends AbstractFileProcessingStrategy {
 // ------------------------------ FIELDS ------------------------------
 
     private static final boolean USE_LOCAL_BASE_FOLDER_CACHE = true; // Must be TRUE in order to ensure correct local file set statistics when dealing with multi-destination jobs.
    private static final LocalFileSystemFolderReader LOCAL_FILE_SYSTEM_FOLDER_READER = new DefaultLocalFileSystemFolderReader(null, null, USE_LOCAL_BASE_FOLDER_CACHE);
 
     private File initialDirectory;
     private File currentDirectory;
     private final LocalFolderDestination destination;
     private File lastDirectory;
     private Collection<? extends LightFileSystemObject> lastDirectoryContent;
 
     private char separatorChar = File.separatorChar;
 
 // -------------------------- STATIC METHODS --------------------------
 
     public static FTPFile createFTPFile(File f) {
         FTPFile ftpFile = new FTPFile();
         ftpFile.setName(f.getName());
         ftpFile.setSize(f.length());
         ftpFile.setTimestamp(DateUtils.toCalendar(new Date(f.lastModified())));
         ftpFile.setType(f.isDirectory() ? FTPFile.DIRECTORY_TYPE : FTPFile.FILE_TYPE);
         return ftpFile;
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public LocalFolderFileProcessingStrategy(final LocalFolderDestination destination) {
         this.destination = destination;
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface FileProcessingStrategy ---------------------
 
 
     @Override
     public void changeToParentDirectory() throws IOException {
         File parent = currentDirectory.getParentFile();
         if (parent != null) {
             currentDirectory = parent;
         } else {
             throw new IOException("Current directory has no parent");
         }
     }
 
     @Override
     public void changeWorkingDirectory(final String folderName) throws IOException {
         File relFolder = new File(currentDirectory, folderName);
         if (relFolder.isDirectory()) {
             currentDirectory = relFolder;
         } else {
             File absFolder = new File(folderName);
             if (absFolder.isDirectory()) {
                 currentDirectory = absFolder;
             } else {
                 throw new IOException("Could not find directory '" + folderName + "'.");
             }
         }
     }
 
     @Override
     public void connect() throws IOException {
         this.currentDirectory = this.initialDirectory = new File(destination.getPathToBaseFromRoot());
         if (!this.initialDirectory.isDirectory()) {
             throw new FileNotFoundException(this.initialDirectory.getAbsolutePath());
         }
     }
 
     @Override
     public void disconnect() {
     }
 
     @Override
     public String getDestinationId() {
         return destination.getId();
     }
 
     @Override
     public Collection<? extends LightFileSystemObject> getDirectoriesInWorkingDirectory(CommandCancellationStatus commandCancellationStatus) throws FileNotFoundException, CancellationRequestedException {
         List<LightFileSystemObject> dirs = new LinkedList<>();
         Collection<? extends LightFileSystemObject> folder = getFolderContent(commandCancellationStatus);
         for (LightFileSystemObject f : folder) {
             if (f.isFolder()) {
                 dirs.add(f);
             }
         }
         return dirs;
     }
 
     @Override
     public Collection<LightFileSystemObject> getFilesInWorkingDirectory(CommandCancellationStatus commandCancellationStatus) throws FileNotFoundException, CancellationRequestedException {
         List<LightFileSystemObject> dirs = new LinkedList<>();
         Collection<? extends LightFileSystemObject> folder = getFolderContent(commandCancellationStatus);
         for (LightFileSystemObject f : folder) {
             if (!f.isFolder()) {
                 dirs.add(f);
             }
         }
         return dirs;
     }
 
     @Override
     public String getInitialPath() {
         return initialDirectory.getAbsolutePath();
     }
 
     @Override
     public int getServerTimeOffset() throws IOException {
         return 0;
     }
 
     @Override
     public String getWorkingDirectory() {
         return currentDirectory.getAbsolutePath();
     }
 
     @Override
     public String getWorkingDirectoryRelativeToInitialPath(char separator) {
         return currentDirectory.getAbsolutePath().substring(initialDirectory.getAbsolutePath().length()).replace(getDirectorySeparator(), separator);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public void createFolderInWorkingDirectoryImpl(final String folderName) throws IOException {
         File newFolder = new File(currentDirectory, folderName.replace(FTPSession.FTP_PATH_SEPARATOR_CHAR, getDirectorySeparator()));
         if (!newFolder.mkdirs()) {
             throw new IOException("Could not create folder '" + folderName + "'.");
         }
     }
 
     @Override
     public char getDirectorySeparator() {
         return separatorChar;
     }
 
     @Override
     public void deleteFileInWorkingDirectoryImpl(final String fileName) throws IOException {
         File f = new File(currentDirectory, fileName);
         if (f.isFile()) {
             if (!f.delete()) {
                 throw new IOException("Could not delete file '" + fileName + "'.");
             }
         } else {
             throw new IOException("The path '" + fileName + "' does not refer to a file.");
         }
     }
 
     @Override
     public void deleteFolderInWorkingDirectoryImpl(final String folderName) throws IOException {
         File f = new File(currentDirectory, folderName);
         if (f.isDirectory()) {
             FileUtils.deleteDirectory(f);
         } else {
             throw new IOException("The path '" + folderName + "' does not refer to a directory.");
         }
     }
 
     private Collection<? extends LightFileSystemObject> getFolderContent(final CommandCancellationStatus commandCancellationStatus) throws FileNotFoundException, CancellationRequestedException {
         if (lastDirectory != null && lastDirectory.equals(currentDirectory)) {
             return lastDirectoryContent;
         }
         lastDirectory = currentDirectory;
         lastDirectoryContent = LOCAL_FILE_SYSTEM_FOLDER_READER.getFolderContent(currentDirectory.getAbsolutePath(), commandCancellationStatus);
         return lastDirectoryContent;
     }
 
     @Override
     public void moveFileImpl(final String fromAbsolute, final String toAbsolute) throws IOException {
         File from = new File(fromAbsolute);
         File to = new File(toAbsolute);
         if (from.isFile() && !to.exists()) {
             if (!from.renameTo(to)) {
                 throw new IOException("Could not move '" + fromAbsolute + "' to '" + toAbsolute + "'.");
             }
         } else {
             throw new IOException("Either source is not a file or target already exists (source: '" + fromAbsolute + "', target: '" + toAbsolute + "'.");
         }
     }
 
     public void setDirectorySeparator(final char separatorChar) {
         this.separatorChar = separatorChar;
     }
 
     @Override
     public void uploadFileToWorkingDirectoryImpl(String absolutePath) throws IOException {
         File srcFile = new File(absolutePath);
         File destFile = new File(currentDirectory, srcFile.getName());
         FileUtils.copyFile(srcFile, destFile);
     }
 }
