 package com.censoredsoftware.Demigods.Episodes.Demo.Deity.Insignian;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.*;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Devotion;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.DeityInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.*;
 import com.google.common.collect.Lists;
 
 public class DrD1sco extends Deity
 {
 	private static String name = "DrD1sco", alliance = "Insignian", permission = "demigods.insignian.disco";
 	private static ChatColor color = ChatColor.STRIKETHROUGH;
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
 			add(new RainbowWalking());
 			add(new RainbowHorse());
 		}
 	};
 
 	public DrD1sco()
 	{
 		super(new DeityInfo(name, alliance, permission, color, claimItems, lore, type), abilities);
 	}
 }
 
 class RainbowWalking extends Ability
 {
 	private static String deity = "DrD1sco", name = "Rainbow Walking", command = null, permission = "demigods.insignian.disco";
 	private static int cost = 0, delay = 0, repeat = 5, cooldownMin = 0, cooldownMax = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + "Spread the disco while sneaking.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.STEALTH;
 
 	protected RainbowWalking()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, cooldownMin, cooldownMax, details, type), null, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(Player online : Bukkit.getOnlinePlayers())
 				{
 					if(Deity.canUseDeitySilent(online, "DrD1sco") && online.isSneaking() && !online.isFlying() && !ZoneUtility.zoneNoPVP(online.getLocation()) && !StructureUtility.isTrespassingInNoGriefingZone(online)) doEffect(online, true);
 					else if(Deity.canUseDeitySilent(online, "DrD1sco")) doEffect(online, false);
 				}
 			}
 
 			private void doEffect(Player player, boolean effect)
 			{
 				for(Entity entity : player.getNearbyEntities(30, 30, 30))
 				{
 					if(!(entity instanceof Player)) continue;
 					Player viewing = (Player) entity;
 					if(effect)
 					{
 						viewing.hidePlayer(player);
 						rainbow(player, viewing);
 					}
 					else viewing.showPlayer(player);
 				}
 				if(effect)
 				{
 					rainbow(player, player);
 					playRandomNote(player.getLocation());
 				}
 			}
 
 			private void rainbow(Player disco, Player player)
 			{
 				player.sendBlockChange(disco.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(), Material.WOOL, (byte) MiscUtility.generateIntRange(0, 15));
 				if(SpigotUtility.runningSpigot()) SpigotUtility.playParticle(disco.getLocation(), Effect.COLOURED_DUST, 1, 0, 1, 10F, 100, 30);
 			}
 
 			private void playRandomNote(Location location)
 			{
 				location.getWorld().playSound(location, Sound.NOTE_BASS_GUITAR, 0.5F, (float) ((double) MiscUtility.generateIntRange(5, 10) / 10.0));
 			}
 		});
 	}
 }
 
 class RainbowHorse extends Ability
 {
 	private static String deity = "DrD1sco", name = "Horse of a Different Color", command = null, permission = "demigods.insignian.disco";
 	private static int cost = 0, delay = 0, repeat = 200, cooldownMin = 0, cooldownMax = 0;
 	private static AbilityInfo info;
 	private static List<String> details = new ArrayList<String>()
 	{
 		{
 			add(ChatColor.GRAY + " " + UnicodeUtility.rightwardArrow() + " " + ChatColor.WHITE + "All of you horse are belong to us.");
 		}
 	};
 	private static Devotion.Type type = Devotion.Type.PASSIVE;
 
 	protected RainbowHorse()
 	{
 		super(info = new AbilityInfo(deity, name, command, permission, cost, delay, repeat, cooldownMin, cooldownMax, details, type), null, new BukkitRunnable()
 		{
 			@Override
 			public void run()
 			{
 				for(TameableWrapper horse : TameableWrapper.findByType(EntityType.HORSE))
 				{
					if(horse.getDeity().getInfo().getName().equals("DrD1sco") && !horse.getEntity().isDead())
 					{
						((Horse) horse.getEntity()).setColor(getRandomHorseColor());
 					}
 				}
 			}
 
 			private Horse.Color getRandomHorseColor()
 			{
 				return Lists.newArrayList(Horse.Color.values()).get(MiscUtility.generateIntRange(0, 5));
 			}
 		});
 	}
 }
