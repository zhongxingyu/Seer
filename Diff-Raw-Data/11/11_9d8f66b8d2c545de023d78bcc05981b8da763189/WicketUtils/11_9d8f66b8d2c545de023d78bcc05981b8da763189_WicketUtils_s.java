 /**
  * Copyright 2010 Molindo GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package at.molindo.wicketutils.utils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.wicket.Application;
 import org.apache.wicket.IRequestTarget;
 import org.apache.wicket.Page;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.Response;
 import org.apache.wicket.Session;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.markup.html.link.AbstractLink;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.protocol.http.BufferedWebResponse;
 import org.apache.wicket.protocol.http.RequestUtils;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.WebRequestCycle;
 import org.apache.wicket.protocol.http.WebResponse;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;
 import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.request.RequestParameters;
 import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
 import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
 import org.apache.wicket.util.value.ValueMap;
 
 public final class WicketUtils {
 	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WicketUtils.class);
 
 	private WicketUtils() {
 		// no instance
 	}
 
 	public static Class<? extends Page> getBookmarkablePage(final RequestCycle cycle) {
 		if (cycle == null) {
 			return null;
 		}
 
 		final IRequestTarget target = cycle.getRequestTarget();
 		if (target instanceof BookmarkablePageRequestTarget) {
 			return ((BookmarkablePageRequestTarget) target).getPageClass();
 		}
 
 		return null;
 	}
 
 	public static boolean isBookmarkableRequest(final URL url) {
 		// if referer is one of our bookmarkable pages, suggest a link to that
 		// page
 		final IRequestTarget rt = getRequestTarget(url);
 		return rt instanceof IBookmarkablePageRequestTarget;
 	}
 
 	public static Class<? extends Page> getBookmarkablePage(final URL url) {
 		final IRequestTarget rt = getRequestTarget(url);
 		if (rt instanceof IBookmarkablePageRequestTarget) {
 			final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget) rt;
 			return target.getPageClass();
 		} else {
 			return null;
 		}
 	}
 
 	public static IBookmarkablePageRequestTarget getBookmarkableRequestTarget(final URL url) {
 		final IRequestTarget rt = getRequestTarget(url);
 		if (rt instanceof IBookmarkablePageRequestTarget) {
 			return (IBookmarkablePageRequestTarget) rt;
 		} else {
 			return null;
 		}
 	}
 
 	public static IRequestTarget getRequestTarget(final URL url) {
 		if (url != null) {
 			final RequestCycle rc = RequestCycle.get();
 			final IRequestCycleProcessor processor = rc.getProcessor();
 			final RequestParameters requestParameters = processor.getRequestCodingStrategy().decode(
 					new UrlRequest(rc.getRequest(), url));
 
 			try {
 				return processor.resolve(rc, requestParameters);
 			} catch (final WicketRuntimeException e) {
 				// ignore
 			}
 
 		}
 		return null;
 	}
 
 	public static AbstractLink getBookmarkableRefererLink(final String id, final IModel<String> labelModel) {
 		final String referer = getReferer();
 		if (referer == null) {
 			return null;
 		}
 		try {
 			if (isBookmarkableRequest(new URL(referer))) {
 				return new ExternalLink(id, new Model<String>(referer), labelModel);
 			}
 		} catch (final MalformedURLException e) {
 			log.warn("malformed referer url: " + referer + " (" + e.toString() + ")");
 		}
 		return null;
 	}
 
 	public static String getReferer() {
 		return getHttpServletRequest().getHeader("Referer");
 	}
 
 	public static class UrlRequest extends Request {
 		private final ValueMap params = new ValueMap();
 
 		private final Request realRequest;
 
 		private final URL url;
 
 		/**
 		 * Construct.
 		 * 
 		 * @param realRequest
 		 * @param url
 		 */
 		public UrlRequest(final Request realRequest, final URL url) {
 			this.realRequest = realRequest;
 			this.url = url;
 
 			final String query = url.getQuery();
 			if (query != null) {
 				RequestUtils.decodeParameters(query, params);
 			}
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getLocale()
 		 */
 		@Override
 		public Locale getLocale() {
 			return realRequest.getLocale();
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getParameter(java.lang.String)
 		 */
 		@Override
 		public String getParameter(final String key) {
 			return (String) params.get(key);
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getParameterMap()
 		 */
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		@Override
 		public Map getParameterMap() {
 			return params;
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getParameters(java.lang.String)
 		 */
 		@Override
 		public String[] getParameters(final String key) {
 			final String param = (String) params.get(key);
 			if (param != null) {
 				return new String[] { param };
 			}
 			return new String[0];
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getPath()
 		 */
 		@Override
 		public String getPath() {
 			String path = url.getPath();
 			if (path.startsWith("/")) {
 				path = path.substring(1);
 			}
 			return path;
 		}
 
 		@Override
 		public String getRelativePathPrefixToContextRoot() {
 			throw new NotImplementedException();
 		}
 
 		@Override
 		public String getRelativePathPrefixToWicketHandler() {
 			throw new NotImplementedException();
 		}
 
 		/**
 		 * @see org.apache.wicket.Request#getURL()
 		 */
 		@Override
 		public String getURL() {
 			return url.toString();
 		}
 
 		@Override
 		public String getQueryString() {
 			return realRequest.getQueryString();
 		}
 	}
 
 	public static String getRequested() {
 		final HttpServletRequest req = getHttpServletRequest();
 		final StringBuilder buf = new StringBuilder();
 		buf.append(req.getServletPath());
 		final String path = req.getPathInfo();
 		if (path != null) {
 			buf.append(path);
 		}
 		final String query = req.getQueryString();
 		if (query != null) {
 			buf.append("?").append(query);
 		}
 		return buf.toString();
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static HttpServletRequest getHttpServletRequest() {
 		WebRequest wr = getWebRequest();
 		return wr == null ? null : wr.getHttpServletRequest();
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static HttpServletResponse getHttpServletResponse() {
 		WebResponse wr = getWebResponse();
 		return wr == null ? null : wr.getHttpServletResponse();
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static HttpSession getHttpSession() {
 		HttpServletRequest r = getHttpServletRequest();
 		return r == null ? null : r.getSession();
 	}
 
 	public static String getRemoteAddr() {
 		return getHttpServletRequest().getRemoteAddr();
 	}
 
 	public static String getRequestParameter(final String name) {
 		return getHttpServletRequest().getParameter(name);
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static Request getRequest() {
 		final RequestCycle rc = RequestCycle.get();
 		return rc == null ? null : rc.getRequest();
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static WebRequest getWebRequest() {
 		final Request request = getRequest();
 		return request instanceof WebRequest ? (WebRequest) request : null;
 	}
 
 	public static WebRequestCycle getWebRequestCycle() {
 		final RequestCycle rc = RequestCycle.get();
 		return rc instanceof WebRequestCycle ? (WebRequestCycle) rc : null;
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static Response getResponse() {
 		final RequestCycle rc = RequestCycle.get();
 		return rc == null ? null : rc.getResponse();
 	}
 
 	/**
 	 * @return may return null
 	 */
 	public static WebResponse getWebResponse() {
 		final Response response = getResponse();
 		return response instanceof WebResponse ? (WebResponse) response : null;
 	}
 
 	public static boolean isHttps() {
 		return "https".equalsIgnoreCase(getHttpServletRequest().getScheme());
 	}
 
 	public static Cookie getCookie(final String name) {
 		final Cookie[] cookies = getHttpServletRequest().getCookies();
 		if (cookies == null) {
 			return null;
 		}
 		for (final Cookie c : cookies) {
 			if (name.equals(c.getName())) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public static void deleteCookie(final String name) {
 		final Cookie c = new Cookie(name, "");
 		c.setMaxAge(0);
 		getHttpServletResponse().addCookie(c);
 	}
 
 	public static String getHeader(final String name) {
 		final HttpServletRequest r = getHttpServletRequest();
 		return r == null ? null : r.getHeader(name);
 	}
 
 	public static WebClientInfo getClientInfo() {
 		return (WebClientInfo) Session.get().getClientInfo();
 	}
 
 	public static String getUserAgent() {
 		final WebClientInfo info = getClientInfo();
 		return info == null ? null : info.getUserAgent();
 	}
 
 	/**
 	 * @return true if cookies are disabled, false if unknown
 	 */
 	public static boolean isCookiesDisabled() {
 		final WebClientInfo info = getClientInfo();
 		return info == null || info.getProperties() == null ? false : !info.getProperties().isCookiesEnabled();
 
 	}
 
 	public static String getClientInfoString() {
 		final WebClientInfo info = getClientInfo();
 		return info == null ? null : info.getUserAgent() + " (" + info.getProperties().getRemoteAddress() + ")";
 	}
 
 	public static String getRequestContextPath() {
 		final HttpServletRequest r = getHttpServletRequest();
 		if (r == null) {
 			return null;
 		}
 		try {
 			final URL url = new URL(r.getRequestURL().toString());
 			final StringBuilder buf = new StringBuilder(100);
 
 			buf.append(url.getProtocol()).append("://").append(url.getHost());
 			if (url.getPort() != -1) {
 				buf.append(":").append(url.getPort());
 			}
 			return buf.append(r.getContextPath()).toString();
 		} catch (final MalformedURLException e) {
 			throw new WicketRuntimeException("client sent an illegal url?", e);
 		}
 
 	}
 
 	public static String getHost() {
 		final HttpServletRequest r = getHttpServletRequest();
 		if (r == null) {
 			return null;
 		}
 		try {
 			return new URL(r.getRequestURL().toString()).getHost();
 		} catch (final MalformedURLException e) {
 			throw new WicketRuntimeException("client sent an illegal url?", e);
 		}
 	}
 
 	public static String toAbsolutePath(final Class<? extends Page> pageClass) {
 		return toAbsolutePath(pageClass, null);
 	}
 
 	public static String toAbsolutePath(final Class<? extends Page> pageClass, final PageParameters parameters) {
 		return RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(pageClass, parameters).toString());
 	}
 
 	public static void performRedirect(final String targetURL, final int statusCode) {
 		final BufferedWebResponse response = (BufferedWebResponse) RequestCycle.get().getResponse();
 		response.getHttpServletResponse().setHeader("Location", targetURL);
		throw new AbortWithHttpStatusException(HttpServletResponse.SC_MOVED_PERMANENTLY, true);
 	}
 
 	public static void performRedirect(final Class<? extends Page> pageClass, final PageParameters parameters,
 			final int statusCode) {
 		performRedirect(toAbsolutePath(pageClass, parameters), statusCode);
 	}
 
 	public static URL toUrl(final Class<? extends Page> pageClass) {
 		return toUrl(pageClass, null);
 	}
 
 	public static URL toUrl(final Class<? extends Page> pageClass, final PageParameters params) {
 		final String url = toAbsolutePath(pageClass, params);
 		try {
 			return new URL(url);
 		} catch (final MalformedURLException e) {
 			throw new WicketRuntimeException("failed to create URL from " + url, e);
 		}
 	}
 
 	/**
 	 * @return <code>true</code> if header "Wicket-Ajax" is set
 	 * @see ServletWebRequest#isAjax()
 	 */
 	public static boolean isAjax() {
 		Request req = getRequest();
 		if (req instanceof ServletWebRequest) {
 			return ((ServletWebRequest) req).isAjax();
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean isDeployment() {
 		// should this be cached?
 		return Application.DEPLOYMENT.equals(Application.get().getConfigurationType());
 	}
 }
