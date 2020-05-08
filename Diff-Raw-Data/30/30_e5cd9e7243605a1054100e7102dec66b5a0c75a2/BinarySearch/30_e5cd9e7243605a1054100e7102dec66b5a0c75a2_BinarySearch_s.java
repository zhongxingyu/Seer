 package BinarySearch;
 
 import java.util.List;
 
/**
 * Created with IntelliJ IDEA.
 * User: Mike
 * Date: 11/7/12
 * Time: 9:44 PM
 * To change this template use File | Settings | File Templates.
 */
 public class BinarySearch {
 
     //Given a sorted array find a value
     public static boolean binarySearch(List<Integer> array, int value)
     {
         int size = array.size();
         int half = size / 2;
         int left = 0;
         int right = size-1;
 
        while (left < right) {
 
            int halfValue = array.get(half);
 
             if (value == halfValue)
                 return true;
             else if(value < halfValue)
                right = half;
             else
                left = half;
         }
 
         return false;
     }
 }
