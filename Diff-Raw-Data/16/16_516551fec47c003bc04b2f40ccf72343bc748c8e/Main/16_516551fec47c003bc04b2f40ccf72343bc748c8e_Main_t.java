 package com.IntArithmetic;
 /**
  * @file Main.java
  * @brief Main class
  *
  * @version 0.1
  * @author Jos Ignacio Carmona Villegas <joseicv@correo.ugr.es>
  * @author Juan Hernandez Garca <juanhg@correo.ugr.es>
  * @date 07/October/2013
  *
  * @section LICENSE
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of
  * the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details at
  * http://www.gnu.org/copyleft/gpl.html
  * 
  */
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 import com.IntsArithmetic.DataStructure.BigInt;
 import com.IntsArithmetic.DataStructure.BigInt.ResultExtendedEuclidean;
 
 /**
  * @brief Main.
  * Entry point of the application.
  */
 public class Main
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		
 		FileInputStream in;
 		BufferedReader br;
 		String strLine1;
 		String strLine2;
 		
 		try
 		{
 			//System.out.println("Integer.MAX_VALUE = "+Integer.MAX_VALUE);
 			//System.out.println("Long.MAX_VALUE = "+Long.MAX_VALUE);
 			
 			// data.txt should include two lines. Each should be a big decimal natural number.
 			in = new FileInputStream("data.txt");
 			br = new BufferedReader(new InputStreamReader(in));
 			
 			strLine1 = br.readLine();
 			strLine2 = br.readLine();
 			
 			BigInt number1 = new BigInt(strLine1);
 			BigInt number2 = new BigInt(strLine2);
 			
 			br.close();
 			in.close();
 			
 			//number1.setIsNegative(true);
			number2.setIsNegative(true);
			
 			/*
 			System.out.println("The number " + strLine1 + " is represented as " + number1.toString());
 			System.out.println("The number " + strLine2 + " is represented as " + number2.toString());
 			*/
 			/*
 			System.out.println();
 			BigInt resultAdd = number1.add(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" + " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultAdd.toString());
 			System.out.println();
 			*/
 			/*
 			System.out.println();
 			BigInt resultSubstract = number1.subtract(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" - " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultSubstract.toString());
 			System.out.println();
 			*/
 			/*
 			System.out.println();
 			BigInt resultMultiply1 = number1.multiplySchool(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" * " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultMultiply1.toString());
 			System.out.println();
 			*/
 			/*
 			System.out.println();
 			BigInt resultMultiply2 = number1.multiplyKaratsuba(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" * " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultMultiply2.toString());
 			System.out.println();
 			*/
 			
 			System.out.println();
 			BigInt quotant = new BigInt();
 			BigInt resultDivision = number1.division(number2, quotant);
 			System.out.println("   " + number1.toString());
 			System.out.println(" / " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println("Quotant = " + quotant.toString());
 			System.out.println("Rest = " + resultDivision.toString());
 			System.out.println();
 			
 			
 			/*System.out.println();
 			BigInt resultMod = number1.mod(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" % " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultMod.toString());
 			System.out.println();*/
 			
 			/*
 			System.out.println();
 			BigInt resultModulo = number1.modulo(number2);
 			System.out.println(number1.toString() + " mod(" + number2.toString() + " ) = " + resultModulo.toString());
 			System.out.println();
 			*/
 			/*
 			System.out.println();
 			ResultExtendedEuclidean resultEuclidean = number1.extendedEuclidean(number2);
 			System.out.println("d = GCD(" + number1.toString() + "," + number2.toString() + ")");
 			System.out.println("d = " + resultEuclidean.d.toString());
 			System.out.println("u = " + resultEuclidean.u.toString());
 			System.out.println("v = " + resultEuclidean.v.toString());
 			System.out.println();
 			*/
 			/*
 			System.out.println();
 			BigInt resultMultiplyModular = number1.multiplyModular(number2);
 			System.out.println("   " + number1.toString());
 			System.out.println(" * " + number2.toString());
 			System.out.println("_____________________________________________________________________");
 			System.out.println(resultMultiplyModular.toString());
 			System.out.println();
 			*/
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 }
