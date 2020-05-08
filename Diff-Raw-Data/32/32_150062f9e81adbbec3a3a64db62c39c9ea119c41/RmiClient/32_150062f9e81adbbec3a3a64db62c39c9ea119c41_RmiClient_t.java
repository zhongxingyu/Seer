 package rmi.client;
 
 import java.rmi.registry.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import rmi.server.RmiServer;
 
 public class RmiClient {
 	private int clientNum;
 	private String hostname;
 	private int scenario;
 	
 	public RmiClient(int cn, String shn, int scenario){
 		this.clientNum = cn;
 		this.hostname = shn;
 		this.scenario = scenario;
 	}
 	
 	public static void main(String[] args) {
 		int scenario = Integer.parseInt(args[0]);
 		String hostname = args[1];
 		int clientNum = Integer.parseInt(args[2]);
 		System.out.println("Running Scenario: "+scenario+" on hostname: "+hostname);
 		RmiClient client = new RmiClient(clientNum, hostname, scenario);
 		client.execute();
 	}
 
 	public void execute(){
 		int size; 
 		long startTime = 0;
 		if (scenario == 2)
 			size = 10;
 		else
 			size = 1;
 		
 		ExecutorService es = Executors.newFixedThreadPool(size); // controls the thread pool. allows us to return values from threads; very useful!
 		Set<Callable<double[]>> senderSet = new HashSet<Callable<double[]>>();
 		Set<Future<double[]>> resultSet = new HashSet<Future<double[]>>(); // the "Future" class, blocks on the completion of the thread
 		
 		try {
 			String name = "RmiServer"; // the name of the interface we're trying to call the implementation for
 			Registry reg = LocateRegistry.getRegistry(hostname); // get the registry that contains the reference to our RmiServerImpl, located at serverHostname
 			RmiServer server = (RmiServer) reg.lookup(name); // retrieve our RmiServer reference from the registry
 			for (int i = 0; i < size; i++){
 				try{
 					Callable<double[]>sender = new RmiClientSender(server, genPayload(), i, clientNum);
 					senderSet.add(sender);
 					
				} catch (Exception e){ 
					e.printStackTrace();
					try{
						Thread.sleep(10000);
					}catch(Exception ex){ex.printStackTrace();}
				}
 			}
 			startTime = System.currentTimeMillis();
 			for (Callable<double[]> sender : senderSet){
 				Future<double[]> future = es.submit(sender);
 				resultSet.add(future);
 			}
 			for (Future<double[]> result : resultSet){
 				result.get();
 			}
		} catch (Exception e){
			e.printStackTrace();
			try{
				Thread.sleep(10000);
			}catch(Exception ex){ex.printStackTrace();}
		}
		System.out.println("Client:"+clientNum+": Received all results in: "+(System.currentTimeMillis()-startTime)+"ms");
		try{
			Thread.sleep(100000);
		}catch(Exception ex){ex.printStackTrace();}
 	}
 	
 	public ArrayList<double[]> genPayload(){
 		int size=5;
 		ArrayList<double[]> payload = new ArrayList<double[]>(size);
 			for (int i=0; i<size; i++){
 				payload.add(genArray());
 			}
 			return payload;
 	}
 	
 	public double[] genArray(){
 		double [] arr = new double[1000];
 		for (int i = 0; i < arr.length; i++){
 			arr[i]=Math.random()*10;
 		}
 		return arr;
 	}
 
 }
 
 
