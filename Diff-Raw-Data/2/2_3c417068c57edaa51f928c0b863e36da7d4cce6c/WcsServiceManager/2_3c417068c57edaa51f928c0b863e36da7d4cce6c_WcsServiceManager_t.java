 package org.deegree.securityproxy.wcs;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.deegree.securityproxy.authorization.RequestAuthorizationManager;
 import org.deegree.securityproxy.authorization.logging.AuthorizationReport;
 import org.deegree.securityproxy.filter.ServiceManager;
 import org.deegree.securityproxy.filter.StatusCodeResponseBodyWrapper;
 import org.deegree.securityproxy.request.KvpNormalizer;
 import org.deegree.securityproxy.request.OwsRequest;
 import org.deegree.securityproxy.request.OwsRequestParser;
 import org.deegree.securityproxy.request.UnsupportedRequestTypeException;
 import org.deegree.securityproxy.responsefilter.ResponseFilterManager;
 import org.deegree.securityproxy.responsefilter.logging.ResponseFilterReport;
 import org.springframework.security.core.Authentication;
 
 /**
  * This is an implementation of a {@link ServiceManager} for wcs-requests. It contains wcs specific parser,
  * authorization-manager and filter-manager. It is possible to start parsing of wcs-requests, wcs-authorization,
  * wcs-response-filtering and a check whether response-filtering is enabled.
  * 
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: stenger $
  * @version $Revision: $, $Date: $
  */
 class WcsServiceManager implements ServiceManager {
 
     private final OwsRequestParser parser;
 
     private final RequestAuthorizationManager requestAuthorizationManager;
 
     private final ResponseFilterManager filterManager;
 
     public WcsServiceManager( OwsRequestParser parser, RequestAuthorizationManager requestAuthorizationManager,
                               ResponseFilterManager filterManager ) {
         this.parser = parser;
         this.requestAuthorizationManager = requestAuthorizationManager;
         this.filterManager = filterManager;
     }
 
     @Override
     public OwsRequest parse( HttpServletRequest httpRequest )
                             throws UnsupportedRequestTypeException {
         return parser.parse( httpRequest );
     }
 
     @Override
     public AuthorizationReport authorize( Authentication authentication, OwsRequest owsRequest ) {
         return requestAuthorizationManager.decide( authentication, owsRequest );
     }
 
     @Override
     public boolean isResponseFilterEnabled( OwsRequest owsRequest ) {
         return filterManager.supports( owsRequest.getClass() );
     }
 
     @Override
     public ResponseFilterReport filterResponse( StatusCodeResponseBodyWrapper wrappedResponse,
                                                 Authentication authentication, OwsRequest owsRequest )
                             throws IOException {
         return filterManager.filterResponse( wrappedResponse, owsRequest, authentication );
     }
 
     @Override
     public boolean isServiceTypeSupported( HttpServletRequest request ) {
         @SuppressWarnings("unchecked")
         Map<String, String[]> kvpMap = KvpNormalizer.normalizeKvpMap( request.getParameterMap() );
        return "wcs".equalsIgnoreCase( kvpMap.get( "service" )[0] );
     }
 
 }
