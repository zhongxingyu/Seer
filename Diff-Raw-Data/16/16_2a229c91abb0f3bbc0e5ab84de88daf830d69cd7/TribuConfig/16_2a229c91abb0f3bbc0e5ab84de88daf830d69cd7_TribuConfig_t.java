 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Configuration;
 
 import graindcafe.tribu.Package;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 
 public class TribuConfig extends TribuDefaultConfiguration {
 
 	protected static void debugMsg(final String info) {
		//Logger.getLogger("Minecraft").info("[Tribu] " + info);
 	}
 
 	protected static LinkedList<Package> getDefaultPackages(final FileConfiguration config) {
 		LinkedList<Package> DefaultPackages = null;
 		if (config.isConfigurationSection("DefaultPackages")) {
 			DefaultPackages = new LinkedList<Package>();
 			Package pck;
 			List<Integer> enchIds;
 			List<Integer> enchLvls;
 			final ConfigurationSection defaultPackage = config.getConfigurationSection("DefaultPackages");
 			ConfigurationSection pckCs, item;
 			byte i = 0;
 			final HashMap<Enchantment, Integer> enchts = new HashMap<Enchantment, Integer>();
 
 			for (final String pckName : defaultPackage.getKeys(false)) {
 
 				pckCs = defaultPackage.getConfigurationSection(pckName);
 				if (pckCs != null) {
 					pck = new Package(pckName);
 					for (final String itemName : pckCs.getKeys(false)) {
 
 						item = pckCs.getConfigurationSection(itemName);
 						if (item != null && item.contains("id")) {
 
 							enchts.clear();
 							if (item.contains("enchantmentsId")) {
 								enchIds = item.getIntegerList("enchantmentsId");
 								if (item.contains("enchantmentsLevel"))
 									enchLvls = item.getIntegerList("enchantmentsLevel");
 								else
 									enchLvls = new LinkedList<Integer>();
 								i = 0;
 								for (final Integer id : enchIds) {
 									enchts.put(Enchantment.getById(id), (enchLvls.size() > i) ? enchLvls.get(i) : 1);
 									i++;
 								}
 							}
 							pck.addItem(item.getInt("id"), (short) item.getInt("data", item.getInt("subid", item.getInt("durability", 0))), (short) item.getInt("amount", 1), enchts);
 						} else
 							debugMsg(itemName + " not loaded");
 					}
 					DefaultPackages.push(pck);
 				} else
 					debugMsg(pckName + " not loaded");
 
 			}
 		}
 		return DefaultPackages;
 	}
 
 	public TribuConfig() {
 		this(Constants.configFile);
 	}
 
 	public TribuConfig(final File config) {
 		this(config, new TribuDefaultConfiguration());
 	}
 
 	public TribuConfig(final File config, final TribuDefaultConfiguration DefaultConfig) {
 		this(YamlConfiguration.loadConfiguration(config), DefaultConfig);
 
 	}
 
 	public TribuConfig(final FileConfiguration config) {
 		this(config, new TribuDefaultConfiguration());
 
 	}
 
 	public TribuConfig(final FileConfiguration config, final TribuDefaultConfiguration DefaultConfig) {
 		/*
 		 * try { config.load(config); } catch (FileNotFoundException e2) {
 		 * 
 		 * } catch (IOException e2) { e2.printStackTrace(); } catch
 		 * (InvalidConfigurationException e2) { e2.printStackTrace(); }
 		 */
 
 		load(config, DefaultConfig);
 
 	}
 
 	public TribuConfig(final String config) {
 		this(new File(config));
 	}
 
 	private void load(final FileConfiguration config, final TribuDefaultConfiguration DefaultConfig) {
 		config.options().header("# Tribu Config File Version " + Constants.ConfigFileVersion + " \n");
 
 		final HashMap<String, Object> DefaultConfiguration = (HashMap<String, Object>) DefaultConfig.toMap();
 
 		for (final String key : config.getKeys(true)) {
 			this.load(key, config);
 			DefaultConfiguration.remove(key);
 		}
 		// Add missing keys
 		for (final Entry<String, Object> e : DefaultConfiguration.entrySet())
 			if (e.getValue() instanceof TribuEnum)
 				config.set(e.getKey(), e.getValue().toString());
 			else
 				config.set(e.getKey(), e.getValue());
 	}
 
 	/*
 	 * protected void LogSevere(String string) {
 	 * Logger.getLogger("Minecraft").severe("[Tribu] " + string); }
 	 */
 	public void load(final String key, final FileConfiguration config) {
 		final String[] keyNode = key.split("\\.");
 		final byte nodeCount = (byte) keyNode.length;
 		debugMsg(key);
 		if (nodeCount >= 2) {
 			if (keyNode[0].equalsIgnoreCase("PluginMode")) {
 				if (keyNode[1].equalsIgnoreCase("ServerExclusive"))
 					PluginModeServerExclusive = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("WorldExclusive"))
 					PluginModeWorldExclusive = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("Language"))
 					PluginModeLanguage = config.getString(key);
 				else if (keyNode[1].equalsIgnoreCase("AutoStart"))
 					PluginModeAutoStart = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("DefaultLevel")) PluginModeDefaultLevel = config.getString(key);
 			} else if (keyNode[0].equalsIgnoreCase("Level")) {
 				if (keyNode[1].equalsIgnoreCase("Jail"))
 					LevelJail = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("JailRadius")) {
 					LevelJailRadius = config.getDouble(key);
 					LevelJailRadius *= LevelJailRadius;
 				} else if (keyNode[1].equalsIgnoreCase("ClearZone"))
 					LevelClearZone = config.getDouble(key);
 				else if (keyNode[1].equalsIgnoreCase("StartDelay"))
 					LevelStartDelay = config.getInt(key);
 				else if (keyNode[1].equalsIgnoreCase("MinPlayers")) {
 					LevelMinPlayers = config.getInt(key);
 					if (LevelMinPlayers < 1) LevelMinPlayers = 1;
 				}
 			} else if (keyNode[0].equalsIgnoreCase("WaveStart")) {
 				if (keyNode[1].equalsIgnoreCase("SetTime"))
 					WaveStartSetTime = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("SetTimeTo")) {
 					debugMsg("WaveStartSetTimeTo < " + WaveStartSetTimeTo);
 					WaveStartSetTimeTo = config.getInt(key);
 					debugMsg("WaveStartSetTimeTo > " + WaveStartSetTimeTo);
 				} else if (keyNode[1].equalsIgnoreCase("Delay"))
 					WaveStartDelay = config.getInt(key);
 				else if (keyNode[1].equalsIgnoreCase("TeleportPlayers"))
 					WaveStartTeleportPlayers = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("HealPlayers")) WaveStartHealPlayers = config.getBoolean(key);
 
 			} else if (keyNode[0].equalsIgnoreCase("Zombies")) {
 				if (keyNode[1].equalsIgnoreCase("Quantity"))
 					ZombiesQuantity = config.getDoubleList(key);
 				else if (keyNode[1].equalsIgnoreCase("Health"))
 					ZombiesHealth = config.getDoubleList(key);
 				else if (keyNode[1].equalsIgnoreCase("Damage"))
 					ZombiesDamage = config.getDoubleList(key);
 				else if (keyNode[1].equalsIgnoreCase("FireResistant"))
 					ZombiesFireResistant = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("Focus"))
 					ZombiesFocus = FocusType.fromString(config.getString(key));
 				else if (keyNode[1].equalsIgnoreCase("TimeToSpawn"))
 					ZombiesTimeToSpawn = config.getDoubleList(key);
 				else if (keyNode[1].equalsIgnoreCase("Speed") && nodeCount > 2) {
 					if (keyNode[2].equalsIgnoreCase("Random")) ZombiesSpeedRandom = config.getBoolean(key);
 					if (keyNode[2].equalsIgnoreCase("Base")) ZombiesSpeedBase = (float) config.getDouble(key);
 					if (keyNode[2].equalsIgnoreCase("Rush")) ZombiesSpeedRush = (float) config.getDouble(key);
 				}
 			} else if (keyNode[0].equalsIgnoreCase("Stats")) {
 				if (nodeCount > 2) if (keyNode[1].equalsIgnoreCase("OnZombieKill")) {
 					if (keyNode[2].equalsIgnoreCase("Points"))
 						StatsOnZombieKillPoints = config.getInt(key);
 					else if (keyNode[2].equalsIgnoreCase("Money")) StatsOnZombieKillMoney = config.getInt(key);
 				} else if (keyNode[1].equalsIgnoreCase("OnPlayerDeath")) {
 					if (keyNode[2].equalsIgnoreCase("Points"))
 						StatsOnPlayerDeathPoints = config.getInt(key);
 					else if (keyNode[2].equalsIgnoreCase("Money")) StatsOnPlayerDeathMoney = config.getInt(key);
 				} else if (keyNode[1].equalsIgnoreCase("RewardMethod"))
 					StatsRewardMethod = config.getString(key);
 				else if (keyNode[1].equalsIgnoreCase("RewardOnlyAlive")) StatsRewardOnlyAlive = config.getBoolean(key);
 			} else if (keyNode[0].equalsIgnoreCase("Players")) {
 				if (keyNode[1].equalsIgnoreCase("DontLooseItem"))
 					PlayersDontLooseItem = config.getBoolean(key);
 				else if (keyNode[1].equalsIgnoreCase("StoreInventory"))
 					PlayersStoreInventory = config.getBoolean(key);
				else if (keyNode[1].equalsIgnoreCase("Rollback"))
					PlayersRollback = config.getBoolean(key);
				else if (keyNode[1].equalsIgnoreCase("AllowPlace"))
					PlayersAllowPlace = config.getBoolean(key);
				else if (keyNode[1].equalsIgnoreCase("AllowBreak")) PlayersAllowBreak = config.getBoolean(key);
				//debugMsg(keyNode[1] +": "+ config.getBoolean(key));
 			} else if (keyNode[0].equalsIgnoreCase("DefaultPackages"))
 				return;
 			else {
 				debugMsg("Not found : " + key);
 				try {
 					this.getClass().getField(key).set(toMap().get(key), config.get(key));
 				} catch (final Exception e) {
 					debugMsg("Failed " + key);
 					return;
 				}
 
 			}
 		} else if (key.equalsIgnoreCase("DefaultPackages"))
 			DefaultPackages = TribuConfig.getDefaultPackages(config);
 		else
 			debugMsg("Section : " + key);
 		return;
 	}
 
 	public void reload(final FileConfiguration config) {
 		this.load(config, this);
 	}
 
 }
