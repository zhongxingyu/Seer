 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package filter;
 
 import ERMS.entity.EmployeeEntity;
 import ERMS.entity.FunctionalityEntity;
 import ERMS.entity.RoleEntity;
 import ERMS.session.EmployeeSessionBean;
 import Exception.ExistException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
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
  *
  * @author Ser3na
  */
 @WebFilter(filterName = "IRMSFilter", urlPatterns = {"*.xhtml"})
 public class IRMSFilter implements Filter {
 
     private static final boolean debug = true;
     // The filter configuration object we are associated with.  If
     // this value is null, this filter instance is not currently
     // configured. 
     private FilterConfig filterConfig = null;
     @EJB
     EmployeeSessionBean employeeManager;
 
     public IRMSFilter() {
     }
 
     private void doBeforeProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
         if (debug) {
             //log("IRMSFilter:DoBeforeProcessing");
         }
 
         // Write code here to process the request and/or response before
         // the rest of the filter chain is invoked.
 
         // For example, a logging filter might log items on the request object,
         // such as the parameters.
 	/*
          for (Enumeration en = request.getParameterNames(); en.hasMoreElements(); ) {
          String name = (String)en.nextElement();
          String values[] = request.getParameterValues(name);
          int n = values.length;
          StringBuffer buf = new StringBuffer();
          buf.append(name);
          buf.append("=");
          for(int i=0; i < n; i++) {
          buf.append(values[i]);
          if (i < n-1)
          buf.append(",");
          }
          log(buf.toString());
          }
          */
     }
 
     private void doAfterProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
         if (debug) {
             //  log("IRMSFilter:DoAfterProcessing");
         }
 
         // Write code here to process the request and/or response after
         // the rest of the filter chain is invoked.
 
         // For example, a logging filter might log the attributes on the
         // request object after the request has been processed. 
 	/*
          for (Enumeration en = request.getAttributeNames(); en.hasMoreElements(); ) {
          String name = (String)en.nextElement();
          Object value = request.getAttribute(name);
          log("attribute: " + name + "=" + value.toString());
 
          }
          */
 
         // For example, a filter might append something to the response.
 	/*
          PrintWriter respOut = new PrintWriter(response.getWriter());
          respOut.println("<P><B>This has been appended by an intrusive filter.</B>");
          */
     }
 
     /**
      *
      * @param request The servlet request we are processing
      * @param response The servlet response we are creating
      * @param chain The filter chain we are processing
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet error occurs
      */
     public void doFilter(ServletRequest request, ServletResponse response,
             FilterChain chain) throws IOException, ServletException {
 
         HttpServletRequest req = (HttpServletRequest) request;
         HttpServletResponse resp = (HttpServletResponse) response;
 
         String pagePath = req.getServletPath();
         if (req.getSession(true).getAttribute("isLogin") == null) {
             req.getSession(true).setAttribute("isLogin", false);
         }
         doBeforeProcessing(request, response);
 
         Boolean isLogin = (Boolean) req.getSession(true).getAttribute("isLogin");
 
         Throwable problem = null;
         try {
 
             System.err.println("This is IRMS Filter");
 
             if (!excludeLoginCheck(pagePath)) {
 
                 if (isLogin == true) {
                     String employeeId = (String) req.getSession().getAttribute("userId");
 
                     if ((!excludeRoleCheck(pagePath)) && (!excludeAdminCheck(employeeId, pagePath))) {
 
                         if (checkAccessRight(employeeId, pagePath)) {
                             chain.doFilter(request, response);
                         } else {
                             req.getRequestDispatcher("/commonInfrastructure/AccessDeniedPage.xhtml").forward(req, resp);
                         }
                     } else {
                         chain.doFilter(request, response);
                     }
 
                 } else {
                     req.getSession(true).setAttribute("lastAction", pagePath);
                     req.getRequestDispatcher("/commonInfrastructure/login.xhtml").forward(req, resp);
                 }
 
 
             } else {
                 chain.doFilter(request, response);
             }
 
 
         } catch (Throwable t) {
             // If an exception is thrown somewhere down the filter chain,
             // we still want to execute our after processing, and then
             // rethrow the problem after that.
             problem = t;
             t.printStackTrace();
         }
 
         doAfterProcessing(request, response);
 
         // If there was a problem, we want to rethrow it if it is
         // a known type, otherwise log it.
         if (problem != null) {
             if (problem instanceof ServletException) {
                 throw (ServletException) problem;
             }
             if (problem instanceof IOException) {
                 throw (IOException) problem;
             }
             sendProcessingError(problem, response);
         }
     }
 
     private Boolean excludeAdminCheck(String employeeId, String path) throws ExistException {
 
         //System.err.println("excludeAdminCheck...");
 
         EmployeeEntity user = employeeManager.getEmployeeById(employeeId);
         //System.err.println("excludeAdminCheck: " + user.getEmployeeName());
         List<String> userType = new ArrayList<String>();
 
         for (int i = 0; i < user.getRoles().size(); i++) {
             userType.add(user.getRoles().get(i).getRoleName());
         }
 
 
         if (userType.contains("SuperAdmin")) {
             if (path.contains("accountManagement")) {
                 return true;
             }
         } else if (userType.contains("ACMSAdmin")) {
             if (path.contains("acms")) {
                 return true;
             }
         } else if (userType.contains("FBMSAdmin")) {
             if (path.contains("fbms")) {
                 return true;
             }
         } else if (userType.contains("CRMSAdmin")) {
             if (path.contains("crms")) {
                 return true;
             }
         } else if (userType.contains("CEMSAdmin")) {
             if (path.contains("cems")) {
                 return true;
             }
         } else if (userType.contains("SMMSAdmin")) {
             if (path.contains("smms")) {
                 return true;
             }
         } else if (userType.contains("ATMSAdmin")) {
             if (path.contains("atms")) {
                 return true;
             }
         } else if (userType.contains("ESMSAdmin")) {
             if (path.contains("esms")) {
                 return true;
             }
         } else {
             return false;
         }
         return false;
 
     }
 
     private Boolean excludeRoleCheck(String path) {
         if (path.contains("commonInfrastructure")
                 || path.contains("message")
                 || path.contains("utility")
                 || path.endsWith("/")) {
             return true;
         } else {
             return false;
         }
     }
 
     private Boolean excludeLoginCheck(String path) {
         if (path.contains("index.xhtml")
                 || path.contains("login.xhtml")
                 || path.contains("initialization.xhtml")
                 || path.contains("AccessDeniedPage.xhtml")
                 || path.startsWith("/javax.faces.resource")
                 || path.startsWith("/resources")
                 || path.contains("resetPassword.xhtml")
                || path.contains("resetPasswordResult.xhtml")
                || path.contains("firstTimeLoginPwdChangeResult.xhtml")
                 || path.endsWith("/")) {
             return true;
         } else {
             return false;
         }
     }
 
     private Boolean checkAccessRight(String employeeId, String path) throws ExistException {
         //System.err.println("check access right");
         if (path.equals("/test.xhtml") || path.equals("/error.xhtml") || path.equals("/initialization.xhtml")) {
             return true;
         } else {
             EmployeeEntity employee = employeeManager.getEmployeeById(employeeId);
             String accessRight = path.replaceAll(".xhtml", "");
             accessRight = accessRight.substring(1);
 
             Boolean flag = false;
             List<RoleEntity> roleList = employee.getRoles();
             for (RoleEntity role : roleList) {
                 //System.out.println("first for loop...");
                 List<FunctionalityEntity> functionalityList = role.getFunctionalities();
                 for (FunctionalityEntity functionality : functionalityList) {
                     //System.err.println("functionality name: " + functionality.getFuncName());
                     if (accessRight.contains(functionality.getFuncName())) {
                         flag = true;
                         //System.err.println("flag in:" + flag);
                         break;
                     }
                 }
             }
 
 
 //            EmployeeEntity user = employeeManager.getEmployeeById(employeeId);
 //            List<String> userType = new ArrayList<String>();
 //            for (int i = 0; i < user.getRoles().size(); i++) {
 //                userType.add(user.getRoles().get(i).getRoleName());
 //            }
 //            if (userType.contains("SuperAdmin")) {
 //            }
             System.err.println("flag out:" + flag);
             return flag;
         }
     }
 
     /**
      * Return the filter configuration object for this filter.
      */
     public FilterConfig getFilterConfig() {
         return (this.filterConfig);
     }
 
     /**
      * Set the filter configuration object for this filter.
      *
      * @param filterConfig The filter configuration object
      */
     public void setFilterConfig(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
     }
 
     /**
      * Destroy method for this filter
      */
     public void destroy() {
     }
 
     /**
      * Init method for this filter
      */
     public void init(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
         if (filterConfig != null) {
             if (debug) {
                 // log("IRMSFilter:Initializing filter");
             }
         }
     }
 
     /**
      * Return a String representation of this object.
      */
     @Override
     public String toString() {
         if (filterConfig == null) {
             return ("IRMSFilter()");
         }
         StringBuffer sb = new StringBuffer("IRMSFilter(");
         sb.append(filterConfig);
         sb.append(")");
         return (sb.toString());
     }
 
     private void sendProcessingError(Throwable t, ServletResponse response) {
         String stackTrace = getStackTrace(t);
 
         if (stackTrace != null && !stackTrace.equals("")) {
             try {
                 response.setContentType("text/html");
                 PrintStream ps = new PrintStream(response.getOutputStream());
                 PrintWriter pw = new PrintWriter(ps);
                 pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N
 
                 // PENDING! Localize this for next official release
                 pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                 pw.print(stackTrace);
                 pw.print("</pre></body>\n</html>"); //NOI18N
                 pw.close();
                 ps.close();
                 response.getOutputStream().close();
             } catch (Exception ex) {
             }
         } else {
             try {
                 PrintStream ps = new PrintStream(response.getOutputStream());
                 t.printStackTrace(ps);
                 ps.close();
                 response.getOutputStream().close();
             } catch (Exception ex) {
             }
         }
     }
 
     public static String getStackTrace(Throwable t) {
         String stackTrace = null;
         try {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             t.printStackTrace(pw);
             pw.close();
             sw.close();
             stackTrace = sw.getBuffer().toString();
         } catch (Exception ex) {
         }
         return stackTrace;
     }
 
     public void log(String msg) {
         filterConfig.getServletContext().log(msg);
     }
 }
