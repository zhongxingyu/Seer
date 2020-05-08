 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.myfaces.extensions.scripting.cdi.core;
 
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 import org.apache.myfaces.extensions.scripting.jsf.startup.StartupServletContextPluginChainLoader;
 import org.apache.webbeans.config.OWBLogConst;
 import org.apache.webbeans.corespi.scanner.AbstractMetaDataDiscovery;
 import org.apache.webbeans.corespi.scanner.AnnotationDB;
 import org.apache.webbeans.corespi.se.BeansXmlAnnotationDB;
 import org.apache.webbeans.exception.WebBeansConfigurationException;
 import org.apache.webbeans.logger.WebBeansLogger;
 import org.apache.webbeans.util.WebBeansUtil;
 import org.scannotation.WarUrlFinder;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Configures the web application to find beans.
  */
 public class ExtScriptScannerService extends AbstractMetaDataDiscovery
 {
     private static final String RELOADING_LISTENER = "ReloadingListener";
     private final WebBeansLogger logger = WebBeansLogger.getLogger(ExtScriptScannerService.class);
 
     private boolean configure = false;
 
     protected ServletContext servletContext = null;
 
     public ExtScriptScannerService()
     {
 
     }
 
     public void init(Object context)
     {
         super.init(context);
         this.servletContext = (ServletContext) context;
         initExtScript(this.servletContext);
     }
 
     private void initExtScript(ServletContext servletContext)
     {
         try
         {

            if (servletContext.getAttribute(RELOADING_LISTENER) == null)
            {
                StartupServletContextPluginChainLoader.startup(servletContext);
                servletContext.setAttribute(RELOADING_LISTENER, new ReloadingListener());
                WeavingContext.getInstance().addListener((ReloadingListener) servletContext.getAttribute(RELOADING_LISTENER));
            }
         }
         catch (IOException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     protected void configure()
     {
         try
         {
             if (!configure)
             {
                 Set<String> arcs = getArchives();
                 String[] urls = new String[arcs.size()];
                 urls = arcs.toArray(urls);
 
                 getAnnotationDB().scanArchives(urls);
 
                 configure = true;
             }
 
         }
         catch (Exception e)
         {
             throw new WebBeansConfigurationException(logger.getTokenString(OWBLogConst.ERROR_0002), e);
         }
 
     }
 
     /**
      * @return all beans.xml paths
      */
     private Set<String> getArchives() throws Exception
     {
         Set<String> lists = createURLFromMarkerFile();
         String warUrlPath = createURLFromWARFile();
         String targetUrlPath = createURLFromTargetPath();
 
         if (warUrlPath != null)
         {
             lists.add(warUrlPath);
         }
         lists.add(targetUrlPath);
 
         return lists;
     }
 
     protected String createURLFromTargetPath()
     {
         File targetPath = WeavingContext.getInstance().getConfiguration().getCompileTarget();
         return "file:" + targetPath.getAbsolutePath();
     }
 
     /* Creates URLs from the marker file */
     protected Set<String> createURLFromMarkerFile() throws Exception
     {
         Set<String> listURL = new HashSet<String>();
 
         // Root with beans.xml marker.
         String[] urls = findBeansXmlBases("META-INF/beans.xml", WebBeansUtil.getCurrentClassLoader());
 
         if (urls != null)
         {
             String addPath;
             for (String url : urls)
             {
                 String fileDir = new URL(url).getFile();
                 if (fileDir.endsWith(".jar!/"))
                 {
                     fileDir = fileDir.substring(0, fileDir.lastIndexOf("/")) + "/META-INF/beans.xml";
 
                     //fix for weblogic
                     if (!fileDir.startsWith("file:/"))
                     {
                         fileDir = "file:/" + fileDir;
                         //TODO switch to a more stable approach
                         //url = new URL("jar:" + fileDir);
                     }
 
                     if (logger.wblWillLogDebug())
                     {
                         logger.debug("OpenWebBeans found the following url while doing web scanning: " + fileDir);
                     }
 
                     addPath = "jar:" + fileDir;
 
                     if (logger.wblWillLogDebug())
                     {
                         logger.debug("OpenWebBeans added the following jar based path while doing web scanning: " +
                                 addPath);
                     }
                 } else
                 {
                     //X TODO check!
                     addPath = "file:" + url + "META-INF/beans.xml";
 
                     if (logger.wblWillLogDebug())
                     {
                         logger.debug("OpenWebBeans added the following file based path while doing web scanning: " +
                                 addPath);
                     }
 
                 }
 
                 listURL.add(url);
             }
         }
 
         return listURL;
     }
 
     /**
      * Returns the web application class path if it contains
      * a beans.xml marker file.
      *
      * @return the web application class path
      * @throws Exception if any exception occurs
      */
     protected String createURLFromWARFile() throws Exception
     {
         if (servletContext == null)
         {
             // this may happen if we are running in a test container, in IDE development, etc
             return null;
         }
 
         URL url = servletContext.getResource("/WEB-INF/beans.xml");
 
         if (url != null)
         {
             addWebBeansXmlLocation(url);
             URL resourceUrl = WarUrlFinder.findWebInfClassesPath(this.servletContext);
 
             if (resourceUrl == null)
             {
                 return null;
             }
 
             //set resource to beans.xml mapping
             AnnotationDB annotationDB = getAnnotationDB();
 
             if (annotationDB instanceof BeansXmlAnnotationDB)
             {
                 ((BeansXmlAnnotationDB) annotationDB).setResourceBeansXml(resourceUrl.toExternalForm(), url.toExternalForm());
             }
             return resourceUrl.toExternalForm();
         }
 
         return null;
     }
 
 }
