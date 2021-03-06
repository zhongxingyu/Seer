 package com.mrz.dyndns.server.warpsuite.players;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.NEGATIVE_PRIMARY;
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.NEGATIVE_SECONDARY;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.managers.WarpManager;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.util.Config;
 import com.mrz.dyndns.server.warpsuite.util.MyConfig;
 import com.mrz.dyndns.server.warpsuite.util.SimpleLocation;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class WarpSuitePlayer
 {
 	private final String playerName;
 	private final MyConfig config;
 	private final WarpManager manager;
 	private final WarpSuite plugin;
 	
 	private SimpleLocation warpRequest;
 	private long timeWhenRequestWasMade = -1;
 	
 	public WarpSuitePlayer(String playerName, WarpSuite plugin)
 	{
 		this.playerName = playerName;
 		this.plugin = plugin;
 		config = new MyConfig("players/" + playerName, plugin);
 		manager = new WarpManager(config);
 	}
 	
 	public Player getPlayer()
 	{
		//this is an efficency test. Lets see how often this actually gets called
		System.out.println("getPlayer() called for " + playerName);
		return Bukkit.getPlayer(playerName);
 	}
 	
 	public MyConfig getConfig()
 	{
 		return config;
 	}
 	
 	public SimpleLocation getRequest()
 	{
 		if(tryTimeout())
 		{
 			return null;
 		}
 		else
 		{
 			return warpRequest;
 		}
 	}
 	
 	public void teleport(final SimpleLocation sLoc)
 	{
 		//run it next tick so if a world had to be loaded we'll let it load up before teleporting the player there
 		Bukkit.getScheduler().runTask(plugin, new Runnable() {
 			@Override
 			public void run()
 			{
 				getPlayer().teleport(sLoc.toLocation());
 			}
 		});
 	}
 	
 	public WarpManager getWarpManager()
 	{
 		return manager;
 	}
 	
 	public String getName()
 	{
 		return playerName;
 	}
 	
 	/**
 	 * Convenience method for sending messages
 	 * @param message Message sent to the player
 	 */
 	public void sendMessage(String message)
 	{
 		getPlayer().sendMessage(message); 
 	}
 	
 	/**
 	 * 
 	 * @param sLoc request location
 	 * @return false if request coudln't be made because one is already pending
 	 */
 	public boolean sendRequest(SimpleLocation sLoc)
 	{
 		if(tryTimeout())
 		{
 			timeWhenRequestWasMade = Util.getUnixTime();
 			warpRequest = sLoc;
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return true if there are no pending warp requests, or the request has timed out
 	 */
 	private boolean tryTimeout()
 	{
 		if(warpRequest == null || timeWhenRequestWasMade == -1)
 		{
 			return true;
 		}
 		
 		long timeoutTime = Config.warpInviteTimeout;
 		
 		if (timeWhenRequestWasMade + timeoutTime > Util.getUnixTime())
 		{
 			timeWhenRequestWasMade = -1;
 			warpRequest = null;
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param plugin WarpSuite plugin
 	 * @param sLoc place to teleport to
 	 * @param override if player is teleported regardless of circumstances
 	 * @return true of warp was successful
 	 */
 	public boolean warpTo(final SimpleLocation sLoc, boolean override)
 	{
 		boolean canGoToWorld = sLoc.tryLoad(plugin);
 		if(canGoToWorld)
 		{
 			//it is time to teleport!
 			if(Permissions.DELAY_BYPASS.check(this, false) || !Util.areTherePlayersInRadius(this) || override)
 			{
 				teleport(sLoc);
 				return true;
 			}
 			else
 			{
 				Util.sendYouWillBeWarpedMessage(this);
 				final WarpSuitePlayer player = this;
 				int id = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 					@Override
 					public void run()
 					{
 						if(plugin.getPendingWarpManager().isWaitingToTeleport(player.getName()))
 						{
 							plugin.getPendingWarpManager().removePlayer(player.getName());
 							player.teleport(sLoc);
 						}
 					}
 				}, Config.timer * 20L).getTaskId();
 
 				plugin.getPendingWarpManager().addPlayer(player.getName(), id);
 				
 				return true;
 			}
 		}
 		else
 		{
 			sendMessage(NEGATIVE_PRIMARY + "The world warp \'" + NEGATIVE_SECONDARY + "\' is located in either no longer exists, or isn't loaded");
 			return true;
 		}
 	}
 }
