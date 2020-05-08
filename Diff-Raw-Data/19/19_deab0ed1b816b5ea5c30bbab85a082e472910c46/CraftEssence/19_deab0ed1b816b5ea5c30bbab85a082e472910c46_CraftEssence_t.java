 package me.furt.CraftEssence;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import me.furt.CraftEssence.commands.*;
 import me.furt.CraftEssence.listener.ceEntityListener;
 import me.furt.CraftEssence.listener.cePlayerListener;
 import me.furt.CraftEssence.misc.AFKKickTask;
 import me.furt.CraftEssence.misc.AFKMarkerTask;
 import me.furt.CraftEssence.misc.VoteTask;
 import me.furt.CraftEssence.sql.HomeTable;
 import me.furt.CraftEssence.sql.KitItemsTable;
 import me.furt.CraftEssence.sql.KitTable;
 import me.furt.CraftEssence.sql.MailTable;
 import me.furt.CraftEssence.sql.UserTable;
 import me.furt.CraftEssence.sql.WarpTable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class CraftEssence extends JavaPlugin {
 	public static ArrayList<String> prayList = new ArrayList<String>();
 	public static ArrayList<String> reply = new ArrayList<String>();
 	public static ArrayList<String> homeInvite = new ArrayList<String>();
 	public HashMap<String, Long> users = new HashMap<String, Long>();
 	public String vote = null;
 	public HashMap<String, String> vuser = new HashMap<String, String>();
 	private Timer etimer = new Timer();
 	private AFKKickTask afkKick;
 	private AFKMarkerTask afkMarker;
 	private VoteTask voteTask;
 	public final static String premessage = ChatColor.RED + "[CraftEssence] "
 			+ ChatColor.YELLOW;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public cePlayerListener cepl = new cePlayerListener(this);
 	public ceEntityListener ceel = new ceEntityListener(this);
 	public boolean permEnabled;
 
 	public void onEnable() {
 		registerEvents();
 		checkFiles();
 		setupDatabase();
 		addCommands();
 		if (ceConfig.enableAFK == true) {
 			checkPlayers();
 		}
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName() + "] v" + pdfFile.getVersion()
 				+ " loaded");
 	}
 
 	public void onDisable() {
 		etimer.cancel();
 		etimer = null;
 		afkMarker = null;
 		afkKick = null;
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " Disabled");
 
 	}
 
 	public void startVoteTimer() {
 		voteTask = new VoteTask(this);
		etimer.schedule(voteTask, ceConfig.voteTimer * 1000);
 	}
 
 	private void checkPlayers() {
 		try {
 			afkMarker = new AFKMarkerTask(this);
 			afkKick = new AFKKickTask(this);
			etimer.schedule(afkMarker, 1000, ceConfig.afkTimer * 1000);
			etimer.schedule(afkKick, 2000, ceConfig.kickTimer * 1000);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void addCommands() {
 		getCommand("afk").setExecutor(new AFKCommand(this));
 		getCommand("clearinventory").setExecutor(
 				new ClearInventoryCommand(this));
 		getCommand("broadcast").setExecutor(new BroadcastCommand(this));
 		getCommand("ban").setExecutor(new BanCommand(this));
 		getCommand("compass").setExecutor(new CompassCommand(this));
 		getCommand("give").setExecutor(new GiveCommand(this));
 		getCommand("heal").setExecutor(new HealCommand(this));
 		getCommand("home").setExecutor(new HomeCommand(this));
 		getCommand("item").setExecutor(new ItemCommand(this));
 		getCommand("jump").setExecutor(new JumpCommand(this));
 		getCommand("kick").setExecutor(new KickCommand(this));
 		getCommand("kill").setExecutor(new KillCommand(this));
 		getCommand("kit").setExecutor(new KitCommand(this));
 		getCommand("mail").setExecutor(new MailCommand(this));
 		getCommand("me").setExecutor(new MeCommand(this));
 		getCommand("motd").setExecutor(new MotdCommand(this));
 		getCommand("msg").setExecutor(new MsgCommand(this));
 		getCommand("mute").setExecutor(new MuteCommand(this));
 		getCommand("pardon").setExecutor(new PardonCommand(this));
 		getCommand("playerlist").setExecutor(new PlayerlistCommand(this));
 		getCommand("reply").setExecutor(new ReplyCommand(this));
 		getCommand("sethome").setExecutor(new SetHomeCommand(this));
 		getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
 		getCommand("setwarp").setExecutor(new SetWarpCommand(this));
 		getCommand("spawn").setExecutor(new SpawnCommand(this));
 		getCommand("spawnmob").setExecutor(new SpawnMobCommand(this));
 		getCommand("support").setExecutor(new SupportCommand(this));
 		getCommand("time").setExecutor(new TimeCommand(this));
 		getCommand("top").setExecutor(new TopCommand(this));
 		getCommand("tp").setExecutor(new TpComand(this));
 		getCommand("tphere").setExecutor(new TpHereCommand(this));
 		getCommand("ceuser").setExecutor(new UserCommand(this));
 		getCommand("vote").setExecutor(new VoteCommand(this));
 		getCommand("warp").setExecutor(new WarpCommand(this));
 		getCommand("who").setExecutor(new WhoCommand(this));
 		getCommand("worldlist").setExecutor(new WorldListCommand(this));
 	}
 
 	public boolean isPlayer(CommandSender sender) {
 		if (sender instanceof Player)
 			return true;
 
 		return false;
 	}
 
 	public String message(String[] args) {
 		StringBuilder msg = new StringBuilder();
 		for (String loop : args) {
 			msg.append(loop + " ");
 		}
 		return msg.toString();
 	}
 
 	public Player playerMatch(String name) {
 		if (this.getServer().getOnlinePlayers().length < 1) {
 			return null;
 		}
 
 		Player[] online = this.getServer().getOnlinePlayers();
 		Player lastPlayer = null;
 
 		for (Player player : online) {
 			String playerName = player.getName();
 			String playerDisplayName = player.getDisplayName();
 
 			if (playerName.equalsIgnoreCase(name)) {
 				lastPlayer = player;
 				break;
 			} else if (playerDisplayName.equalsIgnoreCase(name)) {
 				lastPlayer = player;
 				break;
 			}
 
 			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
 				if (lastPlayer != null) {
 					return null;
 				}
 
 				lastPlayer = player;
 			} else if (playerDisplayName.toLowerCase().indexOf(
 					name.toLowerCase()) != -1) {
 				if (lastPlayer != null) {
 					return null;
 				}
 
 				lastPlayer = player;
 			}
 		}
 
 		return lastPlayer;
 	}
 
 	public boolean hasPerm(CommandSender sender, String label) {
		if ((!sender.isOp()) && (sender instanceof Player)) {
			Player p = (Player) sender;
			return p.hasPermission("craftessence." + label);
 		}
		return false;
 	}
 
 	private void checkFiles() {
 		if (!this.getDataFolder().exists())
 			this.getDataFolder().mkdirs();
 
 		if (!new File("plugins" + File.separator + "CraftEssence"
 				+ File.separator + "MobBlackList").isDirectory())
 			new File("plugins" + File.separator + "CraftEssence"
 					+ File.separator + "MobBlackList").mkdir();
 
 		ceConfig.Load(getConfiguration());
 
 		if (!new File(getDataFolder(), "motd.txt").exists()) {
 			this.createMotdConfig();
 			log.info("motd.txt not found, creating.");
 		}
 		if (!new File(getDataFolder(), "bans.txt").exists()) {
 			this.createBansConfig();
 			log.info("bans.txt not found, creating.");
 		}
 	}
 
 	public void createMobBlacklist(String world) {
 		try {
 			new File("plugins" + File.separator + "CraftEssence"
 					+ File.separator + "MobBlackList", world + ".txt")
 					.createNewFile();
 			FileWriter fstream = new FileWriter(new File("plugins"
 					+ File.separator + "CraftEssence" + File.separator
 					+ "MobBlackList", world + ".txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 
 	}
 
 	public void registerEvents() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.cepl,
 				Event.Priority.High, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.cepl,
 				Event.Priority.High, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.ceel,
 				Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.ceel,
 				Event.Priority.Highest, this);
 	}
 
 	private void setupDatabase() {
 		try {
 			File ebeans = new File("ebean.properties");
 			if (!ebeans.exists()) {
 				try {
 					ebeans.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			getDatabase().find(HomeTable.class).findRowCount();
 			getDatabase().find(WarpTable.class).findRowCount();
 			getDatabase().find(MailTable.class).findRowCount();
 			getDatabase().find(KitTable.class).findRowCount();
 			getDatabase().find(KitItemsTable.class).findRowCount();
 			getDatabase().find(UserTable.class).findRowCount();
 			// getDatabase().find(JailTable.class).findRowCount();
 		} catch (PersistenceException ex) {
 			System.out.println("[CraftEssence] Installing database.");
 			installDDL();
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(HomeTable.class);
 		list.add(WarpTable.class);
 		list.add(MailTable.class);
 		list.add(KitTable.class);
 		list.add(KitItemsTable.class);
 		list.add(UserTable.class);
 		// list.add(JailTable.class);
 		return list;
 	}
 
 	public void createMotdConfig() {
 		try {
 			new File(this.getDataFolder(), "motd.txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder(),
 					"motd.txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("4Welcome to our 9Minecraft Server4,f +d4!\n");
 			out.write("4There are +online players online!\n");
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 	}
 
 	public void createBansConfig() {
 		try {
 			new File(this.getDataFolder(), "bans.txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder(),
 					"bans.txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 
 	}
 
 	public List<String> spawnList(Configuration config) {
 		config.load();
 		ArrayList<String> spawns = new ArrayList<String>();
 		List<?> worldSpawn = config.getList("worldSpawn");
 		String spawnString = null;
 		Iterator<?> i$;
 		if ((worldSpawn != null) && (worldSpawn.size() > 0))
 			for (i$ = worldSpawn.iterator(); i$.hasNext();) {
 				Object spawn = i$.next();
 				spawnString = (String) spawn;
 				spawns.add("  " + spawnString);
 			}
 		return spawns;
 	}
 
 	public String[] getBans() {
 		ArrayList<String> banlist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "bans.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				banlist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			log.info("[CraftEssence] Could not get ban list");
 		}
 
 		return banlist.toArray(new String[] {});
 	}
 
 	public String locationToString(Location location) {
 		StringBuilder test = new StringBuilder();
 		test.append(location.getBlockX() + ":");
 		test.append(location.getBlockY() + ":");
 		test.append(location.getBlockZ() + ":");
 		test.append(location.getYaw() + ":");
 		test.append(location.getPitch());
 		return test.toString();
 	}
 
 	public static String string(int i) {
 		return String.valueOf(i);
 	}
 
 	public String argument(String original, String[] arguments, String[] points) {
 		for (int i = 0; i < arguments.length; i++) {
 			if (arguments[i].contains(",")) {
 				for (String arg : arguments[i].split(",")) {
 					original = original.replace(arg, points[i]);
 				}
 			} else {
 				original = original.replace(arguments[i], points[i]);
 			}
 		}
 
 		return original;
 	}
 
 	public String[] getMotd() {
 		ArrayList<String> motd = new ArrayList<String>();
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "motd.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				motd.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 
 		return motd.toArray(new String[] {});
 	}
 
 	public List<String> readMail(Player player) {
 		return readMail(player.getName());
 	}
 
 	public List<String> readMail(String reciever) {
 		ArrayList<String> mailarray = new ArrayList<String>();
 		List<MailTable> mt = this.getDatabase().find(MailTable.class).where()
 				.ieq("reciever", reciever).findList();
 		for (MailTable m : mt) {
 			mailarray.add(m.getSender() + ": " + m.getMessage());
 		}
 		return mailarray;
 	}
 
 	public void sendMail(Player player, String targetPlayer, String message) {
 		MailTable mt = new MailTable();
 		mt.setSender(player.getName());
 		mt.setReciever(targetPlayer);
 		mt.setMessage(message);
 		this.getDatabase().save(mt);
 		player.sendMessage(CraftEssence.premessage + "Mail sent");
 	}
 
 	public void clearMail(Player player) {
 		List<MailTable> mt = this.getDatabase().find(MailTable.class).where()
 				.ieq("reciever", player.getName()).findList();
 		for (MailTable m : mt) {
 			if (m == null)
 				continue;
 
 			this.getDatabase().delete(m);
 		}
 		player.sendMessage(CraftEssence.premessage + "Mail deleted");
 	}
 
 	public boolean hasKitRank(Player player, String kitname) {
 		if (!this.permEnabled) {
 			KitTable kit = this.getDatabase().find(KitTable.class).where()
 					.ieq("name", kitname).findUnique();
 			String userGroup = "";
 			if (userGroup.equalsIgnoreCase(kit.getRank()))
 				return true;
 
 			return false;
 		}
 
 		return true;
 	}
 
 	public ArrayList<String> getKit(Player player, String[] args) {
 		int id = 0;
 		KitTable kit = this.getDatabase().find(KitTable.class).where()
 				.ieq("name", args[0]).findUnique();
 		if (kit != null) {
 			id = kit.getId();
 		} else {
 			player.sendMessage("Kit not found.");
 			return null;
 		}
 		ArrayList<String> itemarray = new ArrayList<String>();
 
 		List<KitItemsTable> kt = this.getDatabase().find(KitItemsTable.class)
 				.where().eq("id", id).findList();
 		for (KitItemsTable k : kt) {
 			itemarray.add(k.getItem() + " " + k.getQuanity());
 		}
 
 		return itemarray;
 	}
 
 	public List<String> kitList(Player player) {
 		return kitList(player.getName());
 	}
 
 	public List<String> kitList(String player) {
 		ArrayList<String> namearray = new ArrayList<String>();
 
 		List<KitTable> kt = this.getDatabase().find(KitTable.class)
 				.select("name").findList();
 		for (KitTable k : kt) {
 			namearray.add(k.getName());
 		}
 
 		return namearray;
 	}
 
 	public String[] getMobs(String world) {
 		ArrayList<String> moblist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "MobBlackList"
 							+ File.separator + world + ".txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				moblist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			log.info("[CraftEssence] Could not get mob blacklist.");
 		}
 
 		return moblist.toArray(new String[] {});
 	}
 
 	public String[] itemList() {
 		ArrayList<String> itemlist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "item.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				itemlist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			log.info("[CraftEssence] Could not get item list.");
 		}
 
 		return itemlist.toArray(new String[] {});
 	}
 
 }
