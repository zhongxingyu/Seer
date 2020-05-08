 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.samples.showcase.example.ace.resizable;
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import org.icefaces.ace.event.ResizeEvent;
 import org.icefaces.samples.showcase.dataGenerators.ImageSet;
 
 @ComponentExample(
         parent = ResizableBean.BEAN_NAME,
         title = "example.ace.resizableListener.title",
         description = "example.ace.resizableListener.description",
         example = "/resources/examples/ace/resizable/resizeListener.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="resizeListener.xhtml",
                     resource = "/resources/examples/ace/resizable/resizeListener.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="ResizeListenerBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                     "/example/ace/resizable/ResizeListenerBean.java"),
             
             @ExampleResource(type = ResourceType.java,
                     title="ImageSet.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase"+
                    "/dataGenerators/ImageSet.java")
         }
 )
 @ManagedBean(name= ResizeListenerBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class ResizeListenerBean extends ComponentExampleImpl<ResizeListenerBean> implements Serializable
 {
     public static final String BEAN_NAME = "resizeListenerBean";
     private String resizeParameters;
     private String imageLocation;
     private boolean showImage;
     private final int MIN_WIDTH_TO_DISPLAY_IMAGE = 255;
     private final int MIN_HEIGHT_TO_DISPLAY_IMAGE = 235;
     /////////////---- CONSTRUCTOR BEGIN
     public ResizeListenerBean() 
     {
         super(ResizeListenerBean.class);
         resizeParameters = "Resize panel to call its listener !";
         showImage = false;
         imageLocation = ImageSet.getImage(ImageSet.ImageSelect.PICTURE).getPath();
     }
     
     /////////////---- EVENT LISTENERS BEGIN
     public void handleResizeEvent(ResizeEvent event)
     {
         showImage = customLogic(event.getWidth(), event.getHeight());
         if(showImage)
             resizeParameters = "My size changed to: "+event.getWidth() +" x "+ event.getHeight()+" and I have enough space to fit an image";
         else
             resizeParameters = "My size changed to: "+event.getWidth() +" x "+ event.getHeight();
     }
     /////////////---- PRIVATE METHODS BEGIN
     private boolean customLogic(int width, int height)
     {
         if(width>=MIN_WIDTH_TO_DISPLAY_IMAGE && height >=MIN_HEIGHT_TO_DISPLAY_IMAGE) return true;
         else return false;
     }
     /////////////---- GETTERS & SETTERS BEGIN
     public String getResizeParameters() { return resizeParameters; }
     public void setResizeParameters(String resizeParameters) { this.resizeParameters = resizeParameters; }
     public boolean isShowImage() { return showImage; }
     public void setShowImage(boolean showImage) { this.showImage = showImage; }
     public String getImageLocation() { return imageLocation; }
     public void setImageLocation(String imageLocation) { this.imageLocation = imageLocation; }
 }
