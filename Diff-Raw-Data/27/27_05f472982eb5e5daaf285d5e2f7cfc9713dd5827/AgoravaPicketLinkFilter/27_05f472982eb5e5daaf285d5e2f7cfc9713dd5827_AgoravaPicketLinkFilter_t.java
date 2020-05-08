 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.gastaldi.app.agorava.http;
 
import org.agorava.LinkedIn;
import org.agorava.core.api.oauth.OAuthService;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
 
import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
import javax.servlet.*;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
 
 @WebFilter(filterName = "AgoravaPicketLink Filter",
          urlPatterns = "/*")
 public class AgoravaPicketLinkFilter implements Filter
 {
 
    @Inject
    private HttpProducer producer;
 
    @Inject
    @LinkedIn
   Instance<OAuthService> services;
 
    @Inject
    Identity identity;
 
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
       // Do nothing
    }
 
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
             throws IOException,
             ServletException
    {
       HttpServletRequest httpRequest = (HttpServletRequest) request;
       HttpServletResponse httpResponse = (HttpServletResponse) response;
       producer.setRequest(httpRequest);
       producer.setResponse(httpResponse);
 
       String requestURI = httpRequest.getRequestURI();
       if (requestURI.contains("oauth_callback"))
       {
         OAuthService service=services.get();
          String verifier = httpRequest.getParameter(service.getVerifierParamName());
          service.setVerifier(verifier);
          service.initAccessToken();
          AuthenticationResult result = identity.login();
          System.out.println("RESULT: " + result);
 
          // TODO: Redirect someplace else
          httpResponse.sendRedirect(httpRequest.getContextPath());
       }
       else
       {
          chain.doFilter(request, response);
       }
    }
 
    @Override
    public void destroy()
    {
    }
 }
