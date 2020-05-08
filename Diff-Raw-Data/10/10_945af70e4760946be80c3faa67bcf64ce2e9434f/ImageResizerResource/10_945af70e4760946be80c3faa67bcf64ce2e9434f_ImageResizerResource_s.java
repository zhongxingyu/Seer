 package com.higomo.media.imageresizer.web.resource;
 
 import com.higomo.media.imageresizer.*;
 import com.higomo.media.imageresizer.web.InvalidURIException;
 import com.higomo.media.imageresizer.web.StreamingImageOutput;
 import com.higomo.media.imageresizer.web.URIConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.StreamingOutput;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 @Path("/resizer")
 @Component
 @PropertySource("/com/higomo/media/imageresizer/web/config/imageresizer.properties")
 public class ImageResizerResource {
     private final static Logger log = LoggerFactory.getLogger(ImageResizerResource.class);
     @Autowired
     private FileDownloadService downloadService;
     @Autowired
     @Qualifier("Im4JavaImageResizer")
     private ImageResizer imageResizer;
     @Autowired
     private ExecutorService executorService;
     @Autowired
     private Environment environment;
 
     @GET
     @Path("/image")
     public Response image(
             @QueryParam("uri") String uriString,
             @DefaultValue("320") @QueryParam("width") int width,
             @DefaultValue("240") @QueryParam("height") int height,
             @DefaultValue("false") @QueryParam("crop") boolean crop
     ) throws FileSizeLimitExceededException, ImageResizeException, InvalidURIException, FileDownloadException, IOException {
         URI uri = URIConverter.fromBase64Encoded(uriString);
 
         if (environment.getProperty("higomo.imageresizer.allowedDomains", "").length() > 0) {
             Set<String> allowedDomains = new HashSet<String>(Arrays.asList(StringUtils.tokenizeToStringArray(environment.getProperty("higomo.imageresizer.allowedDomains", ""), ",")));
             if (!allowedDomains.contains(uri.getHost())) {
                 log.debug(uri.getHost() + "not found in allowed domains: " + allowedDomains.toString());
                 return Response.status(403).build();
             }
         }
 
         File source = null;
         try {
             source = downloadService.download(uri);
             BufferedImage sourceImage = ImageIO.read(source);
             BufferedImage resized =
                     imageResizer.resize(sourceImage, width, height, crop);

             BufferedImage resizedRGB = new BufferedImage(resized.getWidth(),
                     resized.getHeight(), BufferedImage.TYPE_INT_RGB);
 
             // write data into an RGB buffered image, no transparency
             resizedRGB.setData(resized.getData());

            StreamingOutput output = new StreamingImageOutput(resizedRGB);
 
             return Response
                     .ok(output)
                     .type("image/jpeg")
                     .header(
                             "Content-Disposition",
                            String.format("attachment; filename=image-%dx%d.jpg", width, height))
                     .build();
 
         } finally {
             delete(source);
         }
     }
 
     private void delete(File file) {
         if (file != null && file.exists()) {
             if (file.delete()) {
                 log.debug("Deleted {}", file.getName());
             } else {
                 log.error("Failed to delete {}", file.getName());
             }
         }
     }
 
 }
