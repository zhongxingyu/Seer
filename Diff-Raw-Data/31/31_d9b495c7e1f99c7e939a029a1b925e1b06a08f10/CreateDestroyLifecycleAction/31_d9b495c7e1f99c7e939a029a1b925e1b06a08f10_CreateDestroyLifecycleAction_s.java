 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.kernel.plugins.dependency;
 
 import java.util.List;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.LifecycleMetaData;
 import org.jboss.beans.metadata.spi.ParameterMetaData;
 
 /**
  * CreateDestroyLifecycleAction.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class CreateDestroyLifecycleAction extends LifecycleAction
 {
    public String getInstallMethod(BeanMetaData beanMetaData)
    {
       LifecycleMetaData lifecycle = beanMetaData.getCreate();
       if (lifecycle != null)
          return lifecycle.getMethodName();
       return null;
    }
 
    public String getDefaultInstallMethod()
    {
       return "create";
    }
 
    public String getInstallAnnotation()
    {
       return CreateLifecycle.class.getName();
    }
 
    public List<ParameterMetaData> getInstallParameters(BeanMetaData beanMetaData)
    {
       LifecycleMetaData lifecycle = beanMetaData.getCreate();
       if (lifecycle != null)
          return lifecycle.getParameters();
       return null;
    }
 
    public String getUninstallMethod(BeanMetaData beanMetaData)
    {
       LifecycleMetaData lifecycle = beanMetaData.getDestroy();
       if (lifecycle != null)
          return lifecycle.getMethodName();
       return null;
    }
 
    public String getDefaultUninstallMethod()
    {
       return "destroy";
    }
 
    public String getUninstallAnnotation()
    {
       return DestroyLifecycle.class.getName();
    }
 
    public List<ParameterMetaData> getUninstallParameters(BeanMetaData beanMetaData)
    {
       LifecycleMetaData lifecycle = beanMetaData.getDestroy();
       if (lifecycle != null)
          return lifecycle.getParameters();
       return null;
    }
 }
