 /* cPlugin
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package castro.base.plugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public abstract class CMessenger extends CBasePlugin
 {
 	protected Logger logger;
 	
 	
 	private String prepareMsg(String msg, boolean pdf)
 	{
 		if(pdf)
 			msg = getPdf() + msg;
 		return ChatColor.translateAlternateColorCodes('&', msg);
 	}
 	
 	
 	public boolean broadcast(String msg)
 	{ return broadcast(msg, true); }
 	public boolean broadcast(String msg, boolean pdf)
 	{
 		getServer().broadcastMessage(prepareMsg(msg, pdf));
 		return true;
 	}
 	
 	
 	public boolean sendMessage(Player target, String msg)			{ return sendMessage(target, msg, true); }
 	public boolean sendMessage(String target, String msg)			{ return sendMessage(target, msg, true); }
 	public boolean sendMessage(CommandSender sender, String msg)	{ return sendMessage(sender, msg, true); }
 	public boolean sendMessage(String target, String msg, boolean pdf)
 	{
 		CommandSender sender = target.equalsIgnoreCase("CONSOLE") ? getServer().getConsoleSender() : getServer().getPlayerExact(target);
 		return sendMessage(sender, msg, pdf);
 	}
 	public boolean sendMessage(Player target, String msg, boolean pdf)
 	{ return sendMessage((CommandSender)target, msg, pdf); }
 	public boolean sendMessage(CommandSender sender, String msg, boolean pdf)
 	{
 		if (sender != null)
 			sender.sendMessage(prepareMsg(msg, pdf));
 		return true;
 	}
 	
 	
 	public boolean log(String msg)
	{ return log(msg, true); }
 	public boolean log(String msg, boolean pdf)
 	{
 		logger.info(prepareMsg(msg, pdf));
 		return true;
 	}
 }
