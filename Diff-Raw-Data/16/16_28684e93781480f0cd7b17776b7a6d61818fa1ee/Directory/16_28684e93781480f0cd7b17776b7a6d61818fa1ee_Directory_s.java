 package com.callcenter.external.model;
 
 import com.callcenter.util.Constants;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author deep
  */
 public class Directory {
 
     private File file;
 
     public Directory(final String directoryPath) {
         file = new File(directoryPath);
     }
 
     public String getAbsolutePath() {
         return file.getAbsolutePath();
     }
 
     public boolean isValid() {
         return file.isDirectory();
     }
 
     public List<File> list() {
        final List<File> files = new ArrayList<File>();
        for(String fileName : file.list()) {
            files.add(
                    new File(file.getPath() + Constants.System.FILE_PATH_SEPERATOR + fileName));
        }
        return files;
     }
 }
