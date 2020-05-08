 package com.github.kpacha.jkata.fizzBuzz;
 
 public class FizzBuzz {
 
     public static Object generate(int total) {
 	Object answer = null;
	if (total % 3 == 0) {
 	    answer = "Fizz";
 	} else if (total == 5) {
 	    answer = "Buzz";
 	} else {
 	    answer = total;
 	}
 	return answer;
     }
 }
