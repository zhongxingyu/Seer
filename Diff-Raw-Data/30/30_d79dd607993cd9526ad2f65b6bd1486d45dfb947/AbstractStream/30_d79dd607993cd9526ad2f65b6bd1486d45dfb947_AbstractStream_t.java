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
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import net.sf.staccatocommons.check.Ensure;
 import net.sf.staccatocommons.check.Validate;
 import net.sf.staccatocommons.collections.internal.ToPair;
 import net.sf.staccatocommons.collections.iterable.Iterables;
 import net.sf.staccatocommons.collections.iterable.internal.IterablesInternal;
 import net.sf.staccatocommons.collections.stream.impl.ListStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.AppendIterableStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.AppendStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.DeconsTransformStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.DropStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.DropWhileStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.FilterIndexStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.FilterStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.FlatMapStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.GroupByStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.MapStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.MemorizedStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.PrependStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.SortedStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.TakeStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.TakeWhileStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.TransformStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.ZipStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.delayed.DelayedAppendStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.delayed.DelayedDeconsTransformStream;
 import net.sf.staccatocommons.collections.stream.impl.internal.delayed.DelayedPrependStream;
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Applicable2;
 import net.sf.staccatocommons.defs.EmptyAware;
 import net.sf.staccatocommons.defs.Evaluable;
 import net.sf.staccatocommons.defs.Evaluable2;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.defs.function.Function;
 import net.sf.staccatocommons.defs.function.Function2;
 import net.sf.staccatocommons.defs.type.NumberType;
 import net.sf.staccatocommons.iterators.thriter.Thriter;
 import net.sf.staccatocommons.iterators.thriter.Thriterator;
 import net.sf.staccatocommons.lang.Compare;
 import net.sf.staccatocommons.lang.Option;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.lang.function.AbstractFunction2;
 import net.sf.staccatocommons.lang.internal.ToString;
 import net.sf.staccatocommons.lang.predicate.AbstractPredicate;
 import net.sf.staccatocommons.lang.predicate.Equiv;
 import net.sf.staccatocommons.lang.predicate.Predicates;
 import net.sf.staccatocommons.lang.tuple.Pair;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 import net.sf.staccatocommons.restrictions.check.NotNegative;
 import net.sf.staccatocommons.restrictions.processing.ForceRestrictions;
 
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
 
   @Override
   public boolean contains(A element) {
     return IterablesInternal.containsInternal(this, element);
   }
 
   @Override
   public Stream<A> filter(final Evaluable<? super A> predicate) {
     return new FilterStream<A>(this, predicate);
   }
 
   public Stream<A> skip(A element) {
     return filter(Predicates.equal(element).not());
   }
 
   @Override
   public Stream<A> takeWhile(final Evaluable<? super A> predicate) {
     return new TakeWhileStream<A>(this, predicate);
   }
 
   @Override
   public Stream<A> take(@NotNegative final int amountOfElements) {
     return new TakeStream<A>(this, amountOfElements);
   }
 
   @Override
   public Stream<A> dropWhile(Evaluable<? super A> predicate) {
     return new DropWhileStream<A>(this, predicate);
   }
 
   @Override
   public Stream<A> drop(@NotNegative int amountOfElements) {
     return new DropStream<A>(this, amountOfElements);
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
 
   @Override
   public <B> Stream<B> map(final Function<? super A, ? extends B> function) {
     return new MapStream<A, B>(this, function);
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
     return new FilterIndexStream<A>(this, predicate);
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
     return new MemorizedStream<A>(this);
   }
 
   @Override
   public A[] toArray(Class<? super A> clazz) {
     return toArray(clazz, toList());
   }
 
   protected A[] toArray(Class<? super A> clazz, Collection<A> readOnlyColView) {
     return readOnlyColView.toArray((A[]) Array.newInstance(clazz, readOnlyColView.size()));
   }
 
   @Override
   @ForceRestrictions
   public String joinStrings(@NonNull String separator) {
     return StringUtils.join(iterator(), separator);
   }
 
   @Override
   public Pair<List<A>, List<A>> partition(Evaluable<? super A> predicate) {
     return Iterables.partition(this, predicate);
   }
 
   @Override
   public final Pair<Stream<A>, Stream<A>> streamPartition(Evaluable<? super A> predicate) {
     Pair<List<A>, List<A>> partition = partition(predicate);
     return _(Streams.from(partition._0()), Streams.from(partition._1()));
   }
 
   @Override
   public final boolean equiv(A... elements) {
     return equiv(Arrays.asList(elements));
   }
 
   @Override
   public boolean equiv(Iterable<? extends A> other) {
     return Iterables.equiv(this, other);
   }
 
   @Override
   public boolean equivBy(Evaluable2<A, A> equalty, Iterable<? extends A> other) {
     return Iterables.equivBy(this, other, equalty);
   }
 
   @Override
   public final boolean equivBy(Evaluable2<A, A> equalityTest, A... elements) {
     return equivBy(equalityTest, Arrays.asList(elements));
   }
 
   @Override
   public final <B> boolean equivOn(Applicable<? super A, ? extends B> function, Iterable<? extends A> iterable) {
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
   public <B> Stream<Pair<A, B>> zip(Iterable<B> iterable) {
     return zip(iterable, ToPair.<A, B> getInstance());
   }
 
   @Override
   public Pair<A, Stream<A>> decons() {
     Iterator<A> iter = iterator();
    VALIDATE_ELEMENT.that(iter.hasNext(), "Empty streams can not be deconstructed");
    return _(iter.next(), Streams.from(iter)); // FIXME properties are lost
   }
 
   @Override
   public Pair<Thunk<A>, Stream<A>> delayedDecons() {
     Thriterator<A> iter = iterator();
    VALIDATE_ELEMENT.that(iter.hasNext(), "Empty streams can not be deconstructed");
     return _(iter.delayedNext(), Streams.from(iter));
   }
 
   @Override
   public Stream<A> tail() {
    // TODO not very efficient
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
 
   @ForceRestrictions
   public <B, C> Stream<C> zip(@NonNull final Iterable<B> iterable, @NonNull final Function2<A, B, C> function) {
     return new ZipStream<C, A, B>(this, iterable, function);
   }
 
   @Override
   public A sum() {
     return Iterables.sum(this);
   }
 
   @Override
   public A sum(NumberType<A> numberType) {
     return Iterables.sum(this, numberType);
   }
 
   @Override
   public A product() {
     return Iterables.product(this);
   }
 
   @Override
   public A product(NumberType<A> numberType) {
     return Iterables.product(this, numberType);
   }
 
   @Override
   public A average() {
     return average(numberType());
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
   public final <B extends Comparable<B>> A maximumOn(Applicable<? super A, B> function) throws NoSuchElementException {
     return maximumBy(Compare.on(function));
   }
 
   @Override
   public final <B extends Comparable<B>> A minimumOn(Applicable<? super A, B> function) throws NoSuchElementException {
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
 
   @Override
   public NumberType<A> numberType() {
     throw new ClassCastException("Source can not be casted to NumerTypeAware");
   }
 
   public Stream<A> reverse() {
     if (this.isEmpty())
       return Streams.empty();
     LinkedList<A> reversedList = new LinkedList<A>();
     for (A element : this)
       reversedList.addFirst(element);
     return Streams.from((List<A>) reversedList);
   }
 
   // TODO move to top level
   /***
    * TODO pass aggregation function? This case would a particular one of
    * groupBy(pred, concat())
    * 
    * @param pred
    * @return a new {@link Stream}. Although the resulting stream itself is lazy,
    *         its stream elements are not.
    */
   public Stream<Stream<A>> groupBy(final Evaluable2<A, A> pred) {
     return new GroupByStream<A>(this, pred);
   }
 
   public Stream<Pair<A, A>> cross() {
     return cross(this);
   }
 
   public <B> Stream<Pair<A, B>> cross(@NonNull Iterable<B> other) {
     return cross(Streams.from(other));
   }
 
   // this >>= (\x -> other >>= (\y -> return (x,y)))
   @ForceRestrictions
   public <B> Stream<Pair<A, B>> cross(@NonNull final Stream<B> other) {
     return transform(new AbstractFunction<Stream<A>, Stream<Pair<A, B>>>() {
       public Stream<Pair<A, B>> apply(Stream<A> stram) {
         return flatMap(new AbstractFunction<A, Stream<Pair<A, B>>>() {
           public Stream<Pair<A, B>> apply(final A x) {
             return other.flatMap(new AbstractFunction<B, Stream<Pair<A, B>>>() {
               public Stream<Pair<A, B>> apply(B y) {
                 return cons(_(x, y));
               }
             });
           }
         });
       }
     });
   }
 
   // TODO
   <B> Stream<Pair<A, B>> join(Stream<B> other, final Evaluable2<A, B> predicate) {
     return cross(other).filter(new AbstractPredicate<Pair<A, B>>() {
       @Override
       public boolean eval(Pair<A, B> argument) {
         return predicate.eval(argument._0(), argument._1());
       }
     });
   }
 
   @ForceRestrictions
   public Stream<Stream<A>> fullCross(@NonNull Stream<Stream<A>> other) {
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
 
   // TODO equiv should forward to equiv on stream objects
 
   @Override
   public final void print(java.lang.Appendable o) throws IOException {
     o.append('[');
     printElement(o, head());
     for (A element : tail()) {
       o.append(", ");
       printElement(o, element);
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
   public final void print() throws IOException {
     println(System.out);
   }
 
   @Override
   public final void println(java.lang.Appendable o) throws IOException {
     print(o);
     o.append('\n');
   }
 
   @Override
   public final void println() throws IOException {
     println(System.out);
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
