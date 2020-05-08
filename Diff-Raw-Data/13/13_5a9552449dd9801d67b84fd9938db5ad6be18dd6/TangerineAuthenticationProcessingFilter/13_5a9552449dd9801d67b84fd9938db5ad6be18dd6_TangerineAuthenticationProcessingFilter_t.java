 package com.orangeleap.tangerine.security;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.Authentication;
 import org.springframework.security.AuthenticationException;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
 import org.springframework.security.util.TextUtils;
 import org.springframework.util.Assert;
 import org.springframework.web.util.WebUtils;
 
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.service.ldap.LdapService;
import com.orangeleap.tangerine.service.SessionService;
 import com.orangeleap.tangerine.type.AccessType;
 import com.orangeleap.tangerine.util.HttpUtil;
 
 public class TangerineAuthenticationProcessingFilter extends AuthenticationProcessingFilter {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     public static final String SITE_KEY = "sitename";
 
     private String siteParameter = SITE_KEY;
    
     public static final String FULLNAME_KEY = "j_fullname";
 
     private final String fullNameParameter = FULLNAME_KEY;
 
     private PageCustomizationService pageCustomizationService;
 
    @Autowired
    private SessionService sessionService;

     public void setPageCustomizationService(PageCustomizationService pageCustomizationService) {
         this.pageCustomizationService = pageCustomizationService;
     }
 
     @Autowired
     private LdapService ldapService;
 
     @Override
     public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
         String username = obtainUsername(request);
         String password = obtainPassword(request);
         String site = obtainSite(request);
         String fullName = obtainFullName(request);
 
         if (username == null) {
             username = "";
         }
 
         if (password == null) {
             password = "";
         }
 
         username = username.trim();
 
         TangerineAuthenticationToken authRequest = new TangerineAuthenticationToken(username, password, site);
 
         // Place the last username attempted into HttpSession for views
         HttpSession session = request.getSession(false);
 
         if (session != null || getAllowSessionCreation()) {
             request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextUtils.escapeEntities(fullName));
         }
 
         // Allow subclasses to set the "details" property
         setDetails(request, authRequest);
         return getAuthenticationManager().authenticate(authRequest);
     }
 
     @Override
     protected void onPreAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
         HttpUtil.removeCookie("siteCookie", response);
         super.onPreAuthentication(request, response);
     }
 
     @Override
     protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
         super.onSuccessfulAuthentication(request, response, authResult);
         HttpUtil.setCookie("siteCookie", obtainSite(request), Integer.MAX_VALUE, response);
         GrantedAuthority[] authorities = authResult.getAuthorities();
         List<String> roles = new ArrayList<String>();
         for (int i = 0; i < authorities.length; i++) {
             roles.add(authorities[i].getAuthority());
         }
         Map<String, AccessType> pageAccess = pageCustomizationService.readPageAccess(roles);
         WebUtils.setSessionAttribute(request, "pageAccess", pageAccess);
         ((TangerineAuthenticationToken) authResult).setPageAccess(pageAccess);
         logger.trace(pageAccess);
 
        // Call to ensure user is setup in the database. This comes into play
        // when a user is in LDAP but not in the database yet. Calling this
        // method will create the user record and associate with the correct site
        sessionService.lookupSite();

         WebUtils.setSessionAttribute(request, "passwordChangeRequired", ldapService.isPasswordChangeRequired(60));
         WebUtils.setSessionAttribute(request, "lastLoginDate", ldapService.getLastLogin());
         WebUtils.setSessionAttribute(request, "currentDate",new Date());
         ldapService.setLastLogin();
     }
 
     protected String obtainSite(HttpServletRequest request) {
         return request.getParameter(siteParameter);
     }
 
     protected String obtainFullName(HttpServletRequest request) {
         return request.getParameter(fullNameParameter);
     }
 
     protected void setDetails(HttpServletRequest request, TangerineAuthenticationToken authRequest) {
         authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
     }
 
     public void setSiteParameter(String siteParameter) {
         Assert.hasText(siteParameter, "Site parameter must not be empty or null");
         this.siteParameter = siteParameter;
     }
 
     public String getSiteParameter() {
         return siteParameter;
     }
 }
