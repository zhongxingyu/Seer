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
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 @ComponentExample(
         parent = DividerBean.BEAN_NAME,
         title = "example.compat.divider.content.title",
         description = "example.compat.divider.content.description",
         example = "/resources/examples/compat/divider/dividerContent.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="dividerContent.xhtml",
                     resource = "/resources/examples/compat/"+
                                "divider/dividerContent.xhtml"),
             // Java Source
             @ExampleResource(type = ResourceType.java,
                     title="DividerContent.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/"+
                                "showcase/example/compat/divider/DividerContent.java")
         }
 )
 @ManagedBean(name= DividerContent.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class DividerContent extends ComponentExampleImpl<DividerContent> implements Serializable {
 	
 	public static final String BEAN_NAME = "dividerContent";
 	
 	private String[] availableUrls;
 	private String leftUrl;
 	private String rightUrl;
 	
 	public DividerContent() {
                     super(DividerContent.class);
                     initializeInstanceVariables();
 	}
 	
         
 	public String[] getAvailableUrls() { return availableUrls; }
 	public String getLeftUrl() { return leftUrl; }
 	public String getRightUrl() { return rightUrl; }
 	
 	public void setLeftUrl(String leftUrl) { this.leftUrl = leftUrl; }
 	public void setRightUrl(String rightUrl) { this.rightUrl = rightUrl; }
 
     private void initializeInstanceVariables() {
         this.availableUrls = new String[] {
            "http://www.icefaces.org/",
            "http://wiki.icefaces.org/",
            "http://auctionmonitor.icefaces.org/",
            "http://memorygame.icefaces.org/",
            "http://www.bing.com/",
            "http://www.ask.com/"
        };
        this.leftUrl = availableUrls[0];
        this.rightUrl = availableUrls[4];
     }
 }
