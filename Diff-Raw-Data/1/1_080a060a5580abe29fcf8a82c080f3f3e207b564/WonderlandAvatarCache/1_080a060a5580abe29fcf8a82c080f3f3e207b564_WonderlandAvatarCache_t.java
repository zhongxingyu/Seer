 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 
 import com.jme.image.Texture;
 import com.jme.util.TextureManager;
 import imi.cache.CacheBehavior;
 import imi.utils.MD5HashUtils;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 /**
  * Wonderland implementation of the avatar CacheBehavior interface
  */
 public class WonderlandAvatarCache implements CacheBehavior {
 
     /** Logger ref **/
     private static final Logger logger = Logger.getLogger(WonderlandAvatarCache.class.getName());
     /** The folder we will be searching for and storing cache files in. **/
     private File cacheFolder = null;
     private String baseURL;
 
     /**
      * Construct a new cache using the specified folder.
      * @param cacheFolder
      */
     public WonderlandAvatarCache(String baseURL, File cacheFolder) {
         this.baseURL = baseURL;
         if (cacheFolder == null) {
             throw new ExceptionInInitializerError("Cannot have a null cache folder!");
         } else {
             this.cacheFolder = cacheFolder;
         }
     }
 
     @Override
     public boolean initialize(Object[] params) {
         // Nothing needs to be done currently.
         return true;
     }
 
     @Override
     public boolean shutdown() {
         return true;
     }
 
     @Override
     public boolean isFileCached(URL location) {
         File cacheFile = urlToCacheFile(location);
         return cacheFile.exists();
     }
 
     @Override
     public InputStream getStreamToResource(URL location) {
         File cacheFile = urlToCacheFile(location);
         InputStream result = null;
         if (cacheFile != null && cacheFile.exists()) {
             System.err.println("WonderlandAvatarCache using cached file " + location.toExternalForm());
             try {
                 result = new FileInputStream(cacheFile);
             } catch (FileNotFoundException ex) {
                 logger.severe("Although the cache file exists, a FileNotFoundException" +
                         "was thrown.");
             }
         }
         return result;
     }
 
     @Override
     public OutputStream getStreamForWriting(URL location) {
         File cacheFile = urlToCacheFile(location);
         OutputStream result = null;
         if (cacheFile != null) {
             try {
                 result = new FileOutputStream(cacheFile);
             } catch (FileNotFoundException ex) {
                 logger.severe("Although the cache file exists, a FileNotFoundException" +
                         "was thrown.");
             }
         }
         return result;
     }
 
     @Override
     public boolean clearCache() {
         for (File file : cacheFolder.listFiles()) {
             file.delete();
         }
         if (cacheFolder.listFiles().length == 0) {
             return true; // Success
         } else {
             return false;
         }
     }
 
     private File urlToCacheFile(URL location) {
         File localFile = null; // If a local version exists.
 
         // If the URL points to a local file, check the last modified time
         if (location.getProtocol().equalsIgnoreCase("file")) {
             try {
                 localFile = new File(location.toURI());
             } catch (URISyntaxException ex) {
                 logger.severe("Unable to form a file object from the URI");
             }
         }
 
         // Get the path relative to the "assets" folder
         String urlString = location.toString();
         int assetsIndex = urlString.indexOf("assets/");
 
         if (assetsIndex != 1) // If found, truncate
         {
             urlString = urlString.substring(assetsIndex + 7);
         }
 
         String hashFileName = MD5HashUtils.getStringFromHash(urlString.getBytes());
         File result = new File(cacheFolder, hashFileName);
 
         if (localFile != null) {
             // Determine which one is newer, if the cache version is older,
             // then we will delete it.
             if (localFile.lastModified() > result.lastModified()) {
                 result.delete();
             }
         }
 
         System.err.println("***** CACHE " + location.toExternalForm() + "  " + result);
 
         return result;
     }
 
     public Texture loadTexture(URL location) {
        logger.warning("WonderlandAvatarCache "+location.toExternalForm());
         if (location.getProtocol().equalsIgnoreCase("file")) {
             // Workaround for hard coded file:// urls in head bhf files
             String relativePath = location.toExternalForm();
             int assetsIndex = relativePath.indexOf("assets/");
             if (assetsIndex != -1) {
                 relativePath = relativePath.substring(assetsIndex);
             }
 
             URL localURL = null;
             try {
                 localURL = new URL(baseURL + relativePath);
                 return TextureManager.loadTexture(localURL);
             } catch (MalformedURLException ex) {
                 logger.warning("Error creating texture url "+baseURL+relativePath);
             }
 
         }
         return TextureManager.loadTexture(location);
     }
 }
