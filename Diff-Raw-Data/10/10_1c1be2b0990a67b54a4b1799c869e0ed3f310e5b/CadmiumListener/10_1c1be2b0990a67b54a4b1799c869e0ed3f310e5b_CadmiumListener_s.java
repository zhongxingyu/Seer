 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.servlets.guice;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.ws.rs.Path;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 import org.jgroups.JChannel;
 import org.jgroups.MembershipListener;
 import org.jgroups.MessageListener;
 import org.jgroups.Receiver;
 import org.reflections.Reflections;
 import org.reflections.vfs.Vfs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.grapher.GrapherModule;
 import com.google.inject.grapher.InjectorGrapher;
 import com.google.inject.grapher.graphviz.GraphvizModule;
 import com.google.inject.grapher.graphviz.GraphvizRenderer;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.name.Names;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.meltmedia.cadmium.core.CadmiumModule;
 import com.meltmedia.cadmium.core.CommandAction;
 import com.meltmedia.cadmium.core.ConfigurationGitService;
 import com.meltmedia.cadmium.core.ConfigurationWorker;
 import com.meltmedia.cadmium.core.ContentGitService;
 import com.meltmedia.cadmium.core.ContentService;
 import com.meltmedia.cadmium.core.ContentWorker;
 import com.meltmedia.cadmium.core.CoordinatedWorker;
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.SiteDownService;
 import com.meltmedia.cadmium.core.commands.CommandBodyMapProvider;
 import com.meltmedia.cadmium.core.commands.CommandMapProvider;
 import com.meltmedia.cadmium.core.commands.CommandResponse;
 import com.meltmedia.cadmium.core.commands.ContentUpdateRequest;
 import com.meltmedia.cadmium.core.commands.HistoryResponseCommandAction;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.history.HistoryManager;
 import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
 import com.meltmedia.cadmium.core.messaging.ChannelMember;
 import com.meltmedia.cadmium.core.messaging.MembershipTracker;
 import com.meltmedia.cadmium.core.messaging.MessageConverter;
 import com.meltmedia.cadmium.core.messaging.MessageReceiver;
 import com.meltmedia.cadmium.core.messaging.MessageSender;
 import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
 import com.meltmedia.cadmium.core.messaging.jgroups.JChannelProvider;
 import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;
 import com.meltmedia.cadmium.core.messaging.jgroups.MultiClassReceiver;
 import com.meltmedia.cadmium.core.meta.ConfigProcessor;
 import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;
 import com.meltmedia.cadmium.core.reflections.JBossVfsUrlType;
 import com.meltmedia.cadmium.core.worker.ConfigCoordinatedWorkerImpl;
 import com.meltmedia.cadmium.core.worker.CoordinatedWorkerImpl;
 import com.meltmedia.cadmium.servlets.ErrorPageFilter;
 import com.meltmedia.cadmium.servlets.FileServlet;
 import com.meltmedia.cadmium.servlets.MaintenanceFilter;
 import com.meltmedia.cadmium.servlets.RedirectFilter;
 import com.meltmedia.cadmium.servlets.SecureRedirectFilter;
 import com.meltmedia.cadmium.servlets.SecureRedirectStrategy;
 import com.meltmedia.cadmium.servlets.XForwardedSecureRedirectStrategy;
 
 /**
  * Builds the context with the Guice framework. To see how this works, go to:
  * http://code.google.com/p/google-guice/wiki/ServletModule
  * 
  * @author Christian Trimble
  */
 
 public class CadmiumListener extends GuiceServletContextListener {
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   public static final String CONFIG_PROPERTIES_FILE = "config.properties";
   public static final String BASE_PATH_ENV = "com.meltmedia.cadmium.contentRoot";
   public static final String SSH_PATH_ENV = "com.meltmedia.cadmium.github.sshKey";
   public static final String LAST_UPDATED_DIR = "com.meltmedia.cadmium.lastUpdated";
   public static final String JGROUPS_CHANNEL_CONFIG_URL = "com.meltmedia.cadmium.jgroups.channel.config";
   public static final String SSL_HEADER = "REQUEST_IS_SSL";
   public static final String LOG_DIR_INIT_PARAM = "log-directory";
   public static final String JBOSS_LOG_DIR = "jboss.server.log.dir";
   public File sharedContentRoot;
   public File applicationContentRoot;
   private String repoDir = "git-checkout";
   private String contentDir = "renderedContent";
   private File sshDir;
   private List<ChannelMember> members;
   private String warName;
   private String vHostName;
   private String channelConfigUrl;
   private ConfigManager configManager;
   private ScheduledThreadPoolExecutor executor;
   
   private String failOver;
   
   private ServletContext context;
 
   private Injector injector = null;
   
   @Override
   public void contextDestroyed(ServletContextEvent event) {
     Set<Closeable> closed = new HashSet<Closeable>();
     Injector injector = this.injector;
     while(injector != null) {
       for (Key<?> key : injector.getBindings().keySet()) {
         try {
           Object instance = injector.getInstance(key);
           
           if(instance instanceof Closeable) {
             try {
               Closeable toClose = (Closeable) instance;
               if(!closed.contains(toClose)) {
                 closed.add(toClose);
                 log.info("Closing instance of {}, key {}", instance.getClass().getName(), key);
                 IOUtils.closeQuietly(toClose);
               }
             } catch(Exception e){}
           }
         } catch(Throwable t) {}
       }
       injector = injector.getParent();
     }
     try {
       Reflections reflections = new Reflections("com.meltmedia");
       for(Class<? extends Closeable> toCloseClass : reflections.getSubTypesOf(Closeable.class)) {
         try {
           Closeable toClose = this.injector.getInstance(toCloseClass);
           if(!closed.contains(toClose)) {
             closed.add(toClose);
             log.info("Closing instance of {}", toCloseClass.getName());
             IOUtils.closeQuietly(toClose);
           }
         } catch(Throwable t) {}
       }
     } catch(Throwable t) {
       log.warn("Failed to close down fully.", t);
     }
     closed.clear();
     
     if( executor != null ) {
       try {
         executor.shutdown();
         if( !executor.awaitTermination(10, TimeUnit.SECONDS) ) {
           log.warn("Thread pool executor did not terminate after 10 seconds, forcing shutdown.");
           for( Runnable terminated : executor.shutdownNow() ) {
             log.warn("Terminated task of type {}.", terminated.getClass().getName());
           }
         }
       }
       catch( Throwable t ) {
         log.warn("Throwable thrown while terminating thread pool executor.", t);
       }
     }
     super.contextDestroyed(event);
   }
 
   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
    
     failOver = servletContextEvent.getServletContext().getRealPath("/");
     MaintenanceFilter.siteDown.start();
     context = servletContextEvent.getServletContext();
     
     configManager = new ConfigManager();
     
     Properties cadmiumProperties = configManager.getPropertiesByContext(context, "/WEB-INF/cadmium.properties");
     
     
     Properties configProperties = new Properties();
     configProperties = configManager.getSystemProperties();
     configProperties.putAll(cadmiumProperties);
     
     sharedContentRoot = sharedContextRoot(configProperties, context, log);
 
     // compute the directory for this application, based on the war name.
     warName = getWarName(context);
     
     vHostName = getVHostName(context);
     
     applicationContentRoot = applicationContentRoot(sharedContentRoot, warName, log);
     
     executor = new ScheduledThreadPoolExecutor(1);
     executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
     executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
     executor.setKeepAliveTime(5, TimeUnit.MINUTES);
     executor.setMaximumPoolSize(Math.min(Runtime.getRuntime().availableProcessors(), 4));
     
     configProperties = configManager.appendProperties(configProperties, new File(applicationContentRoot, CONFIG_PROPERTIES_FILE));
     
     configManager.setDefaultProperties(configProperties);
     
     try {
       configureLogback(servletContextEvent.getServletContext(), applicationContentRoot);
     } catch(IOException e) {
       log.error("Failed to reconfigure logging", e);
     }
     
 
     if ((sshDir = getSshDir(configProperties, sharedContentRoot )) != null) {
       GitService.setupSsh(sshDir.getAbsolutePath());
     }
     
 
 
     String repoDir = servletContextEvent.getServletContext().getInitParameter("repoDir");
     if (repoDir != null && repoDir.trim().length() > 0) {
       this.repoDir = repoDir;
     }
     String contentDir = servletContextEvent.getServletContext()
         .getInitParameter("contentDir");
     if (contentDir != null && contentDir.trim().length() > 0) {
       this.contentDir = contentDir;
     }
     if (configProperties.containsKey(LAST_UPDATED_DIR)) {
       File cntDir = new File(configProperties.getProperty(LAST_UPDATED_DIR));
       if (cntDir.exists() && cntDir.canRead()) {
         this.contentDir = cntDir.getName();
       }
     }
     File repoFile = new File(this.applicationContentRoot, this.repoDir);
     if (repoFile.isDirectory() && repoFile.canWrite()) {
       this.repoDir = repoFile.getAbsoluteFile().getAbsolutePath();
     } else {
       log.warn("The repo directory may not have been initialized yet.");
       this.repoDir = repoFile.getAbsoluteFile().getAbsolutePath();
     }
 
     File contentFile = new File(this.applicationContentRoot, this.contentDir);
     if (contentFile.exists() && contentFile.isDirectory()
         && contentFile.canWrite()) {
       this.contentDir = contentFile.getAbsoluteFile().getAbsolutePath();
     } else {
       log.warn("The content directory may not have been initialized yet.");
       this.contentDir = contentFile.getAbsoluteFile().getAbsolutePath();
     }
     
     String channelCfgUrl = configProperties.getProperty(JGROUPS_CHANNEL_CONFIG_URL);
     //String channelCfgUrl = System.getProperty(JGROUPS_CHANNEL_CONFIG_URL);
     if(channelCfgUrl != null) {
       File channelCfgFile = null;
       URL fileUrl = null;
       try {
         fileUrl = new URL(channelCfgUrl);
       } catch(Exception e) {
         channelCfgFile = new File(channelCfgUrl);
       }
       if(fileUrl == null && channelCfgFile != null) {
         if(!channelCfgFile.isAbsolute() && !channelCfgFile.exists()) {
           channelCfgFile = new File(this.sharedContentRoot, channelCfgUrl);
           if(channelCfgFile.exists()) {
             this.channelConfigUrl = "file://" + channelCfgFile.getAbsoluteFile().getAbsolutePath();
           }
         } else {
           this.channelConfigUrl = "file://" + channelCfgFile.getAbsoluteFile().getAbsolutePath();
         }
       } else if(fileUrl != null) {
         this.channelConfigUrl = fileUrl.toString();
       }
     }
     
 
     injector = Guice.createInjector(createServletModule(), createModule());
     super.contextInitialized(servletContextEvent);
     File graphFile = new File(applicationContentRoot, "injector.dot");
     graphGood(graphFile, injector);
   }
 
   @Override
   protected Injector getInjector() {
     return injector;
   }
 
   private ServletModule createServletModule() {
     return new ServletModule() {
       @Override
       protected void configureServlets() {
         Map<String, String> maintParams = new HashMap<String, String>();
         maintParams.put("ignorePrefix", "/system");
         
         Map<String, String> fileParams = new HashMap<String, String>();
         fileParams.put("basePath", FileSystemManager.exists(contentDir) ? contentDir : failOver);
         
         // hook Jackson into Jersey as the POJO <-> JSON mapper
         bind(JacksonJsonProvider.class).in(Scopes.SINGLETON);
         
         bind(SecureRedirectStrategy.class).to(XForwardedSecureRedirectStrategy.class).in(Scopes.SINGLETON);
 
         serve("/system/*").with(SystemGuiceContainer.class);
         serve("/api/*").with(ApiGuiceContainer.class);
         serve("/*").with(FileServlet.class, fileParams);
 
         filter("/*").through(ErrorPageFilter.class, maintParams);
         filter("/*").through(RedirectFilter.class);
         filter("/*").through(SecureRedirectFilter.class);
         
       }
     };
   }
   
   private Module createModule() {
     return new AbstractModule() {
       @SuppressWarnings("unchecked")
       @Override
       protected void configure() {
         Vfs.addDefaultURLTypes(new JBossVfsUrlType());
         
         Reflections reflections = new Reflections("com.meltmedia.cadmium");
         Properties configProperties = configManager.getDefaultProperties();
 
         bind(SiteDownService.class).toInstance(MaintenanceFilter.siteDown);
         
         bind(ScheduledExecutorService.class).toInstance(executor);
         bind(ExecutorService.class).toInstance(executor);
         bind(Executor.class).toInstance(executor);
 
         bind(FileServlet.class).in(Scopes.SINGLETON);
         bind(ContentService.class).to(FileServlet.class);
 
         bind(MessageConverter.class);
         bind(MessageSender.class).to(JGroupsMessageSender.class);
 
         bind(DelayedGitServiceInitializer.class).annotatedWith(ContentGitService.class).toInstance(new DelayedGitServiceInitializer());
         bind(DelayedGitServiceInitializer.class).annotatedWith(ConfigurationGitService.class).toInstance(new DelayedGitServiceInitializer());
 
         members = Collections.synchronizedList(new ArrayList<ChannelMember>());
         bind(new TypeLiteral<List<ChannelMember>>() {
         }).annotatedWith(Names.named("members")).toInstance(members);
         
         @SuppressWarnings("rawtypes")
         Multibinder<CommandAction<?>> commandActionBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<CommandAction<?>>(){});
         
         @SuppressWarnings("rawtypes")
         Set<Class<? extends CommandAction>> commandActionSet = 
             reflections.getSubTypesOf(CommandAction.class);
         log.debug("Found {} CommandAction classes.", commandActionSet.size());
         
         for( @SuppressWarnings("rawtypes") Class<? extends CommandAction> commandActionClass : commandActionSet ) {
           commandActionBinder.addBinding().to((Class<? extends CommandAction<?>>)commandActionClass);
         }
 
         bind(CommandResponse.class)
             .annotatedWith(Names.named(ProtocolMessage.HISTORY_RESPONSE))
             .to(HistoryResponseCommandAction.class).in(Scopes.SINGLETON);
 
         bind(new TypeLiteral<Map<String, CommandAction<?>>>() {}).annotatedWith(Names.named("commandMap")).toProvider(CommandMapProvider.class);
         bind(new TypeLiteral<Map<String, Class<?>>>(){}).annotatedWith(Names.named("commandBodyMap")).toProvider(CommandBodyMapProvider.class);
         
         bind(String.class).annotatedWith(Names.named("contentDir")).toInstance(contentDir);
         bind(String.class).annotatedWith(Names.named("sharedContentRoot")).toInstance(sharedContentRoot.getAbsolutePath());
         bind(String.class).annotatedWith(Names.named("warName")).toInstance(warName);
 
         String environment = configProperties.getProperty("com.meltmedia.cadmium.environment", "development");
         
         // Bind channel name
         bind(String.class).annotatedWith(Names.named(JChannelProvider.CHANNEL_NAME)).toInstance("CadmiumChannel-v2.0-"+vHostName+"-"+environment);
         
         bind(String.class).annotatedWith(Names.named("applicationContentRoot")).toInstance(applicationContentRoot.getAbsoluteFile().getAbsolutePath());
         
         bind(HistoryManager.class);
         
         //bind(Properties.class).annotatedWith(Names.named(CONFIG_PROPERTIES_FILE)).toInstance(configProperties);
         
         bind(ConfigManager.class).toInstance(configManager);
 
         // Bind Config file URL
         if(channelConfigUrl == null) {
           log.info("Using internal tcp.xml configuration file for JGroups.");
           URL propsUrl = JChannelProvider.class.getClassLoader().getResource("tcp.xml");
           bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(propsUrl);
         } else {
           try {
             log.info("Using {} configuration file for JGroups.", channelConfigUrl);
             bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(new URL(channelConfigUrl));
           } catch (MalformedURLException e) {
             log.error("Failed to setup jgroups with the file specified ["+channelConfigUrl+"]. Failing back to built in configuration!", e);
           }
         }
 
         // Bind JChannel provider
         bind(JChannel.class).toProvider(JChannelProvider.class).in(Scopes.SINGLETON);
 
         bind(MembershipListener.class).to(MembershipTracker.class);
         bind(MessageListener.class).to(MessageReceiver.class);
 
         bind(LifecycleService.class);
         bind(new TypeLiteral<CoordinatedWorker<ContentUpdateRequest>>(){}).annotatedWith(ContentWorker.class).to(CoordinatedWorkerImpl.class);
         bind(new TypeLiteral<CoordinatedWorker<ContentUpdateRequest>>(){}).annotatedWith(ConfigurationWorker.class).to(ConfigCoordinatedWorkerImpl.class);
         
         bind(SiteConfigProcessor.class);
         
         Multibinder<ConfigProcessor> configProcessorBinder = Multibinder.newSetBinder(binder(), ConfigProcessor.class);
         
         Set<Class<? extends ConfigProcessor>> configProcessorSet = 
             reflections.getSubTypesOf(ConfigProcessor.class);
         
         log.debug("Found {} ConfigProcessor classes.", configProcessorSet.size());
         
         for( Class<? extends ConfigProcessor> configProcessorClass : configProcessorSet ) {
           configProcessorBinder.addBinding().to(configProcessorClass);
           //bind(ConfigProcessor.class).to(configProcessorClass);
         }
 
         bind(Receiver.class).to(MultiClassReceiver.class).asEagerSingleton();
         
         Set<Class<?>> modules = reflections.getTypesAnnotatedWith(CadmiumModule.class);
         log.debug("Found {} Module classes.", modules.size());
         for(Class<?> module : modules) {
           if(Module.class.isAssignableFrom(module)) {	  	
                   log.debug("Installing module {}", module.getName());
        	  	try {
        	  	  install(((Class<? extends Module>)module).newInstance());
          	       } catch (InstantiationException e) {
        	           log.warn("Failed to instantiate "+module.getName(), e);
        	  	     } catch (IllegalAccessException e) {
         	  	     log.debug("Modules ["+module.getName()+"] constructor is not accessible.", e);
        	  	     }  	
               }	  	
             }
         
         // Bind Jersey Endpoints
         Set<Class<? extends Object>> jerseySet = 
             reflections.getTypesAnnotatedWith(Path.class);
         
         log.debug("Found {} jersey services with the Path annotation.", jerseySet.size());
         
         for( Class<? extends Object> jerseyService : jerseySet ) {
           log.debug("Binding jersey endpoint class {}", jerseyService.getName());
           bind(jerseyService).asEagerSingleton();
         }
         
       }
     };
   }
   
   /**
    * <p>Reconfigures the logging context.</p>
    * <p>The LoggerContext gets configured with "/WEB-INF/context-logback.xml". There is a <code>logDir</code> property set here which is expected to be the directory that the log file is written to.</p>
    * <p>The <code>logDir</code> property gets set on the LoggerContext with the following logic.</p>
    * <ul>
    *   <li>{@link LOG_DIR_INIT_PARAM} context parameter will be created and checked if it is writable.</li>
    *   <li>The File object passed in is used as a fall-back if it can be created and written to.</li>
    * </ul>    
    * @see {@link LoggerContext}
    * @param servletContext The current servlet context.
    * @param logDirFallback The fall-back directory to log to.
    * @throws FileNotFoundException Thrown if no logDir can be written to.
    * @throws MalformedURLException 
    * @throws IOException
    */
   public void configureLogback( ServletContext servletContext, File logDirFallback ) throws FileNotFoundException, MalformedURLException, IOException {
     log.debug("Reconfiguring Logback!");
     String systemLogDir = System.getProperty(JBOSS_LOG_DIR);
     if (systemLogDir != null) {
     	systemLogDir += "/" + getVHostName(servletContext);
     }
     File logDir = FileSystemManager.getWritableDirectoryWithFailovers(systemLogDir,servletContext.getInitParameter(LOG_DIR_INIT_PARAM), logDirFallback.getAbsolutePath());
     if(logDir != null) {
       log.debug("Resetting logback context.");
       URL configFile = servletContext.getResource("/WEB-INF/context-logback.xml");
       log.debug("Configuring logback with file, {}", configFile);
       LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
       try {
         JoranConfigurator configurator = new JoranConfigurator();
         configurator.setContext(context);
         context.stop();
         context.reset();  
         context.putProperty("logDir", logDir.getCanonicalPath());
         configurator.doConfigure(configFile);
       } catch (JoranException je) {
         // StatusPrinter will handle this
       } finally {
         context.start();
         log.debug("Done resetting logback.");
       }
       StatusPrinter.printInCaseOfErrorsOrWarnings(context);
     }
   }
   
   
   public static File sharedContextRoot( Properties configProperties, ServletContext context, Logger log ) {
     File sharedContentRoot = null;
     
     if (configProperties.containsKey(BASE_PATH_ENV)) {
       sharedContentRoot = new File(configProperties.getProperty(BASE_PATH_ENV));
       if (!sharedContentRoot.exists() || !sharedContentRoot.canRead() || !sharedContentRoot.canWrite()) {
         if (!sharedContentRoot.mkdirs()) {
           sharedContentRoot = null;
         }
       }
     }
     
     if (sharedContentRoot == null) {
       log.warn("Could not access cadmium content root.  Using the tempdir.");
       sharedContentRoot = (File) context.getAttribute("javax.servlet.context.tempdir");
     }
     return sharedContentRoot;
   }
   
   public String getVHostName( ServletContext context ) {
     String jbossWebXml = context.getRealPath("/WEB-INF/jboss-web.xml");
     try {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setNamespaceAware(true); // never forget this!
       DocumentBuilder builder = factory.newDocumentBuilder();
       Document doc = builder.parse(jbossWebXml);
       
       XPathFactory xpFactory = XPathFactory.newInstance();
       XPath xpath = xpFactory.newXPath();
       
       XPathExpression expr = xpath.compile("/jboss-web/virtual-host/text()");
       
       NodeList result = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
       
       if(result.getLength() > 0) {
         return result.item(0).getNodeValue();
       }
       
     } catch(Exception e) {
       log.warn("Failed to read/parse file.", e);
     }
     return getWarName(context);
   }
   
   public static String getWarName( ServletContext context ) {
     String[] pathSegments = context.getRealPath("/WEB-INF/web.xml").split("/");
     return pathSegments[pathSegments.length - 3];
   }
   
   public static File applicationContentRoot(File sharedContentRoot, String warName, Logger log) {
     File applicationContentRoot = new File(sharedContentRoot, warName);
     if (!applicationContentRoot.exists())
       applicationContentRoot.mkdir();
 
     log.info("Application content root:" + applicationContentRoot.getAbsolutePath());
     return applicationContentRoot;
 
   }
   
   public static File getSshDir(Properties configProperties, File sharedContentRoot ) {
     File sshDir = null;
     if (configProperties.containsKey(SSH_PATH_ENV)) {
       sshDir = new File(configProperties.getProperty(SSH_PATH_ENV));
       if (!sshDir.exists() && !sshDir.isDirectory()) {
         sshDir = null;
       }
     }
     if (sshDir == null) {
       sshDir = new File(sharedContentRoot, ".ssh");
       if (!sshDir.exists() && !sshDir.isDirectory()) {
         sshDir = null;
       }
     }
     return sshDir;
   }
   
   public final static Injector graphGood(File file, Injector inj) {
     try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos);
 
        Injector injector =
           Guice.createInjector(new GrapherModule(), new GraphvizModule());
        GraphvizRenderer renderer = 
           injector.getInstance(GraphvizRenderer.class);
        renderer.setOut(out).setRankdir("TB");
 
        injector.getInstance(InjectorGrapher.class).of(inj).graph();
 
        out = new PrintWriter(file, "UTF-8");
        String s = baos.toString("UTF-8");
        s = fixGrapherBug(s);
        s = hideClassPaths(s);
        out.write(s);
        out.close();
 
     } catch (FileNotFoundException e) {
        e.printStackTrace();
     } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
     } catch (IOException e) {
        e.printStackTrace();
     }
     return inj;
  }
 
  public static String hideClassPaths(String s) {
     s = s.replaceAll("\\w[a-z\\d_\\.]+\\.([A-Z][A-Za-z\\d_]*)", "");
     s = s.replaceAll("value=[\\w-]+", "random");
     return s;
  }
 
  public static String fixGrapherBug(String s) {
     s = s.replaceAll("style=invis", "style=solid");
     return s;
  }
 }
