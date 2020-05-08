 package ru.ttyh.LorStyleStars;
 
 
 import java.io.BufferedWriter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.util.config.Configuration;
 
 public class LorStyleStarsSystem {
 	private static File file;
 	private static Configuration config;
 	public static boolean changed = false;
 
 	public static void setup() {
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
 			config = new Configuration(file);
 			config.load();
 	}
 
 	public static void disable() {
 		config.save();
 	}
 	
 	public static void reload() {
 		config = new Configuration(file);
 		config.load();
 	}
 	public static void save() {
 		config.save();
 	}
 
 	public static int getScore(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = config.getInt(name + ".score", score);
 		return score;
 	}
 	
 	public static void addScore(String name, String count) {
 		name = name.toLowerCase();
 		int value = Integer.parseInt(count);
 		int score = 40;
 		score = config.getInt(name + ".score", score);
 		score += value;
 		config.setProperty(name + ".score", score);
 		int maxscore = config.getInt(name + ".maxscore", score);
 		if (score > maxscore)
 			config.setProperty(name + ".maxscore", score);
 	}
 
 	public static void setScore(String name, String value) {
 		name = name.toLowerCase();
 		int score = Integer.parseInt(value);
 		config.setProperty(name + ".score", score);
 		int maxscore = config.getInt(name + ".maxscore", score);
 		if (score > maxscore)
 			config.setProperty(name + ".maxscore", score);
 	}
 
 	public static void updScore(String name) {
 		name = name.toLowerCase();
 		String now = new java.text.SimpleDateFormat("dd-MM-yy")
 				.format(java.util.Calendar.getInstance().getTime());
		String then = config.getString(name + ".utime", now);
 		if (now.equals(then)) {
 			return;
 		} else {
 			addScore(name, "1");
 			config.setProperty(name + ".utime", now);
 		}
 
 	}
 
 	public static String scoreToStars(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = config.getInt(name + ".score", score);
 		int maxscore = score;
 		maxscore = config.getInt(name + ".maxscore", maxscore);
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
 	public static int greenStars(String name) {
 		name = name.toLowerCase();
 		int score = 45;
 		score = config.getInt(name + ".score", score);
 		if (score > 500)
 			score = 500;
 		return score/100;
 	}
 
 	
 }
