 package com.intalio.cloud.login;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 public class SessionFilter implements Filter {
 	
 	private Logger log = Logger.getLogger(this.getClass());
  
     private ArrayList<String> urlList;
  
     public void destroy() {
     }
  
     public void doFilter(ServletRequest req, ServletResponse res,
             FilterChain chain) throws IOException, ServletException {
  
         HttpServletRequest request = (HttpServletRequest) req;
         HttpServletResponse response = (HttpServletResponse) res;
         String url = request.getServletPath();
         log.debug("Url is: " + url);
         boolean allowedRequest = false;
         
         /*if(urlList.contains(url)) {
             allowedRequest = true;
         }*/
         
        if(url.equals("/login.jsp")){
        	allowedRequest = true;
        }
        log.debug("allowedRequest is: " + allowedRequest);
        if (!allowedRequest) {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            if (username == null) {
                response.sendRedirect("login.jsp");
             }
        }
        
         chain.doFilter(req, res);
     }
  
     public void init(FilterConfig config) throws ServletException {
         /*String urls = config.getInitParameter("avoid-urls");
         log.debug("url are: " + urls);
         StringTokenizer token = new StringTokenizer(urls, ",");
         
         urlList = new ArrayList<String>();
  
         while (token.hasMoreTokens()) {
             urlList.add(token.nextToken());
  
         }*/
     }
 }
