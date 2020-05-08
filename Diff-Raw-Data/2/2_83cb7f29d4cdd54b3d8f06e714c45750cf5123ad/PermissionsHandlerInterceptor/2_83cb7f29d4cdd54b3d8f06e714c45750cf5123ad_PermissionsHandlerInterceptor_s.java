 package com.crowdstore.web.common.interceptors;
 
 import com.crowdstore.common.security.Permissions;
 import com.crowdstore.models.security.ConditionnalOperator;
 import com.crowdstore.models.security.GlobalAuthorization;
 import com.crowdstore.models.users.AuthenticatedUser;
 import com.crowdstore.web.common.annotations.PublicSpace;
 import com.crowdstore.web.common.annotations.RequireGlobalAuthorizations;
 import com.crowdstore.web.common.session.SessionHolder;
 import com.crowdstore.web.common.util.HttpResponses;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.method.HandlerMethod;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author fcamblor
  */
 public class PermissionsHandlerInterceptor extends HandlerInterceptorAdapter {
     static Logger LOG = LoggerFactory.getLogger(PermissionsHandlerInterceptor.class);
     String authUrl;
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         boolean result = super.preHandle(request, response, handler);
 
         if (handler instanceof HandlerMethod) {
             HandlerMethod method = (HandlerMethod) handler;
 
             // If current method is annotated with @PublicSpace, don't go further : we should have access
             // even if we are not logged in !
             PublicSpace publicSpaceAnnotation = method.getMethodAnnotation(PublicSpace.class);
             if (publicSpaceAnnotation != null) {
                 return result;
             }
 
             // Starting from here, we should ensure user is logged in !
             AuthenticatedUser user = SessionHolder.getAuthenticatedUser(request);
             // If not logged in, send redirect to auth url
             if (user == null) {
                 LOG.warn("Access to {} ({}) needs an authenticated user !", request.getMethod(), request.getRequestURI());
                 HttpResponses.sendJSONRedirect(authUrl, request, response);
                 return false;
             }
 
             // Now checking if current method is annotated with @RequireGlobalAuthorizations : if so, current authenticated user
             // should match @RequireGlobalAuthorizations authorizations restrictions
             RequireGlobalAuthorizations requireGlobalAuthorizationsAnnotation = method.getMethodAnnotation(RequireGlobalAuthorizations.class);
             if (requireGlobalAuthorizationsAnnotation != null) {
                 GlobalAuthorization[] globalAuthorization = requireGlobalAuthorizationsAnnotation.value();
                 ConditionnalOperator operator = requireGlobalAuthorizationsAnnotation.operator();
 
                 if (!Permissions.hasAuthorizations(user, operator, globalAuthorization)) {
                     LOG.warn("User {} and globalAuthorizations {} cannot access {} {} (insufficient privileges !)",
                            user.getIdentity().getEmail(), user.getGlobalRole().getAuthorizations(),
                             request.getMethod(), request.getRequestURI());
                     HttpResponses.sendJSONRedirect(authUrl, request, response);
                     return false;
                 }
             }
         }
 
         return result;
     }
 
     public PermissionsHandlerInterceptor setAuthUrl(String _authUrl) {
         this.authUrl = _authUrl;
         return this;
     }
 }
