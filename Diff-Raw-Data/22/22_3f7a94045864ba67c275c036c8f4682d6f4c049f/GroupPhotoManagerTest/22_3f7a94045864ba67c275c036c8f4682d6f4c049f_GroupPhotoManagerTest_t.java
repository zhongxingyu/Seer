 package com.abudko.scheduled.service;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.abudko.scheduled.vkontakte.SavedPhoto;
 import com.abudko.scheduled.vkontakte.UploadedPhoto;
 
 @RunWith(MockitoJUnitRunner.class)
 public class GroupPhotoManagerTest extends PhotoManagerTestHelper {
 
     @Before
     public void setup() {
         photoManager = new GroupPhotoManager();
         super.setup();        
         setupMocks();
         when(photosTemplate.savePhoto(Mockito.any(UploadedPhoto.class), Mockito.anyString())).thenReturn(
                 new SavedPhoto());
     }
 
     @Test
     public void testReadDumpFile() throws Exception {
         final String dumpFileLocation = "dumpFileLocation";
         photoManager.publish("csvResourcePath", dumpFileLocation, null);
 
         verify(photoDataLogger).read(dumpFileLocation);
     }
     
     @Test
     public void testDeleteAll() throws Exception {
         final String dumpFileLocation = "dumpFileLocation";
         Map<String, String> dump = new HashMap<String, String>();
         final String photoId1 = "photoId1";
         final String photoId2 = "photoId2";
         final String groupId1 = "groupId1";
         final String groupId2 = "groupId2";
         dump.put(photoId1, groupId1);
         dump.put(photoId2, groupId2);
         when(photoDataLogger.read(dumpFileLocation)).thenReturn(dump);
 
         photoManager.publish("csvResourcePath", dumpFileLocation, null);
 
         verify(photosTemplate).getCommentsCount(photoId1, "-"+groupId1);
         verify(photosTemplate).getCommentsCount(photoId2, "-"+groupId2);
         verify(photosTemplate).deletePhoto(photoId1, "-"+groupId1);
         verify(photosTemplate).deletePhoto(photoId2, "-"+groupId2);
     }
     
     @Test
     public void testCannotDeleteBecauseOfComment() throws Exception {
         final String dumpFileLocation = "dumpFileLocation";
         Map<String, String> dump = new HashMap<String, String>();
         final String photoId1 = "photoId1";
         final String groupId1 = "groupId1";
         dump.put(photoId1, groupId1);
         when(photoDataLogger.read(dumpFileLocation)).thenReturn(dump);
         when(photosTemplate.getCommentsCount(photoId1, "-"+groupId1)).thenReturn(1);
         
         photoManager.publish("csvResourcePath", dumpFileLocation, null);
         
         verify(photosTemplate, times(0)).deletePhoto(photoId1, "-"+groupId1);
     }
     
     @Test
     public void testPublish() throws Exception {
         final String fileLocation = "fileLocation";
         getTestData().get(0).setFileLocation(fileLocation);
         final String description = "description";
         getTestData().get(0).setDescription(description);
         final String uploadUrl = "uploadUrl";
         when(photosTemplate.getUploadServer(GROUPID1, ALBUMID1)).thenReturn(uploadUrl);
         final UploadedPhoto uploadedPhoto = new UploadedPhoto();
         when(photosTemplate.uploadPhoto(uploadUrl, "/photos/" + fileLocation)).thenReturn(uploadedPhoto);
 
         photoManager.publish("csvResourcePath", "dumpFileLocation", null);
 
         verify(photosTemplate).savePhoto(uploadedPhoto, description);
     }
 
     @Test
     public void testDumpAllWhenException() throws Exception {
         final String dumpFileLocation = "dumpFileLocation";
         final String fileLocation = "fileLocation";
         getTestData().get(0).setFileLocation(fileLocation);
         final String description = "description";
         getTestData().get(0).setDescription(description);
         final String uploadUrl = "uploadUrl";
         when(photosTemplate.getUploadServer(GROUPID1, ALBUMID1)).thenReturn(uploadUrl);
         final UploadedPhoto uploadedPhoto = new UploadedPhoto();
         when(photosTemplate.uploadPhoto(uploadUrl, "/photos/" + fileLocation)).thenReturn(uploadedPhoto);
         SavedPhoto savedPhoto = new SavedPhoto();
         final String ownerId = "ownerId";
         savedPhoto.setOwnerId(ownerId);
         final String photoId = "photoId";
         savedPhoto.setPhotoId(photoId);
         when(photosTemplate.savePhoto(uploadedPhoto, description)).thenReturn(savedPhoto);
         when(photosTemplate.getUploadServer(GROUPID1, ALBUMID2)).thenThrow(new RuntimeException());
 
         try {
             photoManager.publish("csvResourcePath", dumpFileLocation, null);
             fail("should throw an exception before");
         }
         catch (Exception e) {
             // as expected
         }
 
         Map<String, String> map = new HashMap<String, String>();
         map.put(photoId, ownerId);
         verify(photoDataLogger).dump(map, dumpFileLocation);
     }
     
     @Test
     public void testNoDumpWhenDumpMapIsEmpty() throws Exception {
         final String dumpFileLocation = "dumpFileLocation";
         when(photosTemplate.getUploadServer(GROUPID1, ALBUMID1)).thenThrow(new RuntimeException());
 
         try {
             photoManager.publish("csvResourcePath", dumpFileLocation, null);
             fail("should throw an exception before");
         }
         catch (Exception e) {
             // as expected
         }
 
         verify(photoDataLogger, times(0)).dump(Mockito.anyMap(), Mockito.eq(dumpFileLocation));
     }
    
    @Test
    public void testOwnerIdNoPrefix() throws Exception {
        final String ownerId = "238623726";
        assertEquals("-"+ownerId, photoManager.getOwnerId(ownerId));
    }
    
    @Test
    public void testOwnerIdWithPrefix() throws Exception {
        final String ownerId = "238623726";
        assertEquals("-"+ownerId, photoManager.getOwnerId("-"+ownerId));
    }
 }
