 /*  
  *  OkGlass, Minecraft bukkit plugin
  *  (c)2013, fromgate, fromgate@gmail.com
  *  http://dev.bukkit.org/server-mods/okglass/
  *    
  *  This file is part of CPFix.
  *  
  *  OkGlass is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  OkGlass is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with CPFix.  If not, see <http://www.gnorg/licenses/>.
  * 
  */
 package me.fromgate.okglass;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 public abstract class Gadget {
 	public OkGlass plg;
 	YamlConfiguration cfg;
 	boolean enabled = true;
 	
 	public boolean isEnabled(){
 		return enabled;
 	}
 	
 	public void setEnabled(boolean enabled){
 		this.enabled = enabled;
 	}
 	
 	public void initGadget(OkGlass plugin, YamlConfiguration cfg){
 		this.cfg = cfg;
 		plg = plugin;
 		setEnabled(loadBoolean ("enabled",true));
 		init();
 	}
 
 	public int loadInt(String key, int defvalue){
 		int v = cfg.getInt(getName()+"."+key,defvalue);
 		cfg.set(getName()+"."+key, v);
 		return v;
 	}
 	public String loadStr(String key, String defvalue){
 		String v = cfg.getString(getName()+"."+key,defvalue);
 		cfg.set(getName()+"."+key, v);
 		return v;
 	}
 	public boolean loadBoolean(String key, boolean defvalue){
 		boolean v = cfg.getBoolean(getName()+"."+key,defvalue);
 		cfg.set(getName()+"."+key, v);
 		return v;
 	}
 	
 	public String getScoreName(){
 		String str = getResultName();
 		if ((str == null)||(str.isEmpty())) str = getName();
 		if ((str == null)||(str.isEmpty())) str = this.getClass().getName();
 		if (str.length()>16) str = str.substring(0, 15);
 		return ChatColor.translateAlternateColorCodes('&', str);
 	}

 	public abstract void init();
 	public abstract String getName();
 	public abstract String getResultName();
 	public abstract int getResultValue();
 
 
 
 }
