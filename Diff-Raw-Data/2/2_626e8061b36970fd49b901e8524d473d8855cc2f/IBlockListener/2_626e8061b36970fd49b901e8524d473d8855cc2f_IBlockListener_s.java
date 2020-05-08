 package me.greatman.plugins.inn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 
 public class IBlockListener extends BlockListener{
 	
 	private final Inn plugin;
     public IBlockListener(Inn instance) {
         plugin = instance;
     }
     @Override
 	public void onBlockBreak(BlockBreakEvent event){
     	if (event.getBlock().getType() == Material.WOODEN_DOOR){
     		int x,y,z;
         	Location loc = event.getBlock().getLocation();
         	x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
        	if (Inn.doorAlreadyExists(x,y,z) || Inn.doorAlreadyExists(x, y-1, z) || Inn.doorAlreadyExists(x,y+1,z) && Inn.getOwner(x,y,z).equalsIgnoreCase(event.getPlayer().getName()) || IPermissions.permission(event.getPlayer(), "inn.bypass", event.getPlayer().isOp())){
         		event.getPlayer().sendMessage(ChatColor.RED + "[Inn] You doesn't own this door!");
         		event.setCancelled(true);
         	}else{
         		String query = "DELETE FROM doors WHERE x=" + x + " AND y=" + y + " AND z=" + z +"";
         		Inn.manageSQLite.deleteQuery(query);
         		if (Inn.doorAlreadyExists(x,y-1,z)){
         			int y2 = y - 1;
         			query = "DELETE FROM doors WHERE x=" + x + " AND y=" + y2 + " AND z=" + z +"";
             		Inn.manageSQLite.deleteQuery(query);
         		}else if (Inn.doorAlreadyExists(x,y+1,z)){
         			int y2 = y + 1;
         			query = "DELETE FROM doors WHERE x=" + x + " AND y=" + y2 + " AND z=" + z +"";
             		Inn.manageSQLite.deleteQuery(query);
         		}
         		event.getPlayer().sendMessage(ChatColor.RED + "[Inn] Door unregistered!");
         	}
     	}
     	
     }
 }
