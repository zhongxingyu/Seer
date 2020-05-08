 
 package fermanext.system;
 
 import java.util.*;
 import java.util.jar.*;
 import java.net.*;
 import java.io.*;
 import java.lang.reflect.*;
 import fermanext.logging.log4cxx.*;
 
 class PluginLoadException extends Exception {}
 
 /**
  * Class for verifying and loading FermaNext Java plugins
  * from specified path. Every Java plugin should be a JAR 
  * file (even this plugin is only a single class), then 
  * JAR should contain Manifest file with "Main-Class:" 
  * signature (`main` method of this class can be skipped).
  * 
  * TODO: add further description.
  */
 public class JavaPluginLoader
 {
     /** Loader logger */
     private static Logger logger = 
         Logger.getLogger("java.loader.JavaPluginLoader");
 
     /** Class loaders and loaded Java plugins */
     private Map<String, URLClassLoader> classLoaders = 
         new HashMap<String, URLClassLoader>();
     private Map<String, JavaPlugin> javaPlugins = 
         new HashMap<String, JavaPlugin>();
 
     public JavaPlugin loadPlugin ( String jarPath )
         throws PluginLoadException
     {
         logger.debug( "loadPlugin: " + jarPath );
 
         File file = new File( jarPath );
 
         // Change path to absolute
         // TODO: do we need this conversion? 
         // jarPath = file.getAbsolutePath();
 
         if ( javaPlugins.containsKey(jarPath) )
             return javaPlugins.get(jarPath);
 
         String javaPluginClassName = null;
         try { 
             JarFile jarFile = new JarFile( file );
             Manifest man = jarFile.getManifest();
             // Jar file should contain Manifest
             if ( man == null ) {
                 logger.warn( "Can't find Manifest file in " + jarPath );
                 throw new PluginLoadException();
             }
             // Manifest should contains 'Main-Class' attribute
             Attributes attrs = man.getMainAttributes();
             if ( !attrs.containsKey( Attributes.Name.MAIN_CLASS ) ) {
                 logger.warn( "Can't find 'Main-Class' attribute in Manifest" );
                 throw new PluginLoadException();
             }            
             javaPluginClassName = attrs.getValue( Attributes.Name.MAIN_CLASS );
         } catch ( IOException excp ) {
             logger.warn( "Can't open JAR file" + jarPath );
             throw new PluginLoadException();
         }
 
         try {
             URL url = null;
             // Create correct url
             if ( !jarPath.startsWith("file:///") )
                 url = new URL("file:///" + jarPath);
             else
                 url = new URL(jarPath);
 
             URLClassLoader loader = new URLClassLoader( new URL[]{ url } );
             Class<fermanext.system.JavaPlugin> javaPluginClass = 
                 fermanext.system.JavaPlugin.class;
             Class loadedClass = loader.loadClass( javaPluginClassName );
             // Loaded class should extends basic Java plugin class
             if ( !javaPluginClass.isAssignableFrom(loadedClass) ) {
                 logger.warn( "Class " + javaPluginClassName  + 
                              " is not a child of JavaPlugin: " + jarPath );
                 throw new PluginLoadException();
             }
             // Find constructor of this Java plugin
             Constructor javaPluginConstr = 
                 loadedClass.getConstructor( String.class );
             // Create an instance of this Java plugin
             JavaPlugin javaPluginInst = 
                 (JavaPlugin)javaPluginConstr.newInstance( jarPath );
 
             // Correct save
             classLoaders.put( jarPath, loader );
             javaPlugins.put( jarPath, javaPluginInst );
             return javaPluginInst;
             
         } catch ( MalformedURLException excp ) {
             logger.warn( "Can't find Jar file, URL is wrong: " + jarPath );
             throw new PluginLoadException();
         }
         catch ( ClassNotFoundException excp ) {
             logger.warn( "Can't find Main-Class '" + javaPluginClassName +  
                          "' in " + jarPath );
             throw new PluginLoadException();
         }
         catch ( InstantiationException excp ) {
             logger.warn( "Can't find Main-Class '" + javaPluginClassName +  
                          "' in " + jarPath );
             throw new PluginLoadException();
         }
         catch ( IllegalAccessException excp ) {
             logger.warn( "Main-Class '" + javaPluginClassName +  
                          "' in " + jarPath + " is abstract" );
             throw new PluginLoadException();
         }
         catch ( NoSuchMethodException excp ) {
             logger.warn( "Can't find constructor of Main-Class '" + 
                          javaPluginClassName + "' in " + jarPath );
             throw new PluginLoadException();
         }
         catch ( InvocationTargetException excp ) {
             logger.warn( "Constructor of Main-Class '" + 
                          javaPluginClassName + "' in " + jarPath + 
                          " throws an exception" );
             throw new PluginLoadException();
         }
     }
 
     public void unloadPlugin ( String jarPath )
     {
         logger.debug( "unloadPlugin(String): " + jarPath );
         if ( !javaPlugins.containsKey(jarPath) ) {
            logger.info( "Can't find " + jarPath + " to unload" );
             return;
         }
         classLoaders.remove(jarPath);
         javaPlugins.remove(jarPath);
     }
 
     public void unloadPlugin ( JavaPlugin plugin )
     {
        String jarPath = pathToPlugin( plugin );
         logger.debug( "unloadPlugin(JavaPlugin): " + jarPath );
         if ( jarPath.length() == 0 ) {
            logger.info( "Can't find " + jarPath + " to unload" );
             return;
         }
         classLoaders.remove(jarPath);
         javaPlugins.remove(jarPath);
     }
 
     public void unloadPlugins ()
     {
         logger.debug( "unloadPlugins" );
         classLoaders.clear();
         javaPlugins.clear();
     }
 
     public Vector<JavaPlugin> loadedPlugins () 
     {
         Vector<JavaPlugin> plugins = new Vector<JavaPlugin>();
         Iterator<Map.Entry<String, JavaPlugin>> it = 
             javaPlugins.entrySet().iterator();
         while ( it.hasNext() ) {
             Map.Entry<String, JavaPlugin> entry = it.next();
             plugins.add( entry.getValue() );
         }
 
         return plugins;
     }
 
     public boolean pluginIsLoaded ( JavaPlugin plugin )
     {
         String jarPath = pathToPlugin( plugin );
         if ( jarPath.length() == 0 )
             return false;
         return true;
     }
 
     public int countLoadedPlugins ()
     {
         return loadedPlugins().size();
     }
 
     public String pathToPlugin ( JavaPlugin plugin )
     {
         Iterator<Map.Entry<String, JavaPlugin>> it = 
             javaPlugins.entrySet().iterator();
         while ( it.hasNext() ) {
             Map.Entry<String, JavaPlugin> entry = it.next();
             if ( entry.getValue() == plugin ) 
                 return entry.getKey();            
         }        
         return new String();
     }
 
 
     public static void main ( String[] args )
     {
         JavaPluginLoader loader = new JavaPluginLoader();
         try { 
             JavaPlugin javaPlugin = loader.loadPlugin( args[0] );
             System.out.println( "Plugin loaded >>>>" );
             System.out.println( "Path: " + javaPlugin.pluginPath() );
             System.out.println( "Name: " + javaPlugin.pluginInfo().name );
             System.out.println( "Desc: " + javaPlugin.pluginInfo().description);
             System.out.println( "Type: " + javaPlugin.pluginInfo().type );
         } catch ( PluginLoadException excp ) {
             System.out.println("Can't load plugin");
         }
     }
 }
