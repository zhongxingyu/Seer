 package de.minestar.cok.game;
 
 import java.util.LinkedList;
 import java.util.Random;
 
 import net.minecraft.util.ChunkCoordinates;
 import de.minestar.cok.helper.PlayerHelper;
 
 public class Team {
 	
 	private String name;
 	private char color;
 	private String captain;
 	
 	public ChunkCoordinates spawnCoordinates;
 
 
 	private LinkedList<String> players = new LinkedList<String>();
 	
 	public LinkedList<String> getAllPlayers(){
 		return players;
 	}
 	
 	public Team(String name, char color) {
 		this.setName(name);
 		this.setColor(color);
 		this.captain = "";
 	}
 	
 	public Team(String name, char color, String captain){
 		this(name, color);
 		this.captain = captain;
 	}
 	
 	
 	/**
 	 * get any team member that is currently online, empty string if there is none
 	 * @param rand
 	 * @return
 	 */
 	public String getRandomOnlinePlayer(Random rand){
 		LinkedList<String> onlinePlayers = new LinkedList<String>();
 		for(String player : players){
 			if(PlayerHelper.isOnlineUser(player)){
 				onlinePlayers.add(player);
 			}
 		}
 		if(onlinePlayers.size() == 0){
 			return "";
 		} else{
 			return onlinePlayers.get(rand.nextInt(onlinePlayers.size()));
 		}
 	}
 	
 	/**
 	 * sets a random player from the team as the captain. The player must be online,
 	 * if there is none, the captain will be ""
 	 */
 	public void setRandomCaptain(){
 		captain = getRandomOnlinePlayer(CoKGame.rand);		
 	}
 	
 	public int getColorAsInt(){
 		return color >= 97 ? color - 87 : color - 48;
 	}
 	
 	public boolean addPlayer(String name){
 		boolean res = players.contains(name);
 		if(!res){
 			players.add(name);
			if(captain == ""){
 				captain = name;
 			}
 		}	
 		return !res;
 	}
 	
 	public boolean removePlayer(String name){
 		boolean res = players.contains(name);
 		if(res){
 			players.remove(name);
			if(name == captain){
 				setRandomCaptain();
 			}
 		}
 		return res;
 	}
 	
 	public boolean hasPlayer(String name){
 		return players.contains(name);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public char getColor() {
 		return color;
 	}
 
 	public void setColor(char color) {
 		if(color < 0){
 			this.color = 0;
 			return;
 		}
 		if(color > 'f'){
 			this.color = 'f';
 			return;
 		}
 		this.color = color;
 	}
 	
 	
 	public String getCaptain() {
 		return captain;
 	}
 
 	public void setCaptain(String captain) {
 		this.captain = captain;
 	}
 	
 }
