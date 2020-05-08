 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.action.staticResource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jodd.io.StreamUtil;
 import jodd.servlet.filter.GzipResponseWrapper;
 import jodd.util.Wildcard;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.threewks.thundr.action.ActionException;
 import com.threewks.thundr.action.ActionResolver;
 import com.threewks.thundr.exception.BaseException;
 import com.threewks.thundr.http.HttpSupport.Header;
 import com.threewks.thundr.logger.Logger;
 import com.threewks.thundr.route.RouteType;
 
 // TODO - Better caching control:
 // TODO - Vary: Accept-Encoding
 public class StaticResourceActionResolver implements ActionResolver<StaticResourceAction> {
 
 	private static final String ActionName = "static";
 	private static final Pattern ActionNamePattern = Pattern.compile("^static:(.+)");
 
 	private final String protectedPath = "/?WEB-INF/.*";
 
 	private boolean gzipEnabled = true;
 
 	private Map<String, String> defaultMimeTypes = new HashMap<String, String>();
 	{
 		defaultMimeTypes.put(".html", "text/html");
 		defaultMimeTypes.put(".htm", "text/html");
 		defaultMimeTypes.put(".css", "text/css");
 		defaultMimeTypes.put(".gif", "image/gif");
 		defaultMimeTypes.put(".ico", "image/vnd.microsoft.icon");
 		defaultMimeTypes.put(".jpeg", "image/jpeg");
 		defaultMimeTypes.put(".jpg", "image/jpeg");
 		defaultMimeTypes.put(".js", "text/javascript");
 		defaultMimeTypes.put(".png", "image/png");
 		defaultMimeTypes.put(".htc", "text/x-component");
 	}
 
 	private Set<String> compressedMimeTypes = new HashSet<String>();
 	{
 		compressedMimeTypes.add("text/*");
 	}
 
 	private int cacheDuration = 24 * 60 * 60;
 	private ServletContext servletContext;
 
 	public StaticResourceActionResolver(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 
 	@Override
 	public StaticResourceAction createActionIfPossible(String actionName) {
 		if (ActionName.equalsIgnoreCase(actionName) || ActionNamePattern.matcher(actionName).matches()) {
 			return new StaticResourceAction();
 		}
 		return null;
 	}
 
 	@Override
 	public Object resolve(StaticResourceAction action, RouteType routeType, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) throws ActionException {
 		try {
 			serve(action, req, resp);
 			return null;
 		} catch (Exception e) {
 			Throwable original = e.getCause() == null ? e : e.getCause();
 			throw new BaseException(original, "Failed to load resource %s: %s", req.getRequestURI(), original.getMessage());
 		}
 	}
 
 	protected void serve(StaticResourceAction action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String resource = request.getRequestURI();
 		URL resourceUrl = servletContext.getResource(resource);
 		boolean allowed = isAllowed(resource);
 		if (resourceUrl == null || !allowed) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			Logger.info("%s -> %s not resolved: %s", resource, action, allowed ? "Not found" : "Not Permitted");
 			return;
 		}
 
 		URLConnection urlConnection = resourceUrl.openConnection();
 		String mimeType = deriveMimeType(resource);
 		long contentLength = urlConnection.getContentLength();
 		long lastModified = urlConnection.getLastModified();
 		String acceptEncoding = request.getHeader(Header.AcceptEncoding);
 		long cacheTimeSeconds = deriveCacheDuration(resource, mimeType);
 
 		response.setContentType(mimeType);
 		response.setDateHeader(Header.Expires, System.currentTimeMillis() + cacheTimeSeconds * 1000L); // HTTP 1.0
 		response.setHeader(Header.CacheControl, String.format("max-age=%d, public", cacheTimeSeconds)); // HTTP 1.1
 		response.setDateHeader(Header.LastModified, lastModified);
 
 		OutputStream os = null;
 		InputStream is = urlConnection.getInputStream();
 
 		if (shouldZip(acceptEncoding, mimeType)) {
 			GzipResponseWrapper wrapper = new GzipResponseWrapper(response);
 			os = wrapper.getOutputStream();
 			StreamUtil.copy(is, os);
 			wrapper.finishResponse();
 		} else {
 			response.setHeader(Header.ContentLength, Long.toString(contentLength));
 			os = response.getOutputStream();
 			StreamUtil.copy(is, os);
 			os.close();
 		}
 
 		response.setStatus(HttpServletResponse.SC_OK);
 		Logger.debug("%s -> %s resolved as %s(%d bytes)", resource, action, mimeType, contentLength);
 	}
 
 	private long deriveCacheDuration(String resource, String mimeType) {
 		return cacheDuration;
 	}
 
 	String deriveMimeType(String resource) {
 		String mimeType = servletContext.getMimeType(resource);
 		if (mimeType == null) {
 			String extension = resource.substring(resource.lastIndexOf('.'));
 			mimeType = defaultMimeTypes.get(extension);
 		}
 		return mimeType;
 	}
 
 	boolean shouldZip(String acceptEncoding, String mimeType) {
 		return gzipEnabled && StringUtils.indexOf(acceptEncoding, "gzip") > -1 && matchesCompressedMimeTypes(mimeType);
 	}
 
 	boolean matchesCompressedMimeTypes(String mimeType) {
		if (mimeType != null) {
			for (String compressedMimeType : compressedMimeTypes) {
				if (Wildcard.match(mimeType, compressedMimeType)) {
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 	boolean isAllowed(String resourcePath) {
 		return !resourcePath.matches(protectedPath);
 	}
 }
