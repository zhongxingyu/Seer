 package com.sansaretti;
 
 public class Main {
 
 	public static void main(String[] args) {
 		/*
 		 * taniticilari tanimla.
 		 */
		Tanitici[] tanicilar = new Tanitici[] { new AkgunTanitici(), new FiratTanitici() };
 
 		/*
 		 * her bir tanitici icin
 		 */
 		for (Tanitici t : tanicilar) {
 			/*
 			 * kendini tanit
 			 */
 			System.out.println(t.tanit());
 		}
 	}
 }
