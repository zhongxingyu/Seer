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
 package org.jboss.classloading.spi.vfs.policy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.CodeSource;
 import java.security.Permission;
 import java.security.PermissionCollection;
 import java.security.Policy;
 import java.security.ProtectionDomain;
 import java.security.cert.Certificate;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.jar.Manifest;
 
 import org.jboss.classloader.plugins.ClassLoaderUtils;
 import org.jboss.classloader.spi.ClassLoaderPolicy;
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.classloader.spi.PackageInformation;
 import org.jboss.classloader.spi.filter.ClassFilter;
 import org.jboss.classloader.spi.filter.FilteredDelegateLoader;
 import org.jboss.classloading.plugins.vfs.PackageVisitor;
 import org.jboss.classloading.spi.metadata.ExportAll;
 import org.jboss.logging.Logger;
 import org.jboss.util.collection.SoftValueHashMap;
 import org.jboss.virtual.VFSUtils;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * VFSClassLoaderPolicy.
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a> 
  * @author <a href="ales.justin@jboss.org">Ales Justin</a>
  * @author <a href="anil.saldhana@jboss.org">Anil Saldhana</a>
  * @version $Revision: 1.1 $
  */
 public class VFSClassLoaderPolicy extends ClassLoaderPolicy
 {
    /** The log */
    private static Logger log = Logger.getLogger(VFSClassLoaderPolicy.class);
 
    /** Tag for no manifest */
    private static final Manifest NO_MANIFEST = new Manifest();
 
    /** A name for the policy */
    private String name;
    
    /** The delegates */
    private List<? extends DelegateLoader> delegates;
    
    /** The roots */
    private VirtualFile[] roots;
    
    /** The excluded roots */
    private VirtualFile[] excludedRoots;
 
    /** Whether to export all */
    private ExportAll exportAll;
 
    /** The included */
    private ClassFilter included;
 
    /** The excluded */
    private ClassFilter excluded;
    
    /** The excluded for export */
    private ClassFilter excludedExport;
    
    /** The exported packages */
    private String[] exportedPackages;
 
    /** The import all */
    private boolean importAll;
 
    /** Whether we are cachable */
    private boolean cacheable = true;
    
    /** Whether we are blacklistable */
    private boolean blackListable = true;
    
    /** Manifest cache */
    private Map<URL, Manifest> manifestCache = new ConcurrentHashMap<URL, Manifest>();
    
    /** Cache of virtual file information by path */
    @SuppressWarnings("unchecked")
    private Map<String, VirtualFileInfo> vfsCache = Collections.synchronizedMap(new SoftValueHashMap());
    
    /** A generator that is capable of providing Java Security Manager friendly CodeSource */
    private CodeSourceGenerator codeSourceGenerator = DefaultCodeSourceGenerator.INSTANCE;
 
    /** Code source permission */
    private static final Permission csgPermission = new RuntimePermission(VFSClassLoaderPolicy.class.getName() + ".setCodeSourceGenerator");
 
    /**
     * Determine a name from the roots
     * 
     * @param roots the roots
     * @return the name
     */
    private static String determineName(VirtualFile[] roots)
    {
       if (roots == null)
          return "";
       
       try
       {
          for (VirtualFile root : roots)
             return root.toURL().toString();
       }
       catch (Exception ignored)
       {
       }
       return "";
    }
    
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param roots the roots
     * @return the classloader policy
     * @throws IllegalArgumentException for null roots
     */
    public static VFSClassLoaderPolicy createVFSClassLoaderPolicy(VirtualFile... roots)
    {
       return new VFSClassLoaderPolicy(roots);
    }
    
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param name a name of the policy
     * @param roots the roots
     * @return the classloader policy
     * @throws IllegalArgumentException for null roots
     */
    public static VFSClassLoaderPolicy createVFSClassLoaderPolicy(String name, VirtualFile... roots)
    {
       return new VFSClassLoaderPolicy(name, roots);
    }
    
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param roots the roots
     * @param excludedRoots the excluded roots
     * @return the classloader policy
     * @throws IllegalArgumentException for null roots
     */
    public static VFSClassLoaderPolicy createVFSClassLoaderPolicy(VirtualFile[] roots, VirtualFile[] excludedRoots)
    {
       return new VFSClassLoaderPolicy(roots, excludedRoots);
    }
    
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param name a name of the policy
     * @param roots the roots
     * @param excludedRoots the excluded roots
     * @return the classloader policy
     * @throws IllegalArgumentException for null roots
     */
    public static VFSClassLoaderPolicy createVFSClassLoaderPolicy(String name, VirtualFile[] roots, VirtualFile[] excludedRoots)
    {
       return new VFSClassLoaderPolicy(name, roots, excludedRoots);
    }
    
 
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param roots the roots
     * @throws IllegalArgumentException for null roots
     */
    public VFSClassLoaderPolicy(VirtualFile[] roots)
    {
       this(determineName(roots), roots);
    }
 
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param roots the roots
     * @param excludedRoots the excluded roots
     * @throws IllegalArgumentException for null roots
     */
    public VFSClassLoaderPolicy(VirtualFile[] roots, VirtualFile[] excludedRoots)
    {
       this(determineName(roots), roots, excludedRoots);
    }
 
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param name the name
     * @param roots the roots
     * @throws IllegalArgumentException for null roots
     */
    public VFSClassLoaderPolicy(String name, VirtualFile[] roots)
    {
       this(name, roots, null);
    }
 
    /**
     * Create a new VFSClassLoaderPolicy.
     * 
     * @param name the name
     * @param roots the roots
     * @param excludedRoots the excluded roots
     * @throws IllegalArgumentException for null roots
     */
    public VFSClassLoaderPolicy(String name, VirtualFile[] roots, VirtualFile[] excludedRoots)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (roots == null)
          throw new IllegalArgumentException("Null roots");
       for (VirtualFile root : roots)
       {
          if (root == null)
             throw new IllegalArgumentException("Null root in " + Arrays.asList(roots));
       }
       if (excludedRoots != null)
       {
          for (VirtualFile excludedRoot : excludedRoots)
          {
             if (excludedRoot == null)
                throw new IllegalArgumentException("Null excluded root in " + Arrays.asList(excludedRoots));
          }
       }
 
       this.name = name;
       this.roots = roots;
       this.excludedRoots = excludedRoots;
    }
 
    @Override
    public String getName()
    {
       return name;
    }
 
    @Override
    public List<? extends DelegateLoader> getDelegates()
    {
       return delegates;
    }
 
    /**
     * Set the delegates.
     * 
     * @param delegates the delegates.
     */
    public void setDelegates(List<? extends DelegateLoader> delegates)
    {
       this.delegates = delegates;
    }
 
    /**
     * Get the included.
     * 
     * @return the included.
     */
    public ClassFilter getIncluded()
    {
       return included;
    }
 
    /**
     * Set the included.
     * 
     * @param included the included.
     */
    public void setIncluded(ClassFilter included)
    {
       this.included = included;
    }
 
    /**
     * Get the excluded.
     * 
     * @return the excluded.
     */
    public ClassFilter getExcluded()
    {
       return excluded;
    }
 
    /**
     * Set the excluded.
     * 
     * @param excluded the excluded.
     */
    public void setExcluded(ClassFilter excluded)
    {
       this.excluded = excluded;
    }
 
    /**
     * Get the excludedExport.
     * 
     * @return the excludedExport.
     */
    public ClassFilter getExcludedExport()
    {
       return excludedExport;
    }
 
    /**
     * Set the excludedExport.
     * 
     * @param excludedExport the excludedExport.
     */
    public void setExcludedExport(ClassFilter excludedExport)
    {
       this.excludedExport = excludedExport;
    }
 
    /**
     * Get the exportAll.
     * 
     * @return the exportAll.
     */
    public ExportAll getExportAll()
    {
       return exportAll;
    }
 
    /**
     * Set the exportAll.
     * 
     * @param exportAll the exportAll.
     */
    public void setExportAll(ExportAll exportAll)
    {
       this.exportAll = exportAll;
       if (exportAll != null)
       {
          if (exportedPackages == null)
          {
             Set<String> exported = PackageVisitor.determineAllPackages(roots, excludedRoots, exportAll, included, excluded, excludedExport);
             exportedPackages = exported.toArray(new String[exported.size()]);
          }
       }
    }
 
    /**
     * Get the exported packages
     * 
     * @return the exported packages
     */
    public String[] getExportedPackages()
    {
       return exportedPackages;
    }
 
    /**
     * Set the exportedPackages.
     * 
     * @param exportedPackages the exportedPackages.
     */
    public void setExportedPackages(String[] exportedPackages)
    {
       this.exportedPackages = exportedPackages;
    }
 
    @Override
    public String[] getPackageNames()
    {
       return exportedPackages;
    }
 
    @Override
    public boolean isImportAll()
    {
       return importAll;
    }
 
    /**
     * Set the importAll.
     * 
     * @param importAll the importAll.
     */
    public void setImportAll(boolean importAll)
    {
       this.importAll = importAll;
    }
 
    @Override
    public boolean isCacheable()
    {
       return cacheable;
    }
 
    /**
     * Set the cacheable.
     * 
     * @param cacheable the cacheable.
     */
    public void setCacheable(boolean cacheable)
    {
       this.cacheable = cacheable;
    }
 
    @Override
    public boolean isBlackListable()
    {
       return blackListable;
    }
 
    /**
     * Set the blackListable.
     * 
     * @param blackListable the blackListable.
     */
    public void setBlackListable(boolean blackListable)
    {
       this.blackListable = blackListable;
    }
 
    @Override
    public DelegateLoader getExported()
    {
       if (getExportAll() != null)
          return new FilteredDelegateLoader(this, ClassFilter.EVERYTHING);
       return super.getExported();
    }
 
    @Override
    public URL getResource(String path)
    {
       if (checkFilters(path, "getResource"))
          return null;
       
       VirtualFile child = findChild(path);
       if (child != null)
       {
          try
          {
             return child.toURL();
          }
          catch (Exception ignored)
          {
             log.debug("Error determining URL for " + child, ignored);
             return null;
          }
       }
       return null;
    }
    
    @Override
    public InputStream getResourceAsStream(String path)
    {
       if (checkFilters(path, "getResourceAsStream"))
          return null;
 
       VirtualFile child = findChild(path);
       if (child != null)
       {
          try
          {
             return child.openStream();
          }
          catch (Exception ignored)
          {
             log.debug("Error opening stream for " + child, ignored);
             return null;
          }
       }
       return null;
    }
 
    @Override
    public void getResources(String name, Set<URL> urls) throws IOException
    {
       if (checkFilters(name, "getResources"))
          return;
 
       for (VirtualFile root : roots)
       {
          try
          {
             VirtualFile child = root.getChild(name);
             if (child != null)
                urls.add(child.toURL());
          }
          catch (Exception e)
          {
             log.debug("Error getting resources for " + root, e);
          }
       }
    }
 
    /**
     * Set a CodeSource Generator.
     *
     * @param csg the code soruce generator
     */
    public void setCodeSourceGenerator(final CodeSourceGenerator csg)
    {
       if (csg == null)
          throw new IllegalArgumentException("Null code source generator.");
 
       SecurityManager sm = System.getSecurityManager();
       if(sm != null)
          sm.checkPermission(csgPermission);
       
       codeSourceGenerator = csg; 
    }
    
    /**
     * Find a child from a path
     * 
     * @param path the path
     * @return the child if found in the roots
     */
    protected VirtualFile findChild(String path)
    {
       VirtualFileInfo vfi = findVirtualFileInfo(path);
       if (vfi == null)
          return null;
       return vfi.getFile();
    }
 
    /**
     * Find a root from a path
     * 
     * @param path the path
     * @return the root if found in the roots
     */
    protected VirtualFile findRoot(String path)
    {
       VirtualFileInfo vfi = findVirtualFileInfo(path);
       if (vfi == null)
          return null;
       return vfi.getRoot();
    }
 
    /**
     * Find the virtual file information for a path
     * 
     * @param path the path
     * @return the virtual file information
     */
    protected VirtualFileInfo findVirtualFileInfo(String path)
    {
       VirtualFileInfo result = vfsCache.get(path);
       if (result != null)
          return result;
       
       for (VirtualFile root : roots)
       {
          try
          {
             VirtualFile file = root.getChild(path);
             if (file != null)
             {
                result = new VirtualFileInfo(file, root);
                vfsCache.put(path, result);
                return result;
             }
          }
          catch (Exception ignored)
          {
          }
       }
       return null;
    }
    
    @Override
    public PackageInformation getClassPackageInformation(String className, String packageName)
    {
       String path = ClassLoaderUtils.classNameToPath(className);
       VirtualFile root = findRoot(path);
       Manifest manifest = null;
       URL rootURL = null;
       if (root != null)
       {
          try
          {
             rootURL = root.toURL();
             manifest = manifestCache.get(rootURL);
             if (manifest == null)
             {
                manifest = VFSUtils.getManifest(root);
                if (manifest == null)
                   manifestCache.put(rootURL, NO_MANIFEST);
                else
                   manifestCache.put(rootURL, manifest);
             }
             
             if (manifest == NO_MANIFEST)
                manifest = null;
          }
          catch (Exception ignored)
          {
             if (log.isTraceEnabled())
                log.trace("Unable to retrieve manifest for " + path + " url=" + rootURL + " error="  + ignored.getMessage());
          }
       }
       return new PackageInformation(packageName, manifest);
    }
 
    @Override
    protected void toLongString(StringBuilder builder)
    {
       builder.append(" roots=").append(Arrays.asList(roots)).append(" ");
       super.toLongString(builder);
       if (exportAll != null)
          builder.append(exportAll);
    }
 
    @Override
    protected ProtectionDomain getProtectionDomain(String className, String path)
    {
       VirtualFile clazz = findChild(path);
       if (clazz == null)
       {
          log.trace("Unable to determine class file for " + className);
          return null;
       }
       try
       {
          VirtualFile root = findRoot(path);
          URL codeSourceURL = root.toURL();  
          
         Certificate[] certs = null; // TODO JBCL-67 determine certificates
          CodeSource cs = codeSourceGenerator.getCodeSource(codeSourceURL, certs);
          PermissionCollection permissions = Policy.getPolicy().getPermissions(cs);
          return new ProtectionDomain(cs, permissions);
       }
       catch (Exception e)
       {
          throw new Error("Error determining protection domain for " + clazz, e);
       }
    }
    
    /**
     * Check the filters
     * 
     * @param path the path to check
     * @param context the context
     * @return true if it fails the filters
     */
    protected boolean checkFilters(String path, String context)
    {
       if (included != null && included.matchesResourcePath(path) == false)
       {
          if (log.isTraceEnabled())
             log.trace(this + " " + context + " path=" + path + " doesn't match include filter: " + included);
          return true;
       }
       if (excluded != null && excluded.matchesResourcePath(path))
       {
          if (log.isTraceEnabled())
             log.trace(this + " " + context + "  path=" + path + " matches exclude filter: " + excluded);
          return true;
       }
       return false;
    }
 
    /**
     * VirtualFileInfo.    */
    private static class VirtualFileInfo
    {
       /** The file */
       private VirtualFile file;
       
       /** The root */
       private VirtualFile root;
       
       public VirtualFileInfo(VirtualFile file, VirtualFile root)
       {
          this.file = file;
          this.root = root;
       }
 
       /**
        * Get the file.
        * 
        * @return the file.
        */
       public VirtualFile getFile()
       {
          return file;
       }
 
       /**
        * Get the root.
        * 
        * @return the root.
        */
       public VirtualFile getRoot()
       {
          return root;
       }
    }
 }
