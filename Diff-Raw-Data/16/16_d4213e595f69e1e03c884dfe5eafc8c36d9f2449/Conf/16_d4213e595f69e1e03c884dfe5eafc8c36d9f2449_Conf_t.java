 package net.immortalcraft.races;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 
 import com.massivecraft.mcore.store.Entity;
 import com.massivecraft.mcore.util.MUtil;
 
 public class Conf extends Entity<Conf>
 {
 
 	// -------------------------------------------- //
 	// META
 	// -------------------------------------------- //
 
 	public static Conf get(Object worldNameExtractable)
 	{
 		return ConfColls.i.get2(worldNameExtractable);
 	}
 
 	// -------------------------------------------- //
 		// MISC
 		// -------------------------------------------- //
 
 		public Set<DamageCause> blockDamageFrom = MUtil.set(
			DamageCause.FIRE,
 			DamageCause.FALL,
 			DamageCause.STARVATION
 		);
 
 		public Set<RegainReason> blockHealthFrom = MUtil.set(
 			RegainReason.SATIATED,
 			RegainReason.REGEN	
 		);
 
 		// -------------------------------------------- //
 		// UPDATE
 		// -------------------------------------------- //
 
		public Map<String,Boolean> updatePermsRace = MUtil.map(
			"race.is.demon", true,
			"race.is.elf", true,
			"race.is.human", true,
			"race.is.orc", true
 		);
 
 		public int updateRespawnFood = 20;
 		public int updateRespawnHealth = 20;
 
 		public Boolean updateNameColor = false;
 		public ChatColor updateNameColorTo = ChatColor.RED;
 
 		// -------------------------------------------- //
 		// DROP SELF
 		// -------------------------------------------- //
 
 		public Set<Material> dropSelfMaterials = MUtil.set(
 			Material.WEB,
 			Material.GLOWSTONE,
 			Material.BOOKSHELF,
 			Material.DEAD_BUSH
 		);
 }
