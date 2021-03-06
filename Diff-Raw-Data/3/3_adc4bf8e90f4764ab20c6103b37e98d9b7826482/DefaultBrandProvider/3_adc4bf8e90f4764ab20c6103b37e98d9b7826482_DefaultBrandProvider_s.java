 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.karaf.webconsole.core.brand;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.karaf.webconsole.core.BasePage;
 import org.apache.karaf.webconsole.core.behavior.FormalizeBehavior;
import org.apache.karaf.webconsole.core.behavior.JQueryUiThemeBehavior;
 import org.apache.wicket.Page;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.behavior.IBehavior;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.image.Image;
 
 public class DefaultBrandProvider implements BrandProvider, Serializable /* for tests mainly */ {
 
     public Image getHeaderImage(String imageId) {
         Image image = new Image(imageId, new ResourceReference(BasePage.class, "images/karaf-logo.png"));
         image.add(new SimpleAttributeModifier("width", "150"));
         image.add(new SimpleAttributeModifier("height", "70"));
         image.add(new SimpleAttributeModifier("alt", "Karaf logo"));
         image.add(new SimpleAttributeModifier("title", "Karaf logo"));
         return image;
     }
 
     public List<IBehavior> getBehaviors() {
         return Collections.emptyList();
     }
 
     public void modify(Page page) {
         page.add(new FormalizeBehavior());
        page.add(new JQueryUiThemeBehavior());
     }
 
 }
