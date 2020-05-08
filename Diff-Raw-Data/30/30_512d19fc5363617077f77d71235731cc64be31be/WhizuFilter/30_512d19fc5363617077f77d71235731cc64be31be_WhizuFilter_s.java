 /*******************************************************************************
  * Copyright (c) 2013 Rudy D'hauwe @ Whizu
  * Licensed under the EUPL V.1.1
  *   
  * This Software is provided to You under the terms of the European 
  * Union Public License (the "EUPL") version 1.1 as published by the 
  * European Union. Any use of this Software, other than as authorized 
  * under this License is strictly prohibited (to the extent such use 
  * is covered by a right of the copyright holder of this Software).
  *
  * This Software is provided under the License on an "AS IS" basis and 
  * without warranties of any kind concerning the Software, including 
  * without limitation merchantability, fitness for a particular purpose, 
  * absence of defects or errors, accuracy, and non-infringement of 
  * intellectual property rights other than copyright. This disclaimer 
  * of warranty is an essential part of the License and a condition for 
  * the grant of any rights to this Software.
  *   
  * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
  *
  * Contributors:
  *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
  *******************************************************************************/
 package org.whizu.server;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.whizu.jquery.RequestContext;
 
 /**
  * @author Rudy D'hauwe
  */
 public class WhizuFilter implements Filter {
 
 	private static final Logger log = LoggerFactory.getLogger(WhizuFilter.class);
 
 	private RequestDispatcher requestDispatcher_;
 
 	@Override
 	public void destroy() {
 		requestDispatcher_ = null;
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
 			throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest) req;
 		HttpServletResponse response = (HttpServletResponse) res;
 
 		/**
 		 * Returns the part of this request's URL from the protocol name up to the
 		 * query string in the first line of the HTTP request. The web container
 		 * does not decode this string.
 		 */
		//log.debug("HttpServletRequest.requestURI {}", request.getRequestURI());
 
 		/**
 		 * Returns the portion of the request URI that indicates the context of the
 		 * request. The context path always comes first in a request URI. The path
 		 * starts with a "/" character but does not end with a "/" character. For
 		 * servlets in the default (root) context, this method returns "". The
 		 * container does not decode this string.
 		 * 
 		 * It is possible that a servlet container may match a context by more than
 		 * one context path. In such cases this method will return the actual
 		 * context path used by the request and it may differ from the path
 		 * returned by the ServletContext.getContextPath() method.
 		 */
 		//log.debug("HttpServletRequest.contextPath {}", request.getContextPath());
 
 		/**
 		 * Returns the context path of the web application. The context path is the
 		 * portion of the request URI that is used to select the context of the
 		 * request. The context path always comes first in a request URI. The path
 		 * starts with a / character but does not end with a / character. For
 		 * servlets in the default (root) context, this method returns "".
 		 * 
 		 * It is possible that a servlet container may match a context by more than
 		 * one context path. In such cases the HttpServletRequest.getContextPath()
 		 * will return the actual context path used by the request and it may
 		 * differ from the path returned by this method.
 		 */
 		//log.debug("ServletContext.contextPath {}", request.getServletContext().getContextPath());
 
 		// dispatch and process the request if possible
 		boolean dispatched = requestDispatcher_.dispatch(request, response);
 
 		// if the request could not be dispatched
 		if (!dispatched) {
 			// forward the request to the next filter or resource in the chain
 			log.debug("Forwarding request {} onto the FilterChain", request.getRequestURI());
 			chain.doFilter(request, response);
 		}
 	}
 
 	@Override
 	public void init(FilterConfig config)
 			throws ServletException {
 		RequestContext.setInstance(new RequestContextImpl(config.getServletContext().getContextPath()));
 		requestDispatcher_ = RequestDispatcherBuilder.createFromFilterConfig(config).build();
 	}
 }
