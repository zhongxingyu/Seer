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
        Assert.assertNotNull(photoService);
     }
 
     @Test
    public void testGetAllPhotos() {
         List<Photo> photos = photoService.getPhotos();
         Assert.assertNotNull(photos);
         Assert.assertTrue(CollectionUtils.isNotEmpty(photos));
     }
 }
