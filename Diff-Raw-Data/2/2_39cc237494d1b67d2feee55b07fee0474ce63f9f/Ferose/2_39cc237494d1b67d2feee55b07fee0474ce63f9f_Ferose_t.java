 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 public class Ferose {
 
 	public static void main(String[] args) throws FileNotFoundException {
 		start(args[0]);
 	}
 
 	public static void start(String filename) throws FileNotFoundException {
 		Scanner in = new Scanner(new File(filename));
 		new Solution(in);
 		in.close();
 	}
 
 	public static class Solution {
 
 		public Solution(Scanner in) {
 			while (in.hasNextLong()) {
 				System.out.println(compute(in.nextLong(), in.nextLong()));
 			}
 		}
 
 		private String compute(long n, long k) {
 			long addition = n - 1;
 			long sum = 0;
 			long count = 0;
			while (sum < k) {
 				sum += addition--;
 				count++;
 			}
 			return count + " " + (count + 1 + k - (sum - addition));
 		}
 	}
 }
