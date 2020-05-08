 package nl.lolmen.apply;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
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
 	public PermissionManager perm;
 	public HashMap<Player, Applicant> list = new HashMap<Player, Applicant>();
 	public HashMap<Player, String> lookingat = new HashMap<Player, String>();
 	protected Settings set;
 	protected MySQL mysql;
 	
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
 		this.getServer().getPluginManager().registerEvents(new AppListener(this), this);
 		this.mysql = new MySQL(
 				this.set.getHost(), 
 				this.set.getPort(), 
 				this.set.getUsername(), 
 				this.set.getPassword(), 
 				this.set.getDatabase(), 
 				this.set.getTable());
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
 		if(!(sender instanceof Player)){
 			//TODO check the length
 			//TODO get MySQL applicants
 			return true;
 		}
 		Player p = (Player)sender;
 		if(p.hasPermission("apply.check")){
 			//has permission to check other's applications
 			if(args.length == 0){
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE promoted = 0 ORDER BY player" );
 				if(set == null){
 					p.sendMessage("No players to apply! (or an error ocurred)");
 					return true;
 				}
 				try{
 					while(set.next()){
 						p.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + set.getString("player"));
 						p.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + set.getString("banned"));
 						p.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + set.getString("goodat"));
 						p.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + set.getString("name"));
 						p.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + set.getString("age"));
 						p.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + set.getString("country"));
 						p.sendMessage("Accept with /apply accept Reject with /apply deny");
 						this.lookingat.put(p, set.getString("player"));
 					}
 					return true;
 				}catch(Exception e){
 					p.sendMessage("An error occured while reading the application!");
 					e.printStackTrace();
 					return true;
 				}
 			}
 			if(args[0].equalsIgnoreCase("accept")){
 				if(!this.lookingat.containsKey(p)){
 					sender.sendMessage("You have to see someone's application first. /apply");
 					return true;
 				}
 				String player = this.lookingat.get(p);
 				ResultSet set = this.mysql.executeQuery("SELECT * FROM " + this.set.getTable() + " WHERE player='" + player + "'");
 				if(set == null){
 					sender.sendMessage("Well that's just weird.. " + player + " is not in the database O.o");
 					return true;
 				}
 				try {
 					if(set.getInt("promoted") == 1 || !this.perm.getUser(player).inGroup("")){
 						p.sendMessage("Someone else already promoted him: " + set.getString("promoter"));
 						return true;
 					}
 					this.mysql.executeQuery("UPDATE " + this.set.getTable() + " SET promoter='" + sender.getName() + "', promoted=1 WHERE player='" + player + "'");
 					if(!this.perm.getUser(player).inGroup("Non-Applied")){
 						sender.sendMessage("He's not in the non-applied group anymore apparently!");
 						return true;
 					}
 					this.getServer().dispatchCommand(this.getServer().getConsoleSender(), "pex promote " + player);
 				} catch (SQLException e) {
 					p.sendMessage("An error occured while reading the application!");
 					e.printStackTrace();
 					return true;
 				}
 				
 			}
 			
 			
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
 										p.sendMessage("Someone already applied him!");
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
 										p.sendMessage("Someone else already did it.. No-one next!");
 										return true;
 									}
 									if(ar.length == 1){
 										p.sendMessage("Only 1 to apply.. Sorry!");
 										return true;
 									}
 									try{
 										File use = ar[1];
 										Properties prop = new Properties();
 										FileInputStream in = new FileInputStream(use);
 										prop.load(in);
 										p.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + prop.getProperty("IGN"));
 										p.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + prop.getProperty("banned"));
 										p.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + prop.getProperty("goodat"));
 										p.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + prop.getProperty("name"));
 										p.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + prop.getProperty("age"));
 										p.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + prop.getProperty("country"));
 										p.sendMessage("Accept with /apply accept");
 										lookingat.put(p, prop.getProperty("IGN"));
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
 				if(p.hasPermission("apply.apply")){
 					if(perm.getUser(p).inGroup("owner") || perm.getUser(p).inGroup("Admins") || perm.getUser(p).inGroup("Moderator")){
 						File[] ar = new File("plugins/Apply/apps/").listFiles();
 						if(ar.length == 0){
 							p.sendMessage(ChatColor.GREEN + "No-one to apply!");
 							return true;
 						}
 						try {
 							File process = ar[0];
 							Properties prop = new Properties();
 							FileInputStream in = new FileInputStream(process);
 							prop.load(in);
 							p.sendMessage(ChatColor.RED + "IGN: " + ChatColor.WHITE + prop.getProperty("IGN"));
 							p.sendMessage(ChatColor.RED + "Banned: " + ChatColor.WHITE + prop.getProperty("banned"));
 							p.sendMessage(ChatColor.RED + "Good at: " + ChatColor.WHITE + prop.getProperty("goodat"));
 							p.sendMessage(ChatColor.RED + "Name: " + ChatColor.WHITE + prop.getProperty("name"));
 							p.sendMessage(ChatColor.RED + "Age: " + ChatColor.WHITE + prop.getProperty("age"));
 							p.sendMessage(ChatColor.RED + "Country: " + ChatColor.WHITE + prop.getProperty("country"));
 							p.sendMessage("Accept with /apply accept");
 							lookingat.put(p, prop.getProperty("IGN"));
 							in.close();
 							return true;
 						} catch (Exception e) {
 							e.printStackTrace();
 							p.sendMessage("File Error.");
 							return true;
 						}
 						
 					}
 					p.sendMessage("Why would you want to apply " + ChatColor.RED + "again?");
 					return true;
 				}
 				if(list.containsKey(p)){
 					Applicant c = list.get(p);
 					if(c.getNext().equals(todo.CONFIRM)){
 						if(args.length == 0){
 							p.sendMessage("Last thing: These are the rules.");
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
 									pl.sendMessage(p.getName() + " finished the application progress.");
 								}
 							}
 							return true;
 						}else
 							if(args[0].equalsIgnoreCase("reset")){
 								c.setNext(todo.GOODAT);
 								p.sendMessage("Okay, we'll start from the beginnning. " + ChatColor.RED + "What are you good at?");
 								return true;
 							}else{
 								p.sendMessage("Did you mean " + ChatColor.RED + "/apply " + ChatColor.WHITE + "or "+ ChatColor.RED + "/apply reset?");
 								return true;
 							}
 					}else{
 						p.sendMessage("You have to answer the questions first!");
 						return true;
 					}
 				}
 				//Check if already applied, but not yet made Citizen
 				
 				if(new File("plugins/Apply/apps/" + ((Player)sender).getName() + ".txt").exists()){
 					p.sendMessage(ChatColor.RED + "You already applied! " + ChatColor.WHITE + " A Moderator will look at it soon.");
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
