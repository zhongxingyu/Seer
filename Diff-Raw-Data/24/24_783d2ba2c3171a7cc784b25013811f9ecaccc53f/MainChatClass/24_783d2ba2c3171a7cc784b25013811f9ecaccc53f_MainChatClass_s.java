 package com.gmail.br45entei.enteischatmanager;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gmail.br45entei.enteispluginlib.EPLib;
 import com.gmail.br45entei.enteispluginlib.FileMgmt;
 import com.gmail.br45entei.enteispluginlib.InvalidYamlException;
 
 /**The main class of this plugin.
  * @author Brian_Entei */
 public class MainChatClass extends JavaPlugin implements Listener {
 	public static MainChatClass plugin;
 	public static ConsoleCommandSender console = null;
 	private static final Logger logger = Logger.getLogger("Minecraft");
 	public static PluginDescriptionFile pdffile;
 	public static String pluginName = EPLib.rwhite + "[" + EPLib.yellow + "Entei's Chat Manager" + EPLib.rwhite + "] ";
 	public static Chat chat = null;
 	public static Permission permission = null;
 	public static boolean useVaultChat = false;
 	public static boolean useVaultPermissions = false;
 	public static boolean madeChatMsg = false;
 	public static String broadcastDisplay = "";
 	public static String announcementDisplay = "";
 	public static String noAccess = "";
 	public static String noPerm = "";
 	public static String playerOnly = "";
 
 	private static ArrayList<String> oneTimeMessageList = new ArrayList<String>();
 
 	public static boolean YamlsAreLoaded = false;
 	public static FileConfiguration config;
 	public static File configFile = null;
 	public static String configFileName = "config.yml";
 	public static FileConfiguration channels;
 	public static File channelsFile = null;
 	public static String channelsFileName = "channels.yml";
 	public static int chatFontSize = 3;
 	public static String chatLogFileName = "chatLog.html";
 	public static File chatLogFile = null;
 
 	public static boolean logChats = false;
 
 	public static String dataFolderName = "";
 	public static boolean showDebugMsgs = false;
 	public static boolean showChatDebugMessages = false;
 	public static boolean enableChatManagement = false;
 	public static boolean enableCorrectMemes = false;
 	public static String chatSayFormat = "";
 	public static boolean enableChatFormat = false;
 	public static boolean enableWelcomeMsg = false;
 	public static boolean enableCustomNicks = false;
 	public static boolean enableGroupManagement = false;
 	public static boolean enableUpdateGroupWorldSuffix = false;
 	public static String consoleSayFormat = "";
 	public static boolean useConsoleSayFormat = false;
 	public static String chatMsgTooSimilar = "";
 	public static boolean useTooSimilarMsg = false;
 	public static boolean enableTooSimilarFilter = false;
 	public static String chatterCussed = "";
 	public static boolean useChatterCussedMsg = false;
 	public static String chatterCussedConsoleMsg = "";
 	public static boolean useCussConsoleMsg = false;
 	public static String blockedLinkMsg = "";
 	public static boolean showBlockedLinkMsg = false;
 	public static boolean blockLinkMessages = false;
 	public static String noSpamMessage = "";
 	public static boolean enableSpamFilter = false;
 	public static int percentOfCapitalLettersLimit = 0;
 	public static int capitalLetterGraceCount = 0;
 
 	public static boolean useChannels = false;
 	public static String channelType = "";
 	public static List<String> chChannel1 = new ArrayList<String>();
 	public static String chChannel1Prefix = "";
 	public static List<String> chChannel2 = new ArrayList<String>();
 	public static String chChannel2Prefix = "";
 	public static List<String> chAdmin = new ArrayList<String>();
 	public static String chAdminPrefix = "";
 	public static List<String> chConsole = new ArrayList<String>();
 	public static String chConsolePrefix = "";
 
 	public static List<String> lastPlayerMessage = new ArrayList<String>();
 	public static List<CommandSender> lastPlayerPm = new ArrayList<CommandSender>();
 	public static List<CommandSender> lastPlayerPmer = new ArrayList<CommandSender>();
 	public static int lastPlayerPmInt = 0;
 	public static int lastPlayerInt = 0;
 	public static final int chatCache = 200;
 	public static List<CommandSender> lastPlayerMsgPlayer = new ArrayList<CommandSender>();
 	public static final String getPunctuationChars = "\\p{Punct}+";
 	public static final String getWhiteSpaceChars = "\\s+";
 	public static final String getAlphaNumericChars = "\\p{Alnum}+";
 	public static final String getAlphabetChars = "\\p{Alpha}+";
 	public static final String getNumberChars = "\\p{Digit}+";
 	public static final String getUpperCaseChars = "\\p{Lower}+";
 	public static final String getLowerCaseChars = "\\p{Upper}+";
 
 	public void LoginListener(MainChatClass JavaPlugin) {
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	@Override
 	public void onDisable() {
 		sendConsoleMessage(pluginName + "&eVersion " + pdffile.getVersion() + " is now disabled.");
 	}
 	@Override
 	public void onEnable() {
 		pdffile = getDescription();
 		Bukkit.getServer().getPluginManager().registerEvents(this, this);
 		console = Bukkit.getServer().getConsoleSender();
 		File dataFolder = getDataFolder();
 		if(!dataFolder.exists())
 			dataFolder.mkdir();
 		try{
 			dataFolderName = dataFolder.getAbsolutePath();} catch (SecurityException localSecurityException) {
 		}EPLib.ECMDataFolderName = dataFolderName;
 		EPLib.showDebugMsg(pluginName + "The dataFolderName variable is: \"" + dataFolderName + "\"!", showDebugMsgs);
 		LoadConfig();
 		boolean vaultAvailable = setUpVaultRegistrations();
 		sendConsoleMessage("&6Chat available: &4" + useVaultChat + "&6.");
 		if(vaultAvailable) {
 			sendConsoleMessage(pluginName + "&aVersion " + pdffile.getVersion() + " is now enabled!");
 		} else {
 			logger.severe(pdffile.getPrefix() + " No Vault detected! The plugin will be useless without vault. Disabling...");
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 	}
 	public boolean LoadConfig() {
 		saveDefaultConfig();
 		configFile = new File(dataFolderName, configFileName);
 		config = new YamlConfiguration();
 		channelsFile = new File(dataFolderName, channelsFileName);
 		channels = new YamlConfiguration();
 		try{
 			loadYamlFiles();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		YamlsAreLoaded = reloadYamls(true);
 		if(YamlsAreLoaded) {
 			EPLib.showDebugMsg(pluginName + "&aAll YAML Configration Files loaded successfully!", showDebugMsgs);
 		} else {
 			sendConsoleMessage(pluginName + "&cError: Some YAML Files failed to load successfully! Check the server log or \"" + dataFolderName + "\\crash-reports.txt\" to solve the problem.");
 		}
 		chatLogFile = new File(dataFolderName, chatLogFileName);
 		if(logChats && !chatLogFile.exists()) {
 			chatLogFile.getParentFile().mkdirs();
 			FileMgmt.copy(getResource(chatLogFileName), chatLogFile, dataFolderName);
 			try{
 				String newLine = String.valueOf("\r");
 				RandomAccessFile raf = new RandomAccessFile(chatLogFile, "rw");
 				raf.writeBytes("<!DOCTYPE HTML>" + newLine + "<html>" + newLine + "\t<head>" + newLine + "\t\t<meta http-equiv=\"refresh\" content=\"5\">" + newLine + "\t\t<title>Chat Log for server \"" + Bukkit.getServerName() + "\"</title>" + newLine + "\t</head>" + newLine + "\t<body style=\"font-family:Lucida;\" bgcolor=\"#000000\" text=\"#FFFFFF\">" + newLine + "\t\t<font size=\"4\">Chat Log for server \"" + Bukkit.getServerName() + "\"</font><br>" + newLine);
 				raf.close();
 			} catch (Exception e) {
 				FileMgmt.LogCrash(e, "LoadConfig()", "Could not read/write to file \"" + chatLogFileName + "\"! Is the file read-only, perhaps?", false, dataFolderName);
 			}
 		}
 		return YamlsAreLoaded;}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPlayerQuit(PlayerQuitEvent evt) {
 		if(enableCustomNicks) {
 			evt.setQuitMessage(evt.getQuitMessage().replace(evt.getPlayer().getName(), updatePlayerDisplayName(evt.getPlayer(), "", "reload", true)));
 		}
 	}
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent evt) {
 		if(enableCustomNicks) {
 			updatePlayerDisplayName(evt.getPlayer(), "", "reload", false);
 			evt.setJoinMessage(evt.getJoinMessage().replace(evt.getPlayer().getName(), evt.getPlayer().getDisplayName()));
 		}
 		if(enableGroupManagement) {
 			updatePlayerGroup(evt.getPlayer().getWorld(), evt.getPlayer());
 		}
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent evt) {
 		if(enableCustomNicks) {
 			updatePlayerDisplayName(evt.getPlayer(), "", "reload", false);
 			sendMessage(evt.getPlayer(), pluginName + "&aYour display name was updated to: \"&r" + evt.getPlayer().getDisplayName() + "&r&a\".");
 			EPLib.showDebugMsg(pluginName + evt.getPlayer().getName() + "&r&a's display name was updated to: \"&r" + evt.getPlayer().getDisplayName() + "&r&a\".", showDebugMsgs);
 		}
 		if(enableGroupManagement) {
 			updatePlayerGroup(evt.getPlayer().getWorld(), evt.getPlayer());
 		}
 	}
 	public void loadYamlFiles() throws Exception {
 		if(!configFile.exists()) {
 			configFile.getParentFile().mkdirs();
 			FileMgmt.copy(getResource(configFileName), configFile, dataFolderName);
 		}
 		if(!channelsFile.exists()) {
 			channelsFile.getParentFile().mkdirs();
 			FileMgmt.copy(getResource(channelsFileName), channelsFile, dataFolderName);
 		}
 	}
 	public static boolean reloadYamls(boolean ShowStatus) {
 		YamlsAreLoaded = false;
 		boolean loadedAllVars = false;
 		String unloadedFiles = "\"";
 		Exception e1 = null;try{config.load(configFile);} catch (Exception e) {e1 = e;unloadedFiles += configFileName + "\" ";}
 		Exception e2 = null;try{channels.load(channelsFile);} catch (Exception e) {e2 = e;unloadedFiles += channelsFileName + "\" ";}
 		try{
 			if(unloadedFiles.equals("\"")) {
 				YamlsAreLoaded = true;
 				loadedAllVars = loadYamlVariables();
 				if(loadedAllVars) {
 					EPLib.showDebugMsg(pluginName + "&aAll of the yaml configuration files loaded successfully!", ShowStatus);
 				} else {
 					EPLib.showDebugMsg(pluginName + "&aSome of the settings did not load correctly from the configuration files! Check the server log to solve the problem.", ShowStatus);
 				}
 				return true;
 			}
 			String Causes = "";
 			if(e1 != null) {Causes = Causes.concat(Causes + e1.toString());}
 			if(e2 != null) {Causes = Causes.concat(Causes + e2.toString());}
 			throw new InvalidYamlException(Causes);
 		} catch (InvalidYamlException e) {
 			FileMgmt.LogCrash(e, "reloadYamls()", "Failed to load one or more of the following YAML files: " + unloadedFiles, false, dataFolderName);
 			EPLib.showDebugMsg(pluginName + "&cThe following YAML files failed to load properly! Check the server log or \"" + dataFolderName + "\\crash-reports.txt\" to solve the problem: (" + unloadedFiles + ")", true);
 		}
 		return false;
 	}
 	public boolean saveYamls() {
 		String unSavedFiles = "\"";
 		Exception e1 = null;try{config.save(configFile);} catch (Exception e) {e1 = e;unSavedFiles = unSavedFiles + configFileName + "\" ";}
 		Exception e2 = null;try{channels.save(channelsFile);} catch (Exception e) {e2 = e;unSavedFiles = unSavedFiles + channelsFileName + "\" ";}
 		try{
 			if(unSavedFiles.equals("\"")) {EPLib.showDebugMsg(pluginName + "&aAll of the yaml configuration files were saved successfully!", true);return true;}
 			String Causes = "";
 			if(e1 != null) Causes = Causes.concat(Causes + e1.toString());
 			if(e2 != null) Causes = Causes.concat(Causes + e2.toString());
 			throw new InvalidYamlException(Causes);
 		} catch (InvalidYamlException e) {
 			FileMgmt.LogCrash(e, "saveYamls()", "Failed to save one or more of the following YAML files: (" + unSavedFiles + ")", false, dataFolderName);
 			EPLib.showDebugMsg(pluginName + "&cThe following YAML files failed to get saved properly! Check the server log or \"" + dataFolderName + "\\crash-reports.txt\" to solve the problem: (" + unSavedFiles + ")", true);
 		}
 		return false;
 	}
 	private static String sendConsoleMessage(String msg) {return EPLib.sendConsoleMessage(msg);}
 	private static String sendMessage(CommandSender target, String msg) {return EPLib.sendMessage(target, msg);}
 	private static String sendMessage(Player target, String msg) {return EPLib.sendMessage(target, msg);}
 	public static boolean StringIsColorCode(String str) {
 		boolean strIsChatColor = false;
 		String pattern1 = "(?i)(\\u00A7[0-9A-FK-OR]|&[0-9A-FK-OR])+";
 		Pattern p1 = Pattern.compile(pattern1);
 		Matcher m1 = p1.matcher(str);
 		if(m1.matches() && m1.group().equalsIgnoreCase(str)) {
 			strIsChatColor = true;
 		}
 		return strIsChatColor;
 	}
 	public static boolean loadYamlVariables() {
 		boolean loadedAllVars = true;
 		try{announcementDisplay = EPLib.formatColorCodes(config.getString("announcementPrefix"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("announcementPrefix", "config.yml", pluginName);}
 		try{broadcastDisplay = EPLib.formatColorCodes(config.getString("broadcastPrefix"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("broadcastPrefix", "config.yml", pluginName);}
 		try{noAccess = EPLib.formatColorCodes(pluginName + config.getString("noAccess"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("noAccess", "config.yml", pluginName);}
 		try{noPerm = EPLib.formatColorCodes(pluginName + config.getString("noPermission"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("noPermission", "config.yml", pluginName);}
 		try{playerOnly = EPLib.formatColorCodes(pluginName + config.getString("playerOnly"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("playerOnly", "config.yml", pluginName);}
 		try{logChats = config.getBoolean("enableChatLogging");EPLib.logChats = logChats;
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableChatLogging", "config.yml", pluginName);}
 		try{String getDateColorCode = config.getString("chatLogDateColor");
 			if(StringIsColorCode(getDateColorCode)) {
 				EPLib.ecmDateColor = EPLib.formatColorCodes(config.getString("chatLogDateColor"));
 			} else {
 				EPLib.ecmDateColor = "&f";
 			}
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatLogDateColor", "config.yml", pluginName);}
 		try{
 			if(!config.getString("chatLogFileName").equals("")) {
 				chatLogFileName = config.getString("chatLogFileName");EPLib.chatLogFileName = chatLogFileName;
 			}
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatLogFileName", "config.yml", pluginName);}
 		String setUpFontSize = config.getString("chatFontSize");
 		if(setUpFontSize != null) {
 			if(setUpFontSize.equalsIgnoreCase("tiny")) {
 				chatFontSize = 1;
 			} else if(setUpFontSize.equalsIgnoreCase("small")) {
 				chatFontSize = 2;
 			} else if(setUpFontSize.equalsIgnoreCase("medium") || setUpFontSize.equalsIgnoreCase("normal") || setUpFontSize.equals("")) {
 				chatFontSize = 3;
 			} else if(setUpFontSize.equalsIgnoreCase("large")) {
 				chatFontSize = 5;
 			} else if(setUpFontSize.equalsIgnoreCase("huge")) {
 				chatFontSize = 7;
 			}
 			EPLib.chatLogFontSize = chatFontSize;
 		} else {
 			loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatFontSize", "config.yml", pluginName);
 		}
 		try{consoleSayFormat = EPLib.formatColorCodes(config.getString("consoleSayPrefix"));
 			EPLib.consoleSayFormat = consoleSayFormat;
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("consoleSayPrefix", "config.yml", pluginName);}
 		try{useConsoleSayFormat = config.getBoolean("useConsoleSayFormat");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("useConsoleSayFormat", "config.yml", pluginName);}
 		try{showDebugMsgs = config.getBoolean("showDebugMsgs");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("showDebugMsgs", "config.yml", pluginName);}
 		try{enableChatManagement = config.getBoolean("enableChatManagement");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableChatManagement", "config.yml", pluginName);}
 		try{chatSayFormat = EPLib.formatColorCodes(config.getString("chatSayFormat"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatSayFormat", "config.yml", pluginName);}
 		try{enableChatFormat = config.getBoolean("enableChatFormat");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableChatFormat", "config.yml", pluginName);}
 		try{showChatDebugMessages = (config.getBoolean("showChatDebugMessages")) && (enableChatFormat);
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("showChatDebugMessages", "config.yml", pluginName);}
 		try{enableGroupManagement = config.getBoolean("enableGroupManagement");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableGroupManagement", "config.yml", pluginName);}
 		try{enableUpdateGroupWorldSuffix = config.getBoolean("updateGroupWorldSuffixes");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("updateGroupWorldSuffixes", "config.yml", pluginName);}
 		try{enableWelcomeMsg = config.getBoolean("enableWelcomeMessages");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableWelcomeMessages", "config.yml", pluginName);}
 		try{enableCustomNicks = config.getBoolean("enableCustomNicks");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableCustomNicks", "config.yml", pluginName);}
 		try{enableCorrectMemes = config.getBoolean("enableConvertMemes");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableConvertMemes", "config.yml", pluginName);}
 		try{chatMsgTooSimilar = EPLib.formatColorCodes(pluginName + config.getString("chatMsgTooSimilar"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatMsgTooSimilar", "config.yml", pluginName);}
 		try{useTooSimilarMsg = config.getBoolean("useTooSimilarMsg");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("useTooSimilarMsg", "config.yml", pluginName);}
 		try{enableTooSimilarFilter = config.getBoolean("enableTooSimilarFilter");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableTooSimilarFilter", "config.yml", pluginName);}
 		try{chatterCussed = EPLib.formatColorCodes(pluginName + config.getString("chatterCussed"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatterCussed", "config.yml", pluginName);}
 		try{useChatterCussedMsg = config.getBoolean("useChatterCussedMsg");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("useChatterCussedMsg", "config.yml", pluginName);}
 		try{chatterCussedConsoleMsg = EPLib.formatColorCodes(config.getString("chatterCussedConsoleMsg"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("chatterCussedConsoleMsg", "config.yml", pluginName);}
 		try{useCussConsoleMsg = config.getBoolean("useCussConsoleMsg");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("useCussConsoleMsg", "config.yml", pluginName);}
 		try{blockedLinkMsg = EPLib.formatColorCodes(pluginName + config.getString("blockedLinkMsg"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("blockedLinkMsg", "config.yml", pluginName);}
 		try{showBlockedLinkMsg = config.getBoolean("showBlockedLinkMsg");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("showBlockedLinkMsg", "config.yml", pluginName);}
 		try{blockLinkMessages = config.getBoolean("blockLinkMessages");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("blockLinkMessages", "config.yml", pluginName);}
 		try{noSpamMessage = EPLib.formatColorCodes(config.getString("noSpamMessage"));
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("noSpamMessage", "config.yml", pluginName);}
 		try{enableSpamFilter = config.getBoolean("enableSpamFilter");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("enableSpamFilter", "config.yml", pluginName);}
 		try{percentOfCapitalLettersLimit = config.getInt("percentOfCapitalLettersLimit");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("percentOfCapitalLettersLimit", "config.yml", pluginName);}
 		try{capitalLetterGraceCount = config.getInt("capitalLetterGraceCount");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("capitalLetterGraceCount", "config.yml", pluginName);}
 		try{useChannels = channels.getBoolean("useChannels");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("useChannels", "channels.yml", pluginName);}
 		try{chChannel1 = channels.getStringList("Channel1Players");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("Channel1Players", "channels.yml", pluginName);}
 		try{chChannel2 = channels.getStringList("Channel2Players");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("Channel2Players", "channels.yml", pluginName);}
 		try{chAdmin = channels.getStringList("AdminChannelPlayers");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("AdminChannelPlayers", "channels.yml", pluginName);}
 		try{chConsole = channels.getStringList("ConsoleChannelPlayers");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("ConsoleChannelPlayers", "channels.yml", pluginName);}
 		try{chChannel1Prefix = channels.getString("Channel1Prefix");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("Channel1Prefix", "channels.yml", pluginName);}
 		try{chChannel2Prefix = channels.getString("Channel2Prefix");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("Channel2Prefix", "channels.yml", pluginName);}
 		try{chAdminPrefix = channels.getString("AdminChannelPrefix");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("AdminChannelPrefix", "channels.yml", pluginName);}
 		try{chConsolePrefix = channels.getString("ConsoleChannelPrefix");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("ConsoleChannelPrefix", "channels.yml", pluginName);}
 		try{channelType = channels.getString("channelType");
 		} catch (Exception e) {loadedAllVars = false;EPLib.unSpecifiedVarWarning("channelType", "channels.yml", pluginName);}
 		return loadedAllVars;
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public boolean onPlayerChat(AsyncPlayerChatEvent evt) {
 		Player chatter = evt.getPlayer();
 		if(enableChatManagement) {
 			if(useVaultChat && chat != null) {
 				String message = "";
 				String rawMsg = evt.getMessage();
 				if(chatter.hasPermission("ecm.chat.freecolor")) {
 					message = EPLib.formatColorCodes(rawMsg);
 					if(message.contains("&z"))
 						message.replaceAll("&z", "\n");
 				} else {
 					message = rawMsg;
 				}
 				if(enableCorrectMemes) {
 					message = EPLib.GrammarEnforcement(message, chatter, dataFolderName);
 				}
 				if(blockLinkMessages) {
 					String ptrn = "\\w+(\\.[^\\p{Digit}\\p{Space}]\\w+)+";
 					try{
 						Pattern p = Pattern.compile(ptrn);
 						Matcher m = p.matcher(message);
 						String chattedLinks = "";boolean foundALink = false;
 						while(m.find()) {
 							foundALink = true;
 							message = message.replaceAll(m.group(), "");
 							chattedLinks = chattedLinks.concat("\"" + m.group() + "\", ");
 						}
 						if((chatter != null) && (foundALink)) {
 							chattedLinks = chattedLinks + ".";chattedLinks = chattedLinks.replace(", .", "");
 							blockedLinkMsg = blockedLinkMsg.replaceAll("WORDS", chattedLinks);
 							if(showBlockedLinkMsg)
 								sendMessage(chatter, blockedLinkMsg);
 						}
 					} catch (PatternSyntaxException e) {
 						FileMgmt.LogCrash(e, "blockLinkMessage()", "A bad internal regex pattern was used: \"" + ptrn + "\". Contact Brian_Entei(The author of this plugin) at br45entei@gmail.com if you have a better pattern!", true, dataFolderName);
 					}
 				}
 				String didChatterCuss = ChatFilter(message);
 				if(didChatterCuss.equals("")) {
 					if(checkCAPSLock(message)) {
 						evt.setCancelled(true);
 						sendMessage(chatter, pluginName + "Please do not type in the chat with more than " + percentOfCapitalLettersLimit + "% of your letters being capitalized.&zYou can type a whole message of capital letters containing up to " + capitalLetterGraceCount + " " + fixPluralWord(capitalLetterGraceCount, "letter") + ".");
 						return false;
 					}
 					if(enableSpamFilter) {
 						String getChattersSpam = checkMsgForSpam(message);
 						if(!getChattersSpam.equals("")) {
 							evt.setCancelled(true);
 							sendMessage(chatter, noSpamMessage.replace("SPAMWORDS", getChattersSpam).replace("CHATTER", chatter.getDisplayName()));
 							return false;
 						}
 					}
 					String prevMessage = getLastPlayerMsg(chatter);
					if((message.equals("")) || (message == null)) {
 						evt.setCancelled(true);
 						return false;
					}if((showChatDebugMessages) && (!prevMessage.equals("")) && (prevMessage != null)) {
 						sendConsoleMessage(pluginName + chatter.getName() + " Chatted: \"" + rawMsg + "\";" + chatter.getDisplayName() + "'s previous msg: " + prevMessage);
 					}
 					if((enableTooSimilarFilter) && 
 						(EPLib.isSimilarTo(message, prevMessage))) {
 						evt.setCancelled(true);
 						if(useTooSimilarMsg) {
 							sendMessage(chatter, chatMsgTooSimilar);
 						}
 						return false;
 					}
 					if(enableChatFormat) {
 						if(!chatSayFormat.equals("") && chatSayFormat != null) {
 							if(!madeChatMsg) {sendConsoleMessage(pluginName + "&aUsing customized chat format from config.yml!");madeChatMsg = true;}
 							evt.setFormat(getPlayerChatFormat(chatter, true));
 							evt.setMessage(setLastPlayerMsg(chatter, message));
 							if(logChats) {EPLib.logChat((getPlayerChatFormat(chatter, false) + ": " + message).trim(), dataFolderName, chatLogFileName, chatFontSize);}
 							return true;
 						}
 						EPLib.showDebugMsg(pluginName + "Variable chatSayFormat was null, is it set in the config?", true);
 						if(!madeChatMsg) {sendConsoleMessage(pluginName + EPLib.yellow + "Failed to get variable chatSayFormat!\n&eWas it left blank, or accidentally deleted form the config.yml?");madeChatMsg = true;}
 						evt.setFormat(EPLib.formatColorCodes(chat.getPlayerPrefix(chatter)) + EPLib.rwhite + " <" + chatter.getDisplayName() + EPLib.rwhite + ">" + EPLib.formatColorCodes(chat.getPlayerSuffix(chatter)) + EPLib.rwhite + ": %2$s");
 						evt.setMessage(setLastPlayerMsg(chatter, message));
 						if(logChats) EPLib.logChat((chat.getPlayerPrefix(chatter) + EPLib.rwhite + " <" + chatter.getDisplayName() + EPLib.rwhite + ">" + chat.getPlayerSuffix(chatter) + EPLib.rwhite + ": " + message).trim(), dataFolderName, chatLogFileName, chatFontSize);
 						return true;
 					}
 					if(!madeChatMsg) {sendConsoleMessage(pluginName + "&6Chat formatting was disabled in the config.yml.\n&6Player usernames will be handled as if this plugin were not installed, except for nicknames.");madeChatMsg = true;}
 					if(logChats) {EPLib.logChat((chat.getPlayerPrefix(chatter) + EPLib.rwhite + " <" + chatter.getDisplayName() + EPLib.rwhite + ">" + chat.getPlayerSuffix(chatter) + EPLib.rwhite + ": " + message).trim(), dataFolderName, chatLogFileName, chatFontSize);return true;}
 					return false;
 				}
 				evt.setCancelled(true);
 				String cussMessage = chatterCussed;String consoleCussMsg = chatterCussedConsoleMsg;
 				consoleCussMsg = consoleCussMsg.replace("CHATTER", chatter.getDisplayName());consoleCussMsg = consoleCussMsg.replace("WORDS", didChatterCuss);
 				cussMessage = cussMessage.replace("CHATTER", chatter.getDisplayName());cussMessage = cussMessage.replace("WORDS", didChatterCuss);
 				if(useChatterCussedMsg) sendMessage(chatter, cussMessage);
 				if(useCussConsoleMsg) sendConsoleMessage(pluginName + consoleCussMsg);
 				return true;
 			}
 			if(!madeChatMsg) {sendConsoleMessage(pluginName + EPLib.yellow + "No vault detected; not managing chat.\n&6(No Vault plugin found, or coding problem?)");madeChatMsg = true;}
 			if(logChats) {EPLib.logChat((EPLib.rwhite + "<" + chatter.getDisplayName() + EPLib.rwhite + ">: " + evt.getMessage()).trim(), dataFolderName, chatLogFileName, chatFontSize);return true;}
 			return false;
 		}
 		if(!madeChatMsg) {sendConsoleMessage(pluginName + "&6Chat management was disabled in the config.yml; not managing chat.");madeChatMsg = true;}
 		if(logChats) {EPLib.logChat((EPLib.rwhite + "<" + chatter.getDisplayName() + EPLib.rwhite + ">: " + evt.getMessage()).trim(), dataFolderName, chatLogFileName, chatFontSize);return true;}
 		return false;
 	}
 	public static CommandSender setLastPlayerPm(CommandSender chatter, CommandSender target) {
 		if(lastPlayerInt < 200) {
 			lastPlayerPm.add(target);
 			lastPlayerPmer.add(chatter);
 			lastPlayerPmInt += 1;
 		} else try{
 			lastPlayerPm.clear();
 			lastPlayerPmer.clear();
 			lastPlayerPmInt = 0;
 			lastPlayerPm.add(target);
 			lastPlayerPmer.add(chatter);
 			lastPlayerPmInt += 1;
 		} catch (UnsupportedOperationException e) {
 			FileMgmt.LogCrash(e, "setLastPlayerPm()", "Unable to reset an internal list of player messages!", false, dataFolderName);
 		}
 		return target;
 	}
 	public static CommandSender getLastPlayerPm(String chatter) {
 		CommandSender target = null;
 		int playerPMListIntPosition = 0;
 		int x = 0;
 		Iterator<CommandSender> plyr = lastPlayerPmer.iterator();
 		while(plyr.hasNext()) {
 			x++;
 			CommandSender getPmmer = plyr.next();
 			if(getPmmer.getName().equals(chatter)) {
 				playerPMListIntPosition = x;
 			}
 		}
 		String debugInfo = "\"lastPlayerPm.size()\" equals: " + lastPlayerPm.size() + "; but the int \"playerPMListIntPosition\" variable equals: " + playerPMListIntPosition + " for player \"" + chatter + "\".";
 		if(playerPMListIntPosition > 0 && playerPMListIntPosition <= lastPlayerPm.size()) try{
 			target = lastPlayerPm.get(playerPMListIntPosition - 1);
 		} catch (IndexOutOfBoundsException e) {
 			FileMgmt.LogCrash(e, "getLastPlayerPm()", debugInfo, false, dataFolderName);
 		}
 		return target;
 	}
 	private static boolean setUpVaultRegistrations() {
 		boolean vaultAvailable = false;
 		boolean chatAvailable = false;
 		boolean permAvailable = false;
 		RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
 		RegisteredServiceProvider<Permission> permProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
 		if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
 			vaultAvailable = true;
 			if(chatProvider != null) {
 				chat = chatProvider.getProvider();
 				chatAvailable = true;
 				EPLib.showDebugMsg(pluginName + "&aInternal variable \"chat\" is not null! Chat formatting will be available.", showDebugMsgs);
 			} else {
 				EPLib.showDebugMsg(pluginName + "&4Could not load chat service... Chat formatting will not be available.(No Vault Plugin, or coding issue?)", showDebugMsgs);
 			}
 			if(permProvider != null) {
 				permission = permProvider.getProvider();
 				permAvailable = true;
 				EPLib.showDebugMsg(pluginName + "&aInternal variable \"permission\" is not null! Group Management will be available.", showDebugMsgs);
 			} else {
 				EPLib.showDebugMsg(pluginName + "&4Could not load permission service... Group Management will not be available.(No Vault Plugin, or coding issue?)", showDebugMsgs);
 			}
 		} else {
 			vaultAvailable = false;
 			chatAvailable = false;
 			permAvailable = false;
 			EPLib.showDebugMsg(pluginName + "&4Vault not detected; Chat formatting and group management will be disabled.", showDebugMsgs);
 		}
 		useVaultChat = chatAvailable;
 		useVaultPermissions = permAvailable;
 		return vaultAvailable;
 	}
 	public static boolean sendOneTimeMessage(String str, String target) {
 		if((str.equals("")) || (str.equals(null)) || (str == null)) return false;
 		boolean messageHasBeenSentBefore = false;
 		for(String curMsg : oneTimeMessageList) {
 			if(str.equalsIgnoreCase(curMsg)) {
 				messageHasBeenSentBefore = true;
 				break;
 			}
 		}
 		if(!messageHasBeenSentBefore) {
 			Player plyr = Bukkit.getServer().getPlayer(target);
 			if(plyr != null) {
 				sendMessage(plyr, str);
 			} else if((target.equalsIgnoreCase("console")) || (target.equals("!"))) {
 				sendConsoleMessage(str);
 			}
 			return true;
 		}
 		return false;
 	}
 	public static String broadcastMsg(String msg, boolean useFormat, String channel) {
 		if(useFormat == true) {msg = EPLib.formatColorCodes(msg);}
 		msg = msg.trim();
 		String ch = Channels.Channel(channel);
 		if(ch != null && useChannels == true) {
 			if(channelType.equals("List")) {
 				if(ch.equals(Channels.Public)) {
 					Bukkit.getServer().broadcastMessage(msg);
 				} else if(ch.equals(Channels.Ch1)) {
 					for(Object plyrName : chChannel1) {
 						if(plyrName instanceof String) {
 							Player curPlayer = Bukkit.getServer().getPlayer((String) plyrName);
 							if(curPlayer != null) {
 								sendMessage(curPlayer, chChannel1Prefix + msg);
 							}
 						}
 					}
 					sendConsoleMessage(chChannel1Prefix + msg);
 				} else if(ch.equals(Channels.Ch2)) {
 					for(Object plyrName : chChannel2) {
 						if(plyrName instanceof String) {
 							Player curPlayer = Bukkit.getServer().getPlayer((String) plyrName);
 							if(curPlayer != null) {
 								sendMessage(curPlayer, chChannel2Prefix + msg);
 							}
 						}
 					}
 					sendConsoleMessage(chChannel2Prefix + msg);
 				} else if(ch.equals(Channels.Administration)) {
 					for(Object plyrName : chAdmin) {
 						if(plyrName instanceof String) {
 							Player curPlayer = Bukkit.getServer().getPlayer((String) plyrName);
 							if(curPlayer != null) {
 								sendMessage(curPlayer, chAdminPrefix + msg);
 							}
 						}
 					}
 					sendConsoleMessage(chAdminPrefix + msg);
 				} else if(ch.equals(Channels.Console)) {
 					sendConsoleMessage(chAdminPrefix + msg);
 					for(Object plyrName : chConsole) {
 						if(plyrName instanceof String) {
 							Player curPlayer = Bukkit.getServer().getPlayer((String) plyrName);
 							if(curPlayer != null) {
 								sendMessage(curPlayer, chConsolePrefix + msg);
 							}
 						}
 					}
 				}
 			} else if(channelType.equals("Permission")) {
 				if(ch.equals(Channels.Public)) {
 					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 					for(Player curPlayer : onlinePlayers) {
 						if(curPlayer.hasPermission("enteischatmanager.chat.channels." + Channels.Public)) {
 							sendMessage(curPlayer, msg);
 						}
 					}
 					sendConsoleMessage(msg);
 				} else if(ch.equals(Channels.Ch1)) {
 					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 					for(Player curPlayer : onlinePlayers) {
 						if(curPlayer.hasPermission("enteischatmanager.chat.channels." + Channels.Ch1)) {
 							sendMessage(curPlayer, msg);
 						}
 					}
 					sendConsoleMessage(msg);
 				} else if(ch.equals(Channels.Ch2)) {
 					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 					for(Player curPlayer : onlinePlayers) {
 						if(curPlayer.hasPermission("enteischatmanager.chat.channels." + Channels.Ch2)) {
 							sendMessage(curPlayer, msg);
 						}
 					}
 					sendConsoleMessage(msg);
 				} else if(ch.equals(Channels.Administration)) {
 					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 					for(Player curPlayer : onlinePlayers) {
 						if(curPlayer.hasPermission("enteischatmanager.chat.channels." + Channels.Administration)) {
 							sendMessage(curPlayer, msg);
 						}
 					}
 					sendConsoleMessage(msg);
 				} else if(ch.equals(Channels.Console)) {
 					sendConsoleMessage(msg);
 					Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 					for(Player curPlayer : onlinePlayers) {
 						if(curPlayer.hasPermission("enteischatmanager.chat.channels." + Channels.Console)) {
 							sendMessage(curPlayer, msg);
 						}
 					}
 				}
 			} else {
 				sendConsoleMessage(pluginName + "&cError: Invalid string assigned to variable \"channelType\" in config.yml; valid ones are: 'List', 'Permission'");
 			}
 		} else {
 			EPLib.showDebugMsg("The setting \"useChannels\" is currently set to false, or there was an error loading the channel \"" + channel + "\".", false);
 			Bukkit.getServer().broadcastMessage(msg);
 		}
 		return msg;
 	}
 	public static double isNumber(String str) {double rtrn = 0.0D;if(Pattern.matches("[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*", str)) {return Double.valueOf(str).doubleValue();}return rtrn;}
 	public static String setLastPlayerMsg(Player chatter, String message) {
 		if(lastPlayerInt < 200) {
 			lastPlayerMessage.add(message);
 			lastPlayerMsgPlayer.add(chatter);
 			lastPlayerInt += 1;
 		} else {
 			try{
 				lastPlayerMessage.clear();
 				lastPlayerMsgPlayer.clear();
 				lastPlayerInt = 0;
 				int x = 0;
 				do{broadcastMsg("newLine".replaceAll("newLine", "\n"), false, Channels.Public);x++;}while(x < 200);
 				lastPlayerMessage.add(message);
 				lastPlayerMsgPlayer.add(chatter);
 				lastPlayerInt += 1;
 			} catch (UnsupportedOperationException e) {
 				FileMgmt.LogCrash(e, "setLastPlayerMessage()", "Failed to set the last chat message of \"" + chatter.getName() + "\" to: \"" + message + "\".", true, dataFolderName);
 			} 
 		}
 		return message;
 	}
 	public static String getLastPlayerMsg(Player chatter) {
 		String message = null;
 		int playerListIntPosition = 0;
 		int x = 0;
 		Iterator<CommandSender> plyr = lastPlayerMsgPlayer.iterator();
 		while(plyr.hasNext()) {
 			x++;
 			String playerName = plyr.next().getName();
 			if(playerName.equalsIgnoreCase(chatter.getName())) {
 				playerListIntPosition = x;
 			}
 		}
 		String debugInfo = "\"lastPlayerMessage.size()\" equals: " + lastPlayerMessage.size() + "; but the int \"playerListIntPosition\" variable equals: " + playerListIntPosition + ".";
 		if(playerListIntPosition > 0 && playerListIntPosition <= lastPlayerMessage.size()) {
 			try{message = lastPlayerMessage.get(playerListIntPosition - 1);
 			} catch (IndexOutOfBoundsException e) {
 				FileMgmt.LogCrash(e, "getLastPlayerMessage()", debugInfo, false, dataFolderName);
 				EPLib.showDebugMsg(debugInfo + " Check the server log or \"" + dataFolderName + "\\crash-reports.txt\" to solve the problem.", showDebugMsgs);
 			}
 		} else if((playerListIntPosition != 0 || playerListIntPosition != 1) && lastPlayerMessage.size() != 0) {
 			sendConsoleMessage(debugInfo);
 		}
 		return message;
 	}
 	public static String ChatFilter(String msg) {
 		msg = msg.toLowerCase();msg = msg.replaceAll("_", "");msg = msg.replaceAll("0", "");msg = msg.replaceAll("1", "");msg = msg.replaceAll("2", "");msg = msg.replaceAll("3", "");msg = msg.replaceAll("4", "");msg = msg.replaceAll("5", "");msg = msg.replaceAll("6", "");msg = msg.replaceAll("7", "");msg = msg.replaceAll("8", "");msg = msg.replaceAll("9", "");msg = msg.replaceAll("a", "");msg = msg.replaceAll("b", "");msg = msg.replaceAll("c", "");msg = msg.replaceAll("d", "");msg = msg.replaceAll("e", "");msg = msg.replaceAll("f", "");msg = msg.replaceAll("k", "");msg = msg.replaceAll("l", "");msg = msg.replaceAll("m", "");msg = msg.replaceAll("o", "");msg = msg.replaceAll("r", "");
 		ArrayList<String> cusswords = new ArrayList<String>();
 		cusswords.add("fuck");
 		cusswords.add("fu");
 		cusswords.add("fing");
 		cusswords.add("shit");
 		cusswords.add("shat");
 		cusswords.add("donkeyhole");
 		cusswords.add("ass");
 		cusswords.add("asshole");
 		cusswords.add("asswipe");
 		cusswords.add("arse");
 		cusswords.add("damn");
 		cusswords.add("dayum");
 		cusswords.add("dammit");
 		cusswords.add("a$$");
 		cusswords.add("bitch");
 		cusswords.add("b*");
 		cusswords.add("b*tch");
 		cusswords.add("bi*ch");
 		cusswords.add("bit*h");
 		cusswords.add("bitc*");
 		cusswords.add("a**");
 		cusswords.add("slut");
 		cusswords.add("whore");
 		cusswords.add("f*");
 		cusswords.add("skank");
 		cusswords.add("sex");
 		cusswords.add("muncher");
 		cusswords.add("fag");
 		cusswords.add("beaner");
 		cusswords.add("nigger");
 		cusswords.add("boob");
 		cusswords.add("dick");
 		cusswords.add("penis");
 		cusswords.add("vagina");
 		cusswords.add("cooch");
 		cusswords.add("butt");
 		cusswords.add("cracka");
 		cusswords.add("nigga");
 		cusswords.add("hell");
 		cusswords.add("cunt");
 		cusswords.add("cock");
 		cusswords.add("piss");
 		cusswords.add("tits");
 		cusswords.add("bastard");
 		cusswords.add("beaney");
 		cusswords.add("jock");
 		cusswords.add("raghead");
 		cusswords.add("cum");
 		cusswords.add("semen");
 		cusswords.add("sperm");
 		cusswords.add("queef");
 		cusswords.add("ejaculate");
 		cusswords.add("orgasm");
 		cusswords.add("orgasim");
 		cusswords.add("orgasum");
 		cusswords.add("orgi");
 		cusswords.add("intercourse");
 		cusswords.add("prostitute");
 		cusswords.add("dildo");
 		cusswords.add("doosh");
 		cusswords.add("tities");
 		cusswords.add("titties");
 		cusswords.add("rape");
 		cusswords.add("r*pe");
 		cusswords.add("trolo");
 		cusswords.add("ballsack");
 		cusswords.add("ball sack");
 		cusswords.add("balsac");
 		cusswords.add("ballsac");
 		cusswords.add("balsack");
 		cusswords.add("scrotum");
 		EPLib.showDebugMsg("The size of the cusswords list is: " + cusswords.size(), showDebugMsgs);
 		String[] words = msg.split("\\s+");
 		Iterator<String> it = cusswords.iterator();
 		String[] checkMsgForSpacedBadwords = msg.toLowerCase().replaceAll("\\p{Punct}+", "").replaceAll("\\s+", "").split("\\s+");
 		String returnStr = "";
 		returnStr = findGivenWords(it, words, "");
 		returnStr = returnStr + findGivenWords(it, checkMsgForSpacedBadwords, "");
 		return returnStr;
 	}
 	public static String checkMsgForSpam(String message) {
 		String getSpam = "\"";
 		String[] words = message.split("\\s+");
 		for(int i = 0;i < words.length;i++) {
 			String ptrn = "(?i)(?:([\\p{Graph}])\\1{4,})*";
 			String checkForSpamChars = "[^\\p{Alpha}]+";
 			Pattern p = Pattern.compile(ptrn);
 			Matcher m = p.matcher(words[i]);
 			if((m.matches()) || (words[i].replace(ptrn, "").length() * 100.0D / words[i].length() <= percentOfCapitalLettersLimit)) {
 				getSpam = getSpam + words[i] + "\", \"";
 			} else if(!message.replaceAll(ptrn, "").equals(message)) {
 				getSpam = getSpam + words[i] + "\", \"";
 			} else if(words[i].replace(checkForSpamChars, "").length() * 100 / words[i].length() <= percentOfCapitalLettersLimit) {
 				getSpam = getSpam + words[i] + "\", \"";
 			}
 		}
 		if(!getSpam.equals("\"")) {
 			getSpam = (getSpam + ".").replace(", \".", "");
 			return getSpam;
 		}
 		return "";
 	}
 	public static String fixPluralWord(int number, String word) {
 		String newWord = "";
 		if(number != 1) {
 			if((word.length() <= 3) && (word.equalsIgnoreCase("is"))) {
 				word = "are";
 				return word;
 			}
 			if(word.length() >= 4) {
 				String beginningOfWord = word.substring(0, word.length() - 3);
 				String endOfWord = word.substring(word.length() - 3, word.length());
 				ArrayList<String> suffixes = new ArrayList<String>();
 				suffixes.add("x");
 				suffixes.add("sh");
 				suffixes.add("ch");
 				suffixes.add("z");
 				suffixes.add("ss");
 				Iterator<String> it = suffixes.iterator();
 				boolean replacedSuffix = false;
 				while(it.hasNext()) {
 					if((endOfWord.contains(it.next())) && (!replacedSuffix)) {
 						endOfWord = endOfWord + "es";
 						replacedSuffix = true;
 					}
 				}
 				if(replacedSuffix) {
 					newWord = beginningOfWord + endOfWord;
 					return newWord;
 				}
 				return word + "s";
 			}
 			return word;
 		}if(word.equalsIgnoreCase("are")) {
 			word = "is";
 			return word;
 		}
 		if((word.endsWith("s")) && (word.length() >= 2)) {
 			return word.substring(0, word.length() - 1);
 		}
 		return word;
 	}
 	public static String findGivenWords(Iterator<String> givenWordList, String[] words, String checkAllChar) {
 		String foundWords = "";
 		boolean addedAWord = false;
 		while(givenWordList.hasNext()) {
 			String nextWord = givenWordList.next();
 			for (int i = 0;i < words.length;i++) {
 				words[i] = words[i].toLowerCase();
 				if(words[i].contains("*") == false) {
 					words[i] = words[i].replaceAll(getPunctuationChars, "");
 				}
 				if(nextWord.contains(checkAllChar) == true) {
 					String check = nextWord.replaceAll("", "");
 					if(words[i].contains(check)) {//hmm...
 						foundWords = foundWords + "\"" + check + "\", ";
 						addedAWord = true;
 					}
 				} else {
 					if(words[i].equals(nextWord)||words[i] == nextWord) {
 						foundWords = foundWords + "\"" + nextWord + "\", ";
 						addedAWord = true;
 					}
 				}
 			}
 		}
 		if(addedAWord) {
 			foundWords = foundWords + ".";foundWords = foundWords.replace(", .", "");
 			return foundWords;
 		}
 		return "";
 	}
 	public static boolean checkCAPSLock(String msg) {
 		String getCapitalLetters = "";
 		String Message = msg.replaceAll("[^\\p{Alpha}]+", "");
 		boolean foundACapitalLetter = false;
 		for(char c : Message.toCharArray()) {
 			if(Character.isUpperCase(c)) {
 				getCapitalLetters = getCapitalLetters + c;
 				foundACapitalLetter = true;
 			}
 		}
 		if(foundACapitalLetter) {
 			int percentOfCapitalLetters = (int)Math.floor(getCapitalLetters.length() * 100 / Message.length());
 			EPLib.showDebugMsg("&aDEBUG: (" + getCapitalLetters.length() + " * 100) / " + Message.length() + " == " + percentOfCapitalLetters, showDebugMsgs);
 			EPLib.showDebugMsg("&aNumber of capitalized letters: " + getCapitalLetters.length() + "; capital letters include: \"" + getCapitalLetters + "\".", showDebugMsgs);
 			if((percentOfCapitalLetters >= percentOfCapitalLettersLimit) && (Message.length() > capitalLetterGraceCount)) {
 				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 	public static String updatePlayerDisplayName(Player target, String newName, String newNameOrReload, boolean onlyGetNickName) {
 		if(enableCustomNicks) {
 			String nickname = "";
 			File newFolder = null;
 			File saveTo = null;
 			if(target != null) {
 				if(!onlyGetNickName) {
 					if(newNameOrReload.equals("newName")) {
 						nickname = EPLib.formatColorCodes(newName + EPLib.reset);
 						FileMgmt.WriteToFile(target.getName(), nickname, true, "Players", dataFolderName);
 						target.setDisplayName(nickname);
 					} else if(newNameOrReload.equals("reload")) {
 						File dataFolder = FileMgmt.getPluginFolder(dataFolderName);
 						newFolder = new File(dataFolder, "Players");
 						if(!newFolder.exists()) {
 							newFolder.mkdir();
 						}
 						saveTo = new File(newFolder, target.getName() + ".txt");
 						if(saveTo.exists()) {
 							nickname = FileMgmt.ReadFromFile(saveTo, dataFolderName);
 							if((!nickname.equals("")) && (nickname != null) && (!nickname.equals("null"))) {
 								nickname = EPLib.formatColorCodes(nickname);
 								target.setDisplayName(nickname);
 								return nickname;
 							}
 							nickname = target.getName() + EPLib.reset;
 							if(!onlyGetNickName) {
 								target.setDisplayName(nickname);
 							}
 							FileMgmt.WriteToFile(target.getName(), nickname, true, "Players", dataFolderName);
 							sendConsoleMessage(pluginName + EPLib.yellow + target.getName() + " had no stored nickname, so a new one was created for them: " + target.getDisplayName() + EPLib.yellow + ".");
 						} else {
 							nickname = target.getName() + EPLib.reset;
 							target.setDisplayName(nickname);
 							FileMgmt.WriteToFile(target.getName(), nickname, true, "Players", dataFolderName);
 							sendConsoleMessage(pluginName + EPLib.yellow + target.getName() + " had no nickname file, so a file was created for them with the nickname: " + target.getDisplayName() + EPLib.yellow + ".");
 							if((!onlyGetNickName) && 
 								(enableWelcomeMsg)) {
 								broadcastMsg(announcementDisplay + "&aPlease welcome " + getPlayerChatFormat(target, false) + "&r&a to the server!", true, Channels.Public);
 							}
 						}
 					}
 					return nickname;
 				}
 				newFolder = new File(dataFolderName, "Players");
 				if(!newFolder.exists()) {
 					newFolder.mkdir();
 				}
 				saveTo = new File(newFolder, target.getName() + ".txt");
 				if(saveTo.exists()) {
 					nickname = FileMgmt.ReadFromFile(saveTo, dataFolderName);
 					if((!nickname.equals("")) && (nickname != null) && (!nickname.equals("null"))) {
 						nickname = EPLib.formatColorCodes(nickname);
 						EPLib.showDebugMsg("Retrieved name from file: " + nickname, showDebugMsgs);
 						return nickname;
 					}
 					return target.getDisplayName();
 				}
 				return target.getDisplayName();
 			}
 			return "";
 		}
 		String returner = "";
 		if((target != null) && 
 			(target != null)) {
 			returner = target.getDisplayName();
 		}
 		return returner;
 	}
 	public static String getPlayerChatFormat(Player chatter, boolean useMessage) {
 		String newFormat = chatSayFormat;
 		newFormat = newFormat.replace("prefix", EPLib.formatColorCodes(chat.getPlayerPrefix(chatter)));
 		newFormat = newFormat.replace("name", chatter.getDisplayName());
 		newFormat = newFormat.replace("suffix", EPLib.formatColorCodes(chat.getPlayerSuffix(chatter)));
 		if(useMessage) {
 			return newFormat.replace("message", "%2$s");
 		}
 		newFormat = newFormat.replace("message", "");
 		newFormat = newFormat.replace(":", "");
 		newFormat = newFormat.trim();
 		return newFormat;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
 		String strArgs = "";
 		if(args.length != 0) {
 			strArgs = "";
 			int x = 0;
 			do{strArgs = strArgs.concat(args[x] + " ");x++;}while(x < args.length);
 		}
 		strArgs = strArgs.trim();
 		Player user = Bukkit.getServer().getPlayer(sender.getName());
 		String userName = sender.getName();
 		if(user != null) {
 			userName = user.getDisplayName();
 		}
 		if(userName.equals("")) {
 			userName = sender.getName();
 		}
 		if(command.equalsIgnoreCase("enteischatmanager") || command.equalsIgnoreCase("ecm")) {
 			if(args.length >= 1) {
 				if(args[0].equalsIgnoreCase("reload")) {
 					boolean userHasPerm = false;
 					if(user != null) {
 						userHasPerm = user.hasPermission("ecm.reload");
 					} else {
 						userHasPerm = true;
 					}
 					if(userHasPerm || user == null) {
 						boolean reloaded = LoadConfig();
 						if(reloaded) {
 							if(!sender.equals(console)) {
 								sendMessage(sender, pluginName + "&2Configuration files successfully reloaded!");
 							} else {
 								EPLib.showDebugMsg(pluginName + "&aYaml configuration files reloaded successfully!", showDebugMsgs);
 							}
 						} else if(!sender.equals(console)) {
 							sendMessage(sender, pluginName + "&cThere was an error when reloading the configuration files.");
 						} else {
 							EPLib.showDebugMsg(pluginName + "&eSome of the yaml configuration files failed to load successfully, check the server log for more information.", showDebugMsgs);
 						}
 					} else {
 						sendMessage(sender, noPerm);
 					}
 					return true;
 				}
 				if(args[0].equalsIgnoreCase("save")) {
 					boolean userHasPerm = false;
 					if(user != null) {
 						userHasPerm = user.hasPermission("ecm.save");
 					} else {
 						userHasPerm = true;
 					}
 					if((userHasPerm) || (user == null)) {
 						boolean saved = saveYamls();
 						if(saved) {
 							if(!sender.equals(console)) {
 								sendMessage(sender, pluginName + "&2The configuration files saved successfully!");
 							} else {
 								EPLib.showDebugMsg(pluginName + "&aThe yaml configuration files were saved successfully!", showDebugMsgs);
 							}
 						} else if(!sender.equals(console)) {
 							sendMessage(sender, pluginName + "&cThere was an error when saving the configuration files.");
 						} else {
 							EPLib.showDebugMsg(pluginName + "&eSome of the yaml configuration files failed to save successfully, check the crash-reports.txt file for more information.", showDebugMsgs);
 						}
 					} else {
 						sendMessage(sender, noPerm);
 					}
 					return true;
 				}
 				if(args[0].equalsIgnoreCase("info")) {
 					if(user.hasPermission("ecm.info") || user.hasPermission("ecm.*")) {
 						if(args.length == 1) {
 							String authors = "\"";
 							String curAuthor = "";
 							Iterator<String> it = pdffile.getAuthors().iterator();
 							while(it.hasNext()) {
 								authors = authors + curAuthor + "\", \"";
 								curAuthor = it.next();
 							}
 							if(!authors.equals("\"")) {authors = authors + "."; authors = authors.replace("\", \".", "\"");} else {
 								authors = "&oNone specified in plugin.yml!&r";
 							}
 							sendMessage(sender, EPLib.green + pdffile.getPrefix() + " " + pdffile.getVersion() + "; Main class: " + pdffile.getMain() + "; Author(s): (" + authors + "&2).");
 						} else {
 							sendMessage(sender, pluginName + "&eUsage: /" + command + " info");
 						}
 					} else {
 						sendMessage(sender, pluginName + noPerm);
 					}
 					return true;
 				}
 				EPLib.sendMessage(sender, pluginName + "&eUsage: \"/" + command + " info\" or use an admin command.");
 				return true;
 			}
 			EPLib.sendMessage(sender, pluginName + "&eUsage: \"/" + command + " info\" or use an admin command.");
 			return true;
		}if(command.equalsIgnoreCase("color")) {
 			if(!(sender instanceof Player)) {
 				if(args.length != 0) {
 					String mess = "";
 					int x = 0;
 					do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 					mess = "&eConsole: &r" + mess;
 					broadcastMsg(mess, true, Channels.Public);
 				} else {
 					sendMessage(sender, pluginName + "&eUsage: /" + command + " <message>. You can use '&' color codes and &z.");
 				}
			} else if((sender.hasPermission("ecm.chat.color")) || (sender.hasPermission("ecm.*"))) {
 				if(args.length != 0) {
 					String mess = "";
 					int x = 0;
 					do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 					user.chat(EPLib.formatColorCodes(mess));
 				} else {
 					sendMessage(sender, pluginName + "&eUsage: /" + command + " <message>. You can use '&' color codes and &z.");
 				}
 			} else sendMessage(sender, noPerm);
 			return true;
		}if(command.equalsIgnoreCase("custom")) {
 			if(!(sender instanceof Player)) {
 				if(args.length != 0) {
 					if(args[0].equals("impersonate")) {
 						if(args.length < 3) {
 							sendMessage(sender, pluginName + "&eUsage: /" + command.toLowerCase() + " impersonate <target> <message>. You can use '&' color codes and &z.");
 						} else {
 							Player target = Bukkit.getServer().getPlayer(args[1]);
 							if((target == null) || (!target.isOnline())) {
 								sendMessage(sender, pluginName + EPLib.yellow + args[1] + " is not online!");
 							} else if(((!target.getName().equals("Brian_Entei")) && (!target.isOp())) || (sender.isOp()) || (sender.equals(console))) {
 								String mess = "";
 								int x = 2;
 								do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 								target.chat(EPLib.formatColorCodes(mess));
 							} else {
 								broadcastMsg(userName + EPLib.rwhite + " has just tried to impersonate " + target.getDisplayName() + EPLib.rwhite + "! Shame on " + userName + EPLib.rwhite + "!", true, Channels.Public);
 							}
 						}
 					} else {
 						String mess = "";
 						int x = 0;
 						do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 						broadcastMsg(mess, true, Channels.Public);
 					}
 				} else sendMessage(sender, pluginName + "&eUsage: /" + command.toLowerCase() + " <message>. You can use '&' color codes and &z.");
 			} else if((sender.hasPermission("ecm.chat.custom")) || (sender.hasPermission("ecm.*"))) {
 				if(args.length != 0) {
 					if(args[0].equals("impersonate")) {
 						if((sender.hasPermission("ecm.chat.impersonate")) || (sender.hasPermission("ecm.*"))) {
 							if(args.length < 3) {
 								sendMessage(sender, pluginName + "&eUsage: /" + command.toLowerCase() + " impersonate <target> <message>. You can use '&' color codes and &z.");
 							} else {
 								Player target = Bukkit.getServer().getPlayer(args[1]);
 								if(target == null || !target.isOnline()) {
 									sendMessage(sender, pluginName + EPLib.yellow + args[1] + " is not online!");
 								} else {
 									String mess = "";
 									int x = 2;
 									do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 									mess = EPLib.formatColorCodes(mess);
 									if(!target.getName().equals("Brian_Entei")) {
 										target.chat(mess);
 									}
 								}
 							}
 						} else {
 							sendMessage(sender, noPerm);
 						}
 					} else {
 						String mess = "";
 						int x = 0;
 						do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 						if(!mess.contains("Server")) {
 							broadcastMsg(EPLib.rwhite + mess, true, Channels.Public);
 						} else {
 							sendMessage(sender, "&4You cannot impersonate the &r&f[&5Server&r&f]&4.");
 						}
 					}
 				} else {
 					sendMessage(sender, pluginName + "&eUsage: /" + command.toLowerCase() + " <message>. You can use '&' color codes and &z.");
 				}
 			} else {
 				sendMessage(sender, noPerm);
 			}
 			return true;
 		}
 		if(command.equalsIgnoreCase("nick")) {
 			if(enableCustomNicks) {
 				if(!(sender instanceof Player)) {
 					sendMessage(sender, playerOnly);
 				} else if((sender.hasPermission("ecm.chat.nick")) || (sender.hasPermission("ecm.*"))) {
 					if(args.length != 0) {
 						if(args[0].equals("reset")) {
 							String newName = user.getName() + EPLib.reset;
 							sendMessage(sender, pluginName + "&aYou reset your name back to: \"" + updatePlayerDisplayName(user, newName, "newName", false) + "\".");
 						} else if(args[0].equals("reload")) {
 							sendMessage(sender, pluginName + "&aYou have reloaded your saved nickname from file \"" + EPLib.reset + updatePlayerDisplayName(user, "", "reload", false) + "\".");
 						} else {
 							String mess = "";
 							int x = 0;
 							if(args.length == 1) {
 								mess = args[0];} else {
 								do{
 									mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 								mess = mess.trim();
 							}
 							mess = updatePlayerDisplayName(user, mess, "newName", false);
 							sendMessage(sender, pluginName + "&aYour new nickname is now \"" + mess + "&a\". Have fun! --" + EPLib.formatColorCodes("&6Brian&0_&4Entei"));
 						}
 					} else {
 						sendMessage(sender, pluginName + "&eUsage: /nick <newname> or /nick reset or /nick reload.\nCreates and stores a nickname for you.");
 					}
 				} else {
 					sendMessage(sender, noPerm);
 				}
 			} else {
 				sendMessage(sender, pluginName + "&4This command was disabled in the config.yml.");
 			}
 			return true;
 		}
 		if(command.equalsIgnoreCase("msg") || command.equalsIgnoreCase("message") || command.equalsIgnoreCase("pm") || command.equalsIgnoreCase("r")) {
 			if(command.equalsIgnoreCase("r")) {
 				CommandSender lastMsgdPlyr = getLastPlayerPm(sender.getName());
 				if(lastMsgdPlyr != null) {
 					if(args.length >= 1) {
 						String mess = "";
 						int x = 0;
 						do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 						sendMessage(lastMsgdPlyr, "&6[" + userName + "&_&6] &6sent you a msg: " + mess);
 						Player lastMsgedPlayer = Bukkit.getPlayer(lastMsgdPlyr.getName());
 						if(lastMsgedPlayer != null) {
 							sendMessage(sender, "&6[You --> &r" + lastMsgedPlayer.getDisplayName() + "&r&6]: &r&f" + mess);
 						} else {
 							sendMessage(sender, "&6[You --> &r" + lastMsgdPlyr.getName() + "&r&6]: &r&f" + mess);
 						}
 					} else {
 						sendMessage(sender, pluginName + "&6Usage: /" + command + "&6 <msg>");
 					}
 				} else sendMessage(sender, pluginName + "&6You have no one to whom you can reply.");
 				return true;
			}if((command.equalsIgnoreCase("msg")) || (command.equalsIgnoreCase("message")) || (command.equalsIgnoreCase("pm"))) {
				if((user != null) && 
					(!user.hasPermission("ecm.chat.msg")) && (!user.hasPermission("ecm.*"))) {
 					sendMessage(user, noPerm);
 					return true;
 				}
 				if(args.length >= 2) {
 					Player target = Bukkit.getServer().getPlayer(args[0]);
 					String mess = "";
 					int x = 1;
 					do{mess = mess.concat(args[x] + " ");x++;}while(x < args.length);
 					mess = mess.trim();
 					if(target != null) {
 						sendMessage(target, "&6[&r" + userName + "&r&6 --> You]: &r" + mess);
 						sendMessage(sender, "&6[You --> &r" + target.getDisplayName() + "&r&6]: &r&f" + mess);
 						setLastPlayerPm(sender, target);
 					} else if((args[0].equalsIgnoreCase("console")) || (args[0].equalsIgnoreCase("!"))) {
 						sendConsoleMessage("&6[&r" + userName + "&r&6 --> You]: &r" + mess);
 						sendMessage(sender, "&6[You --> &r" + console.getName() + "&r&6]: &r&f" + mess);
 						setLastPlayerPm(sender, console);
 					} else if(args[0].equalsIgnoreCase("rcon")) {
 						if(EPLib.rcon != null) {
 							EPLib.rcon.sendMessage(EPLib.formatColorCodes("&6[&r" + userName + "&r&6 --> You]: &r" + mess));
 							sendMessage(sender, "&6[You --> &r" + EPLib.rcon.getName() + "&r&6]: &r&f" + mess);
 							setLastPlayerPm(sender, EPLib.rcon);
 						} else {
 							sendMessage(sender, pluginName + "There is no active rcon connection to send a message to.");
 						}
 					} else {
 						sendMessage(sender, pluginName + args[0] + "&e is not online, or is not a player!");
 					}
 				} else {
 					sendMessage(sender, pluginName + "&6Usage: /" + command + "&6 <playerName> <msg>");
 				}
 				return true;
 			}
 			return true;
 		}
 		return false;
 	}
 	public static boolean updatePlayerGroup(World world, Player target) {
 		if(enableGroupManagement) {
 			boolean changedPlayersGroup = false;
 			EPLib.showDebugMsg("&aGroup Management Enabled: " + enableGroupManagement, showDebugMsgs);
 			String worldGroupPermission = "";
 			String wildcardGroupPermission = "";
 			if(useVaultPermissions) {
 				if(!target.isOp()) {
 					if(enableGroupManagement) {
 						String worldName = world.getName();
 						if(worldName.equalsIgnoreCase("world")) {
 							worldName = "Spawn World";
 						} else if(worldName.equalsIgnoreCase("build")) {
 							worldName = "Build World";
 						} else if(worldName.equalsIgnoreCase("skyworld")) {
 							worldName = "SkyWorld";
 						}
 						String[] groups = permission.getGroups();
 						for(String groupName : groups) {
 							if(enableUpdateGroupWorldSuffix) {
 								chat.setGroupSuffix(world, groupName, EPLib.reset + "" + EPLib.white + " [" + EPLib.gold + worldName + EPLib.reset + EPLib.white + "]");
 							}
 							worldGroupPermission = "ecm." + world.getName().toLowerCase() + "." + groupName.toLowerCase();
 							wildcardGroupPermission = "ecm.*." + groupName.toLowerCase();
 							boolean targetHasPermission = (permission.has(target, wildcardGroupPermission)) || (permission.has(target, worldGroupPermission));
 							if(targetHasPermission) {
 								EPLib.showDebugMsg("&w" + target.getName() + "&a has the permission: " + worldGroupPermission, showDebugMsgs);
 								if(!permission.playerInGroup(target, groupName)) {
 									EPLib.showDebugMsg("&w" + target.getName() + "&a is not in the group \"&3" + groupName + "&a\", but has the permission \"&3" + worldGroupPermission + "&a\"; setting " + target.getName() + "&a's group to \"&3" + groupName + "&a\" and removing other groups.", true);
 									boolean addedPlayerToNewGroup = permission.playerAddGroup(target, groupName);
 									if(addedPlayerToNewGroup) {
 										changedPlayersGroup = true;
 									} else {
 										sendConsoleMessage(pluginName + "&eError: could not set " + target.getName() + "'s group to \"" + groupName + "\"!");
 									}
 								} else {
 									EPLib.showDebugMsg("&w" + target.getName() + "&a is already in the group: &3" + groupName + "&a, so their group is current for the world: &3" + worldName, showDebugMsgs);
 								}
 								for(String removeOtherGroups : permission.getGroups()) {
 									if(!removeOtherGroups.equals(groupName)) {
 										boolean removedOneOfPlayersGroups = permission.playerRemoveGroup(target, removeOtherGroups);
 										if(removedOneOfPlayersGroups) {
 											changedPlayersGroup = true;
 										}
 									}
 								}
 								return changedPlayersGroup;
 							}
 						}
 					} else {
 						sendOneTimeMessage(pluginName + "&cconfig.yml variable \"enableGroupManagement\" equals: " + enableGroupManagement + "&c, not managing groups.", "console");
 						return changedPlayersGroup;
 					}
 				} else {
 					EPLib.showDebugMsg("&aThe player \"&r" + target.getName() + "\"&a is an operator, so they cannot benefit from the functionality of per-world groups due to technical difficulties with permissions.", true);
 				}
 			} else {
 				sendConsoleMessage(pluginName + "&4Error: variable useVaultPermissions equals " + useVaultPermissions + "&4!");
 				return changedPlayersGroup;
 			}
 			return changedPlayersGroup;
 		}
 		return false;
 	}
 	
 }
