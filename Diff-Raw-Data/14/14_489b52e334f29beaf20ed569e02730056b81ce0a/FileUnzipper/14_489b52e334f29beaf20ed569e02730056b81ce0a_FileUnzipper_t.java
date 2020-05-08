 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 public class FileUnzipper {
 
   private ArrayList<File> outFiles;
   private static final Logger LOG = LogManager.getLogger(FileUnzipper.class);
 
   public FileUnzipper(Path zipFile) {
     outFiles = new ArrayList<>();
     try {
       ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()));
       ZipEntry ze = zis.getNextEntry();
       while (ze != null) {
         int len;
         File extractedFile = Main.tmpDirectory.resolve(ze.getName()).toAbsolutePath().toFile();
 
         System.out.println("Extracting File: " + extractedFile.getAbsolutePath());
         FileOutputStream fos = new FileOutputStream(extractedFile);
        byte[] buffer = new byte[Main.CHUNK_SIZE];
         while ((len = zis.read(buffer)) > 0) {
           fos.write(buffer, 0, len);
         }
         fos.close();
         System.out.println("Extracted File: " + extractedFile.getAbsolutePath());
         outFiles.add(extractedFile);
         ze = zis.getNextEntry();
       }
       zis.closeEntry();
       zis.close();
     } catch (FileNotFoundException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (IOException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     }
   }
 
   public ArrayList<File> getOutFiles() {
     return outFiles;
   }
 
   public void setOutFiles(ArrayList<File> outFiles) {
     this.outFiles = outFiles;
   }
 
 }
