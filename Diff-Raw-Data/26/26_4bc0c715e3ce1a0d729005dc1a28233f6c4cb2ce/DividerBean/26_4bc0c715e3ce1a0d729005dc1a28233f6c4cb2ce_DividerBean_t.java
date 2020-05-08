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
 
 package org.icefaces.samples.showcase.example.compat.divider;
 
 import java.io.Serializable;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.Menu;
 import org.icefaces.samples.showcase.metadata.annotation.MenuLink;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 @ComponentExample(
         title = "example.compat.divider.title",
         description = "example.compat.divider.description",
         example = "/resources/examples/compat/divider/divider.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="divider.xhtml",
                     resource = "/resources/examples/compat/"+
                                "divider/divider.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="DividerBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/"+
                                "showcase/example/compat/divider/DividerBean.java")
         }
 )
 @Menu(
 	title = "menu.compat.divider.subMenu.title", menuLinks = {
             @MenuLink(title = "menu.compat.divider.subMenu.main", isDefault = true, exampleBeanName = DividerBean.BEAN_NAME),
             @MenuLink(title = "menu.compat.divider.subMenu.orientation", exampleBeanName = DividerOrientation.BEAN_NAME),
             @MenuLink(title = "menu.compat.divider.subMenu.position", exampleBeanName = DividerPosition.BEAN_NAME),
            @MenuLink(title = "menu.compat.divider.subMenu.content", exampleBeanName = DividerContent.BEAN_NAME),
             @MenuLink(title = "menu.compat.divider.subMenu.embedded", exampleBeanName = DividerEmbedded.BEAN_NAME)
 })
 @ManagedBean(name= DividerBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DividerBean extends ComponentExampleImpl<DividerBean> implements Serializable {
 	
 	public static final String BEAN_NAME = "divider";
 	
 	public DividerBean() {
 		super(DividerBean.class);
 	}
 }
