 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se.wallenius.web.filter;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Cross-site headers to support loading from other domains.
  *
  * @author fredrik
  */
 public class CrossOriginHeaderFilter implements Filter {
 
     @Override
     public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
             throws IOException, ServletException {
         
         final HttpServletResponse response = (HttpServletResponse) res;
         final HttpServletRequest request = (HttpServletRequest) req;
 
       
         response.setHeader("Access-Control-Allow-Origin", "*");
         response.setHeader("Access-Control-Allow-Methods", "GET");
 
         chain.doFilter(req, res);
     }
 
     @Override
     public void init(final FilterConfig filterConfig) {
         //Nothing to do
     }
 
     @Override
     public void destroy() {
         //Nothing to do
     }
 }
