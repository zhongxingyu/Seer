 package no.runsafe.nchat.handlers;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.hook.IPlayerNameDecorator;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.nchat.Constants;
 import no.runsafe.nchat.Globals;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 
 import java.util.*;
 
 public class ChatHandler implements IConfigurationChanged, IPlayerNameDecorator
 {
     public ChatHandler(IConfiguration configuration, Globals globals)
     {
         this.configuration = configuration;
         this.globals = globals;
     }
 
     public String convertColors(String theString)
     {
         for (int i = 0; i < ChatColor.values().length; i++)
         {
             ChatColor theColor = ChatColor.values()[i];
             theString = theString.replaceAll(String.format(Constants.COLOR_CHARACTER, theColor.getChar()), theColor.toString());
         }
 
         return theString;
     }
 
     public String stripColors(String theString)
     {
         for (int i = 0; i < ChatColor.values().length; i++)
         {
             ChatColor theColor = ChatColor.values()[i];
             theString = theString.replaceAll(String.format(Constants.COLOR_CHARACTER, theColor.getChar()), "");
         }
 
         return theString;
     }
 
     public String getGroupPrefix(RunsafePlayer player)
     {
 		return (String) this.chatGroupPrefixes.get(player.getGroups().get(0).toLowerCase());
     }
 
 	public String getTabListPrefix(RunsafePlayer player)
 	{
		return (String) this.tabListPrefixes.get(player.getGroups().get(0).toLowerCase());
 	}
 
     public String getWorldPrefix(String worldName)
     {
         String worldPrefix = (String) this.worldPrefixes.get(worldName);
 
         if (worldPrefix != null)
             return worldPrefix;
 
         return "";
     }
 
     public String getPlayerNickname(RunsafePlayer player, String nameString)
     {
 		String playerName = player.getName();
         String playerNickname = (String) this.playerNicknames.get(playerName);
 
         if (playerNickname != null)
             return nameString.replace(playerName, playerNickname);
 
         return nameString;
     }
 
     public List<String> getPlayerTags(String playerName)
     {
         List<String> returnTags = new ArrayList<String>();
         List<String> playerTags = this.playerTags.getStringList(playerName);
 
         if (playerTags != null)
         {
             for (Iterator<String> i = playerTags.iterator(); i.hasNext();)
             {
                 String tag = i.next();
                 returnTags.add(String.format(this.playerTagFormat, tag));
             }
         }
 
         return returnTags;
     }
 
 	public String formatPlayerName(RunsafePlayer player, String editedName)
 	{
 		String formatName = this.playerNameFormat;
 		String worldName = (player.isOnline() && !player.isVanished()) ? player.getWorld().getName() : "console";
 
 		HashMap<String, String> replacements = new HashMap<String, String>();
 
 		if (formatName == null) return null;
 
 		replacements.put(Constants.FORMAT_OP, (this.enableOpTag && player.isOP()) ? this.opTagFormat : "");
 		replacements.put(Constants.FORMAT_WORLD, (this.enableWorldPrefixes) ? this.getWorldPrefix(worldName) : worldName);
 		replacements.put(Constants.FORMAT_GROUP, (this.enableChatGroupPrefixes) ? this.getGroupPrefix(player) : "");
 		replacements.put(Constants.FORMAT_TAG, (this.enablePlayerTags) ? this.globals.joinList(this.getPlayerTags(player.getName())) : "");
 		replacements.put(Constants.FORMAT_PLAYER_NAME, (this.enableNicknames) ? this.getPlayerNickname(player, editedName) : editedName);
 
 		formatName = this.globals.mapReplace(formatName, replacements);
 		return this.convertColors(formatName);
 	}
 
 	@Override
 	public String DecorateName(RunsafePlayer runsafePlayer, String s)
 	{
 		return this.formatPlayerName(runsafePlayer, s);
 	}
 
 	public String formatChatMessage(String message, RunsafePlayer player)
     {
 		return this.formatMessage(message, player, this.playerChatMessage);
     }
 
 	public String formatPlayerSystemMessage(String message, RunsafePlayer player)
 	{
 		return this.formatMessage(message, player, this.playerSystemMessage);
 	}
 
 	private String formatMessage(String message, RunsafePlayer player, String formatMessage)
 	{
 		String playerName = this.formatPlayerName(player, player.getName());
 		message = message.replace("%", "%%");
 
 		if (!player.hasPermission("runsafe.nchat.colors") && !this.enableColorCodes)
 			message = this.stripColors(message);
 
 		formatMessage = formatMessage.replace(Constants.FORMAT_MESSAGE, message);
 		formatMessage = formatMessage.replace(Constants.FORMAT_PLAYER_NAME, playerName);
 
 		return this.convertColors(formatMessage);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.chatGroupPrefixes = configuration.getSection("chatGroupPrefixes");
 		this.worldPrefixes = configuration.getSection("worldPrefixes");
 		this.playerNicknames = configuration.getSection("playerNicknames");
 		this.playerTags = configuration.getSection("playerTags");
 		this.playerTagFormat = configuration.getConfigValueAsString("chatFormatting.playerTagFormat");
 		this.playerNameFormat = configuration.getConfigValueAsString("chatFormatting.playerName");
 		this.playerChatMessage = configuration.getConfigValueAsString("chatFormatting.playerChatMessage");
 		this.playerSystemMessage = configuration.getConfigValueAsString("chatFormatting.playerSystemMessage");
 		this.enableWorldPrefixes = configuration.getConfigValueAsBoolean("nChat.enableWorldPrefixes");
 		this.enableChatGroupPrefixes = configuration.getConfigValueAsBoolean("nChat.enableChatGroupPrefixes");
 		this.enableNicknames = configuration.getConfigValueAsBoolean("nChat.enableNicknames");
 		this.enablePlayerTags = configuration.getConfigValueAsBoolean("nChat.enablePlayerTags");
 		this.enableOpTag = configuration.getConfigValueAsBoolean("nChat.enableOpTag");
 		this.enableColorCodes = configuration.getConfigValueAsBoolean("nChat.enableColorCodes");
 		this.opTagFormat = configuration.getConfigValueAsString("chatFormatting.opTagFormat");
 		this.tabListPrefixes = configuration.getSection("tabListGroupPrefix");
 	}
 
 	private ConfigurationSection tabListPrefixes;
     private Globals globals;
     private boolean enableWorldPrefixes;
     private boolean enableChatGroupPrefixes;
     private boolean enableNicknames;
     private boolean enablePlayerTags;
     private boolean enableOpTag;
 	private boolean enableColorCodes;
     private IConfiguration configuration;
     private ConfigurationSection chatGroupPrefixes;
     private ConfigurationSection worldPrefixes;
     private ConfigurationSection playerNicknames;
     private ConfigurationSection playerTags;
     private String playerTagFormat;
     private String opTagFormat;
     private String playerChatMessage;
 	private String playerSystemMessage;
 	private String playerNameFormat;
 }
