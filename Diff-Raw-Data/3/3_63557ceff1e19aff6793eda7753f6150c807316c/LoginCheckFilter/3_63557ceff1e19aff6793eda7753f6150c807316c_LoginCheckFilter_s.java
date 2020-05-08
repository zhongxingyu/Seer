 package sjsu.cs157a.dbpro.servlet.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 public class LoginCheckFilter implements Filter {
 
 	private static final Logger logger = Logger
 			.getLogger(LoginCheckFilter.class);
 
 	private static final String SIGN_IN_PATH = "/signin";
 
 	private static final String REGISTER_PATH = "/register";
 
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest) request;
 		HttpServletResponse resp = (HttpServletResponse) response;
 		String path = req.getServletPath();
 		// place your code here
 		logger.info("before chain.doFilter(): " + path);
 		if (!path.equals(SIGN_IN_PATH) && !path.equals(REGISTER_PATH)
 				&& path.indexOf('.') == -1) {
 			if (req.getSession().getAttribute("username") == null) {
 				logger.warn("unexpected access to path: " + path);
				resp.sendRedirect(req.getServletContext().getContextPath()
						+ SIGN_IN_PATH);
 				return;
 			}
 		}
 		// pass the request along the filter chain
 		chain.doFilter(request, response);
 		logger.info("after chain.doFilter(): " + path);
 	}
 
 	public void init(FilterConfig fConfig) throws ServletException {
 		logger.info("filter init...");
 	}
 
 	@Override
 	public void destroy() {
 		logger.info("filter destroy...");
 	}
 
 }
