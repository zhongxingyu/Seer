 package com.cqlybest.common.mongo.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.cqlybest.common.mongo.bean.Image;
 import com.cqlybest.common.mongo.dao.MongoDb;
 
 @Service("mongoImageService")
 public class ImageService {
 
   @Autowired
   private MongoDb mongoDb;
 
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
