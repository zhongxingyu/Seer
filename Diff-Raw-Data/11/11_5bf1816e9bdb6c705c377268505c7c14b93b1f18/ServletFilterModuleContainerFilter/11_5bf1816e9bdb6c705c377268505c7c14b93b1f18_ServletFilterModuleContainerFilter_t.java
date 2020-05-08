 package com.atlassian.plugin.servlet.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.atlassian.plugin.servlet.ServletModuleManager;
 
 /**
  * Applications need to create a concrete subclass of this for use in their filter stack.  This filters responsiblity
  * is to retrieve the filters to be applied to the request from the {@link ServletModuleManager} and build a
  * {@link FilterChain} from them.  Once all the filters in the chain have been applied to the request, this filter 
  * returns control to the main chain.
  * <p/>
  * There is one init parameters that must be configured for this filter, the "location" parameter.  It can be one of
  * "top", "middle" or "bottom".  A filter with a "top" location must appear before the filter with a "middle" location
  * which must appear before a filter with a "bottom" location.  Where any other application filters lie in the filter
  * stack is completely up to the application.        
  * 
  * @since 2.1.0
  */
 public abstract class ServletFilterModuleContainerFilter implements Filter
 {
     private static final Log log = LogFactory.getLog(ServletFilterModuleContainerFilter.class);
     
     private FilterConfig filterConfig;
     private FilterLocation location;
 
     public void init(FilterConfig filterConfig) throws ServletException
     {
         this.filterConfig = filterConfig;
         location = FilterLocation.parse(filterConfig.getInitParameter("location"));
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
     {
         doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
     }
     
     void doFilter(HttpServletRequest request, HttpServletResponse response, final FilterChain chain) throws IOException, ServletException
     {
         if (getServletModuleManager() == null)
         {
             log.info("Could not get ServletModuleManager. Skipping filter plugins.");
             chain.doFilter(request, response);
             return;
         }
         
         final Iterable<Filter> filters = getServletModuleManager().getFilters(location, getUri(request), filterConfig);
         FilterChain pluginFilterChain = new IteratingFilterChain(filters.iterator(), chain);
         pluginFilterChain.doFilter(request, response);
     }
     
     public void destroy()
     {
     }
 
     /**
      * Retrieve the DefaultServletModuleManager from your container framework.
      */
     protected abstract ServletModuleManager getServletModuleManager();
 
    protected final FilterConfig getFilterConfig()
     {
         return filterConfig;
     }
 
    protected final FilterLocation getFilterLocation()
     {
         return location;
     }
 
     /**
      * Gets the uri from the request.  Copied from Struts 2.1.0.
      *
      * @param request
      *            The request
      * @return The uri
      */
    private static String getUri(HttpServletRequest request) {
         // handle http dispatcher includes.
         String uri = (String) request
                 .getAttribute("javax.servlet.include.servlet_path");
         if (uri != null) {
             return uri;
         }
 
         uri = getServletPath(request);
         if (uri != null && !"".equals(uri)) {
             return uri;
         }
 
         uri = request.getRequestURI();
         return uri.substring(request.getContextPath().length());
     }
 
     /**
      * Retrieves the current request servlet path.
      * Deals with differences between servlet specs (2.2 vs 2.3+).
      * Copied from Struts 2.1.0.
      *
      * @param request the request
      * @return the servlet path
      */
    private static String getServletPath(HttpServletRequest request) {
         String servletPath = request.getServletPath();
 
         String requestUri = request.getRequestURI();
         // Detecting other characters that the servlet container cut off (like anything after ';')
         if (requestUri != null && servletPath != null && !requestUri.endsWith(servletPath)) {
             int pos = requestUri.indexOf(servletPath);
             if (pos > -1) {
                 servletPath = requestUri.substring(requestUri.indexOf(servletPath));
             }
         }
 
         if (null != servletPath && !"".equals(servletPath)) {
             return servletPath;
         }
 
         int startIndex = request.getContextPath().equals("") ? 0 : request.getContextPath().length();
         int endIndex = request.getPathInfo() == null ? requestUri.length() : requestUri.lastIndexOf(request.getPathInfo());
 
         if (startIndex > endIndex) { // this should not happen
             endIndex = startIndex;
         }
 
         return requestUri.substring(startIndex, endIndex);
     }
 }
