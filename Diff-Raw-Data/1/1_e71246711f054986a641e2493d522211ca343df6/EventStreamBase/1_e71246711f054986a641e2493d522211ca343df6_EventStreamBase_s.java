 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
 import java.util.concurrent.Executor;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import lombok.val;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.base.Supplier;
 
 import fi.jawsy.jawwa.lang.Effect;
 
 public abstract class EventStreamBase<E> implements EventStream<E>, Serializable {
 
     private static final long serialVersionUID = 4389299638075146452L;
 
     @Override
     public EventStream<E> foreach(Effect<? super E> e) {
         return foreach(e, CancellationToken.NONE);
     }
 
     @Override
     public <U> EventStream<U> map(final Function<? super E, U> mapper) {
         class MappedEventStream extends EventStreamBase<U> {
             private static final long serialVersionUID = 8746853070424352228L;
 
             @Override
             public EventStream<U> foreach(final Effect<? super U> e, CancellationToken token) {
                 class MapperEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -6599624715377023011L;
 
                     @Override
                     public void apply(E input) {
                         e.apply(mapper.apply(input));
                     }
                 }
                 EventStreamBase.this.foreach(new MapperEffect(), token);
                 return this;
             }
         }
         return new MappedEventStream();
     }
 
     @Override
     public <U> EventStream<U> map(U constant) {
         return map(Functions.constant(constant));
     }
 
     @Override
     public <U> EventStream<U> map(Supplier<U> s) {
         return map(Functions.forSupplier(s));
     }
 
     @Override
     public EventStream<E> filter(final Predicate<? super E> p) {
         class FilteredEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 2518677092062764830L;
 
             @Override
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 class FilterEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -7284834867826632697L;
 
                     @Override
                     public void apply(E input) {
                         if (p.apply(input)) {
                             e.apply(input);
                         }
                     }
                 }
                 EventStreamBase.this.foreach(new FilterEffect(), token);
                 return this;
             }
         }
         return new FilteredEventStream();
     }
 
     @Override
     public <U> EventStream<U> flatMap(final Function<? super E, EventStream<U>> f) {
         class FlatMappedEventStream extends EventStreamBase<U> {
             private static final long serialVersionUID = -1662566691398092407L;
 
             @Override
             public EventStream<U> foreach(final Effect<? super U> e, CancellationToken token) {
                 val innerToken = new AtomicReference<CancellationTokenSource>();
 
                 class FlatMapEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -3177496251373686951L;
 
                     @Override
                     public void apply(E input) {
                         val newToken = new CancellationTokenSource();
 
                         val oldToken = innerToken.getAndSet(newToken);
                         if (oldToken != null)
                             oldToken.cancel();
 
                        innerToken.set(newToken);
                         f.apply(input).foreach(e, newToken);
                     }
                 }
 
                 EventStreamBase.this.foreach(new FlatMapEffect(), token);
 
                 if (token.canBeCancelled()) {
                     token.onCancel(new Runnable() {
                         @Override
                         public void run() {
                             val oldToken = innerToken.getAndSet(null);
                             if (oldToken != null)
                                 oldToken.cancel();
                         }
                     });
                 }
 
                 if (token.isCancelled()) {
                     val oldToken = innerToken.getAndSet(null);
                     if (oldToken != null)
                         oldToken.cancel();
                 }
 
                 return this;
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
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
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
                 EventStreamBase.this.foreach(new DistinctEffect(), token);
                 return this;
             }
         }
         return new DistinctEventStream();
     }
 
     @Override
     public Signal<E> hold(E initial) {
         return hold(initial, CancellationToken.NONE);
     }
 
     @Override
     public Signal<E> hold(E initial, CancellationToken token) {
         final Signal.Var<E> s = new Signal.Var<E>(initial);
         pipeTo(s, token);
         return s;
     }
 
     @Override
     public EventStream<E> drop(final int amount) {
         class DropEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 4078126696318463072L;
 
             private final AtomicInteger count = new AtomicInteger(amount);
 
             @Override
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 class DropEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 8355486960586782753L;
 
                     @Override
                     public void apply(E input) {
                         if (count.get() > 0) {
                             if (count.decrementAndGet() >= 0) {
                                 return;
                             }
                         }
                         e.apply(input);
                     }
                 }
                 EventStreamBase.this.foreach(new DropEffect(), token);
                 return this;
             }
         }
         return new DropEventStream();
     }
 
     @Override
     public EventStream<E> take(final int amount) {
         class TakeEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = -487977136605864409L;
 
             private final AtomicInteger count = new AtomicInteger(amount);
 
             @Override
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 class TakeEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -8483020072868231410L;
 
                     @Override
                     public void apply(E input) {
                         if (count.get() <= 0) {
                             return;
                         }
                         if (count.decrementAndGet() >= 0) {
                             e.apply(input);
                         }
                     }
                 }
                 EventStreamBase.this.foreach(new TakeEffect(), token);
                 return this;
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
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 val gateToken = new CancellationTokenSource();
 
                 class DropUntilGateEffect implements Effect<Object>, Serializable {
                     private static final long serialVersionUID = 2741111517742942539L;
 
                     @Override
                     public void apply(Object input) {
                         if (active.compareAndSet(false, true)) {
                             gateToken.cancel();
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
 
                 es.foreach(new DropUntilGateEffect(), gateToken);
 
                 if (token.canBeCancelled()) {
                     token.onCancel(new Runnable() {
                         @Override
                         public void run() {
                             gateToken.cancel();
                         }
                     });
                 }
 
                 if (token.isCancelled()) {
                     gateToken.cancel();
                 }
 
                 EventStreamBase.this.foreach(new DropUntilEffect(), token);
                 return this;
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
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 val innerToken = new CancellationTokenSource();
 
                 class TakeUntilGateEffect implements Effect<Object>, Serializable {
                     private static final long serialVersionUID = 2102835224218414042L;
 
                     @Override
                     public void apply(Object input) {
                         if (finished.compareAndSet(false, true)) {
                             innerToken.cancel();
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
 
                 es.foreach(new TakeUntilGateEffect(), innerToken);
                 EventStreamBase.this.foreach(new TakeUntilEffect(), innerToken);
 
                 if (token.canBeCancelled()) {
                     token.onCancel(new Runnable() {
                         @Override
                         public void run() {
                             innerToken.cancel();
                         }
                     });
                 }
 
                 if (token.isCancelled()) {
                     innerToken.cancel();
                 }
 
                 return this;
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
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 class SynchronizedEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = 3131604565473738487L;
 
                     @Override
                     public void apply(E input) {
                         synchronized (lock) {
                             e.apply(input);
                         }
                     }
                 }
 
                 EventStreamBase.this.foreach(new SynchronizedEffect(), token);
                 return this;
             }
         }
         return new SynchronizedEventStream();
     }
 
     @Override
     public EventStream<E> asynchronous(final Executor executor) {
         class AsynchronousEventStream extends EventStreamBase<E> {
             private static final long serialVersionUID = 7220127200602223002L;
 
             @Override
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
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
                 EventStreamBase.this.foreach(new AsynchronousEffect(), token);
                 return this;
             }
 
         }
         return new AsynchronousEventStream();
     }
 
     @Override
     public EventStream<E> takeWhile(final Predicate<? super E> p) {
         class TakeWhileEventStream extends EventStreamBase<E> {
 
             private static final long serialVersionUID = -6796045368801928080L;
 
             @Override
             public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
                 class TakeWhileEffect implements Effect<E>, Serializable {
                     private static final long serialVersionUID = -7795789919137505152L;
 
                     @Override
                     public void apply(E input) {
                         if (p.apply(input)) {
                             e.apply(input);
                         }
                     }
                 }
 
                 EventStreamBase.this.foreach(new TakeWhileEffect(), token);
                 return null;
             }
 
         }
         return new TakeWhileEventStream();
     }
 
     @Override
     public EventStream<E> pipeTo(EventSink<? super E> sink) {
         return pipeTo(sink, CancellationToken.NONE);
     }
 
     @Override
     public EventStream<E> pipeTo(final EventSink<? super E> sink, CancellationToken token) {
         return foreach(new Effect<E>() {
             @Override
             public void apply(E input) {
                 sink.fire(input);
             }
         }, token);
     }
 
 }
