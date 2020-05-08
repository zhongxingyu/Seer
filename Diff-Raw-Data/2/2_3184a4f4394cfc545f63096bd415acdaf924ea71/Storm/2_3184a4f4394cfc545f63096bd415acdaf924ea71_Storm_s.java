 package com.censoredsoftware.demigods.episodes.demo.ability.ultimate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.Vector;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.element.Ability;
 import com.censoredsoftware.demigods.engine.element.Deity;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.google.common.collect.Sets;
 
 public class Storm extends Ability
 {
 	private final static String name = "Storm", command = "storm";
 	private final static int cost = 3700, delay = 600, repeat = 0;
 	private static Info info;
 	private final static Devotion.Type type = Devotion.Type.ULTIMATE;
 	private final static List<String> details = new ArrayList<String>(1)
 	{
 		{
 			add("Throw all of your enemies into the sky as lightning fills the heavens.");
 		}
 	};
 
 	public Storm(final String deity, String permission)
 	{
 		super(info = new Info(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(Demigods.isDisabledWorld(interactEvent.getPlayer().getWorld())) return;
 
 				if(!Ability.Util.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(!Deity.Util.canUseDeitySilent(player, deity)) return;
 
 				if(player.getItemInHand() != null && character.getMeta().checkBind(name, player.getItemInHand()))
 				{
 					if(!DCharacter.Util.isCooledDown(character, name, true)) return;
 
 					storm(player);
 
 					int cooldownMultiplier = (int) (delay * ((double) character.getMeta().getAscensions() / 100));
 					DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + cooldownMultiplier * 1000);
 				}
 			}
 		}, null);
 	}
 
 	public static void storm(Player player)
 	{
 		// Define variables
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 		Set<Entity> entitySet = Sets.newHashSet();
 		Vector playerLocation = player.getLocation().toVector();
 
 		if(!Ability.Util.doAbilityPreProcess(player, name, cost, info)) return;
 
 		for(Entity anEntity : player.getWorld().getEntities())
 			if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entitySet.add(anEntity);
 
 		for(Entity entity : entitySet)
 		{
 			if(entity instanceof Player)
 			{
 				Player otherPlayer = (Player) entity;
 				DCharacter otherChar = DPlayer.Util.getPlayer(otherPlayer).getCurrent();
 				if(otherPlayer.equals(player)) continue;
 				if(otherChar != null && !DCharacter.Util.areAllied(character, otherChar) && !otherPlayer.equals(player))
 				{
 					Util.strikeLightning(player, otherPlayer);
 					Util.strikeLightning(player, otherPlayer);
 					continue;
 				}
 			}
 			if(entity instanceof LivingEntity)
 			{
 				LivingEntity livingEntity = (LivingEntity) entity;
 				Util.strikeLightning(player, livingEntity);
 				Util.strikeLightning(player, livingEntity);
 			}
 		}
 	}
 
 	public static class Lightning extends Ability
 	{
 		private final static String name = "Lighting", command = "lightning";
 		private final static int cost = 140, delay = 1000, repeat = 0;
 		private static Info info;
 		private final static List<String> details = new ArrayList<String>(1)
 		{
 			{
 				add("Strike lightning upon your enemies.");
 			}
 		};
 		private final static Devotion.Type type = Devotion.Type.OFFENSE;
 
 		public Lightning(final String deity, String permission)
 		{
 			super(info = new Info(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 			{
 				@EventHandler(priority = EventPriority.HIGH)
 				public void onPlayerInteract(PlayerInteractEvent interactEvent)
 				{
 					if(Demigods.isDisabledWorld(interactEvent.getPlayer().getWorld())) return;
 
 					if(!Util.isLeftClick(interactEvent)) return;
 					if(!Deity.Util.canUseDeitySilent(interactEvent.getPlayer(), deity)) return;
 
 					// Set variables
 					Player player = interactEvent.getPlayer();
 					DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 					if(player.getItemInHand() != null && character.getMeta().checkBind(name, player.getItemInHand()))
 					{
 						if(!DCharacter.Util.isCooledDown(character, name, false)) return;
 
 						lightning(player);
 					}
 				}
 			}, null);
 		}
 
 		protected static void lightning(Player player)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			Location target;
 			LivingEntity entity = Util.autoTarget(player);
 			boolean notify;
 			if(entity != null)
 			{
				target = Util.autoTarget(player).getLocation();
 				notify = true;
 				if(!Util.doAbilityPreProcess(player, entity, "lightning", cost, info)) return;
 			}
 			else
 			{
 				target = Util.directTarget(player);
 				notify = false;
 				if(!Util.doAbilityPreProcess(player, "lightning", cost, info)) return;
 			}
 
 			DCharacter.Util.setCoolDown(character, name, System.currentTimeMillis() + delay);
 			character.getMeta().subtractFavor(cost);
 
 			Storm.Util.strikeLightning(player, target, notify);
 		}
 	}
 
 	public static class Util
 	{
 		public static boolean strikeLightning(Player player, LivingEntity target)
 		{
 			return Battle.Util.canTarget(target) && strikeLightning(player, target.getLocation(), true);
 		}
 
 		public static boolean strikeLightning(Player player, Location target, boolean notify)
 		{
 			// Set variables
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 			if(!player.getWorld().equals(target.getWorld())) return false;
 			Location toHit = Ability.Util.adjustedAimLocation(character, target);
 
 			player.getWorld().strikeLightningEffect(toHit);
 
 			for(Entity entity : toHit.getBlock().getChunk().getEntities())
 			{
 				if(entity instanceof LivingEntity)
 				{
 					if(!Battle.Util.canTarget(entity)) continue;
 					LivingEntity livingEntity = (LivingEntity) entity;
 					if(livingEntity.equals(player)) continue;
 					if((toHit.getBlock().getType().equals(Material.WATER) || toHit.getBlock().getType().equals(Material.STATIONARY_WATER)) && livingEntity.getLocation().distance(toHit) < 8) Ability.Util.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 6, EntityDamageEvent.DamageCause.LIGHTNING);
 					else if(livingEntity.getLocation().distance(toHit) < 2) Ability.Util.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 4, EntityDamageEvent.DamageCause.LIGHTNING);
 				}
 			}
 
 			if(!Ability.Util.isHit(target, toHit))
 			{
 				if(notify) player.sendMessage(ChatColor.RED + "Missed...");
 			}
 
 			return true;
 		}
 	}
 }
