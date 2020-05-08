 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.WikiBase;
 import org.jamwiki.utils.Utilities;
 
 /**
  * Make sure character encoding is properly set to UTF-8.  See
  * http://wiki.apache.org/tomcat/Tomcat/UTF-8 for a further discussion.
  */
 public class JAMWikiFilter implements Filter {
 
 	private static Logger logger = Logger.getLogger(JAMWikiFilter.class.getName());
 	private String encoding = "UTF-8";
 	private FilterConfig config = null;
	/** A flag to indicate whether the server supports the response.setCharacterEncoding API. */
	private static boolean SERVLET24_SUPPORT = true;
 
 	/**
 	 *
 	 */
 	public void destroy() {
 	}
 
 	/**
 	 * Set request encoding to UTF-8.
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(this.encoding);
		setResponseEncoding(response);
 		if (redirectNeeded(request, response)) return;
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 *
 	 */
 	public void init(FilterConfig config) throws ServletException {
 		this.encoding = config.getInitParameter("encoding");
 		this.config = config;
 	}
 
 	/**
 	 *
 	 */
 	private boolean redirectNeeded(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
 		if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
 			return false;
 		}
 		HttpServletRequest request = (HttpServletRequest)servletRequest;
 		HttpServletResponse response = (HttpServletResponse)servletResponse;
 		if (Utilities.isFirstUse() && !JAMWikiServlet.isTopic(request, "Special:Setup")) {
 			// redirect to setup page
 			String url = request.getContextPath() + "/" + WikiBase.DEFAULT_VWIKI + "/Special:Setup";
 			redirect(request, response, url);
 			return true;
 		} else if (Utilities.isUpgrade() && !JAMWikiServlet.isTopic(request, "Special:Upgrade")) {
 			// redirect to upgrade page
 			String url = request.getContextPath() + "/" + WikiBase.DEFAULT_VWIKI + "/Special:Upgrade";
 			redirect(request, response, url);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	private void redirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException, ServletException {
 		response.sendRedirect(url);
 		return;
 	}

	/**
	 *
	 */
	private void setResponseEncoding(ServletResponse response) {
		// try to set the response encoding.  this method is servlet 2.4 and
		// later only, so use reflection in case of a servlet 2.3 container.
		if (!SERVLET24_SUPPORT) return;
		try {
			Class[] parameterTypes = null;
			Method m = response.getClass().getMethod("setCharacterEncoding", new Class[]{String.class});
			Object[] args = new Object[]{this.encoding};
			m.invoke(response, args);
		} catch (Exception e) {
			logger.warn("Unable to set response encoding.  For full UTF-8 support consider upgrading to a web application server that supports the servlet 2.4 standard.");
			SERVLET24_SUPPORT = false;
		}
	}
 }
