 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.picketbox.http.config;
 
 import org.picketbox.core.authorization.AuthorizationManager;
 import org.picketbox.core.config.PicketBoxAuthenticationConfig;
 import org.picketbox.core.exceptions.ConfigurationException;
 import org.picketbox.core.identity.DefaultIdentityManager;
 import org.picketbox.core.identity.IdentityManager;
 import org.picketbox.core.logout.LogoutManager;
 import org.picketbox.core.resource.ProtectedResourceConstraint;
 import org.picketbox.core.resource.ProtectedResourceManager;
 import org.picketbox.http.PicketBoxHTTPMessages;
 import org.picketbox.http.PicketBoxManager;
 import org.picketbox.http.logout.HTTPLogoutManager;
 
 /**
  * <p>
  * This class should be used to build the configuration and start the {@link PicketBoxManager}.
  * </p>
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 @SuppressWarnings({ "rawtypes" })
 public final class PicketBoxConfiguration {
 
     private PicketBoxManager picketBoxManager;
 
     private AuthorizationManager authorizationManager;
     private IdentityManager identityManager;
     private LogoutManager logoutManager;
     private ProtectedResourceManager protectedResourceManager;
 
     private PicketBoxAuthenticationConfig authConfig;
 
     public PicketBoxConfiguration() {
     }
 
     public PicketBoxAuthenticationConfig authentication() {
         if (this.authConfig == null) {
             this.authConfig = new PicketBoxAuthenticationConfig();
         }
 
         return this.authConfig;
     }
 
     public void setProtectedResourceManager(ProtectedResourceManager protectedResourceManager) {
         this.protectedResourceManager = protectedResourceManager;
     }
 
     /**
      * <pConfiguration method to register an @{link AuthorizationManager} instance.</p>
      *
      * @param authorizationManager
      * @return the configuration with the {@link AuthorizationManager} instance configured.
      */
     public PicketBoxConfiguration authorization(AuthorizationManager authorizationManager) {
         this.authorizationManager = authorizationManager;
         return this;
     }
 
     /**
      * <pConfiguration method to register an {@link IdentityManager} instance.</p>
      *
      * @param identityManager
      * @return the configuration with the {@link IdentityManager} instance configured.
      */
     public PicketBoxConfiguration identityManager(IdentityManager identityManager) {
         this.identityManager = identityManager;
         return this;
     }
 
     /**
      * <pConfiguration method to register an {@link LogoutManager} instance.</p>
      *
      * @param identityManager
      * @return the configuration with the {@link LogoutManager} instance configured.
      */
     public PicketBoxConfiguration logoutManager(LogoutManager logoutManager) {
         this.logoutManager = logoutManager;
         return this;
     }
 
     /**
      * <p>
      * Create and starts a {@link PicketBoxManager} instance. Call this method when all configuration was done.
      * </p>
      *
      * @return a started {@link PicketBoxManager} instance.
      * @throws ConfigurationException if some error occur during the creation or startup of the {@link PicketBoxManager}
      *         instance. Or if the {@link PicketBoxManager} was already builded or started.
      */
     public PicketBoxManager buildAndStart() throws ConfigurationException {
         try {
             if (this.logoutManager == null) {
                 this.logoutManager = new HTTPLogoutManager();
             }
 
             this.picketBoxManager = new PicketBoxManager(this.authConfig.build(), this.logoutManager,
                     this.protectedResourceManager);
 
             this.picketBoxManager.setAuthorizationManager(this.authorizationManager);
 
             if (this.identityManager == null) {
                 this.identityManager = new DefaultIdentityManager();
             }
 
             this.picketBoxManager.setIdentityManager(this.identityManager);
 
             this.picketBoxManager.start();
         } catch (Exception e) {
             this.picketBoxManager = null;
             throw PicketBoxHTTPMessages.MESSAGES.failedToConfigurePicketBoxManager(e);
         }
 
         if (!picketBoxManager.started()) {
             throw PicketBoxHTTPMessages.MESSAGES.picketBoxManagerNotProperlyStarted();
         }
 
         return this.picketBoxManager;
     }
 
     public void addProtectedResource(String pattern, ProtectedResourceConstraint constraint) {
         this.protectedResourceManager.addProtectedResource(pattern, constraint);
     }
 
     public PicketBoxConfiguration resourceManager(ProtectedResourceManager resourceManager) {
         this.protectedResourceManager = resourceManager;
         return this;
     }
 }
