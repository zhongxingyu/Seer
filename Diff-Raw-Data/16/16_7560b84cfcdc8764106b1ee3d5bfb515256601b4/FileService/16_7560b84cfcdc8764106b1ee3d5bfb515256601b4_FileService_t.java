 package com.bajoneando.lnramirez.files.services;
 
 import com.bajoneando.lnramirez.files.MongoStoredFile;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import org.bson.types.ObjectId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.web.PageableDefaults;
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

    public void delete(String id) {
        GridFS gridFS = new GridFS(mongoTemplate.getDb(),"images");
        gridFS.remove(new ObjectId(id));
    }
     
     public List<MongoStoredFile> getFiles(@PageableDefaults(pageNumber=0, value=5) Pageable pageable) {
         try {
         List<MongoStoredFile> files = new ArrayList<>(pageable.getPageSize());
         GridFS gridFS = new GridFS(mongoTemplate.getDb(),"images");
         DBObject sortCriteria = new BasicDBObject("uploadDate",-1);
         DBCursor dBCursor = gridFS.getFileList();
         dBCursor.sort(sortCriteria);
         dBCursor.skip(pageable.getOffset()).limit(pageable.getOffset() + pageable.getPageSize());
         while (dBCursor.hasNext()) {
             DBObject dbObject = dBCursor.next();
             ObjectId objectId = (ObjectId) dbObject.get("_id");
             MongoStoredFile mongoStoredFile = new MongoStoredFile();
             mongoStoredFile.setId(objectId.toString());
             mongoStoredFile.setName((String) dbObject.get("filename"));
             mongoStoredFile.setContentType((String) dbObject.get("contentType"));
             files.add(mongoStoredFile);
         }
         return files;
         } catch (Throwable e) {
             e.printStackTrace();
             throw e;
         }
     }
     
     public MongoStoredFile getFile(String objectId) throws IOException  {
         GridFS gridFS = new GridFS(mongoTemplate.getDb(),"images");
         GridFSDBFile file = gridFS.find(new ObjectId(objectId));
         MongoStoredFile mongoStoredFile = new MongoStoredFile();
         mongoStoredFile.setId(objectId.toString());
         mongoStoredFile.setName((String) file.get("filename"));
         mongoStoredFile.setContentType((String) file.get("contentType"));
         mongoStoredFile.setInputStream(file.getInputStream());
         return mongoStoredFile;
     }
     
     @Autowired
     private MongoTemplate mongoTemplate;
     
 }
