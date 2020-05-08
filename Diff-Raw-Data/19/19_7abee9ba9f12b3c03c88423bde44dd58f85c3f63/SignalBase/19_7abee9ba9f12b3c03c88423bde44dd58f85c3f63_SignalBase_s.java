 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableList;
 
 import fi.jawsy.jawwa.lang.Effect;
 import fi.jawsy.jawwa.lang.Function2;
 import fi.jawsy.jawwa.lang.Tuple2;
 import fi.jawsy.jawwa.lang.Tuple2Functions;
 import fi.jawsy.jawwa.lang.Tuple3;
 import fi.jawsy.jawwa.lang.Tuple3Functions;
 
 /**
  * Base class for signal implementations.
  * 
  * @param <T>
  *            value type
  */
 public abstract class SignalBase<T> implements Signal<T>, Serializable, Supplier<T> {
 
     private static final long serialVersionUID = -8545700156708474986L;
 
     @Override
     public Supplier<T> asSupplier() {
         return this;
     }
 
     @Override
     public T get() {
         return now();
     }
 
     @Override
     public EventStream<T> nowAndChange() {
         return EventStreams.instant(this.asSupplier()).union(change());
     }
 
     @Override
     public <U> Signal<U> map(final Function<? super T, U> f) {
         Preconditions.checkNotNull(f, "function cannot be null");
         class MappedSignal extends SignalBase<U> {
             private static final long serialVersionUID = -3228645957665057772L;
 
             @Override
             public U now() {
                 return f.apply(SignalBase.this.now());
             }
 
             @Override
             public EventStream<U> change() {
                 return SignalBase.this.change().map(f);
             }
         }
         return new MappedSignal();
     }
 
     @Override
     public <U> Signal<U> map(Supplier<U> s) {
         Preconditions.checkNotNull(s, "supplier cannot be null");
         return map(Functions.forSupplier(s));
     }
 
     @Override
     public <U> Signal<U> map(U constant) {
         return map(Functions.constant(constant));
     }
 
     @Override
     public <U> Signal<U> flatMap(final Function<? super T, Signal<U>> f) {
         Preconditions.checkNotNull(f, "function cannot be null");
         class FlatMappedSignal extends SignalBase<U> {
             private static final long serialVersionUID = -6722902423420227469L;
 
             @Override
             public U now() {
                 return f.apply(SignalBase.this.now()).now();
             }
 
             @Override
             public EventStream<U> change() {
                 class FlatMapSignalEventStream extends EventStreamBase<U> {
                     private static final long serialVersionUID = 7806489755846636883L;
 
                     @Override
                     public EventStream<U> foreach(final Effect<? super U> e, CancellationToken token) {
                         final AtomicReference<CancellationTokenSource> oldToken = new AtomicReference<CancellationTokenSource>();
 
                         class FlatMappedSignalEffect implements Effect<T> {
                             @Override
                             public void apply(T input) {
                                 CancellationTokenSource newToken = new CancellationTokenSource();
                                 f.apply(input).change().foreach(e, newToken);
 
                                 CancellationTokenSource old = oldToken.getAndSet(newToken);
                                 if (old != null)
                                     old.cancel();
                             }
                         }
 
                         SignalBase.this.change().foreach(new FlatMappedSignalEffect(), token);
                         return this;
                     }
                 }
                 return new FlatMapSignalEventStream();
             }
 
         }
         return new FlatMappedSignal();
     }
 
     @Override
     public Signal<ImmutableList<T>> sequence() {
         return sequence(CancellationToken.NONE);
     }
 
     @Override
     public Signal<ImmutableList<T>> sequence(CancellationToken token) {
         return sequenceInternal(-1, token);
     }
 
     @Override
     public Signal<ImmutableList<T>> sequence(int windowSize) {
         return sequence(windowSize, CancellationToken.NONE);
     }
 
     @Override
     public Signal<ImmutableList<T>> sequence(int windowSize, CancellationToken token) {
         Preconditions.checkArgument(windowSize >= 0, "windowSize must be positive");
         return sequenceInternal(windowSize, token);
     }
 
     private Signal<ImmutableList<T>> sequenceInternal(final int windowSize, CancellationToken token) {
         Preconditions.checkNotNull(token, "cancellation token cannot be null");
         final AtomicReference<ImmutableList<T>> data = new AtomicReference<ImmutableList<T>>(ImmutableList.<T> of());
         final EventSource<ImmutableList<T>> change = new EventSource<ImmutableList<T>>();
 
         this.nowAndChange().foreach(new Effect<T>() {
             @Override
             public void apply(T input) {
                 ImmutableList<T> newData;
                 synchronized (data) {
                     FluentIterable<T> oldData = (windowSize >= 0) ? FluentIterable.from(data.get()).limit(windowSize) : FluentIterable.from(data.get());
                     newData = ImmutableList.<T> builder().addAll(oldData).add(input).build();
                     data.set(newData);
                 }
                 change.fire(newData);
             }
         }, token);
 
         class SequenceSignal extends SignalBase<ImmutableList<T>> {
             private static final long serialVersionUID = 6639513066972787774L;
 
             @Override
             public ImmutableList<T> now() {
                 return data.get();
             }
 
             @Override
             public EventStream<ImmutableList<T>> change() {
                 return change;
             }
 
         }
         return new SequenceSignal();
     }
 
     @Override
     public <A> Signal<Tuple2<T, A>> zip(Signal<A> a) {
         return zip(a, CancellationToken.NONE);
     }
 
     @Override
     public <A> Signal<Tuple2<T, A>> zip(Signal<A> a, CancellationToken token) {
         final Signal.Var<Tuple2<T, A>> zippedSignal = new Signal.Var<Tuple2<T, A>>(Tuple2.of(this.now(), a.now()));
         this.change().foreach(new Effect<T>() {
             @Override
             public void apply(T input) {
                 zippedSignal.update(Tuple2Functions.<T, A> withA(input));
             }
         }, token);
         a.change().foreach(new Effect<A>() {
             @Override
             public void apply(A input) {
                 zippedSignal.update(Tuple2Functions.<T, A> withB(input));
             }
         });
         return zippedSignal;
     }
 
     @Override
     public <A, B> Signal<Tuple3<T, A, B>> zip(Signal<A> a, Signal<B> b) {
         return zip(a, b, CancellationToken.NONE);
     }
 
     @Override
     public <A, B> Signal<Tuple3<T, A, B>> zip(Signal<A> a, Signal<B> b, CancellationToken token) {
         final Signal.Var<Tuple3<T, A, B>> zippedSignal = new Signal.Var<Tuple3<T, A, B>>(Tuple3.of(this.now(), a.now(), b.now()));
         this.change().foreach(new Effect<T>() {
             @Override
             public void apply(T input) {
                 zippedSignal.update(Tuple3Functions.<T, A, B> withA(input));
             }
         }, token);
         a.change().foreach(new Effect<A>() {
             @Override
             public void apply(A input) {
                 zippedSignal.update(Tuple3Functions.<T, A, B> withB(input));
             }
         });
         b.change().foreach(new Effect<B>() {
             @Override
             public void apply(B input) {
                 zippedSignal.update(Tuple3Functions.<T, A, B> withC(input));
             }
         });
         return zippedSignal;
     }
 
     @Override
     public Signal<T> distinct() {
         final EventStream<T> change = change().distinct();
         final EventStream<T> nowAndChange = nowAndChange().distinct();
 
         class DistinctSignal extends SignalBase<T> {
             private static final long serialVersionUID = -3549509419976918727L;
 
             @Override
             public T now() {
                 return SignalBase.this.now();
             }
 
             @Override
             public EventStream<T> change() {
                 return change;
             }
 
             @Override
             public EventStream<T> nowAndChange() {
                 return nowAndChange;
             }
 
         }
         return new DistinctSignal();
     }
 
     @Override
     public <U> Signal<U> foldLeft(U initial, Function2<U, T, U> f) {
         return foldLeft(initial, f, CancellationToken.NONE);
     }
 
     @Override
     public <U> Signal<U> foldLeft(U initial, final Function2<U, T, U> f, CancellationToken token) {
         final Signal.Var<U> foldLeftSignal = new Signal.Var<U>(f.apply(initial, now()));
 
         this.change().foreach(new Effect<T>() {
             @Override
             public void apply(final T input) {
                 foldLeftSignal.update(new Function<U, U>() {
                     @Override
                     public U apply(U accumInput) {
                         return f.apply(accumInput, input);
                     }
                 });
             }
         }, token);
 
         return foldLeftSignal;
     }
 
 }
