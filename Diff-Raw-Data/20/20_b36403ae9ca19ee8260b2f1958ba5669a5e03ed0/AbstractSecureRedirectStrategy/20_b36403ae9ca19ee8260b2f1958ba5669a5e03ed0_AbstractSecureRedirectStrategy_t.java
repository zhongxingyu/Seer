 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.servlets;
 
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Provides an abstract base class for SecureRedirectStrategy implementations.
  * 
  * @author Christian Trimble
  */
 public abstract class AbstractSecureRedirectStrategy implements SecureRedirectStrategy {
   
   /** The default port for HTTP */
   public static final int DEFAULT_HTTP_PORT = 80;
   
   public static final String HTTP_PROTOCOL = "http";
   
   /** The default port for HTTPS */
   public static final int DEFAULT_HTTPS_PORT = 443;
   
   public static final String HTTPS_PROTOCOL = "https";
   
   /** A mapping from common insecure ports to secure ports. */
   private static final Map<Integer, Integer> TO_SECURE_PORT_MAP = new HashMap<Integer, Integer>();
   
   /** A mapping from common secure ports to insecure ports. */
   private static final Map<Integer, Integer> TO_INSECURE_PORT_MAP = new HashMap<Integer, Integer>();
   
   /** Adds an entry to the secure and insecure port map. */
   private static void addPortMapping( Integer insecurePort, Integer securePort ) {
     TO_SECURE_PORT_MAP.put(insecurePort, securePort);
     TO_INSECURE_PORT_MAP.put(securePort, insecurePort);
   }
 
   static {
     addPortMapping(DEFAULT_HTTP_PORT, DEFAULT_HTTPS_PORT);
     addPortMapping(8080, 8443);
   }
   
   /**
    * Returns the default port for the specified protocol.
    * 
    * @param protocol the protocol name.
    * @return the default port for the specifed protocol.
    */
   public static int getDefaultPort( String protocol ) {
     if( HTTP_PROTOCOL.equals(protocol) ) { return DEFAULT_HTTP_PORT; }
     else if( HTTPS_PROTOCOL.equals(protocol) ) { return DEFAULT_HTTPS_PORT; }
     else { throw new IllegalArgumentException("No known default for "+protocol); }
   }
   
   /**
    * Looks up a corresponding port number from a port mapping.
    * @param mapping the port mapping
    * @param port the key in the port map.
    * @return the corresponding port number from the port mapping.
    * @throws RuntimeException if a value could not be found.
    */
   public static int mapPort(Map<Integer, Integer> mapping, int port) {
     Integer mappedPort = mapping.get(port);
     if( mappedPort == null ) throw new RuntimeException("Could not map port "+port);
     return mappedPort;
   }
   
   /**
    * Gets the original protocol for the specified request.
    * @param request the request made by the user.
    * @return the original protocol for the request.
    */
   public abstract String getProtocol( HttpServletRequest request );
   
   /**
    * Gets the original port for the specified request.
    * @param request the request made by the user.
    * @return the original protocol for the request.
    */
   public abstract int getPort( HttpServletRequest request );
   
   /**
    * Returns the secure version of the original URL for the request.
    * 
    * @param request the insecure request that was made.
    * @param response the response for the request.
    * @return the secure version of the original URL for the request.
    * @throws IOException if the url could not be created.
    */
   public String secureUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
     String protocol = getProtocol(request);
     if( protocol.equalsIgnoreCase(HTTP_PROTOCOL) ) {
       int port = mapPort(TO_SECURE_PORT_MAP, getPort(request));
       try {
         URI newUri = changeProtocolAndPort(HTTPS_PROTOCOL, port == DEFAULT_HTTPS_PORT ? -1 : port, request);
         return newUri.toString();
       } catch (URISyntaxException e) {
         throw new IllegalStateException("Failed to create URI.", e);
       }
     }
     else {
       throw new UnsupportedProtocolException("Cannot build secure url for "+protocol);
     }
   }
 
   /**
    * Returns the insecure version of the original URL for the request.
    * 
    * @param request the secure request that was made.
    * @param response the response for the request.
    * @return the insecure version of the original URL for the request.
    * @throws IOException if the url could not be created.
    */
   public String insecureUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
     String protocol = getProtocol(request);
     if( protocol.equalsIgnoreCase(HTTPS_PROTOCOL) ) {
       int port = mapPort(TO_INSECURE_PORT_MAP, getPort(request));
       try {
         return changeProtocolAndPort(HTTP_PROTOCOL, port == DEFAULT_HTTP_PORT ? -1 : port, request).toString();
       } catch (URISyntaxException e) {
         throw new IllegalStateException("Failed to create URI.", e);
       }
     }
     else {
       throw new UnsupportedProtocolException("Cannot build insecure url for "+protocol);
     }
   }
   
   /**
    * Sends a moved perminately redirect to the secure form of the request URL.
    * 
    * @request the request to make secure.
    * @response the response for the request.
    */
   @Override
   public void makeSecure(HttpServletRequest request, HttpServletResponse response)
     throws IOException
   {
     response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
     response.setHeader("Location", secureUrl(request, response));
     response.getOutputStream().flush();
     response.getOutputStream().close();
   }
 
   /**
    * Sends a moved perminately redirect to the insecure form of the request URL.
    * 
    * @request the request to make secure.
    * @response the response for the request.
    */
   @Override
   public void makeInsecure(HttpServletRequest request, HttpServletResponse response) throws IOException {
     response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
     response.setHeader("Location", insecureUrl(request, response));
     response.getOutputStream().flush();
     response.getOutputStream().close();
   }
   
   /**
    * Constructs a URI for request and calls changeProtocolAndPort(String, int, URI).
    * 
    * @param protocol the new protocol (scheme) in the resulting URI.
    * @param port the new port in the resulting URI, or the default port if -1 is provided.
    * @param request the request to use as the URI template.
    * @return a new URI object with the updated protocol and port.
    * @throws URISyntaxException 
     */
   public static URI changeProtocolAndPort(String protocol, int port, HttpServletRequest request) throws URISyntaxException {
    return changeProtocolAndPort(
        protocol,
        port,
        URI.create(
            request.getRequestURL()
                .append((StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString()))
                .toString()));
   }
 
   /**
    * Returns a new URI object, based on the specified URI template, with an updated port (scheme) and port.  If the port
    * number is -1, then the default port is used in the resulting URI. 
    * 
    * @param protocol the new protocol (scheme) in the resulting URI.
    * @param port the new port in the resulting URI, or the default port if -1 is provided.
    * @param template the source of all other values for the new URI.
    * @return a new URI object with the updated protocol and port.
    * @throws URISyntaxException 
    */
   public static URI changeProtocolAndPort(String protocol, int port, URI template) throws URISyntaxException {
    return new URI(protocol, template.getUserInfo(), template.getHost(), port, template.getPath(), template.getQuery(), null);
   }
 
 }
