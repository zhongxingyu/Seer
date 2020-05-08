 package de.minestar.cok.game;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import net.minecraft.block.Block;
 import net.minecraftforge.common.Configuration;
 import de.minestar.cok.helper.ChatSendHelper;
 import de.minestar.cok.references.Color;
 import de.minestar.cok.tileentity.TileEntitySocket;
 
 public class CoKGame {
 	
 	public static HashMap<String, Team> teams;
 	public static HashMap<Integer, HashSet<TileEntitySocket>> sockets;
 	public static HashSet<TileEntitySocket> unsortedSockets;
 	
 	public static boolean gameRunning = false;
 	
 	public static void initGame(Configuration config){
 		Settings.defaultbuildingBlockID = config.get(Configuration.CATEGORY_GENERAL, "Default building Block ID", Block.stone.blockID).getInt();
 		Settings.buildingHeight = config.get(Configuration.CATEGORY_GENERAL, "Building Height", 25).getInt();
 		Settings.protectedRadius = config.get(Configuration.CATEGORY_GENERAL, "Protected Radius", 3).getInt();
 		
 		if(config.hasChanged()){
 			config.save();
 		}
 		
 		teams = new HashMap<String, Team>();
 		sockets = new HashMap<Integer,HashSet<TileEntitySocket>>();
 		unsortedSockets = new HashSet<TileEntitySocket>();
 		gameRunning = false;
 	}
 	
 	/**
 	 * clean up the game on server stop
 	 */
 	public static void cleanUpGame(){
 		teams.clear();
 		sockets.clear();
 		unsortedSockets.clear();
 		gameRunning = false;
 	}
 	
 	/**
 	 * start a round of CoK
 	 */
 	public static void startGame(){
 		gameRunning = true;
 		ChatSendHelper.broadCastError("Let the clash of kingdoms begin!");
 	}
 	
 	/**
 	 * Stop the current round and declare the scores
 	 */
 	public static void stopGame(){
 		if(!gameRunning){
 			return;
 		}
 		gameRunning = false;
 		ChatSendHelper.broadCastError("The game has ended!");
 		ChatSendHelper.broadCastError("Results:");
 		for(Team team : teams.values()){
 			int maxScore = Settings.buildingHeight * (sockets.get(team.getColorAsInt()) == null ? 0 : sockets.get(team.getColorAsInt()).size());
 			ChatSendHelper.broadCastMessage(Color.getColorCodeFromChar(team.getColor())
 					+ team.getName() + Color.getColorCodeFromString("white") + ": "
 					+ getScoreForTeam(team) + "/" + maxScore);
 			//TODO finish call
 		}
 	}
 	
 	/**
 	 * get the score for the given teamName
 	 * @param name - name of the team
 	 */
 	public static int getScoreForTeam(Team team){
 		sortSockets();
 		HashSet<TileEntitySocket> teamSockets = sockets.get(team.getColorAsInt());
 		if(teamSockets == null){
 			return 0;
 		}
 		int res = 0;
 		for(TileEntitySocket socket : teamSockets){
 			res += socket.countBlocks();
 		}
 		return res;
 	}
 	
 	/**
 	 * register a socket block
 	 * @param coords
 	 */
 	public static boolean registerSocket(TileEntitySocket socket){
 		return unsortedSockets.add(socket);
 	}
 	
 	/**
 	 * sort unsorted sockets.
 	 * Needed this due to divergation in the call "socket.getBlockMetadata()"
 	 */
 	public static void sortSockets(){
 		if(sockets != null){
 			for(TileEntitySocket socket : unsortedSockets){
 				HashSet<TileEntitySocket> teamSockets = sockets.get(socket.getBlockMetadata());
 				if(teamSockets == null){
 					teamSockets = new HashSet<TileEntitySocket>();
 					sockets.put(socket.getBlockMetadata(), teamSockets);
 				}
 				teamSockets.add(socket);
 			}
 			unsortedSockets.clear();
 		}
 	}
 	
 	/**
 	 * unregister a socket block
 	 * @param coords
 	 * @return
 	 */
 	public static boolean removeSocket(TileEntitySocket socket){
 		sortSockets();
 		if(sockets != null){
 			HashSet<TileEntitySocket> teamSockets = sockets.get(socket.getBlockMetadata());
 			if(teamSockets == null){
 				return false;
 			}
 			return teamSockets.remove(socket);
 		}
 		return false;
 	}
 	
 	/**
 	 * Add a team
 	 * @param name of the team
 	 * @param color of the team
 	 * @return if the team has been successfully added
 	 */
 	public static boolean addTeam(String name, char color){
 		boolean res = teams.containsKey(name);
 		if(!res){
 			teams.put(name, new Team(name, color));
 		}
 		return !res;
 	}
 	
 	/**
 	 * Add a team
 	 * @param name
 	 * @param color
 	 * @param captain
 	 * @return if the team has been successfully added
 	 */
 	public static boolean addTeam(String name, char color, String captain){
 		boolean res = teams.containsKey(name);
 		if(!res){
 			Team team = new Team(name, color, captain);
 			team.addPlayer(name);
 			teams.put(name, team);
 		}
 		return !res;
 	}
 	
 	/**
 	 * remove a team with the given team
 	 * @param name
 	 * @return if the team could be removed (was present)
 	 */
 	public static boolean removeTeam(String name){
 		boolean res = teams.containsKey(name);
 		if(res){
 			teams.remove(name);
 		}
 		return res;
 	}
 	
 	/**
 	 * get the team with the specified name
 	 * @param name
 	 * @return team or null if there is no team with this name
 	 */
 	public static Team getTeam(String name){
 		return teams.get(name);
 	}
 	
 	/**
 	 * Set the captain of the specified team
 	 * @param teamName
 	 * @param captain
 	 * @return
 	 */
 	public static boolean setCaptainForTeam(String teamName, String captain){
 		boolean res = teams.containsKey(teamName);
 		if(res){
 			teams.get(teamName).setCaptain(captain);
 		}
 		return res;
 	}
 	
 	/**
 	 * Get the team of a specified player
 	 * @param playername
 	 * @return
 	 */
 	public static Team getTeamOfPlayer(String playername){
 		for(Team team: teams.values()){
 			if(team.hasPlayer(playername)){
 				return team;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Add player to the team if they aren't in another one already
 	 * @param teamname
 	 * @param playername
 	 * @return
 	 */
 	public static boolean addPlayerToTeam(String teamname, String playername){
 		boolean res = teams.containsKey(teamname) && getTeamOfPlayer(playername) == null;
 			if(res){
 				res = teams.get(teamname).addPlayer(playername);
 			}
 		return res;
 	}
 	
 	/**
 	 * remove player from specified team
 	 * @param teamname
 	 * @param playername
 	 * @return if the operation was successfull
 	 */
 	public static boolean removePlayerFromTeam(String teamname, String playername){
 		boolean res = teams.containsKey(teamname);
 		if(res){
 			res = teams.get(teamname).removePlayer(playername);
 		}
 		return res;
 	}
 	
 }
