 package uk.co.jacekk.bukkit.grouplock.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import uk.co.jacekk.bukkit.baseplugin.v5.command.BaseCommandExecutor;
 import uk.co.jacekk.bukkit.baseplugin.v5.command.CommandHandler;
 import uk.co.jacekk.bukkit.grouplock.Config;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 import uk.co.jacekk.bukkit.grouplock.Locker;
 import uk.co.jacekk.bukkit.grouplock.Permission;
 
 public class LockExecutor extends BaseCommandExecutor<GroupLock> {
 	
 	public LockExecutor(GroupLock plugin){
 		super(plugin);
 	}
 	
	@CommandHandler(names = {"lock", "l"}, description = "Lock or unlock a block", usage = "[<add/remove> <player_name>]")
 	public boolean lock(CommandSender sender, String label, String[] args){
 		if (!(sender instanceof Player)){
 			sender.sendMessage(plugin.formatMessage(ChatColor.RED + "The /lock command can only be used in game"));
 			return true;
 		}
 		
 		if (!Permission.LOCK.has(sender)){
 			sender.sendMessage(plugin.formatMessage(ChatColor.RED + "You do not have permission to use this command"));
 			return true;
 		}
 		
 		//TODO: Fix blocks not being removed.
 		
 		Player player = (Player) sender;
 		String playerName = player.getName();
 		Block block = player.getTargetBlock(null, 20);
 		Material type = block.getType();
 		
 		if (plugin.config.getStringList(Config.IGNORE_WORLDS).contains(block.getWorld().getName())){
 			player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot lock blocks in this world"));
 			return true;
 		}
 		
 		String blockName = type.name().toLowerCase().replace('_', ' ');
 		String ucfBlockName = Character.toUpperCase(blockName.charAt(0)) + blockName.substring(1);
 		
 		if (!plugin.lockableBlocks.contains(block.getType())){
 			player.sendMessage(plugin.formatMessage(ChatColor.RED + ucfBlockName + " is not a lockable block"));
 			return true;
 		}
 		
 		if (!plugin.locker.isBlockLocked(block)){
 			plugin.locker.lock(block, playerName);
 			player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " locked"));
 		}else{
 			String owner = Locker.getOwner(block);
 			
 			if (!Permission.UNLOCK_LOCKED.has(player) && !owner.equals(playerName)){
 				player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + owner));
 			}else{
 				if (args.length == 2){
 					if (args[0].equalsIgnoreCase("add")){
 						plugin.locker.addAllowedPlayer(block, args[1]);
 						player.sendMessage(plugin.formatMessage(ChatColor.GREEN + args[1] + " has been added to the access list"));
 					}else{
 						plugin.locker.removeAllowedPlayer(block, args[1]);
 						player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " unlocked"));
 						player.sendMessage(plugin.formatMessage(ChatColor.GREEN + args[1] + " has been removed from the access list"));
 					}
 				}else{
 					plugin.locker.unlock(block);
 					player.sendMessage(plugin.formatMessage(ChatColor.GREEN + ucfBlockName + " unlocked"));
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 }
