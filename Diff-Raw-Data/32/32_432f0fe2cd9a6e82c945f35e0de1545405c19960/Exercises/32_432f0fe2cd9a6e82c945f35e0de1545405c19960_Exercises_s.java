 import java.util.List;
 /**
  * Write a description of class Exercises here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Exercises {
     /**
      * (a) Compute the sum of a List of Integer values. Must use function application 
      * and recursion only. Must not use mutation. Must not use any form of the for or 
      * while loop constructs. Must not use any form of the add, addAll, clear, remove, 
      * removeAll, retainAll, or set methods. Must not have side effects, including changing 
      * the structure of an object pointed to by a parameter. Use tail recursion if possible. 
      * 
      * @param values the values to be added
      * 
      * @return the sum of the integers
      */
     public static Integer sum(final List<Integer> values) {  
        /*if (values.size() == 0) {
            return 0;
        }
        return values.get(0) + sum(values.subList(1, values.size()));*/
        
         return values.size() == 0 ? 
                0 : head(values) + sum(values.subList(1, values.size()));
     }
     
     /**
      * (b) Compute the product of a List of Integer values. Must use function application and 
      * recursion only. Must not use mutation. Must not use any form of the for or while loop 
      * constructs. Must not use any form of the add, addAll, clear, remove, removeAll, retainAll, 
      * or set methods. Must not have side effects, including changing the structure of an object 
      * pointed to by a parameter. Use tail recursion if possible. 
      * 
      * @param values the list of integers to be multiplied
      * 
      * @return the product of integers
      */
     public static Integer product(final List<Integer> values) {
        /*if (values.size() == 0) {
            return 1;
        }*/
        
         return values.size() == 0 ? 
                1 : head(values) * product(values.subList(1, values.size()));            
     }
     
     /**
      * (c) Given a non-null string and a non-empty substring, recursively compute the number of 
      * times that the substring appears in the string, without the substrings overlapping. For 
      * example, strCount("catcowcat", "cat") returns 2. Must use function application and recursion 
      * only. Must not use mutation. Must not use any form of the for or while loop constructs. Must 
      * not have side effects. Use tail recursion if possible. 
      * 
      * @param toExamine the string to be searched
      * 
      * @return the number of non-overlapping occurences of the substring in the string 
      */
     public static Integer strCount(final String toExamine, final String sub) {
        /*if (toExamine.indexOf(sub) == -1) {
            return 0;
        }*/
   
         return toExamine.indexOf(sub) == -1 ? 
                0 : 1 + strCount(toExamine.substring(toExamine.indexOf(sub) + sub.length()), sub);
     }
     
     /**
      * d) Given a List of Integers, determine if it is possible to choose a group of some of 
      * the values such that the group sums to a given target value. 
      * Examples: groupExists({2, 4, 9}, 11) → true
      *           groupExists({2, 4, 9}, 15) → true
      *           groupExists({2, 4, 9}, 8) → false
      *           groupExists({2, 4, 9, 2}, 8) → true
      *            
      * Must use function application and recursion only. Must not use mutation. Must not use any form 
      * of the for or while loop constructs. Must not use any form of the add, addAll, clear, remove, 
      * removeAll, retainAll, or set methods. Must not have side effects, including changing the 
      * structure of an object pointed to by a parameter. Use tail recursion if possible. 
      * 
      */
     public static boolean groupExists(final List<Integer> numbers, final int target) {
      return target == 0 || target > 0 && head(numbers) != null &&
              (groupExists(tail(numbers), target) || groupExists(tail(numbers), 
                                                                 target-head(numbers)));
     }
     
    private static Integer head(final List<Integer> is) {
      return is == null || is.isEmpty() ? 
             null : is.get(0);
     }
     
    private static List<Integer> tail(final List<Integer> is) {
      return is.size() < 2 ? 
             null : is.subList(1, is.size());
     }
    
 }
