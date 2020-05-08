 /*
  * Copyright 2012-2013 Danylo Vashchilenko
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
 package org.key2gym.client;
 
 import java.util.Observable;
 
 import org.apache.log4j.Logger;
 import org.key2gym.business.api.ValidationException;
 import org.key2gym.client.resources.ResourcesManager;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * Manages the contexts used by the application.
  * <p/>
  * 
  * The class provides means to login and logout, lookup EJBs within the current
  * context.
  * <p/>
  * 
  * The manager supports one shadow context. For instance, there is a context
  * that the user opened. It's called the primary context. All lookups are done
  * with the primary context. If the user logs in one more time, the current
  * primary context becomes the shadow context, which is kept open but is not
  * used for lookups. The context opened during the second login becomes the new
  * primary context. Then, if the user logs out, the primary context is closed
  * and the shadow context becomes the primary. As it was already mentioned the
  * manager supports only one shadow context, which means that the user log in at
  * most 2 times without logging out.
  * 
  * @author Danylo Vashchilenko
  */
 
 public class ContextManager extends Observable {
 
     public ContextManager() {
 
     }
 
    public synchronized void login(String username, String password)
             throws ValidationException {
 
         if (shadowAuthentication != null) {
             throw new IllegalStateException(
                     "There is already a primary and a shadow authentication.");
         }
 
         logger.debug("Log in attempt: " + username);
 
         Authentication request = new UsernamePasswordAuthenticationToken(
                 username, password);
         Authentication result;
 
         try {
             result = Main.getContext()
                     .getBean("authenticationManager", AuthenticationManager.class)
                     .authenticate(request);
 
         } catch (BadCredentialsException ex) {
             Logger.getLogger(ContextManager.class).info(
                     "Authentication failed for the user: " + username, ex);
             throw new ValidationException(ResourcesManager.getStrings()
                     .getString("Message.LoggingInFailed"));
         }
 
         if (authentication == null) {
             authentication = result;
         } else {
             shadowAuthentication = authentication;
             authentication = result;
         }
         
         SecurityContextHolder.getContext().setAuthentication(authentication);
 
         setChanged();
         notifyObservers();
     }
 
    public synchronized void logout() {
 
         if (shadowAuthentication != null) {
             authentication = shadowAuthentication;
             shadowAuthentication = null;
         } else if (authentication != null) {
             authentication = null;
         } else {
             throw new RuntimeException("No authentication is available.");
         }
 
         SecurityContextHolder.getContext().setAuthentication(authentication);
 
         setChanged();
         notifyObservers(authentication);
     }
 
     /**
      * Looks up the EJB using the current context.
      * 
      * @param <T>
      *            the type to lookup
      * @param clazz
      *            the class of the type to lookup
      * @return an instance of the EJB
      */
     @Deprecated
     public static <T> T lookup(Class<T> clazz) {
         return Main.getContext().getBean(clazz.getName(), clazz);
     }
 
     public boolean isContextAvailable() {
         return authentication != null;
     }
 
     public boolean hasShadowContext() {
         return shadowAuthentication != null;
     }
 
     public static ContextManager getInstance() {
         return Main.getContext().getBean(ContextManager.class);
     }
 
     private Logger logger = Logger.getLogger(ContextManager.class);
 
     /**
      * The primary authentication.
      * 
      * This authentication used for all lookups.
      */
    private volatile Authentication authentication = null;
     /**
      * The shadow authentication.
      * 
      * The user can open a second session without closing the first one. The
      * primary authentication becomes the shadow authentication and the second
      * authentication becomes the new primary authentication.
      */
    private volatile Authentication shadowAuthentication = null;
 }
