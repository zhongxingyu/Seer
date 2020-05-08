 package linewars.gameLogic;
 
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import linewars.configfilehandler.ConfigFileReader.InvalidConfigFileException;
 import linewars.network.Client;
 import linewars.network.MessageProvider;
 import linewars.network.messages.Message;
 
 public class TimingManager implements Runnable{
 	public static final int TIME_PER_TICK_MILLIS = 100;
 	public static final double GAME_TIME_PER_TICK_S = TIME_PER_TICK_MILLIS / 1000.0;
 	
 	private GameStateUpdater manager;
 	private MessageProvider network;
 
 	private int nextTickID;
 	private double gameSpeed;
 	private long lastUpdateTime;
 	
 	public TimingManager(String mapURI, int numPlayers, List<String> raceURIs, List<String> players) throws FileNotFoundException, InvalidConfigFileException{
 		manager = new LogicBlockingManager(mapURI, numPlayers, raceURIs, players);
 		gameSpeed = 1;
 		nextTickID = 1;
 	}
 	
 	public void setClientReference(MessageProvider n){
 		network = n;
 	}
 	
 	public GameStateProvider getGameStateManager(){
 		return (GameStateProvider) manager;//TODO safety
 	}
 
 	@Override
 	public void run() {
 		//TODO any startup code here?
 		lastUpdateTime = System.currentTimeMillis();
 		while(true){//TODO some exit condition?
 			//get orders from network
 			Message[] messagesForTick = network.getMessagesForTick(nextTickID);
 			
 			if(messagesForTick.length > 0)
 			{
 				int i = 0;
 				i = i + 1;
 			}
 			
 			//TODO process them as needed - game speed change orders in particular!
 			//give orders to manager
 			manager.addOrdersForTick(nextTickID, messagesForTick);
 			//update tick id
 			++nextTickID;
 			//compute time to sleep for
 			long nextUpdateTime = lastUpdateTime + TIME_PER_TICK_MILLIS;
 			long timeToSleep = nextUpdateTime - System.currentTimeMillis();
 			//TODO update game speed if necessary?
 			//sleepif(timeToSleep > 0)
 			if(timeToSleep > 0)
 			{
 				try {
 					Thread.sleep(timeToSleep);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		// TODO exit code?
 	}
 	
 }
