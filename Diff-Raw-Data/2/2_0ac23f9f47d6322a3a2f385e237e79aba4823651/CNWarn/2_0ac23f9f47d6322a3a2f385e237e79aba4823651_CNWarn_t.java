 package me.derflash.plugins.cnwarn;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.SqlRow;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class CNWarn extends JavaPlugin {
 	
 	/**** Some general Stuff ****/
 	public static final Logger cubeLog = Logger.getLogger("Minecraft"); // the logger
 	public static final String logPrefix = "[CubeWarn] "; 	// Log Prefix
     PermissionManager perm = null;
 	public HashMap<Player, ConfirmOfflineWarnTable> offlineWarnings = new HashMap<Player, ConfirmOfflineWarnTable>(); //Hashmap warned offline players until confirm
 	
 	/**** Configuration ****/
 	public HashSet<Player> notAccepted = new HashSet<Player>(); //Hashset all (!online!) players with not accepted warnings
 	
 	public void onDisable() {
 	}
 	
 	public void onEnable() {		
 		//Permissions
 		String msgPerm = setupPermissions();
 		
 		// Database
 		setupDatabase();		
 		
 		new CubeWarnPlayerListener(this);
 		
 		// Output
 		cubeLog.info(msgPerm);
 		cubeLog.info(logPrefix + "Version " + this.getDescription().getVersion() + " enabled");
 	}
 	
 	public void clearOld() {
 		getDatabase().createSqlUpdate("UPDATE `cn_warns` SET rating = 0 WHERE TO_DAYS(NOW()) - TO_DAYS(`accepted`) > 30").execute();
 	}
 	
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
     	// player is the warned player; player1 is the sender
     	// permissions cubewarn.staff & cubewarn.admin
     	Player player1 = (Player) sender;
     	if (label.equalsIgnoreCase("warn")) {
     		
     		int alenght = args.length;  		
     		
 			//show the help
     		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
     			showHelp(player1);
     			return true;
     			
     		//shows a list of warnings for a player
     		} else if (alenght <= 2 && args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("info")) {
     			clearOld();
 
     			//show warnings for the sender (/show list)
     			if (alenght != 2) {
 	    			String playerName = player1.getName();
 	    			//show list of all warnings for the sender
 	    			if (warnedPlayersContains(playerName)) {	
 	    				player1.sendMessage(ChatColor.DARK_RED + "|------------- " +  playerName + " Verwarnungen -------------|");
 	    				showList(playerName, player1);
 	    				return true;
 	    			//sender has no warnings
 	    			} else {
 	    				player1.sendMessage(ChatColor.GREEN + "Du bist nicht verwarnt! Weiter so!");
 	    				return true;
 	    			}
 	    		//show warnings for a specific player (/show list name)
 	    		} else if (this.perm.has(player1, "cubewarn.staff")) {
 	    			//show list of all not accepted online players
 	    			if (warnedPlayersContains(args[1])) {	
 	    				player1.sendMessage(ChatColor.DARK_RED  + "|------------- " + args[1] + " Verwarnungen -------------|");
 	    				showList(args[1], player1);
 	    				return true;
 	    			//player has no warnings
 	    			} else {
 	    				player1.sendMessage(ChatColor.YELLOW + args[1] + ChatColor.GREEN + " wurde bisher noch nicht verwarnt.");
 	    				player1.sendMessage(ChatColor.GREEN + "/warn search [..] " + ChatColor.WHITE + ", um nach einem Spielernamen zu suchen.");
 	    				return true;
 	    			}
 	    		}
     		//search 
     		} else if (alenght <= 2 && args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("check") && this.perm.has(player1, "cubewarn.staff")) {
     			clearOld();
 
     			if (args.length <= 1 || args[1].length() < 3) {
     				player1.sendMessage(ChatColor.DARK_RED  + "Der Suchbegriff muss mindestens 3 Zeichen enthalten.");
     			} else {
     				showSuggestions(args[1], player1); 
     			}
     		//accept the warning 
     		} else if (args[0].equalsIgnoreCase("accept")) {
     			String playerName = player1.getName();
     			if (warnedPlayersContains(playerName)) {
     				if (hasUnacceptedWarnings(playerName)) {
     					acceptWarnings(playerName);
     					player1.sendMessage(ChatColor.DARK_RED + "|------------- " +  playerName + " Verwarnungen -------------|");
     					showList(playerName, player1);
     					player1.sendMessage(ChatColor.GREEN + "Du hast deine Verwarnung akzeptiert!");
     					player1.sendMessage(ChatColor.DARK_RED + "Halte dich in Zukunft an unsere Server-Regeln!!!");
     					return true;
     				} else {
     					player1.sendMessage(ChatColor.GREEN + "Du hast deine Verwarnungen bereits aktzeptiert!");
     				}
     				player1.sendMessage(ChatColor.GREEN + "Benutze " + ChatColor.RED + "/warn info" + ChatColor.GREEN + ", um dir dein Verwarnungen anzuschauen.");
     			} else {
     				player1.sendMessage(ChatColor.GREEN + "Du bist nicht verwarnt! Weiter so!");
     				return true;
     			}
     			
     		//delete a specific warning
     		} else if (alenght == 2 && args[0].equalsIgnoreCase("del") && this.perm.has(player1, "cubewarn.admin")) {
     			Integer id;
 		    	try {
 		    		id = Integer.parseInt(args[1]);
 	            } catch (Exception e){
 	               	player1.sendMessage(ChatColor.RED + "Du musst eine Zahl als ID angeben.");
 	             	player1.sendMessage(ChatColor.RED + "/warn del [ID]");
 	               	return true;
 	            }
 		    	
 	    		deleteWarning(id, player1);
 	    		return true;
     		
     		//delete all warnings for a player
     		} else if (args[0].equalsIgnoreCase("delall") && alenght == 2 && this.perm.has(player1, "cubewarn.admin")) {
     			// delete all warnings if the player was warned
     			if (warnedPlayersContains(args[1])) {
     				deleteWarnings(args[1], player1);
     				return true;
     			// nothing to delete, player has no warnings
     			} else {
     				player1.sendMessage(ChatColor.RED + args[1] + " hat keine Verwarnungen.");
     				return true;
     			}
     		} else if (args[0].equalsIgnoreCase("confirm") && this.perm.has(player1, "cubewarn.staff")) {
     	    	confirmOfflinePlayerWarning(player1);
     			
     		// warn a specific player
     		} else if (args.length >= 3 && this.perm.has(player1, "cubewarn.staff")) {
     			
     			clearOld();
     			
 	    		String message = "";
 	    		Integer rating;
 	    		
 	    		//get the message
 	    		for (int i = 1; i < (alenght - 1); i++) {
 	    			message = message + " " + args[i];
 	    		}
 	    		message = message.trim();
 	    		
 	    		//check if the message is at least 5 chars long
 	    		if (message.length() <= 4) {
 	    			player1.sendMessage(ChatColor.RED + "Der Verwarnungsgrund muss mindestens 5 Zeichen lang sein.");
 	    			player1.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
     				return true;
 	    		}
 	    		
 	    		//get the rating
 	    		try {
 	    			rating = Integer.parseInt (args[(alenght - 1)]);
 	    			if (rating > 6 || rating <= 0) {
 	    				player1.sendMessage(ChatColor.RED + "Die Bewertung muss eine Zahl zwischen 1 und 6 sein.");
 	    				player1.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
 	    				return true;
 	    			}
                 } catch (Exception E){
                 	player1.sendMessage(ChatColor.RED + "Die Bewertung muss eine Zahl sein.");
                 	player1.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
                     return true;
                 }
                 
 	    		//get the warned player object and check if he is online
                 String playerName = args[0];
 	    		Player player = getServer().getPlayer(args[0]);
 	    		if (player instanceof Player) {
 	    			//check if the player tries to warn himself
 		    		player.sendMessage(ChatColor.DARK_RED + "!!! ACHTUNG !!! " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + " DU WURDEST VERWARNT !!!");
 		    		player.sendMessage(ChatColor.DARK_RED + "Du kannst dich jetzt nicht mehr bewegen,");
 		    		player.sendMessage(ChatColor.DARK_RED + "bis du die Verwarnung aktzeptiert hast.");
 		    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn info" + ChatColor.DARK_RED + " kannst du dir den Grund ansehen.");
 		    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn accept" + ChatColor.DARK_RED + " aktzeptierst du die Verwarnung.");;
 	    			warnPlayer(player.getName(), player1, message, rating);
 	    			return true;
 	    		} else {
 	    			warnOfflinePlayer(playerName, player1, message, rating);
 	    			return true;
 	    		}
 	    	
     		} else {
     			//the command was wrong, show the help
     			player1.sendMessage(ChatColor.DARK_RED + "Falsche Eingabe: ");
     			showHelp(player1);
     			return true;
     		}
     	} else if (label.equalsIgnoreCase("watch") && this.perm.has(player1, "cubewarn.watch")) {
     		int alenght = args.length;  		
     		
 			//show the help
     		if (alenght == 1 && args[0].equalsIgnoreCase("list")) {
     			List<Watch> watchedUsers = getDatabase().find(Watch.class).findList();
     			if (watchedUsers.size() > 0) {
 	    			player1.sendMessage(ChatColor.DARK_RED + "Beobachtete User:");
 	    			String userList = null;
 	                Iterator<Watch> i = watchedUsers.iterator();
 	                while (i.hasNext()) {
 	                	Watch watchedUser = i.next();
 	                	if (userList == null) userList = "" + watchedUser.getId() + ":" + watchedUser.getPlayername();
 	                	else userList += ", " + watchedUser.getId() + ":" +watchedUser.getPlayername();
 	                }
 	    			player1.sendMessage(ChatColor.AQUA + userList);
 
     			} else {
 	    			player1.sendMessage(ChatColor.GREEN + "Es werden keine User beobachtet");
 
     			}
     			
 
     		} else if (alenght == 2 && args[0].equalsIgnoreCase("info")) {
                 String playerName = args[1];
                 
                 int _id = -1;
                 try {
                     _id = Integer.parseInt(playerName);
                 } catch (Exception e) {}
                 if (_id != -1 && !playerName.equals(Integer.toString(_id))) _id = -1;
                 
                 Watch watchedUser = null;
                 if (_id != -1)	watchedUser = getDatabase().find(Watch.class).setMaxRows(1).where().eq("id", _id).findUnique();
                 else			watchedUser = getDatabase().find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
 	            if (watchedUser != null) {
 	    			player1.sendMessage(ChatColor.DARK_RED + "Beobachteter User: " + watchedUser.getPlayername());
 	    			player1.sendMessage(ChatColor.AQUA + "Erstellt von " + watchedUser.getStaffname() + " am " + watchedUser.getCreated());
 	    			player1.sendMessage(ChatColor.AQUA + "Beschreibung: " + watchedUser.getMessage());
 	    			
 	            } else {
 	    			player1.sendMessage(ChatColor.GREEN + "Dieser User wird nicht beobachtet");
 	    			
 	            }
 	    		
     		} else if (alenght == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove"))) {
                 String playerName = args[1];
 	    		
                 int _id = -1;
                 try {
                     _id = Integer.parseInt(playerName);
                 } catch (Exception e) {}
                 if (_id != -1 && !playerName.equals(Integer.toString(_id))) _id = -1;
                 
                 Watch watchedUser = null;
                 if (_id != -1)	watchedUser = getDatabase().find(Watch.class).setMaxRows(1).where().eq("id", _id).findUnique();
                 else			watchedUser = getDatabase().find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
 	            if (watchedUser != null) {
 	                getDatabase().delete(watchedUser);
 	    			player1.sendMessage(ChatColor.AQUA + watchedUser.getPlayername() + " erfolgreich von der Watchlist entfernt!");
 	    			
 	            } else {
 	    			player1.sendMessage(ChatColor.GREEN + "Dieser User wird nicht beobachtet");
 
 	            }
 	    		
     		} else if (alenght == 0 || args[0].equalsIgnoreCase("help")) {
     			player1.sendMessage(ChatColor.AQUA + "CNWarn - Watchlist");
     			player1.sendMessage(ChatColor.AQUA + "/watch <name> <beschreibung> - Beobachtet diesen User");
     			player1.sendMessage(ChatColor.AQUA + "/watch list - Listet alle beobachteten User auf");
     			player1.sendMessage(ChatColor.AQUA + "/watch info <name|ID> - Gibt alle Infos zum User aus");
     			player1.sendMessage(ChatColor.AQUA + "/watch delete <name|ID> - Lscht den User aus der Beobachtungsliste");
 
 
     		} else if (alenght > 0) {
                 String playerName = args[0];
                 
                 Watch watchedUser = getDatabase().find(Watch.class).setMaxRows(1).where().ieq("playerName", playerName).findUnique();
                 if (watchedUser == null) {
                     String description = "";
                     for (int i = 1; i < args.length; i++) {
                     	description += (" " + args[i]);
                     }
                 	description = description.trim();
 
         			Watch watch = new Watch();
         			watch.setPlayername(playerName);
         			watch.setMessage(description);
         			watch.setCreated(new Date());
         			watch.setStaffname(player1.getName());
         	        getDatabase().save(watch);
         	        
 	    			player1.sendMessage(ChatColor.AQUA + playerName + " erfolgreich zur Watchlist hinzugefgt!");
 
                 } else {
 	    			player1.sendMessage(ChatColor.DARK_RED + "Dieser User steht bereits unter Beobachtung! Siehe: /watch info " + playerName);
 
                 }
                 
     		}
     	}
     	return true;
 	}
 	
     /**** Startup and Basic Methods ****/
 	private String setupPermissions() {
 		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
 		    PermissionManager _permissions = PermissionsEx.getPermissionManager();
 
 			if (_permissions != null) {
 				this.perm = _permissions;
 				return  logPrefix + "Permission Plugin detected.";
 			}
 		}
 		this.perm = null;
 		return logPrefix + "No Permission Plugin detected. Using OP";
 	}
 
 	public void setupDatabase() {
         try {
             getDatabase().find(Warn.class).findRowCount();
             getDatabase().find(Watch.class).findRowCount();
         } catch (PersistenceException ex) {
             System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
             installDDL();
         }
 	}
 	
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(Watch.class);
         list.add(Warn.class);
         return list;
     }
 
 
 
 	/**** Database related Methods (Insert, Update, Delete, Select) ****/
 	private Integer getWarnCount(String playerName) {
 		return getDatabase().find(Warn.class).where().ieq("playername", playerName).findRowCount();
 	}
 	
 	private Integer getRatingSum(String playerName) {
 		return getDatabase().createSqlQuery("SELECT SUM(rating) AS sumrating FROM cn_warns WHERE playername = '" + playerName + "' LIMIT 1;").findUnique().getInteger("sumrating");
 	}	
 	
 	private void warnPlayer(String warnedPlayer, Player staffMember, String message, Integer rating) {
 		Boolean wasWarned = warnedPlayersContains(warnedPlayer);
 		
 		Warn newWarn = new Warn();
 		newWarn.setPlayername(warnedPlayer);
 		newWarn.setStaffname(staffMember.getName());
 		newWarn.setMessage(message);
 		newWarn.setRating(rating);
 		newWarn.setCreated(new Date());
         getDatabase().save(newWarn);
 
 		staffMember.sendMessage(ChatColor.GREEN + "Verwarnter Spieler: " + ChatColor.YELLOW + warnedPlayer);
 		staffMember.sendMessage(ChatColor.GREEN + "Grund: " + ChatColor.WHITE + message + ChatColor.GREEN +  " Stufe: " + ChatColor.WHITE + rating.toString());
 		
 		if (wasWarned) {
 			staffMember.sendMessage(ChatColor.YELLOW + warnedPlayer + ChatColor.RED + " wurde bereits zuvor verwarnt!");
 			staffMember.sendMessage(ChatColor.RED + "Verwarnungen: " + ChatColor.WHITE + getWarnCount(warnedPlayer).toString() + ChatColor.RED + " | Bewertung: " + ChatColor.WHITE + getRatingSum(warnedPlayer).toString());
 		}
 		
 		Player player = getServer().getPlayer(warnedPlayer);
 		if (player != null) notAccepted.add(player);
 	}
 	
 	private void warnOfflinePlayer(String playerName, Player player1, String message, Integer rating) {
 		offlineWarnings.put(player1, new ConfirmOfflineWarnTable(playerName, message, rating));
 		player1.sendMessage(ChatColor.YELLOW + playerName + ChatColor.RED + " ist offline.");
 		player1.sendMessage(ChatColor.WHITE + "Bist du sicher, dass du den Namen richtig geschrieben hast?");
 		player1.sendMessage(ChatColor.YELLOW + "/warn confirm" + ChatColor.GREEN + ", um die Verwarnung zu besttigen.");
 	}	
 	
 	private void confirmOfflinePlayerWarning(Player player1) {
 		if (offlineWarnings.containsKey(player1)) {
 			String playerName = offlineWarnings.get(player1).playerName;
 			String message = offlineWarnings.get(player1).message;
 			Integer rating = offlineWarnings.get(player1).rating;
 			offlineWarnings.remove(player1);
 			
 			warnPlayer(playerName, player1, message, rating);
 
 		} else {
 			player1.sendMessage("Es existiert keine offline Verwarnung, die besttigt werden kann.");
 		}
 	}
 
 	private void deleteWarning(Integer id, Player staffplayer) {
 		String playerName = getPlayerNameFromId(id);
 		if (playerName != null) {
 			getDatabase().delete(Warn.class, id);
 			
 			Player onlinePlayer = getServer().getPlayer(playerName);
 			if (onlinePlayer != null) notAccepted.remove(onlinePlayer);
 			
 			staffplayer.sendMessage("Verwarnung mit der ID " + id + " wurde gelscht.");
 		}
 	}		
 	
 	private String getPlayerNameFromId(Integer id) {
 		Warn warn = getDatabase().find(Warn.class, id);
 		if (warn != null) return warn.getPlayername();
 		return null;
 	}
 
 	private void deleteWarnings(String playerName, Player staffplayer) {
 		Set<Warn> warns = getDatabase().find(Warn.class).where().ieq("playername", playerName).findSet();
 		getDatabase().delete(warns);
 		
 		Player onlinePlayer = getServer().getPlayer(playerName);
 		if (onlinePlayer != null) notAccepted.remove(onlinePlayer);
 		
 		staffplayer.sendMessage("Alle Verwarnungen von " + playerName + " wurden gelscht.");
 	}	
 	
 	private void acceptWarnings(String playerName) {
		Set<Warn> unAccWarns = getDatabase().find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
 		for (Warn warn : unAccWarns) {
 			warn.setAccepted(new Date());
 		}
         getDatabase().save(unAccWarns);
         
 		Player onlinePlayer = getServer().getPlayer(playerName);
 		if (onlinePlayer != null) notAccepted.remove(onlinePlayer);
 	}
 	
 	public boolean hasUnacceptedWarnings(String playerName) {
 		return getDatabase().find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
 	}
 	
 	/**** Hasmap related Methods (put/remove/containskey) ****/
 	public boolean warnedPlayersContains(String playerName) {
 		return getDatabase().find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
 	}
 	
 
 	/**** Lists ****/
 
 	private void showHelp(Player player1) {
 		player1.sendMessage(ChatColor.RED + "/warn list:" + ChatColor.WHITE + " Zeigt dir deine Verwarnungen an.");
 		player1.sendMessage(ChatColor.RED + "/warn accept:" + ChatColor.WHITE + " Damit aktzeptierst du eine Verwarnung.");	
 		if (this.perm.has(player1, "cubewarn.staff")){
 			player1.sendMessage(ChatColor.RED + "/warn [Spielername][Grund][Bewertung]" + ChatColor.WHITE + " Verwarnt einen Spieler");
 			player1.sendMessage(ChatColor.RED + "/warn list [Spielername]:" + ChatColor.WHITE + " Zeigt alle Verwarnungen des Spielers");
 			player1.sendMessage(ChatColor.RED + "/warn confirm:" + ChatColor.WHITE + " Besttigt die Verwarnung eines offline Spielers");
 			player1.sendMessage(ChatColor.RED + "/warn search:" + ChatColor.WHITE + " Nach einem Spieler suchen.");
 		}
 		if (this.perm.has(player1, "cubewarn.admin")){
 			player1.sendMessage(ChatColor.RED + "/warn del [id]:" + ChatColor.WHITE + " Lscht eine einzelne Verwarnung");
 			player1.sendMessage(ChatColor.RED + "/warn delall [Spielername]:" + ChatColor.WHITE + " Lscht alle Verwarnungen des Spielers");
 		}
 	}	
 	
 	private void showList(String playerName, Player player1) {
 		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy 'um' hh:mm 'Uhr'");
 		int ratingGesamt = 0;
 		for (Warn warn : getDatabase().find(Warn.class).where().like("playername", "%"+playerName+"%").findList()) {
 			Date date = warn.getCreated();		
 			String created = formatter.format(date);
 			ratingGesamt += warn.getRating();
 			
 			player1.sendMessage(ChatColor.YELLOW + "[" + warn.getId() + "] Grund: " + ChatColor.WHITE + warn.getMessage() + " [Stufe " + warn.getRating() + "]");
 			player1.sendMessage(ChatColor.YELLOW + "     " +ChatColor.WHITE + created  + " von " + ChatColor.YELLOW + warn.getStaffname() + ChatColor.WHITE + " akzeptiert: "  + (warn.getAccepted() == null ? "Nein" : "Ja") );
 		}
 		
 		player1.sendMessage(ChatColor.RED + "Gesamtpunktzahl: " + ChatColor.WHITE + ratingGesamt);
 	
 	}
 	
 	private void showSuggestions(String playerName, Player player1) {
 		player1.sendMessage(ChatColor.DARK_RED  + "|--------- Suche nach verwarnten Spielern: " + playerName + " ---------|");
 		
 		String query = "SELECT DISTINCT `playername` FROM cn_warns WHERE `playername` LIKE '%" + playerName + "%' LIMIT 8;";
 		
 		List<SqlRow> found = getDatabase().createSqlQuery(query).findList();
 		if (found.isEmpty()) {
 			player1.sendMessage(ChatColor.DARK_RED + "Keine Ergebnisse gefunden.");
 		
 		} else {
 			String out = "";
 			for (SqlRow row : found) {
 				String _name = row.getString("playername");
 				if (out.length() == 0)	out = _name;
 				else 					out = out + ", " + _name;
 			}
 			player1.sendMessage(ChatColor.YELLOW + out);
 		}
 	}	
 	
 	
 	/**
 	private void showListNotAccepted(Player player1) {
 		String list = "";
 	    for (Map.Entry<String, Boolean> i : notAccepted.entrySet()) {
 			String playerName = i.getKey();
 			list = list + playerName + " ";
 		}
 	    if (list.length() < 2) {
 	    	player1.sendMessage("Kein Spieler online, der seine Verwarnung nicht akzeptiert hat.");
 	    } else {
 	    	player1.sendMessage(list);
 	    }
 	}	
 	**/
 
 
 	
 }
