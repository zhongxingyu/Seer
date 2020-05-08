 /* 
  * Copyright 2012 Matthias Huebner
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.gwtao.ui.util.client.mask;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.CssResource.NotStrict;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class WaitInfo implements IsWidget {
 
   public interface Style extends CssResource {
   };
 
   public interface Resource extends ClientBundle {
 
     @Source("circle-ball.gif")
     ImageResource loadingImage();
 
     @Source("mask.css")
     @NotStrict
     Style styles();
   }
 
   private static final Resource defaultResource = GWT.create(Resource.class);
 
   private final VerticalPanel msg = new VerticalPanel();
   private final VerticalPanel inner = new VerticalPanel();
   private final HTML text = new HTML();
   private Resource resource;
 
   public WaitInfo() {
 
     this.msg.setStyleName("gwtao-waitmask-msg");
     this.inner.setStyleName("gwtao-waitmask-msg-inner");
     this.msg.add(inner);
 
     HorizontalPanel row = new HorizontalPanel();
     Image icon = new Image(getResource().loadingImage());
     row.add(icon);
     this.text.setStyleName("gwtao-waitmask-msg-text");
     row.add(this.text);
     row.setCellVerticalAlignment(icon, HasVerticalAlignment.ALIGN_MIDDLE);
     row.setCellVerticalAlignment(this.text, HasVerticalAlignment.ALIGN_MIDDLE);
     add(row);
   }
 
   private Resource getResource() {
     if (resource == null)
       resource = defaultResource;
     resource.styles().ensureInjected();
     return resource;
   }
 
   public void add(Widget row) {
     inner.add(row);
   }
 
   public void setMessage(SafeHtml msg) {
    if (msg == null)
      text.setText("");
    else
      text.setHTML(msg);
   }
 
   public Widget asWidget() {
     return msg;
   }
 
 }
