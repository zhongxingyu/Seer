 package com.jayway.wordgraph;
 
 import static com.jayway.wordgraph.Collections3.fold;
 import static com.jayway.wordgraph.Collections3.reduce;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.lessThan;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.LinkedList;
 
 import org.hamcrest.Matcher;
 import org.junit.Test;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 public class Collections3Test {
     @Test
     public void dummyTest() {
     }
 
     // @BEGIN_VERSION PARALLEL_TRANSFORM
     @Test
     public void parallelMapShouldRunQuicker() {
         Function<Integer, Integer> timeConsumingCalculation = new Function<Integer, Integer>() {
 
             public Integer apply(Integer from) {
                 try {
                     Thread.sleep(1000);
                 }
                 catch (InterruptedException e) {
                 }
                 return from * 2;
             }
         };
         long before = System.currentTimeMillis();
         Collection<Integer> result;
         // @BEGIN_VERSION_ONLY REGULAR_TRANSFORM
         result = Collections2.transform(Arrays.asList(1, 2, 3, 4, 5), timeConsumingCalculation);
         // @END_VERSION_ONLY REGULAR_TRANSFORM
        // @END_VERSION_ONLY PARALLEL_TRANSFORM
         result = Collections3.parallelTransform(Arrays.asList(1, 2, 3, 4, 5), timeConsumingCalculation);
         // @END_VERSION_ONLY PARALLEL_TRANSFORM
         assertThatCollection(result, is(new Integer[] {2, 4, 6, 8, 10}));
         long after = System.currentTimeMillis();
         System.out.println("Transformation took " + (after-before) + " milliseconds");
         assertThat("Transform is running waaaay too slow!", after-before, is(lessThan(1200L)));
     }
     // @END_VERSION PARALLEL_TRANSFORM
 
     // @BEGIN_VERSION REDUCE
     @Test
     public void reduceWithPlusShouldAddAllNumbers() {
         Function2<Integer, Integer, Integer> plus = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum + next;
             }
         };
         assertThat(reduce(plus, Arrays.asList(1, 2, 3, 4)), is(10));
     }
 
     @Test
     public void reduceWithTimesShouldMultiplyAllNumbers() {
         Function2<Integer, Integer, Integer> times = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum * next;
             }
         };
         assertThat(reduce(times, Arrays.asList(1, 2, 3, 4)), is(24));
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
         assertThat(fold(times, 2, Arrays.asList(1, 2, 3, 4)), is(48));
     }
     
     @Test
     public void foldWithAddFirstShouldReverseListIntoEmptyList() {
         Function2<Deque<Integer>, Integer, Deque<Integer>> reverse = new Function2<Deque<Integer>, Integer, Deque<Integer>>() {
             public Deque<Integer> apply(Deque<Integer> accum, Integer next) {
                 accum.addFirst(next);
                 return accum;
             }
         };
         Deque<Integer> expected = new LinkedList<Integer>();
         expected.add(3);
         expected.add(2);
         expected.add(1);
         assertThat(fold(reverse, new LinkedList<Integer>(), Arrays.asList(1, 2, 3)), is(expected));
     }
     // @END_VERSION FOLD
 
     // had trouble getting assertThat to compare a transformed collection with a list or array
     public static void assertThatCollection(Collection<Integer> actual, Matcher<Integer[]> matcher) {
         assertThat(actual.toArray(new Integer[actual.size()]), matcher);
     }
 }
