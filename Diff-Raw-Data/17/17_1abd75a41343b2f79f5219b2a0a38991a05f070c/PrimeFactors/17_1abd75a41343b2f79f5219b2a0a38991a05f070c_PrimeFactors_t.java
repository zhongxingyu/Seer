 package com.github.kpacha.jkata.primeFactors;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrimeFactors {
 
     public static List<Integer> generate(int number) {
 	List<Integer> primes = new ArrayList<Integer>();
	int factorCandidate = 2;
	while (number > 1) {
	    while (number % factorCandidate == 0) {
		primes.add(factorCandidate);
		number /= factorCandidate;
 	    }
	    factorCandidate++;
	}
	if (number > 1) {
	    primes.add(number);
 	}
 	return primes;
     }
 }
