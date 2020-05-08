 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.util.servlet;
 
import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.IOException;
 import java.net.URI;
 import java.util.regex.Pattern;
 import javax.servlet.http.*;
 
 
 /**
  * {@link HttpServletResponse} wrapper used to convert relative redirects correctly when behind a proxy or load balancer.
  * <p/>
  * Relative redirects sent to this {@link HttpServletResponse} will be converted in absolute URLs by using:
  * <ul>
  * <li>A response base for the scheme and authority of the redirection target.</li>
  * <li>The current {@link HttpServletRequest}'s requestURL's path for the context of our relative path.</li>
  * </ul>
  *
  * @author lhunath
  */
 public class HttpServletResponseEndpointWrapper extends HttpServletResponseWrapper {
 
    private static final Logger  logger   = Logger.get( HttpServletResponseEndpointWrapper.class );
     private static final Pattern PROTOCOL = Pattern.compile( "^[^:/]*://" );
 
     private final HttpServletRequestEndpointWrapper wrappedRequest;
    private final String                            baseURL;
 
     /**
     * @param response The real {@link HttpServletResponse} that we're wrapping.
      */
     public HttpServletResponseEndpointWrapper(final HttpServletRequestEndpointWrapper wrappedRequest, HttpServletResponse response,
                                               final String baseURL) {
 
         super( response );
         this.wrappedRequest = wrappedRequest;
         this.baseURL = baseURL;
     }
 
     @Override
     public void sendRedirect(String location)
             throws IOException {
 
         URI locationURI = URI.create( location );
         if (locationURI.isAbsolute()) {
             // Already absolute, nothing to do.
            logger.dbg( "Not resolving redirect, already absolute: %s", location );
             super.sendRedirect( location );
            return;
         }
 
         String absoluteLocation;
         if (location.startsWith( "/" )) {
             // Relative to container root.
             absoluteLocation = URI.create( baseURL ).resolve( location ).toASCIIString();
            logger.dbg( "Resolving redirect: %s  relative to container root: %s -> %s", location, baseURL, absoluteLocation );
         } else {
             // Relative to request URL.
             String requestURL = wrappedRequest.getRequestURL().toString();
             absoluteLocation = URI.create( requestURL ).resolve( location ).toASCIIString();
            logger.dbg( "Resolving redirect: %s relative to request URL: %s -> %s", location, requestURL, absoluteLocation );
         }
 
         super.sendRedirect( absoluteLocation );
     }
 }
