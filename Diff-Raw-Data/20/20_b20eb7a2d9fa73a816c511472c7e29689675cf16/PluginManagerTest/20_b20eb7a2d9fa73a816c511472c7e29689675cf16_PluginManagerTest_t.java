 // ========================================================================
 // Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
 // ------------------------------------------------------------------------
 // All rights reserved. This program and the accompanying materials
 // are made available under the terms of the Eclipse Public License v1.0
 // and Apache License v2.0 which accompanies this distribution.
 // The Eclipse Public License is available at
 // http://www.eclipse.org/legal/epl-v10.html
 // The Apache License v2.0 is available at
 // http://www.opensource.org/licenses/apache2.0.php
 // You may elect to redistribute this code under either of these licenses.
 // ========================================================================
 
 package org.eclipse.jetty.plugins.aether;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarFile;
 
 import org.eclipse.jetty.plugins.PluginManager;
 import org.eclipse.jetty.plugins.impl.PluginManagerImpl;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 /* ------------------------------------------------------------ */
 /**
  */
 @RunWith(MockitoJUnitRunner.class)
 public class PluginManagerTest
 {
     @Mock
     private AetherService _aetherService;
 
     private PluginManager _pluginManager;
 
     private ClassLoader _classLoader = this.getClass().getClassLoader();
     private String _tmpDir;
 
     /* ------------------------------------------------------------ */
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
        URL resource = this.getClass().getResource("/jetty_home");
        _tmpDir = resource.getFile();
         _pluginManager =  new PluginManagerImpl(_aetherService, _tmpDir);
     }
 
     @Test
     public void testListAvailablePlugins()
     {
         List<String> pluginNames = new ArrayList<String>();
         pluginNames.add("jetty-plugin-jta");
         pluginNames.add("jetty-plugin-jmx");
         when(_aetherService.listAvailablePlugins()).thenReturn(pluginNames);
         List<String> availablePlugins = _pluginManager.listAvailablePlugins();
         assertTrue(availablePlugins.contains("jetty-plugin-jta"));
         assertTrue(availablePlugins.contains("jetty-plugin-jmx"));
     }
 
     @Test
     public void testInstallPlugins() throws IOException
     {
         String pluginName = "jetty-plugin-jta";
         String jtaPluginJar = _classLoader.getResource("jta.jar").getFile();
         when(_aetherService.getPluginJar(pluginName)).thenReturn(new JarFile(new File(jtaPluginJar)));
         _pluginManager.installPlugin(pluginName);
         assertTrue(new File(_tmpDir + File.separator +"etc"+ File.separator +"jetty-hightide.xml").exists());
         assertTrue(new File(_tmpDir + File.separator +"start.d"+ File.separator +"20-jta.ini").exists());
         assertTrue(new File(_tmpDir + File.separator +"lib"+ File.separator +"jta").exists());
     }
 
 }
