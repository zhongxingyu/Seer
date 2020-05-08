 package drinkcounter.filter;
 
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class CacheControlFilter implements Filter {
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
 
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

         HttpServletResponse resp = (HttpServletResponse) servletResponse;
         resp.setHeader("Cache-Control", "private, must-revalidate, max-age=0");
         resp.setDateHeader("Expires", 0L);
         chain.doFilter(request, servletResponse);
     }
 
     @Override
     public void destroy() {
 
     }
 }
