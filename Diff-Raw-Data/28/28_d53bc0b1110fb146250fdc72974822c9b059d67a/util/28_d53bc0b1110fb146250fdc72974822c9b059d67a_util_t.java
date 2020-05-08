 /*
     EduMania: Bukkit plugin for educational users.
     Copyright (C) 2012  korikisulda
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.EduMania;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.bukkit.entity.Player;
 
 public class util {
 	EduMania plugin;
 
 	public util(EduMania instance) {
 		plugin = instance;
 	}
 
 	public boolean authPlayer(Player p, String key) {
 		if (!plugin.authKeys.containsKey(p.getName().toLowerCase()))
 			plugin.authKeys.put(p.getName().toLowerCase(), key);
 		if (isPlayerAuthed(p))
 			return true;
 		if (plugin.authKeys.get(p.getName().toLowerCase()).equals(key)) {
 			plugin.authedPlayers.add(p);
 			return true;
 		} else
 			return false;
 	}
 
 	public void deAuthPlayer(Player p) {
 		plugin.authedPlayers.remove(p);
 	}
 
 	public boolean isPlayerAuthed(Player p) {
 		return plugin.authedPlayers.contains(p);
 	}
 
 	public String join(String[] a, String delimiter, Integer startIndex) {
 		try {
 			Collection<String> s = Arrays.asList(a);
 			StringBuffer buffer = new StringBuffer();
 			Iterator<String> iter = s.iterator();
 
 			while (iter.hasNext()) {
 				if (startIndex == 0) {
 					buffer.append(iter.next());
 					if (iter.hasNext()) {
 						buffer.append(delimiter);
 					}
 				} else {
 					startIndex--;
 					iter.next();
 				}
 			}
 
 			return buffer.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
	
	public void playerjoin(Player p){
		if(!plugin.getConfig().getBoolean("Authentication.enable")) plugin.authedPlayers.add(p); else
			p.sendMessage("[EduMania][AUTH]");
			if(plugin.getConfig().getBoolean("TexturePack.enable")){
				p.sendMessage("[EduMania][TXDL]" + plugin.getConfig().getString("TexturePack.URL") + "|" + plugin.getConfig().getString("TexturePack.fileName"));
			}
	}
 
 	public String sit(String iStr, char delimiter, int part) {
 		if (part == 0) {
 			if (!iStr.contains(String.valueOf(delimiter)))
 				return iStr;
 		} else {
 			if (!iStr.contains(String.valueOf(delimiter)))
 				return "";
 		}
 		if (part == 0)
 			return iStr.substring(0, (iStr.indexOf(delimiter, 0)));
 		return iStr.substring(iStr.indexOf(delimiter, 0) + 1, iStr.length());
 	}
 }
