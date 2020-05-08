 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import br.octahedron.cotopaxi.metadata.annotation.Action.HTTPMethod;
 
 /**
  * Encapsulate and provides access to all request's information, such as URL, method, requested
  * format/locale and request parameters.
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class RequestWrapper implements InputHandler {
 
 	private static final Pattern FORMAT_SUFFIX = Pattern.compile(".+\\.[a-z]{3,4}$");
 	private static final Pattern LOCALE_PREFIX = Pattern.compile("^/[a-z]{2}(_[A-Z]{2})?/.*");
 
 	private String url;
 	private String format;
 	private String domain;
 	private Locale locale;
 	private boolean localeFromURL = false;
 	private HttpServletRequest request;
 	private Map<String, String> requestParameters;
 
 	public RequestWrapper(HttpServletRequest req) {
 		this.request = req;
 		this.extractURLparams();
 	}
 
 	private void extractURLparams() {
 		// get domain
 		this.domain = this.request.getServerName();
 		// get url
 		String context = this.request.getContextPath();
 		this.url = this.request.getRequestURI();
 		// remove the context from the uri, if present
 		if (context.length() > 0 && this.url.indexOf(context) == 0) {
 			this.url = this.url.substring(context.length());
 		}
 		// get locale
 		this.locale = this.request.getLocale();
 		if (LOCALE_PREFIX.matcher(this.url).matches()) {
 			int index = this.url.indexOf('/', 1);
 			String strLocale = this.url.substring(1, index);
 			String[] params = strLocale.split("_");
 			if (params.length == 1) {
 				this.locale = new Locale(params[0]);
 			} else {
 				// there's two parameters
 				this.locale = new Locale(params[0], params[1]);
 			}
 			this.url = this.url.substring(index);
 		}
 
 		// get format
 		if (FORMAT_SUFFIX.matcher(this.url).matches()) {
 			int index = this.url.lastIndexOf('.');
 			this.format = this.url.substring(index + 1);
 			this.url = this.url.substring(0, index);
 		}
 	}
 
 	/**
 	 * Gets the requested URL.
 	 * 
 	 * @return the requested URL.
 	 */
 	public String getURL() {
 		return this.url;
 	}
 
 	public HTTPMethod getHTTPMethod() {
 		return HTTPMethod.valueOf(this.request.getMethod());
 	}
 
 	public String getFormat() {
 		return this.format;
 	}
 
 	public Locale getLocale() {
 		return this.locale;
 	}
 
 	/**
 	 * Indicates if request locale was specified on URL, or using the Accept-Language header.
 	 * 
 	 * @return <code>true</code> if the locale was specified on URL, and <code>false</code> if it
 	 *         was specified on Header (or by server language).
 	 */
 	public boolean isLocaleFromURL() {
 		return this.localeFromURL;
 	}
 
 	public String getDomain() {
 		return this.domain;
 	}
 
 	public String getRequestParameter(String name) {
 		String param = null;
 		if (this.requestParameters != null && this.requestParameters.containsKey(name)) {
 			param = this.requestParameters.get(name);
 		} else {
 			param = this.request.getParameter(name);
 		}
 		return param;
 	}
 
 	/**
 	 * Sets a parameter to the request being wrapped by this entity.
 	 * 
 	 * @param key
 	 *            The parameter's name/key
 	 * @param value
 	 *            The parameter's value
 	 */
 	public void setRequestParameter(String key, String value) {
 		if (this.requestParameters == null) {
 			this.requestParameters = new HashMap<String, String>();
 		}
 		this.requestParameters.put(key, value);
 	}
 
 	@Override
 	public Object getSessionParameter(String name) {
 		HttpSession session = this.request.getSession(false);
 		if (session != null) {
			return this.request.getAttribute(name);
 		} else {
 			return null;
 		}
 	}
 
 	public void setSessionAttribute(String key, Object value) {
 		HttpSession session = this.request.getSession();
 		session.setAttribute(key, value);
 	}
 }
