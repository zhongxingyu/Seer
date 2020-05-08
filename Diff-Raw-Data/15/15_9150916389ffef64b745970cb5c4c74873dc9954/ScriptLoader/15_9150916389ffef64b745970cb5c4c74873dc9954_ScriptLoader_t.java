 /**
  * 
  */
 package cello.alt.servlet.script;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 
 import cello.alt.servlet.js.ModuleProvider;
 import cello.alt.servlet.resource.Resource;
 import cello.alt.servlet.resource.ResourceException;
 
 
 /**
  * Default interface for a ScriptLoader loader.  ScriptLoader is intended to
  *  mimic Java's {@link ClassLoader}. 
  *  
  * 
  * @author Marcello
  */
 public abstract class ScriptLoader {
     
     private Map<String,JavaScript> cache = new HashMap<String,JavaScript>();
     private Map<String,Resource> resourceCache = new HashMap<String,Resource>();
     
     private ScriptLoader parentLoader;
 
     /**
      * Initializes ScriptLoader with no parent loader.
      */
     protected ScriptLoader() {
         this(null);
     }
     /**
      * Initializes ScriptLoader with a parent loader.
      * @param parent
      */
     protected ScriptLoader(ScriptLoader parent) {
         this.parentLoader = parent;
     }
     
     /**
      * Returns the {@link JavaScript} object associated with the named script
      * relative to this ScriptLoader. 
      * @param name  The name of the script to get.
      * @return  the JavaScript object
      * @throws ScriptNotFoundException  if the script was not found
      */
    public synchronized JavaScript loadScript(String name) 
             throws ScriptNotFoundException {
         
         // Check if the script has been loaded
         JavaScript script = findLoadedScript(name);
         if (script != null)
             return script;
 
         if (name.endsWith(".*")) {
             script = new MultiResourceScript(this, getPath(name), name);
             cache.put(name,script);
             return script;
         }
         
         // Check that the name is valid
         if (!isValidName(name))
             throw new ScriptNotFoundException("Invalid script name: "+name);
 
         // Check parent loader
         if (parentLoader != null)
             try {
                 return parentLoader.loadScript(name);
             } catch (ScriptNotFoundException ex) {
                 // nothing
             }
             
         // Find the class
         script = findScript(name);
 
         // Store in cache
         cache.put(name,script);
         return script;
     }
     
     /**
      * Returns a previously (cached) loaded script.  If the script cannot be
      * found, null is returned. 
      * @param name  the name of the script
      * @return the cached script or null
      */
     protected JavaScript findLoadedScript(String name) {
         return cache.get(name);
     }
     
     /**
      * Returns a JavaScripty object from this directory based on the specified
      *  module/script name.
      * @param name the module/script name to load
      * @return the script
      * @throws ScriptNotFoundException if the script could not be loaded (does 
      *  not exist)
      */
     protected JavaScript findScript(String name) throws ScriptNotFoundException {
         try {
             String path = getPath(name);
             return new JavaScriptResource(
                     getModuleProvider().getScriptModule(name), 
                     getResource(path), name);
         } catch (ResourceException ex) {
             throw new ScriptNotFoundException("Could not load "+name, ex);
         }
     }
 
     
     /**
      * Returns the {@link Resource} object associated with the path to this 
      *  ScriptLoader. 
      * @param path  The path to the resource to get.
      * @return  the Resource object
      * @throws ResourceException  if the script was not found
      */
    public synchronized Resource getResource(String path) 
            throws ResourceException {
         
         // Check if the script has been loaded
         Resource resource = findLoadedResource(path);
         if (resource != null)
             return resource;
         
         // Check parent loader
         if (parentLoader != null)
             try {
                 return parentLoader.getResource(path);
             } catch (ResourceException ex) {
                 // nothing
             }
             
         // Find the class
         resource = findResource(path);
         
         // Store in cache
         resourceCache.put(path,resource);
         
         return resource;
     }
     
     /**
      * Returns a previously (cached) loaded script.  If the script cannot be
      * found, null is returned. 
      * @param name  the name of the script
      * @return the cached script or null
      */
     protected Resource findLoadedResource(String name) {
         return resourceCache.get(name);
     }
     
     /**
      * Gets a resource relative to another resource.  This can be a relative 
      *  path or an absolute path starting with /.  This method is similar to 
      *  {@link Class#getResource(java.lang.String)}.
      * 
      * @param context  the resource the resource is relative to
      * @param path  the path to this 
      * @return  the resource object
      * @throws ResourceException if the resource could not be loaded
      */
     public Resource getResource(Resource context, String path) 
             throws ResourceException {
         // Absolute path?
         if (path.startsWith("/"))
             return getResource(path);
         
         
         // Strip off everything from last / to 
         String newPath = getBaseDir(context.getPath());
         while (path.startsWith("../")) {
             path = path.substring(3);
             newPath = getBaseDir(newPath);
         }
         newPath += '/'+path;
         // TODO: Is this good?
         if (newPath.contains(".."))
             throw new ResourceException("Security-error path: "+newPath);
 
         // Return the resource
         return getResource(newPath);
     }
     
     private String getBaseDir(String path) {
         int i = path.lastIndexOf('/');
         if (i<=0)
             return "/"; 
         return path.substring(0, i);
     }
 
     /**
      * Returns the {@link URL} object associated with the named resource
      * relative to this ScriptLoader. 
      * @param name  The name of the resource to get.
      * @return  the resource
      * @throws ResourceException if there was a problem loading the resource
      */
     protected abstract Resource findResource(String name) 
             throws ResourceException;
 
     /**
      * Returns a Set of paths starting with the specified path
      * @param basePath the base path
      * @return a set of paths, or null if none is found.
      * @see ServletContext#getResourcePaths(java.lang.String)
      */
     public abstract Set<String> getResourcePaths(String basePath);
     
     
     private ModuleProvider moduleProvider = null;
     /**
      * Sets the current module provider for this loader.
      * @param moduleProvider the new module provider
      */
     public void setModuleProvider(ModuleProvider moduleProvider) {
         this.moduleProvider = moduleProvider;
     }
     
     /**
      * Returns the module provider used by this script loader.
      * 
      * @return the module provider
      */
     public ModuleProvider getModuleProvider() {
         if (moduleProvider==null) {
             ScriptLoader parent = getParentLoader();
             if (parent!=null)
                 return parent.getModuleProvider();
         }
         return moduleProvider;
     }
 
     private static Pattern validName = Pattern.compile(
             "^([a-z_][a-z0-9_]*\\.)*([a-z_][a-z0-9_]*|\\*)",
             Pattern.CASE_INSENSITIVE);
     
     /**
      * Returns whether or not a particular module/script name is valid.  Valid
      *  module/script names are of the following format:
      *  <ul>
      *   <li>abc</li>
      *   <li>abc.*</li>
      *   <li>abc.xyz</li>
      *   <li>abc.xyz123.*</li>
      *  </ul>
      *  
      * @param name  the module/script name to check  
      * @return true if the name is valid
      */
     private static boolean isValidName(String name) {
         return validName.matcher(name).matches();
     }
     
     /**
      * Converts a module/script name into a file system path.  This essentially
      *  replaces '.' with '/', and '.*' with '/'.  Few examples:
      *  <ul>
      *   <li>abc -> abc</li>
      *   <li>abc.* -> abc/</li>
      *   <li>abc.xyz -> abc/xyz.js</li>
      *   <li>abc.xyz123.* -> abc/xyz123/</li>
      *  </ul>
      * @param name   the module/script name
      * @return  the rewritten path
      * @throws RuntimeException  if the module name was invalid
      */
     public static String getPath(String name) throws RuntimeException {
         if (!isValidName(name))
             throw new RuntimeException("Invalid module name: "+name);
         String path = name.replace('.','/');
         if (path.endsWith("*"))
             return path.substring(0,path.length()-1);
         return path+".js";
     }
     /**
      * @return Returns the parentLoader.
      */
     public ScriptLoader getParentLoader() {
         return parentLoader;
     }
     
 }
