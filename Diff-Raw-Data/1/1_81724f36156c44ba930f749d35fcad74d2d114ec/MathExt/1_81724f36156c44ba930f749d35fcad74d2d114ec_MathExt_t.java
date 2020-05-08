 package com.tommytony.math;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tommytony.math.NegativeNumberException;
 
 public class MathExt {
 	
 	//thqnk you reddit/Google Guava for the suggestion
 	public static final BigInteger[] factorialBuffer = {
 		BigInteger.ONE,
 		BigInteger.valueOf(1L * 2),
 		BigInteger.valueOf(1L * 2 * 3),
 		BigInteger.valueOf(1L * 2 * 3 * 4),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18 * 19),
 		BigInteger.valueOf(1L * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 * 13 * 14 * 15 * 16 * 17 * 18 * 19 * 20)
 	};
 	
 	public static final long[] squaresBuffer = {
 		0,
 		1,
 		2 * 2,
 		3 * 3,
 		4 * 4,
 		5 * 5,
 		6 * 6,
 		7 * 7,
 		8 * 8,
 		9 * 9,
 		10 * 10,
 		11 * 11,
 		12 * 12,
 		13 * 13,
 		14 * 14,
 		15 * 15,
 		16 * 16,
 		17 * 17,
 		18 * 18,
 		19 * 19,
 		20 * 20
 	};
 	
 	public static final long[] cubesBuffer = {
 		0,
 		1,
 		2 * 2 * 2,
 		3 * 3 * 3,
 		4 * 4 * 4,
 		5 * 5 * 5,
 		6 * 6 * 6,
 		7 * 7 * 7,
 		8 * 8 * 8,
 		9 * 9 * 9,
 		10 * 10 * 10,
 		11 * 11 * 11,
 		12 * 12 * 12,
 		13 * 13 * 13,
 		14 * 14 * 14,
 		15 * 15 * 15,
 		16 * 16 * 16,
 		17 * 17 * 17,
 		18 * 18 * 18,
 		19 * 19 * 19,
 		20 * 20 * 20
 	};
 	
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
 		
 		
 		for(int i = 2; i < ((j / (lowest_found)) + 1); i += increment) {
 			if(increment_thiscycle) {
 				increment += 1;
 				increment_thiscycle = false;
 			}
 			
 			if(j % i == 0) {
 				list.add(i);
 				//we need the lowest found to limit the numbers we need to check
 				if(lowest_found == 1)
 					lowest_found = i;
 				continue; //fixs flaw of code being able to go to i==2 if it is odd
 			}
 			//Code that is executed if a match isn't found
 			//this code executes if i == 2  and i was not a match therefore the number is odd
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
 	
 	public static BigInteger factorial(int i) throws NegativeNumberException {
 		if(MathExt.isNegative(i))
 			throw new NegativeNumberException();
 		if(i == 0)
 			return BigInteger.valueOf(1L);
 		if(i <= 20)
 			return MathExt.factorialBuffer[i--];
 		else {
 		   BigInteger sum = BigInteger.ONE;
 		        for(; i > 0; i--) {
 			        sum = sum.multiply(BigInteger.valueOf(i));
 		        }
 		    return sum;
 		}
 	}
 	
 	public static long square(int i) {
 		if(i <= 20)
 			return squaresBuffer[i];
 		else
 			return i * i;
 	}
 	
 	public static long cube(int i) {
 		if(i <= 30)
		if(i <= 20)
 			return cubesBuffer[i];
 		else
 			return i * i * i;
 	}
 }
