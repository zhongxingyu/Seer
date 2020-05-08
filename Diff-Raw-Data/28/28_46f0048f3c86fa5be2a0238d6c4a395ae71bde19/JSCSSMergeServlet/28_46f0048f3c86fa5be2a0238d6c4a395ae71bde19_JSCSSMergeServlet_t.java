 /*
  * Copyright 2010 Rajendra Patil 
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  
  */
 package com.googlecode.webutilities.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.*;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import static com.googlecode.webutilities.common.Constants.*;
 
 import com.googlecode.webutilities.util.Utils;
 
 /**
  * The <code>JSCSSMergeServet</code> is the Http Servlet to combine multiple JS or CSS static resources in one HTTP request.
  * using YUICompressor.
  * <p>
  * Using <code>JSCSSMergeServet</code> the multiple JS or CSS resources can grouped together (by adding comma) in one HTTP call.
  * </p>
  * <h3>Usage</h3>
  * <p>
  * Put the <b>webutilities-x.y.z.jar</b> in your classpath (WEB-INF/lib folder of your webapp).
  * </p>
  * <p>
  * Declare this servlet in your <code>web.xml</code> ( web descriptor file)
  * </p>
  * <pre>
  * ...
  * &lt;servlet&gt;
  * 	&lt;servlet-name&gt;JSCSSMergeServet&lt;/servlet-name&gt;</b>
  * 	&lt;servlet-class&gt;<b>com.googlecode.webutilities.JSCSSMergeServet</b>&lt;/servlet-class&gt;
  * 	&lt;!-- This init param is optional and default value is minutes for 7 days in future. To expire in the past use negative value. --&gt;
  * 	&lt;init-param&gt;
  * 		&lt;param-name&gt;expiresMinutes&lt;/param-name&gt;
  * 		&lt;param-value&gt;7200&lt;/param-value&gt; &lt;!-- 5 days --&gt;
  * 	&lt;/init-param&gt;
  * 	&lt;!-- This init param is also optional and default value is true. Set it false to override. --&gt;
  * 	&lt;init-param&gt;
  * 		&lt;param-name&gt;useCache&lt;/param-name&gt;
  * 		&lt;param-value&gt;false&lt;/param-value&gt;
  * 	&lt;/init-param&gt;
  *  &lt;/servlet&gt;
  * ...
  * </pre>
  * Map this servlet to serve your JS and CSS resources
  * <pre>
  * ...
  * &lt;servlet-mapping&gt;
  *   &lt;servlet-name&gt;JSCSSMergeServet&lt;/servlet-name&gt;
  *   &lt;url-pattern&gt;<b>*.js</b>&lt;/url-pattern&gt;
  *   &lt;url-pattern&gt;<b>*.json</b>&lt;/url-pattern&gt;
  *   &lt;url-pattern&gt;<b>*.css</b>&lt;/url-pattern&gt;
  * &lt;/servlet-mapping>
  * ...
  * </pre>
  * <p>
  * In your web pages (HTML or JSP files) combine your multiple JS or CSS in one request as shown below.
  * </p>
  * <p>To serve multiple JS files through one HTTP request</p>
  * <pre>
  * &lt;script language="JavaScript" src="<b>/myapp/js/prototype,controls,dragdrop,myapp.js</b>"&gt;&lt;/script&gt;
  * </pre>
  * <p>To serve multiple CSS files through one HTTP request</p>
  * <pre>
  * &lt;link rel="StyleSheet" href="<b>/myapp/css/common,calendar,aquaskin.css</b>"/&gt;
  * </pre>
  * <p>
  * Also if you wanted to serve them minified all together then you can add <code>YUIMinFilter</code> on them. See <code>YUIMinFilter</code> from <code>webutilities.jar</code> for details.
  * </p>
  * <h3>Init Parameters</h3>
  * <p>
  * Both init parameters are optional.
  * </p>
  * <p>
  * <b>expiresMinutes</b> has default value of 7 days. This value is relative from current time. Use negative value to expire early in the past.
  * Ideally you should never be using negative value otherwise you won't be able to <b>take advantage of browser caching for static resources</b>.
  * </p>
  * <pre>
  *  <b>expiresMinutes</b> - Relative number of minutes (added to current time) to be set as Expires header
  *  <b>useCache</b> - to cache the earlier merged contents and serve from cache. Default true.
  * </pre>
  * <h3>Dependency</h3>
  * <p>Servlet and JSP api (mostly provided by servlet container eg. Tomcat).</p>
  * <p><b>servlet-api.jar</b> - Must be already present in your webapp classpath</p>
  * <h3>Notes on Cache</h3>
  * <p>If you have not set useCache parameter to false then cache will be used and contents will be always served from cache if found.
  * Sometimes you may not want to use cache or you may want to evict the cache then using URL parameters you can do that.
  * </p>
  * <h4>URL Parameters to skip or evict the cache</h4>
  * <pre>
  * <b>_skipcache_</b> - The JS or CSS request URL if contains this parameters the cache will not be used for it.
  * <b>_dbg_</b> - same as above _skipcache_ parameters.
  * <b>_expirecache_</b> - The cache will be cleaned completely. All existing cached contents will be cleaned.
  * </pre>
  * <pre>
  * <b>Eg.</b>
  * &lt;link rel="StyleSheet" href="/myapp/css/common,calendar,aquaskin.css<b>?_dbg=1</b>"/&gt;
  * or
  * &lt;script language="JavaScript" src="/myapp/js/prototype,controls,dragdrop,myapp.js<b>?_expirecache_=1</b>"&gt;&lt;/script&gt;
  * </pre>
  * <h3>Limitations</h3>
  * <p>
  * The multiple JS or CSS files <b>can be combined together in one request if they are in same parent path</b>. eg. <code><b>/myapp/js/a.js</b></code>, <code><b>/myapp/js/b.js</b></code> and <code><b>/myapp/js/c.js</b></code>
  * can be combined together as <code><b>/myapp/js/a,b,c.js</b></code>. If they are not in common path then they can not be combined in one request. Same applies for CSS too.
  * </p>
  *
  * @author rpatil
  * @version 2.0
  */
 public class JSCSSMergeServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     public static final String INIT_PARAM_EXPIRES_MINUTES = "expiresMinutes";
 
     private long expiresMinutes = DEFAULT_EXPIRES_MINUTES; //default value 7 days
 
     private boolean useCache = true; //default true
 
     private Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<String, String>());
 
     private static final Logger logger = Logger.getLogger(JSCSSMergeServlet.class.getName());
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         this.expiresMinutes = Utils.readLong(config.getInitParameter(INIT_PARAM_EXPIRES_MINUTES), this.expiresMinutes);
         this.useCache = Utils.readBoolean(config.getInitParameter(INIT_PARAM_USE_CACHE), this.useCache);
         logger.info("Servlet initialized: " +
                 "{" +
                 "   " + INIT_PARAM_EXPIRES_MINUTES + ":" + this.expiresMinutes + "" +
                 "   " + INIT_PARAM_USE_CACHE + ":" + this.useCache + "" +
                 "}");
     }
 
     /**
      *
      * @param extension - .css or .js etc. (lower case)
      * @param resp - response object
      */
     private void setResponseMimeAndHeaders(String extension, HttpServletResponse resp) {
         String mime = Utils.selectMimeForExtension(extension);
         logger.info("Setting MIME to " + mime);
         resp.setContentType(mime);
         resp.addDateHeader(HEADER_EXPIRES, new Date().getTime() + expiresMinutes * 60 * 1000);
         resp.addDateHeader(HEADER_LAST_MODIFIED, new Date().getTime());
         logger.info("Added expires and last-modified headers");
     }
 
 
     private void expireCache() {
         logger.info("Expiring Cache");
         this.cache.clear();
     }
 
     /* (non-Javadoc)
       * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
       */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
 
         String url = req.getRequestURI();
 
         logger.info("doGetCalled : " + url);
 
         String extension = Utils.detectExtension(url);
 
         this.setResponseMimeAndHeaders(extension, resp);
 
         if (req.getParameter(PARAM_EXPIRE_CACHE) != null) {
             this.expireCache();
         }
 
         boolean useCache = this.useCache && req.getParameter(PARAM_SKIP_CACHE) == null && req.getParameter(PARAM_DEBUG) == null;
 
         if (useCache) {
             logger.info("Using cache for : " + url);
             String fromCache = cache.get(url);
             if (fromCache != null) {
                 logger.info("Cache hit");
                 Writer writer = resp.getWriter();
                 writer.write(fromCache);
                 writer.flush();
                 writer.close();
                 return;
             } else {
                 logger.info("Cache miss");
             }
         }
 
         url = url.replace(req.getContextPath(), "");
 
         Writer out = new StringWriter();
 
         if (!useCache) {
             out = resp.getWriter();
         }
        List<String> resourcesToMerge = findResourcesToMerge(req);
        int resourcesNotFound = 0;
        for (String fullPath : resourcesToMerge) {
             logger.info("Processing resource : " + fullPath);
             InputStream is = null;
             try {
                 is = super.getServletContext().getResourceAsStream(fullPath);
                 if (is != null) {
                     int c;
                     while ((c = is.read()) != -1) {
                         out.write(c);
                     }
                }else{
                    resourcesNotFound++;
                 }
             } catch (Exception e) {
                 logger.warning("Error while reading resource : " + fullPath);
                 logger.severe("Exception :" + e);
             } finally {
                 if (is != null) {
                     is.close();
                    out.flush();
                 }
             }
         }
        if(resourcesNotFound > 0 && resourcesNotFound == resourcesToMerge.size()){ //all resources not found
            resp.sendError(404);
            return;
        }
         if (out != null) {
         	try{
         		out.close();
         	}catch (Exception e) {
 				// ignore
 			}
         }
         if (useCache) {
             logger.info("Updating cache for : " + url);
             cache.put(url, out.toString());
             resp.getWriter().write(out.toString());
         }
     }
 
     /**
      * Split multiple resources with comma eg. if URL is http://server/context/js/a,b,c.js
      * then a.js, b.js and c.js have to be processed and merged together.
      * <p/>
      * b and c can be absolute paths or relative (relative to previous resource) too.
      * <p/>
      * eg.
      * <p/>
      * http://server/context/js/a,/js/libs/b,/js/yui/c.js - absolutes paths for all OR
      * http://server/context/js/a,/js/libs/b,../yui/c.js - relative path used for c.js (relative to b) OR
      * http://server/context/js/a,/js/libs/b,./c.js OR - b & c are in same directory /js/libs
      *
      * @param request HttpServletRequest
      * @return Set of resources to be processed
      */
 
     private List<String> findResourcesToMerge(HttpServletRequest request) {
 
         String contextPath = request.getContextPath();
 
         String requestURI = request.getRequestURI(); //w/o hostname, starts with context. eg. /context/path/subpath/a,b,/anotherpath/c.js
 
         String extension = Utils.detectExtension(requestURI);
 
         logger.info("Detected extension : " + extension);
 
         requestURI = requestURI.replace(contextPath, "").replace(extension, "");//remove the context path & ext. will become /path/subpath/a,b,/anotherpath/c
 
         String[] resourcesPath = requestURI.split(",");
 
         List<String> resources = new ArrayList<String>();
 
         String currentPath = "/"; //default
 
         for (String filePath : resourcesPath) {
             if (filePath == null) continue;
 
             if(filePath.startsWith("./")){
                 filePath = filePath.replaceFirst("./","");
             }
             
             if(filePath.contains("/./")){
                 filePath = filePath.replaceAll("/./","");
             }
 
             String path = "";
 
             if (filePath.startsWith("/")) { //absolute
                 path = filePath + extension;
             } else if(filePath.startsWith("../")){
                 while(filePath.startsWith("../")){
                     filePath = filePath.replaceFirst("../","");
                     currentPath = new File(currentPath).getParent();
                 }
                 path = currentPath + File.separator + filePath + extension;
             }else{
                 path = currentPath + File.separator + filePath + extension;
             }
 
             path = path.replaceAll("//","/");
 
             currentPath = new File(path).getParent();
 
             logger.info("Adding path: " + path + "(Path for next relative resource will be : " + currentPath + ")");
 
             if(!resources.contains(path)){
                 resources.add(path);
             }
         }
         logger.info("Found " + resources.size() + " resources to process and merge.");
         return resources;
     }
 
 
 }
