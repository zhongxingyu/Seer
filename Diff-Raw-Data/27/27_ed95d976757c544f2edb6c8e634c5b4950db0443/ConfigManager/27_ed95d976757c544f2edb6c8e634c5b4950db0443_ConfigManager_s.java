 package de.xcraft.INemesisI.Tickets.Manager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import de.xcraft.INemesisI.Library.Manager.XcraftConfigManager;
 import de.xcraft.INemesisI.Library.Message.Messenger;
 import de.xcraft.INemesisI.Tickets.Log;
 import de.xcraft.INemesisI.Tickets.Log.EntryType;
 import de.xcraft.INemesisI.Tickets.Ticket;
 import de.xcraft.INemesisI.Tickets.XcraftTickets;
 
 public class ConfigManager extends XcraftConfigManager {
 
 	TicketManager tmanager;
 	File ticketFolder;
 	File archiveFolder;
 	File remFile;
 	FileConfiguration reminder;
 
 	public ConfigManager(XcraftTickets plugin) {
 		super(plugin);
 		tmanager = plugin.getPluginManager();
 		tmanager.cManager = this;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void load() {
 		File folder = plugin.getDataFolder();
 		ticketFolder = new File(folder, "tickets");
 		archiveFolder = new File(folder, "archive");
 		if (!ticketFolder.exists()) {
 			ticketFolder.mkdir();
 		}
 		if (!archiveFolder.exists()) {
 			archiveFolder.mkdir();
 		}
 		remFile = new File(folder, "reminder.yml");
 		reminder = YamlConfiguration.loadConfiguration(remFile);
 		// load tickets
 		List<Ticket> tickets = new ArrayList<Ticket>();
 		File[] files = ticketFolder.listFiles();
 		if (files != null) {
 			Arrays.sort(files);
 			for (File file : files) {
 				Ticket ticket = loadTicket(file);
 				if (ticket != null) {
 					tickets.add(ticket);
 				} else {
 					file.delete();
 				}
 			}
 		}
 		tmanager.setTickets(tickets);
 		// load cfg
 		tmanager.setNextID(config.getInt("Next_Ticket_ID", 1));
 		tmanager.getDate().applyPattern(config.getString("DateFormat", "dd.MM HH:mm"));
 		tmanager.setAssignees((List<String>) config.getList("Assignee"));
 		Map<String, String> phrases = new HashMap<String, String>();
 		if (config.isConfigurationSection("Phrases")) {
 			ConfigurationSection cs = config.getConfigurationSection("Phrases");
 			for (String key : cs.getKeys(false)) {
 				phrases.put(key, cs.getString(key));
 			}
 		}
 		tmanager.setPhrases(phrases);
 	}
 
 	@Override
 	public void save() {
 		config.set("Next_Ticket_ID", tmanager.getNextID());
 		config.set("Assignee", tmanager.getAssignees());
 		config.set("Phrases", tmanager.getPhrases());
 		plugin.saveConfig();
 		try {
 			reminder.save(remFile);
 		} catch (IOException e) {
 		}
 		for (Ticket ticket : tmanager.getTickets()) {
 			this.saveTicket(ticketFolder, ticket);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public Ticket loadTicket(File ticket) {
 		String filename = ticket.getAbsoluteFile().getName().replace(".yml", "");
 		if (!filename.matches("\\d*"))
 			return null;
 		int id = Integer.parseInt(filename);
 		FileConfiguration temp = YamlConfiguration.loadConfiguration(ticket);
 		ConfigurationSection cs = temp.getConfigurationSection("Ticket");
 		Log log = new Log(tmanager.getDate());
 		List<String> list = (List<String>) cs.getList("log");
 		if ((list != null) && !list.isEmpty()) {
 			for (int i = 0; i < list.size(); i++) {
 				String split[] = list.get(i).split("; ");
 				long time = split[0].matches("\\d*") ? Long.valueOf(split[0]) : 0;
 				log.add(time, EntryType.valueOf(split[2]), split[1], split.length >= 4 ? split[3] : "");
 			}
 		} else {
 			Messenger.warning("[" + plugin.getName() + "]" + " Ticket " + id + " could not be loaded: missing log!");
 			return null;
 		}
 		List<String> watched = (ArrayList<String>) cs.getList("watched");
 		if (watched == null) {
 			watched = new ArrayList<String>();
 		}
 		String assignee = cs.getString("assignee");
 		if ((assignee != null) && assignee.equals("none")) {
 			assignee = null;
 		}
 		long processed = 0;
 		if (cs.isLong("processed")) {
 			processed = cs.getLong("processed");
 		} else {
 			processed = new Date().getTime();
 		}
 		cs = temp.getConfigurationSection("Ticket.location");
 		Location loc = null;
 		String world = null;
 		if (cs != null) {
 			world = cs.getString("world");
 			World w = plugin.getServer().getWorld(world);
 			loc = new Location(w, cs.getLong("x"), cs.getLong("y"), cs.getLong("z"), cs.getLong("pitch"), cs.getLong("yaw"));
 		}
 		return new Ticket(id, assignee, loc, world, processed, watched, log);
 	}
 
 	public void saveTicket(File folder, Ticket ticket) {
 		File file = new File(folder, ticket.getId() + ".yml");
 		FileConfiguration temp = YamlConfiguration.loadConfiguration(file);
 		temp.set("Ticket.assignee", ticket.getAssignee());
 		List<String> list = new ArrayList<String>();
 		for (int i = 0; i < ticket.getLog().size(); i++) {
 			list.add(ticket.getLog().getEntry(i).toString());
 		}
 		temp.set("Ticket.processed", ticket.getProcessed());
 		temp.set("Ticket.log", list);
 		temp.set("Ticket.watched", ticket.getWatched());
 		Location loc = ticket.getLoc();
		if (loc.getWorld() != null) {
 			temp.set("Ticket.location.x", loc.getX());
 			temp.set("Ticket.location.y", loc.getY());
 			temp.set("Ticket.location.z", loc.getZ());
 			temp.set("Ticket.location.pitch", loc.getPitch());
 			temp.set("Ticket.location.yaw", loc.getYaw());
 			temp.set("Ticket.location.world", ticket.getWorld());
 		}
 		try {
 			temp.save(file);
 		} catch (IOException e) {
 		}
 	}
 
 	public void archiveTicket(Ticket ticket) {
 		this.saveTicket(archiveFolder, ticket);
 		this.deleteTicket(ticket);
 	}
 
 	public void deleteTicket(Ticket ticket) {
 		File file = new File(ticketFolder, ticket.getId() + ".yml");
 		file.delete();
 	}
 
 	public Map<String, Integer> getStats() {
 		File statsFile = new File(plugin.getDataFolder(), "stats.yml");
 		FileConfiguration data = null;
 		Map<String, Integer> stats = new HashMap<String, Integer>();
 		int lastcheck = 0;
 		// if we have no stats, create the file
 		if (!statsFile.exists()) {
 			try {
 				statsFile.createNewFile();
 				data = YamlConfiguration.loadConfiguration(statsFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			// else getstats from last check
 		} else {
 			data = YamlConfiguration.loadConfiguration(statsFile);
 			if (data.isConfigurationSection("Stats")) {
 				ConfigurationSection cs = data.getConfigurationSection("Stats");
 				for (String key : cs.getKeys(false)) {
 					stats.put(key, cs.getInt(key));
 				}
 			}
 			lastcheck = data.getInt("LastTicket");
 		}
 		int newcheck = lastcheck;
 		// check for tickets, we havent analyzed
 		for (File file : archiveFolder.listFiles()) {
 			int number = Integer.parseInt(file.getName().replace(".yml", ""));
 			if (number <= lastcheck) {
 				continue;
 			}
 			if (number > newcheck) {
 				newcheck = number;
 			}
 			// analyze them
 			try {
 				FileInputStream fis = new FileInputStream(file);
 				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
 				BufferedReader br = new BufferedReader(isr);
 				// Check for BOM character.
 				br.mark(1);
 				int bom = br.read();
 				if (bom != 65279) {
 					br.reset();
 				}
 				String s;
 				while ((s = br.readLine()) != null) {
 					if (s.contains("CLOSE")) {
 						String[] split = s.split("; ");
 						if (stats.containsKey(split[1])) {
 							stats.put(split[1], stats.get(split[1]) + 1);
 						} else {
 							stats.put(split[1], 1);
 						}
 						break;
 					}
 				}
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		// save new stats
 		data.set("LastTicket", newcheck);
 		for (String key : stats.keySet()) {
 			data.set("Stats." + key, stats.get(key));
 		}
 		try {
 			data.save(statsFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return stats;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void addReminder(String player, int id) {
 		List<String> list = (List<String>) reminder.getList(player);
 		if (list == null) {
 			list = new ArrayList<String>();
 		}
 		list.add(String.valueOf(id));
 		reminder.set(player, list);
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean removeReminder(String player, int id) {
 		String sid = String.valueOf(id);
 		List<String> list = (List<String>) reminder.getList(player);
 		if (list != null) {
 			if ((list.remove(sid) == true)) {
 				if (list.size() == 0) {
 					reminder.set(player, null);
 				}
 				return true;
 			} else
 				return false;
 		} else
			return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getReminder(String player) {
 		return (List<String>) reminder.getList(player);
 	}
 
 }
