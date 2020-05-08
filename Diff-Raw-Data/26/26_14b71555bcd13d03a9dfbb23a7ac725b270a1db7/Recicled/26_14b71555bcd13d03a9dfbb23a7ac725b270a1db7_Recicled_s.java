 package com;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 /**
  * Problem C. 
  * @see <a href="http://code.google.com/codejam/contest/1460488/dashboard#s=p2">Problem C. Recycled Numbers</a> 
  * @author antonio.diaz.arroyo@gmail.com
  */
 public class Recicled {
 
 	public static void main(String[] args) throws Exception {
 		InputStream inputStream = Recicled.class.getResourceAsStream("C-large-practice.in");
 		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
 		BufferedReader bf = new BufferedReader(inputStreamReader);
 		int cases = Integer.parseInt(bf.readLine());
 		for (int i = 0; i < cases; i++) {
 			int recicledPairs = Recicled.calculateRecicledPairs(bf.readLine());
 			System.out.println(String.format("Case #%1$d: %2$s ", i+1, recicledPairs));
 		}
 	}
 	
 	/**
	 * 
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	public static int calculateRecicledPairs(String myLine) {
 		String[] split = myLine.split(" ");
 		int a = Integer.parseInt(split[0]);
 		int b = Integer.parseInt(split[1]);
 		int bDigits = String.valueOf(b).length();
 		int pairsFounds = 0;
		for (int i = a; i < b; i++) {
 			for (int j=1; j<bDigits; j++){
 				/* if i=1 --> 119 -> 911 */ 
 				/* if i=2 --> 119 -> 191 */ 
				int newA = i%(int)(Math.pow(10,j));
				newA *= Math.pow(10,(bDigits-j));
				newA += i/Math.pow(10,j);
				int newADigits = String.valueOf(newA).length();
				if (i<newA && newA<=b && bDigits==newADigits){
					pairsFounds++;
 				}
 			}
 		}
 		return pairsFounds;
 	}
 }
