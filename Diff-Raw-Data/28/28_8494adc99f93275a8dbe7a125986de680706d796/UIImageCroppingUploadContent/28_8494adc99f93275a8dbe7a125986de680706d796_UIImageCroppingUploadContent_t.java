 /*
  * Copyright (C) 2003-2009 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.portal.webui.image.crop;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.exoplatform.commons.utils.MimeTypeResolver;
 import org.exoplatform.download.DownloadService;
 import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.UIComponent;
 import org.exoplatform.webui.core.UIPopupWindow;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.UIFormStringInput;
 
 /**
  * Created by The eXo Platform SAS
  * Author : An Bao NGUYEN
  *          annb@exoplatform.com
  * Oct 31, 2012  
  */
 @ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:groovy/imagecrop/portlet/UIImageCroppingUploadContent.gtmpl",
   events = {
     @EventConfig(listeners = UIImageCroppingUploadContent.CropActionListener.class),
     @EventConfig(listeners = UIImageCroppingUploadContent.SaveActionListener.class, phase = Phase.PROCESS),
     @EventConfig(listeners = UIImageCroppingUploadContent.CancelActionListener.class)
   }
 )
 public class UIImageCroppingUploadContent extends UIForm {
 
   private static final String CROPPED_INFO = "croppedInfo";
   private static final String X = "X";
   private static final String Y = "Y";
   private static final String WIDTH = "WIDTH";
   private static final String HEIGHT = "HEIGHT";
   
   /** AvatarAttachment instance. */
   private ImageCroppingAttachment avatarAttachment;
 
   /** Stores information of image storage. */
   private String imageSource;
 
   /** */
   private InputStream originImg;
   
   /** */
   private byte[] resizedImgInBytes;
   
   private Map<String, String> croppedInfo;
   
   /**
    * Default constructor.<br>
    *
    */
   public UIImageCroppingUploadContent() {
     UIFormStringInput input = new UIFormStringInput(CROPPED_INFO, CROPPED_INFO, "0");
     input.setId(CROPPED_INFO);
     addUIFormInput(input);
   }
 
   /**
    * Initializes object at the first run time.<br>
    *
    * @param ImageCroppingAttachment
    *        Information about attachment.
    *
    * @throws Exception
    */
   public UIImageCroppingUploadContent(ImageCroppingAttachment avatarAttachment) throws Exception {
     this.avatarAttachment = avatarAttachment;
     setImageSource(avatarAttachment.getImageBytes());
   }
 
 
   /**
    * Gets information of AvatarAttachment.<br>
    *
    * @return AvatarAttachment
    */
   public ImageCroppingAttachment getAvatarAttachment() {
     return avatarAttachment;
   }
 
   /**
    * Sets information of AvatarAttachment.<br>
    *
    * @param ImageCroppingAttachment
    *
    * @throws Exception
    */
   public void setAvatarAttachment(ImageCroppingAttachment avatarAttachment) throws Exception {
     this.avatarAttachment = avatarAttachment;
     setImageSource(avatarAttachment.getImageBytes());
   }
  
  
 @Override
 public void processRender(WebuiRequestContext context) throws Exception {
   super.processRender(context);
}  
  
 
   /**
    * Gets the source of image.
    *
    * @return imageSource link
    */
   public String getImageSource() {
     return imageSource;
   }
 
   /**
    * 
    * @return
    */
   public InputStream getOriginImg() {
     return originImg;
   }
 
   /**
    * 
    * @param originImg
    */
   public void setOriginImg(InputStream originImg) {
     this.originImg = originImg;
   }
 
 
   /*
    * 
    */
   public Map<String, String> getCroppedInfo() {
     return croppedInfo;
   }
 
   /**
    * 
    * @param croppedInfo
    */
   public void setCroppedInfo(Map<String, String> croppedInfo) {
     this.croppedInfo = croppedInfo;
   }
 
   /**
    * 
    * @return
    */
   public byte[] getResizedImgInBytes() {
     return resizedImgInBytes;
   }
 
   /**
    * 
    * @param resizedImgInBytes
    */
   public void setResizedImgInBytes(byte[] resizedImgInBytes) {
     this.resizedImgInBytes = resizedImgInBytes;
   }
 
   /**
    * Crop image, make preview image.
    *
    */
   public static class CropActionListener extends EventListener<UIImageCroppingUploadContent> {
     @Override
     public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
       UIImageCroppingUploadContent uiAvatarUploadContent = event.getSource();
       ImageCroppingAttachment att = uiAvatarUploadContent.avatarAttachment;
       
       String croppedInfoVal =  ((UIFormStringInput)uiAvatarUploadContent.getChildById(CROPPED_INFO)).getValue();
       Map<String, String> croppedInfos = getCroppedInfoValues(croppedInfoVal);
 
       // set cropping information
       uiAvatarUploadContent.setCroppedInfo(croppedInfos);
 
       // get cropped information
       int x = (int)Double.parseDouble(croppedInfos.get(X));
       int y = (int)Double.parseDouble(croppedInfos.get(Y));
       int w = (int)Double.parseDouble(croppedInfos.get(WIDTH));
       int h = (int)Double.parseDouble(croppedInfos.get(HEIGHT));
       
       InputStream in = new ByteArrayInputStream(att.getImageBytes());
       
       //
       uiAvatarUploadContent.setResizedImgInBytes(att.getImageBytes());
       
       BufferedImage image = ImageIO.read(in);
       
       //
       image = image.getSubimage(x, y, w, h);
       
       
       // create and re-store attachment info
       MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
       String extension = mimeTypeResolver.getExtension(att.getMimeType());
       
       ByteArrayOutputStream os = new ByteArrayOutputStream();
       ImageIO.write(image, extension, os);
       os.flush();
       byte[] imageInByte = os.toByteArray();
       os.close();
       att.setImageBytes(imageInByte);
       InputStream input = new ByteArrayInputStream(imageInByte);
 
       att.setInputStream(input);
       
       uiAvatarUploadContent.setAvatarAttachment(att);
       
       event.getRequestContext().addUIComponentToUpdateByAjax(uiAvatarUploadContent);
     }
 
     private Map<String, String> getCroppedInfoValues(String input) {
       Map<String, String> croppedInfo = new HashMap<String, String>();
       String[] value = input.split(",");
       
       for (String val : value) {
         String[] info = val.split(":");
         croppedInfo.put(info[0], info[1]);
       }
       
       return croppedInfo;
     }
   }
   
   /**
    * Accepts and saves the uploaded image to profile.
    * Closes the popup window, refreshes UIProfile.
    *
    */
   public static class SaveActionListener extends EventListener<UIImageCroppingUploadContent> {
     @Override
     public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
       UIImageCroppingUploadContent uiAvatarUploadContent = event.getSource();
       
       // crop image
       uiAvatarUploadContent.crop();
       
       saveAvatar(uiAvatarUploadContent);
       UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
       uiPopup.setShow(false);
       //Utils.updateWorkingWorkSpace();
     }
 
     private void saveAvatar(UIImageCroppingUploadContent uiAvatarUploadContent) throws Exception {
       UIComponent parent =uiAvatarUploadContent.getParent();
       while (parent != null) {
          /*if (UISpaceInfo.class.isInstance(parent)) {
            UISpaceInfo uiSpaceInfo = ((UISpaceInfo)parent);
            SpaceService spaceService = uiSpaceInfo.getSpaceService();
            String id = uiSpaceInfo.getUIStringInput("id").getValue();
            Space space = spaceService.getSpaceById(id);
            if (space != null) {
              uiSpaceInfo.saveAvatar(uiAvatarUploadContent, space);
              return;
            }
          }*/
          parent = parent.getParent();
       }
       
       // Save user avatar
       uiAvatarUploadContent.saveUserAvatar(uiAvatarUploadContent);
       return;
     }
   }
 
   /**
    * Saves avatar of users.
    * 
    * @param uiAvatarUploadContent
    * @throws Exception
    * @since 1.2.2
    */
   public void saveUserAvatar(UIImageCroppingUploadContent uiAvatarUploadContent) throws Exception {
     ImageCroppingAttachment attacthment = uiAvatarUploadContent.getAvatarAttachment();
     
     /*Profile p = Utils.getOwnerIdentity().getProfile();
     p.setProperty(Profile.AVATAR, attacthment);
     Map<String, Object> props = p.getProperties();
 
     // Removes avatar url and resized avatar
     for (String key : props.keySet()) {
       if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
         p.removeProperty(key);
       }
     }
 
     Utils.getIdentityManager().updateProfile(p);*/
   }
   
   /**
    * Cancels, close the popup window.
    *
    */
   public static class CancelActionListener extends EventListener<UIImageCroppingUploadContent> {
     @Override
     public void execute(Event<UIImageCroppingUploadContent> event) throws Exception {
       UIImageCroppingUploadContent uiAvatarUploadContent = event.getSource();
       UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
       uiPopup.setShow(false);
     }
 
   }
 
   /**
    * Sets information of image storage.<br>
    *
    * @param imageBytes
    *        Image information in byte type for storing.
    * @throws Exception
    */
   private void setImageSource(byte[] imageBytes) throws Exception {
     if (imageBytes == null || imageBytes.length == 0) return;
     ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
     DownloadService downloadService = getApplicationComponent(DownloadService.class);
     InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
     downloadResource.setDownloadName(avatarAttachment.getFileName());
     imageSource = downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource));
   }
   
   private void crop() throws Exception {
     
    
     int x_co = 0;
     int y_co = 0;
     int width_co = 0; 
     int height_co = 0;
     
     BufferedImage originImg = ImageIO.read(getOriginImg());
     
     //
     if (getResizedImgInBytes() == null) {
       int imgWidth = originImg.getWidth();
       int imgHeight = originImg.getHeight();
       
       if (imgWidth != imgHeight) { // in case of image has width and height not the same
         int cropDimension = imgWidth > imgHeight ? imgHeight : imgWidth;
         int offset = (cropDimension == imgWidth) ? imgHeight - cropDimension : imgWidth - cropDimension;
         
         if (imgWidth < imgHeight) {
           x_co = 0;
           y_co = (int)(offset/2);
           width_co = imgWidth;
           height_co = imgHeight - offset;
         } else {
           x_co = (int)(offset/2);
           y_co = 0;
           width_co = imgWidth - offset;
           height_co = imgHeight;
         }
       }
     } else {
       BufferedImage resizedImg = ImageIO.read(new ByteArrayInputStream(getResizedImgInBytes()));
   
       // Original image information
       int w_o = originImg.getWidth();
       int h_o = originImg.getHeight();
       
       // Resized image information
       int w_r = resizedImg.getWidth();
       int h_r = resizedImg.getHeight();
       
       // cropped image information on resized image
       int x_cr = (int)Double.parseDouble(getCroppedInfo().get(X));
       int y_cr = (int)Double.parseDouble(getCroppedInfo().get(Y));
       int width_cr = (int)Double.parseDouble(getCroppedInfo().get(WIDTH));
       int height_cr = (int)Double.parseDouble(getCroppedInfo().get(HEIGHT));
       
       // calculate the scale
       double scale_w = (double)w_o/(double)w_r;
       double scale_h = (double)h_o/(double)h_r;
       
       double scale_OR = scale_w > scale_h ? scale_h : scale_w;
       
       // cropped image information on original image
       x_co = (int)(x_cr*scale_w);
       y_co = (int)(y_cr*scale_h);
       width_co = (int) (width_cr*scale_OR); 
       height_co = (int) (height_cr*scale_OR);
     }
     
 
     // sub image with new information
     BufferedImage croppedImg = originImg.getSubimage(x_co, y_co, width_co, height_co);
     
     ImageCroppingAttachment att = getAvatarAttachment();
     MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
     String extension = mimeTypeResolver.getExtension(att.getMimeType());
     
     ByteArrayOutputStream os = new ByteArrayOutputStream();
     ImageIO.write(croppedImg, extension, os);
     os.flush();
     byte[] imageInByte = os.toByteArray();
     os.close();
     att.setImageBytes(imageInByte);
     InputStream input = new ByteArrayInputStream(imageInByte);
 
     att.setInputStream(input);
     
     setAvatarAttachment(att);
   }
 }
