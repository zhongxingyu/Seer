 package org.wyona.yarep.core.impl.fs;
 
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.UID;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class FileSystemRepositoryInputStream extends InputStream {
 
     private static Category log = Category.getInstance(FileSystemRepositoryInputStream.class);
 
     private FileInputStream in;
 
     /**
      *
      */
     public FileSystemRepositoryInputStream(UID uid, Path path, File contentDir) throws RepositoryException {
         try {
             File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
             log.debug(file.toString());
             in = new FileInputStream(file);
         } catch (FileNotFoundException e) {
             log.error(e);
             throw new NoSuchNodeException(e.getMessage(), e);
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     /**
      *
      */
     public int read() throws IOException {
        log.debug("READ");
         return in.read();
     }
 
     /**
      *
      */
     public void close() throws IOException {
         log.debug("CLOSE");
         in.close();
     }
 }
