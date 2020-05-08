 package com.censoredsoftware.Demigods.Episodes.Demo.Deity.God;
 
 import java.util.ArrayList;
 import java.util.HashSet;
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
 
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Devotion;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.DeityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.UnicodeUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 import com.google.common.collect.Sets;
 
 public class Zeus extends Deity
 {
 	private static String name = "Zeus", alliance = "God", permission = "demigods.god.zeus";
 	private static ChatColor color = ChatColor.YELLOW;
 	private static Set<Material> claimItems = new HashSet<Material>()
 	{
 		{
 			add(Material.DIRT);
 		}
 	};
 	private static List<String> lore = new ArrayList<String>()
 	{
 		{
 			add(" ");
 			add(ChatColor.AQUA + " Demigods > " + ChatColor.RESET + color + name);
 			add(ChatColor.RESET + "-----------------------------------------------------");
 			add(ChatColor.YELLOW + " Claim Items:");
 			for(Material item : claimItems)
 			{
 				add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + item.name());
 			}
 			add(ChatColor.YELLOW + " Abilities:");
 		}
 	};
 	private static Type type = Type.DEMO;
 	private static Set<Ability> abilities = new HashSet<Ability>()
 	{
 		{
 			add(new NoFall());
 			add(new Shove());
 			add(new Lightning());
 			add(new Storm());
 		}
 	};
 
 	public Zeus()
 	{
 		super(new DeityInfo(name, alliance, permission, color, claimItems, lore, type), abilities);
 	}
 
 	protected static boolean strikeLightning(Player player, LivingEntity target)
 	{
 		if(ZoneUtility.canTarget(target)) return strikeLightning(player, target.getLocation(), true);
 		return false;
 	}
 
 	protected static boolean strikeLightning(Player player, Location target, boolean notify)
 	{
 		// Set variables
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		if(!player.getWorld().equals(target.getWorld())) return false;
 		Location toHit = Ability.adjustedAimLocation(character, target);
 
 		player.getWorld().strikeLightningEffect(toHit);
 
 		for(Entity entity : toHit.getBlock().getChunk().getEntities())
 		{
 			if(entity instanceof LivingEntity)
 			{
 				if(!ZoneUtility.canTarget(entity)) continue;
 				LivingEntity livingEntity = (LivingEntity) entity;
 				if(livingEntity.equals(player)) continue;
 				if((toHit.getBlock().getType().equals(Material.WATER) || toHit.getBlock().getType().equals(Material.STATIONARY_WATER)) && livingEntity.getLocation().distance(toHit) < 8) Ability.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 6, EntityDamageEvent.DamageCause.LIGHTNING);
 				else if(livingEntity.getLocation().distance(toHit) < 2) Ability.dealDamage(player, livingEntity, character.getMeta().getAscensions() * 4, EntityDamageEvent.DamageCause.LIGHTNING);
 			}
 		}
 
 		if(!Ability.isHit(target, toHit))
 		{
 			if(notify) player.sendMessage(ChatColor.RED + "Missed...");
 		}
 
 		return true;
 	}
 }
 
 class Shove extends Ability
 {
 	private static String deity = "Zeus", name = "Shove", command = "shove", permission = "demigods.god.zeus";
 	private static int cost = 170, delay = 15, repeat = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("Shove your target away from you.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.DEFENSE;
 
 	protected Shove()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) || player.getItemInHand() != null && character.getMeta().checkBind(name, player.getItemInHand()))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					shove(player);
 				}
 			}
 		}, null);
 	}
 
 	// The actual ability command
 	public static void shove(Player player)
 	{
 		// Define variables
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		int ascensions = character.getMeta().getAscensions();
 		double multiply = 0.1753 * Math.pow(ascensions, 0.322917);
 		LivingEntity target = Ability.autoTarget(player);
 
 		if(!Ability.doAbilityPreProcess(player, target, "shove", cost, info)) return;
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 		character.getMeta().subtractFavor(cost);
 
 		if(!Ability.doTargeting(player, target.getLocation(), true)) return;
 
 		Vector vector = player.getLocation().toVector();
 		Vector victor = target.getLocation().toVector().subtract(vector);
 		victor.multiply(multiply);
 		target.setVelocity(victor);
 		Ability.dealDamage(player, target, 0, EntityDamageEvent.DamageCause.FALL);
 	}
 }
 
 class Lightning extends Ability
 {
 	private static String deity = "Zeus", name = "Lighting", command = "lightning", permission = "demigods.god.zeus";
 	private static int cost = 140, delay = 1000, repeat = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("Strike lightning upon your enemies.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.OFFENSE;
 
 	protected Lightning()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isLeftClick(interactEvent)) return;
 				if(!Deity.canUseDeitySilent(interactEvent.getPlayer(), deity)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 				if(character.getMeta().isEnabledAbility(name) || player.getItemInHand() != null && character.getMeta().checkBind(name, player.getItemInHand()))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					lightning(player);
 				}
 			}
 		}, null);
 	}
 
 	protected static void lightning(Player player)
 	{
 		// Define variables
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		Location target;
 		LivingEntity entity = Ability.autoTarget(player);
 		boolean notify;
 		if(entity != null)
 		{
 			target = Ability.autoTarget(player).getLocation();
 			notify = true;
 			if(!Ability.doAbilityPreProcess(player, entity, "lightning", cost, info)) return;
 		}
 		else
 		{
 			target = Ability.directTarget(player);
 			notify = false;
 			if(!Ability.doAbilityPreProcess(player, "lightning", cost, info)) return;
 		}
 
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 		character.getMeta().subtractFavor(cost);
 
 		Zeus.strikeLightning(player, target, notify);
 	}
 }
 
 class Storm extends Ability
 {
 	private static String deity = "Zeus", name = "Storm", command = "storm", permission = "demigods.god.zeus.ultimate";
 	private static int cost = 3700, delay = 600, repeat = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add("Throw all of your enemies into the sky as lightning fills the heavens.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.ULTIMATE;
 
 	protected Storm()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				if(!Ability.isLeftClick(interactEvent)) return;
 
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) || player.getItemInHand() != null && character.getMeta().checkBind(name, player.getItemInHand()))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, true)) return;
 
 					storm(player);
 
 					int cooldownMultiplier = (int) (delay * ((double) character.getMeta().getAscensions() / 100));
 					PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + cooldownMultiplier * 1000);
 				}
 			}
 		}, null);
 	}
 
 	public static void storm(Player player)
 	{
 		// Define variables
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		Set<Entity> entitySet = Sets.newHashSet();
 		Vector playerLocation = player.getLocation().toVector();
 
 		if(!Ability.doAbilityPreProcess(player, name, cost, info)) return;
 
 		for(Entity anEntity : player.getWorld().getEntities())
 			if(anEntity.getLocation().toVector().isInSphere(playerLocation, 50.0)) entitySet.add(anEntity);
 
 		for(Entity entity : entitySet)
 		{
 			if(entity instanceof Player)
 			{
 				Player otherPlayer = (Player) entity;
 				PlayerCharacter otherChar = PlayerWrapper.getPlayer(otherPlayer).getCurrent();
 				if(otherPlayer.equals(player)) continue;
 				if(otherChar != null && !PlayerCharacter.areAllied(character, otherChar) && !otherPlayer.equals(player))
 				{
 					Zeus.strikeLightning(player, otherPlayer);
 					Zeus.strikeLightning(player, otherPlayer);
 					continue;
 				}
 			}
 			if(entity instanceof LivingEntity)
 			{
 				LivingEntity livingEntity = (LivingEntity) entity;
 				Zeus.strikeLightning(player, livingEntity);
 				Zeus.strikeLightning(player, livingEntity);
 			}
 		}
 	}
 }
 
 class NoFall extends Ability
 {
 	private static String deity = "Zeus", name = "No Fall Damage", command = null, permission = "demigods.god.zeus";
 	private static int cost = 0, delay = 0, repeat = 0;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
			add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + "Take no damage from falling.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.PASSIVE;
 
 	protected NoFall()
 	{
 		super(new AbilityInfo(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.MONITOR)
 			public void onEntityDamange(EntityDamageEvent damageEvent)
 			{
 				if(damageEvent.getEntity() instanceof Player)
 				{
 					Player player = (Player) damageEvent.getEntity();
 					if(!Deity.canUseDeitySilent(player, deity)) return;
 
 					// If the player receives falling damage, cancel it
 					if(damageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) damageEvent.setCancelled(true);
 				}
 			}
 		}, null);
 	}
 }
