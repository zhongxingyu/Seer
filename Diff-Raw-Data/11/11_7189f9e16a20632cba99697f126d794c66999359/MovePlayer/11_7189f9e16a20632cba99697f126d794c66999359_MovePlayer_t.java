 package abhinav.rajan.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.abhinav.rajan.ChangeCoordinates;
 import com.abhinav.rajan.Notify;
 import com.abhinav.rajan.PolicyFileLocator;
 
 public class MovePlayer{
 	private static ChangeCoordinates changecord;
 	private static String myKey;
 	
 	public MovePlayer(){
 		
 		myKey=new ClientKeygen().genKey();
 		
 	}
 
 	
 
 	public void doCustomRmiHandling() {
 		Registry registry;
 		try {
 			//Set system properties
 			System.setProperty("java.security.policy", PolicyFileLocator.getLocationOfPolicyFile());
			registry = LocateRegistry.getRegistry("172.20.10.10",9000);
 			changecord= (ChangeCoordinates)registry.lookup(ChangeCoordinates.SERVICE_NAME);
 			 
 			HashMap<String, Object> connect=changecord.connectToServer(myKey);
 			if(connect==null){
 				System.out.println("The game has already started please try again later...");
 				System.exit(0);
 				
 			}
 			
 			//Once connection has been successfully established, start sending periodic heart beats to the server
 			Thread heartbeat=new Thread(new HeartBeatSender());   
 			heartbeat.setDaemon(true);
 			heartbeat.start();
 		         
 		    //Print the current state 
 			
 			System.out.println("_________________________");
 			System.out.println("MY STATE"+"==>"+connect.get(myKey));
 			System.out.println("NO_OF_PLAYERS"+"==>"+connect.get("NO_OF_PLAYERS"));
 			System.out.println("GRID WITH TREASURES");
 			
 			AtomicInteger[][] grid=(AtomicInteger[][]) connect.get("GRID");
 			for(int i=0;i<10;i++){
 				for(int j=0;j<10;j++){
 					System.out.print(grid[i][j]+" ");
 				}
 				System.out.println("");
 			}
 		
 			
 			System.out.println("_________________________");
 			
 			//Wait for a second before executing the moves
 			Thread.sleep(1000);
 						 
 			System.out.println("Please start moving. Enter L/l for LEFT, R/r for RIGHT, U/u for UP and any key for DOWN ");
 			
 						while(true){							
 							BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
 							String move = bufferRead.readLine();
 		            		HashMap<String, Object> afterMove=changecord.moveToLocation(move,myKey);
 		            		
 		            		if(afterMove==null){
 		            			System.out.println("Game Over...");
 		            			System.exit(0);
 		            		}else if(afterMove.get(myKey)=="DISCONNECTED"){
 		            			System.out.println("Time out error. Game over!!");
 		            			System.exit(0);
 		            			
 		            		}else{
 		            			
 		            		System.out.println("________AFTER MOVE__________");
 		        			System.out.println("MY STATE"+"==>"+afterMove.get(myKey));
 		        			System.out.println("NO_OF_PLAYERS"+"==>"+afterMove.get("NO_OF_PLAYERS"));
 		        			System.out.println("GRID WITH TREASURES");
 		        			
 		        			final AtomicInteger[][] gridAfterMove=(AtomicInteger[][]) afterMove.get("GRID");
 		        			for(int i=0;i<10;i++){
 		        				for(int j=0;j<10;j++){
 		        					System.out.print(gridAfterMove[i][j]+" ");
 		        				}
 		        				System.out.println("");
 		        			}
 		        		
 		        			
 		        			System.out.println("_______END OF AFTER MOVE________");
 		        		
 						}	
 		            		Thread.sleep(100);
 						}
 								            	
 		           
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
 		
 	}
 	 public static void main(String[] args) {
 		 
      	new MovePlayer().doCustomRmiHandling();
 	        
 	    }
 	 	 
 	 private static class HeartBeatSender extends Thread{
 		 @Override
 		public void run(){
 			 while(true){
 			 try {
 				 Notify note=new NotifyImpl();
 				 changecord.heartBeat(myKey,note);
 				Thread.sleep(2000);
 			} catch (RemoteException e) {
 				e.printStackTrace(); 
 			} catch (InterruptedException e) {
 				
 				e.printStackTrace();
 			}
 			 
 			 }
 		 }
 		 
 	 }
 	 
 	 @SuppressWarnings("serial")
 		private static class NotifyImpl extends UnicastRemoteObject implements Notify{
 			 protected NotifyImpl() throws RemoteException {
 				super();
 			}
 
 			@Override
 				public void onSuccess() throws RemoteException {
 					//System.out.println("\n All players alive");				
 				}
 
 				@Override
 				public void onFailure(String crashedClient) throws RemoteException {
 					System.out.println(crashedClient+" crashed");
 					
 				}
 
 			 
 		 }
 
 	 
 	 
 	
 }
