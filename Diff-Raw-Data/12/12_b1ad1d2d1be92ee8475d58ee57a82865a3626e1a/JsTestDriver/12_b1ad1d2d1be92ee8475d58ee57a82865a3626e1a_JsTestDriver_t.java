 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.LogManager;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.multibindings.Multibinder;
 import com.google.jstestdriver.config.CmdFlags;
 import com.google.jstestdriver.config.CmdLineFlagsFactory;
 import com.google.jstestdriver.config.Configuration;
 import com.google.jstestdriver.config.InitializeModule;
 import com.google.jstestdriver.config.Initializer;
 import com.google.jstestdriver.config.UnreadableFilesException;
 import com.google.jstestdriver.config.YamlParser;
 import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
 import com.google.jstestdriver.hooks.PluginInitializer;
 
 public class JsTestDriver {
 
   private static final Logger logger = LoggerFactory.getLogger(JsTestDriver.class);
 
   public static void main(String[] args) {
     try {
       // pre-parse parsing... These are the flags
       // that must be dealt with before we parse the flags.
       CmdFlags cmdLineFlags = new CmdLineFlagsFactory().create(args);
       List<Plugin> cmdLinePlugins = cmdLineFlags.getPlugins();
 
       // configure logging before we start seriously processing.
       LogManager.getLogManager().readConfiguration(cmdLineFlags.getRunnerMode().getLogConfig());
 
       final PluginLoader pluginLoader = new PluginLoader();
 
       // load all the command line plugins.
       final List<Module> pluginModules = pluginLoader.load(cmdLinePlugins);
       logger.debug("loaded plugins %s", pluginModules);
       List<Module> initializeModules = Lists.newLinkedList(pluginModules);
 
       Configuration configuration =
           cmdLineFlags.getConfigurationSource().parse(cmdLineFlags.getBasePath(), new YamlParser());
 
       File basePath = configuration.getBasePath().getCanonicalFile();
       initializeModules.add(
           new InitializeModule(pluginLoader,
           basePath,
           new Args4jFlagsParser(cmdLineFlags),
           cmdLineFlags.getRunnerMode()));
       initializeModules.add(new Module() {
         public void configure(Binder binder) {
           Multibinder.newSetBinder(binder, PluginInitializer.class).addBinding()
               .to(TestResultPrintingInitializer.class);
         }
       });
       Injector initializeInjector = Guice.createInjector(initializeModules);
 
       final List<Module> actionRunnerModules =
           initializeInjector.getInstance(Initializer.class)
               .initialize(pluginModules, configuration, cmdLineFlags.getRunnerMode(),
                   cmdLineFlags.getUnusedFlagsAsArgs());
 
       Injector injector = Guice.createInjector(actionRunnerModules);
       injector.getInstance(ActionRunner.class).runActions();
       logger.info("Finished action run.");
     } catch (CmdLineException e) {
       System.out.println(e.getMessage());
       System.exit(1);
     } catch (UnreadableFilesException e) {
      System.out.println("Configuration Error: \n" + e.getMessage());
       System.exit(1);
     } catch (FailureException e) {
       System.out.println("Tests failed: " + e.getMessage());
       System.exit(1);
     } catch (Exception e) {
       logger.debug("Error {}", e);
       e.printStackTrace();
       System.out.println("Unexpected Runner Condition: " + e.getMessage()
           + "\n Use --runnerMode DEBUG for more information.");
       System.exit(1);
     }
   }
 }
