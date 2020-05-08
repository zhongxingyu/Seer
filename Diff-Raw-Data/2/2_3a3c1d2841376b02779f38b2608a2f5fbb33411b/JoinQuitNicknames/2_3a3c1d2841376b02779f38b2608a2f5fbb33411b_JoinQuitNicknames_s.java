 package au.com.addstar.pandora.modules;
 
 import net.ess3.api.IEssentials;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.earth2me.essentials.User;
 
 import au.com.addstar.pandora.MasterPlugin;
 import au.com.addstar.pandora.Module;
 
 public class JoinQuitNicknames implements Module, Listener
 {
 	private boolean ModuleEnabled = false;
 	private IEssentials mEssentials;
 
 	@EventHandler(ignoreCancelled=true)
 	private void onPlayerJoin(PlayerJoinEvent event)
 	{
 		String msg = getNicknamedMsg(event.getJoinMessage(), event.getPlayer());
 		if (msg != null)
 			event.setJoinMessage(msg);
 	}
 
 	@EventHandler(ignoreCancelled=true)
 	private void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String msg = getNicknamedMsg(event.getQuitMessage(), event.getPlayer());
 		if (msg != null)
 			event.setQuitMessage(msg);
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	private void onPlayerKick(PlayerKickEvent event) {
 		String msg = getNicknamedMsg(event.getLeaveMessage(), event.getPlayer());
 		if (msg != null)
 			event.setLeaveMessage(msg);
 	}
 	
 	private String getNicknamedMsg(String origmsg, Player p)
 	{
 		if (ModuleEnabled)
 		{
 			if (p == null)
 				return null;
 			
 			User user = mEssentials.getUser(p);
 			if (user == null)
 				return null;
 			
 			String nickname = ChatColor.stripColor(user.getNickname());
			if ((!nickname.isEmpty()) && (!p.getName().equals(nickname)))
 				return origmsg.replaceAll(p.getName(), nickname);
 		}
 		return null;
 	}
 
 	@Override
 	public void onEnable()
 	{
 		mEssentials = (IEssentials)Bukkit.getPluginManager().getPlugin("Essentials");
 		if(mEssentials == null)
 			throw new RuntimeException("Cannot load essentials");
 		
 		ModuleEnabled = true;
 	}
 
 	@Override
 	public void onDisable()
 	{
 		ModuleEnabled = false;
 	}
 
 	@Override
 	public void setPandoraInstance( MasterPlugin plugin ) {}
 
 }
