 package com.herokuapp.webgalleryshowcase.web.gallery;
 
 import com.herokuapp.webgalleryshowcase.dao.ImageItemDao;
 import com.herokuapp.webgalleryshowcase.domain.ImageItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 @Controller
 @RequestMapping("/albums/{albumId}")
 public class ImageController {
 
     private final Logger log = LoggerFactory.getLogger(ImageController.class);
 
     @Autowired
     private ImageItemDao imageItemDao;
 
     @RequestMapping("/upload")
     public String displayUploadImage() {
         return "uploadImage";
     }
 
     @RequestMapping(value = "/images/{imageId}/{fileName}",
             method = RequestMethod.GET,
             produces = {"image/jpeg", "image/jpg", "image/png"})
     public
     @ResponseBody
     byte[] showImage(@PathVariable("albumId") Integer albumId,
                      @PathVariable("imageId") Integer imageId,
                      @PathVariable("fileName") String fileName,
                      HttpServletResponse response) {
         ImageItem imageItem = imageItemDao.retrieveImage(albumId, imageId);
 
         if (imageItem == null) return new byte[0];
 
         response.setContentType(imageItem.getContentType());
         response.setHeader("Content-Disposition", "inline; filename=\"" + imageItem.getFileName() + "\"");
         response.setHeader("Cache-Control", "max-age=3600");
 
         return imageItem.getFileContent();
     }
 
     @RequestMapping(value = "/images", method = RequestMethod.GET)
     public
     @ResponseBody
     List<ImageItem> uploadImage(@PathVariable int albumId, @RequestParam int fromItem) {
         final int amount = 12;
         return imageItemDao.retrieveThumbnailsId(albumId, fromItem, amount);
     }
 
     @RequestMapping(value = "/images", method = RequestMethod.POST)
     public String uploadFile(@PathVariable Integer albumId,
                              @RequestParam("title") String title,
                              @RequestParam("file") MultipartFile file) {
 
         String returningPath = "redirect:/albums/" + albumId + "/upload?result=";
         final String statusFault = "fault";
         final String statusSuccess = "success";
 
         if (file.isEmpty() | !isContentTypeValid(file.getContentType())) {
             log.info("Trying upload empty or wrong MIME type file.");
             return returningPath + statusFault;
         }
 
         ImageItem imageItem = new ImageItem();
         String fileName = file.getOriginalFilename();
 
         imageItem.setFileName(replaceSpacesWithUnderscore(fileName));
         imageItem.setContentType(file.getContentType());
         imageItem.setAlbumHolderId(albumId);
         imageItem.setTitle(title);
 
         String uploadStatus;
         try {
             imageItem.setFileContent(file.getBytes());
             imageItemDao.uploadImage(imageItem);
             uploadStatus = statusSuccess;
         } catch (IOException ioe) {
             log.error("IO exception while uploading file", ioe);
             uploadStatus = statusFault;
         } catch (Exception e) {
             log.error("Exception while uploading file", e);
             uploadStatus = statusFault;
         }
 
         return returningPath + uploadStatus;
     }
 
     @RequestMapping(value = "/images/{imageId}", method = RequestMethod.DELETE)
     public boolean deleteImage(@PathVariable("imageId") Integer imageId) {
         return false;
     }
 
     private boolean isContentTypeValid(String fileContentType) {
         return fileContentType.equals(MediaType.IMAGE_JPEG_VALUE) | fileContentType.equals(MediaType.IMAGE_PNG_VALUE);
     }
 
     private String replaceSpacesWithUnderscore(String string) {
         return string.replace(' ', '_');
     }
}
