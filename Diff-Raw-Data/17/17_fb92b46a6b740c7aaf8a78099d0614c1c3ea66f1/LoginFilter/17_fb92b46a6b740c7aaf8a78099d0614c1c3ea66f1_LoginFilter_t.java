 package server;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet Filter implementation class LoginFilter
  */
 @WebFilter("/LoginFilter")
 public class LoginFilter implements Filter {
 
     /**
      * Default constructor. 
      */
     public LoginFilter() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Filter#destroy()
 	 */
 	public void destroy() {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
 	 */
	/*
	 * Filters all the request which are directed towards the General & Secured Section of the WebContent  
	 *  i.e. You need to have valid loginId for accessing this page
	 *  */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 			throws IOException, ServletException {
 		System.out.println("In LoginFilter");
 		HttpSession session = ((HttpServletRequest)request).getSession(false);
 		
     	if ( (session != null) && (session.getAttribute("user") != null) 
     			&& (session.getAttribute("events") != null)) {
     		System.out.println ("FILTER user!= null");
     		System.out.println ("FILTER user "+ session.getAttribute("user"));
     		System.out.println ("FILTER events "+ session.getAttribute("events"));
     		
     		// pass the request along the filter chain
     		chain.doFilter(request, response);
     	}
     	else {
     		if (session != null) {
 	    		System.out.println ("FILTER user "+ session.getAttribute("user"));
 	    		session.removeAttribute("user");
 	    		session.invalidate();
 	    		request.getRequestDispatcher("/Login.jsp").forward(request, response);
 	    		return;
     		}
     		if (request.getAttribute("posts") == null) 
     			request.getRequestDispatcher("/FetchPosts").forward(request, response);
     		else
     			request.getRequestDispatcher("/Login.jsp").forward(request, response);
     	}
 	}
 
 	/**
 	 * @see Filter#init(FilterConfig)
 	 */
 	public void init(FilterConfig fConfig) throws ServletException {
 		// TODO Auto-generated method stub
 	}
 
 }
