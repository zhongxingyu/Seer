 package core.service;
 
 import core.service.exceptions.CopyException;
 import core.service.exceptions.CreateFileException;
 import core.service.exceptions.WriteFileException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 import java.io.*;
 import java.util.Scanner;
 
 
 public class FileService {
 
   public static final String ENCODING = "UTF-8";
 
   public File createFileIfNotExist(File file) throws CreateFileException {
     boolean isCreated = false;
     try {
       if (!file.exists()) {
         isCreated = file.createNewFile();
       } else {
         isCreated = true;
       }
     } catch (IOException e) {
       e.printStackTrace();
       throw new CreateFileException(file.getAbsolutePath(), e);
     }
     if (!isCreated) {
       throw new CreateFileException(file.getAbsolutePath());
     }
     return file;
   }
 
   public File createFileIfNotExist(String filepath) throws CreateFileException {
     File file = new File(filepath);
     return createFileIfNotExist(file);
   }
 
   public void writeToFile(File file, String content) throws WriteFileException {
     try {
       FileUtils.writeStringToFile(file, content);
     } catch (IOException e) {
       throw new WriteFileException(file.getAbsolutePath(), e);
     }
   }
 
   public void writeToFile(String filepath, String content) throws WriteFileException {
     File file = new File(filepath);
     writeToFile(file, content);
   }
 
   public void writeToFileCreateIfNotExist(String filepath, String content)
           throws CreateFileException, WriteFileException {
    File file = createFileIfNotExist(filepath);
    writeToFile(file, content);
   }
 
   public String readFile(File file) throws IOException {
     return  FileUtils.readFileToString(file);
   }
 
   public void copyResourceToFile(String resourceFile, String targetFile) throws CreateFileException, FileNotFoundException, CopyException {
     File target = new File(targetFile);
     createFileIfNotExist(target);
     copyResourceToFile(resourceFile, target);
   }
 
   private void copyResourceToFile(String resourceFile, File target) throws FileNotFoundException, CopyException {
     InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceFile);
     OutputStream outputStream = null;
     outputStream = new FileOutputStream(target);
     try {
       IOUtils.copy(resourceAsStream, outputStream);
       outputStream.close();
       resourceAsStream.close();
     } catch (IOException e) {
       throw new CopyException(resourceFile, e);
     }
   }
 }
