 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.service.resource;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.activation.DataSource;
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 import org.shredzone.cilla.ws.ImageProcessing;
 import org.shredzone.cilla.ws.ImageType;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Component;
 
 /**
  * Implementation of {@link ImageProcessor}.
  *
  * @author Richard "Shred" Körber
  */
 @Component
 public class ImageProcessorImpl implements ImageProcessor {
 
     @Override
     @Cacheable(value = "processedImages", key = "#id + '-' + #process")
     public ImageProcessorResult process(DataSource ds, long id, ImageProcessing process) throws IOException {
         ImageType type = process.getType();
         int width = process.getWidth();
         int height = process.getHeight();
 
         if (type == null) {
             if (width > 0 && width <= 256 && height > 0 && height <= 256) {
                 type = ImageType.PNG;
             } else {
                 type = ImageType.JPEG;
             }
         }
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(ds.getInputStream());
         if (width > 0 && height > 0) {
             builder.size(width, height);
         } else if (width > 0) {
             builder.width(width);
         } else if (height > 0) {
             builder.height(height);
         }
 
         BufferedImage scaled = builder.asBufferedImage();
 
         if (type == ImageType.JPEG_LOW) {
            jpegQualityWriter(scaled, new MemoryCacheImageOutputStream(out), type.getCompression());
         } else {
             ImageIO.write(scaled, type.getFormatName(), out);
         }
 
         ImageProcessorResult result = new ImageProcessorResult();
         result.setData(out.toByteArray());
         result.setContentType(type.getContentType());
         return result;
     }
 
     /**
      * Writes a JPEG file with adjustable compression quality.
      *
      * @param image
      *            {@link BufferedImage} to write
      * @param out
     *            {@link ImageOutputStream} to write to
      * @param quality
      *            Compression quality between 0.0f (worst) and 1.0f (best)
      */
    private void jpegQualityWriter(BufferedImage image, ImageOutputStream out, float quality)
     throws IOException {
         ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
 
         ImageWriteParam iwp = writer.getDefaultWriteParam();
         iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
         iwp.setCompressionQuality(quality);
 
         IIOImage ioImage = new IIOImage(image, null, null);
 
         writer.setOutput(out);
         writer.write(null, ioImage, iwp);
         writer.dispose();
     }
 
 }
