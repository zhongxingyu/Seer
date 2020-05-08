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
 
 import com.abudko.reseller.huuto.image.ImageManipulator;
 import com.abudko.reseller.huuto.query.enumeration.Category;
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.scheduled.csv.PhotoData;
 import com.abudko.scheduled.service.PhotoManager;
 import com.abudko.scheduled.vkontakte.Photo;
 
 public abstract class AbstractPublishManager implements PublishManager {
 
     protected static final String COMMENT_KEY = "huuto.comment";
     protected static final String GROUP_URL = "vk.com/kombezi.finland";
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     @Qualifier("groupPhotoManager")
     private PhotoManager photoManager;
 
     @Autowired
     protected ApplicationContext context;
 
     @Value("#{scheduledProperties['imageTempFile']}")
     protected String imageTempFileLocation;
 
     @Autowired
     private ImageManipulator imageManipulator;
 
     @Autowired
     protected AlbumMapper albumMapper;
 
     @Autowired
     private PublishManagerUtils publishManagerUtils;
 
     @Override
     public void publishResults(Category category, Collection<ListResponse> queryResponses) throws InterruptedException,
             UnsupportedEncodingException {
         for (ListResponse queryResponse : queryResponses) {
             try {
                 log.info(String.format("Publishing query response: %s", queryResponse.toString()));
 
                 PhotoData photoData = convert(category, queryResponse);
                 if (photoData.getAlbumId() == null) {
                     log.warn(String.format("Unable to extract albumid for '%s' for category '%s'", queryResponse,
                             category.name()));
                     continue;
                 }
 
                 String id = queryResponse.getItemResponse().getId();
                 if (isPhotoPublished(id, photoData) == false) {
                     photoManager.publishPhoto(photoData);
                 } else {
                     log.info(String.format("Photo with id '%s' aleady published", id));
                 }
             } catch (Throwable e) {
                String error = String.format("Error '%s' happened while publishing queryResponse '%s'", e.getMessage(), queryResponse);
                log.error(error, e.getMessage());
                 throw e;
             }
         }
     }
 
     private PhotoData convert(Category category, ListResponse listResponse) throws UnsupportedEncodingException {
         cropImage(listResponse);
         return getPhotoData(category, listResponse);
     }
 
     protected abstract String getImgUrl(ListResponse listResponse);
 
     private void cropImage(ListResponse listResponse) {
         String url = getImgUrl(listResponse);
         // no need to add advert.
         imageManipulator.storeImage(url, "file:" + imageTempFileLocation, null);
     }
 
     protected PhotoData getPhotoData(Category category, ListResponse listResponse) {
         PhotoData photoData = new PhotoData();
         photoData.setGroupId(AlbumMapper.GROUP_ID);
         photoData.setAlbumId(albumMapper.getAlbumId(category.name(), listResponse));
         photoData.setDescription(getDescription(category, listResponse));
         photoData.setFileResource(new FileSystemResource(imageTempFileLocation));
 
         return photoData;
     }
 
     protected String getDescription(Category category, ListResponse listResponse) {
         ItemResponse itemResponse = listResponse.getItemResponse();
         String newPrice = itemResponse.getNewPrice();
         String id = itemResponse.getId();
         String size = listResponse.hasManySizes() ? listResponse.getItemResponse().getSizes().toString()
                 .replaceAll("\\[|\\]", "") : listResponse.getSize();
         String brand = listResponse.getBrand();
         String title = category.getLabel();
 
         String description = context.getMessage(COMMENT_KEY, new Object[] { title, brand, size, newPrice, id },
                 Locale.getDefault());
 
         return description;
     }
 
     private boolean isPhotoPublished(String id, PhotoData photoData) throws InterruptedException {
         List<Photo> photos = photoManager.getPhotos(photoData.getGroupId(), photoData.getAlbumId());
         for (Photo photo : photos) {
             String description = photo.getDescription();
             String descriptionId = publishManagerUtils.getId(description);
             if (id.equals(descriptionId)) {
                 return true;
             }
         }
 
         return false;
     }
 
 }
