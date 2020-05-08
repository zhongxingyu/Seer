 /*
  * Copyright (c) 1998-2011 Caucho Technology -- all rights reserved
  *
  * This file is part of Resin(R) Open Source
  *
  * Each copy or derived work must preserve the copyright notice and this
  * notice unmodified.
  *
  * Resin Open Source is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Resin Open Source is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
  * of NON-INFRINGEMENT.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Resin Open Source; if not, write to the
  *
  *   Free Software Foundation, Inc.
  *   59 Temple Place, Suite 330
  *   Boston, MA 02111-1307  USA
  *   
  *
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @author Scott Ferguson
  * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
  */
 package org.jboss.arquillian.container.resin.embedded_4;
 
 import java.util.logging.LogManager;
 
 import com.caucho.cloud.topology.CloudCluster;
 import com.caucho.cloud.topology.CloudServer;
 import com.caucho.cloud.topology.CloudSystem;
 import com.caucho.cloud.topology.TopologyService;
 import com.caucho.config.ConfigException;
 import com.caucho.config.types.RawString;
 import com.caucho.lifecycle.Lifecycle;
 import com.caucho.loader.Environment;
 import com.caucho.resin.ArquillianWebAppProgram;
 import com.caucho.resin.PortEmbed;
 import com.caucho.resin.WebAppEmbed;
 import com.caucho.server.cluster.Server;
 import com.caucho.server.host.Host;
 import com.caucho.server.host.HostConfig;
 import com.caucho.server.resin.Resin;
 import com.caucho.server.webapp.WebAppConfig;
 import com.caucho.util.L10N;
 import com.caucho.vfs.Vfs;
 
 /**
  * Embeddable version of the Resin server.
  *
  * <code><pre>
  * ResinEmbed resin = new ResinEmbed();
  *
  * HttpEmbed http = new HttpEmbed(8080);
  * resin.addPort(http);
  *
  * WebAppEmbed webApp = new WebAppEmbed("/foo", "/home/ferg/ws/foo");
  *
  * resin.addWebApp(webApp);
  *
  * resin.start();
  *
  * resin.join();
  * </pre></code>
  */
 public class ResinEmbedded
 {
   private static final L10N L = new L10N(ResinEmbedded.class);
 
   private static final String EMBED_CONF
     = "classpath:com/caucho/resin/resin-embed.xml";
 
   private final Resin _resin;
   private String _configFile = EMBED_CONF;
 
   private CloudCluster _cluster;
   // private ClusterServer _clusterServer;
   
   private Host _host;
   private Server _server;
 
   private String _serverHeader;
 
   private Lifecycle _lifecycle = new Lifecycle();
   private boolean _isConfig;
   private boolean _isDevelopmentMode;
 
   private PortEmbed port;
   
   /**
    * Creates a new resin server.
    */
   public ResinEmbedded()
   {
     _resin = Resin.create("embed");
     _resin.setRootDirectory(Vfs.lookup());
   }
 
   /**
    * Creates a new resin server.
    */
   public ResinEmbedded(String configFile)
   {
     this();
     
     setConfig(configFile);
   }
 
   //
   // Configuration/Injection methods
   //
   
   /**
    * Sets the root directory
    */
   public void setRootDirectory(String rootUrl)
   {
     _resin.setRootDirectory(Vfs.lookup(rootUrl));
   }
 
   /**
    * Sets the config file
    */
   public void setConfig(String configFile)
   {
     _configFile = configFile;
   }
 
   /**
    * Adds a port to the server, e.g. a HTTP port.
    *
    * @param port the embedded port to add to the server
    */
   public void setPort(PortEmbed port)
   {
      this.port = port;
      
     /*
     // server/1e00
     if (_clusterServer == null)
       initConfig(_configFile);
     
     // XXX: port.bindTo(_clusterServer);
      */
   }
 
   /**
    * Sets the server header
    */
   public void setServerHeader(String serverName)
   {
     _serverHeader = serverName;
   }
 
   /**
    * Adds a web-app to the server.
    */
   public void addWebApp(WebAppEmbed webApp)
   {
     if (webApp == null)
       throw new NullPointerException();
 
     Thread thread = Thread.currentThread();
     ClassLoader oldLoader = thread.getContextClassLoader();
 
     try 
     {
       thread.setContextClassLoader(_server.getClassLoader());
 
    
        WebAppConfig config = new WebAppConfig();
        config.setContextPath(webApp.getContextPath());
        config.setRootDirectory(new RawString(webApp.getRootDirectory()));
        
        if (webApp.getArchivePath() != null)
          config.setArchivePath(new RawString(webApp.getArchivePath()));
    
        config.addBuilderProgram(new ArquillianWebAppProgram(webApp));
       
       _host.addWebApp(config);
       
        _host.getWebAppContainer().addWebApp(config);

     }
     finally
     {
        thread.setContextClassLoader(oldLoader);
     }
   }
 
   public void setDevelopmentMode(boolean isDevelopment)
   {
     _isDevelopmentMode = isDevelopment;
   }
 
   /**
    * Initialize the Resin environment
    */
   public void initializeEnvironment()
   {
     Environment.initializeEnvironment();
   }
 
   /**
    * Set log handler
    */
   public void resetLogManager()
   {
     LogManager.getLogManager().reset();
 
     /*
     Logger log = Logger.getLogger("");
     log.addHandler(new PathHandler(Vfs.lookup(path)));
     */
   }
 
   //
   // Lifecycle
   //
 
   /**
    * Starts the embedded server
    */
   public void start()
   {
     if (! _lifecycle.toActive())
       return;
 
     Thread thread = Thread.currentThread();
     ClassLoader oldLoader = thread.getContextClassLoader();
 
     try {
       Environment.initializeEnvironment();
 
       _resin.preConfigureInit();
       
       thread.setContextClassLoader(_resin.getClassLoader());
 
       initConfig(_configFile);
 
       _server = _resin.createServer();
 
       thread.setContextClassLoader(_server.getClassLoader());
 
       if (_serverHeader != null)
         _server.setServerHeader(_serverHeader);
       
       _server.setDevelopmentModeErrorPage(_isDevelopmentMode);
 
       port.bindTo(_server);
       
       _resin.start();
       
       HostConfig hostConfig = new HostConfig();
       _server.addHost(hostConfig);
       _host = _server.getHost("", 0);
 
       if (_host == null) {
         throw new ConfigException(L.l("ResinEmbed requires a <host> to be configured in the resin.xml, because the webapps must belong to a host."));
       }
 
       thread.setContextClassLoader(_host.getClassLoader());
 
     } catch (Exception e) {
       throw ConfigException.create(e);
     } finally {
       thread.setContextClassLoader(oldLoader);
     }
   }
 
   /**
    * Stops the embedded server
    */
   public void stop()
   {
     if (! _lifecycle.toStop())
       return;
 
     try {
       _resin.stop();
     } catch (RuntimeException e) {
       throw e;
     } catch (Throwable e) {
       throw ConfigException.create(e);
     }
   }
 
   /**
    * Waits for the Resin process to exit.
    */
   public void join()
   {
     while (! _resin.isClosed()) {
       try {
         Thread.sleep(1000);
       } catch (Exception e) {
       }
     }
   }
 
   /**
    * Destroys the embedded server
    */
   public void destroy()
   {
     if (! _lifecycle.toDestroy())
       return;
 
     try {
       _resin.close();
     } catch (RuntimeException e) {
       throw e;
     } catch (Throwable e) {
       throw ConfigException.create(e);
     }
   }
 
   //
   // utilities
   //
 
   private void initConfig(String configFile)
   {
     try {
       if (_isConfig)
         return;
       _isConfig = true;
 
       _resin.configureFile(Vfs.lookup(configFile));
     } catch (Exception e) {
       throw ConfigException.create(e);
     }
     
     CloudSystem cloudSystem = TopologyService.getCurrent().getSystem();
 
     if (cloudSystem.getClusterList().length == 0)
       throw new ConfigException(L.l("Resin needs at least one defined <cluster>"));
 
     String clusterId = cloudSystem.getClusterList()[0].getId();
 
     _cluster = cloudSystem.findCluster(clusterId);
 
     if (_cluster.getPodList().length == 0)
       throw new ConfigException(L.l("Resin needs at least one defined <server> or <cluster-pod>"));
 
     if (_cluster.getPodList()[0].getServerList().length == 0)
       throw new ConfigException(L.l("Resin needs at least one defined <server>"));
 
     CloudServer cloudServer = _cluster.getPodList()[0].getServerList()[0]; 
     // _clusterServer = cloudServer.getData(ClusterServer.class);
     
     if (cloudServer != null)
       _resin.setServerId(cloudServer.getId());
   }
 
   protected void finalize()
     throws Throwable
   {
     super.finalize();
 
     destroy();
   }
 }
