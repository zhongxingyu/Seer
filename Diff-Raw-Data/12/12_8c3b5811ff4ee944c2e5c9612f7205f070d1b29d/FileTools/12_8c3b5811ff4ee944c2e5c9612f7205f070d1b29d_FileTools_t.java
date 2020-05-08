 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ecn.pappl.visedsim.utilities;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
 *
  * @author bastien
  */
 public final class FileTools {
 
     public final static List<String> getFilesNamesInDirectory(
             String directoryPath) {
         List<String> filesNames = new LinkedList<String>();
 
         File directory = new File(directoryPath);
         File[] files = directory.listFiles();
 
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if(!files[i].isHidden()){
                    filesNames.add(files[i].getName());
                }
                
            }
         }
 
         return filesNames;
     }
 }
