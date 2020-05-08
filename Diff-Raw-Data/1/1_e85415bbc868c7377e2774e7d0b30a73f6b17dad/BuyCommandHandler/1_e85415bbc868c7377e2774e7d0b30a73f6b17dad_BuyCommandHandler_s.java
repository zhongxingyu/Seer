 package net.mayateck.BuyCommand;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BuyCommandHandler implements CommandExecutor{
 	public BuyCommand plugin;
 	
 	public BuyCommandHandler(BuyCommand plugin){
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
 		List<String> aliases = Arrays.asList("buycommand", "bcmd", "bc", "buycmd");
 		if (aliases.contains(cmd.getName().toLowerCase())){
 			if (args.length>0){
 				Player p = (Player)s;
 				if (args[0].equalsIgnoreCase("info")){
 					s.sendMessage(BuyCommand.tag);
 					Set<String> keys = plugin.getConfig().getConfigurationSection("buyables").getKeys(false);
 					s.sendMessage(" "+keys.size());
 					for (String buyable : keys){
 						String name = plugin.getConfig().getString(buyable+".name");
 						double cost = plugin.getConfig().getDouble(buyable+".cost");
 						String desc = plugin.getConfig().getString(buyable+".msg");
 						s.sendMessage(parseColor(" &8(&e"+cost+"&8)&7 "+name+": &f"+desc));
 					}
 					return true;
 				} else if (args[0].equalsIgnoreCase("reload")){
 					if (s.hasPermission("buycommand.admin.reload")){
 						plugin.reloadConfig();
 						s.sendMessage(parseColor(BuyCommand.head+"Successfully pulled to local from disk."));
 					} else {
 						s.sendMessage(parseColor(BuyCommand.head+"Sorry, you don't have permission to push from disk."));
 					}
 					return true;
 				} else {
 					Set<String> nodes = plugin.getConfig().getConfigurationSection("buyables").getKeys(false);
 					String nodepath = "";
 					String nodename = "";
 					for (String node : nodes){
 						if (plugin.getConfig().getString("buyables."+node+".name").equalsIgnoreCase(args[0])){
 							nodepath = "buyables."+node+".";
 							nodename = node;
 						}
 					}
 					if (nodepath==""){
 						s.sendMessage(parseColor(BuyCommand.head+"Sorry, that buyable doesn't exist."));
 					} else {
 						Economy eco = BuyCommand.economy;
 						if (eco.has(p.getName(), plugin.getConfig().getDouble(nodepath+"cost"))){
 							if (args.length==1+plugin.getConfig().getInt(nodepath+"args")){
 								if (s.hasPermission("buycommand.buy."+nodename)){
 									eco.withdrawPlayer(p.getName(), plugin.getConfig().getDouble(nodepath+"cost"));
 									List<String> cmds = plugin.getConfig().getStringList(nodepath+"commands");
 									plugin.getLogger().info(s.getName()+" ran command "+args[0]+".");
 									for(String command : cmds){
 										String newcmd = String.format(command, s.getName());
 										int iterator = 1;
 										for(String arg : args){
 											if (iterator!=1){
 												newcmd.replaceAll(":arg"+iterator+":", arg);
 												iterator++;
 											} else {
 												plugin.getLogger().info("[BuyCommand] Command successfully fired for "+p.getName()+". ("+args.length+")");
 											}
 											s.sendMessage(parseColor(BuyCommand.head+"Successfully purchased '"+plugin.getConfig().getString(nodepath+"name")+"'."));
 										}
 										plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), newcmd);
 									}
 								} else {
 									s.sendMessage(parseColor(BuyCommand.head+"Sorry, you don't have permission for this buyable."));
 								}
 							} else {
 								s.sendMessage(parseColor(BuyCommand.head+"Sorry, the argument number was incorrect."));
 							}
 						} else {
 							s.sendMessage(parseColor(BuyCommand.head+"Sorry, you can't afford this buyable."));
 							double diff = (plugin.getConfig().getDouble(nodepath+"cost"))-eco.getBalance(p.getName());
 							s.sendMessage(parseColor(BuyCommand.head+"You need &e"+diff+"&f more "+eco.currencyNamePlural()+"."));
 						}
 					}
 					return true;
 				}
 			} else {
 				s.sendMessage(BuyCommand.tag);
 				s.sendMessage(BuyCommand.head+"Normally help would be here.");
 				s.sendMessage(BuyCommand.head+"It's on my TO-DO list. :\\");
 				// TODO write help.
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static String parseColor(String string) {
 		String str = ChatColor.translateAlternateColorCodes('&', string);
 		return str;
 	}
 
 }
