 package bot;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Collections;
 
 import util.Logger;
 
 public class Bot {
 	
 	InputOutputUnit myParser;
 	int dungeonLevel;
 	Map map;
 	
 	public Bot(){
 		dungeonLevel = 0;
 		map = null;
 		myParser = new InputOutputUnit();
 	}
 	
 	public Bot(String hostname)
 			throws UnknownHostException, IOException{
 		this(hostname, Protocole.DEFAULT_PORT);
 	}
 
 	public Bot(String hostname, int port)
 			throws UnknownHostException, IOException{
 		this();
 		myParser = new InputOutputUnit(hostname, port);
 	}
 
 	public void treatInformation(Information i) {
 		switch (i.getVariable()){
 		case DUNGEON_LEVEL: dungeonLevel = (Integer)i.getValue(); break;
 		case MAP: map = (Map)i.getValue(); break;
 		}
 	}
 
 	public void start(){
 		try{
 			while(true){
				try{
					Logger.println("READING FROM SOCKET");
					nextTurn();
					Logger.println("DOING TURN");
					doTurn();
				}catch(UnknownPositionException e){
					System.out.println("The player location has not been found, Skipping turn");
				}
 			}
 
 		}catch(IOException e){
			String message = "Connection with the server has been lost";
			System.out.println(message);
 			return;
 		}
 	}
 	
 	public void doTurn(){
 		randomAction();		
 	}
 	
 	public void randomAction(){
 		double dice = Math.random();
 		if (dice > 0.7)
 			myParser.broadcastSearch();
 		else
 			randomMoveOrOpen();
 			
 	}
 	
 	public void randomMove(){
 		Direction[] myDirs = Direction.values();
 		Collections.shuffle(Arrays.asList(myDirs));
 		for (Direction d : myDirs){
 			if (map.isAllowedMove(d)){
 				myParser.broadcastMove(d);
 				return;
 			}
 		}
 	}
 	
 	public void randomMoveOrOpen(){
 		Direction[] myDirs = Direction.values();
 		Collections.shuffle(Arrays.asList(myDirs));
 		for (Direction d : myDirs){
 			if (map.isAllowedMove(d)){
 				myParser.broadcastMove(d);
 				return;
 			}
 			if (map.isAllowedOpen(d)){
 				myParser.broadcastOpeningDoor(d);
 				return;
 			}
 		}
 	}
 	
 	public void nextTurn() throws IOException, UnknownPositionException{
 		myParser.parseNextTurn(this);
 	}
 	
 	@Override
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append("dungeon_level : " + dungeonLevel + "\n");
 		if (map != null)
 			sb.append(map.toString() + "\n");
 		return sb.toString();
 	}
 }
