 package com.atlassian.sal.core.auth;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.atlassian.sal.api.auth.AuthenticationListener;
 import com.atlassian.sal.api.auth.Authenticator;
 import com.atlassian.seraph.auth.DefaultAuthenticator;
 import com.atlassian.seraph.filter.BaseLoginFilter;
 
 public class SeraphAuthenticationListener implements AuthenticationListener
 {
     public void authenticationSuccess(final Authenticator.Result result, final HttpServletRequest request, final HttpServletResponse response)
     {
         request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, result.getPrincipal());
         request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
         request.setAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);
     }
 
     public void authenticationError(final Authenticator.Result result, final HttpServletRequest request, final HttpServletResponse response)
     {
     }
 
     public void authenticationFailure(final Authenticator.Result result, final HttpServletRequest request, final HttpServletResponse response)
     {
     }
 
     public void authenticationNotAttempted(final HttpServletRequest request, final HttpServletResponse response)
     {
     }
 }
