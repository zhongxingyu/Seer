 package ro.robertmunteanu.eulerproject.problem1;
 
 import static ro.robertmunteanu.eulerproject.util.AssertUtils.assertEquals;
 
 /**
  * 
  * Solves the problem by looping through all the numbers
  * 
  * @author Robert Munteanu
  *
  */
 public class Problem1BruteForceSolution {
 
     // --------------------------------------------------------------------------------------------
    // Class methods
     // --------------------------------------------------------------------------------------------
 
     public static void main(String[] args) {
 
         int sum = 0;
 
         for (int i = 1; i < 1000; i++)
             if ((i % 3 == 0) || (i % 5 == 0))
                 sum += i;
 
         System.out.println(sum);
 
         assertEquals(sum, 233168);
 
     }
 
     /* ----------------------------------------------------------------------------------------- */
 }
