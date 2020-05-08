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
 
 import org.jboss.deployers.plugins.structure.AbstractDeploymentContext;
 import org.jboss.deployers.plugins.structure.vfs.jar.JARCandidateStructureVisitor;
 import org.jboss.deployers.spi.structure.DeploymentContext;
 import org.jboss.logging.Logger;
 import org.jboss.virtual.VirtualFile;
 import org.jboss.virtual.VirtualFileFilter;
 import org.jboss.virtual.VisitorAttributes;
 import org.jboss.virtual.plugins.vfs.helpers.AbstractVirtualFileVisitor;
 
 /**
  * Visits the structure and creates candidates
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class AbstractCandidateStructureVisitor extends AbstractVirtualFileVisitor
 {
    /** The log */
   private static final Logger log = Logger.getLogger(AbstractCandidateStructureVisitor.class);
 
    /** The parent deployment context */
    private final DeploymentContext parent;
 
    /** The meta data location */
    private final String metaDataPath;
 
    /** Ignore directories */
    private boolean ignoreDirectories;
 
    /** A filter */
    private VirtualFileFilter filter;
    
    /**
     * Create a new CandidateStructureVisitor.
     * 
     * @param parent the parent
     * @throws IllegalArgumentException for a null parent
     */
    public AbstractCandidateStructureVisitor(DeploymentContext parent)
    {
       this(parent, null);
    }
    
    /**
     * Create a new CandidateStructureVisitor.
     * 
     * @param parent the parent
     * @param attributes the attributes
     * @throws IllegalArgumentException for a null parent
     */
    public AbstractCandidateStructureVisitor(DeploymentContext parent, VisitorAttributes attributes)
    {
       super(attributes);
       if (parent == null)
          throw new IllegalArgumentException("Null parent");
       this.parent = parent;
       VirtualFile metaDataLocation = parent.getMetaDataLocation();
       if (metaDataLocation != null)
          metaDataPath = metaDataLocation.getPathName(); 
       else
          metaDataPath = null;
    }
    
    /**
     * Get the parent deployment context
     * 
     * @return the parent.
     */
    public DeploymentContext getParent()
    {
       return parent;
    }
 
    /**
     * Get the ignoreDirectories.
     * 
     * @return the ignoreDirectories.
     */
    public boolean isIgnoreDirectories()
    {
       return ignoreDirectories;
    }
 
    /**
     * Get the filter.
     * 
     * @return the filter.
     */
    public VirtualFileFilter getFilter()
    {
       return filter;
    }
 
    /**
     * Set the filter.
     * 
     * @param filter the filter.
     */
    public void setFilter(VirtualFileFilter filter)
    {
       this.filter = filter;
    }
 
    /**
     * Set the ignoreDirectories.
     * 
     * @param ignoreDirectories the ignoreDirectories.
     */
    public void setIgnoreDirectories(boolean ignoreDirectories)
    {
       this.ignoreDirectories = ignoreDirectories;
    }
 
    public void visit(VirtualFile virtualFile)
    {
       DeploymentContext candidate = createCandidate(virtualFile);
       if (candidate != null)
          parent.addChild(candidate);
    }
 
    /**
     * Create a new candidate deployment context
     * 
     * @param virtualFile the virtual file
     * @return the candidate or null if it is not a candidate
     */
    protected DeploymentContext createCandidate(VirtualFile virtualFile)
    {
       // Exclude the meta data location
       if (metaDataPath != null && virtualFile.getPathName().startsWith(metaDataPath))
          return null;
 
       // Ignore directories that are not archives when asked
       try
       {
          if (ignoreDirectories && virtualFile.isLeaf() == false && virtualFile.isArchive() == false)
             return null;
       }
       catch (IOException e)
       {
          log.debug("Ignoring " + virtualFile + " reason=" + e);
          return null;
       }
       
       // Apply any filter
       if (filter != null && filter.accepts(virtualFile) == false)
          return null;
       
       return new AbstractDeploymentContext(virtualFile, true, parent);
    }
 }
