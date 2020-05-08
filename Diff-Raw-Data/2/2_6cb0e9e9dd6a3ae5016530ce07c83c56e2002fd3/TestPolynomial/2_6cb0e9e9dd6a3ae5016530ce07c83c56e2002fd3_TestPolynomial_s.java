 package com.jasimmonsv.Simmons_HW3;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class TestPolynomial {
 	
 	private static int coeff;
 	private static int exp;
 	private static String coeffStr="";
 	private static String expStr="";
 	private static String choiceStr="";
 	
 	public static void main(String[] args) {
 		Term tempTerm = new Term(coeff,exp);
 		Polynomial tempPoly = new Polynomial(coeff,exp);
 		Polynomial poly = new Polynomial(coeff,exp);
 		
 		while (expStr != "t"){
 			readPoly(); 
 			tempTerm = new Term(coeff,exp);
 			tempPoly = new Polynomial(tempTerm);
 			if (choiceStr.equalsIgnoreCase("a")) poly = poly.add(tempPoly);
 			else if (choiceStr.equalsIgnoreCase("m")) poly = poly.multiply(tempPoly);
 			else System.out.println("Your choice was not recognized.");
 			poly.print();
 		}//end while
 	}//end main
 	
 	public static void readPoly(){
 		BufferedReader con1 = new BufferedReader(new InputStreamReader(System.in));
 		//BufferedReader con2 = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println("Press \"x\" to Exit.");
 		System.out.println("Enter in the coeffecent integer of the next term: ");
 		try {
 			coeffStr = con1.readLine();
 		} catch (IOException e) {
 			System.out.println(coeffStr);
 			e.printStackTrace();
 		}
		if (coeffStr.equalsIgnoreCase("t"))System.exit(0);
 		coeff = Integer.valueOf(coeffStr);
 		System.out.println("\nEnter in the exponent integer of the next term: ");
 		try {
 			expStr = con1.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (expStr.equalsIgnoreCase("x"))System.exit(0);
 		exp = Integer.valueOf(expStr);
 		System.out.println("Press A to Add or M to multiply this term?");
 		try {
 			choiceStr = con1.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}//end method readPoly
 	
 }//end class
