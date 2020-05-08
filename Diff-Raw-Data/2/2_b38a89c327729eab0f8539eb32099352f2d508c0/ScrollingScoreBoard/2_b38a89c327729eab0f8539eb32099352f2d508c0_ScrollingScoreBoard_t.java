 package de.MiniDigger.ScrollingScoreBoardAnnouncer;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 @SerializableAs("ScrollingScoreBoard")
 public class ScrollingScoreBoard implements ConfigurationSerializable {
 
 	public Scoreboard board;
 	private String name;
 	private int title_length;
 	private int title_delay;
 	private int title_taskid;
 	private int title_index;
 	private String title_msg;
 	private String title_color;
 	private String title_idle_msg;
 	private ArrayList<Integer> slot_lengths;
 	private ArrayList<Integer> slot_delays;
 	private ArrayList<Integer> slot_taskids;
 	private ArrayList<Integer> slot_indexs;
 	private ArrayList<String> slot_msgs;
 	private ArrayList<String> slot_colors;
 	private ArrayList<String> slot_idle_msgs;
 
 	private boolean usePlayerWhitelist;
 	private ArrayList<String> white_listed_players;
 	private ArrayList<String> black_listed_players;
 
 	private boolean useWorldWhitelist;
 	private ArrayList<String> white_listed_worlds;
 	private ArrayList<String> black_listed_worlds;
 
 	private boolean useGroupWhitelist;
 	private ArrayList<String> white_listed_groups;
 	private ArrayList<String> black_listed_groups;
 
 	private Objective ob;
 
 	public ScrollingScoreBoard(String name) {
 		this.name = name;
 		this.title_length = 1;
 		this.title_delay = 4;
 		this.title_taskid = 0;
 		this.title_index = 0;
 		this.title_msg = "Title";
 		this.title_color = ChatColor.RED + "";
 		this.title_idle_msg = "Idle Title";
 		this.slot_lengths = new ArrayList<Integer>();
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_lengths.add(1);
 		this.slot_delays = new ArrayList<>();
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_delays.add(4);
 		this.slot_taskids = new ArrayList<>();
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_taskids.add(0);
 		this.slot_indexs = new ArrayList<>();
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_indexs.add(0);
 		this.slot_msgs = new ArrayList<>();
 		this.slot_msgs.add("msg1");
 		this.slot_msgs.add("msg2");
 		this.slot_msgs.add("msg3");
 		this.slot_msgs.add("msg4");
 		this.slot_msgs.add("msg5");
 		this.slot_msgs.add("msg6");
 		this.slot_msgs.add("msg7");
 		this.slot_msgs.add("msg8");
 		this.slot_msgs.add("msg9");
 		this.slot_msgs.add("msg10");
 		this.slot_colors = new ArrayList<>();
 		this.slot_colors.add(ChatColor.RED.toString());
 		this.slot_colors.add(ChatColor.YELLOW.toString());
 		this.slot_colors.add(ChatColor.RED.toString());
 		this.slot_colors.add(ChatColor.YELLOW.toString());
 		this.slot_colors.add(ChatColor.RED.toString());
 		this.slot_colors.add(ChatColor.YELLOW.toString());
 		this.slot_colors.add(ChatColor.RED.toString());
 		this.slot_colors.add(ChatColor.YELLOW.toString());
 		this.slot_colors.add(ChatColor.RED.toString());
 		this.slot_colors.add(ChatColor.YELLOW.toString());
 		this.slot_idle_msgs = new ArrayList<>();
 		this.slot_idle_msgs.add("idle_msg1");
 		this.slot_idle_msgs.add("idle_msg2");
 		this.slot_idle_msgs.add("idle_msg3");
 		this.slot_idle_msgs.add("idle_msg4");
 		this.slot_idle_msgs.add("idle_msg5");
 		this.slot_idle_msgs.add("idle_msg6");
 		this.slot_idle_msgs.add("idle_msg7");
 		this.slot_idle_msgs.add("idle_msg8");
 		this.slot_idle_msgs.add("idle_msg9");
 		this.slot_idle_msgs.add("idle_msg10");
 
 		white_listed_groups = new ArrayList<>();
 		white_listed_players = new ArrayList<>();
 		white_listed_worlds = new ArrayList<>();
 
 		black_listed_groups = new ArrayList<>();
 		black_listed_players = new ArrayList<>();
 		black_listed_worlds = new ArrayList<>();
 
 		board = Bukkit.getScoreboardManager().getNewScoreboard();
 
 	}
 
 	public Objective init() {
 		// Clearing
 		if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
 			board.getObjective(DisplaySlot.SIDEBAR).unregister();
 		}
 		if (board.getObjective("msg_board") != null) {
 			board.getObjective("msg_board").unregister();
 		}
 		final Objective obj = board.registerNewObjective("msg_board", "dummy");
 		obj.setDisplayName(title_idle_msg);
 		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 		ob = obj;
 		return obj;
 	}
 
 	@Override
 	public Map<String, Object> serialize() {
 		Map<String, Object> result = new LinkedHashMap<>();
 		result.put("name", name);
 
 		result.put("title_length", title_length);
 		result.put("title_delay", title_delay);
 		result.put("title_msg", title_msg);
 		result.put("title_color", title_color);
 		result.put("title_idle_msg", title_idle_msg);
 
 		result.put("slot_lengths", slot_lengths);
 		result.put("slot_delays", slot_delays);
 		result.put("slot_msgs", slot_msgs);
 		result.put("slot_colors", slot_colors);
 		result.put("slot_idle_msgs", slot_idle_msgs);
 
 		result.put("white_listed_players", white_listed_players);
 		result.put("white_listed_worlds", white_listed_worlds);
 		result.put("white_listed_groups", white_listed_groups);
 		result.put("black_listed_players", black_listed_players);
 		result.put("black_listed_worlds", black_listed_worlds);
 		result.put("black_listed_groups", black_listed_groups);
 
 		result.put("usePlayerWhitelist", usePlayerWhitelist);
 		result.put("useWorldWhitelist", useWorldWhitelist);
 		result.put("useGroupWhitelist", useGroupWhitelist);
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static ScrollingScoreBoard deserialize(Map<String, Object> args) {
 		String name = (String) args.get("name");
 		ScrollingScoreBoard ssb = new ScrollingScoreBoard(name);
 
 		ssb.title_length = (int) args.get("title_length");
 		ssb.title_delay = (int) args.get("title_delay");
 		ssb.title_msg = (String) args.get("title_msg");
 		ssb.title_color = (String) args.get("title_color");
 		ssb.title_idle_msg = (String) args.get("title_idle_msg");
 
 		ssb.slot_lengths = (ArrayList<Integer>) args.get("slot_lengths");
 		ssb.slot_delays = (ArrayList<Integer>) args.get("slot_delays");
 		ssb.slot_msgs = (ArrayList<String>) args.get("slot_msgs");
 		ssb.slot_colors = (ArrayList<String>) args.get("slot_colors");
 		ssb.slot_idle_msgs = (ArrayList<String>) args.get("slot_idle_msgs");
 
 		ssb.white_listed_players = (ArrayList<String>) args
 				.get("white_listed_players");
 		ssb.white_listed_worlds = (ArrayList<String>) args
 				.get("white_listed_worlds");
 		ssb.white_listed_groups = (ArrayList<String>) args
 				.get("white_listed_groups");
 		ssb.black_listed_players = (ArrayList<String>) args
 				.get("black_listed_players");
 		ssb.black_listed_worlds = (ArrayList<String>) args
 				.get("black_listed_worlds");
 		ssb.black_listed_groups = (ArrayList<String>) args
 				.get("black_listed_groups");
 
 		ssb.usePlayerWhitelist = (boolean) args.get("usePlayerWhitelist");
 		ssb.useWorldWhitelist = (boolean) args.get("useWorldWhitelist");
		ssb.useGroupWhitelist = (boolean) args.get("useGroupWhitelist");
 		return ssb;
 	}
 
 	public void start(final Objective obj, final int slot) {
 
 		// obj.getScore(Bukkit.getOfflinePlayer("TEST")).setScore(2);
 
 		if (slot == -1) {
 			title_taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
 					ScrollingScoreBoardAnnouncer.getInstance(), new Runnable() {
 
 						@Override
 						public void run() {
 							String msg = title_color + next(title_msg, -1);
 							if (msg.length() <= title_color.length()) {
 								obj.setDisplayName(title_color + title_idle_msg);
 								cancelTask(-1);
 							} else {
 								title_index += title_length;
 								obj.setDisplayName(msg);
 							}
 						}
 					}, 20L * 3, title_delay);
 		} else if (slot >= 0 && slot <= 9) {
 			slot_taskids.set(
 					slot,
 					Bukkit.getScheduler().scheduleSyncRepeatingTask(
 							ScrollingScoreBoardAnnouncer.getInstance(),
 							new Runnable() {
 								private OfflinePlayer player = null;
 
 								@Override
 								public void run() {
 									if (player != null) {
 										board.resetScores(player);
 									}
 									String msg = "";
 									try {
 										msg = slot_colors.get(slot)
 												+ next(slot_msgs.get(slot),
 														slot);
 									} catch (Exception e) {
 										// Main.debug("Ex");
 									}
 									if (msg.length() <= slot_colors.get(slot)
 											.length()) {
 										player = Bukkit
 												.getOfflinePlayer(slot_idle_msgs
 														.get(slot));
 										obj.getScore(player).setScore(slot + 1);
 										cancelTask(slot);
 									} else {
 										slot_indexs.set(
 												slot,
 												slot_indexs.get(slot)
 														+ slot_lengths
 																.get(slot));
 										player = Bukkit.getOfflinePlayer(msg);
 										obj.getScore(player).setScore(slot + 1);
 									}
 								}
 							}, 20L * 3, slot_delays.get(slot)));
 		} else {
 			ScrollingScoreBoardAnnouncer.debug("Wrong slot number! (" + slot
 					+ ")");
 		}
 	}
 
 	private String next(String s, int slot) {
 		if (slot == -1) {
 			if (s.length() <= 32) {
 				return s;
 			}
 			return s.substring(
 					title_index,
 					Math.min(title_index + 32 - title_color.length(),
 							s.length() - title_color.length()));
 		} else if (slot >= 0 && slot <= 9) {
 			if (s.length() <= 16) {
 				return s;
 			}
 			return s.substring(
 					slot_indexs.get(slot),
 					Math.min(slot_indexs.get(slot) + 16
 							- slot_colors.get(slot).length(), s.length()
 							- slot_colors.get(slot).length()));
 		} else {
 			ScrollingScoreBoardAnnouncer.debug("Wrong slot number! (" + slot
 					+ ")");
 			return "FAILED";
 		}
 	}
 
 	public void annonce(String msg, int slot) {
 		cancelTask(slot);
 		if (slot == -1) {
 			title_msg = msg;
 			start(ob, -1);
 		} else if (slot >= 0 && slot <= 9) {
 			slot_msgs.set(slot, msg);
 			start(ob, slot);
 		} else {
 			ScrollingScoreBoardAnnouncer.debug("Wrong slot number! (" + slot
 					+ ")");
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void cancelTask(int slot) {
 		if (slot == -1) {
 			Bukkit.getScheduler().cancelTask(title_taskid);
 			title_taskid = 0;
 			title_index = 0;
 		} else if (slot >= 0 && slot <= 9) {
 			Bukkit.getScheduler().cancelTask(slot_taskids.get(slot));
 			slot_taskids.set(slot, 0);
 			slot_indexs.set(slot, 0);
 		} else {
 			ScrollingScoreBoardAnnouncer.debug("Wrong slot number! (" + slot
 					+ ")");
 		}
 
 	}
 
 	public void cancelAllTasks() {
 		cancelTask(-1);
 		cancelTask(0);
 		cancelTask(1);
 		cancelTask(2);
 		cancelTask(3);
 		cancelTask(4);
 		cancelTask(5);
 		cancelTask(6);
 		cancelTask(7);
 		cancelTask(8);
 		cancelTask(9);
 	}
 
 	public void startAll() {
 		start(ob, -1);
 		start(ob, 0);
 		start(ob, 1);
 		start(ob, 2);
 		start(ob, 3);
 		start(ob, 4);
 		start(ob, 5);
 		start(ob, 6);
 		start(ob, 7);
 		start(ob, 8);
 		start(ob, 9);
 	}
 
 	public boolean isPlayerWhiteListed(String name) {
 		return white_listed_players.contains(name);
 	}
 
 	public boolean isWorldWhiteListed(String name) {
 		return white_listed_worlds.contains(name);
 	}
 
 	public boolean isGroupWhiteListed(String name) {
 		return white_listed_groups.contains(name);
 	}
 
 	public boolean isPlayerBlackListed(String name) {
 		return black_listed_players.contains(name);
 	}
 
 	public boolean isWorldBlackListed(String name) {
 		return black_listed_worlds.contains(name);
 	}
 
 	public boolean isGroupBlackListed(String name) {
 		return black_listed_groups.contains(name);
 	}
 
 	public boolean usePlayerWhiteList() {
 		return usePlayerWhitelist;
 	}
 
 	public boolean useWorldWhiteList() {
 		return useWorldWhitelist;
 	}
 
 	public boolean useGroupWhiteList() {
 		return useGroupWhitelist;
 	}
 
 }
