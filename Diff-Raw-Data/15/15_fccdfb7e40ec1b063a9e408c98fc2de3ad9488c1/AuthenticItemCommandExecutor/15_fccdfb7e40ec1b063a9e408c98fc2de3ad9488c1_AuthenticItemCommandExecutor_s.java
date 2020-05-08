 package net.loadingchunks.plugins.AuthenticItem.AuthenticItem;
 
 /*
     This file is part of AuthenticItem
 
     Foobar is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Foobar is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class AuthenticItemCommandExecutor implements CommandExecutor {
 
     private AuthenticItem plugin;
 
     public AuthenticItemCommandExecutor(AuthenticItem plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("authentic")) {
         	
        	if(sender.hasPermission("aitem.authentic"))
         	{
         		sender.sendMessage("You do not have permission to use this command.");
         		return true;
         	}
         	
         	if(args.length > 0)
         	{
         		if(!(sender instanceof Player))
         		{
         			sender.sendMessage("You must be in-game to use this command.");
         			return false;
         		}
         		
         		try {
        			AuthenticTypes.valueOf(args[0]); // Worst check ever.
         		} catch (Exception e)
         		{
         			sender.sendMessage("Invalid type given.");
             		sender.sendMessage("Valid types: " + AuthenticTypes.values().toString());
         			return false;
         		}
         		
         		Player p = (Player)sender;
         		
         		AItem item = new AItem(p.getInventory().getItemInHand());
         		
         		if(item.getDisplayName() == null)
         		{
         			sender.sendMessage("You must rename this item first.");
         			return true;
         		}
         		
         		item.setAuthentic(args[0].toUpperCase());
         		
         		String disp = item.getDisplayName();
         		
         		for(AuthenticTypes rem : AuthenticTypes.values())
         		{
         			disp = disp.replace(rem.toString(), "");
         		}
         		
         		disp = ChatColor.stripColor(disp);
         		
         		item.setDisplayName(AuthenticTypes.valueOf(args[0]) + disp + ChatColor.RESET);
         		
         		CraftItemStack cstack = new CraftItemStack(item.getStack());
         		p.getInventory().setItemInHand(cstack);
         	} else {
         		sender.sendMessage("Please specify a trait type for this item.");
         		sender.sendMessage("Valid types: " + AuthenticTypes.values().toString());
         		return false;
         	}
         }
         return false;
     }
 }
