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
 package org.jboss.deployers.vfs.plugins.classloader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.CodeSource;
 import java.security.PermissionCollection;
 import java.security.Policy;
 import java.security.ProtectionDomain;
 import java.security.cert.Certificate;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.jar.Manifest;
 
 import org.jboss.classloader.spi.ClassLoaderPolicy;
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.classloader.spi.PackageInformation;
 import org.jboss.classloader.spi.filter.ClassFilter;
 import org.jboss.classloader.spi.filter.FilteredDelegateLoader;
 import org.jboss.deployers.structure.spi.classloading.ExportAll;
 import org.jboss.logging.Logger;
 import org.jboss.virtual.VFSUtils;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * VFSClassLoaderPolicy.
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a>
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
    
    /** The roots */
    private VirtualFile[] roots;
 
    /** Whether to export all */
    private ExportAll exportAll;
    
    /** The exported packages */
    private String[] exportedPackages;
    
    /** The import all */
    private boolean importAll;
    
    /** Manifest cache */
    private Map<URL, Manifest> manifestCache = new ConcurrentHashMap<URL, Manifest>();
    
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
     * @throws IllegalArgumentException for null roots
     */
    public VFSClassLoaderPolicy(VirtualFile[] roots)
    {
      this(determineName(roots), roots);
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
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (roots == null)
          throw new IllegalArgumentException("Null roots");
       for (VirtualFile root : roots)
       {
          if (root == null)
             throw new IllegalArgumentException("Null root in " + Arrays.asList(roots));
       }
 
       this.name = name;
       this.roots = roots;
    }
 
    @Override
    public String getName()
    {
       return name;
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
          exportedPackages = determineAllPackages().toArray(new String[0]);
       else
          exportedPackages = null;
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
    public String[] getPackageNames()
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
    protected DelegateLoader getExported()
    {
       if (getExportAll() != null)
          return new FilteredDelegateLoader(this, ClassFilter.EVERYTHING);
       return super.getExported();
    }
 
    @Override
    public URL getResource(String path)
    {
       VirtualFile child = findChild(path);
       if (child != null)
       {
          try
          {
             return child.toURL();
          }
          catch (Exception ignored)
          {
             log.trace("Error determining URL for " + child, ignored);
             return null;
          }
       }
       return null;
    }
 
    @Override
    public InputStream getResourceAsStream(String path)
    {
       VirtualFile child = findChild(path);
       if (child != null)
       {
          try
          {
             return child.openStream();
          }
          catch (Exception ignored)
          {
             log.trace("Error opening stream for " + child, ignored);
             return null;
          }
       }
       return null;
    }
 
    @Override
    public void getResources(String name, Set<URL> urls) throws IOException
    {
       for (VirtualFile root : roots)
       {
          try
          {
             VirtualFile child = root.findChild(name);
             urls.add(child.toURL());
          }
          catch (Exception ignored)
          {
             log.trace("Error getting resources for " + root, ignored);
          }
       }
    }
 
    /**
     * Find a child from a path
     * 
     * @param path the path
     * @return the child if found in the roots
     */
    protected VirtualFile findChild(String path)
    {
       for (VirtualFile root : roots)
       {
          try
          {
             return root.findChild(path);
          }
          catch (Exception ignored)
          {
             // not found
          }
       }
       return null;
    }
 
    /**
     * Find a root from a path
     * 
     * @param path the path
     * @return the root if found in the roots
     */
    protected VirtualFile findRoot(String path)
    {
       for (VirtualFile root : roots)
       {
          try
          {
             if (root.findChild(path) != null)
                return root;
          }
          catch (Exception ignored)
          {
             // not found
          }
       }
       return null;
    }
    
    @Override
    public PackageInformation getPackageInformation(String packageName)
    {
       String path = packageName.replace('.', '/');
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
          VirtualFile root = clazz.getVFS().getRoot();
          URL codeSourceURL = root.toURL();
          Certificate[] certs = null; // TODO JBMICROCONT-182 determine certificates
          CodeSource cs = new CodeSource(codeSourceURL, certs);
          PermissionCollection permissions = Policy.getPolicy().getPermissions(cs);
          return new ProtectionDomain(cs, permissions);
       }
       catch (Exception e)
       {
          throw new Error("Error determining protection domain for " + clazz, e);
       }
    }
 
    /**
     * Determine all the packages
     * 
     * @return the packages
     * @throws IllegalArgumentException if there is no exportAll policy
     */
    protected Set<String> determineAllPackages()
    {
       PackageVisitor visitor = new PackageVisitor(exportAll);
       for (VirtualFile root : roots)
       {
          try
          {
             visitor.setRoot(root);
             root.visit(visitor);
          }
          catch (Exception e)
          {
             throw new Error("Error visiting " + root, e);
          }
       }
       return visitor.getPackages();
    }
 }
