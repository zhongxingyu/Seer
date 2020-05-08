 package BinarySearch;
 
 import java.util.List;
 

 public class BinarySearch {
 
     //Given a sorted array find a value
     public static boolean binarySearch(List<Integer> array, int value)
     {
         int size = array.size();
         int half = size / 2;
         int left = 0;
         int right = size-1;
        int halfValue = 0;
 
        while (left <= right) {
 
            half = (left + right) / 2;
            halfValue = array.get(half);
 
             if (value == halfValue)
                 return true;
             else if(value < halfValue)
                right = half - 1;
             else
                left = half + 1;
         }
 
         return false;
     }
 }
