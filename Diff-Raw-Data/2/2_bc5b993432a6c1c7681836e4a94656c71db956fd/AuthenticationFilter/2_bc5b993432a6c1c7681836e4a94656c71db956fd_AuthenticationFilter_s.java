 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.base.Predicate;
 import com.stackframe.regex.RegularExpressions;
 import com.stackframe.sarariman.logincookies.LoginCookie;
 import com.stackframe.sarariman.logincookies.LoginCookies;
 import com.stackframe.xml.DOMUtilities;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.regex.Pattern;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import sun.misc.BASE64Decoder;
 
 /**
  * A filter that intercepts requests and checks for authentication. This uses a confile file that is expected to be at
  * /WEB-INF/authentication.xml.
  *
  * @author mcculley
  */
 public class AuthenticationFilter extends HttpFilter {
 
     private Directory directory;
 
     private LoginCookies loginCookies;
 
     /**
      * The realm name that will be used with Basic authentication.
      */
     private String realm;
 
     /**
      * A Predicate which evaluates to true if the parameter represents a resource which does not require authentication.
      */
     private Predicate<CharSequence> publicPatternsMatches;
 
     /**
      * A Predicate which evaluates to true if the parameter represents a user agent which should use Basic authentication.
      */
     private Predicate<CharSequence> basicAuthMatches;
 
     private static final XPath xpath = XPathFactory.newInstance().newXPath();
 
     private static Iterable<String> getPublicResourcePatterns(Document document) {
         try {
             XPathExpression expr = xpath.compile("/authentication/publicResource/@pattern");
             NodeList nodeList = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
             return DOMUtilities.values(nodeList);
         } catch (XPathExpressionException e) {
             throw new AssertionError(e);
         }
     }
 
     private static Iterable<String> getBasicAuthPatterns(Document document) {
         try {
             XPathExpression expr = xpath.compile("/authentication/forceBasicAuthUserAgent/@pattern");
             NodeList nodeList = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
             return DOMUtilities.values(nodeList);
         } catch (XPathExpressionException e) {
             throw new AssertionError(e);
         }
     }
 
     private static Document getConfigFile(FilterConfig filterConfig) throws ServletException {
         try {
             URL configResource = filterConfig.getServletContext().getResource("/WEB-INF/authentication.xml");
             return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configResource.toExternalForm());
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     public void init(FilterConfig filterConfig) throws ServletException {
         Sarariman sarariman = (Sarariman)filterConfig.getServletContext().getAttribute("sarariman");
         directory = sarariman.getDirectory();
         loginCookies = sarariman.getLoginCookies();
         Document configurationDocument = getConfigFile(filterConfig);
         realm = configurationDocument.getDocumentElement().getAttribute("realm");
         Iterable<Pattern> publicPatterns = RegularExpressions.compile(getPublicResourcePatterns(configurationDocument));
         publicPatternsMatches = RegularExpressions.matchesPredicate(publicPatterns);
         Iterable<Pattern> basicAuthPatterns = RegularExpressions.compile(getBasicAuthPatterns(configurationDocument));
         basicAuthMatches = RegularExpressions.matchesPredicate(basicAuthPatterns);
        System.err.println("basicAuthMatches=" + basicAuthMatches);
     }
 
     private void handleHasLoginCookie(HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginCookie loginCookie) throws IOException, ServletException {
         System.err.println("handleHasLoginCookie entered");
         String username = loginCookie.getUsername();
         int domainIndex = username.indexOf('@');
         username = username.substring(0, domainIndex); // FIXME: Check for proper domain and dispatch.
         Employee user = directory.getByUserName().get(username);
         request.getSession().setAttribute("user", user);
 
         // FIXME: Do this block in a background thread.
         loginCookie.setLastUsed(new Date());
         loginCookie.setUserAgent(request.getHeader("User-Agent"));
         loginCookie.setRemoteAddress(request.getRemoteAddr());
 
         chain.doFilter(request, response);
     }
 
     private void handleHasAuthorizationHeader(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String authorizationHeader) throws IOException, ServletException {
         System.err.println("handleHasAuthorizationHeader entered");
         BASE64Decoder decoder = new BASE64Decoder();
         String[] split = authorizationHeader.split(" ");
         String encodedBytes = split[1];
         byte[] decodedBytes = decoder.decodeBuffer(encodedBytes);
         String decodedString = new String(decodedBytes);
         int firstColon = decodedString.indexOf(':');
         String username = decodedString.substring(0, firstColon);
         username = username.toLowerCase();
         String password = decodedString.substring(firstColon + 1);
         int domainIndex = username.indexOf('@');
         if (domainIndex != -1) {
             // FIXME: Check for proper domain and dispatch.
             String domain = username.substring(domainIndex + 1);
             if (!"stackframe.com".equals(domain)) {
                 response.sendError(401, "invalid username or password");
                 return;
             }
 
             username = username.substring(0, domainIndex);
         }
 
         boolean valid = directory.checkCredentials(username, password);
         if (valid) {
             Employee user = directory.getByUserName().get(username);
             request.getSession().setAttribute("user", user);
             chain.doFilter(request, response);
         } else {
             response.sendError(401, "invalid username or password");
         }
     }
 
     private void sendBasicAuthChallenge(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
         System.err.println("sendBasicAuthChallenge entered");
         response.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", realm));
         response.sendError(401);
     }
 
     private void redirectToLoginPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
         System.err.println("redirectToLoginPage entered");
         String destination = request.getRequestURI();
         String queryString = request.getQueryString();
         if (queryString != null) {
             destination = destination + '?' + queryString;
         }
 
         // FIXME: If the method requested was a POST, redirect to home page or something.
 
         String redirectURL = String.format("%s/login", request.getContextPath());
         String defaultDestination = request.getContextPath() + "/";
         if (!destination.equals(defaultDestination)) {
             redirectURL = redirectURL + String.format("?destination=%s", URLEncoder.encode(destination, "UTF-8"));
         }
 
         response.sendRedirect(redirectURL);
     }
 
     private void handleUnauthenticated(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
         System.err.println("handleUnauthenticated entered");
         LoginCookie loginCookie = loginCookies.findLoginCookie(request);
         if (loginCookie != null) {
             handleHasLoginCookie(request, response, chain, loginCookie);
         } else {
             String authorizationHeader = request.getHeader("Authorization");
             if (authorizationHeader == null) {
                 String requestPath = request.getServletPath();
                 if (publicPatternsMatches.apply(requestPath)) {
                     chain.doFilter(request, response);
                 } else {
                     if (basicAuthMatches.apply(request.getHeader("User-Agent"))) {
                         sendBasicAuthChallenge(request, response, chain);
                     } else {
                         redirectToLoginPage(request, response, chain);
                     }
                 }
             } else {
                 handleHasAuthorizationHeader(request, response, chain, authorizationHeader);
             }
         }
     }
 
     protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
         System.err.println("doFilter entered");
         HttpSession session = request.getSession();
         Employee user = (Employee)session.getAttribute("user");
         if (user == null) {
             handleUnauthenticated(request, response, chain);
         } else {
             chain.doFilter(request, response);
         }
     }
 
     public void destroy() {
     }
 
 }
