 package br.ufrj.dcc.compgraf.im.ui;
 
 import java.io.File;
 
 import javax.swing.JFileChooser;
 
 public class FileChooser {
 
   public static File chooseFile(String startDir) {
     JFileChooser fc = new JFileChooser(startDir);
     
     fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
     fc.addChoosableFileFilter(new ImageFilter());
     
     int retVal = fc.showOpenDialog(null);
     
     if (retVal == JFileChooser.APPROVE_OPTION)
     {
       return fc.getSelectedFile();
     }
     else
     {
       return null;
     }
   }
   
}
