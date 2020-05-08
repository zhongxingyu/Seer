 package com.tommytony.math;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MathExt {
 	
 	public static int gcd(int u, int v) throws NegativeNumberException {
 		if((MathExt.isNegative(u)) || (MathExt.isNegative(v)))
 			throw new NegativeNumberException();
 		
 		if(u == v)
 			return u;
 		if(u == 0)
 			return v;
 		if(v == 0)
 			return u;
 		if(MathExt.isEven(u))
 			if(MathExt.isOdd(v))
 				return MathExt.gcd(u >> 1, v);
 			else
 				return MathExt.gcd(u >> 1, v >> 1) << 1;
 		if(MathExt.isEven(v))
 			return gcd(u, v >> 1);
 		if(u > v)
 			return MathExt.gcd((u - v) >> 1, v);
 		return MathExt.gcd((v - u) >> 1, u);
 	}
 	
 	public static List<Integer> factor(int j) {
 		if((j == 0) || j < 0)
 			return null;
 		List<Integer> list = new ArrayList<Integer>();
 		list.add(1);
 		
 		int lowest_found = 1;
 		int increment = 1;
 		boolean increment_thiscycle = false;
 		
 		
		for(int i = 2; i < (j / (lowest_found)); i += increment) {
 			if(increment_thiscycle) {
 				increment += 1;
 				increment_thiscycle = false;
 			}
 			
 			if(j % i == 0) {
 				list.add(i);
 				//we need the lowest found to limit the numbers we need to check
 				if(lowest_found == 1)
 					lowest_found = i;
 			}
 			//number is Odd, we can skip all the evens
 			if(i == 2) {
 				increment_thiscycle = true;
 			}
 			
 		}
 		
 		list.add(j);
 		return list;
 	}
 	
 	
 	public static boolean isPrime(int i) {
 		return (MathExt.factor(i).size() == 2); //if only 2 then the factors with be 1 and number
 	}
 	
 	public static boolean isPositive(int i) {
 	    return (i > 0);	
 	}
 	
 	public static boolean isNegative(int i) {
 		return (i < 0);
 	}
 	
 	public static boolean isEven(int i) {
 		return (i % 2) == 0;
 	}
 	
 	public static boolean isOdd(int i) {
 		return (i % 2) == 1;
 	}
 	
 	public static int factorial(int i) {
 		if(i == 0) {
 			return 1;
 		}
 		int sum = 1;
 		for(; i > 0; i--) {
 			sum *= i;
 		}
 		return sum;
 	}
 }
