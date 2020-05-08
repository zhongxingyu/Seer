 package com.dat255.Wood.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.dat255.Wood.model.HighScore;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.Server;
 import com.esotericsoftware.minlog.Log;
 
 /**
  * Handles the highscore list and binds the port
  * @author Patrik Larsson
  *
  */
 public class GameServer {
 
 	//The kryonet server handling all connections
 	private Server server;
 	//All highscores
 	private ArrayList<HighScore> scoreList;
 
 	
 	//Initiate the server and starts it.
 	public GameServer() throws IOException{
 		scoreList = new ArrayList<>();
 		server = new Server();
 		registerPackets();
 		NetworkListener nl = new NetworkListener();
 		nl.init(this);
 		server.addListener(nl);
 		server.bind(1337);
 		server.start();
 	}
 
 	/**
 	 * Used to register all packets that is going to be sent to client
 	 */
 	private void registerPackets(){
 		Kryo kryo = server.getKryo();
 		kryo.register(HighScore.class);
 		kryo.register(ArrayList.class);
 	}
 	
 	/**
 	 * Returns the kryonet server
 	 * @return server
 	 */
 	public Server getServer(){
 		return server;
 	}
 
 	/**
 	 * Adding the player in the right position in the list
 	 * @param hs The highscore object that will be added to the list
 	 */
 	public void addPlayer(HighScore hs){
 		scoreList.add(hs);
 		Collections.sort(scoreList, new ScoreComparator());
 	}
 
 	/**
 	 * 	//NOT IMPLEMENTED YET!\\
 	 * Updates the score of an existing user
 	 * @param hs The highscore object tha is going to be updated
 	 */
 
 	public void updateScore(HighScore hs){
 				for(HighScore score: scoreList){
 					if(score.getName().equals(hs.getName())){
 						
 					}
 				}
 	}
 	
 	public ArrayList<HighScore> getScoreList() {
 
 		return scoreList;
 	}
 	
 	//main() for starting initialization of server
 	public static void main(String[] args){
 		try {
 			new GameServer();
 			Log.set(Log.LEVEL_DEBUG);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
