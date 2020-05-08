 package org.seamoo.webapp.filters;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openid4java.discovery.Identifier;
 import org.seamoo.utils.UrlBuilder;
 
 public class OpenIdAuthenticationFilter implements Filter {
 
 	protected String loginUri;
 
 	/**
 	 * This method is called by the servlet container to configure this filter; The init parameter "forwardUri" is required. The
 	 * relying party used will be the default {@link RelyingParty#getInstance() instance} if it is not found in the servlet
 	 * context attributes.
 	 */
 	public void init(FilterConfig config) throws ServletException {
 		loginUri = config.getInitParameter("loginUri");
 		if (loginUri == null)
 			throw new ServletException("loginUri must not be null.");
 	}
 
 	/**
 	 * Gets the configured forward uri.
 	 */
 	public String getLoginUri() {
 		return loginUri;
 	}
 
 	/**
 	 * Delegates to the filter chain if the user associated with this request is authenticated.
 	 */
 	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
 		if (handle((HttpServletRequest) req, (HttpServletResponse) res))
 			chain.doFilter(req, res);
 	}
 
 	public void destroy() {
 
 	}
 
 	/**
 	 * Returns true if the user associated with this request is authenticated.
 	 */
 	public boolean handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		Identifier identifier = (Identifier) request.getSession().getAttribute("identifier");
 		if (identifier == null) {
 			if (request.getRequestURI().startsWith(loginUri)) {
 				// route through the un-rewritten path to avoid filter
 				request.getRequestDispatcher("/app" + loginUri).forward(request, response);
 			} else {
 				String redirectUrl = String.format("%s?returnUrl=%s", loginUri, UrlBuilder.getEncodedUrl(
 						request.getRequestURI(), request.getQueryString()));
 				response.sendRedirect(redirectUrl);
 			}
 			return false;
 		}
 		return true;
 	}
 }
