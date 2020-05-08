 /*
  * RcpuMaster handles the logical communication between
  * the scheduler and the distributed nodes. Note that 
  * the communication is tunneled through the service gat-
  * eway to avoid a lot of network issues. The Master is
  * also the only one aware of the the nodes it is in cha-
  * of and the current work queue for each working node. It
  * is also aware of the partitions assigned from the sch-
  * eduler and is responsible for calculating the strings.
  */
 package rcpu;
 
 import java.io.*;
 import java.net.*;
 import java.util.Queue;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.math.BigInteger;
 
 class RcpuMaster extends Thread {
	private Socket serviceGateway = null;
	private PrintWriter sgOut = null;
 	private BufferedReader sgIn = null;
 	private Queue<BigInteger> partitionIndices = new LinkedList<BigInteger>();
	private PriorityQueue<RcpuWorker> nodes = new PriorityQueue<RcpuWorker>();
 	private String keyspace = null;
 	
 	public RcpuMaster(String sgHost) throws Exception {
 		this(sgHost, 7000);
 	}
 
 	public RcpuMaster(String sgHost, int port) throws Exception {
 		try {
 			this.serviceGateway = new Socket(sgHost, port);
 			this.sgOut = new PrintWriter(this.serviceGateway.getOutputStream(), true);
 			this.sgIn = new BufferedReader(new InputStreamReader(this.serviceGateway.getInputStream()));
 			this.bootstrap();
 		}
 		catch (UnknownHostException e) {
 			System.out.println("Could not find address of service gateway host " + sgHost);
 			throw new Exception("Could not resolve sg");
 		}
 		catch (IOException e) {
 			System.out.println("Failed to establish TCP connection with service gateway " + sgHost);
 			throw new Exception("Could not connect to sg");
 		}
 	}
 
 	public void bootstrap() {
 		// register
 		
 		// get keyspace
 	}
 
 	/*
 	 * Makes a connection to the Service Gateway
 	 * and waits for jobs to get pushed, manages
 	 * the global queue and keeps track of worker
 	 * threads.
 	 */
 	public void run() {
 		while (true) {
 			System.out.println("Running...");
 			try {
 				this.sgOut.println("Hello?");
 				System.out.println(this.sgIn.readLine());
 				Thread.sleep(5000);
 			}
 			catch (Exception e) {
 				System.out.println("FAILED!");
 				System.out.println(e);
 			}
 		}	
 	}
 }
