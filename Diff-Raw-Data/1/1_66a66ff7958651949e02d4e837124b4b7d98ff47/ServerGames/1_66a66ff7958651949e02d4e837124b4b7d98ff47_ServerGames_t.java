 package me.NerdsWBNerds.ServerGames;
 
 import static org.bukkit.ChatColor.GOLD;
 import static org.bukkit.ChatColor.GREEN;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import de.diddiz.LogBlock.QueryParams;
 import me.NerdsWBNerds.ServerGames.Objects.Chests;
 import me.NerdsWBNerds.ServerGames.Objects.Spectator;
 import me.NerdsWBNerds.ServerGames.Objects.Tribute;
 import me.NerdsWBNerds.ServerGames.Timers.Deathmatch;
 import me.NerdsWBNerds.ServerGames.Timers.Finished;
 import me.NerdsWBNerds.ServerGames.Timers.Game;
 import me.NerdsWBNerds.ServerGames.Timers.Lobby;
 import me.NerdsWBNerds.ServerGames.Timers.CurrentState;
 import me.NerdsWBNerds.ServerGames.Timers.Setup;
 import me.NerdsWBNerds.ServerGames.Timers.CurrentState.State;
 
 import org.bukkit.*;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.diddiz.LogBlock.Consumer;
 import de.diddiz.LogBlock.LogBlock;
 
 public class ServerGames extends JavaPlugin implements Listener{
 
     private Consumer lbconsumer = null;
 
 	public String path = "plugins/ServerGames";
 	
 	public SGListener Listener = new SGListener(this);
 	public static Server server;
 	public static Logger log;
 
 	public int max = 24;
 	public static State state = null;
 	public static CurrentState game = null;
 	public static ArrayList<Location> tubes = new ArrayList<Location>();
 	public static ArrayList<Tribute> tributes = new ArrayList<Tribute>();
 	public static ArrayList<Spectator> spectators = new ArrayList<Spectator>();
 	public static Location cornacopia = null, waiting = null;
 	
 	public void onEnable(){
 
         final PluginManager pm = getServer().getPluginManager();
         final Plugin plugin = pm.getPlugin("LogBlock");
         if (plugin != null)
             lbconsumer = ((LogBlock)plugin).getConsumer();
 
 
 		server = this.getServer();
 		log = this.getLogger();
 
 		server.getPluginManager().registerEvents(Listener, this);
 		
 		new File(path).mkdir();
 		load();
 		
 		this.resetPlayers();
 
 		tpAll(waiting);
 		
 		//cornacopia.getWorld().setAutoSave(false);
 	}
 	
 	public void onDisable(){
 		this.cancelTasks();
 		
 		save();
 	}
 
 	public void resetPlayers(){
 		tributes = new ArrayList<Tribute>();
 		for(Player p : server.getOnlinePlayers()){
 			tributes.add(new Tribute(p));
 		}
 	}
 	
 	public void startLobby(){
 		server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " Countdown started.");
 
 		for(Spectator s: this.spectators){
 			tributes.add(new Tribute(s.player));
 		}
 		
 		cancelTasks();
 		
 		load();
 		tpAll(waiting);
 		
 		for(Player p: server.getOnlinePlayers()){
 			showAllFor(p);
 		}
 		this.resetPlayers();
 		
 		state = State.LOBBY;
 		game = new Lobby(this);
 		
 		startTimer();
 	}
 	
 	public void startSetup(){
 		int i = 0;
 		
 		for(Player p : server.getOnlinePlayers()){
 			if(i >= ServerGames.tubes.size())
 				i = 0;
 			
 			Location to = ServerGames.tubes.get(i);
 			p.teleport(toCenter(to));
 			p.setSprinting(false);
 			p.setSneaking(false);
 			p.setPassenger(null);
 			this.clearItems(p);
 			p.setGameMode(GameMode.SURVIVAL);
 			p.setFireTicks(0);
			p.setCompassTarget(cornacopia);
 			
 			i++;
 		}
 
 		cornacopia.getWorld().getEntities().clear();
 		cornacopia.getWorld().setThundering(false);
 		cornacopia.getWorld().setTime(0);
 		cornacopia.getWorld().setWeatherDuration(0);
 		cornacopia.getWorld().setStorm(false);
 
 
         // ----- WORLD RESETTING -----
         Chests.resetChests();
         this.getServer().broadcastMessage(GOLD + "[SurvivalGames] " + GREEN + "MAP IS RESETTING!");
 
 
         LogBlock logblock = (LogBlock)getServer().getPluginManager().getPlugin("LogBlock");
         QueryParams params = new QueryParams(logblock);
 
         params.world = cornacopia.getWorld();
         params.silent = false;
 
         try {
             logblock.getCommandsHandler().new CommandRollback(this.getServer().getConsoleSender(), params, false);
         } catch(Exception e){}
 
         this.getServer().broadcastMessage(GOLD + "[SurvivalGames] " + GREEN + "Map has been reset!");
 
         // ----- WORLD RESETTING -----
 		
 		cancelTasks();
 		
 		state = State.SET_UP;
 		game = new Setup(this);
 		
 		startTimer();
 	}
 	
 	public void startGame(){
 		cancelTasks();
 		
 		state = State.IN_GAME;
 		
 		for(Player p : server.getOnlinePlayers()){
 			p.setHealth(20);
 			p.setFoodLevel(20);
 			this.clearItems(p);
 		}
 			
 		server.broadcastMessage(GOLD + "[ServerGames]" + GREEN + " Let the game begin!");
 
 		game = new Game(this);
 		
 		startTimer();
 	}
 	
 	public void startDeath(){
 		Location x = this.toCenter(ServerGames.tubes.get(ServerGames.tubes.size() / 2)), y = this.toCenter(ServerGames.tubes.get(0));
 		Player xx = ServerGames.tributes.get(0).player, yy = ServerGames.tributes.get(1).player;
 		xx.teleport(x);
 		yy.teleport(y);
 		Listener.tell(xx, GOLD + "[ServerGames] " + GREEN + "You have made it to the deathmatch.");
 		Listener.tell(yy, GOLD + "[ServerGames] " + GREEN + "You have made it to the deathmatch.");
 		
 		cancelTasks();
 		
 		state = State.DEATHMATCH;
 		game = new Deathmatch(this);
 		
 		startTimer();
 	}
 	
 	public void startFinished(){
 		cancelTasks();
 		
 		state = State.DONE;
 		game = new Finished(this);
 		
 		startTimer();
 	}
 	
 	public void stopAll(){
 		game = null;
 		state = null;
 		cancelTasks();
 	}
 	
 	public void startTimer(){
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, game, 20L, 20L);
 	}
 	
 	public Tribute getTribute(Player player){
 		for(Tribute t : tributes){
 			if(t.player == player)
 				return t;
 		}
 		
 		return null;
 	}
 
 	public Spectator getSpectator(Player player){
 		for(Spectator t : spectators){
 			if(t.player == player)
 				return t;
 		}
 		
 		return null;
 	}
 	
 	public void tpAll(Location l){
 		for(Player p:server.getOnlinePlayers()){
 			p.teleport(l);
 		}
 	}
 	
 	public void cancelTasks(){
 		this.getServer().getScheduler().cancelAllTasks();
 	}
 	
 	public static void showPlayer(Player player){
 		for(Player p : server.getOnlinePlayers()){
 			p.showPlayer(player);
 		}
 	}
 	
 	public static void hidePlayer(Player player){
 		for(Player p : server.getOnlinePlayers()){
 			p.hidePlayer(player);
 		}
 	}
 	
 	public static void hideAllFrom(Player player){
 		for(Player p : server.getOnlinePlayers()){
 			player.hidePlayer(p);
 		}
 	}
 	
 	public static void showAllFor(Player player){
 		for(Player p : server.getOnlinePlayers()){
 			player.showPlayer(p);
 		}
 	}
 	
 	public Location toCenter(Location l){
 		return new Location(l.getWorld(), l.getBlockX() + .5, l.getBlockY(), l.getBlockZ() + .5);
 	}
 	
 	public void removeTribute(Player p){
 		tributes.remove(this.getTribute(p));
 	}
 	
 	public void removeSpectator(Player p){
 		spectators.remove(getSpectator(p));
 	}
 	
 	public int toInt(String s){
 		return Integer.parseInt(s);
 	}
 
 	public void save(){
 		//////////// --------- Tubes ------------ ///////////////
 		File file = new File(path + File.separator + "Tubes.loc");
 		new File(path).mkdir();
 		if(!file.exists()){
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		 
 		ArrayList<String> t = new ArrayList<String>();
 		
 		for(Location x: tubes){
 			t.add(x.getWorld().getName() + "," + x.getBlockX() + "," + x.getBlockY() + "," + x.getBlockZ());
 		}
 		
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + File.separator + "Tubes.loc"));
 			oos.writeObject(t);
 			oos.flush();
 			oos.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		//////////// --------- Tubes End ------------ ///////////////
 		//////////// --------- Cornacopia ------------ ///////////////
 		
 		if(cornacopia != null){
 			file = new File(path + File.separator + "Corn.loc");
 			new File(path).mkdir();
 			if(!file.exists()){
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			 
 			String c = "";
 			c = (cornacopia.getWorld().getName() + "," + cornacopia.getBlockX() + "," + cornacopia.getBlockY() + "," + cornacopia.getBlockZ());
 			
 			cornacopia.getWorld().setSpawnLocation(cornacopia.getBlockX(), cornacopia.getBlockY(), cornacopia.getBlockZ());
 			
 			try{
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + File.separator + "Corn.loc"));
 				oos.writeObject(c);
 				oos.flush();
 				oos.close();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		//////////// --------- Cornacopia End ------------ ///////////////	
 		//////////// --------- Waiting ------------ ///////////////
 
 		if(waiting != null){
 			file = new File(path + File.separator + "Wait.loc");
 			new File(path).mkdir();
 			if(!file.exists()){
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 	
 			String w = "";
 			w = (waiting.getWorld().getName() + "," + waiting.getBlockX() + "," + waiting.getBlockY() + "," + waiting.getBlockZ());
 			
 			try{
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + File.separator + "Wait.loc"));
 				oos.writeObject(w);
 				oos.flush();
 				oos.close();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		
 		//////////// --------- Waiting End ------------ ///////////////
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void load(){
 		//////////// --------- Tubes ------------ ///////////////
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + File.separator + "Tubes.loc"));
 			Object result = ois.readObject();
 
 			ArrayList<String> t = new ArrayList<String>();
 			t = (ArrayList<String>)result;
 			
 			tubes = new ArrayList<Location>();
 			
 			for(String x: t){
 				String[] split = x.split(",");
 				tubes.add(new Location(server.getWorld(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3])));
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		//////////// --------- Tubes End ------------ ///////////////
 		//////////// --------- Cornacopia ------------ ///////////////
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + File.separator + "Corn.loc"));
 			Object result = ois.readObject();
 			
 			String[] split = ((String)result).split(",");
 			Location c = new Location(server.getWorld(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3]));
 			
 			ServerGames.cornacopia = c; 
 			cornacopia.getWorld().setSpawnLocation(cornacopia.getBlockX(), cornacopia.getBlockY(), cornacopia.getBlockZ());
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		//////////// --------- Cornacopia End ------------ ///////////////	
 		//////////// --------- Waiting ------------ ///////////////
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + File.separator + "Wait.loc"));
 			Object result = ois.readObject();
 
 			String[] split = ((String)result).split(",");
 			Location w = new Location(server.getWorld(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3]));
 			
 			ServerGames.waiting = w; 
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		//////////// --------- Waiting End ------------ ///////////////
 	}
 
 	public boolean inGame(){
 		if(state == State.IN_GAME)
 			return true;
 			
 		return false;
 	}
 	
 	public boolean inLobby(){
 		if(state == State.LOBBY)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean inSetup(){
 		if(state == State.SET_UP)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean inDone(){
 		if(state == State.DONE)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean inDeath(){
 		if(state == State.DEATHMATCH)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean inNothing(){
 		if(state == null)
 			return true;
 		
 		return false;
 	}
 	
 	public static boolean isOwner(Player player){
 		if(player.getName().equalsIgnoreCase("nerdswbnerds") || player.getName().equalsIgnoreCase("brenhein"))
 			return true;
 		
 		return false;
 	}
 	
 	public void clearItems(Player player){
 		player.getInventory().clear();
 		player.getInventory().setArmorContents(null);
 	}
 	
 	public boolean isTribute(Player player){
 		if(this.getTribute(player)!=null)
 			return true;
 		
 		return false;
 	}
 }
