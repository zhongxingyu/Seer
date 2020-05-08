 package me.NerdsWBNerds.InvClear;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class InvClear extends JavaPlugin{
 	public Logger log;
 	
 	public void onEnable(){
 		log = getServer().getLogger();
 		
 		getServer().getPluginManager().registerEvents(new ICListener(this), this);
 	}
 	
 	public void onDisable(){
 		
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			
 			if(cmd.getName().equalsIgnoreCase("list")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.list")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to list an inventory and was denied permission, required node. invclear.list");
 					return true;
 				}
 				
 				if(args.length == 1){
 					target = getServer().getPlayer(args[0]);
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				ArrayList<String> toSay = getList(target);
 				
 				for(String s: toSay){
 					player.sendMessage(s);
 				}
 				
 				return true;
 			}
 			
 			if(cmd.getName().equalsIgnoreCase("clear")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.clear.inventory")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to clear an inventory and was denied permission, required node. invclear.clear.inventory");
 					return true;
 				}
 				
 				if(args.length == 1){
 					target = getServer().getPlayer(args[0]);
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				clearInventory(target);
 				player.sendMessage(ChatColor.GREEN + target.getName() + "'s" + ChatColor.AQUA + " inventory has been cleared.");
 				
 				return true;
 			}
 			
 			if(cmd.getName().equalsIgnoreCase("cleararmor")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.clear.armour") && !player.hasPermission("invclear.clear.armor")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to clear someone's armour and was denied permission, required node. invclear.clear.armour");
 					return true;
 				}
 				
 				if(args.length == 1){
 					target = getServer().getPlayer(args[0]);
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				clearArmour(target);
 				player.sendMessage(ChatColor.GREEN + target.getName() + "'s" + ChatColor.AQUA + " armour has been cleared.");
 				
 				return true;
 			}
 			
 			if(cmd.getName().equalsIgnoreCase("check")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.check")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to check an inventory and was denied permission, required node. invclear.check");
 					return true;
 				}
 				
 				if(args.length > 1){
 					target = getServer().getPlayer(args[0]);
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				int id = 0;
 				
 				try{
 					id = Integer.parseInt(args[1]);
 				}catch(Exception e){
 					try{
 						id = Material.getMaterial(args[1]).getId();
 					}catch(Exception ee){
 						player.sendMessage(ChatColor.RED + "Error while searching " + target.getName() + "'s inventory for " + args[1]);
 						return true;
 					}
 				}
 					
 				int amount = countItem(target, id);
 				
 				if(amount == 0){
 					player.sendMessage(ChatColor.GREEN + "" + target + " has no " + ChatColor.AQUA + Material.getMaterial(id).name());
 				}else{
 					player.sendMessage(ChatColor.GREEN + "" + target + " has " + ChatColor.AQUA + countItem(target, id) + Material.getMaterial(id).name());
 				}
 				
 				return true;
 			}
 			
 			if(cmd.getName().equalsIgnoreCase("remove")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.remove")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to remove items from an inventory and was denied permission, required node. invclear.remove");
 					return true;
 				}
 				
 				int off = 0;
 				
 				if(args.length == 3){
 					target = getServer().getPlayer(args[0]);
 				}else{
 					off = -1;
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				int id = 0;
 				
 				try{
 					id = Integer.parseInt(args[2 + off]);
 				}catch(Exception e){
 					try{
 						id = Material.getMaterial(args[2 + off].toUpperCase()).getId();
 					}catch(Exception ee){
 						player.sendMessage(ChatColor.RED + "Error while removing " + args[2 + off] + " from " + target.getName() + "'s inventory.");
 						return true;
 					}
 				}
 				
 				int amount = 0;
 
 				try{
 					amount = Integer.parseInt(args[1 + off]);
 				}catch(Exception ee){
 					if(args[1 + off].equalsIgnoreCase("all")){
 						amount = -1;
 					}else{
 						player.sendMessage(ChatColor.RED + "Error while removing " + args[1 + off] + " from " + target.getName() + "'s inventory.");
 						return true;
 					}
 				}
 				
 				int removed = removeItem(target, id, amount);
 				
 				if(removed == 0){
 					player.sendMessage(ChatColor.GREEN + "No " + Material.getMaterial(id).name() + ChatColor.AQUA + " found in " + ChatColor.GREEN + target.getName() + "'s " + ChatColor.AQUA + " inventory.");
 				}else{
 					player.sendMessage(ChatColor.GREEN + "" + removed + " " + Material.getMaterial(id).name() + "(S)" + ChatColor.AQUA + " removed from " + ChatColor.GREEN + target.getName() + "'s " + ChatColor.AQUA + " inventory.");
 				}
 				
 				return true;
 			}
 			
 			if(cmd.getName().equalsIgnoreCase("add")){
 				Player target = player;
 				
 				if(!player.hasPermission("invclear.add")){
 					player.sendMessage("You do not have permission to do this.");
 					log.info(player.getName() + " tried to add items to an inventory and was denied permission, required node. invclear.add");
 					return true;
 				}
 				
 				int off = 0;
 				
 				if(args.length == 3){
 					target = getServer().getPlayer(args[0]);
 				}else{
 					off = -1;
 				}
 				
 				if(target == null || !target.isOnline()){
 					player.sendMessage(ChatColor.RED + "Player " + args[0] + " cannot be found.");
 					return true;
 				}
 				
 				int id = 0;
 				
 				try{
 					id = Integer.parseInt(args[2 + off]);
 				}catch(Exception e){
 					try{
 						id = Material.getMaterial(args[2 + off].toUpperCase()).getId();
 					}catch(Exception ee){
 						player.sendMessage(ChatColor.RED + "Error while adding " + args[2 + off] + " to " + target.getName() + "'s inventory.");
 						return true;
 					}
 				}
 				
 				int amount = 0;
 
 				try{
 					amount = Integer.parseInt(args[1 + off]);
 				}catch(Exception ee){
 					if(args[1 + off].equalsIgnoreCase("all")){
 						amount = -1;
 					}else{
 						player.sendMessage(ChatColor.RED + "Error while adding " + args[1 + off] + " to " + target.getName() + "'s inventory.");
 						return true;
 					}
 				}
 				
 				giveItems(target, id, amount);
 				player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.AQUA + " has been given " + ChatColor.GREEN + amount + Material.getMaterial(id) + "(S)");
 				
 				return true;
 			}
 		}else{
 			
 		}
 		
 		return false;
 	}
 
 	public void clearInventory(Player p){
 		p.getInventory().clear();
 	}
 
 	public void clearArmour(Player p){
 		p.getInventory().setArmorContents(null);
 	}
 	
 	public int removeItem(Player p, int id, int amount){
 		int ret = 0;
 		
 		for(int i = 0; i < 35; i++){
 			ItemStack s = p.getInventory().getItem(i);
 			
 			if(s!=null){
 				if(s.getTypeId() == id){
 					if(amount == -1){
 						ret+=s.getAmount();
 						System.out.println(s.getAmount());
 						p.getInventory().setItem(i, null);
 					}else{
 						if(amount - ret < s.getAmount()){
 							s.setAmount(s.getAmount() - (amount - ret));
 							ret = amount;
 							p.getInventory().setItem(i, s);
 							
 							break;
 						}else{
 							ret += s.getAmount();
 							p.getInventory().setItem(i, null);
 						}
 					}
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	public int countItem(Player p, int id){
 		int ret = 0;
 		
 		for(int i = 0; i < 35; i++){
 			ItemStack s = p.getInventory().getContents()[i];
 			
 			if(s!=null){
 				if(s.getTypeId() == id){
 					ret += s.getAmount();
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	public void giveItems(Player p, int id, int amount){
 		p.getInventory().addItem(new ItemStack(id, amount));
 	}
 	
     public ArrayList<String> getList(Player p){
     	ArrayList<String> ret = new ArrayList<String>();
     		
     	ret.add(ChatColor.GOLD + "** " + p.getName() + "'s inventory **");
     	String list = "";
     	
     	for(int i = 0; i < 35; i++){
     		ItemStack[] stuff = p.getInventory().getContents();
     		
     		if (stuff[i]!=null){
     			if(!list.equalsIgnoreCase("")){
     				list += ChatColor.AQUA + " | " + ChatColor.GREEN;
     			}else{
     				list += ChatColor.GREEN;
     			}
     			
     			list += stuff[i].getAmount() + " " + stuff[i].getType().name();
     		}
     	}
     	
     	if(list.equalsIgnoreCase("")){
     		list = "Nothing in inventory.";
     	}
     	
     	ret.add(ChatColor.GREEN + list);
     	
     	return ret;
     }
 }
