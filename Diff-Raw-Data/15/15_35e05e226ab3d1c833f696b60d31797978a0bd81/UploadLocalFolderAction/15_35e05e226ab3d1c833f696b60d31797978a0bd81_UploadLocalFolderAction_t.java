 package info.mikaelsvensson.ftpbackup.command.job.action;
 
 import info.mikaelsvensson.ftpbackup.command.job.FileProcessingStrategy;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFile;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFolder;
 
 public class UploadLocalFolderAction extends JobAction {
     private final FileSystemFolder localSubFolder;
     private final FileSystemFolder remoteFolder;
 
     public UploadLocalFolderAction(FileProcessingStrategy strategy, FileSystemFolder localSubFolder, FileSystemFolder remoteFolder) {
         super(strategy);
         this.localSubFolder = localSubFolder;
         this.remoteFolder = remoteFolder;
     }
 
     @Override
     public void run() {
         changeToFolder(remoteFolder);
        createAndUploadFolder(localSubFolder);
     }
 
    private void createAndUploadFolder(FileSystemFolder folder) {
         String remoteFolderName = folder.getName();
         if (strategy.createFolderInWorkingDirectory(remoteFolderName) && strategy.changeWorkingDirectory(remoteFolderName)) {
             for (FileSystemFolder subFolder : folder.getFolders()) {
                createAndUploadFolder(subFolder);
             }
             for (FileSystemFile file : folder.getFiles()) {
                 uploadFileToCurrentRemoteDirectory(file);
             }
            strategy.changeToParentDirectory();
         } else {
             REPORT_LOGGER.warn("Could not create (and change to) remote folder '" + remoteFolderName + "'.");
         }
     }
 }
