 import java.util.UUID;
 
 public class RandomSource {
	public static final int pool_size = 10000000; 
 	public static final String[] randomSource = new String[pool_size];
 	private static int index = 0;
 	private static boolean __initialized___ = false; 
 	
 	public static class NoMoreEntropyException extends RuntimeException {}
 
 	private static class EntropyGenerator implements Runnable {
 		private int startIndex;
 		public EntropyGenerator(int index) {
 			startIndex = pool_size * index / LazySkipListTester.numThreads;
 		}
 		
 		public void run() {
 			for(int i = 0; i < LazySkipListTester.runTimes; i++) {
 				randomSource[i + startIndex] = UUID.randomUUID().toString();
 			}
 		}
 	}
 	
 	public static void initialize() {
 		if(!__initialized___) {
 			System.out.print("Generating entropy pool:\n");
 			Thread last = null;
 			for(int i = 0; i < LazySkipListTester.numThreads; i++) {
 				last = new Thread(new EntropyGenerator(i));
 				last.start();
 			}
 			while(true) {
 				try {
 					if(null != last) last.join();
 					break;
 				} catch(Exception ex) {
 				}
 			}
 			__initialized___ = true;
 		}
 	}
 }
