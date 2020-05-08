 package org.xcp23x.RestockIt;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class RIbl extends BlockListener {
     public static RestockIt plugin;
     public RIbl(RestockIt instance) {
         plugin = instance;
     }
     
     @Override
     public void onSignChange(SignChangeEvent event) {
         Location loc = event.getBlock().getLocation();
         Player player = event.getPlayer();
         int x = loc.getBlockX();
         int y = loc.getBlockY() - 1;
         int z = loc.getBlockZ();
         Block blockBelow = player.getWorld().getBlockAt(x,y,z);
         String line1 = event.getLine(1);
         String line2 = event.getLine(2);
         
         if(checkCommand(line1)) if(checkPermissions(player, blockBelow, loc)) if(checkBlockBelow(player, blockBelow, loc)) checkItem(line2, loc, player);
     }
     
     public static boolean checkCommand(String line1) {
         if (line1.equalsIgnoreCase("Full Chest") || line1.equalsIgnoreCase("Full Dispenser") ||
                line1.equalsIgnoreCase("RestockIt") || line1.equalsIgnoreCase("Restock It")) return true;
         return false;
     }
     
     public boolean checkPermissions(Player player, Block block, Location loc) {
         Material material = block.getType();
         if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
             PermissionManager perms = PermissionsEx.getPermissionManager();
             if ((!perms.has(player, "restockit.chest")) && (material == Material.CHEST)) {
                 dropSign(loc, player.getWorld());
                 player.sendMessage("[RestockIt] You do not have permission to make a RestockIt chest.");
                 return false;
             } else if ((!perms.has(player, "restockit.dispenser")) && (material == Material.DISPENSER)) {
                 dropSign(loc, player.getWorld());
                 player.sendMessage("[RestockIt] You do not have permission to make a RestockIt dispenser.");
                 return false;
             } else if ((!perms.has(player, "restockit.chest")) && ((!perms.has(player, "restockit.dispenser")))) {
                 dropSign(loc, player.getWorld());
                 player.sendMessage("[RestockIt] You do not have permission use RestockIt");
                 return false;
             }
         } else if (!player.isOp()) {
             dropSign(loc, player.getWorld());
             if(material == Material.CHEST) player.sendMessage("[RestockIt] You must be an op to make a RestockIt chest");
             if(material == Material.DISPENSER) player.sendMessage("[RestockIt] You must be an op to make a RestockIt dispenser");
             return false;
         }
         return true;
     }
     
     public boolean checkBlockBelow(Player player, Block block, Location loc) {
         if(block.getType() == Material.CHEST) return true;
         else if(block.getType() == Material.DISPENSER) return true;
         player.sendMessage("[RestockIt] No chest or dispenser was found");
         player.sendMessage("[RestockIt] Ensure you placed the sign directly above the chest or dispenser");
         dropSign(loc, player.getWorld());
         return false;
     }
     
     public void checkItem(String line2, Location loc,Player player) {
         World world = player.getWorld();
         int ID = checkID(line2);
         if (ID == 0) {
             player.sendMessage("[RestockIt] \"" + line2 + "\"" + " could not be found");
             player.sendMessage("[RestockIt] If you have trouble with name IDs, use the number instead");
             dropSign(loc, world);
         } else if (ID == -1){
             player.sendMessage("[RestockIt] Item ID " + line2 + " could not be found");
             player.sendMessage("[RestockIt] Try again, or enter its name in UPPER CASE");
             dropSign(loc, world);
         } else if (ID == -2){
             player.sendMessage("[RestockIt] There was a problem with the damage value");
             player.sendMessage("[RestockIt] Make sure you used an ID number with the damage value");
             dropSign(loc, world);
         }
     }
     
     public static int checkID(String line) {
         if(line.contains(":")) {
            line = line.split(":")[0];
             try{
                 String split = line.split(":")[1];
                 Short damage = Short.parseShort(split);
             } catch(ArrayIndexOutOfBoundsException ex) {
               return -2; 
             }
         }
         try {
             int item = Integer.parseInt(line);
             if ((item != 0) && (Material.getMaterial(item) != null)) return 1; //It's an int ID
             return -1; //It's an int, but not an ID
         } catch(NumberFormatException ex) {
             if ((!"AIR".equals(line)) && (line != null) && (Material.getMaterial(line) != null)) return 2; //It's a string ID
         }
         return 0; //Not an int, not an ID
     }
     
     public void dropSign(Location loc, World world) {
         world.getBlockAt(loc).setType(Material.AIR);
         world.dropItem(loc, new ItemStack(Material.SIGN,1));
     }
     
 }
