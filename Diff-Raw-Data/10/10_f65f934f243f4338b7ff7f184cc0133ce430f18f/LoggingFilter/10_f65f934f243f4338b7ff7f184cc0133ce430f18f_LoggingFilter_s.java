 package org.deegree.securityproxy.filter;
 
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.deegree.securityproxy.authorization.wcs.WcsRequestAuthorizationManager;
 import org.deegree.securityproxy.logger.SecurityRequestResposeLogger;
 import org.deegree.securityproxy.report.SecurityReport;
 import org.deegree.securityproxy.request.WcsRequestParser;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Servlet Filter that logs all incoming requests and their response
  * 
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 public class LoggingFilter implements Filter {
 
     @Autowired
     private WcsRequestAuthorizationManager requestAuthorizationManager;
 
     @Autowired
     private WcsRequestParser parser;
 
     @Autowired
     private SecurityRequestResposeLogger proxyReportLogger;
 
     @Override
     public void init( FilterConfig filterConfig )
                             throws ServletException {
     }
 
     @Override
     public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain )
                             throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
         HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
         FilterResponseWrapper wrappedResponse = new FilterResponseWrapper( httpResponse );
         String uuid = createUuidHeader( wrappedResponse );
         chain.doFilter( httpRequest, wrappedResponse );
         generateAndLogProxyReport( uuid, httpRequest, wrappedResponse );
     }
 
     @Override
     public void destroy() {
     }
 
     private void generateAndLogProxyReport( String uuid, HttpServletRequest request, FilterResponseWrapper response ) {
         int statusCode = response.getStatus();
         boolean isRequestSuccessful = SC_OK == statusCode ? true : false;
         String targetURI = request.getRequestURL().toString();
         String queryString = request.getQueryString();
         String requestURL = queryString != null ? targetURI + "?" + queryString : targetURI;
         String message = generateMessage( statusCode );
         SecurityReport report = new SecurityReport( uuid, request.getRemoteAddr(), requestURL, isRequestSuccessful, message );
         proxyReportLogger.logProxyReportInfo( report );
     }
 
     private String generateMessage( int statusCode ) {
         StringBuilder builder = new StringBuilder( "Status code is " );
         builder.append( statusCode );
         switch ( statusCode ) {
         case 200:
             builder.append( ": OK" );
             break;
         case 400:
             builder.append( ": Bad request" );
             break;
         case 401:
             builder.append( ": Unauthorized" );
             break;
         case 403:
             builder.append( ": Forbidden" );
             break;
         case 404:
             builder.append( ": Not found" );
             break;
         case 500:
             builder.append( ": Internal server error" );
             break;
         }
         return builder.toString();
     }
 
     private String createUuidHeader( FilterResponseWrapper wrappedResponse ) {
         String uuid = UUID.randomUUID().toString();
         wrappedResponse.addHeader( "serial_uuid", uuid );
         return uuid;
     }
 
 }
