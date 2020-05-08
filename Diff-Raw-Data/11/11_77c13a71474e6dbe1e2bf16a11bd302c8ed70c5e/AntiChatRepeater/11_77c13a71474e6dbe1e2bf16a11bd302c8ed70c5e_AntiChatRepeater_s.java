 package au.com.addstar.pandora.modules;
 
 import java.io.File;
 import java.util.AbstractMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.WeakHashMap;
 
 import net.ess3.api.IEssentials;
 import net.ess3.api.IUser;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.scoreboard.Team;
 
 import com.earth2me.essentials.utils.FormatUtil;
 
 import au.com.addstar.pandora.AutoConfig;
 import au.com.addstar.pandora.ConfigField;
 import au.com.addstar.pandora.MasterPlugin;
 import au.com.addstar.pandora.Module;
 import au.com.addstar.pandora.Utilities;
 
 public class AntiChatRepeater implements Module, Listener
 {
 	private WeakHashMap<Player, Entry<String, Long>> mLastChat = new WeakHashMap<Player, Entry<String, Long>>();
 	
 	private Config mConfig;
 	private IEssentials mEssentials;
 	
 	private void sendFakeChat(Player player, String message)
 	{
 		IUser user = mEssentials.getUser(player);
 		
 		if(user == null)
 			return;
 		
 		message = FormatUtil.formatMessage(user, "essentials.chat", message);
 		
 		String group = user.getGroup();
         String world = player.getName();
         Team team = player.getScoreboard().getPlayerTeam(user.getBase());
 
         String format = mEssentials.getSettings().getChatFormat(group);
         format = format.replace("{0}", group);
         format = format.replace("{1}", world);
         format = format.replace("{2}", world.substring(0, 1).toUpperCase(Locale.ENGLISH));
         format = format.replace("{3}", team == null ? "" : team.getPrefix());
         format = format.replace("{4}", team == null ? "" : team.getSuffix());
         format = format.replace("{5}", team == null ? "" : team.getDisplayName());
         
         message = String.format(format, player.getDisplayName(), message);
         
         player.sendMessage(message);
 	}
 	
 	private boolean isCommandAllowed(String cmdString)
 	{
 		String[] parts = cmdString.split(" ");
 		if(parts.length == 0)
 			return true;
 		
		Command cmd = Bukkit.getPluginCommand(parts[0].substring(1));
 		
		if(cmd == null)
			return false;
 		
		if(mConfig.commands.contains(cmd.getName()))
 			return mConfig.isWhitelist;
 		else
 			return !mConfig.isWhitelist;
 	}
 	
 	private boolean isRepeat(Player player, String message)
 	{
 		if(!mLastChat.containsKey(player))
 			return false;
 		
 		Entry<String, Long> entry = mLastChat.get(player);
 		
 		if(!entry.getKey().equalsIgnoreCase(message))
 			return false;
 		
 		return (System.currentTimeMillis() - entry.getValue()) < mConfig.timeout;
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	private void onPlayerChat(AsyncPlayerChatEvent event)
 	{
 		if(event.getPlayer().hasPermission("pandora.chatrepeat.bypass"))
 			return;
 		
 		if(isRepeat(event.getPlayer(), event.getMessage()))
 		{
 			event.setCancelled(true);
 			sendFakeChat(event.getPlayer(), event.getMessage());
 		}
 		
 		mLastChat.put(event.getPlayer(), new AbstractMap.SimpleEntry<String, Long>(event.getMessage(), System.currentTimeMillis()));
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	private void onPlayerCommand(PlayerCommandPreprocessEvent event)
 	{
 		if(event.getPlayer().hasPermission("pandora.chatrepeat.bypass") || isCommandAllowed(event.getMessage()))
 			return;
 		
 		if(isRepeat(event.getPlayer(), event.getMessage()))
 			event.setCancelled(true);
 		
 		mLastChat.put(event.getPlayer(), new AbstractMap.SimpleEntry<String, Long>(event.getMessage(), System.currentTimeMillis()));
 	}
 	
 	@Override
 	public void onEnable() 
 	{
 		if(mConfig.load())
 			mConfig.save();
 		
 	}
 
 	@Override
 	public void onDisable()	{}
 
 	@Override
 	public void setPandoraInstance( MasterPlugin plugin ) 
 	{
 		mEssentials = (IEssentials)Bukkit.getPluginManager().getPlugin("Essentials");
 		if(mEssentials == null)
 			throw new RuntimeException("Cannot load essentials");
 		
 		mConfig = new Config(new File(plugin.getDataFolder(), "AntiChatRepeater.yml"));
 	}
 
 	private static class Config extends AutoConfig
 	{
 		Config(File file)
 		{
 			super(file);
 		}
 		
 		@ConfigField
 		public boolean isWhitelist = false;
 		
 		@ConfigField(comment="Use just the name the command (without the /) in lowercase.\nDont bother with aliases, aliases are resolved to the real command before being matched against these commands.")
 		public HashSet<String> commands = new HashSet<String>();
 		
 		@ConfigField(name="timeout", comment="The time specified as a date diff, after which the same message/command can be repeated.")
 		private String timeoutStr = "10s";
 		
 		public long timeout;
 		
 		@Override
 		protected void onPostLoad() throws InvalidConfigurationException
 		{
 			timeout = Utilities.parseDateDiff(timeoutStr);
 			if(timeout <= 0)
 			{
 				timeout = 10000;
 				throw new InvalidConfigurationException("Invalid timeout value \"" + timeoutStr + "\"");
 			}
 		}
 	}
 }
