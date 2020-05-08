 package fi.jawsy.jawwa.frp;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import lombok.RequiredArgsConstructor;
 
 import com.google.common.base.Function;
 import com.google.common.base.Supplier;
 import com.google.common.collect.ImmutableList;
 
 public interface Signal<T> {
 
     Supplier<T> asSupplier();
 
     EventStream<T> nowAndChange();
 
     T now();
 
     EventStream<T> change();
 
     <U> Signal<U> map(Function<? super T, U> f);
 
     <U> Signal<U> map(U constant);
 
     <U> Signal<U> map(Supplier<U> s);
 
     <U> Signal<U> flatMap(Function<? super T, Signal<U>> f);
 
     Signal<ImmutableList<T>> sequence();
 
     Signal<ImmutableList<T>> sequence(CancellationToken token);
 
     Signal<ImmutableList<T>> sequence(int windowSize);
 
     Signal<ImmutableList<T>> sequence(int windowSize, CancellationToken token);
 
     @RequiredArgsConstructor
     public static class Val<T> extends SignalBase<T> {
 
         private static final long serialVersionUID = -1559828865027042537L;
 
         private final T value;
 
         @Override
         public T now() {
             return value;
         }
 
         @Override
         public EventStream<T> change() {
             return EventStreams.empty();
         }
 
         public static <T> Signal.Val<T> create(T value) {
             return new Signal.Val<T>(value);
         }
 
         @Override
         public EventStream<T> nowAndChange() {
             return EventStreams.instant(value);
         }
 
     }
 
     public static class Var<T> extends SignalBase<T> implements EventSink<T> {
         private static final long serialVersionUID = 669403298523450091L;
 
         private final AtomicReference<T> value;
         private final EventSource<T> eventSource = new EventSource<T>();
 
         public Var(T initial) {
             value = new AtomicReference<T>(initial);
         }
 
         @Override
         public T now() {
             return value.get();
         }
 
         @Override
         public EventStream<T> change() {
             return eventSource;
         }
 
         @Override
         public void fire(T value) {
             update(value);
         }
 
         public void update(T value) {
             this.value.set(value);
             eventSource.fire(value);
         }
 
         public static <T> Signal.Var<T> create(T initial) {
             return new Signal.Var<T>(initial);
         }
 
     }
 
 }
