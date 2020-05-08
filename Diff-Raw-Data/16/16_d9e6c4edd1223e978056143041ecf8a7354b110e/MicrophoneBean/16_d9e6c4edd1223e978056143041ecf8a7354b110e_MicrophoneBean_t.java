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
 
 package org.icemobile.samples.mobileshowcase.view.examples.device.microphone;
 
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
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The Microphone Bean demonstrates how a mobile device's microphone can be
  * used to record and sound clip.  Device integration is only available
  * from an application running from within the the icefaces container.
  * <p/>
  * The example is quite simple, a microphone button can be pressed and held to
  * start a recording and release to end it.  An upload button allows the file
  * to be sent to the server and once there audio controls are presented to play
  * the sound bite.
  */
 @Destination(
         title = "example.device.microphone.destination.title.short",
         titleExt = "example.device.microphone.destination.title.long",
         titleBack = "example.device.microphone.destination.title.back",
         contentPath = "/WEB-INF/includes/examples/device/microphone.xhtml"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/device/microphone-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/device/microphone-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "microphone-example.xhtml",
                         resource = "/WEB-INF/includes/examples/device/microphone-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "MicrophoneBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/device/microphone/MicrophoneBean.java")
         }
 )
 @ManagedBean(name = MicrophoneBean.BEAN_NAME)
 @SessionScoped
 public class MicrophoneBean extends ExampleImpl<MicrophoneBean> implements
         Serializable {
 
     private static final Logger logger =
             Logger.getLogger(MicrophoneBean.class.toString());
 
     public static final String BEAN_NAME = "microphoneBean";
 
     public static final String FILE_KEY = "file";
     public static final String CONTENT_TYPE_KEY = "contentType";
 
     // uploaded video will be stored as a resource.
     private Resource outputResource;
 
     // uploaded audio file.
     private Map audioFileMap;
     private File audioFile;
 
     // upload error message
     private String uploadMessage;
 
     public MicrophoneBean() {
         super(MicrophoneBean.class);
     }
 
     public void processUploadedAudio(ActionEvent event) {
         if (audioFileMap != null &&
                 audioFileMap.get("contentType") != null &&
                 ((String)audioFileMap.get("contentType")).startsWith("audio")) {
             // clean up previously upload file
             if (audioFile != null){
                 disposeResources();
             }
             audioFile = (File) audioFileMap.get(FILE_KEY);
             if (audioFile != null) {
                 // copy the bytes into the resource object.
                 try {
                     outputResource = DeviceInput.createResourceObject(
                             audioFile, UUID.randomUUID().toString(),
                             (String) audioFileMap.get(CONTENT_TYPE_KEY));
                 } catch (IOException ex) {
                     logger.warning("Error setting up video resource object");
                 }
                 uploadMessage = "Upload was successful";
                 return;
             }
         }else{
             // create error message for users.
            uploadMessage = "The uploaded audio file could not be correctly processed.";
         }
         // a null/empty object is used in the page to hide the audio
         // component.
         outputResource = null;
     }
 
     @PreDestroy
     public void disposeResources(){
         boolean success = audioFile.delete();
         if (!success && logger.isLoggable(Level.FINE)){
             logger.fine("Could not dispose of media file" + audioFile.getAbsolutePath());
         }
     }
 
     public Map getAudioFileMap() {
         return audioFileMap;
     }
 
     public void setAudioFileMap(Map audioFileMap) {
         this.audioFileMap = audioFileMap;
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
