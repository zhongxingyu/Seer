 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions an
  * limitations under the License.
  */
 
 package org.icemobile.samples.mediacast;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A new photo upload containing a small, medium and large photo data.
  */
 public class MediaMessage {
 
     private static final Logger logger =
             Logger.getLogger(MediaMessage.class.toString());
 
     public static final String MEDIA_TYPE_PHOTO = "photo";
     public static final String MEDIA_TYPE_VIDEO = "video";
     public static final String MEDIA_TYPE_AUDIO = "audio";
 
     private String comment;
 
     private Media[] photos = new Media[3];
     private File mediaFile;
     private String mediaType = MEDIA_TYPE_PHOTO;
 
     public Media getSmallPhoto() {
         return photos[0];
     }
 
     public void addSmallPhoto(Media photo) {
         photos[0] = photo;
     }
 
     public Media getMediumPhoto() {
         return photos[1];
     }
 
     public void addMediumPhoto(Media photo) {
         photos[1] = photo;
     }
 
     public void addPhoto(File cameraFile){
         mediaFile = cameraFile;
     }
 
     public Media getLargePhoto() {
         return photos[2];
     }
 
     public void addLargePhoto(Media photo) {
         photos[2] = photo;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public Media[] getPhotos() {
         return photos;
     }
 
     public void setPhotos(Media[] photos) {
         this.photos = photos;
         mediaType = MEDIA_TYPE_PHOTO;
     }
     
     public void addVideo(File videoFile)  {
         this.mediaFile = videoFile;
         this.mediaType = MEDIA_TYPE_VIDEO;
     }
     
     public void addAudio(File audioFile)  {
         this.mediaFile = audioFile;
         this.mediaType = MEDIA_TYPE_AUDIO;
     }
     
     public String getMediaType()  {
         return mediaType;
     }
 
     public boolean getShowAudio()  {
         return MEDIA_TYPE_AUDIO.equals(mediaType);
     }
 
     public boolean getShowVideo()  {
         return MEDIA_TYPE_VIDEO.equals(mediaType);
     }
 
     public boolean getShowPhoto()  {
         return MEDIA_TYPE_PHOTO.equals(mediaType);
     }
 
     public String getMediaURL()  {
         ExternalContext externalContext = FacesContext
                 .getCurrentInstance().getExternalContext();
         String rootPath = externalContext.getRealPath("/");
         String absolutePath = mediaFile.getAbsolutePath();
         String urlPath = absolutePath.substring(rootPath.length() - 1);
         return urlPath;
     }
 
     /**
      * Clean up file resoruces.
      */
     public void dispose(){
         if (mediaFile != null){
             boolean success = mediaFile.delete();
             if (!success && logger.isLoggable(Level.FINE)){
                 logger.fine("Could not dispose of media file" + mediaFile.getAbsolutePath());
             }
         }
        // try and clean up the data[], but only for real photos, we don't
        // want to delete the video and audio icons.
        if (getShowPhoto()){
            for (Media photo : photos){
                photo.dispose();
            }
         }
     }
 
 }
