 /*
  * Copyright (C) 2003-2012 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.portal.webui.image.crop;
 
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.exoplatform.commons.utils.MimeTypeResolver;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.upload.UploadResource;
 import org.exoplatform.upload.UploadService;
 import org.exoplatform.web.application.ApplicationMessage;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.UIApplication;
 import org.exoplatform.webui.core.UIPopupWindow;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.UIFormUploadInput;
 
 /**
  * Created by The eXo Platform SAS
  * Author : An Bao NGUYEN
  *          annb@exoplatform.com
  * Oct 31, 2012  
  */
 @ComponentConfigs ({
   @ComponentConfig(
     lifecycle = UIFormLifecycle.class,
     template = "app:groovy/imagecrop/portlet/UIImageCroppingUploader.gtmpl",
     events = {
       @EventConfig(listeners = UIImageCroppingUploader.ConfirmActionListener.class),
       @EventConfig(listeners = UIImageCroppingUploader.CancelActionListener.class)
     }
   )
 })
 public class UIImageCroppingUploader extends UIForm{
   
   /** Message alert that image is not uploaded successfully. */
   private static final String MSG_IMG_NOT_UPLOADED = "UIAvatarUploader.msg.img_not_loaded";
 
   /** Message alert that mimetype is not accepted. */
   private static final String MSG_MIMETYPE_NOT_ACCEPTED = "UIAvatarUploader.msg.mimetype_not_accepted";
 /*  *//** Message alert that the file name is too long *//*
   static private final String MSG_CHARACTERS_TOO_LONG = "UIAvatarUploader.msg.characters_too_long";
   *//** The number of characters allowed to rename *//*
   static private final int ALLOWED_CHARACTERS_LONG = 50;*/
   /** FIELD NAME. */
   private static final String FIELD_NAME = "Name";
 
   /** FIELD Uploader. */
   private static final String FIELD_UPLOADER = "Uploader";
 
   /** The limit size for upload image. */
   private static final int uploadLimit = 6; //MB
 
   /** List of accepted mimetype. */
   private static final String[] ACCEPTED_MIME_TYPES = new String[] {"image/jpeg", "image/jpg", "image/png",
           "image/x-png", "image/pjpeg"};
 
   /** Contains pair of IE Mimetype and Standard Mimetype. */
   private final Map<String, String> IE_MIME_TYPES = new HashMap<String, String>() {
     {
       put("image/pjpeg", "image/jpeg");
       put("image/x-png", "image/png");
     }
   };
   
   /** Stores UIFormUploadInput instance. */
   private final UIFormUploadInput uiAvatarUploadInput;
 
   /**
    * Initializes upload form.<br>\
    *
    */
   public UIImageCroppingUploader() throws Exception {
     uiAvatarUploadInput = new UIFormUploadInput(FIELD_UPLOADER, null, uploadLimit);
     uiAvatarUploadInput.setAutoUpload(true);
     addUIFormInput(uiAvatarUploadInput);
     setActions(new String[]{"Confirm", "Cancel"});
   }
 
   /**
    * Checks if the provided mimeType matches acceptedMimeTypes.<br>
    *
    * @param mimeType String
    *
    * @return boolean
    */
   private boolean isAcceptedMimeType(String mimeType) {
     for (String acceptedMimeType : ACCEPTED_MIME_TYPES) {
       if (mimeType.equals(acceptedMimeType)) return true;
     }
     return false;
   }
 
   /**
    * Gets standard mimetype from IE mimetype appropriately.
    * 
    * @param mimeType
    * @return standard mimetype.
    * @since 1.1.3
    */
   private String getStandardMimeType(String mimeType) {
     return IE_MIME_TYPES.get(mimeType);  
   }
   
   /**
    * Changes and displays avatar on the profile if upload successful, else
    * inform user to upload image.
    */
   public static class ConfirmActionListener extends EventListener<UIImageCroppingUploader> {
     // The width of resized avatar fix 200px like facebook avatar
     private static final int WIDTH = 240;
 
     @Override
     public void execute(Event<UIImageCroppingUploader> event) throws Exception {
       WebuiRequestContext ctx = event.getRequestContext();
       UIApplication uiApplication = ctx.getUIApplication();
       UIImageCroppingUploader uiAvatarUploader = event.getSource();
       UIFormUploadInput uiAvatarUploadInput = uiAvatarUploader.getChild(UIFormUploadInput.class);
       UIPopupWindow uiPopup = uiAvatarUploader.getParent();
       UIImageCroppingUploadContent uiAvatarUploadContent = uiAvatarUploader.createUIComponent(UIImageCroppingUploadContent.class,
                                                                                        null,
                                                                                        null);
       
       InputStream uploadedStream = uiAvatarUploadInput.getUploadDataAsStream();
 
       if (uploadedStream == null) {
         uiApplication.addMessage(new ApplicationMessage(MSG_IMG_NOT_UPLOADED,
                                                         null,
                                                         ApplicationMessage.ERROR));
         ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
         return;
       }
       
       uiAvatarUploadContent.setOriginImg(uiAvatarUploadInput.getUploadDataAsStream());
       
       UploadResource uploadResource = uiAvatarUploadInput.getUploadResource();
 
       String mimeType = uploadResource.getMimeType();
       String uploadId = uiAvatarUploadInput.getUploadId();
       if (!uiAvatarUploader.isAcceptedMimeType(mimeType)) {
         UploadService uploadService = (UploadService) PortalContainer.getComponent(UploadService.class);
         uploadService.removeUploadResource(uploadId);
         uiApplication.addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED,
                                                         null,
                                                         ApplicationMessage.ERROR));
         ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
       } else {
         MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
         String fileName = uploadResource.getFileName();
         
         // @since 1.1.3
         String extension = mimeTypeResolver.getExtension(mimeType);
         if ("".equals(extension)) {
           mimeType = uiAvatarUploader.getStandardMimeType(mimeType);
         }
         
         // Resize avatar to fixed width if can't(avatarAttachment == null) keep
 //         origin avatar
         ImageCroppingAttachment avatarAttachment =null;
 //        AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(uploadedStream,
 //                                                                                    0,
 //                                                                                    WIDTH,
 //                                                                                    null,
 //                                                                                    fileName,
 //                                                                                    mimeType,
 //                                                                                    null);
  
         
         if (avatarAttachment == null) {
           avatarAttachment = new ImageCroppingAttachment(null,
                                                   fileName,
                                                   mimeType,
                                                   uploadedStream,
                                                   null,
                                                   System.currentTimeMillis());
         }
 
         UploadService uploadService = (UploadService) PortalContainer.getComponent(UploadService.class);
         uploadService.removeUploadResource(uploadId);
 
         uiAvatarUploadContent.setAvatarAttachment(avatarAttachment);
         uiPopup.setUIComponent(uiAvatarUploadContent);
         ctx.addUIComponentToUpdateByAjax(uiPopup);
       }
     }
   }
 
   /**
    * Cancels the upload image.<br>
    *
    */
   public static class CancelActionListener extends EventListener<UIImageCroppingUploader> {
 
     @Override
     public void execute(Event<UIImageCroppingUploader> event) throws Exception {
       UIImageCroppingUploader uiAvatarUploader = event.getSource();
       UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      uiPopup.setUIComponent(null);
       uiPopup.setShow(false);
     }
 
   }  
 
 }
