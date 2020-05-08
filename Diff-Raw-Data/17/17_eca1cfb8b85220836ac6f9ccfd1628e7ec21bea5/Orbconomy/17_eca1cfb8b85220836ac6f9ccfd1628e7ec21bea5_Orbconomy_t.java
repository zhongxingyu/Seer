 package de.bananaco.orbconomy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Orbconomy extends JavaPlugin {
 
	public static OrbconomyManager ocm;
 	
 	public void log(Object input) {
 		System.out.println("[Orbconomy "+this.getDescription().getVersion()+"] "+String.valueOf(input));
 	}
 	
 	@Override
 	public void onDisable() {
 		log("Disabled");
 		
 	}
 
 	@Override
 	public void onEnable() {
		ocm = new OrbconomyManager(this, false);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, new DuplicationFix(this), Priority.Normal, this);
 		getServer().getPluginManager().addPermission(new Permission("oc.user","access Orbconomy user commands", PermissionDefault.OP));
 		getServer().getPluginManager().addPermission(new Permission("oc.admin","access Orbconomy admin commands", PermissionDefault.OP));
 		log("Enabled");
 		
 	}
 	
 	public boolean isUser(CommandSender sender) {
 		return sender.hasPermission("oc.user");
 	}
 	
 	public boolean isAdmin(CommandSender sender) {
 		return sender.hasPermission("oc.admin");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		boolean isPlayer = false;
 		if(sender instanceof Player)
 			isPlayer = true;
 		if(command.getName().equalsIgnoreCase("exp")) {
 			
 		// Basic /exp command
 		// Player only
 		if(args.length == 0) {
 			if(isPlayer && isUser(sender)) {
 				int balance = ocm.getBalance((Player) sender).getAmount();
 				sender.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA + "You have "+balance+" orbs.");
 				return true;
 			}
 			else {
 				sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You can't do that!");
 				return true;
 			}
 		}
 		if(args.length == 1) {
 			if(args[0].equalsIgnoreCase("help")) {
 				sender.sendMessage(ChatColor.AQUA+"--"+ChatColor.GREEN+"Commands"+ChatColor.AQUA+"--");
 				if(isPlayer) {
 					if(isUser(sender)) {
 						sender.sendMessage("/exp - shows your orb balance");
 						sender.sendMessage("/exp help - shows this help menu");
 						sender.sendMessage("/exp playername - shows the orb balance of any online player");
 					}
 					if(isAdmin(sender)) {
 						sender.sendMessage("/exp grant playername amount - adds to a players balance");
 						sender.sendMessage("/exp subtract playername amount - subtracts from a players balance");
 						sender.sendMessage("/exp set playername amount - sets a players balance");
 					}
 					return true;
 				}
 				sender.sendMessage("exp help - shows this help menu");
 				sender.sendMessage("exp playername - shows the orb balance of any online player");
 				sender.sendMessage("exp grant playername amount - adds to a players balance");
 				sender.sendMessage("exp subtract playername amount - subtracts from a players balance");
 				sender.sendMessage("exp set playername amount - sets a players balance");
 				return true;
 			}
 			else {
 			if(isPlayer)
 				if(!isUser(sender)) {
 					sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You can't do that!");
 					return true;
 				}
 			Player targetPlayer = getServer().getPlayer(args[0]);
 			if(targetPlayer == null) {
 				sender.sendMessage(ChatColor.RED + "[Orbconomy] Cannot find "+args[0]+" online");
 				return true;
 			}
 			OrbBalance to = ocm.getBalance(targetPlayer);
 			int balance = to.getAmount();
 			
 			sender.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA + targetPlayer.getName() + " has "+balance+" orbs.");
 			return true;
 			}
 		}
 		// Now for some more advanced things
 		// Firstly, let's have basic balance functions
 		if(args.length == 3) {
 			String usage = args[0];
 			String target = args[1];
 			int amount = 0;
 			
 			try {
 			amount = Integer.parseInt(args[2]);
 			} catch (Exception e) {
 				sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"Incorrectly formatted command!");
 				return true;
 			}
 			
 			Player targetPlayer = getServer().getPlayer(target);
 			
 			if(usage.equals("set")) {
 				if(isPlayer)
 					if(!isAdmin(sender)) {
 						sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You can't do that!");
 						return true;
 					}
 				if(targetPlayer == null) {
 					sender.sendMessage(ChatColor.RED + "[Orbconomy] Cannot find "+target+" online");
 					return true;
 				}
 				OrbBalance to = ocm.getBalance(targetPlayer);
 				to.setAmount(amount);
 				sender.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+targetPlayer.getName()+"'s balance set to "+amount+" orbs.");
 				return true;
 			}
 			
 			if(usage.equals("grant")) {
 				if(isPlayer)
 					if(!isAdmin(sender)) {
 						sender.sendMessage(ChatColor.RED +"[Orbconomy] You can't do that!");
 						return true;
 					}
 				if(targetPlayer == null) {
 					sender.sendMessage(ChatColor.RED + "[Orbconomy] Cannot find "+target+" online");
 					return true;
 				}
 				OrbBalance to = ocm.getBalance(targetPlayer);
 				to.add(amount);
 				sender.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+"Granted "+targetPlayer.getName()+" "+amount+" orbs.");
 				targetPlayer.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+"You were granted "+amount+" orbs.");
 				return true;
 			}
 			
 			if(usage.equals("subtract")) {
 				if(isPlayer)
 					if(!isAdmin(sender)) {
 						sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You can't do that!");
 						return true;
 					}
 				if(targetPlayer == null) {
 					sender.sendMessage(ChatColor.RED + "[Orbconomy] "+ChatColor.AQUA+"Cannot find "+target+" online");
 					return true;
 				}
 				OrbBalance to = ocm.getBalance(targetPlayer);
 				if(!to.hasEnough(amount)) {
 					sender.sendMessage(ChatColor.RED + "[Orbconomy] "+ChatColor.AQUA+targetPlayer.getName()+" does not have enough orbs to do that. Subtracting "+to.getAmount()+" orbs instead");
 					amount = to.getAmount();
 				}
 				to.subtract(amount);
 				sender.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+"Took "+amount+" orbs from "+targetPlayer.getName()+".");
 				targetPlayer.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+"You had "+amount+" orbs taken from you.");
 				return true;
 			}
 			
 			// send money from one player to another
 			if(usage.equalsIgnoreCase("send"))
 			if(isPlayer && isUser(sender)) {
 				Player fromPlayer = (Player) sender;
 				if(targetPlayer == null) {
 					sender.sendMessage(ChatColor.RED + "[Orbconomy] Cannot find "+target+" online");
 					return true;
 				}
 				OrbBalance from = ocm.getBalance(fromPlayer);
 				OrbBalance to = ocm.getBalance(targetPlayer);
 				if(from.hasEnough(amount)) {
 					from.subtract(amount);
 					to.add(amount);
 					fromPlayer.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+"You sent "+targetPlayer.getName()+" "+amount+" orbs.");
 					targetPlayer.sendMessage(ChatColor.GREEN + "[Orbconomy] "+ChatColor.AQUA+fromPlayer.getName()+" sent you "+amount+" orbs.");
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You don't have enough orbs!");
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED +"[Orbconomy] "+ChatColor.AQUA+"You can't do that!");
 				return true;
 			}
 		}
 		}	
 			
 		return false;
 		
 	}
 
 }
