 /**
  * Copyright 2013 ArcBees Inc.
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
 
 package com.jci.client.application;
 
 import javax.inject.Inject;
 
 import com.arcbees.core.client.mvp.ViewImpl;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.jci.client.application.ui.FooterView;
 import com.jci.client.application.ui.HeaderView;
 
 public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
     public interface Binder extends UiBinder<Widget, ApplicationView> {
     }
 
     @UiField
     HTMLPanel main;
     @UiField(provided = true)
     HeaderView header;
     @UiField(provided = true)
     FooterView footer;
 
     @Inject
     public ApplicationView(Binder uiBinder,
                            HeaderView headerView,
                            FooterView footerView) {
         this.header = headerView;
         this.footer = footerView;

         initWidget(uiBinder.createAndBindUi(this));
     }
 
     @Override
     public void setInSlot(Object slot, Widget content) {
         if (content != null) {
             if (slot == ApplicationPresenter.TYPE_SetMainContent) {
                 main.clear();
                 main.add(content);
             }
         }
     }
 }
