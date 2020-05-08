 package com.rojel.parkourpvp.data;
 
 import java.util.ArrayList;
import java.util.Iterator;
 
 import org.bukkit.Location;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.rojel.parkourpvp.ParkourPVP;
 import com.rojel.pluginsignapi.PluginSignAPI;
 
 public class Room {
 	public static final int MAX_PLAYERS = 4;
 	public static final int MIN_PLAYERS = 2;
 	
 	private String name;
 	private Location lobby;
 	private ArrayList<Location> spawns;
 	private Location goal;
 	private ArrayList<PlayerData> players;
 	private RoomState state;
 	private int waitingCounter;
 	private int gameCounter;
 	private int spawnIndex;
 	
 	public Room(String name) {
 		this.name = name;
 		this.players = new ArrayList<PlayerData>();
 		this.spawns = new ArrayList<Location>();
 		this.state = RoomState.WAITING;
 		this.waitingCounter = 20;
 		this.gameCounter = 300;
 		
 		ParkourPVP.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(ParkourPVP.getPlugin(), new BukkitRunnable() {
 			@Override
 			public void run() {
 				if(state == RoomState.WAITING && getPlayerCount() >= MIN_PLAYERS) {
 					waitingCounter--;
 					
 					updateState();
 				} else if(state == RoomState.RUNNING) {
 					gameCounter--;
 					if(gameCounter % 60 == 0)
 						sendMessage((gameCounter / 60) + " 3min left.");
 					
 					updateState();
 				}
 			}
 		}, 0, 20);
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public Location getLobby() {
 		return lobby;
 	}
 	
 	public void setLobby(Location loc) {
 		this.lobby = loc;
 	}
 	
 	public Location getSpawn() {
 		Location spawn = spawns.get(spawnIndex);
 		spawnIndex++;
 		if(spawnIndex >= spawns.size())
 			spawnIndex = 0;
 		
 		return spawn;
 	}
 	
 	public void addSpawn(Location loc) {
 		this.spawns.add(loc);
 	}
 	
 	public int getSpawnCount() {
 		return spawns.size();
 	}
 	
 	public Location getGoal() {
 		return goal;
 	}
 	
 	public void setGoal(Location loc) {
 		this.goal = loc;
 	}
 	
 	public int getPlayerCount() {
 		return players.size();
 	}
 	
 	public int getWaitingCounter() {
 		return waitingCounter;
 	}
 	
 	public int getGameCounter() {
 		return gameCounter;
 	}
 	
 	public boolean isInRoom(PlayerData player) {
 		for(PlayerData data : players)
 			if(data == player)
 				return true;
 		
 		return false;
 	}
 	
 	public void joinRoom(PlayerData player) {
 		sendMessage(player.getPlayer().getDisplayName() + " 3joined the room.");
 		
 		players.add(player);
 		player.joinRoom(this);
 		
 		updateState();
 	}
 	
 	public void leaveRoom(PlayerData player) {		
 		players.remove(player);
 		player.leaveRoom();
 		
 		sendMessage(player.getPlayer().getDisplayName() + " 3left the room.");
 		
 		updateState();
 	}
 	
 	public RoomState getState() {
 		return state;
 	}
 	
 	private void updateState() {
 		if(state == RoomState.WAITING && getPlayerCount() < MIN_PLAYERS) {
 			waitingCounter = 20;
 		} else if(state == RoomState.WAITING && waitingCounter <= 0) {
 			state = RoomState.RUNNING;
 			waitingCounter = 20;
 			
 			for(PlayerData player : players) {
 				player.getPlayer().teleport(getSpawn());
 			}
 			
 			sendMessage("3The game has started. Try to reach the goal, but don't let the others knock you off.");
 		} else if(state == RoomState.RUNNING && (getWinner() != null || getPlayerCount() <= 1)) {
 			PlayerData winner;
 			
 			if(getWinner() != null)
 				winner = getWinner();
 			else
 				winner = players.get(0);
 			
 			sendMessage(winner.getPlayer().getDisplayName() + " 3won the game.");
 			
 			state = RoomState.ENDING;
 			
 			ParkourPVP.getPlugin().getServer().getScheduler().runTaskLater(ParkourPVP.getPlugin(), new BukkitRunnable() {
 				@Override
 				public void run() {
 					state = RoomState.WAITING;
					Iterator<PlayerData> iterator = players.iterator();
					
					while(iterator.hasNext()) {
						PlayerData player = iterator.next();
						iterator.remove();
 						player.leaveRoom();
 					}
 					
 					updateState();
 				}
 			}, 100);
 		} else if(state == RoomState.RUNNING && getGameCounter() <= 0) {
 			gameCounter = 300;
 			state = RoomState.WAITING;
 			
 			sendMessage("3Time has run out and no one won. Teleporting everyone back to the lobby.");
 			
			Iterator<PlayerData> iterator = players.iterator();
			
			while(iterator.hasNext()) {
				PlayerData player = iterator.next();
				iterator.remove();
 				player.leaveRoom();
 			}
 		}
 		
 		PluginSignAPI.updateSigns();
 	}
 	
 	public void playerScores(PlayerData player) {
 		player.addPoint();
 		sendMessage(player.getPlayer().getDisplayName() + " 3scored a point.");
 		
 		for(PlayerData data : players) {
 			data.getPlayer().teleport(getSpawn());
 		}
 		
 		updateState();
 	}
 	
 	public PlayerData getWinner() {
 		for(PlayerData player : players) {
 			if(player.getPoints() >= 2)
 				return player;
 		}
 		
 		return null;
 	}
 	
 	public void sendMessage(String msg) {
 		for(PlayerData player : players)
 			player.getPlayer().sendMessage(msg);
 	}
 	
 	public boolean isJoinable() {
 		if(lobby == null)
 			return false;
 		if(spawns.size() == 0)
 			return false;
 		if(goal == null)
 			return false;
 		if(getPlayerCount() == MAX_PLAYERS)
 			return false;
 		if(state != RoomState.WAITING)
 			return false;
 		
 		return true;
 	}
 }
