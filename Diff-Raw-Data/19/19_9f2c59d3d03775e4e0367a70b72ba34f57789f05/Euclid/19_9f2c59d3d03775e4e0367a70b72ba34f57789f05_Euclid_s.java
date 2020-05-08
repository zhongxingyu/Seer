 package main;
 
 public class Euclid {
 	
 	public static int GCD(int m, int n) {
 		//Ensure m is greater than n otherwise swap
 		if(m<n){int t=m;m=n;n=t;}
 
 		int r;
 		while((r=m%n)!=0) {
 			m = n;
 			n = r;
 		} 
 
 		return n;
 	}
 }
