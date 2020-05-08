 package _031;
 
 
 public class _031 {
 	
 	private int max;
 
 	private int[] coins = { 1, 2, 5, 10, 20, 50, 100, 200 };
 
 	public static void main(String[] args) {
 		new _031();
 	}
 
 	/**
 	 * created a general solution in case I have to do similar problem later
 	 */
 	public _031() {
 		max = 200;
 		// System.out.println(count(max, coins.length - 1));
 		System.out.println(dynamic());
 	}
 
 	private int dynamic() {
 		int[] combMat = new int[max + 1];
 		combMat[0] = 1; // start with one combination
 
 		// for each coin iterate through, layering, and summing up total combinations
 		for (int i = 0; i < coins.length; i++) {
 			for (int j = coins[i]; j <= max; j++) {
 				combMat[j] += combMat[j - coins[i]];
 			}
 		}
 		return combMat[combMat.length - 1];
 	}
 
 	@SuppressWarnings("unused")
 	private int bruteForce() {
 		int combinations = 0;
 		for (int a = max; a >= 0; a -= 200) {
 			for (int b = a; b >= 0; b -= 100) {
 				for (int c = b; c >= 0; c -= 50) {
 					for (int d = c; d >= 0; d -= 20) {
 						for (int e = d; e >= 0; e -= 10) {
 							for (int f = e; f >= 0; f -= 5) {
 								for (int g = f; g >= 0; g -= 2) {
 									combinations++;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return combinations;
 	}
 
 	private int count(int amount, int coin) {
 		if(coin < 1) {
 			return 1; // base case
 		}
 		int total = 0;
 		while(amount >= 0) {
 			total += count(amount, coin - 1); 
 			amount -= coins[coin];
 		}
 		return total;
 	}
 
 }
