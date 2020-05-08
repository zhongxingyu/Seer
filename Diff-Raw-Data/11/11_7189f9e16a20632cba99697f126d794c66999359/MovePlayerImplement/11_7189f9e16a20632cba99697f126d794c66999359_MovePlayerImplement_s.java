 package abhinav.rajan.server;
 
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 import com.abhinav.rajan.ChangeCoordinates;
 import com.abhinav.rajan.Notify;
 
 public class MovePlayerImplement implements ChangeCoordinates {
 	public static int UNIQUE_ID=1000;
 	private static AtomicInteger NUMBER_OF_PLAYERS=new AtomicInteger();
 	private static int XCORD=7;
 	private static int YCORD=3;
 	private static AtomicInteger[][] initGrid= new AtomicInteger[10][10];
 	private static HashMap <String,Object> connectReturn;
 	private static boolean CONNECT_FLAG=true;
 	private static int sumOfTreasures=0;
 	private static boolean trasuresExist=true;
 	private static ConcurrentHashMap<String,Long> clientListLocal;
 	CheckForAndUpdateFailures heart= new CheckForAndUpdateFailures();
 	
 	
 	public MovePlayerImplement(){
 		Random random=new Random();
 		for(int i=0;i<10;i++){
 			for(int j=0;j<10;j++){
 				initGrid[i][j]=new AtomicInteger(random.nextInt(5)); //Making the number of treasure from 0 to 4
 				sumOfTreasures+=initGrid[i][j].get();
 			}	
 		}
 		connectReturn=new HashMap<String,Object>();
 		clientListLocal=new ConcurrentHashMap<String,Long>();
 	}
 
 	@Override
 	public HashMap<String, Object> moveToLocation(String keyPressed, String playerId)
 			throws RemoteException {
 		if(trasuresExist){
 			if(connectReturn.get(playerId)==null){
 				HashMap <String,Object> error=new HashMap<String,Object>();
 				error.put(playerId, "DISCONNECTED");
 				return error;
 				
 			}
 		HashMap<String,Integer> playerInfo=	(HashMap<String, Integer>) connectReturn.get(playerId);
 		int xCord=playerInfo.get("XCORD");
 		int yCord=playerInfo.get("YCORD");
 		
 		if(keyPressed.equalsIgnoreCase("L")){
 			if(yCord-1>=0)
 			playerInfo.put("YCORD",--yCord);
 		}else if(keyPressed.equalsIgnoreCase("R")){
 			if((yCord+1)<=9)
 			playerInfo.put("YCORD",++yCord);
 		}else if(keyPressed.equalsIgnoreCase( "U")){
 			if((xCord-1)>=0)
 			playerInfo.put("XCORD",--xCord);
 		}else{
 			if(xCord+1<=9){
 				playerInfo.put("XCORD",++xCord);
 			}
 		}
 		if(initGrid[xCord][yCord].get()>0){
 			initGrid[xCord][yCord].set(initGrid[xCord][yCord].decrementAndGet());
 		int treasureCollected=playerInfo.get("TREASURES_COLLECTED");
 		playerInfo.put("TREASURES_COLLECTED", treasureCollected+1);
 		connectReturn.put(playerId, playerInfo);
 		connectReturn.put("GRID", initGrid);
 		sumOfTreasures--;
 		if(sumOfTreasures==0){
 			trasuresExist=false;
 		}
 		}
 		
 		return connectReturn;
 		}else{
 			return null;
 		}
 		
 	}
 
 	@Override
 	public HashMap<String, Object> connectToServer(String clientKey) throws RemoteException {
 		if(CONNECT_FLAG){
 		HashMap<String,Integer> playerInfo=new HashMap<String,Integer>();
 		playerInfo.put("PLAYER_ID", UNIQUE_ID);
 		playerInfo.put("XCORD", XCORD);
 		playerInfo.put("YCORD", YCORD);
 		playerInfo.put("TREASURES_COLLECTED", 0);
 		updateGlobalInfo(clientKey,playerInfo);
 		
 		//Start the thread to check for client heart beats every 10 seconds
 		Thread th=new Thread(new CheckForAndUpdateFailures());
 		th.start();
 		
 		
 		try {
 			Thread.sleep(20000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		CONNECT_FLAG=false;
 		return connectReturn;
 		}else{
 			return null;
 		}
 		
 	}
 	
 	private void updateGlobalInfo(String playerID,HashMap<String,Integer> playerInfo){
 		XCORD=(XCORD+7)%10;
 		YCORD=(YCORD+13)%10;
 		UNIQUE_ID++;
		NUMBER_OF_PLAYERS.set(NUMBER_OF_PLAYERS.decrementAndGet());
 		connectReturn.put(playerID, playerInfo);
 		connectReturn.put("NO_OF_PLAYERS", NUMBER_OF_PLAYERS);
 		connectReturn.put("GRID", initGrid);
 		connectReturn.put("TREASURE_SUM",sumOfTreasures);
 		
 	}
 
 	@Override
 	public synchronized void heartBeat(String clientKey, Notify notify) throws RemoteException {
 		
 		long newTimeStamp=Calendar.getInstance().getTimeInMillis();
 		
 		if(!clientListLocal.keySet().contains(clientKey)){
 			
 			clientListLocal.put(clientKey, newTimeStamp);
 		}
 					
 		if(clientListLocal.get(clientKey)==-1L){
 			notify.onFailure(clientKey);
 		}else{
 			clientListLocal.put(clientKey, newTimeStamp);
 			notify.onSuccess();
 		}
 		
 	}
 	
 	
 	private class CheckForAndUpdateFailures extends Thread{
 		
 		private static final long UPDATE_INTERVAL = 12000;
 		private  long currentTimeStamp;
 		@Override
 		public void run(){
 			
 			
 			 while(true){
 					try {
 						currentTimeStamp=Calendar.getInstance().getTimeInMillis();		
 			for (Entry<String, Long> entry : clientListLocal.entrySet()) {
 				
 				if(entry.getValue()==-1)
 					break;
 				
 				if((currentTimeStamp-entry.getValue()) > UPDATE_INTERVAL){
 					entry.setValue(-1L);
 					//update the number of players in the game
 					NUMBER_OF_PLAYERS.set(NUMBER_OF_PLAYERS.decrementAndGet());
 					connectReturn.put("NO_OF_PLAYERS", NUMBER_OF_PLAYERS);
 					//Remove from the global list
 					connectReturn.remove(entry.getKey());
 					
 				}
 			}
 				Thread.sleep(10000);
 				//update
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	  }
 	}
 
 
 }
