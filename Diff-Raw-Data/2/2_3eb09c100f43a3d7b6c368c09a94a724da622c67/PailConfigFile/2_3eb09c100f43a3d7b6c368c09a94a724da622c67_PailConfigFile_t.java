 package net.eonz.bukkit.psduo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PailConfigFile {
 
 	private final PailStone main;
 
 	private File cfg;
 
 	ArrayList<PCfgKey> keys = new ArrayList<PCfgKey>();
 
 	public PailConfigFile(PailStone main, File cfg) {
 		this.main = main;
 		this.cfg = cfg;
 	}
 
 	public void load() {
 		keys.clear();
 
 		if (cfg.isDirectory()) {
 			cfg = new File(cfg.getAbsolutePath() + "config.txt");
 		}
 
 		if (!cfg.exists()) {
 			try {
 				cfg.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				this.main.e("Error while creating config file.");
 			}
 		} else {
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(cfg));
 				String line;
 				while ((line = in.readLine()) != null) {
 					line = line.trim();
 					if (line.charAt(0) != '#') {
 						if (line.contains("=")) {
 							String[] args = line.split("=");
 							String key = args[0].trim();
							String value = (args.length >= 2)?args[1].trim():"";
 							keys.add(new PCfgKey(key, value));
 						}
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				this.main.e("Error while updating config file.");
 			}
 		}
 	}
 
 	public void save() {
 		boolean changes = false;
 		for (int i = 0; i < keys.size(); i++) {
 			if (keys.get(i).isChanged()) {
 				changes = true;
 			}
 		}
 		if (changes) {
 			try {
 				PrintWriter out = new PrintWriter(new FileOutputStream(cfg));
 				for (int i = 0; i < keys.size(); i++) {
 					String line = "";
 					PCfgKey cc = keys.get(i);
 					line += cc.key;
 					line += " = ";
 					line += cc.value;
 					out.write(line + "\n");
 				}
 				out.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 				this.main.e("Error while updating config file.");
 			}
 		}
 	}
 
 	public void announce() {
 		for (int i = 0; i < keys.size(); i++) {
 			PCfgKey k = keys.get(i);
 			this.main.c(k.getKey() + " is set to '" + k.getValue() + "'");
 		}
 	}
 
 	public String getString(String key, String defaultValue) {
 		for (int i = 0; i < keys.size(); i++) {
 			PCfgKey k = keys.get(i);
 			if (key.equals(k.getKey())) {
 				return k.getValue();
 			}
 		}
 		PCfgKey nk = new PCfgKey(key, defaultValue);
 		nk.dirty();
 		keys.add(nk);
 		return nk.getValue();
 	}
 
 	public boolean getBoolean(String key, boolean defaultValue) {
 		String val = this.getString(key, Boolean.toString(defaultValue));
 		return Boolean.parseBoolean(val);
 	}
 
 	public List<Integer> getIntegerList(String key, List<Integer> defaultValue) {
 		String valString = this.getString(key, "");
 		if (valString == null || valString.equals(""))
 			return defaultValue;
 		List<Integer> val = new ArrayList<Integer>();
 		String[] split = valString.replaceAll(" ", "").split(",");
 		for (String s : split) {
 			val.add(new Integer(s));
 		}
 		return val;
 	}
 
 	public int getInt(String key, int defaultValue) {
 		String val = this.getString(key, Integer.toString(defaultValue));
 		return Integer.parseInt(val);
 	}
 
 	private static class PCfgKey {
 		private final String key;
 		private String value;
 
 		private boolean changed;
 
 		public PCfgKey(String key, String value) {
 			this.key = key;
 			this.value = value;
 			changed = false;
 		}
 
 		public void dirty() {
 			changed = true;
 		}
 
 		public String getValue() {
 			return value;
 		}
 
 		public String getKey() {
 			return key;
 		}
 
 		public boolean isChanged() {
 			return changed;
 		}
 	}
 
 }
