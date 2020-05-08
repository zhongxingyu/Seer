 public class LazySkipListTester {
 	static final int numThreads = Runtime.getRuntime().availableProcessors();
 	static final int runTimes = 100000;
 	private static LazySkipList skipList;
 	private static long startTime;
 	private static long endTime;
 	
 	private static class TestThread implements Runnable {
     int id;
     public TestThread(int id) {
		  this.id = id;
    }
 		public void run() {
 			while(System.nanoTime() - startTime < 0) ;
 			for(int i = 0; i < runTimes; i++) {
 				skipList.add(RandomSource.randomSource[id*runTimes+i]);
 			}
 			if(id + 1 == numThreads) endTime = System.nanoTime();
 		}
 	}
 	
 	public static void main(String[] args) {
 		RandomSource.initialize();
 		System.out.println(numThreads);
 		Thread[] workers = new Thread[numThreads];
 		startTime = System.nanoTime() + 1000000000L;
 		skipList = new LazySkipList();
 		for(int i = 0; i < numThreads; i++) {
 			workers[i] = new Thread(new TestThread(i));
 			workers[i].start();
 		}
 		System.out.println("Initialized");
 		for(int i = 0; i < numThreads; i++) {
 			try {
 				workers[i].join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println((endTime - startTime) + "ns");
 	}
 }
