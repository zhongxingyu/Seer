 package com.mrz.dyndns.server.warpsuite;
 
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mrz.dyndns.server.CommandSystem.CommandSystem;
 import com.mrz.dyndns.server.CommandSystem.SimpleCommand;
 import com.mrz.dyndns.server.warpsuite.commands.*;
 import com.mrz.dyndns.server.warpsuite.listeners.EntityDamageByEntityListener;
 import com.mrz.dyndns.server.warpsuite.listeners.PlayerMoveListener;
 import com.mrz.dyndns.server.warpsuite.managers.PendingWarpManager;
 import com.mrz.dyndns.server.warpsuite.managers.PlayerManager;
 import com.mrz.dyndns.server.warpsuite.managers.WarpManager;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.util.Coloring;
 import com.mrz.dyndns.server.warpsuite.util.Config;
 import com.mrz.dyndns.server.warpsuite.util.MyConfig;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class WarpSuite extends JavaPlugin
 {
 	private CommandSystem cs;
 	private PlayerManager playerManager;
 	private WarpManager publicWarpManager;
 	private boolean usingMultiverse;
 	private PendingWarpManager pendingWarpManager;
 	
 	@Override
 	public void onEnable()
 	{
 		Util.initialize(this);
 		Util.setDebugging(true);
 		
 		cs = new CommandSystem(this);
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		Config.load(getConfig());
 		
 		playerManager = new PlayerManager(this);
 		publicWarpManager = new WarpManager(new MyConfig("public", this));
 		pendingWarpManager = new PendingWarpManager();
 		
 		getServer().getPluginManager().registerEvents(playerManager, this);
 		
 		cs.registerCommand("warp set|add", new SetPlayersOwnWarp(this));
 		cs.registerCommand("warp", new GoPlayersOwnWarp(this));
 		
 		final WarpSuite plugin = this;
 		cs.registerCommand("warp reload", new SimpleCommand() {
 			@Override
 			public boolean Execute(String commandName, final CommandSender sender, List<String> args, List<String> variables)
 			{
 				if(Permissions.RELOAD.check(sender))
 				{
 					getServer().getScheduler().runTask(plugin, new Runnable() {
 						@Override
 						public void run()
 						{
 							plugin.onDisable();
 							plugin.onEnable();
 							for(WarpSuitePlayer player : plugin.getPlayerManager().getPlayers())
 							{
 								player.getConfig().reloadCustomConfig();
 							}
 							plugin.getLogger().info("Reloaded WarpSuite");
 							sender.sendMessage(Coloring.POSITIVE_PRIMARY + "Reloaded WarpSuite!");
 						}
 					});
 				}
 				else
 				{
 					Util.invalidPermissions(sender);
 				}
 				return true;
 			}
 		});
 		
 		if(getServer().getPluginManager().getPlugin("Multiverse-Core") != null)
 		{
 			getLogger().info("Hooking into Multiverse");
 			usingMultiverse = true;
 		}
 		else
 		{
 			usingMultiverse = false;
 		}
 		
		getServer().getScheduler().runTaskTimer(this, playerManager, 72000L, 72000L);//every hour
 		
 		getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
 		getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		playerManager.clearPlayers();
 		cs.close();
 	}
 	
 	//you will never need it
 	public WarpManager getPublicWarpManager()
 	{
 		return publicWarpManager;
 	}
 	
 	public PlayerManager getPlayerManager()
 	{
 		return playerManager;
 	}
 	
 	public boolean isUsingMultiverse()
 	{
 		return usingMultiverse;
 	}
 	
 	public PendingWarpManager getPendingWarpManager()
 	{
 		return pendingWarpManager;
 	}
 }
