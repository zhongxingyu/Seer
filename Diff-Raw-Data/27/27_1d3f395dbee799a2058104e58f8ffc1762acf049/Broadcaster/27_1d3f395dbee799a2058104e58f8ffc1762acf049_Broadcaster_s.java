 package com.dre.managerxl.broadcaster;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.dre.managerxl.Config;
 import com.dre.managerxl.MPlayer;
 import com.dre.managerxl.P;
 
 public class Broadcaster {
 
 	public static int maxLevel;// = 10;
 	public static int sendsPerLevel;// = 20;
 	public static int minTimeInMinutes;// = 10;
 	public static int maxTimeInMinutes;// = 60;
 	public static String broadcastColor;// = "&2";
 
 	public static String broadcastText;// = "Broadcast";
 	public static String newsText;// = "News";
 	public static String dateText;// = "Date";
 
 	public static String broadcastFolderName;// = "Broadcaster";
 	public static String broadcastDataFileName;
 	public static String broadcastMsgFileName;
 
 	public static ArrayList<String> timeColors = new ArrayList<String>();
 
 	private File broadcastFolder;
 	private File broadcastDataFile;
 	private File broadcastMsgFile;
 
 	public Broadcaster() {
 		loadConfig();
 
 		initializeBroadcaster();
 
 		this.load();
 
 		initBroadcastSchedulers();
 	}
 
 	public void initializeBroadcaster() {
 		broadcastFolder = new File(P.p.getDataFolder(), broadcastFolderName);
 
 		if (!broadcastFolder.exists()) {
 			broadcastFolder.mkdirs();
 		}
 
 		broadcastDataFile = new File(P.p.getDataFolder(), broadcastFolderName + "/" + broadcastDataFileName);
 
 		if (!broadcastDataFile.exists()) {
 			try {
 				broadcastDataFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		broadcastMsgFile = new File(P.p.getDataFolder(), broadcastFolderName + "/" + broadcastMsgFileName);
 
 		if (!broadcastMsgFile.exists()) {
 			try {
 				broadcastMsgFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void initBroadcastSchedulers() {
 		P.p.getServer().getScheduler().scheduleSyncRepeatingTask(P.p, new Runnable() {
 			public void run() {
 				Player[] players = P.p.getServer().getOnlinePlayers();
 
 				for (Integer msgId : BroadcasterMsg.messages.keySet()) {
 					BroadcasterMsg msg = BroadcasterMsg.messages.get(msgId);
 					boolean endThisMsg = false;
 					if (msg.getEndTime() < System.currentTimeMillis()) {
 						endThisMsg = true;
 					}
 					for (Player player : players) {
 						if (endThisMsg) {
 							broadcastMsg(player, msg);
 						} else if (getNextSendTime(player, msg) < System.currentTimeMillis()) {
 							broadcastMsg(player, msg);
 						}
 					}
 					if (endThisMsg) {
 						msg.setDelete();
 					}
 				}
 
 				deleteOldMessages();
 
 				deleteOldPlayerData();
 			}
 		}, 0L, 1200L);
 	}
 
 	private void deleteOldMessages() {
 		for (Iterator<Map.Entry<Integer, BroadcasterMsg>> it = BroadcasterMsg.messages.entrySet().iterator(); it.hasNext();) {
 			Map.Entry<Integer, BroadcasterMsg> entry = it.next();
 			if (entry.getValue().isDelete()) {
 
 				it.remove();
 			}
 		}
 	}
 
 	private void deleteOldPlayerData() {
 		for (MPlayer mPlayer : MPlayer.get()) {
 			for (Iterator<Map.Entry<Integer, BroadcasterPlayerMsg>> it = mPlayer.playerMsgs.entrySet().iterator(); it.hasNext();) {
 				Map.Entry<Integer, BroadcasterPlayerMsg> entry = it.next();
 				if (entry.getValue().getBroadcastMsg() == null) {
 					it.remove();
 					break;
 				}
 			}
 		}
 	}
 
 	public static void broadcastMsg(Player player, BroadcasterMsg msg) {
 
 		MPlayer mPlayer = MPlayer.getOrCreate(player.getName());
 		BroadcasterPlayerMsg bMsg = mPlayer.getBMsg(msg.getId());
 
 		bMsg.setSendCount(bMsg.getSendCount() + 1);
 
 		bMsg.setLastSend(System.currentTimeMillis());
 
 		int newPlayerLevel = (bMsg.getSendCount() / sendsPerLevel) + 1;
 		if (newPlayerLevel > maxLevel) {
 			newPlayerLevel = maxLevel;
 		}
 
 		bMsg.setPlayerLevel(newPlayerLevel);
 
 		if (msg.getType().equalsIgnoreCase("Broadcast")) {
 			player.sendMessage(msg.getMsgAsBroadcast());
 		} else if (msg.getType().equalsIgnoreCase("News")) {
 			player.sendMessage(msg.getMsgAsNews());
 		} else if (msg.getType().equalsIgnoreCase("Date")) {
 			player.sendMessage(msg.getMsgAsDate());
 		}
 
 	}
 
 	public static long getNextSendTime(Player player, BroadcasterMsg msg) {
 		MPlayer mPlayer = MPlayer.getOrCreate(player.getName());
 		BroadcasterPlayerMsg bMsg = mPlayer.getBMsg(msg.getId());
 		long lastSend = bMsg.getLastSend();
 		int playerLevel = bMsg.getPlayerLevel();
 		int timeDiff = maxTimeInMinutes - minTimeInMinutes;
 		double time;
 		if (playerLevel > maxLevel) {
 			time = maxTimeInMinutes;
 		} else if (playerLevel > 0) {
 			time = (double) timeDiff / (double) maxLevel * (double) playerLevel;
 		} else {
 			time = minTimeInMinutes;
 		}
 
 		long milliTime = (long) (time * 60 * 1000);
 
 		return milliTime + lastSend;
 	}
 
 	public void save() {
 		saveMessages();
 		saveData();
 	}
 
 	public void load() {
 		loadMessages();
 		loadData();
 	}
 
 	public void saveMessages() {
 		FileConfiguration file = new YamlConfiguration();
 
 		file.set("idCounterBroadcasterVar", BroadcasterMsg.idCounter);
 
 		for (Integer id : BroadcasterMsg.messages.keySet()) {
 			BroadcasterMsg message = BroadcasterMsg.messages.get(id);
 			file.set(id + ".type", message.getType());
 			file.set(id + ".msg", message.getMsg());
 			file.set(id + ".endTime", message.getEndTime());
 			file.set(id + ".startTime", message.getStartTime());
 		}
 
 		try {
 			file.save(broadcastMsgFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void saveData() {
 		FileConfiguration file = new YamlConfiguration();
 
 		for (MPlayer mPlayer : MPlayer.get()) {
 			for (Integer id : mPlayer.playerMsgs.keySet()) {
 				BroadcasterPlayerMsg bpMsg = mPlayer.playerMsgs.get(id);
 				file.set(mPlayer.getPlayer() + "." + id + ".playerLevel", bpMsg.getPlayerLevel());
 				file.set(mPlayer.getPlayer() + "." + id + ".sendCount", bpMsg.getSendCount());
 				file.set(mPlayer.getPlayer() + "." + id + ".lastSend", bpMsg.getLastSend());
 			}
 		}
 	}
 
 	public void loadMessages() {
 		FileConfiguration file = YamlConfiguration.loadConfiguration(broadcastMsgFile);
 
 		BroadcasterMsg.idCounter = file.getInt("idCounterBroadcasterVar");
 
 		String type;
 		String msg;
 		long endTime;
 		long startTime;
 
 		for (String id : file.getKeys(false)) {
 			if (!id.equalsIgnoreCase("idCounterBroadcasterVar")) {
 				type = file.getString(id + ".type");
 				msg = file.getString(id + ".msg");
 				endTime = file.getLong(id + ".endTime");
 				startTime = file.getLong(id + ".startTime");
 				new BroadcasterMsg(Integer.parseInt(id), type, msg, endTime, startTime);
 			}
 		}
 	}
 
 	public void loadData() {
 		FileConfiguration file = YamlConfiguration.loadConfiguration(broadcastDataFile);
 		MPlayer mPlayer;
 		int playerLevel;
 		int sendCount;
 		long lastSend;
 
 		for (String name : file.getKeys(false)) {
 			mPlayer = MPlayer.getOrCreate(name);
 			ConfigurationSection section = file.getConfigurationSection(name);
 			for (String id : section.getKeys(false)) {
 				playerLevel = file.getInt(name + "." + id + ".playerLevel");
 				sendCount = file.getInt(name + "." + id + ".sendCount");
 				lastSend = file.getInt(name + "." + id + ".lastSend");
 				new BroadcasterPlayerMsg(Integer.parseInt(id), mPlayer, playerLevel, sendCount, lastSend);
 			}
 		}
 	}
 
 	public static void saveDefaultConfig(Config config) {
 		config.saveSingleConfig("Broadcaster.maxLevel", 10);
 		config.saveSingleConfig("Broadcaster.sendsPerLevel", 20);
 		config.saveSingleConfig("Broadcaster.minTimeInMinutes", 10);
 		config.saveSingleConfig("Broadcaster.maxTimeInMinutes", 60);
 
 		config.saveSingleConfig("Broadcaster.broadcastColor", "&2");
 
 		config.saveSingleConfig("Broadcaster.broadcastText", "Broadcast");
 		config.saveSingleConfig("Broadcaster.newsText", "News");
 		config.saveSingleConfig("Broadcaster.dateText", "Event");
 
 		config.saveSingleConfig("Broadcaster.broadcastFolderName", "Broadcaster");
 		config.saveSingleConfig("Broadcaster.broadcastDataFileName", "data.yml");
 		config.saveSingleConfig("Broadcaster.broadcastMsgFileName", "msg.yml");
 
 		ArrayList<String> tColors = new ArrayList<String>();
 
 		tColors.add("&4");
 		tColors.add("&c");
 		tColors.add("&6");
 		tColors.add("&e");
 		tColors.add("&2");
 		tColors.add("&a");
 
 		config.saveSingleConfig("Broadcaster.timeColors", tColors);
 	}
 
 	// cast of List to ArrayList
 	@SuppressWarnings("unchecked")
 	public void loadConfig() {
 		ConfigurationSection broadcasterConfig = P.p.config.getBroadcasterConfigSection();
 
		maxLevel = broadcasterConfig.getInt("Broadcaster.maxLevel");
		sendsPerLevel = broadcasterConfig.getInt("Broadcaster.sendsPerLevel");
		minTimeInMinutes = broadcasterConfig.getInt("Broadcaster.minTimeInMinutes");
		maxTimeInMinutes = broadcasterConfig.getInt("Broadcaster.maxTimeInMinutes");
		broadcastColor = broadcasterConfig.getString("Broadcaster.broadcastColor");
 
		broadcastText = broadcasterConfig.getString("Broadcaster.broadcastText");
		newsText = broadcasterConfig.getString("Broadcaster.newsText");
		dateText = broadcasterConfig.getString("Broadcaster.dateText");
 
		broadcastFolderName = broadcasterConfig.getString("Broadcaster.broadcastFolderName");
		broadcastDataFileName = broadcasterConfig.getString("Broadcaster.broadcastDataFileName");
		broadcastMsgFileName = broadcasterConfig.getString("Broadcaster.broadcastMsgFileName");
 
		timeColors = (ArrayList<String>) broadcasterConfig.getList("Broadcaster.timeColors");
 	}
 }
