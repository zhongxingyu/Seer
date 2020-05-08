 package uk.ac.cam.cl.dtg.teaching;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ResourceFilter implements Filter {
 	private static Logger log = LoggerFactory.getLogger(ResourceFilter.class);
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 			log.info("Initialised Resource Filter");
 		}
 
 	@Override
 	public void doFilter(ServletRequest servletReq, ServletResponse servletResp,
 			FilterChain chain) throws IOException, ServletException {
 
 		// Convert arguments to HTTP ones to allow grabbing session etc.
 		HttpServletRequest request = (HttpServletRequest) servletReq;
 		HttpServletResponse response = (HttpServletResponse) servletResp;
 		HttpSession session = request.getSession();
 
 		String path = request.getRequestURI();
 		if(path!=null){
 			log.info("Stopping filter chain for " + path);
 			String context = request.getContextPath();
 			path = path.replaceFirst(context, ""); // Trim context path from request forwarder
						
 			request.getRequestDispatcher(path).forward(request, response);
 		}
 	}
 
 	@Override
 	public void destroy() {
 		// Nothing to do
 	}
 }
 
