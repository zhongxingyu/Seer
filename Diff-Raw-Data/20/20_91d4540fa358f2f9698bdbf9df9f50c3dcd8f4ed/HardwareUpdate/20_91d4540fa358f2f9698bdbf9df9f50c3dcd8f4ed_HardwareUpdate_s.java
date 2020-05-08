 package com.ngdb.web.pages.article.hardware;
 
 import com.ngdb.entities.ActionLogger;
 import com.ngdb.entities.article.Hardware;
 import com.ngdb.entities.article.element.Picture;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.model.OriginList;
 import com.ngdb.web.model.PlatformList;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 import com.ngdb.web.services.infrastructure.PictureService;
 import org.apache.shiro.authz.annotation.RequiresAuthentication;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.beaneditor.Validate;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.got5.tapestry5.jquery.JQueryEventConstants;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 @RequiresAuthentication
 public class HardwareUpdate {
 
     @Persist
     @Property
     private Hardware hardware;
 
     @Property
     @Validate("required,maxLength=255")
     private String title;
 
     @Property
     private UploadedFile mainPicture;
 
     @Inject
     private Session session;
 
     @Property
     private String details;
 
     @Property
     private String ean;
 
     @Property
     private String reference;
 
     @Property
     private Origin origin;
 
     @Property
     @Validate("required")
     private Date releaseDate;
 
     @Property
     @Validate("required")
     private Platform platform;
 
     @InjectPage
     private HardwareView hardwareView;
 
     @Inject
     private PictureService pictureService;
 
     @Inject
     private ReferenceService referenceService;
 
     @Inject
     private CurrentUser currentUser;
 
     @Persist
     @Property
     private List<UploadedFile> pictures;
 
     @Property
     private Set<Picture> storedPictures;
 
     @Property
     private Picture picture;
 
     @Inject
     private ActionLogger actionLogger;
 
     public void onActivate(Hardware hardware) {
         this.hardware = hardware;
         if (pictures == null) {
             pictures = new ArrayList<UploadedFile>();
         }
     }
 
     @SetupRender
     public void setup() {
         if (hardware == null) {
             this.details = null;
             this.origin = null;
             this.releaseDate = new DateTime().withYear(1990).toDate();
             this.details = null;
             this.title = null;
             this.platform = null;
             this.ean = null;
             this.reference = reference;
         } else {
             this.details = hardware.getDetails();
             this.origin = referenceService.findOriginByTitle(hardware.getOriginTitle());
             this.releaseDate = hardware.getReleaseDate();
             this.details = hardware.getDetails();
             this.title = hardware.getTitle();
             this.platform = referenceService.findPlatformByName(hardware.getPlatformShortName());
             this.ean = hardware.getUpc();
             this.reference = hardware.getReference();
             this.storedPictures = hardware.getPictures().all();
         }
     }
 
     @OnEvent(component = "uploadImage", value = JQueryEventConstants.AJAX_UPLOAD)
     void onImageUpload(UploadedFile uploadedFile) {
         if (uploadedFile != null) {
             this.pictures.add(uploadedFile);
         }
     }
 
     @CommitAfter
     @DiscardAfter
     Object onSuccess() {
         Hardware hardware = new Hardware();
         if (isEditMode()) {
             hardware = this.hardware;
             hardware.updateModificationDate();
         }
         hardware.setDetails(details);
         hardware.setOrigin(origin);
         hardware.setReleaseDate(releaseDate);
         hardware.setTitle(title);
         hardware.setPlatform(platform);
         hardware.setUpc(ean);
         hardware.setReference(reference);
         hardware = (Hardware) session.merge(hardware);
         if (this.mainPicture != null) {
             Picture picture = pictureService.store(mainPicture, hardware);
            currentUser.addPictureOn(hardware);
             hardware.setCover(picture);
             if (isEditMode()) {
                 session.merge(picture);
             }
         }
         if (pictures != null) {
             for (UploadedFile uploadedPicture : pictures) {
                 Picture picture = pictureService.store(uploadedPicture, hardware);
                currentUser.addPictureOn(hardware);
                 hardware.addPicture(picture);
                 if (isEditMode()) {
                     session.merge(picture);
                 }
             }
         }
         hardwareView.setHardware(hardware);
         User user = currentUser.getUser();
         actionLogger.addEditAction(user, hardware);
         return hardwareView;
     }
 
     @CommitAfter
     Object onActionFromDeletePicture(Picture picture) {
         hardware.removePicture(picture);
         pictureService.delete(picture);
         this.storedPictures = hardware.getPictures().all();
         return this;
     }
 
     public String getSmallPictureUrl() {
         return picture.getUrl("small");
     }
 
     public boolean isEditMode() {
         return this.hardware != null;
     }
 
     public SelectModel getOrigins() {
         return new OriginList(referenceService.getOrigins());
     }
 
     public SelectModel getPlatforms() {
         return new PlatformList(referenceService.getPlatforms());
     }
 
 }
