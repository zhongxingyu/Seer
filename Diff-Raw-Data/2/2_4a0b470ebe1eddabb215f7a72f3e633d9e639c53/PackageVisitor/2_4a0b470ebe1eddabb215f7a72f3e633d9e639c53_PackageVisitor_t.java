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
 package org.jboss.classloading.plugins.vfs;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.classloader.spi.filter.ClassFilter;
 import org.jboss.classloading.spi.metadata.ExportAll;
 import org.jboss.virtual.VirtualFile;
 import org.jboss.virtual.VirtualFileVisitor;
 import org.jboss.virtual.VisitorAttributes;
 import org.jboss.virtual.plugins.vfs.helpers.AbstractVirtualFileFilterWithAttributes;
 
 /**
  * Visits a virtual file system recursively
  * to determine package names based on the exportAll policy
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class PackageVisitor extends AbstractVirtualFileFilterWithAttributes implements VirtualFileVisitor
 {
    /** The packages */
    private Set<String> packages = new HashSet<String>();
 
    /** The roots */
    private VirtualFile[] roots;
 
    /** The excluded roots */
    private VirtualFile[] excludedRoots;
    
    /** The current root */
    private VirtualFile root;
    
    /** The export all */
    private ExportAll exportAll;
    
    /** The root */
    private String rootPath;
    
    /** The root with slash*/
    private String rootPathWithSlash;
 
    /** The included packages */
    private ClassFilter included;
 
    /** The excluded packages */
    private ClassFilter excluded;
    
    /** The excluded export packages */
    private ClassFilter excludedExport;
    
    /**
     * Determine the packages
     * 
     * @param roots the roots
     * @param excludedRoots the excluded roots
     * @param exportAll the exportAll
     * @param included the included packages
     * @param excluded the excluded packages
     * @param excludedExport the excluded export packages
     * @return the packages
     */
    public static Set<String> determineAllPackages(VirtualFile[] roots, VirtualFile[] excludedRoots, ExportAll exportAll, ClassFilter included, ClassFilter excluded, ClassFilter excludedExport)
    {
       PackageVisitor visitor = new PackageVisitor(roots, excludedRoots, exportAll, included, excluded, excludedExport);
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
 
    /**
     * Create a new PackageVisitor.
     *
     * @param roots the vfs roots
     * @param excludedRoots the excluded roots
     * @param exportAll the export all policy
     * @param included the included packages
     * @param excluded the excluded packages
     * @param excludedExport the excluded export packages
     * @throws IllegalArgumentException for a null exportAll policy
     */
    PackageVisitor(VirtualFile[] roots, VirtualFile[] excludedRoots, ExportAll exportAll, ClassFilter included, ClassFilter excluded, ClassFilter excludedExport)
    {
       if (exportAll == null)
          throw new IllegalArgumentException("Null export policy");
       this.roots = roots;
       this.excludedRoots = excludedRoots;
       this.exportAll = exportAll;
       this.included = included;
       this.excluded = excluded;
       this.excludedExport = excludedExport; 
    }
 
    /**
     * Set the root
     * 
     * @param root the root
     * @throws IllegalArgumentException for a null root
     */
    void setRoot(VirtualFile root)
    {
       if (root == null)
          throw new IllegalArgumentException("Null root");
       this.root = root;
       rootPath = root.getPathName();
       rootPathWithSlash = rootPath + "/";
    }
    
    /**
     * Get the packages.
     * 
     * @return the packages.
     */
    Set<String> getPackages()
    {
       return packages;
    }
 
    public VisitorAttributes getAttributes()
    {
       VisitorAttributes attributes = new VisitorAttributes();
       attributes.setIncludeRoot(true);
       attributes.setRecurseFilter(this);
       return attributes;
    }
    
    public boolean accepts(VirtualFile file)
    {
       // This is our current root
       if (file.equals(root))
          return true;
      
       // Some other root, it will be handled later
       for (VirtualFile other : roots)
       {
          if (file.equals(other))
             return false;
       }
       // Is this an excluded roots?
       if (excludedRoots != null)
       {
          for (VirtualFile other : excludedRoots)
          {
             if (file.equals(other))
                return false;
          }
       }
       
       // Ok
       return true;
    }
    
    public void visit(VirtualFile file)
    {
       try
       {
          // We only want only directories
          if (file.isLeaf() == false && accepts(file))
          {
             boolean empty = true;
             // Include empty directories?
             if (exportAll == ExportAll.ALL)
                empty = false;
             else
             {
                // Determine whether there is anything there
                List<VirtualFile> children = file.getChildren();
                if (children != null && children.isEmpty() == false)
                {
                   for (VirtualFile child : children)
                   {
                      // We must have a leaf to be non-empty
                      if (child.isLeaf())
                      {
                         empty = false;
                         break;
                      }
                   }
                }
             }
             // This looks interesting
             if (empty == false)
             {
                String path = file.getPathName();
                if (path.equals(rootPath))
                   path = "";
                else if (path.startsWith(rootPathWithSlash))
                   path = path.substring(rootPathWithSlash.length());
                String pkg = path.replace('/', '.');
                
                // Check for inclusions/exclusions
                if (included != null && included.matchesPackageName(pkg) == false)
                   return;
                if (excluded != null && excluded.matchesPackageName(pkg))
                   return;
                if (excludedExport != null && excludedExport.matchesPackageName(pkg))
                   return;
                
                // Ok this is a package for export
                packages.add(pkg);
             }
          }
       }
       catch (IOException e)
       {
          throw new Error("Error visiting " + file, e);
       }
    }
 }
