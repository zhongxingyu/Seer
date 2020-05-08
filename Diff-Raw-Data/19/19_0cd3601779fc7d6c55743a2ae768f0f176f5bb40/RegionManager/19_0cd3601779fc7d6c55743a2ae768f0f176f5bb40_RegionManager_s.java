 package de.javakara.manf.mcdefrag.api.manager;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import de.javakara.manf.mcdefrag.MCDefrag;
 import de.javakara.manf.mcdefrag.Config;
 import de.javakara.manf.mcdefrag.Language;
 import de.javakara.manf.mcdefrag.api.Region;
 import de.javakara.manf.mcdefrag.api.ResponseRegion;
 import de.javakara.manf.mcdefrag.api.Highscore;
 import de.javakara.manf.mcdefrag.api.ShedulerFactory;
 import de.javakara.manf.util.ChatTable;
 import de.javakara.manf.util.LanguageComplete;
 import de.javakara.manf.util.SpacerReplace;
 import de.javakara.manf.util.TimeUtilis;
 
 @LanguageComplete
 public class RegionManager {
 	static HashMap<String, ArrayList<Region>> regions;
 	static Hashtable<String, Region> endzone;
 	static HashMap<String, Highscore> highscores;
 	static HashMap<String, Long> maxtime;
 	static HashMap<String, String> blockedroutes;
 	static boolean debug = true;
 	static HashMap<String, Boolean> disabledroutes;
 	static File regionFile;
 	static File highscoreFolder;
 	
 	static {
 		regions = new HashMap<String, ArrayList<Region>>();
 		endzone = new Hashtable<String, Region>();
 		highscores = new HashMap<String, Highscore>();
 		maxtime = new HashMap<String, Long>();
 		blockedroutes = new HashMap<String, String>();
 		disabledroutes = new HashMap<String, Boolean>();
 	}
 
 	public static void initialise(File dataFolder){
 		regionFile = new File(dataFolder + File.separator + "regions.defragbase");
 		highscoreFolder = new File(dataFolder + File.separator + "scores");
 		try {
 			if(!regionFile.exists()){
 				regionFile.createNewFile();
 			}else{
 				load();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ShedulerFactory.newSheduler(new Runnable(){
 			@Override
 			public void run() {
 				try {
 					RegionManager.save();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 		}, 20*60*5, 20*60*5);
 	}
 	
 	public static synchronized boolean areaCheck(String worldname, double x, double y, double z, ResponseRegion r) {
 		if (regions.size() == 0) {
 			return false;
 		}
 		for (Region region : regions.get(worldname)) {
 			if (!disabledroutes.get(region.getName())) {
 				if (region.playerIsInStartRegion(x, y, z)) {
 					if (r != null) {
 						r.setResponse(endzone.get(region.getName()));
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static synchronized void addRegion(String name, Region r) {
 		addRegion(name, r, Config.getLong("default-time")*1000, false,r);
 	}
 
 	public static synchronized void setEndRegion(String region, Region r) {
 		if (endzone.containsKey(region)) {
 			r.setName(region);
 			endzone.put(region, r);
 		}
 
 	}
 
 	public static synchronized String[] getRegions(String world) {
 		ArrayList<Region> reg = regions.get(world);
 		if (reg == null || reg.size() == 0) {
 			return Language.get("route.noworld");
 		}
 		ChatTable ct = new ChatTable(reg.size() + 1, 3);
 
 		ct.setHeader(Language.getRaw("regions.header"));
 		for (Region r : reg) {
 			String name = r.getName();
 			Highscore score = highscores.get(name);
 			ct.add(r.getName() + "-" + score.getTop(1) + " ");
 		}
 		return ct.output();
 	}
 
 	public static synchronized String[] getHighscore(String region) {
 		if (highscores.containsKey(region)) {
 			ChatTable ct = new ChatTable(
 					highscores.get(region).getScores().length + 1, 3);
 			ct.setHeader(Language.getRaw("highscore.header"));
 			for (String[] s : highscores.get(region).getScores()) {
 				String score = s[Highscore.type_score];
 				long scorelong = Long.valueOf(score);
 				ct.add(s[Highscore.type_name] + "-"
 						+ TimeUtilis.getFormattedMinutes(scorelong, 1000));
 			}
 			return ct.output();
 		} else {
 			return Language.get("route.noroute");
 		}
 
 	}
 
 	public static synchronized int addStats(String region, String name,
 			long amount) {
 		if (highscores.containsKey(region)) {
 			return highscores.get(region).newScore(name, amount);
 		}
 		return 0;
 	}
 
 	public static synchronized boolean isBlocked(String region) {
 		if (blockedroutes.containsKey(region)) {
 			return !blockedroutes.get(region).equals("");
 		}
 		return false;
 	}
 
 	public static synchronized long getMaxTime(String region) {
 		if (maxtime.containsKey(region)) {
 			return maxtime.get(region);
 		}
 		return 0;
 	}
 
 	public static synchronized void setTime(String region, long time) {
 		if (maxtime.containsKey(region)) {
 			maxtime.put(region, time);
 		}
 	}
 
 	public static synchronized void addPlayingPlayer(String region, String name) {
 		if (blockedroutes.containsKey(region)) {
 			blockedroutes.put(region, name);
 			PlayerManager.addPlayingPlayer(name, region);
 		}
 	}
 
 	public static synchronized void resetRoute(String route) {
 		String name = blockedroutes.get(route);
 		removePlayingPlayer(route, name);
 		highscores.get(route).reset();
 	}
 
 	public static synchronized void removePlayingPlayer(String name) {
 		String route = PlayerManager.getPlayerRoute(name);
 		removePlayingPlayer(route, name);
 	}
 
 	protected static synchronized void removePlayingPlayer(String route,
 			String name) {
 		blockedroutes.put(route, "");
 		PlayerManager.removePlayingPlayer(name);
 		ShedulerFactory.unregister(name);
 	}
 
 	public static synchronized boolean isProtected(Block b) {
 		return areaCheck(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(),
 				null);
 	}
 
 	public static synchronized void newRecord(Player p, String region, long time) {
 		SpacerReplace sr = new SpacerReplace();
 		sr.addSpacer("%playername%", p.getName());
 		sr.addSpacer("%route%", region);
 		sr.addSpacer("%time%", TimeUtilis.getFormattedMinutes(time, 1000));
 		String msg = MCDefrag.getArgumentString("player.newrecord", sr)[0];
 		try {
 			saveHighscore(region);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		switch (Config.getInt("broadcasttype")) {
 		case 0:
 			String cmd = Config.getString("broadcast-command");
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
 					cmd.replace("<[p]>",p.getName()));
 			return;
 		case 1:
 		default:
 			Bukkit.broadcastMessage(msg);
 			return;
 		case 2:
 			for (Player ps : Bukkit.getOnlinePlayers()) {
 				if (ps.getWorld().getName().equals(p.getWorld().getName())) {
 					ps.sendMessage(msg);
 				}
 			}
 			return;
 		
 		case -1:
 			return;
 		}
 	}
 
 	public static synchronized boolean disableRegion(String region) {
 		return setRegion(region, true);
 	}
 
 	public static synchronized boolean enableRegion(String region) {
 		return setRegion(region, false);
 	}
 
 	private static synchronized boolean setRegion(String region, boolean b) {
 		if (disabledroutes.containsKey(region)) {
 			disabledroutes.put(region, b);
 			return true;
 		}
 		return false;
 	}
 
 	public static synchronized boolean deleteHighscoreScore(String region, int i) {
 		if (highscores.containsKey(region)) {
 			highscores.get(region).deleteScore(i);
 		}
 		return false;
 	}
 	
 	private static synchronized void addRegion(String name,Region r,Long time,boolean disabled,Region end){
 		r.setName(name);
 		end.setName(name);
 		endzone.put(name, end);
 		highscores.put(name, new Highscore(Config.getInt("highscores.max")));
 		maxtime.put(name, time);
 		blockedroutes.put(name, "");
 		disabledroutes.put(name, disabled);
 		String w = r.getWorld();
 		if (regions.containsKey(w)) {
 			regions.get(w).add(r);
 
 		} else {
 			ArrayList<Region> list = new ArrayList<Region>();
 			list.add(r);
 			regions.put(w, list);
 		}
 	}
 	
 	public static synchronized void save() throws IOException{
 		FileWriter fstream = new FileWriter(regionFile);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write("#Region Files " + System.currentTimeMillis() + "\n");
 		StringBuilder sb = new StringBuilder();
 	
 		for (String world : regions.keySet()) {
 			for(Region region:regions.get(world)){
 				sb.append(region.getName() + ":");
 				sb.append(region.getFileString() + ":");
 				sb.append(maxtime.get(region.getName()) + ":");
 				sb.append(disabledroutes.get(region.getName()) + ":");
 				sb.append(region.getWorld() + ":");
 				sb.append(endzone.get(region.getName()).getFileString());
 				sb.append(";\n");
 				saveHighscore(region.getName());
 			}
 		}
 		out.write(sb.toString());
 		out.close();
 	}
 	
 	private static synchronized void saveHighscore(String name) throws IOException{
 		if(!highscores.containsKey(name)){
 			return;
 		}
 		if(!highscoreFolder.exists()){
 			highscoreFolder.mkdir();
 		}
 		File scoreFile = new File(highscoreFolder + File.separator + name);
 		if(!scoreFile.exists()){
 			scoreFile.createNewFile();
 		}
 		FileWriter fstream = new FileWriter(scoreFile);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write("#Highscore Files " + System.currentTimeMillis() + "\n");
 		//test-0+1+2+3+4+5-4000-true-worldname
 		StringBuilder sb = new StringBuilder();
 		for (String[] score: highscores.get(name).getScores()) {
			sb.append(score[Highscore.type_name] + ";" + score[Highscore.type_score] + "\n");
 		}
 		out.write(sb.toString());
 		out.close();
 	}
 	
 	private static synchronized void loadHighscore(String name) throws IOException{
 		if(!highscores.containsKey(name)){
 			return;
 		}
 		FileReader fr = new FileReader(highscoreFolder + File.separator + name);
 		BufferedReader reader = new BufferedReader(fr);
 		String line;
 		Highscore highscore = new Highscore(Config.getInt("highscores.max"));
 		while ((line = reader.readLine()) != null) {
 			if (!line.startsWith("#")) {
 				String args[] = line.split(";");
 				highscore.newScore(args[0], Long.parseLong(args[1]));
 			}
 		}
 		highscores.put(name, highscore);
 		reader.close();
 	}
 	
 	
 	//test-0+1+2+3+4+5-4000-true-worldname
 	private static synchronized void load() throws NumberFormatException, IOException{
 		FileReader fr = new FileReader(regionFile);
 		BufferedReader reader = new BufferedReader(fr);
 		String line;
 		while ((line = reader.readLine()) != null) {
 			if (!line.startsWith("#")) {
 				String[] regions = line.split(";");
 				for (String region : regions) {
 					String args[] = region.split(":");
 					if (args.length >= 6) {
 						String name = args[0];
 						Region r = Region.parse(args[1].split("\\+"));
 						long time = Long.parseLong(args[2]);
 						boolean disabled = Boolean.parseBoolean(args[3]);
 						r.setWorld(args[4]);
 						Region end = Region.parse(args[5].split("\\+"));
 						addRegion(name,r,time,disabled,end);
 						loadHighscore(name);
 					}
 				}
 			}
 		}
 		reader.close();
 	}
 }
