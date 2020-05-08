 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.classloading.spi.dependency;
 
 import java.io.IOException;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.jboss.classloader.spi.ClassLoaderPolicy;
 import org.jboss.classloader.spi.ClassLoaderSystem;
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.classloader.spi.ParentPolicy;
 import org.jboss.classloader.spi.ShutdownPolicy;
 import org.jboss.classloader.spi.base.BaseClassLoader;
 import org.jboss.classloader.spi.filter.ClassFilter;
 import org.jboss.classloader.spi.filter.FilteredDelegateLoader;
 import org.jboss.classloader.spi.filter.PackageClassFilter;
 import org.jboss.classloading.plugins.metadata.PackageCapability;
 import org.jboss.classloading.plugins.metadata.PackageRequirement;
 import org.jboss.classloading.spi.helpers.NameAndVersionSupport;
 import org.jboss.classloading.spi.metadata.Capability;
 import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
 import org.jboss.classloading.spi.metadata.ExportAll;
 import org.jboss.classloading.spi.metadata.ExportPackages;
 import org.jboss.classloading.spi.metadata.OptionalPackages;
 import org.jboss.classloading.spi.metadata.Requirement;
 import org.jboss.classloading.spi.visitor.ResourceFilter;
 import org.jboss.classloading.spi.visitor.ResourceVisitor;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyInfo;
 import org.jboss.dependency.spi.DependencyItem;
 import org.jboss.logging.Logger;
 
 /**
  * Module.
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class Module extends NameAndVersionSupport
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 1L;
 
    /** The log */
    private static final Logger log = Logger.getLogger(Module.class);
    
    /** The modules by classloader */
    private static Map<ClassLoader, Module> modulesByClassLoader = new ConcurrentHashMap<ClassLoader, Module>();
    
    /** The lazily shutdown modules */
    private static Set<Module> lazyShutdownModules = new CopyOnWriteArraySet<Module>();
    
    /** The context name */
    private String contextName;
    
    /** Our cached capabilities */
    private List<Capability> capabilities;
    
    /** Our cached requirements */
    private List<Requirement> requirements;
 
    /** The controller context */
    private ControllerContext context;
 
    /** The domain */
    private Domain domain;
 
    /** The classloading space */
    private ClassLoadingSpace space;
    
    /** The requirements */
    private List<RequirementDependencyItem> requirementDependencies;
 
    /** Any lifecycle associated with the classloader */
    private LifeCycle lifeCycle;
    
    /** The remembered policy for cascade shutdown */
    private Boolean cascadeShutdown;
    
    /** Requirements resolved to us */
    private Set<RequirementDependencyItem> depends = new CopyOnWriteArraySet<RequirementDependencyItem>();
    
    /**
     * Register a classloader for a module
     * 
     * @param module the module
     * @param classLoader the classloader
     * @throws IllegalArgumentException for a null parameter
     */
    protected static void registerModuleClassLoader(Module module, ClassLoader classLoader)
    {
       if (module == null)
          throw new IllegalArgumentException("Null module");
       if (classLoader == null)
          throw new IllegalArgumentException("Null classloader");
 
       modulesByClassLoader.put(classLoader, module);
 
       // This is hack - we might not know until the classloader gets constructed whether
       // it is in a domain that specifies lazy shutdown of the classloader
       module.cascadeShutdown = module.isCascadeShutdown();
       if (module.cascadeShutdown == false)
          module.enableLazyShutdown();
       
       LifeCycle lifeCycle = module.getLifeCycle();
       if (lifeCycle != null)
          lifeCycle.fireResolved();
    }
    
    /**
     * Register a classloader for a module
     * 
     * @param module the module
     * @param classLoader the classloader
     * @throws IllegalArgumentException for a null parameter
     */
    protected static void unregisterModuleClassLoader(Module module, ClassLoader classLoader)
    {
       if (module == null)
          throw new IllegalArgumentException("Null module");
       if (classLoader == null)
          throw new IllegalArgumentException("Null classloader");
 
       modulesByClassLoader.remove(classLoader);
       
       module.unresolveDependencies();
       
       LifeCycle lifeCycle = module.getLifeCycle();
       if (lifeCycle != null)
          lifeCycle.fireUnresolved();
       
       if (module.isCascadeShutdown() == false && module.depends.isEmpty() == false)
          lazyShutdownModules.add(module);
    }
    
    /**
     * Create a new Module with the default version
     *
     * @param name the name
     * @throws IllegalArgumentException for a null parameter
     */
    public Module(String name)
    {
       this(name, name, null);
    }
    
    /**
     * Create a new Module with the given version
     *
     * @param name the name
     * @param version pass null for the default version
     * @throws IllegalArgumentException for a null parameter
     */
    public Module(String name, Object version)
    {
       this(name, name, version);
    }
    
    /**
     * Create a new Module with the given version
     *
     * @param name the name
     * @param contextName the real name of the module in the controller
     * @param version pass null for the default version
     * @throws IllegalArgumentException for a null parameter
     */
    public Module(String name, String contextName, Object version)
    {
       super(name, version);
       if (contextName == null)
          contextName = name + version.toString();
       this.contextName = contextName;
    }
 
    /**
     * Get the context name
     * 
     * @return the context name
     */
    public String getContextName()
    {
       return contextName;
    }
    
    /**
     * Get the domain.
     * 
     * @return the domain.
     */
    Domain getDomain()
    {
       return domain;
    }
 
    void setDomain(Domain domain)
    {
       this.domain = domain;
    }
 
    protected Domain checkDomain()
    {
       Domain result = domain;
       if (result == null)
          throw new IllegalStateException("Domain is not set for " + this);
       return result;
    }
    
    /**
     * Whether this is a valid  module
     * 
     * @return true when valid
     */
    public boolean isValid()
    {
       return domain != null;
    }
    
    /**
     * Get the domain name.
     * 
     * @return the domain name.
     */
    public String getDomainName()
    {
       return null;
    }
 
    /**
     * Get the determined domain name.
     * 
     * @return the determined domain.
     */
    public String getDeterminedDomainName()
    {
       String domainName = getDomainName();
       if (domainName == null)
          domainName = ClassLoaderSystem.DEFAULT_DOMAIN_NAME;
       return domainName;
    }
 
    /**
     * Get the parent domain name.
     * 
     * @return the parent domain name.
     */
    public String getParentDomainName()
    {
       return null;
    }
 
    /**
     * Get the determined parentDomain name.
     * 
     * @return the parentDomain.
     */
    public String getDeterminedParentDomainName()
    {
       String parentDomain = getParentDomainName();
       if (parentDomain == null)
       {
          if (ClassLoaderSystem.DEFAULT_DOMAIN_NAME.equals(getDeterminedDomainName()) == false)
             return ClassLoaderSystem.DEFAULT_DOMAIN_NAME;
       }
       return parentDomain;
    }
 
    /**
     * Get the classloading space.
     * 
     * @return the space.
     */
    ClassLoadingSpace getClassLoadingSpace()
    {
       return space;
    }
 
    /**
     * Set the classloading space.
     * 
     * @param space the space.
     */
    void setClassLoadingSpace(ClassLoadingSpace space)
    {
       this.space = space;
    }
 
    /**
     * Get the export all for the module
     * 
     * @return the export all
     */
    public ExportAll getExportAll()
    {
       return null;
    }
 
    /**
     * Get the shutdown policy
     * 
     * @return the shutdown policy
     */
    public ShutdownPolicy getShutdownPolicy()
    {
       return null;
    }
 
    /**
     * Whether to casecade the shutdown
     * 
     * @return true to cascade the shutdown
     */
    public boolean isCascadeShutdown()
    {
       // Has it been determined?
       if (cascadeShutdown != null)
          return cascadeShutdown;
       
       // This is ugly
       ClassLoader cl = getClassLoader();
       if (cl != null && cl instanceof BaseClassLoader)
       {
          ShutdownPolicy shutdownPolicy = ((BaseClassLoader) cl).getShutdownPolicy();
          return ShutdownPolicy.GARBAGE_COLLECTION != shutdownPolicy;
       }
       return true;
    }
 
    /**
     * Get a filter for the included packages
     * 
     * @return the excluded packages
     */
    public ClassFilter getIncluded()
    {
       return null;
    }
 
    /**
     * Get a filter for the excluded packages
     * 
     * @return the excluded packages
     */
    public ClassFilter getExcluded()
    {
       return null;
    }
 
    /**
     * Get a filter for the excluded export packages
     * 
     * @return the excluded packages
     */
    public ClassFilter getExcludedExport()
    {
       return null;
    }
 
    /**
     * Get the import all for the module
     * 
     * @return the import all
     */
    public boolean isImportAll()
    {
       return false;
    }
 
    /**
     * Get delegate policy
     * 
     * @return the delegation policy
     */
    public boolean isJ2seClassLoadingCompliance()
    {
       return true;
    }
 
    public ParentPolicy getDeterminedParentPolicy()
    {
       if (isJ2seClassLoadingCompliance())
          return ParentPolicy.BEFORE;
       else
          return ParentPolicy.AFTER_BUT_ONLY_JAVA_BEFORE;
    }
    
    /**
     * Whether to cache<p>
     * 
     * @return true to cache
     */
    protected boolean isCacheable()
    {
       return true;
    }
 
    /**
     * Whether to cache misses<p>
     * 
     * @return true to cache misses
     */
    protected boolean isBlackListable()
    {
       return true;
    }
    
    /**
     * Get the lifecycle.
     * 
     * @return the lifecycle.
     */
    public LifeCycle getLifeCycle()
    {
       return lifeCycle;
    }
 
    /**
     * Set the lifeCycle.
     * 
     * @param lifeCycle the lifeCycle.
     */
    public void setLifeCycle(LifeCycle lifeCycle)
    {
       if (lifeCycle != null && lifeCycle.getModule() != this)
          throw new IllegalArgumentException("Cannot setLifeCycle on module " + this + " it is associated with a different module: " + lifeCycle.getModule());
       this.lifeCycle = lifeCycle;
    }
 
    /**
     * Find the module for a classloader
     * 
     * @param cl the classloader
     * @return the module or null if the classloader does not correspond to a registered module classloader
     * @throws SecurityException if the caller doesn't have <code>new RuntimePermision("getClassLoader")</code>
     */
    static Module getModuleForClassLoader(ClassLoader cl)
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
          sm.checkPermission(new RuntimePermission("getClassLoader"));
       
       // Determine the module (if any) for the classloader 
       if (cl != null)
          return modulesByClassLoader.get(cl);
       // Unknown
       return null;
    }
    
    /**
     * Find the module that loads a class
     * 
     * @param clazz the class
     * @return the module or null if the class is not loaded by a registered module classloader
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    public static Module getModuleForClass(Class<?> clazz)
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
          sm.checkPermission(new RuntimePermission("getClassLoader"));
 
       ClassLoader cl = getClassLoaderForClass(clazz);
 
       // Determine the module (if any) for the classloader 
       if (cl != null)
          return modulesByClassLoader.get(cl);
       // Unknown
       return null;
    }
    
    /**
     * Find the module that loads a class
     * 
     * @param className the class name
     * @return the module or null if the class is not loaded by a registered module classloader
     * @throws ClassNotFoundException when the class is not found
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    public Module getModuleForClass(String className) throws ClassNotFoundException
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
          sm.checkPermission(new RuntimePermission("getClassLoader"));
 
       ClassLoader cl = getClassLoaderForClass(className);
 
       // Determine the module (if any) for the classloader 
       if (cl != null)
          return modulesByClassLoader.get(cl);
       // Unknown
       return null;
    }
 
    /**
     * Get the classloader for a class 
     * 
     * @param clazz the class
     * @return the classloader
     */
    protected static ClassLoader getClassLoaderForClass(final Class<?> clazz)
    {
       if (clazz == null)
          throw new IllegalArgumentException("Null class");
       
       // Determine the classloader for this class
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
       {
          return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
          {
             public ClassLoader run()
             {
                return clazz.getClassLoader(); 
             }
         });
       }
       return clazz.getClassLoader();
    }
 
    /**
     * Get the classloader for a class name 
     * 
     * @param className the class name
     * @return the class
     * @throws ClassNotFoundException when the class is not found
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    protected ClassLoader getClassLoaderForClass(String className) throws ClassNotFoundException
    {
       // Determine the classloader for this class
       final Class<?> clazz = loadClass(className);
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
       {
          return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
          {
             public ClassLoader run()
             {
                return clazz.getClassLoader(); 
             }
         });
       }
       return clazz.getClassLoader();
    }
 
    /**
     * Load a class for this module 
     * 
     * @param className the class name
     * @return the class
     * @throws ClassNotFoundException when the class is not found
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException
    {
       ClassLoader classLoader = getClassLoader();
       if (classLoader == null)
          throw new IllegalStateException("No classloader for this module " + this);
       return classLoader.loadClass(className);
    }
 
    /**
     * Get a resource for this module 
     * 
     * @param resourceName the resource name
     * @return the class
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    public URL getResource(String resourceName) 
    {
       ClassLoader classLoader = getClassLoader();
       if (classLoader == null)
          throw new IllegalStateException("No classloader for this module " + this);
       return classLoader.getResource(resourceName);
    }
 
    /**
     * Get resources for this module 
     * 
     * @param resourceName the resource name
     * @return the class
     * @throws IOException for an error
     * @throws IllegalStateException when the module is not associated with a classloader
     */
    public Enumeration<URL> getResources(String resourceName) throws IOException
    {
       ClassLoader classLoader = getClassLoader();
       if (classLoader == null)
          throw new IllegalStateException("No classloader for this module " + this);
       return classLoader.getResources(resourceName);
    }
 
    /**
     * Get the classloader for this module
     * 
     * @return the classloader
     */
    protected ClassLoader getClassLoader()
    {
       return null;
    }
    
    /**
     * Visit the resources in this module
     * using the filter defined on the visitor
     * 
     * @param visitor the visitor
     */
    public void visit(ResourceVisitor visitor)
    {
       if (visitor == null)
          throw new IllegalArgumentException("Null visitor");
       visit(visitor, visitor.getFilter());
    }
 
    /**
     * Visit the resources in this module
     * using the given filter
     * 
     * @param visitor the visitor
     * @param filter the filter
     */
    public void visit(ResourceVisitor visitor, ResourceFilter filter)
    {
       visit(visitor, filter, null);
    }
 
    /**
     * Visit the resources in this module using the given filter(s)
     * <p/>
     * Typically the filter is used to determine which types of files to visit, e.g. .class files.
     * While the recurseFilter determines which jars/directories to recurse into.
     *
     * @param visitor       the visitor
     * @param filter        the filter
     * @param recurseFilter the recursion filter (null means recurse into everything)
     * @param urls the urls we should visit
     */
    public void visit(ResourceVisitor visitor, ResourceFilter filter, ResourceFilter recurseFilter, URL... urls)
    {
       throw new UnsupportedOperationException("The module " + getContextName() + " does not support filtering: " + getClass().getName());
    }
 
    /**
     * Get the delegate loaders for this module
     * 
     * @return the delegates
     */
    public List<? extends DelegateLoader> getDelegates()
    {
       if (requirementDependencies == null || requirementDependencies.isEmpty())
          return null;
 
       List<DelegateLoader> result = new CopyOnWriteArrayList<DelegateLoader>();
       List<DelegateLoader> dynamic = new CopyOnWriteArrayList<DelegateLoader>();
       Set<Module> visited = new HashSet<Module>();
       addDelegates(this, result, dynamic, visited, false);
       
       // Make sure the dynamic delegates are last
       result.addAll(dynamic);
       
       return result;
    }
    
    /**
     * Get the dependency items
     * 
     * @return the depenency items
     */
    protected List<RequirementDependencyItem> getRequirementDependencyItems()
    {
       return requirementDependencies;
    }
 
    /**
     * Add delegates
     * 
     * @param module the module to add delegates from
     * @param delegates the current list of delegates
     * @param dynamic the dynamic delegates
     * @param visited the visited modules
     * @param reExport whether to only add re-exports
     */
    protected void addDelegates(Module module, List<DelegateLoader> delegates, List<DelegateLoader> dynamic, Set<Module> visited, boolean reExport)
    {
       // Check whether we already did this module
       if (visited.contains(module))
          return;
       
       visited.add(module);
       
       List<RequirementDependencyItem> dependencies = module.getRequirementDependencyItems();
       if (dependencies == null || dependencies.isEmpty())
          return;
       
       // Maps the ClassLoaderPolicy that we get from the iDependOnModule to the list of package names that we are importing
       Map<ClassLoaderPolicy, List<String>> delegateToRequiredPackages = new LinkedHashMap<ClassLoaderPolicy, List<String>>();
       
       for (RequirementDependencyItem item : dependencies)
       {
          Requirement requirement = item.getRequirement();
          
          // If we are looking at everything or this is a re-export
          if (reExport == false || requirement.isReExport())
          {
             // Sanity checks
             if (item.isResolved() == false)
                throw new IllegalStateException("Item not resolved: " + item);
             
             // Dynamic requirement, create it lazily
             if (requirement.isDynamic())
             {
                DelegateLoader delegate = createLazyDelegateLoader(checkDomain(), item);
                dynamic.add(delegate);
                continue;
             }
 
             String name = (String) item.getIDependOn();
             if (name == null)
             {
                // Optional requirement, just ignore
                if (requirement.isOptional())
                   continue;
                // Something has gone wrong
                throw new IllegalStateException("No iDependOn for item: " + item);
             }
             
             Module iDependOnModule = checkDomain().getModule(name);
             if (iDependOnModule == null)
                throw new IllegalStateException("Module not found with name: " + name);
 
             // Determine the delegate loader for the module
             DelegateLoader delegate = iDependOnModule.getDelegateLoader(module, requirement);
             if (delegate == null)
                throw new IllegalStateException("Cannot obtain delegate for: " + requirement); 
 
             // Check for re-export by the module
             if (requirement.wantReExports())
                addDelegates(iDependOnModule, delegates, dynamic, visited, true);
             
             // Only add a the delegate if this is not a self-dependency
             if (iDependOnModule != module)
             {
                // If we are connecting to another module we collect the imported package names per delegate
                if (requirement instanceof PackageRequirement)
                {
                   ClassLoaderPolicy policy = delegate.getPolicy();
                   List<String> packageNames = delegateToRequiredPackages.get(policy);
                   if (packageNames == null)
                   {
                      packageNames = new ArrayList<String>();
                      delegateToRequiredPackages.put(policy, packageNames);
                   }
                   
                   PackageRequirement packageRequirement = (PackageRequirement)requirement;
                   packageNames.add(packageRequirement.getName());
                }
                else
                {
                   delegates.add(delegate);
                }
             }
          }
       }
       
       // Add FilteredDelegateLoaders for all collected package requirements
       for (Entry<ClassLoaderPolicy, List<String>> entry : delegateToRequiredPackages.entrySet())
       {
          PackageClassFilter filter = PackageClassFilter.createPackageClassFilter(entry.getValue());
          delegates.add(new FilteredDelegateLoader(entry.getKey(), filter));
       }
    }
 
    /**
     * Create a lazy delegate loader
     * 
     * @param domain the domain
     * @param item the dependency item
     * @return the delegate loader
     */
    public abstract DelegateLoader createLazyDelegateLoader(Domain domain, RequirementDependencyItem item);
 
    /**
     * Get the delegate loader
     * 
     * @param requiringModule the requiring module
     * @param requirement the requirement
     * @return the delegate loader
     */
    public abstract DelegateLoader getDelegateLoader(Module requiringModule, Requirement requirement);
 
    /**
     * Get the exported packages
     * 
     * @return the exported packages
     */
    public Collection<ExportPackage> getExportedPackages()
    {
       Collection<ExportPackage> result = new HashSet<ExportPackage>();
       List<Capability> capabilities = getCapabilitiesRaw();
       if (capabilities != null && capabilities.isEmpty() == false)
       {
          for (Capability capability : capabilities)
          {
             if (capability instanceof PackageCapability)
             {
                ExportPackage exportPackage = new ExportPackage(this, (PackageCapability) capability);
                result.add(exportPackage);
             }
          }
       }
       return result;
    }
 
    /**
     * Refresh the specified modules<p>
     * 
     * Pass null to refresh any undeployed lazy shutdown modules dependencies
     * 
     * @param modules the modules
     * @throws Exception for any error
     */
    public static void refreshModules(Module... modules) throws Exception
    {
       if (modules == null || modules.length == 0)
       {
          Set<Module> snapshot = new HashSet<Module>(lazyShutdownModules);
          modules = snapshot.toArray(new Module[snapshot.size()]);
       }
       
       if (modules.length == 0)
          return;
       
       Set<LifeCycle> lifecycles = new HashSet<LifeCycle>();
       Set<Module> processed = new HashSet<Module>();
       for (Module module : modules)
       {
          if (module == null)
             throw new IllegalArgumentException("Null module");
          module.addRefreshModule(lifecycles, processed);
       }
       
       if (lifecycles.isEmpty() == false)
       {
          LifeCycle[] result = lifecycles.toArray(new LifeCycle[lifecycles.size()]);
          result[0].bounce(result);
       }
    }
    
    private void addRefreshModule(Set<LifeCycle> lifecycles, Set<Module> processed)
    {
       // Avoid recursion
       if (processed.contains(this))
          return;
       processed.add(this);
       
       // Add our lifecycle - if we have a classloader
       if (getClassLoader() != null)
       {
          LifeCycle lifeCycle = getLifeCycle();
          if (lifeCycle != null)
             lifecycles.add(lifeCycle);
          else
             log.warn(this + " has no lifecycle, don't know how to refresh it.");
       }
       
       // Add dependencies that are not already managed
       if (isCascadeShutdown() == false && depends.isEmpty() == false)
       {
          for (RequirementDependencyItem item : depends)
             item.getModule().addRefreshModule(lifecycles, processed);
       }
    }
 
    public static boolean resolveModules(Module... modules) throws Exception
    {
       if (modules == null || modules.length == 0)
          return true;
 
       LifeCycle[] lifeCycles = new LifeCycle[modules.length]; 
       for (int i = 0; i < modules.length; ++i)
       {
          Module module = modules[i];
          if (module == null)
             throw new IllegalArgumentException("Null module");
          LifeCycle lifeCycle = module.getLifeCycle();
          if (lifeCycle == null)
            throw new IllegalStateException(module + " has no lifecycle, don't know how to resolve it.");
          lifeCycles[i] = lifeCycle; 
       }
 
       lifeCycles[0].resolve(lifeCycles);
       
       for (LifeCycle lifeCycle : lifeCycles)
       {
          if (lifeCycle.isResolved() == false)
             return false;
       }
       return true;
    }
 
    /**
     * Get the capabilities.
     * 
     * @return the capabilities.
     */
    public List<Capability> getCapabilities()
    {
       // Have we already worked this out?
       if (capabilities != null)
          return capabilities;
       
       // Are there any configured ones?
       List<Capability> capabilities = determineCapabilities();
       
       // Use the defaults
       if (capabilities == null)
          capabilities = defaultCapabilities();
 
       //Add global capabilities
       capabilities = checkDomain().mergeGlobalCapabilities(capabilities);
       
       // Cache it
       this.capabilities = capabilities;
       return capabilities;
    }
 
    /**
     * Determine the capabilities
     * 
     * @return the capabilities
     */
    protected List<Capability> determineCapabilities()
    {
       return null;
    }
 
    /**
     * Determine the default capabilities.<p>
     * 
     * By default it is just the module capability
     * 
     * @return the capabilities
     */
    protected List<Capability> defaultCapabilities()
    {
       List<Capability> capabilities = new CopyOnWriteArrayList<Capability>();
       ClassLoadingMetaDataFactory factory = ClassLoadingMetaDataFactory.getInstance();
       Capability capability = factory.createModule(getName(), getVersion());
       capabilities.add(capability);
       return capabilities;
    }
 
    List<Capability> getCapabilitiesRaw()
    {
       return capabilities;
    }
    
    /**
     * Get the package names
     * 
     * @return the package names
     */
    public String[] getPackageNames()
    {
       List<String> packageNames = determinePackageNames(true);
       return packageNames.toArray(new String[packageNames.size()]);
    }
 
    /**
     * Determine the package names
     * 
     * TODO JBCL-23 Better handling of conflicts for optional packages
     * @param optional whether to include optional packages
     * @return the package names
     */
    public List<String> determinePackageNames(boolean optional)
    {
       List<String> packageNames = Collections.emptyList();
 
       List<Capability> capabilities = getCapabilities();
       if (capabilities != null && capabilities.isEmpty() == false)
       {
          for (Capability capability : capabilities)
          {
             if (capability instanceof ExportPackages)
             {
                ExportPackages exported = (ExportPackages) capability;
                Set<String> exportPackages = exported.getPackageNames(this);
                if (exportPackages != null)
                {
                   if (packageNames.isEmpty())
                      packageNames = new ArrayList<String>();
                   packageNames.addAll(exportPackages);
                }
             }
          }
       }
 
       List<Requirement> requirements = getRequirements();
       if (requirements != null && requirements.isEmpty() == false)
       {
          for (Requirement requirement : getRequirements())
          {
             if (optional == false && requirement instanceof OptionalPackages)
             {
                OptionalPackages exported = (OptionalPackages) requirement;
                Set<String> optionalPackages = exported.getOptionalPackageNames(this);
                if (optionalPackages != null && packageNames.isEmpty() == false)
                   packageNames.removeAll(optionalPackages);
             }
          }
       }
 
       return packageNames;
    }
    
    /**
     * Return the export package capability for a given package name
     * @param exportedPackage the name of the exported package
     * @return null if the capability cannot be found
     */
    public PackageCapability getExportCapability(String exportedPackage)
    {
       List<Capability> capabilities = getCapabilities();
       if (capabilities != null && capabilities.isEmpty() == false)
       {
          for (Capability capability : capabilities)
          {
             if (capability instanceof PackageCapability)
             {
                PackageCapability exported = (PackageCapability) capability;
                for (String packageName : exported.getPackageNames(this))
                {
                   if (packageName.equals(exportedPackage))
                      return exported;
                }
             }
          }
       }
       return null;
    }
    
    /**
     * Get the state for the classloader
     * 
     * @return the state
     */
    public ControllerState getClassLoaderState()
    {
       return ControllerState.INSTALLED;
    }
 
    /**
     * Get the requirements.
     * 
     * @return the requirements.
     */
    public List<Requirement> getRequirements()
    {
       // Have we already worked this out?
       if (requirements != null)
          return requirements;
       
       // Are there any configured ones?
       List<Requirement> requirements = determineRequirements();
       
       // Cache it
       this.requirements = requirements;
       return requirements;
    }
 
    /**
     * Determine the requirements.
     * 
     * @return the requirements.
     */
    public List<Requirement> determineRequirements()
    {
       return Collections.emptyList();
    }
 
    /**
     * Get the requirements as they are now.
     * 
     * @return the requirements.
     */
    List<Requirement> getRequirementsRaw()
    {
       return requirements;
    }
 
    /**
     * Return a URL where dynamic classes can be stored
     * 
     * @return the url or null if there isn't one
     */
    public URL getDynamicClassRoot()
    {
       return null;
    }
    
    List<RequirementDependencyItem> getDependencies()
    {
       return requirementDependencies;
    }
    
    /**
     * Create the dependencies for the module
     */
    protected void createDependencies()
    {
       ControllerState classLoaderState = getClassLoaderState();
       
       List<Requirement> requirements = getRequirements();
       if (requirements != null)
       {
          requirementDependencies = new ArrayList<RequirementDependencyItem>();
          for (Requirement requirement : requirements)
          {
             // [JBCL-113] RequirementDependencyItems can only resolve againt INSTALLED contexts
             RequirementDependencyItem item = new RequirementDependencyItem(this, requirement, classLoaderState, classLoaderState);
             addIDependOn(item);
             requirementDependencies.add(item);
          }
       }
    }
 
    /**
     * Remove dependencies
     */
    protected void removeDependencies()
    {
       if (requirementDependencies != null && requirementDependencies.isEmpty() == false)
       {
          for (RequirementDependencyItem item : requirementDependencies)
             removeIDependOn(item);
       }
       requirementDependencies = null;
    }
 
    /**
     * Unresolve dependencies
     */
    protected void unresolveDependencies()
    {
       Controller controller = context.getController();
       if (requirementDependencies != null && requirementDependencies.isEmpty() == false)
       {
          for (RequirementDependencyItem item : requirementDependencies)
             item.unresolved(controller);
       }
    }
 
    /**
     * Get the controller context.
     * 
     * @return the controller context.
     */
    protected ControllerContext getControllerContext()
    {
       return context;
    }
 
    /**
     * Set the controller context
     * 
     * @param context the context
     */
    protected void setControllerContext(ControllerContext context)
    {
       this.context = context;
    }
    
    /**
     * Add a dependency
     * 
     * @param item the dependency item
     */
    protected void addIDependOn(RequirementDependencyItem item)
    {
       if (context == null)
          throw new IllegalStateException("No controller context");
       context.getDependencyInfo().addIDependOn(item);
    }
    
    /**
     * Remove a dependency
     * 
     * @param item the dependency item
     */
    protected void removeIDependOn(RequirementDependencyItem item)
    {
       if (context == null)
          throw new IllegalStateException("No controller context");
       
       // Remove the DependsOnMe part of this item
       item.setResolved(false);
       
       // Remove the IDependOn part of this item
       DependencyInfo dependencyInfo = context.getDependencyInfo();
       dependencyInfo.removeIDependOn(item);
    }
 
    /**
     * Remove a dependency.
     *
     * @param item the dependency item.
     */
    protected void removeDependsOnMe(RequirementDependencyItem item)
    {
       if (context == null)
          return;
       
       DependencyInfo dependencyInfo = context.getDependencyInfo();
       dependencyInfo.removeDependsOnMe(item);
    }
 
    /**
     * Add a dependency resolved against us
     * 
     * @param item the dependency
     */
    void addDepends(RequirementDependencyItem item)
    {
       depends.add(item);
    }
 
    /**
     * Remove a dependency resolved against us
     * 
     * @param item the dependency
     */
    void removeDepends(RequirementDependencyItem item)
    {
       depends.remove(item);
       if (depends.isEmpty())
          lazyShutdownModules.remove(this);
    }
    
    /**
     * Get the importing modules
     * 
     * @param type the requirement type to filter on or null for all
     * @return the importing modules
     */
    public Collection<Module> getImportingModules(Class<? extends Requirement> type)
    {
       Set<Module> result = new HashSet<Module>();
       if (depends.isEmpty() == false)
       {
          for (RequirementDependencyItem item : depends)
          {
             Requirement requirement = item.getRequirement();
             if (type == null || type.isInstance(requirement))
                result.add(item.getModule());
          }
       }
       return result;
    }
    
    /**
     * Resolve a requirement
     * 
     * @param dependency the dependency the dependency
     * @param resolveSpace whether to resolve the module in the classloading space
     * @return the resolved name or null if not resolved
     */
    protected Module resolveModule(RequirementDependencyItem dependency, boolean resolveSpace)
    {
       ClassLoadingSpace space = getClassLoadingSpace();
       if (resolveSpace && space != null)
          space.resolve(this);
 
       Requirement requirement = dependency.getRequirement();
       return checkDomain().resolveModule(this, requirement);
    }
    
    /**
     * Release the module
     */
    public void release()
    {
       Domain domain = this.domain;
       if (domain != null)
          domain.removeModule(this);
       reset();
    }
    
    /**
     * Reset the module
     */
    public void reset()
    {
       ClassLoader classLoader = getClassLoader();
       if (classLoader != null)
          unregisterModuleClassLoader(this, classLoader);
       this.capabilities = null;
       this.requirements = null;
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       if (obj == null || obj instanceof Module == false)
          return false;
       return super.equals(obj);
    }
 
    // It is lazy shutdown so remove anything that depends upon us for a requirement
    // We can still handle it after the classloader gets unregistered
    private void enableLazyShutdown()
    {
       ControllerContext ctx = getControllerContext();
       if (ctx != null)
       {
          DependencyInfo info = ctx.getDependencyInfo();
          if (info != null)
          {
             Set<DependencyItem> items = info.getDependsOnMe(RequirementDependencyItem.class);
             if (items != null && items.isEmpty() == false)
             {
                for (DependencyItem item : items)
                   info.removeDependsOnMe(item);
             }
          }
       }
    }
 }
