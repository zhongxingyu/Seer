 package com.censoredsoftware.demigods.engine.util;
 
 import com.censoredsoftware.censoredlib.util.WorldGuards;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.google.common.base.Predicate;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 public class Zones
 {
 	static
 	{
 		WorldGuards.createFlag("STATE", "demigods", true, "ALL");
 		WorldGuards.registerCreatedFlag("demigods");
 		WorldGuards.setWhenToOverridePVP(Demigods.PLUGIN, new Predicate<EntityDamageByEntityEvent>()
 		{
 			@Override
 			public boolean apply(EntityDamageByEntityEvent event)
 			{
 				return !Zones.inNoDemigodsZone(event.getEntity().getLocation());
 			}
 		});
 	}
 
 	/**
 	 * Returns true if <code>location</code> is within a no-PVP zone.
 	 * 
 	 * @param location the location to check.
 	 * @return true/false depending on if it's a no-PVP zone or not.
 	 */
 	public static boolean inNoPvpZone(Location location)
 	{
 		if(Configs.getSettingBoolean("zones.allow_skills_anywhere")) return false;
		if(WorldGuards.canWorldGuard()) return Structure.Util.isInRadiusWithFlag(location, Structure.Flag.NO_PVP) || WorldGuards.canPVP(location);
 		return Structure.Util.isInRadiusWithFlag(location, Structure.Flag.NO_PVP);
 	}
 
 	/**
 	 * Returns true if <code>location</code> is within a no-build zone
 	 * for <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @param location the location to check.
 	 * @return true/false depending on the position of the <code>player</code>.
 	 */
 	public static boolean inNoBuildZone(Player player, Location location)
 	{
 		if(WorldGuards.canWorldGuard() && !WorldGuards.canBuild(player, location)) return true;
 		StructureData save = Structure.Util.getInRadiusWithFlag(location, Structure.Flag.NO_GRIEFING);
 		if(save != null && save.getOwner() != null) return !save.getOwner().equals(DPlayer.Util.getPlayer(player).getCurrent().getId());
 		return false;
 	}
 
 	public static boolean inNoDemigodsZone(Location location)
 	{
 		return isNoDemigodsWorld(location.getWorld()); // || WorldGuards.canWorldGuard() && WorldGuards.checkForCreatedFlagValue("demigods", "deny", location);
 	}
 
 	public static boolean isNoDemigodsWorld(World world)
 	{
 		return Demigods.DISABLED_WORLDS.contains(world.getName());
 	}
 }
