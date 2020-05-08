 package com.earlofmarch.reach.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 public class PlayerIO {
 	
 	private static final String ROOT = "Reach Scores";
 	private static final String SUFFIX = ".txt";
 	
 	/*
 	 *  Reach Scores
 	 *  	<name>.txt
 	 *  
 	 *  name.txt contents:
 	 *  <score 1>
 	 *  <score 2>
 	 *  etc.	
 	 */
 	
 	/**
 	 * Returns a LinkedList with all players saved in the "Reach Scores", 
 	 * returns null if none exist.
 	 * @return LinkedList of Players
 	 * @param fullHistory Adds the player's score history
 	 */
 	public static LinkedList<Player> getAllPlayers(boolean fullHistory){
 		
 		File root = new File(ROOT);
 		LinkedList<Player> players = new LinkedList<Player>();
 		
 		//Check if the folder exists
 		if(root.isDirectory()){
 			
 			File[] playerFiles = root.listFiles();
 			//Check if the folder contains any files
 			if(playerFiles == null || playerFiles.length == 0){
 				System.err.println("No player files exist!");
 				return null;
 			}
 			
 			clean();
 			
 			for(File pfile :  playerFiles){
 				if(fullHistory)
 					players.add(getPlayerFull(pfile.getName().substring(0,pfile.getName().length()-SUFFIX.length())));
 				else
 					players.add(new Player(pfile.getName().substring(0,pfile.getName().length()-SUFFIX.length())));
 			}	
 			
 			return players.size()>0?players:null;
 			
 		}else{
 			System.err.println("Folder not found! Creating one now...");
 			root.mkdir();
 			return null;
 		}
 	}
 	
 	/**
 	 * Reads a <player name>.txt file and adds all the scores inside to the player's score history.
 	 * @param name	The player's name
 	 * @return	Player	The player with the their full score history.
 	 */
 	public static Player getPlayerFull(String name){
 		Player player = new Player(name);
 		IO.openInputFile(ROOT+"\\"+name+SUFFIX);
 		try {
 			String score = IO.readLine();
 			while(score!=null){
 				player.addToHistory(Integer.parseInt(score));
 				score = IO.readLine();
 			}
 			IO.closeInputFile();
 		} catch (IOException e) {}
 		
 		return player;
 	}
 		
 	public static void updateAll(LinkedList<Player> players){
 		for(Player p : players)
 			updatePlayer(p);
 	}
 	
 	public static void updatePlayer(Player p){
 		if(!new File(ROOT).isDirectory())
 			return;
 		
 		File f = new File(ROOT+"\\"+p.getName()+SUFFIX);
 		
 		IO.createOutputFile(f.getPath(), f.isFile());
		if(p.getScore() != 0)
			IO.println(p.getScore()+"");
 		IO.closeOutputFile();
 		
 	}	
 	
 	public static void clean(){		
 		File root = new File(ROOT);
 		if(!root.isDirectory())
 			return;
 		
 		File[] playerFiles = root.listFiles();
 		if(playerFiles == null || playerFiles.length == 0)
 			return;
 		
 		for(File f : playerFiles)
 			if(!f.getName().endsWith(SUFFIX) || f.getName().equals(SUFFIX))
 				f.delete();
 	}
 }
