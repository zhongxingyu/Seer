 /**
  *  Copyright (c) 2010-2012, The StaccatoCommons Team
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
 
 import static net.sf.staccatocommons.collections.stream.Streams.*;
 import static net.sf.staccatocommons.lang.Compare.*;
 import static net.sf.staccatocommons.lang.tuple.Tuples.*;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import net.sf.staccatocommons.check.Ensure;
 import net.sf.staccatocommons.check.Validate;
 import net.sf.staccatocommons.collections.internal.iterator.DropIterator;
 import net.sf.staccatocommons.collections.internal.iterator.FilterIndexIterator;
 import net.sf.staccatocommons.collections.internal.iterator.FilterIterator;
 import net.sf.staccatocommons.collections.internal.iterator.TakeWhileIterator;
 import net.sf.staccatocommons.collections.iterable.Iterables;
 import net.sf.staccatocommons.collections.iterable.internal.IterablesInternal;
 import net.sf.staccatocommons.collections.stream.internal.ListStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.AppendIterableStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.AppendStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.DeconsTransformStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.DropWhileStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.FlatMapStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.MapStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.MemorizedStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.PrependStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.SortedStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.TakeStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.TransformStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.ZipStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.delayed.DelayedAppendStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.delayed.DelayedDeconsTransformStream;
 import net.sf.staccatocommons.collections.stream.internal.algorithms.delayed.DelayedPrependStream;
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Applicable2;
 import net.sf.staccatocommons.defs.Evaluable;
 import net.sf.staccatocommons.defs.Evaluable2;
 import net.sf.staccatocommons.defs.Executable;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.defs.function.Function;
 import net.sf.staccatocommons.defs.function.Function2;
 import net.sf.staccatocommons.defs.partial.EmptyAware;
 import net.sf.staccatocommons.defs.predicate.Predicate2;
 import net.sf.staccatocommons.defs.reduction.Accumulator;
 import net.sf.staccatocommons.defs.reduction.Reduction;
 import net.sf.staccatocommons.defs.tuple.Tuple2;
 import net.sf.staccatocommons.defs.type.NumberType;
 import net.sf.staccatocommons.iterators.thriter.Thriter;
 import net.sf.staccatocommons.iterators.thriter.Thriterator;
 import net.sf.staccatocommons.lang.Compare;
 import net.sf.staccatocommons.lang.Option;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.lang.function.AbstractFunction2;
 import net.sf.staccatocommons.lang.function.Functions;
 import net.sf.staccatocommons.lang.internal.ToString;
 import net.sf.staccatocommons.lang.predicate.AbstractPredicate2;
 import net.sf.staccatocommons.lang.predicate.Equiv;
 import net.sf.staccatocommons.lang.predicate.Predicates;
 import net.sf.staccatocommons.lang.tuple.Tuples;
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 import net.sf.staccatocommons.restrictions.check.NotNegative;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * An abstract implementation of a {@link Stream}. Only it {@link Iterator}
  * method is abstract
  * 
  * @author flbulgarelli
  * 
  * @param <A>
  */
 public abstract class AbstractStream<A> implements Stream<A> {
 
   protected static final Validate<NoSuchElementException> VALIDATE_ELEMENT = Validate
     .throwing(NoSuchElementException.class);
 
   @Override
   public int size() {
     int size = 0;
     Thriter<A> iter = this.iterator();
     while (iter.hasNext()) {
       iter.advanceNext();
       size++;
     }
     return size;
   }
 
   @Override
   public boolean isEmpty() {
     return iterator().isEmpty();
   }
 
   public void forEach(Executable<? super A> block) {
     for (A element : this)
       block.exec(element);
   }
 
   public Stream<A> each(final Executable<? super A> block) {
     return map(Functions.impure(block));
   }
 
   public boolean contains(A element) {
     return IterablesInternal.containsInternal(this, element);
   }
 
   @Override
   public Stream<A> filter(final Evaluable<? super A> predicate) {
     return Streams.from(new FilterIterator<A>(iterator(), predicate));
   }
 
   public Stream<A> skip(A element) {
     return filter(Predicates.equal(element).not());
   }
 
   @Override
   public Stream<A> takeWhile(final Evaluable<? super A> predicate) {
     return Streams.from(new TakeWhileIterator<A>(iterator(), predicate));
   }
 
   @Override
   public Stream<A> take(@NotNegative final int amountOfElements) {
     return new TakeStream<A>(iterator(), amountOfElements);
   }
 
   @Override
   public Stream<A> dropWhile(Evaluable<? super A> predicate) {
     return new DropWhileStream<A>(this, predicate);
   }
 
   @Override
   public Stream<A> drop(@NotNegative int amountOfElements) {
     return Streams.from(new DropIterator<A>(amountOfElements, iterator()));
   }
 
   @Override
   public A reduce(Applicable2<? super A, ? super A, ? extends A> function) {
     try {
       return Iterables.reduce(this, function);
     } catch (IllegalArgumentException e) { // FIXME why illegal argument ???
       return VALIDATE_ELEMENT.fail("Can not reduce an empty stream");
     }
   }
 
   @Override
   public <B> B reduce(Reduction<? super A, B> reduction) throws NoSuchElementException {
     return Iterables.reduce(this, reduction);
   }
 
   @Override
   public <O> O fold(O initial, Applicable2<? super O, ? super A, ? extends O> function) {
     return Iterables.fold(this, initial, function);
   }
 
   @Override
   public A any() {
     return Iterables.any(this);
   }
 
   @Override
   public Option<A> anyOrNone() {
     return Iterables.anyOrNone(this);
   }
 
   @Override
   public A anyOrNull() {
     return anyOrNone().valueOrNull();
   }
 
   @Override
   public A anyOrElse(Thunk<A> thunk) {
     return anyOrNone().valueOrElse(thunk);
   }
 
   @Override
   public A anyOrElse(A value) {
     return anyOrNone().valueOrElse(value);
   }
 
   @Override
   public A find(Evaluable<? super A> predicate) {
     return Iterables.find(this, predicate);
   }
 
   @Override
   public Option<A> findOrNone(Evaluable<? super A> predicate) {
     return Iterables.findOrNone(this, predicate);
   }
 
   @Override
   public A findOrNull(Evaluable<? super A> predicate) {
     return findOrNone(predicate).valueOrNull();
   }
 
   @Override
   public A findOrElse(Evaluable<? super A> predicate, Thunk<? extends A> thunk) {
     return findOrNone(predicate).valueOrElse(thunk);
   }
 
   @Override
   public A findOrElse(Evaluable<? super A> predicate, A element) {
     return findOrNone(predicate).valueOrElse(element);
   }
 
   @Override
   public boolean all(Evaluable<? super A> predicate) {
     return Iterables.all(this, predicate);
   }
 
   @Override
   public boolean allEquiv() {
     return Iterables.allEqual(this);
   }
 
   @Override
   public boolean allEquivBy(Evaluable2<? super A, ? super A> equivTest) {
     return Iterables.allEquivBy(this, equivTest);
   }
 
   @Override
   public boolean any(Evaluable<? super A> predicate) {
     return Iterables.any(this, predicate);
   }
 
   public <B> Stream<Tuple2<A, B>> clone(Applicable<? super A, ? extends B> function) {
     return map(Tuples.clone(function));
   }
 
   public <B, C> Stream<Tuple2<B, C>> branch(Applicable<? super A, ? extends B> function0,
     Applicable<? super A, ? extends C> function1) {
     return map(Tuples.branch(function0, function1));
   }
 
   @Override
   public <B> Stream<B> map(final Function<? super A, ? extends B> function) {
     return new MapStream<A, B>(this, function);
   }
 
   @Override
   public <B> Stream<B> map(Applicable<? super A, ? extends B> function) {
     return map(Functions.from(function));
   }
 
   @Override
   public <B> Stream<B> flatMap(final Function<? super A, ? extends Iterable<? extends B>> function) {
     return new FlatMapStream<A, B>(this, function);
   }
 
   public <B> Stream<B> flatMapArray(@NonNull Function<? super A, ? extends B[]> function) {
     return flatMap(AbstractStream.<B> toIterable().of(function));
   }
 
   @Override
   public Stream<A> append(final Iterable<A> other) {
     return new AppendIterableStream<A>(this, other);
   }
 
   public Stream<A> appendUndefined() {
     return append(Streams.<A> undefined());
   }
 
   @Override
   public A first() {
     return get(0);
   }
 
   @Override
   public A second() {
     return get(1);
   }
 
   @Override
   public A third() {
     return get(2);
   }
 
   @Override
   public A last() {
     Thriterator<A> iter = iterator();
     VALIDATE_ELEMENT.that(iter.hasNext(), "Empty streams have no elements");
     while (iter.hasNext())
       iter.advanceNext();
     return iter.current();
   }
 
   @Override
   public A get(int n) {
     Thriterator<A> iter = this.iterator();
     for (int i = 0; i <= n; i++)
       try {
         iter.advanceNext();
       } catch (NoSuchElementException e) {
         throw new IndexOutOfBoundsException("At " + n);
       }
     return iter.current();
   }
 
   public final Stream<A> filterIndex(Evaluable<Integer> predicate) {
     return Streams.from(new FilterIndexIterator<A>(iterator(), predicate));
   }
 
   public final Stream<A> skipIndex(int index) {
     return filterIndex(Predicates.equal(index).not());
   }
 
   @Override
   public int indexOf(A element) {
     return Iterables.indexOf(this, element);
   }
 
   @Override
   public final int positionOf(A element) {
     int index = indexOf(element);
     if (index == -1)
       throw new NoSuchElementException(element.toString());
     return index;
   }
 
   @Override
   public boolean isBefore(A previous, A next) {
     return Iterables.isBefore(this, previous, next);
   }
 
   @Override
   public Set<A> toSet() {
     return Iterables.toSet(this);
   }
 
   @Override
   public List<A> toList() {
     return Iterables.toList(this);
   }
 
   @Override
   public Stream<A> force() {
     return new ListStream<A>(toList()) {
       @Override
       public List<A> toList() {
         return Collections.unmodifiableList(getList());
       }
     };
   }
 
   @Override
   public Stream<A> memorize() {
     return new MemorizedStream<A>(iterator());
   }
 
   @Override
   public A[] toArray(Class<? super A> clazz) {
     return toArray(clazz, toList());
   }
 
   protected A[] toArray(Class<? super A> clazz, Collection<A> readOnlyColView) {
     return readOnlyColView.toArray((A[]) Array.newInstance(clazz, readOnlyColView.size()));
   }
 
   @Override
   public String joinStrings(@NonNull String separator) {
     return StringUtils.join(iterator(), separator);
   }
 
   @Override
   public Tuple2<List<A>, List<A>> partition(Evaluable<? super A> predicate) {
     return Iterables.partition(this, predicate);
   }
 
   @Override
   public final Tuple2<Stream<A>, Stream<A>> streamPartition(Evaluable<? super A> predicate) {
     Tuple2<List<A>, List<A>> partition = partition(predicate);
     return _(Streams.from(partition._0()), Streams.from(partition._1()));
   }
 
   @Override
   public final boolean equiv(A... elements) {
     return equiv(Arrays.asList(elements));
   }
 
   @Override
   public boolean equiv(Iterable<? extends A> other) {
     return equivBy(equalOrEquiv(), other);
   }
 
   @Constant
   private static <A> Predicate2<A, A> equalOrEquiv() {
     return Equiv.<A> equalNullSafe().or(new AbstractPredicate2<A, A>() {
       public boolean eval(A arg0, A arg1) {
         return arg0 instanceof Stream<?> && arg1 instanceof Stream<?>
           && ((Stream) arg0).equiv((Stream) arg1);
       }
     });
   }
 
   @Override
   public boolean equivBy(Evaluable2<? super A, ? super A> equalty, Iterable<? extends A> other) {
     return Iterables.equivBy(this, other, equalty);
   }
 
   @Override
   public final boolean equivBy(Evaluable2<? super A, ? super A> equalityTest, A... elements) {
     return equivBy(equalityTest, Arrays.asList(elements));
   }
 
   @Override
   public final <B> boolean equivOn(Applicable<? super A, ? extends B> function,
     Iterable<? extends A> iterable) {
     return equivBy(Equiv.on(function), iterable);
   }
 
   @Override
   public final <B> boolean equivOn(Applicable<? super A, ? extends B> function, A... elements) {
     return equivOn(function, Arrays.asList(elements));
   }
 
   @Override
   public <B> Stream<B> transform(final Applicable<Stream<A>, ? extends Stream<B>> function) {
     return new TransformStream<A, B>(this, function);
   }
 
   /**
    * <pre>
    * intersperse _   []      = []
    * intersperse _   [x]     = [x]
    * intersperse sep (x:xs)  = x : sep : intersperse sep xs
    * </pre>
    * 
    */
   @Override
   public Stream<A> intersperse(final A sep) {
     return transform(new AbstractDelayedDeconsApplicable<A, A>() {
       public Stream<A> apply(Thunk<A> head, Stream<A> tail) {
         if (tail.isEmpty())
           return cons(head);
         return cons(head, cons(sep, tail.intersperse(sep)));
       }
     });
   }
 
   public Stream<A> incorporate(final Function<? super A, ? extends A> function) {
     return flatMap(new AbstractFunction<A, Iterable<? extends A>>() {
       public Iterable<? extends A> apply(A arg) {
         return cons(arg, function.apply(arg));
       }
     });
   }
 
   public Stream<A> incorporate(A element) {
     return incorporate(Functions.constant(element));
   }
 
   @Override
   public Stream<A> append(A element) {
     return new AppendStream<A>(this, element);
   }
 
   @Override
   public Stream<A> append(Thunk<A> element) {
     return new DelayedAppendStream<A>(this, element);
   }
 
   @Override
   public Stream<A> prepend(A element) {
     return new PrependStream<A>(element, this);
   }
 
   @Override
   public Stream<A> prepend(Thunk<A> element) {
     return new DelayedPrependStream<A>(element, this);
   }
 
   @Override
   public <B> Stream<B> transform(final DeconsApplicable<A, B> function) {
     return new DeconsTransformStream<B, A>(this, function);
   }
 
   @Override
   public <B> Stream<B> transform(final DelayedDeconsApplicable<A, B> function) {
     return new DelayedDeconsTransformStream<A, B>(this, function);
   }
 
   @Override
   public <B> Stream<Tuple2<A, B>> zip(Iterable<B> iterable) {
     return zip(iterable, Tuples.<A, B> toTuple2());
   }
 
   @Override
   public Tuple2<A, Stream<A>> decons() {
     Iterator<A> iter = iterator();
     VALIDATE_ELEMENT.that(iter.hasNext(), "Empty streams can not be deconstructed");
     return _(iter.next(), Streams.from(iter));
   }
 
   @Override
   public Tuple2<Thunk<A>, Stream<A>> delayedDecons() {
     Thriterator<A> iter = iterator();
     VALIDATE_ELEMENT.that(iter.hasNext(), "Empty streams can not be deconstructed");
     return _(iter.delayedNext(), Streams.from(iter));
   }
 
   @Override
   public Stream<A> tail() {
     VALIDATE_ELEMENT.that(!isEmpty(), "Empty streams have not tail");
     return drop(1);
   }
 
   @Override
   public A head() {
     try {
       return first();
     } catch (IndexOutOfBoundsException e) {
       return VALIDATE_ELEMENT.fail("Empty streams have no head");
     }
   }
 
   public <B, C> Stream<C> zip(@NonNull final Iterable<B> iterable,
     @NonNull final Function2<A, B, C> function) {
     return new ZipStream<C, A, B>(this, iterable, function);
   }
 
   @Override
   public A sum(NumberType<A> numberType) {
     return Iterables.sum(this, numberType);
   }
 
   @Override
   public A product(NumberType<A> numberType) {
     return Iterables.product(this, numberType);
   }
 
   @Override
   public A average(final NumberType<A> numberType) {
     class Ref {
       A val = numberType.zero();
     }
     final Ref size = new Ref();
     return numberType.divide(fold(numberType.zero(), new AbstractFunction2<A, A, A>() {
       public A apply(A arg0, A arg1) {
         size.val = numberType.increment(size.val);
         return numberType.add(arg0, arg1);
       }
     }), size.val);
   }
 
   @Override
   public A maximum() {
     return maximumBy(natural());
   }
 
   @Override
   public A minimum() {
     return minimumBy(natural());
   }
 
   @Override
   public A maximumBy(Comparator<? super A> comparator) {
     return reduce(max(comparator));
   }
 
   @Override
   public A minimumBy(Comparator<? super A> comparator) {
     return reduce(min(comparator));
   }
 
   @Override
   public final <B extends Comparable<B>> A maximumOn(Applicable<? super A, B> function)
     throws NoSuchElementException {
     return maximumBy(Compare.on(function));
   }
 
   @Override
   public final <B extends Comparable<B>> A minimumOn(Applicable<? super A, B> function)
     throws NoSuchElementException {
     return minimumBy(Compare.on(function));
   }
 
   public Stream<A> sort() {
     return sortBy(natural());
   }
 
   public Stream<A> sortBy(Comparator<A> comparator) {
     return new SortedStream<A>(this, comparator);
   }
 
   public final <B extends Comparable<B>> Stream<A> sortOn(Applicable<? super A, B> function) {
     return sortBy(Compare.on(function));
   }
 
   private Comparator<A> natural() {
     return (Comparator<A>) Compare.<Comparable> natural();
   }
 
   public Stream<A> reverse() {
     if (this.isEmpty())
       return Streams.empty();
     LinkedList<A> reversedList = new LinkedList<A>();
     for (A element : this)
       reversedList.addFirst(element);
     return Streams.from((List<A>) reversedList);
   }
 
   public <K, V> Map<K, V> groupOn(Applicable<? super A, K> groupFunction, Reduction<A, V> reduction) {
     Map<K, Accumulator<A, V>> map = new LinkedHashMap<K, Accumulator<A, V>>();
 
     for (A element : this) {
       K key = groupFunction.apply(element);
       Accumulator<A, V> accum = map.get(key);
 
       if (accum == null) {
         accum = reduction.newAccumulator();
         map.put(key, accum);
       }
       accum.accumulate(element);
     }
     for (Entry<K, Accumulator<A, V>> e : map.entrySet())
       ((Entry<K, V>) e).setValue(e.getValue().value());
     return (Map<K, V>) map;
   }
 
   public Stream<Tuple2<A, A>> cross() {
     return cross(this);
   }
 
   public <B> Stream<Tuple2<A, B>> cross(@NonNull Iterable<B> other) {
     return cross(Streams.from(other));
   }
 
   // this >>= (\x -> other >>= (\y -> return (x,y)))
   public <B> Stream<Tuple2<A, B>> cross(@NonNull final Stream<B> other) {
     return transform(new AbstractFunction<Stream<A>, Stream<Tuple2<A, B>>>() {
       public Stream<Tuple2<A, B>> apply(Stream<A> stram) {
         return flatMap(new AbstractFunction<A, Stream<Tuple2<A, B>>>() {
           public Stream<Tuple2<A, B>> apply(final A x) {
             return other.flatMap(new AbstractFunction<B, Stream<Tuple2<A, B>>>() {
               public Stream<Tuple2<A, B>> apply(B y) {
                 return cons(_(x, y));
               }
             });
           }
         });
       }
     });
   }
 
   public Stream<Stream<A>> crossStreams(@NonNull Stream<Stream<A>> other) {
     Ensure.that().isNotEmpty("other", (EmptyAware) other);
     return fcross(other.prepend(this));
   }
 
   // fcross [xs,ys] = xs >>= \x -> ys >>= \y -> return [x,y]
   // fcross (xs:xss) = xs >>= \x -> (fcross xss) >>= \ys -> return (x:ys)
   private static <A> Stream<Stream<A>> fcross(Stream<Stream<A>> other) {
     return other.transform(new AbstractFunction<Stream<Stream<A>>, Stream<Stream<A>>>() {
       public Stream<Stream<A>> apply(Stream<Stream<A>> xss_) {
         final Stream<Stream<A>> xss = xss_.memorize();
         if (xss.size() == 2)
           return xss.first().flatMap(new AbstractFunction<A, Stream<Stream<A>>>() {
             public Stream<Stream<A>> apply(final A x) {
               return xss.second().flatMap(new AbstractFunction<A, Stream<Stream<A>>>() {
                 public Stream<Stream<A>> apply(A y) {
                   return cons(cons(x, y));
                 }
               });
             }
           });
 
         return xss.head().flatMap(new AbstractFunction<A, Stream<Stream<A>>>() {
           public Stream<Stream<A>> apply(final A x) {
             return fcross(xss.tail()).flatMap(new AbstractFunction<Stream<A>, Stream<Stream<A>>>() {
               public Stream<Stream<A>> apply(Stream<A> ys) {
                 return cons(cons(x, ys));
               }
             });
           }
         });
       }
     });
   }
 
   @Override
   public final void print(java.lang.Appendable o) throws IOException {
     o.append('[');
    if(!isEmpty()) {
      Tuple2<A, Stream<A>> ht = decons();
      printElement(o, ht._0());
      for (A element : ht._1()) {
        o.append(", ");
        printElement(o, element);
      }
     }
     o.append(']');
   }
 
   private void printElement(java.lang.Appendable o, A element) throws IOException {
     if (element instanceof Stream<?>)
       ((Stream<?>) element).print(o);
     else
       o.append(String.valueOf(element));
   }
 
   @Override
   public final void print() {
     try {
       println(System.out);
     } catch (IOException e) {
       throw new AssertionError(e);
     }
   }
 
   @Override
   public final void println(java.lang.Appendable o) throws IOException {
     print(o);
     o.append('\n');
   }
 
   @Override
   public final void println() {
     try {
       println(System.out);
     } catch (IOException e) {
       throw new AssertionError(e);
     }
   }
 
   @Override
   public final String printString() {
     try {
       StringBuilder sb = new StringBuilder();
       print(sb);
       return sb.toString();
     } catch (IOException e) {
       throw new AssertionError(e);
     }
   }
 
   @Constant
   private static <A> Function<A[], Iterable<A>> toIterable() {
     return new AbstractFunction<A[], Iterable<A>>() {
       public Iterable<A> apply(A[] arg) {
         return Arrays.asList(arg);
       }
     };
   }
 
   public final String toString() {
     return ToString.toString(this);
   }
 
 }
