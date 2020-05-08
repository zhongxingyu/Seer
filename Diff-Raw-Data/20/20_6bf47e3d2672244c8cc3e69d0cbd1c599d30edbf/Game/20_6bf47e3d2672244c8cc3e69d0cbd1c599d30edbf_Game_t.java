 package rageteam.cookieslap.games;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import rageteam.cookieslap.lobby.LobbySign;
 import rageteam.cookieslap.main.CookieSlap;
import rageteam.cookieslap.main.ScoreboardUtils;
 import rageteam.cookieslap.maps.Map;
 import rageteam.cookieslap.players.CookieSlapPlayer;
 import rageteam.cookieslap.players.UtilPlayer;
 import rageteam.cookieslap.runnables.GameTime;
 import rageteam.cookieslap.runnables.LobbyCountdown;
 import rageteam.cookieslap.store.InvStore;
 
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class Game {
 	
 	CookieSlap cookieslap;
 	String name;
 	Map map;
 	Status status;
 	HashMap<String,CookieSlapPlayer> players;
 	HashSet<Location> floor;
 	ArrayList<Rollback> data;
 	private int lobbycount;
 	int time;
 	int y1;
 	int y2;
 	int small;
 	int counter = 0;
 	int timer = 0;
 	private boolean	starting;
 	private LobbySign sign;
 	
 	public Game(CookieSlap cookieslap, Map map) {
 		this.cookieslap = cookieslap;
 		this.map = map;
 		this.name = map.getName();
 		this.status = Status.LOBBY;
 		this.players = new HashMap<String, CookieSlapPlayer>();
 		this.floor = new HashSet<Location>();
 		this.data = new ArrayList<Rollback>();
 		time = 601;
 		setLobbyCount(31);
 		y1 = 0;
 		y2 = 0;
 		small = -1;
 		this.setSign(new LobbySign(map, cookieslap));
 		final Map m = map;
 		new BukkitRunnable() {
 			public void run(){ getSign().update(m, true); }
 		}.runTaskLater(cookieslap, 10L);
 		this.setStarting(false);
 	}
 	
 	public void startGameTimer() {
 		this.timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(cookieslap, new GameTime(cookieslap, this), 0L, 20L);
 	}
 	
 	public void stopGameTimer() {
 		Bukkit.getScheduler().cancelTask(timer);
 	}
 	
 	public int getCounterID() {
 		return this.counter;
 	}
 	
 	public HashSet<Location> getFloor(){
 		return this.floor;
 	}
 	
 	public ArrayList<Rollback> getDatas() {
 		return this.data;
 	}
 	
 	public HashMap<String, CookieSlapPlayer> getPlayers() {
 		return this.players;
 	}
 	
 	public ArrayList<CookieSlapPlayer> getSp(){
 		ArrayList<CookieSlapPlayer> cp = new ArrayList<CookieSlapPlayer>();
 		for(CookieSlapPlayer cpc : players.values()){
 			cp.add(cpc);
 		}
 		
 		return cp;
 	}
 	
 	public CookieSlapPlayer getPlayer(Player player){
 		return (CookieSlapPlayer)players.get(player.getName());
 	}
 	
 	public Status getStatus() {
 		return this.status;
 	}
 	
 	public void setStatus(Status status){
 		this.status = status;
 	}
 	
 	public void setMap(Map map){
 		this.map = map;
 	}
 	
 	public Map getMap(){
 		return this.map;
 	}
 	
 	public void joinGame(UtilPlayer u){
 		Player player = u.getPlayer();
 		if(u.getGame() != null){
 			cookieslap.chat.sendMessage(player, "You are already in a game");
 		} else {
 			if(players.containsKey(player.getName())){
 				cookieslap.chat.sendMessage(player, "You are already in this lobby");
 			} else {
 				if(status == Status.LOBBY){
 					int size = this.players.size();
 					int max = this.map.getSpawnCount();
 					if(max == 1){
 						CookieSlapPlayer cp = new CookieSlapPlayer(player, u);
 						
 						u.setAlive(true);
 						u.getStore().save();
 						cookieslap.utils.clearInventory(player);
 						
 						player.setHealth(20.0);
 						player.setFoodLevel(20);
 						player.setLevel(0);
 						player.setExp(0.0F);
 						player.setGameMode(GameMode.ADVENTURE);
 						
 						players.put(player.getName(), cp);
 						u.setGame(cookieslap.games.getGame(name));
 						
 						player.teleport(cookieslap.config.getLobby());
 						
 						cookieslap.chat.sendMessage(player, "You have been teleported to the cookieslap lobby");
 						
 						cookieslap.chat.sendMessage(player, "Players in your game:" + getPlayersIn());
 						
 						cookieslap.chat.sendMessage(player, "You have joined the lobby for map c" + map.getName() + "6.");
 						
						ScoreboardUtils.get().setScoreAll(this, "Queue", players.size());
						
 						if (this.players.size() >= cookieslap.getConfig().getInt("auto-start.players") && (!this.isStarting())){
 							startCountdown();
 							this.setStarting(true);
 						}
 					} else {
 						if(size >= max && !(player.hasPermission("cookieslap.joinfull"))){
 							cookieslap.chat.sendMessage(player, "cSorry, this game is efullc. Max players: " + max + ".");
 						} else {
 							if(size >= max) {
 								cookieslap.chat.sendMessage(player, "6You have joined a full game!");
 							}
 							
 							CookieSlapPlayer cp = new CookieSlapPlayer(player, u);
 							
 							u.setAlive(true);
 							u.getStore().save();
 							cookieslap.utils.clearInventory(player);
 							
 							player.setHealth(20.0);
 							player.setFoodLevel(20);
 							player.setLevel(0);
 							player.setExp(0.0F);
 							player.setGameMode(GameMode.ADVENTURE);
 							
 							players.put(player.getName(), cp);
 							u.setGame(cookieslap.games.getGame(name));
 							
 							player.teleport(cookieslap.config.getLobby());
 							
 
							cookieslap.chat.sendMessage(player, "You have been teleported to the cookieslap lobby. You will be teleported to the map on the game start.");
 
 							cookieslap.chat.sendMessage(player, "Players in your game: " + getPlayersIn());
							
							ScoreboardUtils.get().setScoreAll(this, "Queue", players.size());
 
 							cookieslap.chat.sendMessage(player, "You have joined the lobby for map c" + map.getName() + "6.");
 							cookieslap.chat.bcNotForPlayer(player, (cookieslap.special.contains(player.getName()) ? "4" : "a") + player.getName() + "&6 has joined the game. &e" + players.size() + "/" + max, this);
 
 							if (this.players.size() >= cookieslap.getConfig().getInt("auto-start.players") && (!this.isStarting())) {
 								startCountdown();
 								this.setStarting(true);
 							}
 						}
 					}
 					getSign().update(map, false);
 				} else if(status == Status.DISABLED) {
 					cookieslap.chat.sendMessage(player, "4Map is disabled");
 				} else {
 					cookieslap.chat.sendMessage(player, "5Game in progress");
 				}
 			}
 		}
 	}
 	
 	private String getPlayersIn() {
 		String p = "";
 		
 		for(CookieSlapPlayer cp : players.values()){
 			p = (cookieslap.special.contains(cp.getPlayer().getName()) ? "4" : "a") + cp.getPlayer().getName() + "6, " +p;  
 		}
 		
 		return p;
 	}
 	
 	private void startCountdown() {
 		Bukkit.getScheduler().cancelTask(counter);
 		
 		if(this.status == Status.LOBBY){
 			this.setLobbyCount(31);
 			for (CookieSlapPlayer cp : players.values()){
 				cp.getPlayer().setLevel(getLobbyCount());
 				cp.getScoreboard().setScore("Starting in", getLobbyCount());
 			}
 			
 			this.counter = Bukkit.getScheduler().scheduleSyncRepeatingTask(cookieslap, new LobbyCountdown(this, getLobbyCount()), 0L, 20L);
 		}
 	}
 	
 	public void leaveGame(UtilPlayer u){
 		players.remove(u.getName());
 		u.getPlayer().teleport(cookieslap.config.getLobby());
 		u.setGame(null);
 		u.setAlive(false);
 		u.getPlayer().setHealth(20.0);
 		InvStore store = u.getStore();
 		store.load();
 		store.reset();
 		u.getPlayer().setFallDistance(0.0F);
 		if(!cookieslap.disabling){
 			getSign().update(map, false);
 		}
 	}
 	
 	public int getLobbyCount(){
 		return lobbycount;
 	}
 	
 	public int getCount(){
 		return time;
 	}
 	
 	public int getLowestPossible(){
 		return this.small;
 	}
 	
 	public boolean loadFloors(){
 		this.floor.clear();
 		
 		if(map.getFloors() > 0){
 			for(int i = 1; i <= map.getFloors(); i++){
 				Selection sel = new CuboidSelection(map.getSpawn(1).getWorld(), map.getFloor(i, "1"), map.getFloor(i, "2"));
 				Location min = sel.getMinimumPoint();
 				Location max = sel.getMaximumPoint();
 				
 				int minX = min.getBlockX();
 				int minY = min.getBlockY();
 				this.y1 = minY;
 				int minZ = min.getBlockZ();
 				int maxX = max.getBlockX();
 				int maxY = max.getBlockY();
 				this.y2 = maxY;
 				int maxZ = max.getBlockZ();
 				
 				this.small = Math.min(y1, y2);
 				
 				for(int x = minX; x <= maxX; x++){
 					for(int y = minY; y <= maxY; y++){
 						for(int z = minZ; z <= maxZ; z++){
 							Location l = new Location(min.getWorld(), x, y, z);
 							floor.add(l);
 							Block block = l.getWorld().getBlockAt(x, y, z);
 							data.add(new Rollback(l.getWorld().getName(), block.getTypeId(), block.getData(), x, y, z));
 						}
 					}
 				}
 			} 
 		} else {
 			return false;
 		}
 		return true;
 	}
 	
 	public void resetArena(){
 		for(Rollback d : data){
 			Location l = new Location(Bukkit.getWorld(d.getWorld()), d.getX(), d.getY(), d.getZ());
 			l.getBlock().setTypeId(d.getPrevid());
 			l.getBlock().setData(d.getPrevdata());
 			l.getBlock().getState().update();
 		}
 	}
 	
 	public boolean isStarting(){
 		return starting;
 	}
 	
 	public void setStarting(boolean starting){
 		this.starting = starting;
 	}
 	
 	public LobbySign getSign(){
 		return sign;
 	}
 	
 	public void setSign(LobbySign sign){
 		this.sign = sign;
 	}
 	
 	public void setLobbyCount(int lobbycount){
 		this.lobbycount = lobbycount;
 	}
 }
