 package Filters;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 public class AuthFilter implements Filter {
 
 	public void init(FilterConfig filterConfig) throws ServletException {}
 
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		
	    HttpServletRequest req= (HttpServletRequest) request;
		HttpSession session = req.getSession();
		String userAuth = (String) session.getAttribute("userAuth");
		String url = req.getRequestURL().toString();
	    String route = url.substring(url.lastIndexOf("/")).replace("/", "");
	    
		if((userAuth != null && userAuth.equals("true")) 
				|| route.equals("style.css") || route.equals("scripts.js")
				|| route.equals("jquery-1.10.2.min.js") || route.equals("shattered.png")) {
 			chain.doFilter(request, response);
 		} else {
 			RequestDispatcher rs = request.getRequestDispatcher("/login");
 			rs.forward(request, response);
 		}
 	}
 
 	public void destroy() {};
 }
