 package com.dj.example;
 
 public class TryCatch {
 
 	public static void main(String[] args) {
		int d, a;
 		try {
			d = 0;
 			a = 42 / d;
 			System.out.println("This should not be printed");
 		} catch (ArithmeticException e) {
 			System.out.println("This is in Exception:");
 			e.printStackTrace();
 		}
 		System.out.println("After the exception");
 	}
 
 }
