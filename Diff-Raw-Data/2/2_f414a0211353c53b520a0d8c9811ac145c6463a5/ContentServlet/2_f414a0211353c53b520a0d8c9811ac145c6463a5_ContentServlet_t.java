 /*
  * Copyright 2004-2008 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.eclipse.virgo.management.console;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.management.ManagementFactory;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.virgo.management.console.internal.ContentURLFetcher;
 import org.eclipse.virgo.management.console.internal.GZIPResponseStream;
 import org.eclipse.virgo.management.console.internal.Parser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Special servlet to load static resources.
  * 
  */
 public class ContentServlet extends HttpServlet {
 	
 	private static final Logger log = LoggerFactory.getLogger(ContentServlet.class);
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String HTTP_CONTENT_LENGTH_HEADER = "Content-Length";
 
 	private static final String HTTP_LAST_MODIFIED_HEADER = "Last-Modified";
 
 	private static final String HTTP_EXPIRES_HEADER = "Expires";
 
 	private static final String HTTP_CACHE_CONTROL_HEADER = "Cache-Control";
 
 	protected static final String CONTENT_SERVLET_PREFIX = "prefix";
 
 	protected static final String CONTENT_SERVLET_SUFFIX = "suffix";
 	
     private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
 	
 	private boolean gzipEnabled = true;
 
 	private int cacheTimeout = 60; //The number of seconds content should be cached by the client. Zero disables caching, 31556926 is one year.
 
 	private ContentURLFetcher urlFetcher;
 
 	private Map<String, String> defaultMimeTypes = new HashMap<String, String>();
 	{
 		defaultMimeTypes.put(".html",  "text/html");
 		defaultMimeTypes.put(".htm",   "text/html");
 		defaultMimeTypes.put(".xhtml", "text/html");
 	}
 
 	private Set<String> compressedMimeTypes = new HashSet<String>();
 	{
 		compressedMimeTypes.add("text/.*");
 		compressedMimeTypes.add(".*/xhtml.xml");
 	}
 
     public void init(ServletConfig config) throws ServletException {
     	super.init(config);
     	String prefix = config.getInitParameter(CONTENT_SERVLET_PREFIX);
     	String suffix = config.getInitParameter(CONTENT_SERVLET_SUFFIX);
     	this.urlFetcher = new ContentURLFetcher(config.getServletContext(), prefix, suffix);
     }
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String rawRequestPath = this.getRequestPath(request);
 		if (log.isDebugEnabled()) {
 			log.debug("Attempting to GET content: " + rawRequestPath);
 		}
 		
 		URL resource = this.urlFetcher.getRequestedContentURL(rawRequestPath);
 		if (resource == null) {
 			if (log.isDebugEnabled()) {
 				log.debug("Content not found: " + rawRequestPath);
 			}
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 		prepareContentResponse(response, resource);
 		PrintWriter out = selectOutputStream(request, response, resource);
 		URLConnection resourceConn = resource.openConnection();
 		try {
 			InputStream in = resourceConn.getInputStream();
 			try {
 				Map<String, String> pageContext = new HashMap<String, String>();
 				this.preparePageContext(pageContext, rawRequestPath);
 				new Parser(out, this.urlFetcher, pageContext).parse(in);
 			} finally {
 				in.close();
 			}
 		} finally {
 			out.close();
 		}
 	}
 
 	private PrintWriter selectOutputStream(final HttpServletRequest request, final HttpServletResponse response, final URL resource) throws IOException {
 		String acceptEncoding = request.getHeader("Accept-Encoding");
 		String mimeType;
 		try {
 			mimeType = response.getContentType();
 		} catch(UnsupportedOperationException e){
 			mimeType = getResponseMimeType(resource);
 		}
 		if (gzipEnabled && 
 				acceptEncoding != null && 
 				acceptEncoding.indexOf("gzip") > -1 && 
 				matchesCompressedMimeTypes(mimeType)) {
 			log.debug("Enabling GZIP compression for the current response.");
 			return new PrintWriter(new GZIPResponseStream(response));
 		} else {
 			return response.getWriter();
 		}
 	}
 
 	private boolean matchesCompressedMimeTypes(final String mimeType) {
 		for(String compressedMimeType: compressedMimeTypes){
 			if(mimeType.matches(compressedMimeType)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void prepareContentResponse(final HttpServletResponse response, final URL resource) throws IOException {	
 		URLConnection resourceConn = resource.openConnection();
 		response.setContentType(getResponseMimeType(resource));
 		response.setHeader(HTTP_CONTENT_LENGTH_HEADER, Long.toString(resourceConn.getContentLength()));
 		response.setDateHeader(HTTP_LAST_MODIFIED_HEADER, resourceConn.getLastModified());
 		if (cacheTimeout > 0) {
 			configureCaching(response, cacheTimeout);
 		}
 	}
 	
 	private String getResponseMimeType(final URL resource){
 		String extension = resource.getPath().substring(resource.getPath().lastIndexOf('.'));
 		String mimeType = (String) defaultMimeTypes.get(extension);
 		if (mimeType == null) {
 			 mimeType = getServletContext().getMimeType(resource.getPath());
 		}
 		return mimeType;
 	}
 	
 	private void preparePageContext(final Map<String, String> pageContext, final String rawRequestPath){
 		String viewName = rawRequestPath;
 		if('/' == viewName.charAt(0)){
 			viewName = viewName.substring(1);
 		}
 		
 		List<String> menuItems = new ArrayList<String>();
 
 		this.addIfMbeanPresent(menuItems, "artifacts", "org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=*,*");
 		this.addIfMbeanPresent(menuItems, "repositories", "org.eclipse.virgo.kernel:type=Repository,name=*");
 		this.addIfMbeanPresent(menuItems, "wirings", "osgi.core:version=1.0,type=wiringState,region=*");
 		this.addIfMbeanPresent(menuItems, "dumps", "org.eclipse.virgo.kernel:type=Medic,name=DumpInspector");
 		this.addIfMbeanPresent(menuItems, "configurations", "osgi.compendium:service=cm,version=1.3,region=*");
		//this.addIfMbeanPresent(menuItems, "logging", "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator");
 		
 		String stringArray = Arrays.toString(menuItems.toArray(new String[menuItems.size()]));
 		pageContext.put("menuNames", stringArray.substring(1, stringArray.length() - 1));
 		pageContext.put("viewName", viewName);
 		
 		ServletContext servletContext = getServletContext();
 		try {
 			pageContext.put("contextPath", servletContext.getContextPath());
 			pageContext.put("servletContextName", servletContext.getServletContextName());
 		} catch(UnsupportedOperationException e){
 			pageContext.put("contextPath", Activator.contextPath);
 			pageContext.put("servletContextName", Activator.APPLICATION_NAME);
 		}
 		pageContext.put("servletContainer", servletContext.getServerInfo());
 		pageContext.put("virtualMachine", String.format("%s - %s %s (%s)", System.getProperty("java.version"), System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("java.vm.vendor"), System.getProperty("java.version")));
 		pageContext.put("operatingSystem", String.format("%s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")));
 	}
 
 	private void addIfMbeanPresent(List<String> menuItems, String viewName, String objectName){
 		try {
 			ObjectName objectNameImpl = new ObjectName(objectName);
 			if(objectNameImpl.isPattern()){
 				Set<ObjectName> queryNames = this.server.queryNames(objectNameImpl, null);
 				if(queryNames.size() > 0){
 					menuItems.add(viewName);
 				}
 			}else{
 				if(this.server.isRegistered(objectNameImpl)){
 					menuItems.add(viewName);
 				}
 			}
 		} catch (Exception e) {
 			this.log("Error checking for MBean required for Admin Console Page" + objectName, e);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	protected long getLastModified(HttpServletRequest request) {
 		String rawRequestPath = this.getRequestPath(request);
 		if (log.isDebugEnabled()) {
 			log.debug("Checking last modified of content: " + rawRequestPath);
 		}
 		URL resource;
 		try {
 			resource = this.urlFetcher.getRequestedContentURL(rawRequestPath);
 		} catch (MalformedURLException e) {
 			return -1;
 		}
 		if (resource == null) {
 			return -1;
 		}
 		try {
 			return resource.openConnection().getLastModified();
 		} catch (IOException e) {
 			return -1;
 		}
 	}
 	
 	private String getRequestPath(HttpServletRequest request){
 		String rawRequestPath = request.getPathInfo();
 		if(rawRequestPath == null){
 			rawRequestPath = "/overview";
 		}
 		return rawRequestPath;
 	}
 
 	/**
 	 * Set HTTP headers to allow caching for the given number of seconds.
 	 * @param seconds number of seconds into the future that the response should be cacheable for
 	 */
 	private void configureCaching(final HttpServletResponse response, final int seconds) {
 		response.setDateHeader(HTTP_EXPIRES_HEADER, System.currentTimeMillis() + seconds * 1000L); // HTTP 1.0 header
 		response.setHeader(HTTP_CACHE_CONTROL_HEADER, "max-age=" + seconds);// HTTP 1.1 header
 	}
 
 }
