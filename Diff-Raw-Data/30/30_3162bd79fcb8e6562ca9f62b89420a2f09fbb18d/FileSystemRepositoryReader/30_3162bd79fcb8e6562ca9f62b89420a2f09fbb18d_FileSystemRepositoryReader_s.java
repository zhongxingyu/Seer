 package org.wyona.yarep.core.impl.fs;
 
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.UID;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import org.apache.log4j.Category;
 
 /**
  *@deprecated
  */
 public class FileSystemRepositoryReader extends Reader {
 
     private static Category log = Category.getInstance(FileSystemRepositoryReader.class);
 
     private FileReader reader;
 
     /**
      *
      */
     public FileSystemRepositoryReader(UID uid, Path path, File contentDir) throws NoSuchNodeException {
         try {
             File file = new File(contentDir.getAbsolutePath() + File.separator + uid.toString());
             log.debug(file.toString());
             reader = new FileReader(file);
         } catch (Exception e) {
             log.error(e);
             throw new NoSuchNodeException(e.getMessage());
         }
     }
 
     /**
      *
      */
     public int read(char cbuf[], int off, int len) throws IOException {
        log.debug("READ");
         return reader.read(cbuf, off, len);
     }
 
     /**
      *
      */
     public void close() throws IOException {
         log.debug("CLOSE");
         reader.close();
     }
 }
