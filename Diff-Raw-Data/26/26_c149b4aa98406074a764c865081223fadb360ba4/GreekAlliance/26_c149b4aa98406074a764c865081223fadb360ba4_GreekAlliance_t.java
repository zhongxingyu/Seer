 package com.censoredsoftware.demigods.greek.deity;
 
 import com.censoredsoftware.demigods.engine.deity.Alliance;
import com.censoredsoftware.demigods.engine.deity.Deity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
 import org.bukkit.permissions.PermissionDefault;
 
 public enum GreekAlliance implements Alliance
 {
 	/**
 	 * Main Alliances
 	 */
 	GOD("God", "A short description of the Gods.", "demigods.alliance.god", PermissionDefault.TRUE, true), TITAN("Titan", "A short description of the Titans.", "demigods.alliance.titan", PermissionDefault.TRUE, true),
 
 	/**
 	 * Special Alliances
 	 */
 	FATE("Fate", "A short description of the Fates.", "demigods.alliance.fate", PermissionDefault.OP, true), DONOR("Donor", "A short description of the Donors.", "demigods.alliance.donor", PermissionDefault.FALSE, false);
 
 	private String name, shortDescription, permission;
 	private PermissionDefault permissionDefault;
 	private boolean playable;
 
 	private GreekAlliance(String name, String shortDescription, String permission, PermissionDefault permissionDefault, boolean playable)
 	{
 		this.name = name;
 		this.shortDescription = shortDescription;
 		this.permission = permission;
 		this.permissionDefault = permissionDefault;
 		this.playable = playable;
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
 
 	public String getShortDescription()
 	{
 		return shortDescription;
 	}
 
 	public String getPermission()
 	{
 		return permission;
 	}
 
 	public PermissionDefault getPermissionDefault()
 	{
 		return permissionDefault;
 	}
 
 	public boolean isPlayable()
 	{
		try
		{
			return playable && Iterables.any(Alliance.Util.getLoadedDeitiesInAlliance(this), new Predicate<Deity>()
			{
				@Override
				public boolean apply(Deity deity)
				{
					return deity.getFlags().contains(Deity.Flag.PLAYABLE);
				}
			});
		}
		catch(Throwable ignored)
		{}
		return false;
 	}
 }
