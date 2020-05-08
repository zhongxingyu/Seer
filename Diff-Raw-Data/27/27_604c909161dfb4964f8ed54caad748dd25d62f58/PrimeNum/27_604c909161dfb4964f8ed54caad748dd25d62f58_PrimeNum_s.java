 package main.com.simpragma.primenumber;
 
 public class PrimeNum {
	public static void main(String[] args) {
 		
		Integer num = Integer.valueOf(args[0]);
 		
		if (num < 1) {
			System.out.println("Not a natural number");
			return;
		}
		for (int i=2; i<Math.sqrt(num); i++) {
 			if (num%i == 0) {
				System.out.println(num + " is not a prime number");
				return;
 			}
 		}
		System.out.println(num + " is a prime number");
 	}
}
