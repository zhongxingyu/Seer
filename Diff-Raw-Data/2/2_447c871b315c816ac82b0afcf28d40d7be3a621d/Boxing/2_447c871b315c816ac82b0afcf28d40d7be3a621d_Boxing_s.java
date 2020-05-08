 package com.jiekebo.scjp;
 
 public class Boxing {
 	
 	public static void increase(Object b) {
 		System.out.println(b);
 	}
 	
 	public static void overload(int... i) {
 		System.out.println("integer");
 		for (int j : i) {
 			System.out.println(j);
 		}
 	}
 	
 	public static void overload(long... i) {
 		System.out.println("long");
 		for (long l : i) {
 			System.out.println(l);
 		}
 	}
 	
 	public static void main(String[] args ) {
 		// Widen and box
 		byte b = 5;
 		increase(b);
 		// varargs
 		int i = 1;
		overload(i);
 		Integer test = 10;
 		Object o = test;
 		
 		String[] testArray = new String[3];
 		int size = testArray.length;
 		System.out.println(size);
 		String testString = "abcdefg";
 		System.out.println(testString.length());
 	}
 }
