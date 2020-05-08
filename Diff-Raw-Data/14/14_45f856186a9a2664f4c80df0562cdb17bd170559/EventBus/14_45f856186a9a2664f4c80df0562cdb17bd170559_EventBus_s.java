 package ch.cmbntr.eventbus;
 
 import static ch.cmbntr.eventbus.Handlers.IS_SUBSCRIBE_METHOD;
 import static ch.cmbntr.eventbus.Handlers.handlerBuilder;
 import static ch.cmbntr.eventbus.Handlers.linearizeHierarchy;
 import static ch.cmbntr.eventbus.Handlers.newHandlerCache;
 import static com.google.common.base.Functions.constant;
 import static com.google.common.base.Predicates.alwaysFalse;
 import static com.google.common.base.Predicates.alwaysTrue;
 import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
 import static java.lang.Boolean.FALSE;
 import static java.lang.Boolean.TRUE;
 import static java.util.concurrent.Executors.newSingleThreadExecutor;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.Executor;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Supplier;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.Queues;
 import com.google.common.collect.SetMultimap;
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.DeadEvent;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Asynchronous event bus, which is compatible with the Guava EventBus (i.e. it uses {@link Subscribe},
  * {@link AllowConcurrentEvents} and {@link DeadEvent}).
  * 
  * @author Michael Locher <cmbntr@gmail.com>
  */
 public class EventBus implements Runnable {
 
   private static final ThreadLocal<Boolean> IS_DISPATCHING = new ThreadLocal<Boolean>() {
     @Override
     protected Boolean initialValue() {
       return FALSE;
     }
   };
 
   private final SetMultimap<Class<?>, Handler> handlersByEventType = Multimaps.newSetMultimap(new MapMaker().weakKeys()
       .<Class<?>, Collection<Handler>> makeMap(), new Supplier<Set<Handler>>() {
     @Override
     public Set<Handler> get() {
       return newHandlerSet();
     }
 
   });
 
   private final LoadingCache<Class<?>, Set<Handler>> handlersByTargetType;
 
   private final Queue<Object> pending = Queues.newConcurrentLinkedQueue();
 
   private final Executor dispatcher;
 
   /**
    * Creates this, with a "default" identifier.
    */
   public EventBus() {
     this("default");
   }
 
   /**
    * Creates this. Will use a custom single thread for batched event dispatching and handling.
    * 
    * @param identifier the bus identifier
    */
   public EventBus(final String identifier) {
     this(identifier, true, sameThreadExecutor(), newSingleThreadExecutor());
   }
 
   /**
    * Creates this.
    * 
    * @param identifier the bus identifier
    * @param batchDispatching if {@code true}, event processing is batched (same thread per handler/event)
    * @param handlerExecutor the executor which will performs the handler callbacks
    * @param dispatcher the executor which will perform the dispatching
    */
   public EventBus(final String identifier, final boolean batchDispatching, final Executor handlerExecutor,
       final Executor dispatcher) {
     this(handlerBuilder(identifier, constant(handlerExecutor), batchDispatching ? alwaysTrue() : alwaysFalse()),
         dispatcher);
   }
 
   /**
    * Creates this.
    * 
    * @param handlerBuilder the factory for handlers
    * @param dispatcher the executor which will perform the dispatching
    */
   public EventBus(final Function<? super Method, Handler> handlerBuilder, final Executor dispatcher) {
     this(IS_SUBSCRIBE_METHOD, handlerBuilder, dispatcher);
   }
 
   /**
    * Creates this.
    * 
    * @param isHandler determines if a method is a handler
    * @param handlerBuilder the factory for handlers
    * @param dispatcher the executor which will perform the dispatching
    */
   protected EventBus(final Predicate<? super Method> isHandler, final Function<? super Method, Handler> handlerBuilder,
       final Executor dispatcher) {
     super();
     this.dispatcher = dispatcher;
     this.handlersByTargetType = newHandlerCache(isHandler, handlerRegistration(handlerBuilder));
   }
 
   private Function<Method, Handler> handlerRegistration(final Function<? super Method, Handler> builder) {
     return new Function<Method, Handler>() {
       @Override
       public Handler apply(final Method m) {
         assert m != null;
         final Handler h = builder.apply(m);
         registerHandler(m, h);
         return h;
       }
     };
   }
 
   /**
    * Provides a new empty set for handlers. Note: the set must be thread-safe.
    * 
    * @return a new set
    */
   protected Set<Handler> newHandlerSet() {
     return Sets.newCopyOnWriteArraySet();
   }
 
   private void registerHandler(final Method m, final Handler h) {
     this.handlersByEventType.put(m.getParameterTypes()[0], h);
   }
 
   /**
    * Dispatches an event. Will be called from the dispatcher executor.
    * 
    * @param event the event
    */
   protected void dispatch(final Object event) {
     boolean dispatched = false;
     for (final Class<?> eventType : linearizeHierarchy(event.getClass())) {
       if (this.handlersByEventType.containsKey(eventType)) {
         for (final Handler h : this.handlersByEventType.get(eventType)) {
           dispatched |= h.dispatch(event);
         }
       }
     }
     if (!dispatched && !(event instanceof DeadEvent)) {
       post(new DeadEvent(this, event));
     }
   }
 
   /**
    * Posts an event to all registered handlers.
    * 
    * @param event event to post.
    */
   public void post(final Object event) {
     this.pending.add(event);
     if (!IS_DISPATCHING.get()) {
       this.dispatcher.execute(this);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void run() {
     try {
       IS_DISPATCHING.set(TRUE);
       while (true) {
         final Object event = this.pending.poll();
         if (event == null) {
           return;
         }
         dispatch(event);
       }
     } finally {
       IS_DISPATCHING.set(FALSE);
     }
   }
 
   private Set<Handler> getHandlersForTargetType(final Class<? extends Object> targetType) {
     return this.handlersByTargetType.getUnchecked(targetType);
   }
 
   /**
    * Registers all handler methods on {@code object} to receive events.
    * 
   * @param object object whose handler methods should be registered.
    */
   public void register(final Object target) {
     for (final Handler handler : getHandlersForTargetType(target.getClass())) {
       handler.register(target);
     }
   }
 
   /**
    * Registers all handler methods on {@code object} to receive events, but only keeps a weak reference to the
    * object.
    * 
   * @param object object whose handler methods should be registered.
    */
   public void registerWeak(final Object target) {
     for (final Handler handler : getHandlersForTargetType(target.getClass())) {
       handler.registerWeak(target);
     }
   }
 
   /**
    * Unregisters all handler methods on a registered {@code object}.
    * 
   * @param object object whose handler methods should be unregistered.
    */
   public void unregister(final Object target) {
     for (final Handler handler : getHandlersForTargetType(target.getClass())) {
       handler.unregister(target);
     }
   }
 
 }
