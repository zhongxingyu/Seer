 /* cBorder
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
 
 package castro.cBorder;
 
 import java.util.HashMap;
 
 import org.bukkit.World;
import org.bukkit.event.Listener;
 
 
public class BorderMgr implements Listener
 {
 	private static HashMap<String, Border> limits = new HashMap<String, Border>();
 	private static Border noBorder = new NoBorder();
 	
 	public static void init()
 	{
 		limits = Config.getAllBorders();
 	}
 	
 	
 	public static boolean contains(String world)
 	{
 		return limits.containsKey(world);
 	}
 	
 	
 	public static Border getBorder(World world)
 	{ return getBorder(world.getName()); }
 	public static Border getBorder(String world)
 	{
 		Border border = limits.get(world);
 		if(border == null)
 			return noBorder;
 		return border;
 	}
 	
 	
 	public static void setBorder(String worldname, Border newBorder)
 	{
 		if(newBorder.equals(getBorder(worldname)))
 			return;
 		
 		
 		Config.setBorder(worldname, newBorder);
 		Border oldBorder = limits.put(worldname, newBorder);
 		
 		BorderListener.refreshBorder(oldBorder, newBorder, worldname);
 	}
 	
 	
 	public static void removeBorder(String world)
 	{
 		if(limits.containsKey(world))
 		{
 			limits.remove(world);
 			Config.removeWorld(world);
 		}
 	}
 }
