 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.util.servlet;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.regex.Pattern;
 import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 
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
 
    private static final Log LOG = LogFactory.getLog( HttpServletResponseEndpointWrapper.class );
     private static final Pattern PROTOCOL = Pattern.compile( "^[^:/]*://" );
 
     private final HttpServletRequestEndpointWrapper wrappedRequest;
    private final String baseURL;
 
     /**
     * @param response     The real {@link HttpServletResponse} that we're wrapping.
     * @param baseURL
      */
     public HttpServletResponseEndpointWrapper(final HttpServletRequestEndpointWrapper wrappedRequest, HttpServletResponse response,
                                               final String baseURL) {
 
         super( response );
         this.wrappedRequest = wrappedRequest;
         this.baseURL = baseURL;
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     public void sendRedirect(String location)
             throws IOException {
 
         URI locationURI = URI.create( location );
         if (locationURI.isAbsolute()) {
             // Already absolute, nothing to do.
            LOG.debug( "Not resolving redirect, already absolute: " + location );
             super.sendRedirect( location );
         }
 
         String absoluteLocation;
         if (location.startsWith( "/" )) {
             // Relative to container root.
             absoluteLocation = URI.create( baseURL ).resolve( location ).toASCIIString();
            LOG.debug( "Resolving redirect: " + location + " relative to container root: " + baseURL + " -> " + absoluteLocation );
         } else {
             // Relative to request URL.
             String requestURL = wrappedRequest.getRequestURL().toString();
             absoluteLocation = URI.create( requestURL ).resolve( location ).toASCIIString();
            LOG.debug( "Resolving redirect: " + location + " relative to request URL: " + requestURL + " -> " + absoluteLocation );
         }
 
         super.sendRedirect( absoluteLocation );
     }
 }
