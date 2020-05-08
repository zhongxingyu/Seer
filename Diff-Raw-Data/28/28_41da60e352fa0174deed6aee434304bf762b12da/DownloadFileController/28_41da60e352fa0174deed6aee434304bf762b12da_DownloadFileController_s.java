 /*
 * 
 * 
  */
 package adg.red.controllers;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.stage.FileChooser;
 
 /**
 * Download file implementer: save download contents to the local file, 
 * specify file name and file format
  * @author Witt
  */
 public class DownloadFileController
 {
 
     /**
      * Save file with specified file name and format
     * @param content contents of file
      * @param extension file format
      */
     public static void saveFile(String content, String extension)
     {
         FileChooser fileChooser = new FileChooser();
         //Set extension filter
         FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(extension + " file  (*." + extension + ")", "*." + extension);
         fileChooser.getExtensionFilters().add(extFilter);
 
 
 
         //Show open file dialog
         File file = fileChooser.showSaveDialog(null);
        String filePath = file.getAbsolutePath() + "." + extension;
 
        save(content, filePath);
     }
 
     /**
      * write downloaded contents to the specified file
      * @param content file contents
     * @param file path of the file
      */
     private static void save(String content, String file)
     {
         try
         {
             FileWriter writer = new FileWriter(file);
             writer.write(content);
             writer.close();
         }
         catch (IOException ex)
         {
             Logger.getLogger(DownloadFileController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
