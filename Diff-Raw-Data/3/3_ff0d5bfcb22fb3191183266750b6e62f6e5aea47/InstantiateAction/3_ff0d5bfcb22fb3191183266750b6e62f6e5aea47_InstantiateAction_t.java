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
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.dependency.spi.DependencyInfo;
 import org.jboss.dependency.spi.ScopeInfo;
 import org.jboss.joinpoint.spi.Joinpoint;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.dependency.KernelControllerContextAware;
 import org.jboss.kernel.spi.dependency.InstantiateKernelControllerContextAware;
 import org.jboss.kernel.spi.metadata.KernelMetaDataRepository;
 import org.jboss.metadata.spi.scope.ScopeKey;
 import org.jboss.metadata.spi.scope.CommonLevels;
 
 /**
  * InstantiateAction.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class InstantiateAction extends AnnotationsAction
 {
    @SuppressWarnings("deprecation")
    protected void installActionInternal(KernelControllerContext context) throws Throwable
    {
       KernelController controller = (KernelController) context.getController();
       Kernel kernel = controller.getKernel();
       KernelConfigurator configurator = kernel.getConfigurator();
 
       BeanMetaData metaData = context.getBeanMetaData();
       BeanInfo info = context.getBeanInfo();
       final Joinpoint joinPoint = configurator.getConstructorJoinPoint(info, metaData.getConstructor(), metaData);
 
       Object object = dispatchJoinPoint(context, joinPoint);
       if (object == null)
          throw new IllegalStateException("Instantiate joinpoint returned a null object: " + joinPoint);
       context.setTarget(object);
 
       try
       {
          if (info == null)
          {
             info = configurator.getBeanInfo(object.getClass(), metaData.getAccessMode());
             context.setBeanInfo(info);
 
             // update class scope with class info
             KernelMetaDataRepository repository = kernel.getMetaDataRepository();
             // remove old context
             repository.removeMetaData(context);
             // create new scope key
            ScopeInfo scopeInfo = context.getScopeInfo();
             ScopeKey scopeKey = new ScopeKey(scopeInfo.getScope().getScopes());
             scopeKey.addScope(CommonLevels.CLASS, info.getClassInfo().getType());
             scopeInfo.setScope(scopeKey);
             // re-register
             repository.addMetaData(context);
 
             // handle custom annotations
             applyAnnotations(context);
          }
 
          DependencyInfo dependencyInfo = context.getDependencyInfo();
          if (dependencyInfo != null && dependencyInfo.isAutowireCandidate())
             controller.addInstantiatedContext(context);
       }
       catch (Throwable t)
       {
          uninstall(context);
          throw t;
       }
    }
 
    protected void uninstallActionInternal(KernelControllerContext context)
    {
       try
       {
          Object object = context.getTarget();
          if (object != null)
          {
             KernelController controller = (KernelController) context.getController();
             DependencyInfo dependencyInfo = context.getDependencyInfo();
             if (dependencyInfo != null && dependencyInfo.isAutowireCandidate())
                controller.removeInstantiatedContext(context);
          }
       }
       catch (Throwable ignored)
       {
          log.debug("Ignored error unsetting context ", ignored);
       }
       finally
       {
          context.setTarget(null);
       }
    }
 
    protected ControllerState getState()
    {
       return ControllerState.INSTANTIATED;
    }
 
    protected Class<? extends KernelControllerContextAware> getActionAwareInterface()
    {
       return InstantiateKernelControllerContextAware.class;
    }
 }
