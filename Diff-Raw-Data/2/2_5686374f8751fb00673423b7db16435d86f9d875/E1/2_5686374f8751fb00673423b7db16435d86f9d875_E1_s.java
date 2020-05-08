 package thyscom.eulersolutions;
 
 /**
  * Add all the natural numbers below one thousand that are multiples of 3 or 5.
  */
 public class E1 {
 	public static void main(String[] args) {
 		E1 e1 = new E1();
 		System.out.println("Solution 1: " + e1.trial());
 		System.out.println("Solution 2: " + e1.progression());
 	}
 	
 	private int trial() {
 		int total = 0;
 		for(int i=0; i<1000; i++) {
 			if((i % 5 == 0 ) || (i % 3 == 0)) {
 				total += i;
 			}
 		}
 		return total;
 	}
 	
 	
 	private int progression() {
 		return sumDivisibleBy(3, 999) + sumDivisibleBy(5, 999) - sumDivisibleBy(15, 999);	
 	}
 	
 	/*
	 * 1+2+3+...+p=(*p*(p+1))/2
 	 */
 	private int sumDivisibleBy(int div, int max) {
 		int p = max / div;
 		return (int) div * (p * (p + 1))/2;
 	}
 }
