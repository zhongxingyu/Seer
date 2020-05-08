 package com.github.kpacha.jkata.fizzBuzz;
 
 public class FizzBuzz {
 
     public static Object generate(int total) {
	Object answer = "";
 	if (total % 3 == 0) {
 	    answer = "Fizz";
	}
	if (total % 5 == 0) {
	    answer += "Buzz";
	}
	if (answer.equals("")) {
 	    answer = total;
 	}
 	return answer;
     }
 }
