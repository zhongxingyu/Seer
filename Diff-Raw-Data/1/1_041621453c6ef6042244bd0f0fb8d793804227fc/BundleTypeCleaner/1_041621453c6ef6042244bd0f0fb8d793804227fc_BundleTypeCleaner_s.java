 package org.javabits.yar.guice.osgi.internal;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import org.javabits.yar.RegistryHook;
 import org.javabits.yar.TypeEvent;
 import org.javabits.yar.TypeListener;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleReference;
 import org.osgi.framework.SynchronousBundleListener;
 
 import java.lang.reflect.Type;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 import static java.util.logging.Level.SEVERE;
 import static org.javabits.yar.guice.Reflections.getRawType;
 
 /**
  * This class is responsible to cleanup all the types from a specific bundle when this one is removed.
  * It is avoid memory leak on ClassLoader.
  * Date: 6/3/13
  *
  * @author Romain Gilles
  */
 class BundleTypeCleaner implements SynchronousBundleListener, TypeListener {
     private static final Logger LOG = Logger.getLogger(BundleTypeCleaner.class.getName());
     private final RegistryHook registryHook;
     private LoadingCache<Long, Set<Type>> loadingCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, Set<Type>>() {
         @Override
         public Set<Type> load(Long key) {
             return new CopyOnWriteArraySet<>();
         }
     });
 
     BundleTypeCleaner(RegistryHook registryHook) {
         this.registryHook = registryHook;
     }
 
     @Override
     public void bundleChanged(BundleEvent event) {
         switch (event.getType()) {
             case BundleEvent.STOPPING:
                 try {
                     long bundleId = event.getBundle().getBundleId();
                     Set<Type> types = loadingCache.get(bundleId);
                     if (!types.isEmpty()) {
                         registryHook.invalidateAll(types);
                     }
                     loadingCache.invalidate(bundleId);
                 } catch (ExecutionException e) {
                     LOG.log(SEVERE, "Cannot clean up types on event: " + event, e);
                 }
                 break;
             default:
                 //nothing to do
         }
     }
 
     @Override
     public void typeChanged(TypeEvent typeEvent) {
         switch (typeEvent.eventType()) {
             case ADDED:
                 Type type = typeEvent.type();
                 Class<?> rawType = getRawType(type);
                 ClassLoader classLoader = rawType.getClassLoader();
                 if (classLoader instanceof BundleReference) {
                     Bundle bundle = ((BundleReference) classLoader).getBundle();
                     try {
                         loadingCache.get(bundle.getBundleId()).add(type);
                     } catch (ExecutionException e) {
                         LOG.log(SEVERE, String.format("Cannot add type: %s", type), e);
                     }
                 } else {
                     LOG.warning(type + "'s class loader is not a BundleReference");
                 }
                 break;
             case REMOVED:
             default:
                 // nothing to do
         }
     }
 }
