 package fi.jawsy.jawwa.frp;
 
 import java.util.concurrent.atomic.AtomicReference;
 
 import lombok.RequiredArgsConstructor;
 
 import com.google.common.base.Function;
 import com.google.common.base.Supplier;
 import com.google.common.collect.ImmutableList;
 
 import fi.jawsy.jawwa.lang.Function2;
 import fi.jawsy.jawwa.lang.Tuple2;
 import fi.jawsy.jawwa.lang.Tuple3;
 
 /**
  * Mutable state container that tracks the current value, and publishes value changes using event streams.
  * 
  * All default implementations of signals are thread-safe.
  * 
  * @param <T>
  *            value type
  */
 public interface Signal<T> {
 
     /**
      * Converts this signal to a supplier that always returns the current signal value.
      * 
      * @return supplier
      */
     Supplier<T> asSupplier();
 
     /**
      * Returns an event stream that publishes the current value immediately when a subscription is added, and tracks
      * changes after that.
      * 
      * @return event stream
      */
     EventStream<T> nowAndChange();
 
     /**
      * Returns the current value of the signal.
      * 
      * @return value
      */
     T now();
 
     /**
      * Returns a new signal that filters out sequential values if they are equal.
      * 
      * @return signal
      */
     Signal<T> distinct();
 
     <U> Signal<U> foldLeft(U initial, Function2<U, T, U> f);
 
     <U> Signal<U> foldLeft(U initial, Function2<U, T, U> f, CancellationToken token);
 
     /**
      * Returns an event stream that will track the changes of this signal.
      * 
      * @return value
      */
     EventStream<T> change();
 
     /**
      * Returns a new signal that applies the given function to current and future values.
      * 
      * @param f
      *            non-null function
      * @return signal
      */
     <U> Signal<U> map(Function<? super T, U> f);
 
     /**
      * Returns a new signal that replaces all values with the given constant.
      * 
      * @param constant
      *            constant value
      * @return signal
      */
     <U> Signal<U> map(U constant);
 
     /**
     * Returns a new signal that replaces all values with values returned by the given signal.
     * 
     * @param s
     *            non-null signal
     * @return signal
     */
    <U> Signal<U> map(Signal<U> s);

    /**
      * Returns a new signal that replaces all values with values returned by the given supplier.
      * 
      * @param s
      *            non-null value supplier
      * @return signal
      */
     <U> Signal<U> map(Supplier<U> s);
 
     /**
      * Returns a new signal that implements signal switching. The returned signal will always be connected to the signal
      * returned by the last invocation of the given function.
      * 
      * @param f
      *            non-null function
      * @return signal
      */
     <U> Signal<U> flatMap(Function<? super T, Signal<U>> f);
 
     /**
      * Returns a new signal that collects the current and all future values into an immutable list with no size bound.
      * 
      * <b>Creates a subscription, so this method has a side effect.</b> The subscription is never removed, so memory
      * leaks may occur depending on the type of the signal.
      * 
      * @return signal
      */
     Signal<ImmutableList<T>> sequence();
 
     /**
      * Returns a new signal that collects the current and all future values into an immutable list with no size bound.
      * 
      * <b>Creates a subscription, so this method has a side effect.</b> The subscription is removed when the given token
      * is cancelled.
      * 
      * @param token
      *            non-null cancellation token
      * @return signal
      */
     Signal<ImmutableList<T>> sequence(CancellationToken token);
 
     /**
      * Returns a new signal that collects current and future values into an immutable list with specific maximum size.
      * 
      * <b>Creates a subscription, so this method has a side effect.</b> The subscription is never removed, so memory
      * leaks may occur depending on the type of the signal.
      * 
      * @param windowSize
      *            positive window size
      * @return signal
      */
     Signal<ImmutableList<T>> sequence(int windowSize);
 
     /**
      * Returns a new signal that collects current and future values into an immutable list with specific maximum size.
      * 
      * <b>Creates a subscription, so this method has a side effect.</b> The subscription is removed when the given token
      * is cancelled.
      * 
      * @param windowSize
      *            positive window size
      * @param token
      *            non-null cancellation token
      * @return signal
      */
     Signal<ImmutableList<T>> sequence(int windowSize, CancellationToken token);
 
     /**
      * Returns a new signal that tracks the values of this signal and the given signal using a tuple.
      * 
      * <b>Creates subscriptions, so this method has a side effect.</b> The subscription is never removed, so memory
      * leaks may occur depending on the type of the signal.
      * 
      * @param a
      *            second signal
      * @return signal
      */
     <A> Signal<Tuple2<T, A>> zip(Signal<A> a);
 
     /**
      * Returns a new signal that tracks the values of this signal and the given signal using a tuple.
      * 
      * <b>Creates subscriptions, so this method has side effects.</b> The subscriptions are removed when the given token
      * is cancelled.
      * 
      * @param a
      *            second signal
      * @param token
      *            non-null cancellation token
      * @return signal
      */
     <A> Signal<Tuple2<T, A>> zip(Signal<A> a, CancellationToken token);
 
     /**
      * Returns a new signal that tracks the values of this signal and the two given signals using a tuple.
      * 
      * <b>Creates subscriptions, so this method has a side effect.</b> The subscription is never removed, so memory
      * leaks may occur depending on the type of the signal.
      * 
      * @param a
      *            second signal
      * @param b
      *            third signal
      * @return signal
      */
     <A, B> Signal<Tuple3<T, A, B>> zip(Signal<A> a, Signal<B> b);
 
     /**
      * Returns a new signal that tracks the values of this signal and the two given signals using a tuple.
      * 
      * <b>Creates subscriptions, so this method has side effects.</b> The subscriptions are never removed, so memory
      * leaks may occur depending on the type of the signal.
      * 
      * @param a
      *            second signal
      * @param b
      *            third signal
      * @return signal
      */
     <A, B> Signal<Tuple3<T, A, B>> zip(Signal<A> a, Signal<B> b, CancellationToken token);
 
     /**
      * An immutable signal whose value never changes.
      * 
      * @param <T>
      *            value type
      */
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
 
         /**
          * Creates a new immutable signal with the given initial value.
          * 
          * @param value
          *            value
          * @return immutable signal
          */
         public static <T> Signal.Val<T> create(T value) {
             return new Signal.Val<T>(value);
         }
 
         @Override
         public EventStream<T> nowAndChange() {
             return EventStreams.instant(value);
         }
 
         @Override
         public Signal<T> distinct() {
             return this;
         }
 
     }
 
     /**
      * A mutable signal whose value can change. Also functions as an event sink.
      * 
      * This class is thread-safe.
      * 
      * @param <T>
      *            value type
      */
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
 
         /**
          * Updates the signal with the new value, publishes it to all subscribers, and returns the old value.
          * 
          * @param newValue
          *            new value
          * @return old value
          */
         public T update(T newValue) {
             T old = this.value.getAndSet(newValue);
             eventSource.fire(newValue);
             return old;
         }
 
         /**
          * Updates the signal using the given updater function. The updater function should be side-effect free, because
          * it might be called multiple times in high contention scenarios.
          * 
          * @param updateFunction
          *            function that returns a new value given the old value
          * @return old value
          */
         public T update(Function<T, T> updateFunction) {
             while (true) {
                 T old = this.value.get();
                 T newValue = updateFunction.apply(old);
                 if (this.value.compareAndSet(old, newValue)) {
                     eventSource.fire(newValue);
                     return old;
                 }
             }
         }
 
         /**
          * Creates a new mutable signal with the given initial value.
          * 
          * @param initial
          *            initial value
          * @return mutable signal
          */
         public static <T> Signal.Var<T> create(T initial) {
             return new Signal.Var<T>(initial);
         }
 
     }
 
 }
