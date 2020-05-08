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
 
 import com.google.gwt.i18n.client.LocaleInfo;
 import com.google.gwt.user.client.Window;
 import com.google.web.bindery.event.shared.EventBus;
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.jci.client.rest.RegistrationService;
 import com.jci.shared.domain.RegistrationUrl;
 import org.fusesource.restygwt.client.Method;
 import org.fusesource.restygwt.client.MethodCallback;
 
 import javax.inject.Inject;
 
 public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView> implements HeaderUiHandlers {
     public interface MyView extends View, HasUiHandlers<HeaderUiHandlers> {
         void activateCurrentLink(String nameToken);
 
         void setRegistrationUrl(String registrationUrl);

        void logError(String errorMsg);
     }
 
     private final PlaceManager placeManager;
     private final RegistrationService registrationService;
 
     @Inject
     public HeaderPresenter(EventBus eventBus,
                            MyView view,
                            PlaceManager placeManager,
                            RegistrationService registrationService) {
         super(eventBus, view);
 
         this.placeManager = placeManager;
         this.registrationService = registrationService;
 
         getView().setUiHandlers(this);
     }
 
     @Override
     protected void onBind() {
         super.onBind();
 
         activateCurrentLinks();
         getSubscriptionUrl();
     }
 
     @Override
     protected void onReset() {
         super.onReset();
 
         activateCurrentLinks();
     }
 
     private void activateCurrentLinks() {
         PlaceRequest placeRequest = placeManager.getCurrentPlaceRequest();
         String nameToken = placeRequest.getNameToken();
 
         getView().activateCurrentLink(nameToken);
     }
 
     @Override
     public void switchLocale() {
         LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
 
         String newLocale;
 
         if (currentLocale.getLocaleName().equals("fr")) {
             newLocale = "en";
         } else {
             newLocale = "fr";
         }
 
         Window.Location.assign(Window.Location.createUrlBuilder()
                 .setParameter(LocaleInfo.getLocaleQueryParam(), newLocale)
                 .buildString());
     }
 
 
     private void getSubscriptionUrl() {
         registrationService.getRegistrationUrl(new MethodCallback<RegistrationUrl>() {
             @Override
             public void onFailure(Method method, Throwable exception) {
                getView().logError("Failure to load registration url");
             }
 
             @Override
             public void onSuccess(Method method, RegistrationUrl registrationUrl) {
                 LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
                 if (currentLocale.getLocaleName().equals("fr")) {
                     getView().setRegistrationUrl(registrationUrl.getFr());
                 } else {
                     getView().setRegistrationUrl(registrationUrl.getEn());
                 }
             }
         });
     }
 }
