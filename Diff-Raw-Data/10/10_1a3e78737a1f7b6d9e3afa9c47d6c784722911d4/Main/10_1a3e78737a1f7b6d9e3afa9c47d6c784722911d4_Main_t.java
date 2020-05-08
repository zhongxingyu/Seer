 package com.revised.CallOfDutyMC;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Main extends JavaPlugin{
 	/*
 	 * @return Plugin Version
 	 */
 	public String getVersion(){
 		return this.getDescription().getVersion();
 	}
 	
 	/*
 	 * @return Plugin Name
 	 */
 	
 	public String getName(){
 		return "[" + this.getDescription().getName() + "]";
 	}
 	
 	/*
 	 * @return Plugin Info
 	 */
 	
 	public String getInfo(){
 		return getName() + " " + getVersion();
 	}
 
 	@Override
 	public void onDisable() {
 		System.out.println(getInfo());
 		
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PL(), this);
 		pm.registerEvents(new EL(), this);
 		System.out.println(getInfo());
 		
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args){
 		Player p = (Player)sender;
 		int classa = p.getTotalExperience() -  10;
 		int classb = p.getTotalExperience() - 20;
 		int classc = p.getTotalExperience() - 15;
 		ChatColor g = ChatColor.GREEN;
 		ItemStack i = new ItemStack(Material.SNOW_BALL,1);
 		ItemStack ii = new ItemStack(Material.EGG,1);
 		String playername = p.getName();
 		ChatColor a = ChatColor.AQUA;
 		Block target = p.getTargetBlock(null, 200);
 		Location tloc= target.getLocation();
 		World world = p.getWorld();
 		ChatColor red = ChatColor.RED;
 		if (cmdLabel.equalsIgnoreCase("cod") && p.hasPermission("cod.menu")){
 			sender.sendMessage(ChatColor.RED + getVersion());
 			sender.sendMessage(g + "/Airstrike - Must have stick in hand*");
 			sender.sendMessage(g + "/Auto - Automatic guns");
 			sender.sendMessage(g + "/Smoke - Smoke Grenades *Snow Ball");
 			sender.sendMessage(g + "/Grenade - Frag *Egg");
 			sender.sendMessage(g + "/Team <Creepers/Zombies>");
 			sender.sendMessage(g + "/Body Count - Shows your Score of Body Count");
 			sender.sendMessage(g + "/Start - Turns it on and off");
 			sender.sendMessage(g + "/Level <1-10>");
 			sender.sendMessage(g + "/Minigun - Fast Shooting Minigun *Right Click");
 			sender.sendMessage(g + "/Remove Team - Removes from Current Team");
 			sender.sendMessage(g + "/Buy <A/B/C> - Buy Weapon Classes with Exp");
 			return true;
 		}
 		/*
 		 * Implement New Commands for New Version
 		 */
 		else if(cmdLabel.equalsIgnoreCase("buy") && args.length==1 && p.hasPermission("cod.buy")){
 			//Class A
 			if(args[0].equalsIgnoreCase("a")){
 				if(p.getTotalExperience() >= 10){
 					p.setTotalExperience(classa);
 					Classes.classA(p);
 					sender.sendMessage(g + "You have selected Class " + args[0] + "!");
 					return true;
 				} else {
 					sender.sendMessage(red + "Im sorry you need +10 Exp!");
 				}
 				return true;
 			}
 			//Class B
 			else if(args[0].equalsIgnoreCase("b")){
 				if(p.getTotalExperience() >= 20){
 					p.setTotalExperience(classb);
 					Classes.classB(p);
 					sender.sendMessage(g + "You have selected Class " + args[0] + "!");
 					return true;
 				} else {
 					sender.sendMessage(red + "Im sorry you need +20 Exp!");
 				}
 				return true;
 			}
 			//Class C 
 			else if (args[0].equalsIgnoreCase("c")){
				if(p.getTotalExperience() >= 15){
 					p.setTotalExperience(classc);
 					Classes.classC(p);
 					sender.sendMessage(g + "You have selected Class " + args[0] + "!");
					return true;
				} else {
 				sender.sendMessage(red + "Im sorry you need +15 Exp!");
				}
				return true;
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("nuke") && args.length > 0 &&  p.hasPermission("cod.nuke")){
 			if(args.length==0){
 				sender.sendMessage(ChatColor.RED + "/Nuke could lag out your server, please type ' /Nuke Yes ' to confirm");
 				return true;
 			}
 			else if (args.length==1){
 				if(args[0].equalsIgnoreCase("yes")){
 					EX.nuke(world, tloc);
 					return true;
 				}
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("remove") && args.length==1 && p.hasPermission("cod.team")){
 			if(args[0].equalsIgnoreCase("team")){
 				p.setDisplayName(playername);
 			}
 		}
 		else if (cmdLabel.equalsIgnoreCase("airstrike") && p.hasPermission("cod.air")){
 			if(p.getItemInHand().getType()==Material.STICK){
 				target.getWorld().createExplosion(tloc, 3);
 				target.getWorld().strikeLightning(tloc);
 				return true;		
 			}
 		}
 		else if (cmdLabel.equalsIgnoreCase("auto") && p.hasPermission("cod.auto")){
 			if(p.getItemInHand().getType()==Material.STICK){
 				PL.auto=!PL.auto;
 				if(PL.auto){		
 					sender.sendMessage("Auto on!");
 				} else {
 					sender.sendMessage("Auto off!");
 				}
 				return true;
 			}
 		}
 		else if (cmdLabel.equalsIgnoreCase("smoke") && p.hasPermission("cod.smoke")){
 			EL.smoke=!EL.smoke;
 			if(EL.smoke){
 				sender.sendMessage("Smoke on!");
 				p.getInventory().addItem(i);
 			} else {
 				sender.sendMessage("Smoke off!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("grenade") && p.hasPermission("cod.grenade")){
 			EL.gran=!EL.gran;
 			p.getInventory().addItem(ii);
 			if(EL.gran){
 				sender.sendMessage("Enabled!");
 			} else {
 				sender.sendMessage("Disabled!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("join")){
 			PL.join=!PL.join;
 			if(PL.join){
 				sender.sendMessage("Join message enabled!");
 			} else {
 				sender.sendMessage("Join message Disabled!");
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("team") && args.length==1 && p.hasPermission("cod.team")){
 			if(args[0].equalsIgnoreCase("creepers")){
 				sender.sendMessage(g + "You are in team Creepers!");
 				p.setDisplayName(ChatColor.DARK_GREEN + "[Creepers] " + playername);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("zombies")){
 				sender.sendMessage(g + "You are in team Zombies!");
 				p.setDisplayName(a + "[Zombies] " + playername);
 				return true;
 			}
 		}
 		else if (cmdLabel.equalsIgnoreCase("body") && args.length==1){
 			if(args[0].equalsIgnoreCase("count")){
 				int killedEntityCount = ++PlayerInfo.get((Player)sender).killedEntityCount;
 				sender.sendMessage("You killed: " + killedEntityCount);	
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("level") && args.length==1 && p.hasPermission("cod.level")){
 			if(args[0].equalsIgnoreCase("1")){
 				target.getWorld().spawnCreature(tloc, CreatureType.PIG_ZOMBIE);
 			}
 			else if(args[0].equalsIgnoreCase("2")){
 				target.getWorld().spawnCreature(tloc, CreatureType.SKELETON);
 			}
 			else if (args[0].equalsIgnoreCase("3")){
 				target.getWorld().spawnCreature(tloc, CreatureType.WOLF);
 			}
 			else if (args[0].equalsIgnoreCase("4")){
 				target.getWorld().spawnCreature(tloc, CreatureType.SPIDER);
 			}
 			else if (args[0].equalsIgnoreCase("5")){
 				target.getWorld().spawnCreature(tloc, CreatureType.ZOMBIE);
 			}
 			else if (args[0].equalsIgnoreCase("6")){
 				target.getWorld().spawnCreature(tloc, CreatureType.ENDERMAN);
 			}
 			else if (args[0].equalsIgnoreCase("7")){
 				target.getWorld().spawnCreature(tloc, CreatureType.CREEPER);
 			}
 			else if (args[0].equalsIgnoreCase("8")){
 				target.getWorld().spawnCreature(tloc, CreatureType.GIANT);
 			}
 			else if (args[0].equalsIgnoreCase("9")){
 				target.getWorld().spawnCreature(tloc, CreatureType.GHAST);
 			}
 			else if (args[0].equalsIgnoreCase("10")){
 				sender.sendMessage(a + "Congrats! You have won! Return to /cod for some more fun!");
 				return true;
 			}
 			if(Integer.parseInt(args[0]) < 10){
 				sender.sendMessage(a + "Level One! Go! /Level "+ args[0]+ " For Next Round");
 				return true;
 			}
 		}
 		else if (cmdLabel.equalsIgnoreCase("start") && p.hasPermission("cod.start")){
 			PL.on=!PL.on;
 			if(PL.on){
 				sender.sendMessage(a + "Call of Duty MC On!");
 				p.getServer().broadcastMessage(playername + " has turned Call of Duty On!");		
 			} else {
 				sender.sendMessage(a + "Call of Duty MC Off!");
 				p.getServer().broadcastMessage(playername + " has turned Call of Duty Off!");	
 			}
 			return true;
 		}
 		else if (cmdLabel.equalsIgnoreCase("minigun") && p.hasPermission("cod.mini")){
 			PL.mini=!PL.mini;
 			if(PL.mini){
 				sender.sendMessage("Minigun on!");
 			} else {
 				sender.sendMessage("Miniguns off!");
 			}
 			return true;
 			}
 		/*
 		 * Start Classes
 		 */
 		return true;
 	}
 }
 
