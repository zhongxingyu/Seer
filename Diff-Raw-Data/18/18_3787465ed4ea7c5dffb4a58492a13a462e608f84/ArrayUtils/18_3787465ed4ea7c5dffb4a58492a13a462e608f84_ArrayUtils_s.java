 package Lesson_7_Exceptions;
 
 /**
  *
  * @author mike
  */
 public class ArrayUtils {
 
     /**
      * Finds the minum value stored within an integer array.
      *
      * @param list A list of int values
      * @return An integer which represents the smallest value in the array
      * @throws BadArrayException if the array is Null or has a length of 0
      */
     public static int minValue(int[] list) throws BadArrayException {
 
         if (list == null) {
             throw new BadArrayException("Array is null");
         }
 
         if (list.length == 0) {
             throw new BadArrayException("Array is empty");
         }
 
 
         int index = list[0];
         int again = 0;
         int minValue = list[again];
 
         // repeat for the length of the array
         while (again < list.length) {
             // if the current value is less than our current min value
             if (list[again] < minValue) {
                 minValue = list[again];
             }
             again++;
         }
         return minValue;
     }
 
     /**
      * Creates a new array of the specified substring at the starting postion
      * specified as startIndex to the end of input array.
      *
      * @param list the array to be copied from
      * @param startIndex the starting position to be copied from
      * @return an array which will represent the sub array of the original
      * @throws ArrayIndexOutOfBoundsException if the specified start index is
      * out of bounds
      * @throws BadArrayException if the input array is null
      */
     public static int[] copyRange(int[] list, int startIndex) throws ArrayIndexOutOfBoundsException, BadArrayException {
 
         if (list == null) {
             throw new BadArrayException("Array is null");
         }
 
         if (startIndex < 0) {
             throw new ArrayIndexOutOfBoundsException(startIndex);
         }
         if (startIndex > list.length) {
             throw new ArrayIndexOutOfBoundsException(startIndex);
         }
 
 
         // the length of the new arary based on starting position
         int newLength = list.length - startIndex;
         // the new starting index
         int index = startIndex;
         // loop variable
         int again = 0;
         // declaration of our new array
         int[] newList = new int[newLength];
 
         while (again < newLength) {
             newList[again] = list[index];
             index++;
             again++;
         }
         return newList;
     }
 
     /**
      * Searches the specified array for the search value specified and returns
      * its index.
      *
      * @param list the array to search
      * @param searchValue the value to search for
      * @return the index ofthe value if it is found, -1 if it is not
      * @throws BadArrayException if the given array is null
      */
     public static int indexOf(int[] list, int searchValue) throws BadArrayException {
 
         if (list == null) {
             throw new BadArrayException("Array is null");
         }
         if (list.length == 0) {
             return -1;
         }
 
         int index = list[0];
         int again = 0;
 
         // repeat for the length of the array
         while (again < list.length) {
             // check to see if current index is correct
             if (searchValue == list[again]) {
                 return again;
             } else {
                 try {
                     again++;
                     index = list[again];
                 } catch (ArrayIndexOutOfBoundsException e) {
                     return -1;
                 }
             }
         }
 
         return -1;
     }
 
     /**
      * This method returns the last occurring index of the specified search
      * value within the given array.
      *
      * @param list the array to search.
      * @param searchValue the value to go searching for.
      * @return the last occurring index of the value or -1 if it does not exist.
      * @throws BadArrayException if the given array is null.
      */
     public static int lastIndexOf(int[] list, int searchValue) throws BadArrayException {
 
         if (list == null) {
             throw new BadArrayException("Array is null");
         }
         if (list.length == 0) {
             return -1;
         }

        int index = list[0];
        int again = 0;
         int validIndex = -1;
 
         // repeat for the length of the array
        while (again < list.length) {
             // check to see if current index is correct
            if (searchValue == list[again]) {
                validIndex = again;
            } else {
                again++;
                index = list[again];
            }
         }
 
         return validIndex;
     }
 }
