 package com.github.kpacha.jkata.primeFactors;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrimeFactors {
 
     public static List<Integer> generate(final int number) {
 	List<Integer> primes = new ArrayList<Integer>();
 	if (number > 1) {
	    primes.add(2);
 	}
 	return primes;
     }
 }
