 package info.mikaelsvensson.ftpbackup.model.filesystem;
 
 import info.mikaelsvensson.ftpbackup.command.job.ArchiveFileNameTemplate;
 import info.mikaelsvensson.ftpbackup.command.job.BooleanFileSystemObjectFilter;
 import info.mikaelsvensson.ftpbackup.util.CalendarUtil;
 import org.apache.commons.lang3.StringUtils;
 
 import java.io.File;
 import java.util.*;
 
 public class FileSystemFolder extends FileSystemObject {
     final Collection<FileSystemObject> children = new HashSet<>();
 
     FileSystemFolder(String rootPath, File localFolderFile) {
         super(rootPath.equals(localFolderFile.getAbsolutePath()) ? null : localFolderFile.getName(), localFolderFile.lastModified());
     }
 
     private FileSystemFolder(String name, long timeStamp) {
         this(new String[]{name}, timeStamp);
     }
 
     @Override
     public long getSize() {
         long totalSize = 0;
         for (FileSystemObject child : children) {
             totalSize += child.getSize();
         }
         return totalSize;
     }
 
     public FileSystemFolder(String name, Calendar timestamp) {
         this(name, timestamp.getTimeInMillis());
     }
 
     @Override
     protected FileSystemFolder getCurrentFolder() {
         return this;
     }
 
     FileSystemFolder(String[] folderPathFromRoot, long timeStamp, FileSystemObject... children) {
         super(folderPathFromRoot != null ? folderPathFromRoot[folderPathFromRoot.length - 1] : null, timeStamp);
         FileSystemFolder childOfParent = this;
         if (folderPathFromRoot != null) {
             for (int i = folderPathFromRoot.length - 2; i >= 0; i--) {
                 FileSystemFolder folder = new FileSystemFolder(folderPathFromRoot[i], 0);
                 folder.addChild(childOfParent);
                 childOfParent = folder;
             }
         }
         for (FileSystemObject child : children) {
             addChild(child);
         }
     }
 
     public FileSystemFolder(String name, FileSystemObject... children) {
         this(new String[]{name}, CalendarUtil.now().getTime().getTime(), children);
     }
 
     public Collection<FileSystemFile> getFiles() {
         Collection<FileSystemFile> files = new LinkedList<>();
         for (FileSystemObject child : children) {
             if (child instanceof FileSystemFile) {
                 files.add((FileSystemFile) child);
             }
         }
         return files;
     }
 
     public Collection<FileSystemFolder> getFolders() {
         Collection<FileSystemFolder> folders = new LinkedList<>();
         for (FileSystemObject child : children) {
             if (child instanceof FileSystemFolder) {
                 folders.add((FileSystemFolder) child);
             }
         }
         return folders;
     }
 
     public void addChild(FileSystemObject object) {
         object.parent = this;
         children.add(object);
     }
 
     public FileSystemFolder findFolder(FileSystemFolder needle) {
         return findFolder(needle.getName());
     }
 
     public FileSystemFolder findFolder(String name) {
         return findFolder(new NameFilter<FileSystemFolder>(name));
     }
 
     public FileSystemObject findObject(String name) {
         return findObject(new NameFilter<FileSystemObject>(name));
     }
 
     private FileSystemFolder findFolder(BooleanFileSystemObjectFilter<FileSystemFolder> filter) {
         for (FileSystemFolder folder : getFolders()) {
             if (filter.perform(folder)) {
                 return folder;
             }
         }
         return null;
     }
 
     private FileSystemObject findObject(BooleanFileSystemObjectFilter<FileSystemObject> filter) {
         for (FileSystemObject child : children) {
             if (filter.perform(child)) {
                 return child;
             }
         }
         return null;
     }
 
     public FileSystemFile findFile(FileSystemFile needle) {
         return findFile(needle.getName());
     }
 
     FileSystemFile findFile(final String name) {
         return findFile(new NameFilter<FileSystemFile>(name), false);
     }
 
     public int getFileCount(boolean recursive) {
         int count = getFiles().size();
         if (recursive) {
             for (FileSystemFolder folder : getFolders()) {
                 count += folder.getFileCount(true);
             }
         }
         return count;
     }
 
 /*
     public static FileSystemFolder fromFolder(Folder source) {
         FileSystemFolder fsf = new FileSystemFolder(source.getFolderFile().getName(), source.getFolderFile().lastModified());
         for (Folder subFolder : source.getFolders()) {
             fsf.addChild(fromFolder(subFolder));
         }
         for (LocalFile file : source.getFiles()) {
             fsf.addChild(new FileSystemFile(file));
         }
         return fsf;
     }
 */
 
 /*
     public Collection<FileSystemFile> getArchivedFiles(ArchiveFileNameTemplate[] templates) {
         return getArchivedFiles(templates, new LinkedList<FileSystemFile>());
     }
 */
 
     public Map<FileSystemFile, ArchiveFileNameTemplate> getArchivedFiles(ArchiveFileNameTemplate[] templates, boolean recursive) {
         Map<FileSystemFile, ArchiveFileNameTemplate> files = new HashMap<>();
         if (recursive) {
             for (FileSystemFolder folder : getFolders()) {
                 files.putAll(folder.getArchivedFiles(templates, recursive));
             }
         }
         for (FileSystemFile file : getFiles()) {
             for (ArchiveFileNameTemplate template : templates) {
                 String path = file.getPathFromRoot();
                 if (template.isArchivedFile(path)) {
                     files.put(file, template);
                     break;
                 }
             }
         }
         return files;
     }
 
     public FileSystemFolder findFolderByRootPath(String pathFromRoot) {
         String[] parts = StringUtils.split(pathFromRoot, File.separatorChar);
         FileSystemFolder folder = getRoot();
         for (String part : parts) {
             folder = folder.findFolder(part);
             if (null == folder) {
                 return null;
             }
         }
         return folder;
     }
 
     public FileSystemObject findObjectByRootPath(String pathFromRoot) {
         String name = StringUtils.substringAfterLast(pathFromRoot, File.separator);
         String folderPath = StringUtils.substringBeforeLast(pathFromRoot, File.separator);
         FileSystemFolder folder = findFolderByRootPath(folderPath);
         if (null != folder) {
             return folder.findObject(name);
         }
         return null;
     }
 
     public FileSystemFile findFileByRootPath(String pathFromRoot) {
         if (pathFromRoot.indexOf(File.separatorChar) >= 0) {
             String folderPath = StringUtils.substringBeforeLast(pathFromRoot, File.separator);
             FileSystemFolder folder = findFolderByRootPath(folderPath);
             if (null != folder) {
                 String name = StringUtils.substringAfterLast(pathFromRoot, File.separator);
                 return folder.findFile(name);
             } else {
                 return null;
             }
         } else {
             return getRoot().findFile(pathFromRoot);
         }
     }
 
     FileSystemFile findFile(BooleanFileSystemObjectFilter<FileSystemFile> filter, boolean recursive) {
         if (recursive) {
             for (FileSystemFolder folder : getFolders()) {
                 FileSystemFile match = folder.findFile(filter, recursive);
                 if (null != match) {
                     return match;
                 }
             }
         }
         for (FileSystemFile file : getFiles()) {
             if (filter.perform(file)) {
                 return file;
             }
         }
         return null;
     }
 
     public Collection<FileSystemObject> getFilesAndFolders() {
         return Collections.unmodifiableCollection(children);
     }
 }
