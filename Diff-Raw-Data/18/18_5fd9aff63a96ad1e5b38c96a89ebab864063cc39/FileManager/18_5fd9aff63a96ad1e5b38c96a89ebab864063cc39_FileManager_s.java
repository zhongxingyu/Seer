 import java.io.*;
 import javax.swing.*;
 
 /** Loads and saves Courses
  * @author Qasim Ali
  */
 public class FileManager {
   /** Serialize the specified course, saving it to the specified file.
    * @param fileName    the file to save to
    * @param course      the course to save
    */
   public static void saveCourse(String fileName, Course course) {
     try {
       // Create output streams for the given filename
      FileOutputStream fo = new FileOutputStream(filename);
       ObjectOutputStream out = new ObjectOutputStream(fo);
       // Write and close files
       out.writeObject(course);
       out.close();
       fo.close();
     }
     // Thrown when the file is a directory or can't be written to, so alert the
     // user and exit the program
     catch(FileNotFoundException e) {
       JOptionPane.showMessageDialog(null, "Error opening \'" + fileName
               + "\' for writing: file is either a directory or cannot be "
               + "written to", "Error writing file", JOptionPane.ERROR_MESSAGE);
       System.exit(1);
     }
     // Thrown when an exception occurs when writing the file from writeObject()
     catch(IOException e) {
       // Alert the user and exit
       JOptionPane.showMessageDialog(null, "Error writing \'" + fileName
               + "\': " + e.getMessage(), "Error writing file", 
               JOptionPane.ERROR_MESSAGE);
       System.exit(1);
     }
   }
   
   /** Load a course from the specified file
    * @param fileName    the file to read from
    * @return            the loaded course
    */
   public static Course loadCourse(String fileName) {
     try {
       // Create input streams for the given filename
       FileInputStream fi = new FileInputStream(fileName);
       ObjectInputStream in = new ObjectInputStream(fi);
       // Read and close files
      Course course = (Course)in.readObject();
       in.close();
       fi.close();
     }
     catch(FileNotFoundException e) {
       // Alert the user and exit
       JOptionPane.showMessageDialog(null, "Error opening \'" + fileName
               + "\': file not found, is a directory, or could not be opened",
               "Error reading file", JOptionPane.ERROR_MESSAGE);
       System.exit(1);
     }
     catch(IOException e) {
       JOptionPane.showMessageDialog(null, "Error opening \'" + fileName
               + "\': " + e.getMessage(), "Error reading file", 
               JOptionPane.ERROR_MESSAGE);
     }
   }
 }
