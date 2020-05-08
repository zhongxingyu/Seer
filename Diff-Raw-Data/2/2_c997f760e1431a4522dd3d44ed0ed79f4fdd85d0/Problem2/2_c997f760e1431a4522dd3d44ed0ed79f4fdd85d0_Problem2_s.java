 /**
  * Solves problem 2 of the Euler Project.
  *
  * See <a href="http://projecteuler.net/index.php?section=problems&id=2">Euler Project, problem 2</a> for more details.
  */
 public final class Problem2 {
     private Problem2() {
         // Intentionally blank
     }
 
     /**
      * Calculates the Fibonacci number at position <code>term</code>.
      *
      * @param term the position of the Fibonacci number to be calculated
      * @return the Fibonacci number at position <code>term</code>.
      */
     private static final int fib(final int term) {
         if (1 >= term) {
            return 1;
         }
         return fib(term - 2) + fib(term - 1);
     }
 
     /**
      * Solves the problem.
      */
     public static final int problem2() {
         int sum = 0;
         // I precalculated the max term that is less than 4 mil; it was 32.
         for (int i = 0; i < 33; i++) {
             final int currentFib = fib(i);
             if (0 == currentFib % 2) {
                 sum += currentFib;
             }
         }
         return sum;
     }
 
     /**
      * Calls {@link #problem2()} and prints the result.
      *
      * @param args the command-line arguments to the program
      */
     public static void main(final String... args) {
         System.out.println(problem2());
     }
 }
 
