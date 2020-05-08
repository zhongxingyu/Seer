 package edu.msergey.jalg.inversions;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class InversionsCounterTest {
     @Test
     public void test_count_zero_length_data() {
         List<Integer> data = new ArrayList<Integer>();
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 0;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_one_length_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(1));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 0;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_two_length_sorted_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(1, 2));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 0;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_two_length_unsorted_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(2, 1));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 1;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_three_length_sorted_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 0;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_three_length_reverse_order_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(3, 2, 1));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 3;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_three_length_unsorted_data() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(2, 1, 3));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 1;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_example_from_class_description() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(3, 14, 1, 5, 9));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 4;
         assertEquals(expected, actual);
     }
 
     @Test
     public void test_count_example_from_lecture() {
         List<Integer> data = new ArrayList<Integer>(Arrays.asList(1, 3, 5, 2, 4, 6));
         InversionsCounter counter = new InversionsCounter(data);
         long actual = counter.count();
         long expected = 3;
         assertEquals(expected, actual);
     }
 }
