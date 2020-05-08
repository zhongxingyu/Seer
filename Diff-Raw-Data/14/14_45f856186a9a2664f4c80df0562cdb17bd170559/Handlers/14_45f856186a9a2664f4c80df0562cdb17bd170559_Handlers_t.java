 package ch.cmbntr.eventbus;
 
 import static com.google.common.base.Functions.compose;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.collect.FluentIterable.from;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 import static java.lang.String.format;
 import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;
 import static java.lang.reflect.Modifier.isSynchronized;
 import static java.util.Collections.newSetFromMap;
 import static java.util.Collections.singleton;
 
 import java.lang.annotation.Annotation;
 import java.lang.invoke.MethodHandles;
 import java.lang.invoke.MethodHandles.Lookup;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.Executor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.CheckForNull;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Throwables;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Queues;
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.reflect.TypeToken;
 import com.google.common.util.concurrent.UncheckedExecutionException;
 
 /**
  * Event handling.
  * 
  * @author Michael Locher <cmbntr@gmail.com>
  */
 public class Handlers {
 
   /**
    * Tests if a method is a {@link Subscribe} handler.
    */
   public static final Predicate<Method> IS_SUBSCRIBE_METHOD = isHandlerMarkedWith(Subscribe.class);
 
   private static final Predicate<Method> CONCURRENT_DELIVERY = new Predicate<Method>() {
     @Override
     public boolean apply(final Method method) {
       assert method != null;
       return method.getAnnotation(AllowConcurrentEvents.class) != null || isSynchronized(method.getModifiers());
     }
   };
 
   private static final Function<Class<?>, List<Method>> GET_METHODS = new Function<Class<?>, List<Method>>() {
     @Override
     public List<Method> apply(final Class<?> clazz) {
       assert clazz != null;
       return Arrays.asList(clazz.getMethods());
     }
   };
 
   private static final LoadingCache<Class<?>, Set<Class<?>>> HIERARCHIES = weakKeyCache(new CacheLoader<Class<?>, Set<Class<?>>>() {
     @Override
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public Set<Class<?>> load(final Class<?> concreteClass) {
       return (Set) TypeToken.of(concreteClass).getTypes().rawTypes();
     }
   });
 
   private static final int WRITE_CONCURRENCY = 1;
 
   private Handlers() {
     super();
   }
 
   private static <K, V> LoadingCache<K, V> weakKeyCache(final CacheLoader<? super K, V> loader) {
     return CacheBuilder.newBuilder().weakKeys().concurrencyLevel(WRITE_CONCURRENCY).build(loader);
   }
 
   /**
    * Creates a new event {@link Handler} cache.
    * 
    * @param isHandler determines if a method is a handler
    * @param builder the factory for handlers
    * @return a new handler cache
    */
   public static LoadingCache<Class<?>, Set<Handler>> newHandlerCache(final Predicate<? super Method> isHandler,
       final Function<? super Method, Handler> builder) {
     return weakKeyCache(CacheLoader.from(compose(filterAndBuild(isHandler, builder), GET_METHODS)));
   }
 
   private static Function<List<Method>, Set<Handler>> filterAndBuild(final Predicate<? super Method> isHandler,
       final Function<? super Method, Handler> builder) {
     final Function<Method, Handler> cachedBuilder = weakKeyCache(CacheLoader.from(builder));
     return new Function<List<Method>, Set<Handler>>() {
       @Override
       public Set<Handler> apply(final List<Method> methods) {
         assert methods != null;
         return ImmutableSet.copyOf(from(methods).filter(isHandler).transform(cachedBuilder));
       }
     };
   }
 
   /**
    * Event delivery.
    */
   public static interface EventDelivery {
     /**
      * Delivers an event.
      * 
      * @param target the target
      * @param event the event
      */
     public void deliverEvent(Object target, Object event);
   }
 
   /**
    * Creates a new delivery for the given handler method.
    * 
    * @param handler the handler
    * @param concurrent if {@code true}, concurrent deliveries are allowed on a single target
    * @return the delivery
    */
   public static EventDelivery createDelivery(final Method handler, final boolean concurrent) {
     try {
       return createDelivery(MethodHandles.publicLookup(), handler, concurrent);
     } catch (final IllegalAccessException e) {
       throw Throwables.propagate(e);
     }
   }
 
   private static EventDelivery createDelivery(final Lookup lookup, final Method h, final boolean concurrent)
       throws IllegalAccessException {
     h.setAccessible(true);
     final EventDelivery handler = asInterfaceInstance(EventDelivery.class, lookup.unreflect(h));
     return concurrent ? handler : new SynchronizedDelivery(handler);
   }
 
   /**
    * Only allows one delivery per target at any given time.
    */
   private static class SynchronizedDelivery implements EventDelivery {
 
     private static final LoadingCache<Object, Object> SYNCS_BY_TARGET = weakKeyCache(new CacheLoader<Object, Object>() {
       @Override
       public Object load(final Object key) throws Exception {
         return new Object();
       }
     });
 
     private final EventDelivery delivery;
 
     /**
      * Creates this.
      * 
      * @param delivery the unsynchronized delivery
      */
     public SynchronizedDelivery(final EventDelivery delivery) {
       this.delivery = delivery;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deliverEvent(final Object target, final Object event) {
       synchronized (SynchronizedDelivery.SYNCS_BY_TARGET.getUnchecked(target)) {
         this.delivery.deliverEvent(target, event);
       }
     }
 
   }
 
   /**
    * Creates a handler factory.
    * 
    * @param identifier a bus identifier
    * @param deliveryExecutor provides executors for the handler methods
    * @param batchDelivery determines if handler invocations are batched
    * @return a new handler factory
    */
   public static Function<Method, Handler> handlerBuilder(final String identifier,
       final Function<? super Method, ? extends Executor> deliveryExecutor, final Predicate<? super Method> batchDelivery) {
     final Logger logger = Logger.getLogger(EventHandler.class.getName() + "." + checkNotNull(identifier));
     return new Function<Method, Handler>() {
       @Override
       public Handler apply(final Method handler) {
         assert handler != null;
         final Executor exec = deliveryExecutor.apply(handler);
         final boolean batching = batchDelivery.apply(handler);
         final boolean threadSafe = CONCURRENT_DELIVERY.apply(handler);
         final EventDelivery delivery = createDelivery(handler, threadSafe);
         return EventHandler.create(logger, delivery, batching, exec);
       }
     };
   }
 
   /**
    * An event handler.
    */
   public static final class EventHandler implements Handler, Runnable {
 
     private final Logger logger;
 
     private final Queue<EventAndTargets> pending = Queues.newConcurrentLinkedQueue();
 
     private final Executor deliveryExecutor;
 
     private final EventDelivery delivery;
 
     private final boolean batchDelivery;
 
     @CheckForNull
     private Set<Object> strongTargets;
 
     @CheckForNull
     private Set<Object> weakTargets;
 
     /**
      * Creates this.
      * 
      * @param logger the logger to use
      * @param delivery the delivery to perform
      * @param batchDelivery whether to perform batched deliveries
      * @param deliveryExecutor the executor to use for deliveries
      */
     protected EventHandler(final Logger logger, final EventDelivery delivery, final boolean batchDelivery,
         final Executor deliveryExecutor) {
       this.logger = logger;
       this.delivery = delivery;
       this.batchDelivery = batchDelivery;
       this.deliveryExecutor = deliveryExecutor;
     }
 
     /**
      * Creates this a handler.
      * 
      * @param logger the logger to use
      * @param delivery the delivery to perform
      * @param batchDelivery whether to perform batched deliveries
      * @param deliveryExecutor the executor to use for deliveries
      * @return the handler
      */
     public static EventHandler create(final Logger logger, final EventDelivery delivery, final boolean batchDelivery,
         final Executor deliveryExecutor) {
       return new EventHandler(logger, delivery, batchDelivery, deliveryExecutor);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void run() {
       final EventAndTargets next = this.pending.poll();
       if (next == null) {
         return;
       }
       final Object event = next.event;
       for (final Object target : next.targets) {
         try {
           this.delivery.deliverEvent(target, event);
         } catch (final RuntimeException e) {
           this.logger.log(Level.SEVERE, "Could not dispatch event", e);
         }
       }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public synchronized boolean dispatch(final Object event) {
       final Collection<Object> targets = getTargets();
       if (targets == null || targets.isEmpty()) {
         return false;
       }
       if (this.batchDelivery) {
         deliverBatchMode(event, targets);
       } else {
         deliverSingleMode(event, targets);
       }
       return true;
     }
 
     private void deliverSingleMode(final Object event, final Collection<Object> targets) {
       for (final Object target : targets) {
         this.pending.add(new EventAndTargets(event, singleton(target)));
         this.deliveryExecutor.execute(this);
       }
     }
 
     private void deliverBatchMode(final Object event, final Collection<Object> targets) {
       this.pending.add(new EventAndTargets(event, targets));
       this.deliveryExecutor.execute(this);
     }
 
     @CheckForNull
     private synchronized Collection<Object> getTargets() {
       final Set<Object> s = this.strongTargets;
       final Set<Object> w = this.weakTargets;
 
       if (s == null && w == null) {
         return null;
       } else if (s == null) {
         // take a snapshot
         final Collection<Object> targets = newArrayList(w);
         if (targets.isEmpty()) {
           // all weak references are gone
           this.weakTargets = null;
           return null;
         }
         return targets;
       } else if (w == null) {
         // take a snapshot
         return newArrayList(s);
       } else {
         // take a snapshot and remove possible duplicates
         final int count = s.size() + w.size();
         final Collection<Object> snapshot = count < 2 ? newArrayListWithCapacity(1) : newStrongSet(count);
         snapshot.addAll(s);
         snapshot.addAll(w);
         return snapshot;
       }
     }
 
     private static Set<Object> newStrongSet() {
       return newSetFromMap(Maps.<Object, Boolean> newIdentityHashMap());
     }
 
     private static Set<Object> newStrongSet(final int expectedMaxSize) {
       return newSetFromMap(new IdentityHashMap<Object, Boolean>(expectedMaxSize));
     }
 
     private static Set<Object> newWeakSet() {
       return newSetFromMap(new MapMaker().concurrencyLevel(WRITE_CONCURRENCY).weakKeys().<Object, Boolean> makeMap());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public synchronized void register(final Object target) {
       if (this.strongTargets == null) {
         this.strongTargets = newStrongSet();
       }
       this.strongTargets.add(target);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public synchronized void registerWeak(final Object target) {
       if (this.weakTargets == null) {
         this.weakTargets = newWeakSet();
       }
       this.weakTargets.add(target);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public synchronized void unregister(final Object target) {
       if (this.strongTargets != null) {
         this.strongTargets.remove(target);
         if (this.strongTargets.isEmpty()) {
           this.strongTargets = null;
         }
       }
 
       if (this.weakTargets != null) {
         this.weakTargets.remove(target);
         if (this.weakTargets.isEmpty()) {
           this.weakTargets = null;
         }
       }
     }
 
   }
 
   /**
    * Pair of an event and targets.
    */
   public static final class EventAndTargets {
 
     public final Object event;
     public final Iterable<Object> targets;
 
     /**
      * Creates this.
      * 
      * @param event the event
      * @param targets the targets
      */
     public EventAndTargets(final Object event, final Iterable<Object> targets) {
       super();
       this.event = event;
       this.targets = targets;
     }
 
   }
 
   /**
    * Checks if a method is a valid handler method.
    * 
    * @param marker the marker to identify handlers
    * @return the predicate, which returns {@code true} if a method is a handler method
    */
   public static Predicate<Method> isHandlerMarkedWith(final Class<? extends Annotation> marker) {
     return new Predicate<Method>() {
       @Override
       public boolean apply(final Method method) {
         assert method != null;
         for (final Class<?> c : TypeToken.of(method.getDeclaringClass()).getTypes().rawTypes()) {
           try {
             final Method m = c.getMethod(method.getName(), method.getParameterTypes());
             if (m.isAnnotationPresent(marker)) {
               checkSingleArgument(method);
               return true;
             }
           } catch (final NoSuchMethodException ignored) {
             continue;
           }
         }
         return false;
       }
 
       private void checkSingleArgument(final Method method) {
         final int paramCount = method.getParameterTypes().length;
         if (paramCount != 1) {
           final String msg = format(
               "Method %s has @%s annotation, but requires %d arguments.  Event handler methods must require a single argument.",
               method, marker.getSimpleName(), paramCount);
           throw new IllegalArgumentException(msg);
         }
       }
     };
   }
 
   /**
    * Linearizes a class's type hierarchy into a set of Class objects. The set will include all superclasses
    * (transitively), and all interfaces implemented by these superclasses.
    * 
    * @param concreteClass the type whose hierarchy will be processed
    * @return the complete type hierarchy, flattened and uniqued
    */
   public static Set<Class<?>> linearizeHierarchy(final Class<?> concreteClass) {
     try {
       return HIERARCHIES.getUnchecked(concreteClass);
     } catch (final UncheckedExecutionException e) {
       throw Throwables.propagate(e.getCause());
     }
   }
 
 }
