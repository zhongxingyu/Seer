 package me.boecki.SignCodePad.event;
 
 import me.boecki.SignCodePad.MD5;
 import me.boecki.SignCodePad.SignCodePad;
 import me.boecki.SignCodePad.SignLoc;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 public class SignCreate extends BlockListener {
     SignCodePad plugin;
     
     public SignCreate(SignCodePad pplugin) {
         plugin = pplugin;
     }
 
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.getBlock().getTypeId() == Material.WALL_SIGN.getId()) {
             if (plugin.hasSetting(event.getBlock().getLocation())) {
             	if(plugin.getSetting(event.getBlock().getLocation(), "Owner") == event.getPlayer() || plugin.hasPermission(event.getPlayer(), "SignCodePad.masterdestroy")){
                 plugin.removeSetting(event.getBlock().getLocation());
                 event.getPlayer().sendMessage("CodePad Destroyed.");
                 plugin.save();
             	}
             	else {
             		event.getPlayer().sendMessage("You do not own this SignCodePad.");
             		event.setCancelled(true);
             	}
             }
         }
     }
 
     private Block getBlockBehind(Sign sign) {
         Location signloc = sign.getBlock().getLocation();
         double x = -1;
         double y = signloc.getY();
         double z = -1;
 
         switch ((int) sign.getRawData()) {
         //Westen
         case 2:
             x = signloc.getX();
             z = signloc.getZ() + 2;
 
             break;
 
         //Osten
         case 3:
             x = signloc.getX();
             z = signloc.getZ() - 2;
 
             break;
 
         //Sden
         case 4:
             x = signloc.getX() + 2;
             z = signloc.getZ();
 
             break;
 
         //Norden
         case 5:
             x = signloc.getX() - 2;
             z = signloc.getZ();
 
             break;
         }
 
         return sign.getBlock().getWorld()
                    .getBlockAt(new Location(sign.getBlock().getWorld(), x, y, z));
     }
 
     public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[SignCodePad]")) {
         	if(!plugin.hasPermission(event.getPlayer(), "SignCodePad.use")){
         		event.getPlayer().sendMessage("You do not have Permission to do that.");
         		event.getBlock().setType(Material.AIR);
                 event.getBlock().getLocation().getWorld()
                     .dropItem(event.getBlock().getLocation(),
                     new ItemStack(Material.SIGN, 1));
         		return;
         	}
             if (event.getLine(1).equalsIgnoreCase("Cal")) {
             	plugin.CalLoc.put(event.getPlayer().getName(), new SignLoc(event.getBlock().getLocation()));
             	plugin.CalSaverList.put(event.getPlayer().getName(), new CalSaver());
                 event.setLine(0, "+              ");
                 event.setLine(1, "Press the");
                 event.setLine(2, "cross");
                 event.setLine(3, "");
             } else {
             	if(!plugin.hasPermission(event.getPlayer(), "SignCodePad.create")){
             		event.getPlayer().sendMessage("You do not have Permission to do that.");
                     event.getBlock().setType(Material.AIR);
                     event.getBlock().getLocation().getWorld()
                         .dropItem(event.getBlock().getLocation(),
                         new ItemStack(Material.SIGN, 1));
             		return;
             	}
                 boolean Worked = true;
                 int Number = 0;
 
                 try {
                     Number = Integer.parseInt(event.getLine(1));
                 } catch (Exception e) {
                     Worked = false;
                 }
 
                 if (Worked) {
                     if ((Number < 10000) && (Number > 999)) {
                         MD5 md5 = new MD5(Number);
 
                         if (md5.isGen()) {
                             if (!Zeiledrei(event.getLine(2), event) ||
                                     !Zeilevier(event.getLine(3), event)) {
                                 return;
                             }
 
                             plugin.setSetting(event.getBlock().getLocation(),
                                 "MD5", md5.getValue());
                             plugin.setSetting(event.getBlock().getLocation(), "Owner",
                                     event.getPlayer().getName());
 
                             event.setLine(0, "1 2 3 |       ");
                             event.setLine(1, "4 5 6 | ----");
                             event.setLine(2, "7 8 9 |  <<- ");
                             event.setLine(3, "* 0 # |  OK  ");
 
                             Block block = event.getPlayer().getWorld().getBlockAt((Location) plugin.getSetting(event.getBlock().getLocation(), "OK-Location"));
                             if(block.getType() != Material.AIR&&!plugin.hasPermission(event.getPlayer(),"SignCodePad.replaceblock")){
                             	event.getPlayer().sendMessage("OK-Target not Air.");
                             	if (plugin.hasSetting(event.getBlock().getLocation())) {
                                     plugin.removeSetting(event.getBlock().getLocation());
                                     plugin.save();
                                 }
                             	return;
                             }
                             block.setTypeId(Material.TORCH.getId());
                             if (((Location) plugin.getSetting(event.getBlock().getLocation(),"Error-Location")).getY() >= 0) {
                                 Block blockb = event.getPlayer().getWorld().getBlockAt((Location) plugin.getSetting(event.getBlock().getLocation(), "Error-Location"));
                                 if(blockb.getType() != Material.AIR&&!plugin.hasPermission(event.getPlayer(),"SignCodePad.replaceblock")){
                                 	event.getPlayer().sendMessage("Error-Target not Air.");
                                 	if (plugin.hasSetting(event.getBlock().getLocation())) {
                                         plugin.removeSetting(event.getBlock().getLocation());
                                         plugin.save();
                                     }
                                 	return;
                                 }
                                 blockb.setTypeId(Material.TORCH.getId());
                             }
                             plugin.save();
                             event.getPlayer().sendMessage("CodePad Created.");
                         } else {
                             event.getPlayer()
                                  .sendMessage("Internal Error (MD5).");
                         }
                     } else {
                         event.getPlayer().sendMessage("Wrong Code.");
                     }
                 } else {
                     event.getPlayer().sendMessage("Wrong Code.");
                 }
             }
         }
     }
 
     private boolean Zeiledrei(String line, SignChangeEvent event) {
         String[] linesplit = line.split(";");
 
         if (linesplit[0] != "" && linesplit[0].length()>0) {
             plugin.setSetting(event.getBlock().getLocation(), "OK-Delay",
                 linesplit[0]);
         } else {
             plugin.setSetting(event.getBlock().getLocation(), "OK-Delay", 3);
         }
 
         if ((linesplit.length > 1) && (linesplit[1] != "")) {
             String[] loc = linesplit[1].split(",");
 
             if (loc.length < 3) {
                 event.getPlayer()
                      .sendMessage("Error in Line 3. (Destination Format) ("+loc.length+")");
 
                 return false;
             }
 
             if ((loc[0] == "") || (loc[1] == "") || (loc[2] == "")) {
                 event.getPlayer()
                      .sendMessage("Error in Line 3. (Destination Format)");
 
                 return false;
             }
 
             Location blockloc = getBlockBehind((Sign) event.getBlock().getState())
                                     .getLocation();
             double x = blockloc.getX();
             double y = blockloc.getY() + Integer.parseInt(loc[1]);
             double z = blockloc.getZ();
 
             switch ((int) ((Sign) event.getBlock().getState()).getRawData()) {
             //Westen
             case 2:
                 x += (Integer.parseInt(loc[2]) * -1);
                 z += Integer.parseInt(loc[0]);
 
                 break;
 
             //Osten
             case 3:
                 x += Integer.parseInt(loc[2]);
                 z += (Integer.parseInt(loc[0]) * -1);
 
                 break;
 
             //Sden
             case 4:
                 x += Integer.parseInt(loc[0]);
                 z += Integer.parseInt(loc[2]);
 
                 break;
 
             //Norden
             case 5:
                 x += (Integer.parseInt(loc[0]) * -1);
                 z += (Integer.parseInt(loc[2]) * -1);
 
                 break;
             }
 
             plugin.setSetting(event.getBlock().getLocation(), "OK-Location",
                 new Location(event.getBlock().getWorld(), x, y, z));
         } else {
             plugin.setSetting(event.getBlock().getLocation(), "OK-Location",
                 getBlockBehind((Sign) event.getBlock().getState()).getLocation());
         }
 
         return true;
     }
 
     private boolean Zeilevier(String line, SignChangeEvent event) {
         String[] linesplit = line.split(";");
 
         if (linesplit[0] != "" && linesplit[0].length()>0) {
             plugin.setSetting(event.getBlock().getLocation(), "Error-Delay",
                 linesplit[0]);
         } else {
             plugin.setSetting(event.getBlock().getLocation(), "Error-Delay", 3);
         }
 
         if ((linesplit.length > 1) && (linesplit[1] != "")) {
             String[] loc = linesplit[1].split(",");
 
             if (loc.length < 3) {
                 event.getPlayer()
                      .sendMessage("Error in Line 4. (Destination Format) ("+loc.length+")");
 
                 return false;
             }
 
             if ((loc[0] == "") || (loc[1] == "") || (loc[2] == "")) {
                 event.getPlayer()
                 .sendMessage("Error in Line 4. (Destination Format)");
 
                 return false;
             }
 
             Location blockloc = getBlockBehind((Sign) event.getBlock().getState())
                                     .getLocation();
             double x = blockloc.getX();
             double y = blockloc.getY() + Integer.parseInt(loc[1]);
             double z = blockloc.getZ();
 
             switch ((int) ((Sign) event.getBlock().getState()).getRawData()) {
             //Westen
             case 2:
                 x += (Integer.parseInt(loc[2]) * -1);
                 z += Integer.parseInt(loc[0]);
 
                 break;
 
             //Osten
             case 3:
                 x += Integer.parseInt(loc[2]);
                 z += (Integer.parseInt(loc[0]) * -1);
 
                 break;
 
             //Sden
             case 4:
                 x += Integer.parseInt(loc[0]);
                 z += Integer.parseInt(loc[2]);
 
                 break;
 
             //Norden
             case 5:
                 x += (Integer.parseInt(loc[0]) * -1);
                 z += (Integer.parseInt(loc[2]) * -1);
 
                 break;
             }
 
             plugin.setSetting(event.getBlock().getLocation(), "Error-Location",
                 new Location(event.getBlock().getWorld(), x, y, z));
         } else {
             plugin.setSetting(event.getBlock().getLocation(), "Error-Location",
                 new Location(event.getBlock().getWorld(), 0, -1, 0));
         }
 
         if ((linesplit.length > 2) && (linesplit[2] != "")) {
             String sCount = linesplit[2];
             int Count = Integer.parseInt(sCount);
             plugin.setSetting(event.getBlock().getLocation(), "Error-Count",
                 Count);
         } else {
             plugin.setSetting(event.getBlock().getLocation(), "Error-Count", 0);
         }
 
         return true;
     }
 }
