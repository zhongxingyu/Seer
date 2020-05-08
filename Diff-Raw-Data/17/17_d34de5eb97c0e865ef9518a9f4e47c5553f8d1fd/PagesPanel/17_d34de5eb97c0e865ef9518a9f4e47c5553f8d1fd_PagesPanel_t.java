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
 package com.gwtao.app.client;
 
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtao.ui.util.client.SwitchPanel;
 
 public class PagesPanel implements IsWidget {
 
   private final SwitchPanel switchPanel = new SwitchPanel();
 
   @Override
   public Widget asWidget() {
     return switchPanel;
   }
 
   public void show(IPage page) {
     int idx = switchPanel.getWidgetIndex(page);
     if (idx == -1) {
       switchPanel.add(page);
      idx = switchPanel.getWidgetIndex(page);
     }
    switchPanel.showWidget(idx);
   }
 
   public void close(IPage page) {
     int idx = switchPanel.getWidgetIndex(page);
     if (idx == -1) {
       switchPanel.remove(idx);
     }
   }
 
 }
