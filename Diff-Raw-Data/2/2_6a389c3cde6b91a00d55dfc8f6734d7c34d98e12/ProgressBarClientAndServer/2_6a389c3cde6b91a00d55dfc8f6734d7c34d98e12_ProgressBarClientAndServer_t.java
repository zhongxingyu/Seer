  /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.samples.showcase.example.ace.progressbar; 
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import org.icefaces.ace.event.ProgressBarChangeEvent;
 
 
 @ComponentExample(
         parent = ProgressBarBean.BEAN_NAME,
         title = "example.ace.progressBarClientAndServer.title",
         description = "example.ace.progressBarClientAndServer.description",
         example = "/resources/examples/ace/progressbar/progressBarClientAndServer.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="progressBarClientAndServer.xhtml",
                     resource = "/resources/examples/ace/progressbar/progressBarClientAndServer.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="ProgressBarClientAndServer.java",
                    resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/progressbar/ProgressBarClientAndServer.java")
         }
 )
 @ManagedBean(name= ProgressBarClientAndServer.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class ProgressBarClientAndServer extends ComponentExampleImpl<ProgressBarClientAndServer> implements Serializable {
 
     public static final String BEAN_NAME = "progressBarClientAndServer";
     private int progressValue;
     private String message;
     /////////////---- CONSTRUCTOR BEGIN
     public ProgressBarClientAndServer() 
     {
         super(ProgressBarClientAndServer.class);
         progressValue = 0;
         message = "";
     }
 
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 
     /////////////---- EVENT LISTENERS BEGIN
     public void changeListener(ProgressBarChangeEvent event) 
     {
          message = (int)event.getPercentage() + "%";
     }
     /////////////---- GETTERS & SETTERS BEGIN
     public int getProgressValue() { return progressValue; }
     public void setProgressValue(int progressValue) { this.progressValue = progressValue; }
     public String getMessage() { return message; }
     public void setMessage(String message) { this.message = message; }
 }
