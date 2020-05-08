 package com.bajoneando.lnramirez.files.services;
 
 import com.bajoneando.lnramirez.files.MongoStoredFile;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSInputFile;
 import java.io.IOException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.stereotype.Service;
 
 /**
  *
  * @author lrmonterosa
  */
 @Service
 public class FileService {
     
     public String save(MongoStoredFile file) throws IOException {
         GridFS gridFS = new GridFS(mongoTemplate.getDb(),"images");
         GridFSInputFile inputFile = gridFS.createFile(file.getData());
         inputFile.setContentType(file.getContentType());
         inputFile.setFilename(file.getName());
         inputFile.save();
        return inputFile.getId().toString();
     }
     
     @Autowired
     private MongoTemplate mongoTemplate;
     
 }
