 package edu.mssm.pharm.maayanlab.Enrichr;
 
 public class Shortener {
 
	private static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final int base = 36;
 	
 	public static String encode(int number) {
 		StringBuilder linkId = new StringBuilder();
 		
 		do {
 			int remainder = (int) (number % base);
 			linkId.append(alphabet.charAt(remainder));
 			number /= base;
 		} while (number > 0);
 		
 		linkId.reverse();
 		return linkId.toString();
 	}
 	
 	public static int decode(String linkId) {
 		int number = 0;
 		
 		for (int i = 0; i < linkId.length(); i++) {
 			int position = alphabet.indexOf(linkId.charAt(i));
 			number += position * (int) Math.pow(base, linkId.length() - i - 1);
 		}
 		
 		System.out.println("");
 		
 		return number;
 	}
 
 }
