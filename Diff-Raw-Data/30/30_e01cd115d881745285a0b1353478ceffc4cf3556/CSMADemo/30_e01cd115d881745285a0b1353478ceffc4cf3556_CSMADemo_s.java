 package exec;
 
 import graph.GraphMaker;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.jfree.data.xy.XYSeries;
 
 import threadtype.CSMAThreadNP;
 import threadtype.CSMAThreadP;
 import util.LogUtil;
 import util.ThreadUtil;
 
 @SuppressWarnings("unused")
 public class CSMADemo {
 	public static final String X_AXIS_GRAPH_TITLE = "Time (sec)";
 	public static final String Y_AXIS_GRAPH_TITLE = "Client Number";
 
 	public static long THREAD_START_INTERVAL = 500L;
 	public static int THREAD_COUNT = 10;
 	public static long CHANNEL_USE_PERIOD = 1500L;
 	public static Long START_TIME;
 	public static long PP_WAIT_UNIT_TIME = 150L;
 	public static Float PP_PROB = 0.27F;
 	public static int NP_MAX_RANDOM_WAIT = 2000;
 
 	public static void main(String[] args) {
 		CSMADemo obj = new CSMADemo();
 		// obj.csmaPPTesterLoop();
 		obj.csmaPPTester();
 		// obj.csma1PTesterLoop();
 		obj.csma1PTester();
 		// obj.csmaNonPTesterLoop();
 		obj.csmaNonPTester();
 		LogUtil.printLog("*** main END ***");

 	}
 
 	private void csmaPPTesterLoop() {
 		THREAD_START_INTERVAL = 500L;
 		THREAD_COUNT = 10;
 		CHANNEL_USE_PERIOD = 1500L;
 		PP_WAIT_UNIT_TIME = 150L;
 		PP_PROB = 0.2F;
 		float throughput = 0.0f;
 		for (int i = 0; i < 5; i++) {
 			throughput += csmaPPTester();
 			PP_PROB = (float) (PP_PROB + 0.20);
 		}
 		System.out.println("throughput=" + throughput / 5);
 	}
 
 	/*
 	 * p-Persistent CSMA Protocol:
 	 * 
 	 * 1. If the medium is idle, transmit with probability p, and delay for one time unit with probability (1 - p) (time unit = length of propagation delay)
 	 * 
 	 * 2. If the medium is busy, continue to listen until medium becomes idle, then go to Step 1
 	 * 
 	 * 3. If transmission is delayed by one time unit, continue with Step 1
 	 */
 	private float csmaPPTester() {
 		System.out.println("\n");
 		START_TIME = System.currentTimeMillis();
 		String graphTitle = "P-Persistent Channel Use Graph";
 		LogUtil.printLog("*** csmaPPTester ***");
 		float throughput = commonHelper(graphTitle, new CSMAThreadP(false));
 		LogUtil.printLog("*** csmaPPTester END ***");
 		return throughput;
 	}
 
 	private void csma1PTesterLoop() {
 		THREAD_START_INTERVAL = 500L;
 		THREAD_COUNT = 10;
 		CHANNEL_USE_PERIOD = 1500L;
 		float throughput = 0.0f;
 		for (int i = 0; i < 5; i++) {
 			throughput += csma1PTester();
 		}
 		System.out.println("throughput=" + throughput / 5);
 	}
 
 	/*
 	 * 1. If the medium is idle, transmit immediately
 	 * 
 	 * 2. If the medium is busy, continue to listen until medium becomes idle, and then transmit immediately
 	 */
 	private float csma1PTester() {
 		System.out.println("\n");
 		START_TIME = System.currentTimeMillis();
 		String graphTitle = "1-Persistent Channel Use Graph";
 		LogUtil.printLog("*** csma1PTester ***");
 		float throughput = commonHelper(graphTitle, new CSMAThreadP(true));
 		LogUtil.printLog("*** csma1PTester END ***");
 		return throughput;
 	}
 
 	private void csmaNonPTesterLoop() {
 		THREAD_START_INTERVAL = 500L;
 		THREAD_COUNT = 10;
 		CHANNEL_USE_PERIOD = 1500L;
 		NP_MAX_RANDOM_WAIT = 500;
 		float throughput = 0.0f;
 		for (int i = 0; i < 5; i++) {
 			throughput += csmaNonPTester();
 			NP_MAX_RANDOM_WAIT += 500;
 		}
 		System.out.println("throughput=" + throughput / 5);
 
 	}
 
 	/*
 	 * 1. If the medium is idle, transmit immediately
 	 * 
 	 * 2. If the medium is busy, wait a random amount of time and repeat Step 1
 	 */
 	private float csmaNonPTester() {
 		System.out.println("\n");
 		START_TIME = System.currentTimeMillis();
 		String graphTitle = "Non-Persistent Channel Use Graph";
 		LogUtil.printLog("*** csmaNonPTester ***");
 		float throughput = commonHelper(graphTitle, new CSMAThreadNP());
 		LogUtil.printLog("*** csmaNonPTester END ***");
 		return throughput;
 	}
 
 	private float commonHelper(String iGraphTitle, Runnable iRunnable) {
 		List<Thread> theThreads = ThreadUtil.threadStart(THREAD_COUNT, iRunnable, THREAD_START_INTERVAL);
 		for (Thread aThread : theThreads) {
 			try {
 				aThread.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		float timeTaken = (float) (System.currentTimeMillis() - START_TIME) / 1000;
 		float throughput = timeTaken / THREAD_COUNT;
 		System.out.println("Throughput for " + THREAD_COUNT + " clients = " + throughput + " (sec/client)");
 		timeTaken += 5;
 		new GraphMaker(iGraphTitle, X_AXIS_GRAPH_TITLE, Y_AXIS_GRAPH_TITLE, ThreadUtil.THREAD_NAME_XY_MAP.values(), timeTaken);
 		cleanUp();
 		return throughput;
 	}
 
 	private void cleanUp() {
 		ThreadUtil.THREAD_NAME_ID_MAP = new HashMap<String, Integer>();
 		ThreadUtil.THREAD_NAME_XY_MAP = new HashMap<String, List<XYSeries>>();
 	}
 
 }
