 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 
 package edu.rpi.cct.webdav.servlet.common;
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.XmlUtil;
 
 import java.io.FilterReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.apache.log4j.Logger;
 import org.bedework.davdefs.WebdavTags;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /** Base class for all webdav servlet methods.
  */
 public abstract class MethodBase {
   protected boolean debug;
 
   protected boolean dumpContent;
 
   protected transient Logger log;
 
 //  private ServletConfig config;
 
   /** namespace interface for this request
    */
   protected WebdavNsIntf nsIntf;
 
   private String urlPrefix;
 
   private String resourceUri;
 
   // private String content;
 
   protected XmlEmit xml;
 
   /** Called at each request
    */
   public abstract void init();
 
   private SimpleDateFormat httpDateFormatter =
       new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss ");
 
   /**
    * @param req
    * @param resp
    * @throws WebdavException
    */
   public abstract void doMethod(HttpServletRequest req,
                                 HttpServletResponse resp)
         throws WebdavException;
 
   /** Allow servelt to create method.
    */
   public static class MethodInfo {
     private Class methodClass;
 
     private boolean requiresAuth;
 
     /**
      * @param methodClass
      * @param requiresAuth
      */
     public MethodInfo(Class methodClass, boolean requiresAuth) {
       this.methodClass = methodClass;
       this.requiresAuth = requiresAuth;
     }
 
     /**
      * @return Class for this method
      */
     public Class getMethodClass() {
       return methodClass;
     }
 
     /** Called when servicing a request to determine if this method requires
      * authentication. Allows the servlet to reject attempts to change state
      * while unauthenticated.
      *
      * @return boolean true if authentication required.
      */
     public boolean getRequiresAuth() {
       return requiresAuth;
     }
   }
 
   /** Called at each request
    *
    * @param nsIntf
    * @param debug
    * @param dumpContent
    * @throws WebdavException
    */
   public void init(WebdavNsIntf nsIntf,
                    boolean debug,
                    boolean dumpContent) throws WebdavException{
     this.nsIntf = nsIntf;
     this.debug = debug;
     this.dumpContent = dumpContent;
 
 //    config = servlet.getServletConfig();
     urlPrefix = WebdavUtils.getUrlPrefix(nsIntf.getRequest());
     xml = nsIntf.getXmlEmit();
 
     // content = null;
     resourceUri = null;
 
     init();
   }
 
   /** Get namespace interface
    *
    * @return WebdavNsIntf
    */
   public WebdavNsIntf getNsIntf() {
     return nsIntf;
   }
 
   /**
    * @return String url prefix
    */
   public String getUrlPrefix() {
     return urlPrefix;
   }
 
   /** Get the decoded and fixed resource URI
    *
    * @param req      Servlet request object
    * @return String  fixed up uri
    * @throws WebdavException
    */
   public String getResourceUri(HttpServletRequest req)
       throws WebdavException {
     if (resourceUri != null) {
       return resourceUri;
     }
 
     String uri = req.getServletPath();
 
     if ((uri == null) || (uri.length() == 0)) {
       /* No path specified - set it to root. */
       uri = "/";
     }
 
     if (debug) {
       trace("uri: " + uri);
     }
 
     resourceUri = fixPath(uri);
 
     if (debug) {
       trace("resourceUri: " + resourceUri);
     }
 
     return resourceUri;
   }
 
   /** Return a path, beginning with a "/", after "." and ".." are removed.
    * If the parameter path attempts to go above the root we return null.
    *
    * Other than the backslash thing why not use URI?
    *
    * @param path      String path to be fixed
    * @return String   fixed path
    * @throws WebdavException
    */
   protected String fixPath(String path) throws WebdavException {
     if (path == null) {
       return null;
     }
 
     String decoded;
     try {
       decoded = URLDecoder.decode(path, "UTF8");
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
 
     if (decoded == null) {
       return (null);
     }
 
     /** Make any backslashes into forward slashes.
      */
     if (decoded.indexOf('\\') >= 0) {
       decoded = decoded.replace('\\', '/');
     }
 
     /** Ensure a leading '/'
      */
     if (!decoded.startsWith("/")) {
       decoded = "/" + decoded;
     }
 
     /** Remove all instances of '//'.
      */
     while (decoded.indexOf("//") >= 0) {
       decoded = decoded.replaceAll("//", "/");
     }
 
     if (decoded.indexOf("/.") < 0) {
       return decoded;
     }
 
     /** Somewhere we may have /./ or /../
      */
 
     StringTokenizer st = new StringTokenizer(decoded, "/");
 
     ArrayList<String> al = new ArrayList<String>();
     while (st.hasMoreTokens()) {
       String s = st.nextToken();
 
       if (s.equals(".")) {
         // ignore
       } else if (s.equals("..")) {
         // Back up 1
         if (al.size() == 0) {
           // back too far
           return null;
         }
 
         al.remove(al.size() - 1);
       } else {
         al.add(s);
       }
     }
 
     /** Reconstruct */
     StringBuffer sb = new StringBuffer();
     for (String s: al) {
       sb.append('/');
       sb.append(s);
     }
 
     return sb.toString();
   }
 
   protected int defaultDepth(int depth, int def) {
     if (depth < 0) {
       return def;
     }
 
     return depth;
   }
 
   /* depth must have given value
    */
   protected void checkDepth(int depth, int val) throws WebdavException {
     if (depth != val) {
       throw new WebdavBadRequest();
     }
   }
 
   /* depth must be in min-max
    */
   protected void checkDepth(int depth, int min, int max) throws WebdavException {
     if ((depth < min) || (depth > max)) {
       throw new WebdavBadRequest();
     }
   }
 
   protected void addHref(WebdavNsNode node) throws WebdavException {
     try {
       if (debug) {
         trace("Adding href " + getUrlPrefix() + node.getEncodedUri());
       }
 
       String url = getUrlPrefix() + new URI(node.getEncodedUri()).toASCIIString();
       property(WebdavTags.href, url);
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   protected void addStatus(int status) throws WebdavException {
     try {
       property(WebdavTags.status, "HTTP/1.1 " + status + " " +
                WebdavStatusCode.getMessage(status));
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /* * Get request content.
    *
    * @param req      Servlet request object
    * @return String  Body of the request
    * /
   protected String getContent(HttpServletRequest req) throws WebdavException{
     if (content != null) {
       return content;
     }
 
     if (req.getContentLength() == 0) {
       return null;
     }
 
     try {
       String enc = req.getCharacterEncoding();
       if (enc == null) {
         enc = System.getProperty("file.encoding");
       }
 
       // Apparently we can get leading or trailing quotes
       if (enc.startsWith("\"")) {
         enc = enc.substring(1, enc.length());
       }
 
       if (enc.endsWith("\"")) {
         enc = enc.substring(0, enc.length() - 1);
       }
 
       int segLen;
       int totLen = 0;
       StringBuffer sb = new StringBuffer();
       InputStream is = req.getInputStream();
 
       byte[] seg = new byte[4096];  // IBM page size
 
       segLen = is.read(seg);
       while (segLen != -1) {
         String segStr = new String(seg, 0, segLen, enc);
         sb.append(segStr);
 
         segLen = is.read(seg);
       }
 
       content = sb.toString();
 
       if (dumpContent) {
         debugMsg(content);
       }
 
       return content;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }*/
 
   private class DebugReader extends FilterReader {
     StringBuffer sb = new StringBuffer();
 
     /** Constructor
      * @param rdr
      */
     public DebugReader(Reader rdr) {
       super(rdr);
     }
 
     public void close() throws IOException {
       if (sb != null) {
         trace(sb.toString());
       }
 
       super.close();
     }
 
     public int read() throws IOException {
       int c = super.read();
 
       if (c == -1) {
         if (sb != null) {
           trace(sb.toString());
           sb = null;
         }
         return c;
       }
 
       if (sb != null) {
         char ch = (char)c;
         if (ch == '\n') {
           trace(sb.toString());
           sb = new StringBuffer();
         } else {
           sb.append(ch);
         }
       }
 
       return c;
     }
 
     public int read(char[] cbuf, int off, int len) throws IOException {
       int res = super.read(cbuf, off, len);
       if ((res > 0) && (sb != null)) {
         sb.append(cbuf, off, res);
       }
 
       return res;
     }
   }
 
   protected Reader getReader(HttpServletRequest req) throws Throwable {
     if (debug) {
       return new DebugReader(req.getReader());
     }
 
     return req.getReader();
   }
 
   /** Parse the Webdav request body, and return the DOM representation.
    *
    * @param req        Servlet request object
    * @param resp       Servlet response object for bad status
    * @return Document  Parsed body or null for no body
    * @exception WebdavException Some error occurred.
    */
   protected Document parseContent(HttpServletRequest req,
                                   HttpServletResponse resp)
       throws WebdavException{
     if (req.getContentLength() == 0) {
       return null;
     }
 
     Reader rdr = null;
 
     try {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setNamespaceAware(true);
 
       DocumentBuilder builder = factory.newDocumentBuilder();
 
       return builder.parse(new InputSource(getReader(req)));
     } catch (SAXException e) {
       resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       throw new WebdavException(HttpServletResponse.SC_BAD_REQUEST);
     } catch (Throwable t) {
       resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
       throw new WebdavException(t);
     } finally {
       if (rdr != null) {
         try {
           rdr.close();
         } catch (Throwable t) {}
       }
     }
   }
 
   /** See if nore matches tag
    *
    * @param nd
    * @param tag
    * @return boolean true for match
    */
   public static boolean nodeMatches(Node nd, QName tag) {
     return tag.nodeMatches(nd);
   }
 
   protected String formatHTTPDate(Timestamp val) {
     if (val == null) {
       return null;
     }
 
     synchronized (httpDateFormatter) {
       return httpDateFormatter.format(val) + "GMT";
     }
   }
 
   /* ====================================================================
    *                   XmlUtil wrappers
    * ==================================================================== */
 
   protected Element[] getChildren(Node nd) throws WebdavException {
     try {
       return XmlUtil.getElementsArray(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
      throw new WebdavBadRequest(t.getMessage());
     }
   }
 
   protected Element getOnlyChild(Node nd) throws WebdavException {
     try {
       return XmlUtil.getOnlyElement(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
      throw new WebdavBadRequest(t.getMessage());
     }
   }
 
   protected String getElementContent(Element el) throws WebdavException {
     try {
       return XmlUtil.getElementContent(el);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
      throw new WebdavBadRequest(t.getMessage());
     }
   }
 
   /* ====================================================================
    *                   XmlEmit wrappers
    * ==================================================================== */
 
   protected void startEmit(HttpServletResponse resp) throws WebdavException {
     try {
       xml.startEmit(resp.getWriter());
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Add a namespace
    *
    * @param val
    */
   public void addNs(String val) {
     xml.addNs(val);
   }
 
   /** Get a namespace abbreviation
    *
    * @param ns namespace
    * @return String abbrev
    */
   public String getNsAbbrev(String ns) {
     return xml.getNsAbbrev(ns);
   }
 
   protected void openTag(QName tag) throws WebdavException {
     try {
       xml.openTag(tag);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   protected void openTagNoNewline(QName tag) throws WebdavException {
     try {
       xml.openTagNoNewline(tag);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   protected void closeTag(QName tag) throws WebdavException {
     try {
       xml.closeTag(tag);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Emit an empty tag
    *
    * @param tag
    * @throws WebdavException
    */
   public void emptyTag(QName tag) throws WebdavException {
     try {
       xml.emptyTag(tag);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Emit a property
    *
    * @param tag
    * @param val
    * @throws WebdavException
    */
   public void property(QName tag, String val) throws WebdavException {
     try {
       xml.property(tag, val);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Emit a property
    *
    * @param tag
    * @param val
    * @throws WebdavException
    */
   public void property(QName tag, Reader val) throws WebdavException {
     try {
       xml.property(tag, val);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** Emit a property
   *
    * @param tag
    * @param tagVal
    * @throws WebdavException
    */
   public void propertyTagVal(QName tag, QName tagVal) throws WebdavException {
     try {
       xml.propertyTagVal(tag, tagVal);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   protected void flush() throws WebdavException {
     try {
       xml.flush();
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /** ===================================================================
    *                   Logging methods
    *  =================================================================== */
 
   /**
    * @return Logger
    */
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void error(String msg) {
     getLogger().error(msg);
   }
 
   protected void logIt(String msg) {
     getLogger().info(msg);
   }
 
   protected void trace(String msg) {
     getLogger().debug(msg);
   }
 }
 
