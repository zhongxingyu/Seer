 package me.greatman.plugins.inn;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 import com.nijikokun.register.payment.Method.MethodAccount;
 
 public class IPlayerListener extends PlayerListener {
 	private final Inn plugin;
     public IPlayerListener(Inn instance) {
         plugin = instance;
     }
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         String playerName = player.getName();
         if (!plugin.getPlayerData().containsKey(playerName)) {
             plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
         }
 
         // If our user is select & is not holding an item, selection time
         if (plugin.getPlayerData().get(playerName).isSelecting() && player.getItemInHand().getType() == Material.AIR && event.getClickedBlock().getType() == Material.WOODEN_DOOR) {
             int x, y, z;
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
             PlayerData pData = plugin.getPlayerData().get(playerName);
             
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
             	
             	
             	if (doorAlreadyExists(x,y,z)){
             		player.sendMessage(ChatColor.RED + "This door is already registered!");
             		return;
             	}
                 int[] xyz = { x, y, z };
                 pData.setPositionA(xyz);
                 player.sendMessage(ChatColor.DARK_AQUA + "Door selected");
                 
 	            if(pData.getPositionA() != null) {
 	                   player.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.WHITE + "/inn create [Price]" + ChatColor.DARK_AQUA + ", if you're happy with your selection, otherwise keep selecting!");
 	            }
                 
         
             }
         //Are we trying to open a door?
         }else if (event.getClickedBlock().getType() == Material.WOODEN_DOOR){
         	if (IPermissions.permission(plugin.getPlayer(player), "inn.bypass", plugin.getPlayer(player).isOp()))
         			return;
         	int x, y, z;
         	ILogger.info("Where");
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
            ILogger.info("Where");
             if (doorAlreadyExists(x,y,z)){
            	ILogger.info("Where" + getOwner(x,y,z) + playerName);
            	if (getOwner(x,y,z) == playerName){
             		return;
            	}
            	ILogger.info("Where");
             	int price = getDoorPrice(x,y,z);
            	String owner = getOwner(x,y,z);
            	ILogger.info("Where");
             	MethodAccount playerAccount = plugin.Method.getAccount(playerName);
             	if (playerAccount.hasEnough(price)){
            		ILogger.info("Where");
             		playerAccount.subtract(price);
             		MethodAccount playerAccount2 = plugin.Method.getAccount(owner);
             		playerAccount2.add(price);
            		ILogger.info("Where");
             		player.sendMessage(ChatColor.DARK_AQUA + "You are entering " + owner + " inn room");
             	}else
             		event.setCancelled(true);
             }
         }
     }
     public boolean doorAlreadyExists(int x, int y, int z){
     	String query = "SELECT id FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     	ResultSet result = Inn.manageSQLite.sqlQuery(query);
     	int id = 0;
     	try {
 			if (result.next()){
 				id = result.getInt("id");
 				if (id != 0)
 					return true;
 				else
 					return false;
 			}else
 				return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
     	return false;
     }
     public int getDoorPrice(int x, int y, int z){
     	if (doorAlreadyExists(x,y,z)){
     		String query = "SELECT price FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getInt("price");
 				}else
 					return -1;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}else
     		return -1;
     return -1;
     }
     public String getOwner(int x, int y, int z){
     	if (doorAlreadyExists(x,y,z)){
     		String query = "SELECT owner FROM doors WHERE x="+ x + " AND y=" + y + " AND z=" + z;
     		ResultSet result = Inn.manageSQLite.sqlQuery(query);
     		try {
 				if (result.next()){
 					return result.getString("owner");
 				}else
 					return "";
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
     	}else
     		return "";
     return "";
     }
 }
