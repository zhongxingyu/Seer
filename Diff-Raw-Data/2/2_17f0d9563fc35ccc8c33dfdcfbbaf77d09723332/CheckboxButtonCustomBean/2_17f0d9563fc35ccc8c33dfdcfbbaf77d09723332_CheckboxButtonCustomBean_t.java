 package org.icefaces.samples.showcase.example.ace.checkboxButton;
 
 import org.icefaces.samples.showcase.metadata.annotation.*;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 
 /**
  * Copyright 2010-2013 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils Lundquist
  * Date: 2013-02-11
  * Time: 12:31 PM
  */
 @ComponentExample(
         title = "example.ace.checkboxButton.custom.title",
         description = "example.ace.checkboxButton.custom.description",
         example = "/resources/examples/ace/checkboxButton/checkboxbuttoncustom.xhtml",
         parent = CheckboxButtonBean.BEAN_NAME
 )
 @ExampleResources(
     resources ={
         // xhtml
         @ExampleResource(type = ResourceType.xhtml,
                          title="checkboxbuttoncustom.xhtml",
                         resource = "/resources/examples/ace/checkboxButton/checkboxbuttoncustom.xhtml"),
         // Java Source
         @ExampleResource(type = ResourceType.java,
                          title="CheckboxButtonCustomBean.java",
                          resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/checkboxButton/CheckboxButtonCustomBean.java")
     }
 )
 @ManagedBean(name= CheckboxButtonCustomBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class CheckboxButtonCustomBean extends ComponentExampleImpl<CheckboxButtonCustomBean> implements Serializable {
     public static final String BEAN_NAME = "checkboxButtonCustom";
 
     public CheckboxButtonCustomBean() {
         super(CheckboxButtonCustomBean.class);
     }
 
     @PostConstruct
     public void initMetaData() {
         super.initMetaData();
     }
 }
