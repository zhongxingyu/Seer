 package tc.yuzko.mc;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Base extends JavaPlugin {
 
 	@Override 
 	public void onEnable(){
 		getLogger().info("[Trade] Plugin enabled!");
 	}
 
 	@Override
 	public void onDisable(){
 		getLogger().info("[Trade] Plugin disabled!");
 	}
 
 
 
 	public boolean onCommand(CommandSender caller, Command cmd,
 			String label, String[] args){
 
 		Player pcaller = (Player) caller; 
 
 		if(cmd.getName().equalsIgnoreCase("Trade"))
 			if(args.length != 3){ //If it's not /command arg0 arg1 arg2
 				caller.sendMessage(ChatColor.RED + "#Trade: " + ChatColor.BLUE + "Please " +
 						ChatColor.BLUE + "use /trade <player> <amount> <price>");
 				return true;
 			}else{
 
 				String target = args[0];
 				Player p = Bukkit.getPlayer(target);
 
 				if(args.length == 3){ //If command is /command arg0 arg1 arg2
 
 					if(!p.isOnline()){ //If p (target) is not online
 						caller.sendMessage(ChatColor.RED + "#Trade: " + 
 								ChatColor.BLUE + p.getName() + ChatColor.BLUE + "Is not online!");
 					}
 				}else{
 
 					boolean number = true;
 
 					if(p.isOnline()){
 						try{
 							Integer.parseInt(args[1]);
 						}catch(NumberFormatException e){
 							p.sendMessage(ChatColor.RED + "#Trade: " +
 									ChatColor.BLUE + "Please use /trade <player> <amount> <price>");
 							number = false;
 						}
 
 						String getNumber = args[1];
 
 						int getAmount = pcaller.getInventory().getItemInHand().getAmount();
 
 						if(getAmount < Integer.getInteger(getNumber) ){
 							caller.sendMessage(ChatColor.RED + "#Trade: " +
 									ChatColor.BLUE + "Invalid amount!");
 						}else{
 							
 							boolean moneyIsNumber = true;
						
 							
							if(number = true){
 								try{
 									Integer.parseInt(args[2]);
 								}catch(NumberFormatException e){
 									p.sendMessage(ChatColor.RED + "#Trade: " +
									ChatColor.BLUE + "Please use /trade <player> <amount> <price>");
 									moneyIsNumber = false;
								}else{
									int moneyTyped = args[2];
									//Check how much money player has
 								}
 							}
 						}
 					}
 				}
 			}
 
 
 		return false;
 	}
 
 
 
 }
