 package com.sun.identity.admin;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletResponse;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 
 public class AuthFilter implements Filter {
     private static String LOGIN_PATH = "/UI/Login";
 
     private FilterConfig filterConfig = null;
 
     public void doFilter(
             ServletRequest request,
             ServletResponse response,
             FilterChain chain)
             throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         if (isAuthenticated(httpRequest)) {
             chain.doFilter(request, response);
         } else {
             String redirect = getRedirectUrl(httpRequest);
             httpResponse.sendRedirect(redirect);
         }
     }
 
     private String getRedirectUrl(HttpServletRequest request) {
         String gotoUrl = getGotoUrl(request);
         String loginUrl = getLoginUrl(request);
 
         return loginUrl + "?goto=" + gotoUrl;
     }
 
     private String getLoginUrl(HttpServletRequest request) {
         StringBuffer loginUrl = new StringBuffer();
 
         String scheme = request.getScheme();
         String server = request.getServerName();
         int port = request.getServerPort();
         String path = request.getContextPath();
 
         loginUrl.append(scheme);
         loginUrl.append("://");
         loginUrl.append(server);
         loginUrl.append(":");
         loginUrl.append(port);
         loginUrl.append(path);
 
         loginUrl.append(LOGIN_PATH);
 
         return loginUrl.toString();
     }
 
     private String getGotoUrl(HttpServletRequest request) {
         return request.getRequestURL().toString();
     }
 
     private boolean isAuthenticated(HttpServletRequest httpRequest) {
         try {
             SSOTokenManager manager = SSOTokenManager.getInstance();
             SSOToken ssoToken = manager.createSSOToken(httpRequest);
 
             return manager.isValidToken(ssoToken);
         } catch (SSOException ssoe) {
             return false;
         }
     }
 
     public FilterConfig getFilterConfig() {
         return (this.filterConfig);
     }
 
     public void setFilterConfig(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
     }
 
     public void destroy() {
     }
 
     public void init(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
     }
 }
