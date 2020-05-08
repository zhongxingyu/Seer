 package com.infomancers.tests;
 
 import com.infomancers.collections.yield.Yielder;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Some base tests.
  */
 public class YielderTests {
 
     @Test
     public void arraySize() {
         final int[] arr = new int[]{1, 3, 5, 7};
 
         Iterable<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 int[] l = arr;
                 yieldReturn(l.length);
             }
         };
 
         Assert.assertEquals(arr.length, (int) it.iterator().next());
     }
 
     @Test
     public void matrixSize() {
         final int[][] mat = new int[5][5];
         mat[2][2] = 5;
 
         Iterable<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 int[][] l = mat;
                 yieldReturn(l.length);
             }
         };
 
         Assert.assertEquals(mat.length, (int) it.iterator().next());
     }
 
     @Test
     public void arrayAccess() {
         final int[] arr = new int[]{1, 3, 5, 7};
 
         Iterable<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 int[] l = arr;
                 yieldReturn(l[2]);
             }
         };
 
         Assert.assertEquals(arr[2], (int) it.iterator().next());
     }
 
     @Test
     public void matrixAccess() {
         final int[][] mat = new int[5][5];
         mat[2][2] = 5;
 
         Iterable<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 int[][] l = mat;
                 yieldReturn(l[2][2]);
             }
         };
 
         Assert.assertEquals(mat[2][2], (int) it.iterator().next());
     }
 
     @Test
     public void array() {
         final int[] arr = new int[]{1, 3, 5, 7};
 
         Iterable<Integer> it = new Yielder<Integer>() {
 
             @Override
             protected void yieldNextCore() {
                 for (int i : arr) {
                     yieldReturn(i);
                 }
             }
         };
 
         int i = 0;
         for (Integer integer : it) {
             Assert.assertEquals("Bad value for index " + i, (int) integer, arr[i]);
             i++;
         }
 
         Assert.assertEquals("Iterated not over the entire array", i, arr.length);
     }
 
     @Test
     public void list() {
         final List<String> list = Arrays.asList("Yielding", "Is", "The", "Best", "Way", "To", "Write", "Iterators");
 
         Iterable<String> it = new Yielder<String>() {
 
             @Override
             protected void yieldNextCore() {
                 for (String s : list) {
                     yieldReturn(s);
                 }
             }
         };
 
         Iterator it1 = it.iterator();
         Iterator it2 = list.iterator();
         int i = 0;
 
         while (it1.hasNext() && it2.hasNext()) {
             Assert.assertEquals("Element " + i + " not equal!", it1.next(), it2.next());
             i++;
         }
 
         Assert.assertFalse("Yielder has too many elements", it1.hasNext());
         Assert.assertFalse("Yielder had too few elements", it2.hasNext());
     }
 
     @Test
     public void someNumbers() {
         final int i1 = 2;
         final int i2 = 4;
         final int i3 = 8;
 
         Iterator<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 int ii1 = i1;
                 int ii2 = i2;
                 int ii3 = i3;
 
                 yieldReturn(ii1);
                 yieldReturn(ii2);
                 yieldReturn(ii3);
             }
         }.iterator();
 
         Assert.assertEquals(i1, (int) it.next());
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals(i2, (int) it.next());
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals(i3, (int) it.next());
         Assert.assertFalse(it.hasNext());
     }
 
     @Test
     public void someStrings() {
         Iterator<String> it = new Yielder<String>() {
             @Override
             protected void yieldNextCore() {
                 yieldReturn("This");
                 yieldReturn("Is");
                 yieldReturn("Great");
             }
         }.iterator();
 
 
         Assert.assertEquals("This", it.next());
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals("Is", it.next());
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals("Great", it.next());
         Assert.assertFalse(it.hasNext());
     }
 
     @Test
     public void someStringsWithBreak() {
         Iterator<String> it = new Yielder<String>() {
             @Override
             protected void yieldNextCore() {
                 yieldReturn("Reach");
                 yieldReturn("Here");
                 yieldBreak();
                 yieldReturn("Not");
                 yieldReturn("Here");
             }
         }.iterator();
 
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals("Reach", it.next());
         Assert.assertTrue(it.hasNext());
         Assert.assertEquals("Here", it.next());
         Assert.assertFalse("Did not break", it.hasNext());
     }
 
     @Test(timeout = 5000)
     public void stopConditionTest() {
         final int stop = 10;
 
         Iterator<Integer> it = new Yielder<Integer>() {
 
             @Override
             protected void yieldNextCore() {
                 int i = 1;
                 while (true) {
                     if (i == stop) {
                         yieldBreak();
                     }
 
                     if (checkPrime(i)) {
                         yieldReturn(i);
                     }
 
                     i++;
                 }
             }
 
             private boolean checkPrime(int i) {
                 switch (i) {
                     case 1:
                         return false;
                     case 2:
                         return true;
                 }
 
                 if (i % 2 == 0) {
                     return false;
                 }
 
                 for (int c = 3; c <= Math.sqrt(i); c += 2) {
                     if (i % c == 0) {
                         return false;
                     }
                 }
 
                 return true;
             }
         }.iterator();
 
         Assert.assertEquals(2, (int) it.next());
         Assert.assertEquals(3, (int) it.next());
         Assert.assertEquals(5, (int) it.next());
         Assert.assertEquals(7, (int) it.next());
         Assert.assertFalse("Too many elements in iterator", it.hasNext());
     }
 
     @Test
     public void nestedLoops() {
         final int[][] mat = new int[][]{
                 new int[]{1, 2, 3},
                 new int[]{4, 5, 6},
                 new int[]{7, 8, 9}
         };
 
         Iterator<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 for (int i = 0; i < mat.length; i++) {
                     int[] arr = mat[i];
                     for (int i1 = 0; i1 < arr.length; i1++) {
                         int x = arr[i1];
                         yieldReturn(x);
                     }
                 }
             }
         }.iterator();
 
 
         for (int i = 0; i < mat.length; i++) {
             int[] ints = mat[i];
             for (int i1 = 0; i1 < ints.length; i1++) {
                 int anInt = ints[i1];
                 Assert.assertEquals(anInt, (int) it.next());
             }
         }
 
         Assert.assertFalse("Too many elements", it.hasNext());
 
 
     }
 
     @Test
     public void nestedLoopsWithEod() {
         final int[][] mat = new int[][]{
                 new int[]{1, 2, 3},
                 new int[]{4, 5, 6},
                 new int[]{7, 8, 9}
         };
 
         Iterator<Integer> it = new Yielder<Integer>() {
             @Override
             protected void yieldNextCore() {
                 for (int[] arr : mat) {
                     for (int x : arr) {
                         yieldReturn(x);
                     }
                 }
             }
         }.iterator();
 
 
         for (int[] arr : mat) {
             for (int x : arr) {
                 Assert.assertEquals(x, (int) it.next());
             }
         }
 
         Assert.assertFalse("Too many elements", it.hasNext());
     }
 
     @Test
     public void grandchildYielder() {
        Iterator<String> it = new SuperYielder<String>() {
 
             /**
              * Implemented by the developer to yield elements of
              * type T back to the caller.
              * <p/>
              * A yieldReturn call will set the next element returned
              * by the iterator's <code>next</code> call. A yieldBreak
              * call will mark that there are no more elements in the
              * iteration.
              * <p/>
              * If the method reaches its end without any
              * yielding, it is considered as if it had implicitly called
              * yieldBreak.
              *
              * @see #yieldBreak()
              * @see #yieldReturn(Object)
              */
             @Override
             protected void yieldNextCore() {
                 yieldReturn("I'm");
                 yieldReturn("a");
                 yieldReturn("grandchild");
                 yieldReturn("class");
             }
         }.iterator();
 
         Assert.assertEquals("I'm", it.next());
         Assert.assertEquals("a", it.next());
         Assert.assertEquals("grandchild", it.next());
         Assert.assertEquals("class", it.next());
     }
 }
