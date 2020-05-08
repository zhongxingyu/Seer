 package uk.co.jacekk.bukkit.simpleircformatter;
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import uk.co.jacekk.bukkit.baseplugin.v9.event.BaseListener;
 import uk.co.jacekk.bukkit.baseplugin.v9.util.ChatUtils;
 import uk.co.jacekk.bukkit.simpleirc.RemotePlayerChatEvent;
 
 public class ChatListener extends BaseListener<SimpleIRCFormatter> {
 	
 	private Chat chat;
 	
 	public ChatListener(SimpleIRCFormatter plugin){
 		super(plugin);
 		
 		RegisteredServiceProvider<Chat> chatProvider = plugin.server.getServicesManager().getRegistration(Chat.class);
 		
 		if (chatProvider != null){
 			this.chat = chatProvider.getProvider();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerRemoteChat(RemotePlayerChatEvent event){
 		String playerName = event.getPlayerName();
 		Player player = plugin.server.getPlayer(playerName);
 		String worldName = (player == null) ? plugin.server.getWorlds().get(0).getName() : player.getWorld().getName();
 		String message = ChatColor.stripColor(event.getMessage());
 		
 		if (message.isEmpty()){
 			event.setCancelled(true);
 		}
 		
 		String ircPrefix = ChatUtils.parseFormattingCodes(plugin.config.getString(Config.IRC_PREFIX));
		
		String prefix = "";
		String suffix = "";
		
		if (this.chat != null){
			prefix = this.chat.getPlayerPrefix(worldName, playerName);
			suffix = this.chat.getPlayerSuffix(worldName, playerName);
		}
 		
 		if (prefix.isEmpty()){
 			prefix = ChatUtils.parseFormattingCodes(plugin.config.getString(Config.DEFAULT_PREFIX));
 		}
 		
 		if (suffix.isEmpty()){
 			suffix = ChatUtils.parseFormattingCodes(plugin.config.getString(Config.DEFAULT_SUFFIX));
 		}
 		
 		event.setFormat(ircPrefix + prefix + "%s" + suffix + "%s");
 	}
 	
 }
