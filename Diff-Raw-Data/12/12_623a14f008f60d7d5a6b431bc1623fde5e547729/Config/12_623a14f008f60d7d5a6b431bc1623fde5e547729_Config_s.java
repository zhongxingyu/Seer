 package com.wimbli.TexturePackMenu;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.reader.UnicodeReader;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.io.CRCStore;
 
 public class Config
 {
 	// private stuff used within this class
 	private static TexturePackMenu plugin;
 	private static Yaml yaml;
 	private static File configFile;
 	private static File playerFile;
 
 	private static Map<String, String> texturePacks = new LinkedHashMap<String, String>();  // (Pack Name, URL)
 	private static Map<String, String> playerPacks = new HashMap<String, String>();  // (Player Name, Pack Name)
 
 	private static byte[] crcBuffer = new byte[16384];
 
 
 	// load config
 	public static void load(TexturePackMenu master)
 	{
 		plugin = master;
 		configFile = new File(plugin.getDataFolder(), "config.yml");
 		playerFile = new File(plugin.getDataFolder(), "players.yml");
 
 		// make our yml output more easily human readable, instead of compact and ugly
 		DumperOptions options = new DumperOptions();
 		options.setPrettyFlow(true);
 		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
 		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
 		yaml = new Yaml(options);
 
 		loadTexturePackList();
 		loadPlayerPacks();
 	}
 
 
 
 	public static String[] texPackNames()
 	{
 		return texturePacks.keySet().toArray(new String[0]);
 	}
 	public static String[] texPackURLs()
 	{
 		return texturePacks.values().toArray(new String[0]);
 	}
 	public static int texturePackCount()
 	{
 		return texturePacks.size();
 	}
 
 	public static String getPack(String playerName)
 	{
 		if (!playerPacks.containsKey(playerName.toLowerCase()))
 			return "";
 
 		return playerPacks.get(playerName.toLowerCase());
 	}
 
 	public static void setPack(SpoutPlayer sPlayer, String packName)
 	{
 		if (!texturePacks.containsKey(packName))
 		{
 			setPack(sPlayer, 0);
 			if (sPlayer.hasPermission("texturepackmenu.texture"))
 				sPlayer.sendNotification("Texture packs available", "Use command: "+ChatColor.AQUA+"/texture", Material.PAPER, (short)0, 10000);
 		}
 		else
 			setPlayerTexturePack(sPlayer, packName);
 	}
 	public static void setPack(SpoutPlayer sPlayer, int index)
 	{
 		if (texturePacks.size() < index - 1)
 			index = 0;
 		setPlayerTexturePack(sPlayer, texPackNames()[index]);
 	}
 	public static void setPackDelayed(final SpoutPlayer sPlayer, final int index)
 	{
 		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				setPack(sPlayer, index);
 			}
 		}, 10);
 	}
 
 	private static void setPlayerTexturePack(SpoutPlayer sPlayer, String packName)
 	{
 		if (sPlayer == null || !sPlayer.isOnline())
 			return;
 
 		String packURL = texturePacks.get(packName);
 
 		sPlayer.sendNotification("Texture pack selected:", packName, Material.PAINTING);
 		storePlayerTexturePack(sPlayer.getName(), packName);
 
 		if (packURL == null || packURL.isEmpty())
 			sPlayer.resetTexturePack();
 		else
 		{
 			sPlayer.setTexturePack(packURL);
 
 			// make sure it checks out as valid, by getting CRC value for it
 			Long crc = null;
 			crc = CRCStore.getCRC(packURL, crcBuffer);
 			if (crc == null || crc == 0)
 				plugin.logWarn("Bad CRC value for texture pack. It is probably an invalid URL: "+packURL);
 		}
 	}
 
 	public static void storePlayerTexturePack(String playerName, String packName)
 	{
 		playerPacks.put(playerName.toLowerCase(), packName);
 	}
 
 	public static void resetPlayerTexturePack(String playerName)
 	{
 		playerPacks.remove(playerName.toLowerCase());
 
 		Player player = plugin.getServer().getPlayer(playerName);
 		if (player != null)
 		{
 			SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 			if (sPlayer != null && sPlayer.isSpoutCraftEnabled())
 				setPack(sPlayer, 0);
 		}
 	}
 
 
 
 	public static void loadTexturePackList()
 	{
 		try
 		{
 			FileInputStream in = new FileInputStream(configFile);
 			texturePacks = (Map<String, String>)yaml.load(new UnicodeReader(in));
 		}
 		catch (FileNotFoundException e)
 		{
 		}
 
 		if (texturePacks == null || texturePacks.isEmpty())
 			useDefaults();
 	}
 
 	// load player data
 	private static void loadPlayerPacks()
 	{
 		if (!playerFile.getParentFile().exists() || !playerFile.exists())
 			return;
 
 		try
 		{
 			FileInputStream in = new FileInputStream(playerFile);
 			playerPacks = (Map<String, String>)yaml.load(new UnicodeReader(in));
 		}
 		catch (FileNotFoundException e)
 		{
 		}
 	}
 
 	// save player data
 	public static void savePlayerPacks()
 	{
 		if (!playerFile.getParentFile().exists())
 			return;
 
 		try
 		{
 			BufferedWriter out = new BufferedWriter(new FileWriter(playerFile, false));
 			out.write(yaml.dump(playerPacks));
 			out.close();
 		}
 		catch (IOException e)
 		{
 			plugin.logWarn("ERROR SAVING PLAYER DATA: " + e.getLocalizedMessage());
 		}
 	}
 
 	// set default values, and save new config file
 	private static void useDefaults()
 	{
 		plugin.logConfig("Configuration not present, creating new default config.yml file. YOU WILL NEED TO EDIT IT MANUALLY.");
 
 		texturePacks = new LinkedHashMap<String, String>();
 		texturePacks.put("Player Choice", "");
 		texturePacks.put("Minecraft Default", "https://github.com/Brettflan/TexturePackMenu/raw/master/packs/default.zip");
 		texturePacks.put("Default Pack Copy", "https://github.com/Brettflan/TexturePackMenu/raw/master/packs/default.zip");
 
 		if (!configFile.getParentFile().exists())
 		{
 			if (!configFile.getParentFile().mkdirs())
 			{
 				plugin.logWarn("FAILED TO CREATE PLUGIN FOLDER.");
 				return;
 			}
 		}
 
 		String newLine = System.getProperty("line.separator");
 
 		try
 		{
 			BufferedWriter out = new BufferedWriter(new FileWriter(configFile, false));
 			out.write("# Below is a default config which you will need to update to contain your own texture pack choices. You will need to restart your server after making changes." + newLine);
 			out.write("# Each entry consists of the displayed name of the texture pack, followed by the download URL that will be used. There is no limit to the number of entries." + newLine);
 			out.write("# Texture pack names must be limited to 26 characters long at most. Longer ones will cause errors." + newLine);
 			out.write("# The first entry will always be the default, which players new to the server will be set to." + newLine);
 			out.write("# Be sure to test each one you add to make sure it works. An invalid URL will cause an error, and an URL which fails will make that entry do nothing." + newLine);
 			out.write("# If a texture pack fails to load properly, try changing the URL filename. In particular, try replacing spaces (\" \") and other special characters with underscores (\"_\")." + newLine);
 			out.write("# If you do want to allow players to use their own texture pack, you can leave a blank URL as seen below for \"Player Choice\"." + newLine);
 			out.write("# Note that if you want to use quotation marks (\") in texture pack names, you will need to add them with a backslash like so to prevent parsing errors: \\\"" + newLine);
 			out.write("#     example: \"The \\\"Silly\\\" Pack\": \"http://fake-server.net/sillypack.zip\"" + newLine);
 			out.write("#     would display as:    The \"Silly\" Pack" + newLine);
 			out.write("#" + newLine);
 			out.write("# For more information, head here: http://dev.bukkit.org/server-mods/texturepackmenu/" + newLine);
 			out.write(newLine);
 			out.write(yaml.dump(texturePacks));
 			out.close();
 		}
 		catch (IOException e)
 		{
 			plugin.logWarn("ERROR SAVING DEFAULT CONFIG: " + e.getLocalizedMessage());
 		}
 	}
 }
