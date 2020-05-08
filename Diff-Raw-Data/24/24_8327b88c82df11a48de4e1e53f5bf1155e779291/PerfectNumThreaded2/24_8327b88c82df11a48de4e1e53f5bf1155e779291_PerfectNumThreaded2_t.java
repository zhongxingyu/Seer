 import java.util.ArrayList;
 import java.util.List;
 
 public class PerfectNumThreaded2 {
 	public static int MAX_THREADS = Runtime.getRuntime().availableProcessors();
 	public static List<Integer> nums = new ArrayList<Integer>();
 	public static int found = 0;
	
 	public static void main(String[] args) throws Exception {
 		for (int i = 0; i < MAX_THREADS; i++) {
			PerfectNumThread2 th1 = new PerfectNumThread2(6 + 2 * i);
 			th1.start();
 		}
 		System.out.println("Num threads: " + MAX_THREADS);
 	}
	
 	public static boolean isPerfect(int num) {
 		int factorSum = 0;
 		int halfnum = num / 2 + 1;
 		// divide the number by every number
 		// below it up until half the number
 		for (int i = 1; i < halfnum; i++) {
 			// if we found the factor then add it to the total
 			if (num % i == 0)
 				factorSum += i;
 			// if we are past the total, then we can exit
 			if (factorSum > num)
 				break;
 		}
 		return factorSum == num;
 	}
 }
 
 class PerfectNumThread2 extends Thread {
	
 	int startNum;
	
 	public PerfectNumThread2(int num) {
 		this.startNum = num;
 	}
	
 	@Override
 	public void run() {
 		long startTime = System.currentTimeMillis();
 		int num = startNum;
 		while (PerfectNumThreaded2.found != 5) {
 			if (PerfectNumThreaded2.isPerfect(num)) {
 				PerfectNumThreaded2.found++;
 				PerfectNumThreaded2.nums.add(num);
 				System.out.println(num);
 			}
 			num += PerfectNumThreaded2.MAX_THREADS * 2;
			if (num % 100000 == 0) {
				System.out.println("Checking: " + num);
			}
 		}
		System.out.println("Total time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.");
 	}
 }
