 package ru.forxy.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import ru.forxy.pojo.Photo;
 
 import java.util.List;
 
 public class PhotoServiceImplTest {
 
     IPhotoService photoService = new PhotoServiceImpl();
 
     @Test
     public void testAddDeletePhoto() {
        photoService.addPhoto(new Photo("xander@gmail.com", "xander"));
        Photo photo = photoService.login("xander@gmail.com", "xander");
        Assert.assertNotNull(photo);
        Assert.assertEquals("xander@gmail.com", photo.getUrl());
        photoService.deletePhoto("xander@gmail.com");
        photo = photoService.login("xander@gmail.com", "xander");
        Assert.assertNull(photo);
     }
 
     @Test
    public void testGetAllUsers() {
         List<Photo> photos = photoService.getPhotos();
         Assert.assertNotNull(photos);
         Assert.assertTrue(CollectionUtils.isNotEmpty(photos));
     }
 }
