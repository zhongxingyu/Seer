 package me.arno.blocklog.commands;
 
 import me.arno.blocklog.WandSettings;
 import me.arno.blocklog.WandSettings.ResultType;
 import me.arno.blocklog.WandSettings.PlayerItem;
 import me.arno.blocklog.util.Syntax;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class CommandWand extends BlockLogCommand {
 	public CommandWand() {
 		super("blocklog.wand");
 		setCommandUsage("/bl wand [results <value>] [since <value>] [until <value>] [type <all|blocks|chests|interactions>]");
 	}
 	
 	@Override
 	public boolean execute(CommandSender sender, Command cmd, String[] args) {
 		if(args.length > 1)
 			return false;
 		
 		if(!hasPermission(sender)) {
 			sender.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		Syntax syn = new Syntax(args);
 		Material wand = getSettingsManager().getWand();
 		
 		if(plugin.wandSettings.containsKey(player.getName())) {
 			WandSettings wandSettings = plugin.wandSettings.get(player.getName());
 			ItemStack itemStack = wandSettings.getPreviousItem().getItem();
 			Material itemInHand = player.getItemInHand().getType();
 			
 			int prevItemSlot = wandSettings.getPreviousItem().getSlot();
 			int itemInHandSlot = player.getInventory().getHeldItemSlot();
 			
 			if(itemInHandSlot == prevItemSlot || (itemInHand == wand && itemInHandSlot != prevItemSlot))
 				player.setItemInHand(itemStack);
 			else
 				player.getInventory().setItem(prevItemSlot, itemStack);
 			
 			plugin.wandSettings.remove(player.getName());
 			player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand disabled!");
 		} else {
			ResultType resultType = ResultType.valueOf(syn.getString("type").toUpperCase());
 			if(resultType == null)
 				resultType = ResultType.ALL;
 			
 			WandSettings wandSettings = new WandSettings(syn.getInt("results", getSettingsManager().getMaxResults()), syn.getTime("since"), syn.getTime("until"), new PlayerItem(player.getItemInHand(), player.getInventory().getHeldItemSlot()), resultType);
 			
 			plugin.wandSettings.put(player.getName(), wandSettings);
 
 			player.setItemInHand(new ItemStack(wand, 1));
 			player.sendMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "Wand enabled!");
 		}
 		return true;
 	}
 }
