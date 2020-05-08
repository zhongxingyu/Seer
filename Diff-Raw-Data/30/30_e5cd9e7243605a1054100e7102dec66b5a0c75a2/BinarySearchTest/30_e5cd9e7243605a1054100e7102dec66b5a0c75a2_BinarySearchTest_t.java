 package BinarySearch;
 
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mike
  * Date: 11/7/12
  * Time: 9:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BinarySearchTest {
 
     @Test
     public void testFindValue()
     {
         List<Integer> array = Arrays.asList(1,2,3,4,5,6,7,10);
 
         assertEquals(true, BinarySearch.binarySearch(array,2));
         assertEquals(true, BinarySearch.binarySearch(array,5));
         assertEquals(true, BinarySearch.binarySearch(array,6));
         assertEquals(true, BinarySearch.binarySearch(array,1));
         assertEquals(true, BinarySearch.binarySearch(array,10));
     }
 
     @Test
     public void testDoNotFindValue()
     {
        List<Integer> array = Arrays.asList(1,2,3,4,5,6,7,10);
 
         assertEquals(false, BinarySearch.binarySearch(array,11));
         assertEquals(false, BinarySearch.binarySearch(array,0));
         assertEquals(false, BinarySearch.binarySearch(array,8));
     }
 }
