 package darvin939.DarkDays.Configuration;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import lib.PatPeter.SQLibrary.Database;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import darvin939.DarkDays.DarkDays;
 import darvin939.DarkDays.SQL.DBInit;
 import darvin939.DarkDays.SQL.Chests.ChestManager;
 import darvin939.DarkDays.SQL.Chests.SQLChest;
 import darvin939.DarkDays.SQL.Players.PlayerManager;
 import darvin939.DarkDays.SQL.Players.SQLPlayer;
 import darvin939.DarkDays.Utils.FGUtilCore;
 
 public class Config extends FGUtilCore {
 	public static FGUtilCore FGU;
 	private static PlayerConfig playerCfg;
 	private static ChestConfig chestCfg;
 	private static RegionConfig regionCfg;
 	private static LootConfig lootCfg;
 	private static SpawnConfig spawnCfg;
 
 	public Config(DarkDays plg, boolean vcheck, String lng, String devbukkitname, String px) {
 		super(plg, vcheck, lng, devbukkitname, px);
 		setupMessages();
 		SaveMSG();
 		FGU = this;
 	}
 
 	public static enum Nodes {
 		language("Language", "english"), verCheck("Version-check", true), prefix("Prefix", "DarkDays"), only_zombies("Zombie.OnlyZombies", true), zombie_speed("Zombie.Speed", 0.4), zombie_smoothness("Zombie.Smoothness", 15), attack_strength("Zombie.AttackStrength", 4), zombie_health(
 				"Zombie.Health", 24), zombie_pickup("Zombie.PickUpPlayerArmor", true), thirst_speed("Thirst.Speed", 2), bandage_id("Bandages.Id", 339), bandage_health("Bandages.Restore", 8), chest_empty("Chest.IfOnlyEmpty", true), chest_regen("Chest.RegenTime", 2), chest_disappear(
 				"Chest.Disappear", true), wand_item("WandItem", 369), control_sitemst("ControlSItems", true), coloured_tegs("ColouredTags", true), enable_regions("EnableRegions", false), MYSQL_USER("MySQL.Username", "root"), MYSQL_PASS("MySQL.Password", "root"), MYSQL_HOST("MySQL.Hostname",
 				"localhost"), MYSQL_PORT("MySQL.Port", 3306), MYSQL_DATABASE("MySQL.Database", "darkdays"), MYSQL_DBWRAPPER("MySQL.DataWrapper", "none"), disable_health_regen("DisableHealthRegen", false);
 
 		String node;
 		Object value;
 
 		private Nodes(String node, Object value) {
 			this.node = node;
 			this.value = value;
 		}
 
 		public String getNode() {
 			return node;
 		}
 
 		public Object getValue() {
 			return this.value;
 		}
 
 		public Boolean getBoolean() {
 			return (Boolean) value;
 		}
 
 		public Integer getInteger() {
 			if (value instanceof Double)
 				return ((Double) value).intValue();
 
 			return (Integer) value;
 		}
 
 		public Double getDouble() {
 			if (value instanceof Integer)
 				return (double) ((Integer) value).intValue();
 
 			return (Double) value;
 		}
 
 		public String getString() {
 			return (String) value;
 		}
 
 		public Long getLong() {
 			if (value instanceof Integer)
 				return ((Integer) value).longValue();
 
 			return (Long) value;
 		}
 
 		public void setValue(Object value) {
 			this.value = value;
 		}
 
 		public String toString() {
 			return String.valueOf(value);
 		}
 	}
 
 	private void setupMessages() {
 		// loot_
 		addMSG("loot_set", "LootID is set to %1%");
 		addMSG("loot_error", "Error occurred with assigning LootID");
 		addMSG("loot_nf", "LootID not found!");
 		addMSG("loot_flag_potion_isEmpty", "Usage: &2..flag potion &7<spawn=[%,minCount-maxCount]> <effect=[type,minlvl-maxlvl,splash,extend]>");
 		addMSG("loot_flag_item_isEmpty", "Usage: &2..flag &7[id] <spawn=[%,minCount-maxCount]> <effect=[type,minlvl-maxlvl,%]>");
 		addMSG("loot_save", "Loot %1% successfully saved");
 		addMSG("loot_remove", "Loot %1% successfully removed");
 		addMSG("loot_new", "A new loot &7%1%&f created. Type &2/dd loot save %2%&f to save this loot");
 		addMSG("loot_new_isempty", "First add the new loot! Type &2/dd loot new &7[name]&f to create new loot");
 		// chest_
 		addMSG("chest_normal", "This is a normal chest");
 		addMSG("chest_newWithID", "Created new looted chest with LootID");
 		addMSG("chest_new", "Created new looted chest");
 		addMSG("chest_remove", "Deleted loot chest");
 		addMSG("chest_cantDestroy", "You can't destroy the loot chest!");
 		// cmd_
 		addMSG("cmd_indev", "This command in the process of developing");
 		addMSG("cmd_occurred", "Error occurred when trying the command /%1%");
 		addMSG("cmd_noperm", "You don't have the Permission(%1%) to do that!");
 		// game_
 		addMSG("game_start", "You are in the world. Good luck!");
 		addMSG("game_alrady", "You alrady spawned!");
 		addMSG("game_need_water", "Need more water");
 		addMSG("game_noplay", "You are not in the world. Type the %1% to start playing");
 		// spawn_
 		addMSG("spawn_lobby_new", "New lobby location created");
 		addMSG("spawn_lobby_error", "Failed to create location of lobby");
 		addMSG("spawn_new", "Added new spawn point");
 		addMSG("spawn_error", "Failed to added new spawn point");
 		addMSG("spawn_lobby_nf", "Lobby not found");
 		addMSG("spawn_nf", "Spawns not found");
 		addMSG("spawn_remove_success", "Spawn %1% successfully removed");
 		addMSG("spawn_remove_fail", "An error occurred while removing spawn %1%");
 		// tags_
 		addMSG("tag_ends", "TAG's %1%");
 		// hlp_
 		addMSG("hlp_topic", "Type %1% to show help topic");
 		addMSG("hlp_nf", "Help for %1% command not found");
 		// hlp_cmd_
 		addMSG("hlp_cmd_status", "Show your progress");
 		addMSG("hlp_cmd_help", "Show this help topic");
 		addMSG("hlp_cmd_chest", "Show information about the chest (look at the chest)");
 		addMSG("hlp_cmd_chest_add", "Create new looted chest. Type &2..create &7[name] &fto create chest with lootID");
 		addMSG("hlp_cmd_chest_remove", "Remove looted chest");
 		addMSG("hlp_cmd_chest_set", "Set the lootID for chest");
 		addMSG("hlp_cmd_spawn", "Start playing");
 		addMSG("hlp_cmd_spawn_set", "Add new spawn point. Type &2..set lobby &fto add new lobby location");
 		addMSG("hlp_cmd_spawn_list", "Show list of all spawns and lobby locations");
 		addMSG("hlp_cmd_spawn_remove", "Type &2..remove &7[name] &fto remove the spawn point by name");
 		addMSG("hlp_cmd_tag", "Type&2 ..tag enable/disable &fto enable/disable colored names");
 		addMSG("hlp_cmd_region", "Make a region of zombie spawn locations. Select region with WorldEdit Wand. Then type &2..region save &7[name] [parametrs]&f. Parametrs: &2s=&7[true/false]&f - can spawn, &2h=&7[true/false]&f - top-to-bottom");
 		addMSG("hlp_cmd_loot", "Create, delete, assigning flags for loot");
 		addMSG("hlp_cmd_loot_new", "Type &2/dd loot new &7[name]&f to create new loot in server memory. Type &2/dd loot save&f to save your loot");
 		addMSG("hlp_cmd_loot_remove", "Remove the loot for the specified name");
 		addMSG("hlp_cmd_loot_list", "Show list of existing loot");
 		addMSG("hlp_cmd_loot_item", "Add items to your loot");
 		addMSG("hlp_cmd_loot_flag", "Set the flags for the items in your loot");
 		addMSG("hlp_cmd_loot_save", "Save your loot");
 		addMSG("hlp_cmd_about", "Show information about the plugin");
 		addMSG("hlp_cmd_debug", "&4Simple debug command. Only for the developers!");
 	}
 
 	public void init() {
 		if (isSqlWrapper()) {
 			new DBInit();
 			Database db = DBInit.DATABASE;
 			db.open();
 			SQLPlayer.createTables();
 			SQLPlayer.initPrep();
 			SQLChest.createTables();
 			SQLChest.initPrep();
 
 			PlayerManager.init();
 			ChestManager.init();
 		}
 
 		playerCfg = new PlayerConfig(plg);
 		lootCfg = new LootConfig(plg);
 		chestCfg = new ChestConfig(plg);
 		regionCfg = new RegionConfig(plg);
 		spawnCfg = new SpawnConfig(plg);
 
 	}
 
 	public static SpawnConfig getSpawnCfg() {
 		return spawnCfg;
 	}
 
 	public static LootConfig getLC() {
 		return lootCfg;
 	}
 
 	public static RegionConfig getRC() {
 		return regionCfg;
 	}
 
 	public static ChestConfig getCC() {
 		return chestCfg;
 	}
 
 	public static PlayerConfig getPC() {
 		return playerCfg;
 	}
 
 	public static boolean isSqlWrapper() {
 		String wrapper = Nodes.MYSQL_DBWRAPPER.getString();
		return DarkDays.sqlibrary && (wrapper.equalsIgnoreCase("mysql") || wrapper.equalsIgnoreCase("sqlite"));
 	}
 
 	public static void extract(String[] names) {
 		for (String name : names) {
 			File actual = new File(DarkDays.getDataPath(), name);
 			if (!actual.exists()) {
 				InputStream input = DarkDays.class.getResourceAsStream("/" + name);
 				if (input != null) {
 					FileOutputStream output = null;
 					try {
 						output = new FileOutputStream(actual);
 						byte[] buf = new byte[8192];
 						int length = 0;
 						while ((length = input.read(buf)) > 0) {
 							output.write(buf, 0, length);
 						}
 					} catch (Exception e) {
 					} finally {
 						try {
 							if (input != null)
 								input.close();
 						} catch (Exception e) {
 						}
 						try {
 							if (output != null)
 								output.close();
 						} catch (Exception e) {
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static void load(File file) {
 		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 		for (Nodes n : Nodes.values())
 			if (!n.getNode().isEmpty())
 				if (config.get(n.getNode()) != null)
 					n.setValue(config.get(n.getNode()));
 	}
 
 	public static void save(File file) {
 		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 		for (Nodes n : Nodes.values())
 			if (!n.getNode().isEmpty())
 				config.set(n.getNode(), n.getValue());
 		try {
 			config.save(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
