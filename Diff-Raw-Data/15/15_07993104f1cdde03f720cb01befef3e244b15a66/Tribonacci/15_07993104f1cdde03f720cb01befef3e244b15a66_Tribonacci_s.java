 /**
  * The class Tribonacci calculates the first 37 numbers of the tribonacci
  * sequence.
  *
  * @author Markus Iser, Martin Thoma
  * @version 1.0
  */
 public final class Tribonacci {
 
     /**
      * Utility classes should not have a public or default constructor.
      */
     private Tribonacci() {
     }
 
     /**
      * This method calculates the n'th tribonacci number.
      *
      * @param n A number that is not negative
      * @return The n'th tribonacci number
      */
     public static int getTribonacciNr(final int n) {
         int a = 1, b = 1, c = 1;
<<<<<<< HEAD
         // start with 4, as we already have calculated 3
         // tribonacci numbers
         for (int i = 4; i <= n; i++) {
             int d = a + b + c;
=======
        for (int i = 4; i <= 37; i++) {
>>>>>>> b1c25a7f77f7fea601fa16708761887a4b693859
             a = b;
             b = c;
            c = a + b + c;
         }
         return c;
     }
 
     /**
      * Command line interface.
      *
     * @param args
     *            The command line parameters.
      */
     public static void main(final String[] args) {
        System.out.println(37);
     }
 }
 
