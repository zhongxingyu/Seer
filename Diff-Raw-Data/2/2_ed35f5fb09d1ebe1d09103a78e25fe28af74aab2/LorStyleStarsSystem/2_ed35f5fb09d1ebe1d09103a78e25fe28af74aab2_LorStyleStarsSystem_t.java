 package ru.ttyh.LorStyleStars;
 
 
 import java.io.BufferedWriter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 public class LorStyleStarsSystem {
 	private File file;
 	private Configuration base;
 
 	public void setup() {
 		new File("plugins/LorStyleStars/").mkdir();
 		try {
 			new File("plugins/LorStyleStars/players.yml").createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		file = new File("plugins/LorStyleStars/players.yml");
 		if (!file.exists()) {
 			BufferedWriter out = null;
 			try {
 				out = new BufferedWriter(new FileWriter(file));
 				out.write("");
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (out != null)
 						out.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 			base = new Configuration(file);
 			base.load();
 	}
 
 	public void disable() {
 		base.save();
 	}
 	
 	public void reload() {
 		base = new Configuration(file);
 		base.load();
 	}
 	public void save() {
 		base.save();
 	}
 
 	public int getScore(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = base.getInt(name + ".score", score);
 		return score;
 	}
 	
 	public void addScore(String name, String count) {
 		name = name.toLowerCase();
 		int value = Integer.parseInt(count);
 		int score = 40;
 		score = base.getInt(name + ".score", score);
 		score += value;
 		base.setProperty(name + ".score", score);
 		int maxscore = base.getInt(name + ".maxscore", score);
 		if (score > maxscore)
 			base.setProperty(name + ".maxscore", score);
 	}
 
 	public void setScore(String name, String value) {
 		name = name.toLowerCase();
 		int score = Integer.parseInt(value);
 		base.setProperty(name + ".score", score);
 		int maxscore = base.getInt(name + ".maxscore", score);
 		if (score > maxscore)
 			base.setProperty(name + ".maxscore", score);
 	}
 
 	public void updScore(String name) {
 		name = name.toLowerCase();
 		String now = new java.text.SimpleDateFormat("dd-MM-yy")
 				.format(java.util.Calendar.getInstance().getTime());
 		String then = base.getString(name + ".utime", "never");
 		if (now.equals(then)) {
 			return;
 		} else {
 			addScore(name, "1");
 			base.setProperty(name + ".utime", now);
 		}
 
 	}
 
 	public String scoreToStars(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = base.getInt(name + ".score", score);
 		int maxscore = score;
 		maxscore = base.getInt(name + ".maxscore", maxscore);
 		int green = 0, grey = 0;
 		if (maxscore > 500)
 			maxscore = 500;
 		if (score > 500)
 			score = 500;
 		green = score/100;
 		grey = maxscore/100 - green;
 		if ( green == 0 && grey == 0)
 			return "";
 		String out = " " + ChatColor.GREEN;
 		while (green != 0) {
 			out += "*";
 			green--;
 		}
 		out += ChatColor.GRAY;
 		while (grey != 0) {
 			out += "*";
 			grey--;
 		}
 		out += ChatColor.WHITE;
 		return out;
 	}
 	public static String stars(String name) {
 		return LorStyleStars.system.scoreToStars(name);
 	}
 	public int greenStars(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = base.getInt(name + ".score", score);
 		if (score > 500)
 			score = 500;
 		return score/100;
 	}
 	public void heal(long count) {
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if ( p.getHealth() >= 20 ) 
 				continue;
 			if (greenStars(p.getName()) != 0 ) {
 				if (count % (6 - greenStars(p.getName())) == 0) 
 					p.setHealth(p.getHealth() + 1);
 			}
 		}
 	}
 
 
 	
 }
