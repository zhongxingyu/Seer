 /**
 * Given an array, try to find if a single number dominates the others in the array.
 * For example: [12, 85, 39, 92, 12, 12, 92]
 * 12 would be the dominator because it occupies 3 of 7 indeces. That's more than any other number does.
 *
 * Basically, we're finding the mode... but Dominator sounds cooler
* 
* Update: I may have misread the problem. It might be the case that for a number to dominate,
* it must be in more than have of the indeces of the array. So, over half of the array would have to be that number.
* It's a small change. Aaaaaaaaaaand done.
 */
 
 import java.util.*;
 
 public class Dominator{
 	
 	public static void main(String _[]){
		int[] nums = new int[]{12, 85, 39, 92, 12, 12, 92, 12};
		printNums(nums);
		System.out.println("Dominator is " + findDominator(nums));

		nums = new int[]{12, 85, 39, 92, 12, 12, 92, 49, 81, 72};
 		printNums(nums);
 		System.out.println("Dominator is " + findDominator(nums));
 	}
 
 	public static int findDominator(int[] nums){
 		int retVal = 0, highCount = 0;
 		Arrays.sort(nums);
 		HashMap<Integer, Integer> numsMap = new HashMap<Integer,Integer>();
 
 		for(int i = 0, n = nums.length-1; i < n; i++){
 			if(numsMap.get(nums[i]) == null){
 				numsMap.put(nums[i], 1);
 			}else{
				numsMap.put(nums[i], numsMap.get(nums[i])+1);
 				if(numsMap.get(nums[i]) > highCount)
 					retVal = nums[i];
 			}
 		}
 
		if(numsMap.get(retVal) < nums.length/2) retVal = -1;		

 		return retVal;
 	}
 
 	public static void printNums(int[] nums){
 		System.out.print("List contains: ");
 		for(int i = 0, n = nums.length; i < n; i++){
 			System.out.print(nums[i]+" ");
 		}
		System.out.println("");
 	}
 }
