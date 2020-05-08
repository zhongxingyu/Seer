 package com.censoredsoftware.Demigods.Episodes.Demo.Deity.God;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.util.Vector;
 
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Devotion;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.DeityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.DemigodsPlayer;
 import com.censoredsoftware.Demigods.Engine.Object.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Utility.UnicodeUtility;
 
 public class Poseidon extends Deity
 {
 	private static String name = "Poseidon", alliance = "God";
 	private static ChatColor color = ChatColor.AQUA;
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
 			add(new Swim());
 			add(new Reel());
 		}
 	};
 
 	public Poseidon()
 	{
 		super(new DeityInfo(name, alliance, color, claimItems, lore, type), abilities);
 	}
 }
 
 class Swim extends Ability
 {
 	private static String deity = "Poseidon", name = "Swim", command = null, permission = "demigods.god.poseidon";
 	private static int cost = 0, delay = 0, cooldownMin = 0, cooldownMax = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + "Crouch while in water to swim like Poseidon.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.PASSIVE;
 
 	protected Swim()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, cooldownMin, cooldownMax, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGH)
 			public void onPlayerMove(PlayerMoveEvent event)
 			{
 				Player player = event.getPlayer();
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				// PHELPS SWIMMING
 				if(player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER) || player.getLocation().getBlock().getType().equals(Material.WATER))
 				{
 					Vector direction = player.getLocation().getDirection().normalize().multiply(1.3D);
 					Vector victor = new Vector(direction.getX(), direction.getY(), direction.getZ());
 					if(player.isSneaking()) player.setVelocity(victor);
 				}
 			}
 		});
 	}
 }
 
 class Reel extends Ability
 {
 	private static String deity = "Poseidon", name = "Reel", command = "reel", permission = "demigods.god.poseidon";
 	private static int cost = 120, delay = 1100, cooldownMin = 0, cooldownMax = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.GREEN + "/reel" + ChatColor.WHITE + " - Use a fishing rod for a stronger attack.");
 		}
 	};
	private static Devotion.Type type = Devotion.Type.OFFENSE;
 
 	protected Reel()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, cooldownMin, cooldownMax, details, type), new Listener()
 		{
 			@EventHandler(priority = EventPriority.HIGHEST)
 			public void onPlayerInteract(PlayerInteractEvent interactEvent)
 			{
 				// Set variables
 				Player player = interactEvent.getPlayer();
 				PlayerCharacter character = DemigodsPlayer.getPlayer(player).getCurrent();
 
 				if(!Ability.isLeftClick(interactEvent)) return;
 
 				if(!Deity.canUseDeitySilent(player, deity)) return;
 
 				if(character.getMeta().isEnabledAbility(name) && (player.getItemInHand().getType() == Material.FISHING_ROD))
 				{
 					if(!PlayerCharacter.isCooledDown(character, name, false)) return;
 
 					reel(player);
 				}
 			}
 		});
 	}
 
 	public static void reel(Player player)
 	{
 		// Set variables
 		PlayerCharacter character = DemigodsPlayer.getPlayer(player).getCurrent();
 		int damage = (int) Math.ceil(0.37286 * Math.pow(character.getMeta().getAscensions() * 100, 0.371238)); // TODO
 		LivingEntity target = Ability.autoTarget(player);
 
 		if(!Ability.doAbilityPreProcess(player, target, name, cost, info)) return;
 		character.getMeta().subtractFavor(cost);
 		PlayerCharacter.setCoolDown(character, name, System.currentTimeMillis() + delay);
 
 		if(!Ability.targeting(player, target)) return;
 
 		Ability.customDamage(player, target, damage, EntityDamageEvent.DamageCause.CUSTOM);
 
 		if(target.getLocation().getBlock().getType() == Material.AIR)
 		{
 			target.getLocation().getBlock().setType(Material.WATER);
 			target.getLocation().getBlock().setData((byte) 0x8);
 		}
 	}
 }
