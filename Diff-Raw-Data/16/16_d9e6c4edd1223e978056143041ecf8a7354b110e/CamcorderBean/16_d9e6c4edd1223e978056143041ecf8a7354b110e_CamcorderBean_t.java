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
 
 package org.icemobile.samples.mobileshowcase.view.examples.device.camcorder;
 
 /**
  * The Camcorder Bean demonstrates how a mobile device's camera can be
  * used to record a video clip.  Device integration is only available
  * from an application running from within the the icefaces container.
  * <p/>
  * The example is quite simple, a camcorder button can be pressed  which
  * launches the native video recorder.  An upload button allows the file
  * to be sent to the server and once there video controls are presented to play
  * the video file.
  */
 
 import org.icemobile.samples.mobileshowcase.view.examples.device.DeviceInput;
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.*;
 import org.icemobile.samples.mobileshowcase.view.metadata.context.ExampleImpl;
 
 import javax.annotation.PreDestroy;
 import javax.faces.application.Resource;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @Destination(
         title = "example.device.camcorder.destination.title.short",
         titleExt = "example.device.camcorder.destination.title.long",
         titleBack = "example.device.camcorder.destination.title.back",
         contentPath = "/WEB-INF/includes/examples/device/camcorder.xhtml"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/device/camcorder-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/device/camcorder-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "camcorder-example.xhtml",
                         resource = "/WEB-INF/includes/examples/device/camcorder-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "CamcorderBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/device/camcorder/CamcorderBean.java")
         }
 )
 
 @ManagedBean(name = CamcorderBean.BEAN_NAME)
 @SessionScoped
 public class CamcorderBean extends ExampleImpl<CamcorderBean> implements
         Serializable {
 
     private static final Logger logger =
             Logger.getLogger(CamcorderBean.class.toString());
 
     public static final String BEAN_NAME = "camcorderBean";
 
     public static final String FILE_KEY = "file";
     public static final String CONTENT_TYPE_KEY = "contentType";
 
     private Map camcorderImage = new HashMap();
     private File camcorderFile;
     // uploaded video will be stored as a resource.
     private Resource outputResource;
 
     // upload error message
     private String uploadMessage;
 
     public CamcorderBean() {
         super(CamcorderBean.class);
     }
 
     /**
      * If the upload was successful  the video stream is copied into a
      * Resource object for playback.
      *
      * @param event jsf action event
      */
     public void processUploadedVideo(ActionEvent event) {
         if (camcorderImage != null &&
                 camcorderImage.get("contentType") != null &&
                 ((String)camcorderImage.get("contentType")).startsWith("video")) {
             // clean up previously upload file
             if (camcorderFile != null){
                 disposeResources();
             }
             camcorderFile = (File) camcorderImage.get(FILE_KEY);
             if (camcorderFile != null) {
                 // copy the bytes into the resource object.
                 try {
                     outputResource = DeviceInput.createResourceObject(
                             camcorderFile, UUID.randomUUID().toString(),
                             (String) camcorderImage.get(CONTENT_TYPE_KEY));
                 } catch (IOException ex) {
                     logger.warning("Error setting up video resource object");
                 }
                 uploadMessage = "Upload was successful";
                 return;
             }
         }else{
             // create error message for users.
            uploadMessage = "The uploaded video file could not be correctly processed.";
         }
         // a null/empty object is used in the page to hide the audio
         // component.
         outputResource = null;
     }
 
     @PreDestroy
     public void disposeResources(){
         boolean success = camcorderFile.delete();
         if (!success && logger.isLoggable(Level.FINE)){
             logger.fine("Could not dispose of media file" + camcorderFile.getAbsolutePath());
         }
     }
 
     public void setClip(Map videoInfo) {
         this.camcorderImage = videoInfo;
     }
 
     public Map getClip() {
         return camcorderImage;
     }
 
     public Resource getOutputResource() {
         return outputResource;
     }
 
     public String getUploadMessage() {
         return uploadMessage;
     }
 
     public void setUploadMessage(String uploadMessage) {
         this.uploadMessage = uploadMessage;
     }
 }
