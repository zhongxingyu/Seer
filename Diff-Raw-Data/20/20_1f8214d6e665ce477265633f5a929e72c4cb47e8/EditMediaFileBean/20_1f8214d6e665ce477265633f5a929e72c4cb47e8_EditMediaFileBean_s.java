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
 import net.mymam.entity.MediaFileUserProvidedMetaData;
 import net.mymam.exceptions.NotFoundException;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
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
 
     @EJB
     private MediaFileEJB mediaFileEJB;
 
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
             this.metaData = mediaFileEJB.loadMetaData(mediaFile);
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
 
    // TODO: Don't return http://localhost:8080/mymam-server-0.1
     public String getLowResMp4Url() {
        return "http://localhost:8080/mymam-server-0.1/static/lowres/mp4/" + getVideoId();
     }
 
     public String getLowResWebmUrl() {
        return "http://localhost:8080/mymam-server-0.1/static/lowres/webm/" + getVideoId();
     }
 
     public Access getAccess() {
         return mediaFile.getAccess();
     }
 
     public void setAccess(Access access) {
         mediaFile.setAccess(access);
     }
 
     public void submit() throws NotFoundException, IOException {
         mediaFileEJB.updateStatusAndAccessAndMetaData(mediaFile, MediaFileImportStatus.READY, mediaFile.getAccess(), metaData);
         FacesContext.getCurrentInstance().getExternalContext().redirect("dashboard.xhtml");
     }
 }
