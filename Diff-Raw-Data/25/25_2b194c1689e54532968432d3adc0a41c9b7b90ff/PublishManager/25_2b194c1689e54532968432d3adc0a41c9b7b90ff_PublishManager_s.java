 package com.abudko.scheduled.service.huuto;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.image.ImageManipulator;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.scheduled.csv.PhotoData;
 import com.abudko.scheduled.service.PhotoManager;
 import com.abudko.scheduled.vkontakte.Photo;
 
 @Component
 public class PublishManager {
 
     private static final String COMMENT_KEY = "huuto.comment";
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     @Qualifier("groupPhotoManager")
     private PhotoManager photoManager;
 
     @Autowired
     protected ApplicationContext context;
 
     @Value("#{scheduledProperties['imageTempFile']}")
     private String imageTempFileLocation;
 
     @Autowired
     private ImageManipulator imageManipulator;
 
     @Autowired
     private AlbumMapper albumMapper;
 
     @Autowired
     private PublishManagerUtils publishManagerUtils;
 
     public void publishResults(Category category, Collection<ListResponse> queryResponses) throws InterruptedException,
             UnsupportedEncodingException {
         for (ListResponse queryResponse : queryResponses) {
             log.info(String.format("Publishing query response: %s", queryResponse.toString()));
 
             PhotoData photoData = convert(category, queryResponse);
 
             String id = queryResponse.getItemResponse().getId();
             if (isPhotoPublished(id, photoData) == false) {
                 photoManager.publishPhoto(photoData);
             } else {
                 log.info(String.format("Photo with id '%s' aleady published", id));
             }
         }
     }
 
     private PhotoData convert(Category category, ListResponse listResponse) throws UnsupportedEncodingException {
         cropImage(listResponse);
 
         PhotoData photoData = new PhotoData();
         photoData.setGroupId(AlbumMapper.GROUP_ID);
         photoData.setAlbumId(albumMapper.getAlbumId(category.name(), Integer.valueOf(listResponse.getSize())));
         photoData.setDescription(getDescription(category, listResponse));
         photoData.setFileResource(new FileSystemResource(imageTempFileLocation));
 
         return photoData;
     }
     
     private void cropImage(ListResponse listResponse) {
         ItemResponse itemResponse = listResponse.getItemResponse();
         String url = itemResponse.getImgBaseSrc() + "-orig.jpg";
         imageManipulator.storeImage(url, "file:" + imageTempFileLocation);
     }
     
     private String getDescription(Category category, ListResponse listResponse) {
         ItemResponse itemResponse = listResponse.getItemResponse();
         String newPrice = itemResponse.getNewPrice();
         String id = itemResponse.getId();
         String size = listResponse.getSize();
         String brand = listResponse.getBrand();
         String title = category.getLabel();
         
         String description = context.getMessage(COMMENT_KEY, new Object[] { title, brand, size, newPrice, id },
                 Locale.getDefault());
         
         return description;
     }
 
     private boolean isPhotoPublished(String id, PhotoData photoData) {
         List<Photo> photos = photoManager.getPhotos(photoData.getGroupId(), photoData.getAlbumId());
         for (Photo photo : photos) {
             String description = photo.getDescription();
             if (id.equals(publishManagerUtils.getId(description))) {
                 return true;
             }
         }
 
         return false;
     }
 }
