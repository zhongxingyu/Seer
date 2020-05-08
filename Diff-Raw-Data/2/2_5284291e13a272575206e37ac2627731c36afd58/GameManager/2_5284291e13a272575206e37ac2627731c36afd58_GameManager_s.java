 package rageteam.cookieslap.games;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import rageteam.cookieslap.main.CookieSlap;
 import rageteam.cookieslap.main.ScoreboardUtils;
 import rageteam.cookieslap.maps.Map;
 import rageteam.cookieslap.players.CookieSlapPlayer;
 import rageteam.cookieslap.players.UtilPlayer;
 
 public class GameManager {
 	
 	CookieSlap cookieslap;
 	UtilPlayer u;
 	
 	public GameManager(CookieSlap cookieslap){
 		this.cookieslap = cookieslap;
 	}
 	
 	public void startGame(Game game){
 		cookieslap.chat.log("New game commencing..");
 		
 		game.startGameTimer();
 		
 		Bukkit.getScheduler().cancelTask(game.counter);
 		
 		game.status = Status.INGAME;
 		game.time = 240;
 		game.setLobbyCount(cookieslap.getConfig().getInt("auto-start.time"));
 		
 		int c = 1;
 		
 		game.loadFloors();
 		
 		cookieslap.chat.bc("You are playing on &2 " + game.getMap().getName() + " &6.", game);
 		
 		Map map = game.getMap();
 		
 		ScoreboardUtils.get().hideScoreAll(game, "Starting in");
 		ScoreboardUtils.get().hideScoreAll(game, "Queue");
 		
 		for(CookieSlapPlayer cp : game.players.values()){
 			cp.getPlayer().setLevel(0);
 			
 			cp.getUtilPlayer().setAlive(true);
 			
 			if(c > map.getSpawnCount()){
 				c = 1;
 			}
 			
 			cp.getPlayer().teleport(map.getSpawn(c));
 			c++;
 			
 			cp.getPlayer().setLevel(0);
 			cp.getPlayer().setGameMode(GameMode.ADVENTURE);
 			
 			//give items
			for(int i = 0; i < 2; i++){
 				cp.getPlayer().getInventory().setItem(i, getCookie());
 			}
 		}
 		
 		game.getSign().update(map, false);
 		
 		cookieslap.chat.bc("Use your cookie to knock other players off of the map", game);
 	}
 	
 	public void stopGame(Game game, int r){
 		cookieslap.chat.log("Commencing Shutdown of " + game.getMap().getName() + ".");
 		
 		game.status = Status.ENDING;
 		
 		game.stopGameTimer();
 		
 		game.setLobbyCount(31);
 		game.time = 240;
 		game.setStatus(Status.LOBBY);
 		
 		game.resetArena();
 		game.data.clear();
 		game.floor.clear();
 		
 		game.setStarting(false);
 		
 		HashMap<String, CookieSlapPlayer> h = new HashMap<String, CookieSlapPlayer>(game.players);
 		game.players.clear();
 		
 		for(CookieSlapPlayer cp : h.values()){
 			game.leaveGame(cp.getUtilPlayer());
 		}
 		
 		if(r != 5){
 			cookieslap.chat.bc("CookieSlap has ended on the map " + game.getMap().getName() + ".");
 		}
 		
 		if(!cookieslap.disabling){
 			game.getSign().update(game.map, true);
 		}
 		
 		cookieslap.chat.log("Game has reset.");
 	}
 	
 	public String getDigitTIme(int count){
 		int minutes = count / 60;
 		int seconds = count % 60;
 		String disMinu = (minutes < 10 ? "0" : "") + minutes;
 		String disSec = (seconds < 10 ? "0" : "") + seconds;
 		String formattedTime = disMinu + ":" + disSec;
 		return formattedTime;
 	}
 	
 	public void inGameTime(int count, HashMap<String, CookieSlapPlayer> players){
 		for(CookieSlapPlayer cp : players.values()){
 			cookieslap.chat.sendMessage(cp.getPlayer(), "CookieSlap is ending in 51" + cookieslap.game.getDigitTIme(count));
 		}
 	}
 	
 	public ItemStack getCookie(){
 		ItemStack cookie = new ItemStack(Material.COOKIE);
 		cookie.addUnsafeEnchantment(Enchantment.KNOCKBACK, 25);
 		ItemMeta cookieMeta = cookie.getItemMeta();
 		cookieMeta.setDisplayName("The Magical Cookie");
 		cookieMeta.setLore(Arrays.asList(new String[] { "3Left-Click to knock players off the edge" }));
 		cookie.setItemMeta(cookieMeta);
 		return cookie;
 	}
 }
