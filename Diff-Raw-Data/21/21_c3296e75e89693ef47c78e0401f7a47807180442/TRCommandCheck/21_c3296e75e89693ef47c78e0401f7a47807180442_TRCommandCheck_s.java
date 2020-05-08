 package com.github.dreadslicer.tekkitrestrict.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.dreadslicer.tekkitrestrict.Send;
 import com.github.dreadslicer.tekkitrestrict.TRLimiter;
 import com.github.dreadslicer.tekkitrestrict.TRPermHandler;
 import com.github.dreadslicer.tekkitrestrict.objects.TRLimit;
 import com.github.dreadslicer.tekkitrestrict.objects.TRPermLimit;
 
 public class TRCommandCheck implements CommandExecutor {
 	private Send send;
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		send.sender = sender;
 		
 		if (send.noConsole()) return true;
 		
 		if (send.noPerm("checklimits")) return true;
 		
 		if (args.length > 1){
 			send.msg(ChatColor.RED + "Incorrect syntaxis!");
 			send.msg("/checklimits", "Check all your limits.");
 			send.msg("/checklimits id", "Check limits for the given id.");
 			send.msg("/checklimits hand", "Check limits about the block you are holding.");
 			return true;
 		}
 		Player player = (Player) sender;
 		
 		TRLimiter cc = TRLimiter.getOnlineLimiter(player);
 		
		
		
 		if (args.length == 1){
 			if(cc.itemlimits.isEmpty()){
 				send.msg(ChatColor.RED + "You don't have any limits!");
 				return true;
 			}
 			
 			int id;
 			try {
 				id = Integer.parseInt(args[0]);
 			} catch (NumberFormatException ex){
 				if (args[0].equalsIgnoreCase("hand")){
 					ItemStack item = player.getItemInHand();
 					if (item == null){
 						send.msg(ChatColor.RED + "You don't have anything in your hand!");
 						return true;
 					} else {
 						id = item.getTypeId();
 					}
 				} else if (args[0].equalsIgnoreCase("help")){
 					send.msg("/checklimits", "Check all your limits.");
 					send.msg("/checklimits id", "Check limits for the given id.");
 					send.msg("/checklimits hand", "Check limits about the block you are holding.");
 					return true;
 				} else {
 					send.msg(ChatColor.RED + "You didn't specify a valid number!");
 					return true;
 				}
 			}
 			
 			for (TRLimit l : cc.itemlimits) {
 				if (l.id != id) continue;
 				int cccl = cc.getMax(player, l.id, l.data);
 				cccl = cccl == -1 ? 0 : cccl;
 				send.msg("[" + l.id + ":" + l.data + "] - " + l.placedBlock.size() + "/" + cccl + " blocks");
 			}
 		} else {
 			List<TRLimit> skip = new ArrayList<TRLimit>();
 			for (TRLimit l : cc.itemlimits) {
 				int cccl = cc.getMax(player, l.id, l.data);
 				cccl = cccl == -1 ? 0 : cccl;
 				send.msg("[" + l.id + ":" + l.data + "] - " + l.placedBlock.size()+"/"+cccl+" blocks");
 				skip.add(l);
 			}
 			List<String> permMsgs = getAllPerms(player, skip);
 			for (String msg : permMsgs) send.msg(msg);
 		}
 		return true;
 	}
 	
 	public static List<String> getAllPerms(Player player, List<TRLimit> skip){
 		List<TRPermLimit> perms = TRPermHandler.getAllLimiterPerms(player);
 		List<String> tbr = new ArrayList<String>();
 		outer:
 		for (TRPermLimit limit : perms){
 			for (TRLimit l : skip){
 				if (l.id == limit.id && (l.data == -1 || l.data == limit.data || limit.data == -1)) continue outer;
 			}
 			tbr.add("["+ limit.id + ":" + limit.data + "] - 0/"+(limit.max == -2 || limit.max == -1 ? 0 : limit.max) + " blocks");
 		}
 		return tbr;
 	}
 
 }
