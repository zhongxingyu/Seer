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
 
 import org.picketbox.core.config.AuthenticationConfiguration;
 import org.picketbox.core.config.AuthorizationConfiguration;
import org.picketbox.core.config.EventManagerConfiguration;
 import org.picketbox.core.config.IdentityManagerConfiguration;
 import org.picketbox.core.config.PicketBoxConfiguration;
 import org.picketbox.core.config.SessionManagerConfig;
import org.picketbox.core.event.PicketBoxEventManager;
 
 /**
  * A HTTP Configuration for PicketBox
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 public class PicketBoxHTTPConfiguration extends PicketBoxConfiguration {
 
     private ProtectedResourceConfig protectedResource;
 
     public PicketBoxHTTPConfiguration(AuthenticationConfiguration authentication, AuthorizationConfiguration authorization,
            IdentityManagerConfiguration identityManager, ProtectedResourceConfig protectedResource, SessionManagerConfig sessionManager, EventManagerConfiguration eventManager) {
        super(authentication, authorization, identityManager, sessionManager, eventManager);
         this.protectedResource = protectedResource;
     }
 
     /**
      * Return the {@link ProtectedResourceConfig}
      *
      * @return the protected resource config
      */
     public ProtectedResourceConfig getProtectedResource() {
         return protectedResource;
     }
 
     @Override
     public HTTPSessionManagerConfiguration getSessionManager() {
         return (HTTPSessionManagerConfiguration) super.getSessionManager();
     }
 }
