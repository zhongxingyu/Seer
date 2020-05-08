 package rmi.server;
 
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.rmi.server.*;
 import java.rmi.registry.*;
 
 public class RmiServerImpl implements RmiServer {
 	ExecutorService es;
 	public RmiServerImpl(){
 		this.es = Executors.newFixedThreadPool(10);
 	}	
 	
 	public static void main(String [] args){
 		try{
 			int port = Integer.parseInt(args[0]);
 			int registryPort = 1099;
 			if (args.length > 1){
 				registryPort = Integer.parseInt(args[1]);
 			}
 						
 			String name = "RmiServer";
 			RmiServer server = new RmiServerImpl(); // create our server object
 			RmiServer stub = (RmiServer) UnicastRemoteObject.exportObject(server,port); // makes the server available for remote calls on port 
 			Registry reg = LocateRegistry.createRegistry(registryPort); // create a new registry on port 1099
 			reg.rebind(name,stub); // register the name RmiServer with the registry, which the client uses to call this Class
 			System.out.println("Rmi Server startup complete.\nRegistry listening on port: "+registryPort+"\nServer listening on port: "+port); // ready for calls
		} catch (Exception e){
			e.printStackTrace();
			try{
				Thread.sleep(10000);
			}catch(Exception ex){ex.printStackTrace();}
		}
 	}
 	
 	public double[] getAverage(ArrayList<double[]>payload){ // implement the RmiServer interface
 		double[] result = null;
 		Callable<double[]> request = new RmiServerThread(payload); 
 		Future<double[]> future = es.submit(request);
 		try{
 			result = future.get();
		}catch (Exception e){
			e.printStackTrace();
			try{
				Thread.sleep(10000);
			}catch(Exception ex){ex.printStackTrace();}
		}
 		return result;
 	}
 	
 	
 }
