 package linewars.test;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 import linewars.gamestate.MapConfiguration;
 import linewars.gamestate.NodeConfiguration;
 import linewars.gamestate.Race;
 import linewars.gamestate.mapItems.BuildingDefinition;
 import linewars.init.Game;
 import linewars.init.UserData;
 
 public class SingleUserStart 
 {
 	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
 	{
 		//get the map
 		MapConfiguration testMap = (MapConfiguration)(new ObjectInputStream(new FileInputStream(new File("resources/maps/testMap.cfg"))).readObject());
		for(NodeConfiguration n : testMap.nodes())
			n.setStartNode(true);
 		
 		//get the race
 		Race testRace = (Race)(new ObjectInputStream(new FileInputStream(new File("resources/races/testRace.cfg"))).readObject());
 //		Race testRace = createRace();
 		
 		//construct the player
 		UserData testPlayer = new UserData();
 		testPlayer.setColor(Color.blue);
 		testPlayer.setName("TEST");
 		testPlayer.setRace(testRace);
 		testPlayer.setStartingSlot(1);
 		testPlayer.setIpAddress("127.0.0.1");
 		testPlayer.setObserver(false);
 		
 		//create the list of players
		ArrayList<UserData> playerList = new ArrayList<UserData>();
 		playerList.add(testPlayer);
 		
 		//create the list of clients
 		ArrayList<String> clientList = new ArrayList<String>();
 		clientList.add("127.0.0.1");
 		
 		//construct the game
//		Game testGame = new Game(testMap, playerList);
//		testGame.initializeServer(clientList);
//		testGame.initializeClient("127.0.0.1", 0);
//		testGame.run();
 	}
 	
 //	private static Race createRace()
 //	{
 //		BuildingDefinition b = new BuildingDefinition();
 //		b.se
 //		Race r = new Race();
 //		r.addBuilding(b, true);
 //		
 //		return r;
 //	}
 }
