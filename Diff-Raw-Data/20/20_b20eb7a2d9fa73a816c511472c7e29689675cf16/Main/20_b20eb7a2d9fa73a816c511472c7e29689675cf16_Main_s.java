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
 
 package org.eclipse.jetty.plugins;
 
 import java.util.List;
 
 import org.eclipse.jetty.plugins.aether.AetherService;
 import org.eclipse.jetty.plugins.aether.impl.AetherServiceImpl;
 import org.eclipse.jetty.plugins.impl.PluginManagerImpl;
 
 /* ------------------------------------------------------------ */
 /**
  */
 public class Main
 {
     private AetherService _aetherService = new AetherServiceImpl();
     private PluginManager _pluginManager;
     private String _jettyHome;
     private String _installPlugin;
     private boolean _listPlugins;
 
     /* ------------------------------------------------------------ */
     /**
      * @param args
      */
     public static void main(String[] args)
     {
         Main main = new Main();
         main.execute(args);
     }
 
     private void execute(String[] args)
     {
         parseCommandline(args);
 
         _pluginManager = new PluginManagerImpl(_aetherService,_jettyHome);
 
         if (_listPlugins)
             listPlugins();
 
         if (_installPlugin != null)
             installPlugin();
     }
 
     private void listPlugins()
     {
         List<String> availablePlugins = _pluginManager.listAvailablePlugins();
         for (String plugin : availablePlugins)
             System.out.println(plugin);
     }
 
     private void installPlugin()
     {
         _pluginManager.installPlugin(_installPlugin);
         System.out.println("Successfully installed plugin: " + _installPlugin);
     }
 
     private void parseCommandline(String[] args)
     {
         for (String arg : args)
         {
             if (arg.startsWith("--jettyHome="))
                 _jettyHome = arg.substring(12);
            if (arg.startsWith("--install-plugin="))
                _installPlugin = arg.substring(17);
             if ("--listPlugins".equals(arg))
                 _listPlugins = true;
         }
         if (_jettyHome == null && _installPlugin != null)
             throw new IllegalArgumentException("No --jettyHome commandline option specified!");
         if (_installPlugin == null && _listPlugins == false)
            throw new IllegalArgumentException("Neither --install-plugin=<pluginname> nor --list-plugins commandline option specified. Nothing to do for me!");
         if (_installPlugin != null && _listPlugins)
             throw new IllegalArgumentException(
                    "Please specify either --install-plugin=<pluginname> or --list-plugins commandline options, but not both at the same time!");
     }
 }
