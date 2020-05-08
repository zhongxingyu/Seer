 package com.jayway.wordgraph;
 
 
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.ImmutableList.copyOf;
 import static com.jayway.wordgraph.Sneak.sneakyThrow;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Function;
 
 public class Collections3 {
     // @BEGIN_VERSION TO_BACKGROUND_FUNCTION
 	private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
 	private static long timeout = 1000*60;
 
     public static void setExecutorService(ExecutorService executorService) {
         threadPool = executorService;
     }
 
     public static void setTimeout(long l) {
         timeout = l;
     }
 
	public static <F,T> Function<F, Future<T>> toBackgroundFunction(final Function<F, T> f) {
 		return new Function<F, Future<T>>() {
 			public Future<T> apply(final F from) {
 				return threadPool.submit(new Callable<T>() {
 					public T call() throws Exception {
						return f.apply(from);
 					}
 				});
 			}
 		};
 	};
     // @END_VERSION TO_BACKGROUND_FUNCTION
 
 	// @BEGIN_VERSION BACKGROUND_TRANSFORM
    public static <F,T> Collection<Future<T>> transformInBackground(Collection<F> fromCollection, Function<F, T> f) {
        return copyOf(transform(fromCollection, toBackgroundFunction(f)));
     }
     // @END_VERSION BACKGROUND_TRANSFORM
 
     // @BEGIN_VERSION FROM_FUTURE_FUNCTION
     public static <A> Function<Future<A>, A> fromFuture() {
         return new Function<Future<A>, A>() {
             public A apply(Future<A> from) {
                 try {
                     return from.get(timeout, TimeUnit.MILLISECONDS);
                 } catch (Exception e) {
                     throw sneakyThrow(e);
                 }
             }
         };
     }
     // @END_VERSION FROM_FUTURE_FUNCTION
 
     // @BEGIN_VERSION GET_ALL
     public static <A> Collection<A> getAll(Collection<Future<A>> coll) {
         return copyOf(transform(coll, Collections3.<A>fromFuture()));
 //      return copyOf(transform(coll, Collections3.fromFuture())); // does not work
 //      return copyOf(transform(coll, fromFuture())); // does not work
     }
     // @END_VERSION GET_ALL
 
     // @BEGIN_VERSION PARALLEL_TRANSFORM
	public static <F,T> Collection<T> parallelTransform(Collection<F> fromCollection, Function<F, T> f) {
		return getAll(transformInBackground(fromCollection, f));
     }
     // @END_VERSION PARALLEL_TRANSFORM
 
     // @BEGIN_VERSION REDUCE
     public static <A> A reduce(Iterable<A> coll, Function2<? super A, ? super A, ? extends A> f) {
         Iterator<A> iterator = coll.iterator();
         if (!iterator.hasNext()) {
             throw new IllegalArgumentException("coll is not allowed to be empty");
         }
         A result = iterator.next();
         for (; iterator.hasNext();) {
             A next = iterator.next();
             result = f.apply(result, next);
         }
         return result;
     }
     // @END_VERSION REDUCE
 
     // @BEGIN_VERSION FOLD
     public static <A, B> A fold(Iterable<B> coll, A val, Function2<A, B, A> f) {
         A result = val;
         for (B next : coll) {
             result = f.apply(result, next);
         }
         return result;
     }
     // @END_VERSION FOLD
 }
