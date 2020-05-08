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
 
 package org.icemobile.samples.mobileshowcase.view.examples.layout.tabset;
 
 import org.icemobile.samples.mobileshowcase.view.metadata.annotation.*;
 import org.icemobile.samples.mobileshowcase.view.metadata.context.ExampleImpl;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import java.io.Serializable;
 
 
 /**
  * Content stack bean stores the id of panels that can be selected.
  */
 @Destination(
         title = "example.layout.tabset.destination.title.short",
         titleExt = "example.layout.tabset.destination.title.long",
         titleBack = "example.layout.tabset.destination.title.back"
 )
 @Example(
         descriptionPath = "/WEB-INF/includes/examples/layout/tabset-desc.xhtml",
         examplePath = "/WEB-INF/includes/examples/layout/tabset-example.xhtml",
         resourcesPath = "/WEB-INF/includes/examples/example-resources.xhtml"
 )
 @ExampleResources(
         resources = {
                 // xhtml
                 @ExampleResource(type = ResourceType.xhtml,
                         title = "tabset-example.xhtml",
                         resource = "/WEB-INF/includes/examples/layout/tabset-example.xhtml"),
                 // Java Source
                 @ExampleResource(type = ResourceType.java,
                         title = "TabsetBean.java",
                         resource = "/WEB-INF/classes/org/icemobile/samples/mobileshowcase" +
                                 "/view/examples/layout/tabset/TabsetBean.java")
         }
 )
 @ManagedBean(name = TabsetBean.BEAN_NAME)
 @SessionScoped
 public class TabsetBean extends ExampleImpl<TabsetBean> implements
         Serializable {
 
     public static final String BEAN_NAME = "tabsetBean";
 
     private String currentId = "tab0";
     
    private boolean fixedPosition = true;
     private boolean orientationTop = false;
 
     public TabsetBean() {
         super(TabsetBean.class);
     }
 
     public String getCurrentId() {
         return currentId;
     }
 
     public void setCurrentId(String currentId) {
         this.currentId = currentId;
     }
 
     public boolean isFixedPosition() {
         return fixedPosition;
     }
 
     public void setFixedPosition(boolean fixedPosition) {
         this.fixedPosition = fixedPosition;
     }
 
     public boolean isOrientationTop() {
         return orientationTop;
     }
 
     public void setOrientationTop(boolean orientationTop) {
         this.orientationTop = orientationTop;
     }
 }
