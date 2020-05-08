 package info.mikaelsvensson.ftpbackup.model.filesystem;
 
 import org.apache.commons.lang3.StringUtils;
 
 import java.io.File;
import java.util.Arrays;
 import java.util.Set;
 
 public class FileSystemBaseFolder extends FileSystemFolder {
     private final String[] pathToBaseFromRoot;
     private final char pathSeparator;
 
     public FileSystemBaseFolder(final String pathToBaseFromRoot, final char pathSeparator, final Set<FileSystemObject> children) {
         this(pathToBaseFromRoot, pathSeparator);
         this.children = children;
     }
 
     @Override
     public FileSystemFolder getParent() {
         throw new UnsupportedOperationException("The root folder has no parent");
     }
 
     public FileSystemBaseFolder(String pathToBaseFromRoot, final char pathSeparator, FileSystemObject... children) {
         super(null, 0, children);
         this.pathToBaseFromRoot = StringUtils.splitPreserveAllTokens(pathToBaseFromRoot, pathSeparator);
         this.pathSeparator = pathSeparator;
     }
 
     public FileSystemBaseFolder(String pathToBaseFromRoot, File localFolderFile, final char pathSeparator) {
         super(pathToBaseFromRoot, localFolderFile);
         this.pathToBaseFromRoot = StringUtils.splitPreserveAllTokens(pathToBaseFromRoot, pathSeparator);
         this.pathSeparator = pathSeparator;
     }
 
     public String getPathToBaseFromRoot() {
         return StringUtils.join(pathToBaseFromRoot, pathSeparator);
     }
 
     public String getPathToBaseFromRoot(final char sep) {
         return StringUtils.join(pathToBaseFromRoot, sep);
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof FileSystemBaseFolder)) {
             return false;
         }
 //        if (!super.equals(o)) {
 //            return false;
 //        }
 
         FileSystemBaseFolder that = (FileSystemBaseFolder) o;
 
        if (!Arrays.equals(pathToBaseFromRoot, that.pathToBaseFromRoot)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = 0;//super.hashCode();
        result = 31 * result + Arrays.hashCode(pathToBaseFromRoot);
         return result;
     }
 
     public char getPathSeparator() {
         return pathSeparator;
     }
 }
