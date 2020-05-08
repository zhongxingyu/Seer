 //Copyright (C) 2011-2012 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.xcp23x.restockit;
 
 import java.util.List;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 
 public class listeners implements Listener {
     
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK){ //If they right click...
             Block chest = event.getClickedBlock();
             if(chest.getType() == Material.CHEST || chest.getType() == Material.DISPENSER) {  //And it's a chest or dispenser...
                 if(chestUtils.isRIchest(chest)) { //And it's a RestockIt chest...
                     Block sign = signUtils.getSignFromChest(chest);
                     String line2 = ((Sign)sign.getState()).getLine(2);
                     String line3 = ((Sign)sign.getState()).getLine(3);
                     eventTriggered(chest, line2, line3, sign); //Pass relevant lines to eventTriggered()
                 }
             }
         }
     }
     
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         Block sign = event.getBlock();
         Player player = event.getPlayer();
         String[] lines = event.getLines();
         String line0 = lines[0], line1 = lines[1], line2 = lines[2], line3 = lines[3]; //Get the lines
         
         
         //If the player forgot the blank line, correct it for them
         if(signUtils.isRIsign(line0)) {
             event.setLine(3, line2);
             event.setLine(2, line1);
             event.setLine(1, line0);
             event.setLine(0, "");
             lines = event.getLines();
             line0 = lines[0];
             line1 = lines[1];
             line2 = lines[2];
             line3 = lines[3]; 
         }
         
         if(signUtils.isRIsign(line1)){
             
             if(chestUtils.getChestFromSign(sign) == null){ //There's no chest there
                 signUtils.dropSign(sign);
                 playerUtils.sendPlayerMessage(player, 6);
                 return;
             }
             
             if(!playerUtils.hasContainerPermissions(player, chestUtils.getChestFromSign(sign), line2)){ //They don't have permission
                 signUtils.dropSign(sign);
                 playerUtils.sendPlayerMessage(player, 2, chestUtils.getChestFromSign(sign).getType().name().toLowerCase());
                 return;
             }
             
             if(chestUtils.isAlreadyRIchest(sign)) { //It's already a RestockIt chest
                 signUtils.dropSign(sign);
                 playerUtils.sendPlayerMessage(player, 1);
                 return;
             }
             
             if(signUtils.isIncinerator(line2)) { //It's an incinerator, we can go straight to eventTriggered()
                 eventTriggered(chestUtils.getChestFromSign(sign),line2,line3,sign);
                 return;
             }
             
             if(signUtils.line2hasErrors(line2, player)) { //Errors were found (no need to tell the player, they've already been told)
                 signUtils.dropSign(sign);
                 return;
             }
             
             //Check Blacklist
             List<String> blacklist = RestockIt.plugin.getConfig().getStringList("blacklist");
             int size = blacklist.size();
             for(int x = 0; x<size; x++) {
                 String item = blacklist.get(x);
                 if(signUtils.getType(item) <= 0) {
                     RestockIt.log.warning("[RestockIt] Error in blacklist: " + item + "not recognised - Ignoring");
                 } else if ((signUtils.getType(line2) == signUtils.getType(item)) && !playerUtils.hasBlacklistPermissions(player)){
                     playerUtils.sendPlayerMessage(player, 7, signUtils.getMaterial(item).name());
                     signUtils.dropSign(sign);
                    return;
                 }
             }
             
             eventTriggered(chestUtils.getChestFromSign(sign), line2, line3, sign);
         }
     }
     
     @EventHandler
     public void onBlockDispense(BlockDispenseEvent event) { //For auto-refilling dispensers
         Block block = event.getBlock();
         if(block.getType() == Material.DISPENSER) {   //Make sure the dispensable dispensee was dispensed by a dispenser
             if(chestUtils.isRIchest(block)) {
                 Block sign = signUtils.getSignFromChest(block);
                 String line2 = ((Sign)sign.getState()).getLine(2);
                 String line3 = ((Sign)sign.getState()).getLine(3);
                 eventTriggered(block, line2, line3, sign);
             }
         }
     }
     
     private void eventTriggered(Block chest, String line2, String line3, Block sign){
         if(signUtils.isDelayedSign(line3)){
             scheduler.startSchedule(sign, signUtils.getPeriod(line3)); //If it's a delayed sign, start a schedule
         } else chestUtils.fillChest(chest, line2); //If not, RestockIt.
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         Block block = event.getBlock();
         Material mat = block.getType();
         if(mat == Material.CHEST || mat == Material.DISPENSER) {
             Block sign = signUtils.getSignFromChest(block);
             if(sign == null) return;
             String line = ((Sign)sign.getState()).getLine(1);
             if(signUtils.isRIsign(line)) {
                 Inventory inv = chestUtils.getInventory(block); //Stops chests bursting everywhere when broken
                 inv.clear();
                 scheduler.stopSchedule(sign); //Stop any schedules running for this block
             }
             signUtils.dropSign(sign); //Remove the sign
         }
         else if(mat == Material.WALL_SIGN|| mat == Material.SIGN_POST) {
             Block sign = block;
             String line = ((Sign)sign.getState()).getLine(1);
             if(signUtils.isRIsign(line)) {
                 Block chest = chestUtils.getChestFromSign(sign);
                 if(chest == null) return;
                 Inventory inv = chestUtils.getInventory(chest);
                 inv.clear(); //Empty the chest
                 scheduler.stopSchedule(block); //Stop any schedules for this block
             }
         }
         
     }
 }
