 package com.censoredsoftware.Demigods.API;
 
 // TODO Move all these somewhere.
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 
 public class MiscAPI
 {
 	public static void customDamage(LivingEntity source, LivingEntity target, int amount, EntityDamageEvent.DamageCause cause)
 	{
 		if(target instanceof Player)
 		{
 			if(source instanceof Player)
 			{
				target.damage(amount);
 				target.setLastDamageCause(new EntityDamageByEntityEvent(source, target, cause, amount));
 			}
 			else target.damage(amount);
 		}
 		else target.damage(amount);
 	}
 
 	public static boolean canUseDeity(Player player, String deity)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		if(!character.isDeity(deity))
 		{
 			player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 			return false;
 		}
 		else if(!character.isImmortal())
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean canUseDeitySilent(Player player, String deity)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		if(character == null) return false;
 		return character.isDeity(deity) && character.isImmortal();
 	}
 }
