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
 
 package org.picketlink.oauth.provider.setup;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.logging.Logger;
import org.picketbox.core.event.InitializedEvent;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.credential.internal.Password;
 import org.picketlink.idm.model.SimpleUser;
 import org.picketlink.idm.model.User;
 
 /**
  * @author Pedro Silva
  * 
  */
 @ApplicationScoped
 public class SecurityInitializationEventHandler {
 
     @Inject
     private Logger logger;
     
     public void onInitialized(@Observes InitializedEvent event) {
         IdentityManager identityManager = event.getPicketBoxManager().getIdentityManager();
 
         User admin = identityManager.getUser("admin");
         
         if (admin == null) {
             // Instantiate an admin user
             admin = new SimpleUser("admin");
             admin.setFirstName("OAuth Provider");
             admin.setLastName("Admin");
 
             identityManager.add(admin);
 
 //            Calendar calendar = Calendar.getInstance();
 //            calendar.add(Calendar.YEAR, 100);
 
             identityManager.updateCredential(admin, new Password("admin123"));
 
             if (logger.isDebugEnabled()) {
                 logger.debug("User admin has been inserted into the identity store");
             }
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("User admin already exists in the identity store");
             }
         }
     }
 
 }
