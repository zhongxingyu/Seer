 import java.util.Locale;
 import java.util.Random;
 
 /**
  * Benchmark the difference between iteration on {@code ArrayList<Integer>} and regular java array.
  * @author Roman Elizarov
  */
 public class IntListIterationTiming {
 	private static final String[] CLASS_NAMES = new String[]{"IntList$ViaArrayList", "IntList$ViaJavaArray"};
 	private static int dummy; // to avoid HotSpot optimizing away iteration
 	private final IntList list;
 
 	@SuppressWarnings("unchecked")
 	private IntListIterationTiming(String className, int size) throws Exception {
 		list = (IntList)Class.forName(className).newInstance();
 		Random random = new Random(1);
 		for	(int i = 0; i < size; i++)
 			list.add(random.nextInt());
 	}
 
 	private double time() {
 		int reps = 100000000 / list.size();
 		long start = System.nanoTime();
 		for	(int rep = 0; rep < reps; rep++)
 			dummy += runIteration();
 		return (double)(System.nanoTime() - start) / reps / list.size();
 	}
 
 	private int runIteration() {
 		int sum = 0;
 		for (int i = 0, n = list.size(); i < n; i++)
 			sum += list.getInt(i);
 		return sum;
 	}
 
 	public static void main(String[] args) throws Exception {
 		for (int pass = 1; pass <= 3; pass++) { // 2 passes to let JIT compile everything, look at 3rd
			System.out.printf("----- PASS %d -----%d%n", pass);
 			for (int size = 1000; size <= 10000000; size *= 10) {
 				for (String className : CLASS_NAMES) {
 					dummy = 0;
 					IntListIterationTiming timing = new IntListIterationTiming(className, size);
 					double time = timing.time();
 					System.out.printf(Locale.US, "%30s[%8d]: %.2f ns per item (%d)%n", className, size, time, dummy);
 				}
 			}
 		}
 	}
 }
