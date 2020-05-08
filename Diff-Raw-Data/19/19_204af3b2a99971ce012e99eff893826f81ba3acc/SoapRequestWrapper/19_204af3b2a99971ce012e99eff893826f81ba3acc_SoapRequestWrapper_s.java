 //$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/RequestWrapper.java $
 //$Id: RequestWrapper.java 677 2011-09-09 15:26:10Z PRO-VISION\SSeifert $
 package org.liveSense.service.cxf;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
 import org.apache.sling.api.request.RequestParameter;
 import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 @Deprecated
 
 /**
  * Request wrapper that maps all pathinfo to a virtual path, to whom the SOAP
  * services are registered to.
  * @Deprecated
  * Use @WebServiceMarkerInterface and declaretive service instead. 
  * Example: https://github.com/liveSense/org.liveSense.sample.webServiceServlet/tree/master/src/main/java/org/liveSense/sample/WebServiceServlet/ds
  */
 public class SoapRequestWrapper extends HttpServletRequestWrapper {
 
 	Logger log = LoggerFactory.getLogger(HttpServletRequestWrapper.class);
 	public static final String VIRTUAL_PATH = "/soaprequest";
 
 	String servletUrl = null;
 	HttpServletRequest pRequest;
 	
 	public SoapRequestWrapper(HttpServletRequest pRequest, String servletUrl) {
 		super(pRequest);
 		this.servletUrl = servletUrl;
 //		ServletRequest recursiveRequest = pRequest;	
 //		while (recursiveRequest instanceof  SlingHttpServletRequestWrapper && ((SlingHttpServletRequestWrapper)recursiveRequest).getRequest() != null) {
 //			recursiveRequest = ((SlingHttpServletRequestWrapper)recursiveRequest).getRequest();
 //		}
 		this.pRequest = pRequest;
 	}
 
 	public String getServletUrl() {
 		return servletUrl;
 	}
 
 
 	@Override
 	public String getRequestURI() {
 		return getPathInfo();
 	}
 
 	public MediaType getMediaType(MediaType defaultMediaType) {
 		if (defaultMediaType == null) {
 			defaultMediaType = MediaType.TEXT_PLAIN_TYPE;
 		}
 		if (servletUrl != null) {
 			String requestPath = ((HttpServletRequest)getRequest()).getPathInfo();
 			int index = StringUtils.indexOf(requestPath, servletUrl);
 			if (index == -1) {
 				return defaultMediaType;
 			} else 
 				try {
 					URI uri = new URI(requestPath);
 
 					if (StringUtils.contains(uri.getPath(), servletUrl)) {
 						String urlPrefix = StringUtils.substring(uri.getPath(), 0, StringUtils.indexOf(uri.getPath(), servletUrl));
 						String urlPostfix = StringUtils.substring(uri.getPath(), StringUtils.indexOf(uri.getPath(), servletUrl)+servletUrl.length());
 						String selector = null;
 						if (urlPostfix.startsWith(".")) {
 							if (StringUtils.contains(urlPostfix, "/")) {
 								selector = StringUtils.substring(urlPostfix, StringUtils.indexOf(urlPostfix, ".")+1, StringUtils.indexOf(urlPostfix, "/"));
 								urlPostfix = "/" + StringUtils.substringAfter(urlPostfix, "/");
 							} else {
 								selector = urlPostfix.substring(1);
 								urlPostfix = "";
 							}
 						}
 						if (StringUtils.contains(selector, ".")) {
 							selector = StringUtils.substring(selector, 0, StringUtils.indexOf(selector, "."));
 						}
 	
 						if (StringUtils.isNotEmpty(selector)) {
 							if (selector.equalsIgnoreCase("json")) {
 								return MediaType.APPLICATION_JSON_TYPE;
 							} else if (selector.equalsIgnoreCase("xml")) {
 								return MediaType.TEXT_XML_TYPE;
 							} else if (selector.equalsIgnoreCase("text")) {
 								return MediaType.TEXT_PLAIN_TYPE;
 							} else if (selector.equalsIgnoreCase("html")) {
 								return MediaType.TEXT_HTML_TYPE;
 							} else if (selector.contains("-")) {
 								String selectors[] = selector.split("-");
 								return (new MediaType(selectors[0], selectors[1]));
 							} else
 								return defaultMediaType;
 						} else {
 							return defaultMediaType;
 						}
 					} else {
 						log.warn("URI ("+uri.toString()+") does not contain: "+requestPath);
 						return defaultMediaType;
 					}
 				} catch (URISyntaxException e) {
 					log.error("URI creation problem: "+requestPath, e);
 					return defaultMediaType;
 				}
 		} else {
 			return defaultMediaType;
 		}
 
 	}
 
 	@Override
 	public String getPathInfo() {
 		if (servletUrl != null) {
 			String requestPath = ((HttpServletRequest)getRequest()).getPathInfo();
 			int index = StringUtils.indexOf(requestPath, servletUrl);
 			if (index == -1) {
 				return servletUrl;
 			} else 
 				try {
 					URI uri = new URI(requestPath);
 
 					if (StringUtils.contains(uri.getPath(), servletUrl)) {
 						String urlPrefix = StringUtils.substring(uri.getPath(), 0, StringUtils.indexOf(uri.getPath(), servletUrl));
 						String urlPostfix = StringUtils.substring(uri.getPath(), StringUtils.indexOf(uri.getPath(), servletUrl)+servletUrl.length());
 						String selector = null;
 						if (urlPostfix.startsWith(".")) {
 							if (StringUtils.contains(urlPostfix, "/")) {
 								selector = StringUtils.substring(urlPostfix, StringUtils.indexOf(urlPostfix, ".")+1, StringUtils.indexOf(urlPostfix, "/"));
 								urlPostfix = "/" + StringUtils.substringAfter(urlPostfix, "/");
 							} else {
 								selector = urlPostfix.substring(1);
 								urlPostfix = "";
 							}
 						}
 						UriBuilderImpl uriBuilder = new UriBuilderImpl(uri);
 						if (StringUtils.isNotEmpty(selector)) {
 							uriBuilder.replacePath(urlPrefix+servletUrl+urlPostfix+"."+selector);
 						} else {
 							uriBuilder.replacePath(urlPrefix+servletUrl+urlPostfix);	    				
 						}
						return uriBuilder.build((Object)null).toString();
 					} else {
 						log.warn("URI ("+uri.toString()+") does not contain: "+requestPath);
 						return servletUrl;
 					}
 				} catch (URISyntaxException e) {
 					log.error("URI creation problem: "+requestPath, e);
 					return VIRTUAL_PATH;
 				}
 		} else {
 			return VIRTUAL_PATH;
 		}
 	}
 	/*
 	public String getTranslatedPath(String requestPath) throws URISyntaxException {
 		URI uri = new URI(requestPath);
 		System.out.println("Path: "+uri.getPath()+" Query: "+uri.getQuery()+" Fragment: "+uri.getFragment());
 		return uri.toString();
 	}
 	 */
 
 	@Override
 	public Enumeration getParameterNames() {
 		if (pRequest instanceof  SlingHttpServletRequestWrapper) {
 			return Collections.enumeration(((SlingHttpServletRequestWrapper)pRequest).getRequestParameterMap().keySet());
 		} else {
 			return pRequest.getParameterNames();
 		}
 	}
 
 	@Override
 	public String[] getParameterValues(String name) {
 		if (pRequest instanceof  SlingHttpServletRequestWrapper) {
 			// Convert parameter value to String
 			ArrayList<String> retParams = new ArrayList<String>();
 			for (RequestParameter par : ((SlingHttpServletRequestWrapper)pRequest).getRequestParameterMap().get(name)) {
 				// TODO: Encoding?
 				retParams.add(par.getString());
 			}
 			return retParams.toArray(new String[retParams.size()]);
 		} else {
 			return pRequest.getParameterValues(name);
 		}
 	}
 }
