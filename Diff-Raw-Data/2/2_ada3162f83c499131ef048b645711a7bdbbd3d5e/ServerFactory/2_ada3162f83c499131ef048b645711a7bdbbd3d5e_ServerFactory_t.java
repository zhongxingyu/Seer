 package org.eluder.jetty.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jetty.annotations.AnnotationConfiguration;
 import org.eclipse.jetty.io.RuntimeIOException;
 import org.eclipse.jetty.plus.webapp.EnvConfiguration;
 import org.eclipse.jetty.plus.webapp.PlusConfiguration;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.HttpConfiguration;
 import org.eclipse.jetty.server.HttpConnectionFactory;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.ServerConnector;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.resource.Resource;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.eclipse.jetty.util.thread.ThreadPool;
 import org.eclipse.jetty.webapp.Configuration.ClassList;
 import org.eclipse.jetty.webapp.FragmentConfiguration;
 import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
 import org.eclipse.jetty.webapp.MetaInfConfiguration;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.eclipse.jetty.webapp.WebInfConfiguration;
 import org.eclipse.jetty.webapp.WebXmlConfiguration;
 import org.eluder.jetty.server.configuration.ClassPathConfiguration;
 import org.eluder.jetty.server.configuration.JarAppConfiguration;
 
 public class ServerFactory {
 
     private static final String CLASSPATH_PREFIX = "classpath:";
     
     protected final ServerConfig config;
 
     public ServerFactory(final ServerConfig config) {
         this.config = config;
     }
     
     protected ThreadPool createThreadPool() {
         QueuedThreadPool threadPool = new QueuedThreadPool();
         threadPool.setMinThreads(config.getMinThreads());
         threadPool.setMaxThreads(config.getMaxThreads());
         return threadPool;
     }
     
     protected ClassList createClassList() {
         ClassList classList = new ClassList(new String[0]);
         if (config.isJarApp()) {
             classList.add(JarAppConfiguration.class.getName());
         } else if (config.isClassPath()) {
             classList.add(ClassPathConfiguration.class.getName());
         } else {
             classList.add(WebInfConfiguration.class.getName());
             classList.add(WebXmlConfiguration.class.getName());
         }
         classList.add(AnnotationConfiguration.class.getName());
         classList.add(MetaInfConfiguration.class.getName());
         classList.add(FragmentConfiguration.class.getName());
         if (config.isPlus()) {
             classList.add(EnvConfiguration.class.getName());
             classList.add(PlusConfiguration.class.getName());
         }
         classList.add(JettyWebXmlConfiguration.class.getName());
         return classList;
     }
     
     protected HttpConfiguration createHttpConfiguration() {
         return new HttpConfiguration();
     }
     
     protected Connector createConnector(final Server server) {
         ServerConnector connector = new ServerConnector(
                 server, new HttpConnectionFactory(createHttpConfiguration()));
         connector.setPort(config.getPort());
         connector.setIdleTimeout(config.getIdleTimeout());
         connector.setSoLingerTime(config.getSoLingerTime());
         return connector;
     }
     
     protected List<Handler> createContexts() {
         WebAppContext context = new WebAppContext();
         context.setContextPath(config.getContextPath());
         context.setWar(config.getWebApp());
         context.setBaseResource(getBaseResource(config.getBaseResource()));
 
         if (config.isDefaultServlet()) {
             context.addServlet(createDefaultServlet(), "/");
         }
 
         List<Handler> handlers = new ArrayList<>(1);
         handlers.add(context);
         return handlers;
     }
     
     protected ServletHolder createDefaultServlet() {
         ServletHolder sh = new ServletHolder("default", DefaultServlet.class);
         sh.setInitOrder(0);
         return sh;
     }
     
     protected ContextHandlerCollection createContextHandlerCollection() {
         List<Handler> contexts = createContexts();
         ContextHandlerCollection handlers = new ContextHandlerCollection();
         handlers.setHandlers(contexts.toArray(new Handler[contexts.size()]));
         return handlers;
     }
     
     public Server create() {
         Server server = new Server(createThreadPool());
         server.addBean(createClassList());
         server.setConnectors(new Connector[] {
                 createConnector(server)
         });
         server.setHandler(createContextHandlerCollection());
         return server;
     }
     
    protected final Resource getBaseResource(final String resource) {
         if (resource == null) {
             return null;
         }
         try {
             if (resource.startsWith(CLASSPATH_PREFIX)) {
                 return Resource.newClassPathResource(resource.substring(CLASSPATH_PREFIX.length()));
             } else {
                 return Resource.newResource(resource);
             }
         } catch (IOException ex) {
             throw new RuntimeIOException(ex);
         }
     }
 }
