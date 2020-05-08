 package me.masterodie.craftStats;
 

 import java.util.Date;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.*;
 
 public class craftStatsPlayerListener extends PlayerListener {
 
 	public static craftStats plugin;
 	private craftStatsSqlHandler sql;
 	private Date playerLoginTime;
 	private Date playerLogoutTime;
 		
 	public craftStatsPlayerListener(craftStats instance) {
 		plugin = instance;
 	}
 	
 	private String playerLocationString;
 	
 	public void playerLocation(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Location playerLocation = player.getLocation();
 		
 		playerLocationString = "Deine Koordinaten sind: X:" + playerLocation.getX() + ", Y:" + playerLocation.getY() + ", Z:" + playerLocation.getZ();
 	}
 	
 	public String getPlayerLocationString() {
 		
 		String output = playerLocationString;		
 		return output;
 	}
 	
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		
 		playerLoginTime = new Date();
 		
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 				
 		
 		sql = new craftStatsSqlHandler(plugin);
 		sql.mySqlInsertQuery(player.getName(), 1, "is_online", "player");
		sql.mySqlInsertQuery(playerName, this.playerLoginTime.getTime(), "last_login", "player");
 					
		plugin.log.info("Player: " + playerName + " connected from " + player.getAddress().getHostName() + " at " + this.playerLoginTime.getTime() );
 	}
 	
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		
 		playerLogoutTime = new Date();
 		long timeOnline = this.playerLogoutTime.getTime() - this.playerLoginTime.getTime();
 		
 		String group[] = craftStatsPlayerListener.plugin.permissionHandler.getGroups(event.getPlayer().getWorld().getName(), event.getPlayer().getName());
 		String groups = "";
 		
		for(int i = 0; i <= group.length; i++) {
 			if(i == 0) 
 				groups = group[i];
 			else
 			    groups = groups + ", " + group[i];
 			
 		}
 		
 		
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 				
 		sql = new craftStatsSqlHandler(plugin);
 		sql.mySqlInsertQuery(playerName, 0, "is_online", "player");
 		sql.mySqlInsertQuery(playerName, timeOnline, "time_online", "player");
 		sql.mySqlInsertQuery(playerName, groups, "permissions_groups", "player");
 	
 		plugin.log.info("Player: " + playerName + " disconnected at: " + this.playerLogoutTime.getTime() + " Time Online: " + timeOnline + "ms");
 	}
 	
 	public void onPlayerMove (PlayerMoveEvent event) {
 		
 		
 		Location from = event.getFrom();
 		Location to = event.getTo();
 		
 		double distance = calculateDistance(from, to, true);
 		
 		sql = new craftStatsSqlHandler(plugin);	
 		sql.mySqlInsertQuery(event.getPlayer().getName(), distance, "meters_walked", "player");
 	}
 	
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		
 		int i = event.getItemDrop().getItemStack().getTypeId();
 		String p = event.getPlayer().getName();
 		
 		sql = new craftStatsSqlHandler(plugin);
 		if(i > 0 && i <= 128) {
 			sql.mySqlInsertQuery(p, 1, "dropped_" + i, "block");
 		} else {
 			sql.mySqlInsertQuery(p, 1, "dropped_" + i, "item");
 		}
 	}
 	
 
 	
 	private double calculateDistance(Location from, Location to, boolean height) {
 		if(from.getWorld() != to.getWorld()) {
 			return 0;
 		}
 		
 		double x1 = from.getX();
 		double x2 = to.getX();
 		
 		double y1 = from.getY();
 		double y2 = to.getY();
 		
 		double z1 = from.getZ();
 		double z2 = to.getZ();
 		
 		double distance = 0;
 		
 		if(height) {
 			distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2)) ;						
 		} else {
 			distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((z1 - z2), 2));
  		}
 		
 		return distance;	
 	}
 }
