 package net.year4000.einvsee;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 
 import com.sk89q.commandbook.CommandBook;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.minecraft.util.commands.CommandPermissionsException;
 import com.sk89q.minecraft.util.commands.WrappedCommandException;
 import com.zachsthings.libcomponents.ComponentInformation;
 import com.zachsthings.libcomponents.bukkit.BukkitComponent;
 
 @ComponentInformation(friendlyName = "eInvSee", desc = "Look or edit players enderchest or inventories.")
 public class EInvSee extends BukkitComponent{
 
	private String component = "[eInvSee]";
     
     public void enable() {
     	registerCommands(Commands.class);
        Logger.getLogger(component).log(Level.INFO, component+" has been enabled.");
    }
    public void reload() {
    	Logger.getLogger(component).log(Level.INFO, component+" has been reloaded.");
    }
    public void disabled(){
    	Logger.getLogger(component).log(Level.INFO, component+" has been disabled.");
     }
     
     public class Commands{
 		@Command(aliases = {"open"}, usage = "[player]",
 				desc = "Opens the players inventory or enderchest", flags = "i e", min = 1, max = 2)
 		@CommandPermissions({"einvsee.inventory", "einvsee.enderchest"})
     	public void command(CommandContext args, CommandSender player) throws CommandPermissionsException, WrappedCommandException{
 			try{
				OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args.getString(0));
 				Player p = Bukkit.getPlayer(player.getName()).getPlayer();
 				if(args.hasFlag('i') || args.argsLength() == 1){
 					if(otherPlayer != p){
 						CommandBook.inst().checkPermission(player, "einvsee.inventory");
						p.openInventory(((HumanEntity) otherPlayer).getInventory());
 					} else{
 						player.sendMessage(ChatColor.YELLOW +  "You can't check your own inventory.");
 					}
 				}
 				if(args.hasFlag('e')){
 					CommandBook.inst().checkPermission(player, "einvsee.enderchest");
					p.openInventory(((HumanEntity) otherPlayer).getEnderChest());
 				}
 			} catch(Exception e){
 				player.sendMessage(ChatColor.RED +  "You are not a player.");
 			}
     	}
 		
 		@Command(aliases = {"workbench", "crafting"},
 				desc = "Opens your workbench", flags = "", max = 0)
 		@CommandPermissions({"einvsee.workbench"})
     	public void workbench(CommandContext args, CommandSender player){
 			try{
 				Player p = Bukkit.getPlayer(player.getName());
 				p.openWorkbench(null, true);
 			} catch(ClassCastException e){
 				player.sendMessage(ChatColor.RED +  "You are not a player.");
 			}
     	}
 		
 		@Command(aliases = {"enchant"},
 				desc = "Opens your enchanting table", flags = "", max = 0)
 		@CommandPermissions({"einvsee.enchant"})
     	public void enchant(CommandContext args, CommandSender player){
 			try{
 				Player p = Bukkit.getPlayer(player.getName());
 				p.openEnchanting(null, true);
 			} catch(ClassCastException e){
 				player.sendMessage(ChatColor.RED +  "You are not a player.");
 			}
     	}
     }
 }
