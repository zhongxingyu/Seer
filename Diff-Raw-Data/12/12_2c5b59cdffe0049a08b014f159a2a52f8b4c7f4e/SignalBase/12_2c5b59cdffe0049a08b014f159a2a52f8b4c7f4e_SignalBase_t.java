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
         return map(Functions.forSupplier(s));
     }
 
     @Override
     public <U> Signal<U> map(U constant) {
         return map(Functions.constant(constant));
     }
 
     @Override
     public <U> Signal<U> flatMap(final Function<? super T, Signal<U>> f) {
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
 }
