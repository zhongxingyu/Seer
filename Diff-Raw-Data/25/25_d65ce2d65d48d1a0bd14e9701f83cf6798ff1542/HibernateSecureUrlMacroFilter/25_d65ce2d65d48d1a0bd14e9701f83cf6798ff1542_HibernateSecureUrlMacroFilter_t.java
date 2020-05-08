 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.hibernate;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.Validate;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.globant.katari.core.security.SecureUrlAccessHelper;
 
 /** A filter that adds to the request attributes that will be used by
  * freemarker macros.
  *
  * This filter puts the SecureUrlAccessHelper as an attribute named
  * secureUrlHelper, and a boolean (under attribute named securityDebug) that
  * indicates if the security debug feature is enabled.
  *
  * To enable the security debug feature, add securityDebug=true as a request
  * parameter, for example:
  *
  * http://localhost/katari-web/module/user/users.do?securityDebug=true
  *
  * After that, debug mode is enabled until the request includes a parameter
  * securityDebug=false.
  *
  * The security debug feature is only enabled if it is a development database.
  *
  * This filter is intended to be used as a bean that overrides the
  * SecureUrlMacroFilter definition, called katari.secureUrlMacroFilter in
  * katari-core.xml.
  */
 public class HibernateSecureUrlMacroFilter implements Filter {
 
   /** The class logger.
    */
   private static Log log = LogFactory.getLog(
       HibernateSecureUrlMacroFilter.class);
 
   /** The secure url macro helper.
    *
    * It is never null.
    */
   private final SecureUrlAccessHelper helper;
 
   /** The database checker.
   *
   * It is null when the application is not using a database.
   */
  private final DevelopmentDataBaseChecker developmentDataBaseChecker;
 
   /** The constructor for an application with a database.
    *
    * @param theHelper the secure url macro helper instance. It cannot be null.
    *
    * @param checker the development database checker. It cannot be null.
    */
   public HibernateSecureUrlMacroFilter(final SecureUrlAccessHelper theHelper,
       final DevelopmentDataBaseChecker checker) {
     Validate.notNull(theHelper, "The SecureUrlAccessHelper cannot be null");
     Validate.notNull(checker, "the DevelopmentDataBaseChecker cannot be"
         + " null");
     helper = theHelper;
     developmentDataBaseChecker = checker;
   }
 
   /** Puts the helper in the request and continues with the chain.
    *
    * {@inheritDoc}
    */
   public void doFilter(final ServletRequest request,
       final ServletResponse response, final FilterChain chain)
       throws IOException, ServletException {
 
     log.trace("Entering doFilter");
 
     if (!(request instanceof HttpServletRequest)) {
       throw new ServletException("This filter can only be applied to http"
           + " requests.");
     }
 
     HttpServletRequest servletRequest = (HttpServletRequest) request;
 
     String debugParameter = request.getParameter("securityDebug");
 
     // WARNING: The order of the expressions in the if is important. Only
     // consider the debugParameter in a development database. This is checked
     // after the debugParameter because checkForDevelopmentDatabase hits the
     // database.
    if (debugParameter != null
        && developmentDataBaseChecker.checkForDevelopmentDatabase()) {
       HttpSession session = servletRequest.getSession();
       if (Boolean.valueOf(debugParameter)) {
         log.debug("Enabling security debug mode.");
         session.setAttribute("securityDebug", debugParameter);
       } else {
         log.debug("Disabling security debug mode.");
         session.removeAttribute("securityDebug");
       }
     }
     servletRequest.setAttribute("secureUrlHelper", helper);
 
     chain.doFilter(request, response);
 
     log.trace("Leaving doFilter");
   }
 
   /** Initializes the filter.
    *
    * This implementation is empty.
    *
    * {@inheritDoc}
    */
   public void init(final FilterConfig filterConfig) throws ServletException {
   }
 
   /** This operation is empty.
    *
    * {@inheritDoc}
    */
   public void destroy() {
   }
 }
 
