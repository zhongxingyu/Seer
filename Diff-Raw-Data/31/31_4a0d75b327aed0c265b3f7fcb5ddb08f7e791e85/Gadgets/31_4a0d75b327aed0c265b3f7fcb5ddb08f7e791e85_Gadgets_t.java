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
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 public class Gadgets {
 	OkGlass plg;
 	OGUtil u;
 	List<Gadget> gadgets;
 	Scoreboard brd;
 
 	public Gadgets (OkGlass plg){
 		this.plg = plg;
 		this.u = plg.u;
 		this.gadgets = new ArrayList<Gadget>();
 		init();
 		initScoreBoard();
 		refreshTicks();
 	}
 
 	public void init(){
 		gadgets.clear();
 		loadGadgetsFromJar();
 	}
 
 	public void loadGadgetsFromJar(){
 		File cf = new File (plg.getDataFolder()+File.separator+"gadgets.yml");
 		YamlConfiguration cfg = new YamlConfiguration();
 		try {
 			cfg.load(cf);
 		} catch (Exception cfge) {
 			u.log("Something wrong while loading the gadgets.yml file. Default settings will be used...");
 		}
 
 		File dir = new File ( plg.getDataFolder()+File.separator+"Gadgets");
 		if (!dir.exists()) return;
 		if (!dir.isDirectory()) return;
 
 		File[] fl = dir.listFiles();
 		if (fl.length>0)
 			for (File f : fl){
 				if (!f.getName().toLowerCase().endsWith(".jar")) continue;
 				JarFile jarFile;
 				try {
 					jarFile = new JarFile(f.getPath());
 				} catch (Exception e1) {
 					return;
 				}
 				Enumeration<JarEntry> e = jarFile.entries();
 				ClassLoader cl;					
 				try {
 					URL[] urls = { new URL("jar:file:" +f.getPath()+"!/") };
 					cl = URLClassLoader.newInstance(urls, plg.getClass().getClassLoader());
 				} catch (Exception e2) {
 					u.log("Failed to load jar file");
 					return;
 				}					
 
 				while (e.hasMoreElements()) {
 					JarEntry je = (JarEntry) e.nextElement();
 					if(je.isDirectory() || !je.getName().endsWith(".class")){
 						continue;
 					}
 					String className = je.getName().substring(0,je.getName().length()-6);
 					className = className.replace('/', '.');
 					if (className.contains("$")) continue; //пропускаем лишнее
 					try {
 						Class<?> clazz = Class.forName(className, true, cl);
 						Class<? extends Gadget> c = clazz.asSubclass(Gadget.class);
 						Gadget g = (Gadget) c.newInstance();
 						g.initGadget(plg,cfg);
 						if (g.isEnabled()) {
 							gadgets.add(g);
 							u.log("Gadget loaded: "+g.getName());
 						} else u.log("Gadjet "+g.getName()+" was not loaded (disabled at the gadgets.yml)");
 					} catch (Exception e3){
 						u.log("Failed to gadget(s) from class: "+className);
 						continue;
 					}
 				}
 			}
 		try {
 			cfg.save(cf);
 		}catch (Exception cfge){
 			u.log("Failed to save settings at gadgets.yml file");
 		}
 
 
 	}
 
 	public void initScoreBoard(){
 		brd = plg.getServer().getScoreboardManager().getNewScoreboard();
 		Objective obj = brd.getObjective(ChatColor.GOLD+"OK'GLASS");
 		if (obj == null) obj = brd.registerNewObjective(ChatColor.GOLD+"OK'GLASS", "dummy");
 	}
 
 
 	public void refreshGadgets(){
 		if (gadgets.isEmpty()) return;
 		brd.clearSlot(DisplaySlot.SIDEBAR);
 		clearGadgets();
 		Objective obj = brd.getObjective(ChatColor.GOLD+"OK'GLASS");
 		if (obj == null) obj = brd.registerNewObjective(ChatColor.GOLD+"OK'GLASS", "dummy");
 		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (int i = gadgets.size()-1; i>=0; i--){
 			Gadget g = gadgets.get(i);
 			Score score=obj.getScore(Bukkit.getOfflinePlayer(g.getScoreName()));
			try {
				score.setScore(g.getResultValue());
			} catch (Exception e){
				u.log("Failed to interact with gadget "+g.getName()+". Gadget was disabled.");
				u.log("Exception message: "+e.getMessage());
				gadgets.remove(i);
			} catch (Error e){
				u.log("Failed to interact with gadget "+g.getName()+". Gadget was disabled.");
				u.log("Error message: "+e.getMessage());
				gadgets.remove(i);
			}
 		}
 	}
 
 	public void clearGadgets(){
 		Objective obj = brd.getObjective(ChatColor.GOLD+"OK'GLASS");
 		if (obj == null) obj = brd.registerNewObjective(ChatColor.GOLD+"OK'GLASS", "dummy");
 		for (OfflinePlayer op : brd.getPlayers())
 			brd.resetScores(op);
 	}
 
 	public void sendGadgetsToAll(){
 		Set<Player> cansee = new HashSet<Player>();
 		clearGadgets();
 		for (Player p : Bukkit.getOnlinePlayers()){
 			if (!p.hasPermission("okglass.gadget")) continue;
 			p.setScoreboard(brd);
 			if (isPlayerCanSeeGadget(p)) cansee.add(p);
 		}
 		if (!cansee.isEmpty()){
 			refreshGadgets();
 			for (Player p : cansee) p.setScoreboard(brd);
 		}
 	}
 
 	public boolean isPlayerCanSeeGadget(Player p){
 		if (p.hasMetadata("okglass")) return true;
 		return false;
 	}
 
 	public void setPlayerCanSeeGadget (Player p, boolean cansee){
 		if (cansee) p.setMetadata("okglass", new FixedMetadataValue (plg, true));
 		else if (p.hasMetadata("okglass")) p.removeMetadata("okglass", plg);
 	}
 
 	public void refreshTicks(){
 		Bukkit.getScheduler().runTaskTimerAsynchronously(plg, new Runnable(){
 			@Override
 			public void run() {
 				sendGadgetsToAll();
 			}
 		}, 200,20*plg.refreshdelay);
 	}

 	public void printGadgetList(Player p){
 		if (gadgets.isEmpty()) u.printMSG(p, "gl_gadgjetlistempty");
 		else {
 			String gs = "";
 			for (int i = 0; i<gadgets.size();i++)
 				gs = gs+", "+gadgets.get(i).getName();
 			gs = gs.replaceFirst(", ","");
 			u.printMSG(p, "gl_gadgjetlist",gadgets.size(),"&e"+gs);
 		}
 	}
 
 
 }
