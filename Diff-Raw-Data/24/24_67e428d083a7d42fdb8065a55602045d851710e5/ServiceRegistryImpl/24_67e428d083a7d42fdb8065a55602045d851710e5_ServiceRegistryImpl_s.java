 package org.jboss.forge.furnace.container.cdi.impl;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import java.util.concurrent.Callable;
 import java.util.logging.Logger;
 
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.inject.Qualifier;
 
 import org.jboss.forge.furnace.addons.Addon;
 import org.jboss.forge.furnace.container.cdi.services.ExportedInstanceImpl;
 import org.jboss.forge.furnace.exception.ContainerException;
 import org.jboss.forge.furnace.lock.LockManager;
 import org.jboss.forge.furnace.spi.ExportedInstance;
 import org.jboss.forge.furnace.spi.ServiceRegistry;
 import org.jboss.forge.furnace.util.Addons;
 import org.jboss.forge.furnace.util.Assert;
 import org.jboss.forge.furnace.util.ClassLoaders;
 
 public class ServiceRegistryImpl implements ServiceRegistry
 {
    private final Class<?>[] services;
 
    private final BeanManager manager;
 
    private final Addon addon;
 
    private static final Logger log = Logger.getLogger(ServiceRegistryImpl.class.getName());
 
    @SuppressWarnings("unused")
    private final LockManager lock;
 
    private final Set<Class<?>> servicesSet;
 
   private final Map<Integer, Class<?>> classCache = new WeakHashMap<>();
   private final Map<Integer, ExportedInstance<?>> instanceCache = new WeakHashMap<>();
   private final Map<Integer, Set<ExportedInstance<?>>> instancesCache = new WeakHashMap<>();
 
    public ServiceRegistryImpl(LockManager lock, Addon addon, BeanManager manager,
             Set<Class<?>> services)
    {
       this.lock = lock;
       this.addon = addon;
       this.manager = manager;
       // Copy set to avoid any reference pointers
       Set<Class<?>> copy = new LinkedHashSet<>();
       copy.addAll(services);
       this.services = new ArrayList<>(copy).toArray(new Class<?>[copy.size()]);
       this.servicesSet = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(this.services)));
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public <T> ExportedInstance<T> getExportedInstance(String clazz)
    {
       Class<T> type;
       try
       {
          type = (Class<T>) loadAddonClass(clazz);
          return getExportedInstance(type);
       }
       catch (ClassNotFoundException e)
       {
          return null;
       }
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public <T> ExportedInstance<T> getExportedInstance(final Class<T> clazz)
    {
       Assert.notNull(clazz, "Requested Class type may not be null");
       Addons.waitUntilStarted(addon);
 
      ExportedInstance<T> result = (ExportedInstance<T>) instanceCache.get(clazz.hashCode());
       if (result == null)
       {
          final Class<T> actualLoadedType;
          try
          {
             actualLoadedType = loadAddonClass(clazz);
          }
          catch (ClassNotFoundException cnfe)
          {
             log.fine("Class " + clazz.getName() + " is not present in this addon [" + addon + "]");
             return null;
          }
 
          try
          {
             result = ClassLoaders.executeIn(addon.getClassLoader(), new Callable<ExportedInstance<T>>()
             {
                @Override
                public ExportedInstance<T> call() throws Exception
                {
                   Set<Bean<?>> beans = manager.getBeans(actualLoadedType, getQualifiersFrom(actualLoadedType));
                   if (!beans.isEmpty())
                   {
                      ExportedInstance<T> result = new ExportedInstanceImpl<>(
                               addon,
                               manager, (Bean<T>)
                               manager.resolve(beans),
                               actualLoadedType,
                               actualLoadedType
                               );
                     instanceCache.put(clazz.hashCode(), result);
                      return result;
                   }
                   return null;
                }
             });
          }
          catch (Exception e)
          {
             throw new ContainerException("Could not get service of type [" + clazz + "] from addon [" + addon
                      + "]", e);
          }
 
       }
       return result;
    }
 
    @Override
    public boolean hasService(String clazz)
    {
       try
       {
          Class<?> type = loadAddonClass(clazz);
          return hasService(type);
       }
       catch (ClassNotFoundException e)
       {
          return false;
       }
    }
 
    @Override
    public boolean hasService(Class<?> clazz)
    {
       Addons.waitUntilStarted(addon);
       Class<?> type;
       try
       {
          type = loadAddonClass(clazz);
       }
       catch (ClassNotFoundException e)
       {
          return false;
       }
       for (Class<?> service : services)
       {
          if (type.isAssignableFrom(service))
          {
             return true;
          }
       }
       return false;
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<ExportedInstance<T>> getExportedInstances(String clazz)
    {
       try
       {
          Class<T> type = (Class<T>) loadAddonClass(clazz);
          return getExportedInstances(type);
       }
       catch (ClassNotFoundException e)
       {
          return Collections.emptySet();
       }
    }
 
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Set<ExportedInstance<T>> getExportedInstances(Class<T> clazz)
    {
       Addons.waitUntilStarted(addon);
 
       final Class<T> actualLoadedType;
       try
       {
          actualLoadedType = loadAddonClass(clazz);
       }
       catch (ClassNotFoundException e)
       {
          log.fine("Class " + clazz.getName() + " is not present in this addon [" + addon + "]");
          return Collections.emptySet();
       }
 
      Set<ExportedInstance<T>> result = (Set) instancesCache.get(actualLoadedType.hashCode());
 
       if (result == null)
       {
          result = new HashSet<>();
          for (int i = 0; i < services.length; i++)
          {
             final Class<?> type = services[i];
             if (actualLoadedType.isAssignableFrom(type))
             {
                try
                {
                   result.addAll(ClassLoaders.executeIn(addon.getClassLoader(), new Callable<Set<ExportedInstance<T>>>()
                   {
                      @Override
                      public Set<ExportedInstance<T>> call() throws Exception
                      {
                         Set<ExportedInstance<T>> result = new HashSet<>();
                         Set<Bean<?>> beans = manager.getBeans(type, getQualifiersFrom(type));
                         Class<? extends T> assignableClass = (Class<? extends T>) type;
                         for (Bean<?> bean : beans)
                         {
                            result.add(new ExportedInstanceImpl<>(
                                     addon,
                                     manager,
                                     (Bean<T>) bean,
                                     actualLoadedType,
                                     assignableClass
                                     ));
                         }
                         return result;
                      }
                   }));
                }
                catch (Exception e)
                {
                   throw new ContainerException("Could not get services of type [" + clazz + "] from addon ["
                            + addon
                            + "]", e);
                }
 
             }
            instancesCache.put(actualLoadedType.hashCode(), (Set) result);
          }
       }
       return result;
    }
 
    @Override
    public Set<Class<?>> getExportedTypes()
    {
       return servicesSet;
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Class<T>> getExportedTypes(Class<T> type)
    {
       Set<Class<T>> result = new HashSet<>();
       for (Class<?> serviceType : services)
       {
          if (type.isAssignableFrom(serviceType))
             result.add((Class<T>) serviceType);
       }
       return result;
    }
 
    /**
     * Ensures that the returned class is loaded from this {@link Addon}
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> loadAddonClass(Class<T> actualType) throws ClassNotFoundException
    {
       /*
        * FIXME The need for this method defeats the entire purpose of a true module system. This needs to be fixed by
        * the CLAC.
        */
       final Class<T> type;
       if (actualType.getClassLoader() == addon.getClassLoader())
       {
          type = actualType;
       }
       else
       {
          type = (Class<T>) loadAddonClass(actualType.getName());
       }
       return type;
    }
 
    private Class<?> loadAddonClass(String className) throws ClassNotFoundException
    {
      Class<?> cached = classCache.get(className.hashCode());
       if (cached == null)
       {
          Class<?> result = Class.forName(className, false, addon.getClassLoader());
          // potentially not thread-safe
         classCache.put(className.hashCode(), result);
          cached = result;
       }
 
       return cached;
    }
 
    @Override
    public String toString()
    {
       return services.toString();
    }
 
    /**
     * Returns the annotation qualifiers from a type
     */
    public static Annotation[] getQualifiersFrom(final Class<?> c)
    {
       Set<Annotation> annotations = new HashSet<>();
       for (Annotation annotation : c.getAnnotations())
       {
          if (annotation.annotationType().isAnnotationPresent(Qualifier.class))
          {
             annotations.add(annotation);
          }
       }
       return annotations.toArray(new Annotation[annotations.size()]);
    }
 
 }
