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
 package org.shredzone.cilla.admin;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import javax.activation.DataHandler;
 import javax.faces.context.FacesContext;
 import javax.imageio.ImageIO;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.shredzone.cilla.ws.ImageProcessing;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 /**
  * Abstract superclass for image providing beans.
  * <p>
  * It provides methods for fetching image IDs or indexes, and helps creating
  * {@link StreamedContent} for the resulting image.
  *
  * @author Richard "Shred" Körber
  */
 @Component
 public abstract class AbstractImageBean implements Serializable {
     private static final long serialVersionUID = 3624378631087966758L;
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private @Value("${previewImageMaxCache}") int maxCache;
 
     private final Map<DataHandler, byte[]> weakScaleCache = new WeakHashMap<>();
 
     /**
      * Gets the "renderId" request parameter used for rendering images by their ID.
      */
     protected Long getFacesRenderId() {
         String renderId = FacesContext.getCurrentInstance().getExternalContext()
                         .getRequestParameterMap().get("renderId");
         return (renderId != null ? Long.valueOf(renderId) : null);
     }
 
     /**
      * Gets the "index" request parameter used for rendering images by their index.
      */
     protected Integer getFacesIndex() {
         String index = FacesContext.getCurrentInstance().getExternalContext()
                         .getRequestParameterMap().get("index");
         return (index != null ? Integer.valueOf(index) : null);
     }
 
     /**
      * Creates an empty {@link StreamedContent}. Should be used when no image can be
      * streamed.
      *
      * @return {@link StreamedContent} containing an empty file
      */
     protected StreamedContent createEmptyStreamedContent() {
         return new DefaultStreamedContent(new ByteArrayInputStream(new byte[0]), "image/png");
     }
 
     /**
      * Creates a {@link StreamedContent} for the given {@link DataHandler}.
      *
      * @param dh
      *            {@link DataHandler} to stream
      * @return {@link StreamedContent} containing that image
      */
     protected StreamedContent createStreamedContent(DataHandler dh) {
         if (dh != null) {
             try {
                 return new DefaultStreamedContent(dh.getInputStream(), dh.getContentType());
             } catch (IOException ex) {
                 log.error("Exception while streaming content", ex);
             }
         }
         return createEmptyStreamedContent();
     }
 
     /**
      * Creates a {@link StreamedContent} for the given {@link DataHandler}, with the image
      * having the given width and height. The aspect ratio is kept. A PNG type image is
      * returned.
      * <p>
      * <em>NOTE:</em> The scaled image is cached related to the {@link DataHandler}. Thus,
      * it is not possible to create different sizes of the same {@link DataHandler} using
      * this method. A weak cache is used, to keep the memory footprint as small as
      * possible.
      *
      * @param dh
      *            {@link DataHandler} to stream
      * @param width
      *            Maximum image width
      * @param height
      *            Maximum image height
      * @return {@link StreamedContent} containing that image
      */
     protected StreamedContent createStreamedContent(DataHandler dh, ImageProcessing process) {
         byte[] scaledData = null;
         if (dh != null) {
             scaledData = weakScaleCache.get(dh);
             if (scaledData == null) {
                 try {
                     BufferedImage image = ImageIO.read(dh.getInputStream());
                     if (image != null) {
                         BufferedImage scaled = scale(image, process.getWidth(), process.getHeight());
                         try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                             ImageIO.write(scaled, process.getType().getFormatName(), bos);
                             scaledData = bos.toByteArray();
                         }
                         if (weakScaleCache.size() < maxCache) {
                             weakScaleCache.put(dh, scaledData);
                         }
                     }
                 } catch (IOException ex) {
                     log.error("Exception while streaming scaled content: " + dh.getName(), ex);
                 }
             }
         }
 
         if (scaledData != null) {
            return new DefaultStreamedContent(new ByteArrayInputStream(scaledData),
                            process.getType().getContentType());
         } else {
             return createEmptyStreamedContent();
         }
     }
 
     /**
      * Scales a {@link BufferedImage} to the given size, keeping the aspect ratio. If the
      * image is smaller than the resulting size, it will be magnified.
      *
      * @param image
      *            {@link BufferedImage} to scale
      * @param width
      *            Maximum result width
      * @param height
      *            Maximum result height
      * @return {@link BufferedImage} with the scaled image
      */
     private BufferedImage scale(BufferedImage image, int width, int height) {
         ImageObserver observer = null;
 
         Image scaled = null;
         if (image.getWidth() > image.getHeight()) {
             scaled = image.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
         } else {
             scaled = image.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
         }
 
         BufferedImage result = new BufferedImage(
                 scaled.getWidth(observer), scaled.getHeight(observer),
                 BufferedImage.TYPE_INT_RGB);
         result.createGraphics().drawImage(scaled, 0, 0, observer);
         return result;
     }
 
 }
