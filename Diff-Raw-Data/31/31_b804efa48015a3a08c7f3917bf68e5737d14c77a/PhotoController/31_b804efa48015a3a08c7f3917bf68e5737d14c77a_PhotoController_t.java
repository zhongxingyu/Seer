 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.action;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.sql.SQLException;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.shiro.SecurityUtils;
 import org.debox.photo.model.Configuration;
 import org.debox.photo.model.Photo;
 import org.debox.photo.model.ThumbnailSize;
 import org.debox.photo.server.ApplicationContext;
 import org.debox.photo.util.SessionUtils;
 import org.debox.photo.util.img.ImageHandler;
 import org.debux.webmotion.server.render.Render;
 import org.debux.webmotion.server.render.RenderStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class PhotoController extends DeboxController {
 
     private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);
     
     public Render getThumbnail(String token, String photoId) throws IOException, SQLException {
         photoId = StringUtils.substringBeforeLast(photoId, ".jpg");
         
         Photo photo;
         if (SessionUtils.isLogged(SecurityUtils.getSubject())) {
             photo = photoDao.getPhoto(photoId);
         } else {
             photo = photoDao.getVisiblePhoto(token, photoId);
         }
         if (photo == null) {
            return renderError(HttpURLConnection.HTTP_NOT_FOUND);
         }
         Configuration configuration = ApplicationContext.getInstance().getConfiguration();
         FileInputStream fis = null;
         try {
             fis = ImageHandler.getInstance().getStream(configuration, photo, ThumbnailSize.SQUARE);
             RenderStatus status = handleLastModifiedHeader(photo, ThumbnailSize.SQUARE);
             if (status.getCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                 return status;
             }
             
         } catch (Exception ex) {
             logger.error("Unable to get stream", ex);
         }
         return renderStream(fis, "image/jpeg");
     }
 
     public Render getPhotoStream(String token, String photoId) throws IOException, SQLException {
         photoId = StringUtils.substringBeforeLast(photoId, ".jpg");       
         
         Photo photo;
         if (SessionUtils.isLogged(SecurityUtils.getSubject())) {
             photo = photoDao.getPhoto(photoId);
         } else {
             photo = photoDao.getVisiblePhoto(token, photoId);
         }
 
         if (photo == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
         Configuration configuration = ApplicationContext.getInstance().getConfiguration();
         FileInputStream fis = null;
         try {
             fis = ImageHandler.getInstance().getStream(configuration, photo, ThumbnailSize.LARGE);
             RenderStatus status = handleLastModifiedHeader(photo, ThumbnailSize.LARGE);
             if (status.getCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                 return status;
             }
             
         } catch (Exception ex) {
             logger.error("Unable to get stream", ex);
         }
         return renderStream(fis, "image/jpeg");
     }
 
 }
