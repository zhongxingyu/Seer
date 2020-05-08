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
 
 
 package net.sf.staccatocommons.control.monad;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 
 import net.sf.staccatocommons.control.monad.internal.BlockingMonadValue;
 import net.sf.staccatocommons.control.monad.internal.IteratorMonadValue;
 import net.sf.staccatocommons.control.monad.internal.NilMonad;
 import net.sf.staccatocommons.control.monad.internal.SingleMonadValue;
 import net.sf.staccatocommons.control.monad.internal.SubmitMonadValue;
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Evaluable;
 import net.sf.staccatocommons.defs.Executable;
 import net.sf.staccatocommons.defs.ProtoMonad;
 import net.sf.staccatocommons.defs.tuple.Tuple2;
 import net.sf.staccatocommons.lang.Option;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.lang.function.Functions;
 import net.sf.staccatocommons.lang.tuple.Tuples;
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 import net.sf.staccatocommons.restrictions.processing.IgnoreRestrictions;
 
 /**
  * Simple {@link Monad}s and {@link MonadicFunction}s
  * 
  * @author flbulgarelli
  * @since 1.2
  */
 public class Monads {
   
   /*
    * Simple Monads
    */
 
   /**
    * Answers a {@link Monad} that wraps a single element. Evaluating this monad
    * has no effect
    * 
    * @param element
    *          the element to wrap
    * @return a monad that wraps the given element
    */
   public static <A> Monad<A> cons(A element) {
     return from(new SingleMonadValue<A>(element));
   }
 
   /**
    * Answers a {@link Monad} that wraps an array of elements. Evaluating this
    * monad has no effect
    * 
    * @param elements
    *          the elements to wrap
    * @return a monad that wraps the given array of elements
    */
   public static <A> Monad<A> from(A... elements) {
     return from(Arrays.asList(elements));
   }
 
   /**
    * Answers a {@link Monad} that wraps an {@link Iterable} of elements.
    * Evaluating this monad has no effect
    * 
    * @param elements
    *          the elements to wrap
    * @return a monad that wraps the given {@link Iterable} of elements
    */
   public static <A> Monad<A> from(Iterable<? extends A> elements) {
     Iterator<? extends A> iterator = elements.iterator();
     if (iterator.hasNext())
       return from(new IteratorMonadValue<A>(iterator));
     return nil();
   }
 
   public static <A> Monad<A> from(Option<? extends A> element) {
     if (element.isDefined())
       return cons((A) element.value());
     return nil();
   }
 
   public static <A> Monad<A> from(BlockingQueue<? extends A> queue) {
     return from(new BlockingMonadValue<A>(queue));
   }
 
   public static <A> Monad<A> from(MonadicValue<A> monadValue) {
     return new UnboundMonad<A>(monadValue);
   }
 
   /**
    * The async {@link Monad}
    * 
    * @param <A>
    * @param executor
    * @param callable
    * @return
    */
   public static <A> Monad<A> async(final ExecutorService executor, Callable<A> callable) {
     return from(new SubmitMonadValue<A>(executor, callable));
   }
 
   /**
    * The Empty Monad, that is, the monad that has no elements to visit and has
    * no side effect. Binding the nil monad always results in the nil monad
    * 
    * @param <A>
    * @return the constant empty {@link Monad}
    */
   @Constant
   public static <A> Monad<A> nil() {
     return new NilMonad();
   }
   
   /*
    * Simple Monadic Functions 
    */
 
   /**
    * Answers a {@link MonadicFunction} that performs mapping using the given
    * mapping function
    * 
    * @param function
    * @return a new {@link MonadicFunction} that performs mapping, as defined in
    *         {@link ProtoMonad#map(Applicable)}
    */
   public static <A, B> MonadicFunction<A, B> map(
     @NonNull final Applicable<? super A, ? extends B> function) {
     return new AbstractMonadicFunction<A, B>() {
       public Monad<B> apply(A arg) {
         return Monads.cons((B) function.apply(arg));
       }
     };
   }
 
   /**
    * Answers a {@link MonadicFunction} that performs filtering using the given
    * filtering function
    * 
    * @param function
    * @return a new {@link MonadicFunction} that performs filtering, as defined
    *         in {@link ProtoMonad#filter(Applicable)}
    */
   public static <A> MonadicFunction<A, A> filter(@NonNull final Evaluable<? super A> predicate) {
     return new AbstractMonadicFunction<A, A>() {
       public Monad<A> apply(A arg) {
         if (predicate.eval(arg))
           return Monads.cons(arg);
         return Monads.nil();
       }
     };
   }
 
   /**
    * Answers a {@link MonadicFunction} that executes an actions with effect
    * using the given {@code block}
    * 
    * @param block
    * @return a new {@link MonadicFunction}
    */
   @IgnoreRestrictions
   public static <A> MonadicFunction<A, A> each(@NonNull final Executable<? super A> block) {
     return map(Functions.impure(block));
   }
 
   /**
    * Answers a {@link MonadicFunction} that performs
    * {@link Tuples#clone(Applicable)} using the given {@code function}
    * 
    * @param function
    * @return a new {@link MonadicFunction} that performs cloning
    */
   @IgnoreRestrictions
   public static <A, B> MonadicFunction<A, Tuple2<A, B>> clone(
     final Applicable<? super A, ? extends B> function) {
     return map(Tuples.clone(function));
   }
 
   /**
    * Answers a {@link MonadicFunction} that performs
    * {@link Tuples#branch(Applicable)} using the given {@code function}
    * 
    * @param function
    * @return a new {@link MonadicFunction} that performs branching
    */
   @IgnoreRestrictions
   public static <A, B, C> MonadicFunction<A, Tuple2<B, C>> branch(
     final Applicable<? super A, ? extends B> function0,
     final Applicable<? super A, ? extends C> function1) {
     return map(Tuples.branch(function0, function1));
   }
 
   /**
    * The async Monad function
    * 
    * @param <A>
    * @param executor
    * @return
    */
   public static <A> MonadicFunction<A, A> async(final ExecutorService executor) {
     return new AbstractMonadicFunction<A, A>() {
       public Monad<A> apply(final A arg) {
         return Monads.async(executor, new Callable<A>() {
           public A call() throws Exception {
             return arg;
           }
         });
       }
     };
   }
 
   /**
    * The cons monadic function. It takes its argument and lifts into a Monad
    * 
    * @return the {@link MonadicFunction} that performs {@link #cons(Object))} on
    *         its argument
    */
   @Constant
   public static <A> MonadicFunction<A, A> cons() {
     return new AbstractMonadicFunction<A, A>() {
       public Monad<A> apply(A arg) {
         return Monads.cons(arg);
       }
     };
   }
   
   /**
    * The flatMap monadic function that performs flat mapping.
    * 
    * @param function
    * @return a {@link MonadicFunction} that performs flat mapping
    * @since 2.3
    */
  public static <A, B> Applicable<A, Monad<B>> flatMap(
       final Applicable<? super A, ? extends Iterable<? extends B>> function) {
    return new Applicable<A, Monad<B>>() {
       public Monad<B> apply(A arg) {
         return Monads.from(function.apply(arg));
       }
     };
   }
   
  
  public static <A> AbstractFunction<A, Monad<A>> incorporate(final Applicable<? super A, Monad<A>> function) {
    return new AbstractFunction<A, Monad<A>>() {
       public Monad<A> apply(A arg) {
         return Monads.cons(arg).append(function.apply(arg));
       }
     };
   }
 
 }
