 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletContext;
 import javax.servlet.Filter;
 import javax.servlet.FilterConfig;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** A filter that delegates all processing to another filter obtained from the
  * spring application context.
  *
  * It dispatches all requests to a spring configured filter defind under the
 * bean named, by default 'anbis.moduleFilterProxy' in the web application
  * context.
  *
  * The specific bean name is configured using the 'filterBeanName'
  * initialization parameter.
  */
 public final class SpringBootstrapFilter implements Filter {
 
   /** The serialization version number.
    *
    * This number must change every time a new serialization incompatible change
    * is introduced in the class.
    */
   private static final long serialVersionUID = 20080221;
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(SpringBootstrapFilter.class);
 
   /** The target filter that this filter delegates all requests.
    *
    * This is null until initialization.
    */
   private Filter delegate = null;
 
   /** Called by the servlet container to indicate to the filter that it is
    * being placed into service.
    *
    * @param config The filter configuration and initialization parameters.
    * This object is created by the container.
    *
    * @throws ServletException if an error occurs.
    */
   public void init(final FilterConfig config) throws ServletException {
     log.trace("Entering init");
     String beanName = getBeanName(config);
     WebApplicationContext context;
     context = getWebApplicationcontext(config.getServletContext());
     delegate = (Filter) context.getBean(beanName);
     delegate.init(config);
     log.trace("Leaving init");
   }
 
   /** Called by the servlet container to allow the filter to pre-process the
    * request.
    *
    * It delegates all the requests to a spring configured filter defined under
    * the bean named, by default, 'filterDispatcher'.
    *
    * @param request The HttpServletRequest object that contains the client's
    * request.
    *
    * @param response The HttpServletResponse object that contains the
    * servlet's response
    *
    * @param chain Allows the filter to pass on the request and response to the
    * next entity in the chain. It cannot be null.
    *
    * @throws IOException if an input or output exception occurs.
    *
    * @throws ServletException if another error occurs.
    */
   public void doFilter(final ServletRequest request, final
       ServletResponse response, final FilterChain chain) throws
       ServletException, IOException {
     log.trace("Entering doFilter");
     delegate.doFilter(request, response, chain);
     log.trace("Leaving doFilter");
   }
 
   /** Called by the web container to indicate to a filter that it is being
    * taken out of service.
    *
    * It passes the message to the delegate.
    */
   public void destroy() {
     delegate.destroy();
   }
 
   /** Returns the spring web application context.
    *
    * @param context The servlet context. It cannot be null.
    *
    * @return Returns the spring web application context.
    */
   private WebApplicationContext getWebApplicationcontext(final ServletContext
       context) {
     return WebApplicationContextUtils.getWebApplicationContext(context);
   }
 
   /** Obtains, from the servlet configuation, the name of the spring bean to
    * delegate the requests to.
    *
    * The name of the bean is specified with the filterBeanName init parameter.
    * The bean must implement the Filter interface.
    *
    * @param config The filter configuration. It cannot be null.
    *
    * @return the bean name as specified in the filter configuration, with the
    * 'filterBeanName' parameter. If the bean name was not specified, it returns
    * 'filterContainer'.
    */
   private String getBeanName(final FilterConfig config) {
     String beanName = config.getInitParameter("filterBeanName");
     if (beanName == null) {
       return "katari.moduleFilterProxy";
     } else {
       return beanName;
     }
   }
 }
 
