 /*
  * Copyright 2005 Joe Walker
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.directwebremoting.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.directwebremoting.Calls;
 import org.directwebremoting.DebugPageGenerator;
 import org.directwebremoting.DwrConstants;
 import org.directwebremoting.Remoter;
 import org.directwebremoting.Replies;
 import org.directwebremoting.dwrp.HtmlCallMarshaller;
 import org.directwebremoting.dwrp.PlainCallMarshaller;
 import org.directwebremoting.dwrp.PollHandler;
 import org.directwebremoting.util.Continuation;
 import org.directwebremoting.util.IdGenerator;
 import org.directwebremoting.util.JavascriptUtil;
 import org.directwebremoting.util.LocalUtil;
 import org.directwebremoting.util.Logger;
 import org.directwebremoting.util.MimeConstants;
 
 /**
  * This is the main servlet that handles all the requests to DWR.
  * <p>It is on the large side because it can't use technologies like JSPs etc
  * since it all needs to be deployed in a single jar file, and while it might be
  * possible to integrate Velocity or similar I think simplicity is more
  * important, and there are only 2 real pages both script heavy in this servlet
  * anyway.</p>
  * <p>There are 5 things to do, in the order that you come across them:</p>
  * <ul>
  * <li>The index test page that points at the classes</li>
  * <li>The class test page that lets you execute methods</li>
  * <li>The interface javascript that uses the engine to send requests</li>
  * <li>The engine javascript to form the iframe request and process replies</li>
  * <li>The exec 'page' that executes the method and returns data to the iframe</li>
  * </ul>
  * @author Joe Walker [joe at getahead dot ltd dot uk]
  */
 public class UrlProcessor
 {
     /**
      * Handle servlet requests aimed at DWR
      * @param request The servlet request
      * @param response The servlet response
      * @throws IOException If there are IO issues
      */
     public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         try
         {
             String pathInfo = request.getPathInfo();
             String servletPath = request.getServletPath();
             String contextPath = request.getContextPath();
 
             if (nullPathInfoWorkaround && pathInfo == null)
             {
                 pathInfo = request.getServletPath();
                 servletPath = PathConstants.PATH_ROOT;
                 log.debug("Default servlet suspected. pathInfo=" + pathInfo + "; contextPath=" + contextPath + "; servletPath=" + servletPath);
             }
 
             if (pathInfo == null || pathInfo.length() == 0 || pathInfo.equals(PathConstants.PATH_ROOT))
             {
                 response.sendRedirect(contextPath + servletPath + PathConstants.FILE_INDEX);
             }
             else if (pathInfo.startsWith(PathConstants.FILE_INDEX))
             {
                 String page = debugPageGenerator.generateIndexPage(contextPath + servletPath);
 
                 response.setContentType(MimeConstants.MIME_HTML);
                 PrintWriter out = response.getWriter();
                 out.print(page);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_TEST))
             {
                 String scriptName = pathInfo;
                 scriptName = LocalUtil.replace(scriptName, PathConstants.PATH_TEST, "");
                 scriptName = LocalUtil.replace(scriptName, PathConstants.PATH_ROOT, "");
 
                 String page = debugPageGenerator.generateTestPage(contextPath + servletPath, scriptName);
 
                 response.setContentType(MimeConstants.MIME_HTML);
                 PrintWriter out = response.getWriter();
                 out.print(page);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_INTERFACE))
             {
                 String scriptName = pathInfo;
                 scriptName = LocalUtil.replace(scriptName, PathConstants.PATH_INTERFACE, "");
                 scriptName = LocalUtil.replace(scriptName, PathConstants.EXTENSION_JS, "");
                 String path = contextPath + servletPath;
 
                 String script = remoter.generateInterfaceScript(scriptName, path);
 
                 // Officially we should use MimeConstants.MIME_JS, but if we cheat and
                 // use MimeConstants.MIME_PLAIN then it will be easier to read in a
                 // browser window, and will still work just fine.
                 response.setContentType(MimeConstants.MIME_PLAIN);
                 PrintWriter out = response.getWriter();
                 out.print(script);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_PLAIN_POLL))
             {
                 pollHandler.doPoll(request, response, true);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_HTML_POLL))
             {
                 pollHandler.doPoll(request, response, false);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_PLAIN_CALL))
             {
                 Calls calls = plainCallMarshaller.marshallInbound(request, response);
                 Replies replies = remoter.execute(calls);
                 plainCallMarshaller.marshallOutbound(replies, request, response);
             }
             else if (pathInfo.startsWith(PathConstants.PATH_HTML_CALL))
             {
                 Calls calls = htmlCallMarshaller.marshallInbound(request, response);
                 Replies replies = remoter.execute(calls);
                 htmlCallMarshaller.marshallOutbound(replies, request, response);
             }
             else if (pathInfo.equals(PathConstants.FILE_ENGINE))
             {
                 doFile(request, response, PathConstants.FILE_ENGINE, MimeConstants.MIME_JS, true);
             }
             else if (pathInfo.equals(PathConstants.FILE_UTIL))
             {
                 doFile(request, response, PathConstants.FILE_UTIL, MimeConstants.MIME_JS, false);
             }
             else if (pathInfo.equals(PathConstants.WW_FILE_UTIL))
             {
                 doFile(request, response, PathConstants.WW_FILE_UTIL, MimeConstants.MIME_JS, false);
             }
             else
             {
                 doNotFound(request, response, pathInfo);
             }
         }
         catch (Exception ex)
         {
             handleException(request, response, ex);
         }
     }
 
     /**
      * Handles an exception occuring during the request disptaching.
      * @param request The request from the browser
      * @param response The response channel
      * @param cause The occurred exception
      * @throws IOException If writing to the output fails
      */
     protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception cause) throws IOException
     {
         // Allow Jetty RequestRetry exception to propogate to container
         Continuation.rethrowIfContinuation(cause);
 
         log.warn("Error: " + cause);
         if (cause instanceof SecurityException && log.isDebugEnabled())
         {
             log.debug("- User Agent: " + request.getHeader(HttpConstants.HEADER_USER_AGENT));
             log.debug("- Remote IP:  " + request.getRemoteAddr());
             log.debug("- Request URL:" + request.getRequestURL());
             log.debug("- Query:      " + request.getQueryString());
             log.debug("- Method:     " + request.getMethod());
         }
 
         // We are going to act on this in engine.js so we are hoping that
         // that SC_NOT_IMPLEMENTED (501) is not something that the servers
         // use that much. I would have used something unassigned like 506+
         // But that could cause future problems and might not get through
         // proxies and the like
         response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
         response.setContentType(MimeConstants.MIME_HTML);
         PrintWriter out = response.getWriter();
         out.println(cause.getMessage());
 
         log.warn("Sent 501", cause);
     }
     
     /**
      * Display a 404 "not found" message
      * @param request The request from the browser
      * @param response The response channel
      * @param pathInfo The requested URL
      * @throws IOException If writing to the output fails
      */
     protected void doNotFound(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException
     {
         log.warn("Page not found. pathInfo='" + pathInfo + "' requestUrl='" + request.getRequestURI() + "'");
         log.warn("In debug/test mode try viewing /[WEB-APP]/dwr/");
 
         response.sendError(HttpServletResponse.SC_NOT_FOUND);
     }
 
     /**
      * Basically a file servlet component that does some <b>very limitted</b>
      * EL type processing on the file. See the source for the cheat.
      * @param request The request from the browser
      * @param response The response channel
      * @param path The path to search for, process and output
      * @param mimeType The mime type to use for this output file
      * @param dynamic Should the script be recalculated each time?
      * @throws IOException If writing to the output fails
      */
     protected void doFile(HttpServletRequest request, HttpServletResponse response, String path, String mimeType, boolean dynamic) throws IOException
     {
         if (dynamic)
         {
             response.setHeader("pragma", "public");
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
         }
 
         if (!dynamic && isUpToDate(request, path))
         {
             response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
             return;
         }
 
         String output;
 
         synchronized (scriptCache)
         {
             output = (String) scriptCache.get(path);
             if (output == null)
             {
                 StringBuffer buffer = new StringBuffer();
 
                 String resource = DwrConstants.PACKAGE + path;
                 InputStream raw = getClass().getResourceAsStream(resource);
                 if (raw == null)
                 {
                     throw new IOException("Failed to find resource: " + resource);
                 }
 
                 BufferedReader in = new BufferedReader(new InputStreamReader(raw));
                 while (true)
                 {
                     String line = in.readLine();
                     if (line == null)
                     {
                         break;
                     }
 
                     if (dynamic)
                     {
                        if (line.indexOf(PARAM_HTTP_SESSIONID) != -1)
                        {
                            line = LocalUtil.replace(line, PARAM_HTTP_SESSIONID, request.getSession(true).getId());
                        }

                         if (line.indexOf(PARAM_SCRIPT_SESSIONID) != -1)
                         {
                             line = LocalUtil.replace(line, PARAM_SCRIPT_SESSIONID, generator.generateId(pageIdLength));
                         }
                     }
 
                     buffer.append(line);
                     buffer.append('\n');
                 }
 
                 output = buffer.toString();
 
                 if (mimeType.equals(MimeConstants.MIME_JS) && scriptCompressed)
                 {
                     output = JavascriptUtil.compress(output, compressionLevel);
                 }
 
                 if (!dynamic)
                 {
                     scriptCache.put(path, output);
                 }
             }
         }
 
         response.setContentType(mimeType);
         response.setDateHeader(HttpConstants.HEADER_LAST_MODIFIED, servletContainerStartTime);
         response.setHeader(HttpConstants.HEADER_ETAG, etag);
 
         PrintWriter out = response.getWriter();
         out.println(output);
     }
 
     /**
      * Do we need to send the conent for this file
      * @param req The HTTP request
      * @param path The file path (for debug purposes)
      * @return true iff the ETags and If-Modified-Since headers say we have not changed
      */
     private boolean isUpToDate(HttpServletRequest req, String path)
     {
         if (ignoreLastModified)
         {
             return false;
         }
 
         long modifiedSince = -1;
         try
         {
             // HACK: Webfear appears to get confused sometimes
             modifiedSince = req.getDateHeader(HttpConstants.HEADER_IF_MODIFIED);
         }
         catch (RuntimeException ex)
         {
             log.warn("Websphere/RAD date failure. If you understand why this might happen please report to dwr-users mailing list");
         }
 
         if (modifiedSince != -1)
         {
             // Browsers are only accurate to the second
             modifiedSince -= modifiedSince % 1000;
         }
         String givenEtag = req.getHeader(HttpConstants.HEADER_IF_NONE);
 
         // Deal with missing etags
         if (givenEtag == null)
         {
             // There is no ETag, just go with If-Modified-Since
             if (modifiedSince > servletContainerStartTime)
             {
                 if (log.isDebugEnabled())
                 {
                     log.debug("Sending 304 for " + path + " If-Modified-Since=" + modifiedSince + ", Last-Modified=" + servletContainerStartTime);
                 }
                 return true;
             }
 
             // There are no modified setttings, carry on
             return false;
         }
 
         // Deal with missing If-Modified-Since
         if (modifiedSince == -1)
         {
             if (!etag.equals(givenEtag))
             {
                 // There is an ETag, but no If-Modified-Since
                 if (log.isDebugEnabled())
                 {
                     log.debug("Sending 304 for " + path + " Old ETag=" + givenEtag + ", New ETag=" + etag);
                 }
                 return true;
             }
 
             // There are no modified setttings, carry on
             return false;
         }
 
         // Do both values indicate that we are in-date?
         if (etag.equals(givenEtag) && modifiedSince <= servletContainerStartTime)
         {
             if (log.isDebugEnabled())
             {
                 log.debug("Sending 304 for " + path);
             }
             return true;
         }
 
         return false;
     }
 
     /**
      * @param ignoreLastModified The ignoreLastModified to set.
      */
     public void setIgnoreLastModified(boolean ignoreLastModified)
     {
         this.ignoreLastModified = ignoreLastModified;
     }
 
     /**
      * To what level do we compress scripts?
      * @param scriptCompressed The scriptCompressed to set.
      */
     public void setScriptCompressed(boolean scriptCompressed)
     {
         this.scriptCompressed = scriptCompressed;
     }
 
     /**
      * @param compressionLevel The compressionLevel to set.
      */
     public void setCompressionLevel(int compressionLevel)
     {
         this.compressionLevel = compressionLevel;
     }
 
     /**
      * Setter for the remoter
      * @param remoter
      */
     public void setRemoter(Remoter remoter)
     {
         this.remoter = remoter;
     }
 
     /**
      * Setter for the debug page generator
      * @param debugPageGenerator
      */
     public void setDebugPageGenerator(DebugPageGenerator debugPageGenerator)
     {
         this.debugPageGenerator = debugPageGenerator;
     }
 
     /**
      * Setter for the Plain Javascript Marshaller
      * @param marshaller The new marshaller
      */
     public void setPlainCallMarshaller(PlainCallMarshaller marshaller)
     {
         this.plainCallMarshaller = marshaller;
     }
 
     /**
      * Setter for the HTML Javascript Marshaller
      * @param marshaller The new marshaller
      */
     public void setHtmlCallMarshaller(HtmlCallMarshaller marshaller)
     {
         this.htmlCallMarshaller = marshaller;
     }
 
     /**
      * Setter for Poll Handler
      * @param marshaller The new marshaller
      */
     public void setPollMarshaller(PollHandler marshaller)
     {
         this.pollHandler = marshaller;
     }
 
     /**
      * Do we use our hack for when pathInfo is null?
      * @param nullPathInfoWorkaround The nullPathInfoWorkaround to set.
      */
     public void setNullPathInfoWorkaround(boolean nullPathInfoWorkaround)
     {
         this.nullPathInfoWorkaround = nullPathInfoWorkaround;
     }
 
     /**
      * The time on the script files
      */
     private static final long servletContainerStartTime;
 
     /**
      * The etag (=time for us) on the script files
      */
     private static final String etag;
 
     /**
      * Initialize the container start time
      */
     static
     {
         // Browsers are only accurate to the second
         long now = System.currentTimeMillis();
         servletContainerStartTime = now - (now % 1000);
 
         etag = "\"" + servletContainerStartTime + '\"';
     }
 
     /**
      * Do we use our hack for when pathInfo is null?
      * Enabling this will require you to have a / on the end of the DWR root URL
      */
     protected boolean nullPathInfoWorkaround = false;
 
     /**
      * The page id length
      */
     protected int pageIdLength = 16;
 
     /**
      * The method by which we get new page ids
      */
     protected IdGenerator generator = new IdGenerator();
 
     /**
      * Do we ignore all the Last-Modified/ETags blathering?
      */
     protected boolean ignoreLastModified = false;
 
     /**
      * How much do we compression javascript by?
      */
     protected int compressionLevel = JavascriptUtil.LEVEL_DEBUGGABLE;
 
     /**
      * Do we retain comments and unneeded spaces in Javascript code?
      */
     protected boolean scriptCompressed = false;
 
     /**
      * We cache the script output for speed
      */
     protected final Map scriptCache = new HashMap();
 
     /**
      * The bean to handle debug page requests
      */
     protected DebugPageGenerator debugPageGenerator = null;
 
     /**
      * The 'HTML Javascript' method by which objects are marshalled
      */
     protected PlainCallMarshaller plainCallMarshaller = null;
 
     /**
      * The 'Plain Javascript' method by which objects are marshalled
      */
     protected HtmlCallMarshaller htmlCallMarshaller = null;
 
     /**
      * The method by which poll requests are handled
      */
     protected PollHandler pollHandler = null;
 
     /**
      * The bean to execute remote requests and generate interfaces
      */
     protected Remoter remoter = null;
 
     /**
     * The session id parameter that goes in engine.js
     */
    protected static final String PARAM_HTTP_SESSIONID = "${httpSessionId}";

    /**
      * The page id parameter that goes in engine.js
      */
     protected static final String PARAM_SCRIPT_SESSIONID = "${scriptSessionId}";
 
     /**
      * The log stream
      */
     protected static final Logger log = Logger.getLogger(UrlProcessor.class);
 }
