 /* MyMAM - Open Source Digital Media Asset Management.
  * http://www.mymam.net
  *
  * Copyright 2013, MyMAM contributors as indicated by the @author tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.mymam.controller;
 
 import net.mymam.data.json.MediaFileImportStatus;
 import net.mymam.ejb.MediaFileEJB;
 import net.mymam.entity.Access;
 import net.mymam.entity.MediaFile;
 import net.mymam.entity.MediaFileThumbnailData;
 import net.mymam.entity.MediaFileUserProvidedMetaData;
 import net.mymam.exceptions.NotFoundException;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
 import javax.servlet.ServletContext;
 import java.io.IOException;
 import java.util.Date;
 
 /**
  * @author fstab
  */
 @ManagedBean
 @ViewScoped
 public class EditMediaFileBean {
 
     private Long videoId;
     private MediaFile mediaFile;
     private MediaFileUserProvidedMetaData metaData;
     private String fileServletPath;
     private Long thumbnailOffsetMs;
 
     @EJB
     private MediaFileEJB mediaFileEJB;
 
     @PostConstruct
     public void postConstruct() {
         ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
         this.fileServletPath = servletContext.getContextPath() + "/static";
     }
 
     public Long getVideoId() {
         return videoId;
     }
 
     public void setVideoId(Long videoId) {
         this.videoId = videoId;
     }
 
     // listener for the pre-render-view event
     public void loadFile(ComponentSystemEvent event) {
         // TODO: The post-back check will become unnecessary in JSF 2.2,
         // when the <f:viewAction action="#{editMediaFileBean.loadFile}" onPostback="false" />
         // is introduced.
         if (!FacesContext.getCurrentInstance().isPostback()) {
             this.mediaFile = mediaFileEJB.findById(videoId);
            if ( mediaFile.getUserProvidedMetadata() != null ) {
                this.metaData = mediaFile.getUserProvidedMetadata();
            } else {
                this.metaData = new MediaFileUserProvidedMetaData();
            }
             this.thumbnailOffsetMs = mediaFile.getThumbnailData().getThumbnailOffsetMs();
         }
     }
 
     public Date getCreationDate() {
         return mediaFile.getCreationDate();
     }
 
     public String getOrigFile() {
         return mediaFile.getOrigFile();
     }
 
     public MediaFileUserProvidedMetaData getMetaData() {
         return metaData;
     }
 
     public String getLowResMp4Url() {
         return fileServletPath + "/lowres/mp4/" + getVideoId();
     }
 
     public String getLowResWebmUrl() {
         return fileServletPath + "/lowres/webm/" + getVideoId();
     }
 
     public Access getAccess() {
         return mediaFile.getAccess();
     }
 
     public void setAccess(Access access) {
         mediaFile.setAccess(access);
     }
 
     public Long getThumbnailOffsetMs() {
         return thumbnailOffsetMs;
     }
 
     public void setThumbnailOffsetMs(Long thumbnailOffsetMs) {
         this.thumbnailOffsetMs = thumbnailOffsetMs;
     }
 
     public void submit() throws NotFoundException, IOException {
         mediaFileEJB.updateAccessAndMetaData(mediaFile, mediaFile.getAccess(), metaData);
         if ( ! mediaFile.getThumbnailData().getThumbnailOffsetMs().equals(thumbnailOffsetMs) ) {
             mediaFileEJB.scheduleGenerateThumbnailsTask(mediaFile.getId(), thumbnailOffsetMs);
         }
         FacesContext.getCurrentInstance().getExternalContext().redirect("dashboard.xhtml");
     }
 }
