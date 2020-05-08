 package main.com.simpragma.primenumber;
 
 public class PrimeNum {
	public Boolean isItPrime(Integer num) {

		if (num <= 1) {
			return false;
		}
 		
		int sq = (int)Math.sqrt(num);
 		
		for (int i=2; i<sq; i++) {
 			if (num%i == 0) {
				return false;
 			}
 		}
		return true;
 	}
}
