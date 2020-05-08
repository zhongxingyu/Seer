 package strimy.bukkit.plugins.showdamage;
 
import java.util.HashMap;

 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class SDEntityListener extends EntityListener  
 {
	HashMap<Player, LastDamage> lastDamages = new HashMap<Player, LastDamage>();
 	ShowDamage plugin;
 	
 	public SDEntityListener(ShowDamage plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public void onEntityDamage(EntityDamageEvent event) 
 	{
 		if(!ShowDamageManager.Instance.isPluginEnabled())
 			return;
 		
 		if(event instanceof EntityDamageByEntityEvent)
 		{
 			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent)event;
 			Entity damager = entityEvent.getDamager();
 			
 			if(damager instanceof Player && entityEvent.getEntity() instanceof LivingEntity)
 			{
 				Player player = (Player)damager;
 				LivingEntity entity = (LivingEntity)entityEvent.getEntity(); 
				LastDamage d;
				if(lastDamages.containsKey(player))
				{
					d = lastDamages.get(player);
					if(d.getDamaged() == entity && d.getHealtLeft() == entity.getHealth())
					{
						super.onEntityDamage(event);
						return;
					}
					lastDamages.remove(player);
				}
					
				d = new LastDamage();
				
				
				d.setDamaged(entity);
				d.setHealtLeft(entity.getHealth());
				lastDamages.put(player, d);
				
 				int newHealth = entity.getHealth() - entityEvent.getDamage();
 				player.sendMessage(ChatColor.BLUE+ "You damaged " + entity.getClass().getSimpleName() + " with " + 
 						entityEvent.getDamage() + " ( "+ 
						((newHealth < 0) ? 0 : newHealth) +" remaining)");
 			}
 		}
 		super.onEntityDamage(event);
 	}
 
 }
