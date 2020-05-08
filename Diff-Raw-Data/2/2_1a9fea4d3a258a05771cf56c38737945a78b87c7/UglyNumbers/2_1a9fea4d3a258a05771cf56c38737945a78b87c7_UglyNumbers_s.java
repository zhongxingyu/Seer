 package com.shashank.uglynumbers;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
 import java.util.LinkedHashSet;
 
 public class UglyNumbers {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println(getUgly(150));
 	}
 	private static int getUgly(int i) {
 		int uglyArray [] = new int[i];
 		uglyArray[0] = 1;
 		for (int n=1 ; n<i ; n++) {
 			ArrayList<Integer> arr = new ArrayList<Integer>();
 			for (int j=1 ; j<=n ;j++) {
 				arr.add(uglyArray[j-1]*2);
 				arr.add(uglyArray[j-1]*3);
 				arr.add(uglyArray[j-1]*5);
 			}
 			Collections.sort(arr);
 			LinkedHashSet<Integer> hs = new LinkedHashSet<Integer>();
 			hs.addAll(arr);
 			Object[] arr1 = hs.toArray();
 			uglyArray[n] = (Integer) arr1[n-1];
 		}
 		return uglyArray[i-1];
 	}
 }
