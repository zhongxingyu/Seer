 
 /*
 a Prime number generator, using the sieve of erasthostenos (or something)
 */
 import java.util.ArrayList;
 class PrimeGenerator {
 	
 	/*
 	generate all prime numbers upto (inclusive) n
 	*/
	public static ArrayList<Integer> generate(int n){
		boolean[] sieve = new boolean[n];
 		double sqrt = Math.sqrt(n);
 		for(int i = 2;i<=sqrt; i++){
			for(int j= 2; i*j < n;j++){
				boolean[i*j]=true;
 			}
 		}
 		
 		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i=0; i<n; i++) {
			if(! sieve[i]) list.append(sieve[i]+1)
 		}
 		return list;
 	}
 	
	public static void main(String[] args) {
		Test.test
 	}
 }
