 package com.ringfulhealth.demoapp.servlets;
 
 import com.ringfulhealth.demoapp.entity.StatusType;
 import com.ringfulhealth.demoapp.entity.User;
 import com.ringfulhealth.demoapp.services.UserManager;
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import java.util.logging.Logger;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 
 public class SecurityFilter implements Filter {
 
     private static final Logger log = Logger.getLogger(SecurityFilter.class.getName());
 
     FilterConfig config;
 
     static String[] safeList = new String[] 
         { "/", "index.jsp", "status.jsp",
           "signup.jsp", "/signup", "/msignup",
           "login.jsp", "/login", "/mlogin", 
           "register.jsp", "/register", "/mregister", "register_fail.jsp",
           "reset_password.jsp", "/reset_password", 
           "/ap", "/ae",
           ".css", ".js", ".png", ".jpg", ".gif", ".ico", ".eot", ".svg", ".ttf", ".woff", ".txt", ".html" };
     static String[] adminList = new String[] { 
         "admin_index.jsp"
     };
     
     @PersistenceUnit
     private EntityManagerFactory emf;
     
     public void destroy() {
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpSession session = httpRequest.getSession();
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         String uri = httpRequest.getRequestURI();
         
         boolean isSafe = isListed(safeList, uri);
         boolean isAdmin = isListed(adminList, uri);
         
         User user = (User) session.getAttribute("user");
         // Reload user for confirmation status
         if (user != null && (user.getEmailConfirmed() == 0 || user.getPhoneConfirmed() == 0)) {
             if (emf == null) {
                 // This is for Tomcat
                 emf = (EntityManagerFactory) config.getServletContext().getAttribute("emf");
             }
             UserManager um = new UserManager (emf);
             user = um.getUserByUsername(user.getUsername());
             session.setAttribute("user", user);
         }
 
         // if (!isSafe) {
         if (!isSafe && uri.endsWith("jsp")) {
             httpRequest.getSession().setAttribute("targetUrl", uri);
         }
         
         if (isSafe) {
             chain.doFilter(request, response);
         } else {
             if (user == null) {
                 // Not logged in
                 log.info("Rejecting " + uri + " as unsafe");
                 httpResponse.sendRedirect("login.jsp");
             } else {
                 // logged in.
                 if (isAdmin && user.getStatus() != StatusType.ADMIN) {
                     // No authorization
                     httpResponse.sendRedirect("status.jsp?error=You do not have permission to access this page");
                     return;
                 } else {
                     // has authorization. Move forward
                     chain.doFilter(request, response);
                 }
             }
         }
     }
 
     private boolean isListed(String list[], String uri) {
         for (String value : list) {
             if (uri.endsWith(value)) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean redirect (HttpServletRequest req, HttpServletResponse resp, String org, String dest) throws IOException {
         String uri = req.getRequestURI();
         String qs  = req.getQueryString();
         if (uri.endsWith(org)) {
             if (qs == null) {
                 resp.sendRedirect (dest);
             } else {
                 resp.sendRedirect (dest + "?" + qs);
             }
             return true;
         }
         return false;
     }
 
     public void init(FilterConfig config) throws ServletException {
         this.config = config;
     }
 
 }
