 package dashteacup.pman;
 
 import java.io.File;
 
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Custom file filter to restrict the openable files to .PMAN files
  */
 public class PmanFileFilter extends FileFilter {
 
     /**
      * Determines if a file should be displayed as selectable in the file
      * chooser.
      */
     @Override
     public boolean accept(File f) {
         if (f.isDirectory()) {
             return true;
         }
         String filename = f.getName();
         int extIndex = filename.lastIndexOf('.');
        try {
            // I don't want to worry about the case of the extension
            String extension = filename.substring(extIndex + 1).toLowerCase();
            return extension.equals("pman");
        }
        catch (IndexOutOfBoundsException e) {
            return false;
        }
     }
 
     /**
      * Description of the file type displayed in the file chooser.
      */
     @Override
     public String getDescription() {
         return "PMAN file";
     }
 
 }
