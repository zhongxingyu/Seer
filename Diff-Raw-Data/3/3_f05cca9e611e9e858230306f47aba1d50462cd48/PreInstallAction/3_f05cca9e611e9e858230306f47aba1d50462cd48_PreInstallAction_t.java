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
 package org.jboss.kernel.plugins.dependency;
 
 import java.lang.annotation.Annotation;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.dependency.plugins.AbstractController;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.metadata.KernelMetaDataRepository;
 import org.jboss.metadata.plugins.loader.memory.MemoryMetaDataLoader;
 import org.jboss.metadata.spi.MutableMetaData;
 import org.jboss.metadata.spi.repository.MutableMetaDataRepository;
 import org.jboss.metadata.spi.retrieval.AnnotationItem;
 import org.jboss.metadata.spi.retrieval.AnnotationsItem;
 import org.jboss.metadata.spi.retrieval.MetaDataItem;
 import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
 import org.jboss.metadata.spi.scope.Scope;
 import org.jboss.metadata.spi.scope.ScopeFactoryLookup;
 import org.jboss.metadata.spi.scope.ScopeKey;
 
 /**
  * PreInstallAction.
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  */
 public class PreInstallAction extends KernelControllerContextAction
 {
    protected void installActionInternal(KernelControllerContext context) throws Throwable
    {
       KernelController controller = (KernelController)context.getController();
       Kernel kernel = controller.getKernel();
       KernelConfigurator configurator = kernel.getConfigurator();
 
       BeanMetaData metaData = context.getBeanMetaData();
       if (metaData.getBean() != null)
       {
          BeanInfo info = configurator.getBeanInfo(metaData);
          context.setBeanInfo(info);
 
          KernelMetaDataRepository repository = controller.getKernel().getMetaDataRepository();
          repository.addMetaData(context);
          try
          {
             applyScoping(context);
          }
          catch (Throwable t)
          {
             removeMetaData(context);
             throw t;
          }
       }
    }
 
    @SuppressWarnings("unchecked")
    protected ScopeKey getInstallScopeKey(
          KernelControllerContext context,
          KernelController controller,
          KernelMetaDataRepository repository) throws Throwable
    {
       MetaDataRetrieval retrieval = repository.getMetaDataRetrieval(context);
       if (retrieval != null)
       {
          AnnotationsItem annotations = retrieval.retrieveAnnotations();
          if (annotations != null)
          {
             AnnotationItem[] annotationItems = annotations.getAnnotations();
             if (annotationItems != null && annotationItems.length > 0)
             {
                Collection<Scope> scopes = new HashSet<Scope>();
                for (AnnotationItem annItem : annotationItems)
                {
                   Annotation annotation = annItem.getAnnotation();
                   if (annotation.annotationType().isAnnotationPresent(ScopeFactoryLookup.class))
                   {
                      ScopeFactoryLookup sfl = annotation.annotationType().getAnnotation(ScopeFactoryLookup.class);
                      Scope scope = sfl.value().newInstance().create(annotation);
                      scopes.add(scope);
                   }
                }
                if (scopes.size() > 0)
                {
                   return new ScopeKey(scopes);
                }
             }
          }
       }
       return null;
    }
 
    protected void applyScoping(KernelControllerContext context) throws Throwable
    {
       KernelController controller = (KernelController)context.getController();
       KernelMetaDataRepository repository = controller.getKernel().getMetaDataRepository();
       ScopeKey scopeKey = getInstallScopeKey(context, controller, repository);
       if (scopeKey != null)
       {
          scopeKey.freeze();
          context.setInstallScope(scopeKey);
          // todo - should this be done (repare the current context scope key)
          //        or where to store this 'deployment' key?
 /*
                   ScopeKey contextScopeKey = context.getScope();
                   for (Scope s : scopes)
                      contextScopeKey.addScope(s);
 */
          // find scoped controller
          MutableMetaDataRepository mmdr = repository.getMetaDataRepository();
          MetaDataRetrieval mdr = mmdr.getMetaDataRetrieval(scopeKey);
          if (mdr == null)
          {
             mdr = new MemoryMetaDataLoader(scopeKey);
             mmdr.addMetaDataRetrieval(mdr);
          }
          MetaDataItem<ScopedKernelController> controllerItem = mdr.retrieveMetaData(ScopedKernelController.class);
          ScopedKernelController scopedController;
          if (controllerItem != null)
          {
             scopedController = controllerItem.getValue();
          }
          else
          {
             AbstractController parentController = null;
             ScopeKey parentKey = scopeKey.getParent();
             while (parentController == null && parentKey != null)
             {
                MetaDataRetrieval pmdr = mmdr.getMetaDataRetrieval(parentKey);
                if (pmdr != null)
                {
                   MetaDataItem<ScopedKernelController> pci = pmdr.retrieveMetaData(ScopedKernelController.class);
                   if (pci != null)
                   {
                      parentController = pci.getValue();
                   }
                }
                parentKey = parentKey.getParent();
             }
             if (parentController == null)
             {
                if (controller instanceof AbstractController == false)
                   throw new IllegalArgumentException("Underlying controller not AbstractController instance!");
                parentController = (AbstractController) controller;
             }
             scopedController = new ScopedKernelController(controller.getKernel(), parentController);
             ((MutableMetaData)mdr).addMetaData(scopedController, ScopedKernelController.class);
          }
          scopedController.addControllerContext(context);
          context.setController(scopedController);
       }
    }
 
    protected void removeScoping(KernelControllerContext context) throws Throwable
    {
       KernelController controller = (KernelController)context.getController();
       KernelMetaDataRepository repository = controller.getKernel().getMetaDataRepository();
       ScopeKey scopeKey = context.getInstallScope();
       if (scopeKey != null)
       {
          // find scoped controller
          MutableMetaDataRepository mmdr = repository.getMetaDataRepository();
          MetaDataRetrieval mdr = mmdr.getMetaDataRetrieval(scopeKey);
          if (mdr == null)
          {
             throw new IllegalArgumentException("Expecting MetaDataRetrieval instance in scope: " + scopeKey);
          }
          MetaDataItem<ScopedKernelController> controllerItem = mdr.retrieveMetaData(ScopedKernelController.class);
          if (controllerItem == null)
          {
             throw new IllegalArgumentException("Expecting ScopedKernelController instance in scope:" + scopeKey);
          }
          ScopedKernelController scopedController = controllerItem.getValue();
          scopedController.removeControllerContext(context);
         context.setController(scopedController.getUnderlyingController());
          if (scopedController.isActive() == false)
          {
             try
             {
                ((MutableMetaData)mdr).removeMetaData(ScopedKernelController.class);
             }
             finally
             {
                scopedController.release();
             }
          }
       }
    }
 
    protected void uninstallActionInternal(KernelControllerContext context)
    {
       try
       {
          removeScoping(context);
       }
       catch (Throwable ignored)
       {
          log.warn("Unexpected error removing scoping: ", ignored);
       }
       finally
       {
          removeMetaData(context);
          context.setBeanInfo(null);
       }
    }
 
    /**
     * Remove any previously added metadata
     *
     * @param context the context
     */
    private void removeMetaData(KernelControllerContext context)
    {
       try
       {
          KernelController controller = (KernelController)context.getController();
          KernelMetaDataRepository repository = controller.getKernel().getMetaDataRepository();
          repository.removeMetaData(context);
       }
       catch (Throwable ignored)
       {
          log.warn("Unexpected error removing metadata: ", ignored);
       }
    }
 }
