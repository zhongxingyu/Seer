 package com.worksnet.utils.filter;
 
import com.worksnet.utils.DB;
import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
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
        try{
             chain.doFilter(request, response);
         } catch (Throwable t) {
            t.printStackTrace();
         }
     }
 
 }
