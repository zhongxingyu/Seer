 /**
  * http://leetcode.com/onlinejudge#question_53
  * 
  * 
  */
 public class MaxSubarray {
 	public int maxSubArray(int[] A) {
 		int maxSofar = 0;
 		int maxEndingHere = 0;
 		boolean allNegative = true;
		int max = 0;
 		for (int element : A) {
 			if (element > max) {
 				max = element;
 			}
 			if (element >= 0) {
 				allNegative = false;
 				break;
 			}
 		}
 		if (allNegative) {
 			return max;
 		}
 		for (int element : A) {
 			maxEndingHere = Math.max(0, maxEndingHere + element);
 			maxSofar = Math.max(maxEndingHere, maxSofar);
 		}
 		return maxSofar;
 	}
 }
