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
 
 import org.picketbox.core.config.ConfigurationBuilder;
 import org.picketbox.core.config.PicketBoxConfiguration;
 
 /**
  * A {@link ConfigurationBuilder} for HTTP
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 public class HTTPConfigurationBuilder extends ConfigurationBuilder {
 
     private ProtectedResourceConfigurationBuilder protectedResource;
     private HTTPSessionManagerConfigurationBuilder sessionManager;
 
     public HTTPConfigurationBuilder() {
         this.protectedResource = new ProtectedResourceConfigurationBuilder(this);
         this.sessionManager = new HTTPSessionManagerConfigurationBuilder(this);
     }
 
     /**
      * @return
      */
     public ProtectedResourceConfigurationBuilder protectedResource() {
         return this.protectedResource;
     }
 
     /* (non-Javadoc)
      * @see org.picketbox.core.config.ConfigurationBuilder#sessionManager()
      */
     @Override
     public HTTPSessionManagerConfigurationBuilder sessionManager() {
         return this.sessionManager;
     }
 
     @Override
     public PicketBoxConfiguration doBuild() {
         return new PicketBoxHTTPConfiguration(this.authentication().build(), this.authorization().build(), this
                .identityManager().build(), this.protectedResource.build(), this.sessionManager().build(), this.eventManager().build());
     }
 }
