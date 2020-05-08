 package LandingPage;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import javax.annotation.Resource;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.sql.DataSource;
 
 /**
  *
  * @author 1
  */
 public class SubscribersCounterFilter implements Filter {
 
     @Resource(name = "jdbc/LPDB")
     private DataSource jdbcLPDB = null;
     
     private static final boolean debug = false;
     // The filter configuration object we are associated with.  If
     // this value is null, this filter instance is not currently
     // configured. 
     private FilterConfig filterConfig = null;
 
     public SubscribersCounterFilter() {
     }
 
     private void doBeforeProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
         if (debug) {
             log("SubscribersCounterFilter:DoBeforeProcessing");
         }
 
         try {
             if (jdbcLPDB == null) {
 
                 log("Get DataSource context");
 
                 // Obtain our environment naming context
                 Context initCtx = new InitialContext();
                 Context envCtx = (Context) initCtx.lookup("java:comp/env");
 
                 // Look up our data source
                 jdbcLPDB = (DataSource) envCtx.lookup("jdbc/LPDB");
 
                 if (jdbcLPDB == null) {
                     
                     log("Invalid DataSource context");
 
                     throw new ServletException("'jdbc:mysql://localhost:3306/landing_page' is in unknown DataSource");
                 } else {
                     log("DataSource context -- Complete:" + jdbcLPDB.getClass().toString());
                 }
             }
         } catch (NamingException ne) {
             throw new ServletException(ne.getMessage());
         }
 
         Connection conn = null;
         Statement stmt = null;
         ResultSet rs;
 
         int subscribersCount = 437;
         String subscribersSuffix = "человек";
 
         try {
 
             String selectMaxIdSQL = "select max(subscribers.id) from subscribers";
 
             conn = jdbcLPDB.getConnection();
             stmt = conn.createStatement();
             rs = stmt.executeQuery(selectMaxIdSQL);
 
             if (rs.first()) {
                 subscribersCount = rs.getInt(1);
 
                 String id = rs.getString(1);
 
                 String last_symbol = id.substring(id.length() - 1);
                 Integer last_number = Integer.valueOf(last_symbol);
 
                 String prev_last_symbol = "0";
                 if (id.length() > 1) {
                     prev_last_symbol = id.substring(id.length() - 2);
                 }
                 Integer prev_last_number = Integer.valueOf(prev_last_symbol);
 
                 if ((last_number > 1 && last_number < 5) && (prev_last_number != 1)) {
                     subscribersSuffix = "человека";
                 } else {
                     subscribersSuffix = "человек";
                 }
             }
 
         } catch (Exception e) {
             //out.println("Login Failed! Database error! Invalid user login or password!<br/>");
             throw new ServletException(e.getMessage());
 
         } finally {
             try {
                 if (stmt != null) {
                     stmt.close();
                 }
                 if (conn != null) {
                     conn.close();
                 }
             } catch (SQLException sqle) {
             }
         } // try
         
        request.setAttribute("subscribersCount", subscribersCount);
         request.setAttribute("subscribersSuffix", subscribersSuffix);
         
     }
 
     private void doAfterProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
         if (debug) {
             log("SubscribersCounterFilter:DoAfterProcessing");
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
             FilterChain chain)
             throws IOException, ServletException {
 
         if (debug) {
             log("SubscribersCounterFilter:doFilter()");
         }
 
         doBeforeProcessing(request, response);
 
         Throwable problem = null;
         try {
             chain.doFilter(request, response);
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
                 log("SubscribersCounterFilter:Initializing filter");
             }
         }
     }
 
     /**
      * Return a String representation of this object.
      */
     @Override
     public String toString() {
         if (filterConfig == null) {
             return ("SubscribersCounterFilter()");
         }
         StringBuffer sb = new StringBuffer("SubscribersCounterFilter(");
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
