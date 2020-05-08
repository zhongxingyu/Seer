 package com.ettrema.web.security;
 
 import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
 import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Map;
 
 /**
  *
  * @author brad
  */
 public class LogoutResourceFactory implements ResourceFactory {
 
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( LogoutResourceFactory.class );
     private final ResourceFactory wrapped;
     private final LogoutHandler logoutHandler;
     private String name;
     private String redirect;
 
     public LogoutResourceFactory( LogoutHandler logoutHandler, ResourceFactory wrapped ) {
         this.logoutHandler = logoutHandler;
         this.wrapped = wrapped;
     }
 
     @Override
     public Resource getResource( String host, String path ) throws NotAuthorizedException, BadRequestException {
         Path p = Path.path( path );
         if( p.getName().equals( name ) ) {
             Resource r = wrapped.getResource( host, p.getParent().toString() );
             if( r == null ) {
                 return null;
             } else {
                 log.trace( "getResource: got logout resource" );
                 return new LogoutResource( name, redirect, r );
             }
         } else {
             return null;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public void setName( String name ) {
         this.name = name;
     }
 
     public String getRedirect() {
         return redirect;
     }
 
     public void setRedirect( String redirect ) {
         this.redirect = redirect;
     }
 
     public LogoutHandler getLogoutHandler() {
         return logoutHandler;
     }
 
     public class LogoutResource implements GetableResource {
 
         private final String name;
         private final String redirect;
         private final Resource wrapped;
         private Auth auth;
 
         public LogoutResource( String name, String redirect, Resource wrapped ) {
             this.name = name;
             this.redirect = redirect;
             this.wrapped = wrapped;
         }
 
         public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
             log.trace( "sendContent" );
         }
 
         public Long getMaxAgeSeconds( Auth auth ) {
             return null;
         }
 
         public String getContentType( String accepts ) {
             return "text/html";
         }
 
         public Long getContentLength() {
             return null;
         }
 
         public String getUniqueId() {
             return null;
         }
 
         public String getName() {
             return name;
         }
 
         public Object authenticate( String user, String password ) {
             log.trace( "authenticate" );
             return wrapped.authenticate( user, password );
         }
 
         public boolean authorise( Request request, Method method, Auth auth ) {
             if( log.isTraceEnabled() ) {
                 log.trace( "authorise: " + auth );
             }
             this.auth = auth;
             return true;
         }
 
         public String getRealm() {
             return wrapped.getRealm();
         }
 
         public Date getModifiedDate() {
             return null;
         }
 
         public String checkRedirect( Request request ) {
             if( auth != null ) {
                 log.trace( "checkRedirect: logging out" );
                 logoutHandler.logout( request, auth );
             } else {
                 log.trace( "checkRedirect: not logged in" );
             }
             return redirect;
         }
     }
 }
