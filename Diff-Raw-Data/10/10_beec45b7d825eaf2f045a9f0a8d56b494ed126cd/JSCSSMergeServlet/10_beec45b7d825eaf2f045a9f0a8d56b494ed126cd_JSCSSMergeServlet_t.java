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
 package com.googlecode.webutilities;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
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
  *		&lt;param-name&gt;expiresMinutes&lt;/param-name&gt;
  *		&lt;param-value&gt;7200&lt;/param-value&gt; &lt;!-- 5 days --&gt;
  * 	&lt;/init-param&gt;
  *	&lt;!-- This init param is also optional and default value is true. Set it false to override. --&gt; 
  * 	&lt;init-param&gt;
  *		&lt;param-name&gt;useCache&lt;/param-name&gt;
  *		&lt;param-value&gt;false&lt;/param-value&gt; 
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
  * 	In your web pages (HTML or JSP files) combine your multiple JS or CSS in one request as shown below.
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
  * @author rpatil
  * @version 1.0
  *
  */
 public class JSCSSMergeServlet extends HttpServlet{
 
 	private static final long serialVersionUID = 1L;
 	
 	private long expiresMinutes = 7*24*60; //+ or - minutes to be added as expires header from current time. default 7 days
 	
 	private boolean useCahce = true;
 	
 	private Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<String, String>());
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		this.expiresMinutes = ifValidNumber(config.getInitParameter("expiresMinutes"), this.expiresMinutes);
 	}
 	
 	private String addHeaders(HttpServletRequest req,HttpServletResponse resp){
		String url = req.getRequestURI(), lowerUrl = url.toLowerCase();
		if(lowerUrl.endsWith(".json")){
 			resp.setContentType("application/json");
		}else if(lowerUrl.endsWith(".js")){
 			resp.setContentType("text/javascript");
		}else if(lowerUrl.endsWith(".css")){
 			resp.setContentType("text/css");
 		}
 		resp.addDateHeader("Expires", new Date().getTime() + expiresMinutes*60*1000);
 		resp.addDateHeader("Last-Modified", new Date().getTime());
 		return url;
 	}
 	
 	private void expireCache(){
 		this.cache.clear();
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		
 		String url = addHeaders(req, resp);
 		if(req.getParameter("_expirecache_") != null){
 			this.expireCache();
 		}
 		boolean useCache = this.useCahce && req.getParameter("_skipcache_") == null && req.getParameter("_dbg_") == null;
 		
 		if(useCache){
 			String fromCache = cache.get(req.getRequestURI());
 			if(fromCache != null){
 				Writer writer = resp.getWriter();
 				writer.write(fromCache);
 				writer.flush();
 				writer.close();
 				return;
 			}
 		}
 		url = url.replace(req.getContextPath(), ""); 
 		//Split multiple files with comma eg. if URL is http://server/context/js/a,b,c.js then a.js, b.js and c.js have to loaded together
 		String path = super.getServletContext().getRealPath(url.substring(0,url.lastIndexOf("/")));
 		String file = url.substring(url.lastIndexOf("/")+1);
 		
 		Writer out = new StringWriter();
 		if(!useCache){
 			out = resp.getWriter();
 		}
 		String[] files = file.split(",");
 		for(String f : files){
 			if(url.endsWith(".json") && !f.endsWith(".json")){
 				f += ".json";
 			}
 			if(url.endsWith(".js") && !f.endsWith(".js")){
 				f += ".js";
 			}
 			if(url.endsWith(".css") && !f.endsWith(".css")){
 				f += ".css";
 			}
 			String fullPath = path + File.separator + f;
 			FileInputStream fis = null;
 			try{
 				File fl = new File(fullPath);
 				fis = new FileInputStream(fl);
 				int c;
 				while((c = fis.read()) != -1){
 					out.write(c);
 				}
 				out.flush();
 			}catch (Exception e) {
 				
 				log("Exception in "+JSCSSMergeServlet.class.getSimpleName()+":" + e);
 				
 			}finally{
 				if(fis != null){
 					fis.close();
 				}
 				if(out != null){
 					out.close();
 				}
 			}
 			
 		}
 		if(useCache){
 			cache.put(req.getRequestURI(), out.toString());
 			resp.getWriter().write(out.toString());
 		}
 		
 	}
 	
 	private long ifValidNumber(String s, long def){
 		if(s != null && s.matches("[0-9]+")){
 			try{
 				return Long.parseLong(s);
 			}catch (Exception e) {
 				return def;
 			}
 		}
 		return def;
 	}
 }
