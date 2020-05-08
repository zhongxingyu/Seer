 package me.ceramictitan.inventory;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class InventoryRequest extends JavaPlugin {
     private Plugin plugin;
     private final RequestHandler handler = new RequestHandler();
     @Override
     public void onEnable(){
 	plugin = getServer().getPluginManager().getPlugin("PluginManager");
 	if(plugin != null){
 	    getLogger().info("Plugin Manager version "+ plugin.getDescription().getVersion()+" has been found!");
 	}else{
 	    getLogger().info("Plugin Manager was not found! Continuing...");
 	    getLogger().info("Plugin Manager is used for the reload command");
 	}
     }
     @Override
     public void onDisable(){
 	handler.clearRequests();
     }
 
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel,String[] args) {
 	if(cmd.getName().equalsIgnoreCase("debug")){
 	    if(sender.hasPermission("debug.hashmap")){
 		if(args.length == 1){
 		    if(args[0].equalsIgnoreCase("check")){
 			handler.isInHashMap(sender);
 			return true;
 		    }else if(args[0].equalsIgnoreCase("clear")){
 
 			handler.clearUser(sender);
 			return true;
 		    }
 		}else if(args.length == 2){
 		    if(args[0].equalsIgnoreCase("check")){
			Player target = getServer().getPlayer(args[1]);
			handler.checkUserInHashmap(sender, target);
			return true;
 		    }
 		}
 	    }
 	}
 	if(cmd.getName().equalsIgnoreCase("invreq")){
 	    if(args.length > 2){
 		sender.sendMessage("Too many arguments");
 		return true;
 	    }
 	    if(args.length == 1 || args.length == 2){
 		if(args[0].equalsIgnoreCase("help")){
 		    sender.sendMessage("========="+"Inventory Request "+this.getDescription().getVersion()+"==========");
 		    sender.sendMessage("/invreq send [name] - Sends [name] an inventory request");
 		    sender.sendMessage("/invreq accept - Accepts the request, if you have one");
 		    sender.sendMessage("/invreq deny - Denies the request, if you have one");
 		    if(plugin != null && plugin.isEnabled()){
 			if(sender.hasPermission("invreq.reload")){
 			    sender.sendMessage("/invreq reload - Reloads the plugin!");
 			}
 			sender.sendMessage("Plugin Manager version "+ plugin.getDescription().getVersion()+" is installed!");
 		    }
 		    return true;
 		}else if(args[0].equalsIgnoreCase("send")){
 		    if(sender instanceof Player){
 			Player requester = (Player)sender;
 			Player target = getServer().getPlayer(args[1]);
 			if(target !=null){
 			    handler.sendRequest(requester, target);
 			    requester.sendMessage("Inventory request sent to "+target.getName());
 			    target.sendMessage("You have an inventory request from "+ requester.getName());//Fixed NPE
 			    return true;
 			}else{
 			    sender.sendMessage(ChatColor.YELLOW+ String.valueOf(args[1])+ChatColor.RED+" is offline!");
 			    return true;
 			}
 		    }
 		}else if(args[0].equalsIgnoreCase("accept")){
 		    if(sender instanceof Player){
 			Player player = (Player)sender;
 			if(player != null){
 			    if(handler.hasRequest(player)){
 				handler.acceptRequest(handler.getRequester(player), player);
 				return true;
 			    }else{
 				player.sendMessage("You have no requests!");
 				return true;
 			    }
 			}
 		    }
 		}else if(args[0].equalsIgnoreCase("deny")){
 		    if(sender instanceof Player){
 			Player player = (Player)sender;
 			if(player != null){
 			    if(handler.hasRequest(player)){
 				handler.denyRequest(handler.getRequester(player), player);
 				return true;
 			    }else{
 				player.sendMessage("You have no requests!");
 				return true;
 			    }
 			}
 		    }
 		}else if(args[0].equalsIgnoreCase("reload")){
 		    if(plugin != null){
 			if(plugin.isEnabled()){
 			    this.getServer().dispatchCommand(getServer().getConsoleSender(), "/pm reload "+this.getName());
 			    sender.sendMessage(ChatColor.GREEN+"Plugin reloaded!");
 			    return true;
 			}
 		    }
 		}
 	    }
 	}
 	return false;
     }
 }
