 /**
  * Project Wonderland
  *
  * $RCSfile: AssetType.java,v $
  *
  * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision: 1.3 $
  * $Date: 2007/05/04 23:11:34 $
  * $State: Exp $
  */
 package org.jdesktop.wonderland.common;
 
import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
import java.util.logging.Logger;
 
 /**
  * The ResourceURI class uniquely identifies a resource within the sytem that is
  * managed by the client-side asset management system. The format of a resource
  * URI is:
  * <p>
  * <protocol>://<module name>@<server name>:<port>/<asset path>
  * <p>
  * where <protocol> refers to the specific kind of asset (e.g. 'wla' for art
  * assets, 'wlj' for plugin assets), <module name> is the name of the module
  * where the asset lives, and <server name>:<port> is the server name from
  * which the asset comes, and <asset path> is the relative path of the asset
  * in the module.
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @ExperimentalAPI
public abstract class ResourceURI implements Serializable {
     /* The URI which is wrapped by this class */
     private URI uri = null;
     
     /** Default constructor */
     public ResourceURI() {
     }
     
     /**
      * Constructor which takes the string represents of the URI.
      * 
      * @param uri The string URI representation
      * @throw URISyntaxException If the URI is not well-formed
      */
     public ResourceURI(String uri) throws URISyntaxException {
         this.uri = new URI(uri);
     }
     
     /**
      * Returns the URI object
      * 
      * @return The actual URI object
      */
     public URI getURI() {
         return this.uri;
     }
  
     /**
      * Returns a URL from the URI.
      * 
      * @return A URL
      */
     public URL toURL() throws MalformedURLException {
         return this.uri.toURL();
     }
     
     /**
      * Returns the module name off this URI.
      * 
      * @return The module name
      */
     public String getModuleName() {
         /*
          * If the URI contains a hostname (modulename@host:port), then the
          * user info contains the module name. Otherwise, if user info is null,
          * then only the module name exists in the authority field.
          */
         String userInfo = this.uri.getUserInfo();
         String authority = this.uri.getAuthority();
         
         if (userInfo == null) {
             return authority;
         }
         return userInfo;
     }
     
     /**
      * Returns the raw relative path of the asset, without prepending any
      * assumed directory like "art/".
      */
     public String getRawPath() {
         String path = this.uri.getPath();
         if (path.startsWith("/") == true) {
             return path.substring(1);
         }
         return path;
     }
     
     /**
      * Returns the base URL of the server as a string, or null if not present
      * 
      * @return The base server URL
      */
     public String getServerURL() {
         String userInfo = this.uri.getUserInfo();
         String host = this.uri.getHost();
         int port = this.uri.getPort();
         
         /*
          * If a host:port is present, then host is non-null. Otherwise, if
          * user info is null, the only the module name is present and there is
          * no host name
          */
         if (userInfo == null) {
             return null;
         }
         
         String url = "http://" + host;
         if (port != -1) {
             url = url + ":" + port;
         }
         return url;
     }
     
     /**
      * Returns the relative path of the resource specified by the URI. The
      * relative path does not being with any forward "/".
      * 
      * @return The relative path within the URI
      */
     public abstract String getRelativePath();
     
     /**
      * Returns a relative path of the asset so that it exists in a unique
      * location within a cache. The path does not have a leading "/".
      * 
      * @return A unique relative path for the URI
      */
     public abstract String getRelativeCachePath();
     
     /**
      * Returns the string representation of the URI
      * 
      * @return The string representation of the URI
      */
     @Override
     public String toString() {
         return this.uri.toString();
     }
 }
