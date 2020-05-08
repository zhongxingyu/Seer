 package com.worksnet.utils.filter;
 
import com.worksnet.system.Log;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import java.io.IOException;
 
 /**
  * User: maxim.levicky
  * Date: 6/25/13
  * Time: 12:39 PM
  */
 public class ErrorFilter implements Filter {
     private FilterConfig filterConfig;
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         this.filterConfig = filterConfig;
     }
 
     @Override
     public void destroy() {
         this.filterConfig = null;
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
             chain.doFilter(request, response);
         } catch (Throwable t) {
            Log.getLogger(ErrorFilter.class).error("Not catcher error.", t);
            request.getRequestDispatcher("/WEB-INF/views/error/generalError.jsp").forward(request, response);
         }
     }
 
 }
