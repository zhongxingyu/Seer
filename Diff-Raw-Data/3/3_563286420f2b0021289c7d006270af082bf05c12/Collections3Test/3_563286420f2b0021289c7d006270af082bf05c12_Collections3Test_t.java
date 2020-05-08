 package com.jayway.wordgraph;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.lessThan;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
// @BEGIN_VERSION FOLD
 import java.util.Deque;
// @END_VERSION FOLD
 import java.util.LinkedList;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.hamcrest.Matcher;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 
 public class Collections3Test {
     @Test
     public void dummyTest() {
     }
 
     // @BEGIN_VERSION TO_BACKGROUND_FUNCTION
     private static final Function<Integer, Integer> timeConsumingCalculation = new Function<Integer, Integer>() {
         public Integer apply(Integer from) {
             try {
                 Thread.sleep(1000);
             }
             catch (InterruptedException e) {
             }
             return from * 2;
         }
     };
     
     @Before
     public void setupStaticFields() {
         Collections3.setTimeout(1000*60);
         Collections3.setExecutorService(Executors.newFixedThreadPool(10));
     }
 
     @Test
     public void backgroundFunctionShouldHaveSameResult() throws Exception {
         String expected = "world";
         Future<String> future = Collections3.toBackgroundFunction(Functions.constant(expected)).apply("hello");
         assertThat(future.get(1, TimeUnit.SECONDS), equalTo(expected));
     }
     
     @Test
     public void backgroundFunctionShouldReturnFast() throws Exception {
         long before = System.currentTimeMillis();
         Future<Integer> future = Collections3.toBackgroundFunction(timeConsumingCalculation).apply(1);
         long after = System.currentTimeMillis();
         assertThat(after-before, lessThan(50L));
         assertThat(future.get(2, TimeUnit.SECONDS), equalTo(2));
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void toBackgroundFunctionUsesGivenExecutorService() throws Exception {
         ExecutorService mockExecutorService = mock(ExecutorService.class);
         Collections3.setExecutorService(mockExecutorService);
         Collections3.toBackgroundFunction(Functions.identity()).apply("whatever");
         verify(mockExecutorService).submit(Mockito.any(Callable.class));
     }
     // @END_VERSION TO_BACKGROUND_FUNCTION
     
     // @BEGIN_VERSION BACKGROUND_TRANSFORM
     @Test
     public void backgroundTransformShouldHaveSameResult() throws Exception {
         String expected = "hello";
         Collection<Future<String>> futures = Collections3.transformInBackground(Collections.<Object>singleton("world"), Functions.constant(expected));
         assertThat(Iterables.getOnlyElement(futures).get(1, TimeUnit.SECONDS), equalTo(expected));
     }
     
     @Test
     public void backgroundTransformShouldPerformFunctionOnlyOnce() throws Exception {
         Function<Object, Integer> countingFunction = new Function<Object, Integer>() {
             private int callCount = 0;
             public Integer apply(Object from) {
                 return ++callCount;
             }
         };
         Collection<Future<Integer>> futures = Collections3.transformInBackground(Collections.<Object>singleton("world"), countingFunction);
         assertThat(Iterables.getOnlyElement(futures).get(1, TimeUnit.SECONDS), equalTo(1));
         assertThat(Iterables.getOnlyElement(futures).get(1, TimeUnit.SECONDS), equalTo(1)); // check twice to force second iteration
         assertThat(countingFunction.apply("qwe"), equalTo(2));
     }
     // @END_VERSION BACKGROUND_TRANSFORM
 
     // @BEGIN_VERSION FROM_FUTURE_FUNCTION
     @SuppressWarnings("unchecked")
     @Test
     public void fromFutureMustUseTimeout() throws Exception {
         Collections3.setTimeout(1000L);
         Future<Object> future = mock(Future.class);
         Collections3.fromFuture().apply(future);
         verify(future).get(1000L, TimeUnit.MILLISECONDS);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void fromFutureShouldGetValue() throws Exception {
         Future<Object> future = Mockito.mock(Future.class);
         Object expected = "result";
         when(future.get(anyLong(), (TimeUnit)anyObject())).thenReturn(expected);
         assertThat(Collections3.fromFuture().apply(future), equalTo(expected));
     }
 
     // @END_VERSION FROM_FUTURE_FUNCTION
 
     // @BEGIN_VERSION GET_ALL
     @SuppressWarnings("unchecked")
     @Test
     public void getAllOnCollectionOfFuturesShouldGetTheFuturesResults() throws Exception {
         Future<Object> future = mock(Future.class);
         Object expected = Collections.singletonList(5);
         when(future.get(anyLong(), (TimeUnit)anyObject())).thenReturn(5);
         assertThat(Collections3.getAll(Collections.singletonList(future)), equalTo(expected));
     }
     // @END_VERSION GET_ALL
 
     // @BEGIN_VERSION REGULAR_TRANSFORM
     @Test
     public void parallelMapShouldRunQuicker() {
         long before = System.currentTimeMillis();
         Collection<Integer> result;
         // @BEGIN_VERSION_ONLY REGULAR_TRANSFORM
         // A regular transform performs the calculation sequentially, which may not always good enough.
         // In this step you're just supposed to run the tests, and maybe think about a solution.
         // No coding. Just run mvn lab:next when you're ready to move on.
         result = Collections2.transform(Arrays.asList(1, 2, 3, 4, 5), timeConsumingCalculation);
         // @END_VERSION_ONLY REGULAR_TRANSFORM
         // @BEGIN_VERSION PARALLEL_TRANSFORM
         result = Collections3.parallelTransform(Arrays.asList(1, 2, 3, 4, 5), timeConsumingCalculation);
         // @END_VERSION PARALLEL_TRANSFORM
         assertThatCollection(result, is(new Integer[] {2, 4, 6, 8, 10}));
         long after = System.currentTimeMillis();
         System.out.println("Transformation took " + (after-before) + " milliseconds");
         assertThat("Transform is running waaaay too slow!", after-before, is(lessThan(1200L)));
     }
     // @END_VERSION REGULAR_TRANSFORM
 
     // @BEGIN_VERSION REDUCE
     @Test
     public void reduceWithPlusShouldAddAllNumbers() {
         Function2<Integer, Integer, Integer> plus = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum + next;
             }
         };
         // 1+2=3
         //     3+3=6
         //         6+4=10 
         assertThat(Collections3.reduce(Arrays.asList(1, 2, 3, 4), plus), is(10));
     }
 
     @Test
     public void reduceWithTimesShouldMultiplyAllNumbers() {
         Function2<Integer, Integer, Integer> times = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum * next;
             }
         };
         // 1*2=2
         //     2*3=6
         //         6*4=24 
         assertThat(Collections3.reduce(Arrays.asList(1, 2, 3, 4), times), is(24));
     }
 
     @Test
     public void reduceWithEmptyCollectionThrowsException() {
         try {
             Collections3.reduce(Collections.emptyList(), null);
             fail("IllegalArgumentException expected");
         } catch (IllegalArgumentException expected) {
             // expected
         }
     }
 
     @Test
     public void reduceWithOneElementReturnsThatElement() {
         String result = Collections3.reduce(Collections.singletonList("whatever"), null);
         assertThat(result, is("whatever"));
     }
     // @END_VERSION REDUCE
 
     // @BEGIN_VERSION FOLD
     @Test
     public void foldWithTimesShouldMultiplyInitialValueWithAllNumbers() {
         Function2<Integer, Integer, Integer> times = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum * next;
             }
         };
         // note the initial value 2
         // 2*1=2
         //     2*2=4
         //         4*3=12
         //             12*4=48
         assertThat(Collections3.fold(Arrays.asList(1, 2, 3, 4), 2, times), is(48));
     }
     
     @Test
     public void foldWithAddFirstShouldReverseListIntoEmptyList() {
         // Deque is an interface that provides addFirst, which we use to reverse
         Function2<Deque<Integer>, Integer, Deque<Integer>> reverse = new Function2<Deque<Integer>, Integer, Deque<Integer>>() {
             public Deque<Integer> apply(Deque<Integer> accum, Integer next) {
                 accum.addFirst(next);
                 return accum;
             }
         };
         Deque<Integer> expected = new LinkedList<Integer>(Arrays.asList(3, 2, 1));
         assertThat(Collections3.fold(Arrays.asList(1, 2, 3), new LinkedList<Integer>(), reverse), is(expected));
     }
     
     @Test
     public void foldWithEmptyCollectionsReturnsInitialValue() {
         assertThat(Collections3.fold(Collections.emptyList(), "some init value", null), is("some init value"));
     }
     // @END_VERSION FOLD
 
     // had trouble getting assertThat to compare a transformed collection with a list or array
     public static void assertThatCollection(Collection<Integer> actual, Matcher<Integer[]> matcher) {
         assertThat(actual.toArray(new Integer[actual.size()]), matcher);
     }
 }
