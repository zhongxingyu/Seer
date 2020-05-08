 package com.pie.demo;
 
 public class pie {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("Pie products..");
 		
 		System.out.println("version 1 pie products : ");
 		System.out.println(getOldPieProducts());
		
		//Fix production bug
		System.out.println("Fixing production defect for v1.1");
 	}
 	
 	public static String getOldPieProducts()
 	{
 		return "Pie old 1, Pie old 2";
 	}
 
 }
