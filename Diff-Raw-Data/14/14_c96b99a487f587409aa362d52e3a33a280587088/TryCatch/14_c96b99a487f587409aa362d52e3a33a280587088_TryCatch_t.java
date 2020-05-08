 package com.dj.example;
 
 public class TryCatch {
 
 	public static void main(String[] args) {
		int d, a, test[] = {1};
 		try {
			d = 1;
 			a = 42 / d;
			System.out.println("This should be printed");
			System.out.println(test[5]);
 			System.out.println("This should not be printed");
			
 		} catch (ArithmeticException e) {
 			System.out.println("This is in Exception:");
 			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("This an out of bounds Exception:");
			e.printStackTrace();
 		}
 		System.out.println("After the exception");
 	}
 
 }
