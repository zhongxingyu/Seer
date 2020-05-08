 /*
  * Copyright 2005 John R. Fallows
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.apress.projsf.weblets;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.URL;
 
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.java.dev.weblets.Weblet;
 import net.java.dev.weblets.WebletConfig;
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletException;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 
 import org.apache.commons.digester.Digester;
 
 import org.xml.sax.SAXException;
 
 public class WebletContainerImpl extends WebletContainer
 {
   public WebletContainerImpl() throws WebletException
   {
     this(null);
   }
 
   public WebletContainerImpl(
     MessageFormat webletURLFormat) throws WebletException
   {
     _webletURLFormat = webletURLFormat;
     try
     {
       ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Enumeration e = loader.getResources("META-INF/weblets-config.xml");
       while (e.hasMoreElements())
       {
         URL resource = (URL)e.nextElement();
         registerConfig(resource);
       }
 
       WebletContainer.setInstance(this);
     }
     catch (IOException e)
     {
       throw new WebletException(e);
     }
   }
 
   public void destroy()
   {
     Iterator i = _weblets.values().iterator();
     while (i.hasNext())
     {
       Weblet weblet = (Weblet)i.next();
       weblet.destroy();
     }
     _weblets = null;
     _webletConfigs = null;
     _webletMappings = null;
   }
 
   public WebletRequest getWebletRequest(
     String contextPath,
     String requestURI,
     long   ifModifiedSince)
   {
     Iterator i = _webletMappings.entrySet().iterator();
     while (i.hasNext())
     {
       Map.Entry entry = (Map.Entry)i.next();
       Pattern pattern = (Pattern)entry.getValue();
       Matcher matcher = pattern.matcher(requestURI);
       if (matcher.matches())
       {
         String webletPath = matcher.group(1);
         String pathInfo = matcher.group(2);
         String webletName = (String)entry.getKey();
         return new WebletRequestImpl(webletName, webletPath,
                                      contextPath, pathInfo,
                                      ifModifiedSince);
       }
     }
 
     return null;
   }
 
   public void service(
     WebletRequest  request,
     WebletResponse response) throws IOException, WebletException
   {
     String webletName = request.getWebletName();
 
     Weblet weblet = (Weblet)_weblets.get(webletName);
 
     if (weblet == null)
     {
       try
       {
         WebletConfigImpl config = (WebletConfigImpl)_webletConfigs.get(webletName);
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class klass = loader.loadClass(config.getWebletClass());
         weblet = (Weblet)klass.newInstance();
         weblet.init(config);
         _weblets.put(webletName, weblet);
       }
       catch (ClassNotFoundException e)
       {
         throw new WebletException(e);
       }
       catch (InstantiationException e)
       {
         throw new WebletException(e);
       }
       catch (IllegalAccessException e)
       {
         throw new WebletException(e);
       }
     }
 
     if (response.getDefaultContentType() == null)
     {
       String pathInfo = request.getPathInfo();
       WebletConfig webConfig = weblet.getWebletConfig();
       String mimeType = webConfig.getMimeType(pathInfo);
       response.setDefaultContentType(mimeType);
     }
 
     weblet.service(request, response);
   }
 
   public String getWebletURL(
     String webletName,
     String pathInfo) throws WebletException
   {
     WebletConfig config = (WebletConfig)_webletConfigs.get(webletName);
     if (config == null)
       throw new WebletException("Missing Weblet configuration for '" + webletName + "'");
 
     String webletPath = (String)_webletPaths.get(webletName);
     if (webletPath == null)
       throw new WebletException("Missing Weblet mapping for '" + webletName + "'");
 
     String webletVersion = config.getWebletVersion();
 
     // URL-syntax  /webletPath[$version]/pathInfo
     StringBuffer buffer = new StringBuffer();
     buffer.append(webletPath);
     if (webletVersion != null)
     {
       buffer.append('$');
       buffer.append(webletVersion);
     }
     buffer.append(pathInfo);
     String webletURL = buffer.toString();
 
     if (_webletURLFormat != null)
       webletURL = _webletURLFormat.format(new Object[]{webletURL});
 
     return webletURL;
   }
 
   public void registerConfig(
     URL webletsConfig)
   {
     try
     {
       InputStream in = webletsConfig.openStream();
       try
       {
         Digester digester = new Digester();
         digester.setValidating(false);
         digester.push(this);
         digester.addObjectCreate("weblets-config/weblet", WebletConfigImpl.class);
         digester.addSetNext("weblets-config/weblet", "addWeblet", WebletConfigImpl.class.getName());
         digester.addCallMethod("weblets-config/weblet/weblet-name",
                                "setWebletName", 0);
         digester.addCallMethod("weblets-config/weblet/weblet-class",
                                "setWebletClass", 0);
         digester.addCallMethod("weblets-config/weblet/weblet-version",
                                "setWebletVersion", 0);
         digester.addCallMethod("weblets-config/weblet/init-param",
                                "addInitParam", 2);
         digester.addCallParam("weblets-config/weblet/init-param/param-name", 0);
         digester.addCallParam("weblets-config/weblet/init-param/param-value", 1);
         digester.addCallMethod("weblets-config/weblet/mime-mapping",
                                "addMimeMapping", 2);
         digester.addCallParam("weblets-config/weblet/mime-mapping/extension", 0);
         digester.addCallParam("weblets-config/weblet/mime-mapping/mime-type", 1);
         digester.addCallMethod("weblets-config/weblet-mapping",
                                "setWebletMapping", 2);
         digester.addCallParam("weblets-config/weblet-mapping/weblet-name", 0);
         digester.addCallParam("weblets-config/weblet-mapping/url-pattern", 1);
         digester.parse(in);
       }
       finally
       {
         in.close();
       }
     }
     catch (IOException e)
     {
       throw new WebletException(e);
     }
     catch (SAXException e)
     {
       throw new WebletException(e);
     }
   }
 
   public void addWeblet(WebletConfigImpl webletConfig)
   {
     _webletConfigs.put(webletConfig.getWebletName(), webletConfig);
   }
 
   public void setWebletMapping(
     String webletName,
     String urlPattern)
   {
     WebletConfig webletConfig = (WebletConfig)_webletConfigs.get(webletName);
 
     if (webletConfig == null)
       throw new WebletException("Weblet configuration not found: " + webletName);
 
     Matcher matcher = _WEBLET_PATH_PATTERN.matcher(urlPattern);
     if (matcher.matches())
     {
       String webletVersion = webletConfig.getWebletVersion();
       String webletPath = matcher.group(1);
 
       StringBuffer buffer = new StringBuffer();
       buffer.append("(\\Q");
       buffer.append(webletPath);
       buffer.append("\\E)");
       if (webletVersion != null)
       {
         buffer.append("\\Q$");
         buffer.append(webletVersion);
         buffer.append("\\E");
       }
       buffer.append("(/.*)");;
 
       _webletMappings.put(webletName, Pattern.compile(buffer.toString()));
       _webletPaths.put(webletName, webletPath);
     }
     else
     {
       throw new IllegalArgumentException("Invalid weblet mapping: " + urlPattern);
     }
   }
 
   private MessageFormat _webletURLFormat;
   private Map _weblets = new HashMap();
   private Map _webletPaths = new HashMap();
   private Map _webletConfigs = new HashMap();
   private Map _webletMappings = new LinkedHashMap();
 
   static private final Pattern _WEBLET_PATH_PATTERN =
                                     Pattern.compile("(/[^\\*]+)?/\\*");
 }
