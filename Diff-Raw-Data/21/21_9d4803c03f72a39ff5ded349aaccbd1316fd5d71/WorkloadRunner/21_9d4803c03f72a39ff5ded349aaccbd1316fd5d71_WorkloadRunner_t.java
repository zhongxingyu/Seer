 package workloadgen;
 
 import workloadgen.loadjobs.LoadJobClient;
 import workloadgen.loadjobs.LoadJobController;
 
 
 public class WorkloadRunner {
 	
 	private LoadJobClient client = null;
 	private LoadJobController loadcontroller = null;
 	private static WorkloadRunner _instance = null;
 	public static String confPath = null;
 	public static String tracePath = null;
 	
 	public WorkloadRunner(String conf, String trace){	
 		loadcontroller = new LoadJobController("WorkloadGen");
 		if (confPath == null){
 			confPath = conf;
 		}
 		if (tracePath == null){
 			tracePath = trace;
 		}
 		client = new LoadJobClient(confPath, tracePath, loadcontroller);
 	}
 	
 	/**
 	 * the main loop 
 	 */
 	public void mainService() throws Exception{
 		client.addAllJobs();
 		Thread mainthread = new Thread(loadcontroller);
 		mainthread.start();
 		long startTime = System.currentTimeMillis();
 	    while (!loadcontroller.stopped()) {
 	      System.out.println("Jobs in waiting state: "
 	          + loadcontroller.getWaitingNum());
 	      System.out.println("Jobs in ready state: "
 	          + loadcontroller.getReadyNum());
 	      System.out.println("Jobs in running state: "
 	          + loadcontroller.getRunningNum());
 	      System.out.println("Jobs in success state: "
 	          + loadcontroller.getSuccessNum());
 	      System.out.println("Jobs in failed state: "
 	          + loadcontroller.getFailedNum());
 	      System.out.println("\n");
 
 	      try {
 	        Thread.sleep(10 * 1000);
 	      } catch (Exception e) {
 
 	      }
 	    }
 	    long endTime = System.currentTimeMillis();
	    System.out.println("WorkloadGen results:");
 	    System.out.println("Total num of Jobs: " + loadcontroller.getTotalJobNum());
 	    System.out.println("ExecutionTime: " + ((endTime-startTime)/1000));
 	}
 	
 	/**
 	 * construct the unique instance of WorkloadRunner
 	 * @param conf the configuration file path
 	 * @param trace the trace file
 	 * @return the instance of WorkloadRunner
 	 */
 	private static WorkloadRunner Instance(String conf, String trace){
 		if (_instance == null){
 			_instance = new WorkloadRunner(conf, trace);
 		}
 		return _instance;
 	}
 	
 	/**
 	 * @param args[0] confPath
 	 * @param args[1] tracePath
 	 */
 	public static void main(String[] args) throws Exception{
 		
 		WorkloadRunner.Instance(args[0], args[1]).mainService();
 	}
 
 }
