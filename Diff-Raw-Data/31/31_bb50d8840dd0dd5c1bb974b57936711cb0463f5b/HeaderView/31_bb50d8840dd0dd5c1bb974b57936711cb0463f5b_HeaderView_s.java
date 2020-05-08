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
 
 package com.jci.client.application.ui;
 
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 import com.jci.client.resource.header.HeaderResource;
 
 import javax.inject.Inject;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 public class HeaderView extends ViewWithUiHandlers<HeaderUiHandlers> implements HeaderPresenter.MyView, AttachEvent.Handler {
     interface Binder extends UiBinder<Widget, HeaderView> {
     }
 
     @UiField
     DivElement menu;
     @UiField
     AnchorElement localeSwitch;
 
     private final String activeStyleName;
 
     @Inject
     public HeaderView(Binder binder,
                       HeaderResource headerResource) {
         initWidget(binder.createAndBindUi(this));
 
         activeStyleName = headerResource.style().active();
 
         asWidget().addAttachHandler(this);
     }
 
     @Override
     public void onAttachOrDetach(AttachEvent attachEvent) {
         if (attachEvent.isAttached()) {
             bindGwtQuery();
         }
     }
 
     @Override
     public void activateCurrentLink(String nameTokens) {
         $("." + activeStyleName).removeClass(activeStyleName);
         $("." + nameTokens).addClass(activeStyleName);
     }
 
     @Override
     public void setRegistrationUrl(String registrationUrl) {
        $("#registerHeader").attr("href", "http://www.jccq.qc.ca" + registrationUrl);
        $("#registerHeaderHome").attr("href", "http://www.jccq.qc.ca" + registrationUrl);
     }
 
     private void bindGwtQuery() {
         $(localeSwitch).click(new Function() {
             @Override
             public void f() {
                 getUiHandlers().switchLocale();
             }
         });
     }
 }
