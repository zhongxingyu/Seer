 /* *
 
  * HiddenSwitch - Hidden switches and buttons for Bukkit 
  * Copyright (C) 2011-2012  Luphie (devLuphie) luphie@lumpcraft.com
 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
  * */
 package lc.Luphie.hiddenswitch.utilities;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import lc.Luphie.hiddenswitch.HiddenSwitch;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class BlockContainer {
 
 	/** Preloaded key blocks */
 	public HashMap<String, KeyBlock> keyblocks = new HashMap<String, KeyBlock>();
 	private HiddenSwitch me;
 
 	public BlockContainer() {
 
 		me = HiddenSwitch.instance;
 		
 		ResultSet result = HiddenSwitch.DBH.load();
 		try {
 
 			while(result.next()) {
 
 				KeyBlock kb = new KeyBlock(
 
 						result.getString("idstring"),
 						result.getString("world"),
 						result.getInt("x"),
 						result.getInt("y"),
 						result.getInt("z"),
 						result.getString("user"),
 						result.getString("key"),
 						result.getString("owner"),
 						true
 						);
 
 				keyblocks.put(kb.id,kb);
 
 			}
 
			result.close();
 		} catch (SQLException e) {
 
 			me.log.severe(HiddenSwitch.logName + me.lang.getLang().getString("language.errors.cannotloadkeyblocks"));
 			me.log.severe(HiddenSwitch.logName + e.getMessage());
 			e.printStackTrace();
 			return;
 			
 		}
		
 		if(HiddenSwitch.debug) {
 			
 			me.log.info(HiddenSwitch.logName + "Loaded " + Integer.toString(keyblocks.size()) + "KeyBlocks into memory");
 			
 		}
 	}
 	public static Block getBlock(Player player) {
 
 		Block block = player.getTargetBlock(null,32);
 		return block;
 
 	}
 }
 
 
