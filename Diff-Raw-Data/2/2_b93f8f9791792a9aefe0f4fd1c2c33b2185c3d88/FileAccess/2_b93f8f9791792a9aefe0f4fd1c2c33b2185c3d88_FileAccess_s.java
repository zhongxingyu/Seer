 
 package pt.uac.cafeteria.model.persistence;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
 * Simple abstraction from file access.
  *
  * Meant to be used as a base class. It holds a reference to a File
  * object for subclasses that need one, and any common and useful methods
  * related to the file referenced.
  */
 public abstract class FileAccess {
 
     /** File object that is a representation of a system file. */
     protected File file;
 
     /**
      * Constructor.
      *
      * Forces subclasses to declare a constructor that initializes the file
      * reference.
      *
      * @param filePath relative or absolute path to the file.
      */
     public FileAccess(String filePath) {
         this.file = new File(filePath);
     }
 
     /** Gets a reference to the File object. */
     public File getFile() {
         return file;
     }
 
     /**
      * Similar to getFile, but makes sure the file actually exists in the system.
      *
      * @return file object reference.
      * @throws IOException if can't create file.
      */
     protected File useFile() throws IOException {
         if (!file.exists()) {
             createNewFile();
         }
         return file;
     }
 
     /**
      * Attempts to create file and necessary parent directory.
      * Nothing happens if file already exists.
      *
      * @throws IOException if can't create file.
      */
     protected void createNewFile() throws IOException {
         file.getParentFile().mkdirs();
         file.createNewFile();
     }
 }
