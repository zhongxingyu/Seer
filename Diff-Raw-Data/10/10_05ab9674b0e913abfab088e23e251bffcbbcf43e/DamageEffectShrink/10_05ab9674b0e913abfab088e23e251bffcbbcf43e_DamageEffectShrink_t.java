 package uk.thecodingbadgers.minekart.powerup.damageeffect;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Slime;
 
 import net.citizensnpcs.api.npc.NPC;
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 
 public class DamageEffectShrink extends DamageEffect {
 
 	@Override
 	@SuppressWarnings("deprecation")
 	public void use(Jockey jockey) {
 		
 		NPC mount = jockey.getMount();
 		if (mount == null) {
 			return;
 		}
 		
 		int oldValue = -1;
 				
 		final Entity entity = mount.getBukkitEntity();
 		if (entity instanceof Ageable) {
 			Ageable ageable = (Ageable)entity;
 			oldValue = ageable.getAge();
 			ageable.setBaby();
 		}
 		else if (entity instanceof Slime) {
 			Slime slime = (Slime)entity;
 			oldValue = slime.getSize();
 			slime.setSize(1);
 		}
 		
		final int shrinkLength = 4;
 		final int restoreValue = oldValue;
 		Bukkit.getScheduler().scheduleSyncDelayedTask(MineKart.getInstance(), new Runnable() {
 
 			@Override
 			public void run() {
 				if (entity instanceof Ageable) {
 					Ageable ageable = (Ageable)entity;
 					ageable.setAge(restoreValue);
 				}
 				else if (entity instanceof Slime) {
 					Slime slime = (Slime)entity;
 					slime.setSize(restoreValue);
 				}
 			}
 			
 		}, shrinkLength * 20L);
 		
 	}
 
 }
