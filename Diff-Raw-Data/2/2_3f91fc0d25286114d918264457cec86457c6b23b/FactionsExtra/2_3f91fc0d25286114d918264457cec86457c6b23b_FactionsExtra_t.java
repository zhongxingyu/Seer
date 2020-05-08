 package com.mutinycraft.jigsaw.FactionsExtra;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.massivecraft.factions.Factions;
 import com.massivecraft.factions.P;
 
 public class FactionsExtra extends JavaPlugin implements Listener {
 
 	Logger log;
 	File factionFile;
 	FileConfiguration factionData;
 
 	private FactionsExtraCommandExecutor cmdExecutor;
 	private String topFactions;
 	private static final String VERSION = " v1.4";
 
 	/***************** Enable *****************/
 
 	@Override
 	public void onEnable() {
 		factionData = new YamlConfiguration();
 		cmdExecutor = new FactionsExtraCommandExecutor(this);
 		log = this.getLogger();
 
 		try {
 			factionFile = new File(getDataFolder(), "FactionScores.yml");
 			firstRun();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		getServer().getPluginManager().registerEvents(this, this);
 		new FactionsExtraEventHandler(this);
 
 		if (Bukkit.getServer().getPluginManager().getPlugin("TagAPI") != null) {
 			new FactionsExtraTagEventHandler(this);
			log.info("Successfully hooked into TagAPI.");
 		} else {
 			log.info("TagAPI was not found!");
 			log.info("Disabling support for colored tags!");
 		}
 
 		loadYamls();
 		setTopFactions();
 		getCommand("factionscore").setExecutor(cmdExecutor);
 		getCommand("factiontier").setExecutor(cmdExecutor);
 		getCommand("factiontop").setExecutor(cmdExecutor);
 		getCommand("factionreset").setExecutor(cmdExecutor);
 
 		new PlayerDataTask(this.getDataFolder())
 				.runTaskTimer(this, 1200, 72000);
 
 		log.info(this.getName() + VERSION + " enabled!");
 	}
 
 	private void firstRun() throws Exception {
 		if (!factionFile.exists()) {
 			factionFile.getParentFile().mkdirs();
 			copy(getResource("FactionScores.yml"), factionFile);
 			// Get Factions
 			getFactionsFromFile();
 		}
 	}
 
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream fout = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				fout.write(buf, 0, len);
 			}
 			fout.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadYamls() {
 		try {
 			factionData.load(factionFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/***************** Config Handling *****************/
 
 	public FileConfiguration getCustomConfig() {
 		if (factionData == null) {
 			this.reloadCustomConfig();
 		}
 		return factionData;
 	}
 
 	public void reloadCustomConfig() {
 		if (factionFile == null) {
 			factionFile = new File(getDataFolder(), "customConfig.yml");
 		}
 		factionData = YamlConfiguration.loadConfiguration(factionFile);
 
 		InputStream defConfigStream = this.getResource("customConfig.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration
 					.loadConfiguration(defConfigStream);
 			factionData.setDefaults(defConfig);
 		}
 	}
 
 	/***************** Score Handling *****************/
 
 	public List<Integer> getFactionData(String factionID) {
 		if (factionData.contains(factionID)) {
 			return factionData.getIntegerList(factionID);
 		} else {
 			addFaction(factionID);
 			return factionData.getIntegerList(factionID);
 		}
 	}
 
 	public int getFactionTier(String factionID) {
 		List<Integer> data = factionData.getIntegerList(factionID);
 		if (data != null && data.size() >= 2) {
 			return data.get(1);
 		}
 		return 0;
 	}
 
 	public int getFactionScore(String factionID) {
 		List<Integer> data = factionData.getIntegerList(factionID);
 		if (data != null && data.size() >= 2) {
 			return data.get(0);
 		}
 		return 0;
 	}
 
 	public void addFaction(String factionID) {
 		if (!factionData.contains(factionID)) {
 			List<Integer> data = new ArrayList<Integer>(1);
 			// Default score of 0
 			data.add(0);
 			// Default tier of 0
 			data.add(0);
 			updateFaction(factionID, data);
 		}
 	}
 
 	public void updateFaction(String factionID, List<Integer> data) {
 
 		getCustomConfig().set(factionID, data);
 
 		if (factionData == null || factionFile == null) {
 			return;
 		}
 		try {
 			getCustomConfig().save(factionFile);
 		} catch (IOException ex) {
 			this.getLogger().log(Level.SEVERE,
 					"Could not save data to " + factionFile, ex);
 		}
 
 		// If a faction is updated we need to update the top factions.
 		setTopFactions();
 	}
 
 	public void setTopFactions() {
 		Vector<Faction> factions = new Vector<Faction>(50);
 		Set<String> factionTags = P.p.getFactionTags();
 		StringBuilder top = new StringBuilder();
 
 		Iterator<String> iter = factionTags.iterator();
 		while (iter.hasNext()) {
 			String factionName = iter.next();
 			String factionID = Factions.i.getByTag(factionName).getId();
 			if (getFactionTier(factionID) != 0) {
 				factions.add(new Faction(factionName,
 						getFactionScore(factionID), getFactionTier(factionID)));
 			}
 		}
 		Collections.sort(factions);
 		// Get the top 10 and put them into a string.
 		for (int i = 0; (i < 10 && i < factions.size()); i++) {
 			top.append(ChatColor.GREEN);
 			top.append(i + 1);
 			top.append(". Faction: ");
 			top.append(ChatColor.RED);
 			top.append(factions.get(i).getFactionName());
 			top.append(ChatColor.GREEN);
 			top.append(" Score: ");
 			top.append(ChatColor.RED);
 			top.append(factions.get(i).getScore());
 			top.append(ChatColor.GREEN);
 			top.append(" Tier: ");
 			top.append(ChatColor.RED);
 			top.append(factions.get(i).getTier());
 			top.append("\n");
 		}
 
 		this.topFactions = top.toString();
 	}
 
 	public String getTopFactions() {
 		if (topFactions.length() == 0) {
 			return "There are no Factions with a score greater than 0";
 		}
 		return this.topFactions;
 	}
 
 	public void resetAllFactions() {
 		Set<String> factions = P.p.getFactionTags();
 		Iterator<String> iter = factions.iterator();
 		while (iter.hasNext()) {
 			String factionName = iter.next();
 			List<Integer> data = new ArrayList<Integer>(1);
 			// Default score of 0
 			data.add(0);
 			// Default tier of 0
 			data.add(0);
 			updateFaction(Factions.i.getByTag(factionName).getId(), data);
 		}
 	}
 
 	// This should only be called one time when the plugin is first ran.
 	public void getFactionsFromFile() {
 		Set<String> factions = P.p.getFactionTags();
 		Iterator<String> iter = factions.iterator();
 		while (iter.hasNext()) {
 			String factionName = iter.next();
 			List<Integer> data = new ArrayList<Integer>(1);
 			// Default score of 0
 			data.add(0);
 			// Default tier of 0
 			data.add(0);
 			updateFaction(Factions.i.getByTag(factionName).getId(), data);
 		}
 	}
 
 	/***************** Disable *****************/
 	public void onDisable() {
 		log.info(this.getName() + VERSION + " disabled!");
 	}
 }
