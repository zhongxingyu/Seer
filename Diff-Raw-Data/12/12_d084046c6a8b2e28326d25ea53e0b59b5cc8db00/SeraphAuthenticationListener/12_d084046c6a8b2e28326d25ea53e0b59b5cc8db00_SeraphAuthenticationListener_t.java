 package com.atlassian.sal.core.auth;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.atlassian.sal.api.auth.AuthenticationListener;
 import com.atlassian.sal.api.auth.Authenticator;
 import com.atlassian.seraph.auth.DefaultAuthenticator;
 import com.atlassian.seraph.filter.BaseLoginFilter;
 
 public class SeraphAuthenticationListener implements AuthenticationListener
 {
    // This is equal to BaseLoginFilter.ALREADY_FILTERED, which has protected access and so can't be referenced
    private static final String ALREADY_FILTERED = "loginfilter.already.filtered";

     public void authenticationSuccess(final Authenticator.Result result, final HttpServletRequest request, final HttpServletResponse response)
     {
         request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, result.getPrincipal());
         request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

        // This must be set to indicate to Crowd that authentication was successful on this request, so don't invalidate
        // it if other credentials such as SSO tokens are not found
         request.setAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);

        // This must be set because the OAuth filter is plugged in before the login filter, which overwrites the
        // OS_AUTHSTATUS_KEY attribute, thus this listener will break for SSO providers like Crowd
        request.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
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
