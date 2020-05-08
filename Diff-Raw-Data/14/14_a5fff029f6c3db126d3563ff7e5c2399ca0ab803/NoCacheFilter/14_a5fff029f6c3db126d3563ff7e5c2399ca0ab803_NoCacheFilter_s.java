 package ru.tsystems.karpova.filters;
 
 import javax.servlet.*;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
@WebFilter(servletNames = "Faces Servlet", urlPatterns = "*.xhtml")
 public class NoCacheFilter implements Filter {
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
 
     }
 
     @Override
     public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
         HttpServletResponse hsr = (HttpServletResponse) res;
         hsr.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
         hsr.setHeader("Pragma", "no-cache"); // HTTP 1.0.
         hsr.setDateHeader("Expires", 0); // Proxies.
         chain.doFilter(req, res);
     }
 
     @Override
     public void destroy() {
 
     }
 }
