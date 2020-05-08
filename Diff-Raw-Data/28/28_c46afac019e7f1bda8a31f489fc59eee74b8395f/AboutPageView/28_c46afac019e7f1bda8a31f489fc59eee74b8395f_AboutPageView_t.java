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
 
 package com.jci.client.application.about;
 
 import com.arcbees.core.client.mvp.ViewImpl;
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 public class AboutPageView extends ViewImpl implements AboutPagePresenter.MyView {
     interface Binder extends UiBinder<Widget, AboutPageView> {
     }
 
     @UiField
     DivElement quebec;
     @UiField
     DivElement canada;
     @UiField
     AnchorElement buttonCanada;
     @UiField
     AnchorElement buttonQuebec;
 
     @Inject
     public AboutPageView(final Binder uiBinder) {
         initWidget(uiBinder.createAndBindUi(this));
 
         $(buttonCanada).click(new Function() {
             @Override
            public void f() {
                 $(canada).show();
                 $(quebec).hide();
             }
         });
 
         $(buttonQuebec).click(new Function() {
             @Override
            public void f() {
                 $(quebec).show();
                 $(canada).hide();
             }
         });
     }
 }
