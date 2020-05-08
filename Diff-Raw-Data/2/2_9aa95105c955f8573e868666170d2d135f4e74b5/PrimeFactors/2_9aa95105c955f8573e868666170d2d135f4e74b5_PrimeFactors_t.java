 package com.github.kpacha.jkata.primeFactors;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrimeFactors {
 
     public static List<Integer> generate(int number) {
 	List<Integer> primes = new ArrayList<Integer>();
 	if (number > 1) {
	    while (number % 2 == 0) {
 		primes.add(2);
 		number /= 2;
 	    }
 	    if (number > 1) {
 		primes.add(number);
 	    }
 	}
 	return primes;
     }
 }
