 package com.censoredsoftware.Demigods.API;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.BlockIterator;
 
 import com.censoredsoftware.Demigods.Engine.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Event.Ability.AbilityEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Ability.AbilityTargetEvent;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedPlayer;
 
 public class AbilityAPI
 {
 	private static final int TARGETOFFSET = 5;
 
 	/**
 	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 	 * with a cost of <code>cost</code>, that is Type <code>type</code>, has
 	 * passed all pre-process tests.
 	 * 
 	 * @param player the player doing the ability
 	 * @param name the name of the ability
 	 * @param cost the cost (in favor) of the ability
 	 * @param type the Type of the ability
 	 * @return true/false depending on if all pre-process tests have passed
 	 */
 	public static boolean doAbilityPreProcess(Player player, String name, int cost, Ability.Type type)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 		return doAbilityPreProcess(player, cost) && event(name, character, cost, type);
 	}
 
 	/**
 	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 	 * with a cost of <code>cost</code>, that is Type <code>type</code>, that
 	 * is targeting the LivingEntity <code>target</code>, has passed all pre-process tests.
 	 * 
 	 * @param player the Player doing the ability
 	 * @param target the LivingEntity being targeted
 	 * @param name the name of the ability
 	 * @param cost the cost (in favor) of the ability
 	 * @param type the Type of the ability
 	 * @return true/false depending on if all pre-process tests have passed
 	 */
 	public static boolean doAbilityPreProcess(Player player, LivingEntity target, String name, int cost, Ability.Type type)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 		if(doAbilityPreProcess(player, cost) && event(name, character, cost, type))
 		{
 			if(!(target instanceof LivingEntity))
 			{
 				player.sendMessage(ChatColor.YELLOW + "No target found.");
 				return false;
 			}
 			else if(!ZoneAPI.canTarget(target))
 			{
 				player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
 				return false;
 			}
 			else if(target instanceof Player)
 			{
 				if(PlayerAPI.areAllied(player, (Player) target)) return false;
 			}
 			Bukkit.getServer().getPluginManager().callEvent(new AbilityTargetEvent(character, target));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the event <code>event</code> is caused by a click.
 	 * 
 	 * @param event the interact event
 	 * @return true/false depending on if the event is caused by a click or not
 	 */
 	public static boolean isClick(PlayerInteractEvent event)
 	{
 		Action action = event.getAction();
 		return action != Action.PHYSICAL;
 	}
 
 	/**
 	 * Returns the LivingEntity that <code>player</code> is targeting.
 	 * 
 	 * @param player the interact event
 	 * @return the targeted LivingEntity
 	 */
 	public static LivingEntity autoTarget(Player player)
 	{
 		BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0, 100);
 
 		while(iterator.hasNext())
 		{
 			Block item = iterator.next();
 			for(Entity entity : player.getNearbyEntities(100, 100, 100))
 			{
 				if(entity instanceof LivingEntity)
 				{
 					int acc = 2;
 					for(int x = -acc; x < acc; x++)
 					{
 						for(int z = -acc; z < acc; z++)
 						{
 							for(int y = -acc; y < acc; y++)
 							{
 								if(entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) return (LivingEntity) entity;
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns true if the <code>player</code> ability hits <code>target</code>.
 	 * 
 	 * @param player the player using the ability
 	 * @param target the targeted LivingEntity
 	 * @return true/false depending on if the ability hits or misses
 	 */
 	public static boolean targeting(Player player, LivingEntity target)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 		Location toHit = aimLocation(character, target.getLocation());
 		if(isHit(target, toHit)) return true;
 		player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
 		return false;
 	}
 
 	/**
 	 * Returns true if the ability event for <code>character</code>, called <code>name</code>,
 	 * with a cost of <code>cost</code>, that is Type <code>type</code>, has passed
 	 * all pre-process tests.
 	 * 
 	 * @param character the character triggering the ability event
 	 * @param name the name of the ability
 	 * @param cost the cost (in favor) of the ability
 	 * @param type the Type of the ability
 	 * @return true/false if the event isn't cancelled or not
 	 */
 	public static boolean event(String name, PlayerCharacter character, int cost, Ability.Type type)
 	{
 		AbilityEvent event = new AbilityEvent(name, character, cost, type);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 		return !event.isCancelled();
 	}
 
 	/**
 	 * Returns the location that <code>character</code> is actually aiming
 	 * at when targeting <code>target</code>.
 	 * 
 	 * @param character the character triggering the ability event
 	 * @param target the location the character is targeting at
 	 * @return the aimed at location
 	 */
 	public static Location aimLocation(PlayerCharacter character, Location target)
 	{
 		int ascensions = character.getMeta().getAscensions();
		if(ascensions < 3) ascensions = 3;
 
 		int offset = (int) (TARGETOFFSET + character.getPlayer().getPlayer().getLocation().distance(target));
 		int adjustedOffset = offset / ascensions;
 		if(adjustedOffset < 1) adjustedOffset = 1;
 		Random random = new Random();
 		World world = target.getWorld();
 
 		int randomInt = random.nextInt(adjustedOffset);
 
 		int sampleSpace = random.nextInt(3);
 
 		double X = target.getX();
 		double Z = target.getZ();
 		double Y = target.getY();
 
 		if(sampleSpace == 0)
 		{
 			X += randomInt;
 			Z += randomInt;
 		}
 		else if(sampleSpace == 1)
 		{
 			X -= randomInt;
 			Z -= randomInt;
 		}
 		else if(sampleSpace == 2)
 		{
 			X -= randomInt;
 			Z += randomInt;
 		}
 		else if(sampleSpace == 3)
 		{
 			X += randomInt;
 			Z -= randomInt;
 		}
 
 		return new Location(world, X, Y, Z);
 	}
 
 	/**
 	 * Returns true if <code>target</code> is hit at <code>hit</code>.
 	 * 
 	 * @param target the LivingEntity being targeted
 	 * @param hit the location actually hit
 	 * @return true/false if <code>target</code> is hit
 	 */
 	public static boolean isHit(LivingEntity target, Location hit)
 	{
 		Location shouldHit = target.getLocation();
 		return hit.distance(shouldHit) <= 2;
 	}
 
 	private static boolean doAbilityPreProcess(Player player, int cost)
 	{
 		PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 		if(!ZoneAPI.canTarget(player))
 		{
 			player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 			return false;
 		}
 		else if(character.getMeta().getFavor() < cost)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
 			return false;
 		}
 		else return true;
 	}
 
 	public static List<Ability> getLoadedAbilities()
 	{
 		return new ArrayList<Ability>()
 		{
 			{
 				for(Deity deity : Demigods.getLoadedDeities())
 				{
 					addAll(deity.getAbilities());
 				}
 			};
 		};
 	}
 
 	public static boolean invokeAbilityCommand(Player player, String command, boolean bind)
 	{
 		for(Ability ability : getLoadedAbilities())
 		{
 			if(ability.getInfo().getType() == Ability.Type.PASSIVE) continue;
 			if(ability.getInfo().getCommand().equalsIgnoreCase(command))
 			{
 				PlayerCharacter character = TrackedPlayer.getTracked(player).getCurrent();
 
 				if(!Demigods.permission.hasPermissionOrOP(player, ability.getInfo().getPermission())) return true;
 
 				if(!MiscAPI.canUseDeity(player, ability.getInfo().getDeity())) return true;
 
 				if(bind)
 				{
 					// Bind item
 					character.getMeta().setBound(ability.getInfo().getName(), player.getItemInHand().getType());
 				}
 				else
 				{
 					if(character.getMeta().isEnabledAbility(ability.getInfo().getName()))
 					{
 						character.getMeta().toggleAbility(ability.getInfo().getName(), false);
 						player.sendMessage(ChatColor.YELLOW + ability.getInfo().getName() + " is no longer active.");
 					}
 					else
 					{
 						character.getMeta().toggleAbility(ability.getInfo().getName(), true);
 						player.sendMessage(ChatColor.YELLOW + ability.getInfo().getName() + " is now active.");
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }
