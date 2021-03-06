 package hudson;
 
 import hudson.model.Hudson;
 import hudson.util.Service;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Manages {@link PluginWrapper}s.
  *
  * @author Kohsuke Kawaguchi
  */
 public final class PluginManager {
     /**
      * All discovered plugins.
      */
     private final List<PluginWrapper> plugins = new ArrayList<PluginWrapper>();
 
     /**
      * All active plugins.
      */
     private final List<PluginWrapper> activePlugins = new ArrayList<PluginWrapper>();
 
     private final List<FailedPlugin> failedPlugins = new ArrayList<FailedPlugin>();
 
     /**
      * Plug-in root directory.
      */
     public final File rootDir;
 
     public final ServletContext context;
 
     /**
      * {@link ClassLoader} that can load all the publicly visible classes from plugins
      * (and including the classloader that loads Hudson itself.)
      *
      */
     // implementation is minimal --- just enough to run XStream
     // and load plugin-contributed classes.
     public final ClassLoader uberClassLoader = new UberClassLoader();
 
     public PluginManager(ServletContext context) {
         this.context = context;
         rootDir = new File(Hudson.getInstance().getRootDir(),"plugins");
         if(!rootDir.exists())
             rootDir.mkdirs();
 
         File[] archives = rootDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".hpi")        // plugin jar file
                     || name.endsWith(".hpl");       // linked plugin. for debugging.
             }
         });
 
         if(archives==null) {
             LOGGER.severe("Hudson is unable to create "+rootDir+"\nPerhaps its security privilege is insufficient");
             return;
         }
         for( File arc : archives ) {
             try {
                PluginWrapper p = new PluginWrapper(this, arc);
                 plugins.add(p);
                 if(p.isActive())
                     activePlugins.add(p);
             } catch (IOException e) {
                 failedPlugins.add(new FailedPlugin(arc.getName(),e));
                 LOGGER.log(Level.SEVERE, "Failed to load a plug-in " + arc, e);
             }
         }
 
         for (PluginWrapper p : activePlugins.toArray(new PluginWrapper[0]))
             try {
                 p.load(this);
             } catch (IOException e) {
                 failedPlugins.add(new FailedPlugin(p.getShortName(),e));
                 LOGGER.log(Level.SEVERE, "Failed to load a plug-in " + p.getShortName(), e);
                 activePlugins.remove(p);
                 plugins.remove(p);
             }
     }
 
     public List<PluginWrapper> getPlugins() {
         return plugins;
     }
 
     public List<FailedPlugin> getFailedPlugins() {
         return failedPlugins;
     }
 
     public PluginWrapper getPlugin(String shortName) {
         for (PluginWrapper p : plugins) {
             if(p.getShortName().equals(shortName))
                 return p;
         }
         return null;
     }
 
     /**
      * Discover all the service provider implementations of the given class,
      * via <tt>META-INF/services</tt>.
      */
     public <T> Collection<Class<? extends T>> discover( Class<T> spi ) {
         Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
 
         for (PluginWrapper p : activePlugins) {
             Service.load(spi, p.classLoader, result);
         }
 
         return result;
     }
 
     /**
      * Orderly terminates all the plugins.
      */
     public void stop() {
         for (PluginWrapper p : activePlugins) {
             p.stop();
         }
     }
 
     private final class UberClassLoader extends ClassLoader {
         public UberClassLoader() {
             super(PluginManager.class.getClassLoader());
         }
 
         @Override
         protected Class<?> findClass(String name) throws ClassNotFoundException {
             // first, use the context classloader so that plugins that are loading
             // can use its own classloader first.
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             if(cl!=null)
                 try {
                     return cl.loadClass(name);
                 } catch(ClassNotFoundException e) {
                     // not found. try next
                 }
 
             for (PluginWrapper p : activePlugins) {
                 try {
                     return p.classLoader.loadClass(name);
                 } catch (ClassNotFoundException e) {
                     //not found. try next
                 }
             }
             // not found in any of the classloader. delegate.
             throw new ClassNotFoundException(name);
         }
 
         @Override
         protected URL findResource(String name) {
             for (PluginWrapper p : activePlugins) {
                 URL url = p.classLoader.getResource(name);
                 if(url!=null)
                     return url;
             }
             return null;
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
 
     /**
      * Remembers why a plugin failed to deploy.
      */
     public static final class FailedPlugin {
         public final String name;
         public final IOException cause;
 
         public FailedPlugin(String name, IOException cause) {
             this.name = name;
             this.cause = cause;
         }
 
         public String getExceptionString() {
             StringWriter sw = new StringWriter();
             cause.printStackTrace(new PrintWriter(sw));
             return sw.toString();
         }
     }
 }
