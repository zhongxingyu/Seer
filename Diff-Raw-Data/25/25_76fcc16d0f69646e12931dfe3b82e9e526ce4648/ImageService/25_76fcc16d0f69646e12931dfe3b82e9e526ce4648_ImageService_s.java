 package com.cqlybest.common.mongo.service;
 
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
 import com.cqlybest.common.mongo.bean.Image;
 import com.cqlybest.common.mongo.dao.MongoDb;
 
import eu.medsea.util.MimeUtil;

 @Service("mongoImageService")
 public class ImageService {
 
   @Autowired
   private MongoDb mongoDb;
 
  public Image upload(MultipartFile file) throws IOException {
    String name = file.getOriginalFilename();
    Image image = new Image();
    image.setId(UUID.randomUUID().toString());
    image.setExtension(MimeUtil.getFileExtension(name).toLowerCase());
    image.setContentType(MimeUtil.getMimeType(name).toLowerCase().split(",")[0]);
    image.setData(file.getBytes());

    Date now = new Date();
    image.setCreatedTime(now);
    image.setLastUpdated(now);

    return mongoDb.getMongoDao().createObject("Image", image);
  }

   public void addImage(Image image) {
     mongoDb.createObject("Image", image);
   }
 
   public void updateImage(Image image) {
     mongoDb.updateObject("Image", image.getId(), image);;
   }
 
   public Image getImage(String imageId) {
     return mongoDb.createQuery("Image").eq("_id", imageId).findObject(Image.class);
   }
 
 }
