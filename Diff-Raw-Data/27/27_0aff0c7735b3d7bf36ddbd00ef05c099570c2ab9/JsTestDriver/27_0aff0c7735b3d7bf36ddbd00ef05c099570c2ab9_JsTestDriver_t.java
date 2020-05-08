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
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.LogManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.browser.BrowserPanicException;
 import com.google.jstestdriver.config.CmdFlags;
 import com.google.jstestdriver.config.CmdLineFlag;
 import com.google.jstestdriver.config.CmdLineFlagsFactory;
 import com.google.jstestdriver.config.Configuration;
 import com.google.jstestdriver.config.ConfigurationException;
 import com.google.jstestdriver.config.InitializeModule;
 import com.google.jstestdriver.config.Initializer;
 import com.google.jstestdriver.config.InvalidFlagException;
 import com.google.jstestdriver.config.UnreadableFilesException;
 import com.google.jstestdriver.config.UserConfigurationSource;
 import com.google.jstestdriver.config.YamlParser;
 import com.google.jstestdriver.embedded.JsTestDriverBuilder;
 import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
 import com.google.jstestdriver.hooks.PluginInitializer;
 import com.google.jstestdriver.hooks.ServerListener;
 import com.google.jstestdriver.model.ConcretePathPrefix;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 import com.google.jstestdriver.model.NullPathPrefix;
 import com.google.jstestdriver.output.TestResultListener;
 import com.google.jstestdriver.runner.RunnerMode;
 import com.google.jstestdriver.util.NullStopWatch;
 import com.google.jstestdriver.util.RetryException;
 
 public class JsTestDriver {
 
   /**
    * @author corysmith@google.com (Cory Smith)
    * 
    */
   private final class PluginInitializerModule implements Module {
     public void configure(Binder binder) {
       Multibinder<PluginInitializer> setBinder =
           Multibinder.newSetBinder(binder, PluginInitializer.class);
       for (Class<? extends PluginInitializer> initializer : initializers) {
         setBinder.addBinding().to(initializer);
       }
     }
   }
 
   private static final Logger logger = LoggerFactory.getLogger(JsTestDriver.class);
   private final Configuration defaultConfiguration;
   private final PluginLoader pluginLoader;
   private final List<Class<? extends PluginInitializer>> initializers;
   private final String[] defaultFlags;
   private final RunnerMode runnerMode;
   private final int port;
   private final List<Module> pluginModules;
   private final List<ServerListener> serverListeners;
   private final List<TestResultListener> testListeners;
   private final File baseDir;
   private final String serverAddress;
 
   /**
    * @param strings
    * @param runnerMode
    * @param initializers
    * @param pluginLoader
    * @param configuration
    * @param testListeners
    * @param serverListeners
    * @param pluginModules
    * @param port
    * @param baseDir
    * @param serverAddress
    * @param injector
    */
   public JsTestDriver(Configuration configuration, PluginLoader pluginLoader,
       List<Class<? extends PluginInitializer>> initializers, RunnerMode runnerMode, String[] flags,
       int port, List<Module> pluginModules, List<ServerListener> serverListeners,
       List<TestResultListener> testListeners, File baseDir, String serverAddress) {
     this.defaultConfiguration = configuration;
     this.pluginLoader = pluginLoader;
     this.initializers = initializers;
     this.runnerMode = runnerMode;
     this.defaultFlags = flags;
     this.port = port;
     this.pluginModules = pluginModules;
     this.serverListeners = serverListeners;
     this.testListeners = testListeners;
     this.baseDir = baseDir;
     this.serverAddress = serverAddress;
   }
 
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
 
       JsTestDriverBuilder builder = new JsTestDriverBuilder();
       builder.setBaseDir(cmdLineFlags.getBasePath().getCanonicalFile());
       builder.setConfigurationSource(cmdLineFlags.getConfigurationSource());
       builder.addPluginModules(pluginModules);
       builder.withPluginInitializer(TestResultPrintingInitializer.class);
       builder.setRunnerMode(cmdLineFlags.getRunnerMode());
       builder.setFlags(cmdLineFlags.getUnusedFlagsAsArgs());
       JsTestDriver jstd = builder.build();
       jstd.runConfiguration();
 
       logger.info("Finished action run.");
     } catch (InvalidFlagException e) {
       e.printErrorMessages(System.out);
       CmdFlags.printUsage(System.out);
       System.exit(1);
     } catch (UnreadableFilesException e) {
       System.out.println("Configuration Error: \n" + e.getMessage());
       System.exit(1);
     } catch (RetryException e) {
       System.out.println("Tests failed due to unexpected environment issue: "
           + e.getCause().getMessage());
       System.exit(1);
     } catch (FailureException e) {
       System.out.println("Tests failed: " + e.getMessage());
       System.exit(1);
    } catch (BrowserPanicException e) {
      System.out.println("Test run failed due to unresponsive browser: " + e);
      System.exit(1);
     } catch (Exception e) {
       logger.debug("Error {}", e);
       e.printStackTrace();
       System.out.println("Unexpected Runner Condition: " + e.getMessage()
           + "\n Use --runnerMode DEBUG for more information.");
       System.exit(1);
     }
   }
 
   public void startServer() {
     // TODO(corysmith): refactor these to do less hacking.
     if (port == -1) {
       throw new ConfigurationException("Port not defined, cannot start local server.");
     }
     runConfigurationWithFlags(defaultConfiguration,
         createFlagsArray("--port", String.valueOf(port)));
   }
 
   /**
    * @return
    */
   private String[] createFlagsArray(String...coreflags) {
     List<String> flags = Lists.newArrayList(coreflags);
     if (serverAddress != null && !serverAddress.isEmpty()) {
       flags.add("--server");
       flags.add(serverAddress);
     }
     if (port > 0) {
       flags.add("--port");
       flags.add(String.valueOf(port));
     }
     CmdLineFlag handlerPrefixFlag = findServerHandlerPrefixFlag();
     if (handlerPrefixFlag != null) {
       handlerPrefixFlag.addToArgs(flags);
     }
     String[] flagsArray = flags.toArray(new String[flags.size()]);
     return flagsArray;
   }
 
   public void stopServer() {
     // TODO(corysmith): refactor these to do less hacking.
     CmdLineFlag handlerPrefixFlag = findServerHandlerPrefixFlag();
     HandlerPathPrefix prefix = new NullPathPrefix();
     if (handlerPrefixFlag != null) {
       prefix = new ConcretePathPrefix(handlerPrefixFlag.value);
     }
     StringBuilder urlBuilder =
         new StringBuilder(defaultConfiguration.getServer(serverAddress, port, prefix));
     urlBuilder.append("/");
     urlBuilder.append("quit");
     new HttpServer(new NullStopWatch()).ping(urlBuilder.toString());
 
   }
 
   /**
    * Runs the default configuration with the default flags.
    */
   public List<TestCase> runConfiguration() {
     runConfigurationWithFlags(defaultConfiguration, defaultFlags);
     return null;
   }
 
   public List<TestCase> runAllTests(String path) {
     return runAllTests(parseConfiguration(path));
   }
 
   public List<TestCase> runAllTests(Configuration config) {
     // TODO(corysmith): Refactor to avoid passing string flags.
     runConfigurationWithFlags(config, createFlagsArray("--tests", "all"));
     return null;
   }
 
   public List<TestCase> runTests(Configuration config, List<String> tests) {
     // TODO(corysmith): Refactor to avoid passing string flags.
     runConfigurationWithFlags(config,
       createFlagsArray("--tests", Joiner.on(",").join(tests)));
     return null;
   }
 
   public void reset() {
     // TODO(corysmith): Refactor to avoid passing string flags.
     runConfigurationWithFlags(defaultConfiguration, createFlagsArray("--reset"));
   }
 
   public List<TestCase> getTestCasesFor(String path) {
     return getTestCasesFor(parseConfiguration(path));
   }
 
   public List<TestCase> getTestCasesFor(Configuration config) {
     // TODO(corysmith): Refactor to avoid passing string flags.
     runConfigurationWithFlags(config, createFlagsArray("--dryRunFor", "all"));
     return null;
   }
 
   private Configuration parseConfiguration(String path) {
     File configFile = new File(path);
     if (!configFile.exists()) {
       throw new ConfigurationException("Could not find " + configFile);
     }
     return new UserConfigurationSource(configFile).parse(baseDir, new YamlParser());
   }
 
   private void runConfigurationWithFlags(Configuration config, String[] flags) {
     List<Module> initializeModules = Lists.newArrayList();
     File basePath;
     try {
       // configure logging before we start seriously processing.
       LogManager.getLogManager().readConfiguration(runnerMode.getLogConfig());
       basePath = config.getBasePath().getCanonicalFile();
       initializeModules.add(new InitializeModule(pluginLoader, basePath, new Args4jFlagsParser(),
           runnerMode));
       initializeModules.add(new PluginInitializerModule());
     } catch (IOException e) {
       throw new ConfigurationException("Could not find " + config.getBasePath(), e);
     }
     Injector initializeInjector = Guice.createInjector(initializeModules);
 
     List<Module> actionRunnerModules;
     actionRunnerModules =
         initializeInjector.getInstance(Initializer.class).initialize(pluginModules,
             defaultConfiguration, runnerMode, flags);
     Injector injector = Guice.createInjector(actionRunnerModules);
     injector.getInstance(ActionRunner.class).runActions();
   }
 
   private CmdLineFlag findServerHandlerPrefixFlag() {
     // TODO(corysmith): refactor these to do less hacking.
     for (int i = 0; i < defaultFlags.length; i++) {
       if (defaultFlags[i].startsWith("--serverHandlerPrefix")) {
         String flag = defaultFlags[i];
         String value = "";
         if (i + 1 < defaultFlags.length) {
           value = defaultFlags[i + 1];
         }
         return new CmdLineFlag(flag, value);
       }
     }
     return null;
   }
 }
