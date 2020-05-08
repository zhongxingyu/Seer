 package com.webquiz.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import com.webquiz.servlet.WebQuizServlet;
 
 public class SessionFilter implements Filter {
 
     private FilterConfig config;
     
     private static final Logger log = Logger.getLogger(WebQuizServlet.class);
 
     @Override
     public void init(FilterConfig config) throws ServletException {
         this.config = config;
     }
 
     private String getParameter(HttpServletRequest request, String name, String def) {
         String val = request.getParameter(name);
         return (val != null) ? val : def;
     }
 
     private String getParameter(HttpServletRequest request, String name) {
         return getParameter(request, name, "");
     }
 
     @Override
     public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
             ServletException {
 
         if (req instanceof HttpServletRequest) {
             HttpServletRequest request = (HttpServletRequest) req;
 
             String uri = request.getRequestURI();
 
             // allow certain files to skip this
             if (!(uri.indexOf("/css") > 0 || uri.indexOf("/images") > 0 || uri.indexOf("/js") > 0)) {
 
                 String action = getParameter(request, "action");
                 
                if (!(action.equals("LOGIN") || action.equals("ABOUT") || action.equals("NEWUSER"))) {
                     HttpSession session = request.getSession();
                     if (session.getAttribute("user") == null) {
                         String newURI = "/login.jsp";
                         req.getRequestDispatcher(newURI).forward(req, res);
                         log.debug("forward to login.jsp");
                         return;
                     }
                 }
             }
         }
 
         chain.doFilter(req, res);
     }
 
     @Override
     public void destroy() {
         this.config = null;
     }
 }
