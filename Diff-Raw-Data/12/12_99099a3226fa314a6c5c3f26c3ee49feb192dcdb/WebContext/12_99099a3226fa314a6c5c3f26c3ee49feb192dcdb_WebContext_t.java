 /*
  * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     bstefanescu
  *
  */
 
 package org.eclipse.ecr.web.framework;
 
 import java.net.URI;
 import java.security.Principal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.UriInfo;
 
 import org.eclipse.ecr.web.jaxrs.context.RequestContext;
 
 public class WebContext {
 
 	public static void setActiveContext(WebContext ctx) {
 		RequestContext.getActiveContext().put(WebContext.class.getName(), ctx);
 	}
 
 	public static void setActiveContext(ServletRequest request, WebContext ctx) {
 		RequestContext.getActiveContext(request).put(
 				WebContext.class.getName(), ctx);
 	}
 
 	public static WebContext getActiveContext() {
 		return (WebContext) RequestContext.getActiveContext().get(
 				WebContext.class.getName());
 	}
 
 	public static WebContext getActiveContext(ServletRequest request) {
 		return (WebContext) RequestContext.getActiveContext(request).get(
 				WebContext.class.getName());
 	}
 
 	protected UriInfo uriInfo;
 
 	protected HttpServletRequest request;
 
 	protected HttpServletResponse response;
 
 	protected WebApplication app;
 
 	protected String contextPath;
 
 	protected String basePath;
 
 	protected String skinPath;
 
 	protected String path;
 
 	protected Locale locale;
 
 	protected Breadcrumb breadcrumb;
 
 	protected Map<String, Object> properties;
 
 	WebContext() {
 		breadcrumb = new Breadcrumb();
 		properties = new HashMap<String, Object>();
 	}
 
 	public WebApplication getApplication() {
 		return app;
 	}
 
 	public void setUriInfo(UriInfo uriInfo) {
 		this.uriInfo = uriInfo;
 	}
 
 	public UriInfo getUriInfo() {
 		return uriInfo;
 	}
 
 	public void setRequest(HttpServletRequest request) {
 		this.request = request;
 	}
 
 	public HttpServletRequest getRequest() {
 		return request;
 	}
 
 	public void setResponse(HttpServletResponse response) {
 		this.response = response;
 	}
 
 	public HttpServletResponse getResponse() {
 		return response;
 	}
 
 	public Principal getPrincipal() {
 		return request.getUserPrincipal();
 	}
 
 	public void setContextPath(String contextPath) {
 		if (contextPath.endsWith("/")) {
 			this.contextPath = contextPath.substring(0,
 					contextPath.length() - 1);
 		} else {
 			this.contextPath = contextPath;
 		}
 	}
 
 	protected void initPaths() {
 		String basePath = contextPath.concat("/");
 		List<String> uris = uriInfo.getMatchedURIs();
 		String modulePath = uris.get(uris.size() - 1);
 		if (modulePath.endsWith("/")) {
 			modulePath = modulePath.substring(0, modulePath.length() - 1);
 		}
 		basePath = basePath.concat(modulePath);
 		this.basePath = basePath;
 		this.skinPath = basePath.concat("/skin");
 	}
 
 	public String getContextPath() {
 		return contextPath;
 	}
 
 	public String getBasePath() {
 		if (basePath == null) {
 			initPaths();
 		}
 		return basePath;
 	}
 
 	public String getSkinPath() {
 		if (skinPath == null) {
 			initPaths();
 		}
 		return skinPath;
 	}
 
 	public String getPath() {
 		if (this.path == null) {
 			String path = contextPath.concat("/").concat(uriInfo.getPath());
 			if (path.endsWith("/")) {
 				path = path.substring(0, path.length() - 1);
 			}
 			this.path = path;
 		}
 		return this.path;
 	}
 
 	public String getRelativePath(String absPath) {
 		int len = contextPath.length() + 1;
 		return absPath.substring(len);
 	}
 
 	public URI getUri(String absPath) {
 		return uriInfo.getAbsolutePathBuilder().replacePath(absPath).build();
 	}
	
	public String getRequestUrl() {
		return request.getRequestURL().toString();
	}
 
 	public WebRoot getRoot() {
 		List<Object> res = uriInfo.getMatchedResources();
 		return (WebRoot) res.get(res.size() - 1);
 	}
 
 	public String getMessage(String key) {
 		return getApplication().getMessage(getLocale().getLanguage(), key);
 	}
 
 	public String getMessage(String key, Object... vars) {
 		return getApplication()
 				.getMessage(getLocale().getLanguage(), key, vars);
 	}
 
 	public String getMessage(String key, List<Object> vars) {
 		return getApplication()
 				.getMessage(getLocale().getLanguage(), key, vars);
 	}
 
 	public void setLanguage(String language) {
 		this.locale = new Locale(language);
 	}
 
 	public void setLocale(Locale locale) {
 		this.locale = locale;
 	}
 
 	public Locale getLocale() {
 		if (locale == null) {
 			return request.getLocale();
 		}
 		return locale;
 	}
 
 	public Breadcrumb getBreadcrumb() {
 		return breadcrumb;
 	}
 
 	public String getParameter(String key) {
 		return request.getParameter(key);
 	}
 
 	public String[] getParameters(String key) {
 		return request.getParameterValues(key);
 	}
 
 	public Map<String, Object> getProperties() {
 		return properties;
 	}
 
 	public Object getProperty(String key) {
 		return properties.get(key);
 	}
 
 	public void setProperty(String key, Object value) {
 		properties.put(key, value);
 	}
 
 }
