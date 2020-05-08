 package rmi.client;
 
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 
 import rmi.server.RmiServer;
 
 public class RmiClientSender implements Callable<double[]>{
 	public RmiServer server;
 	public ArrayList<double[]>payload;
 	public int clientNum;
 	public int threadNum;
 	
 	public RmiClientSender(RmiServer server, ArrayList<double[]>payload, int tn, int cn){
 		this.server = server;
 		this.payload = payload;
 		this.clientNum = cn;
 		this.threadNum = tn;
 	}
 	
 	public double[] call(){
 		long startTime = 0;
 		long timeTaken = 0;
 		long totalTime = 0;
 		double[] result = null;
 		try{
 			System.out.println("Client:"+clientNum+"|Thread:"+threadNum+": Sending payload ...");
 			startTime = System.currentTimeMillis(); // measuring only the time taken to transfer
 			result = server.getAverage(payload);
 			timeTaken = System.currentTimeMillis() - startTime;
 			totalTime+=timeTaken;
 			System.out.println("Client:"+clientNum+"|Thread:"+threadNum+": Received result in: "+timeTaken+"ms");
		}catch (Exception e){e.printStackTrace();}		
 		return result;
 	}
 }
