 package com.kmcguire.KRandomTeleport;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class P extends JavaPlugin implements Listener {
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     @Override
     public void onDisable() {
         
     }
     
     @EventHandler 
     public void onSignChange(SignChangeEvent event) {
         Player          player;
         
         player = event.getPlayer();
         
         if (player == null) {
             return;
         }
         
         if (event.getLine(0) == null || event.getLine(1) == null || event.getLine(2) == null || event.getLine(3) == null) {
             return;
         }
         
         if (event.getLine(0).equals("RandomTeleport") && event.getLine(1).equals("[Click Me]")) {
             if (!player.isOp()) {
                 player.sendMessage("[KRandomTeleport] You must an OP to set the first line to RandomTeleport and the second line to [Click Me].");
                 event.setCancelled(true);
                 return;
             }
             
             if (event.getLine(1) == null) {
                 player.sendMessage("[KRandomTeleport] The second line must be a number which is the radius.");
                 event.setCancelled(true);
                 return;
             }
             try {
                 Long.parseLong(event.getLine(2));
                 Long.parseLong(event.getLine(3));
             } catch (NumberFormatException ex) {
                 player.sendMessage("[KRandomTeleport] The third line must be a *number* which is the maximum radius.");
                 player.sendMessage("[KRandomTeleport] The forth line must be a *number* which is the minimum radius.");
                 player.sendMessage("[KRandomTeleport] One of the lines or both is not a valid whole number!");
                 event.setCancelled(true);
                 return;
             }
             player.sendMessage("[KRandomTeleport] The sign has been registered.");
             return;
         }
         
         return;
     }
     
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block           block;
         Sign            sign;
         int             minradius;
         int             maxradius;
         Player          player;
         Location        location;
         int             cycles;
         
         player = event.getPlayer();
         
         block = event.getClickedBlock();
         
         if ((player == null) || (block == null)) {
             return;
         }
         
         if (!(block.getState() instanceof Sign)) {
             return;
         }
         
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
             return;
         }
         
         sign = (Sign)block.getState();
         
         if (sign.getLine(0).equals("RandomTeleport") && sign.getLine(1).equals("[Click Me]")) {
             try {
                 maxradius = Integer.parseInt(sign.getLine(2));
                 minradius = Integer.parseInt(sign.getLine(3));
             } catch (NumberFormatException ex) {
                 player.sendMessage("[KRandomTeleport] The radius, second line, can not be interpreted as a number.");
                 return;
             }
             
             cycles = 0;
             do {
                 ++cycles;
                 location = getRandomLocation(
                         player.getWorld(), 
                         player.getLocation().getBlockX(),
                         player.getLocation().getBlockZ(),
                         minradius, maxradius, true
                 );
 
                 if (location != null) {
                    teleportPlayer(player, location);
                     player.sendMessage(String.format("[KRandomTeleport] The calculation took %d cycles!", cycles));
                     player.sendMessage("[KRandomTeleport] Hold on you are being teleported!");
                     return;
                 }
             } while (location == null);
             
             player.sendMessage("[KRandomTeleport] Opps, try again. I could not grab a good location!");
             return;
         }
         // no not the sign we are looking for
         return;
     }
     
    public void teleportPlayer(Player p, Location l) {
        Chunk           chunk;
        Location        nl;
        int             x, z, y;
        Block           block;
        
        chunk = l.getWorld().getChunkAt(l);
        
        x = l.getBlockX() & 0xf;
        z = l.getBlockZ() & 0xf;
        y = l.getBlockY();
        
        for (y = l.getBlockY(); y > -1; --y) {
            block = chunk.getBlock(x, y, z);
            if (!block.isEmpty()) {
                nl = new Location(l.getWorld(), l.getBlockX(), y, l.getBlockZ());
                p.sendBlockChange(nl, block.getTypeId(), block.getData());
                break;
            }
        }
        
        p.teleport(l);
    }    
    
     public Location getRandomLocation(World world, int ox, int oz, int min, int max, boolean noWater) {
         int             x, z;
         int             bx, bz;
         Chunk           chunk;
         double          vx, vz, _vx, _vz;
 
         vx = Math.random() - 0.5;
         vz = Math.random() - 0.5;
 
         _vx = vx / Math.sqrt(vx*vx+vz*vz);
         _vz = vz / Math.sqrt(vx*vx+vz*vz);
 
         x = (int)(_vx * (double)min + _vx * Math.random() * (double)max);
         z = (int)(_vz * (double)min + _vz * Math.random() * (double)max);
 
         x += ox;
         z += oz;
 
         chunk = world.getChunkAt(x >> 4, z >> 4);
 
         bx = x & 0xf;
         bz = z & 0xf;
 
         for (int y = 255; y > -1; --y) {
             if (chunk.getBlock(bx, y, bz).isEmpty() && chunk.getBlock(bx, y + 1, bz).isEmpty() && !chunk.getBlock(bx, y - 1, bz).isEmpty()) {
                 if (noWater && chunk.getBlock(bx, y - 1, bz).isLiquid()) {
                     return null;
                 }
                 return new Location(world, x, y + 0.01, z);
             }
         }
 
         return null;
     }
 }
