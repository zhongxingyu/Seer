 package com.censoredsoftware.camcorder;
 
 import com.censoredsoftware.core.improve.CSPlugin;
 
 public class CamcorderPlugin extends CSPlugin
 {
 	public static CamcorderPlugin plugin;
 
 	/**
 	 * The Bukkit enable method.
 	 */
 	@Override
 	public void onEnable()
 	{
 		plugin = this;
 		saveDefaultConfig();
 		(new CamcorderCommand()).register(this, false);
 		new CamcorderRecord(this);
 		new CamcorderPlayback(this);
 	}
 
 	/**
 	 * The Bukkit disable method.
 	 */
 	@Override
 	public void onDisable()
 	{
		// TODO
 	}
 }
