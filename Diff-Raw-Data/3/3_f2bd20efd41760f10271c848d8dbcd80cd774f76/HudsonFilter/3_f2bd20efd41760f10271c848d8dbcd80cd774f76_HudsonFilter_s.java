 /**
  * *****************************************************************************
  *
  * Copyright (c) 2004-2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 
  * Kohsuke Kawaguchi, Winston Prakash
  *
  ******************************************************************************
  */
 package hudson.security;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import org.eclipse.hudson.security.HudsonSecurityEntitiesHolder;
 import org.eclipse.hudson.security.HudsonSecurityManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.security.AuthenticationManager;
 import org.springframework.security.ui.rememberme.RememberMeServices;
 import org.springframework.security.userdetails.UserDetailsService;
 
 /**
  * {@link Filter} that Hudson uses to implement security support.
  *
  * <p> This is the instance the servlet container creates, but internally this
  * just acts as a proxy to the real {@link Filter}, created by {@link SecurityRealm#createFilter(FilterConfig)}.
  *
  * @author Kohsuke Kawaguchi
  * @since 1.160
  */
 public class HudsonFilter implements Filter {
 
     private transient Logger logger = LoggerFactory.getLogger(HudsonFilter.class);
     /**
      * The SecurityRealm specific filter.
      */
     private volatile Filter filter;
     /**
      * The {@link #init(FilterConfig)} may be called before the Hudson instance
      * is up (which is required for initialization of the filter). So we store
      * the filterConfig for later lazy-initialization of the filter.
      */
     private FilterConfig filterConfig;
     /**
      * {@link AuthenticationManager} proxy so that the Spring Security filter
      * chain can stay the same even when security setting is reconfigured.
      *
      * @deprecated in 1.271. This proxy always delegate to {@code Hudson.getInstance().getSecurityRealm().getSecurityComponents().manager},
      * so use that instead.
      */
     public static final AuthenticationManagerProxy AUTHENTICATION_MANAGER = new AuthenticationManagerProxy();
     /**
      * {@link UserDetailsService} proxy so that the Spring Security filter chain
      * can stay the same even when security setting is reconfigured.
      *
      * @deprecated in 1.271. This proxy always delegate to {@code Hudson.getInstance().getSecurityRealm().getSecurityComponents().userDetails},
      * so use that instead.
      */
     public static final UserDetailsServiceProxy USER_DETAILS_SERVICE_PROXY = new UserDetailsServiceProxy();
     /**
      * {@link RememberMeServices} proxy so that the Spring Security filter chain
      * can stay the same even when security setting is reconfigured.
      *
      * @deprecated in 1.271. This proxy always delegate to {@code Hudson.getInstance().getSecurityRealm().getSecurityComponents().rememberMe},
      * so use that instead.
      */
     public static final RememberMeServicesProxy REMEMBER_ME_SERVICES_PROXY = new RememberMeServicesProxy();
 
     public void init(FilterConfig filterConfig) throws ServletException {
         this.filterConfig = filterConfig;
 
         HudsonSecurityManager hudsonSecurityManager = HudsonSecurityEntitiesHolder.getHudsonSecurityManager();
 
         try {
             SecurityRealm securityRealm = hudsonSecurityManager.getSecurityRealm();
             reset(securityRealm);
             logger.debug("securityRealm is " + securityRealm);
             logger.debug("Security initialized");
         } catch (Exception exc) {
             // see HUDSON-4592. In some containers this happens before
             // WebAppMain.contextInitialized kicks in, which makes
             // the whole thing fail hard before a nicer error check
             // in WebAppMain.contextInitialized. So for now,
             // just report it here, and let the WebAppMain handle the failure gracefully.
             logger.error("Failed to initialize Hudson", exc);
         }
 
         // This is how we make this security filter available to the rest of Hudson.
         HudsonSecurityEntitiesHolder.setHudsonSecurityFilter(this);
     }
 
     /**
      * Gets the {@link HudsonFilter} created for the given {@link ServletContext}.
      */
     public static HudsonFilter get(ServletContext context) {
        return (HudsonFilter) context.getAttribute(HudsonFilter.class.getName());
     }
 
     /**
      * Reset the proxies and filter for a change in {@link SecurityRealm}.
      */
     public void reset(SecurityRealm securityRealm) throws ServletException {
         if (securityRealm != null) {
             SecurityRealm.SecurityComponents sc = securityRealm.getSecurityComponents();
             AUTHENTICATION_MANAGER.setDelegate(sc.manager);
             USER_DETAILS_SERVICE_PROXY.setDelegate(sc.userDetails);
             REMEMBER_ME_SERVICES_PROXY.setDelegate(sc.rememberMe);
             // make sure this.filter is always a valid filter.
             Filter oldf = this.filter;
             Filter newf = securityRealm.createFilter(this.filterConfig);
             newf.init(this.filterConfig);
             this.filter = newf;
             if (oldf != null) {
                 oldf.destroy();
             }
         } else {
             // no security related filter needed.
             AUTHENTICATION_MANAGER.setDelegate(null);
             USER_DETAILS_SERVICE_PROXY.setDelegate(null);
             REMEMBER_ME_SERVICES_PROXY.setDelegate(null);
             filter = null;
         }
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         logger.debug("doFilter");
 
         // to deal with concurrency, we need to capture the object.
         Filter f = filter;
 
         if (f == null) {
             // Hudson is starting up.
             chain.doFilter(request, response);
         } else {
             f.doFilter(request, response, chain);
         }
     }
 
     public void destroy() {
         // the filter can be null if the filter is not initialized yet.
         if (filter != null) {
             filter.destroy();
         }
     }
 }
