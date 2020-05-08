 package org.jboss.forge.furnace.container.cdi.services;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.enterprise.inject.spi.InjectionPoint;
 
 import org.jboss.forge.furnace.addons.Addon;
 import org.jboss.forge.furnace.addons.AddonFilter;
 import org.jboss.forge.furnace.addons.AddonRegistry;
 import org.jboss.forge.furnace.proxy.ForgeProxy;
 import org.jboss.forge.furnace.proxy.Proxies;
 import org.jboss.forge.furnace.spi.ExportedInstance;
 import org.jboss.forge.furnace.spi.ServiceRegistry;
 import org.jboss.forge.furnace.util.AddonFilters;
 import org.jboss.forge.furnace.util.ClassLoaders;
 
 public class ExportedInstanceLazyLoader implements ForgeProxy
 {
    private static final AddonFilter ALL_STARTED = AddonFilters.allStarted();
    private final Class<?> serviceType;
    private final AddonRegistry registry;
    private final InjectionPoint injectionPoint;
    private Object delegate;
 
    public ExportedInstanceLazyLoader(AddonRegistry registry, Class<?> serviceType, InjectionPoint injectionPoint)
    {
       this.registry = registry;
       this.serviceType = serviceType;
       this.injectionPoint = injectionPoint;
    }
 
    public static Object create(AddonRegistry registry, InjectionPoint injectionPoint, Class<?> serviceType)
    {
       ExportedInstanceLazyLoader callback = new ExportedInstanceLazyLoader(registry, serviceType,
                injectionPoint);
       return Proxies.enhance(serviceType, callback);
    }
 
    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
    {
       try
       {
          if (thisMethod.getDeclaringClass().getName().equals(ForgeProxy.class.getName()))
          {
             // Must call from "this." or method call is not properly processed by this class.
             return this.getDelegate();
          }
       }
       catch (Exception e)
       {
       }
 
       if (delegate == null)
          delegate = loadObject();
 
       Object result;
       try
       {
          result = thisMethod.invoke(delegate, args);
       }
       catch (InvocationTargetException e)
       {
          if (e.getCause() instanceof Exception)
             throw (Exception) e.getCause();
          throw e;
       }
       return result;
    }
 
    private Object loadObject() throws Exception
    {
       Object result = null;
       for (Addon addon : registry.getAddons(ALL_STARTED))
       {
          if (ClassLoaders.containsClass(addon.getClassLoader(), serviceType))
          {
             ServiceRegistry serviceRegistry = addon.getServiceRegistry();
             if (serviceRegistry.hasService(serviceType))
             {
                ExportedInstance<?> instance = serviceRegistry.getExportedInstance(serviceType);
                if (instance != null)
                {
                   if (instance instanceof ExportedInstanceImpl)
                      // FIXME remove the need for this implementation coupling
                      result = ((ExportedInstanceImpl<?>) instance).get(new LocalServiceInjectionPoint(
                               injectionPoint,
                               serviceType));
                   else
                      result = instance.get();
 
                   if (result != null)
                      break;
                }
             }
          }
       }
 
       if (result == null)
       {
         throw new IllegalStateException("Remote service [" + serviceType.getName() + "] is not registered.");
       }
 
       return result;
    }
 
    @Override
    public Object getDelegate() throws Exception
    {
       // Delegate must be loaded here when requested. Returning null can cause some breakage in CLAC.
       if (delegate == null)
          delegate = loadObject();
       return delegate;
    }
 
 }
