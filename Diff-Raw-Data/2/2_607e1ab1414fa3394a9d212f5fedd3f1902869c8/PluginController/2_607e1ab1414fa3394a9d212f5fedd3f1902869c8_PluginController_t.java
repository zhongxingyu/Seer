 package de.ronnyfriedland.time.logic;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 
 import de.ronnyfriedland.time.logic.plugin.Plugin;
 
 /**
  * Controller für die Verwaltung und das Initialisieren von Plugins.
  * 
  * @author Ronny Friedland
  */
 public final class PluginController {
 
     private static final Logger LOG = Logger.getLogger(PluginController.class.getName());
 
     private static PluginController instance;
 
     /**
      * Liefert eine Instanz von {@link PluginController}.
      * 
      * @return the {@link PluginController}
      */
     public static PluginController getInstance() {
         synchronized (PluginController.class) {
             if (null == instance) {
                 instance = new PluginController();
             }
         }
         return instance;
     }
 
     private PluginController() {
         // empty
     }
 
     /**
      * Ausführen der verfügbaren Plugins.
      */
     public void executePlugins() {
         if (LOG.isLoggable(Level.INFO)) {
             LOG.info("Retrieving plugins ...");
         }
         // configure classpath entries
         Set<URL> urls = new HashSet<URL>();
        urls.addAll(ClasspathHelper.forJavaClassPath());
         try {
             Properties availablePlugins = new Properties();
             availablePlugins.load(Thread.currentThread().getContextClassLoader()
                     .getResourceAsStream("plugin.properties"));
             String files = availablePlugins.getProperty("files");
             if (null != files && files.length() > 2) {
                 for (String file : files.split(";")) {
                     urls.add(Thread.currentThread().getContextClassLoader().getResource(file));
                 }
             }
         } catch (Exception e) {
             LOG.warning("There are problems starting plugin. Please check configuration.");
         }
 
         Reflections reflections = new Reflections(new ConfigurationBuilder()
                 .addClassLoader(URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]))).addUrls(urls)
                 .setScanners(new TypeAnnotationsScanner())
                 .setExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())));
 
         Set<Class<?>> plugins = reflections.getTypesAnnotatedWith(Plugin.class);
         if (LOG.isLoggable(Level.INFO)) {
             LOG.info(String.format("Number of plugins found: %d", plugins.size()));
         }
         for (final Class<?> plugin : plugins) {
             new Thread(new Runnable() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see java.lang.Runnable#run()
                  */
                 @Override
                 public void run() {
                     try {
                         if (LOG.isLoggable(Level.FINE)) {
                             LOG.fine("Initialize plugin: " + plugin);
                         }
                         plugin.newInstance();
                     } catch (Exception e) {
                         LOG.log(Level.SEVERE, "Error starting plugin: " + plugin, e);
                     }
                 }
             }).start();
         }
     }
 
 }
