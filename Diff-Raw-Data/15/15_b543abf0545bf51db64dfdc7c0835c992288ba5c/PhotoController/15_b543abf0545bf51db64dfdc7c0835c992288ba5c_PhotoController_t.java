 package se.bhg.photos.web;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.MemoryCacheImageOutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
 
 import com.mortennobel.imagescaling.ResampleOp;
 import com.mortennobel.imagescaling.AdvancedResizeOp;
 
 import se.bhg.photos.model.Photo;
 import se.bhg.photos.service.FileService;
 import se.bhg.photos.service.PhotoService;
 
 @Controller
 public class PhotoController {
     private static final Logger LOG = LoggerFactory.getLogger(PhotoController.class);
     @Autowired
     FileService fileService;
 
     @Autowired
     PhotoService photoService;
 
     @ResponseBody
     @RequestMapping(value = "/photo/{id}/{max}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getPhoto(@PathVariable String id, @PathVariable int max, WebRequest webRequest) throws IOException {
        if (webRequest.checkNotModified(0)) {
            return null;
        }

         Photo photo = photoService.getPhoto(id);
 
         if (photo == null) {
             return null;
         }
 
         if (max > 0) {
             long start = System.currentTimeMillis();
             File img = new File(photo.getPath());
             BufferedImage original = ImageIO.read(img);
             int height = original.getHeight();
             int width = original.getWidth();
             double factor = 0f;
             long newX, newY;
             LOG.debug("Original size {}x{}", width, height);
             if (height > width) {
             	factor = max * 1.0d / height * 1.0d;
             } else {
             	factor = max * 1.0d / width * 1.0d;
             }
             LOG.debug("Max is {}, factor is {}", max, factor);
             newX = Math.round(height * factor);
             newY = Math.round(width * factor);
             LOG.debug("New size {}x{}", newX, newY);
             
             ResampleOp resampleOp = new ResampleOp((int) newY, (int) newX);
             resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
             BufferedImage resized = resampleOp.filter(original, null);
 
             byte[] data = writeJpeg(resized, 0.99f);
 
             long end = System.currentTimeMillis();
             LOG.debug("Time to resize image: {}", end - start);
             return data;
 
         } else {
             return fileService.readFile(photo.getPath());
         }
     }
     
     @ResponseBody
     @RequestMapping(value = "/photo/{id}/{x}/{y}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
     public byte[] getPhoto(@PathVariable String id, @PathVariable int x, @PathVariable int y) throws IOException {
         Photo photo = photoService.getPhoto(id);
 
         if (photo == null) {
             return null;
         }
 
         if (x > 0 && y > 0) {
             long start = System.currentTimeMillis();
             File img = new File(photo.getPath());
             BufferedImage original = ImageIO.read(img);
             ResampleOp resampleOp = new ResampleOp(x, y);
             resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
             BufferedImage resized = resampleOp.filter(original, null);
 
             byte[] data = writeJpeg(resized, 0.9f);
 
             long end = System.currentTimeMillis();
             LOG.debug("Time to resize image: {}", end - start);
             return data;
 
         } else {
             return fileService.readFile(photo.getPath());
         }
     }
 
 
     /**
      * http://stackoverflow.com/questions/7742175/how-to-get-a-good-quality-
      * thumbnail
      * 
      * Write a JPEG file setting the compression quality.
      * 
      * @param image
      *            a BufferedImage to be saved
      * @param destFile
      *            destination file (absolute or relative path)
      * @param quality
      *            a float between 0 and 1, where 1 means uncompressed.
      * @throws IOException
      *             in case of problems writing the file
      */
     private byte[] writeJpeg(BufferedImage image, float quality) throws IOException {
         ImageWriter writer = null;
 
         writer = ImageIO.getImageWritersByFormatName("jpeg").next();
         ImageWriteParam param = writer.getDefaultWriteParam();
         param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
         param.setCompressionQuality(quality);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
         writer.setOutput(mcios);
         IIOImage iioImage = new IIOImage(image, null, null);
         writer.write(null, iioImage, param);
         byte[] ret = baos.toByteArray();
         baos.flush();
         return ret;
     }
 }
