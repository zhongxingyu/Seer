 package me.Travja.HungerArena;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SpawnsCommand implements CommandExecutor {
 	public Main plugin;
 	int i = 0;
 	int a = 0;
 	public SpawnsCommand(Main m) {
 		this.plugin = m;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player p = (Player) sender;
 		if(cmd.getName().equalsIgnoreCase("StartPoint")){
 			if(p.hasPermission("HungerArena.StartPoint")){
 				if(!plugin.restricted || (plugin.restricted && plugin.worlds.contains(p.getWorld().getName()))){
 					Location loc = null;
 					double x;
 					double y;
 					double z;
 					if(args.length == 6){
 						String world = args[2];
 						x = Double.parseDouble(args[3]);
 						y = Double.parseDouble(args[4]);
 						z = Double.parseDouble(args[5]);
 						loc = new Location(Bukkit.getWorld(world), x, y, z);
						if(plugin.location.get(a)!= null){
							if(plugin.location.get(a).size()>= i){
								plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
							}else{
								plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
							}
						}else{
							plugin.location.put(a, new HashMap<Integer, Location>());	
							plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
							plugin.Playing.put(a, new ArrayList<String>());
							plugin.Ready.put(a, new ArrayList<String>());
							plugin.Dead.put(a, new ArrayList<String>());
							plugin.Quit.put(a, new ArrayList<String>());
							plugin.Out.put(a, new ArrayList<String>());
							plugin.Watching.put(a, new ArrayList<String>());
							plugin.NeedConfirm.put(a, new ArrayList<String>());
							plugin.inArena.put(a, new ArrayList<String>());
							plugin.Frozen.put(a, new ArrayList<String>());
							plugin.arena.put(a, new ArrayList<String>());
							plugin.canjoin.put(a, false);
							plugin.open.put(a, true);
						}
						String coords = plugin.location.get(a).get(i).getWorld().getName() + "," + (plugin.location.get(a).get(i).getX()+.5) + "," + plugin.location.get(a).get(i).getY() + "," + (plugin.location.get(a).get(i).getZ()+.5);
						p.sendMessage(coords);
						plugin.spawns.set("Spawns." + a + "." + i, coords);
						plugin.saveSpawns();
						plugin.maxPlayers.put(a, plugin.location.get(a).size());
						p.sendMessage(ChatColor.AQUA + "You have set the spawn location of Tribute " + i + " in arena " + a + "!");
						return true;
 					}
 					if(args.length>= 2){
 						try{
 							i = Integer.valueOf(args[1]);
 							a = Integer.valueOf(args[0]);
 						}catch(Exception e){
 							p.sendMessage(ChatColor.RED + "Argument not an integer!");
 							return true;
 						}
 						if(i >= 1 && i <= plugin.config.getInt("maxPlayers")){
 							if(plugin.restricted && !plugin.worlds.contains(p.getWorld().getName())){
 								p.sendMessage(ChatColor.GOLD + "We ran the command, however, this isn't a world you defined in the config...");
 								p.sendMessage(ChatColor.GOLD + "If this is the right world, please disregard this message.");
 							}
 							loc = p.getLocation().getBlock().getLocation();
 							x = loc.getX();
 							y = loc.getY();
 							z = loc.getZ();
 							if(plugin.location.get(a)!= null){
 								if(plugin.location.get(a).size()>= i){
 									plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
 								}else{
 									plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
 								}
 							}else{
 								plugin.location.put(a, new HashMap<Integer, Location>());	
 								plugin.location.get(a).put(i, new Location(loc.getWorld(), x, y, z));
 								plugin.Playing.put(a, new ArrayList<String>());
 								plugin.Ready.put(a, new ArrayList<String>());
 								plugin.Dead.put(a, new ArrayList<String>());
 								plugin.Quit.put(a, new ArrayList<String>());
 								plugin.Out.put(a, new ArrayList<String>());
 								plugin.Watching.put(a, new ArrayList<String>());
 								plugin.NeedConfirm.put(a, new ArrayList<String>());
 								plugin.inArena.put(a, new ArrayList<String>());
 								plugin.Frozen.put(a, new ArrayList<String>());
 								plugin.arena.put(a, new ArrayList<String>());
 								plugin.canjoin.put(a, false);
 								plugin.open.put(a, true);
 							}
 							String coords = plugin.location.get(a).get(i).getWorld().getName() + "," + (plugin.location.get(a).get(i).getX()+.5) + "," + plugin.location.get(a).get(i).getY() + "," + (plugin.location.get(a).get(i).getZ()+.5);
 							p.sendMessage(coords);
 							plugin.spawns.set("Spawns." + a + "." + i, coords);
 							plugin.saveSpawns();
 							plugin.maxPlayers.put(a, plugin.location.get(a).size());
 							p.sendMessage(ChatColor.AQUA + "You have set the spawn location of Tribute " + i + " in arena " + a + "!");
 						}else{
 							p.sendMessage(ChatColor.RED + "You can't go past " + plugin.config.getInt("maxPlayers") + " players!");
 						}
 					}else if(args.length == 1){
 						if (sender instanceof Player){
 							p = (Player) sender;
 							a = Integer.parseInt(args[0]);
 							if (plugin.spawns.get("Spawns." + a) != null){
 								int start = 1;
 								while(start<plugin.config.getInt("maxPlayers")+2){
 									if (plugin.spawns.get("Spawns." + a + "." + start) != null){
 										start = start+1;
 										if(start== plugin.config.getInt("maxPlayers")+1){
 											p.sendMessage(ChatColor.DARK_AQUA + "[HungerArena] " + ChatColor.GREEN + "All spawns set, type /startpoint [Arena #] [Spawn #] to over-ride previous points!");
 											return true;
 										}
 									}else{
 										int sloc = start;
 										start = plugin.config.getInt("maxPlayers")+1;
 										p.sendMessage(ChatColor.DARK_AQUA + "[HungerArena] " + ChatColor.RED + "Begin Setting For Arena " + a + " Starting From Point " + sloc);
 										plugin.setting.put(p.getName(), a + "-" + sloc);
 										return true;
 									}
 								}
 							}
 							p.sendMessage(ChatColor.DARK_AQUA + "[HungerArena] " + ChatColor.RED + "Begin Setting For Arena " + a + " Starting From Point " + 1);
 							plugin.setting.put(p.getName(), a + "-" + 1);
 							return true;
 						}else{
 							sender.sendMessage(ChatColor.BLUE + "This Can Only Be Sent As A Player");
 						}
 					}else
 						return false;
 				}
 			}else{
 				p.sendMessage(ChatColor.RED + "You don't have permission!");
 			}
 		}
 		return false;
 	}
 
 }
