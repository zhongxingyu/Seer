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
 
 package org.icefaces.samples.showcase.example.ace.file;
 
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 
 @ComponentExample(
         parent = FileEntryBean.BEAN_NAME,
         title = "menu.ace.fileentry.subMenu.validation",
         description = "example.ace.fileentry.valid.description",
         example = "/resources/examples/ace/fileentry/validation.xhtml"
 )
 @ExampleResources(
 resources ={
     // xhtml
     @ExampleResource(type = ResourceType.xhtml,
             title="validation.xhtml",
             resource = "/resources/examples/ace/"+
                        "fileentry/validation.xhtml"),
     // Java Source
     @ExampleResource(type = ResourceType.java,
             title="FileEntryValidationOptionsBean.java",
             resource = "/WEB-INF/classes/org/icefaces/samples/"+
                        "showcase/example/ace/file/FileEntryValidationOptionsBean.java")
 }
 )
 @ManagedBean(name= FileEntryValidationOptionsBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class FileEntryValidationOptionsBean extends ComponentExampleImpl<FileEntryValidationOptionsBean>
         implements Serializable {
 
     public static final String BEAN_NAME = "fileEntryValidation";
 
     private boolean maxFileSizeSet = false;
     private Integer maxFileSize = 0;
 
     public Integer getMaxFileSize() {
         return maxFileSize;
     }
 
     public void setMaxFileSize(Integer maxFileSize) {
         this.maxFileSize = maxFileSize;
     }
 
     public boolean isMaxFileSizeSet() {
         return maxFileSizeSet;
     }
 
     public void setMaxFileSizeSet(boolean maxFileSizeSet) {
         this.maxFileSizeSet = maxFileSizeSet;
     }
 
     public String getMaxFileSizeString() {
         return maxFileSize + " (bytes)";
     }
 
     public String getMaxFileSizeMessage() {
        return "File size cannot exceed "+(maxFileSize/102400)*100+" KB";
     }
 
     public FileEntryValidationOptionsBean() {
         super(FileEntryValidationOptionsBean.class);
     }
 }
