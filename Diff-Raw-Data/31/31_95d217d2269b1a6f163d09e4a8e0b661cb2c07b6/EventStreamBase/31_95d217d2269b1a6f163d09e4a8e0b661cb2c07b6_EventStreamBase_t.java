 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
 import java.util.concurrent.Executor;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 
 import fi.jawsy.jawwa.lang.Effect;
 
 public abstract class EventStreamBase<E> implements EventStream<E>, Serializable {
 
     private static final long serialVersionUID = 4389299638075146452L;
 
     @Override
     public <U> EventStream<U> map(final Function<? super E, U> mapper) {
         class MappedEventStream extends EventStreamBase<U> {
             private static final long serialVersionUID = 8746853070424352228L;
 
             @Override
             public CleanupHandle foreach(final Effect<? super U> e) {
                 class MapperEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -6599624715377023011L;
 
                     @Override
                     public void apply(E input) {
                         e.apply(mapper.apply(input));
                     }
                 }
                 return EventStreamBase.this.foreach(new MapperEffect());
             }
         }
         return new MappedEventStream();
     }
 
     @Override
     public EventStream<E> filter(final Predicate<? super E> p) {
         class FilteredEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 2518677092062764830L;
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class FilterEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -7284834867826632697L;
 
                     @Override
                     public void apply(E input) {
                         if (p.apply(input)) {
                             e.apply(input);
                         }
                     }
                 }
                 return EventStreamBase.this.foreach(new FilterEffect());
             }
         }
         return new FilteredEventStream();
     }
 
     @Override
     public <U> EventStream<U> flatMap(final Function<? super E, EventStream<U>> f) {
         class FlatMappedEventStream extends EventStreamBase<U> {
             private static final long serialVersionUID = -1662566691398092407L;
 
             @Override
             public CleanupHandle foreach(final Effect<? super U> e) {
                final AtomicReference<CleanupHandle> inner = new AtomicReference<CleanupHandle>();
 
                 class FlatMapEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -3177496251373686951L;
 
                     @Override
                     public void apply(E input) {
                         CleanupHandle old = inner.getAndSet(null);
                         if (old != null)
                             old.cleanup();
                         inner.set(f.apply(input).foreach(e));
                     }
                 }
 
                 final CleanupHandle outer = EventStreamBase.this.foreach(new FlatMapEffect());
 
                 class FlatMapCleanup implements CleanupHandle, Serializable {
                     private static final long serialVersionUID = 7160659915770883489L;
 
                     @Override
                     public void cleanup() {
                         outer.cleanup();
                         CleanupHandle old = inner.getAndSet(null);
                         if (old != null)
                             old.cleanup();
                     }
                 }
                 return new FlatMapCleanup();
             }
         }
         return new FlatMappedEventStream();
     }
 
     @Override
     public EventStream<E> union(final EventStream<? extends E> es) {
         return EventStreams.union(this, es);
     }
 
     @Override
     public EventStream<E> distinct() {
         class DistinctEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 7323926323136519568L;
 
             private final AtomicReference<E> lastValue = new AtomicReference<E>();
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class DistinctEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 6109531897034720266L;
 
                     @Override
                     public void apply(E input) {
                         E previous = lastValue.getAndSet(input);
                         if (!Objects.equal(previous, input)) {
                             e.apply(input);
                         }
                     }
                 }
                 return EventStreamBase.this.foreach(new DistinctEffect());
             }
         }
         return new DistinctEventStream();
     }
 
     @Override
     public Signal<E> hold(E initial) {
         final Signal.Var<E> s = new Signal.Var<E>(initial);
         s.consume(this);
         return s;
     }
 
     @Override
     public EventStream<E> drop(final int amount) {
         class DropEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 4078126696318463072L;
 
             private final AtomicInteger count = new AtomicInteger(amount);
 
             private volatile boolean active = false;
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class DropEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 8355486960586782753L;
 
                     @Override
                     public void apply(E input) {
                         if (!active) {
                             if (count.decrementAndGet() >= 0) {
                                 return;
                             }
                             active = true;
                         }
                         e.apply(input);
                     }
                 }
                 return EventStreamBase.this.foreach(new DropEffect());
             }
         }
         return new DropEventStream();
     }
 
     @Override
     public EventStream<E> take(final int amount) {
         class TakeEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = -487977136605864409L;
 
             private final AtomicInteger count = new AtomicInteger(amount);
 
             private volatile boolean finished = false;
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class TakeEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -8483020072868231410L;
 
                     @Override
                     public void apply(E input) {
                         if (finished) {
                             return;
                         }
                         if (count.decrementAndGet() >= 0) {
                             e.apply(input);
                         } else {
                             finished = true;
                         }
                     }
                 }
                 return EventStreamBase.this.foreach(new TakeEffect());
             }
         }
         return new TakeEventStream();
     }
 
     @Override
     public EventStream<E> dropUntil(final EventStream<?> es) {
         class DropUntilEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = -2737049603737761556L;
 
             private final AtomicBoolean active = new AtomicBoolean();
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                final AtomicReference<CleanupHandle> gateHandle = new AtomicReference<CleanupHandle>(CleanupHandles.NOOP);
 
                 class DropUntilGateEffect implements Effect<Object>, Serializable {
                     private static final long serialVersionUID = 2741111517742942539L;
 
                     @Override
                     public void apply(Object input) {
                         if (active.compareAndSet(false, true)) {
                            CleanupHandle old = gateHandle.getAndSet(null);
                             if (old != null)
                                 old.cleanup();
                         }
                     }
                 }
 
                 class DropUntilEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 2243784890063849398L;
 
                     @Override
                     public void apply(E input) {
                         if (!active.get())
                             return;
                         e.apply(input);
                     }
 
                 }
 
                CleanupHandle gate = es.foreach(new DropUntilGateEffect());
 
                 if (!gateHandle.compareAndSet(CleanupHandles.NOOP, gate))
                     gate.cleanup();
 
                 return EventStreamBase.this.foreach(new DropUntilEffect());
             }
 
         }
         return new DropUntilEventStream();
     }
 
     @Override
     public EventStream<E> takeUntil(final EventStream<?> es) {
         class TakeUntilEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = -2779911717822153295L;
 
             private final AtomicBoolean finished = new AtomicBoolean();
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                final AtomicReference<CleanupHandle> gateHandle = new AtomicReference<CleanupHandle>();
                final AtomicReference<CleanupHandle> handle = new AtomicReference<CleanupHandle>();
 
                 class TakeUntilGateEffect implements Effect<Object>, Serializable {
                     private static final long serialVersionUID = 2102835224218414042L;
 
                     @Override
                     public void apply(Object input) {
                         if (finished.compareAndSet(false, true)) {
                            CleanupHandle oldGate = gateHandle.getAndSet(null);
                             if (oldGate != null)
                                 oldGate.cleanup();
                            CleanupHandle old = handle.getAndSet(null);
                             if (old != null)
                                 old.cleanup();
                         }
                     }
                 }
 
                 class TakeUntilEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 3986357517495794590L;
 
                     @Override
                     public void apply(E input) {
                         if (finished.get())
                             return;
                         e.apply(input);
                     }
                 }
 
                CleanupHandle oldGate = gateHandle.getAndSet(es.foreach(new TakeUntilGateEffect()));
                 if (oldGate != null)
                     oldGate.cleanup();
 
                CleanupHandle old = handle.getAndSet(EventStreamBase.this.foreach(new TakeUntilEffect()));
                 if (old != null)
                     old.cleanup();
 
                 return CleanupHandles.merge(gateHandle, handle);
             }
 
         }
         return new TakeUntilEventStream();
     }
 
     @Override
     public EventStream<E> synchronize() {
         class SynchronizedEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = -8492933126353306230L;
 
             private final Object lock = new Object();
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class SynchronizedEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 3131604565473738487L;
 
                     @Override
                     public void apply(E input) {
                         synchronized (lock) {
                             e.apply(input);
                         }
                     }
                 }
 
                 return EventStreamBase.this.foreach(new SynchronizedEffect());
             }
         }
         return new SynchronizedEventStream();
     }
 
     @Override
     public EventStream<E> asynchronous(final Executor executor) {
         class AsynchronousEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 7220127200602223002L;
 
             @Override
             public CleanupHandle foreach(final Effect<? super E> e) {
                 class AsynchronousEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 4797805764789744272L;
 
                     @Override
                     public void apply(final E input) {
                         executor.execute(new Runnable() {
                             @Override
                             public void run() {
                                 e.apply(input);
                             }
                         });
                     }
 
                 }
                 return EventStreamBase.this.foreach(new AsynchronousEffect());
             }
 
         }
         return new AsynchronousEventStream();
     }
 
 }
