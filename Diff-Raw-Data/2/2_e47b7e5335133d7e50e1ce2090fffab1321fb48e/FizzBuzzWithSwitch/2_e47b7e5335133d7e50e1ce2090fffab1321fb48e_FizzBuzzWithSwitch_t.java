 package fizzbuzz;
 
 public class FizzBuzzWithSwitch implements FizzBuzz {
 
     @Override
     public String messageFor(int number) {
 
         // Simple bit flag approach
         int status = isMultipleOf(3, number);
         status |= (isMultipleOf(5, number) << 1);
 
         switch(status){
             case 0: return "" + number;    // Simples
             case 1: return "Fizz";      // Multiple of 3
             case 2: return "Buzz";      // Multiple of 5
             case 3: return "FizzBuzz";  // Multiple of 3 & 5
             default:
                throw new AssertionError("Unexpected status: " + status);
         }
     }
 
     private static int isMultipleOf(int divisor, int number){
         return (number % divisor == 0) ? 1 : 0;
     }
 }
