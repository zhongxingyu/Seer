 package monitor.cassandra;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class TestCass extends Thread {
 	private String cassServerlist = "127.0.0.1";
 	SimpleClient cassandraClient;
 	private int loopcount = 10;
 	final static SimpleDateFormat sdf = new SimpleDateFormat(
 			"yyyy/MM/dd HH:mm:ss:z");
 
 	public static void main(String[] args) throws Exception {
 		TestCass objTestCass = new TestCass();
 	/*	objTestCass.cassandraClient = new SimpleClient();
 		objTestCass.cassandraClient.connect("127.0.0.1");
 		//objTestCass.cassandraClient.DropKey();
		objTestCass.cassandraClient.createSchema();
		objTestCass.cassandraClient.close();
 		Thread.sleep(1000);
 		ExecutorService executor = Executors.newFixedThreadPool(100);
 		System.out.println();
 		System.out.println("started at "+getTimestamp(new Date()));
 		for (int i = 0; i <10; i++) {
 			objTestCass = new TestCass();
 			executor.execute(objTestCass);
 		}
 		executor.shutdown();
 		while (!executor.isTerminated()) {
 		}
 		System.out.println("Finished all threads");
		System.out.println("finshed at "+getTimestamp(new Date()));*/
		objTestCass = new TestCass();
 		objTestCass.cassandraClient = new SimpleClient();
		
 		objTestCass.cassandraClient.connect("127.0.0.1");
 		objTestCass.cassandraClient.createSchema();
 		objTestCass.cassandraClient.getQStatus();
 		objTestCass.cassandraClient.close();
 	}
 
 	public void run() {
 		cassandraClient = new SimpleClient();
 		cassandraClient.connect(cassServerlist);
 		
 		for (int i = 0; i < loopcount; i++) {
 			try {
 				//Thread.sleep(1000);
 				String[] values = { "ami-" + Thread.currentThread().getName(),
 						getTimestamp(new Date()), "busy" };
 				cassandraClient.insertNodeStatus(values);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		cassandraClient.close();
 	}
 
 	private static String getTimestamp(Date date) {
 		return sdf.format(date);
 	}
 
 }
