 package net.minebot.causeofdeath;
 
 import java.util.Stack;
 
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.craftbukkit.command.ColouredConsoleSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class DeathListener implements Listener {
 
 	private CauseOfDeath plugin;
 	private DeathMessages dm;
 	
 	public DeathListener(CauseOfDeath plugin, DeathMessages dm) {
 		this.plugin = plugin;
 		this.dm = dm;
 	}
 	
	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (!(event.getEntity() instanceof Player)) return;
 		
 		Player p = (Player)(event.getEntity());
 		Coroner coroner = new Coroner(event);
 		String message = dm.getDeathMessage(p, coroner);
 		
 		if (message != null) {
 			((PlayerDeathEvent)event).setDeathMessage(message);
 			ConsoleCommandSender ccs = ColouredConsoleSender.getInstance();
 			ccs.sendMessage("[CauseOfDeath] " + message);
 		}
 		else {
 			//Leave it alone, but note why we couldn't make a message
 			StringBuilder sb = new StringBuilder();
 			Stack<String> causes = coroner.getCauses();
 			while (causes.size() > 0) {
 				sb.append(causes.pop() + " ");
 			}
 			plugin.getLogger().warning("Failed to make a death message! Causes: (" + sb.toString() + ")");
 			plugin.getLogger().warning("Default message: " + ((PlayerDeathEvent)event).getDeathMessage());
 		}
 		
 	}
 	
 }
