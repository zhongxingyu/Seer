 package nl.lolmen.apply;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import nl.lolmen.apply.Applicant.todo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class Main extends JavaPlugin{
 	public Logger log;
 	private PermissionManager perm;
 	public HashMap<String, Applicant> list = new HashMap<String, Applicant>();
 	private HashMap<String, String> lookingat = new HashMap<String, String>();
 	private Settings set;
 	private MySQL mysql;
 
 	public void onDisable() {
 		this.mysql.close();
 	}
 
 	public void onEnable() {
 		this.log = this.getLogger();
 		new File("plugins/Apply/").mkdir();
 		//new File("plugins/Apply/apps/").mkdir();
 		this.checkPerm();
 		this.set = new Settings();
 		this.getServer().getPluginManager().registerEvents(new Listeners(this), this);
 		this.mysql = new MySQL(
 				this.set.getHost(), 
 				this.set.getPort(), 
 				this.set.getUsername(), 
 				this.set.getPassword(), 
 				this.set.getDatabase(), 
 				this.set.getTable());
 	}
 	
 	protected MySQL getMySQL(){
 		return this.mysql;
 	}
 	
 	protected Settings getSettings(){
 		return this.set;
 	}
 
 	private void checkPerm() {
 		Plugin test = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 		if(test != null){
 			this.perm = PermissionsEx.getPermissionManager();
 			this.log.info("Permissions Plugin found! (PEX)");
 		}else{
 			this.log.info("PEX not found! Disabling!");
 			this.getServer().getPluginManager().disablePlugin(this);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args){
 		if(!cmd.getName().equalsIgnoreCase("apply")){
 			return false;
 		}
 		if(sender.hasPermission("apply.check")){
 			//has permission to check other's applications
 			if(args.length == 0){
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE promoted = 0 ORDER BY player" );
 				if(set == null){
 					sender.sendMessage("It seems there was an error.. Check the logs.");
 					return true;
 				}
 				try{
 					while(set.next()){
 						if(this.list.containsKey(set.getString("player"))){
 							//is still busy
 							continue;
 						}
 						sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + set.getString("player"));
 						sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + set.getString("banned"));
 						sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + set.getString("goodat"));
 						sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + set.getString("name"));
 						sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + set.getString("age"));
 						sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + set.getString("country"));
 						sender.sendMessage("Accept with /apply accept Reject with /apply deny");
 						this.lookingat.put(sender.getName(), set.getString("player"));
 						return true;
 					}
 					sender.sendMessage("No players to apply!");
 					return true;
 				}catch(Exception e){
 					sender.sendMessage("An error occured while reading the application!");
 					e.printStackTrace();
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("accept")){
 				if(!this.lookingat.containsKey(sender.getName())){
 					sender.sendMessage("You have to see someone's application first. /apply");
 					return true;
 				}
 				String player = this.lookingat.get(sender.getName());
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "'");
 				if(set == null){
 					sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
 					return true;
 				}
 				try {
 					while(set.next()){
 						if(set.getInt("promoted") == 1){
 							sender.sendMessage("Someone else already promoted him: " + set.getString("promoter"));
 							return true;
 						}
 						this.mysql.executeQuery("UPDATE " + this.set.getTable() + " SET promoter='" + sender.getName() + "', promoted=1 WHERE player='" + player + "'");
 						if(!this.perm.getUser(player).inGroup("Non-Applied")){
 							sender.sendMessage("He's not in the non-applied group anymore apparently!");
 							return true;
 						}
 						this.getServer().dispatchCommand(this.getServer().getConsoleSender(), "pex promote " + player + " Main");
 						Player prom = this.getServer().getPlayer(player);
 						if(prom == null || !prom.isOnline()){
 							return true;
 						}
 						prom.sendMessage(ChatColor.RED + "You have been promoted by " + ChatColor.GREEN + sender.getName() + "!");
 						this.lookingat.remove(sender.getName());
 						return true;
 					}
 					sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
 					return true;
 				} catch (SQLException e) {
 					sender.sendMessage("An error occured while reading the application!");
 					e.printStackTrace();
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("reject")){
 				if(!this.lookingat.containsKey(sender.getName())){
 					sender.sendMessage("You have to see someone's application first. /apply");
 					return true;
 				}
 				String player = this.lookingat.get(sender.getName());
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "'");
 				if(set == null){
 					sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
 					return true;
 				}
 				try {
 					while(set.next()){
 						if(set.getInt("promoted") == 1 || !this.perm.getUser(player).inGroup("Non-Applied")){
 							sender.sendMessage("Someone else already promoted him: " + set.getString("promoter"));
 							return true;
 						}
 						this.mysql.executeQuery("DELETE FROM " + this.set.getTable() + " WHERE player='" + player + "'");
 						if(!this.perm.getUser(player).inGroup("Non-Applied")){
 							sender.sendMessage("He's not in the non-applied group anymore apparently!");
 							return true;
 						}
 						Player prom = this.getServer().getPlayer(player);
 						if(prom == null || !prom.isOnline()){
 							return true;
 						}
 						prom.sendMessage(ChatColor.RED + "Your application has been rejected, please apply again!");
 						this.lookingat.remove(sender.getName());
 						return true;
 					}
 					sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
 					return true;
 				} catch (SQLException e) {
 					sender.sendMessage("An error occured while reading the application!");
 					e.printStackTrace();
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("lookup")){
 				if(args.length == 1){
 					sender.sendMessage("ERR: args. Correct usage: /apply lookup <player>");
 					return true;
 				}
 				String player = args[1];
 				if(this.getServer().getPlayer(player)!=null){
 					player = this.getServer().getPlayer(player).getName();
 				}
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "' LIMIT 1");
 				if(set == null){
 					sender.sendMessage("This query returned null, sorry!");
 					return true;
 				}
 				try {
 					while(set.next()){
 						sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + set.getString("player"));
 						sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + set.getString("banned"));
 						sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + set.getString("goodat"));
 						sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + set.getString("name"));
 						sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + set.getString("age"));
 						sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + set.getString("country"));
 						sender.sendMessage(ChatColor.RED + "Promoted: " + ChatColor.WHITE + (set.getInt("promoted") == 0 ? "false" : "true"));
						sender.sendMessage(ChatColor.RED + "Promoter: " + ChatColor.WHITE + (set.getString("promoter").equals(null) ? "no-one" : set.getString("promoter")));
 						return true;
 					}
 					sender.sendMessage("Player " + player + " apparently isn't in the database!");
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 					sender.sendMessage("An error occured while reading the application!");
 					return true;
 				}
 			}
 			sender.sendMessage("Unknown Apply command: /apply " + args[0]);
 			return true;
 		}
 		//It's a normal player doing the command
 		if(args.length == 0){
 			if(this.list.containsKey(sender.getName())){
 				//Confirms the data
 				sender.sendMessage("Last thing you need to know: the rules.");
 				this.list.get(sender.getName()).sendRules();
 				this.list.remove(sender.getName());
 				return true;
 			}
 			//check if he already applied
 			ResultSet set = this.getMySQL().executeQuery("SELECT * FROM " + this.getSettings().getTable() + " WHERE player='" + sender.getName() + "' LIMIT 1");
 			if(set == null){
 				sender.sendMessage("The query is null, sorry!");
 				return true;
 			}
 			try {
 				while(set.next()){
 					if(set.getInt("promoted") == 1){
 						//Already promoted
 						sender.sendMessage("You've already applied, and have been promoted by " + (set.getString("promoter").equals(null) ? "no-one" : set.getString("promoter")));
 						return true;
 					}
 					sender.sendMessage("To apply, find a [Apply] sign!");
 					return true;
 				}
 				if(this.perm.getUser(sender.getName()).inGroup("Non-Applied")){
 					sender.sendMessage("To apply, find a [Apply] sign!");
 					return true;
 				}
 				sender.sendMessage("You've already been promoted, but we've got no clue how :O");
 				return true;
 			} catch (SQLException e) {
 				e.printStackTrace();
 				sender.sendMessage("Something went terribly wrong while reading the data!");
 				return true;
 			}
 		}
 		if(args[0].equalsIgnoreCase("reject")){
 			if(this.list.containsKey(sender.getName())){
 				sender.sendMessage("We've reset your application, you can now try again!");
 				Applicant c = this.list.get(sender.getName());
 				c.setNext(todo.GOODAT);
 				sender.sendMessage("So, what are you good at?");
 				return true;
 			}
 			sender.sendMessage("What's there to reject?");
 			return true;
 		}
 
 
 		/*
 		if(str.equalsIgnoreCase("apply")){
 			if(sender instanceof Player){
 				final Player p = (Player)sender;
 				if(args.length == 1){
 					if(perm.getUser(p).inGroup("owner") || perm.getUser(p).inGroup("Admins") || perm.getUser(p).inGroup("Moderator")){
 						String get = args[0];
 						if(get.equalsIgnoreCase("accept") || get.equalsIgnoreCase("deny")){
 							if(lookingat.containsKey(p)){
 								String at = lookingat.get(p);
 								if(get.equalsIgnoreCase("accept")){
 									if(perm.getUser(at).inGroup("applied")){
 										sender.sendMessage("Someone already applied him!");
 										lookingat.remove(p);
 										return true;
 									}
 									getServer().dispatchCommand(getServer().getConsoleSender(), "pex promote " + at);
 									getServer().broadcastMessage(ChatColor.RED + at + ChatColor.WHITE + " is now a " + ChatColor.GREEN + "Citizen! " + ChatColor.WHITE + "Hooray!");
 									lookingat.remove(p);
 									new File("plugins/Apply/apps/" + at + ".txt").delete();
 									return true;
 								}else if(get.equalsIgnoreCase("deny") || get.equalsIgnoreCase("reject") ){
 									getServer().broadcastMessage(at + " had his Citizen application rejected. Try again!");
 									new File("plugins/Apply/apps/" + at + ".txt").delete();
 									lookingat.remove(p);
 									return true;
 								}else if (get.equalsIgnoreCase("next")){
 									File[] ar = new File("plugins/Apply/apps/").listFiles();
 									if(ar.length == 0){
 										sender.sendMessage("Someone else already did it.. No-one next!");
 										return true;
 									}
 									if(ar.length == 1){
 										sender.sendMessage("Only 1 to apply.. Sorry!");
 										return true;
 									}
 									try{
 										File use = ar[1];
 										Properties prop = new Properties();
 										FileInputStream in = new FileInputStream(use);
 										prosender.load(in);
 										sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + prosender.getProperty("IGN"));
 										sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + prosender.getProperty("banned"));
 										sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + prosender.getProperty("goodat"));
 										sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + prosender.getProperty("name"));
 										sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + prosender.getProperty("age"));
 										sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + prosender.getProperty("country"));
 										sender.sendMessage("Accept with /apply accept");
 										lookingat.put(p, prosender.getProperty("IGN"));
 										in.close();
 										return true;
 									}catch(Exception e){
 										e.printStackTrace();
 									}
 								}else{
 									sender.sendMessage("Unknown command.. Sorry!");
 									return true;
 								}
 							}
 						}
 					}
 				}
 				if(sender.hasPermission("apply.apply")){
 					if(perm.getUser(p).inGroup("owner") || perm.getUser(p).inGroup("Admins") || perm.getUser(p).inGroup("Moderator")){
 						File[] ar = new File("plugins/Apply/apps/").listFiles();
 						if(ar.length == 0){
 							sender.sendMessage(ChatColor.GREEN + "No-one to apply!");
 							return true;
 						}
 						try {
 							File process = ar[0];
 							Properties prop = new Properties();
 							FileInputStream in = new FileInputStream(process);
 							prosender.load(in);
 							sender.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + prosender.getProperty("IGN"));
 							sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + prosender.getProperty("banned"));
 							sender.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + prosender.getProperty("goodat"));
 							sender.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + prosender.getProperty("name"));
 							sender.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + prosender.getProperty("age"));
 							sender.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + prosender.getProperty("country"));
 							sender.sendMessage("Accept with /apply accept");
 							lookingat.put(p, prosender.getProperty("IGN"));
 							in.close();
 							return true;
 						} catch (Exception e) {
 							e.printStackTrace();
 							sender.sendMessage("File Error.");
 							return true;
 						}
 
 					}
 					sender.sendMessage("Why would you want to apply " + ChatColor.RED + "again?");
 					return true;
 				}
 				if(list.containsKey(p)){
 					Applicant c = list.get(p);
 					if(c.getNext().equals(todo.CONFIRM)){
 						if(args.length == 0){
 							sender.sendMessage("Last thing: These are the rules.");
 							c.sendRules();
 							c.save();
 							getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
 								public void run() {
 									list.remove(p);
 								}
 							}, 320L);
 							list.remove(p);
 							for(Player pl: getServer().getOnlinePlayers()){
 								if(perm.getUser(p).inGroup("owner") || perm.getUser(p).inGroup("Moderator") || perm.getUser(p).inGroup("Admins")){
 									pl.sendMessage(sender.getName() + " finished the application progress.");
 								}
 							}
 							return true;
 						}else
 							if(args[0].equalsIgnoreCase("reset")){
 								c.setNext(todo.GOODAT);
 								sender.sendMessage("Okay, we'll start from the beginnning. " + ChatColor.RED + "What are you good at?");
 								return true;
 							}else{
 								sender.sendMessage("Did you mean " + ChatColor.RED + "/apply " + ChatColor.WHITE + "or "+ ChatColor.RED + "/apply reset?");
 								return true;
 							}
 					}else{
 						sender.sendMessage("You have to answer the questions first!");
 						return true;
 					}
 				}
 				//Check if already applied, but not yet made Citizen
 
 				if(new File("plugins/Apply/apps/" + ((Player)sender).getName() + ".txt").exists()){
 					sender.sendMessage(ChatColor.RED + "You already applied! " + ChatColor.WHITE + " A Moderator will look at it soon.");
 					return true;
 				}else{
 					Applicant c = new Applicant(this, (Player)sender);
 					c.start();
 					list.put(((Player)sender), c);
 					return true;
 				}		
 			}else{
 				sender.sendMessage("Huh?!? Ur not a player :O");
 				return true;
 			}
 		}*/
 		return false;
 	}
 
 }
