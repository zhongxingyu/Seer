 package org.wixpress.hoopoe.lambda;
 
 import org.hamcrest.Factory;
 import org.junit.Test;
 
 import java.util.*;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.wixpress.hoopoe.lambda.Lambdas.*;
 
 /**
  * @author Yoav
  * @since 10/3/11
  */
 
 public class TestSAM {
 
     interface Adder {
         public int add(int a, int b);
     }
 
     @Test
     public void testSAM() {
         Adder f4 = Lambda(Adder.class).build("a+b");
         assertThat(f4.add(1, 2), is(3));
     }
 
     public interface MyComparator<T> {
         int compare(T o1, T o2);
     }
 
     @Test
     public void testGenericSAM() {
         @SuppressWarnings({"unchecked"})
         MyComparator<Integer> f4 = Lambda(MyComparator.class, Integer.class).build("a==b?1:-1");
         assertThat(f4.compare(1, 1), is(1));
     }
 
     @Test
     public void testSortListUsingSAM() {
         List<Integer> aList = buildList(Integer.class, 1,4,3,5,2,7,6);
         //noinspection unchecked
         Collections.sort(aList, Lambda(Comparator.class, Integer.class).build("(a>b?1:(a<b?-1:0))"));
         assertThat(aList.get(0), is(1));
         assertThat(aList.get(1), is(2));
         assertThat(aList.get(2), is(3));
         assertThat(aList.get(3), is(4));
         assertThat(aList.get(4), is(5));
     }
 
     @Factory
     private <T> Comparator<T> Comparator(Class<T> ofType, String code) {
         //noinspection unchecked
         return Lambda(Comparator.class, ofType).build(code);
     }
 
     @Test
     public void testGenericSAMWithFactory() {
         List<Integer> aList = buildList(Integer.class, 1,4,3,5,2,7,6);
         Collections.sort(aList, Comparator(Integer.class, "(a>b?1:(a<b?-1:0))"));
         assertThat(aList.get(0), is(1));
         assertThat(aList.get(1), is(2));
         assertThat(aList.get(2), is(3));
         assertThat(aList.get(3), is(4));
         assertThat(aList.get(4), is(5));
     }
 
     private <V> List<V> buildList(Class<V> itemType, V ... values) {
         List<V> aList = new ArrayList<V>();
         aList.addAll(Arrays.asList(values));
         return aList;
     }
 
 }
