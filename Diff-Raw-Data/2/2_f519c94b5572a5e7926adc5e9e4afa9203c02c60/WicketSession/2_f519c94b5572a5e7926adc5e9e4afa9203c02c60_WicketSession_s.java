 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.dip.komusubi.lunch.wicket;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import jp.dip.komusubi.lunch.Configuration;
 import jp.dip.komusubi.lunch.model.User;
 import jp.dip.komusubi.lunch.module.Basket;
 import jp.dip.komusubi.lunch.service.AccountService;
 import jp.dip.komusubi.lunch.wicket.component.FormKey;
 import jp.dip.komusubi.lunch.wicket.component.SimpleBrowserInfoPage;
 
 import org.apache.wicket.Session;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.request.Request;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * wicket session.
  * @author jun
  */
 public class WicketSession extends AuthenticatedWebSession {
 
     public static final String VARIATION_JQUERY_MOBILE = "jquery";
     private static final long serialVersionUID = 2537313227105289690L;
     private static final Logger logger = LoggerFactory.getLogger(WicketSession.class);
     private User loggedInUser;
     private Set<FormKey> formKeys = new HashSet<>();
     private Basket basket;
 
     public static WicketSession get() {
         return (WicketSession) Session.get();
     }
 
     /**
      * constructor.
      * @param request wicket request object.
      */
     public WicketSession(Request request) {
         super(request);
     }
 
     @Override
     public boolean authenticate(final String email, final String password) {
         AccountService accountService = Configuration.getInstance(AccountService.class);
         if (accountService.signIn(email, password)) {
             loggedInUser = accountService.find(email);
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public Roles getRoles() {
         if (isSignedIn()) {
             return new Roles(Roles.ADMIN);
         }
         return null;
     }
 
     public User getSignedInUser() {
         if (!get().isSignedIn()) {
             if (logger.isDebugEnabled())
                 logger.debug("user didn't login yet.");
             loggedInUser = null;
         }
         return loggedInUser;
     }
 
     @Override
     public WebPage newBrowserInfoPage() {
         return new SimpleBrowserInfoPage();
     }
 
     public void addFormKey(FormKey key) {
         formKeys.add(key);
     }
 
     public boolean removeFormKey(FormKey key) {
         return formKeys.remove(key);
     }
     
     public Basket getBasket() {
         if (this.basket == null)
            this.basket = Configuration.INSTANCE.getInstance(Basket.class);
         return this.basket;
     }
 }
