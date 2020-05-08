 /******************************************************************************
  * ResponderCompile.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.servlets.responders;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpUtils;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.openlaszlo.cm.CompilationManager;
 import org.openlaszlo.compiler.Canvas;
 import org.openlaszlo.compiler.CompilationError;
 import org.openlaszlo.compiler.CompilationEnvironment;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.utils.ContentEncoding;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.utils.LZHttpUtils;
 import org.openlaszlo.utils.LZGetMethod;
 import org.openlaszlo.utils.StringUtils;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.servlets.LZBindingListener;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.log4j.Logger;
 
 
 
 public abstract class ResponderCompile extends Responder
 {
     protected static CompilationManager mCompMgr = null;
     protected static ScriptCompiler mScriptCache = null;
 
     private static boolean mIsInitialized = false;
     private static boolean mAllowRequestSOURCE = true;
     private static boolean mAllowRecompile = true;
     private static boolean mCheckModifiedSince = true;
     private static String mAdminPassword = null;
     private static Logger  mLogger = Logger.getLogger(ResponderCompile.class);
 
     /** @param filename path of the actual file to be compiled -- happens when
      * we're compiling JSPs. */
     abstract protected void respondImpl(String fileName, HttpServletRequest req,
                                         HttpServletResponse res)
         throws IOException;
 
 
     synchronized public void init(String reqName, ServletConfig config, Properties prop)
         throws ServletException, IOException
     {
         super.init(reqName, config, prop);
 
         // All compilation classes share cache directory and compilation manager.
         if (! mIsInitialized) {
 
             mAllowRequestSOURCE =
                 prop.getProperty("allowRequestSOURCE", "true").intern() == "true";
             mCheckModifiedSince =
                 prop.getProperty("checkModifiedSince", "true").intern() == "true";
             mAllowRecompile =
                 prop.getProperty("allowRecompile", "true").intern() == "true";
 
             mAdminPassword = prop.getProperty("adminPassword", null);
 
             // Initialize the Compilation Cache
             String cacheDir = config.getInitParameter("lps.cache.directory");
             if (cacheDir == null) {
                 cacheDir = prop.getProperty("cache.directory");
             }
             if (cacheDir == null) {
                 cacheDir = LPS.getWorkDirectory() + File.separator + "cache";
             }
 
             File cache = checkDirectory(cacheDir);
             mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="application cache is at " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-95", new Object[] {cacheDir})
 );
 
             if (mCompMgr == null) {
                 mCompMgr = new CompilationManager(null, cache, prop);
             }
 
             if (mScriptCache == null) {
                 String scacheDir = config.getInitParameter("lps.scache.directory");
                 if (scacheDir == null) {
                     scacheDir = prop.getProperty("scache.directory");
                 }
                 if (scacheDir == null) {
                     scacheDir = LPS.getWorkDirectory() + File.separator + "scache";
                 }
                 File scache = checkDirectory(scacheDir);
                 mScriptCache = ScriptCompiler.initScriptCompilerCache(scache, prop);
             }
 
             String cmOption = prop.getProperty("compMgrDependencyOption");
             if (cmOption!=null) {
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Setting cm option to \"" + p[0] + "\""
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-122", new Object[] {cmOption})
 );
                 mCompMgr.setProperty("recompile", cmOption);
             }
         }
     }
 
 
     /**
      * This method should be called by subclasses in respondImpl(req, res);
      *
      */
     protected final void respondImpl(HttpServletRequest req, HttpServletResponse res)
         throws IOException
     {
         String fileName = LZHttpUtils.getRealPath(mContext, req);
 
         String lzt  = req.getParameter("lzt");
 
         // FIXME: [2003-01-14 bloch] this needs to be rethought out for real!!
         // Is this the right logic for deciding when to do preprocessing?
         File file = new File(fileName);
         if ( ! file.canRead() ) {
 
             File base = new File(FileUtils.getBase(fileName));
 
             if (base.canRead() && base.isFile()) {
                 String tempFileName = doPreProcessing(req, fileName);
                 if (tempFileName != null) {
                     fileName = tempFileName;
                 }
             }
         }
 
         if (! new File(fileName).exists()) {
             boolean isOpt = fileName.endsWith(".lzo");
             boolean lzogz = new File(fileName+".gz").exists();
             if (!(isOpt && lzogz)) {
                 boolean hasFb = req.getParameter("fb") != null;
                 if (!(isOpt && hasFb)) {
                     res.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI() + " not found");
                     mLogger.info(req.getRequestURI() + " not found");
                     return;
                 } else {
                     fileName = fileName.substring(0, fileName.length()-1) + 'x';
                 }
             }
         }
 
         try {
             /* If it's a krank instrumented file then we always
              * fall through and call the code to deliver the
              * .swf to the client, regardless of the object
              * file modification time, because that code path
              * is where we launch the serialization
              * port-listener thread.
              */
             if (mCheckModifiedSince) {
                 long lastModified = getLastModified(fileName, req);
                 // Round to the nearest second.
                 lastModified = ((lastModified + 500L)/1000L) * 1000L;
                 if (notModified(lastModified, req, res)) {
                     return;
                 }
             }
 
             respondImpl(fileName, req, res);
 
             // Check that global config knows about app security options
             String path = req.getServletPath();
             if (LPS.configuration.getApplicationOptions(path) == null) {
                 Canvas canvas = getCanvas(fileName, req);
                 LPS.configuration.setApplicationOptions(path, canvas.getSecurityOptions());
             }
 
         } catch (CompilationError e) {
             handleCompilationError(e, req, res);
         }
     }
 
     protected void handleCompilationError(CompilationError e,
                                           HttpServletRequest req,
                                           HttpServletResponse res)
         throws IOException
     {
         throw e;
     }
 
     /**
      * @return File name for temporary LZX file that is the
      *         result of this http pre-processing; null for a bad request
      * @param req request
      * @param fileName file name associated with request
      */
     private String doPreProcessing(HttpServletRequest req, String fileName)
         throws IOException
     {
         // Do an http request for this and see what we get back.
         //
         StringBuffer s = HttpUtils.getRequestURL(req);
         int len = s.length();
         // Remove the .lzx from the end of the URL
         if (len <= 4) {
             return null;
         }
         s.delete(len-4, len);
 
         // FIXME [2002-12-15 bloch] does any/all of this need to be synchronized on session?
 
         // First get the temporary file name for this session
         HttpSession        session = req.getSession();
         String             sid = session.getId();
         String             tempFileName = getTempFileName(fileName, sid);
         File               tempFile = new File(tempFileName);
 
         tempFile.deleteOnExit();
 
         // Now pre-process the request and copy the data to
         // the temporary file that is unique to this session
 
         // FIXME: query string processing
 
         String surl = s.toString();
 
         URL url = new URL(surl);
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Preprocessing request at " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-263", new Object[] {surl})
 );
         GetMethod getRequest = new LZGetMethod();
         getRequest.setPath(url.getPath());
         //getRequest.setQueryString(url.getQuery());
         getRequest.setQueryString(req.getQueryString());
 
         // Copy headers to request
         LZHttpUtils.proxyRequestHeaders(req, getRequest);
         // Mention the last modified time, if the file exists
         if (tempFile.exists()) {
             long  lastModified = tempFile.lastModified();
             getRequest.addRequestHeader("If-Modified-Since",
                         LZHttpUtils.getDateString(lastModified));
         } else {
             // Otherwise, create a listener that will clean up the tempfile
             // Note: web server administrators must make sure that their servers are killed
             // gracefully or temporary files will not be handled by the LZBindingListener.
             // Add a binding listener for this session that
             // will remove our temporary files
             LZBindingListener listener = (LZBindingListener)session.getAttribute("tmpl");
             if (listener == null) {
                 listener = new LZBindingListener(tempFileName);
                 session.setAttribute("tmpl", listener);
             } else {
                 listener.addTempFile(tempFileName);
             }
         }
 
         HostConfiguration hostConfig = new HostConfiguration();
         hostConfig.setHost(url.getHost(),url.getPort(),url.getProtocol());
  
 
         HttpClient htc = new HttpClient();
         htc.setHostConfiguration(hostConfig);
 
         int rc = htc.executeMethod(getRequest);
         mLogger.debug("Response Status: " + rc);
         if (rc >= 400) {
             // respondWithError(req, res, "HTTP Status code: " + rc + " for url " + surl, rc);
             return null;
         } if (rc != HttpServletResponse.SC_NOT_MODIFIED) {
             FileOutputStream output = new FileOutputStream(tempFile);
             try {
                 // FIXME:[2002-12-17 bloch] verify that the response body is XML
                 FileUtils.sendToStream(getRequest.getResponseBodyAsStream(), output);
                 // TODO: [2002-12-15 bloch] What to do with response headers?
             } catch (FileUtils.StreamWritingException e) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="StreamWritingException while sending error: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-313", new Object[] {e.getMessage()})
 );
             } finally {
                 FileUtils.close(output);
             }
         }
 
         return tempFileName;
     }
 
     /**
      * @return Name of temporary cached version of this file
      * for this session.
      */
     private String getTempFileName(String fileName, String sid)
     {
         String webappPath = LZHttpUtils.getRealPath(mContext, "/");
         File source = mCompMgr.getCacheSourcePath(fileName, webappPath);
         File cacheDir = source.getParentFile();
         if (cacheDir != null) {
             cacheDir.mkdirs();
         }
         String sourcePath = source.getAbsolutePath();
         StringBuffer buf = new StringBuffer(sourcePath);
         int index = sourcePath.lastIndexOf(File.separator);
         buf.insert(index + 1, "lzf-" + sid + "-");
         return buf.toString();
     }
 
     /**
      * Process if-modified-since req header and stick last-modified
      * response header there.
      */
     private boolean notModified(long lastModified, HttpServletRequest req,
                                 HttpServletResponse res)
         throws IOException
     {
         if (lastModified != 0) {
 
             String lms = LZHttpUtils.getDateString(lastModified);
 
             mLogger.debug("Last-Modified: " + lms);
 
             // Check last-modified and if-modified-since dates
             String ims = req.getHeader(LZHttpUtils.IF_MODIFIED_SINCE);
             long ifModifiedSince = LZHttpUtils.getDate(ims);
 
             if (ifModifiedSince != -1) {
 
                 mLogger.debug("If-Modified-Since: " + ims);
 
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="modsince " + p[0] + " lastmod " + p[1]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-370", new Object[] {new Long(ifModifiedSince), new Long(lastModified)})
                                 );
                 if (lastModified <= ifModifiedSince) {
                     res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                     mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Responding with NOT_MODIFIED"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-379")
 );
                     return true;
                 }
             }
 
             res.setHeader(LZHttpUtils.LAST_MODIFIED, lms);
         }
 
         return false;
     }
 
     /**
      * Return the last modified time for this source file (and its dependencies)
      *
      * @param fileName name of file to compile
      * @return the canvas
      */
     protected long getLastModified(String fileName, HttpServletRequest req)
         throws CompilationError, IOException
     {
         Properties props = initCMgrProperties(req);
         return mCompMgr.getLastModified(fileName, props);
     }
 
     /**
      * Sets up various parameters for the CompilationManager, using
      * data from the HttpServletRequest
      *
      * @param req
      *
      *
      * Looks for these properties:
      * <br>
      * <ul>
      * <li> "debug"
      * <li> "logdebug"
      * <li> "lzbacktrace"
      * <li> "profile"
      * <li> "sourcelocators"
      * <li> "lzr" (swf version := swf5 | swf6)
      * <li> "lzproxied" true|false
      * <li> "lzscript" true|false   -- emit javascript, not object file
      * <li> "lzconsoledebug" use remote debug protocol
      * <li> "cssfile"
      * <ul>
      * also grabs the request URL.
      */
     static protected Properties initCMgrProperties(HttpServletRequest req) {
 
         Properties props = new Properties();
 
         // Look for "runtime=..." flag
         String runtime = req.getParameter("lzr");
         if (runtime == null) {
             runtime = LPS.getProperty("compiler.runtime.default", "swf6");
         }
         props.setProperty(CompilationEnvironment.RUNTIME_PROPERTY, runtime);
 
         // TODO: [2003-04-11 pkang] the right way to do this is to have a
         // separate property to see if this allows debug.
         if (mAllowRequestSOURCE) {
             // Look for "logdebug=true" flag
             props.setProperty(CompilationEnvironment.LOGDEBUG_PROPERTY, "false");
             String logdebug = req.getParameter(CompilationEnvironment.LOGDEBUG_PROPERTY);
             if (logdebug != null) {
                 props.setProperty(CompilationEnvironment.LOGDEBUG_PROPERTY, logdebug);
             }
 
             // Look for "lzconsoledebug=true" flag
             props.setProperty(CompilationEnvironment.CONSOLEDEBUG_PROPERTY, "false");
             String lzconsoledebug = req.getParameter(CompilationEnvironment.CONSOLEDEBUG_PROPERTY);
             if (lzconsoledebug != null) {
                 props.setProperty(CompilationEnvironment.CONSOLEDEBUG_PROPERTY, lzconsoledebug);
             }
 
 
             // Look for "debug=true" flag
             props.setProperty(CompilationEnvironment.DEBUG_PROPERTY, "false");
             String debug = req.getParameter(CompilationEnvironment.DEBUG_PROPERTY);
             if (debug != null) {
                 props.setProperty(CompilationEnvironment.DEBUG_PROPERTY, debug);
             }
 
             // Look for "sourcelocators=true" flag
             props.setProperty(CompilationEnvironment.SOURCELOCATOR_PROPERTY, "false");
             String sl = req.getParameter(CompilationEnvironment.SOURCELOCATOR_PROPERTY);
             if (sl != null) {
                 props.setProperty(CompilationEnvironment.SOURCELOCATOR_PROPERTY, sl);
             }
 
             // Look for "profile=true" flag
             props.setProperty(CompilationEnvironment.PROFILE_PROPERTY, "false");
             String profile = req.getParameter(CompilationEnvironment.PROFILE_PROPERTY);
             if (profile != null) {
                 props.setProperty(CompilationEnvironment.PROFILE_PROPERTY, profile);
             }
 
             // Look for "lzbacktrace=true" flag
             props.setProperty(CompilationEnvironment.BACKTRACE_PROPERTY, "false");
             String backtrace = req.getParameter("lzbacktrace");
             if (backtrace != null) {
                 props.setProperty(CompilationEnvironment.BACKTRACE_PROPERTY, backtrace);
             }
             
            // Look for "hdebug=true" flag
            // This flag is a shorthand for turning on various props needed by Harpoon debugging
            props.setProperty(CompilationEnvironment.HDEBUG_PROPERTY, "false");
            String hdebug = req.getParameter(CompilationEnvironment.HDEBUG_PROPERTY);
            if (hdebug != null) {
                props.setProperty(CompilationEnvironment.HDEBUG_PROPERTY, hdebug);
                props.setProperty(CompilationEnvironment.NAME_FUNCTIONS, hdebug);
                props.setProperty(CompilationEnvironment.TRACK_LINES, hdebug); 
                props.setProperty(CompilationEnvironment.BACKTRACE_PROPERTY, hdebug); 
             }
 
         }
 
         // Set the 'lzproxied' default = false
         String proxied = req.getParameter(CompilationEnvironment.PROXIED_PROPERTY);
         if (proxied != null) {
             props.setProperty(CompilationEnvironment.PROXIED_PROPERTY, proxied);
         }
 
         String lzs = req.getParameter("lzscript");
         if (lzs != null) {
             props.setProperty("lzscript", lzs);
         }
 
         if (mAllowRecompile) {
             String recompile = req.getParameter(CompilationManager.RECOMPILE);
             if (recompile != null) {
                 // Check for passwd if required.
                 String pwd = req.getParameter("pwd");
                 if ( mAdminPassword == null || 
                         (pwd != null && pwd.equals(mAdminPassword)) ) {
                     props.setProperty(CompilationManager.RECOMPILE, "true");
                 } else {
                     mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="lzrecompile attempted but not allowed"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ResponderCompile.class.getName(),"051018-523")
 );
                 }
             }
         }
 
         return props;
     }
 
 
     /**
      * Return the canvas for the given LZX file.
      *
      * @param fileName pathname of file
      * @return the canvas
      */
     protected Canvas getCanvas(String fileName, HttpServletRequest req)
         throws CompilationError, IOException
     {
         Properties props = initCMgrProperties(req);
         return mCompMgr.getCanvas(fileName, props);
     }
 
     public static CompilationManager getCompilationManager()
     {
         return mCompMgr;
     }
 }
