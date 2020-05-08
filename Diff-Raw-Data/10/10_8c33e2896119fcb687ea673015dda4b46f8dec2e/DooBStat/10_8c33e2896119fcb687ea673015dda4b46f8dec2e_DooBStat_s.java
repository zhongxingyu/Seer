 package net.doobler.doobstat;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.doobler.doobstat.commands.DooBStatDstatCommand;
 import net.doobler.doobstat.updatechecker.UpdateChecker;
 import net.doobler.doobstat.utils.CleanPlayersTask;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public final class DooBStat extends JavaPlugin {
 	
 	// obiekt połączenia z bazą.
 	public DooBStatDAO db = null;
 	// Lista obiektów obecnych na serwerze graczy.
 	public Map<String, DooBStatPlayerData> playerslist = new HashMap<String, DooBStatPlayerData>();
 	
 	public final DooBStatPlayerListener playerListener = new DooBStatPlayerListener(this);
 	
 	
 	/**
 	 * onEnable
 	 */
 	@Override
 	public void onEnable() {
 		
 		Date curdate = new Date();
 		Timestamp curtimestamp = new Timestamp(curdate.getTime());
 		
 		//zapisanie domyślnego konfigu na dysk
 		this.saveDefaultConfig();
 		
 		// nawiązanie połączenia z bazą
 		this.db = new DooBStatDAO(this,
 				 this.getConfig().getString("mysql.host"),
 				 this.getConfig().getString("mysql.port"),
 				 this.getConfig().getString("mysql.dbname"),
 				 this.getConfig().getString("mysql.user"),
 				 this.getConfig().getString("mysql.pass"),
 				 this.getConfig().getString("mysql.prefix"));
 
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		// rejestracja ewentów
 		pm.registerEvents(this.playerListener, this);
 		
 		// rejestracja komend
 		getCommand("dstat").setExecutor(new DooBStatDstatCommand(this));
 		
 		
 		// automatyczne czyszczenie graczy których dawno nie było
 		if(this.getConfig().getBoolean("clean.auto")) {
 			if(!CleanPlayersTask.is_working) {
 				// uruchomienie 10 minut po starcie serwera
 				new CleanPlayersTask(this).runTaskLater(this, (20*60*10));
 			}
 		}
 		
 		
 		// jeśli był zrobiony reload serwera to znaczy, że są jacyś gracze
 		// i trzeba ich dodać do pluginu...
 		PreparedStatement prest = this.db.getPreparedStatement("getPlayerByName");
 		ResultSet res = null;
 		PreparedStatement prest2 = this.db.getPreparedStatement("updatePlayerJoin");
 		for(Player all:getServer().getOnlinePlayers()) {
 
 			try {
 				prest.clearParameters();
 				prest.setString(1, all.getName());
 				res = prest.executeQuery();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
     		try {
     			// jeśli istnieje następny wiersz to dane zostały pobrane
 				if(res.next()) {
	    			prest2.setString(1, res.getString("this_login"));
	    			prest2.setTimestamp(2, curtimestamp);
	    			prest2.setInt(3, res.getInt("id"));
 	    			prest2.addBatch();
 	    			
 	    			// dodanie gracza do listy obecnych na serwerze graczy
 	        		this.playerslist.put(res.getString("player_name").toLowerCase(),
 	        							   new DooBStatPlayerData(res.getInt("id"), 
 	        									   	              res.getString("player_name"),
 	        									   	              curdate));
 				} // else się nie przejmujemy bo tutaj powinni być tylko gracze,
 				// którzy już wcześniej byli na serwerze...
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 
 			try {
 				res.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		try {
 			prest2.executeBatch();
 			prest2.clearBatch();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		
 		// sprawdzenie czy jest dostępna nowsza wersja i wyświetlenie
 		// komunikatu w konsoli
 		if(!this.getConfig().getBoolean("debug")) {
 			// sprawdzaj update tylko gdy wyłączony jest debug
 			new UpdateChecker(this, "http://dev.bukkit.org/server-mods/doobstat/files.rss");
 		}
 		
 	}
 	
 	
 	/**
 	 * onDisable
 	 */
 	@Override
 	public void onDisable() {
 		
 		Date curdate = new Date();
 		Timestamp curtimestamp = new Timestamp(curdate.getTime());
 		
 		// zapisanie wszystkich pozostających na serwerze graczy
 		PreparedStatement prest = this.db.getPreparedStatement("updatePlayerQuit");
 		PreparedStatement prest2 = this.db.getPreparedStatement("updatePlayerStatQuit");
 		
 		
 		Iterator<Map.Entry<String, DooBStatPlayerData>> wpisy = this.playerslist.entrySet().iterator();
 		while (wpisy.hasNext()) {
 		    Map.Entry<String, DooBStatPlayerData> wpis = wpisy.next();
 		    try {
 		    	DooBStatPlayerData playerData = wpis.getValue();
 		    	
 				prest.setTimestamp(1, curtimestamp);
 				prest.setInt(2, (int)((curdate.getTime() - playerData.getLoginDate().getTime())/1000));
 				prest.setInt(3, playerData.getPlayerId());
 				prest.addBatch();
 				
 				prest2.setInt(1, (int)playerData.getDist(DooBStatPlayerData.FOOT));
 				prest2.setInt(2, (int)playerData.getDist(DooBStatPlayerData.FLY));
 				prest2.setInt(3, (int)playerData.getDist(DooBStatPlayerData.SWIM));
 				prest2.setInt(4, (int)playerData.getDist(DooBStatPlayerData.PIG));
 				prest2.setInt(5, (int)playerData.getDist(DooBStatPlayerData.CART));
 				prest2.setInt(6, (int)playerData.getDist(DooBStatPlayerData.BOAT));
 				prest2.setInt(7, playerData.getBedEnter());
 				prest2.setInt(8, playerData.getFish());
 				prest2.setInt(9, playerData.getPlayerId());
 				prest2.addBatch();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		    wpisy.remove();
 		}
 		
 		try {
 			prest.executeBatch();
 			prest.clearBatch();
 			prest2.executeBatch();
 			prest2.clearBatch();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
		getLogger().info("onDisable function");
 	}
 	
 }
