 package me.greatman.plugins.inn.commands;
 
 import me.greatman.plugins.inn.IPermissions;
 import me.greatman.plugins.inn.ITools;
 import me.greatman.plugins.inn.Inn;
 import me.greatman.plugins.inn.PlayerData;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.nijikokun.register.payment.Method.MethodAccount;
 
 public class InnCmd implements CommandExecutor {
 	private final Inn plugin;
     public InnCmd(Inn instance) {
         plugin = instance;
     }
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	boolean handled = false;
     	if (is(label, "inn")) {
     		if (args == null || args.length == 0){
     			sendMessage(sender,colorizeText("/ticket help for help",ChatColor.YELLOW));
     			return true;
     		}
     		if (is(args[0], "help")){
    			handled = true;
     			sendMessage(sender, "You are using " + colorizeText(Inn.name, ChatColor.GREEN)
                         + " version " + colorizeText(Inn.version, ChatColor.GREEN) + ".");
         		sendMessage(sender, "Commands:");
         		if (isPlayer(sender) && IPermissions.permission(plugin.getPlayer(sender), "inn create", plugin.getPlayer(sender).isOp())){
         			sendMessage(sender,colorizeText("/inn select",ChatColor.YELLOW) +" - Select a door for Inn usage");
         			sendMessage(sender,colorizeText("/inn create <Price>",ChatColor.YELLOW) + "- Create a inn door");
         		}
     		}
     		if (is(args[0], "select")){
     			handled = true;
     			if (!(sender instanceof Player)){
     				sendMessage(sender,colorizeText("Only players can use this command.",ChatColor.RED));
     				return handled;
     			}
     			if (IPermissions.permission(plugin.getPlayer(sender), "inn.create", plugin.getPlayer(sender).isOp())){
     				Player player = (Player) sender;
     				String playerName = player.getName();
     				if (!plugin.getPlayerData().containsKey(playerName)) {
     	                plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
     	            }
     	            plugin.getPlayerData().get(playerName).setSelecting(!plugin.getPlayerData().get(playerName).isSelecting());
 
     	            if (plugin.getPlayerData().get(playerName).isSelecting()) {
     	                sender.sendMessage(ChatColor.WHITE + "Inn selection enabled." + ChatColor.DARK_AQUA + " Use " + ChatColor.WHITE + "bare hands " + ChatColor.DARK_AQUA + "to select!");
     	                sender.sendMessage(ChatColor.DARK_AQUA + "Left click the room door");
     	            } else {
     	                sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
     	                plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
     	            }
     			}else
     				sendMessage(sender,colorizeText("Permission denied.",ChatColor.RED));
     		}else if(is(args[0], "create")){
     			handled = true;
     			if (!(sender instanceof Player)){
     				sendMessage(sender,colorizeText("Only players can use this command.",ChatColor.RED));
     				return handled;
     			}
     			if (IPermissions.permission(plugin.getPlayer(sender), "inn.create", plugin.getPlayer(sender).isOp())){
     				if (args.length == 1){
     					sendMessage(sender,colorizeText("Syntax: /inn create [Price]",ChatColor.RED));
     					return true;
     				}
     				if (ITools.isInt(args[1])){
     					Player player = (Player) sender;
         				String playerName = player.getName();
     					int[] xyz = plugin.getPlayerData().get(playerName).getPositionA();
     					int y2 = xyz[1] + 1;
     					String query = "INSERT INTO doors(x,y,z,owner,price) VALUES("+ xyz[0] +"," + xyz[1] +"," + xyz[2] +",'" + playerName + "'," + args[1] + ")";
     					String query2 = "INSERT INTO doors(x,y,z,owner,price) VALUES("+ xyz[0] +"," + y2 +"," + xyz[2] +",'" + playerName + "'," + args[1] + ")";
     					Inn.manageSQLite.insertQuery(query);
     					Inn.manageSQLite.insertQuery(query2);
     					MethodAccount playerAccount = plugin.Method.getAccount(playerName);
     					//We check if the player have enough money to create a inn door
         				if (playerAccount.hasEnough(Inn.cost)){
         					playerAccount.subtract(Inn.cost);
         					plugin.getPlayerData().get(playerName).setSelecting(!plugin.getPlayerData().get(playerName).isSelecting());
         					sendMessage(sender,colorizeText("Inn room created!",ChatColor.GREEN));
     					}else
     						sendMessage(sender,colorizeText("You don't have enough money!",ChatColor.RED));
     					
     				}else
     					sendMessage(sender,colorizeText("Expected integer. Received string.",ChatColor.RED));
     			}
     		}	
     	}
     	return handled;
     }
  // Simplifies and shortens the if statements for commands.
     private boolean is(String entered, String label) {
         return entered.equalsIgnoreCase(label);
     }
 
     // Checks if the current user is actually a player.
     private boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
 
     // Checks if the current user is actually a player and sends a message to that player.
     private boolean sendMessage(CommandSender sender, String message) {
         boolean sent = false;
         if (isPlayer(sender)) {
             Player player = (Player) sender;
             player.sendMessage(message);
             sent = true;
         }
         return sent;
     }
     public String colorizeText(String text, ChatColor color) {
         return color + text + ChatColor.WHITE;
     }
 }
