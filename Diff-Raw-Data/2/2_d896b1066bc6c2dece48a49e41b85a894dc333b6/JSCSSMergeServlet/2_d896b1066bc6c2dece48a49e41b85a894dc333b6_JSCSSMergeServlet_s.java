 /*
  * Copyright 2010-2011 Rajendra Patil
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
  */
 package com.googlecode.webutilities.servlets;
 
 import static com.googlecode.webutilities.common.Constants.CSS_IMG_URL_PATTERN;
 import static com.googlecode.webutilities.common.Constants.DEFAULT_CACHE_CONTROL;
 import static com.googlecode.webutilities.common.Constants.DEFAULT_EXPIRES_MINUTES;
 import static com.googlecode.webutilities.common.Constants.EXT_CSS;
 import static com.googlecode.webutilities.common.Constants.HEADER_EXPIRES;
 import static com.googlecode.webutilities.common.Constants.HEADER_LAST_MODIFIED;
 import static com.googlecode.webutilities.common.Constants.HEADER_X_OPTIMIZED_BY;
 import static com.googlecode.webutilities.common.Constants.HTTP_CACHE_CONTROL_HEADER;
 import static com.googlecode.webutilities.common.Constants.HTTP_ETAG_HEADER;
 import static com.googlecode.webutilities.common.Constants.HTTP_IF_MODIFIED_SINCE;
 import static com.googlecode.webutilities.common.Constants.HTTP_IF_NONE_MATCH_HEADER;
 import static com.googlecode.webutilities.common.Constants.X_OPTIMIZED_BY_VALUE;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
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
  * Visit http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet for more details.
  * Also visit http://code.google.com/p/webutilities/wiki/AddExpiresHeader for details about how to use for setting
  * expires/Cache control header.
  *
  * @author rpatil
  * @version 2.0
  */
 public class JSCSSMergeServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     public static final String INIT_PARAM_EXPIRES_MINUTES = "expiresMinutes";
 
     public static final String INIT_PARAM_CACHE_CONTROL = "cacheControl";
 
     public static final String INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS = "autoCorrectUrlsInCSS";
 
     public static final String INIT_PARAM_TURN_OFF_E_TAG = "turnOffETag";
 
     private long expiresMinutes = DEFAULT_EXPIRES_MINUTES; //default value 7 days
 
     private String cacheControl = DEFAULT_CACHE_CONTROL; //default
 
     private boolean autoCorrectUrlsInCSS = true; //default
 
     private boolean turnOfETag = false; //default enable eTag
 
     private static final Logger logger = Logger.getLogger(JSCSSMergeServlet.class.getName());
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         this.expiresMinutes = Utils.readLong(config.getInitParameter(INIT_PARAM_EXPIRES_MINUTES), this.expiresMinutes);
         this.cacheControl = config.getInitParameter(INIT_PARAM_CACHE_CONTROL) != null ? config.getInitParameter(INIT_PARAM_CACHE_CONTROL) : this.cacheControl ;
         this.autoCorrectUrlsInCSS = Utils.readBoolean(config.getInitParameter(INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS),this.autoCorrectUrlsInCSS);
         this.turnOfETag = Utils.readBoolean(config.getInitParameter(INIT_PARAM_TURN_OFF_E_TAG),this.turnOfETag);
         logger.info("Servlet initialized: " +
                 "{" +
                 "   " + INIT_PARAM_EXPIRES_MINUTES + ":" + this.expiresMinutes + "" +
                 "   " + INIT_PARAM_CACHE_CONTROL + ":" + this.cacheControl + "" +
                 "   " + INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS + ":" + this.autoCorrectUrlsInCSS + "" +
                 "   " + INIT_PARAM_TURN_OFF_E_TAG + ":" + this.turnOfETag + "" +
                 "}");
     }
 
     /**
      *
      * @param extensionOrFile - .css or .js etc. (lower case) or the absolute path of the file in case of image files
      * @param resourcesToMerge - from request
      * @param hashForETag - from request
      * @param resp - response object
      */
     private void addAppropriateResponseHeaders(String extensionOrFile, List<String> resourcesToMerge, String hashForETag, HttpServletResponse resp) {
         String mime = Utils.selectMimeForExtension(extensionOrFile);
         if(mime != null){
             logger.info("Setting MIME to " + mime);
             resp.setContentType(mime);
         }
         long lastModifiedFor = Utils.getLastModifiedFor(resourcesToMerge, this.getServletContext());
         resp.addDateHeader(HEADER_EXPIRES, lastModifiedFor + expiresMinutes * 60 * 1000);
         resp.addHeader(HTTP_CACHE_CONTROL_HEADER, this.cacheControl);
         resp.addDateHeader(HEADER_LAST_MODIFIED, lastModifiedFor);
         if(hashForETag != null && !this.turnOfETag){
         	resp.addHeader(HTTP_ETAG_HEADER, hashForETag);
         }
         resp.addHeader(HEADER_X_OPTIMIZED_BY, X_OPTIMIZED_BY_VALUE);
         logger.info("Added expires, last-modified & ETag headers");
     }
 
     /* (non-Javadoc)
       * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
       */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
 
         String url = this.getURL(req);
 
         logger.info("Started processing request : " + url);
 
         List<String> resourcesToMerge = Utils.findResourcesToMerge(req.getContextPath(), url);
 
         //If not modified, return 304 and stop
         ResourceStatus status = this.isNotModified(req, resp, resourcesToMerge);
         if(status.isNotModified()){
             logger.info("Resources Not Modified. Sending 304.");
             this.sendNotModified(resp);
     		return;
         }
 
         String extensionOrPath = Utils.detectExtension(url);//in case of non js/css files it null
         if(extensionOrPath == null){
             extensionOrPath = resourcesToMerge.get(0);//non grouped i.e. non css/js file, we refer it's path in that case
         }
 
         //Add appropriate headers
         this.addAppropriateResponseHeaders(extensionOrPath, resourcesToMerge, status.getActualETag(), resp);
 
         Writer out = resp.getWriter();
         int resourcesNotFound = this.processResources(req.getContextPath(), out, resourcesToMerge);
 
         if(resourcesNotFound > 0 && resourcesNotFound == resourcesToMerge.size()){ //all resources not found
             resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             logger.warning("All resources are not found. Sending 404.");
             return;
         }
         if (out != null) {
         	try{
         		resp.setStatus(HttpServletResponse.SC_OK);
                 out.close();
         	}catch (Exception e) {
 				// ignore
 			}
         }
         logger.info("Finished processing Request : " + url);
     }
 
     /**
      *
      * @param response httpServletResponse
      */
     private void sendNotModified(HttpServletResponse response){
         response.setContentLength(0);
         response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
     }
 
     /**
      *
      * @param request HttpServletRequest
      * @return
      */
     private String getURL(HttpServletRequest request){
         String url = request.getRequestURI();
         return Utils.removeFingerPrint(url);
     }
 
     /**
      *
      * @param request
      * @param response
      * @param resourcesToMerge
      * @return true if not modified based on if-None-Match and If-Modified-Since
      */
     private ResourceStatus isNotModified(HttpServletRequest request, HttpServletResponse response, List<String> resourcesToMerge){
         ServletContext context = this.getServletContext();
         //If-None-match
         String requestETag = request.getHeader(HTTP_IF_NONE_MATCH_HEADER);
         String actualETag = this.turnOfETag ? null : Utils.buildETagForResources(resourcesToMerge, context);
         if(!this.turnOfETag && !Utils.isAnyResourceETagModified(resourcesToMerge, requestETag, actualETag, context)){
         	return new ResourceStatus(actualETag, true);
         }
         //If-Modified-Since
         String ifModifiedSince = request.getHeader(HTTP_IF_MODIFIED_SINCE);
         if(ifModifiedSince != null){
             Date date = Utils.readDateFromHeader(ifModifiedSince);
             if(date != null){
                 if(!Utils.isAnyResourceModifiedSince(resourcesToMerge, date.getTime(), context)){
                     this.sendNotModified(response);
                     return new ResourceStatus(actualETag, true);
                 }
             }
         }
         return new ResourceStatus(actualETag, false);
     }
 
     /**
      *
      * @param contextPath HttpServletRequest context path
      * @param out - PrintWrite
      * @param resourcesToMerge list of resources to merge
      * @return number of non existing, unprocessed resources
      */
 
     private int processResources(String contextPath, Writer out, List<String> resourcesToMerge){
 
         int resourcesNotFound = 0;
 
         ServletContext context = this.getServletContext();
 
         for (String resourcePath : resourcesToMerge) {
 
             logger.info("Processing resource : " + resourcePath);
 
             InputStream is = null;
 
             try {
                 is = context.getResourceAsStream(resourcePath);
                 if (is == null) {
                     resourcesNotFound++;
                     continue;
                 }
                 if (resourcePath.endsWith(EXT_CSS) && autoCorrectUrlsInCSS) { //Need to deal with images url in CSS
 
                     this.processCSS(contextPath, resourcePath, is, out);
 
                 }else{
                     int c;
                     while ((c = is.read()) != -1) {
                         out.write(c);
                     }
                 }
             } catch (IOException e) {
                 logger.warning("Error while reading resource : " + resourcePath);
                 logger.severe("IOException :" + e);
             }
 
             if (is != null) {
                 try{
                     is.close();
                 }catch (IOException ex){
                     logger.warning("Failed to close stream:" + ex);
                 }
                 try{
                     out.flush();
                 }catch (IOException ex){
                     logger.severe("Failed to flush out:" + out);
                 }
             }
 
         }
         return resourcesNotFound;
     }
 
     /**
      *
      * @param cssFilePath
      * @param contextPath
      * @param is
      * @param out
      * @throws IOException
      */
     private void processCSS(String contextPath, String cssFilePath, InputStream is, Writer out) throws IOException{
         ServletContext context = this.getServletContext();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = bufferedReader.readLine()) != null) {
             line = this.processCSSLine(context, contextPath, cssFilePath, line);
             out.write(line + "\n");
         }
     }
 
     /**
      *
      * @param context
      * @param contextPath
      * @param cssFilePath
      * @param line
      * @return
      */
     private String processCSSLine(ServletContext context, String contextPath, String cssFilePath, String line){
         Matcher matcher = CSS_IMG_URL_PATTERN.matcher(line);
         while (matcher.find()) {
             String refImgPath = matcher.group(1);
             if (!refImgPath.matches("^[a-z0-9\\+\\.\\-]+:.*$")) { //ignore absolute protocol paths
                 String cssRealPath = context.getRealPath(cssFilePath);
                 String resolvedImgPath = refImgPath;
                 if(!refImgPath.startsWith("/")){
                     resolvedImgPath = Utils.buildProperPath(new File(cssFilePath).getParent(), refImgPath);
                 }
                 String imgRealPath = context.getRealPath(resolvedImgPath);
                 String fingerPrint = Utils.buildETagForResource(resolvedImgPath, context);
                 line = line.replaceAll(refImgPath, contextPath + (fingerPrint != null ? Utils.addFingerPrint(fingerPrint, resolvedImgPath) : resolvedImgPath));
                Utils.updateReferenceMap(cssFilePath,imgRealPath);
             }
         }
         return line;
     }
 }
 
 /**
  * Class to store resource ETag and modified status
  */
 class ResourceStatus{
 
     private String actualETag;
 
     private boolean notModified = true;
 
     ResourceStatus(String actualETag, boolean notModified) {
         this.actualETag = actualETag;
         this.notModified = notModified;
     }
 
     public String getActualETag() {
         return actualETag;
     }
 
     public boolean isNotModified() {
         return notModified;
     }
 
 }
