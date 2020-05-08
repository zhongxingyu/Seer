 package de.minestar.cok.game;
 
 import java.util.LinkedList;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.ChunkCoordinates;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import de.minestar.cok.hook.ServerTickHandler;
 import de.minestar.cok.profession.Profession;
 
 public class Team {
 	
 	private String name;
 	private char color;
 	
 	//Special Classes for this team
 	private String captain;
 	private String crossbowman;
 	private String barbarian;
 	
 	private ChunkCoordinates spawnCoordinates;
 	
 	private LinkedList<String> players;
 	
 	private LinkedList<String> onlinePlayers;
 
 	
 	public LinkedList<String> getAllPlayers(){
 		return players;
 	}
 	
 	public Team(String name, char color) {
 		this.setName(name);
 		this.setColor(color);
 		this.captain = "";
 		this.crossbowman = "";
 		this.barbarian = "";
 		this.setSpawnCoordinates(null);
 		this.players = new LinkedList<String>();
 		this.onlinePlayers = new LinkedList<String>();
 	}
 	
 	
 	public int getColorAsInt(){
 		return color >= 97 ? color - 87 : color - 48;
 	}
 	
 	public boolean addPlayer(String name){
 		boolean res = players.contains(name);
 		if(!res){
 			players.add(name);
 			if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
 				EntityPlayer playerEntity = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
 				if(playerEntity != null){
 					playerReturned(playerEntity.username);
 					if(this.spawnCoordinates != null){
 						playerEntity.setSpawnChunk(spawnCoordinates, true);
 					}
 				}
 			}
 		}	
 		return !res;
 	}
 	
 	public boolean removePlayer(String name){
 		boolean res = players.contains(name);
 		if(res){
 			players.remove(name);
 			playerGone(name);
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
 
 	public ChunkCoordinates getSpawnCoordinates() {
 		return spawnCoordinates;
 	}
 
 	/**
 	 * sets the spawnCoordinates for this team and it's members
 	 * @param spawnCoordinates
 	 */
 	public void setSpawnCoordinates(ChunkCoordinates spawnCoordinates) {
 		if(spawnCoordinates != null && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
 			for(String player : players){
 				EntityPlayer playerEntity = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
 				if(playerEntity != null){
 					playerEntity.setSpawnChunk(spawnCoordinates, true);
 				}
 			}
 		}
 		this.spawnCoordinates = spawnCoordinates;
 	}
 	
 	public void resetProfessions(){
 		if(!captain.equals("")){
 			CoKGame.playerProfessions.remove(captain);
 			onlinePlayers.add(captain);
 			captain = "";
 		}
 		if(!barbarian.equals("")){
 			CoKGame.playerProfessions.remove(barbarian);
 			onlinePlayers.add(barbarian);
 			barbarian = "";
 		}
 		if(!crossbowman.equals("")){
 			CoKGame.playerProfessions.remove(crossbowman);
 			onlinePlayers.add(crossbowman);
 			crossbowman = "";
 		}
 		
 	}
 	
 	/**
 	 * Distribute the Professions to the online players.
 	 */
 	public void distributeProfessions(){
 		String playerName;
 		if(captain.equals("") && onlinePlayers.size() > 0){
 			playerName = onlinePlayers.removeFirst();
 			CoKGame.playerProfessions.put(playerName, Profession.KING);
 			captain = playerName;
 			if(!ServerTickHandler.changedProfessions.contains(playerName)){
 				ServerTickHandler.changedProfessions.add(playerName);
 			}
 		}
 		if(crossbowman.equals("") && onlinePlayers.size() > 0){
 			playerName = onlinePlayers.removeFirst();
 			CoKGame.playerProfessions.put(playerName, Profession.CROSSBOWMAN);
 			crossbowman = playerName;
 			if(!ServerTickHandler.changedProfessions.contains(playerName)){
 				ServerTickHandler.changedProfessions.add(playerName);
 			}
 		}
 		if(barbarian.equals("") && onlinePlayers.size() > 0){
 			playerName = onlinePlayers.removeFirst();
 			CoKGame.playerProfessions.put(playerName, Profession.BARBARIAN);
 			barbarian = playerName;
 			if(!ServerTickHandler.changedProfessions.contains(playerName)){
 				ServerTickHandler.changedProfessions.add(playerName);
 			}
 		}
 	}
 	
 	/**
 	 * a player died or logged off -> redistribute professions
 	 * @param player
 	 */
 	public void playerGone(String player){
 		onlinePlayers.remove(player);
		ServerTickHandler.changedProfessions.remove(player);
 		
 		CoKGame.playerProfessions.remove(player);
 		if(captain.equalsIgnoreCase(player)){
 			captain = "";
 		}
 		if(crossbowman.equalsIgnoreCase(player)){
 			crossbowman = "";
 		}
 		if(barbarian.equalsIgnoreCase(player)){
 			barbarian = "";
 		}
 		distributeProfessions();
 	}
 	
 	/**
 	 * player got back online/respawned
 	 * @param player
 	 */
 	public void playerReturned(String player){
 		if(!onlinePlayers.contains(player) && !captain.equalsIgnoreCase(player)
 				&& !crossbowman.equalsIgnoreCase(player) && !barbarian.equalsIgnoreCase(player)){
 			onlinePlayers.add(player);
 		}
 		distributeProfessions();
 	}
 	
 }
