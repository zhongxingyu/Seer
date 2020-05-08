 package net.immortalcraft.races;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

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
 }
