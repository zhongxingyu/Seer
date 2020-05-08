 // Copyright (c) 2011 Tim Niblett All Rights Reserved.
 //
 // File:        LoginServlet.java  (31-Oct-2011)
 // Author:      tim
 
 //
 // Copyright in the whole and every part of this source file belongs to
 // Tim Niblett (the Author) and may not be used,
 // sold, licenced, transferred, copied or reproduced in whole or in
 // part in any manner or form or in or on any media to any person
 // other than in accordance with the terms of The Author's agreement
 // or otherwise without the prior written consent of The Author.  All
 // information contained in this source file is confidential information
 // belonging to The Author and as such may not be disclosed other
 // than in accordance with the terms of The Author's agreement, or
 // otherwise, without the prior written consent of The Author.  As
 // confidential information this source file must be kept fully and
 // effectively secure at all times.
 //
 
 
 package com.cilogi.shiro.web;
 
 import com.cilogi.shiro.gae.UserDAO;
 import com.cilogi.shiro.persona.PersonaAuthenticationToken;
 import com.cilogi.shiro.persona.PersonaLogin;
 import com.cilogi.util.doc.CreateDoc;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.web.util.SavedRequest;
 import org.apache.shiro.web.util.WebUtils;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 
 @Singleton
 public class LoginServlet extends BaseServlet {
     static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
 
     private static final String TOKEN = "password";
     private static final String REMEMBER_ME = "rememberMe";
 
     private final String host;
     private final PersonaLogin personaLogin;
     private CreateDoc create;
 
     @Inject
     LoginServlet(PersonaLogin personaLogin, @Named("host") String host, CreateDoc create) {
         this.personaLogin = personaLogin;
         this.host = host;
         this.create = create;
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         showView(response, "login.ftl");
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             String token = WebUtils.getCleanParam(request, TOKEN);
             boolean rememberMe = WebUtils.isTrue(request, REMEMBER_ME);
 
             PersonaAuthenticationToken personaToken = new PersonaAuthenticationToken(token, host, rememberMe);
             try {
                 personaLogin.login(personaToken);
                 SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
                 /*
                 String redirectUrl = (savedRequest == null) ? "/index.html" : savedRequest.getRequestUrl();
                 LOG.info("redirect to " + ((savedRequest == null) ? "home" : redirectUrl));
                 response.sendRedirect(response.encodeRedirectURL(redirectUrl));
                 */
                 String redirectUrl = (savedRequest == null) ? null : savedRequest.getRequestUrl();
                 if (redirectUrl != null) {
                     response.sendRedirect(response.encodeRedirectURL(redirectUrl));
                 }  else {
                     Subject subject = SecurityUtils.getSubject();
                     String principal = (String)subject.getPrincipal();
                     issueJson(response, HTTP_STATUS_OK,
                             MESSAGE, "known",
                             "name", principal,
                             "authenticated", Boolean.toString(subject.isAuthenticated()),
                             "admin", Boolean.toString(hasRole(subject, "admin")));
                 }
             } catch (AuthenticationException e) {
                 LOG.info("Authorization failure: " + e.getMessage());
                 issue(MIME_TEXT_PLAIN, HTTP_STATUS_NOT_FOUND, "cannot authorize token: " + token, response);
             }
         } catch (Exception e) {
             LOG.info("Internal error: " + e.getMessage());
             issue(MIME_TEXT_PLAIN, HTTP_STATUS_INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage(), response);
         }
     }
 
     private void showView(HttpServletResponse response, String templateName, Object... args) throws IOException {
         String html =  create.createDocumentString(templateName, CreateDoc.map(args));
         issue(MIME_TEXT_HTML, HTTP_STATUS_OK, html, response);
     }
 }
