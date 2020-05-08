 package com.stormdev.minigamez.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.stormdev.minigamez.main.minigamez;
 
 public class Minigame {
	public String gamename;
 	private ArenaType gameType = ArenaType.INAVLID;
 	private List<String> players = new ArrayList<String>();
 	private List<String> inplayers = new ArrayList<String>();
 	private List<String> blue = new ArrayList<String>();
 	private List<String> red = new ArrayList<String>();
 	private Map<String, ItemStack[]> oldInventories = new HashMap<String,ItemStack[]>();
 	public Map<String, Integer> lapsLeft = new HashMap<String, Integer>();
 	public Map<String, Integer> ucarsCooldown = new HashMap<String, Integer>();
 	private String gameId = "";
 	private Arena arena = null;
 	private String arenaName = "";
 	private String winner = "Unknown";
 	public Boolean running = false;
 	private BukkitTask task = null;
 	private Scoreboard teams = null;
 	private BukkitTask countDown = null;
 	public Map<String,Option> gameOptions = new HashMap<String, Option>();
 	private int count = 50;
 	public HashMap<String, Integer> lives = new HashMap<String, Integer>();
	public Minigame(Arena arena, String arenaName, String gamename, Map<String,Option> gameOptions){
 		this.gameId = UniqueString.generate();
 		this.arena = arena;
 		this.gameType = arena.getType();
 		this.arenaName = arenaName;
		this.gamename = gamename;
 		this.gameOptions = gameOptions;
 		this.teams = minigamez.plugin.getServer().getScoreboardManager().getMainScoreboard();
 	}
 	public void joinBlue(String name){
 		this.blue.add(name);
 		return;
 	}
 	public void setOldInventories(Map<String, ItemStack[]> inventories){
 		this.oldInventories = inventories;
 		return;
 	}
 	public Map<String, ItemStack[]> getOldInventories(){
 		return this.oldInventories;
 	}
 	public void joinRed(String name){
 		this.red.add(name);
 		return;
 	}
 	public Scoreboard getTeams(){
 		return this.teams;
 	}
 	public void setTeamScoreboard(Scoreboard board){
 		this.teams = board;
 		return;
 	}
 	public void setCount(int count){
 		this.count = count;
 		return;
 	}
 	public int getCount(){
 		return this.count;
 	}
 	public void setBlue(List<String> blue){
 		this.blue = blue;
 		return;
 	}
 	public void setRed(List<String> red){
 		this.red = red;
 		return;
 	}
     public List<String> getBlue(){
     	return this.blue;
     }
     public List<String> getRed(){
     	return this.red;
     }
     public List<String> getInPlayers(){
     	return this.inplayers;
     }
     public void setInPlayers(List<String> in){
     	this.inplayers = in;
     	return;
     }
     public void playerOut(String name){
     	if(this.blue.contains(name)){
     		this.blue.remove(name);
     	}
     	if(this.red.contains(name)){
     		this.red.remove(name);
     	}
     	if(this.inplayers.contains(name)){
     	this.inplayers.remove(name);
     	}
     }
 	public Boolean join(String playername){
 		if(players.size() < this.arena.getPlayerLimit()){
 			players.add(playername);
 			return true;
 		}
 		return false;
 	}
 	public void startCountDown(){
 		Arena gameArena = this.getArena();
 		//if option for countdown in game:
 		/*
 		this.setCount();
 		final Minigame game = this;
 		if(gameArena.getDoCountdown()){
 		*/
 			//end
 		final Minigame game = this;
 		Boolean option = false;
 			if(option){
 			this.countDown = minigamez.plugin.getServer().getScheduler().runTaskTimer(minigamez.plugin, new Runnable(){
 				@Override
 				public void run() {
 					for(String player:game.getPlayers()){
 						minigamez.plugin.getServer().getPlayer(player).sendMessage(ChatColor.BLUE+"Time remaining: "+game.getCount());
 					}
 					game.setCount((game.getCount() -1));
 					minigamez.plugin.gameScheduler.updateGame(game);
 					if(game.getCount() < 0){
 						for(String player:game.getPlayers()){
 							minigamez.plugin.getServer().getPlayer(player).sendMessage(ChatColor.BLUE+"Time remaining: "+"End!");
 						}
 						//end the game
 						game.setWinner("The players");
 						game.end();
 					}
 				}}, 20l, 20l);
 		}
 		return;
 	}
 	public void leave(String playername){
 		
 		this.getPlayers().remove(playername);
 		if(this.getRed().contains(playername)){
 			this.getRed().remove(playername);
 		}
 		if(this.getBlue().contains(playername)){
 			this.getBlue().remove(playername);
 		}
 		if(this.teams.getTeam("blue"+this.gameId).getPlayers().contains(minigamez.plugin.getServer().getOfflinePlayer(playername))){
 			this.teams.getTeam("blue"+this.gameId).removePlayer(minigamez.plugin.getServer().getOfflinePlayer(playername));
 		}
 		if(this.teams.getTeam("red"+this.gameId).getPlayers().contains(minigamez.plugin.getServer().getOfflinePlayer(playername))){
 			this.teams.getTeam("red"+this.gameId).removePlayer(minigamez.plugin.getServer().getOfflinePlayer(playername));
 		}
 		this.playerOut(playername);
 		Player player = minigamez.plugin.getServer().getPlayer(playername);
 		if(player != null){
 			player.getInventory().clear();
 		}
 		if(this.getOldInventories().containsKey(playername)){
 			if(player != null){
 		player.getInventory().setContents(this.getOldInventories().get(playername));
 			}
 		this.getOldInventories().remove(playername);
 		}
 		if(player != null){
 			player.setGameMode(GameMode.SURVIVAL);
 			player.teleport(player.getWorld().getSpawnLocation());
 			player.sendMessage(ChatColor.GOLD+"Successfully left the minigame!");
 			}
 		for(String playerName:this.getPlayers()){
 			if(minigamez.plugin.getServer().getPlayer(playerName) != null && minigamez.plugin.getServer().getPlayer(playerName).isOnline()){
 				Player p=minigamez.plugin.getServer().getPlayer(playerName);
 				p.sendMessage(ChatColor.GOLD+playername+" left the minigame!");
 			}
 		}
 		return;
 	}
 	public Boolean isEmpty(){
 		if(this.players.size() < 1){
 			return true;
 		}
 		return false;
 	}
 	public String getGameId(){
 		return this.gameId;
 	}
 	public String getArenaName(){
 		return this.arenaName;
 	}
 	public Arena getArena(){
 		return this.arena;
 	}
     public ArenaType getGameType(){
     	return this.gameType;
     }
     public List<String> getPlayers(){
     	return this.players;
     }
     public void setWinner(String winner){
     	this.winner = winner;
     	return;
     }
     public String getWinner(){
     	return this.winner;
     }
     public Boolean getRunning(){
     	return this.running;
     }
     public void start(){
     	this.running = true;
     	final Minigame game = this;
     	this.task = minigamez.plugin.getServer().getScheduler().runTaskTimer(minigamez.plugin, new Runnable(){
 
 			@Override
 			public void run() {
 				MinigameUpdateEvent event = new MinigameUpdateEvent(game);
 				minigamez.plugin.getServer().getPluginManager().callEvent(event);
 				return;
 			}}, 30l, 30l);
     	minigamez.plugin.getServer().getPluginManager().callEvent(new MinigameStartEvent(this));
     }
     public void end(){
     	this.running = false;
     	if(task != null){
     		task.cancel();
     	}
     	try {
 			Set<OfflinePlayer> bluePlayers = this.teams.getTeam("blue").getPlayers();
 			if(!(bluePlayers == null)){
 				for(OfflinePlayer pl:bluePlayers){
 					this.teams.getTeam("blue").removePlayer(pl);
 				}	
 			}
 		} catch (Exception e) {
 		}
     	try {
 			Set<OfflinePlayer> redPlayers = this.teams.getTeam("red").getPlayers();
 			if(!(redPlayers == null)){
 				for(OfflinePlayer pl:redPlayers){
 					this.teams.getTeam("red").removePlayer(pl);
 				}	
 			}
 		} catch (Exception e) {
 		}
     	this.teams.getTeam("blue"+this.gameId).unregister();
     	this.teams.getTeam("red"+this.gameId).unregister();
     	
     	if(this.countDown != null){
     		this.countDown.cancel();
     	}
     	minigamez.plugin.getServer().getPluginManager().callEvent(new MinigameFinishEvent(this));
     }
 }
