 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.aop.microcontainer.integration;
 
 import java.util.Set;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.dependency.plugins.AbstractDependencyItem;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyInfo;
 import org.jboss.dependency.spi.DependencyItem;
 import org.jboss.kernel.spi.dependency.DependencyBuilderListItem;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.logging.Logger;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 class AspectDependencyBuilderListItem implements DependencyBuilderListItem
 {
    protected static Logger log = Logger.getLogger(AspectDependencyBuilderListItem.class);
    
    /**
     * The name of our dependency
     */
    protected String dependencyName;
    
    AspectDependencyBuilderListItem(String name)
    {
       this.dependencyName = name; 
    }
    
    public void addDependency(KernelControllerContext context)
    {
       BeanMetaData metaData = context.getBeanMetaData();
       DependencyItem dependencyItem = new AbstractDependencyItem(metaData.getName(), dependencyName, ControllerState.INSTANTIATED, ControllerState.INSTALLED);
       DependencyInfo depends = context.getDependencyInfo();
       depends.addIDependOn(dependencyItem);
    }
    
    public void removeDependency(KernelControllerContext context)
    {
       DependencyInfo depends = context.getDependencyInfo();
 
       Set<DependencyItem> items = depends.getIDependOn(null);
       if (items.size() > 0)
       {
          for (DependencyItem item : items)
          {
             try
             {
               Object iDependOn = item.getIDependOn();
               if (iDependOn != null && iDependOn.equals(dependencyName))
                {
                   depends.removeIDependOn(item);
                }
             }
             catch (RuntimeException e)
             {
                log.warn("Problem uninstalling dependency " + dependencyName + " for " + context, e);
             }
          }
       }
    }
 
    public boolean equals(Object o)
    {
       if (o instanceof AspectDependencyBuilderListItem)
       {
          return dependencyName.equals(((AspectDependencyBuilderListItem)o).dependencyName);
       }
       return false;
    }
    
    public int hashCode()
    {
       return dependencyName.hashCode();
    }
 }
