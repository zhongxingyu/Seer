 package net.canarymod.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import net.canarymod.Logman;
 import net.canarymod.config.ConfigurationFile;
 
 /**
  * This class loads, reload, enables and disables plugins.
  * 
  * @author Jos Kuijpers
  */
 public class PluginLoader {
 
     private static final Object lock = new Object();
 
     // Loaded plugins
     private List<Plugin> plugins;
 
     // Plugins that will be loaded before the world
     private HashMap<String, URLClassLoader> preLoad;
     // Dependency storage for the pre-load plugins
     private HashMap<String, ArrayList<String>> preLoadDependencies;
     // Solved order to load preload plugins
     private ArrayList<String> preOrder;
 
     // Plugins that will be loaded after the world
     private HashMap<String, URLClassLoader> postLoad;
     // Dependency storage for the post-load plugins
     private HashMap<String, ArrayList<String>> postLoadDependencies;
     // Solved order to load postload plugins
     private ArrayList<String> postOrder;
 
     // Plugin names that won't be loaded
     private ArrayList<String> noLoad;
 
     private HashMap<String, String> casedNames;
 
     private int stage = 0; // 0 none, 1 scanned, 2 pre, 3 pre+post
 
     public PluginLoader() {
         this.plugins = new ArrayList<Plugin>();
         this.preLoad = new HashMap<String, URLClassLoader>();
         this.postLoad = new HashMap<String, URLClassLoader>();
         this.noLoad = new ArrayList<String>();
         this.preLoadDependencies = new HashMap<String, ArrayList<String>>();
         this.postLoadDependencies = new HashMap<String, ArrayList<String>>();
         this.casedNames = new HashMap<String, String>();
     }
 
     /**
      * Scan for plugins: find the plugins and examine them. Then solve the
      * dependency lists
      * 
      * @return
      */
     public boolean scanPlugins() {
         // We can't do a rescan this way because it needs a reload 
         // of the plugins (AFAIK)
         if (stage != 0) return false;
 
         File dir = new File("plugins/");
         if (!dir.isDirectory()) {
         	Logman.logSevere("Failed to scan for plugins. 'plugins/' is not a directory.");
             return false;
         }
 
         for (String classes : dir.list()) {
             if (!classes.endsWith(".jar")) continue;
             if (!this.scan(classes)) continue;
             String sname = classes.toLowerCase();
             this.casedNames.put(sname.substring(0, sname.lastIndexOf(".")), classes);
         }
 
         // Solve the dependency tree
 
         preOrder = this.solveDependencies(this.preLoadDependencies);
         if (preOrder == null) {
             Logman.logSevere("Failed to solve preload dependency list.");
             return false;
         }
 
         postOrder = this.solveDependencies(this.postLoadDependencies);
         if (postOrder == null) {
             Logman.logSevere("Failed to solve postload dependency list.");
             return false;
         }
 
         // Change the stage
         stage = 1;
 
         return true;
     }
 
     /**
      * Loads the plugins for pre or post load
      * 
      * @param preLoad
      */
     public boolean loadPlugins(boolean preLoad) {
         if ((preLoad && stage != 1) || stage == 3) return false;
         Logman.logInfo("Loading " + ((preLoad) ? "preloadable " : "") + "plugins...");
         if (preLoad) {
             for (String name : this.preOrder) {
                 String rname = this.casedNames.get(name);
                 this.load(rname.substring(0, rname.lastIndexOf(".")), this.preLoad.get(name));
             }
             this.preLoad.clear();
         } else {
             for (String name : this.postOrder) {
                 String rname = this.casedNames.get(name);
                 this.load(rname.substring(0, rname.lastIndexOf(".")), this.postLoad.get(name));
             }
             this.postLoad.clear();
         }
 
        Logman.logInfo("Loaded " + ((preLoad) ? "preloadable " : "") + "plugins...");
 
         // Prevent a double-load (which makes the server crash)
         stage++;
 
         return true;
     }
 
     /**
      * Extract information from the given Jar
      * 
      * This information includes the dependencies and mount point
      * 
      * @param filename
      * @return
      */
     private boolean scan(String filename) {
         try {
             File file = new File("plugins/" + filename);
             String className = filename.substring(0, filename.indexOf("."));
             URL manifestURL = null;
             ConfigurationFile manifesto;
 
             if (!file.isFile()) return false;
 
             // Load the jar file
             URLClassLoader jar = null;
             try {
                 jar = new CanaryClassLoader(new URL[] { file.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
             } catch (MalformedURLException ex) {
                 Logman.logStackTrace("Exception while loading Plugin jar", ex);
                 return false;
             }
 
             // Load file information
             manifestURL = jar.getResource("CANARY.INF");
             if (manifestURL == null) {
                 Logman.logSevere("Failed to load plugin '" + className + "': resource CANARY.INF is missing.");
                 return false;
             }
 
             // Parse the file
             manifesto = new ConfigurationFile(jar.getResourceAsStream("CANARY.INF"));
 
             // Find the mount-point to determine the load-time
             int mountType = 0; // 0 = no, 1 = pre, 2 = post // reused for dependencies
             String mount = manifesto.getString("mount-point", "after");
             if (mount.trim().equalsIgnoreCase("after") || mount.trim().equalsIgnoreCase("post")) mountType = 2;
             else if (mount.trim().equalsIgnoreCase("before") || mount.trim().equalsIgnoreCase("pre")) mountType = 1;
             else if (mount.trim().equalsIgnoreCase("no-load") || mount.trim().equalsIgnoreCase("none")) mountType = 0;
             else {
                 Logman.logSevere("Failed to load plugin " + className + ": resource CANARY.INF is invalid.");
                 return false;
             }
 
             if (mountType == 2) this.postLoad.put(className.toLowerCase(), jar);
             else if (mountType == 1) this.preLoad.put(className.toLowerCase(), jar);
             else if (mountType == 0) { // Do not load, close jar
                 this.noLoad.add(className.toLowerCase());
                 return true;
             }
 
             // Find dependencies and put them in the dependency order-list
             String[] dependencies = manifesto.getString("dependencies", "").split("[,;]");
             ArrayList<String> depends = new ArrayList<String>();
             for (String dependency : dependencies) {
                 dependency = dependency.trim();
 
                 // Remove empty entries
                 if (dependency == "") continue;
 
                 // Remove duplicates
                 if (depends.contains(dependency.toLowerCase())) continue;
 
                 depends.add(dependency.toLowerCase());
             }
             if (mountType == 2) // post
             this.postLoadDependencies.put(className.toLowerCase(), depends);
         } catch (Throwable ex) {
             Logman.logStackTrace("Exception while scanning plugin", ex);
             return false;
         }
 
         return true;
     }
 
     /**
      * The class loader
      * 
      * @param pluginName
      * @param jar
      * @return
      */
     private boolean load(String pluginName, URLClassLoader jar) {
         try {
             String mainClass = "";
 
             try {
             	ConfigurationFile manifesto;
             	
             	// TODO: cache the object instead?
             	// Load the configuration file again
             	manifesto = new ConfigurationFile(jar.getResourceAsStream("CANARY.INF"));
             	
             	// Get the main class, or use the plugin name as class
                 mainClass = manifesto.getString("main-class", pluginName);
             } catch (IOException e) {
                 Logman.logStackTrace("Failed to load manifest of plugin '" + pluginName + "'.", e);
                 return false;
             }
 
             if (mainClass == "") {
                 Logman.logSevere("Failed to find Manifest in plugin '" + pluginName + "'");
                 return false;
             }
 
             Class<?> c = jar.loadClass(mainClass);
             Plugin plugin = (Plugin) c.newInstance();
 
             synchronized (lock) {
                 this.plugins.add(plugin);
                 plugin.enable();
             }
         } catch (Throwable ex) {
             Logman.logStackTrace("Exception while loading plugin '" + pluginName + "'", ex);
             return false;
         }
 
         return true;
     }
 
     /**
      * Start solving the dependency list given.
      * 
      * @param pluginDependencies
      * @return
      */
     private ArrayList<String> solveDependencies(HashMap<String, ArrayList<String>> pluginDependencies) {
         // http://www.electricmonk.nl/log/2008/08/07/dependency-resolving-algorithm/
 
         if (pluginDependencies.size() == 0) return new ArrayList<String>();
 
         ArrayList<String> retOrder = new ArrayList<String>();
         HashMap<String, DependencyNode> graph = new HashMap<String, DependencyNode>();
 
         // Create the node list
         for (String name : pluginDependencies.keySet()) {
             graph.put(name, new DependencyNode(name));
         }
 
         // Add dependency nodes to the nodes
         ArrayList<String> isDependency = new ArrayList<String>();
         for (String pluginName : pluginDependencies.keySet()) {
         	DependencyNode node = graph.get(pluginName);
             for (String depName : pluginDependencies.get(pluginName)) {
                 if (!graph.containsKey(depName)) {
                     // Dependency does not exist, lets happily fail
                     Logman.logWarning("Failed to solve dependency '" + depName + "'");
                     continue;
                 }
                 node.addEdge(graph.get(depName));
                 isDependency.add(depName);
             }
         }
 
         // Remove nodes in the top-list that are in the graph too
         for (String dep : isDependency) {
             graph.remove(dep);
         }
 
         // If there are no nodes anymore, there might have been a circular dependency
         if (graph.size() == 0) {
             Logman.logWarning("Failed to solve dependency graph. Is there a circular dependency?");
             return null;
         }
 
         // The graph now contains elements that either have edges or are lonely
 
         ArrayList<DependencyNode> resolved = new ArrayList<DependencyNode>();
         for (String n : graph.keySet()) {
 
             this.depResolve(graph.get(n), resolved);
         }
 
         for (DependencyNode x : resolved)
             retOrder.add(x.getName());
 
         return retOrder;
     }
 
     /**
      * This recursive method actually solves the dependency lists
      * 
      * @param node
      * @param resolved
      */
     private void depResolve(DependencyNode node, ArrayList<DependencyNode> resolved) {
         for (DependencyNode edge : node.edges) {
             if (!resolved.contains(edge)) this.depResolve(edge, resolved);
         }
         resolved.add(node);
     }
 
     public Plugin getPlugin(String name) {
         synchronized (lock) {
             for (Plugin plugin : plugins) {
                 if (plugin.getName().equalsIgnoreCase(name)) {
                     return plugin;
                 }
             }
         }
 
         return null;
     }
 
     public String[] getPluginList() {
         ArrayList<String> list = new ArrayList<String>();
         String[] ret = {};
 
         synchronized (lock) {
             for (Plugin plugin : this.plugins) {
                 list.add(plugin.getName());
             }
         }
 
         return list.toArray(ret);
     }
 
     public String getReadablePluginList() {
         StringBuilder sb = new StringBuilder();
 
         synchronized (lock) {
             for (Plugin plugin : plugins) {
                 sb.append(plugin.getName());
                 sb.append(" ");
                 //sb.append(plugin.isEnabled() ? "(E)" : "(D)");
                 sb.append(",");
             }
         }
         String str = sb.toString();
 
         if (str.length() > 1) {
             return str.substring(0, str.length() - 1);
         } else {
             return "Empty";
         }
     }
 
     // TODO implement enabling/disabling plugins
     public boolean enablePlugin(String name) {
         Plugin plugin = this.getPlugin(name);
         if (plugin == null) return false;
 
         //if(plugin.isEnabled())
         //	return true;
 
         plugin.enable();
 
         return true;
     }
 
     public boolean disablePlugin(String name) {
         Plugin plugin = this.getPlugin(name);
         if (plugin == null) return false;
 
         //if(!plugin.isEnabled())
         //	return true;
 
         plugin.disable();
 
         return true;
     }
 
     /**
      * A node used in solving the dependency tree.
      * 
      * @author Jos Kuijpers
      *
      */
     class DependencyNode {
 
         private String name;
         public ArrayList<DependencyNode> edges;
 
         DependencyNode(String name) {
             this.name = name;
             this.edges = new ArrayList<DependencyNode>();
         }
 
         String getName() {
             return this.name;
         }
 
         void addEdge(DependencyNode node) {
             this.edges.add(node);
         }
 
         /* Debugging only
         public String toString() {
             StringBuilder sb = new StringBuilder();
 
             sb.append("<" + this.name + ">(");
             for (DependencyNode node : this.edges) {
                 sb.append(node.toString());
                 sb.append(",");
             }
             int idx = sb.lastIndexOf(",");
             if (idx != -1) sb.deleteCharAt(idx);
             sb.append(")");
 
             return sb.toString();
         }*/
     }
     
     /**
      * Class loader used to load classes dynamically. This also closes the jar so we
      * can reload the plugin.
      * 
      * @author James
      * 
      */
     class CanaryClassLoader extends URLClassLoader {
 
         public CanaryClassLoader(URL[] urls, ClassLoader loader) {
             super(urls, loader);
         }
 
         @SuppressWarnings("rawtypes")
         public void close() {
             try {
                 Class<?> clazz = java.net.URLClassLoader.class;
                 java.lang.reflect.Field ucp = clazz.getDeclaredField("ucp");
 
                 ucp.setAccessible(true);
                 Object sun_misc_URLClassPath = ucp.get(this);
                 java.lang.reflect.Field loaders = sun_misc_URLClassPath.getClass().getDeclaredField("loaders");
 
                 loaders.setAccessible(true);
                 Object java_util_Collection = loaders.get(sun_misc_URLClassPath);
 
                 for (Object sun_misc_URLClassPath_JarLoader : ((java.util.Collection) java_util_Collection).toArray()) {
                     try {
                         java.lang.reflect.Field loader = sun_misc_URLClassPath_JarLoader.getClass().getDeclaredField("jar");
 
                         loader.setAccessible(true);
                         Object java_util_jar_JarFile = loader.get(sun_misc_URLClassPath_JarLoader);
 
                         ((java.util.jar.JarFile) java_util_jar_JarFile).close();
                     } catch (Throwable t) {
                         // if we got this far, this is probably not a JAR loader so
                         // skip it
                     }
                 }
             } catch (Throwable t) {
                 // Probably not a Sun (correct: Oracle) VM.
             }
             return;
         }
     }
 }
