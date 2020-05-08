package com.igordaoconsulting.training.util;
 
 import java.util.ArrayList;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.math3.util.ArithmeticUtils;
 
 public class StringUtils {
 
 	public static Collection<String> allPermutations(String string) {
 		if (string == null || string.isEmpty())
 			throw new IllegalArgumentException("Empty string");
 
 		if (string.length() == 2)
 			return Arrays.asList(string, reverse(string));
 
 		char lastChar = string.charAt(string.length() - 1);
 		String substring = string.substring(0, string.length() - 1);
 
 		// Recursive call to allPermutations
 		Collection<String> substringPermutations = allPermutations(substring);
 
 		List<String> result = new ArrayList<String>(factorial(string.length()));
 		for (String permutation : substringPermutations)
 			for (int i = 0; i <= permutation.length(); i++)
 				result.add(new StringBuilder(permutation).insert(i, lastChar).toString());
 
 		return result;
 	}
 
 	private static String reverse(String string) {
 		return org.apache.commons.lang3.StringUtils.reverse(string);
 	}
 
 	private static int factorial(int n) {
 		return (int) ArithmeticUtils.factorial(n);
 	}
 
 }
