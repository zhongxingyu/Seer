 /*
 iControlU - By FireBreath15
 
 This plugin allows server operators to control other players. Issue one command and anything the operator does, the
 controlled player does. Chat, walking, and more features to come. When an OP chats, and they are controlling someone,
 their message isn't sent. Instead the controled player says the message. Also, controlled players cannot move freely.
 The OP controls that too. Each step is mimicked exactly by the controlled player. When the OP is finished, another 
 command stops the process and the controlled player can chat and move freely again.
 
 Idea by me ^_^ after watching Doctor Who, I got inspired :p
 
 
 controllers.<name>.person = the name of the player being controlled
 controllers.<name>.controlling = exists if the player is controlling someone.
 <player name> = exists if the player is being controlled.
 <player name>.who = the name of the OP who controls this player
 */
 package me.firebreath15.icontrolu;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitTask;
 
 public class iControlU extends JavaPlugin{
 	
 	INVAPI api;
 	
 	public void onEnable(){
 		this.getConfig().set("controllers", null);  //no ones controlled upon startup!
 		this.getConfig().set("controlled", null);  //no ones controlled upon startup!
 		this.saveConfig();
 		this.getServer().getPluginManager().registerEvents(new onMove(this), this);
 		this.getServer().getPluginManager().registerEvents(new onChat(this), this);
 		this.getServer().getPluginManager().registerEvents(new onLogout(this), this);
 		this.getServer().getPluginManager().registerEvents(new onHurt(this), this);
 		this.getServer().getPluginManager().registerEvents(new onInteract(this), this);
 		api=new INVAPI();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
 		
 		if(cmd.getName().equalsIgnoreCase("icu")){
 			if(sender instanceof Player){
 				if(args.length == 0 || args.length > 2){
 					//show help menu. they got their arguments wrong!
					sender.sendMessage(ChatColor.YELLOW+"==========[ iControlU Help v1.5.7]==========");
 					sender.sendMessage(ChatColor.BLUE+"/icu control <player>"+ChatColor.GREEN+" Enter Control Mode with <player>.");
 					sender.sendMessage(ChatColor.BLUE+"/icu stop"+ChatColor.GREEN+" Exit Control Mode.");
 					sender.sendMessage(ChatColor.YELLOW+"              ==========              ");
 					sender.sendMessage(ChatColor.DARK_PURPLE+"Created by FireBreath15");
					sender.sendMessage(ChatColor.YELLOW+"==========[ iControlU Help v1.5.7]==========");
 				}
 				
 				if(args.length == 2){
 					if(args[0].equalsIgnoreCase("control")){
 						if(sender.hasPermission("icu.control")){
 							String name = sender.getName();
 							if(!(this.getConfig().contains("controllers."+name))){
 								if(this.getServer().getPlayer(args[1]) != null){
 								Player victim = this.getServer().getPlayer(args[1]);
 								if(!(victim.hasPermission("icu.exempt"))){
 									//here
 									Player s = (Player)sender;
 									victim.hidePlayer(s);
 									s.teleport(victim);
 									s.hidePlayer(victim);
 									Player[] ps = this.getServer().getOnlinePlayers();
 									int pon = ps.length;
 									for(int i=0; i<pon; i++){
 										ps[i].hidePlayer(s);
 									}
 									
 									this.getConfig().set("controlled."+victim.getName(), sender.getName());
 									this.getConfig().set("controllers."+sender.getName()+".person", victim.getName());
 									this.getConfig().set("controllers."+name+".controlling", true);
 									this.saveConfig();
 									
 									api.storePlayerInventory(s.getName());
 									api.storePlayerArmor(s.getName());
 									s.getInventory().setContents(victim.getInventory().getContents());
 									s.getInventory().setArmorContents(victim.getInventory().getArmorContents());
 									@SuppressWarnings("unused")
 									BukkitTask sync = new InvSync(this).runTaskLater(this, 20);
 									
 									s.sendMessage(ChatColor.GOLD+"[iControlU] "+ChatColor.BLUE+"Control Mode activated.");
 									s.sendMessage(ChatColor.GOLD+"[iControlU] "+ChatColor.BLUE+"You begun controlling "+ChatColor.GREEN+victim.getName());
 									s.sendMessage(ChatColor.GOLD+"[iControlU] "+ChatColor.BLUE+"You now control "+victim.getName()+"'s chats and movements.");
 									}else{
										sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You can't control that player!");
 									}
 								}else{
									sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" Player not found!");
 								}
 							}else{
								sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You're already controlling someone!");
 							}
 						}else{
							sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You don't have permission");
 						}
 					}					
 				}
 				
 				if(args.length == 1){
 					if(args[0].equalsIgnoreCase("stop")){
 						if(sender.hasPermission("icu.stop")){
 					String name = sender.getName();
 					if(this.getConfig().contains("controllers."+name+".controlling")){
 						String player = this.getConfig().getString("controllers."+name+".person");
 						Player victim = this.getServer().getPlayer(player);
 						Player s = (Player)sender;
 						this.getConfig().set("controlled."+victim.getName(),null);
 						this.getConfig().set("controllers."+name, null);
 						this.saveConfig();
 						victim.showPlayer(s);
 						PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1);
 						s.addPotionEffect(effect);
 						s.showPlayer(victim);
 						Player[] ps = this.getServer().getOnlinePlayers();
 						int pon = ps.length;
 						for(int i=0; i<pon; i++){
 							ps[i].showPlayer(s);
 							// Now we cycle through every player online and show the sender to them. The controlling is over
 							// so we need to see the troll.
 						}
 						
 						api.restorePlayerInventory(s.getName());
 						api.restorePlayerArmor(s.getName());
 						
						sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You are no longer controlling someone");
 					}else{
						sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You aren't controlling anyone!");
 					}
 					}else{
						sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" You don't have permission!");
 					}
 					}
 				}
 				
 			}else{
				sender.sendMessage(ChatColor.GOLD+"[iControlU]"+ChatColor.RED+" iControlU commands can only be sent from a player!");
 			}
 			return true;
 		}
 		
 		return false;
 	}
 }
