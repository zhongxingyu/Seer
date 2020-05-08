 package com.centralviewgaming.mccustomplugin;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class MinecartEjectListener implements Listener{
 	
     public MinecartEjectListener(CVGCustomPlugin plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onSignCreate(SignChangeEvent e){
     		if(e.getLine(0).equalsIgnoreCase("(CVG EJECT)")){
     			PermissionUser user = PermissionsEx.getUser(e.getPlayer());
     			String[] groups = user.getGroupsNames();
    			List <String> groupList = Arrays.asList(groups);
    			if(groupList.contains("admins")){
            		e.getBlock().breakNaturally();
             		e.getPlayer().sendMessage(ChatColor.RED + "RESTRICTED SIGN: CentralView Staff Only Sign");
     			}
     		}
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onVehicleMoveEvent(VehicleMoveEvent event){
     		if(event.getVehicle().getType() == EntityType.MINECART){
     			if(event.getVehicle().getPassenger() != null){
     				Player player = (Player) event.getVehicle().getPassenger();
     				Location loc = event.getVehicle().getLocation();
     				loc.setY(loc.getY() - 2);
     				if(loc.getBlock().getState() instanceof Sign){
     					Sign s = (Sign) loc.getBlock().getState();
     					if(s.getLine(0).toString().equals("(CVG EJECT)")){
     						event.getVehicle().eject();
     						String[] xyz = s.getLine(1).split("\\s+");
     						Double telX = Double.parseDouble(xyz[0]) + .5;
     						Double telY = Double.parseDouble(xyz[1]);
     						Double telZ = Double.parseDouble(xyz[2]) + .5;
     						Float telP = Float.parseFloat(s.getLine(2));
     						Location tele = new Location(Bukkit.getWorld(player.getWorld().getName()), telX, telY, telZ, telP, 0);
     						player.teleport(tele);
     					}
     				}
     			}
     		}
     	}
 }
