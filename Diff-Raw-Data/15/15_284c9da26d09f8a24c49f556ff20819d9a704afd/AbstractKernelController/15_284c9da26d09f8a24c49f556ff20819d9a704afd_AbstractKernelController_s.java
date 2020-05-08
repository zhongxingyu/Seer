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
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.SupplyMetaData;
 import org.jboss.dependency.plugins.AbstractController;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.event.AbstractEventEmitter;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.kernel.spi.event.KernelEvent;
 import org.jboss.kernel.spi.event.KernelEventFilter;
 import org.jboss.kernel.spi.event.KernelEventListener;
 import org.jboss.kernel.spi.registry.KernelRegistry;
 import org.jboss.kernel.spi.registry.KernelRegistryEntry;
 import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
 
 /**
  * Abstract Kernel controller.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractKernelController extends AbstractController implements KernelController, KernelRegistryPlugin
 {
    /** The kernel */
    protected Kernel kernel;
 
    /** The emitter delegate */
    protected AbstractEventEmitter emitterDelegate = new AbstractEventEmitter();
 
    /** The supplies */
    protected Map<Object, List<KernelControllerContext>> suppliers = new ConcurrentHashMap<Object, List<KernelControllerContext>>();
 
    /** The contexts by class Map<Class, Set<ControllerContext>> */
    protected Map<Class, ClassContext> contextsByClass = new ConcurrentHashMap<Class, ClassContext>();
 
    /**
     * Create an abstract kernel controller
     * 
     * @throws Exception for any error
     */
    public AbstractKernelController() throws Exception
    {
    }
 
    public KernelControllerContext install(BeanMetaData metaData) throws Throwable
    {
       return install(metaData, null);
    }
 
    public KernelControllerContext install(BeanMetaData metaData, Object target) throws Throwable
    {
       KernelControllerContext context = new AbstractKernelControllerContext(null, metaData, target);
       install(context);
       return context;
    }
 
    public KernelRegistryEntry getEntry(Object name)
    {
       List<KernelControllerContext> list = suppliers.get(name);
       if (list != null && list.isEmpty() == false)
          return list.get(0);
       else if (name instanceof Class)
          return getContextByClass((Class)name);
       else
          return null;
    }
 
    public ControllerContext getContext(Object name, ControllerState state)
    {
       ControllerContext context = super.getContext(name, state);
       if (context != null)
          return context;
       if (state == null || ControllerState.INSTALLED.equals(state))
       {
          KernelRegistry registry = kernel.getRegistry();
          try
          {
             return registry.getEntry(name);
          }
          catch (Throwable ignored)
          {
          }
       }
       return null;
    }
 
    public void addSupplies(KernelControllerContext context)
    {
       BeanMetaData metaData = context.getBeanMetaData();
       Set<SupplyMetaData> supplies = metaData.getSupplies();
       if (supplies != null)
       {
          boolean trace = log.isTraceEnabled();
 
          if (supplies.isEmpty() == false)
          {
             lockWrite();
             try
             {
                for (SupplyMetaData supplied : supplies)
                {
                   Object supply = supplied.getSupply();
                   List<KernelControllerContext> list = suppliers.get(supply);
                   if (list == null)
                   {
                      list = new CopyOnWriteArrayList<KernelControllerContext>();
                      suppliers.put(supply, list);
                   }
                   list.add(context);
                   if (trace)
                      log.trace("Suppliers of " + supply + ": " + list);
                }
             }
             finally
             {
                unlockWrite();
             }
          }
       }
    }
 
    public void removeSupplies(KernelControllerContext context)
    {
       BeanMetaData metaData = context.getBeanMetaData();
       Set<SupplyMetaData> supplies = metaData.getSupplies();
       if (supplies != null)
       {
          boolean trace = log.isTraceEnabled();
 
          if (supplies.isEmpty() == false)
          {
             lockWrite();
             try
             {
                for (SupplyMetaData supplied : supplies)
                {
                   Object supply = supplied.getSupply();
                   List<KernelControllerContext> list = suppliers.get(supply);
                   if (list != null)
                   {
                      list.remove(context);
                      if (list.isEmpty())
                         suppliers.remove(supply);
                      if (trace)
                         log.trace("Suppliers of " + supply  + ": " + list);
                   }
                }
             }
             finally
             {
                unlockWrite();
             }
          }
       }
    }
 
    public Kernel getKernel()
    {
       Kernel.checkAccess();
       return kernel;
    }
 
    public void setKernel(Kernel kernel) throws Throwable
    {
       Kernel.checkConfigure();
       this.kernel = kernel;
    }
 
    public void fireKernelEvent(KernelEvent event)
    {
       emitterDelegate.fireKernelEvent(event);
    }
 
    public void registerListener(KernelEventListener listener, KernelEventFilter filter, Object handback) throws Throwable
    {
       emitterDelegate.registerListener(listener, filter, handback);
    }
 
    public void unregisterListener(KernelEventListener listener, KernelEventFilter filter, Object handback) throws Throwable
    {
       emitterDelegate.unregisterListener(listener, filter, handback);
    }
 
    /**
     * Get contexts by class.
     * This method should be taken with read lock.
     *
     * @param clazz the class type
     * @return contexts by class
     */
    protected Set<KernelControllerContext> getContexts(Class clazz)
    {
       ClassContext classContext = contextsByClass.get(clazz);
       if (classContext != null)
       {
          if (log.isTraceEnabled())
          {
             log.trace("Marking class " + clazz + " as used.");
          }
          classContext.used = true;
          return classContext.contexts;
       }
       return null;
    }
 
    /**
     * @return all instantiated contexts whose target is instance of this class clazz param
     */
    public Set<KernelControllerContext> getInstantiatedContexts(Class clazz)
    {
       lockRead();
       try
       {
         return Collections.unmodifiableSet(getContexts(clazz));
       }
       finally
       {
          unlockRead();
       }
    }
 
    public Set<KernelControllerContext> getContexts(Class clazz, ControllerState state)
    {
       lockRead();
       try
       {
          Set<KernelControllerContext> contexts = getContexts(clazz);
          if (contexts != null && contexts.isEmpty() == false)
          {
             Set<KernelControllerContext> kccs = new HashSet<KernelControllerContext>();
             List<ControllerState> states = getStates();
             int stateIndex = states.indexOf(state);
             for(KernelControllerContext context : contexts)
             {
                int contextStateIndex = states.indexOf(context.getState());
                if (contextStateIndex >= stateIndex)
                   kccs.add(context);
             }
             return Collections.unmodifiableSet(kccs);
          }
          else
             return null;
       }
       finally
       {
          unlockRead();
       }
    }
 
    public void addInstantiatedContext(KernelControllerContext context)
    {
       prepareToTraverse(context, true);
    }
 
    public void removeInstantiatedContext(KernelControllerContext context)
    {
       prepareToTraverse(context, false);
    }
 
    protected void prepareToTraverse(KernelControllerContext context, boolean addition)
    {
       lockWrite();
       try
       {
          Object target = context.getTarget();
          if (target != null)
          {
             traverseBean(context, target.getClass(), addition, log.isTraceEnabled());
          }
       }
       finally
       {
          unlockWrite();
       }
    }
 
    /**
     * Traverse over target and map it to all its superclasses
     * and interfaces - using recursion.
     *
     * @param context context whose target is instance of clazz
     * @param clazz current class to map context to
     * @param addition whether this is an addition
     * @param trace whether trace is enabled
     */
    protected void traverseBean(KernelControllerContext context, Class clazz, boolean addition, boolean trace)
    {
       if (clazz == null || clazz == Object.class)
       {
          return;
       }
       ClassContext classContext = contextsByClass.get(clazz);
       if (addition)
       {
          if (classContext == null)
          {
             classContext = new ClassContext();
             classContext.contexts = new HashSet<KernelControllerContext>();
             contextsByClass.put(clazz, classContext);
          }
          else if (classContext.used)
          {
             log.warn("Additional matching bean - contextual injection already used for class: " + clazz);
          }
          if (trace)
          {
             log.trace("Mapping contex " + context + " to class: " + clazz);
          }
          classContext.contexts.add(context);
       }
       else
       {
          if (classContext != null)
          {
             if (trace)
             {
                log.trace("Removing contex " + context + " to class: " + clazz);
             }
             classContext.contexts.remove(context);
          }
       }
       // traverse superclass
       traverseBean(context, clazz.getSuperclass(), addition, trace);
       Class[] interfaces = clazz.getInterfaces();
       // traverse interfaces
       for(Class intface : interfaces)
       {
          traverseBean(context, intface, addition, trace);
       }
    }
 
    private class ClassContext
    {
       private boolean used;
       private Set<KernelControllerContext> contexts;
    }
 
    /**
     * If zero or multiple instances match class clazz
     * a warning is issued, but no throwable is thrown
     *
     * @return context whose target is instance of this class clazz param or null if zero or multiple such instances
     */
    public KernelControllerContext getContextByClass(Class clazz)
    {
       Set<KernelControllerContext> contexts = getInstantiatedContexts(clazz);
       int numberOfMatchingBeans = 0;
       if (contexts != null)
       {
          numberOfMatchingBeans = contexts.size();
       }
 
       if (log.isTraceEnabled())
       {
          log.trace("Checking for contextual injection, current matches: " + numberOfMatchingBeans + " - " + clazz);
       }
 
       if (numberOfMatchingBeans != 1)
       {
          if (numberOfMatchingBeans > 1)
          {
             log.warn("Multiple beans match class type: " + clazz);
          }
          return null;
       }
       return contexts.iterator().next();
    }
 
 }
