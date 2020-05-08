 package com.jayway.wordgraph;
 
 import static com.jayway.wordgraph.Collections3.reduce;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 public class Collections3Test {
     @Test
     public void reduceWithPlusShouldAddAllNumbers() {
        Function2<Integer, Integer, Integer> plus = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum + next;
             }
         };
         assertThat(reduce(plus, 0, Arrays.asList(1, 2, 3, 4)), is(10));
     }
 
     @Test
     public void reduceWithTimesShouldMultiplyAllNumbers() {
        Function2<Integer, Integer, Integer> times = new Function2<Integer, Integer, Integer>() {
             public Integer apply(Integer accum, Integer next) {
                 return accum * next;
             }
         };
         assertThat(reduce(times, 1, Arrays.asList(1, 2, 3, 4)), is(24));
     }
 
     @Test
     public void reduceWithAddFirstShouldReverse() {
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
         assertThat(reduce(reverse, new LinkedList<Integer>(), Arrays.asList(1, 2, 3)), is(expected));
     }
 }
