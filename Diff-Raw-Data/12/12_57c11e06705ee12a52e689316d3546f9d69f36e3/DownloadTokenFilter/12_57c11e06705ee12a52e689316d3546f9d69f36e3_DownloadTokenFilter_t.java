 package gov.usgs;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class DownloadTokenFilter implements Filter {
 
 	public static final String TOKEN_NAME = "downloadToken";
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		String token = request.getParameter(TOKEN_NAME);
 		if (token != null) {
 			if (response instanceof HttpServletResponse) {
 				HttpServletResponse hresp = (HttpServletResponse) response;
 				Cookie cookie = new Cookie(TOKEN_NAME, token);
				String path = "/";
				if (request instanceof HttpServletRequest) {
					HttpServletRequest hreq = (HttpServletRequest) request;
					path = hreq.getContextPath();
					if ("".equals(path)) {
						path = "/";
					}
				}
				cookie.setPath(path);
 				
 				String server = request.getServerName();
 				
 				if (server.contains("er.usgs.gov")) {
 					cookie.setDomain(".er.usgs.gov");
 				}
 				cookie.setVersion(1);
 
 				hresp.addCookie(cookie);
 				
 				// also add a v0 token for old browsers
 				cookie = new Cookie(TOKEN_NAME, token);
 				hresp.addCookie(cookie);
 				
 			} else {
 				System.err.println("got token, but not HttpServletResponse, actual type = " + response.getClass().getName());
 			}
 		}
 		chain.doFilter(request, response);
 	}
 
 	@Override
 	public void init(FilterConfig fConfig) throws ServletException {
 		// nothing to do here
 	}
 
 	@Override
 	public void destroy() {
 		// nothing to do here		
 	}
 
 }
