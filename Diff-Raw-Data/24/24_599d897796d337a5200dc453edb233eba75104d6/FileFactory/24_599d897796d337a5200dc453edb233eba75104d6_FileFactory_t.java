 package org.sukrupa.app.services;
 
 import org.springframework.stereotype.Component;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 
 @Component
 public class FileFactory {
     public File create(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if(!file.exists()){
            throw new FileNotFoundException("Image not found");
        }
        return file;
     }
 }
