 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.aerogear.prodoctor.rest;
 
 import org.aerogear.prodoctor.model.SaleAgent;
 import org.aerogear.prodoctor.rest.ResponseHeaders;
 import org.jboss.aerogear.security.auth.AuthenticationManager;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.model.Attribute;
 import org.picketlink.idm.model.IdentityType;
 import org.picketlink.idm.model.SimpleUser;
 import org.picketlink.idm.model.User;
 import org.picketlink.idm.query.IdentityQuery;
 
 
 import javax.ejb.Stateless;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import java.util.logging.Logger;
 
 @Stateless
 public class Login
 {
 
    private static final Logger LOGGER = Logger.getLogger(Login.class.getSimpleName());
 
 
    @Inject
    private AuthenticationManager authenticationManager;
    
    @Inject
    private IdentityManager identityManager;
 
   
 
    @Inject
    Event<ResponseHeaders> headers;
 
    public void index()
    {
       LOGGER.info("Login page!");
    }
 
    /**
     * {@link org.jboss.aerogear.security.model.AeroGearUser} registration
     *
     * @param user represents a simple implementation that holds user's credentials.
     * @return HTTP response and the session ID
     */
    public SaleAgent login(final SaleAgent user)
    {
       performLogin(user);
       return user;
    }
 
    public void logout()
    {
       LOGGER.info("User logout!");
       authenticationManager.logout();
    }
 
    private void performLogin(SaleAgent saleAgent)
    {
       authenticationManager.login(saleAgent,saleAgent.getPassword());
    }
 
  
 }
