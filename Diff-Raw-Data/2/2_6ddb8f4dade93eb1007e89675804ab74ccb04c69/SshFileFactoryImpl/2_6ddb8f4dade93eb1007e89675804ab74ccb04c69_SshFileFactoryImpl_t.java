 package edu.umn.msi.tropix.ssh;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.annotation.ManagedBean;
 import javax.inject.Inject;
 
 import org.apache.commons.io.output.ProxyOutputStream;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.sshd.server.SshFile;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import edu.umn.msi.tropix.common.io.InputStreamCoercible;
 import edu.umn.msi.tropix.files.creator.TropixFileCreator;
 import edu.umn.msi.tropix.grid.credentials.Credential;
 import edu.umn.msi.tropix.models.Folder;
 import edu.umn.msi.tropix.models.TropixFile;
 import edu.umn.msi.tropix.models.TropixObject;
 import edu.umn.msi.tropix.models.locations.Locations;
 import edu.umn.msi.tropix.persistence.service.FolderService;
 import edu.umn.msi.tropix.persistence.service.TropixObjectService;
 import edu.umn.msi.tropix.storage.core.StorageManager;
 import edu.umn.msi.tropix.storage.core.StorageManager.FileMetadata;
 
 @ManagedBean
 public class SshFileFactoryImpl implements SshFileFactory {
   private static final Log LOG = LogFactory.getLog(SshFileFactoryImpl.class);
   private final TropixObjectService tropixObjectService;
   private final TropixFileCreator tropixFileCreator;
   private final StorageManager storageManager;
   private final FolderService folderService;
 
   private static final String HOME_DIRECTORY_PATH = "/" + Locations.MY_HOME;
   private static final String MY_GROUP_FOLDERS_PATH = "/" + Locations.MY_GROUP_FOLDERS;
   private static final Set<String> META_OBJECT_PATHS = Sets.newHashSet("/", HOME_DIRECTORY_PATH, MY_GROUP_FOLDERS_PATH);
 
   @Inject
   public SshFileFactoryImpl(final TropixObjectService tropixObjectService,
       final TropixFileCreator tropixFileCreator,
       final StorageManager storageManager,
       final FolderService folderService) {
     this.tropixObjectService = tropixObjectService;
     this.tropixFileCreator = tropixFileCreator;
     this.storageManager = storageManager;
     this.folderService = folderService;
   }
 
   public SshFile getFile(final Credential credential, final String virtualPath) {
     return new SshFileImpl(credential, virtualPath);
   }
 
   class SshFileImpl implements SshFile {
     private final Credential credential;
     private final String identity;
     private final String virtualPath;
     private final String absolutePath;
     private final boolean isMetaLocation;
     private StorageManager.FileMetadata fileMetadata;
     private boolean fileMetadataSet = false;
     private TropixObject object;
 
     private void initObject() {
       if(object == null) {
         log("setting object");
         object = getTropixObject(virtualPath);
       }
     }
 
     private TropixObject getTropixObject(final String path) {
       List<String> pathPieces = Utils.pathPieces(path);
       if(pathPieces.size() > 0 && Locations.isValidBaseLocation(pathPieces.get(0))) {
         return tropixObjectService.getPath(identity, Iterables.toArray(pathPieces, String.class));
       } else {
         return null;
       }
     }
 
     SshFileImpl(final Credential credential, final String virtualPath) {
       this.credential = credential;
       this.identity = credential.getIdentity();
       this.virtualPath = virtualPath;
       this.absolutePath = Utils.cleanAndExpandPath(virtualPath);
       this.isMetaLocation = META_OBJECT_PATHS.contains(absolutePath);
       log("Create file for virtualPath " + virtualPath);
     }
 
     SshFileImpl(final Credential credential, final String virtualPath, final TropixObject object) {
       this(credential, virtualPath);
       this.object = object;
     }
 
     SshFileImpl(final Credential credential, final String virtualPath, final TropixObject object, final FileMetadata fileMetadata) {
       this(credential, virtualPath, object);
       this.fileMetadata = fileMetadata;
       this.fileMetadataSet = true;
     }
 
     // Remove trailing / for directories, is this expected?
     public String getAbsolutePath() {
       final String absolutePath = this.absolutePath;
       log(String.format("getAbsolutePath called, result is %s", absolutePath));
       return absolutePath;
     }
 
     // What should this return for root?
     public String getName() {
       final String name = Utils.name(virtualPath);
       log(String.format("getName called, result is %s", name));
       return name;
     }
 
     public boolean isDirectory() {
       log("isDirectory");
       if(isMetaLocationOrRootFolder()) {
         return true;
       } else if(internalDoesExist()) {
         initObject();
         return !(object instanceof TropixFile);
       } else {
         return false;
       }
     }
 
     public boolean isFile() {
       log("isFile");
       if(isMetaLocationOrRootFolder()) {
         return false;
       } else if(internalDoesExist()) {
         initObject();
         return (object instanceof TropixFile);
       } else {
         return false;
       }
     }
 
     public boolean doesExist() {
       log("doesExist");
       return internalDoesExist();
     }
 
     private boolean internalDoesExist() {
       boolean doesExist = false;
       if(isMetaLocation) {
         doesExist = true;
       } else {
         initObject();
         doesExist = object != null;
       }
       return doesExist;
     }
 
     public boolean isRemovable() {
       log("isRemovable");
       return !isMetaLocation && internalDoesExist();
     }
 
     public SshFile getParentFile() {
       log("getParentFile");
       return getFile(credential, Utils.parent(virtualPath));
     }
 
     public long getLastModified() {
       log("getLastModified");
       initFileMetadata();
       if(fileMetadata != null) {
         return fileMetadata.getDateModified();
       } else {
         return 0L;
       }
     }
 
     public boolean setLastModified(final long time) {
       return false;
     }
 
     private boolean isTropixFile() {
       if(isMetaLocationOrRootFolder()) {
         return false;
       } else {
         initObject();
         return object instanceof TropixFile;
       }
     }
 
     private String getFileId() {
       Preconditions.checkState(isTropixFile(), "getFileId called for non-file object");
       final TropixFile file = (TropixFile) object;
       return file.getFileId();
     }
     
     private void initFileMetadata() {
       if(!fileMetadataSet) {
         if(isTropixFile()) {
           fileMetadata = storageManager.getFileMetadata(getFileId(), identity);
         }
         fileMetadataSet = true;
       }
     }
 
     public long getSize() {
       log("getSize");
       initFileMetadata();
       if(fileMetadata != null) {
         return fileMetadata.getLength();
       } else {
         return 0;
       }
     }
 
     private InputStreamCoercible readFile() {
       return storageManager.download(getFileId(), identity);
     }
 
     public void truncate() throws IOException {
       log("truncate");
       if(internalDoesExist()) {
         // TODO: Handle this better
         throw new IllegalStateException("Cannot truncate this file, please delete and add a new file.");
       }
     }
 
     public boolean delete() {
       log("delete");
       if(isMetaLocation || !internalDoesExist()) {
         return false;
       } else {
         initObject();
         tropixObjectService.delete(identity, object.getId());
         return true;
       }
     }
 
     public boolean move(final SshFile destination) {
       log("move");
       if(isMetaLocation) {
         return false;
       }
       initObject();
       boolean moved = false;
       if(parentIsFolder() && destination instanceof SshFileImpl) {
         final SshFileImpl destinationFile = (SshFileImpl) destination;
         final boolean validDestination = !destinationFile.doesExist() && destinationFile.parentIsFolder();
         System.out.println(destinationFile.doesExist());
         log("Move valid destination - " + validDestination);
         if(validDestination) {
           final String objectId = object.getId();
           tropixObjectService.move(identity, objectId, destinationFile.parentAsFolder().getId());
           final TropixObject object = tropixObjectService.load(identity, objectId);
           object.setName(destination.getName());
           tropixObjectService.update(identity, object);
           moved = true;
         }
       }
       log(String.format("In move - moved? %b", moved));
       return moved;
     }
 
     public boolean isReadable() {
       final boolean readable = true;
       log(String.format("Checking is readable - %b", readable));
       return readable;
     }
 
     private void log(final String message) {
       if(LOG.isTraceEnabled()) {
         LOG.trace(String.format("For virtual path <%s> - %s", virtualPath, message));
       }
     }
 
     public boolean isWritable() {
       log("Checking is writable");
       if(isHomeDirectory()) {
         return true;
       } else if(isMetaLocation) {
         return false;
       } else {
         initObject();
         return object instanceof Folder || (object == null && parentIsFolder());
       }
     }
 
     public void handleClose() throws IOException {
     }
 
     private boolean isMetaLocationOrRootFolder() {
       return isMetaLocation || isRootGroupFolder();
     }
 
     // TODO: Check uniqueness
     public boolean mkdir() {
       log("Creating directory");
       if(isMetaLocationOrRootFolder()) {
         return false;
       }
       final TropixObject parentObject = getParentFolder();
       if(!(parentObject instanceof Folder)) {
         return false;
       } else {
         final Folder newFolder = new Folder();
         newFolder.setCommitted(true);
         newFolder.setName(Utils.name(virtualPath));
         folderService.createFolder(identity, parentObject.getId(), newFolder);
         return true;
       }
     }
 
     private TropixObject getParentFolder() {
       final String parentPath = Utils.parent(virtualPath);
       final TropixObject parentObject = getTropixObject(parentPath);
       return parentObject;
     }
 
     private boolean parentIsFolder() {
       return getParentFolder() instanceof Folder;
     }
 
     private Folder parentAsFolder() {
       return (Folder) getParentFolder();
     }
 
     public OutputStream createOutputStream(final long offset) throws IOException {
       if(!parentIsFolder()) {
         final String errorMessage = "Invalid path to create output stream from " + virtualPath;
         LOG.warn(errorMessage);
         throw new IllegalStateException(errorMessage);
       } else if(offset > 0) {
         final String errorMessage = "Server only supports offsets of 0 - path " + virtualPath;
         LOG.warn(errorMessage);
         throw new IllegalStateException(errorMessage);
       } else {
         final String newFileId = UUID.randomUUID().toString();
         final OutputStream outputStream = storageManager.prepareUploadStream(newFileId, identity);
         return new ProxyOutputStream(outputStream) {
           @Override
           public void close() throws IOException {
             try {
               super.close();
             } finally {
               LOG.debug("Preparing file for tropixfilecreator");
               final Folder parentFolder = parentAsFolder();
               final TropixFile tropixFile = new TropixFile();
               tropixFile.setName(getName());
               tropixFile.setCommitted(true);
               tropixFile.setFileId(newFileId);
               tropixFileCreator.createFile(credential, parentFolder.getId(), tropixFile, null);
               LOG.debug("File created " + virtualPath);
             }
           }
         };
       }
     }
 
     public InputStream createInputStream(final long offset) throws IOException {
       final InputStream inputStream = readFile().asInputStream();
       inputStream.skip(offset);
       return inputStream;
     }
 
     public List<SshFile> listSshFiles() {
       log("listSshFiles");
       final ImmutableList.Builder<SshFile> children = ImmutableList.builder();
       if(isRoot()) {
         children.add(getFile(credential, HOME_DIRECTORY_PATH));
         children.add(getFile(credential, MY_GROUP_FOLDERS_PATH));
       } else if(isMyGroupFolders()) {
         final TropixObject[] objects = folderService.getGroupFolders(identity);
         buildSshFiles(objects, children);
       } else {
         initObject();
         final TropixObject[] objects = tropixObjectService.getChildren(identity, object.getId());
         buildSshFiles(objects, children);
       }
       return children.build();
     }
 
     // Hacking this to make sure names are unique in a case-insensitive manner because MySQL
     // likes are case-insensitive for the current schema. At some point we should update the name column
     // according to http://dev.mysql.com/doc/refman/5.0/en/case-sensitivity.html.
     private <T extends TropixObject> void buildSshFiles(final T[] objects, final ImmutableList.Builder<SshFile> children) {
       final Map<String, Boolean> uniqueName = Maps.newHashMap();
       for(TropixObject object : objects) {
         final String objectName = object.getName().toUpperCase();
         if(!uniqueName.containsKey(objectName)) {
           uniqueName.put(objectName, true);
         } else {
           uniqueName.put(objectName, false);
         }
       }
 
       // Optimization: Separate files so we can batch prefetch metadata for them all at once
       final ImmutableList.Builder<TropixFile> tropixFilesBuilder = ImmutableList.builder();
       final ImmutableList.Builder<String> tropixFileIds = ImmutableList.builder();
       for(TropixObject object : objects) {
         if(object instanceof TropixFile) {
           TropixFile tropixFile = (TropixFile) object;
           tropixFilesBuilder.add(tropixFile);
           tropixFileIds.add(tropixFile.getFileId());
         } else {
           final String name = object.getName();
           final String derivedName = uniqueName.get(name.toUpperCase()) ? name : String.format("%s [id:%s]", name, object.getId());
           final String childName = Utils.join(virtualPath, derivedName);
           LOG.debug(String.format("Creating child with name [%s]", childName));
           children.add(new SshFileImpl(credential, childName, object));
         }
       }
       final List<TropixFile> tropixFiles = tropixFilesBuilder.build();
       if(!tropixFiles.isEmpty()) {
         final Iterator<FileMetadata> filesMetadataIterator = storageManager.getFileMetadata(tropixFileIds.build(), identity).iterator();
         for(final TropixFile tropixFile : tropixFiles) {
           final String name = tropixFile.getName();
           final String derivedName = uniqueName.get(name.toUpperCase()) ? name : String.format("%s [id:%s]", name, object.getId());
           final String childName = Utils.join(virtualPath, derivedName);
           LOG.debug(String.format("Creating child with name [%s]", childName));
          children.add(new SshFileImpl(credential, childName, tropixFile, filesMetadataIterator.next()));
         }        
       }
     }
 
     private boolean isRoot() {
       return "/".equals(absolutePath);
     }
 
     private boolean isMyGroupFolders() {
       return MY_GROUP_FOLDERS_PATH.equals(absolutePath);
     }
 
     private boolean isRootGroupFolder() {
       boolean isRootGroupFolder = false;
       if(absolutePath.startsWith(MY_GROUP_FOLDERS_PATH)) {
         isRootGroupFolder = Utils.pathPiecesCountForAbsolutePath(absolutePath) == 2;
       }
       return isRootGroupFolder;
     }
 
     private boolean isHomeDirectory() {
       return HOME_DIRECTORY_PATH.equals(absolutePath);
     }
 
     public boolean create() throws IOException {
       log("Attempting create");
       return !isTropixFile();
     }
 
     public boolean isExecutable() {
       log("Checking executable");
       return true;
     }
 
   }
 
 }
