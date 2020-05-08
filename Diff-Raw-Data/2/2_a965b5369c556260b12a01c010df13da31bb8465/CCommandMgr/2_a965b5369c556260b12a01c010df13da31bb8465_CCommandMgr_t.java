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
 
 package castro.base;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import castro.base.plugin.CPlugin;
 
 public abstract class CCommandMgr
 {
 	private final CPlugin plugin;
 	protected abstract BaseCCommand getCommand(CommandSender sender, Command command, String[] args);
 	
 	
 	public CCommandMgr(CPlugin plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	
 	public boolean onCommand(CommandSender sender, Command command, String[] args)
 	{
 		BaseCCommand ccommand = getCommand(sender, command, args);
 		if(ccommand != null)
 		{
 			ccommand.baseInit(plugin, sender, command, args);
			return ccommand.run();
 		}
 		return false;
 	}
 }
