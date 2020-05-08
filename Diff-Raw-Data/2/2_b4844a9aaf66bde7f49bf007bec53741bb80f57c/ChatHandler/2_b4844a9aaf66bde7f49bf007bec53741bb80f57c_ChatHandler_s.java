 package no.runsafe.nchat.handlers;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.hook.IPlayerNameDecorator;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.nchat.Constants;
 import no.runsafe.nchat.Globals;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ChatHandler implements IConfigurationChanged, IPlayerNameDecorator
 {
 	public ChatHandler(Globals globals, IOutput output, RegionHandler regionHandler)
 	{
 		this.globals = globals;
 		this.console = output;
 		this.regionHandler = regionHandler;
 	}
 
 	public String getGroupPrefix(RunsafePlayer player)
 	{
 		if (!player.getGroups().isEmpty())
 		{
 			String groupName = player.getGroups().get(0).toLowerCase();
 			if (this.chatGroupPrefixes.containsKey(groupName))
 				return this.chatGroupPrefixes.get(groupName);
 		}
 		return "";
 	}
 
 	public String getTabListPrefixedName(RunsafePlayer player)
 	{
 		String firstGroup = (player.isVanished() ? "vanish" : player.getGroups().get(0).toLowerCase());
 		String playerName = player.getName();
 		String prefix = (this.tabListPrefixes.containsKey(firstGroup)) ? this.tabListPrefixes.get(firstGroup) : "";
 		int nameLength = 16 - prefix.length();
 		String displayName = (playerName.length() > nameLength) ? playerName.substring(0, nameLength) : playerName;
 		return prefix + displayName;
 	}
 
 	public void refreshPlayerTabListName(RunsafePlayer player)
 	{
 		player.setPlayerListName(ChatColour.ToMinecraft(this.getTabListPrefixedName(player)));
 	}
 
 	public String getWorldPrefix(String worldName)
 	{
 		if (worldPrefixes.containsKey(worldName))
 			return this.worldPrefixes.get(worldName);
 
 		return "";
 	}
 
 	public String getPlayerNickname(RunsafePlayer player, String nameString)
 	{
 		String playerName = player.getName();
 		if (playerNicknames.containsKey(playerName))
 			return nameString.replace(playerName, playerNicknames.get(playerName));
 
 		return nameString;
 	}
 
 	public List<String> getPlayerTags(String playerName)
 	{
 		List<String> returnTags = new ArrayList<String>();
 		if (playerTags.containsKey(playerName))
 			for (String tag : playerTags.get(playerName))
 				returnTags.add(String.format(this.playerTagFormat, tag));
 
 		return returnTags;
 	}
 
 	public String formatPlayerName(RunsafePlayer player, String editedName)
 	{
 		String formatName = this.playerNameFormat;
 		String worldName = (player.isOnline() && !player.isVanished() && player.getWorld() != null) ? player.getWorld().getName() : "console";
 
 		HashMap<String, String> replacements = new HashMap<String, String>();
 
 		if (formatName == null) return null;
 
 		replacements.put(Constants.FORMAT_OP, (this.enableOpTag && player.isOP()) ? this.opTagFormat : "");
 
 		String worldReplace = worldName;
 		if (this.enableWorldPrefixes)
 		{
 			if (this.enableRegionPrefixes)
 			{
 				String regionTag = this.regionHandler.getRegionTag(player);
 				if (regionTag != null)
 					worldReplace = regionTag;
 			}
 			else
 			{
 				worldReplace = this.getWorldPrefix(worldName);
 			}
 		}
 
 		replacements.put(Constants.FORMAT_WORLD, worldReplace);
 		replacements.put(Constants.FORMAT_GROUP, (this.enableChatGroupPrefixes) ? this.getGroupPrefix(player) : "");
 		replacements.put(Constants.FORMAT_TAG, (this.enablePlayerTags) ? this.globals.joinList(this.getPlayerTags(player.getName())) : "");
 		replacements.put(Constants.FORMAT_PLAYER_NAME, (this.enableNicknames) ? this.getPlayerNickname(player, editedName) : editedName);
 
 		formatName = this.globals.mapReplace(formatName, replacements);
 		return formatName;
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
 		return ChatColour.ToMinecraft(this.formatMessage(message, player, this.playerSystemMessage));
 	}
 
 	private String formatMessage(String message, RunsafePlayer player, String formatMessage)
 	{
 		String playerName = this.formatPlayerName(player, player.getName());
 		message = message.replace("%", "%%");
 		console.fine("Formatting message '%s' for '%s' into '%s'", message, player.getName(), formatMessage);
 		if (!(this.enableColorCodes || player.hasPermission("runsafe.nchat.colors")))
 		{
 			message = ChatColour.Strip(message);
 			console.fine("Stripped codes making message '%s'", message);
 		}
 		formatMessage = formatMessage.replace(Constants.FORMAT_MESSAGE, message);
 		formatMessage = formatMessage.replace(Constants.FORMAT_PLAYER_NAME, playerName);
 		console.fine("Returning formatted message '%s'", formatMessage);
 		return formatMessage;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.chatGroupPrefixes = configuration.getConfigValuesAsMap("chatGroupPrefixes");
 		this.worldPrefixes = configuration.getConfigValuesAsMap("worldPrefixes");
 		this.playerNicknames = configuration.getConfigValuesAsMap("playerNicknames");
 		this.playerTags = configuration.getConfigSectionsAsList("playerTags");
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
 		this.tabListPrefixes = configuration.getConfigValuesAsMap("tabListGroupPrefix");
		this.enableRegionPrefixes = configuration.getConfigValueAsBoolean("enableRegionPrefixes");
 	}
 
 	private Map<String, String> tabListPrefixes;
 	private final Globals globals;
 	private final IOutput console;
 	private boolean enableWorldPrefixes;
 	private boolean enableChatGroupPrefixes;
 	private boolean enableNicknames;
 	private boolean enablePlayerTags;
 	private boolean enableOpTag;
 	private boolean enableColorCodes;
 	private boolean enableRegionPrefixes;
 	private Map<String, String> chatGroupPrefixes;
 	private Map<String, String> worldPrefixes;
 	private Map<String, String> playerNicknames;
 	private Map<String, List<String>> playerTags;
 	private String playerTagFormat;
 	private String opTagFormat;
 	private String playerChatMessage;
 	private String playerSystemMessage;
 	private String playerNameFormat;
 	private RegionHandler regionHandler;
 }
