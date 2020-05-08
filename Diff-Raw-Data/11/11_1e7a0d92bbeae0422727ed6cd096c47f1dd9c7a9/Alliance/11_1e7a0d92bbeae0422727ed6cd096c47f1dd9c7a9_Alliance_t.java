 package com.censoredsoftware.demigods.deity;
 
import java.util.Collection;

import org.bukkit.entity.Player;

 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 
 public enum Alliance
 {
 	/**
 	 * Test Alliances
 	 */
	TEST("Test", "demigods.alliance.test", "A short description of the Tests."),
 
 	/**
 	 * Main Alliances
 	 */
 	GOD("God", "demigods.alliance.god", "A short description of the Gods."), TITAN("Titan", "demigods.alliance.titan", "A short description of the Titans."),
 
 	/**
 	 * Special Alliances
 	 */
 	FATE("Fate", "demigods.alliance.fate", "A short description of the Fates."), DONOR("Donor", "demigods.alliance.donor", "A short description of the Donors.");
 
 	private String name, permission, shortDescription;
 
 	private Alliance(String name, String permission, String shortDescription)
 	{
 		this.name = name;
 		this.permission = permission;
 		this.shortDescription = shortDescription;
 	}
 
 	@Override
 	public String toString()
 	{
 		return getName();
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public String getPermission()
 	{
 		return permission;
 	}
 
 	public String getShortDescription()
 	{
 		return shortDescription;
 	}
 
 	public static class Util
 	{
 		public static Collection<Deity> getLoadedPlayableDeitiesInAlliance(final Alliance alliance)
 		{
 			return Collections2.filter(getLoadedDeitiesInAlliance(alliance), new Predicate<Deity>()
 			{
 				@Override
 				public boolean apply(Deity d)
 				{
 					return d.getFlags().contains(Deity.Flag.PLAYABLE) && d.getAlliance().getName().equalsIgnoreCase(alliance.getName());
 				}
 			});
 		}
 
 		public static Collection<Deity> getLoadedMajorPlayableDeitiesInAllianceWithPerms(final Alliance alliance, final Player player)
 		{
 			return Collections2.filter(getLoadedDeitiesInAlliance(alliance), new Predicate<Deity>()
 			{
 				@Override
 				public boolean apply(Deity d)
 				{
 					return player.hasPermission(d.getPermission()) && d.getFlags().contains(Deity.Flag.PLAYABLE) && d.getFlags().contains(Deity.Flag.MAJOR_DEITY) && d.getAlliance().getName().equalsIgnoreCase(alliance.getName());
 				}
 			});
 		}
 
 		public static Collection<Deity> getLoadedMajorPlayableDeitiesInAlliance(final Alliance alliance)
 		{
 			return Collections2.filter(getLoadedDeitiesInAlliance(alliance), new Predicate<Deity>()
 			{
 				@Override
 				public boolean apply(Deity d)
 				{
 					return d.getFlags().contains(Deity.Flag.PLAYABLE) && d.getFlags().contains(Deity.Flag.MAJOR_DEITY) && d.getAlliance().getName().equalsIgnoreCase(alliance.getName());
 				}
 			});
 		}
 
 		public static Collection<Deity> getLoadedDeitiesInAlliance(final Alliance alliance)
 		{
 			return Collections2.filter(Collections2.transform(Sets.newHashSet(Deity.values()), new Function<Deity, Deity>()
 			{
 				@Override
 				public Deity apply(Deity d)
 				{
 					return d;
 				}
 			}), new Predicate<Deity>()
 			{
 				@Override
 				public boolean apply(Deity d)
 				{
 					return d.getAlliance().getName().equalsIgnoreCase(alliance.getName());
 				}
 			});
 		}
 	}
 }
