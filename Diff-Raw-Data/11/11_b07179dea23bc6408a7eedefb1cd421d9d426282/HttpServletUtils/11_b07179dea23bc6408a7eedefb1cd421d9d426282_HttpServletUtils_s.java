 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.sss.util.servlets;
 
 import java.util.Enumeration;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.log4j.Logger;
 import org.apache.struts.upload.MultipartRequestWrapper;
 
 /** Package of small useful methods
  */
 public class HttpServletUtils {
   /** Had to have this because it's subclassed.
    *
    * @throws Exception
    */
   public HttpServletUtils() throws Exception {
     throw new Exception("Dont instantiate");
   }
 
   /** Return the browser type for the given request. We attempt to reduce the
    *  set of browsers to a more manageable number. The returned value is one of:
    * <ul>
    * <li>opera     The opera browser</li>
    * <li>MSIE      Microsoft Internet Explorer compatible</li>
    * <li>Netscape6 Netscape 6.x</li>
    * <li>Mozilla   Mozilla</li>
    * <li>Netscape4 Netscape 4.x compatible</li>
    * <li>default   none of the above</li>
    * </ul>
    *
    * @param   request Incoming HttpServletRequest object
    * @return  String  browser type
    */
   public static String getBrowserType(HttpServletRequest request) {
     if (request == null) {
       return "default";
     }
 
     String userAgent = String.valueOf(
         request.getHeader("User-Agent")).toLowerCase();
 
     if ((userAgent.indexOf("aladdino") >= 0) ||
         (userAgent.indexOf("avantgo") >= 0) || // AvantGo service
         (userAgent.indexOf("docomo") >= 0) ||
         (userAgent.indexOf("Elaine") >= 0) || // this is the Palm
         (userAgent.indexOf("isilo") >= 0) ||
         (userAgent.indexOf("mazingo") >= 0) ||
         (userAgent.indexOf("mobipocket webcompanion") >= 0) ||
         (userAgent.indexOf("mobipocket+webcompanion") >= 0) ||
         (userAgent.indexOf("plucker") >= 0) ||
         (userAgent.indexOf("webwasher") >= 0)) {
       /**
        * The browser is a PDA
        */
       return "PDA";
     }
 
     if (userAgent.indexOf("opera") >= 0) {
       /**
        * Opera browser
        */
       return "Opera";
     }
 
     if (userAgent.indexOf("msie") >= 0) {
       /**
        * The browser is Microsoft Internet Explorer compatible
        */
       return "MSIE";
     }
 
     if (userAgent.indexOf("netscape6") >= 0) {
       /**
        *  The browser is Netscape 6.x
        */
       return "Netscape6";
     }
 
     if (userAgent.indexOf("gecko") >= 0) {
       /**
        *  The browser is Mozilla
        */
       return "Mozilla";
     }
 
     if ((userAgent.indexOf("mozilla/4") >= 0) &&
         (userAgent.indexOf("spoofer") == -1) &&
         (userAgent.indexOf("webtv") == -1)) {
       /**
        * The browser is Netscape 4.x compatible
        */
       return "Netscape4";
     }
 
     /**
      * Otherwise return a generic value
      */
     return "default";
   }
 
   /**
    * @param req
    * @return reconstructed request
    */
   public static String getReqLine(HttpServletRequest req) {
     StringBuffer ret = new StringBuffer(getUrl(req));
     String query = req.getQueryString();
 
     if (query != null) {
       ret.append("?").append(query);
     }
 
     return ret.toString();
   }
 
   /** Returns the String url from the request.
    *
    *  @param   request     incoming request
    *  @return  String      url from the request
    */
   public static String getUrl(HttpServletRequest request) {
    if (request instanceof MultipartRequestWrapper) {
      return ((MultipartRequestWrapper)request).getRequest().
                      getRequestURL().toString();
    }

     try {
       StringBuffer sb = request.getRequestURL();
       if (sb != null) {
         return sb.toString();
       }
 
       // Presumably portlet - see what happens with uri
       return request.getRequestURI();
     } catch (Throwable t) {
       Logger.getLogger(HttpServletUtils.class).warn(
           "Unable to get url from " + request, t);
       return "BogusURL.this.is.probably.a.portal";
     }
   }
 
   /** Returns the scheme + host + port part of the url.
    *  This allows us to link to services on the same machine.
    *  <p>For example, a URL of http://myhost.com:8080/myapp/doit.do will get a
    *  result "http://myhost.com:8080"
    *
    * @param request
    * @return scheme + host + port
    */
   public static String getURLshp(HttpServletRequest request) {
     /*
     String url = getUrl(request);
 
     int pos = url.indexOf(request.getContextPath());
 
     if (pos < 0) {
       // Guess we just have to presume there is no context path
       return url;
     }
 
     return url.substring(0, pos);
     */
 
     StringBuffer sb = new StringBuffer(request.getScheme());
 
     sb.append("://");
     sb.append(request.getServerName());
     sb.append(":");
     sb.append(request.getServerPort());
 
     return sb.toString();
   }
 
   /** Returns the scheme + host + port part of the url together with the
    *  path up to the servlet path. This allows us to append a new action to
    *  the end.
    *  <p>For example, a URL of http://myhost.com:8080/myapp/doit.do will get a
    *  result "http://myhost.com:8080/myapp"
    *
    * @param request
    * @return scheme + host + port + path
    */
   public static String getURLPrefix(HttpServletRequest request) {
     return getURLshp(request) + getContext(request);
   }
 
   /** Returns the context path.
    *  <p>For example, a URL of http://myhost.com:8080/myapp/doit.do will get a
    *  result "/myapp"
    *
    * <p>Note: do we have to deal with MultipartRequestWrapper?
    *
    * @param request
    * @return context path
    */
   public static String getContext(HttpServletRequest request) {
     String context = request.getContextPath();
     if ((context == null) || (context.equals("."))) {
       context = "";
     }
 
     return context;
   }
 
   /** Return a concatenated string of all the headers
    *
    * @param   req    Incoming HttpServletRequest object
    * @return  header values
    */
   public static String getHeaders(HttpServletRequest req) {
     Enumeration en = req.getHeaderNames();
     StringBuffer sb = new StringBuffer();
 
     while (en.hasMoreElements()) {
       String name = (String) en.nextElement();
       sb.append(name);
       sb.append(": ");
       sb.append(req.getHeader(name));
       sb.append("\n");
     }
     return sb.toString();
   }
 
   /** Print all the headers
    *
    * @param   req    Incoming HttpServletRequest object
    * @param log
    */
   public static void dumpHeaders(HttpServletRequest req, Logger log) {
     Enumeration en = req.getHeaderNames();
     while (en.hasMoreElements()) {
       String name = (String) en.nextElement();
       log.debug(name + ": " + req.getHeader(name));
     }
   }
 }
 
