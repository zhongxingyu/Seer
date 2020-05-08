 package projectEuler;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Problem040 {
 
 	public static void main(String[] args) {
 
 		long begin = System.currentTimeMillis();
 		
 		int rollingCounter = 0;
 		int result = 1;
 		final int LIMIT = 1000000;
 		
 		Set<Integer> set = new HashSet<Integer>();
 		
 		for (int pow = 0; pow < 7 ; pow++){
			set.add((int)Math.pow(10, pow));	
 		}
 				
 		for (long l = 1L; l <= LIMIT; l++){
 			for (int i = 0; i < String.valueOf(l).length(); i++){
 				rollingCounter++;
 				if (set.contains(rollingCounter)){
 					System.out.println(rollingCounter + "\t:\t" + String.valueOf(l).charAt(i));
 					result *=  String.valueOf(l).charAt(i) - '0';
 					set.remove(rollingCounter);
 				}
 			}
 			if (set.isEmpty()){
 				break;
 			}
 		}
 		
 		System.out.println("Product: " + result);
 		
 		long end = System.currentTimeMillis();
 		System.out.println("Time: " + (end - begin) + " ms.");
 		
 	}
 
 }
