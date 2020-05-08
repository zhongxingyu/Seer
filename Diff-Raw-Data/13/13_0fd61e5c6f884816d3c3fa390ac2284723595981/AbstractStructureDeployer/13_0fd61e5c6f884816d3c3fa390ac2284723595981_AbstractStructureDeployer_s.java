 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.deployers.plugins.structure.vfs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.deployers.plugins.structure.ClassPathInfoImpl;
 import org.jboss.deployers.plugins.structure.vfs.jar.JARCandidateStructureVisitorFactory;
 import org.jboss.deployers.spi.structure.vfs.ClassPathInfo;
 import org.jboss.deployers.spi.structure.vfs.ContextInfo;
 import org.jboss.deployers.spi.structure.vfs.StructureDeployer;
 import org.jboss.deployers.spi.structure.vfs.StructureMetaData;
 import org.jboss.deployers.spi.structure.vfs.StructuredDeployers;
 import org.jboss.logging.Logger;
 import org.jboss.virtual.VFSUtils;
 import org.jboss.virtual.VirtualFile;
 import org.jboss.virtual.VirtualFileVisitor;
 import org.jboss.virtual.VisitorAttributes;
 
 /**
  * AbstractStructureDeployer.<p>
  * 
  * We don't care about the order by default.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class AbstractStructureDeployer implements StructureDeployer
 {
    /** The log */
    protected Logger log = Logger.getLogger(getClass());
    private int relativeOrder = Integer.MAX_VALUE;
 
    /** The candidate structure visitor factory */
    private CandidateStructureVisitorFactory candidateStructureVisitorFactory = JARCandidateStructureVisitorFactory.INSTANCE;
 
    public int getRelativeOrder()
    {
       return relativeOrder;
    }
    public void setRelativeOrder(int order)
    {
       this.relativeOrder = order;
    }
    
 
    /**
     * Get the candidateStructureVisitorFactory.
     * 
     * @return the candidateStructureVisitorFactory.
     */
    public CandidateStructureVisitorFactory getCandidateStructureVisitorFactory()
    {
       return candidateStructureVisitorFactory;
    }
 
    /**
     * Set the candidateStructureVisitorFactory.
     * 
     * @param candidateStructureVisitorFactory the candidateStructureVisitorFactory.
     * @throws IllegalArgumentException for a null candidate structure
     */
    public void setCandidateStructureVisitorFactory(CandidateStructureVisitorFactory candidateStructureVisitorFactory)
    {
       if (candidateStructureVisitorFactory == null)
          throw new IllegalArgumentException("Null candidateStructureVisitorFactory");
       this.candidateStructureVisitorFactory = candidateStructureVisitorFactory;
    }
 
    public abstract boolean determineStructure(VirtualFile root,
          StructureMetaData metaData, StructuredDeployers deployers);
 
    /**
     * See if a file corresponds to a top-level deployment.
     * 
     * @param root
     * @param metaData
     * @return
     */
    public boolean isTopLevel(VirtualFile file, StructureMetaData metaData)
       throws IOException
    {
       // See if this is a top-level by checking the parent
       VirtualFile parent = file.getParent();
       String parentPath = parent != null ? parent.getPathName() : null;
       boolean isTopLevel = parentPath == null || metaData.getContext(parentPath) == null;
       return isTopLevel;
    }
 
    /**
     * Add an entry to the context classpath.
     * 
     * @param root - the root file the classpath entry should be relative to
     * @param entry - the candidate file to add as a classpath entry
     * @param includeEntry - a flag indicating if the entry should be added to
     *    the classpath
     * @param includeRootManifestCP - a flag indicating if the entry metainf
     *    manifest classpath should be included.
     * @param context - the context to populate
     * @throws IOException
     */
    protected void addClassPath(VirtualFile root, VirtualFile entry,
          boolean includeEntry, boolean includeRootManifestCP,
          ContextInfo context)
       throws IOException
    {
       // Add the manifest locations
       List<VirtualFile> paths = new ArrayList<VirtualFile>();
       if( includeEntry )
          paths.add(entry);
       String rootPath = root.getPathName();
      if( includeRootManifestCP )
       {
         VFSUtils.addManifestLocations(entry, paths);
       }
       // Add to any existing classpath
       List<ClassPathInfo> pathInfo = new ArrayList<ClassPathInfo>();
       if( context.getClassPath() != null )
          pathInfo.addAll(context.getClassPath());
       // Translate from VirtualFile to root relative paths
       for(VirtualFile vf : paths)
       {
          // Set the path relative to the root
          String cp = vf.getPathName();
          if( cp.startsWith(rootPath) )
          {
             if( cp.length() == rootPath.length() )
                cp = "";
             else
                cp = cp.substring(rootPath.length()+1);
          }
          ClassPathInfoImpl cpi = new ClassPathInfoImpl(cp);
          pathInfo.add(cpi);
       }
       context.setClassPath(pathInfo);
    }
 
    /**
     * Add all children as candidates
     * 
     * @param parent the parent context
     * @throws Exception for any error
     */
    protected void addAllChildren(VirtualFile parent, StructureMetaData metaData, StructuredDeployers deployers)
       throws Exception
    {
       addChildren(parent, metaData, deployers, null);
    }
 
    /**
     * Add all children as candidates
     * 
     * @param parent the parent context
     * @param attributes the visitor attributes uses {@link VisitorAttributes#DEFAULT} when null
     * @throws Exception for any error
     */
    protected void addChildren(VirtualFile parent, StructureMetaData metaData, StructuredDeployers deployers,
          VisitorAttributes attributes) throws Exception
    {
       if (parent == null)
          throw new IllegalArgumentException("Null parent");
       
       VirtualFileVisitor visitor = candidateStructureVisitorFactory.createVisitor(parent, metaData, deployers, attributes);
       parent.visit(visitor);
    }
 }
