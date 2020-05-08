 package com.cqlybest.common.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
 
 import net.coobird.thumbnailator.filters.Watermark;
 import net.coobird.thumbnailator.geometry.Positions;
 
 import org.imgscalr.Scalr;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.bean.Image;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.OptionService;
 
 @Controller
public class ImageController extends ControllerHelper {
 
   @Autowired
   private ImageService imageService;
   @Autowired
   private OptionService optionService;
 
   @RequestMapping(method = RequestMethod.GET, value = "/image/upload")
   public void upload() {}
 
   @RequestMapping(method = RequestMethod.POST, value = "/image/upload")
   @ResponseBody
   public Object upload(@RequestParam MultipartFile file) throws Exception {
     Image image = imageService.multipartFileToImage(file);
     imageService.add(image);
     return image;
   }
 
   @RequestMapping(value = "/image/update", method = RequestMethod.POST)
   @ResponseBody
   public void update(@RequestParam String pk, @RequestParam(required = false) String name,
       @RequestParam(required = false) String value,
       @RequestParam(required = false, value = "name[]") List<String> names,
       @RequestParam(required = false, value = "value[]") List<String> values) {
     if (name != null && value != null) {
       imageService.update(pk, name, value);
     }
     if (names != null && values != null && names.size() == values.size()) {
       for (int i = 0; i < names.size(); i++) {
         imageService.update(pk, names.get(i), values.get(i));
       }
     }
   }
 
   @RequestMapping(value = "/image/delete", method = RequestMethod.POST)
   @ResponseBody
   public void delete(@RequestParam String id) {
     imageService.delete(id);
   }
 
   @RequestMapping(value = "/image/{imageId}.{imageType:jpg|png|gif}", method = RequestMethod.GET)
  public Object view(@PathVariable String imageId, @PathVariable String imageType,
       @RequestParam(required = false) Integer width,
       @RequestParam(required = false) Integer height,
       @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince)
       throws IOException {
     if (ifModifiedSince != null) {
       return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
     }
     Image image = imageService.get(imageId);
     if (!imageType.equals(image.getImageType())) {
       return new ResponseEntity<byte[]>(null, null, HttpStatus.NOT_FOUND);
     }
     HttpHeaders headers = new HttpHeaders();
     headers.setDate(System.currentTimeMillis());
     headers.setLastModified(0);
     headers.setExpires(System.currentTimeMillis() + 31536000000L);
     headers.setCacheControl("max-age=31536000000");
     if (imageType.equals("jpg")) {
       headers.setContentType(MediaType.IMAGE_JPEG);
     } else if (imageType.equals("png")) {
       headers.setContentType(MediaType.IMAGE_PNG);
     } else if (imageType.equals("gif")) {
       headers.setContentType(MediaType.IMAGE_GIF);
     }
 
     if (width == null || height == null) {
       Map<String, String> options = optionService.getOptions();
       String watermarkId = options.get(Constant.IMAGE_WATERMARK_IMAGE_ID);
       String watermarkPosition = options.get(Constant.OPTION_WATERMARK_POSITION);
 
       if (watermarkId != null && watermarkPosition != null) {
         BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getImageData()));
         BufferedImage watermarkImage =
             ImageIO.read(new ByteArrayInputStream(imageService.get(watermarkId).getImageData()));
         if (bufferedImage.getWidth() > watermarkImage.getWidth()
             && bufferedImage.getHeight() > watermarkImage.getHeight()) {
           // 水印
           Watermark watermarkFilter =
               new Watermark(Positions.valueOf(watermarkPosition), watermarkImage, 1);
           bufferedImage = watermarkFilter.apply(bufferedImage);
           ByteArrayOutputStream out = new ByteArrayOutputStream();
          write(imageType, bufferedImage, out);
           return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.OK);
         }
       }
       return new ResponseEntity<byte[]>(image.getImageData(), headers, HttpStatus.OK);
     }
 
     // 缩放
     BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getImageData()));
     boolean fixToWidth =
         (double) bufferedImage.getWidth() / (double) bufferedImage.getHeight() < (double) width
             / (double) height;
     BufferedImage scaleImage =
        Scalr.resize(bufferedImage, Scalr.Method.QUALITY, fixToWidth
             ? Scalr.Mode.FIT_TO_WIDTH
             : Scalr.Mode.FIT_TO_HEIGHT, width, height, Scalr.OP_ANTIALIAS);
     scaleImage =
         Scalr.crop(scaleImage, (scaleImage.getWidth() - width) / 2,
             (scaleImage.getHeight() - height) / 2, width, height);
     ByteArrayOutputStream scaleOut = new ByteArrayOutputStream();
    write(imageType, scaleImage, scaleOut);
     return new ResponseEntity<byte[]>(scaleOut.toByteArray(), headers, HttpStatus.OK);
   }

  private void write(String type, BufferedImage in, ByteArrayOutputStream out) throws IOException {
    ImageWriter writer = ImageIO.getImageWritersBySuffix(type).next();
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(1.0F);
    ImageOutputStream ios = ImageIO.createImageOutputStream(out);
    writer.setOutput(ios);
    writer.write(null, new IIOImage(in, null, null), param);
  }
 }
