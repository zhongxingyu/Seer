 import java.util.ArrayDeque;
 import java.util.Random;
@SuppressWarnings("unchecked")
 public class RadixSort {
 
 	public int[] sort(int[] us, int base) {
 		ArrayDeque<Integer>[] buckets = new ArrayDeque[base];
 		for (int i = 0; i < base; i++) {
 			buckets[i] = new ArrayDeque<Integer>();
 		}
 		int max = 0;
 		for (int i = 0; i < us.length - 1; i++) {
 			max = us[i];
 			// get the biggest number
 			if (max < us[i + 1]) {
 				max = us[i + 1];
 			}
 		}
 		int digits = (int) (Math.log10(max) + 1);
 		// run through the whole array j times where j is the number of
 		// digits
 		int divisor = 1;
 		for (int j = 0; j < digits; j++) {
 			for (int i = 0; i < us.length; i++) {
 				int val = us[i];
 				int digit = val / divisor % base;
 				buckets[digit].add(us[i]);
 			}
 			int index = 0;
 			for (ArrayDeque<Integer> deq : buckets) {
 				while (!deq.isEmpty()) {
 					int num = deq.remove();
 					us[index++] = num;
 				}
 			}
 			divisor *= 10;
 		}
 		return us;
 	}
 }
