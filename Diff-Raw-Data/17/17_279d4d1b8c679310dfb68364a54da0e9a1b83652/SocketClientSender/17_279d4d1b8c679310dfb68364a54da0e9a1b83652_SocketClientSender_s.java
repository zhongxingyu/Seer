 package socket.client;
 
 import java.net.*;
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.io.*;
 
 // thread to do the actual sending of a payload to the server
 // it waits for a response for it's payload, and quits when it's done
 
 public class SocketClientSender implements Callable<double[]> {
 	private Socket socket;
 	private int threadNum;
 	private int clientNum;
 	private ArrayList<double[]>payload; 
 
 	/**
 	 * Basic constructor for a SocketClient thread
 	 * 
 	 * @param s Socket object. Will usually start around port 4444. Host will change.
 	 * @param tn Current thread number
 	 */
 	public SocketClientSender(Socket s, ArrayList<double[]> payload, int tn, int cn){
 		this.socket = s;
 		this.threadNum = tn;
 		this.clientNum = cn;
 
 	}
 
 	/**
 	 * Creates and sends a batch of data. Measures return time.
 	 */
 	public double[] call(){
 		long totalTime = 0, timeTaken = 0, startTime = 0; // Performance metrics
 		ObjectOutputStream oos=null;
 		ObjectInputStream ois=null;
 		double[]result = null;
 		try{
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			ois = new ObjectInputStream(socket.getInputStream());
 		}catch (IOException e){}
 		try{
 			System.out.println("Client:"+clientNum+"|Thread:"+threadNum+": Sending payload ...");
 			startTime = System.currentTimeMillis();//starting timer after creation of payload.
			SocketClientHelper.sendHelper(oos, payload);
 			result = (double[]) SocketClientHelper.receiveHelper(ois);
 			timeTaken = System.currentTimeMillis() - startTime;
 			totalTime += timeTaken;
 			System.out.println("Client:"+clientNum+"|Thread:"+threadNum+": Received result in: "+timeTaken+"ms");
 		}catch (Exception e){
 			e.printStackTrace();
 			try{
 				Thread.sleep(10000);
 			}catch(Exception ex){ex.printStackTrace();}
 		}
 		return result;
 	}
 
 	/**
 	 * Creates the ArrayList of 5 arrays that will be sent to the server.
 	 * 
 	 * @return Returns the 5 arrays in one ArrayList
 	 */
 	public ArrayList<double[]> genPayload(){

 		int size=5; // The chosen number of arrays per connection
 		ArrayList<double[]> payload = new ArrayList<double[]>(size);

 		// Populate the ArrayList
 		for (int i=0; i<size; i++){
 			payload.add(genArray());
 		}
 		return payload;
 	}

 	/**
 	 * Creates the arrays for the ArrayList. Currently set to 1000 numbers per array
 	 * 
 	 * @return Returns an array of 1000 randomly generated doubles.
 	 */
 	public double[] genArray(){
 		double [] arr = new double[1000];

 		// RNG
 		for (int i = 0; i < arr.length; i++){
 			arr[i]=Math.random()*10;
 		}
 		return arr;
 	}

 }
