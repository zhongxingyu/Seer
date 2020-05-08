 package fizzbuzz;
 
 public class FizzBuzz {
     public String gives(int number) {
         if (isFizzBuzz(number)) {
             return "FizzBuzz";
 	} else {
             if (isFizz(number)) return "Fizz";
             if (isBuzz(number)) return "Buzz";
 	}
         return Integer.toString(number);
     }
 
     private boolean isFizz(int number) {
        return (number % 3 == 0);
     }
 
     private boolean isBuzz(int number) {
        return (number % 5 == 0);
     }
 
     private boolean isFizzBuzz(int number) {
         return (isFizz(number) && isBuzz(number));
     }
    
     public static final void main(String[] args) {
 	FizzBuzz fizzerBuzzer = new FizzBuzz();
 	for (int i=1; i<=100; i++) {
 	    System.out.printf("%s\n", fizzerBuzzer.gives(i));
 	}
     }
 
 }
