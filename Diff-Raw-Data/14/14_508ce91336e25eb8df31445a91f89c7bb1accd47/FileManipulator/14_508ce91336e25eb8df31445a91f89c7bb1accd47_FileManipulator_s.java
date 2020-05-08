 package file;
 

 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Allows retrieval and storage of data in the standard hierarchy of data files.
  * Any interaction with files should be done through this class's static
 * methods.   
  *
  * @author Japhez
  */
 public class FileManipulator {
 
     /**
      * Writes the passed object to the passed filename on the passed path.
      *
      * @param objectToWrite
      * @param path
      * @param fileName
      */
     public static void writeObject(Object objectToWrite, String path, String fileName) {
         //Create the path if it doesn't exist
         new File(path).mkdirs();
         try {
             ObjectOutputStream oStream;
             FileOutputStream fileOut;
            fileOut = new FileOutputStream(path + fileName);
             oStream = new ObjectOutputStream(fileOut);
             oStream.writeObject(objectToWrite);
             oStream.close();
         } catch (IOException ex) {
             Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     /**
      * Attempts to read in the object from the passed path and file name, and
      * then returns that object.
      *
      * @param path
      * @param fileName
      * @return the read in object
      */
     public static Object readObject(String path, String fileName) {
         try {
             Object result;
             try (ObjectInputStream iStream = new ObjectInputStream(new FileInputStream(path + fileName))) {
                 result = iStream.readObject();
                 return result;
             }
 
         } catch (ClassNotFoundException | IOException ex) {
             Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public static boolean fileExists(String path, String fileName) {
         File file = new File(path + fileName);
         return file.exists();
     }
 }
