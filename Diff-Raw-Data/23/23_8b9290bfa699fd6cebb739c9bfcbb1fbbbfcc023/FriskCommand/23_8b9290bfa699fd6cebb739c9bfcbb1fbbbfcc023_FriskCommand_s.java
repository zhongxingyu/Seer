 package friskstick.cops.commands;
 
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import friskstick.cops.plugin.FriskStick;
 import friskstick.cops.plugin.JailPlayer;
 
 /**
  * 
  * The class that is responsible for handling the '/frisk [player]' command. 
  *
  */
 
 public class FriskCommand implements CommandExecutor{
 
 	private FriskStick plugin;
 	
 	/**
 	 * The constructor for {@link FriskCommand this} class.
	 * @param plugin The {@link FriskStick} object required for the jailing feature to function.
 	 */
 	
 	public FriskCommand(FriskStick plugin) {
 
 		this.plugin = plugin;
 
 	}
 	
 	private JailPlayer jailed = new JailPlayer(plugin);
 
 	private int index = 0;
 	
 	/**
 	 * The method that, in this case, executes the '/frisk [player]' command.
 	 */
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		Player player = null; 		
 
 		if(sender instanceof Player) {
 
 			player = (Player)sender;
 
 		}
 
 		if(player == null) {
 
 			sender.sendMessage("You cannot run this command in the console!");
 
 		} else {
 
 			if(commandLabel.equalsIgnoreCase("frisk")) { // If the player typed /frisk then do the following...
 
 				if(player.hasPermission("friskstick.chat") || player.isOp()) {
 
 					if(args.length == 0) {
 
 						player.sendMessage("Usage: /frisk <playername>");
 
 					} else if(args.length == 1) {
 
 						Player frisked = plugin.getServer().getPlayer(args[0]);
 
 						if(!frisked.hasPermission("friskstick.bypass")) {
 
 							PlayerInventory inventory = frisked.getInventory();
 							boolean found = false;
 
 							for(String drug: plugin.getConfig().getStringList("drug-ids")) {
 
 								if(drug.contains(":")) {
 
 									String firsthalf = drug.split(":")[0];
 									String lasthalf = drug.split(":")[1];
 
 									for(int i = 1; i <= plugin.getConfig().getInt("amount-to-search-for"); i++) {
 
 										ItemStack[] contents = inventory.getContents();
 
 										if(inventory.contains(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf)))) {
 											Iterator<Integer> iter = inventory.all(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf))).keySet().iterator();
 											while(iter.hasNext()){
 												player.getInventory().addItem(new ItemStack(contents[iter.next()]));
 											}
 											inventory.removeItem(new ItemStack(Integer.parseInt(firsthalf), 2305, Short.parseShort(lasthalf)));
 
 											player.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 											plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("&", "").replaceAll("%cop%", player.getName()).replaceAll("%player%", player.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 											if(player.hasPermission("friskstick.jail")) {
 
 												jailed.jail(plugin.getServer().getPlayer(args[0]).getName());
 
 											}
 
 											found = true;
 
 										}
 
 									}
 
 								} else {
 
 									if(inventory.contains(Integer.parseInt(drug))) {
 
 										int drugid = Integer.parseInt(drug);
 										Iterator<Integer> iter = inventory.all(drugid).keySet().iterator();
 										ItemStack[] contents = inventory.getContents();
 										while(iter.hasNext()){
 											player.getInventory().addItem(new ItemStack(contents[iter.next()]));
 										}
 										inventory.removeItem(new ItemStack(drugid, 2305));
 
 										player.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 										plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("%cop%", player.getName()).replaceAll("&", "").replaceAll("%player%", player.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 										if(player.hasPermission("friskstick.jail")) {
 
 											jailed.jail(plugin.getServer().getPlayer(args[0]).getName());
 
 										}
 
 										found = true;
 
 									}
 
 								}
 
 								index++;
 
 							}
 
 							index = 0;
 
 							if(!found) {
 
 								player.sendMessage(plugin.getConfig().getString("cop-not-found-msg").replaceAll("&", "").replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 								plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-not-found-msg").replaceAll("&", "").replaceAll("%cop%", player.getName()).replaceAll("%player%", player.getName()));
 
 								if(player.getHealth() >= 2) {
 
 									player.setHealth(player.getHealth() - 2);
 
 								} else {
 
 									player.setHealth(0);
 
 								}
 
 							}
 
 						} else {
 
 							player.sendMessage("That player cannot be frisked!");
 
 						}
 
 					} 
 
 				} else {
 
 					player.sendMessage(ChatColor.DARK_RED + "You don't have permission to frisk anyone!");
 
 				}
 
 				return true;
 
 			} 
 
 		}
 
 		return false; 
 
 	}
 
 }
