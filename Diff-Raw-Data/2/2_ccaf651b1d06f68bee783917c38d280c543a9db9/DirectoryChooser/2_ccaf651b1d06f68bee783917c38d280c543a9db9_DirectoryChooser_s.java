 package com.crostec.ads.gui;
 
 import com.crostec.ads.edf.BdfFileChooser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.File;
 
 /**
  *
  */
 public class DirectoryChooser extends JFileChooser {
     private File directory;
    private static final Log log = LogFactory.getLog(BdfFileChooser.class);
 
     public DirectoryChooser() {
         setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     }
 
     public DirectoryChooser(File directory) {
         this();
         this.directory = directory;
     }
 
     public File chooseDirectory() {
         int fileChooserState = showOpenDialog(null);
         if (fileChooserState == JFileChooser.APPROVE_OPTION) {
             return getSelectedFile();
         }
         else{
             return null;
         }
     }
 
     public File chooseDirectory(File directory) {
         this.directory = directory;
         return chooseDirectory();
     }
 
 
     @Override
     public int showOpenDialog(Component component) throws HeadlessException {
         if(directory != null){
             if(directory.isDirectory()) {
                 setSelectedFile(directory);
             }
         }
         return super.showOpenDialog(component);
     }
 
 }
