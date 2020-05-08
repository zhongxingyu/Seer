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
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.Singleton;
 import com.google.inject.Stage;
 import com.google.inject.TypeLiteral;
 import com.google.inject.grapher.GrapherModule;
 import com.google.inject.grapher.InjectorGrapher;
 import com.google.inject.grapher.graphviz.GraphvizModule;
 import com.google.inject.grapher.graphviz.GraphvizRenderer;
 import com.google.inject.internal.InternalInjectorCreator;
 import com.google.inject.multibindings.Multibinder;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.meltmedia.cadmium.core.api.VHost;
 import com.meltmedia.cadmium.core.commands.CommandBodyMapProvider;
 import com.meltmedia.cadmium.core.commands.CommandMapProvider;
 import com.meltmedia.cadmium.core.commands.CommandResponse;
 import com.meltmedia.cadmium.core.commands.HistoryResponse;
 import com.meltmedia.cadmium.core.commands.HistoryResponseCommandAction;
 import com.meltmedia.cadmium.core.commands.LoggerConfigResponse;
 import com.meltmedia.cadmium.core.commands.LoggerConfigResponseCommandAction;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.git.DelayedGitServiceInitializer;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.history.HistoryManager;
 import com.meltmedia.cadmium.core.history.loggly.Api;
 import com.meltmedia.cadmium.core.history.loggly.EventQueue;
 import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
 import com.meltmedia.cadmium.core.messaging.ChannelMember;
 import com.meltmedia.cadmium.core.messaging.MembershipTracker;
 import com.meltmedia.cadmium.core.messaging.MessageConverter;
 import com.meltmedia.cadmium.core.messaging.MessageReceiver;
 import com.meltmedia.cadmium.core.messaging.MessageSender;
 import com.meltmedia.cadmium.core.messaging.MessagingChannelName;
 import com.meltmedia.cadmium.core.messaging.MessagingConfigurationUrl;
 import com.meltmedia.cadmium.core.messaging.jgroups.JChannelProvider;
 import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMembershipTracker;
 import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;
 import com.meltmedia.cadmium.core.messaging.jgroups.MultiClassReceiver;
 import com.meltmedia.cadmium.core.meta.ConfigProcessor;
 import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;
 import com.meltmedia.cadmium.core.reflections.JBossVfsUrlType;
 import com.meltmedia.cadmium.core.scheduler.SchedulerService;
 import com.meltmedia.cadmium.core.util.ContainerUtils;
 import com.meltmedia.cadmium.core.util.Jsr250Executor;
 import com.meltmedia.cadmium.core.util.Jsr250Utils;
 import com.meltmedia.cadmium.core.util.LogUtils;
 import com.meltmedia.cadmium.core.util.WarUtils;
 import com.meltmedia.cadmium.core.worker.ConfigCoordinatedWorkerImpl;
 import com.meltmedia.cadmium.core.worker.CoordinatedWorkerImpl;
 import com.meltmedia.cadmium.servlets.ApiEndpointAccessFilter;
 import com.meltmedia.cadmium.servlets.ErrorPageFilter;
 import com.meltmedia.cadmium.servlets.FileServlet;
 import com.meltmedia.cadmium.servlets.MaintenanceFilter;
 import com.meltmedia.cadmium.servlets.RedirectFilter;
 import com.meltmedia.cadmium.servlets.SecureRedirectFilter;
 import com.meltmedia.cadmium.servlets.SecureRedirectStrategy;
 import com.meltmedia.cadmium.servlets.XForwardedSecureRedirectStrategy;
 import com.meltmedia.cadmium.servlets.shiro.PersistablePropertiesRealm;
 import com.meltmedia.cadmium.servlets.shiro.WebEnvironment;
 import org.apache.commons.io.IOUtils;
 import org.apache.shiro.web.env.EnvironmentLoader;
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 import org.eclipse.jgit.util.StringUtils;
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
 
 import javax.servlet.Filter;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.ws.rs.Path;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
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
   private Jsr250Executor jsr250Executor = null;
   private boolean jboss = false;
   private boolean oldJBoss = false;
 
   private String failOver;
 
   private ServletContext context;
 
   private Injector injector = null;
 
   private Reflections reflections;
 
   @Override
   public void contextDestroyed(ServletContextEvent event) {
     Set<Class<? extends Closeable>> closed = new HashSet<Class<? extends Closeable>>();
     Injector injector = this.injector;
     if( jsr250Executor != null ) {
       jsr250Executor.preDestroy();
       jsr250Executor = null;
     }
     Set<Object> singletons = Jsr250Utils.findInstancesInScopes(injector, Singleton.class);
     Set<Object> otherSingletons = Jsr250Utils.findInstancesInScopes(injector, Scopes.SINGLETON);
     closeAll(closed, singletons);
     closeAll(closed, otherSingletons);
     closed.clear();
 
     if( executor != null ) {
       try {
         executor.shutdown();
       } catch (Throwable t){}
       try {
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
     injector = null;
     executor = null;
     members.clear();
     members = null;
     configManager = null;
     context = null;
     reflections = null;
 
     super.contextDestroyed(event);
   }
 
   private void closeAll(Set<Class<? extends Closeable>> closed, Set<Object> singletons) {
     for(Object singleton : singletons) {
       if(singleton instanceof Closeable) {
         Closeable toClose = (Closeable) singleton;
         if(!closed.contains(toClose.getClass())) {
           closed.add(toClose.getClass());
           log.info("Closing instance of {}", toClose.getClass().getName());
           IOUtils.closeQuietly(toClose);
         }
       }
     }
   }
 
   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
     //Force the use of slf4j logger in all JBoss log uses in this wars context!!!
     jboss = ContainerUtils.isJBoss();
     if(jboss) {
       oldJBoss = ContainerUtils.isOldJBoss();
     }
     if(oldJBoss) {
       System.setProperty("org.jboss.logging.provider", "slf4j");
     }
 
     failOver = servletContextEvent.getServletContext().getRealPath("/");
     MaintenanceFilter.siteDown.start();
     context = servletContextEvent.getServletContext();
 
     configManager = new ConfigManager(context);
 
     Properties cadmiumProperties = configManager.getPropertiesByContext(context, "/WEB-INF/cadmium.properties");
 
     Properties configProperties = new Properties();
     configProperties = configManager.getSystemProperties();
     configProperties.putAll(cadmiumProperties);
 
     sharedContentRoot = sharedContextRoot(configProperties, context, log);
 
     // compute the directory for this application, based on the war name.
     warName = WarUtils.getWarName(context);
 
     vHostName = getVHostName(context);
 
     applicationContentRoot = applicationContentRoot(sharedContentRoot, warName, log);
 
     executor = new ScheduledThreadPoolExecutor(1);
     executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
     executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
     executor.setKeepAliveTime(5, TimeUnit.MINUTES);
     executor.setMaximumPoolSize(Math.min(Runtime.getRuntime().availableProcessors(), 4));
 
     configProperties = configManager.appendProperties(configProperties, new File(applicationContentRoot, CONFIG_PROPERTIES_FILE));
 
     configManager.setDefaultProperties(configProperties);
     configManager.setDefaultPropertiesFile(new File(applicationContentRoot, CONFIG_PROPERTIES_FILE));
 
     try {
       LogUtils.configureLogback(servletContextEvent.getServletContext(), applicationContentRoot, getVHostName(servletContextEvent.getServletContext()), log);
     } catch(IOException e) {
       log.error("Failed to reconfigure logging", e);
     }
 
     if ((sshDir = getSshDir(configProperties, sharedContentRoot, log )) != null) {
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
     reflections = Reflections.collect();
     Module modules[] = new Module[] {createServletModule(), createModule()};
     InternalInjectorCreator guiceCreator = new InternalInjectorCreator()
       .stage(Stage.DEVELOPMENT)
       .addModules(Arrays.asList(modules));
     try {
       injector = guiceCreator.build();
       
       //injector = Guice.createInjector(createServletModule(), createModule());
       
       // run the postConstruct methods.
       jsr250Executor = Jsr250Utils.createJsr250Executor(injector, log, Scopes.SINGLETON);
       jsr250Executor.postConstruct();
       
       super.contextInitialized(servletContextEvent);
       File graphFile = new File(applicationContentRoot, "injector.dot");
       graphGood(graphFile, injector);
     } catch(Throwable t) {
       try {
         log.error("Failed to initialize...", t);
         Method primaryInjector = InternalInjectorCreator.class.getDeclaredMethod("primaryInjector");
         primaryInjector.setAccessible(true);
         injector = (Injector) primaryInjector.invoke(guiceCreator);
         if(injector == null) {
           log.error("Injector must not have been created.");
         } else {
           log.error("Found injector {}", injector);
         }
       } catch (Throwable e) {
         log.error("Failed to retrieve injector that failed to initialize.", e);
       }
       throw new RuntimeException("Failed to Initialize", t);
     }
   }
 
   @Override
   protected Injector getInjector() {
     return injector;
   }
 
   private ServletModule createServletModule() {
     return new ServletModule() {
       @Override
       protected void configureServlets() {
        Vfs.addDefaultURLTypes(new JBossVfsUrlType());
        /*Reflections reflections = Reflections.collect();/*new Reflections("com.meltmedia.cadmium",
            new TypeAnnotationsScanner());*/
         Map<String, String> maintParams = new HashMap<String, String>();
         maintParams.put("ignorePrefix", "/system");
         Map<String, String> aclParams = new HashMap<String, String>();
         maintParams.put("jersey-prefix", "/api/");
 
         Map<String, String> fileParams = new HashMap<String, String>();
         fileParams.put("basePath", com.meltmedia.cadmium.core.FileSystemManager.exists(contentDir) ? contentDir : failOver);
 
         // hook Jackson into Jersey as the POJO <-> JSON mapper
         bind(JacksonJsonProvider.class).in(Scopes.SINGLETON);
 
         bind(SecureRedirectStrategy.class).to(XForwardedSecureRedirectStrategy.class).in(Scopes.SINGLETON);
 
         serve("/system/*").with(SystemGuiceContainer.class);
         serve("/api/*").with(ApiGuiceContainer.class);
         serve("/*").with(FileServlet.class, fileParams);
 
         filter("/*").through(ErrorPageFilter.class, maintParams);
         filter("/*").through(RedirectFilter.class);
         filter("/*").through(SecureRedirectFilter.class);
         filter("/api/*").through(ApiEndpointAccessFilter.class, aclParams);
 
         try {
           Set<Class<?>> discoveredFilters = reflections.getTypesAnnotatedWith(com.meltmedia.cadmium.core.CadmiumFilter.class);
           if(discoveredFilters != null) {
             for(Class<?> filterClass : discoveredFilters) {
               if( Filter.class.isAssignableFrom(filterClass)) {
                 com.meltmedia.cadmium.core.CadmiumFilter annot = filterClass.getAnnotation(com.meltmedia.cadmium.core.CadmiumFilter.class);
                 if(!StringUtils.isEmptyOrNull(annot.value())){
                   log.debug("Adding Filter {} mapped with {}", filterClass.getName(), annot.value());
                   filter(annot.value()).through(filterClass.asSubclass(Filter.class));
                 }
               }
             }
           }
         } catch(Exception e){
           log.error("Failed to find servlet Filter implementations.", e);
         }
       }
     };
   }
 
   private Module createModule() {
     return new AbstractModule() {
       @SuppressWarnings("unchecked")
       @Override
       protected void configure() {
        Vfs.addDefaultURLTypes(new JBossVfsUrlType());
        /*Reflections reflections = Reflections.collect();/*new Reflections("com.meltmedia.cadmium",
            new TypeAnnotationsScanner(), 
            new SubTypesScanner(),
            new MethodAnnotationsScanner());*/
         Properties configProperties = configManager.getDefaultProperties();
         
         org.apache.shiro.web.env.WebEnvironment shiroEnv = (org.apache.shiro.web.env.WebEnvironment) context.getAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);
         if(shiroEnv != null && shiroEnv instanceof WebEnvironment) {
           WebEnvironment cadmiumShiroEnv = (WebEnvironment) shiroEnv;
           if(cadmiumShiroEnv.getPersistablePropertiesRealm() != null) {
             log.debug("Binding shiro configurable realm: "+ PersistablePropertiesRealm.class);
             bind(PersistablePropertiesRealm.class).toInstance(cadmiumShiroEnv.getPersistablePropertiesRealm());
             cadmiumShiroEnv.getPersistablePropertiesRealm().loadProperties(applicationContentRoot.getAbsoluteFile());
           }
         }
 
         bind(Boolean.class).annotatedWith(com.meltmedia.cadmium.core.ISJBoss.class).toInstance(jboss);
         bind(Boolean.class).annotatedWith(com.meltmedia.cadmium.core.ISOLDJBoss.class).toInstance(oldJBoss);
 
         bind(com.meltmedia.cadmium.core.SiteDownService.class).toInstance(MaintenanceFilter.siteDown);
         bind(com.meltmedia.cadmium.core.ApiEndpointAccessController.class).toInstance(ApiEndpointAccessFilter.controller);
 
         bind(ScheduledExecutorService.class).toInstance(executor);
         bind(ExecutorService.class).toInstance(executor);
         bind(Executor.class).toInstance(executor);
 
         bind(FileServlet.class).in(Scopes.SINGLETON);
         bind(com.meltmedia.cadmium.core.ContentService.class).to(FileServlet.class);
 
         bind(MessageConverter.class);
         bind(MessageSender.class).to(JGroupsMessageSender.class);
 
         bind(DelayedGitServiceInitializer.class).annotatedWith(com.meltmedia.cadmium.core.ContentGitService.class).toInstance(new DelayedGitServiceInitializer());
         bind(DelayedGitServiceInitializer.class).annotatedWith(com.meltmedia.cadmium.core.ConfigurationGitService.class).toInstance(new DelayedGitServiceInitializer());
 
         members = Collections.synchronizedList(new ArrayList<ChannelMember>());
         bind(new TypeLiteral<List<ChannelMember>>() {
         }).annotatedWith(com.meltmedia.cadmium.core.ClusterMembers.class).toInstance(members);
         Multibinder<com.meltmedia.cadmium.core.CommandAction<?>> commandActionBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<com.meltmedia.cadmium.core.CommandAction<?>>(){});
 
         @SuppressWarnings("rawtypes")
         Set<Class<? extends com.meltmedia.cadmium.core.CommandAction>> commandActionSet =
         reflections.getSubTypesOf(com.meltmedia.cadmium.core.CommandAction.class);
         log.debug("Found {} CommandAction classes.", commandActionSet.size());
 
         for( @SuppressWarnings("rawtypes") Class<? extends com.meltmedia.cadmium.core.CommandAction> commandActionClass : commandActionSet ) {
           commandActionBinder.addBinding().to((Class<? extends com.meltmedia.cadmium.core.CommandAction<?>>)commandActionClass);
         }
 
         bind(new TypeLiteral<CommandResponse<HistoryResponse>>(){}).to(HistoryResponseCommandAction.class).in(Scopes.SINGLETON);
         bind(new TypeLiteral<CommandResponse<LoggerConfigResponse>>(){}).to(LoggerConfigResponseCommandAction.class).in(Scopes.SINGLETON);
 
         bind(new TypeLiteral<Map<String, com.meltmedia.cadmium.core.CommandAction<?>>>() {}).annotatedWith(com.meltmedia.cadmium.core.CommandMap.class).toProvider(CommandMapProvider.class);
         bind(new TypeLiteral<Map<String, Class<?>>>(){}).annotatedWith(com.meltmedia.cadmium.core.CommandBodyMap.class).toProvider(CommandBodyMapProvider.class);
 
         bind(String.class).annotatedWith(com.meltmedia.cadmium.core.ContentDirectory.class).toInstance(contentDir);
         bind(String.class).annotatedWith(com.meltmedia.cadmium.core.SharedContentRoot.class).toInstance(sharedContentRoot.getAbsolutePath());
         bind(String.class).annotatedWith(com.meltmedia.cadmium.core.CurrentWarName.class).toInstance(warName);
 
         String environment = configProperties.getProperty("com.meltmedia.cadmium.environment", "development");
 
         // Bind channel name
         bind(String.class).annotatedWith(MessagingChannelName.class).toInstance("CadmiumChannel-v2.0-"+vHostName+"-"+environment);
         bind(String.class).annotatedWith(VHost.class).toInstance(vHostName);
 
         bind(String.class).annotatedWith(com.meltmedia.cadmium.core.ApplicationContentRoot.class).toInstance(applicationContentRoot.getAbsoluteFile().getAbsolutePath());
 
         bind(HistoryManager.class);
 
         bind(ConfigManager.class).toInstance(configManager);
 
         // Bind Config file URL
         if(channelConfigUrl == null) {
           log.info("Using internal tcp.xml configuration file for JGroups.");
           URL propsUrl = JChannelProvider.class.getClassLoader().getResource("tcp.xml");
           bind(URL.class).annotatedWith(MessagingConfigurationUrl.class).toInstance(propsUrl);
         } else {
           try {
             log.info("Using {} configuration file for JGroups.", channelConfigUrl);
             bind(URL.class).annotatedWith(MessagingConfigurationUrl.class).toInstance(new URL(channelConfigUrl));
           } catch (MalformedURLException e) {
             log.error("Failed to setup jgroups with the file specified ["+channelConfigUrl+"]. Failing back to built in configuration!", e);
           }
         }
 
         // Bind JChannel provider
         bind(JChannel.class).toProvider(JChannelProvider.class).in(Scopes.SINGLETON);
 
         bind(MembershipListener.class).to(JGroupsMembershipTracker.class);
         bind(MembershipTracker.class).to(JGroupsMembershipTracker.class);
         bind(MessageListener.class).to(MessageReceiver.class);
 
         bind(LifecycleService.class);
         bind(new TypeLiteral<com.meltmedia.cadmium.core.CoordinatedWorker<com.meltmedia.cadmium.core.commands.ContentUpdateRequest>>(){}).annotatedWith(com.meltmedia.cadmium.core.ContentWorker.class).to(CoordinatedWorkerImpl.class);
         bind(new TypeLiteral<com.meltmedia.cadmium.core.CoordinatedWorker<com.meltmedia.cadmium.core.commands.ContentUpdateRequest>>(){}).annotatedWith(com.meltmedia.cadmium.core.ConfigurationWorker.class).to(ConfigCoordinatedWorkerImpl.class);
 
         bind(SiteConfigProcessor.class);
         bind(Api.class);
         bind(EventQueue.class);
 
         Multibinder<ConfigProcessor> configProcessorBinder = Multibinder.newSetBinder(binder(), ConfigProcessor.class);
 
         Set<Class<? extends ConfigProcessor>> configProcessorSet = 
             reflections.getSubTypesOf(ConfigProcessor.class);
 
         log.debug("Found {} ConfigProcessor classes.", configProcessorSet.size());
 
         for( Class<? extends ConfigProcessor> configProcessorClass : configProcessorSet ) {
           configProcessorBinder.addBinding().to(configProcessorClass);
           //bind(ConfigProcessor.class).to(configProcessorClass);
         }
 
         bind(Receiver.class).to(MultiClassReceiver.class).asEagerSingleton();
 
         Set<Class<?>> modules = reflections.getTypesAnnotatedWith(com.meltmedia.cadmium.core.CadmiumModule.class);
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
 
         //Discover configuration classes.
         install(new ConfigurationModule(reflections));
 
         // Bind Jersey Endpoints
         Set<Class<? extends Object>> jerseySet = 
             reflections.getTypesAnnotatedWith(Path.class);
 
         log.debug("Found {} jersey services with the Path annotation.", jerseySet.size());
 
         for( Class<? extends Object> jerseyService : jerseySet ) {
           log.debug("Binding jersey endpoint class {}", jerseyService.getName());
           bind(jerseyService).asEagerSingleton();
         }
         
         SchedulerService.bindScheduled(binder(), reflections);
         bind(SchedulerService.class);
       }
     };
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
       configProperties.setProperty(BASE_PATH_ENV, sharedContentRoot.getAbsoluteFile().getAbsolutePath());
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
     return WarUtils.getWarName(context);
   }
 
   public static File applicationContentRoot(File sharedContentRoot, String warName, Logger log) {
     File applicationContentRoot = new File(sharedContentRoot, warName);
     if (!applicationContentRoot.exists())
       applicationContentRoot.mkdir();
 
     log.info("Application content root:" + applicationContentRoot.getAbsolutePath());
     return applicationContentRoot;
 
   }
 
   public static File getSshDir(Properties configProperties, File sharedContentRoot, Logger log ) {
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
     log.debug("Using ssh dir {}", sshDir);
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
