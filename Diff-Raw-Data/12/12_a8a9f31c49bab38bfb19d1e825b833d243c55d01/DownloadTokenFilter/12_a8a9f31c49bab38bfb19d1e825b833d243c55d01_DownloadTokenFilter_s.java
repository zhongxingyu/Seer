 package gov.usgs;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 
 public class DownloadTokenFilter implements Filter {
 
 	public static final String TOKEN_NAME = "downloadToken";
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		String token = request.getParameter(TOKEN_NAME);
 		if (token != null) {
 			if (response instanceof HttpServletResponse) {
 				HttpServletResponse hresp = (HttpServletResponse) response;
				hresp.addCookie(new Cookie(TOKEN_NAME, token));
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
