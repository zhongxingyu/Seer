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
 
 	protected static LinkedList<Package> getDefaultPackages(FileConfiguration config) {
 		LinkedList<Package> DefaultPackages = null;
 		if (config.isConfigurationSection("DefaultPackages")) {
 			DefaultPackages = new LinkedList<Package>();
 			Package pck;
 			List<Integer> enchIds;
 			List<Integer> enchLvls;
 			ConfigurationSection defaultPackage = config.getConfigurationSection("DefaultPackages");
 			ConfigurationSection pckCs, item;
 			byte i = 0;
 			HashMap<Enchantment, Integer> enchts = new HashMap<Enchantment, Integer>();
 
 			for (String pckName : defaultPackage.getKeys(false)) {
 
 				pckCs = defaultPackage.getConfigurationSection(pckName);
 				if (pckCs != null) {
 					pck = new Package(pckName);
 					for (String itemName : pckCs.getKeys(false)) {
 
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
 								for (Integer id : enchIds) {
 									enchts.put(Enchantment.getById(id), (enchLvls.size() > i) ? enchLvls.get(i) : 1);
 									i++;
 								}
 							}
 							pck.addItem(item.getInt("id"), (short) item.getInt("data", item.getInt("subid", item.getInt("durability", 0))),
 									(short) item.getInt("amount", 1), enchts);
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
 
 	public TribuConfig(FileConfiguration config) {
 		this(config, new TribuDefaultConfiguration());
 
 	}
 
 	public TribuConfig() {
 		this(Constants.configFile);
 	}
 
 	public TribuConfig(String config) {
 		this(new File(config));
 	}
 
 	public TribuConfig(File config) {
 		this(config, new TribuDefaultConfiguration());
 	}
 
 	public TribuConfig(File config, TribuDefaultConfiguration DefaultConfig) {
 		this(YamlConfiguration.loadConfiguration(config), DefaultConfig);
 
 	}
 
 	public TribuConfig(FileConfiguration config, TribuDefaultConfiguration DefaultConfig) {
 		/*
 		 * try { config.load(config); } catch (FileNotFoundException e2) {
 		 * 
 		 * } catch (IOException e2) { e2.printStackTrace(); } catch
 		 * (InvalidConfigurationException e2) { e2.printStackTrace(); }
 		 */
 
 		load(config, DefaultConfig);
 
 	}
 	private void load(FileConfiguration config, TribuDefaultConfiguration DefaultConfig)
 	{
 		config.options().header("# Tribu Config File Version " + Constants.ConfigFileVersion + " \n");
 
 		HashMap<String, Object> DefaultConfiguration = (HashMap<String, Object>) DefaultConfig.toMap();
 
 		for (String key : config.getKeys(true)) {
 			this.load(key, config);
 			DefaultConfiguration.remove(key);
 		}
 		// Add missing keys
 		for (Entry<String, Object> e : DefaultConfiguration.entrySet()) {
 			config.set(e.getKey(), e.getValue());
 		}
 	}
 	public void reload(FileConfiguration config)
 	{
 		this.load(config,this);
 	}
 	protected static void debugMsg(String info) {
 		// Logger.getLogger("Minecraft").info("[Tribu] " + info);
 	}
 
 	/*
 	 * protected void LogSevere(String string) {
 	 * Logger.getLogger("Minecraft").severe("[Tribu] " + string); }
 	 */
 	public void load(String key,FileConfiguration config)
 	{
 		String[] keyNode =key.split("\\.");
 		byte nodeCount=(byte) keyNode.length;
 		debugMsg(key);
 		if(nodeCount>=2)
 		{
 		//	info(keyNode[0] + " - "+keyNode[1] );
 			if(keyNode[0].equalsIgnoreCase("PluginMode"))
 			{
 				//if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("ServerExclusive"))
 					{
 						PluginModeServerExclusive=config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("WorldExclusive"))
 					{
 						PluginModeWorldExclusive= config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("Language"))
 					{
 						PluginModeLanguage= config.getString(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("AutoStart"))
 					{
 						PluginModeAutoStart=config.getBoolean(key) ;
 					}
 					else if(keyNode[1].equalsIgnoreCase("DefaultLevel"))
 					{
 						PluginModeDefaultLevel=config.getString(key);
 					}
 				}
 			}else if(keyNode[0].equalsIgnoreCase("Level"))
 			{
 				//if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("Jail"))
 					{
 						LevelJail=config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("JailRadius"))
 					{
 						LevelJailRadius=config.getDouble(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("ClearZone"))
 					{
 						LevelClearZone=(Double) config.getDouble(key);
 					}
 				}
 			}
 			else if(keyNode[0].equalsIgnoreCase("WaveStart"))
 			{
 				//if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("SetTime"))
 					{
 						WaveStartSetTime=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("SetTimeTo"))
 					{
 						debugMsg("WaveStartSetTimeTo < "+WaveStartSetTimeTo);
 						WaveStartSetTimeTo=(Integer) config.getInt(key);
 						debugMsg("WaveStartSetTimeTo > "+WaveStartSetTimeTo);
 					}
 					else if(keyNode[1].equalsIgnoreCase("Delay"))
 					{
 						 WaveStartDelay=(Integer) config.getInt(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("TeleportPlayers"))
 					{
 						 WaveStartTeleportPlayers=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("HealPlayers"))
 					{
 						 WaveStartHealPlayers=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("MinPlayers"))
 					{
 						WaveStartMinPlayers= config.getInt(key);
 						if(WaveStartMinPlayers<1)
 							WaveStartMinPlayers= 1;
 					}
 						
 				}
 			}
 			else if(keyNode[0].equalsIgnoreCase("Zombies"))
 			{
 				//if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("Quantity"))
 					{
 						 ZombiesQuantity=(List<Double>) config.getDoubleList(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("Health"))
 					{
 						 ZombiesHealth=(List<Double>) config.getDoubleList(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("FireResistant"))
 					{
 						 ZombiesFireResistant=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("Focus"))
 					{
						ZombiesFocus = FocusType.fromString(config.getString(key));
 					}
 					else if(keyNode[1].equalsIgnoreCase("TimeToSpawn"))
 						ZombiesTimeToSpawn=(List<Double>) config.getDoubleList(key);
 				}
 			}
 			else if(keyNode[0].equalsIgnoreCase("Stats"))
 			{
 				if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("OnZombieKill"))
 					{
 						if(keyNode[2].equalsIgnoreCase("Points"))
 						{
 							 StatsOnZombieKillPoints=(Integer) config.getInt(key);
 						}
 						else if(keyNode[2].equalsIgnoreCase("Money"))
 						{
 							 StatsOnZombieKillMoney=(Integer) config.getInt(key);
 						}
 					}
 					else if(keyNode[1].equalsIgnoreCase("OnPlayerDeath"))
 					{
 						if(keyNode[2].equalsIgnoreCase("Points"))
 						{
 							 StatsOnPlayerDeathPoints=(Integer) config.getInt(key);
 						}
 						else if(keyNode[2].equalsIgnoreCase("Money"))
 						{
 							 StatsOnPlayerDeathMoney=(Integer) config.getInt(key);
 						}
 					}
 					else if(keyNode[1].equalsIgnoreCase("RewardMethod"))
 						StatsRewardMethod=config.getString(key);
 					else if(keyNode[1].equalsIgnoreCase("RewardOnlyAlive"))
 						StatsRewardOnlyAlive=config.getBoolean(key);
 				}
 			}
 			else if(keyNode[0].equalsIgnoreCase("Players"))
 			{
 				//if(nodeCount>2)
 				{
 					if(keyNode[1].equalsIgnoreCase("DontLooseItem"))
 					{
 						 PlayersDontLooseItem=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("StoreInventory"))
 					{
 						 PlayersStoreInventory=(Boolean) config.getBoolean(key);
 					}
 					else if(keyNode[1].equalsIgnoreCase("Rollback"))
 					{
 						 PlayersRollback=(Boolean) config.getBoolean(key);
 					}
 				}
 			}
 			else if(keyNode[0].equalsIgnoreCase("DefaultPackages"))
 			{
 				 return;
 			}
 			else
 			{
 				debugMsg("Not found : "+key);
 				try {
 					this.getClass().getField(key).set(toMap().get(key), config.get(key));
 				} catch (Exception e)
 				{
 					debugMsg("Failed "+key);
 					return;
 				}
 				
 			}
 		}
 		else if(key.equalsIgnoreCase("DefaultPackages"))
 		{
 			 DefaultPackages=(LinkedList<Package>) TribuConfig.getDefaultPackages(config);
 		}
 		else
 			debugMsg("Section : "+key);
 		return;
 	}
 
 }
