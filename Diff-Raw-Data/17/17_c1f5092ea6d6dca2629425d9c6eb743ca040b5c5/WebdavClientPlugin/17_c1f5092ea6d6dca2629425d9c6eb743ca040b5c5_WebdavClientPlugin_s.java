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
 package org.jdesktop.wonderland.modules.webdav.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;
 import org.apache.commons.httpclient.HttpURL;
 import org.apache.webdav.lib.WebdavResource;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.ClientPlugin;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
 import org.jdesktop.wonderland.modules.webdav.common.FileContentCollection;
 
 /**
  * Register the WebdavContentRepository as the content repository for this
  * session
  * @author jkaplan
  */
 public class WebdavClientPlugin implements ClientPlugin {
     private static final Logger logger =
             Logger.getLogger(WebdavClientPlugin.class.getName());
 
     public void initialize(ServerSessionManager loginInfo) {
         String baseURL = loginInfo.getServerURL() + "/webdav/content";
 
         // register the webdav repository for this session
         try {
             WebdavResource wdr = new WebdavResource(new HttpURL(baseURL), true);
             WebdavContentCollection root =
                     new WebdavContentCollection(wdr, null)
             {
                 // don't include the root node in the path
                 @Override
                 public String getPath() {
                     return "";
                 }
             };
             WebdavContentRepository repo =
                     new WebdavContentRepository(root, loginInfo.getUsername());
 
             ContentRepositoryRegistry.getInstance().registerRepository(loginInfo, repo);
         } catch (IOException ioe) {
             logger.log(Level.WARNING, "Unable to start content repository", ioe);
         }
 
         // register the local repository
         String dirName = "localRepo-" + loginInfo.getUsername();
         final File userDir = ClientContext.getUserDirectory(dirName);
         FileContentCollection localRepo = new FileContentCollection(userDir, null) {
             @Override
             protected URL getBaseURL() {
                 try {
                     return userDir.toURI().toURL();
                 } catch (MalformedURLException ex) {
                     logger.log(Level.WARNING, "Unable to create local repository", ex);
                     return null;
                 }
             }
 
         };
         ContentRepositoryRegistry.getInstance().registerLocalRepository(localRepo);
     }
 }
