 package hello;
 
 public class FizzBuzz {
 
 	public static String fizzbuzz(int i) {
 		String fb = fizz(i) + buzz(i);
 		if (fb.isEmpty()) return String.valueOf(i);
		else return fb;
 	}
 
 	public static String fizz(int i) {
 		if (i % 3 == 0) return "Fizz";
 		return "";
 	}
 
 	public static String buzz(int i) {
 		if (i % 5 == 0) return "Buzz";
 		return "";
 	}
 
 }
