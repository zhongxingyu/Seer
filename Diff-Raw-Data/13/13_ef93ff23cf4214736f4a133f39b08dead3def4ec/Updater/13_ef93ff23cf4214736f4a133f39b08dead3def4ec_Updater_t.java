 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author simon
  */
 public class Updater {
     
     /**
      * Returns the path to the root edacc folder, i.e. the path to the EDACC.jar
      * @return the path to the root edacc folder
      */
     private static String getPath() throws URISyntaxException {
         File f = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         if (f.isDirectory()) {
             return f.getPath();
         } else {
             return f.getParent();
         }
     }
     
     public static void main(String[] args) {
         try {
             Thread.sleep(1500);
         } catch (Exception ex) {
         }
         try {
             ZipInputStream stream = new ZipInputStream(new FileInputStream(new File(getPath() + System.getProperty("file.separator") + "update.zip")));
             ZipEntry entry;
             while ((entry = stream.getNextEntry()) != null) {
                 File file = new File(getPath() + System.getProperty("file.separator") + entry.getName());
                 if (entry.isDirectory()) {
                     while (!file.exists() && !file.mkdir()) {
                         int input = JOptionPane.showConfirmDialog(null, "Couldn't create directory " + file.getPath() + ". Please correct permissions.", "Directory not creatable", JOptionPane.OK_CANCEL_OPTION);
                         if (input == JOptionPane.CANCEL_OPTION) {
                             JOptionPane.showMessageDialog(null, "Update cancelled. Please extract contents of update.zip manually.");
                             return;
                         }
                     }
                 } else {
                     while (file.exists() && !file.delete()) {
                         int input = JOptionPane.showConfirmDialog(null, "File " + file.getPath() + " is in use. Please close any instance of the EDACC Application.", "File in use", JOptionPane.OK_CANCEL_OPTION);
                         if (input == JOptionPane.CANCEL_OPTION) {
                             JOptionPane.showMessageDialog(null, "Update cancelled. Please extract contents of update.zip manually.");
                             return;
                         }
                     }
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(file.getPath());
                     java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                     byte[] data = new byte[1024];
                     int count;
                     while ((count = stream.read(data)) > 0) {
                         bout.write(data, 0, count);
                     }
                     bout.close();
                     fos.close();
                 }
             }
             stream.close();
             new File(getPath() + System.getProperty("file.separator") + "update.zip").delete();
             int input = JOptionPane.showConfirmDialog(null, "Update completed. Start EDACC?", "Update successful", JOptionPane.YES_NO_OPTION);
             if (input == JOptionPane.YES_OPTION) {
                String startEDACC[] = {System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java", "-jar",  getPath() + System.getProperty("file.separator") +  "EDACC.jar"};
                Runtime.getRuntime().exec(startEDACC);
             }
 
         } catch (FileNotFoundException ex) {
             JOptionPane.showMessageDialog(null, "File not found while updating. Update cancelled. Reason:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(null, "Error while reading/writing files. Update cancelled. Reason:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, "Error while updating. Update cancelled. Reason:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         
         }
     }
 }
