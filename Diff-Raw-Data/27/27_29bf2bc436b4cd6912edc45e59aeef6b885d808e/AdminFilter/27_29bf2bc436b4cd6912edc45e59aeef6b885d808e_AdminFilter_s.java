 package main;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet Filter implementation class SecureFilter
  */
 @WebFilter("/addcar.jsp")
 public class AdminFilter implements Filter {
 
     /**
      * Default constructor. 
      */
     public AdminFilter() {
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
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		if(((HttpServletRequest)request).getSession().getAttribute("kullanici") != "admin") {
 			((HttpServletResponse)response).sendRedirect("warning.jsp");
 		}
 		
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 * @see Filter#init(FilterConfig)
 	 */
 	public void init(FilterConfig fConfig) throws ServletException {
 		// TODO Auto-generated method stub
 	}
 
 }
