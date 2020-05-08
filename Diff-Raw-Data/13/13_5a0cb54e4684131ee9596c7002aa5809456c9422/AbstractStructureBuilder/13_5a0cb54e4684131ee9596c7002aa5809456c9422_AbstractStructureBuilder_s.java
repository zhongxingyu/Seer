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
 package org.jboss.deployers.structure.spi.helpers;
 
 import java.lang.reflect.Field;
 import java.util.Comparator;
 import java.util.List;
 
 import org.jboss.deployers.client.spi.Deployment;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.DeploymentState;
 import org.jboss.deployers.spi.attachments.Attachments;
 import org.jboss.deployers.spi.structure.ContextInfo;
 import org.jboss.deployers.spi.structure.StructureMetaData;
 import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
 import org.jboss.deployers.structure.spi.DeploymentContext;
 import org.jboss.deployers.structure.spi.StructureBuilder;
 import org.jboss.logging.Logger;
 
 /**
  * AbstractStructureBuilder.
  * 
  * @author <a href="adrian@jboss.org">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class AbstractStructureBuilder implements StructureBuilder
 {
    /** The log */
    private final Logger log = Logger.getLogger(getClass());
    
    public DeploymentContext populateContext(Deployment deployment, StructureMetaData metaData) throws DeploymentException
    {
       if (deployment == null)
          throw new IllegalArgumentException("Null deployment");
 
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Populating deployment " + deployment.getName() + " metaData=" + metaData);
       
      DeploymentContext result = null;
       try
       {
          result = createRootDeploymentContext(deployment);
          if (result == null)
             throw new IllegalStateException("Root deployment context is null");
          result.setDeployment(deployment);
 
          ContextInfo contextInfo = metaData.getContext("");
          if (contextInfo == null)
             contextInfo = StructureMetaDataFactory.createContextInfo("", null);
          contextInfo.setPredeterminedManagedObjects(deployment.getPredeterminedManagedObjects());
          applyContextInfo(result, contextInfo);
       }
       catch (Throwable t)
       {
          throw DeploymentException.rethrowAsDeploymentException("Error populating deployment " + deployment.getName(), t);
       }
       
       try
       {
         if (metaData != null)
         {
            result.getTransientManagedObjects().addAttachment(StructureMetaData.class, metaData);
            populateContext(result, metaData);
         }
       }
       catch (Throwable t)
       {
          result.setProblem(t);
          result.setState(DeploymentState.ERROR);
          log.warn("Error populating deployment " + deployment.getName(), t);
       }
       return result;
    }
 
    /**
     * Populate the child deployment contexts
     * 
     * @param context the context
     * @param metaData the metadata
     * @throws Exception for any error
     */
    protected void populateContext(DeploymentContext context, StructureMetaData metaData) throws Exception
    {
       if (context == null)
          throw new IllegalArgumentException("Null context");
       if (metaData == null)
          throw new IllegalArgumentException("Null metaData");
       
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Populating Context " + context.getName() + " metaData=" + metaData);
       
       List<ContextInfo> contexts = metaData.getContexts();
       if (contexts == null)
          return;
       
       try
       {
          for (ContextInfo child : contexts)
          {
             // Only process the child contexts
             if ("".equals(child.getPath()) == false)
             {
                DeploymentContext childContext = createChildDeploymentContext(context, child);
                if (childContext == null)
                   throw new IllegalStateException("Child deployment context is null");
 
                context.addChild(childContext);
                childContext.setParent(context);
 
                applyContextInfo(childContext, child);
 
                Attachments attachments = child.getPredeterminedManagedObjects(); 
                if (attachments != null)
                {
                   StructureMetaData childStructure = attachments.getAttachment(StructureMetaData.class);
                   if (childStructure != null)
                      populateContext(childContext, childStructure);
                }
             }
          }
       }
       catch (Throwable t)
       {
          throw DeploymentException.rethrowAsDeploymentException("Error populating context " + context.getName(), t);
       }
    }
 
    /**
     * Apply the context info. This transfers the PredeterminedManagedObjects
     * and TransientManagedObjects and other information from the ContextInfo to the DeploymentContext.
     * 
     * @param context the context
     * @param contextInfo the contextInfo
     * @throws Exception for any error
     */
    protected void applyContextInfo(DeploymentContext context, ContextInfo contextInfo) throws Exception
    {
       Attachments attachments = contextInfo.getPredeterminedManagedObjects();
       if (attachments != null)
          context.setPredeterminedManagedObjects(attachments);
 
       context.setRelativeOrder(contextInfo.getRelativeOrder());
       applyComparator(context, contextInfo);
    }
 
    /**
     * Try to apply the comparator
     * 
     * @param context the context
     * @param contextInfo the contextInfo
     * @throws Exception for any error
     */
    @SuppressWarnings("unchecked")
    protected void applyComparator(DeploymentContext context, ContextInfo contextInfo) throws Exception
    {
       String className = contextInfo.getComparatorClassName();
       if (className == null)
          return;
       Object o = null;
       try
       {
          Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
          try
          {
             Field field = clazz.getField("INSTANCE");
             o = field.get(null);
          }
          catch (NoSuchFieldException ignored)
          {
          }
          if (o == null)
             o = clazz.newInstance();
          Comparator comparator = Comparator.class.cast(o);
          context.setComparator(comparator);
       }
       catch (Throwable t)
       {
          log.warn("Unable to load/set comparator: " + className);
       }
    }
 
    /**
     * Create the root deployment context
     * 
     * @param deployment the deployment
     * @return the deployment context
     * @throws Exception for any error
     */
    protected DeploymentContext createRootDeploymentContext(Deployment deployment) throws Exception
    {
       return new AbstractDeploymentContext(deployment.getName(), "");
    }
    
    /**
     * Create a child deployment context
     * 
     * @param parent the parent deployment context
     * @param child the child context
     * @return the deployment context
     * @throws Exception for any error
     */
    protected DeploymentContext createChildDeploymentContext(DeploymentContext parent, ContextInfo child) throws Exception
    {
       String path = child.getPath();
       String name = parent.getName() + "/" + path;
       return new AbstractDeploymentContext(name, path); 
    }
 }
