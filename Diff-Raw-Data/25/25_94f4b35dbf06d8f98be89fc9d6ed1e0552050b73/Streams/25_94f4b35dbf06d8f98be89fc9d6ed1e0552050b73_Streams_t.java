 /**
  *  Copyright (c) 2011, The Staccato-Commons Team
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation; version 3 of the License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  */
 
 package net.sf.staccatocommons.collections.stream;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.staccatocommons.collections.restrictions.Projection;
 import net.sf.staccatocommons.collections.restrictions.Repeatable;
 import net.sf.staccatocommons.collections.stream.internal.CharSequenceStream;
 import net.sf.staccatocommons.collections.stream.internal.CollectionStream;
 import net.sf.staccatocommons.collections.stream.internal.DequeStream;
 import net.sf.staccatocommons.collections.stream.internal.EmptyStream;
 import net.sf.staccatocommons.collections.stream.internal.IterableStream;
 import net.sf.staccatocommons.collections.stream.internal.IteratorStream;
 import net.sf.staccatocommons.collections.stream.internal.ListStream;
 import net.sf.staccatocommons.collections.stream.internal.SingleStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.DelayedRepatIterator;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.IterateStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.RepeatIterator;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.UndefinedStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.delayed.ConsStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.delayed.DelayedSingleStream;
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Evaluable;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.iterators.EnumerationIterator;
 import net.sf.staccatocommons.lang.internal.Add;
 import net.sf.staccatocommons.lang.predicate.Predicates;
 import net.sf.staccatocommons.lang.thunk.Thunks;
 import net.sf.staccatocommons.restrictions.Conditionally;
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 import net.sf.staccatocommons.restrictions.processing.IgnoreRestrictions;
 
 /**
  * Class methods for creating very simple {@link Stream}s wrapping existing
  * classes from the Java collections framework, specifiying its elements and
  * 
  * @author flbulgarelli
  */
 public class Streams {
 
   private Streams() {}
 
   /**
    * Creates a new {@link Stream} that retrieves elements from a head's thunk,
    * and another {@link Iterable}, called tail.
    * 
    * This operation is known and <em>cons(tructing)</em>, and can be undone by
    * sending {@link Stream#delayedDecons()} to the resulting Stream.
    * 
    * The returned stream is {@link Repeatable} as long as the thunk's head value
    * is always equal, and the tail is repeatable.
    * 
    * @param <A>
    * @param head
    * @param tail
    * @return a new {@link Stream}
    */
   @Projection
   @Conditionally(Repeatable.class)
   public static <A> Stream<A> cons(final Thunk<A> head, @NonNull final Stream<? extends A> tail) {
     return new ConsStream<A>(head, (Stream<A>) tail);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves elements from a head, and
    * another {@link Iterable}, called tail.
    * 
    * This operation is known and <em>cons(tructing)</em>, and can be undone by
    * sending {@link Stream#decons()} to the resulting Stream.
    * 
    * * The returned stream is {@link Repeatable} as long as the tail is
    * repeatable.
    * 
    * @param <A>
    * @param head
    * @param tail
    * @return a new {@link Stream}
    */
   @Projection
   @Conditionally(Repeatable.class)
   public static <A> Stream<A> cons(final A head, @NonNull final Stream<? extends A> tail) {
     return new ConsStream<A>(Thunks.constant(head), (Stream<A>) tail);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves the elements from the given
    * array. This stream permits efficient random access and grants repeatable
    * iteration order.
    * 
    * @param <A>
    *          the element type
    * @param elements
    *          the array that is Stream source
    * @return a new stream that gets its elements from an array
    */
   @Repeatable
   @Projection
   public static <A> Stream<A> cons(@NonNull A... elements) {
     return from(Arrays.asList(elements));
   }
 
   /**
    * Creates a one-element new Stream that will retrieve the thunk's value.
    * 
    * This stream is {@link Repeatable} as long as the thunk's value is always
    * equal.
    * 
    * @param <A>
    * @param element
    * @return a new
    * 
    * @see Thunk#value()
    */
   @Projection
   @Repeatable
   public static <A> Stream<A> cons(Thunk<A> element) {
     return new DelayedSingleStream(element);
   }
 
   /**
    * Creates a new Stream that will retrieve just the given element
    * 
    * @param <A>
    * @param element
    *          the single element the new {@link Stream} will retrieve
    * @return a new {@link Stream}
    */
   @Repeatable
   @Projection
   public static <A> Stream<A> cons(A element) {
     return new SingleStream<A>(element);
   }
 
   /**
    * Creates a new infinite {@link Stream} that retrieves element from the
    * sequence
    * <code>Sequence.from(start, generator, StopConditions.stopNever())</code>
    * 
    * @param <A>
    * @param seed
    *          the initial element of the sequence
    * @param generator
    *          a function used to generated each element from the sequence after
    *          the initial element
    * @return a new {@link Stream}
    * @see IterateStream#from(Object, Applicable, Evaluable)
    */
   @Projection
   @IgnoreRestrictions
   public static <A> Stream<A> iterate(@NonNull A seed,
     @NonNull Applicable<? super A, ? extends A> generator) {
     return new IterateStream(seed, generator);
   }
 
   /**
    * Iterates from the starting integer, adding 1. This generates the infinite
    * stream {@code [start, start+1, start+2...]}
    * 
   * @param start
   *          the initial element of the sequence
   * @return an stream that retrieves the sequence
   *         {@code [start, start+1, start+2...]}
    * @since 1.2
    */
   @Projection
   public static Stream<Integer> enumerate(int start) {
     return iterate(start, Add.one());
   }
 
  /**
   * Iterates from the {@code start} integer, by adding 1, up to the
   * {@code stop} integer, inclusive. This generates the finite stream
   * {@code [start, start+1, start+2..., stop]}
   * 
   * @param start
   *          the initial element of the sequence
   * @param stop
   *          the final element of the sequence
   * @return an stream that retrieves the sequence
   *         {@code [start, start+1, start+2..., stop]}. As a particular case,
   *         if start == stop, the sequence {@code [start]} is retrieved. If
   *         start > stop, an empty stream is retrieved
   * @since 1.2
   */
   @Projection
   public static Stream<Integer> enumerate(int start, int stop) {
     return enumerate(start, stop, 1);
   }
 
   @Projection
   public static Stream<Integer> enumerate(int start, int stop, int step) {
    return iterate(start, Add.add(step)).take(Math.max(0, 1 + (stop - start) / step));
   }
 
   // @Projection
   // public static <A> Stream<A> enumerate(A start, EnumType<A> type) {
   // return iterate(start, type.increment());
   // }
   //
   // @Projection
   // public static <A> Stream<A> enumerate(A start, A stop, EnumType<A> type) {
   // return enumerate(start, type).take(type.increment(type.subtract(stop,
   // start)));
   // }
 
   /**
    * Creates a new infinite {@link Stream} that retrieves element from the
    * sequence
    * <code>Sequence.from(start, generator, StopConditions.stopNever())</code>
    * 
    * @param <A>
    * @param seed
    *          the initial element of the sequence
    * @param generator
    *          a function used to generated each element from the sequence after
    *          the initial element
    * @return a new {@link Stream}
    * @see IterateStream#from(Object, Applicable, Evaluable)
    */
   @Projection
   @IgnoreRestrictions
   public static <A> Stream<A> iterateUntilNull(@NonNull A seed,
     @NonNull Applicable<? super A, ? extends A> generator) {
     return iterate(seed, generator).takeWhile(Predicates.notNull());
   }
 
   /**
    * Answers an infinite Stream that indefinitely retrieves the given element.
    * 
    * @param <A>
    * @param element
    * @return a new {@link Stream} that repeats the given element
    */
   @Projection
   public static <A> Stream<A> repeat(final A element) {
     return from(new RepeatIterator<A>(element));
   }
 
   /**
    * Answers an infinite Stream that indefinitely retrieves the given thunk's
    * value.
    * 
    * @param <A>
    * @param thunk
    *          the {@link Thunk} whose value to repeat
    * @return a new {@link Stream}
    */
   @Projection
   public static <A> Stream<A> repeat(@NonNull final Thunk<A> thunk) {
     return from(new DelayedRepatIterator<A>(thunk));
   }
 
   // private static <A> Stream<A> cycle(@NonNull A element) {
   // //return iterate(element, Functions.<A> identity());
   // }
 
   /**
    * Create a new {@link Stream} that retrieves elements from the given
    * Iterable.
    * 
    * @param <A>
    *          the element type
    * @param iterable
    *          the {@link Iterable} to decorate
    * @return the given iterable, if it is {@link Stream}, a new stream that
    *         wraps it, otherwise
    */
   @Projection
   @Conditionally(Repeatable.class)
   public static <A> Stream<A> from(@NonNull Iterable<? extends A> iterable) {
     return iterable instanceof Stream ? (Stream<A>) iterable : new IterableStream<A>(iterable);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves elements from the given
    * iterator. The resulting stream can not be iterated more than once, thus it
    * is inherently mutable
    * 
    * @param <A>
    * @param iterator
    *          source of the the new {@link Stream}
    * @return a new {@link Stream}
    */
   @Projection
   @IgnoreRestrictions
   public static <A> Stream<A> from(@NonNull Iterator<? extends A> iterator) {
     return new IteratorStream<A>(iterator);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves elements from the given
    * {@link Enumeration}. The resulting stream can not be iterated more than
    * once, thus it is inherently mutable
    * 
    * @param <A>
    * @param enumeration
    *          source of the new {@link Stream}
    * @return a new {@link Stream}
    */
   @Projection
   public static <A> Stream<A> from(@NonNull Enumeration<? extends A> enumeration) {
     return from(new EnumerationIterator<A>(enumeration));
   }
 
   /**
    * Creates a new {@link Stream} that retrieves character elements from the
    * given charSequence
    * 
    * @param charSequence
    *          source of the of characters of the new Stream
    * @return a new {@link Stream}
    */
   @Repeatable
   @Projection
   public static Stream<Character> from(@NonNull final CharSequence charSequence) {
     return new CharSequenceStream(charSequence);
   }
 
   /**
    * Creates a new {@link Stream} that will retrieve elements from the given
    * collection
    * 
    * @param <A>
    * @param collection
    *          source of the new {@link Stream}
    * @return a new {@link Stream}
    */
   @Repeatable
   @Projection
   public static <A> Stream<A> from(@NonNull Collection<? extends A> collection) {
     return new CollectionStream<A>(collection);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves elements from a list. This
    * streams grants repeatable iterator order and supports (not necessary
    * efficient) random access by index.
    * 
    * @param <A>
    *          the element type
    * @param list
    *          the source of the new {@link Stream}
    * @return a new {@link Stream}
    */
   @Repeatable
   @Projection
   public static <A> Stream<A> from(@NonNull List<? extends A> list) {
     return new ListStream<A>(list);
   }
 
   /**
    * Creates a new {@link Stream} that retrieves elements from a {@link Deque}.
    * This streams grants repeatable iterator order and supports (not necessary
    * efficient) random access by index. This stream can be lazily reversed.
    * 
    * @param <A>
    *          the element type
    * @param list
    *          the source of the new {@link Stream}
    * @return a new {@link Stream}
    */
   @NonNull
   @Repeatable
   @Projection
   public static <A> Stream<A> from(@NonNull Deque<? extends A> list) {
     return new DequeStream<A>(list);
   }
 
   // public static <K, V> Stream<Entry<K, V>> from(Map<K, V> iterable) {
   // return new MapEntryStream<K, V>(iterable);
   // }
 
   /**
    * Answers a {@link Stream} that has no elements. This stream is immutable and
    * singleton
    * 
    * @param <A>
    *          the element type
    * @return a singleton empty {@link Stream}
    */
   @Constant
   @Repeatable
   public static <A> Stream<A> empty() {
     return EmptyStream.empty();
   }
 
   /**
    * Answers a one element {@link Stream} that throws an exception when trying
    * to access its element.
    * 
    * @param <A>
    * @return a single elemtn Stream that throws an exception when accessing its
    *         only element
    */
   @Constant
   public static <A> Stream<A> undefined() {
     return UndefinedStream.undefined();
   }
 
   /**
    * Answers the Stream class, but preserving its element generic type. This
    * method is mostly aimed to be used with Staccato-Commons-Lambda:
    * 
    * <pre>
    * lambda($(Streams.&lt;User&gt; type()).toList())
    * </pre>
    * 
    * @param <A>
    * @return (Class&lt;Stream&lt;A&gt;&gt;) Stream.class
    */
   @Constant
   public static <A> Class<Stream<A>> type() {
     return (Class) Stream.class;
   }
 
 }
