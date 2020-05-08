 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.social.quickstart.user;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.social.connect.UsersConnectionRepository;
 import org.springframework.social.facebook.api.Facebook;
 import org.springframework.social.linkedin.api.LinkedIn;
 import org.springframework.social.quickstart.SPConnectionRetriever;
 import org.springframework.social.quickstart.config.Uris;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 import org.springframework.web.servlet.view.RedirectView;
 
 /**
  * Before a request is handled: 1. sets the current User in the {@link SecurityContext} from a
  * cookie, if present and the user is still connected to Facebook. 2. requires that the user sign-in
  * if he or she hasn't already.
  * 
  * NB : this user interceptor is actually invoked only if the view is added in the registry, see
  * WebMVC Config
  * 
  * @author Yves Nicolas adapted from Keith Donald Samples
  */
 public final class UserInterceptor extends HandlerInterceptorAdapter {
 
     private final UsersConnectionRepository connectionRepository;
 
     private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();
 
     public UserInterceptor(UsersConnectionRepository connectionRepository) {
         this.connectionRepository = connectionRepository;
     }
 
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 
         // Handle the requests that should go thru
         if (shouldGoThru(request))
             return true;
 
         SPConnectionRetriever spResolver;
 
         rememberUser(request, response);
 
         // Handle the requests that should go thru
 
         // checking whether connection do the application has been made
         if (!SecurityContext.userSignedIn()) {
             new RedirectView(Uris.SIGNIN, true).render(null, request, response);
             return false;
         }
        
        // Signing out
        if (handleSignOut(request, response)) {
            return false;
        }

 
         // If no service provider defined, proced
         try {
             spResolver = SecurityContext.getCurrentSpResolver();
         } catch (IllegalStateException e) {
            return true; 
 
         }
         // checking whether connection to current service provider has been made and if not,
         // redirect to the correct connection URL:
         if (!SPAuthorized(SecurityContext.getCurrentUser().getId())) {
             // TODO : Modify following to avoid stopping by the confirmation view (directly update
             // the request?)
             new RedirectView(spResolver.getConnectUrl(), true).render(null, request, response);
             return false;
 
         }
 
         // At this stage, we can proceed to the regular controller as signing is
         // effective
         return true;
     }
 
     private boolean shouldGoThru(HttpServletRequest request) {
         if (request.getServletPath().startsWith(Uris.APPCONNECTPREFIX))
             return true;
 
         if (request.getServletPath().startsWith(Uris.SPRINGCONNECTPREFIX))
             return true;
 
         return false;
     }
 
     public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
             throws Exception {
 
         // SecurityContext.remove();
     }
 
     // internal helpers
 
     // Gets a potential user ID from cookies existing on the system.
     private void rememberUser(HttpServletRequest request, HttpServletResponse response) {
         String userId = userCookieGenerator.readCookieValue(request);
         if (userId == null) {
 
             // No Cookie : no potential user found
             return;
         } else if (!userIsValid(userId)) {
 
             // Cookie referencing an invalid user, should be removed
             // Then proceed as if no user on system
             userCookieGenerator.removeCookie(response);
             return;
         }
 
         else {
             // User Id is a valid one, we check it as the current user
             SecurityContext.setCurrentUser(new User(userId));
         }
 
     }
 
     // If signout has been asked for sign out
    private boolean handleSignOut(HttpServletRequest request, HttpServletResponse response) throws Exception {
         if (SecurityContext.userSignedIn() && request.getServletPath().startsWith(Uris.SIGNOUT)) {
             if (request.getServletPath().equals(Uris.PARTIALSIGNOUT)) {
                 SPConnectionRetriever spResolver;
                 try {
                     spResolver = SecurityContext.getCurrentSpResolver();
                     connectionRepository.createConnectionRepository(SecurityContext.getCurrentUser().getId())
                             .removeConnections(spResolver.getActiveSP().toString().toLowerCase());
                     SecurityContext.removesp();
                 } catch (IllegalStateException e) {
                 }
                new RedirectView(Uris.MAIN, true).render(null, request, response);
                return true;
             }
             // userCookieGenerator.removeCookie(response);
             SecurityContext.remove();
            new RedirectView(Uris.BYE, true).render(null, request, response);
             return true;
         } else
             return false;
     }
 
     // Checks validity of userId
     // For the moment, any non void String is considered valid
     private boolean userIsValid(String userId) {
         // pour premiers essais : on est toujours supposé etre un nouveau.
         return (userId != "");
     }
 
     @SuppressWarnings("unchecked")
     private boolean SPAuthorized(String userId) {
         return connectionRepository.createConnectionRepository(userId).findPrimaryConnection(
                 SecurityContext.getCurrentSpResolver().getSPType()) != null;
     }
 }
