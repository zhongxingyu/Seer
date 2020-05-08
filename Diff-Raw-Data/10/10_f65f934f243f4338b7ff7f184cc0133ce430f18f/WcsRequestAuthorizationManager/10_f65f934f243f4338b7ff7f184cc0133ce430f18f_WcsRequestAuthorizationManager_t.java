 package org.deegree.securityproxy.authorization.wcs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.deegree.securityproxy.authentication.wcs.WcsPermission;
 import org.deegree.securityproxy.commons.WcsOperationType;
 import org.deegree.securityproxy.request.WcsRequest;
 import org.springframework.security.access.AccessDecisionManager;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 
 /**
  * Checks if a authenticated User is permitted to perform an incoming {@link HttpServletRequest} against a WCS.
  * 
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 public class WcsRequestAuthorizationManager implements AccessDecisionManager {
 
     @Override
     public void decide( Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes )
                             throws AccessDeniedException, InsufficientAuthenticationException {
         WcsRequest wcsRequest = (WcsRequest) object;
         if ( authentication == null )
             throw new AccessDeniedException( "Not authenticated!" );
         Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
 
         boolean authorized = false;
         if ( isGetCoverageRequest( wcsRequest ) ) {
             authorized = isAuthorizedGetCoverage( wcsRequest, authorities );
         } else if ( isDescribeCoverageRequest( wcsRequest ) ) {
             authorized = isAuthorizedDescribeCoverage( wcsRequest, authorities );
         } else if ( isGetCapabilitiesRequest( wcsRequest ) ) {
             authorized = isAuthorizedGetCapabilities( wcsRequest, authorities );
         }
         if ( !authorized )
             throw new AccessDeniedException( "Unauthorized" );
     }
 
     private boolean isAuthorizedDescribeCoverage( WcsRequest wcsRequest,
                                                   Collection<? extends GrantedAuthority> authorities ) {
         List<String> grantedCoverages = new ArrayList<String>();
         for ( GrantedAuthority authority : authorities ) {
             if ( authority instanceof WcsPermission ) {
                 WcsPermission wcsPermission = (WcsPermission) authority;
                 if ( isOperationTypeAuthorized( wcsRequest, wcsPermission )
                      && isServiceVersionAuthorized( wcsRequest, wcsPermission ) ) {
                     grantedCoverages.add( wcsPermission.getCoverageName() );
                 }
             }
         }
         for ( String coverageName : wcsRequest.getCoverageNames() ) {
             if ( !grantedCoverages.contains( coverageName ) )
                 return false;
         }
         return true;
     }
 
     private boolean isAuthorizedGetCoverage( WcsRequest wcsRequest, Collection<? extends GrantedAuthority> authorities ) {
         for ( GrantedAuthority authority : authorities ) {
             if ( authority instanceof WcsPermission ) {
                 WcsPermission wcsPermission = (WcsPermission) authority;
                 if ( isFirstCoverageNameAuthorized( wcsRequest, wcsPermission )
                      && isOperationTypeAuthorized( wcsRequest, wcsPermission )
                      && isServiceVersionAuthorized( wcsRequest, wcsPermission ) ) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean isAuthorizedGetCapabilities( WcsRequest wcsRequest,
                                                  Collection<? extends GrantedAuthority> authorities ) {
         for ( GrantedAuthority authority : authorities ) {
             if ( authority instanceof WcsPermission ) {
                 WcsPermission wcsPermission = (WcsPermission) authority;
                 if ( isOperationTypeAuthorized( wcsRequest, wcsPermission )
                      && isServiceVersionAuthorized( wcsRequest, wcsPermission ) ) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean isDescribeCoverageRequest( WcsRequest wcsRequest ) {
         return WcsOperationType.DESCRIBECOVERAGE.equals( wcsRequest.getOperationType() );
     }
 
     private boolean isGetCoverageRequest( WcsRequest wcsRequest ) {
         return WcsOperationType.GETCOVERAGE.equals( wcsRequest.getOperationType() );
     }
 
     private boolean isGetCapabilitiesRequest( WcsRequest wcsRequest ) {
         return WcsOperationType.GETCAPABILITIES.equals( wcsRequest.getOperationType() );
 
     }
 
     private boolean isOperationTypeAuthorized( WcsRequest wcsRequest, WcsPermission wcsPermission ) {
         if ( wcsRequest.getOperationType() != null )
             return wcsRequest.getOperationType().equals( wcsPermission.getOperationType() );
         return false;
     }
 
     private boolean isServiceVersionAuthorized( WcsRequest wcsRequest, WcsPermission wcsPermission ) {
         if ( wcsRequest.getServiceVersion() != null )
             return wcsRequest.getServiceVersion().equals( wcsPermission.getServiceVersion() );
         return false;
     }
 
     private boolean isFirstCoverageNameAuthorized( WcsRequest wcsRequest, WcsPermission wcsPermission ) {
        if ( !wcsRequest.getCoverageNames().isEmpty() ) {
             String firstCoverage = wcsRequest.getCoverageNames().get( 0 );
             return firstCoverage.equals( wcsPermission.getCoverageName() );
         }
         return false;
 
     }
 
     @Override
     public boolean supports( ConfigAttribute attribute ) {
         // Not needed in this implementation.
         return true;
     }
 
     @Override
     public boolean supports( Class<?> clazz ) {
         return WcsRequest.class.equals( clazz );
     }
 
 }
